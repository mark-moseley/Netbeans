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

package org.netbeans.modules.gsfret.source.usages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.Indexer;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.api.gsfpath.queries.SourceLevelQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.ParserTaskImpl;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfret.source.GlobalSourcePath;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import org.netbeans.modules.gsfret.source.parsing.FileObjects;
import org.netbeans.modules.gsfret.source.util.LowMemoryEvent;
import org.netbeans.modules.gsfret.source.util.LowMemoryListener;
import org.netbeans.modules.gsfret.source.util.LowMemoryNotifier;
import org.netbeans.spi.gsfpath.classpath.ClassPathFactory;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 * RepositoryUpdater is in charge of maintaining indices of the various classes in 
 * the system, for use by code completion, go to declaration, etc.  The classes include
 * not only the user's source and test directories, but jars from the boot class path etc.
 * The RepositoryUpdater schedules indexing jobs, watches filesystems for modifications,
 * determines whether an index is out of date, etc.
 * 
 * This class is originally from Retouche, under the java/source module. Since it's
 * an important and fairly complicated piece of logic, I am trying my best to keep
 * my copy in sync with the java one. Therefore, I have left the original formatting
 * in place as much as possible. Please don't make gratuitous formatting changes that
 * makes diffing harder.
 * 
 * There are some important changes. Obviously, the various javac-specific setup code
 * has changed, and I also need to -iterate- over files to be indexed to let the
 * potentially multiple language indexers each have a chance to index the file.
 * I have also changed references to other parts that have been renamed, such
 * as JavaSource => Source, etc.
 * 
 * @author Tomas Zezula
 * @author Tor Norbye
 */
public class RepositoryUpdater implements PropertyChangeListener, FileChangeListener {
    private static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Set<String> ignoredDirectories = parseSet("org.netbeans.javacore.ignoreDirectories", "SCCS CVS .svn"); // NOI18N
    private static final boolean noscan = Boolean.getBoolean("netbeans.javacore.noscan");   //NOI18N
    private static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test");
    //private static final String PACKAGE_INFO = "package-info.java";  //NOI18N
    
    private static final int DELAY = Utilities.isWindows() ? 2000 : 1000;
    
    private static RepositoryUpdater instance;
    
    private final GlobalSourcePath cpImpl;
    private final ClassPath cp;
    private final ClassPath ucp;
    private final ClassPath binCp;
    private Set<URL> scannedRoots;
    private Set<URL> scannedBinaries;
    private Map<URL,List<URL>> deps;        //todo: may be shared with scannedRoots, may save some HashMap.Entry
    private Delay delay;
    private Work currentWork;
    private boolean dirty;
    private int noSubmited;
    
    //Preprocessor support
    //private final Map<URL, JavaFileFilterImplementation> filters = Collections.synchronizedMap(new HashMap<URL, JavaFileFilterImplementation>());
    private final FilterListener filterListener = new FilterListener ();    
    
