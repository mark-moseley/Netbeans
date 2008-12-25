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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.model.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *  CsmFile analogue of CsmFileTaskFactory
 * 
 * This factory should be registered in the global lookup by listing its fully qualified
 * name in file <code>META-INF/services/org.netbeans.modules.cnd.modelimpl.csm.scheduling.CsmFileTaskFactory</code>.
 * 
 * @author Sergey Grinev
 */
public abstract class CsmFileTaskFactory {

    private final Map<FileObject, CsmFile> fobj2csm = new HashMap<FileObject, CsmFile>();
    private final Map<Document, Pair> doc2task = new HashMap<Document, Pair>();
    private final ProgressListener progressListener = new ProgressListener();
    private final ModelListener modelListener = new ModelListener();
   
    public final static String USE_OWN_CARET_POSITION = "use-own-caret-position"; // NOI18N

    protected CsmFileTaskFactory() {
        CsmListeners.getDefault().addProgressListener(progressListener);
        CsmListeners.getDefault().addModelListener(modelListener);
    }

    protected abstract PhaseRunner createTask(FileObject fo);
    
    protected abstract Collection<FileObject> getFileObjects();

    protected abstract int taskDelay();

    protected abstract int rescheduleDelay();
    
    protected final void fileObjectsChanged() {
        final Set<FileObject> currentFiles = new HashSet<FileObject>(getFileObjects());
        final long id = Math.round(100.0*Math.random());
        final String name = this.getClass().getName();
        if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: POST worker " + id);}
        DECISION_WORKER.post(new Runnable() {

            public void run() {
                long start = System.currentTimeMillis();
                if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: RUN worker " + id + " [" + name + "]" );}
                stateChangedImpl(currentFiles);
                if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: DONE worker " + id + " after " + (System.currentTimeMillis() - start) + "ms.");}
            }
        });
    }

