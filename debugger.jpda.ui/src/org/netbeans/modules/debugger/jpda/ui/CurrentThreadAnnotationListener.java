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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.openide.ErrorManager;

import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link JPDADebugger#PROP_CURRENT_THREAD}
 * property and annotates current line and call stack for
 * {@link org.netbeans.api.debugger.jpda.JPDAThread}s in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class CurrentThreadAnnotationListener extends DebuggerManagerAdapter {
    
    private static final int ANNOTATION_SCHEDULE_TIME = 100;

    // annotation for current line
    private transient Object                currentPC;
    private transient Object                currentPCLock = new Object();
    private transient boolean               currentPCSet = false;
    private JPDAThread                      currentThread;
    private JPDADebugger                    currentDebugger;
    private SourcePath                      currentSourcePath;
    private String                          currentLanguage;
    private AllThreadsAnnotator             allThreadsAnnotator;



    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens JPDADebuggerEngineImpl and DebuggerManager.
     */
    @Override
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            updateCurrentDebugger ();
            updateCurrentThread ();
            annotate ();
        } else
        if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
            updateCurrentThread ();
            annotate ();
        } else
        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propertyName)) {
            updateCurrentThread ();
            annotate ();
        } else
        if (JPDADebugger.PROP_STATE.equals(propertyName)) {
            annotate ();
        }
        if (JPDADebugger.PROP_THREAD_STARTED.equals(propertyName)) {
            synchronized (this) {
                if (allThreadsAnnotator != null) {
                    allThreadsAnnotator.add((JPDAThread) e.getNewValue());
                }
            }
        }
    }


    // helper methods ..........................................................

    private synchronized void updateCurrentDebugger () {
        JPDADebugger newDebugger = getCurrentDebugger ();
        if (currentDebugger == newDebugger) return;
        if (currentDebugger != null)
            currentDebugger.removePropertyChangeListener (this);
        if (allThreadsAnnotator != null) {
            allThreadsAnnotator.cancel();
            allThreadsAnnotator = null;
        }
        currentSourcePath = null;
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener (this);
            allThreadsAnnotator = new AllThreadsAnnotator(newDebugger);
            currentSourcePath = getCurrentSourcePath(newDebugger);
        }
        currentDebugger = newDebugger;
    }
    
    private static JPDADebugger getCurrentDebugger () {
        DebuggerEngine currentEngine = DebuggerManager.
            getDebuggerManager ().getCurrentEngine ();
        if (currentEngine == null) return null;
        return currentEngine.lookupFirst(null, JPDADebugger.class);
    }

    private void updateCurrentThread () {
        AllThreadsAnnotator allThreadsAnnotator;
        JPDAThread oldCurrent = null;
        JPDAThread newCurrent = null;
        synchronized (this) {
            // get current thread
            if (currentDebugger != null) {
                oldCurrent = currentThread;
                currentThread = currentDebugger.getCurrentThread ();
                newCurrent = currentThread;
            } else {
                currentThread = null;
            }
            allThreadsAnnotator = this.allThreadsAnnotator;
        }
        if (allThreadsAnnotator != null) {
            if (oldCurrent != null) {
                allThreadsAnnotator.annotate(oldCurrent);
            }
            if (newCurrent != null) {
                allThreadsAnnotator.annotate(newCurrent);
            }
        }
    }
    
    private SourcePath getCurrentSourcePath(JPDADebugger debugger) {
        Session currentSession = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                currentSession = sessions[i];
                break;
            }
        }
        // TODO: Listen on changes of the language!
        synchronized (this) {
            currentLanguage = currentSession == null ? 
                null : currentSession.getCurrentLanguage ();
        }
        DebuggerEngine currentEngine = (currentSession == null) ?
            null : currentSession.getCurrentEngine();
        SourcePath sourcePath = (currentEngine == null) ? 
            null : currentEngine.lookupFirst(null, SourcePath.class);
        return sourcePath;
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate () {
        // 1) no current thread => remove annotations
        CallStackFrame[] stack;
        final CallStackFrame csf;
        JPDADebugger debugger;
        final SourcePath sourcePath;
        final String language;
        synchronized (this) {
            debugger = currentDebugger;
            if ( (currentThread == null) ||
                 (debugger.getState () != JPDADebugger.STATE_STOPPED) ) {
                synchronized (currentPCLock) {
                    currentPCSet = false; // The annotation is goint to be removed
                }
                removeAnnotations ();
                return;
            }

            // 2) get call stack & Line
            try {
                stack = currentThread.getCallStack ();
            } catch (AbsentInformationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                synchronized (currentPCLock) {
                    currentPCSet = false; // The annotation is goint to be removed
                }
                removeAnnotations ();
                return;
            }
            csf = debugger.getCurrentCallStackFrame ();
            sourcePath = currentSourcePath;
            language = currentLanguage;
        }

        // 3) annotate current line & stack
        synchronized (currentPCLock) {
            currentPCSet = true; // The annotation is goint to be set
        }
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // show current line
                JPDAThread thread;
                synchronized (CurrentThreadAnnotationListener.this) {
                    thread = currentThread;
                }
                synchronized (currentPCLock) {
                    if (currentPC != null)
                        EditorContextBridge.getContext().removeAnnotation (currentPC);
                    if (csf != null && sourcePath != null && thread != null) {

                        sourcePath.showSource (csf, language);
                        // annotate current line
                        currentPC = sourcePath.annotate (thread, language);
                    }
                }
            }
        });
        annotateCallStack (stack, sourcePath);
    }


    // do not need synchronization, called in a 1-way RP
    private HashMap               stackAnnotations = new HashMap ();
    
    private RequestProcessor rp = new RequestProcessor("Debugger Thread Annotation Refresher");

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task taskRemove;
    private RequestProcessor.Task taskAnnotate;
    private CallStackFrame[] stackToAnnotate;
    private SourcePath sourcePathToAnnotate;

    private void removeAnnotations () {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create (new RemoveAnnotationsTask());
            }
        }
        taskRemove.schedule(ANNOTATION_SCHEDULE_TIME);
    }

    private void annotateCallStack (
        CallStackFrame[] stack,
        SourcePath sourcePath
    ) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            this.stackToAnnotate = stack;
            this.sourcePathToAnnotate = sourcePath;
            if (taskAnnotate == null) {
                taskAnnotate = rp.create (new AnnotateCallStackTask());
            }
        }
        taskAnnotate.schedule(ANNOTATION_SCHEDULE_TIME);
    }
    
    private class RemoveAnnotationsTask implements Runnable {
        public void run () {
            synchronized (currentPCLock) {
                if (currentPCSet) {
                    // Keep the set PC
                    return ;
                }
                if (currentPC != null)
                    EditorContextBridge.getContext().removeAnnotation (currentPC);
                currentPC = null;
            }
            Iterator i = stackAnnotations.values ().iterator ();
            while (i.hasNext ())
                EditorContextBridge.getContext().removeAnnotation (i.next ());
            stackAnnotations.clear ();
        }
    }
    
    private class AnnotateCallStackTask implements Runnable {
        public void run () {
            CallStackFrame[] stack;
            SourcePath sourcePath;
            synchronized (rp) {
                if (stackToAnnotate == null) {
                    return ; // Nothing to do
                }
                stack = stackToAnnotate;
                sourcePath = sourcePathToAnnotate;
                stackToAnnotate = null;
                sourcePathToAnnotate = null;
            }
            HashMap newAnnotations = new HashMap ();
            int i, k = stack.length;
            for (i = 1; i < k; i++) {

                // 1) check Line
                String language = stack[i].getDefaultStratum();                    
                String resourceName = EditorContextBridge.getRelativePath
                    (stack[i], language);
                int lineNumber = stack[i].getLineNumber (language);
                String line = resourceName + lineNumber;

                // 2) line already annotated?
                if (newAnnotations.containsKey (line))
                    continue;

                // 3) line has been annotated?
                Object da = stackAnnotations.remove (line);
                if (da == null) {
                    // line has not been annotated -> create annotation
                    da = sourcePath.annotate (stack[i], language);
                }

                // 4) add new line to hashMap
                if (da != null)
                    newAnnotations.put (line, da);
            } // for

            // delete old anotations
            Iterator iter = stackAnnotations.values ().iterator ();
            while (iter.hasNext ())
                EditorContextBridge.getContext().removeAnnotation (
                    iter.next ()
                );
            stackAnnotations = newAnnotations;
        }
    }
    
    private class AllThreadsAnnotator implements Runnable, PropertyChangeListener {
        
        private boolean active = true;
        private JPDADebugger debugger;
        private Map<JPDAThread, Object> threadAnnotations = new HashMap<JPDAThread, Object>();
        private Set<JPDAThread> threadsToAnnotate = new HashSet<JPDAThread>();
        private Map<JPDAThread, FutureAnnotation> futureAnnotations = new HashMap<JPDAThread, FutureAnnotation>();
        private Set<Object> annotationsToRemove = new HashSet<Object>();
        private RequestProcessor.Task task = rp.create(this);
        
        public AllThreadsAnnotator(JPDADebugger debugger) {
            this.debugger = debugger;
            for (JPDAThread t : debugger.getThreadsCollector().getAllThreads()) {
                add(t);
            }
        }
        
        public void add(JPDAThread t) {
            ((Customizer) t).addPropertyChangeListener(this);
            annotate(t);
        }
        
        public void remove(JPDAThread t) {
            ((Customizer) t).removePropertyChangeListener(this);
            synchronized (this) {
                Object annotation = threadAnnotations.remove(t);
                if (annotation != null) {
                    annotationsToRemove.add(annotation);
                    task.schedule(ANNOTATION_SCHEDULE_TIME);
                }
            }
        }
        
        public synchronized void cancel() {
            active = false;
            for (JPDAThread t : new HashSet<JPDAThread>(threadAnnotations.keySet())) {
                remove(t);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                if (!active) {
                    ((Customizer) evt.getSource()).removePropertyChangeListener(this);
                    return ;
                }
            }
            JPDAThread t = (JPDAThread) evt.getSource();
            annotate(t);
        }
        
        public synchronized void annotate(JPDAThread t) {
            Object annotation = threadAnnotations.remove(t);
            //System.err.println("SCHEDULE removal of "+annotation+" for "+t+" ("+t.getName()+")");
            if (annotation != null) {
                threadsToAnnotate.remove(t);
                annotationsToRemove.add(annotation);
                task.schedule(ANNOTATION_SCHEDULE_TIME);
            }
            if (t != debugger.getCurrentThread()) {
                threadsToAnnotate.add(t);
                FutureAnnotation future = futureAnnotations.get(t);
                if (future == null) {
                    future = new FutureAnnotation(t);
                }
                threadAnnotations.put(t, future);
                futureAnnotations.put(t, future);
                task.schedule(ANNOTATION_SCHEDULE_TIME);
                //System.err.println("SCHEDULE annotation of "+t+" ("+t.getName()+")"+", have future = "+future);
            }
        }
        
        public void run() {
            Set<Object> annotationsToRemove;
            Set<JPDAThread> threadsToAnnotate;
            Map<JPDAThread, FutureAnnotation> futureAnnotations;
            synchronized (this) {
                annotationsToRemove = new HashSet<Object>(this.annotationsToRemove);
                this.annotationsToRemove.clear();
                threadsToAnnotate = new HashSet<JPDAThread>(this.threadsToAnnotate);
                this.threadsToAnnotate.clear();
                futureAnnotations = new HashMap<JPDAThread, FutureAnnotation>(this.futureAnnotations);
                this.futureAnnotations.clear();
                /*for (JPDAThread t : threadsToAnnotate) {
                    FutureAnnotation future = (FutureAnnotation) this.threadAnnotations.get(t);
                    //this.threadAnnotations.put(t, future);
                    futureAnnotations.put(t, future);
                }*/
                //System.err.println("TASK: annotationsToRemove = "+annotationsToRemove);
                //System.err.println("      threadsToAnnotate = "+threadsToAnnotate);
            }
            for (Object annotation : annotationsToRemove) {
                if (annotation instanceof FutureAnnotation) {
                    annotation = ((FutureAnnotation) annotation).getAnnotation();
                    if (annotation == null) {
                        continue;
                    }
                }
                EditorContextBridge.getContext().removeAnnotation(annotation);
            }
            Map<JPDAThread, Object> threadAnnotations = new HashMap<JPDAThread, Object>();
            Set<JPDAThread> removeFutures = new HashSet<JPDAThread>();
            for (JPDAThread t : threadsToAnnotate) {
                Object annotation;
                if (currentSourcePath != null) {
                    annotation = currentSourcePath.annotate(t, currentLanguage, false);
                } else {
                    annotation = null;
                }
                if (annotation != null) {
                    threadAnnotations.put(t, annotation);
                    futureAnnotations.get(t).setAnnotation(annotation);
                } else {
                    removeFutures.add(t);
                }
            }
            synchronized (this) {
                this.threadAnnotations.keySet().removeAll(removeFutures);
                //this.futureAnnotations.keySet().removeAll(removeFutures);
                this.threadAnnotations.putAll(threadAnnotations);
                /*for (JPDAThread t : futureAnnotations.keySet()) {
                    futureAnnotations.get(t).setAnnotation(threadAnnotations.get(t));
                }*/
                //System.err.println("TASK: have annotations: "+threadAnnotations);
            }
        }
        
        private final class FutureAnnotation {
            
            private JPDAThread thread;
            private Object annotation;
            
            public FutureAnnotation(JPDAThread thread) {
                this.thread = thread;
            }
            
            public JPDAThread getThread() {
                return thread;
            }
            
            public void setAnnotation(Object annotation) {
                this.annotation = annotation;
            }
            
            public Object getAnnotation() {
                return annotation;
            }

            @Override
            public String toString() {
                return "Future annotation ("+annotation+") for "+thread;
            }
            
        }

    }
}
