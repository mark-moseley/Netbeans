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

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 * Implementation of breakpoint on function.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class FunctionBreakpointImpl extends BreakpointImpl {
    
    private FunctionBreakpoint  breakpoint;
    private FunctionBreakpoint  latestBreakpoint = null;
    private String              functionName;
    private BreakpointsReader   reader;
    
    
    public FunctionBreakpointImpl(FunctionBreakpoint breakpoint, BreakpointsReader reader,
            GdbDebugger debugger, Session session) {
        super(breakpoint, reader, debugger, session);
        this.reader = reader;
        this.breakpoint = breakpoint;
        functionName = breakpoint.getFunctionName();
        set();
    }
    
    protected void setRequests() {
        if (getDebugger().getState().equals(GdbDebugger.STATE_RUNNING)) {
            getDebugger().setSilentStop();
        }
        if (getState().equals(BPSTATE_UNVALIDATED)) {
            setState(BPSTATE_VALIDATION_PENDING);
            functionName = breakpoint.getFunctionName();
            int token = getDebugger().getGdbProxy().break_insert(functionName);
            getDebugger().addPendingBreakpoint(token, this);
        } else {
            if (getState().equals(BPSTATE_DELETION_PENDING)) {
                getDebugger().getGdbProxy().break_delete(getBreakpointNumber());
	    } else if (getState().equals(BPSTATE_VALIDATED)) {
                if (breakpoint.isEnabled()) {
                    getDebugger().getGdbProxy().break_enable(getBreakpointNumber());
                } else {
                    getDebugger().getGdbProxy().break_disable(getBreakpointNumber());
                }
            }
        }
    }
}

