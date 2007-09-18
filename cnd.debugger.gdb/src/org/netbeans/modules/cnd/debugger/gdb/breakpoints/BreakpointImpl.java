/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public abstract class BreakpointImpl implements PropertyChangeListener {
    
    /* valid breakpoint states */
    public static final String BPSTATE_UNVALIDATED = "BpState_Unvalidated"; // NOI18N
    public static final String BPSTATE_VALIDATION_PENDING = "BpState_ValidationPending"; // NOI18N
    public static final String BPSTATE_VALIDATION_FAILED = "BpState_ValidationFailed"; // NOI18N
    public static final String BPSTATE_VALIDATED = "BpState_Validated"; // NOI18N
    public static final String BPSTATE_DELETION_PENDING = "BpState_DeletionPending"; // NOI18N
    
    private GdbDebugger debugger;
    private String state;
    private int breakpointNumber;
    private GdbBreakpoint breakpoint;
    private BreakpointsReader reader;
    private final Session session;
    
    protected BreakpointImpl(GdbBreakpoint breakpoint, BreakpointsReader reader, GdbDebugger debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        this.breakpoint = breakpoint;
        this.session = session;
        this.state = BPSTATE_UNVALIDATED;
        this.breakpointNumber = -1;
    }

    public void completeValidation(Map<String, String> map) {
        assert getState().equals(BPSTATE_VALIDATION_PENDING) : getState();
        if (map != null) {
            String number = map.get("number"); // NOI18N
            if (number != null) {
                breakpointNumber = Integer.parseInt(number);
                setState(BPSTATE_VALIDATED);
                if (!breakpoint.isEnabled()) {
                    getDebugger().break_disable(breakpointNumber);
                }
                if (this instanceof FunctionBreakpointImpl) {
                    try {
                        breakpoint.setURL(map.get("fullname")); // NOI18N
                        breakpoint.setLineNumber(Integer.parseInt(map.get("line"))); // NOI18N
                    } catch (Exception ex) {
                    }
                }
            } else {
                setState(BPSTATE_VALIDATION_FAILED);
            }
            getDebugger().getBreakpointList().put(number, this);
        }
    }

    /**
     * Get the state of this breakpoint
     */
    protected String getState() {
        return state;
    }

    /** Set the state of this breakpoint */
    protected void setState(String state) {
        if (!state.equals(this.state) &&
                (state.equals(BPSTATE_UNVALIDATED) ||
                 state.equals(BPSTATE_VALIDATION_PENDING) ||
                 state.equals(BPSTATE_VALIDATION_FAILED) ||
                 state.equals(BPSTATE_VALIDATED) ||
                 state.equals(BPSTATE_DELETION_PENDING))) {
            this.state = state;
            if (state.equals(BPSTATE_UNVALIDATED)) {
                setBreakpointNumber(-1);
            }
        }
    }

    public int getBreakpointNumber() {
        return breakpointNumber;
    }

    protected void setBreakpointNumber(int breakpointNumber) {
        this.breakpointNumber = breakpointNumber;
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set() {
        breakpoint.setDebugger(getDebugger());
        breakpoint.addPropertyChangeListener(this);
        update();
    }

    protected abstract void setRequests();

    /**
     * Called when Fix&Continue is invoked. Reqritten in LineBreakpointImpl.
     */
    void fixed() {
        update();
    }

    /**
     * Called from set () and propertyChanged.
     */
    final void update() {
        if (!getDebugger().getState().equals(GdbDebugger.STATE_NONE)) {
            setRequests();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (Breakpoint.PROP_DISPOSED.equals(evt.getPropertyName())) {
            remove();
        }
        if (!evt.getPropertyName().equals(GdbBreakpoint.PROP_LINE_NUMBER)) {
            update();
        }
    }

    protected final void remove() {
        breakpoint.removePropertyChangeListener(this);
        setState(BPSTATE_DELETION_PENDING);
        if (breakpointNumber > 0) {
            getDebugger().getBreakpointList().remove(breakpointNumber);
        }
    }

    public GdbBreakpoint getBreakpoint() {
        return breakpoint;
    }

    protected GdbDebugger getDebugger() {
        return debugger;
    }

    public boolean perform(String condition) {
        boolean resume = false;

        if (condition == null || condition.equals("")) { // NOI18N
            GdbBreakpointEvent e = new GdbBreakpointEvent(getBreakpoint(), getDebugger(), GdbBreakpointEvent.CONDITION_NONE, null);
            getDebugger().fireBreakpointEvent(getBreakpoint(), e);
            //resume = getBreakpoint().getSuspend() == GdbBreakpoint.SUSPEND_NONE || e.getResume();
        } else {
            //resume = evaluateCondition(condition, thread, referenceType, value);
            //PATCH 48174
            //resume = getBreakpoint().getSuspend() == GdbBreakpoint.SUSPEND_NONE || resume;
        }
        if (!resume) {
            DebuggerManager.getDebuggerManager().setCurrentSession(session);
            //getDebugger().setStoppedState(thread);
        }
        return resume;
    }
    
//    private boolean evaluateCondition(String condition, Value value) {
//
//        try {
//            try {
//                boolean result;
//                GdbBreakpointEvent ev;
//                synchronized (getDebugger().LOCK) {
//                    StackFrame sf = thread.frame (0);
//                    result = evaluateConditionIn (condition, sf);
//                    ev = new GdbBreakpointEvent (
//                        getBreakpoint (),
//                        getDebugger(),
//                        result ?
//                            GdbBreakpointEvent.CONDITION_TRUE :
//                            GdbBreakpointEvent.CONDITION_FALSE,
//                        getDebugger().getThread (thread),
//                        referenceType,
//                        getDebugger().getVariable (value)
//                    );
//                }
//                getDebugger().fireBreakpointEvent(getBreakpoint(), ev);
//
//                // condition true => stop here (do not resume)
//                // condition false => resume
//                if (verbose)
//                    System.out.println ("B perform breakpoint (condition = " + result + "): " + this + " resume: " + (!result || ev.getResume ()));
//                return !result || ev.getResume ();
//            } catch (ParseException ex) {
//                GdbBreakpointEvent ev = new GdbBreakpointEvent (
//                    getBreakpoint (),
//                    getDebugger(),
//                    ex,
//                    getDebugger().getThread (thread),
//                    referenceType,
//                    getDebugger().getVariable (value)
//                );
//                getDebugger().fireBreakpointEvent(getBreakpoint(), ev);
//                return ev.getResume ();
//            } catch (InvalidExpressionException ex) {
//                GdbBreakpointEvent ev = new GdbBreakpointEvent (
//                    getBreakpoint (),
//                    getDebugger(),
//                    ex,
//                    getDebugger().getThread (thread),
//                    referenceType,
//                    getDebugger().getVariable (value)
//                );
//                getDebugger ().fireBreakpointEvent (
//                    getBreakpoint (),
//                    ev
//                );
//                return ev.getResume ();
//            }
//        } catch (IncompatibleThreadStateException ex) {
//             should not occurre
//            ex.printStackTrace ();
//        }
//        // some error occured during evaluation of expression => do not resume
//
//
//        return false; // do not resume
//    }
//
//    /**
//     * Evaluates given condition. Returns value of condition evaluation.
//     * Returns true othervise (bad expression).
//     */
//    private boolean evaluateConditionIn(String condExpr, Object frame)
//                        throws ParseException, InvalidExpressionException {
//        // 1) compile expression
//        if (compiledCondition == null || !compiledCondition.getExpression().equals(condExpr)) {
//            compiledCondition = Expression.parse(condExpr, Expression.LANGUAGE_CPLUSPLUS);
//        }
//
//        // 2) evaluate expression
//        // already synchronized (getDebugger().LOCK)
//        Boolean value = getDebugger().evaluateIn(compiledCondition, frame);
//        try {
//            return value.booleanValue();
//        } catch (ClassCastException e) {
//            throw new InvalidExpressionException(e);
//        }
//    }
//
//    /**
//     * Support method for simple patterns.
//     */
//    static boolean match(String name, String pattern) {
//        String star = "*"; // NOI18N
//        if (pattern.startsWith(star)) {
//            return name.endsWith(pattern.substring(1));
//        } else if (pattern.endsWith(star)) {
//            return name.startsWith(pattern.substring(0, pattern.length() - 1));
//        }
//        return name.equals(pattern);
//    }
}