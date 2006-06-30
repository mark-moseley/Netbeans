/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.openide.ErrorManager;
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

    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.jdievents") != null;


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
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet (Arrays.asList (new Object[] {
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
        synchronized (getDebuggerImpl ().LOCK) {
            //S ystem.out.println("\nStepAction.doAction");
            try {
                // 1) init info about current state & remove old
                //    requests in the current thread
                ThreadReference tr = ((JPDAThreadImpl) getDebuggerImpl ().
                    getCurrentThread ()).getThreadReference ();
                removeStepRequests (tr);

                // 2) create new step request
                VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
                if (vm == null) return ; // There's nothing to do without the VM.
                stepRequest = vm.eventRequestManager ().createStepRequest (
                        tr,
                        StepRequest.STEP_LINE,
                        getJDIAction (action)
                    );
                stepRequest.addCountFilter (1);
                getDebuggerImpl ().getOperator ().register (stepRequest, StepActionProvider.this);
                stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
                stepRequest.enable ();
                if (verbose)
                    System.out.println("JDI Request: " + stepRequest);

                // 3) resume JVM
                getDebuggerImpl ().resume ();
            } catch (VMDisconnectedException e) {
                ErrorManager.getDefault().notify(ErrorManager.USER,
                    ErrorManager.getDefault().annotate(e,
                        NbBundle.getMessage(StepActionProvider.class,
                            "VMDisconnected")));
            }   
            //S ystem.out.println("/nStepAction.doAction end");
        }
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
        // TODO: fetch current engine from the Event
        synchronized (getDebuggerImpl ().LOCK) {
            //S ystem.out.println("/nStepAction.exec");

            // 1) remove step request
            //removeStepRequests (((LocatableEvent) ev).thread ());
            
            // 2) init info about current state
            LocatableEvent event = (LocatableEvent) ev;
            String className = event.location ().declaringType ().name ();
            ThreadReference tr = event.thread ();
            
            // 3) ignore step events in not current threads
            JPDAThreadImpl ct = (JPDAThreadImpl) getDebuggerImpl ().
                getCurrentThread ();
            if (ct != null &&
                !ct.getThreadReference ().equals (tr)
            ) {
                // step finished in different thread => ignore
                return true; // resume debugging
            }
            
            // 4) stop execution here?
            
            // Synthetic method?
            try {
                if (ct.getThreadReference().frame(0).location().method().isSynthetic()) {
                    //S ystem.out.println("In synthetic method -> STEP OVER/OUT again");
                    
                    removeStepRequests (ct.getThreadReference ());
                    int step = ((StepRequest)ev.request()).depth();
                    VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
                    if (vm == null) {
                        return false; // The session has finished
                    }
                    stepRequest = vm.eventRequestManager ().createStepRequest (
                        ct.getThreadReference (),
                        StepRequest.STEP_LINE,
                        step
                    );
                    stepRequest.addCountFilter(1);
                    getDebuggerImpl ().getOperator ().register (stepRequest, this);
                    stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
                    stepRequest.enable ();
                    return true;
                }
            } catch (IncompatibleThreadStateException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // Not synthetic
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
                    getDebuggerImpl ().setStoppedState (tr);
                    //S ystem.out.println("/nStepAction.exec end - do not resume");
                    return false; // do not resume
                }
            }

            // do not stop here -> start smart stepping!
            if (ssverbose)
                System.out.println("\nSS:  SMART STEPPING START! ********** ");
            boolean stepInto = ((StepRequest) ev.request ()).depth () == 
                            StepRequest.STEP_INTO;
            getStepIntoActionProvider ().doAction 
                (ActionsManager.ACTION_STEP_INTO);
            //S ystem.out.println("/nStepAction.exec end - resume");
            return true; // resume
        }
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
            compoundSmartSteppingListener = (CompoundSmartSteppingListener) lookupProvider.
                lookupFirst (null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }
}
