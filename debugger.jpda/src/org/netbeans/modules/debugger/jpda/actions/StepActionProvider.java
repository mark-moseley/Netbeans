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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ExpressionPool;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl.MethodExitBreakpointListener;
//import org.netbeans.modules.debugger.jpda.JPDAStepImpl.SingleThreadedStepWatch;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.breakpoints.MethodBreakpointImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standart debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
public class StepActionProvider extends JPDADebuggerActionProvider 
implements Executor {
    
    private StepRequest             stepRequest;
    private ContextProvider         lookupProvider;
    private MethodExitBreakpointListener lastMethodExitBreakpointListener;
    //private SingleThreadedStepWatch stepWatch;
    private boolean smartSteppingStepOut;
    private Properties p;

    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N


    private static int getJDIAction (Object action) {
        if (action == ActionsManager.ACTION_STEP_OUT) 
            return StepRequest.STEP_OUT;
        if (action == ActionsManager.ACTION_STEP_OVER) 
            return StepRequest.STEP_OVER;
        throw new IllegalArgumentException ();
    }
    
    
    public StepActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        setProviderToDisableOnLazyAction(this);
        Map properties = lookupProvider.lookupFirst(null, Map.class);
        if (properties != null) {
            smartSteppingStepOut = properties.containsKey (StepIntoActionProvider.SS_STEP_OUT);
        }
        p = Properties.getDefault().getProperties("debugger.options.JPDA"); // NOI18N
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet<Object>(Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER
        }));
    }
    
    public void doAction (final Object action) {
        runAction(action);
    }
    
    public void postAction(final Object action,
                           final Runnable actionPerformedNotifier) {
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    runAction(action);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction(final Object action) {
        runAction(action, true);
    }

    private void runAction(final Object action, boolean doResume) {
        //S ystem.out.println("\nStepAction.doAction");
        int suspendPolicy = getDebuggerImpl().getSuspend();
        JPDAThreadImpl resumeThread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
        Lock lock;
        if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
            lock = resumeThread.accessLock.writeLock();
        } else {
            lock = getDebuggerImpl().accessLock.writeLock();
        }
        lock.lock();
        try {
            // 1) init info about current state & remove old
            //    requests in the current thread
            // We have to assure that the thread is suspended so that we
            // do not randomly resume threads
            while (!resumeThread.isSuspended()) {
                // The thread is not suspended, release the lock so that others
                // can process what they want with the resumed thread...
                lock.unlock();
                Thread.yield();
                try {
                    Thread.sleep(100); // Wait for a moment
                } catch (InterruptedException iex) {}
                lock.lock(); // Take the lock again to repeat the test
                if (!resumeThread.isSuspended() && !resumeThread.isInStep() && !resumeThread.isMethodInvoking()) {
                    // Explicitely suspend the thread if it's not in a step
                    // or in a method invocation:
                    resumeThread.suspend();
                }
            }
            resumeThread.waitUntilMethodInvokeDone();
            ThreadReference tr = resumeThread.getThreadReference ();
            removeStepRequests (tr);

            // 2) create new step request
            VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
            if (vm == null) return ; // There's nothing to do without the VM.
            stepRequest = EventRequestManagerWrapper.createStepRequest (
                    VirtualMachineWrapper.eventRequestManager (vm),
                    tr,
                    StepRequest.STEP_LINE,
                    getJDIAction (action)
                );
            EventRequestWrapper.addCountFilter (stepRequest, 1);
            getDebuggerImpl ().getOperator ().register (stepRequest, StepActionProvider.this);
            EventRequestWrapper.setSuspendPolicy (stepRequest, suspendPolicy);
            try {
                EventRequestWrapper.enable (stepRequest);
            } catch (IllegalThreadStateException itsex) {
                // the thread named in the request has died.
                // Or suspend count > 1 !
                //itsex.printStackTrace();
                //System.err.println("Thread: "+tr.name()+", suspended = "+tr.isSuspended()+", suspend count = "+tr.suspendCount()+", status = "+tr.status());
                if (logger.isLoggable(Level.WARNING)) {
                    try {
                        logger.warning(itsex.getLocalizedMessage()+"\nThread: "+ThreadReferenceWrapper.name(tr)+", suspended = "+ThreadReferenceWrapper.isSuspended(tr)+", status = "+ThreadReferenceWrapper.status(tr));
                    } catch (Exception e) {
                        logger.warning(e.getLocalizedMessage());
                    }
                }
                getDebuggerImpl ().getOperator ().unregister(stepRequest);
                return ;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                // Thread was collected...
                getDebuggerImpl ().getOperator ().unregister(stepRequest);
                return ;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("JDI Request (action "+action+"): " + stepRequest);
            }
            if (action == ActionsManager.ACTION_STEP_OUT) {
                addMethodExitBP(tr, resumeThread);
            }
            resumeThread.disableMethodInvokeUntilResumed();
            // 3) resume JVM
            resumeThread.setInStep(true, stepRequest);
            if (doResume) {
                if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
                    //stepWatch = new SingleThreadedStepWatch(getDebuggerImpl(), stepRequest);
                    getDebuggerImpl().resumeCurrentThread();
                    //resumeThread.resume();
                } else {
                    getDebuggerImpl ().resume ();
                }
            }
        } catch (VMDisconnectedExceptionWrapper e) {
            // Debugger is disconnected => the action will be ignored.
        } catch (InternalExceptionWrapper e) {
            // Debugger is damaged => the action will be ignored.
        } catch (InvalidStackFrameExceptionWrapper e) {
            Exceptions.printStackTrace(e);
        } catch (ObjectCollectedExceptionWrapper e) {
            // Thread was collected - ignore the step
        } finally {
            lock.unlock();
        }
        //S ystem.out.println("/nStepAction.doAction end");
    }
    
    private void addMethodExitBP(ThreadReference tr, JPDAThread jtr) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper {
        if (!MethodBreakpointImpl.canGetMethodReturnValues(MirrorWrapper.virtualMachine(tr))) {
            return ;
        }
        Location loc;
        try {
            loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
        } catch (IncompatibleThreadStateException ex) {
            logger.fine("Incompatible Thread State: "+ex.getLocalizedMessage());
            return ;
        } catch (IllegalThreadStateExceptionWrapper ex) {
            return ;
        }
        String classType = ReferenceTypeWrapper.name(LocationWrapper.declaringType(loc));
        String methodName = TypeComponentWrapper.name(LocationWrapper.method(loc));
        MethodBreakpoint mb = MethodBreakpoint.create(classType, methodName);
        //mb.setMethodName(methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        mb.setHidden(true);
        mb.setSuspend(JPDABreakpoint.SUSPEND_NONE);
        mb.setThreadFilters(getDebuggerImpl(), new JPDAThread[] { jtr });
        lastMethodExitBreakpointListener = new MethodExitBreakpointListener(mb);
        mb.addJPDABreakpointListener(lastMethodExitBreakpointListener);
        // TODO: mb.setSession(debugger);
        try {
            java.lang.reflect.Method setSessionMethod = JPDABreakpoint.class.getDeclaredMethod("setSession", JPDADebugger.class);
            setSessionMethod.setAccessible(true);
            setSessionMethod.invoke(mb, debugger);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
    }
    
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == getDebuggerImpl ().STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    // Executor ................................................................
    
    /**
     * Executes all step actions and smart stepping. 
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event ev) {
        try {
        // TODO: fetch current engine from the Event
        // 1) init info about current state
        StepRequest sr = (StepRequest) EventWrapper.request(ev);
        JPDAThreadImpl st = getDebuggerImpl().getThread(StepRequestWrapper.thread(sr));
        st.setInStep(false, null);
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
        LocatableEvent event = (LocatableEvent) ev;
        String className = ReferenceTypeWrapper.name(LocationWrapper.declaringType(LocatableWrapper.location(event)));
        ThreadReference tr = LocatableEventWrapper.thread(event);
        setLastOperation(tr);
        removeStepRequests (tr);
        Lock lock;
        if (getDebuggerImpl().getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            lock = st.accessLock.writeLock();
        } else {
            lock = getDebuggerImpl().accessLock.writeLock();
        }
        lock.lock();
        try {
            //S ystem.out.println("/nStepAction.exec");

            int suspendPolicy = getDebuggerImpl().getSuspend();
            
            // Synthetic method?
            try {
                if (TypeComponentWrapper.isSynthetic(LocationWrapper.method(StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0))))) {
                    //S ystem.out.println("In synthetic method -> STEP OVER/OUT again");
                    
                    int step = StepRequestWrapper.depth((StepRequest) EventWrapper.request(ev));
                    VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
                    if (vm == null) {
                        return false; // The session has finished
                    }
                    stepRequest = EventRequestManagerWrapper.createStepRequest(VirtualMachineWrapper.eventRequestManager(vm),
                        tr,
                        StepRequest.STEP_LINE,
                        step
                    );
                    EventRequestWrapper.addCountFilter(stepRequest, 1);
                    getDebuggerImpl ().getOperator ().register (stepRequest, this);
                    EventRequestWrapper.setSuspendPolicy(stepRequest, suspendPolicy);
                    try {
                        EventRequestWrapper.enable(stepRequest);
                    } catch (IllegalThreadStateException itsex) {
                        // the thread named in the request has died.
                        getDebuggerImpl ().getOperator ().unregister(stepRequest);
                        stepRequest = null;
                    }
                    return true;
                }
            } catch (IncompatibleThreadStateException e) {
                Exceptions.printStackTrace(e);
                logger.fine("Incompatible Thread State: " + e.getLocalizedMessage());
            } catch (IllegalThreadStateExceptionWrapper e) {
                return false;
            } catch (InvalidStackFrameExceptionWrapper e) {
                Exceptions.printStackTrace(e);
            }
            
            // Stop execution here?
            boolean fsh = getSmartSteppingFilterImpl ().stopHere (className);
            if (ssverbose)
                System.out.println("SS  SmartSteppingFilter.stopHere (" + 
                    className + ") ? " + fsh
                );
            if (fsh) {
                JPDAThread t = getDebuggerImpl ().getThread (tr);
                if (getCompoundSmartSteppingListener ().stopHere 
                     (lookupProvider, t, getSmartSteppingFilterImpl ())
                ) {
                    // YES!
                    //S ystem.out.println("/nStepAction.exec end - do not resume");
                    return false; // do not resume
                }
            }

            // do not stop here -> start smart stepping!
            if (ssverbose)
                System.out.println("\nSS:  SMART STEPPING START! ********** ");
            boolean useStepFilters = p.getBoolean("UseStepFilters", true);
            boolean stepThrough = useStepFilters && p.getBoolean("StepThroughFilters", false);
            if (!stepThrough || smartSteppingStepOut) {
                // Assure that the action does not resume anything. Resume is done by Operator.
                getStepIntoActionProvider ().runAction(ActionsManager.ACTION_STEP_OUT, false);
            } else {
                // Assure that the action does not resume anything. Resume is done by Operator.
                int origDepth = StepRequestWrapper.depth(sr);
                if (origDepth == StepRequest.STEP_OVER) {
                    runAction(ActionsManager.ACTION_STEP_OVER, false);
                    //getStepIntoActionProvider ().runAction(StepIntoActionProvider.ACTION_SMART_STEP_INTO, false);
                } else if (origDepth == StepRequest.STEP_OUT) {
                    runAction(ActionsManager.ACTION_STEP_OUT, false);
                } else { // if (origDepth == StepRequest.STEP_INTO) {
                    getStepIntoActionProvider ().runAction(StepIntoActionProvider.ACTION_SMART_STEP_INTO, false);
                }
            }
            //S ystem.out.println("/nStepAction.exec end - resume");
            return true; // resume
        } finally {
            lock.unlock();
        }
        } catch (InternalExceptionWrapper e) {
            return false; // Do not resume when something bad happened
        } catch (VMDisconnectedExceptionWrapper e) {
            return false; // Do not resume when disconnected
        } catch (ObjectCollectedExceptionWrapper e) {
            return false; // Do not resume when collected
        }
    }

    public void removed(EventRequest eventRequest) {
        StepRequest sr = (StepRequest) eventRequest;
        try {
            JPDAThreadImpl st = getDebuggerImpl().getThread(StepRequestWrapper.thread(sr));
            st.setInStep(false, null);
        } catch (InternalExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        }
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
        if (lastMethodExitBreakpointListener != null) {
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
    }
    
    private void setLastOperation(ThreadReference tr) throws VMDisconnectedExceptionWrapper {
        Variable returnValue = null;
        if (lastMethodExitBreakpointListener != null) {
            returnValue = lastMethodExitBreakpointListener.getReturnValue();
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
        setLastOperation(tr, getDebuggerImpl(), returnValue);
    }

    public static void setLastOperation(ThreadReference tr, JPDADebuggerImpl debugger, Variable returnValue) throws VMDisconnectedExceptionWrapper {
        Location loc;
        try {
            loc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
        } catch (IncompatibleThreadStateException itsex) {
            Exceptions.printStackTrace(itsex);
            logger.fine("Incompatible Thread State: "+itsex.getLocalizedMessage());
            return ;
        } catch (IllegalThreadStateExceptionWrapper itsex) {
            return ;
        } catch (InternalExceptionWrapper iex) {
            return ;
        } catch (InvalidStackFrameExceptionWrapper iex) {
            return ;
        } catch (ObjectCollectedExceptionWrapper iex) {
            return ;
        }
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        String language = currentSession == null ? null : currentSession.getCurrentLanguage();
        SourcePath sourcePath = debugger.getEngineContext();
        String url;
        try {
            url = sourcePath.getURL(loc, language);
        } catch (InternalExceptionWrapper iex) {
            return ;
        }
        ExpressionPool exprPool = debugger.getExpressionPool();
        ExpressionPool.Expression expr = exprPool.getExpressionAt(loc, url);
        if (expr == null) {
            return ;
        }
        Operation[] ops = expr.getOperations();
        // code index right after the method call (step out)
        int codeIndex;
        byte[] bytecodes;
        try {
            codeIndex = (int) LocationWrapper.codeIndex(loc);
            bytecodes = MethodWrapper.bytecodes(LocationWrapper.method(loc));
        } catch (InternalExceptionWrapper iex) {
            return ;
        }
        if (codeIndex >= 5 && (bytecodes[codeIndex - 5] & 0xFF) == 185) { // invokeinterface
            codeIndex -= 5;
        } else {
            codeIndex -= 3; // invokevirtual, invokespecial, invokestatic
        }
        int opIndex = expr.findNextOperationIndex(codeIndex - 1);
        Operation lastOperation;
        if (opIndex >= 0 && ops[opIndex].getBytecodeIndex() == codeIndex) {
            lastOperation = ops[opIndex];
        } else {
            return ;
        }
        lastOperation.setReturnValue(returnValue);
        JPDAThreadImpl jtr = debugger.getThread(tr);
        jtr.addLastOperation(lastOperation);
        jtr.setCurrentOperation(lastOperation);
    }
    
    private StepIntoActionProvider stepIntoActionProvider;
    
    private StepIntoActionProvider getStepIntoActionProvider () {
        if (stepIntoActionProvider == null) {
            List l = lookupProvider.lookup (null, ActionsProvider.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (l.get (i) instanceof StepIntoActionProvider)
                    stepIntoActionProvider = (StepIntoActionProvider) l.get (i);
        }
        return stepIntoActionProvider;
    }

    private SmartSteppingFilterImpl smartSteppingFilterImpl;
    
    private SmartSteppingFilterImpl getSmartSteppingFilterImpl () {
        if (smartSteppingFilterImpl == null)
            smartSteppingFilterImpl = (SmartSteppingFilterImpl) lookupProvider.
                lookupFirst (null, SmartSteppingFilter.class);
        return smartSteppingFilterImpl;
    }

    private CompoundSmartSteppingListener compoundSmartSteppingListener;
    
    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = lookupProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }
}
