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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.editor.AtomicLockEvent;
import org.netbeans.editor.AtomicLockListener;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingActivityInterceptor;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author Tomas Zezula
 */
public final class RepositoryUpdater implements PathRegistryListener, FileChangeListener, PropertyChangeListener, DocumentListener, AtomicLockListener {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public static synchronized RepositoryUpdater getDefault() {
        if (instance == null) {
            instance = new RepositoryUpdater();
        }
        return instance;
    }

    public void start(boolean force) {
        Work work = null;

        synchronized (this) {
            if (state == State.CREATED) {
                state = State.STARTED;
                LOGGER.fine("Initializing..."); //NOI18N
                this.indexingActivityInterceptors = Lookup.getDefault().lookupResult(IndexingActivityInterceptor.class);
                PathRegistry.getDefault().addPathRegistryListener(this);
                FileUtil.addFileChangeListener(this);
                EditorRegistry.addPropertyChangeListener(this);

                if (force) {
                    work = new InitialRootsWork(scannedRoots2Dependencies, scannedBinaries, false);
                }
            }
        }

        if (work != null) {
            scheduleWork(work, false);
        }
    }

    public void stop() {
        boolean cancel = false;

        synchronized (this) {
            if (state != State.STOPPED) {
                state = State.STOPPED;
                LOGGER.fine("Closing..."); //NOI18N

                PathRegistry.getDefault().removePathRegistryListener(this);
                FileUtil.removeFileChangeListener(this);
                EditorRegistry.removePropertyChangeListener(this);

                cancel = true;
            }
        }

        if (cancel) {
            getWorker().cancelAll();
        }
    }

    public boolean isScanInProgress() {
        boolean beforeInitialScanStarted;
        synchronized (this) {
            beforeInitialScanStarted = state == State.CREATED || state == State.STARTED;
        }
        return beforeInitialScanStarted || getWorker().isWorking() || !PathRegistry.getDefault().isFinished();
    }

    // returns false when timed out
    public boolean waitUntilFinished(long timeout) throws InterruptedException {
        long ts1 = System.currentTimeMillis();
        long ts2 = ts1;
        //long tout = timeout > 0 ? timeout : 1000;

        do {
            boolean timedOut = !getWorker().waitUntilFinished(timeout);
            ts2 = System.currentTimeMillis();
            if (timedOut) {
                return false;
            }
        } while (isScanInProgress() && (timeout <= 0 || ts2 - ts1 < timeout));

        return timeout <= 0 || ts2 - ts1 < timeout;
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
    public void addIndexingJob(URL rootUrl, Collection<? extends URL> fileUrls, boolean followUpJob, boolean checkEditor, boolean wait) {
        assert rootUrl != null;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("addIndexingJob: rootUrl=" + rootUrl + ", fileUrls=" + fileUrls //NOI18N
                + ", followUpJob=" + followUpJob + ", checkEditor=" + checkEditor + ", wait=" + wait); //NOI18N
        }

        FileObject root = URLMapper.findFileObject(rootUrl);
        if (root == null) {
            LOGGER.info(rootUrl + " can't be translated to FileObject"); //NOI18N
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
                flw = new FileListWork(rootUrl, files, followUpJob, checkEditor);
            }
        } else {
            flw = new FileListWork(rootUrl, followUpJob, checkEditor);
        }

        if (flw != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Scheduling index refreshing: root=" + rootUrl + ", files=" + fileUrls); //NOI18N
            }

