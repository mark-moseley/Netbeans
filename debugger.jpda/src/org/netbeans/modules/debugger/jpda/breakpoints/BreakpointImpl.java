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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;
import org.netbeans.modules.debugger.jpda.util.Executor;


/**
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointImpl implements Executor, PropertyChangeListener {

    private JPDADebuggerImpl    debugger;
    private JPDABreakpoint      breakpoint;
    private Expression          compiledCondition;
    private List                requests = new ArrayList ();


    protected BreakpointImpl (JPDABreakpoint p, JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        breakpoint = p;
    }

    final void set () {
        breakpoint.addPropertyChangeListener(this);
        if ( (getVirtualMachine () == null) ||
             (getDebugger ().getState () == JPDADebugger.STATE_DISCONNECTED)
        ) return;
        removeAllEventRequests ();
        if (breakpoint.isEnabled()) {
            setRequests ();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        set();
    }

    protected abstract void setRequests ();
    
    protected final void remove () {
        removeAllEventRequests ();
        breakpoint.removePropertyChangeListener(this);
    }

    protected JPDABreakpoint getBreakpoint () {
        return breakpoint;
    }

    protected JPDADebuggerImpl getDebugger () {
        return debugger;
    }

    protected VirtualMachine getVirtualMachine () {
        return getDebugger ().getVirtualMachine ();
    }
    
    protected EventRequestManager getEventRequestManager () {
        return getVirtualMachine ().eventRequestManager ();
    }

    protected void addEventRequest (EventRequest r) {
        requests.add (r);
        getDebugger ().getOperator ().register (r, this);
        r.setSuspendPolicy (getBreakpoint ().getSuspend ());
        r.enable ();
    }

    private void removeAllEventRequests () {
        if (requests.size () == 0) return;
        if (getDebugger ().getVirtualMachine () == null) return; 
        int i, k = requests.size ();
        try {
            for (i = 0; i < k; i++) { 
                EventRequest r = (EventRequest) requests.get (i);
                getDebugger ().getVirtualMachine ().eventRequestManager ().
                    deleteEventRequest (r);
                getDebugger ().getOperator ().unregister (r);
            }
            
        } catch (VMDisconnectedException e) {
        } catch (com.sun.jdi.InternalException e) {
        }
        requests = new LinkedList ();
    }

    public boolean perform (
        String condition,
        ThreadReference thread,
        ReferenceType referenceType,
        Value value
    ) {
        boolean resume;
        if ((condition == null) || condition.equals ("")) {
            JPDABreakpointEvent e = new JPDABreakpointEvent (
                getBreakpoint (),
                JPDABreakpointEvent.CONDITION_NONE,
                debugger.getThread (thread), 
                referenceType, 
                debugger.getVariable (value)
            );
            getDebugger ().fireBreakpointEvent (
                getBreakpoint (),
                e
            );
            resume = e.getResume ();
        } else {
            resume = evaluateCondition (
                condition, 
                thread,
                referenceType,
                value
            );
        }
        if (!resume)
            getDebugger ().setStoppedState (thread);
        return resume; 
    }

    private boolean evaluateCondition (
        String condition, 
        ThreadReference thread,
        ReferenceType referenceType,
        Value value
    ) {
        try {
            StackFrame sf = thread.frame (0);
            try {
                boolean result = evaluateConditionIn (condition, sf);
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    result ? 
                        JPDABreakpointEvent.CONDITION_TRUE : 
                        JPDABreakpointEvent.CONDITION_FALSE,
                    debugger.getThread (thread), 
                    referenceType, 
                    debugger.getVariable (value)
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                            
                // condition true => stop here (do not resume)
                // condition false => resume
                return !result || ev.getResume ();
            } catch (ParseException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    ex,
                    debugger.getThread (thread), 
                    referenceType, 
                    debugger.getVariable (value)
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                return ev.getResume ();
            } catch (InvalidExpressionException ex) {
                JPDABreakpointEvent ev = new JPDABreakpointEvent (
                    getBreakpoint (),
                    ex,
                    debugger.getThread (thread), 
                    referenceType, 
                    debugger.getVariable (value)
                );
                getDebugger ().fireBreakpointEvent (
                    getBreakpoint (),
                    ev
                );
                return ev.getResume ();
            }
        } catch (IncompatibleThreadStateException ex) {
            // should not occurre
            ex.printStackTrace ();
        }
        // some error occured during evaluation of expression => do not resume
        return false; // do not resume
    }

    /**
     * Evaluates given condition. Returns value of condition evaluation. 
     * Returns true othervise (bad expression).
     */
    private boolean evaluateConditionIn (
        String condExpr, 
        StackFrame frame
    ) throws ParseException, InvalidExpressionException {
        // 1) compile expression
        if ( compiledCondition == null || 
             !compiledCondition.getExpression ().equals (condExpr)
        )
            compiledCondition = Expression.parse (
                condExpr, 
                Expression.LANGUAGE_JAVA_1_5
            );
        
        // 2) evaluate expression
        com.sun.jdi.Value value = getDebugger ().evaluateIn (
            compiledCondition, 
            frame
        );
        try {
            return ((com.sun.jdi.BooleanValue) value).booleanValue ();
        } catch (ClassCastException e) {
            throw new InvalidExpressionException (e);
        }
    }
}
