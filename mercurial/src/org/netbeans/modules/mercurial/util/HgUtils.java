/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mercurial.util;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.ui.status.SyncFileNode;
import org.openide.util.NbBundle;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.windows.OutputEvent;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Utilities;
import org.openide.windows.OutputListener;

/**
 *
 * @author jrice
 */
public class HgUtils {    
    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)hg(\\" + File.separatorChar + ".*|$)"); // NOI18N
    
    // IGNORE SUPPORT HG: following file patterns are added to {Hg repos}/.hgignore and Hg will ignore any files
    // that match these patterns, reporting "I"status for them // NOI18N
    private static final String [] HG_IGNORE_FILES = { "\\.orig$", "\\.rej$"}; // NOI18N
    
    private static final String FILENAME_HGIGNORE = ".hgignore"; // NOI18N

    private static final int MAX_LINES_TO_PRINT = 500;

    private static final String MSG_TOO_MANY_LINES = "The number of output lines is greater than 500; see message log for complete output";


    /**
     * isSolaris - check you are running onthe Solaris OS
     *
     * @return boolean true - on Solaris, false - not on Solaris
     */
    public static boolean isSolaris(){
        return System.getProperty("os.name").equals("SunOS"); // NOI18N
    }

    /**
     * isInUserPath - check if passed in name is on the Users PATH environment setting
     *
     * @param name to check
     * @return boolean true - on PATH, false - not on PATH
     */
    public static boolean isInUserPath(String name) {
        String pathEnv = System.getenv().get("PATH");// NOI18N
        // Work around issues on Windows fetching PATH
        if(pathEnv == null) pathEnv = System.getenv().get("Path");// NOI18N
        if(pathEnv == null) pathEnv = System.getenv().get("path");// NOI18N
        String pathSeparator = System.getProperty("path.separator");// NOI18N
        if (pathEnv == null || pathSeparator == null) return false;

        String[] paths = pathEnv.split(pathSeparator);
        for (String path : paths) {
            File f = new File(path, name);
            // On Windows isFile will fail on hgk.cmd use !isDirectory
            if (f.exists() && !f.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    /**
     * confirmDialog - display a confirmation dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param query ask user
     * @return boolean true - answered Yes, false - answered No
     */
    public static boolean confirmDialog(Class bundleLocation, String title, String query) {
        int response = JOptionPane.showOptionDialog(null, NbBundle.getMessage(bundleLocation, query), NbBundle.getMessage(bundleLocation, title), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

        if (response == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * warningDialog - display a warning dialog
     *
     * @param bundleLocation location of string resources to display
     * @param title of dialog to display    
     * @param warning to display to the user
     */
     public static void warningDialog(Class bundleLocation, String title, String warning) {
        JOptionPane.showMessageDialog(null,
                NbBundle.getMessage(bundleLocation,warning),
                NbBundle.getMessage(bundleLocation,title),
                JOptionPane.WARNING_MESSAGE);
    }
    

    /**
     * stripDoubleSlash - converts '\\' to '\' in path on Windows
     *
     * @param String path to convert
     * @return String converted path
     */
    public static String stripDoubleSlash(String path){
        if(Utilities.isWindows()){                       
            return path.replace("\\\\", "\\");
        }
        return path;
    }
    /**
     * isLocallyAdded - checks to see if this file has been Locally Added to Hg
     *
     * @param file to check
     * @return boolean true - ignore, false - not ignored
     */
    public static boolean isLocallyAdded(File file){
        if (file == null) return false;
        Mercurial hg = Mercurial.getInstance();        

        if ((hg.getFileStatusCache().getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) !=0)
            return true;
        else
            return false;
    }
    
    /**
     * isIgnored - checks to see if this is a file Hg should ignore
     *
     * @param File file to check
     * @return boolean true - ignore, false - not ignored
     */
    public static boolean isIgnored(File file){
        if (file == null) return true;
        String name = file.getName();
        
        Set<Pattern> patterns = new HashSet<Pattern>(5);
        addIgnorePatterns(patterns, Mercurial.getInstance().getTopmostManagedParent(file));

        for (Iterator i = patterns.iterator(); i.hasNext();) {
            Pattern pattern = (Pattern) i.next();
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }

        if (FILENAME_HGIGNORE.equals(name)) return false;
        int sharability = SharabilityQuery.getSharability(file);
        if (sharability == SharabilityQuery.NOT_SHARABLE) return true;
        return false;
    }

    /**
     * createIgnored - creates .hgignore file in the repository in which 
     * the given file belongs. This .hgignore file ensures Hg will ignore 
     * the files specified in HG_IGNORE_FILES list
     *
     * @param path to repository to place .hgignore file
     */
    public static void createIgnored(File path){
        if( path == null) return;
        BufferedWriter fileWriter = null;
        Mercurial hg = Mercurial.getInstance();
        File root = hg.getTopmostManagedParent(path);
        if( root == null) return;
        File ignore = new File(root, FILENAME_HGIGNORE);
        if (ignore.exists()) return;
           
        try     {
            fileWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(ignore)));
            for (String name : HG_IGNORE_FILES) {
                fileWriter.write(name + "\n"); // NOI18N
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                    new Object[] {ignore.getAbsolutePath(), ex.toString()});
        }finally {
            try {
                fileWriter.close();
                hg.getFileStatusCache().refresh(ignore, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.FINE, "createIgnored(): File {0} - {1}",  // NOI18N
                        new Object[] {ignore.getAbsolutePath(), ex.toString()});
            }
        }
    }

    private static void addIgnorePatterns(Set<Pattern> patterns, File file) {
        Set<String> shPatterns;
        try {
            shPatterns = readIgnoreEntries(file);
        } catch (IOException e) {
            // ignore invalid entries
            return;
        }
        for (Iterator i = shPatterns.iterator(); i.hasNext();) {
            String shPattern = (String) i.next();
            if ("!".equals(shPattern)) { // NOI18N
                patterns.clear();
            } else {
                try {
                    patterns.add(Pattern.compile(shPattern));
                } catch (Exception e) {
                    // unsupported pattern
                }
            }
        }
    }

    private static Set<String> readIgnoreEntries(File directory) throws IOException {
        File hgIgnore = new File(directory, FILENAME_HGIGNORE);

        Set<String> entries = new HashSet<String>(5);
        if (!hgIgnore.canRead()) return entries;

        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(hgIgnore));
            while ((s = r.readLine()) != null) {
                entries.addAll(Arrays.asList(s.trim().split(" ")));
            }
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
        return entries;
    }

    private static String computePatternToIgnore(File directory, File file) {
        String name = file.getAbsolutePath().substring(directory.getAbsolutePath().length()+1);
        return name.replace(' ', '?').replace(File.separatorChar, '/');
    }

    private static void writeIgnoreEntries(File directory, Set entries) throws IOException {
        File hgIgnore = new File(directory, FILENAME_HGIGNORE);
        FileObject fo = FileUtil.toFileObject(hgIgnore);

        if (entries.size() == 0) {
            if (fo != null) fo.delete();
            return;
        }

        if (fo == null || !fo.isValid()) {
            fo = FileUtil.toFileObject(directory);
            fo = fo.createData(FILENAME_HGIGNORE);
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
     * addIgnored - Add the specified files to the .hgignore file in the 
     * specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be added
     */
    public static void addIgnored(File directory, File[] files) throws IOException {
        Set<String> entries = readIgnoreEntries(directory);
        for (File file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.add(patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }

    /**
     * removeIgnored - Remove the specified files from the .hgignore file in 
     * the specified repository.
     *
     * @param directory for repository for .hgignore file
     * @param files an array of Files to be removed
     */
    public static void removeIgnored(File directory, File[] files) throws IOException {
        Set entries = readIgnoreEntries(directory);
        for (File file: files) {
            String patterntoIgnore = computePatternToIgnore(directory, file);
            entries.remove(patterntoIgnore);
        }
        writeIgnoreEntries(directory, entries);
    }

    /**
     * Returns a Map keyed by Directory, containing a single File/FileInformation Map for each Directories file contents.
     *
     * @param Map of <File, FileInformation> interestingFiles to be processed and divided up into Files in Directory
     * @param Collection of <File> files to be processed against the interestingFiles
     * @return Map of Dirs containing Map of files and status for all files in each directory
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, Map<File, FileInformation>> getInterestingDirs(Map<File, FileInformation> interestingFiles, Collection<File> files) {
        Map<File, Map<File, FileInformation>> interestingDirs = new HashMap<File, Map<File, FileInformation>>();

        Calendar start = Calendar.getInstance();
        for (File file : files) {
            if (file.isDirectory()) {
                if (interestingDirs.get(file) == null) {
                    interestingDirs.put(file, new HashMap<File, FileInformation>());
                }
            } else {
                File par = file.getParentFile();
                if (par != null) {
                    if (interestingDirs.get(par) == null) {
                        interestingDirs.put(par, new HashMap<File, FileInformation>());
                    }
                    FileInformation fi = interestingFiles.get(file);
                    interestingDirs.get(par).put(file, fi);
                }
            }
        }
        Calendar end = Calendar.getInstance();
        Mercurial.LOG.log(Level.FINE, "getInterestingDirs: process interesting Dirs took {0} millisecs",  // NOI18N
                end.getTimeInMillis() - start.getTimeInMillis());
        return interestingDirs;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActivatedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are represented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @param nodes null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     * @param includingFileStatus if any activated file does not have this CVS status, an empty array is returned
     * @param includingFolderStatus if any activated folder does not have this CVS status, an empty array is returned
     * @return File [] array of activated files, or an empty array if any of examined files/folders does not have given status
     */
    public static VCSContext getCurrentContext(Node[] nodes, int includingFileStatus, int includingFolderStatus) {
        VCSContext context = getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (File file : context.getRootFiles()) {
            FileInformation fi = cache.getStatus(file);
            if (file.isDirectory()) {
                if ((fi.getStatus() & includingFolderStatus) == 0) return VCSContext.EMPTY;
            } else {
                if ((fi.getStatus() & includingFileStatus) == 0) return VCSContext.EMPTY;
            }
        }
        return context;
    }

    /**
     * Semantics is similar to {@link org.openide.windows.TopComponent#getActiva
tedNodes()} except that this
     * method returns File objects instead of Nodes. Every node is examined for
Files it represents. File and Folder
     * nodes represent their underlying files or folders. Project nodes are repr
esented by their source groups. Other
     * logical nodes must provide FileObjects in their Lookup.
     *
     * @return File [] array of activated files
     * @param nodes or null (then taken from windowsystem, it may be wrong on ed
itor tabs #66700).
     */

    public static VCSContext getCurrentContext(Node[] nodes) {
        if (nodes == null) {
            nodes = TopComponent.getRegistry().getActivatedNodes();
        }
        return VCSContext.forNodes(nodes);
    }

   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static String getRootPath(VCSContext context){
        File root = getRootFile(context);
        return (root == null) ? null: root.getAbsolutePath();
    }
    
   /**
     * Returns path to repository root or null if not managed
     *
     * @param VCSContext
     * @return String of repository root path
     */
    public static File getRootFile(VCSContext context){
        if (context == null) return null;
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return null;
        
        File root = hg.getTopmostManagedParent(files[0]);
        return root;
    }
    
   /**
     * Returns File object for Project Directory
     *
     * @param VCSContext
     * @return File object of Project Directory
     */
    public static File getProjectFile(VCSContext context){
        return getProjectFile(getProject(context));
    }

    public static Project getProject(VCSContext context){
        if (context == null) return null;
        Node [] nodes = context.getElements().lookupAll(Node.class).toArray(new Node[0]);
        for (Node node : nodes) {
            Node tmpNode = node;

            Project project = (Project) tmpNode.getLookup().lookup(Project.class);
            while (project == null) {
                tmpNode = tmpNode.getParentNode();
                if (tmpNode ==  null) {
                    Mercurial.LOG.log(Level.FINE, "HgUtils.getProjectFile(): No project for {0}",  // NOI18N
                        node.toString());
                    break;
                }
                project = (Project) tmpNode.getLookup().lookup(Project.class);
            } 
            if (project != null) {
                return project;
            }
        }
        return null;
    }
    
    public static File getProjectFile(Project project){
        if (project == null) return null;

        FileObject fo = project.getProjectDirectory();
        return  FileUtil.toFile(fo);
    }

    public static File[] getProjectRootFiles(Project project){
        if (project == null) return null;
        Set<File> set = new HashSet<File>();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            set.add(rootFile);
        }
        return set.toArray(new File[set.size()]);
    }

    /**
     * Checks file location to see if it is part of mercurial metdata
     *
     * @param file file to check
     * @return true if the file or folder is a part of mercurial metadata, false otherwise
     */
    public static boolean isPartOfMercurialMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
    

    /**
     * Forces refresh of Status for the given directory 
     *
     * @param start file or dir to begin refresh from
     * @return void
     */
    public static void forceStatusRefresh(File file) {
        if (Mercurial.getInstance().isAdministrative(file)) return;
        
        try {
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();

            cache.refreshCached(file);
            File repository = Mercurial.getInstance().getTopmostManagedParent(file);
            if (repository == null) {
                return;
            }
        
            if (file.isDirectory()) {
                Map<File, FileInformation> interestingFiles;
                interestingFiles = HgCommand.getInterestingStatus(repository, file);
                if (!interestingFiles.isEmpty()){
                    Collection<File> files = interestingFiles.keySet();
                    for (File aFile : files) {
                        FileInformation fi = interestingFiles.get(aFile);
                        cache.refreshFileStatus(aFile, fi, null);
                    }
                }
            }

        } catch (HgException ex) {
        }
    }

    /**
     * Forces refresh of Status for the specfied context.
     *
     * @param VCSContext context to be updated.
     * @return void
     */
    public static void forceStatusRefresh(VCSContext context) {
        for (File root :  context.getRootFiles()) {
            forceStatusRefresh(root);
        }
    }

    /**
     * Forces refresh of Status for the project of the specified context
     *
     * @param VCSContext ctx whose project is be updated.
     * @return void
     */
    public static void forceStatusRefreshProject(VCSContext context) {
        Project project = getProject(context);
        if (project == null) return;
        File[] files = getProjectRootFiles(project);
        for (int j = 0; j < files.length; j++) {
            forceStatusRefresh(files[j]);
        }
    }

    /**
     * Tests parent/child relationship of files.
     *
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */
    public static boolean isParentOrEqual(File parent, File file) {
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) return true;
        }
        return false;
    }
    
    /**
     * Returns path of file relative to root repository or a warning message
     * if the file is not under the repository root.
     *
     * @param File to get relative path from the repository root
     * @return String of relative path of the file from teh repository root
     */
    public static String getRelativePath(File file) {
            if (file == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            String shortPath = file.getAbsolutePath();
            if (shortPath == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            Mercurial mercurial = Mercurial.getInstance();
            File rootManagedFolder = mercurial.getTopmostManagedParent(file);
            if ( rootManagedFolder == null){
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
            
            String root = rootManagedFolder.getAbsolutePath();
            if(shortPath.startsWith(root)) {
                return shortPath.substring(root.length()+1);
            }else{
                return NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
            }
     }

    /**
     * Normalize flat files, Mercurial treats folder as normal file
     * so it's necessary explicitly list direct descendants to
     * get classical flat behaviour.
     *
     * <p> E.g. revert on package node means:
     * <ul>
     *   <li>revert package folder properties AND
     *   <li>revert all modified (including deleted) files in the folder
     * </ul>
     *
     * @return files with given status and direct descendants with given status.
     */

    public static File[] flatten(File[] files, int status) {
        LinkedList<File> ret = new LinkedList<File>();

        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (int i = 0; i<files.length; i++) {
            File dir = files[i];
            FileInformation info = cache.getStatus(dir);
            if ((status & info.getStatus()) != 0) {
                ret.add(dir);
            }
            File[] entries = cache.listFiles(dir);  // comparing to dir.listFiles() lists already deleted too
            for (int e = 0; e<entries.length; e++) {
                File entry = entries[e];
                info = cache.getStatus(entry);
                if ((status & info.getStatus()) != 0) {
                    ret.add(entry);
                }
            }
        }

        return ret.toArray(new File[ret.size()]);
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(VCSContext context, int includeStatus) {
        File[] all = Mercurial.getInstance().getFileStatusCache().listFiles(context, includeStatus);
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < all.length; i++) {
            File file = all[i];
            String path = file.getAbsolutePath();
            if (HgModuleConfig.getDefault().isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }

        // ensure that command roots (files that were explicitly selected by user) are included in Diff
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        for (File file : context.getRootFiles()) {
            if (file.isFile() && (cache.getStatus(file).getStatus() & includeStatus) != 0 && !files.contains(file)) {
                files.add(file);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    /**
     * Checks if the file is binary.
     *
     * @param file file to check
     * @return true if the file cannot be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentBinary(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getCookie(EditorCookie.class) == null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    /**
     * @return true if the buffer is almost certainly binary.
     * Note: Non-ASCII based encoding encoded text is binary,
     * newlines cannot be reliably detected.
     */
    public static boolean isBinary(byte[] buffer) {
        for (int i = 0; i<buffer.length; i++) {
            int ch = buffer[i];
            if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    public static class ByImportanceComparator<T> implements Comparator<FileInformation> {
        public int compare(FileInformation i1, FileInformation i2) {
            return getComparableStatus(i1.getStatus()) - getComparableStatus(i2.getStatus());
        }
    }

    /**
     * Gets integer status that can be used in comparators. The more important the status is for the user,
     * the lower value it has. Conflict is 0, unknown status is 100.
     *
     * @return status constant suitable for 'by importance' comparators
     */
    public static int getComparableStatus(int status) {
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return 0;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return 1;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return 10;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return 11;
       } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return 12;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return 13;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return 14;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return 30;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return 31;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return 32;
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return 50;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)){
            return 100;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return 101;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return 102;
        } else {
            throw new IllegalArgumentException("Uncomparable status: " + status); // NOI18N
        }
    }

    protected static int getFileEnabledStatus() {
        return ~0;
    }

    protected static int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    /**
     * Rips an eventual username off - e.g. user@svn.host.org
     *
     * @param host - hostname with a userneame
     * @return host - hostname without the username
     */
    public static String ripUserFromHost(String host) {
        int idx = host.indexOf('@');
        if(idx < 0) {
            return host;
        } else {
            return host.substring(idx + 1);
        }
    }


    /**
     * Print contents of list to Mercurial Output Tab
     *
     * @param list to print out
     * 
     */
     public static void outputMercurialTab(List<String> list){
        if( list.isEmpty()) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        
        int lines = list.size();
        if (lines > MAX_LINES_TO_PRINT) {
            out.println(list.get(1));
            out.println(list.get(2));
            out.println(list.get(3));
            out.println("...");
            out.println(list.get(list.size() -1));
            out.println(MSG_TOO_MANY_LINES);
            for (String s : list){
                Mercurial.LOG.log(Level.WARNING, s);
            }
        } else {
            for (String s : list){
                out.println(s);

            }
        }
        out.close();
    }

     /**
     * Print msg to Mercurial Output Tab
     *
     * @param String msg to print out
     * 
     */
     public static void outputMercurialTab(String msg){
        if( msg == null) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getOut();
        
        out.println(msg);
        out.close();
    }

    /**
     * Print msg to Mercurial Output Tab in Red
     *
     * @param String msg to print out
     * 
     */
     public static void outputMercurialTabInRed(String msg){
        if( msg == null) return;

        InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
        io.select();
        OutputWriter out = io.getErr();
        
        out.println(msg);
        out.close();
    }

    /**
     * Print URL to Mercurial Output Tab as an active Hyperlink
     *
     * @param String sURL to print out
     * 
     */
     public static void outputMercurialTabLink(final String sURL){
         if (sURL == null) return;
         
         try {
             InputOutput io = IOProvider.getDefault().getIO(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
             io.select();
             OutputWriter out = io.getOut();

             OutputListener listener = new OutputListener() {
                         public void outputLineAction(OutputEvent ev) {
                             try {
                                 HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(sURL));
                             } catch (IOException ex) {
                             // Ignore
                             }
                         }
                         public void outputLineSelected(OutputEvent ev) {}
                         public void outputLineCleared(OutputEvent ev) {}
                     };
             out.println(sURL, listener, true);
             out.close();
         } catch (IOException ex) {
         // Ignore
         }
     }

    /**
     * Select and Clear Mercurial Output Tab
     *
     * @param list to print out
     * 
     */
     public static void clearOutputMercurialTab(){
         InputOutput io = IOProvider.getDefault().getIO(
                 Mercurial.MERCURIAL_OUTPUT_TAB_TITLE, false);
         
         io.select();
         OutputWriter out = io.getOut();
         
         try {
             out.reset();
         } catch (IOException ex) {
             // Ignore Exception
         }
         out.close();
    }
     
    /**
     * This utility class should not be instantiated anywhere.
     */
    private HgUtils() {
    }
    
}
