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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.action.BpelActionsProviderSupport.PositionListener;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * The run-to-cursor action.
 *
 * @author Josh Sandusky
 * @author Jan Jancura
 */
public class RunToCursorActionProvider
        extends BpelActionsProviderSupport
        implements PropertyChangeListener
{

    private RunToCursorUtil myUtil;
    
    public RunToCursorActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider, ActionsManager.ACTION_RUN_TO_CURSOR);
        getDebugger().addPropertyChangeListener(new PositionListener(this));
        myUtil = new RunToCursorUtil();
        myUtil.pcs.addPropertyChangeListener(this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        updateState();
    }
    
    
    public void doAction(Object action) {
//        if (getDebugger().getRunToCursorBreakpoint() != null) {
//            getDebugger().setRunToCursorBreakpoint(null);
//        }
        LineBreakpoint breakpoint = myUtil.createBreakpointAtCursor();
        if (breakpoint != null) {
            getDebugger().runToCursor(breakpoint);
        } else {
            getDebugger().resume();
        }
    }
    
    protected void positionChanged(Position oldPosition, Position newPosition) {
        updateState();
    }
    
    private void updateState() {
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            getDebugger().getCurrentPosition() != null &&
            //(EditorContextBridge.getCurrentLineNumber () >= 0) && 
            (myUtil.getCurrentFile().endsWith (".bpel"))
        );
//        if (
//                getDebugger().getCurrentPosition() != null &&
//                getDebugger().getRunToCursorBreakpoint() != null)
//        {
//            getDebugger().setRunToCursorBreakpoint(null);
//        }
    }
}
