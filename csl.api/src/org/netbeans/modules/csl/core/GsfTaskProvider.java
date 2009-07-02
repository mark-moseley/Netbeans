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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;


/**
 * Task provider which provides tasks for the tasklist corresponding
 * to hints in files.
 * 
 * @todo Register via instanceCreate to ensure this is a singleton
 *   (Didn't work - see uncommented code below; try to fix.)
 * @todo Exclude tasks that are not Rule#showInTaskList==true
 * 
 * Much of this class is based on the similar JavaTaskProvider in
 * java/source by Stanislav Aubrecht and Jan Lahoda
 * 
 * @author Jan Jancura
 */
public final class GsfTaskProvider extends PushTaskScanner  {

    private static final Logger LOG = Logger.getLogger(GsfTaskProvider.class.getName());
    
    private static GsfTaskProvider INSTANCE;

    private Callback callback;
    private TaskScanningScope scope;

    public GsfTaskProvider () {
        this (getAllLanguageNames ());
        INSTANCE = this;
    }

    private GsfTaskProvider (String languageList) {
        super (
            NbBundle.getMessage (GsfTaskProvider.class, "GsfTasks", languageList), //NOI18N
            NbBundle.getMessage (GsfTaskProvider.class, "GsfTasksDesc", languageList), //NOI18N
            null
        );
    }

    @Override
    public synchronized void setScope (TaskScanningScope scope, Callback callback) {
        //cancel all current operations:
        cancelAllCurrent();

        this.scope = scope;
        this.callback = callback;

        if (scope == null || callback == null)
            return ;

        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            enqueue(new Work(p, callback));
        }
    }

    public static void refresh (FileObject file) {
        if (INSTANCE != null) {
            INSTANCE.refreshImpl (file);
        }
    }
    
    private synchronized void refreshImpl (FileObject file) {
        LOG.log(Level.FINE, "refresh: {0}", file); //NOI18N

        if (scope == null || callback == null)
            return ; //nothing to refresh

        if (!scope.isInScope(file)) {
            if (!file.isFolder())
                return;

            //the given file may be a parent of some file that is in the scope:
            for (FileObject inScope : scope.getLookup().lookupAll(FileObject.class)) {
                if (FileUtil.isParentOf(file, inScope)) {
                    enqueue(new Work(inScope, callback));
                }
            }

            return ;
        }

        LOG.log(Level.FINE, "enqueing work for: {0}", file); //NOI18N
        enqueue(new Work(file, callback));
    }


    /* package */ static String getAllLanguageNames () {
        StringBuilder sb = new StringBuilder ();
        for (Language language : LanguageRegistry.getInstance ()) {
            if (sb.length () > 0) {
                sb.append (", "); //NOI18N
            }
            sb.append (language.getDisplayName ());
        }

        return sb.toString ();
    }

    private static final Set<RequestProcessor.Task> TASKS = new HashSet<RequestProcessor.Task>();
    private static boolean clearing;
    private static final RequestProcessor WORKER = new RequestProcessor("CSL Task Provider"); //NOI18N

    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = WORKER.post(w);

            TASKS.add(task);
            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove((RequestProcessor.Task) task);
                        }
                    }
                }
            });
            if (task.isFinished()) {
                TASKS.remove(task);
            }
        }
    }

    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (RequestProcessor.Task t : TASKS) {
                    t.cancel();
                }
                TASKS.clear();
            } finally {
                clearing = false;
            }
        }
    }

    private static final class Work implements Runnable {
        private final FileObject file;
        private final Project project;
        private final Callback callback;

        public Work(FileObject file, Callback callback) {
            Parameters.notNull("file", file); //NOI18N
            Parameters.notNull("callback", callback); //NOI18N
            this.file = file;
            this.project = null;
            this.callback = callback;
        }

        public Work(Project project, Callback callback) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("callback", callback); //NOI18N
            this.file = null;
            this.project = project;
            this.callback = callback;
        }

        public void run() {
            Collection<? extends IndexResult> results = null;

            if (file != null) {
                Collection<FileObject> roots = QuerySupport.findRoots (
                    file,
                    null,
                    Collections.<String> emptyList (),
                    Collections.<String> emptyList ()
                );
                
                String relativePath = null;
                for(FileObject root : roots) {
                    if (null != (relativePath = FileUtil.getRelativePath(root, file))) {
                        break;
                    }
                }

                LOG.log(Level.FINE, "Querying TL index for {0}", relativePath); //NOI18N
                if (relativePath != null) {
                    try {
                        QuerySupport querySupport = QuerySupport.forRoots (
                            TLIndexerFactory.INDEXER_NAME,
                            TLIndexerFactory.INDEXER_VERSION,
                            roots.toArray (new FileObject [roots.size ()])
                        );
                        results = querySupport.query("_sn", relativePath, Kind.EXACT); //NOI18N
                    } catch (IOException ioe) {
                        LOG.log(Level.WARNING, null, ioe);
                    }
                }
            } else { // project != null
                Collection<FileObject> roots = QuerySupport.findRoots (
                    project,
                    null,
                    Collections.<String> emptyList (),
                    Collections.<String> emptyList ()
                );
                try {
                    QuerySupport querySupport = QuerySupport.forRoots (
                        TLIndexerFactory.INDEXER_NAME,
                        TLIndexerFactory.INDEXER_VERSION,
                        roots.toArray (new FileObject [roots.size ()])
                    );
                    // search for all documents in the roots
                    results = querySupport.query ("_sn", "", Kind.PREFIX); //NOI18N
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }

            if (results != null) {
                pushTasks(results, callback);
            }
        }

        private static void pushTasks(Collection<? extends IndexResult> results, Callback callback) {
            Map<FileObject, List<Task>> tasks = new HashMap<FileObject, List<Task>>();

            for (IndexResult result : results) {
                FileObject f = result.getFile();
                if (f == null || !f.isValid()) {
                    continue;
                }

                List<Task> l = tasks.get(f);
                if (l == null) {
                    l = new ArrayList<Task>();
                    tasks.put(f, l);
                }

                String description = result.getValue (TLIndexerFactory.FIELD_DESCRIPTION);
                if (description == null) {
                    continue;
                }

                int lineNumber = 1;
                try {
                    lineNumber = Integer.parseInt(result.getValue(TLIndexerFactory.FIELD_LINE_NUMBER));
                } catch (NumberFormatException ex) {
                    // ignore
                }

                Task task = Task.create(
                    f,
                    result.getValue(TLIndexerFactory.FIELD_GROUP_NAME),
                    description,
                    lineNumber
                );
                l.add(task);
            }

            for (FileObject f : tasks.keySet()) {
                List<Task> l = tasks.get(f);
                LOG.log(Level.FINE, "Refreshing TL for {0} with {1}", new Object [] { f, l }); //NOI18N
                callback.setTasks(f, l);
            }
        }
    } // End of Work class
}