            scheduleWork(flw, wait);
        }
    }

    /**
     * Schedules new job for refreshing all indexes created by the given indexer.
     *
     * @param indexerName The name of the indexer, which indexes should be refreshed.
     */
    public void addIndexingJob(String indexerName) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("addIndexingJob: indexerName=" + indexerName); //NOI18N
        }

        CustomIndexerFactory factory = null;
        Set<String> indexerMimeTypes = new HashSet<String>();
        
        for(String mimeType : Util.getAllMimeTypes()) {
            Collection<? extends CustomIndexerFactory> mimeTypeFactories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
            for(CustomIndexerFactory f : mimeTypeFactories) {
                if (f.getIndexerName().equals(indexerName)) {
                    if (factory != null && factory.getClass() != f.getClass()) {
                        LOGGER.warning("Different CustomIndexerFactory implementations using the same name: " //NOI18N
                            + factory.getClass().getName() + ", " + f.getClass().getName()); //NOI18N
                    } else {
                        factory = f;
                        indexerMimeTypes.add(mimeType);
                    }
                }
            }
        }

        if (factory == null) {
            throw new InvalidParameterException("No CustomIndexerFactory with name: '" + indexerName + "'"); //NOI18N
        } else {
            if (indexerMimeTypes.equals(Util.getAllMimeTypes())) {
                indexerMimeTypes = null;
            }
            
            Work w = new RefreshIndices(indexerMimeTypes, factory, scannedRoots2Dependencies);
            scheduleWork(w, false);
        }
    }

    public void refreshAll() {
        scheduleWork(new RootsWork(scannedRoots2Dependencies, scannedBinaries, false), false);
    }

    public synchronized IndexingController getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
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

        boolean existingPathsChanged = false;
        boolean containsRelevantChanges = false;
        for(PathRegistryEvent.Change c : event.getChanges()) {
            if (c.getPathKind() == PathKind.UNKNOWN_SOURCE) {
                continue;
            }

            containsRelevantChanges = true;
            if (c.getEventKind() == EventKind.PATHS_CHANGED || c.getEventKind() == EventKind.INCLUDES_CHANGED) {
                existingPathsChanged = true;
                break;
            }
        }

        if (containsRelevantChanges) {
            scheduleWork(new RootsWork(scannedRoots2Dependencies, scannedBinaries, !existingPathsChanged), false);
        }
    }

    // -----------------------------------------------------------------------
    // FileChangeListener implementation
    // -----------------------------------------------------------------------

    public void fileFolderCreated(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }
        
        //In ideal case this should do nothing,
        //but in Netbeans newlly created folder may
        //already contain files
        boolean processed = false;
        FileObject fo = fe.getFile();
        URL root = null;
        
        if (fo != null && fo.isValid() && VisibilityQuery.getDefault().isVisible(fo)) {
            root = getOwningSourceRoot(fo);
            if (root != null) {
                scheduleWork(new FileListWork(root, Collections.singleton(fo), false, false), false);
                processed = true;
            } else {
                root = getOwningBinaryRoot(fo);
                if (root != null) {
                    scheduleWork(new BinaryWork(root), false);
                    processed = true;
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Folder created (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileDataCreated(FileEvent fe) {
        fileChanged(fe);
    }

    public void fileChanged(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }

        boolean processed = false;
        FileObject fo = fe.getFile();
        URL root = null;

        if (fo != null && fo.isValid() && VisibilityQuery.getDefault().isVisible(fo)) {
            root = getOwningSourceRoot (fo);
            if (root != null) {
                scheduleWork(new FileListWork(root, Collections.singleton(fo), false, false), false);
                processed = true;
            } else {
                root = getOwningBinaryRoot(fo);
                if (root != null) {
                    scheduleWork(new BinaryWork(root), false);
                    processed = true;
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File modified (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileDeleted(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }

        boolean processed = false;
        final FileObject fo = fe.getFile();
        URL root = null;

        if (fo != null && VisibilityQuery.getDefault().isVisible(fo)) {
            root = getOwningSourceRoot (fo);
            if (root != null) {
                if (fo.isData() /*&& FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null*/) {
                    String relativePath = FileUtil.getRelativePath(URLMapper.findFileObject(root), fo);
                    assert relativePath != null : "FileObject not under root: f=" + fo + ", root=" + root; //NOI18N
                    scheduleWork(new DeleteWork(root, Collections.singleton(relativePath)), false);
                    processed = true;
                }
            } else {
                root = getOwningBinaryRoot(fo);
                if (root != null) {
                    scheduleWork(new BinaryWork(root), false);
                    processed = true;
                }
            }
        }
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File deleted (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        if (!authorize(fe)) {
            return;
        }
        
        final FileObject newFile = fe.getFile();
        final String oldNameExt = fe.getExt().length() == 0 ? fe.getName() : fe.getName() + "." + fe.getExt(); //NOI18N
        final URL root = getOwningSourceRoot(newFile);
        boolean processed = false;

        if (root != null) {
            FileObject rootFo = URLMapper.findFileObject(root);
            String oldFilePath = FileUtil.getRelativePath(rootFo, newFile.getParent()) + "/" + oldNameExt; //NOI18N

            if (newFile.isData()) {
                scheduleWork(new DeleteWork(root, Collections.singleton(oldFilePath)), false);
            } else {
                Set<String> oldFilePaths = new HashSet<String>();
                collectFilePaths(newFile, oldFilePath, oldFilePaths);
                scheduleWork(new DeleteWork(root, oldFilePaths), false);
            }

            if (VisibilityQuery.getDefault().isVisible(newFile) && newFile.isData()) {
                // delaying of this task was just copied from the old java.source RepositoryUpdater
                RequestProcessor.getDefault().create(new Runnable() {
                    public void run() {
                        scheduleWork(new FileListWork(root, Collections.singleton(newFile), false, false), false);
                    }
                }).schedule(FILE_LOCKS_DELAY);
            }
            processed = true;
        } else {
            URL binaryRoot = getOwningBinaryRoot(newFile);
            if (binaryRoot != null) {
                final File parentFile = FileUtil.toFile(newFile.getParent());
                if (parentFile != null) {
                    try {
                        URL oldBinaryRoot = new File (parentFile, oldNameExt).toURI().toURL();
                        scheduleWork(new BinaryWork(oldBinaryRoot), false);
                    } catch (MalformedURLException mue) {
                        LOGGER.log(Level.WARNING, null, mue);
                    }
                }

                scheduleWork(new BinaryWork(binaryRoot), false);
                processed = true;
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File renamed (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(newFile) + " Owner: " + root
                    + " Original Name: " + oldNameExt); //NOI18N
        }
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
                                job = new FileListWork(root, Collections.singleton(f), false, true);
                                jobs.put(root, job);
                            } else {
                                job.addFile(f);
                            }
                        }
                    }
                }
            }

            if (jobs.isEmpty()) {
                // either all documents are up-to-date or we can't find owning source roots,
                // which may happen right after start when no roots have been scanned yet,
                // try forcing the initial scan in order to block TaskProcessor (#165170)
                scheduleWork(null, false);
            } else {
                for(FileListWork job : jobs.values()) {
                    scheduleWork(job, false);
                }
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
        // these are not called for BaseDocuments
        handleDocumentModification(e.getDocument());
    }

    public void removeUpdate(DocumentEvent e) {
        // these are not called for BaseDocuments
        handleDocumentModification(e.getDocument());
    }

    // -----------------------------------------------------------------------
    // AtomicLockListener implementation
    // -----------------------------------------------------------------------

    public void atomicLock(AtomicLockEvent e) {
    }

    public void atomicUnlock(AtomicLockEvent e) {
        // XXX: strictly speaking we should use DocumentListener to see whether
        // the document has actually been modified
        handleDocumentModification((Document) e.getSource());
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static RepositoryUpdater instance;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
    private static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test"); //NOI18N
    private static final boolean noRootsScan = Boolean.getBoolean("netbeans.indexing.noRootsScan"); //NOI18N
    private static final boolean notInterruptible = Boolean.getBoolean("netbeans.indexing.notInterruptible"); //NOI18N
    private static final int FILE_LOCKS_DELAY = org.openide.util.Utilities.isWindows() ? 2000 : 1000;
    private static final String PROP_LAST_SEEN_VERSION = RepositoryUpdater.class.getName() + "-last-seen-document-version"; //NOI18N
    
    private final Map<URL, List<URL>>scannedRoots2Dependencies = Collections.synchronizedMap(new HashMap<URL, List<URL>>());
    private final Set<URL>scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedUnknown = Collections.synchronizedSet(new HashSet<URL>());

    private volatile State state = State.CREATED;
    private volatile Task worker;

    private volatile Reference<Document> activeDocumentRef = null;
    private Lookup.Result<? extends IndexingActivityInterceptor> indexingActivityInterceptors = null;
    private IndexingController controller;

    private RepositoryUpdater () {
        // no-op
    }

    private void handleActiveDocumentChange(Document deactivated, Document activated) {
        Document activeDocument = activeDocumentRef == null ? null : activeDocumentRef.get();

        if (deactivated != null && deactivated == activeDocument) {
            if (activeDocument instanceof BaseDocument) {
                ((BaseDocument) activeDocument).removeAtomicLockListener(this);
            } else {
                activeDocument.removeDocumentListener(this);
            }
            activeDocumentRef = null;
            LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }

        if (activated != null && activated != activeDocument) {
            if (activeDocument != null) {
                if (activeDocument instanceof BaseDocument) {
                    ((BaseDocument) activeDocument).removeAtomicLockListener(this);
                } else {
                    activeDocument.removeDocumentListener(this);
                }
                LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
            }

            activeDocument = activated;
            activeDocumentRef = new WeakReference<Document>(activeDocument);
            
            if (activeDocument instanceof BaseDocument) {
                ((BaseDocument) activeDocument).addAtomicLockListener(this);
            } else {
                activeDocument.addDocumentListener(this);
            }
            LOGGER.log(Level.FINE, "Registering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }
    }

    public void handleDocumentModification(Document document) {
        final Reference<Document> ref = activeDocumentRef;
        Document activeDocument = ref == null ? null : ref.get();

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
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false, true, null);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }

                    for(EmbeddingIndexerFactory factory : embeddingIndexerFactories) {
                        try {
                            Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root), root,
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false, true, null);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }
                } else {
                    // an odd event, maybe we could just ignore it
                    try {
                        addIndexingJob(root, Collections.singleton(f.getURL()), false, true, false);
                    } catch (FileStateInvalidException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
    }

    /* test */ void scheduleWork(final Work work, boolean wait) {
        recordCaller();

        boolean scheduleExtraWork = false;

        synchronized (this) {
            if (state == State.STARTED) {
                state = State.INITIAL_SCAN_RUNNING;
                scheduleExtraWork = !(work instanceof InitialRootsWork);
            }
        }

        if (scheduleExtraWork) {
            getWorker().schedule(new InitialRootsWork(scannedRoots2Dependencies, scannedBinaries, true), false);

            if (work instanceof RootsWork) {
                // if the work is the initial RootsWork it's superseeded
                // by the RootsWork we've just scheduled and so we can quit now.
                return;
            }
        }

        if (work != null) {
            getWorker().schedule(work, wait);
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
        List<URL> clone = new ArrayList<URL> (this.scannedRoots2Dependencies.keySet());
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }

    private URL getOwningBinaryRoot(final FileObject fo) {
        if (fo == null) {
            return null;
        }
        String foPath;
        try {
            foPath = fo.getURL().getPath();
        } catch (FileStateInvalidException fsie) {
            LOGGER.log(Level.WARNING, null, fsie);
            return null;
        }

        List<URL> clone = new ArrayList<URL>(this.scannedBinaries);
        for (URL root : clone) {
            URL fileURL = FileUtil.getArchiveFile(root);
            boolean archive = true;
            if (fileURL == null) {
                fileURL = root;
                archive = false;
            }
            String filePath = fileURL.getPath();
            if (filePath.equals(foPath)) {
                return root;
            }
            if (!archive && foPath.startsWith(filePath)) {
                return root;
            }
        }

        return null;
    }

    private boolean authorize(FileEvent event) {
        Collection<? extends IndexingActivityInterceptor> interceptors = indexingActivityInterceptors.allInstances();
        for(IndexingActivityInterceptor i : interceptors) {
            if (i.authorizeFileSystemEvent(event) == IndexingActivityInterceptor.Authorization.IGNORE) {
                return false;
            }
        }
        return true;
    }

    private static void collectFilePaths(FileObject folder, String pathPrefix, Set<String> collectedPaths) {
        assert folder.isFolder() : "Expecting folder: " + folder; //NOI18N

        if (folder.isValid()) {
            for(FileObject kid : folder.getChildren()) {
                if (kid.isValid()) {
                    String kidPath = pathPrefix + "/" + kid.getNameExt(); //NOI18N
                    if (kid.isData()) {
                        collectedPaths.add(kidPath); //NOI18N
                    } else {
                        collectFilePaths(kid, kidPath, collectedPaths);
                    }
                }
            }
        }
    }

    private static final Map<List<StackTraceElement>, Long> lastRecordedStackTraces = new HashMap<List<StackTraceElement>, Long>();
    private static long stackTraceId = 0;
    private static void recordCaller() {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }

        synchronized (lastRecordedStackTraces) {
            StackTraceElement []  stackTrace = Thread.currentThread().getStackTrace();
            List<StackTraceElement> stackTraceList = new ArrayList<StackTraceElement>(stackTrace.length);
            for(StackTraceElement e : stackTrace) {
                stackTraceList.add(e);
            }

            Long id = lastRecordedStackTraces.get(stackTraceList);
            if (id == null) {
                id = stackTraceId++;
                lastRecordedStackTraces.put(stackTraceList, id);
                StringBuilder sb = new StringBuilder();
                sb.append("RepositoryUpdater caller [id=").append(id).append("] :\n"); //NOI18N
                for(StackTraceElement e : stackTraceList) {
                    sb.append(e.toString());
                    sb.append("\n"); //NOI18N
                }
                LOGGER.fine(sb.toString());
            } else {
                StackTraceElement caller = Util.findCaller(stackTrace);
                LOGGER.fine("RepositoryUpdater caller [refid=" + id + "]: " + caller); //NOI18N
            }
        }
    }

// we have to handle *all* mime types because of eg. tasklist indexer or goto-file indexer
//    private static boolean isMonitoredMimeType(FileObject f, Set<String> mimeTypes) {
//        String mimeType = FileUtil.getMIMEType(f, mimeTypes.toArray(new String[mimeTypes.size()]));
//        return mimeType != null && mimeTypes.contains(mimeType);
//    }

    enum State {CREATED, STARTED, INITIAL_SCAN_RUNNING, ACTIVE, STOPPED};

    /* test */ static abstract class Work {

        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicBoolean finished = new AtomicBoolean(false);
        private final boolean followUpJob;
        private final boolean checkEditor;
        private final CountDownLatch latch = new CountDownLatch(1);
        private final Map<String,List<EmbeddingIndexerFactory>> embeddedIndexers = new HashMap<String, List<EmbeddingIndexerFactory>>();
        private final CancelRequest cancelRequest = new CancelRequest() {
            public boolean isRaised() {
                if (cancelled.get()) {
                    synchronized (RepositoryUpdater.getDefault()) {
                        if (RepositoryUpdater.getDefault().getState() == State.STOPPED) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
        private final boolean supportsProgress;
        private ProgressHandle progressHandle = null;

//        private int allLanguagesParsersCount = -1;
//        private int allLanguagesTasksCount = -1;

        protected Work(boolean followUpJob, boolean checkEditor, boolean supportsProgress) {
            this.followUpJob = followUpJob;
            this.checkEditor = checkEditor;
            this.supportsProgress = supportsProgress;
        }

        protected final boolean isFollowUpJob() {
            return followUpJob;
        }
        
        protected final boolean hasToCheckEditor() {
            return checkEditor;
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
            progressHandle.progress(urlForMessage(currentlyScannedRoot));
        }

        protected final void updateProgress(URL currentlyScannedRoot, int scannedFiles, int totalFiles) {
            assert currentlyScannedRoot != null;
            if (progressHandle == null) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(urlForMessage(currentlyScannedRoot));
            sb.append(" (").append(scannedFiles).append(" of ").append(totalFiles).append(")"); //NOI18N
            progressHandle.progress(sb.toString());
        }

        protected final void delete (final Collection<Indexable> deleted, final URL root) throws IOException {
            if (deleted == null || deleted.size() == 0) {
                return;
            }

            LinkedList<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                Set<CustomIndexerFactory> customIndexerFactories = new HashSet<CustomIndexerFactory>();
                Set<EmbeddingIndexerFactory> embeddingIndexerFactories = new HashSet<EmbeddingIndexerFactory>();
                for (String mimeType : Util.getAllMimeTypes()) {
                    Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    customIndexerFactories.addAll(factories);

                    Collection<? extends EmbeddingIndexerFactory> embeddingFactories = MimeLookup.getLookup(mimeType).lookupAll(EmbeddingIndexerFactory.class);
                    embeddingIndexerFactories.addAll(embeddingFactories);
                }

                for (CustomIndexerFactory factory : customIndexerFactories) {
                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor, null);
                    factory.filesDeleted(deleted, ctx);
                }

                for(EmbeddingIndexerFactory factory : embeddingIndexerFactories) {
                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor, null);
                    factory.filesDeleted(deleted, ctx);
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

        protected final boolean index (final Map<String,Collection<Indexable>> resources, final URL root) throws IOException {
            LinkedList<Context> transactionContexts = new LinkedList<Context>();
            try {
                // determine the total number of files
                int scannedFilesCount = 0;
                int totalFilesCount = 0;
                for (String mimeType : resources.keySet()) {
                    final Collection<? extends Indexable> indexables = resources.get(mimeType);
                    if (indexables != null) {
                        totalFilesCount += indexables.size();
                    }
                }

                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                for (String mimeType : resources.keySet()) {
                    if (getShuttdownRequest().isRaised()) {
                        return false;
                    }
                    
                    final Collection<? extends Indexable> indexables = resources.get(mimeType);
                    if (indexables == null) {
                        continue;
                    }
                    
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("-- Indexing " + mimeType + " in " + root); //NOI18N
                    }

                    final Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    boolean supportsEmbeddings = true;
                    for (CustomIndexerFactory factory : factories) {
                        if (getShuttdownRequest().isRaised()) {
                            return false;
                        }

                        boolean b = factory.supportsEmbeddedIndexers();
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.fine("CustomIndexerFactory: " + factory + ", supportsEmbeddedIndexers=" + b); //NOI18N
                        }

                        supportsEmbeddings &= b;
                        final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor, getShuttdownRequest());
                        transactionContexts.add(ctx);

                        // some CustomIndexers (eg. java) need to know about roots even when there
                        // are no modified Inexables at the moment (eg. java checks source level in
                        // the associated project, etc)
                        final CustomIndexer indexer = factory.createIndexer();
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Indexing " + indexables.size() + " indexables; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
//                            LOGGER.fine("Indexing " + indexables + "; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                        }
                        try {
                            SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            LOGGER.log(Level.WARNING, null, t);
                        }
                    }

                    if (supportsEmbeddings) {
                        if (Util.canBeParsed(mimeType)) {
                            //Then use slow gsf like indexers
                            LOGGER.log(Level.FINE, "Using EmbeddingIndexers for {0}", indexables); //NOI18N

//                            final SourceIndexer si = new SourceIndexer(root, cacheRoot, followUpJob, checkEditor);
//                            si.index(indexables, transactionContexts);
                            boolean f = indexEmbedding(cacheRoot, root, indexables, transactionContexts, scannedFilesCount, totalFilesCount);
                            if (!f) {
                                return false;
                            }
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(mimeType + " has no Parser or EmbeddingProvider registered and won't be indexed by embedding indexers"); //NOI18N
                            }
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(mimeType + " files indexed by custom indexers, embedding indexers forbidden"); //NOI18N
                        }
                    }

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("-- Finished indexing " + mimeType + " in " + root); //NOI18N
                    }

                    scannedFilesCount += indexables.size();
                }
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }

            return true;
        }

        protected final void indexBinary(URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning binary root: {0}", root); //NOI18N

            List<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
//                String mimeType = ""; //NOI18N
//
//                final FileObject rootFo = URLMapper.findFileObject(root);
//                if (rootFo != null) {
//                    final File archiveOrDir = FileUtil.archiveOrDirForURL(root);
//                    final FileObject archiveOrDirFo = archiveOrDir == null ? null : FileUtil.toFileObject(archiveOrDir);
//                    if (archiveOrDirFo != null && archiveOrDirFo.isData()) {
//                        mimeType = archiveOrDirFo.getMIMEType();
//                    }
//                }

                final Collection<? extends BinaryIndexerFactory> factories = MimeLookup.getLookup(MimePath.EMPTY).lookupAll(BinaryIndexerFactory.class);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.fine("Using BinaryIndexerFactories: " + factories); //NOI18N
                }

                for(BinaryIndexerFactory f : factories) {
                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, f.getIndexerName(), f.getIndexVersion(), null, false, false, null);
                    transactionContexts.add(ctx);

                    final BinaryIndexer indexer = f.createIndexer();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Indexing binary " + root + " using " + indexer); //NOI18N
                    }
                    SPIAccessor.getInstance().index(indexer, ctx);
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

        private boolean indexEmbedding(final FileObject cache, final URL rootURL, Iterable<? extends Indexable> files, final List<Context> transactionContexts, int scannedFilesCount, int totalFilesCount) throws IOException {
            // XXX: Replace with multi source when done
            for (final Indexable dirty : files) {
                if (getShuttdownRequest().isRaised()) {
                    return false;
                }

                try {
                    final FileObject fileObject = URLMapper.findFileObject(dirty.getURL());
                    if (fileObject != null) {
                        final Source src = Source.create(fileObject);
                        ParserManager.parse(Collections.singleton(src), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                final String mimeType = src.getMimeType();
                                final List<EmbeddingIndexerFactory> indexers = findEmbeddingIndexers (mimeType);
                                for (EmbeddingIndexerFactory indexerFactory : indexers) {
                                    if (LOGGER.isLoggable(Level.FINE)) {
                                        LOGGER.fine("Indexing " + fileObject.getPath() + "; using " + indexerFactory + "; mimeType='" + mimeType + "'"); //NOI18N
                                    }
                                    visit(resultIterator,indexerFactory);
                                }
                            }

                            private void visit (final ResultIterator resultIterator,
                                    final EmbeddingIndexerFactory currentIndexerFactory) throws ParseException,IOException {
                                if (currentIndexerFactory != null) {
                                    final Parser.Result pr = resultIterator.getParserResult();
                                    if (pr != null) {
                                        final String indexerName = currentIndexerFactory.getIndexerName();
                                        final int indexerVersion = currentIndexerFactory.getIndexVersion();
                                        final Context context = SPIAccessor.getInstance().createContext(cache, rootURL, indexerName, indexerVersion, null, followUpJob, checkEditor, null);
                                        transactionContexts.add(context);

                                        final EmbeddingIndexer indexer = currentIndexerFactory.createIndexer(dirty, pr.getSnapshot());
                                        if (indexer != null) {
                                            try {
                                                SPIAccessor.getInstance().index(indexer, dirty, pr, context);
                                            } catch (ThreadDeath td) {
                                                throw td;
                                            } catch (Throwable t) {
                                                LOGGER.log(Level.WARNING, null, t);
                                            }
                                        }
                                    }
                                }
                                Iterable<? extends Embedding> embeddings = resultIterator.getEmbeddings();
                                for (Embedding embedding : embeddings) {
                                    final String mimeType = embedding.getMimeType();
                                    final List<EmbeddingIndexerFactory> indexerFactories = findEmbeddingIndexers(mimeType);
                                    for (EmbeddingIndexerFactory indexerFactory : indexerFactories)
                                        visit(resultIterator.getResultIterator(embedding), indexerFactory);
                                }
                            }
                        });
                    }
                } catch (final ParseException e) {
                    LOGGER.log(Level.WARNING, null, e);
                }

                updateProgress(rootURL, ++scannedFilesCount, totalFilesCount);
            }

            return true;
        }

        private List<EmbeddingIndexerFactory> findEmbeddingIndexers (final String mimeType) {
            assert mimeType != null;
            List<EmbeddingIndexerFactory> indexer = embeddedIndexers.get(mimeType);
            if (indexer != null) {
                return indexer;
            }
            indexer = new ArrayList<EmbeddingIndexerFactory> (MimeLookup.getLookup(mimeType).lookupAll(EmbeddingIndexerFactory.class));
            if (indexer != null) {
                embeddedIndexers.put(mimeType, indexer);
            }
            return indexer;
        }

        /**
         * @return <code>true</code> if finished or <code>false</code> if the task
         *   was cancelled and has to be rescheduled again.
         */
        protected abstract boolean getDone();

        protected boolean isCancelledBy(Work newWork) {
            return false;
        }

        public boolean absorb(Work newWork) {
            return false;
        }

        protected final boolean isCancelled() {
            return cancelled.get();
        }

        protected final CancelRequest getShuttdownRequest() {
            return cancelRequest;
        }

        public final void doTheWork() {
            try {
                finished.compareAndSet(false, getDone());
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, null, t);
                
                // prevent running the faulty work again
                finished.set(true);

                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath)t;
                }
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

        public final void setCancelled(boolean cancelled) {
            this.cancelled.set(cancelled);
        }

        public final void cancelBy(Work newWork) {
            if (isCancelledBy(newWork)) {
                LOGGER.log(Level.FINE, "{0} cancelled by {1}", new Object [] { this, newWork }); //NOI18N
                cancelled.set(true);
                finished.set(true); // work cancelled by other work is by default finished
            }
        }

        public final boolean isFinished() {
            return finished.get();
        }

        public final boolean supportsProgress() {
            return supportsProgress;
        }

        public final void setProgressHandle(ProgressHandle progressHandle) {
            this.progressHandle = progressHandle;
        }

        private String urlForMessage(URL currentlyScannedRoot) {
            String msg = null;

            URL tmp = FileUtil.getArchiveFile(currentlyScannedRoot);
            if (tmp == null) {
                tmp = currentlyScannedRoot;
            }
            try {
                if ("file".equals(tmp.getProtocol())) { //NOI18N
                    final File file = new File(new URI(tmp.toString()));
                    msg = file.getAbsolutePath();
                }
            } catch (URISyntaxException ex) {
                // ignore
            }

            return msg == null ? tmp.toString() : msg;
        }

        public @Override String toString() {
            return getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this)) //NOI18N
                + "[followUpJob=" + followUpJob + ", checkEditor=" + checkEditor; //NOI18N
        }
    } // End of Work class

    /* test */ static final class FileListWork extends Work {

        private final URL root;
        private final Collection<FileObject> files = new HashSet<FileObject>();

        public FileListWork (URL root, boolean followUpJob, boolean checkEditor) {
            super(followUpJob, checkEditor, false);

            assert root != null;
            this.root = root;
        }

        public FileListWork (URL root, Collection<FileObject> files, boolean followUpJob, boolean checkEditor) {
            super(followUpJob, checkEditor, false);
            
            assert root != null;
            assert files != null && files.size() > 0;
            this.root = root;
            this.files.addAll(files);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("FileListWork@" + Integer.toHexString(System.identityHashCode(this)) + ": root=" + root + ", file=" + files); //NOI18N
            }
        }

        public void addFile(FileObject f) {
            assert f != null;
            assert FileUtil.isParentOf(URLMapper.findFileObject(root), f) : "File " + f + " does not belong under the root: " + root; //NOI18N
            files.add(f);
        }

        protected @Override boolean getDone() {
//            updateProgress(root);
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                try {
                    final Crawler crawler = files.isEmpty() ?
                        new FileObjectCrawler(rootFo, false, null, getShuttdownRequest()) : // rescan the whole root (no timestamp check)
                        new FileObjectCrawler(rootFo, files.toArray(new FileObject[files.size()]), null, getShuttdownRequest()); // rescan selected files (no timestamp check)

                    final Map<String,Collection<Indexable>> resources = crawler.getResources();
                    if (crawler.isFinished()) {
                        if (index(resources, root)) {
                            crawler.storeTimestamps();
                        }
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            TEST_LOGGER.log(Level.FINEST, "filelist"); //NOI18N
            return true;
        }

        public @Override boolean absorb(Work newWork) {
            if (newWork instanceof FileListWork) {
                FileListWork nflw = (FileListWork) newWork;
                if (nflw.root.equals(root)
                    && nflw.isFollowUpJob() == isFollowUpJob()
                    && nflw.hasToCheckEditor() == hasToCheckEditor()
                ) {
                    files.addAll(nflw.files);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(this + ", root=" + root + " absorbed: " + nflw.files); //NOI18N
                    }
                    return true;
                }
            }
            return false;
        }
    } // End of FileListWork class

    private static final class BinaryWork extends Work {

        private final URL root;

        public BinaryWork(URL root) {
            super(false, false, true);
            this.root = root;
        }

        protected @Override boolean getDone() {
            try {
                indexBinary(root);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
            return true;
        }
    } // End of BinaryWork class

    private static final class DeleteWork extends Work {

        private final URL root;
        private final Set<String> relativePaths = new HashSet<String>();

        public DeleteWork (URL root, Set<String> relativePaths) {
            super(false, false, false);
            
            Parameters.notNull("root", root); //NOI18N
            Parameters.notNull("relativePath", relativePaths); //NOI18N
            
            this.root = root;
            this.relativePaths.addAll(relativePaths);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("DeleteWork@" + Integer.toHexString(System.identityHashCode(this)) + ": root=" + root + ", files=" + relativePaths); //NOI18N
            }
        }

        public @Override boolean getDone() {
//            updateProgress(root);
            try {
                final Collection<Indexable> indexables = new LinkedList<Indexable>();
                for(String path : relativePaths) {
                    indexables.add(SPIAccessor.getInstance().create(new DeletedIndexable (root, path)));
                }
                delete(indexables, root);
                TEST_LOGGER.log(Level.FINEST, "delete"); //NOI18N
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
            return true;
        }

        public @Override boolean absorb(Work newWork) {
            if (newWork instanceof DeleteWork) {
                DeleteWork ndw = (DeleteWork) newWork;
                if (ndw.root.equals(root)) {
                    relativePaths.addAll(ndw.relativePaths);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(this + ", root=" + root + " absorbed: " + ndw.relativePaths); //NOI18N
                    }
                    return true;
                }
            }
            return false;
        }

    } // End of DeleteWork class

    private static class RefreshIndices extends Work {

        private final Set<String> indexerMimeTypes;
        private final CustomIndexerFactory indexerFactory;
        private final Map<URL, List<URL>> scannedRoots2Dependencies;

        public RefreshIndices(Set<String> indexerMimeTypes, CustomIndexerFactory indexerFactory, Map<URL, List<URL>> scannedRoots2Depencencies) {
            super(false, false, true);
            this.indexerMimeTypes = indexerMimeTypes;
            this.indexerFactory = indexerFactory;
            this.scannedRoots2Dependencies = scannedRoots2Depencencies;
        }

        protected @Override boolean getDone() {
            for(URL root : scannedRoots2Dependencies.keySet()) {
                if (getShuttdownRequest().isRaised()) {
                    // XXX: this only happens when the IDE is shutting down
                    return true;
                }

                try {
                    final FileObject rootFo = URLMapper.findFileObject(root);
                    if (rootFo != null) {
                        Crawler crawler = new FileObjectCrawler(rootFo, false, indexerMimeTypes, getShuttdownRequest());
                        final Map<String, Collection<Indexable>> resources = crawler.getResources();
                        final Collection<Indexable> deleted = crawler.getDeletedResources();

                        if (crawler.isFinished()) {
                            if (deleted.size() > 0) {
                                delete(deleted, root);
                            }

                            final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                            LinkedList<Context> transactionContexts = new LinkedList<Context>();
                            try {
                                for(String mimeType : resources.keySet()) {
                                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, indexerFactory.getIndexerName(), indexerFactory.getIndexVersion(), null, false, false, null);
                                    transactionContexts.add(ctx);

                                    // some CustomIndexers (eg. java) need to know about roots even when there
                                    // are no modified Inexables at the moment (eg. java checks source level in
                                    // the associated project, etc)
                                    final Collection<? extends Indexable> indexables = resources.get(mimeType);
                                    if (indexables != null) {
                                        final CustomIndexer indexer = indexerFactory.createIndexer();
                                        if (LOGGER.isLoggable(Level.FINE)) {
                                            LOGGER.fine("Reindexing " + indexables.size() + " indexables; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                                        }
                                        try {
                                            SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                                        } catch (ThreadDeath td) {
                                            throw td;
                                        } catch (Throwable t) {
                                            LOGGER.log(Level.WARNING, null, t);
                                        }
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

                            crawler.storeTimestamps();
                        }
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            
            return true;
        }

        public @Override String toString() {
            return super.toString() + ", indexer=" + indexerFactory.getIndexerName() + "/" + indexerFactory.getIndexVersion() //NOI18N
                    + ", mimeTypes=" + indexerMimeTypes; //NOI18N
        }
    } // End of RefreshIndices class

    private static class RootsWork extends Work {

        private final Map<URL, List<URL>> scannedRoots2Dependencies;
        private final Set<URL> scannedBinaries;
        private boolean useInitialState;

        private DependenciesContext depCtx;

        public RootsWork(Map<URL, List<URL>> scannedRoots2Depencencies, Set<URL> scannedBinaries, boolean useInitialState) {
            super(false, false, true);
            this.scannedRoots2Dependencies = scannedRoots2Depencencies;
            this.scannedBinaries = scannedBinaries;
            this.useInitialState = useInitialState;
        }

        public @Override String toString() {
            return super.toString() + ", useInitialState=" + useInitialState; //NOI18N
        }

        public @Override boolean getDone() {
            if (isCancelled()) {
                return false;
            }

            updateProgress(NbBundle.getMessage(RepositoryUpdater.class, "MSG_ProjectDependencies")); //NOI18N
            long tm1 = System.currentTimeMillis();
            if (depCtx == null) {
                depCtx = new DependenciesContext(scannedRoots2Dependencies, scannedBinaries, useInitialState);
                final List<URL> newRoots = new LinkedList<URL>();
                newRoots.addAll(PathRegistry.getDefault().getSources());
                newRoots.addAll(PathRegistry.getDefault().getLibraries());

                depCtx.newBinariesToScan.addAll(PathRegistry.getDefault().getBinaryLibraries());
                for (Iterator<URL> it = depCtx.newBinariesToScan.iterator(); it.hasNext(); ) {
                    if (depCtx.oldBinaries.remove(it.next())) {
                        it.remove();
                    }
                }
                newRoots.addAll(PathRegistry.getDefault().getUnknownRoots());

                for (URL url : newRoots) {
                    findDependencies(url, depCtx, null, null);
                }

                try {
                    depCtx.newRootsToScan.addAll(org.openide.util.Utilities.topologicalSort(depCtx.newRoots2Deps.keySet(), depCtx.newRoots2Deps));
                } catch (final TopologicalSortException tse) {
                    LOGGER.log(Level.SEVERE, "Cycles detected in classpath roots dependencies", tse); //NOI18N
                    return true;
                }
                Collections.reverse(depCtx.newRootsToScan);

                if (!useInitialState) {
                    // check for differencies from the initialState
                    final Map<URL,List<URL>> removed = new HashMap<URL,List<URL>>();
                    final Map<URL,List<URL>> addedOrChanged = new HashMap<URL,List<URL>>();
                    diff(depCtx.initialRoots2Deps, depCtx.newRoots2Deps, addedOrChanged, removed);

                    if (LOGGER.isLoggable(Level.FINE) && (addedOrChanged.size() > 0 || removed.size() > 0)) {
                        LOGGER.fine("Changes in dependencies detected:"); //NOI18N
                        LOGGER.fine("initialRoots2Deps="); //NOI18N
                        printMap(depCtx.initialRoots2Deps, Level.FINE);
                        LOGGER.fine("newRoots2Deps="); //NOI18N
                        printMap(depCtx.newRoots2Deps, Level.FINE);
                        LOGGER.fine("addedOrChanged="); //NOI18N
                        printMap(addedOrChanged, Level.FINE);
                    }

                    depCtx.oldRoots.clear();
                    depCtx.oldRoots.addAll(removed.keySet());
                    depCtx.newRootsToScan.retainAll(addedOrChanged.keySet());
                }
            } else {
                depCtx.newRootsToScan.removeAll(depCtx.scannedRoots);
                depCtx.scannedRoots.clear();
                depCtx.newBinariesToScan.removeAll(depCtx.scannedBinaries);
                depCtx.scannedBinaries.clear();
            }

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Resolving dependencies took: {0} ms", System.currentTimeMillis() - tm1); //NOI18N
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Running " + this + " on \n" + depCtx.toString()); //NOI18N
            }

            boolean finished = scanBinaries(depCtx);
            if (finished) {
                finished = scanSources(depCtx);
            }

            for(URL root : depCtx.scannedRoots) {
                List<URL> deps = depCtx.newRoots2Deps.get(root);
                scannedRoots2Dependencies.put(root, deps);
            }
            scannedRoots2Dependencies.keySet().removeAll(depCtx.oldRoots);

            scannedBinaries.addAll(depCtx.scannedBinaries);
            scannedBinaries.removeAll(depCtx.oldBinaries);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(this + " " + (isCancelled() ? "cancelled" : "finished") + ": {"); //NOI18N
                LOGGER.fine("  scannedRoots2Dependencies="); //NOI18N
                printMap(scannedRoots2Dependencies, Level.FINE);
                LOGGER.fine("  scannedBinaries=" + scannedBinaries); //NOI18N
                LOGGER.fine("} ===="); //NOI18N
            }

            return finished;
        }

        protected @Override boolean isCancelledBy(Work newWork) {
            boolean b = (newWork instanceof RootsWork) && useInitialState;
            if (b && LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cancelling " + this + ", because of " + newWork); //NOI18N
            }
            return b;
        }

        public @Override boolean absorb(Work newWork) {
            if (newWork.getClass().equals(RootsWork.class)) {
                if (!((RootsWork) newWork).useInitialState) {
                    // the new work does not use initial state and so should not we
                    useInitialState = ((RootsWork) newWork).useInitialState;
                    LOGGER.fine("Absorbing " + newWork + ", updating useInitialState to " + useInitialState); //NOI18N
                }
                return true;
            } else {
                return false;
            }
        }

        private static void findDependencies(
                final URL rootURL,
                final DependenciesContext ctx,
                Set<String> libraryIds,
                Set<String> binaryLibraryIds)
        {
            if (ctx.useInitialState && ctx.initialRoots2Deps.containsKey(rootURL)) {
                ctx.oldRoots.remove(rootURL);
                return;
            }
            if (ctx.newRoots2Deps.containsKey(rootURL)) {
                return;
            }
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {
                return;
            }

            final List<URL> deps = new LinkedList<URL>();
            ctx.cycleDetector.push(rootURL);
            try {
                if (libraryIds == null || binaryLibraryIds == null) {
                    Set<String> ids;
                    if (null != (ids = PathRegistry.getDefault().getSourceIdsFor(rootURL)) && !ids.isEmpty()) {
                        LOGGER.log(Level.FINER, "Resolving Ids based on sourceIds for {0}: {1}", new Object [] { rootURL, ids }); //NOI18N
                        Set<String> lids = new HashSet<String>();
                        Set<String> blids = new HashSet<String>();
                        for(String id : ids) {
                            lids.addAll(PathRecognizerRegistry.getDefault().getLibraryIdsForSourceId(id));
                            blids.addAll(PathRecognizerRegistry.getDefault().getBinaryLibraryIdsForSourceId(id));
                        }
                        if (libraryIds == null) {
                            libraryIds = lids;
                        }
                        if (binaryLibraryIds == null) {
                            binaryLibraryIds = blids;
                        }
                    } else if (null != (ids = PathRegistry.getDefault().getLibraryIdsFor(rootURL)) && !ids.isEmpty()) {
                        LOGGER.log(Level.FINER, "Resolving Ids based on libraryIds for {0}: {1}", new Object [] { rootURL, ids }); //NOI18N
                        Set<String> blids = new HashSet<String>();
                        for(String id : ids) {
                            blids.addAll(PathRecognizerRegistry.getDefault().getBinaryLibraryIdsForLibraryId(id));
                        }
                        if (libraryIds == null) {
                            libraryIds = ids;
                        }
                        if (binaryLibraryIds == null) {
                            binaryLibraryIds = blids;
                        }
                    }
                }

                LOGGER.log(Level.FINER, "LibraryIds for {0}: {1}", new Object [] { rootURL, libraryIds }); //NOI18N
                LOGGER.log(Level.FINER, "BinaryLibraryIds for {0}: {1}", new Object [] { rootURL, binaryLibraryIds }); //NOI18N

                { // libraries
                    final Set<String> ids = libraryIds == null ? PathRecognizerRegistry.getDefault().getLibraryIds() : libraryIds;
                    for (String id : ids) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            for (ClassPath.Entry entry : cp.entries()) {
                                final URL sourceRoot = entry.getURL();
                                if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                    deps.add(sourceRoot);
//                                    LOGGER.log(Level.FINEST, "#1- {0}: adding dependency on {1}, from {2} with id {3}", new Object [] {
//                                        rootURL, sourceRoot, cp, id
//                                    });
                                    findDependencies(sourceRoot, ctx, libraryIds, binaryLibraryIds);
                                }
                            }
                        }
                    }
                }

                { // binary libraries
                    final Set<String> ids = binaryLibraryIds == null ? PathRecognizerRegistry.getDefault().getLibraryIds() : binaryLibraryIds;
                    for (String id : ids) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            for (ClassPath.Entry entry : cp.entries()) {
                                final URL url = entry.getURL();
                                final URL[] sourceRoots = PathRegistry.getDefault().sourceForBinaryQuery(url, cp, false);
                                if (sourceRoots != null) {
                                    for (URL sourceRoot : sourceRoots) {
                                        if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                            deps.add(sourceRoot);
//                                            LOGGER.log(Level.FINEST, "#2- {0}: adding dependency on {1}, from {2} with id {3}", new Object [] {
//                                                rootURL, sourceRoot, cp, id
//                                            });
                                            findDependencies(sourceRoot, ctx, libraryIds, binaryLibraryIds);
                                        }
                                    }
                                }
                                else {
                                    //What does it mean?
                                    if (ctx.useInitialState) {
                                        if (!ctx.initialBinaries.contains(url)) {
                                            ctx.newBinariesToScan.add (url);
                                        }
                                        ctx.oldBinaries.remove(url);
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                ctx.cycleDetector.pop();
            }

            ctx.newRoots2Deps.put(rootURL, deps);
        }

        private boolean scanBinaries (final DependenciesContext ctx) {
            assert ctx != null;
            long scannedRootsCnt = 0;
            long completeTime = 0;
            boolean finished = true;

            for (URL binary : ctx.newBinariesToScan) {
                if (isCancelled()) {
                    finished = false;
                    break;
                }
                
                final long tmStart = System.currentTimeMillis();
                try {
                    updateProgress(binary);
                    indexBinary (binary);
                    ctx.scannedBinaries.add(binary);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                } finally {
                    final long time = System.currentTimeMillis() - tmStart;
                    completeTime += time;
                    scannedRootsCnt++;
                    if (PERF_TEST) {
                        reportRootScan(binary, time);
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Indexing of: %s took: %d ms", binary.toExternalForm(), time)); //NOI18N
                    }
                }
            }

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(String.format("Complete indexing of %d binary roots took: %d ms", scannedRootsCnt, completeTime)); //NOI18N
            }
            TEST_LOGGER.log(Level.FINEST, "scanBinary", ctx.newBinariesToScan);       //NOI18N

            return finished;
        }

        private boolean scanSources  (final DependenciesContext ctx) {
            assert ctx != null;
            long scannedRootsCnt = 0;
            long completeTime = 0;
            boolean finished = true;

            for (URL source : ctx.newRootsToScan) {
                if (isCancelled()) {
                    finished = false;
                    break;
                }

                final long tmStart = System.currentTimeMillis();
                try {
                    updateProgress(source);
                    if (scanSource (source)) {
                        ctx.scannedRoots.add(source);
                    } else {
                        finished = false;
                        break;
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                } finally {
                    final long time = System.currentTimeMillis() - tmStart;
                    completeTime += time;
                    scannedRootsCnt++;
                    if (PERF_TEST) {
                        reportRootScan(source, time);
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Indexing of: %s took: %d ms", source.toExternalForm(), time)); //NOI18N
                    }
                }
            }

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(String.format("Complete indexing of %d source roots took: %d ms", scannedRootsCnt, completeTime)); //NOI18N
            }
            TEST_LOGGER.log(Level.FINEST, "scanSources", ctx.newRootsToScan); //NOI18N

            return finished;
        }

        private boolean scanSource (URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning sources root: {0}", root); //NOI18N

            if (noRootsScan && useInitialState && TimeStamps.existForRoot(root)) {
                // We've already seen the root at least once and roots scanning is forcibly turned off
                // so just call indexers with no files to let them know about the root, but perform
                // no indexing.
//                    final Map<String, Collection<Indexable>> resources = new HashMap<String, Collection<Indexable>>();
//                    for(String mimeType : Util.getAllMimeTypes()) {
//                        resources.put(mimeType, Collections.<Indexable>emptySet());
//                    }
//                    index(resources, root);
                LinkedList<Context> transactionContexts = new LinkedList<Context>();
                try {
                    final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                    Set<CustomIndexerFactory> customIndexerFactories = new HashSet<CustomIndexerFactory>();
                    for (String mimeType : Util.getAllMimeTypes()) {
                        Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                        customIndexerFactories.addAll(factories);
                    }

                    for (CustomIndexerFactory factory : customIndexerFactories) {
                        final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, isFollowUpJob(), hasToCheckEditor(), null);
                        CustomIndexer indexer = factory.createIndexer();

                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Fake indexing: indexer=" + indexer); //NOI18N
                        }
                        try {
                            SPIAccessor.getInstance().index(indexer, Collections.<Indexable>emptySet(), ctx);
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            LOGGER.log(Level.WARNING, null, t);
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
                return true;
            } else {
                //todo: optimize for java.io.Files
                final FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo != null) {
                    final Crawler crawler = new FileObjectCrawler(rootFo, useInitialState, null, getShuttdownRequest());
                    final Map<String,Collection<Indexable>> resources = crawler.getResources();
                    final Collection<Indexable> deleted = crawler.getDeletedResources();
                    if (crawler.isFinished()) {
                        delete(deleted, root);
                        if (index(resources, root)) {
                            crawler.storeTimestamps();
                            return true;
                        }
                    }
                    return false;
                } else {
                    // can't traverse the root, but still mark it as scanned/finished
                    return true;
                }
            }
        }

        private static void reportRootScan(URL root, long duration) {
            try {
                Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                m.invoke(c.newInstance(), new Object[] {root.toExternalForm(), new Long(duration)});
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }

        private static <A, B> void diff(Map<A, B> oldMap, Map<A, B> newMap, Map<A, B> addedOrChangedEntries, Map<A, B> removedEntries) {
            for(A key : oldMap.keySet()) {
                if (!newMap.containsKey(key)) {
                    removedEntries.put(key, oldMap.get(key));
                } else {
                    if (!org.openide.util.Utilities.compareObjects(oldMap.get(key), newMap.get(key))) {
                        addedOrChangedEntries.put(key, newMap.get(key));
                    }
                }
            }

            for(A key : newMap.keySet()) {
                if (!oldMap.containsKey(key)) {
                    addedOrChangedEntries.put(key, newMap.get(key));
                }
            }
        }

        private static void printMap(Map<URL, List<URL>> deps, Level level) {
            Set<URL> sortedRoots = new TreeSet<URL>(C);
            sortedRoots.addAll(deps.keySet());
            for(URL url : sortedRoots) {
                LOGGER.log(level, "  {0}:\n", url); //NOI18N
                for(URL depUrl : deps.get(url)) {
                    LOGGER.log(level, "  -> {0}\n", depUrl); //NOI18N
                }
            }
        }

        private static final Comparator<URL> C = new Comparator<URL>() {
            public int compare(URL o1, URL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        };
    } // End of RootsWork class

    private final class InitialRootsWork extends RootsWork {

        private final boolean waitForProjects;

        public InitialRootsWork(Map<URL, List<URL>> scannedRoots2Depencencies, Set<URL> scannedBinaries, boolean waitForProjects) {
            super(scannedRoots2Depencencies, scannedBinaries, true);
            this.waitForProjects = waitForProjects;
        }
        
        public @Override boolean getDone() {
            try {
                if (waitForProjects) {
                    try {
                        OpenProjects.getDefault().openProjects().get();
                    } catch (Exception ex) {
                        // ignore
                    }
                }

                return super.getDone();
            } finally {
                if (state == State.INITIAL_SCAN_RUNNING) {
                    synchronized (RepositoryUpdater.this) {
                        if (state == State.INITIAL_SCAN_RUNNING) {
                            state = State.ACTIVE;
                        }
                    }
                }
            }
        }
    } // End of InitialRootsWork class

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
                        if (protectedMode == 0) {
                            enforceWork = true;
                        } else {
//                            LOGGER.log(Level.FINE, "Won't enforce {0} when in protected mode", work); //NOI18N
//                            wait = false;
                            // nobody should actually call schedule(work, true) from
                            // within a UserTask, SchedulerTask or Indexer
                            throw new IllegalStateException("Won't enforce " + work + " when in protected mode"); //NOI18N
                        }
                    }

                    if (!enforceWork) {
                        if (workInProgress != null) {
                            workInProgress.cancelBy(work);
                        }

                        // coalesce ordinary jobs
                        boolean absorbed = false;
                        if (!wait) {
                            for(Work w : todo) {
                                if (w.absorb(work)) {
                                    absorbed = true;
                                    break;
                                }
                            }
                        }

                        if (!absorbed) {
                            LOGGER.log(Level.FINE, "Scheduling {0}", work); //NOI18N
                            todo.add(work);
                        } else {
                            LOGGER.log(Level.FINE, "Work absorbed {0}", work); //NOI18N
                        }
                        
                        if (!scheduled && protectedMode == 0) {
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
                if (!allCancelled) {
                    // stop accepting new work and clean the queue
                    allCancelled = true;
                    todo.clear();

                    // stop the work currently being done
                    final Work work = workInProgress;
                    if (work != null) {
                        work.setCancelled(true);
                    }

                    // wait for until the current work is finished
                    while (scheduled) {
                        try {
                            todo.wait(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
        }

        public boolean isWorking() {
            synchronized (todo) {
                return scheduled || protectedMode > 0;
            }
        }

        public void enterProtectedMode() {
            synchronized (todo) {
                protectedMode++;
                LOGGER.log(Level.FINE, "Entering protected mode: {0}", protectedMode); //NOI18N
            }
        }

        public void exitProtectedMode(Runnable followupTask) {
            synchronized (todo) {
                if (protectedMode <= 0) {
                    throw new IllegalStateException("Calling exitProtectedMode without enterProtectedMode"); //NOI18N
                }

                // stash the followup task, we will run all of them when exiting the protected mode
                if (followupTask != null) {
                    if (followupTasks == null) {
                        followupTasks = new LinkedList<Runnable>();
                    }
                    followupTasks.add(followupTask);
                }

                protectedMode--;
                LOGGER.log(Level.FINE, "Exiting protected mode: {0}", protectedMode); //NOI18N

                if (protectedMode == 0) {
                    // in normal mode again, restart all delayed jobs
                    final List<Runnable> tasks = followupTasks;

                    // delaying of these tasks was just copied from the old java.source RepositoryUpdater
                    RequestProcessor.getDefault().create(new Runnable() {
                        public void run() {
                            schedule(new Work(false, false, false) {
                                protected @Override boolean getDone() {
                                    if (tasks != null) {
                                        for(Runnable task : tasks) {
                                            try {
                                                task.run();
                                            } catch (ThreadDeath td) {
                                                throw td;
                                            } catch (Throwable t) {
                                                LOGGER.log(Level.WARNING, null, t);
                                            }
                                        }
                                    }
                                    return true;
                                }
                            }, false);
                        }
                    }).schedule(FILE_LOCKS_DELAY);
                    LOGGER.log(Level.FINE, "Protected mode exited, scheduling postprocess tasks: {0}", tasks); //NOI18N
                }
            }
        }

        public boolean isInProtectedMode() {
            synchronized (todo) {
                return protectedMode > 0;
            }
        }

        // returns false when timed out
        public boolean waitUntilFinished(long timeout) throws InterruptedException {
            if (Utilities.holdsParserLock()) {
                throw new IllegalStateException("Can't wait for indexing to finish from inside a running parser task"); //NOI18N
            }

            synchronized (todo) {
                while (scheduled) {
                    if (timeout > 0) {
                        todo.wait(timeout);
                        return !scheduled;
                    } else {
                        todo.wait();
                    }
                }
            }

            return true;
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
            recordCaller();
            
            if (notInterruptible) {
                // ignore the request
                return;
            }

            synchronized (todo) {
                if (!cancelled) {
                    cancelled = true;
                    cancelledWork = workInProgress;
                    if (cancelledWork != null) {
                        cancelledWork.setCancelled(true);
                    }
                }
            }
        }

        @Override
        public void run(Result nil, final SchedulerEvent nothing) {
            synchronized (todo) {
                cancelled = false;
                cancelledWork = null;
            }
            try {
                _run();
            } finally {
                synchronized (todo) {
                    if (cancelledWork != null && !cancelledWork.isFinished()) {
                        if (!allCancelled) {
                            // push the work back in the queue
                            cancelledWork.setCancelled(false);
                            todo.add(0, cancelledWork);
                        }
                        cancelledWork = null;
                    }
                    if (todo.isEmpty()) {
                        scheduled = false;
                    } else {
                        Utilities.scheduleSpecialTask(this);
                    }
                    todo.notifyAll();
                }
            }
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private final List<Work> todo = new LinkedList<Work>();
        private Work workInProgress = null;
        private Work cancelledWork = null;
        private boolean scheduled = false;
        private boolean allCancelled = false;
        private boolean cancelled = false;
        private int protectedMode = 0;
        private List<Runnable> followupTasks = null;

        private void _run() {
            ProgressHandle progressHandle = null;
            try {
                for(Work work = getWork(); work != null; work = getWork()) {
                    if (progressHandle == null && work.supportsProgress()) {
                        progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class, "MSG_BackgroundCompileStart")); //NOI18N
                        progressHandle.start();
                    }
                    work.setProgressHandle(progressHandle);
                    try {
                        work.doTheWork();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        LOGGER.log(Level.WARNING, null, t);
                    } finally {
                        work.setProgressHandle(null);
                    }
                }
            } finally {
                if (progressHandle != null) {
                    progressHandle.finish();
                    progressHandle = null;
                }
            }
        }
        
        private Work getWork () {
            synchronized (todo) {
                Work w;
                if (!cancelled && protectedMode == 0 && todo.size() > 0) {
                    w = todo.remove(0);
                } else {
                    w = null;
                }
                workInProgress = w;
                return w;
            }
        }
    } // End of Task class

    private static final class DependenciesContext {

        private final Map<URL, List<URL>> initialRoots2Deps;
        private final Set<URL> initialBinaries;

        private final Set<URL> oldRoots;
        private final Set<URL> oldBinaries;

        private final Map<URL,List<URL>> newRoots2Deps;
        private final List<URL> newRootsToScan;
        private final Set<URL> newBinariesToScan;

        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;

        private final Stack<URL> cycleDetector;
        private final boolean useInitialState;

        public DependenciesContext (final Map<URL, List<URL>> scannedRoots2Deps, final Set<URL> scannedBinaries, boolean useInitialState) {
            assert scannedRoots2Deps != null;
            assert scannedBinaries != null;
            
            this.initialRoots2Deps = Collections.unmodifiableMap(scannedRoots2Deps);
            this.initialBinaries = Collections.unmodifiableSet(scannedBinaries);

            this.oldRoots = new HashSet<URL> (scannedRoots2Deps.keySet());
            this.oldBinaries = new HashSet<URL> (scannedBinaries);

            this.newRoots2Deps = new HashMap<URL,List<URL>>();
            this.newRootsToScan = new ArrayList<URL>();
            this.newBinariesToScan = new HashSet<URL>();

            this.scannedRoots = new HashSet<URL>();
            this.scannedBinaries = new HashSet<URL>();

            this.useInitialState = useInitialState;
            cycleDetector = new Stack<URL>();
        }

        public @Override String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(": {\n"); //NOI18N
            sb.append("  useInitialState=" + useInitialState).append("\n"); //NOI18N
            sb.append("  initialRoots2Deps=" + initialRoots2Deps).append("\n"); //NOI18N
            sb.append("  initialBinaries=" + initialBinaries).append("\n"); //NOI18N
            sb.append("  oldRoots=" + oldRoots).append("\n"); //NOI18N
            sb.append("  oldBinaries=" + oldBinaries).append("\n"); //NOI18N
            sb.append("  newRootsToScan=" + newRootsToScan).append("\n"); //NOI18N
            sb.append("  newBinariesToScan=" + newBinariesToScan).append("\n"); //NOI18N
            sb.append("  scannedRoots=" + scannedRoots).append("\n"); //NOI18N
            sb.append("  scannedBinaries=" + scannedBinaries).append("\n"); //NOI18N
            sb.append("} ----\n"); //NOI18N
            return sb.toString();
        }

    } // End of DependenciesContext class

    private final class Controller extends IndexingController {

        public Controller() {
            super();
            RepositoryUpdater.this.start(false);
        }
        
        @Override
        public void enterProtectedMode() {
            getWorker().enterProtectedMode();
        }

        @Override
        public void exitProtectedMode(Runnable followUpTask) {
            getWorker().exitProtectedMode(followUpTask);
        }

        @Override
        public boolean isInProtectedMode() {
            return getWorker().isInProtectedMode();
        }

        @Override
        public Map<URL, List<URL>> getRootDependencies() {
            return new HashMap<URL, List<URL>>(RepositoryUpdater.this.scannedRoots2Dependencies);
        }

        @Override
        public int getFileLocksDelay() {
            return FILE_LOCKS_DELAY;
        }

    } // End of Controller class

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
        return this.scannedRoots2Dependencies.keySet();
    }

    //Unit test method
    /* test */ Set<URL> getScannedUnknowns () {
        return this.scannedUnknown;
    }
}
