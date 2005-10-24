/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.*;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A singleton CVS manager class, center of CVS module. Use {@link #getInstance()} to get access
 * to CVS module functionality.
 * 
 * @author Maros Sandor
 */
public class CvsVersioningSystem {

    private static CvsVersioningSystem instance;
    
    public static final String FILENAME_CVSIGNORE = ".cvsignore";
    public static final String FILENAME_CVS = "CVS";

    public static final Object EVENT_PARAM_CHANGED = new Object();
    public static final Object PARAM_BATCH_REFRESH_RUNNING = new Object();

    private static final String FILENAME_CVS_REPOSITORY = FILENAME_CVS + "/Repository";
    
    /**
     * Extensions to be treated as text although MIME type may suggest otherwise.
     */ 
    private static final Set textExtensions = new HashSet(Arrays.asList(new String [] { "txt", "xml", "html", "properties", "mf", "jhm", "hs", "form" }));
    
    private final Map clientsCache = new HashMap();
    private final Map params = new HashMap();

    private GlobalOptions defaultGlobalOptions;
    private FileStatusCache fileStatusCache;

    private CvsLiteAdminHandler sah;
    private FilesystemHandler filesystemHandler;

    private Annotator annotator;

    private final Set   userIgnorePatterns = new HashSet();
    private boolean     userIgnorePatternsReset;
    private long        userIgnorePatternsTimestamp;

    public static synchronized CvsVersioningSystem getInstance() {
        if (instance == null) {
            instance = new CvsVersioningSystem();
            instance.init();
        }
        return instance;
    }

    private void init() {
        defaultGlobalOptions = CvsVersioningSystem.createGlobalOptions();
        defaultGlobalOptions.setCompressionLevel(3);
        MetadataAttic.cleanUp();
        sah = new CvsLiteAdminHandler();
        loadCache();
        filesystemHandler  = new FilesystemHandler(this);
        annotator = new Annotator(this);
        filesystemHandler.init();
        // HACK: FileStatusProvider cannot do it itself
        if (FileStatusProvider.getInstance() != null) {
            fileStatusCache.addVersioningListener(FileStatusProvider.getInstance());
            FileStatusProvider.getInstance().init();
        }
    }

    /**
     * Simple cache deserialization.
     */ 
    private void loadCache() {
        fileStatusCache = new FileStatusCache(this);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                fileStatusCache.cleanUp();
            }
        }, 3000);
    }

    private CvsVersioningSystem() {
    }

    public CvsFileTableModel getFileTableModel(Context context, int displayStatuses) {
        return new CvsFileTableModel(context, displayStatuses);
    }
    
    /**
     * Determines correct CVS client from the given cvs root.
     * 
     * @param cvsRoot root never <code>null</code>
     * @return
     */ 
    private ClientRuntime getClientRuntime(String cvsRoot) {
 
            cvsRoot.length();  // rise NPE

            ClientRuntime clientRuntime;
            synchronized(clientsCache) {
                clientRuntime = (ClientRuntime) clientsCache.get(cvsRoot);
                if (clientRuntime == null) {
                    clientRuntime = new ClientRuntime(cvsRoot);
                    clientsCache.put(cvsRoot, clientRuntime);
                }
            }
            return clientRuntime;
    }

    /**
     * Determines CVS root for the given command.
     * 
     * @param cmd a CVS command
     * @return CVSRoot the command will execute in
     * @throws NotVersionedException if the root cannot be determined (no CVS/Root file or unsupported command)
     */ 
    String detectCvsRoot(Command cmd) throws NotVersionedException {
        File [] files;

        if (cmd instanceof AddCommand) {
            AddCommand c = (AddCommand) cmd;
            files = c.getFiles();
        } else if (cmd instanceof BasicCommand) {
            BasicCommand c = (BasicCommand) cmd;
            files = c.getFiles();
        } else {
            throw new NotVersionedException("Cannot determine CVSRoot for command: " + cmd);
        }

        File oneFile = files[0];
        try {
            String cvsRoot = Utils.getCVSRootFor(oneFile);
            return cvsRoot;
        } catch (IOException e) {
            throw new NotVersionedException("Cannot determine CVSRoot for: " + oneFile);
        }

    }

    /**
     * Executes this command asynchronously, in a separate thread, and returns immediately. The command may
     * or may not execute immediately, depending on previous commands sent to the CVS client that may be
     * still waiting for execution.
     *  
     * @param cmd command to execute
     * @param mgr listener for events the command produces
     * @throws CommandException
     * @throws AuthenticationException
     */
    public RequestProcessor.Task post(Command cmd, ExecutorSupport mgr) throws CommandException,
            AuthenticationException, NotVersionedException, IllegalCommandException,
            IOException {
        return post(cmd, defaultGlobalOptions, mgr);
    }

    /**
     * Schedules given command for execution.
     * @param cmd
     * @param options Global options to use, may be set to null to use default options
     * @param mgr
     * @return already scheduled task
     * @throws IllegalCommandException if the command is not valid, e.g. it contains files that cannot be
     * processed by a single command (they do not have a common filesystem root OR their CVS Roots differ)
     */
    public RequestProcessor.Task post(Command cmd, GlobalOptions options, ExecutorSupport mgr) throws IllegalCommandException {
        ClientRuntime clientRuntime = getClientRuntime(cmd, options);
        RequestProcessor.Task task = clientRuntime.createTask(cmd, options != null ? options : defaultGlobalOptions, mgr);
        task.schedule(0);
        return task;
    }

    /**
     * Gets client runtime (a repository session).
     *
     * @return runtime never <code>null</code>
     */
    public ClientRuntime getClientRuntime(Command cmd, GlobalOptions options) {
        String root;
        if (options != null && options.getCVSRoot() != null) {
            root = options.getCVSRoot();
        } else {
            try {
                root = detectCvsRoot(cmd);
            } catch (NotVersionedException e) {
                if (options == null) return null;
                root = options.getCVSRoot();
            }
        }
        return getClientRuntime(root);
    }

    public FileStatusCache getStatusCache() {
        return fileStatusCache;
    }
    
    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
    
    /**
     * Checks if the file is ignored by CVS module. This method assumes that the file is managed so
     * if you do not know this beforehand, you have to call isManaged() first.
     *
     * @param file file to be tested
     * @return true, if the file is ignored by CVS, false otherwise.
     */
    boolean isIgnored(File file) {
        if (file.isDirectory()) {
            File cvsRepository = new File(file, FILENAME_CVS_REPOSITORY);
            if (cvsRepository.canRead()) return false;
        }
        String name = file.getName();
        Set patterns = new HashSet(Arrays.asList(CvsModuleConfig.getDefault().getIgnoredFilePatterns()));
        addUserPatterns(patterns);
        addCvsIgnorePatterns(patterns, file.getParentFile());

        for (Iterator i = patterns.iterator(); i.hasNext();) {
            Pattern pattern = (Pattern) i.next();
            if (pattern.matcher(name).matches()) return true;
        }

        return SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE;
    }
    
    private void addUserPatterns(Set patterns) {
        File userIgnores = new File(System.getProperty("user.home"), FILENAME_CVSIGNORE);
        long lm = userIgnores.lastModified();
        if (lm > userIgnorePatternsTimestamp || lm == 0 && userIgnorePatternsTimestamp > 0) {
            userIgnorePatternsTimestamp = lm;
            parseUserPatterns(userIgnores);
        }
        if (userIgnorePatternsReset) {
            patterns.clear();
        }
        patterns.addAll(userIgnorePatterns);
    }

    private void parseUserPatterns(File userIgnores) {
        userIgnorePatternsReset = false;
        userIgnorePatterns.clear();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(userIgnores));
            String s;
            while ((s = r.readLine()) != null) {
                if ("!".equals(s)) {
                    userIgnorePatternsReset = true;
                    userIgnorePatterns.clear();
                } else {
                    userIgnorePatterns.add(sh2regex(s));
                }
            }
        } catch (IOException e) {
            // user has invalid ignore list, ignore it
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
    }

    /**
     * Converts shell file patern to regex pattern.
     * 
     * @param s unix shell pattern
     * @return regex patterm
     */ 
    private static Pattern sh2regex(String s) {
        // TODO: implement full SH->REGEX convertor
        s = s.replaceAll("\\.", "\\\\.");
        s = s.replaceAll("\\*", ".*");
        return Pattern.compile(s);
    }

    /**
     * Tests whether a file or directory is managed by CVS. All files and folders that have a parent with CVS/Repository
     * file are considered managed by CVS. This method accesses disk and should NOT be routinely called.
     * 
     * @param file a file or directory
     * @return true if the file is under CVS management, false otherwise
     */ 
    boolean isManaged(File file) {
        if (file.isDirectory() && file.getName().equals(FILENAME_CVS)) return false;
        if (file.isFile()) file = file.getParentFile();
        for (; file != null; file = file.getParentFile()) {
            File repository = new File(file, FILENAME_CVS_REPOSITORY);
            if (repository.canRead()) return true;
        }
        return false;
    }

    private void addCvsIgnorePatterns(Set patterns, File file) {
        try {
            Set shPatterns = readCvsIgnoreEntries(file);
            for (Iterator i = shPatterns.iterator(); i.hasNext();) {
                String shPattern = (String) i.next();
                if ("!".equals(shPattern)) {
                    patterns.clear();
                } else {
                    patterns.add(sh2regex(shPattern));
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }
    
     public boolean isInCvsIgnore(File file) {
        try {
            return readCvsIgnoreEntries(file.getParentFile()).contains(file.getName());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
    public boolean isIgnoredFilename(File file) {
        if (FILENAME_CVS.equals(file.getName())) return true;
        return false;
    }
    

    public AdminHandler getAdminHandler() {
        return sah;
    }

    public Annotator getAnnotator() {
        return annotator;
    }
    
    public Object getParameter(Object key) {
        synchronized(params) {
            return params.get(key);
        }
    }

    public KeywordSubstitutionOptions getDefaultKeywordSubstitution(File file) {
        // TODO: Let user configure defaults
        return isText(file) ? KeywordSubstitutionOptions.DEFAULT : KeywordSubstitutionOptions.BINARY;    
    }
    
    public boolean isText(File file) {
        if (FILENAME_CVSIGNORE.equals(file.getName())) {
            return true;            
        }
        // honor Entries, only if this fails use MIME type, etc.
        try {
            Entry entry = sah.getEntry(file);
            if (entry != null) {
                return !entry.isBinary();
            }
        } catch (IOException e) {
            // ignore, probably new or nonexistent file
        }
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) != null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        if (fo.getMIMEType().startsWith("text")) {
            return true;            
        }
        // TODO: HACKS begin, still needed?
        return textExtensions.contains(fo.getExt());
    }
    
    public void setParameter(Object key, Object value) {
        Object old; 
        synchronized(params) {
            old = params.put(key, value);
        }
        if (old != value) listenerSupport.fireVersioningEvent(EVENT_PARAM_CHANGED, key);
    }

    /**
     * Adds all supplied files to 'cvsignore' file. They need not reside in the same folder.
     * 
     * @param files files to ignore
     */ 
    public void setIgnored(File[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                if (FILENAME_CVSIGNORE.equals(files[i].getName())) continue;
                addToCvsIgnore(files[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public void setNotignored(File[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                removeFromCvsIgnore(files[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
        
    private void addToCvsIgnore(File file) throws IOException {
        
        Set entries = readCvsIgnoreEntries(file.getParentFile());
        if (entries.add(file.getName())) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
        }
    }
    
    private void removeFromCvsIgnore(File file) throws IOException {
        Set entries = readCvsIgnoreEntries(file.getParentFile());
        if (entries.remove(file.getName())) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
        }
    }
    
    private Set readCvsIgnoreEntries(File directory) throws IOException {
        File cvsIgnore = new File(directory, FILENAME_CVSIGNORE);
        
        Set entries = new HashSet(5);
        if (!cvsIgnore.canRead()) return entries;
        
        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(cvsIgnore));
            while ((s = r.readLine()) != null) {
                entries.add(s);
            }
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
        return entries;
    }
    
    private void writeCvsIgnoreEntries(File directory, Set entries) throws IOException {
        File cvsIgnore = new File(directory, FILENAME_CVSIGNORE);
        FileObject fo = FileUtil.toFileObject(cvsIgnore);

        if (entries.size() == 0) {
            if (fo != null) fo.delete();
            return;
        }
        
        if (fo == null || !fo.isValid()) {
            fo = FileUtil.toFileObject(directory);
            fo = fo.createData(FILENAME_CVSIGNORE);
        }
        FileLock lock = fo.lock();
        PrintWriter w = null;
        try {
            w = new PrintWriter(fo.getOutputStream(lock));
            for (Iterator i = entries.iterator(); i.hasNext();) {
                w.println(i.next());
            }
        } finally {
            lock.releaseLock();
            if (w != null) w.close();
        }
    }

    /**
     * Hook to obtain CVS system interception listener.
     * 
     * @return InterceptionListener returns file system handler instance
     */ 
    InterceptionListener getFileSystemHandler() {
        return filesystemHandler;
    }

    void shutdown() {
        filesystemHandler.shutdown();
        FileStatusProvider.getInstance().shutdown();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    CvsSynchronizeTopComponent.getInstance().close();
                } catch (Throwable e) {
                    // ignore, this component is already invalid
                }
            }
        });
    }

    /**
     * Creates new GlobalOptions prefilled with default options:
     * <ul>
     *   <li>compression level 3 if not enabled logging
     * </ul>
     */
    public static GlobalOptions createGlobalOptions() {
        GlobalOptions globalOptions = new GlobalOptions();
        if (System.getProperty("cvsClientLog") == null) {    // NOI18N
            int gzipLevel = 3;
            String level = System.getProperty("netbeans.experimental.cvs.io.compressionLevel"); // NOI18N
            if (level != null) {
                try {
                    int candidate = Integer.parseInt(level);
                    if (0 <= candidate && candidate < 10) {
                        gzipLevel = candidate;
                    }
                } catch (NumberFormatException ex) {
                    // default level
                }
            }
            if (gzipLevel > 0) {
                globalOptions.setCompressionLevel(gzipLevel);
            }
        }
        return globalOptions;
    }
}