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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
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
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Extracted from StepIntoActionProvider and StepIntoNextMethodActionProvider
 *
 * @author Martin Entlicher
 */
public class StepIntoNextMethod implements Executor, PropertyChangeListener {

    private static final Logger smartLogger = Logger.getLogger("org.netbeans.modules.debugger.jpda.smartstepping"); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.jdievents"); // NOI18N

    private StepRequest stepRequest;
    private String position;
    private int depth;
    private JPDADebuggerImpl debugger;
    private ContextProvider contextProvider;
    private boolean smartSteppingStepOut;

    public StepIntoNextMethod(ContextProvider contextProvider) {
        this.debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        this.contextProvider = contextProvider;
        getSmartSteppingFilterImpl ().addPropertyChangeListener (this);
        SourcePath ec = contextProvider.lookupFirst(null, SourcePath.class);
        ec.addPropertyChangeListener (this);
        Map properties = contextProvider.lookupFirst(null, Map.class);
        if (properties != null)
            smartSteppingStepOut = properties.containsKey (StepIntoActionProvider.SS_STEP_OUT);
    }

    private final JPDADebuggerImpl getDebuggerImpl() {
        return debugger;
    }

    public void runAction() {
        runAction(true);
    }

    public void runAction(boolean doResume) {
        smartLogger.finer("STEP INTO NEXT METHOD.");
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) {
            // Can not step without current thread.
            smartLogger.finer("Can not step into next method! No current thread!");
            return ;
        }
        Lock lock;
        if (getDebuggerImpl().getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            lock = ((JPDAThreadImpl) t).accessLock.writeLock();
        } else {
            lock = getDebuggerImpl().accessLock.writeLock();
        }
        lock.lock();
        try {
            if (!t.isSuspended()) {
                // Can not step when it's not suspended.
                if (smartLogger.isLoggable(Level.FINER)) {
                    smartLogger.finer("Can not step into next method! Thread "+t+" not suspended!");
                }
                return ;
            }
            JPDAThread resumeThread = setStepRequest (StepRequest.STEP_INTO);
            position = t.getClassName () + '.' +
                       t.getMethodName () + ':' +
                       t.getLineNumber (null);
            depth = t.getStackDepth();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("JDI Request (action step into next method): " + stepRequest);
            }
            if (stepRequest == null) return ;
            ((JPDAThreadImpl) t).setInStep(true, stepRequest);
            if (doResume) {
                if (resumeThread == null) {
                    getDebuggerImpl ().resume ();
                } else {
                    //resumeThread.resume();
                    //stepWatch = new SingleThreadedStepWatch(getDebuggerImpl(), stepRequest);
                    getDebuggerImpl().resumeCurrentThread();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName () == SmartSteppingFilter.PROP_EXCLUSION_PATTERNS) {
            if (ev.getOldValue () != null) {
                // remove some patterns
                smartLogger.finer("Exclusion patterns removed. Removing step requests.");
                JPDAThreadImpl currentThread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
                if (currentThread != null) {
                    ThreadReference tr = currentThread.getThreadReference ();
                    removeStepRequests (tr);
                }
            } else {
                if (smartLogger.isLoggable(Level.FINER)) {
                    if (stepRequest == null)
                        smartLogger.finer("Exclusion patterns has been added");
                    else
                        smartLogger.finer("Add exclusion patterns: "+ev.getNewValue());
                }
                try {
                    addPatternsToRequest((String[]) ((Set<String>) ev.getNewValue ()).toArray (
                        new String [((Set) ev.getNewValue ()).size()]
                    ));
                } catch (InternalExceptionWrapper ex) {
                    return ;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return ;
                }
            }
        } else
        if (ev.getPropertyName () == SourcePathProvider.PROP_SOURCE_ROOTS) {
            smartLogger.finer("Source roots changed");
            JPDAThreadImpl jtr = (JPDAThreadImpl) getDebuggerImpl ().
                getCurrentThread ();
            if (jtr != null) {
                ThreadReference tr = jtr.getThreadReference ();
                removeStepRequests (tr);
            }
        }
    }


    // Executor ................................................................

