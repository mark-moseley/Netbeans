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

import com.sun.jdi.VirtualMachine;

import java.util.Collections;

import java.util.Set;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/**
*
* @author   Jan Jancura
*/
public class StartActionProvider extends ActionsProvider {
//    private static transient String []        stopMethodNames = 
//        {"main", "start", "init", "<init>"}; // NOI18N

    private JPDADebuggerImpl debuggerImpl;
    private ContextProvider lookupProvider;
    
    
    public StartActionProvider (ContextProvider lookupProvider) {
        debuggerImpl = (JPDADebuggerImpl) lookupProvider.lookupFirst
            (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_START);
    }
    
    public void doAction (Object action) {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        if (debugger != null && debugger.getVirtualMachine() != null) return;
        final AbstractDICookie cookie = (AbstractDICookie) lookupProvider.
            lookupFirst (null, AbstractDICookie.class);
        
        Thread startingThread = new Thread (
            new Runnable () {
                public void run () {
                    try {
                        VirtualMachine virtualMachine = cookie.
                            getVirtualMachine ();
                        Operator o = createOperator (virtualMachine);
                        debuggerImpl.setRunning (
                            virtualMachine,
                            o
                        );
                        o.start ();
//                        virtualMachine.resume ();
//                        debuggerImpl.setRunning ();
                    } catch (Exception ex) {
                        debuggerImpl.setException (ex);
                        ((Session) lookupProvider.lookupFirst 
                            (null, Session.class)).kill ();
                    }
                }
            },
            "Debugger start"
        );
        debuggerImpl.setStarting (startingThread);
        startingThread.start ();
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
    
    private Operator createOperator (
        VirtualMachine virtualMachine
    ) {
        return new Operator (
            virtualMachine,
//            null,
//            new Executor () {
//                public boolean exec (com.sun.jdi.event.Event event) {
//                    debuggerImpl.setRunning ();
//                    return true; // resume
//                }
//            },
            null,
            new Runnable () {
                public void run () {
                    ((Session) lookupProvider.lookupFirst 
                        (null, Session.class)).kill ();
                }
            }
        );
    }
}
