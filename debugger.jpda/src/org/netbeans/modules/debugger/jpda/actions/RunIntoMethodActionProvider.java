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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ExpressionPool.Expression;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.ErrorManager;

import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Entlicher
 */
public class RunIntoMethodActionProvider extends ActionsProviderSupport 
                                         implements PropertyChangeListener,
                                                    ActionsManagerListener {

    private JPDADebuggerImpl debugger;
    private Session session;
    private ActionsManager lastActionsManager;
    private SourcePath sourcePath;
    
    public RunIntoMethodActionProvider(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        session = (Session) lookupProvider.lookupFirst 
                (null, Session.class);
        sourcePath = (SourcePath) lookupProvider.lookupFirst 
                (null, SourcePath.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
    }
    
    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
    
    private ActionsManager getActionsManager () {
        ActionsManager current = getCurrentActionsManager();
        if (current != lastActionsManager) {
            if (lastActionsManager != null) {
                lastActionsManager.removeActionsManagerListener(
                        ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            }
            current.addActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = current;
        }
        return current;
    }

    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_RUN_INTO_METHOD,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == debugger.STATE_STOPPED) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
        );
        if (debugger.getState () == debugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_INTO_METHOD);
    }
     
    public void doAction (Object action) {
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final String[] classNamePtr = new String[1];
        final int[] linePtr = new int[1];
        final int[] offsetPtr = new int[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    methodPtr[0] = EditorContextBridge.getContext().getSelectedMethodName ();
                    if (methodPtr[0].length() < 1) return ;
                    linePtr[0] = EditorContextBridge.getContext().getCurrentLineNumber();
                    offsetPtr[0] = EditorContextBridge.getCurrentOffset();
                    urlPtr[0] = EditorContextBridge.getContext().getCurrentURL();
                    classNamePtr[0] = EditorContextBridge.getContext().getCurrentClassName();
                }
            });
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ex.getTargetException());
            return ;
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
            return ;
        }
        final String method = methodPtr[0];
        if (method.length () < 1) {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                    "MSG_Put_cursor_on_some_method_call")
            );
            DialogDisplayer.getDefault ().notify (descriptor);
            return;
        }
        final int methodLine = linePtr[0];
        final int methodOffset = offsetPtr[0];
        final String url = urlPtr[0];
        String className = classNamePtr[0];
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return ;
        List<ReferenceType> classes = vm.classesByName(className);
        if (!classes.isEmpty()) {
            doAction(url, classes.get(0), methodLine, methodOffset, method);
        } else {
            final ClassLoadUnloadBreakpoint cbrkp = ClassLoadUnloadBreakpoint.create(className, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            cbrkp.setHidden(true);
            cbrkp.setSuspend(ClassLoadUnloadBreakpoint.SUSPEND_NONE);
            cbrkp.addJPDABreakpointListener(new JPDABreakpointListener() {

                public void breakpointReached(JPDABreakpointEvent event) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(cbrkp);
                    doAction(url, event.getReferenceType(), methodLine, methodOffset, method);
                }
            });
            DebuggerManager.getDebuggerManager().addBreakpoint(cbrkp);
            resume();
        }
    }
    
    private void resume() {
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            debugger.getCurrentThread().resume();
            //((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            //((JPDADebuggerImpl) debugger).resume();
            session.getEngineForLanguage ("Java").getActionsManager ().doAction (
                ActionsManager.ACTION_CONTINUE
            );
        }
    }
    
    private void doAction(String url, final ReferenceType clazz, int methodLine,
                          int methodOffset, final String methodName) {
        List<Location> locations = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locations = clazz.locationsOfLine(methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (AbsentInformationException aiex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
        }
        Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                fine("doAction("+url+", "+clazz+", "+methodLine+", "+methodName+") locations = "+locations);
        if (locations.isEmpty()) {
            String message = NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                                 "MSG_RunIntoMeth_absentInfo",
                                                 clazz.name());
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descriptor);
            return;
        }
        Expression expr = debugger.getExpressionPool().getExpressionAt(locations.get(0), url);
        Location bpLocation = null;
        if (expr != null) {
            Operation[] ops = expr.getOperations();
            for (int i = 0; i < ops.length; i++) {
                Operation op = ops[i];
                if (op.getMethodStartPosition().getOffset() <= methodOffset &&
                    methodOffset <= op.getMethodEndPosition().getOffset()) {
                    
                    bpLocation = expr.getLocations()[i];
                    break;
                }
            }
        }
        if (bpLocation == null) {
            bpLocation = locations.get(0);
        }
        final VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return ;
        final int line = bpLocation.lineNumber("Java");
        CallStackFrameImpl csf = (CallStackFrameImpl) debugger.getCurrentCallStackFrame();
        if (csf != null && csf.getStackFrame().location().equals(bpLocation)) {
            // We're on the line from which the method is called
            traceLineForMethod(methodName, line);
        } else {
            // Submit the breakpoint to get to the point from which the method is called
            final BreakpointRequest brReq = vm.eventRequestManager().createBreakpointRequest(bpLocation);
            debugger.getOperator().register(brReq, new Executor() {

                public boolean exec(Event event) {
                    Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("Calling location reached, tracing for "+methodName+"()");
                    vm.eventRequestManager().deleteEventRequest(brReq);
                    debugger.getOperator().unregister(brReq);
                    traceLineForMethod(methodName, line);
                    return true;
                }
                
                public void removed(EventRequest eventRequest) {}
            });
            brReq.setSuspendPolicy(debugger.getSuspend());
            brReq.enable();
        }
        resume();
    }
    
    private void traceLineForMethod(final String method, final int methodLine) {
        final int depth = debugger.getCurrentThread().getStackDepth();
        final JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
        step.setHidden(true);
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Logger.getLogger(RunIntoMethodActionProvider.class.getName()).isLoggable(Level.FINE)) {
                    Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("traceLineForMethod("+method+") step is at "+debugger.getCurrentThread().getClassName()+":"+debugger.getCurrentThread().getMethodName());
                }
                //System.err.println("RunIntoMethodActionProvider: Step fired, at "+
                //                   debugger.getCurrentThread().getMethodName()+"()");
                JPDAThread t = debugger.getCurrentThread();
                int currentDepth = t.getStackDepth();
                Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("  depth = "+currentDepth+", target = "+depth);
                if (currentDepth == depth) { // We're in the outer expression
                    try {
                        if (t.getCallStack()[0].getLineNumber("Java") != methodLine) {
                            // We've missed the method :-(
                            step.setHidden(false);
                        } else {
                            step.setDepth(JPDAStep.STEP_INTO);
                            step.addStep(debugger.getCurrentThread());
                        }
                    } catch (AbsentInformationException aiex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
                        // We're somewhere strange...
                        step.setHidden(false);
                    }
                } else {
                    if (t.getMethodName().equals(method)) {
                        // We've found it :-)
                        step.setHidden(false);
                    } else if (t.getMethodName().equals("<init>") && (t.getClassName().endsWith("."+method) || t.getClassName().equals(method))) {
                        // The method can be a constructor
                        step.setHidden(false);
                    } else {
                        step.setDepth(JPDAStep.STEP_OUT);
                        step.addStep(debugger.getCurrentThread());
                    }
                }
            }
        });
        step.addStep(debugger.getCurrentThread());
    } 

    public void actionPerformed(Object action) {
        // Is never called
    }

    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_RUN_INTO_METHOD,
                enabled &&
                (debugger.getState () == debugger.STATE_STOPPED) &&
                (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
                (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
            );
        }
    }
}
