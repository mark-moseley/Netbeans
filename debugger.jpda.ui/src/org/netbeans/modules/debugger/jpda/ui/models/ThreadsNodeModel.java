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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class ThreadsNodeModel implements NodeModel {
    
    public static final String CURRENT_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread"; // NOI18N
    public static final String RUNNING_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N
    public static final String SUSPENDED_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread"; // NOI18N
    public static final String THREAD_GROUP =
        "org/netbeans/modules/debugger/resources/threadsView/ThreadGroup"; // NOI18N
    public static final String CURRENT_THREAD_GROUP =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThreadGroup"; // NOI18N
    
    
    private JPDADebugger debugger;
    private Session session;
    private Vector listeners = new Vector ();
    
    
    public ThreadsNodeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        session = (Session) lookupProvider.
            lookupFirst (null, Session.class);
        new Listener (this, debugger);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Name";
        } else
        if (o instanceof JPDAThread) {
            return ((JPDAThread) o).getName ();
        } else
        if (o instanceof JPDAThreadGroup) {
            return ((JPDAThreadGroup) o).getName ();
        } else 
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Displayes all threads for current session.";
        } else
        if (o instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) o;
            int i = t.getState ();
            String s = "";
            switch (i) {
                case JPDAThread.STATE_UNKNOWN:
                    s = "Unknown";
                    break;
                case JPDAThread.STATE_MONITOR:
                    ObjectVariable ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = "Waiting on synchronized block";
                    else
                        try {
                            s = "Waiting on synchronized block (" + ov.getToStringValue () + ")";
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_NOT_STARTED:
                    s = "Not Started";
                    break;
                case JPDAThread.STATE_RUNNING:
                    s = "Running";
                    break;
                case JPDAThread.STATE_SLEEPING:
                    s = "Sleeping";
                    break;
                case JPDAThread.STATE_WAIT:
                    ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = "Waiting";
                    else
                        try {
                            s = "Waiting on " + ov.getToStringValue ();
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_ZOMBIE:
                    s = "State Zombie";
                    break;
            }
            if (t.isSuspended () && (t.getStackDepth () > 0)) {
                try { 
                    CallStackFrame sf = t.getCallStack () [0];
                    s += " " + CallStackNodeModel.getCSFName (session, sf, true);
                } catch (NoInformationException e) {
                }
            }
            return s;
        } else
        if (o instanceof JPDAThreadGroup) {
            return ((JPDAThreadGroup) o).getName ();
        } else 
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof String) {
            return THREAD_GROUP;
        } else
        if (o instanceof JPDAThread) {
            if (o == debugger.getCurrentThread ())
                return CURRENT_THREAD;
            return ((JPDAThread) o).isSuspended () ? 
                SUSPENDED_THREAD : RUNNING_THREAD;
            
        } else
        if (o instanceof JPDAThreadGroup) {
            JPDAThread t = debugger.getCurrentThread ();
            if (t == null)
                return THREAD_GROUP;
            JPDAThreadGroup tg = t.getParentThreadGroup ();
            while (tg != null) {
                if (tg == o) return CURRENT_THREAD_GROUP;
                tg = tg.getParentThreadGroup ();
            }
            return THREAD_GROUP;
        } else 
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    private void fireTreeNodeChanged (Object parent) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
    }

    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private JPDADebugger debugger;
        
        private Listener (
            ThreadsNodeModel rm,
            JPDADebugger debugger
        ) {
            ref = new WeakReference (rm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener (
                debugger.PROP_CURRENT_THREAD,
                this
            );
        }
        
        private ThreadsNodeModel getModel () {
            ThreadsNodeModel rm = (ThreadsNodeModel) ref.get ();
            if (rm == null) {
                debugger.removePropertyChangeListener (
                    debugger.PROP_CURRENT_THREAD,
                    this
                );
            }
            return rm;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            ThreadsNodeModel rm = getModel ();
            if (rm == null) return;
            rm.fireTreeChanged ();
        }
    }
}