    private boolean checkMimeType(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        } else {
            String mimeType = fileObject.getMIMEType();
            return MIMENames.isHeaderOrCppOrC(mimeType);
        }
    }

    private void stateChangedImpl(Collection<FileObject> currentFiles) {
        Map<Document, Pair> docToRemove = new HashMap<Document, Pair>();
        Map<Document, Pair> docToAdd = new HashMap<Document, Pair>();

        synchronized (this) {
            List<FileObject> addedFiles = new ArrayList<FileObject>(currentFiles);
            List<FileObject> removedFiles = new ArrayList<FileObject>(fobj2csm.keySet());

            addedFiles.removeAll(fobj2csm.keySet());
            removedFiles.removeAll(currentFiles);

            //remove old tasks:
            for (FileObject r : removedFiles) {
                CsmFile csmFile = fobj2csm.remove(r);

                if (csmFile == null) {
                    //TODO: log
                    continue;
                }
                Document doc = CsmUtilities.getDocument(r);
                docToRemove.put(doc, doc2task.remove(doc));
            }

            List<FileObject> verifiedFiles = new ArrayList<FileObject>(fobj2csm.keySet());
            // Model events should be redesigned. It is still inconvenient for clients. Move to lookup of DataObject direction. Stop hacking!
            // verify rest task
            for (FileObject v : verifiedFiles) {
                if (v == null) {
                    continue;
                }
                if (!v.isValid()) {
                    continue;
                }
                if (!checkMimeType(v)) {
                    continue;
                }

                CsmFile csmFile = getCsmFile(v);
                if (csmFile != null) {
                    CsmFile oldCsmFile = fobj2csm.get(v);
                    if (!csmFile.equals(oldCsmFile)) {
                        Document doc = CsmUtilities.getDocument(v);

                        fobj2csm.remove(v);
                        docToRemove.put(doc, doc2task.remove(doc));

                        PhaseRunner task = createTask(v);
                        Pair pair = new Pair(task);
                        docToAdd.put(doc, pair);
                        fobj2csm.put(v, csmFile);
                        doc2task.put(doc, pair);
                    }
                }
            }

            //add new tasks:
            for (FileObject fileObject : addedFiles) {
                if (fileObject == null) {
                    continue;
                }
                if (!fileObject.isValid()) {
                    continue;
                }
                if (!checkMimeType(fileObject)) {
                    continue;
                }

                CsmFile csmFile = getCsmFile(fileObject);                
                if (csmFile != null) {
                    Document doc = CsmUtilities.getDocument(fileObject);

                    PhaseRunner task = createTask(fileObject);
                    Pair pair = new Pair(task);
                    docToAdd.put(doc, pair);

                    fobj2csm.put(fileObject, csmFile);
                    doc2task.put(doc, pair);
                }
            }
        }

        for (Entry<Document, Pair> e : docToRemove.entrySet()) {
            if (OpenedEditors.SHOW_TIME) {System.err.println("CFTF: removing " + CsmUtilities.getCsmFile(e.getKey(), false).getAbsolutePath());}
            if (e!=null && e.getValue()!=null ) {
                PhaseRunner runner = e.getValue().runner;
                Task task = e.getValue().task;
                runner.cancel();
                if (task != null) {
                    task.cancel();
                }
                post(e.getValue(), e.getKey(), PhaseRunner.Phase.CLEANUP, IMMEDIATELY);
            }
            // it isn't necessary to check mime type here -
            // we checked it when adding task
            CsmStandaloneFileProvider.getDefault().notifyClosed(CsmUtilities.getCsmFile(e.getKey(), false));
        }

        for (Entry<Document, Pair> e : docToAdd.entrySet()) {
            if (OpenedEditors.SHOW_TIME) {System.err.println("CFTF: adding "+ //NOI18N
                    (CsmUtilities.getCsmFile(e.getKey(), false).isParsed() ? PhaseRunner.Phase.PARSED : PhaseRunner.Phase.INIT)+
                    " "+e.getValue().runner.toString()+" " + CsmUtilities.getCsmFile(e.getKey(), false).getAbsolutePath());} //NOI18N
                post(e.getValue(), e.getKey(), CsmUtilities.getCsmFile(e.getKey(), false).isParsed() ? PhaseRunner.Phase.PARSED : PhaseRunner.Phase.INIT, taskDelay());
        }
    }

    private static CsmFile getCsmFile(FileObject fo) {
        CsmFile csmFile = null;
        Document doc = CsmUtilities.getDocument(fo);
        if (doc != null) {
            csmFile = CsmUtilities.getCsmFile(doc, false);
        }
        if (csmFile == null) {
            csmFile = CsmUtilities.getCsmFile(fo, false);
        }
        if (csmFile == null) {
            csmFile = CsmStandaloneFileProvider.getDefault().getCsmFile(fo);
        }        
        return csmFile;
    }

    private static final int IMMEDIATELY = 0;
    
    public final synchronized void reschedule(FileObject file) throws IllegalArgumentException {
        runTask(file, PhaseRunner.Phase.PARSED, rescheduleDelay());
    }
    
    private final void runTask(FileObject file, PhaseRunner.Phase phase, int delay) {
        Document doc = CsmUtilities.getDocument(file);
        Pair pr = doc2task.get(doc);
        
        if (pr!=null) {
            pr.runner.cancel();
            if (pr.task != null) {
                pr.task.cancel();
            }
            if (!pr.runner.isValid()) {
                PhaseRunner runner = createTask(file);
                assert runner.isValid();
                pr = new Pair(runner);
                doc2task.put(doc, pr);
            }

            // Run the same task for related document if it exists
            Document doc2 = (Document) doc.getProperty(Document.class);
            if (doc2 != null) {
                PhaseRunner task = createTask(CsmUtilities.getFileObject(doc2));
                Pair pair = new Pair(task);
                doc2.putProperty(USE_OWN_CARET_POSITION, false);
                doc.putProperty(USE_OWN_CARET_POSITION, true);
                if (pair != null) {
                    post(pair, doc2, phase, delay);
                }
            }

            post(pr, doc, phase, delay);
        }
    }
    
    private final void post(Pair pr, Document doc, PhaseRunner.Phase phase, int delay) {
        if (pr.runner.isHighPriority()) {
            pr.task = HIGH_PRIORITY_WORKER.post(new CsmSafeRunnable( getRunnable(pr.runner, phase), doc), delay , Thread.NORM_PRIORITY);
        } else {
            pr.task = WORKER.post(new CsmSafeRunnable( getRunnable(pr.runner, phase), doc), delay );
        }
    }
    
    private static RequestProcessor WORKER = new RequestProcessor("CsmFileTaskFactory", 1); //NOI18N
    private static RequestProcessor HIGH_PRIORITY_WORKER = new RequestProcessor("CsmHighPriorityFileTaskFactory", 1); //NOI18N
    private static RequestProcessor DECISION_WORKER = new RequestProcessor("CsmDecisionFileTaskFactory", 1); //NOI18N

    static {
        CsmFileTaskFactoryManager.ACCESSOR = new CsmFileTaskFactoryManager.Accessor() {

            public void fireChangeEvent(CsmFileTaskFactory f) {
                f.fileObjectsChanged();
            }
        };
    }

    private class ProgressListener extends CsmProgressAdapter {

        @Override
        public void fileParsingFinished(CsmFile file) {
            runTask(CsmUtilities.getFileObject(file), PhaseRunner.Phase.PARSED, IMMEDIATELY);
        }

        @Override
        public void fileParsingStarted(CsmFile file) {
            runTask(CsmUtilities.getFileObject(file), PhaseRunner.Phase.PARSING_STARTED, IMMEDIATELY);
        }
    }
    
    private class ModelListener implements CsmModelListener {

        public void projectOpened(CsmProject project) {
            // do nothing
        }

        public void projectClosed(CsmProject project) {
            // TODO: do something? Cleanup, maybe?
        }

        public void modelChanged(CsmChangeEvent e) {
            for (CsmFile f : e.getRemovedFiles()){
                Document doc = CsmUtilities.getDocument(f);
                if (doc2task.get(doc) != null) {
                    synchronized (this) {
                        runTask(CsmUtilities.getFileObject(f), PhaseRunner.Phase.CLEANUP, IMMEDIATELY);
                        doc2task.put(doc, new Pair(lazyRunner()));
                        // Run the same task for related document if it exists
                        Document doc2 = (Document)doc.getProperty(Document.class);
                        if(doc2 != null) {
                            runTask(CsmUtilities.getFileObject(doc2), PhaseRunner.Phase.CLEANUP, IMMEDIATELY);
                            doc2task.put(doc2, new Pair(lazyRunner()));
                        }
                    }
                }
            }
            if (!e.getNewFiles().isEmpty()){
                fileObjectsChanged();
            }
        }
        
    }
    
    public static interface PhaseRunner {
        public enum Phase { 
            INIT,
            PARSING_STARTED, 
            PARSED, 
            CLEANUP
        };
        public abstract void run(Phase phase);
        public abstract boolean isValid();
        public abstract void cancel();
        public abstract boolean isHighPriority();
    }
    
    private static final class Pair {
        private final PhaseRunner runner;
        private Task task;
        private Pair(PhaseRunner runner) {
            this.runner = runner;
        }
    }
    
    private static final Runnable getRunnable(final PhaseRunner pr, final PhaseRunner.Phase phase) {
        return new Runnable() {
            public void run() {
                pr.run(phase);
            }
        };
    }

    protected static PhaseRunner lazyRunner() {
        return new PhaseRunner() {
            public void run(Phase phase) {
                // do nothing for all phases
            }

            public boolean isValid() {
                return true;
            }

            public void cancel() {
            }

            public boolean isHighPriority() {
                return false;
            }
        };
    }
    
    private static final class CsmSafeRunnable implements Runnable {
        private Document doc;
        private Runnable run;
        public CsmSafeRunnable(Runnable run, Document doc) {
            this.run = run;
            this.doc = doc;
        }

        public void run() {
            if (CsmUtilities.getCsmFile(doc, false).isValid() /*&& (file.isHeaderFile() || file.isSourceFile())*/) {
                run.run();
            }
        }
    }

}
