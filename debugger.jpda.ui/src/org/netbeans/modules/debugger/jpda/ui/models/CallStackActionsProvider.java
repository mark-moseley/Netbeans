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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;

import org.netbeans.modules.debugger.jpda.ui.Context;
import org.netbeans.modules.debugger.jpda.ui.EngineContext;


/**
 * @author   Jan Jancura
 */
public class CallStackActionsProvider implements NodeActionsProvider,
Models.ActionPerformer {
    
    private JPDADebugger    debugger;
    private LookupProvider  lookupProvider;


    public CallStackActionsProvider (LookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (JPDADebugger.class);
    }
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        if (node == TreeModel.ROOT)
            return new Action [0];
        EngineContext ectx = (EngineContext) lookupProvider.lookupFirst 
            (EngineContext.class);
        if (node instanceof CallStackFrame)
            return new Action [] {
                Models.createAction (
                    "Make Current",
                    (CallStackFrame) node,
                    this,
                    debugger.getCurrentCallStackFrame () != node
                ),
                Models.createAction (
                    "Pop To Here",
                    (CallStackFrame) node,
                    this,
                    ectx.sourceAvailable (
                        (CallStackFrame) node,
                        language
                    )
/*
                    Context.sourceAvailable (
                        (CallStackFrame) node,
                        language
                    )
*/
                ),
                Models.createAction (
                    "Properties",
                    (CallStackFrame) node,
                    this,
                    false
                )
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof CallStackFrame) {
            makeCurrent ((CallStackFrame) node);
            return;
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if ("Make Current".equals (action)) {
            makeCurrent ((CallStackFrame) node);
        } else
        if ("Pop To Here".equals (action)) {
            String language = DebuggerManager.getDebuggerManager ().
                getCurrentSession ().getCurrentLanguage ();
            EngineContext ectx = (EngineContext) lookupProvider.lookupFirst 
                (EngineContext.class);
            ectx.showSource ((CallStackFrame) node, language);
        } else
        if ("Properties".equals (action)) {
        }
    }    

    private void makeCurrent (CallStackFrame frame) {
        frame.makeCurrent ();
        popToHere (frame);
    }

    private void popToHere (final CallStackFrame frame) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                String language = DebuggerManager.getDebuggerManager ().
                    getCurrentSession ().getCurrentLanguage ();
                EngineContext ectx = (EngineContext) lookupProvider.lookupFirst 
                    (EngineContext.class);
                ectx.showSource (frame, language);
            }
        });
    }
}
