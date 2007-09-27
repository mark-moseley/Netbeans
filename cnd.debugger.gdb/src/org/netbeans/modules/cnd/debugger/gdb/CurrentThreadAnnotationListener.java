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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.*;

import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager}
 * property and annotates current line and call stack in NetBeans editor.
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CurrentThreadAnnotationListener extends DebuggerManagerAdapter {

    // annotation for current line
    private transient Object                currentPC;
    private transient Object                currentPCLock = new Object();
    private transient boolean               currentPCSet = false;
    //private GdbThread                      currentThread;
    private GdbDebugger                     currentDebugger;

    public CurrentThreadAnnotationListener() {
	updateCurrentDebugger(); // ensure currentDebugger gets set
	assert (!(currentDebugger != null && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
    }

    public String[] getProperties() {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens GdbDebuggerEngineImpl and DebuggerManager.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == DebuggerManager.PROP_CURRENT_ENGINE) {
            updateCurrentDebugger();
            annotate();
        } else if (e.getPropertyName() == GdbDebugger.PROP_CURRENT_THREAD) {
            annotate();
        } else if (e.getPropertyName() == GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME) {
            annotate();
        } else if (e.getPropertyName() == GdbDebugger.PROP_STATE) {
            annotate();
        }
    }


    // helper methods ..........................................................

    private void updateCurrentDebugger() {
        GdbDebugger newDebugger = getCurrentDebugger();
        if (currentDebugger == newDebugger) {
            return;
        }
        if (currentDebugger != null) {
            currentDebugger.removePropertyChangeListener(this);
        }
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener(this);
        }
        currentDebugger = newDebugger;
    }
    
    private static GdbDebugger getCurrentDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate() {
        if (currentDebugger == null) {
            return;
        }
        
        // 1) no current thread => remove annotations
        if (currentDebugger.getState() != GdbDebugger.STATE_STOPPED) {
            synchronized (currentPCLock) {
                currentPCSet = false; // The annotation is goint to be removed
            }
            removeAnnotations();
            return;
        }
        
        // 2) get call stack & Line
        ArrayList stack = currentDebugger.getCallStack();
        final CallStackFrame csf = currentDebugger.getCurrentCallStackFrame();
        final DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        final Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        final String language = currentSession == null ? null : currentSession.getCurrentLanguage();

        // 3) annotate current line & stack
        synchronized (currentPCLock) {
            currentPCSet = true; // The annotation is going to be set
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // show current line
                synchronized (currentPCLock) {
                    if (currentPC != null) {
                        EditorContextBridge.removeAnnotation(currentPC);
                    }
                    if (csf != null) {
                        EditorContextBridge.showSource(csf);
                    }
                }
            }
        });
        annotateCallStack(stack);
    }


    // do not need synchronization, called in a 1-way RP
    private HashMap               stackAnnotations = new HashMap();
    
    private RequestProcessor rp = new RequestProcessor("Debugger Thread Annotation Refresher"); // NOI18N

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task taskRemove;
    private RequestProcessor.Task taskAnnotate;
    private List stackToAnnotate;

    private void removeAnnotations() {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create(new Runnable() {
                    public void run() {
                        synchronized (currentPCLock) {
                            if (currentPCSet) {
                                // Keep the set PC
                                return ;
                            }
                            if (currentPC != null) {
                                EditorContextBridge.removeAnnotation(currentPC);
                            }
                            currentPC = null;
                        }
                        Iterator i = stackAnnotations.values().iterator();
                        while (i.hasNext()) {
                            EditorContextBridge.removeAnnotation(i.next());
                        }
                        stackAnnotations.clear();
                    }
                });
            }
        }
        taskRemove.schedule(500);
    }

    private void annotateCallStack(List stack) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            this.stackToAnnotate = stack;
            //this.sourcePathToAnnotate = sourcePath;
            if (taskAnnotate == null) {
                taskAnnotate = rp.post(new Runnable() {
                    public void run() {
                        List stack;
                        synchronized (rp) {
                            if (stackToAnnotate == null) {
                                return ; // Nothing to do
                            }
                            stack = new ArrayList(stackToAnnotate);
                            stackToAnnotate = null;
                        }
                        
                        // Remove old annotations
                        if (currentPC != null) {
                            EditorContextBridge.removeAnnotation(currentPC);
                        }
                        currentPC = null;
                        Iterator iter = stackAnnotations.values().iterator();
                        while (iter.hasNext()) {
                            EditorContextBridge.removeAnnotation(iter.next());
                        }
                        stackAnnotations.clear();
                        
                        // Add new annotations
                        HashMap newAnnotations = new HashMap();
                        String annotationType = EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
                        int i, k = stack.size();
                        for (i = 0; i < k; i++) {
                            // 1) check Line
                            String language = null;
                            CallStackFrame csf = (CallStackFrame) stack.get(i);
                            int lineNumber = csf.getLineNumber();
                            String line = Integer.toString(lineNumber);

                            // 2) line already annotated?
                            if (newAnnotations.containsKey(line))
                                continue;

                            // 3) annotate line
                            Object da = EditorContextBridge.annotate(csf, annotationType);

                            // 4) add new line to hashMap
                            if (da != null) {
                                newAnnotations.put(line, da);
                            }
                            annotationType = EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE;
                        }
                        stackAnnotations = newAnnotations;
                    }
                });
            }
        }
        taskAnnotate.schedule(50);
    }
}

