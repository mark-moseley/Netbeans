/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    private JPDADebuggerImpl debugger;
    
    /** The ReqeustProcessor used by action performers. */
    private static RequestProcessor actionsRequestProcessor;
    
    private static Set providersToDisableOnLazyActions = new HashSet();
    
    private volatile boolean disabled;
    
    JPDADebuggerActionProvider (JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (debugger.getState() == JPDADebuggerImpl.STATE_DISCONNECTED) {
            synchronized (JPDADebuggerActionProvider.class) {
                if (actionsRequestProcessor != null) {
                    actionsRequestProcessor.stop();
                    actionsRequestProcessor = null;
                }
            }
        }
        checkEnabled (debugger.getState ());
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    public boolean isEnabled (Object action) {
        if (!disabled) {
            checkEnabled (debugger.getState ());
        }
        return super.isEnabled (action);
    }
    
    JPDADebuggerImpl getDebuggerImpl () {
        return debugger;
    }
    
    void removeStepRequests (ThreadReference tr) {
        //S ystem.out.println ("removeStepRequests");
        try {
            VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
            if (vm == null) return;
            EventRequestManager erm = vm.eventRequestManager ();
            ArrayList l = new ArrayList (erm.stepRequests ());
            Iterator it = l.iterator ();
            while (it.hasNext ()) {
                StepRequest stepRequest = (StepRequest) it.next ();
                if (stepRequest.thread ().equals (tr)) {
                    //S ystem.out.println("  remove request " + stepRequest);
                    erm.deleteEventRequest (stepRequest);
                    break;
                }
                //S ystem.out.println("  do not remove " + stepRequest + " : " + stepRequest.thread ());
            }
        } catch (VMDisconnectedException e) {
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (InvalidRequestStateException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Mark the provided action provider to be disabled when a lazy action is to be performed.
     */
    protected final void setProviderToDisableOnLazyAction(JPDADebuggerActionProvider provider) {
        synchronized (JPDADebuggerActionProvider.class) {
            providersToDisableOnLazyActions.add(provider);
        }
    }
    
    /**
     * Do the action lazily in a RequestProcessor.
     * @param run The action to perform.
     */
    protected final void doLazyAction(final Runnable run) {
        final Set disabledActions;
        synchronized (JPDADebuggerActionProvider.class) {
            if (actionsRequestProcessor == null) {
                actionsRequestProcessor = new RequestProcessor("JPDA Processor", 1); // NOI18N
            }
            disabledActions = new HashSet(providersToDisableOnLazyActions);
        }
        for (Iterator it = disabledActions.iterator(); it.hasNext(); ) {
            JPDADebuggerActionProvider ap = (JPDADebuggerActionProvider) it.next();
            Set actions = ap.getActions();
            ap.disabled = true;
            for (Iterator ait = actions.iterator(); ait.hasNext(); ) {
                Object action = ait.next();
                ap.setEnabled (action, false);
                //System.out.println(ap+".setEnabled("+action+", "+false+")");
            }
        }
        actionsRequestProcessor.post(new Runnable() {
            public void run() {
                try {
                    run.run();
                    for (Iterator it = disabledActions.iterator(); it.hasNext(); ) {
                        JPDADebuggerActionProvider ap = (JPDADebuggerActionProvider) it.next();
                        Set actions = ap.getActions();
                        ap.disabled = false;
                        ap.checkEnabled (debugger.getState ());
                    }
                } catch (com.sun.jdi.VMDisconnectedException e) {
                    // Causes kill action when something is being evaluated
                }
            }
        });
    }
}
