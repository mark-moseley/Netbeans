/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author Tomas Zezula
 */
public final class RepositoryUpdater implements PathRegistryListener, FileChangeListener, PropertyChangeListener, DocumentListener {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public static synchronized RepositoryUpdater getDefault() {
        if (instance == null) {
            instance = new RepositoryUpdater();
        }
        return instance;
    }

    public synchronized void start() {
        if (state == State.CREATED) {
            LOGGER.fine("Initializing..."); //NOI18N
            PathRegistry.getDefault().addPathRegistryListener(this);
            FileUtil.addFileChangeListener(this);
            EditorRegistry.addPropertyChangeListener(this);

            state = State.INITIALIZED;
            getWorker().schedule(new RootsWork(scannedRoots, scannedBinaries) {
                public @Override void getDone() {
                    try {
                        super.getDone();
                    } finally {
                        if (state == State.INITIALIZED) {
                            synchronized (RepositoryUpdater.this) {
                                if (state == State.INITIALIZED) {
                                    state = State.INITIALIZED_AFTER_FIRST_SCAN;
                                }
                            }
                        }
                    }
                }
            }, false);
        }
    }

    public synchronized void stop() {
        state = State.CLOSED;
        LOGGER.fine("Closing..."); //NOI18N

        PathRegistry.getDefault().removePathRegistryListener(this);
        FileUtil.removeFileChangeListener(this);
        EditorRegistry.removePropertyChangeListener(this);

        getWorker().cancelAll();
    }

    public boolean isScanInProgress() {
        return getWorker().isWorking();
    }

    /**
     * Schedules new job for indexing files under a root. This method forcible
     * reindexes all files in the job without checking timestamps.
     *
     * @param rootUrl The root that should be reindexed.
     * @param fileUrls Files under the root. Files that are not under the <code>rootUrl</code>
     *   are ignored. Can be <code>null</code> in which case all files under the root
     *   will be reindexed.
     * @param followUpJob If <code>true</code> the indexers will be notified that
     *   they are indexing follow up files (ie. files that one of the indexers involved
     *   in earlier indexing job requested to reindex) in contrast to files that are
     *   being reindexed due to ordinary change events (eg. when classpath roots are
     *   added/removed, file is modified, editor tabs are switched, etc).
     */
    public void addIndexingJob(URL rootUrl, Collection<? extends URL> fileUrls, boolean followUpJob, boolean wait) {
        assert rootUrl != null;

        FileObject root = URLMapper.findFileObject(rootUrl);
        assert root != null : rootUrl + " can't be translated to FileObject"; //NOI18N
        if (root == null) {
            return;
        }

        FileListWork flw = null;
        if (fileUrls != null && fileUrls.size() > 0) {
            Set<FileObject> files = new HashSet<FileObject>();
            for(URL fileUrl : fileUrls) {
                FileObject file = URLMapper.findFileObject(fileUrl);
                if (file != null) {
                    if (FileUtil.isParentOf(root, file)) {
                        files.add(file);
                    } else {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.warning(file + " does not lie under " + root + ", not indexing it"); //NOI18N
                        }
                    }
                }
            }

            if (files.size() > 0) {
                flw = new FileListWork(rootUrl, files, followUpJob);
            }
        } else {
            flw = new FileListWork(rootUrl, followUpJob);
        }

        if (flw != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Scheduling index refreshing: root=" + rootUrl + ", files=" + fileUrls); //NOI18N
            }