    /** Creates a new instance of RepositoryUpdater */
    private RepositoryUpdater() {
        try {
            this.scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
            this.scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());            
            this.deps = Collections.synchronizedMap(new HashMap<URL,List<URL>>());
            this.delay = new Delay();
            this.cpImpl = GlobalSourcePath.getDefault();
            this.cpImpl.setExcludesListener (this);
            this.cp = ClassPathFactory.createClassPath (this.cpImpl.getSourcePath());
            this.cp.addPropertyChangeListener(this);
            this.ucp = ClassPathFactory.createClassPath (this.cpImpl.getUnknownSourcePath());
            this.binCp = ClassPathFactory.createClassPath(this.cpImpl.getBinaryPath());
            this.registerFileSystemListener();
            submitBatch();
        } catch (TooManyListenersException e) {
            throw new IllegalStateException ();
        }
    }
    
    public ClassPath getScannedSources () {
        return this.cp;
    }
    
    public ClassPath getScannedBinaries() {
        return this.binCp;
    }
    
    public Map<URL,List<URL>> getDependencies () {
        return new HashMap<URL,List<URL>> (this.deps);
    }
    
    public void close () {                
        this.cp.removePropertyChangeListener(this);
        this.unregisterFileSystemListener();
        this.delay.cancel();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            submitBatch();
        }
        else if (GlobalSourcePath.PROP_INCLUDES.equals(evt.getPropertyName())) {            
            ClassPath changedCp = (ClassPath) evt.getNewValue();
            assert changedCp != null;
            for (ClassPath.Entry e : changedCp.entries()) {
                URL root = e.getURL();
                scheduleCompilation(root,root, true);
            }
        }
    }
    
    private synchronized void submitBatch () {
        if (this.currentWork == null) {                    
            this.currentWork = Work.batch();
            submit (this.currentWork);
        }
        else {
            this.dirty = true;
        }
    }
    
    public synchronized boolean isScanInProgress() {
        return this.noSubmited > 0;
    }
    
    public synchronized void waitScanFinished () throws InterruptedException {
        while (this.noSubmited > 0 ) {
            this.wait();
        }
    }
    
    
    private synchronized boolean isDirty () {
        if (this.dirty) {
            this.dirty = false;
            return true;
        }
        else {
            this.currentWork = null;
            return false;                        
        }
    }
    
    private synchronized void resetDirty () {
        this.dirty = false;
        this.currentWork = null;
    }
    
    
    public void fileRenamed(FileRenameEvent fe) {
        final FileObject fo = fe.getFile();
        try {
            if ((isRelevantSource (fo) || fo.isFolder()) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot(fo);
                if (root != null) {                
                    String originalName = fe.getName();
                    final String originalExt = fe.getExt();
                    if (originalExt.length()>0) {
                        originalName = originalName+'.'+originalExt;  //NOI18N
                    }
                    final File parentFile = FileUtil.toFile(fo.getParent());
                    if (parentFile != null) {
                        final URL original = new File (parentFile,originalName).toURI().toURL();
                        submit(Work.delete(original,root,fo.isFolder()));
                        delay.post(Work.compile (fo,root));
                    }
                }
            }
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    String originalName = fe.getName();
                    final String originalExt = fe.getExt();
                    if (originalExt.length()>0) {
                        originalName = originalName+'.'+originalExt;    //NOI18N
                    }
                    final File parentFile = FileUtil.toFile(fo.getParent());
                    if (parentFile != null) {
                        final URL original = new File (parentFile,originalName).toURI().toURL();
                        submit(Work.binary(original, root, fo.isFolder()));
                        submit(Work.binary(fo, root));
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        //Not interesting, do nothing
    }

    public void fileFolderCreated(FileEvent fe) {
        //In ideal case this should do nothing,
        //but in Netbeans newly created folder may
        //already contain files
        final FileObject fo = fe.getFile();
        try {
            final URL root = getOwningSourceRoot(fo);
            if ( root != null && VisibilityQuery.getDefault().isVisible(fo)) {
                scheduleCompilation(fo,root);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();        
        final boolean isFolder = fo.isFolder();
        try {
            if ((isRelevantSource(fo) || isFolder) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);
                if (root != null) {                
                    submit(Work.delete(fo,root,isFolder));
                }
            }
            else if ((isBinary(fo) || isFolder) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root !=null) {
                    submit(Work.binary(fo,root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();        
        try {
            if (isRelevantSource(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);        
                if (root != null) {
                    postCompilation(fo, root);
                }
            }
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    submit(Work.binary(fo, root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        try {
            if (isRelevantSource(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);
                if (root != null) {
                    postCompilation(fo, root);
                }
            }        
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    submit(Work.binary(fo, root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }  
    
    
    public final void scheduleCompilation (final FileObject fo, final FileObject root) throws IOException {
        URL foURL = fo.getURL();
        URL rootURL = root.getURL();
        assert "file".equals(foURL.getProtocol()) && "file".equals(rootURL.getProtocol());
        scheduleCompilation (foURL,rootURL,fo.isFolder());
    }      
    
    private final void scheduleCompilation (final FileObject fo, final URL root) throws IOException {
        scheduleCompilation (fo.getURL(),root,fo.isFolder());
    }
    
    private final void scheduleCompilation (final URL file, final URL root, boolean isFolder) {
        submit(Work.compile (file,root, isFolder));
    }
    
    private final void postCompilation (final FileObject file, final URL root) throws FileStateInvalidException {
        delay.post (Work.compile (file,root));
    }
    
    
    /**
     * This method is only for unit tests.
     * Test can schedule compilation and wait on the returned {@link CountDownLatch}
     * until the compilation is finished.
     * @param folder to be compiled
     * @param root the source root. The folder has to be either under the root or
     * equal to the root.
     * @return {@link CountDownLatch} to wait on.
     */
    public final CountDownLatch scheduleCompilationAndWait (final FileObject folder, final FileObject root) throws IOException {
        CountDownLatch[] latch = new CountDownLatch[1];
        submit(Work.compile (folder,root.getURL(),latch));
        return latch[0];
    }
    
    private void submit (final Work  work) {
        if (!noscan) {
            synchronized (this) {
                this.noSubmited++;
            }
            final CompileWorker cw = new CompileWorker (work);
            SourceAccessor.INSTANCE.runSpecialTask (cw, Source.Priority.MAX);
        }
    }
    
    
    private void registerFileSystemListener  () {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fss = new HashSet<FileSystem> ();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject (root);
            if (fo != null) {                
                try {
                    final FileSystem fs = fo.getFileSystem();
                    if (!fss.contains(fs)) {
                        fs.addFileChangeListener (this);
                        fss.add(fs);
                    }
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
    
    private void unregisterFileSystemListener () {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fss = new HashSet<FileSystem> ();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject (root);
            if (fo != null) {                
                try {
                    final FileSystem fs = fo.getFileSystem();
                    if (!fss.contains(fs)) {
                        fs.removeFileChangeListener (this);
                        fss.add(fs);
                    }
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
    
    private URL getOwningSourceRoot (final FileObject fo) {
        if (fo == null) {
            return null;
        }
        List<URL> clone = new ArrayList (this.scannedRoots);
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }        
    
    private URL getOwningBinaryRoot (final FileObject fo){
        if (fo == null) {
            return null;
        }
        try {
            synchronized (this.scannedBinaries) {
                URL foURL = fo.getURL();
                for (URL root : this.scannedBinaries) {
                    URL fileURL = FileUtil.getArchiveFile(root);
                    boolean archive = true;
                    if (fileURL == null) {
                        fileURL = root;
                        archive = false;
                    }
                    String filePath = fileURL.getPath();
                    String foPath = foURL.getPath();
                    if (filePath.equals(foPath)) {
                        return root;
                    }                    
                    if (!archive && foPath.startsWith(filePath)) {                        
                        return root;
                    }
                }
            }
        } catch (FileStateInvalidException fsi) {
            Exceptions.printStackTrace(fsi);
        }
        return null;
    }
    
    /**
     * Temporary implementation which does not care about
     * extended mime types like text/x-something+x-java
     */
    public static boolean isRelevantSource (final FileObject fo) {
        if (fo.isFolder()) {
            return false;
        }

        if (LanguageRegistry.getInstance().isSupported(fo.getMIMEType())) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isBinary (final FileObject fo) {
        return false;
        /* XXX TODO no support for binary persistence files yet, such as compiled ruby files etc.
        if (fo.isFolder()) {
            return false;
        }
        String ext = fo.getExt().toLowerCase();
        if (FileObjects.CLASS.equals(ext) ||
            FileObjects.JAR.equals(ext) ||
            FileObjects.ZIP.equals(ext)) { 
                return true;
        }        
        return false;
         */
    }
    
    private static enum WorkType {
        COMPILE_BATCH, COMPILE_CONT, COMPILE, DELETE, UPDATE_BINARY, FILTER_CHANGED
    };
    
    
    private static class Work {
        private final WorkType workType;
        private final CountDownLatch latch;
        
        protected Work (WorkType workType, CountDownLatch latch) {
            assert workType != null;
            this.workType = workType;
            this.latch = latch;
        }
        
        public WorkType getType () {
            return this.workType;
        }
        
        public void finished () {
            if (this.latch != null) {
                this.latch.countDown();
            }
        }
        
        
        public static Work batch () {
            return new Work (WorkType.COMPILE_BATCH, null);
        }
        
        public static Work compile (final FileObject file, final URL root) throws FileStateInvalidException {
            return compile (file.getURL(), root, file.isFolder());
        }
        
        public static Work compile (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.COMPILE, file, root, isFolder, null);
        }
        
        public static Work compile (final FileObject file, final URL root, CountDownLatch[] latch) throws FileStateInvalidException {
            assert file != null && root != null;
            assert latch != null && latch.length == 1 && latch[0] == null;
            latch[0] = new CountDownLatch (1);
            return new SingleRootWork (WorkType.COMPILE, file.getURL(), root, file.isFolder(),latch[0]);
        }
        
        public static Work delete (final FileObject file, final URL root, final boolean isFolder) throws FileStateInvalidException {
            return delete (file.getURL(), root,file.isFolder());
        }
        
        public static Work delete (final URL file, final URL root, final boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.DELETE, file, root, isFolder,null);
        }
        
        public static Work binary (final FileObject file, final URL root) throws FileStateInvalidException {
            return binary (file.getURL(), root, file.isFolder());
        }
        
        public static Work binary (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.UPDATE_BINARY, file, root, isFolder, null);
        }
        
        public static Work filterChange (final List<URL> roots) {
            assert roots != null;
            return new MultiRootsWork (WorkType.FILTER_CHANGED, roots, null);
        }
        
    }
        
    private static class SingleRootWork extends Work {
        
        private URL file;
        private URL root;
        private boolean isFolder;
        
                
        public SingleRootWork (WorkType type, URL file, URL root, boolean isFolder, CountDownLatch latch) {
            super (type, latch);           
            this.file = file;            
            this.root = root;
            this.isFolder = isFolder;
        }
        
        public URL getFile () {
            return this.file;
        }
        
        public URL getRoot () {
            return this.root;
        }
        
        public boolean isFolder () {            
            return this.isFolder;            
        }
        
    }
    
    private static class MultiRootsWork extends Work {
        private List<URL> roots;
        
        public MultiRootsWork (WorkType type, List<URL> roots, CountDownLatch latch) {
            super (type, latch);
            this.roots = roots;
        }
        
        public List<URL> getRoots () {
            return roots;
        }
    }  
    
    private final class CompileWorker implements CancellableTask<CompilationInfo> {
                
        private Work work;
        private List<URL> state;
        private Set<URL> oldRoots;
        private Set<URL> oldBinaries;
        private Set<URL> newBinaries;                
        private ProgressHandle handle;
        private final Set<URI> dirtyCrossFiles;        
        private final Set<URL> ignoreExcludes;
        private final AtomicBoolean canceled;
        
        public CompileWorker (Work work ) {
            assert work != null;
            this.work = work;
            this.canceled = new AtomicBoolean (false);
            this.dirtyCrossFiles = new HashSet<URI>();
            this.ignoreExcludes = new HashSet<URL>();
        }

        public void cancel () {            
            this.canceled.set(true);
        }
        
        public void run (final CompilationInfo nullInfo) throws IOException {
            ClassIndexManager.getDefault().writeLock (new ClassIndexManager.ExceptionAction<Void> () {
                
                @SuppressWarnings("fallthrough")
                public Void run () throws IOException {
                    boolean continuation = false;
                    try {
                    final WorkType type = work.getType();                        
                    switch (type) {
                        case FILTER_CHANGED:                            
                            try {
                                final MultiRootsWork mw = (MultiRootsWork) work;
                                final List<URL> roots = mw.getRoots();
                                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();                                
                                for (URL root: roots) {
                                    findDependencies (root, new Stack<URL>(), depGraph, null, false);
                                }
                                state = Utilities.topologicalSort(roots, depGraph);                                                                             
                                for (java.util.ListIterator<URL> it = state.listIterator(state.size()); it.hasPrevious(); ) {                
                                    final URL rootURL = it.previous();
                                    it.remove();
                                    updateFolder (rootURL,rootURL, true, handle);
                                }                                
                            } catch (final TopologicalSortException tse) {
                                    final IllegalStateException ise = new IllegalStateException ();                                
                                    throw (IllegalStateException) ise.initCause(tse);
                            }
                            break;
                        case COMPILE_BATCH:
                        {
                            assert handle == null;
                            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                            handle.start();
                            boolean completed = false;
                            try {
                                oldRoots = new HashSet<URL> (scannedRoots);
                                oldBinaries = new HashSet<URL> (scannedBinaries);
                                final List<ClassPath.Entry> entries = new LinkedList<ClassPath.Entry>();
                                entries.addAll (cp.entries());
                                entries.addAll (ucp.entries());
                                final List<ClassPath.Entry> binaryEntries = binCp.entries();
                                newBinaries = new HashSet<URL> ();
                                for (ClassPath.Entry entry : binaryEntries) {
                                    URL binRoot = entry.getURL();
                                    if (!oldBinaries.remove(binRoot)) {
                                        newBinaries.add (binRoot);
                                    }
                                }
                                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();
                                for (ClassPath.Entry entry : entries) {
                                    final URL rootURL = entry.getURL();
                                    findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries, true);
                                }                                
                                CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                deps.putAll(depGraph);
                                completed = true;
                            } catch (final TopologicalSortException tse) {
                                final IllegalStateException ise = new IllegalStateException ();                                
                                throw (IllegalStateException) ise.initCause(tse);
                            } finally {
                                if (!completed) {
                                    resetDirty();
                                }
                            }
                        }
                        case COMPILE_CONT:
                            boolean completed = false;
                            try {
                                if (!scanRoots()) {
                                    CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                    SourceAccessor.INSTANCE.runSpecialTask (CompileWorker.this, Source.Priority.MAX);
                                    continuation = true;
                                    return null;
                                }
                                while (isDirty()) {
                                    assert CompileWorker.this.state.isEmpty();                                    
                                    final List<ClassPath.Entry> entries = new LinkedList<ClassPath.Entry>();
                                    entries.addAll (cp.entries());
                                    entries.addAll (ucp.entries());                                    
                                    final List<ClassPath.Entry> binaryEntries = binCp.entries();
                                    newBinaries = new HashSet<URL> ();
                                    for (ClassPath.Entry entry : binaryEntries) {
                                        URL binRoot = entry.getURL();
                                        if (!scannedBinaries.contains(binRoot)) {
                                            newBinaries.add(binRoot);
                                        }
                                        else {
                                            oldBinaries.remove(binRoot);
                                        }
                                    }
                                    final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();
                                    for (ClassPath.Entry entry : entries) {
                                        final URL rootURL = entry.getURL();
                                        findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries, true);
                                    }
                                    try {
                                        CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                        deps.putAll(depGraph);
                                    } catch (final TopologicalSortException tse) {
                                        final IllegalStateException ise = new IllegalStateException ();
                                        throw (IllegalStateException) ise.initCause(tse);
                                    }
                                    if (!scanRoots ()) {
                                        CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                        SourceAccessor.INSTANCE.runSpecialTask (CompileWorker.this, Source.Priority.MAX);
                                        continuation = true;
                                        return null;
                                    }
                                }
                                completed = true;
                            } finally {
                                if (!completed && !continuation) {
                                    resetDirty ();
                                }
                            }                            
                            final ClassIndexManager cim = ClassIndexManager.getDefault();
                            scannedRoots.removeAll(oldRoots);
                            deps.keySet().remove(oldRoots);
                            for (URL oldRoot : oldRoots) {
                                cim.removeRoot(oldRoot);
//                                JavaFileFilterImplementation filter = filters.remove(oldRoot);
//                                if (filter != null && !filters.values().contains(filter)) {
//                                    filter.removeChangeListener(filterListener);
//                                }
                            }
                            scannedBinaries.removeAll (oldBinaries);
//                            final CachingArchiveProvider cap = CachingArchiveProvider.getDefault();
                            for (URL oldRoot : oldBinaries) {
                                cim.removeRoot(oldRoot);
//                                cap.removeArchive(oldRoot);
                            }
                            break;
                        case COMPILE:
                        {
                            try {
                                final SingleRootWork sw = (SingleRootWork) work;
                                final URL file = sw.getFile();
                                final URL root = sw.getRoot ();
                                if (sw.isFolder()) {
                                    handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_Updating"));
                                    handle.start();
                                    try {
                                        updateFolder (file, root, false, handle);
                                    } finally {
                                        handle.finish();
                                    }
                                }
                                else {
                                    updateFile (file,root);
                                }
                            //} catch (Abort abort) {
                            } catch (Exception abort) {
                                //Ignore abort
                            }                         
                            break;
                        }
                        case DELETE:
                        {
                            final SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot ();
                            delete (file, root, sw.isFolder());
                            break;                                 
                        }
                        case UPDATE_BINARY:
                        {
                            SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot();
                            updateBinary (file, root);
                            break;
                        }
                    }                                                
                    return null;                    
                } finally {
                    if (!continuation) {
                        synchronized (RepositoryUpdater.this) {
                            RepositoryUpdater.this.noSubmited--;
                            if (RepositoryUpdater.this.noSubmited == 0) {
                                RepositoryUpdater.this.notifyAll();
                            }
                        }
                        work.finished ();
                        if (handle != null) {
                            handle.finish ();
                        }
                    }
                }
            }});
        }
        
        private void findDependencies (final URL rootURL, final Stack<URL> cycleDetector, final Map<URL,List<URL>> depGraph,
            final Set<URL> binaries, final boolean useInitialState) {           
            if (useInitialState && RepositoryUpdater.this.scannedRoots.contains(rootURL)) {                                
                this.oldRoots.remove(rootURL);                        
                return;
            }
            if (depGraph.containsKey(rootURL)) {
                return;
            }                                                
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {               
                return;
            }
            
            // BEGIN TOR MODIFICATIONS
            // I don't want to start asking for the ClassPath of directories in the libraries
            // since these start yielding Java jars etc.
            if (true) {
                //if (useInitialState) {
                   depGraph.put(rootURL,new LinkedList<URL> ());

                    if (!RepositoryUpdater.this.scannedBinaries.contains(rootURL)) {
                        binaries.add (rootURL);
                    }
                    oldBinaries.remove(rootURL);
                //}
                return;
            }
            // END TOR MODIFICATIONS
            
            cycleDetector.push (rootURL);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
            final ClassPath[] pathsToResolve = new ClassPath[] {bootPath,compilePath};
            final List<URL> deps = new LinkedList<URL> ();
            for (int i=0; i< pathsToResolve.length; i++) {
                final ClassPath pathToResolve = pathsToResolve[i];
                if (pathToResolve != null) {
                    for (ClassPath.Entry entry : pathToResolve.entries()) {
                        final URL url = entry.getURL();
                        final URL[] sourceRoots = RepositoryUpdater.this.cpImpl.getSourceRootForBinaryRoot(url, pathToResolve, false);
                        if (sourceRoots != null) {
                            for (URL sourceRoot : sourceRoots) {
                                if (sourceRoot.equals (rootURL)) {
                                    this.ignoreExcludes.add (rootURL);
                                }
                                else if (!cycleDetector.contains(sourceRoot)) {
                                    deps.add (sourceRoot);
                                    findDependencies(sourceRoot, cycleDetector,depGraph, binaries, useInitialState);
                                }
                            }
                        }
                        else {
                            if (useInitialState) {
                                if (!RepositoryUpdater.this.scannedBinaries.contains(url)) {
                                    binaries.add (url);
                                }
                                oldBinaries.remove(url);
                            }
                        }
                    }
                }
            }
            depGraph.put(rootURL,deps);
            cycleDetector.pop ();
        }
        
        private boolean scanRoots () {
            
            for (Iterator<URL> it = this.newBinaries.iterator(); it.hasNext(); ) {
                if (this.canceled.getAndSet(false)) {                    
                    return false;
                }
                final URL rootURL = it.next();
                try {
                    it.remove();
                    final ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(rootURL,false);                                        
                    RepositoryUpdater.this.scannedBinaries.add (rootURL);                    
                    long startT = System.currentTimeMillis();
                    ci.getBinaryAnalyser().analyse(rootURL, handle);
                    long endT = System.currentTimeMillis();
                    if (PERF_TEST) {
                        try {
                            Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                            java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                            m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(endT - startT)});
                        } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                        }                            
                    }
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath) e;
                    }
                    else {
                        Exceptions.attachMessage(e, "While scanning: " + rootURL);
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            for (java.util.ListIterator<URL> it = this.state.listIterator(this.state.size()); it.hasPrevious(); ) {
                if (this.canceled.getAndSet(false)) {                    
                    return false;
                }
                try {
                    final URL rootURL = it.previous();
                    it.remove();                                                                                
                    if (!oldRoots.remove(rootURL) && !RepositoryUpdater.this.scannedRoots.contains(rootURL)) {
                        long startT = System.currentTimeMillis();                        
                        updateFolder (rootURL,rootURL, false, handle);
                        long endT = System.currentTimeMillis();
                        if (PERF_TEST) {
                            try {
                                Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                                java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                                m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(endT - startT)});
                            } catch (Exception e) {
                                    Exceptions.printStackTrace(e);
                            }
                        }
                        if (PREINDEXING) {
                            // How do I obtain the data folder for this puppy?
                            Index.preindex(rootURL);
                        }
                    }
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath) e;
                    }
                    else {
                        Exceptions.printStackTrace (e);
                    }
                }
            }
            return true;
        }
        
        private void updateFolder(final URL folder, final URL root, boolean clean, final ProgressHandle handle) throws IOException {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }            
            if (!rootFo.isFolder()) {
                LOGGER.warning("Source root has to be a folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }            
            final ClassPath sourcePath = ClassPath.getClassPath(rootFo,ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);            
            final boolean isInitialCompilation = folder.equals(root);            
            if (sourcePath == null || bootPath == null || compilePath == null) {
                LOGGER.warning("Ignoring root with no ClassPath: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                return;
            }            
            boolean isBoot = isInitialCompilation && ClassIndexManager.getDefault().isBootRoot(root);
            if (!isBoot) {
                String urlString = root.toExternalForm();
                if ((urlString.indexOf("/vendor/") != -1) || urlString.endsWith("jruby-javasupport/1.0.1/")) {
                    isBoot = true;
                }
            }
/*
                // XXX This is suspicious
            if (!clean && isInitialCompilation) {
                //Initial compilation  debug messages
                if (RepositoryUpdater.this.scannedRoots.contains(root)) {
                    return;
                }                
                LOGGER.fine("Scanning Root: " + FileUtil.getFileDisplayName(rootFo));    //NOI18N
            }
*/
            try {     
                File rootFile = FileUtil.toFile(rootFo);
                if (rootFile == null) {
                    FileObject jar = FileUtil.getArchiveFile(rootFo);
                    rootFile = FileUtil.toFile(jar);
                    if (rootFile == null) {
                        // Probably a jar, which sometimes ends up in my updateFolder now because the
                        // isBinary stuff etc. isn't working yet
                        return;
                    }
                }
                final File folderFile = isInitialCompilation ? rootFile : FileUtil.normalizeFile(new File (URI.create(folder.toExternalForm())));
                if (handle != null) {
                    final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_Scannig",rootFile.getAbsolutePath());
                    handle.setDisplayName(message);
                }
//                //Preprocessor support
                Object filter = null;
//                JavaFileFilterImplementation filter = filters.get(root);
//                if (filter == null) {
//                    filter = JavaFileFilterQuery.getFilter(rootFo);
//                    if (filter != null) {
//                        if (!filters.values().contains(filter)) {
//                            filter.addChangeListener(filterListener);
//                        }
//                        filters.put(root, filter);
//                    }
//                }
                List<ParserFile> toCompile = new LinkedList<ParserFile>();
                // TODO - remove these
                final File classCache = Index.getClassFolder(rootFile);
                final Map <String,List<File>> resources = getAllClassFiles(classCache, FileObjects.getRelativePath(rootFile,folderFile),true);
                final LazyFileList children = new LazyFileList(folderFile);
                ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
                assert uqImpl != null;
                SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;

                // If this is a boot class path, don't update it
                boolean isRootFolder = isBoot;
                // Known Rails user-project exceptions (not recorded as boot roots since 
                // they are in the user's project directories)
                if (folderFile.getName().equals("vendor") || folderFile.getName().equals("lib")) { // NOI18N
                    // lib? Won't that mess up my user projects?
                    isRootFolder = true;
                }
                if (isRootFolder) {
                    if (folderFile.exists() && folderFile.canRead()) {
                         if (sa.isUpToDate(null,folderFile.lastModified())) {
                             return;
                         }
                    }
                }                boolean invalidIndex = isInitialCompilation && !sa.isValid();
                Set<File> rs = new HashSet<File> ();
                ClassPath.Entry entry = null;
                final ClasspathInfo cpInfo;
                if (!this.ignoreExcludes.contains(root)) {
                    entry = getClassPathEntry(sourcePath, root);
                    cpInfo = ClasspathInfoAccessor.INSTANCE.create(bootPath,compilePath,sourcePath, filter, true,false);
                }
                else {
                    cpInfo = ClasspathInfoAccessor.INSTANCE.create(bootPath,compilePath,sourcePath, filter, true,true);
                }                
                
//                Set<ElementHandle<TypeElement>> removed = isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
//                Set<ElementHandle<TypeElement>> added =   isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
Set removed = null;
Set added = null;
                for (File child : children) {       
                    String offset = FileObjects.getRelativePath(rootFile,child);                    
                    if (entry == null || entry.includes(offset.replace(File.separatorChar,'/'))) {                                                
                        if (invalidIndex || clean || dirtyCrossFiles.remove(child.toURI())) {
                            toCompile.add (FileObjects.fileFileObject(child, rootFile, isBoot, null/* filter*/));
                        }
                        else {
                            final int index = offset.lastIndexOf('.');  //NOI18N
                            if (index > -1) {
                                offset = offset.substring(0,index);
                            }
                            List<File> files = resources.remove(offset);                        
                            if  (files==null) {
                                toCompile.add(FileObjects.fileFileObject(child, rootFile, isBoot, null/*filter*/));
                            } else {
//                                boolean rsf = files.get(0).getName().endsWith(FileObjects.RS);
                                if (files.get(0).lastModified() < child.lastModified()) {
                                    toCompile.add(FileObjects.fileFileObject(child, rootFile, isBoot, null/*filter*/));
                                    for (File toDelete : files) {                            
                                        toDelete.delete();
//                                        if (rsf) {                                    
//                                            rsf = false;
//                                        }
//                                        else {
                                            String className = FileObjects.getBinaryName(toDelete,classCache);
                                            sa.delete(className);
//                                            if (removed != null) {
//                                                removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                                            }
                                        }                            
//                                    }
//                                }
//                                else if (rsf) {
//                                    files.remove(0);
//                                    rs.addAll(files);
                                }                        
                            }
                        }
                    }
                }
                for (List<File> files : resources.values()) {
                    for (File toDelete : files) {
                        if (!rs.contains(toDelete)) {
                            toDelete.delete();
                            if (toDelete.getName().endsWith(FileObjects.SIG)) {
                                String className = FileObjects.getBinaryName(toDelete,classCache);                        
                                sa.delete(className);
//                                if (removed != null) {
//                                    removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                                }
                            }
                        }
                    }
                }
                if (!toCompile.isEmpty()) {
                    if (handle != null) {
                        // BEGIN TOR MODIFICATIONS
                        // Show message for "indexing" rather than compiling since I'm not keeping trees around etc - it's
                        // all used to populate Lucene at this point.
                        //final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompile",rootFile.getAbsolutePath());
                        String path = rootFile.getAbsolutePath();
                        // Shorten path by prefix to ruby location if possible
                        int rubyIndex = path.indexOf("jruby-1.0.1");
                        if (rubyIndex != -1) {
                            path = path.substring(rubyIndex);
                        }
                        final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_Analyzing",path);
                        handle.setDisplayName(message);
                    }
                    batchCompile(toCompile, rootFo, cpInfo, sa, dirtyCrossFiles, added, handle);
                }
                sa.store();
//                if (added != null) {
//                    assert removed != null;
//                    Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
//                    Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
//                    _at.removeAll(removed);
//                    _rt.removeAll(added);
//                    added.retainAll(removed);                                                                   //Changed
//                    uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
//                            _rt.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,_rt), 
//                            added.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,added));
//                }
            } finally {
                if (!clean && isInitialCompilation) {
                    RepositoryUpdater.this.scannedRoots.add(root);
                }
            }
        }
        
        private void updateFile (final URL file, final URL root) throws IOException {
            final FileObject fo = URLMapper.findFileObject(file);
            if (fo == null) {
                return;
            }

            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(fo.getMIMEType());
            if (language == null) {
                return;
            }
            
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
            if (uqImpl != null) {
                uqImpl.setDirty(null);
//                final JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(fo);
                ClasspathInfo cpInfo = ClasspathInfoAccessor.INSTANCE.create (fo, null/*filter*/, true, false);
                final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
                final File fileFile = FileUtil.toFile(fo);
                //final File classCache = Index.getClassFolder (rootFile);
                //final Map <String,List<File>> resources = getAllClassFiles (classCache, FileObjects.getRelativePath(rootFile, fileFile.getParentFile()),false);
//                String offset = FileObjects.getRelativePath (rootFile,fileFile);
//                final int index = offset.lastIndexOf('.');  //NOI18N
//                if (index > -1) {
//                    offset = offset.substring(0,index);
//                }
                //List<File> files = resources.remove (offset);                
                SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                //final Set<ElementHandle<TypeElement>> added = new HashSet<ElementHandle<TypeElement>>();
                //final Set<ElementHandle<TypeElement>> removed = new HashSet <ElementHandle<TypeElement>> ();
                // TODO: Handle deletions; is this only used for th sig files?
//                if (files != null) {
//                    for (File toDelete : files) {
//                        toDelete.delete();
//                        if (toDelete.getName().endsWith(FileObjects.SIG)) {
//                            String className = FileObjects.getBinaryName (toDelete,classCache);                                                           
//                            sa.delete (className);
//                            removed.add (ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                        }
//                    }
//                }
//                else {
//                    sa.delete(FileObjects.convertFolder2Package(offset, '/'));  //NOI18N
//                }
                ClassPath.Entry entry = getClassPathEntry (cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),root);
                if (entry == null || entry.includes(fo)) {
                    String sourceLevel = SourceLevelQuery.getSourceLevel(fo);
                    final CompilerListener listener = new CompilerListener ();
                    //final JavaFileManager fm = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);                
                    //JavaFileObject active = FileObjects.fileFileObject(fileFile, rootFile, filter);
                    ParserFile active = FileObjects.fileFileObject(fileFile, rootFile, false, null/*filter*/);
                    //JavacTaskImpl jt = JavaSourceAccessor.INSTANCE.createJavacTask(cpInfo, listener, sourceLevel);
                    ParserTaskImpl jt = SourceAccessor.INSTANCE.createParserTask(language, cpInfo, sourceLevel);
                    //jt.setTaskListener(listener);
                    jt.setParseListener(listener);
                    //Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {active});
                    Iterable<ParserResult> trees = jt.parse(new ParserFile[] { active });
                    //jt.enter();            
                    //jt.analyze ();
                    //dumpClasses(listener.getEnteredTypes(), fm, root.toExternalForm(), null, ...
                    //sa.analyse (trees, jt, fm, active, added);
                    sa.analyse (trees, jt, /*fm,*/ active);
                    listener.cleanDiagnostics();                    
                }
                sa.store();
//                Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
//                Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
//                _at.removeAll(removed);
//                _rt.removeAll(added);
//                added.retainAll(removed);                                                                   //Changed
//                uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
//                        _rt.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,_rt), 
//                        added.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,added));                
            }
        }
        
        private void delete (final URL file, final URL root, final boolean folder) throws IOException {
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
            assert "file".equals(file.getProtocol()) : "Unexpected protocol of URL: " + file;   //NOI18N
            final File fileFile = FileUtil.normalizeFile(new File (URI.create(file.toExternalForm())));
            final String offset = FileObjects.getRelativePath (rootFile,fileFile);
            assert offset != null && offset.length() > 0 : String.format("File %s not under root %s ", fileFile.getAbsolutePath(), rootFile.getAbsolutePath());  // NOI18N                        
            final File classCache = Index.getClassFolder (rootFile);
            File[] affectedFiles = null;
            if (folder) {
                final File container = new File (classCache, offset);
                affectedFiles = container.listFiles();
            }
            else {
                int slashIndex = offset.lastIndexOf (File.separatorChar);
                int dotIndex = offset.lastIndexOf('.');     //NOI18N
                final File container = slashIndex == -1 ? classCache : new File (classCache,offset.substring(0,slashIndex));
                final String name = offset.substring(slashIndex+1, dotIndex);
                final String[] patterns = new String[] {
                  name + '.',
                  name + '$'
                };
                final File[] content  = container.listFiles();
                if (content != null) {
                    final List<File> result = new ArrayList<File>(content.length);
                    for (File f : content) {
                        final String fname = f.getName();
                        if (fname.startsWith(patterns[0]) || fname.startsWith(patterns[1])) {
                            result.add(f);
                        }
                    }
                    affectedFiles = result.toArray(new File[result.size()]);
                }
            }
            if (affectedFiles != null && affectedFiles.length > 0) {
//                Set<ElementHandle<TypeElement>> removed = new HashSet<ElementHandle<TypeElement>>();
                final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
                assert uqImpl != null;                
                final SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                for (File f : affectedFiles) {
                    if (f.getName().endsWith(FileObjects.RS)) {
//                        List<File> rsFiles = new LinkedList<File>();
//                        readRSFile(f, classCache, rsFiles);
//                        for (File rsf : rsFiles) {
//                            String className = FileObjects.getBinaryName (rsf,classCache);                                                                        
//                            sa.delete (className);
//                            removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                            rsf.delete();
//                        }
                    }
                    else {
                        String className = FileObjects.getBinaryName (f,classCache);                                                                        
                        sa.delete (className);
//                        removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                    }
                    f.delete();                    
                }
                sa.store();                
//                uqImpl.typesEvent(null,new ClassIndexImplEvent(uqImpl, removed), null);
            }
        }
        
        private void updateBinary (final URL file, final URL root) throws IOException {            
//            CachingArchiveProvider.getDefault().clearArchive(root);                       
            File cacheFolder = Index.getClassFolder(root);
            FileObjects.deleteRecursively(cacheFolder);
            final BinaryAnalyser ba = ClassIndexManager.getDefault().createUsagesQuery(root, false).getBinaryAnalyser();
            ba.analyse(root, handle);
        }                
    }        
    
    static class LazyFileList implements Iterable<File> {
    
        private File root;

        public LazyFileList (final File root) {
            assert root != null;
            this.root = root;
        }

        public Iterator<File> iterator() {
            if (!root.exists()) {
                return Collections.<File>emptySet().iterator();
            }
            return new It (this.root);
        }


        private class It implements Iterator<File> {

            private final List<File> toDo = new LinkedList<File> ();

            public It (File root) {
                
                
//                // Special case: Rails on some systems (such as debian)
//                // creates symbolic links in the vendor directory back into Rails;
//                // I don't want to visit these links. 
//                // Arguably I should do this for all dirs - but it's a performance issue,
//                // and one I want to investigate more closely before making large changes;
//                // this specific fix addresses a serious recorded issue (93019)
//                if (root.getName().equals("vendor")) { // NOI18N
//                    ArrayList<File> list = new ArrayList<File>();
//                    for (File f : root.listFiles()) {
//                        
//                        // Skip vendor/rails - this is really a library which should
//                        // be matching other installations.
//                        // Ugh... I shouldn't skip it if I don't have it in the gems!
//                        if (skipVendorRails && f.getName().equals("rails")) { // NOI18N
//                            continue;
//                        }
//
//                        try {
//                            // See JDK issue 4042001 - need symbolic link support.
//                            // Workaround which will work in this scenario is a
//                            // to compare canonical paths with absolute paths
//                            if (f.getAbsolutePath().equals(f.getCanonicalPath())) {
//                                list.add(f);
//                            }
//                        } catch (IOException ioe) {
//                            Exceptions.printStackTrace(ioe);
//                        }
//                    }
//                    this.toDo.addAll(list);
//                } else {
                    // Normal path
                    
                    this.toDo.addAll (java.util.Arrays.asList(root.listFiles()));
//                }
            }

            public boolean hasNext() {
                while (!toDo.isEmpty()) {
                    File f = toDo.remove (0);   
                    final String name = f.getName();
                    if (f.isDirectory() && !ignoredDirectories.contains(name)/* && Utilities.isJavaIdentifier(name)*/) {
                        File[] content = f.listFiles();
                            for (int i=0,j=0;i<content.length;i++) {
                                f = content[i];
                                if (f.isFile()) {
                                    this.toDo.add(j++,f);
                                }
                                else {
                                    this.toDo.add(f);
                                }
                            }
                    }                    
                    else { // XXX How do I decide if it's a reasonable name?
//                        System.out.println("Should we scan " + name + "?");
//                    else if (name.endsWith('.'+JavaDataLoader.JAVA_EXTENSION) && !PACKAGE_INFO.equals(name) && f.length()>0) { //NOI18N
                        toDo.add(0,f);
                        return true;
                    }                                        
                }
                return false;
            }

            public File next() {
                return toDo.remove (0);
            }

            public void remove() {
                throw new UnsupportedOperationException ();
            }

        }            
    }
    
    
    private final class Delay {
        
        private final Timer timer;
        private final List<Work> tasks;
        
        public Delay () {
            this.timer = new Timer(RepositoryUpdater.class.getName());
            this.tasks = new LinkedList<Work> ();
        }
        
        public synchronized void post (final Work work) {
            assert work != null;
            this.tasks.add (work);
            this.timer.schedule(new DelayTask (work),DELAY);
        }
        
        public void cancel () {
            Work[] toCancel;
            synchronized (this) {
                toCancel = this.tasks.toArray (new Work[this.tasks.size()]);
            }
            for (Work w : toCancel) {
                if (w.workType == WorkType.COMPILE) {
                    w = new SingleRootWork (WorkType.DELETE,((SingleRootWork)w).file,
                        ((SingleRootWork)w).root,((SingleRootWork)w).isFolder,
                        w.latch);
                }
                CompileWorker cw = new CompileWorker (w);
                try {
                    cw.run (null);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        
        private class DelayTask extends TimerTask {
            
            final Work work;
            
            public DelayTask (final Work work) {
                this.work = work;
            }
            
            public void run() {
                submit(work);
                synchronized (Delay.this) {
                    Delay.this.tasks.remove (work);
                }                
            }

            public @Override boolean cancel() {
                boolean retValue = super.cancel();
                if (retValue) {
                    synchronized (Delay.this) {
                        Delay.this.tasks.remove (work);
                    }
                }
                return retValue;
            }                        
        }                
    }
    
    private static class CompilerListener implements /*DiagnosticListener<JavaFileObject>,*/LowMemoryListener, ParseListener {
                               
        final List<Error> errors = new LinkedList<Error> ();
        final List<Error> warnings = new LinkedList<Error> ();
//        final List<ClassSymbol> justEntered = new LinkedList<ClassSymbol> ();
        final List<ParserResult> justEntered = new LinkedList<ParserResult> ();
        final AtomicBoolean lowMemory = new AtomicBoolean ();
//        
        void cleanDiagnostics () {
            if (!this.errors.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Error msg : this.errors) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.errors.clear();
            }
            if (!this.warnings.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Error msg: this.warnings) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.warnings.clear();
            }
            this.justEntered.clear();
        }
        
        List<ParserResult> getEnteredTypes () {
            List<ParserResult> result = new ArrayList<ParserResult>(this.justEntered);
            this.justEntered.clear();
            return result;
        }
        
        public void error(Error error) {
            if (error.getSeverity() == Severity.ERROR) {
                this.errors.add(error);
            } else {
                this.warnings.add(error);
            }
        }
        
        public void exception(Exception exception) {
        }
        
//
//        public void report(final Diagnostic diagnosticMessage) {
//            Diagnostic.Kind kind = diagnosticMessage.getKind();
//            if ( kind == Diagnostic.Kind.ERROR) {
//                this.errors.add (diagnosticMessage);
//            }
//            else if (kind == Diagnostic.Kind.WARNING
//                   ||kind == Diagnostic.Kind.MANDATORY_WARNING) {
//                this.warnings.add (diagnosticMessage);
//            }
//        }
//        
        public void started(ParseEvent e) {
            
        }

        public void finished(ParseEvent event) {
            if (event.getKind() == ParseEvent.Kind.PARSE /*ENTER*/) {
                //final CompilationUnitTree unit = event.getCompilationUnit();
                final ParserResult result = event.getResult();
                if (result != null) {
                    this.justEntered.add(result);
                }
//                
//                for (Tree typeTree : unit.getTypeDecls()) {
//                    if (typeTree instanceof JCTree.JCClassDecl) {       //May be a JCTree.JCExpressionStatement in case of an error
//                        ClassSymbol sym = ((JCTree.JCClassDecl)typeTree).sym;
//                        if (sym != null) {
//                            if (sym.sourcefile == null) {
//                                sym.sourcefile = event.getSourceFile();
//                            }
//                            this.justEntered.add(sym);
//                        }
//                    }          
//                }
            }
        }
        
        public void lowMemory (final LowMemoryEvent event) {
            this.lowMemory.set(true);
        }
    }
    
    public static void batchCompile (final List<ParserFile> toCompile, final FileObject rootFo, final ClasspathInfo cpInfo, final SourceAnalyser sa,
        final Set<URI> dirtyFiles, final Set/*<? super ElementHandle<TypeElement>>*/ added, ProgressHandle handle) throws IOException {
        assert toCompile != null;
        assert rootFo != null;
        assert cpInfo != null;
        ParserFile active = null;
        //final JavaFileManager fileManager = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);
        final CompilerListener listener = new CompilerListener ();        
        LowMemoryNotifier.getDefault().addLowMemoryListener(listener);
        try {
            ParserTaskImpl jt = null;
            
            try {
                List<ParserFile> bigFiles = new LinkedList<ParserFile>();
                int state = 0; // TODO: Document what these states mean
                boolean isBigFile = false;
                final String sourceLevel = SourceLevelQuery.getSourceLevel(rootFo);
                int fileNumber = 0;
                int fileCount = toCompile.size();
                if (fileCount > 0) {
                    handle.switchToDeterminate(fileCount);
                }       
                while (!toCompile.isEmpty() || !bigFiles.isEmpty() || active != null) {
                    try {
                        if (listener.lowMemory.getAndSet(false)) {
                            if (jt != null) {
                                jt.finish();
                            }
                            jt = null;
                            if (state == 1) {
                                break;
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        if (active == null) {
                            if (!toCompile.isEmpty()) {
                                active = toCompile.remove(0);
                                isBigFile = false;
                            } else {
                                active = bigFiles.remove(0);
                                isBigFile = true;
                            }
                        }
                        
                        if (handle != null && active != null) {
                            if (fileCount > 0 && fileNumber <= fileCount) {
                                handle.progress(fileNumber);
                            }
                            fileNumber++;
                        }

                        // Change from what's going on in the Retouche updater:
                        // We could have many language implementations that want to index this root;
                        // we need to iterate through them and let each one of them index if they
                        // want to.
                        List<IndexerEntry> indexers = getIndexers();
                        assert indexers instanceof RandomAccess;
                        // We're gonna do this for every file in the filesystem - do cheaper iteration
                        // using indices rather than iterators
                        Language language = null;
                        for (int in = 0; in < indexers.size(); in++) {
                            IndexerEntry entry = indexers.get(in);
                            Indexer indexer = entry.getIndexer();
                            if (!indexer.isIndexable(active)) {
                                continue;
                            }

                            language = entry.getLanguage();

                            // Cache parser tasks per indexer
                            jt = entry.getParserTask();

                            if (jt == null) {
                                jt = SourceAccessor.INSTANCE.createParserTask(language, cpInfo/*, listener*/, sourceLevel);
                                jt.setParseListener(listener);
                                entry.setParserTask(jt);
                                LOGGER.fine("Created new ParserTask for: " + FileUtil.getFileDisplayName(rootFo));    //NOI18N
                            }
                        }   
                        
                        // Not an interesting source - such as a .zip file, a .gif file etc.
                        if (language == null) {
                            state  = 0;
                            active = null;
                            listener.cleanDiagnostics();
                            continue;
                        }
                        
                        Iterable<ParserResult> trees = jt.parse(new ParserFile[] { active });
                        if (listener.lowMemory.getAndSet(false)) {
                            jt.finish();
                            jt = null;
                            listener.cleanDiagnostics();
                            trees = null;
                            if (state == 1) {
                                if (isBigFile) {
                                    break;
                                } else {
                                    bigFiles.add(active);
                                    active = null;
                                    state = 0;
                                }
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
//                        Iterable<? extends TypeElement> types = jt.enterTrees(trees);
//                        dumpClasses (listener.getEnteredTypes(),fileManager,
//                                rootFo.getURL().toExternalForm(), dirtyFiles,
//                                com.sun.tools.javac.code.Types.instance(jt.getContext()),
//                                com.sun.tools.javac.util.Name.Table.instance(jt.getContext()));
//                        if (listener.lowMemory.getAndSet(false)) {
//                            jt.finish();
//                            jt = null;
//                            listener.cleanDiagnostics();
//                            trees = null;
//                            types = null;
//                            if (state == 1) {
//                                if (isBigFile) {
//                                    break;
//                                } else {
//                                    bigFiles.add(active);
//                                    active = null;
//                                    state = 0;
//                                }
//                            } else {
//                                state = 1;
//                            }
//                            System.gc();
//                            continue;
//                        }                        
//                        final JavaCompiler jc = JavaCompiler.instance(jt.getContext());
//                        final JavaFileObject finalActive = active;
//                        Filter f = new Filter() {
//                            public void process(Env<AttrContext> env) {
//                                try {
//                                    jc.attribute(env);
//                                } catch (Throwable t) {
//                                    if (finalActive.toUri().getPath().contains("org/openide/loaders/OpenSupport.java")) {
//                                        Exceptions.printStackTrace(t);
//                                    }
//                                }
//                            }
//                        };
//                        f.run(jc.todo, types);
//                        dumpClasses (listener.getEnteredTypes(), fileManager,
//                                rootFo.getURL().toExternalForm(), dirtyFiles,
//                                com.sun.tools.javac.code.Types.instance(jt.getContext()),
//                                com.sun.tools.javac.util.Name.Table.instance(jt.getContext()));
//                        if (listener.lowMemory.getAndSet(false)) {
//                            jt.finish();
//                            jt = null;
//                            listener.cleanDiagnostics();
//                            trees = null;
//                            types = null;
//                            if (state == 1) {
//                                if (isBigFile) {
//                                    break;
//                                } else {
//                                    bigFiles.add(active);
//                                    active = null;
//                                    state = 0;
//                                }
//                            } else {
//                                state = 1;
//                            }
//                            System.gc();
//                            continue;
//                        }
                        if (sa != null && trees != null) {
                            //sa.analyse(trees,jt, ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo), active, added);
                            sa.analyse(trees, jt,/* ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo),*/ active);
                        }
                        if (!listener.errors.isEmpty()) {
                            //Log.instance(jt.getContext()).nerrors = 0;
                            listener.cleanDiagnostics();
                        }
                        active = null;
                        state  = 0;
                    } catch (Exception a) {
                        //coupling error
                        //TODO: check if the source sig file ~ the source java file:
                        //couplingAbort(a, active);
                        if (jt != null) {
                            jt.finish();
                        }
                        jt = null;
                        listener.cleanDiagnostics();
                        active = null;
                        state = 0;
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }
                        else {
                            if (jt != null) {
                                jt.finish();
                            }
                            String activeURI;
                            if (active != null) {
                                activeURI = active.getNameExt();
                            } else {
                                activeURI = "unknown";
                            }
                            jt = null;
                            active = null;                            
                            listener.cleanDiagnostics();
                            //if (!(t instanceof Abort)) { // a javac Throwable                                
                                final ClassPath bootPath   = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                                final ClassPath classPath  = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                                final ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                                t = Exceptions.attachMessage(t,String.format("Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s",
                                        FileUtil.getFileDisplayName(rootFo),
                                        activeURI.toString(),
                                        bootPath == null   ? null : bootPath.toString(),
                                        classPath == null  ? null : classPath.toString(),
                                        sourcePath == null ? null : sourcePath.toString()
                                        ));
                                Exceptions.printStackTrace(t);
                            //}
                        }
                    }
                }
                if (state == 1) {
                    LOGGER.warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                }
            } finally {
                if (jt != null) {
                    jt.finish();
                }
            }
        } finally {
            clearIndexerParserTasks();
            LowMemoryNotifier.getDefault().removeLowMemoryListener(listener);
        }
    }
    
    private static Set<String> parseSet(String propertyName, String defaultValue) {
        StringTokenizer st = new StringTokenizer(System.getProperty(propertyName, defaultValue), " \t\n\r\f,-:+!");
        Set<String> result = new HashSet<String>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }
    
    
    public static Map<String,List<File>> getAllClassFiles (final File root, final String offset, boolean recursive) {
        assert root != null;
        Map<String,List<File>> result = new HashMap<String,List<File>> ();
        String rootName = root.getAbsolutePath();
        int len = rootName.length();
        if (rootName.charAt(len-1)!=File.separatorChar) {
            len++;
        }
        File folder = root;
        if (offset.length() > 0) {
            folder = new File (folder,offset);  //NOI18N
            if (!folder.exists() || !folder.isDirectory()) {
                return result;
            }
        }
        getAllClassFilesImpl (folder, root,len,result, recursive);
        return result;
    }
        
    private static void getAllClassFilesImpl (final File folder, final File root, final int oi, final Map<String,List<File>> result, final boolean recursive) {
        final File[] content = folder.listFiles();
        if (content == null) {
            LOGGER.info("IO error while listing folder: " + folder.getAbsolutePath() +" isDirectory: " + folder.isDirectory() +" canRead: " + folder.canRead());    //NOI18N
            return;
        }
        for (File f: content) {
            if (f.isDirectory() && recursive) {
                getAllClassFilesImpl(f, root, oi,result, recursive);                    
            }
            else {
                String path = f.getAbsolutePath();
                int extIndex = path.lastIndexOf('.');  //NO18N
                if (extIndex+1+FileObjects.RS.length() == path.length() && path.endsWith(FileObjects.RS)) {
                    path = path.substring (oi,extIndex);
                    List<File> files = result.get (path);
                    if (files == null) {
                        files = new LinkedList<File>();
                        result.put (path,files);
                    }
                    files.add(0,f); //the rs file has to be the first
                    try {
                        readRSFile (f,root, files);
                    } catch (IOException ioe) {
                        //The signature file is broken, report it but don't stop scanning
                        Exceptions.printStackTrace(ioe);
                    }
                }
                else if (extIndex+1+FileObjects.SIG.length() == path.length() && path.endsWith(FileObjects.SIG)) {
                    int index = path.indexOf('$',oi);  //NOI18N                    
                    if (index == -1) {
                        path = path.substring (oi,extIndex);
                    }
                    else {
                        path = path.substring (oi,index);
                    }                    
                    List<File> files = result.get (path);
                    if (files == null) {
                        files = new LinkedList<File>();
                        result.put (path,files);
                    }
                    files.add (f);
                }
            }
        }
    }
        
    private static void readRSFile (final File f, final File root, final List<? super File> files) throws IOException {
        BufferedReader in = new BufferedReader (new FileReader (f));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                File sf = new File (root, FileObjects.convertPackage2Folder(binaryName)+'.'+FileObjects.SIG);
                files.add(sf);                                        
            }
        } finally {
            in.close();
        }
    }
    
    
    private static ClassPath.Entry getClassPathEntry (final ClassPath cp, final URL root) {
        assert cp != null;
        assert root != null;
        for (ClassPath.Entry e : cp.entries()) {
            if (root.equals(e.getURL())) {
                return e;
            }
        }
        return null;
    }
    
    
    private class FilterListener implements ChangeListener {
            
        public void stateChanged(ChangeEvent event) {
//            Object source = event.getSource();
//            if (source instanceof JavaFileFilterImplementation) {
//                List<URL> dirtyRoots = new LinkedList<URL> ();
//                synchronized (filters) {
//                    for (Map.Entry<URL,JavaFileFilterImplementation> e : filters.entrySet()) {
//                        if (e.getValue() == source) {
//                            dirtyRoots.add(e.getKey());
//                        }
//                    }
//                }
//                submit(Work.filterChange(dirtyRoots));
//            }
        }
    }
    
    public static synchronized RepositoryUpdater getDefault () {
        if (instance == null) {
            instance = new RepositoryUpdater ();
        }
        return instance;
    }        
  
    // BEGIN TOR MODIFICATIONS
    // There could be multiple indexers (for different languages) that want
    // to index a given file. I will iterate over the indexers and let each
    // indexer have a chance to index every file. To do this I compute
    // a list of indexers in advance - and provide a place where we can
    // cache the parser tasks such that only one is created per indexer.
    
    private static List<IndexerEntry> indexers;
    
    private static List<IndexerEntry> getIndexers() {
        if (indexers == null) {
            indexers = new ArrayList<IndexerEntry>();
            for (Language language : LanguageRegistry.getInstance()) {
                Indexer indexer = language.getIndexer();
                if (indexer != null) {
                    IndexerEntry entry = new IndexerEntry(language, indexer);
                    indexers.add(entry);
                }
            }
        }
        
        return indexers;
    }
    
    private static void clearIndexerParserTasks() {
        for (IndexerEntry entry : getIndexers()) {
            entry.setParserTask(null);
        }
    }
    
    private static class IndexerEntry {
        private Language language;
        private Indexer indexer;
        private ParserTaskImpl task;
        
        IndexerEntry(Language language, Indexer indexer) {
            this.language = language;
            this.indexer = indexer;
        }
        
        Indexer getIndexer() {
            return indexer;
        }
        
        Language getLanguage() {
            return language;
        }
        
        ParserTaskImpl getParserTask() {
            return task;
        }
        
        void setParserTask(ParserTaskImpl task) {
            this.task = task;
        }
    } 
    
    // END TOR MODIFICATIONS
}