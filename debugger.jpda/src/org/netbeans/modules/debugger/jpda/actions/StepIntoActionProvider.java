/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 * Implements non visual part of stepping through code in JPDA debugger.
 * It supports standart debugging actions StepInto, Over, Out, RunToCursor, 
 * and Go. And advanced "smart tracing" action.
 *
 * @author  Jan Jancura
 */
public class StepIntoActionProvider extends JPDADebuggerActionProvider 
implements Executor, PropertyChangeListener {
    
    public static final String SS_STEP_OUT = "SS_ACTION_STEPOUT";
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;
    
        
    private StepRequest stepRequest;
    private ThreadReference tr;
    private String position;
    private ContextProvider contextProvider;
    private boolean smartSteppingStepOut;

    public StepIntoActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.contextProvider = contextProvider;
        getSmartSteppingFilterImpl ().addPropertyChangeListener (this);
        SourcePath ec = (SourcePath) contextProvider.
            lookupFirst (null, SourcePath.class);
        ec.addPropertyChangeListener (this);
        Map properties = (Map) contextProvider.lookupFirst (null, Map.class);
        if (properties != null)
            smartSteppingStepOut = properties.containsKey (SS_STEP_OUT);
    }


    // ActionProviderSupport ...................................................
    
    public Set getActions () {
        return new HashSet (Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
        }));
    }
    
    public void doAction (Object action) {
        synchronized (getDebuggerImpl ().LOCK) {
            if (ssverbose)
                System.out.println("\nSS:  STEP INTO !!! *************");
            setStepRequest ();
            try {
                getDebuggerImpl ().resume ();
            } catch (VMDisconnectedException e) {
            }
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        Iterator i = getActions ().iterator ();
        while (i.hasNext ())
            setEnabled (
                i.next (),
                (debuggerState == JPDADebugger.STATE_STOPPED) &&
                (getDebuggerImpl ().getCurrentThread () != null)
            );
    }
    
    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName () == SmartSteppingFilter.PROP_EXCLUSION_PATTERNS) {
            if (ev.getOldValue () != null) {
                // remove some patterns
                if (ssverbose) {
                    System.out.println("\nSS:  exclusion patterns removed");
                }
                removeStepRequests ();
            } else {
                if (ssverbose) {
                    if (stepRequest == null)
                        System.out.println("SS:  exclusion patterns has been added");
                    else
                        System.out.println("\nSS:    add exclusion patterns:");
                }
                addPatternsToRequest ((String[]) 
                    ((Set) ev.getNewValue ()).toArray (
                        new String [((Set) ev.getNewValue ()).size()]
                    )
                );
            }
        } else
        if (ev.getPropertyName () == SourcePathProvider.PROP_SOURCE_ROOTS) {
            if (ssverbose)
                System.out.println("\nSS:  source roots changed");
            removeStepRequests ();
        } else
        super.propertyChange (ev);
    }
    
    
    // Executor ................................................................
    
    /**
     * Executes all step actions and smart stepping. 
     *
     * Should be called from Operator only.
     */
    public boolean exec (Event event) {
        stepRequest.disable ();
        LocatableEvent le = (LocatableEvent) event;
        String np = le.location ().declaringType ().name () + ":" + 
                    le.location ().lineNumber (null);

        ThreadReference tr = le.thread ();
        JPDAThread t = getDebuggerImpl ().getThread (tr);
        boolean stop = (!np.equals (position)) && 
                       getCompoundSmartSteppingListener ().stopHere 
                           (contextProvider, t, getSmartSteppingFilterImpl ());
        if (stop) {
            removeStepRequests ();
            getDebuggerImpl ().setStoppedState (tr);
        } else {
            if (ssverbose)
                System.out.println("SS:  => do next step!");
            if (smartSteppingStepOut)
                getStepActionProvider().doAction(ActionsManager.ACTION_STEP_OUT);
            else
            if (stepRequest != null)
                stepRequest.enable ();
            else
                setStepRequest ();
        }

        if (ssverbose)
            if (stop) {
                System.out.println("SS  FINISH IN CLASS " +  
                    t.getClassName () + " ********\n"
                );
            }
        return !stop;
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
    
    void removeStepRequests () {
        super.removeStepRequests ();
        stepRequest = null;
        if (ssverbose)
            System.out.println("SS:    remove all patterns");
    }
    
    private void setStepRequest () {
        removeStepRequests ();
        ThreadReference tr = ((JPDAThreadImpl) getDebuggerImpl ().
            getCurrentThread ()).getThreadReference ();
        stepRequest = getDebuggerImpl ().getVirtualMachine ().
        eventRequestManager ().createStepRequest (
            tr,
            StepRequest.STEP_LINE,
            StepRequest.STEP_INTO
        );
        getDebuggerImpl ().getOperator ().register (stepRequest, this);
        stepRequest.setSuspendPolicy (getDebuggerImpl ().getSuspend ());
        
        if (ssverbose)
            System.out.println("SS:    set patterns:");
        addPatternsToRequest (
            getSmartSteppingFilterImpl ().getExclusionPatterns ()
        );
        stepRequest.enable ();
    }

    private SmartSteppingFilterImpl smartSteppingFilterImpl;
    
    private SmartSteppingFilterImpl getSmartSteppingFilterImpl () {
        if (smartSteppingFilterImpl == null)
            smartSteppingFilterImpl = (SmartSteppingFilterImpl) contextProvider.
                lookupFirst (null, SmartSteppingFilter.class);
        return smartSteppingFilterImpl;
    }

    private CompoundSmartSteppingListener compoundSmartSteppingListener;
    
    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = (CompoundSmartSteppingListener) 
                contextProvider.lookupFirst (null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }

    private void addPatternsToRequest (String[] patterns) {
        if (stepRequest == null) return;
        int i, k = patterns.length;
        for (i = 0; i < k; i++) {
            stepRequest.addClassExclusionFilter (patterns [i]);
            if (ssverbose)
                System.out.println("SS:      " + patterns [i]);
        }
    }
}