            getWorker().schedule(flw, wait);
        }
    }
    
    // -----------------------------------------------------------------------
    // PathRegistryListener implementation
    // -----------------------------------------------------------------------

    public void pathsChanged(PathRegistryEvent event) {
        assert event != null;
        if (LOGGER.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Paths changed:\n"); //NOI18N
            for(PathRegistryEvent.Change c : event.getChanges()) {
                sb.append(" event=").append(c.getEventKind()); //NOI18N
                sb.append(" pathKind=").append(c.getPathKind()); //NOI18N
                sb.append(" pathType=").append(c.getPathType()); //NOI18N
                sb.append(" affected paths:\n"); //NOI18N
                Collection<? extends ClassPath> paths = c.getAffectedPaths();
                if (paths != null) {
                    for(ClassPath cp : paths) {
                        sb.append("  \""); //NOI18N
                        sb.append(cp.toString(ClassPath.PathConversionMode.PRINT));
                        sb.append("\"\n"); //NOI18N
                    }
                }
                sb.append("--\n"); //NOI18N
            }
            sb.append("====\n"); //NOI18N
            LOGGER.fine(sb.toString());
        }
        getWorker().schedule(new RootsWork(scannedRoots, scannedBinaries), false);
    }

    // -----------------------------------------------------------------------
    // FileChangeListener implementation
    // -----------------------------------------------------------------------

    public void fileFolderCreated(FileEvent fe) {
        //In ideal case this should do nothing,
        //but in Netbeans newlly created folder may
        //already contain files
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot(fo);
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Folder created: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if ( root != null && VisibilityQuery.getDefault().isVisible(fo)) {
            getWorker().schedule(new FileListWork(root, Collections.singleton(fo), false), false);
        }
    }

    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File created: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null && VisibilityQuery.getDefault().isVisible(fo) &&
            FileUtil.getMIMEType(fo, PathRecognizerRegistry.getDefault().getMimeTypesAsArray()) != null)
        {
            getWorker().schedule(new FileListWork(root, Collections.singleton(fo), false), false);
        }
    }

    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File modified: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null && VisibilityQuery.getDefault().isVisible(fo) &&
            FileUtil.getMIMEType(fo, PathRecognizerRegistry.getDefault().getMimeTypesAsArray()) != null)
        {
            getWorker().schedule(new FileListWork(root, Collections.singleton(fo), false), false);
        }
    }

    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();
        if (fo.isFolder()) {
            // filtering out deleted folders
            return;
        }

        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File deleted: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null &&  VisibilityQuery.getDefault().isVisible(fo)
            /*&& FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null*/) {
            getWorker().schedule(new DeleteWork(root, fo), false);
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        // XXX: what should we do here? Mimic fileDeleted() followed by fileDataCreated()?
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // assuming attributes change does not mean change in a file type
    }

    // -----------------------------------------------------------------------
    // PropertyChangeListener implementation
    // -----------------------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        assert SwingUtilities.isEventDispatchThread() : "Changes in focused editor component should be delivered on AWT"; //NOI18N
        
        List<? extends JTextComponent> components = Collections.<JTextComponent>emptyList();

        if (evt.getPropertyName() == null) {
            components = EditorRegistry.componentList();

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
            if (evt.getOldValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getOldValue();
                components = Collections.singletonList(jtc);
                handleActiveDocumentChange(jtc.getDocument(), null);
            }
            
        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
            if (evt.getNewValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getNewValue();
                components = Collections.singletonList(jtc);
                handleActiveDocumentChange(null, jtc.getDocument());
            }

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUSED_DOCUMENT_PROPERTY)) {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc == null) {
                jtc = EditorRegistry.lastFocusedComponent();
            }
            if (jtc != null) {
                components = Collections.singletonList(jtc);
            }

            handleActiveDocumentChange((Document) evt.getOldValue(), (Document) evt.getNewValue());
        }

        if (components.size() > 0) {
            Map<URL, FileListWork> jobs = new HashMap<URL, FileListWork>();
            for(JTextComponent jtc : components) {
                Document d = jtc.getDocument();
                FileObject f = NbEditorUtilities.getFileObject(d);
                if (f != null) {
                    URL root = getOwningSourceRoot(f);
                    if (root != null) {
                        long version = DocumentUtilities.getDocumentVersion(d);
                        Long lastSeenVersion = (Long) d.getProperty(PROP_LAST_SEEN_VERSION);

                        // check if we've ever seen this document, if it supports versioning
                        // and if so then if the version seen last time is the same as the current one
                        if (lastSeenVersion == null || version == 0 || lastSeenVersion < version) {
                            d.putProperty(PROP_LAST_SEEN_VERSION, version);

                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Document modified: " + FileUtil.getFileDisplayName(f) + " Owner: " + root); //NOI18N
                            }

                            FileListWork job = jobs.get(root);
                            if (job == null) {
                                job = new FileListWork(root, Collections.singleton(f), false);
                                jobs.put(root, job);
                            } else {
                                job.addFile(f);
                            }
                        }
                    }
                }
            }

            for(FileListWork job : jobs.values()) {
                getWorker().schedule(job, false);
            }
        }
    }

    // -----------------------------------------------------------------------
    // DocumentListener implementation
    // -----------------------------------------------------------------------

    public void changedUpdate(DocumentEvent e) {
        // no document modification
    }

    public void insertUpdate(DocumentEvent e) {
        removeUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        final Reference<Document> ref = activeDocumentRef;
        Document activeDocument = ref == null ? null : ref.get();
        Document document = e.getDocument();

        FileObject f = NbEditorUtilities.getFileObject(document);
        if (f != null) {
            URL root = getOwningSourceRoot(f);
            if (root != null) {
                if (activeDocument == document) {
                    // An active document was modified, we've indexed that document berfore,
                    // so mark it dirty
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Active document modified: " + FileUtil.getFileDisplayName(f) + " Owner: " + root); //NOI18N
                    }

                    Collection<? extends Indexable> dirty = Collections.singleton(SPIAccessor.getInstance().create(new FileObjectIndexable(URLMapper.findFileObject(root), f)));
                    String mimeType = DocumentUtilities.getMimeType(document);
                    Collection<? extends CustomIndexerFactory> customIndexerFactories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    Collection<? extends EmbeddingIndexerFactory> embeddingIndexerFactories = MimeLookup.getLookup(mimeType).lookupAll(EmbeddingIndexerFactory.class);

                    for(CustomIndexerFactory factory : customIndexerFactories) {
                        try {
                            Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root), root,
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }

                    for(EmbeddingIndexerFactory factory : embeddingIndexerFactories) {
                        try {
                            Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root), root,
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }
                } else {
                    // an odd event, maybe we could just ignore it
                    try {
                        addIndexingJob(root, Collections.singleton(f.getURL()), false, false);
                    } catch (FileStateInvalidException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static RepositoryUpdater instance;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N

    private static final String PROP_LAST_SEEN_VERSION = RepositoryUpdater.class.getName() + "-last-seen-document-version"; //NOI18N
    
    private final Set<URL>scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedUnknown = Collections.synchronizedSet(new HashSet<URL>());

    private volatile State state = State.CREATED;
    private volatile Task worker;

    private volatile Reference<Document> activeDocumentRef = null;

    private RepositoryUpdater () {
        // no-op
    }

    private void handleActiveDocumentChange(Document deactivated, Document activated) {
        Document activeDocument = activeDocumentRef == null ? null : activeDocumentRef.get();

        if (deactivated != null && deactivated == activeDocument) {
            activeDocument.removeDocumentListener(this);
            activeDocumentRef = null;
            LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }

        if (activated != null && activated != activeDocument) {
            if (activeDocument != null) {
                activeDocument.removeDocumentListener(this);
                LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
            }

            activeDocument = activated;
            activeDocumentRef = new WeakReference<Document>(activeDocument);
            
            activeDocument.addDocumentListener(this);
            LOGGER.log(Level.FINE, "Registering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }
    }
    
    private Task getWorker () {
        Task t = this.worker;
        if (t == null) {
            synchronized (this) {
                if (this.worker == null) {
                    this.worker = new Task ();
                }
                t = this.worker;
            }
        }
        return t;
    }

    private URL getOwningSourceRoot(final FileObject fo) {
        if (fo == null) {
            return null;
        }
        List<URL> clone = new ArrayList<URL> (this.scannedRoots);
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }

    enum State {CREATED, INITIALIZED, INITIALIZED_AFTER_FIRST_SCAN, CLOSED};

    private static abstract class Work {

        private final boolean followUpJob;
        private final CountDownLatch latch;
        private ProgressHandle progressHandle = null;
        
        protected Work(boolean followUpJob) {
            this.followUpJob = followUpJob;
            this.latch = new CountDownLatch(1);
        }

        protected final void updateProgress(String message) {
            assert message != null;
            if (progressHandle == null) {
                return;
            }
            progressHandle.progress(message);
        }

        protected final void updateProgress(URL currentlyScannedRoot) {
            assert currentlyScannedRoot != null;
            if (progressHandle == null) {
                return;
            }
            URL tmp = FileUtil.getArchiveFile(currentlyScannedRoot);
            if (tmp == null) {
                tmp = currentlyScannedRoot;
            }
            try {
                if ("file".equals(tmp.getProtocol())) { //NOI18N
                    final File file = new File(new URI(tmp.toString()));
                    progressHandle.progress(file.getAbsolutePath());
                }
                else {
                    progressHandle.progress(tmp.toString());
                }
            } catch (URISyntaxException ex) {
                progressHandle.progress(tmp.toString());
            }
        }

        protected final void index (final Map<String,Collection<Indexable>> resources, final Collection<Indexable> deleted, final URL root) throws IOException {
            LinkedList<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                //First use all custom indexers
                Set<String> allMimeTypes = Util.getAllMimeTypes();
                for (String mimeType : allMimeTypes) {
                    final Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.fine("Using CustomIndexerFactories(" + mimeType + "): " + factories); //NOI18N
                    }

                    boolean supportsEmbeddings = true;
                    try {
                        for (CustomIndexerFactory factory : factories) {
                            boolean b = factory.supportsEmbeddedIndexers();
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.fine("CustomIndexerFactory: " + factory + ", supportsEmbeddedIndexers=" + b); //NOI18N
                            }

                            supportsEmbeddings &= b;
                            final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob);
                            transactionContexts.add(ctx);

                            if (deleted != null && deleted.size() > 0) {
                                factory.filesDeleted(deleted, ctx);
                            }
                            
                            final Collection<? extends Indexable> indexables = resources.get(mimeType);
                            if (indexables != null && indexables.size() > 0) {
                                final CustomIndexer indexer = factory.createIndexer();
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.fine("Indexing " + indexables.size() + " indexables; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                                }
                                SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                            }
                        }
                    } finally {
                        if (!supportsEmbeddings) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Removing roots for " + mimeType + ", indexed by custom indexers, embedded indexers forbidden"); //NOI18N
                            }
                            resources.remove(mimeType);
                        }
                    }
                }
                //Then use slow gsf like indexers
                final List<Indexable> toIndex = new LinkedList<Indexable>();
                for (Collection<Indexable> data : resources.values()) {
                    toIndex.addAll(data);
                }

                LOGGER.log(Level.FINE, "Using EmbeddingIndexers for {0}", toIndex); //NOI18N

                final SourceIndexer si = new SourceIndexer(root, cacheRoot, followUpJob);
                si.index(toIndex, deleted, transactionContexts);
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }
        }

        protected abstract void getDone();

        public final void doTheWork() {
            try {
                getDone();
            } finally {
                latch.countDown();
            }
        }

        public final void waitUntilDone() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }

    } // End of Work class

    private static final class FileListWork extends Work {

        private final URL root;
        private final Collection<FileObject> files;

        public FileListWork (URL root, boolean followUpJob) {
            super(followUpJob);

            assert root != null;
            this.root = root;
            this.files = null;
        }

        public FileListWork (URL root, Collection<FileObject> files, boolean followUpJob) {
            super(followUpJob);
            
            assert root != null;
            assert files != null && files.size() > 0;
            this.root = root;
            this.files = new HashSet<FileObject>();
            this.files.addAll(files);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("FileListWork: root=" + root + ", file=" + files); //NOI18N
            }
        }

        public void addFile(FileObject f) {
            assert f != null;
            assert FileUtil.isParentOf(URLMapper.findFileObject(root), f) : "File " + f + " does not belong under the root: " + root; //NOI18N
            files.add(f);
        }

        public @Override void getDone() {
            updateProgress(root);
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                try {
                    final Crawler crawler = files == null ?
                        new FileObjectCrawler(rootFo, false) : // rescan the whole root (no timestamp check)
                        new FileObjectCrawler(rootFo, files.toArray(new FileObject[files.size()]), false); // rescan selected files (no timestamp check)

                    final Map<String,Collection<Indexable>> resources = crawler.getResources();
                    index (resources, Collections.<Indexable>emptyList(), root);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            TEST_LOGGER.log(Level.FINEST, "filelist"); //NOI18N
        }

    } // End of FileListWork

    private static final class DeleteWork extends Work {

        private final URL root;
        private final FileObject[] files;

        public DeleteWork (URL root, FileObject file) {
            super(false);
            
            assert root != null;
            assert file != null;
            this.root = root;
            this.files = new FileObject[] { file };
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("DeleteWork: root=" + root + ", file=" + file);
            }
        }

        public @Override void getDone() {
            updateProgress(root);
            try {
                final ArrayList<Indexable> indexables = new ArrayList<Indexable>(files.length);
                final FileObject rootFo = URLMapper.findFileObject(root);
                for (int i=0; i< files.length; i++) {
                    indexables.add(SPIAccessor.getInstance().create(new DeletedIndexable (root, FileUtil.getRelativePath(rootFo, files[i]))));
                }
                index(Collections.<String,Collection<Indexable>>emptyMap(), indexables, root);
                TEST_LOGGER.log(Level.FINEST, "delete"); //NOI18N
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

    } // End of FileListWork

    private static class RootsWork extends Work {

        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;

        public RootsWork (Set<URL> scannedRoots, Set<URL> scannedBinaries) {
            super(false);
            
            this.scannedRoots = scannedRoots;
            this.scannedBinaries = scannedBinaries;
        }

        public @Override void getDone() {
            try {
                updateProgress(NbBundle.getMessage(RepositoryUpdater.class, "MSG_ProjectDependencies")); //NOI18N
                final DependenciesContext ctx = new DependenciesContext(scannedRoots, scannedBinaries, true);
                final List<URL> newRoots = new LinkedList<URL>();
                newRoots.addAll(PathRegistry.getDefault().getSources());
                newRoots.addAll(PathRegistry.getDefault().getLibraries());
                ctx.newBinaries.addAll(PathRegistry.getDefault().getBinaryLibraries());
                for (Iterator<URL> it = ctx.newBinaries.iterator(); it.hasNext();) {
                    if (ctx.oldBinaries.remove(it.next())) {
                        it.remove();
                    }
                }
                ctx.newBinaries.removeAll(ctx.oldBinaries);
                newRoots.addAll(PathRegistry.getDefault().getUnknownRoots());

                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();

                for (URL url : newRoots) {
                    findDependencies (url, depGraph, ctx, PathRecognizerRegistry.getDefault().getLibraryIds(), PathRecognizerRegistry.getDefault().getBinaryLibraryIds());
                }
                
                ctx.newRoots.addAll(org.openide.util.Utilities.topologicalSort(depGraph.keySet(), depGraph));
                Collections.reverse(ctx.newRoots);

                scanBinaries(ctx);
                scanSources(ctx);
                ctx.scannedRoots.removeAll(ctx.oldRoots);
                ctx.scannedBinaries.removeAll(ctx.oldBinaries);
            } catch (final TopologicalSortException tse) {
                final IllegalStateException ise = new IllegalStateException ();
                throw (IllegalStateException) ise.initCause(tse);
            }
        }
        
        private void findDependencies(
                final URL rootURL,
                final Map<URL,
                List<URL>> depGraph,
                DependenciesContext ctx,
                final Set<String> libraryClassPathIds,
                final Set<String> binaryLibraryClassPathIds)
        {
            if (ctx.useInitialState && ctx.scannedRoots.contains(rootURL)) {
                ctx.oldRoots.remove(rootURL);
                return;
            }
            if (depGraph.containsKey(rootURL)) {
                return;
            }
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {
                return;
            }

            final List<URL> deps = new LinkedList<URL>();
            ctx.cycleDetector.push(rootURL);
            try {
                { // libraries
                    final List<ClassPath> libraryPathToResolve = new ArrayList<ClassPath>(libraryClassPathIds.size());
                    for (String id : libraryClassPathIds) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            libraryPathToResolve.add(cp);
                        }
                    }

                    for (ClassPath cp : libraryPathToResolve) {
                        for (ClassPath.Entry entry : cp.entries()) {
                            final URL sourceRoot = entry.getURL();
                            if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                deps.add(sourceRoot);
                                findDependencies(sourceRoot, depGraph, ctx, libraryClassPathIds, binaryLibraryClassPathIds);
                            }
                        }
                    }
                }

                { // binary libraries
                    final List<ClassPath> binaryLibraryPathToResolve = new ArrayList<ClassPath>(binaryLibraryClassPathIds.size());
                    for (String id : binaryLibraryClassPathIds) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            binaryLibraryPathToResolve.add(cp);
                        }
                    }

                    for (ClassPath cp : binaryLibraryPathToResolve) {
                        for (ClassPath.Entry entry : cp.entries()) {
                            final URL url = entry.getURL();
                            final URL[] sourceRoots = PathRegistry.getDefault().sourceForBinaryQuery(url, cp, false);
                            if (sourceRoots != null) {
                                for (URL sourceRoot : sourceRoots) {
                                    if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                        deps.add(sourceRoot);
                                        findDependencies(sourceRoot, depGraph, ctx, libraryClassPathIds, binaryLibraryClassPathIds);
                                    }
                                }
                            }
                            else {
                                //What does it mean?
                                if (ctx.useInitialState) {
                                    if (!ctx.scannedBinaries.contains(url)) {
                                        ctx.newBinaries.add (url);
                                    }
                                    ctx.oldBinaries.remove(url);
                                }
                            }
                        }
                    }
                }
            } finally {
                ctx.cycleDetector.pop();
            }

            depGraph.put(rootURL, deps);
        }

        private void scanBinaries (final DependenciesContext ctx) {
            assert ctx != null;
            for (URL binary : ctx.newBinaries) {
                try {
                    updateProgress(binary);
                    scanBinary (binary);
                    ctx.scannedBinaries.add(binary);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            TEST_LOGGER.log(Level.FINEST, "scanBinary", ctx.newBinaries);       //NOI18N
        }

        private void scanBinary(URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning binary root: {0}", root); //NOI18N

            List<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo != null) {
                    final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                    final File archiveOrDir = FileUtil.archiveOrDirForURL(root);
                    assert archiveOrDir != null;
                    final FileObject archiveOrDirFo = FileUtil.toFileObject(archiveOrDir);
                    assert archiveOrDirFo != null;

                    String mimeType = archiveOrDirFo.isFolder() ? "" : archiveOrDirFo.getMIMEType(); //NOI18N
                    final Collection<? extends BinaryIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(BinaryIndexerFactory.class);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.fine("Using CustomIndexerFactories(" + mimeType + "): " + factories); //NOI18N
                    }

                    for(BinaryIndexerFactory f : factories) {
                        final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, f.getIndexerName(), f.getIndexVersion(), null, false);
                        transactionContexts.add(ctx);

                        final BinaryIndexer indexer = f.createIndexer();
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Indexing binary " + root + " using " + indexer); //NOI18N
                        }
                        SPIAccessor.getInstance().index(indexer, ctx);
                    }
                }
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }
        }

        private void scanSources  (final DependenciesContext ctx) {
            assert ctx != null;
            for (URL source : ctx.newRoots) {
                try {
                    updateProgress(source);
                    scanSource (source);
                    ctx.scannedRoots.add(source);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }

            }
            TEST_LOGGER.log(Level.FINEST, "scanSources", ctx.newRoots);         //NOI18N
        }

        private void scanSource (URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning sources root: {0}", root); //NOI18N

            //todo: optimize for java.io.Files
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                final Crawler crawler = new FileObjectCrawler(rootFo, true);
                final Map<String,Collection<Indexable>> resources = crawler.getResources();
                final Collection<Indexable> deleted = crawler.getDeletedResources();
                index (resources, deleted, root);
            }
        }
    } // End of RootsWork class

    private static final class Task extends ParserResultTask {

        // -------------------------------------------------------------------
        // Public implementation
        // -------------------------------------------------------------------

        public void schedule (Work work, boolean wait) {
            boolean enforceWork = false;
            boolean waitForWork = false;

            synchronized (todo) {
                assert work != null;
                if (!allCancelled) {
                    if (wait && Utilities.holdsParserLock()) {
                        enforceWork = true;
                    } else {
                        LOGGER.log(Level.FINE, "Scheduling {0}", work);
                        todo.add(work);
                        if (!scheduled) {
                            scheduled = true;
                            Utilities.scheduleSpecialTask(this);
                        }
                        waitForWork = wait;
                    }
                }
            }

            if (enforceWork) {
                // XXX: this will not set the isWorking() flag, which is strictly speaking
                // wrong, but probably won't harm anything
                LOGGER.log(Level.FINE, "Enforcing {0}", work); //NOI18N
                work.doTheWork();
            } else if (waitForWork) {
                LOGGER.log(Level.FINE, "Waiting for {0}", work); //NOI18N
                work.waitUntilDone();
            }
        }

        public void cancelAll() {
            synchronized (todo) {
                allCancelled = true;
                todo.clear();
                
                while (scheduled) {
                    try {
                        todo.wait(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        }

        public boolean isWorking() {
            synchronized (todo) {
                return scheduled;
            }
        }

        // -------------------------------------------------------------------
        // ParserResultTask implementation
        // -------------------------------------------------------------------

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return null;
        }

        @Override
        public void cancel() {
            // this task is not cancellable by the Parsing infrastructure
        }

        @Override
        public void run(Result nil, final SchedulerEvent nothing) {
            try {
                _run();
            } finally {
                synchronized (todo) {
                    scheduled = false;
                }
            }
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private final List<Work> todo = new LinkedList<Work>();
        private boolean scheduled = false;
        private boolean allCancelled = false;

        private void _run() {
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class, "MSG_BackgroundCompileStart")); //NOI18N
            progressHandle.start();

            try {
                for(Work work = getWork(); work != null; work = getWork()) {
                    work.progressHandle = progressHandle;
                    try {
                        work.doTheWork();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        LOGGER.log(Level.WARNING, null, t);
                    } finally {
                        work.progressHandle = null;
                    }
                }
            } finally {
                progressHandle.finish();
            }
        }
        
        private Work getWork () {
            synchronized (todo) {
                if (todo.size() > 0) {
                    return todo.remove(0);
                } else {
                    return null;
                }
            }
        }

    } // End of Task class

    private static final class DependenciesContext {

        private final Set<URL> oldRoots;
        private final Set<URL> oldBinaries;
        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;
        private final Stack<URL> cycleDetector;
        private final List<URL> newRoots;
        private final Set<URL> newBinaries;
        private final boolean useInitialState;

        public DependenciesContext (final Set<URL> scannedRoots, final Set<URL> scannedBinaries, boolean useInitialState) {
            assert scannedRoots != null;
            assert scannedBinaries != null;
            this.scannedRoots = scannedRoots;
            this.scannedBinaries = scannedBinaries;
            this.useInitialState = useInitialState;
            cycleDetector = new Stack<URL>();
            oldRoots = new HashSet<URL> (scannedRoots);
            oldBinaries = new HashSet<URL> (scannedBinaries);
            this.newRoots = new ArrayList<URL>();
            this.newBinaries = new HashSet<URL>();
        }
    } // End of DependenciesContext class

    // -----------------------------------------------------------------------
    // Methods for tests
    // -----------------------------------------------------------------------

    /**
     * Used by unit tests
     * @return
     */
    /* test */ State getState () {
        return state;
    }

    //Unit test method
    /* test */ Set<URL> getScannedBinaries () {
        return this.scannedBinaries;
    }

    //Unit test method
    /* test */ Set<URL> getScannedSources () {
        return this.scannedRoots;
    }

    //Unit test method
    /* test */ Set<URL> getScannedUnknowns () {
        return this.scannedUnknown;
    }
}
