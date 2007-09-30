/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.java.source.tasklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda, Stanislav Aubrecht
 */
public class TaskCache {
    
    private static final String ERR_EXT = "err";
    private static final String WARN_EXT = "warn";
    
    private static final Logger LOG = Logger.getLogger(TaskCache.class.getName());
    
    static {
//        LOG.setLevel(Level.FINEST);
    }
    
    private static TaskCache theInstance;
    
    private TaskCache() {
    }
    
    public static TaskCache getDefault() {
        if( null == theInstance ) {
            theInstance = new TaskCache();
        }
        return theInstance;
    }
    
    private String getTaskType( Kind k ) {
        switch( k ) {
            case ERROR:
                return "nb-tasklist-error"; //NOI18N
            case WARNING:
            case MANDATORY_WARNING:
                return "nb-tasklist-warning"; //NOI18N
        }
        return null;
    }
    
    public List<Task> getErrors(FileObject file) {
        return getErrors(file, false);
    }
    
    private List<Task> getErrors(FileObject file, boolean onlyErrors) {
        LOG.log(Level.FINE, "getErrors, file={0}", FileUtil.getFileDisplayName(file));
        
        try {
            File input = computePersistentFile(file);
            
            LOG.log(Level.FINE, "getErrors, error file={0}", input == null ? "null" : input.getAbsolutePath());
            
            if (input == null || !input.canRead())
                return Collections.<Task>emptyList();
            
            input.getParentFile().mkdirs();
            
            return loadErrors(input, file, onlyErrors);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return Collections.<Task>emptyList();
    }
    
    private boolean dumpErrors(File output, List<? extends Diagnostic> errors, boolean interestedInReturnValue) throws IOException {
        if (!errors.isEmpty()) {
            boolean existed = interestedInReturnValue && output.exists();
            output.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
            
            for (Diagnostic d : errors) {
                pw.print(d.getKind());
                pw.print(':');
                pw.print(d.getLineNumber());
                pw.print(':');
                
                String description = d.getMessage(null);
                
                description = description.replaceAll("\\\\", "\\\\\\\\");
                description = description.replaceAll("\n", "\\\\n");
                description = description.replaceAll(":", "\\\\d");
                
                pw.println(description);
            }
            
            pw.close();
            
            return !existed;
        } else {
            return output.delete();
        }
    }
    
    private void separate(List<? extends Diagnostic> input, List<Diagnostic> errors, List<Diagnostic> notErrors) {
        for (Diagnostic d : input) {
            if (d.getKind() == Kind.ERROR) {
                errors.add(d);
            } else {
                notErrors.add(d);
            }
        }
    }
    
    public Set<URL> dumpErrors(URL root, URL file, File fileFile, List<? extends Diagnostic> errors) throws IOException {
        if (!fileFile.canRead()) {
            //if the file is not readable anymore, ignore the errors:
            errors = Collections.emptyList();
        }
        
        File[] output = computePersistentFile(root, file);
        
        List<Diagnostic> trueErrors = new LinkedList<Diagnostic>();
        List<Diagnostic> notErrors = new LinkedList<Diagnostic>();
        
        separate(errors, trueErrors, notErrors);
        
        boolean modified = dumpErrors(output[1], trueErrors, true);
        
        dumpErrors(output[2], notErrors, false);
        
        Set<URL> toRefresh = new HashSet<URL>();
        
        toRefresh.add(file);
        
        if (modified) {
            File current = output[1].getParentFile();
            File currentFile = fileFile.getParentFile();

            while (!output[0].equals(current)) {
                toRefresh.add(currentFile.toURL());
                current = current.getParentFile();
                currentFile = currentFile.getParentFile();
            }

            toRefresh.add(currentFile.toURL());

            FileObject rootFO = URLMapper.findFileObject(root);

            //XXX:
            if (rootFO != null) {
                Project p = FileOwnerQuery.getOwner(rootFO);

                if (p != null) {
                    FileObject currentFO = rootFO;
                    FileObject projectDirectory = p.getProjectDirectory();

                    if (FileUtil.isParentOf(projectDirectory, rootFO)) {
                        while (currentFO != null && currentFO != projectDirectory) {
                            toRefresh.add(currentFO.getURL());
                            currentFO = currentFO.getParent();
                        }
                    }

                    toRefresh.add(projectDirectory.getURL());
                }
            }
        }
        
        return toRefresh;
    }

    private List<Task> loadErrors(File input, FileObject file, boolean onlyErrors) throws IOException {
        List<Task> result = new LinkedList<Task>();
        BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
        String line;

        while ((line = pw.readLine()) != null) {
            String[] parts = line.split(":");

            Kind kind = Kind.valueOf(parts[0]);

            if (kind == null) {
                continue;
            }

            int lineNumber = Integer.parseInt(parts[1]);
            String message = parts[2];

            message = message.replaceAll("\\\\d", ":");
            message = message.replaceAll("\\\\n", " ");
            message = message.replaceAll("\\\\\\\\", "\\\\");

            String severity = getTaskType(kind);

            if (null != severity && (!onlyErrors || kind == Kind.ERROR)) {
                Task err = Task.create(file, severity, message, lineNumber);
                result.add(err);
            }
        }

        pw.close();
        
        return result;
    }
    
    public List<URL> getAllFilesWithRecord(URL root) throws IOException {
        return getAllFilesWithRecord(root, false);
    }
    
    private List<URL> getAllFilesWithRecord(URL root, boolean onlyErrors) throws IOException {
        try {
            List<URL> result = new LinkedList<URL>();
            URI rootURI = root.toURI();
            File[] cacheRoot = computePersistentFile(root, root);
            URI cacheRootURI = cacheRoot[0].toURI();
            Queue<File> todo = new LinkedList<File>();
            
            todo.add(cacheRoot[0]);
            
            while (!todo.isEmpty()) {
                File f = todo.poll();
                
                assert f != null;
                
                if (f.isFile()) {
                    if (f.getName().endsWith(ERR_EXT)) {
                        String relative = cacheRootURI.relativize(f.toURI()).getPath();
                        
                        relative = relative.replaceAll(ERR_EXT + "$", "java");
                        result.add(rootURI.resolve(relative).toURL());
                    }
                    if (!onlyErrors && f.getName().endsWith(WARN_EXT)) {
                        String relative = cacheRootURI.relativize(f.toURI()).getPath();
                        
                        relative = relative.replaceAll(WARN_EXT + "$", "java");
                        result.add(rootURI.resolve(relative).toURL());
                    }
                } else {
                    File[] files = f.listFiles();
                    
                    if (files != null) {
                        for (File children : files)
                            todo.offer(children);
                    }
                }
            }
            
            return result;
        } catch (URISyntaxException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    public List<URL> getAllFilesInError(URL root) throws IOException {
        return getAllFilesWithRecord(root, true);
    }
    
    public boolean isInError(FileObject file, boolean recursive) {
        LOG.log(Level.FINE, "file={0}, recursive={1}", new Object[] {file, Boolean.valueOf(recursive)});
        
        if (file.isData()) {
            return !getErrors(file, true).isEmpty();
        } else {
            try {
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                
                if (cp == null) {
                    return false;
                }
                
                FileObject root = cp.findOwnerRoot(file);
                
                if (root == null) {
                    LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file);
                    return false;
                }
                
                String resourceName = cp.getResourceName(file, File.separatorChar, false);
                File cacheRoot = Index.getClassFolder(root.getURL(), true);
                
                if (cacheRoot == null) {
                    //index does not exist:
                    return false;
                }
                
                final File folder = new File(new File(cacheRoot.getParentFile(), "errors"), resourceName);
                
                return folderContainsErrors(folder, recursive);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
                return false;
            }
        }
    }
    
    private boolean folderContainsErrors(File folder, boolean recursively) throws IOException {
        File[] errors = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".err");
            }
        });
        
        if (errors == null)
            return false;
        
        if (errors.length > 0) {
            return true;
        }
        
        if (!recursively)
            return false;
        
        File[] children = folder.listFiles();
        
        if (children == null)
            return false;
        
        for (File c : children) {
            if (c.isDirectory() && folderContainsErrors(c, recursively)) {
                return true;
            }
        }
        
        return false;
    }
    
    private File[] computePersistentFile(URL root, URL file) throws IOException {
        try {
            URI fileURI = file.toURI();
            URI u = root.toURI();
            String resourceName = u.relativize(fileURI).getPath();
            int lastDot = resourceName.lastIndexOf('.');
            if (lastDot != (-1)) {
                resourceName = resourceName.substring(0, lastDot);
            }
            File cacheRoot = Index.getClassFolder(root);
            File errorsRoot = new File(cacheRoot.getParentFile(), "errors");
            File errorCacheFile = new File(errorsRoot, resourceName + "." + ERR_EXT);
            File warningCacheFile = new File(errorsRoot, resourceName + "." + WARN_EXT);
            
            return new File[] {errorsRoot, errorCacheFile, warningCacheFile};
        } catch (URISyntaxException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    private File computePersistentFile(FileObject file) throws IOException {
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        
        if (cp == null)
            return null;
        
        FileObject root = cp.findOwnerRoot(file);
        
        if (root == null) {
            LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file);
            return null;
        }
        
        String resourceName = cp.getResourceName(file, File.separatorChar, false);
        File cacheRoot = Index.getClassFolder(root.getURL());
        File cacheFile = new File(new File(cacheRoot.getParentFile(), "errors"), resourceName + ".err");
        
        return cacheFile;
    }
    
}
