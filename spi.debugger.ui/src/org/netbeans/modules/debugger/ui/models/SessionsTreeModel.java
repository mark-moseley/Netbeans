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

package org.netbeans.modules.debugger.ui.models;

import java.lang.ref.WeakReference;
import java.util.Vector;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class SessionsTreeModel implements TreeModel {
    
    private Listener listener;
    private Vector listeners = new Vector ();
    
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if (parent == ROOT) {
            Session[] bs = DebuggerManager.getDebuggerManager ().
                getSessions ();
            if (listener == null)
                listener = new Listener (this);
            return bs;
        } else
        throw new UnknownTypeException (parent);
    }
    
    public boolean isLeaf (Object node)
    throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof Session) return true;
        throw new UnknownTypeException (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    public void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter {
        
        private WeakReference model;
        
        private Listener (
            SessionsTreeModel tm
        ) {
            model = new WeakReference (tm);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_SESSIONS,
                this
            );
        }
        
        private SessionsTreeModel getModel () {
            SessionsTreeModel m = (SessionsTreeModel) model.get ();
            if (m == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_SESSIONS,
                    this
                );
            }
            return m;
        }
        
        public void sessionAdded (Session s) {
            SessionsTreeModel m = getModel ();
            if (m == null) return;
            m.fireTreeChanged ();
        }
        
        public void sessionRemoved (Session s) {
            SessionsTreeModel m = getModel ();
            if (m == null) return;
            m.fireTreeChanged ();
        }
    }
}
