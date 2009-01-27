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
package org.netbeans.api.java.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavaSourceTaskFactoryManager;
import org.netbeans.modules.parsing.api.Source;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 * A factory for tasks that will be run in the {@link JavaSource} Java parsing harness.
 *
 * Please note that there is usually no need to implement this class directly,
 * as there are support classes for common {@link JavaSourceTaskFactory} implementations.
 *
 * This factory should be registered in the global lookup using {@link org.openide.util.lookup.ServiceProvider}.
 * 
 * @see EditorAwareJavaSourceTaskFactory
 * @see CaretAwareJavaSourceTaskFactory
 * @see LookupBasedJavaSourceTaskFactory
 *
 * @author Jan Lahoda
 */
public abstract class JavaSourceTaskFactory {

    private static final Logger LOG = Logger.getLogger(JavaSourceTaskFactory.class.getName());
            static final String BEFORE_ADDING_REMOVING_TASKS = "beforeAddingRemovingTasks"; //NOI18N
            static final String FILEOBJECTS_COMPUTATION = "fileObjectsComputation"; //NOI18N
            
    private final Phase phase;
    private final Priority priority;

    /**Construct the JavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    protected JavaSourceTaskFactory(@NonNull Phase phase, @NonNull Priority priority) {
        this.phase = phase;
        this.priority = priority;
        this.file2Task = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
        this.file2JS = new HashMap<FileObject, JavaSource>();
    }

    /**Create task for a given file. This task will be registered into the {@link JavaSource}
     * parsing harness with a given {@link #getPriority priority} and {@link #getPhase phase}.
     *
     * Please note that this method should run as quickly as possible.
     *
     * @param file for which file the task should be created.
     * @return created {@link CancellableTask}  for a given file.
     */
    protected abstract @NonNull CancellableTask<CompilationInfo> createTask(FileObject file);

    /**Specifies on which files should be registered tasks created by this factory.
     * On {@link JavaSource}'s corresponding to {@link FileObject}s returned from
     * this method will be registered tasks created by the {@link #createTask} method
     * of this factory.
     *
     * If this list changes, a change event should be fired to all registered
     * {@link ChangeListener}s.
     *
     * @return list of {@link FileObject} on which tasks from this factory should be
     * registered.
     * @see #createTask
     * @see #addChangeListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    protected abstract @NonNull Collection<FileObject> getFileObjects();

    /**Notify the infrastructure that the collection of fileobjects has been changed.
     * The infrastructure calls {@link #getFileObjects()} to get a new collection files.
     */
    protected final void fileObjectsChanged() {
        LOG.log(Level.FINEST, FILEOBJECTS_COMPUTATION);

        final List<FileObject> currentFiles = new ArrayList<FileObject>(getFileObjects());

        if (SYNCHRONOUS_EVENTS) {
            stateChangedImpl(currentFiles);
        } else {
            WORKER.post(new Runnable() {
                public void run() {
                    stateChangedImpl(currentFiles);
                }
            });
        }
    }

    /**for tests:
     */
    static boolean SYNCHRONOUS_EVENTS = false;

    private void stateChangedImpl(List<FileObject> currentFiles) {
        Map<JavaSource, CancellableTask<CompilationInfo>> toRemove = new HashMap<JavaSource, CancellableTask<CompilationInfo>>();
        Map<JavaSource, CancellableTask<CompilationInfo>> toAdd = new HashMap<JavaSource, CancellableTask<CompilationInfo>>();
        
        synchronized (this) {
            List<FileObject> addedFiles = new ArrayList<FileObject>(currentFiles);
            List<FileObject> removedFiles = new ArrayList<FileObject>(file2Task.keySet());
            
            addedFiles.removeAll(file2Task.keySet());
            removedFiles.removeAll(currentFiles);
            
            //remove old tasks:
            for (FileObject r : removedFiles) {
                JavaSource source = file2JS.remove(r);
                
                if (source == null) {
                    //TODO: log
                    continue;
                }
                
                toRemove.put(source, file2Task.remove(r));
            }
            
            //add new tasks:
            for (FileObject a : addedFiles) {
                if (a == null)
                    continue;
                if (!a.isValid()) {
                    continue;
                }
                JavaSource js = JavaSource.forFileObject(a);
                
                if (js != null) {
                    CancellableTask<CompilationInfo> task = createTask(a);
                    
                    toAdd.put(js, task);
                    
                    file2Task.put(a, task);
                    file2JS.put(a, js);
                }
            }
        }
        
        LOG.log(Level.FINEST, BEFORE_ADDING_REMOVING_TASKS);
        
        for (Entry<JavaSource, CancellableTask<CompilationInfo>> e : toRemove.entrySet()) {
            ACCESSOR2.removePhaseCompletionTask(e.getKey(), e.getValue());
        }
        
        for (Entry<JavaSource, CancellableTask<CompilationInfo>> e : toAdd.entrySet()) {
            try {
                ACCESSOR2.addPhaseCompletionTask(e.getKey(), e.getValue(), phase, priority);
            } catch (FileObjects.InvalidFileException ie) {
                LOG.info("JavaSource.addPhaseCompletionTask called on deleted file");       //NOI18N
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
       
    /**Re-run task created by this factory for given file.
     * If the task has not yet been run, does nothing.
     *
     * @param file task created by this factory for this file is re-run.
     */
    protected final synchronized void reschedule(FileObject file) throws IllegalArgumentException {
        JavaSource source = file2JS.get(file);

        if (source == null) {
//            throw new IllegalArgumentException("No JavaSource for given file.");
            return ;
}
        
        CancellableTask<CompilationInfo> task = file2Task.get(file);
        
        if (task == null) {
//                    throw new IllegalArgumentException("This factory did not created any task for " + FileUtil.getFileDisplayName(file)); // NOI18N
            return ;
        }
        
        ACCESSOR2.rescheduleTask(source, task);
    }

    private final Map<FileObject, CancellableTask<CompilationInfo>> file2Task;
    private final Map<FileObject, JavaSource> file2JS;

    private static RequestProcessor WORKER = new RequestProcessor("JavaSourceTaskFactory", 1); // NOI18N
    
    static {
        JavaSourceTaskFactoryManager.ACCESSOR = new JavaSourceTaskFactoryManager.Accessor() {
            public void fireChangeEvent(JavaSourceTaskFactory f) {
                f.fileObjectsChanged();
            }
        };
        ACCESSOR2 = new Accessor2() {
            public void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority) throws IOException {
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js, task, phase, priority);                
            }

            public void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task) {
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js,task);
            }

            public void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task) {
                JavaSourceAccessor.getINSTANCE().rescheduleTask(js, task);
            }
        };
    }

    static interface Accessor2 {
        public abstract void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority ) throws IOException;
        public abstract void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task );
        public abstract void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task);
    }
    
    
    static Accessor2 ACCESSOR2;
    
}