    /**
     * Executes all step actions and smart stepping.
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event event) {
        ThreadReference tr;
        JPDAThreadImpl st;
        try {
            StepRequest sr = (StepRequest) EventWrapper.request(event);
            tr = StepRequestWrapper.thread(sr);
            st = getDebuggerImpl().getThread(tr);
            st.setInStep(false, null);
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        /*if (stepWatch != null) {
            stepWatch.done();
            stepWatch = null;
        }*/
        st.accessLock.readLock().lock();
        try {
            if (stepRequest != null) {
                try {
                    EventRequestWrapper.disable (stepRequest);
                } catch (InternalExceptionWrapper ex) {
                    return false;
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return false;
                }
            }

            try {
                if (TypeComponentWrapper.isSynthetic(
                        LocationWrapper.method(StackFrameWrapper.location(
                        ThreadReferenceWrapper.frame(tr, 0))))) {
                    //S ystem.out.println("In synthetic method -> STEP INTO again");
                    setStepRequest (StepRequest.STEP_INTO);
                    return true;
                }
            } catch (IncompatibleThreadStateException e) {
                //ErrorManager.getDefault().notify(e);
                // This may happen while debugging a free form project
            } catch (IllegalThreadStateExceptionWrapper e) {
            } catch (InvalidStackFrameExceptionWrapper e) {
            } catch (InternalExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
                return true;
            }

            JPDAThread t = getDebuggerImpl ().getThread (tr);
            boolean stop = getCompoundSmartSteppingListener ().stopHere
                               (contextProvider, t, getSmartSteppingFilterImpl ());
            if (stop) {
                String stopPosition = t.getClassName () + '.' +
                                      t.getMethodName () + ':' +
                                      t.getLineNumber (null);
                int stopDepth = t.getStackDepth();
                if (position.equals(stopPosition) && depth == stopDepth) {
                    // We are where we started!
                    stop = false;
                    setStepRequest (StepRequest.STEP_INTO);
                    return true;//resumeThread == null;
                }
            }
            if (stop) {
                removeStepRequests (tr);
            } else {
                smartLogger.finer(" => do next step.");
                if (smartSteppingStepOut) {
                    setStepRequest (StepRequest.STEP_OUT);
                } else if (stepRequest != null) {
                    try {
                        EventRequestWrapper.enable (stepRequest);
                    } catch (IllegalThreadStateException itsex) {
                        try {
                            // the thread named in the request has died.
                            getDebuggerImpl().getOperator().unregister(stepRequest);
                        } catch (InternalExceptionWrapper ex) {
                            return true;
                        } catch (VMDisconnectedExceptionWrapper ex) {
                            return true;
                        }
                        stepRequest = null;
                        return true;
                    } catch (VMDisconnectedExceptionWrapper e) {
                        return true;
                    } catch (InternalExceptionWrapper e) {
                        return true;
                    }
                } else {
                    setStepRequest (StepRequest.STEP_INTO);
                }
            }

            if (stop) {
                if (smartLogger.isLoggable(Level.FINER))
                    smartLogger.finer("FINISH IN CLASS " +
                        t.getClassName () + " ********"
                    );
                try {
                    StepActionProvider.setLastOperation(tr, debugger, null);
                } catch (VMDisconnectedExceptionWrapper e) {
                    return true;
                }
            }
            return !stop;
        } finally {
            st.accessLock.readLock().unlock();
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
    }


    private StepActionProvider stepActionProvider;

    private StepActionProvider getStepActionProvider () {
        if (stepActionProvider == null) {
            List l = contextProvider.lookup (null, ActionsProvider.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (l.get (i) instanceof StepActionProvider)
                    stepActionProvider = (StepActionProvider) l.get (i);
        }
        return stepActionProvider;
    }

    // other methods ...........................................................

    private void removeStepRequests (ThreadReference tr) {
        JPDADebuggerActionProvider.removeStepRequests (getDebuggerImpl(), tr);
        stepRequest = null;
        smartLogger.finer("removing all patterns, all step requests.");
    }

    private JPDAThreadImpl setStepRequest (int step) {
        JPDAThreadImpl thread = (JPDAThreadImpl) getDebuggerImpl().getCurrentThread();
        ThreadReference tr = thread.getThreadReference ();
        removeStepRequests (tr);
        VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
        if (vm == null) return null;
        int suspendPolicy;
        try {
            stepRequest = EventRequestManagerWrapper.createStepRequest(
                    VirtualMachineWrapper.eventRequestManager(vm),
                    tr,
                    StepRequest.STEP_LINE,
                    step);
            getDebuggerImpl ().getOperator ().register (stepRequest, this);
            suspendPolicy = getDebuggerImpl().getSuspend();
            EventRequestWrapper.setSuspendPolicy (stepRequest, suspendPolicy);
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }

        if (smartLogger.isLoggable(Level.FINER)) {
            smartLogger.finer("Set step request("+step+") and patterns: ");
        }
        try {
            addPatternsToRequest (
                getSmartSteppingFilterImpl ().getExclusionPatterns ()
            );
            EventRequestWrapper.enable (stepRequest);
        } catch (IllegalThreadStateException itsex) {
            try {
                // the thread named in the request has died.
                getDebuggerImpl().getOperator().unregister(stepRequest);
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return null;
            }
            stepRequest = null;
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
        if (suspendPolicy == JPDADebugger.SUSPEND_EVENT_THREAD) {
            return thread;
        } else {
            return null;
        }
    }

    private SmartSteppingFilter smartSteppingFilter;

    private SmartSteppingFilter getSmartSteppingFilterImpl () {
        if (smartSteppingFilter == null)
            smartSteppingFilter = contextProvider.lookupFirst(null, SmartSteppingFilter.class);
        return smartSteppingFilter;
    }

    private CompoundSmartSteppingListener compoundSmartSteppingListener;

    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = contextProvider.lookupFirst(null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }

    private void addPatternsToRequest (String[] patterns) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        if (stepRequest == null) return;
        int i, k = patterns.length;
        for (i = 0; i < k; i++) {
            StepRequestWrapper.addClassExclusionFilter(stepRequest, patterns [i]);
            smartLogger.finer("   add pattern: "+patterns[i]);
        }
    }

}
