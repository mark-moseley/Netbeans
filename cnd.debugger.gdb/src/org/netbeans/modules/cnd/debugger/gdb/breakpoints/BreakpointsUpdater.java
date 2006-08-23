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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;


/**
 * Listener on all breakpoints and prints text specified in the breakpoint when a it hits.
 *
 * @see GdbBreakpoint#setPrintText(java.lang.String)
 * @author Gordon Prieur (based on Maros Sandor's JPDA implementation)
 */
public class BreakpointsUpdater extends LazyActionsManagerListener {
    
    private GdbDebugger debugger;
    
    public BreakpointsUpdater(ContextProvider lookupProvider) {
        GdbDebugger debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        this.debugger = debugger;
        EditorContextBridge.createTimeStamp(debugger);
        BreakpointAnnotationListener bal = (BreakpointAnnotationListener) 
            DebuggerManager.getDebuggerManager().lookupFirst(null, BreakpointAnnotationListener.class);
        bal.updateLineBreakpoints();
    }
    
    protected void destroy () {
        EditorContextBridge.disposeTimeStamp(debugger);
    }
    
    public String[] getProperties() {
        return new String[0];
    }
}
