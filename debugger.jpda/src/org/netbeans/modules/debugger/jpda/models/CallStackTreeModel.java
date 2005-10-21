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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.RequestProcessor;


/**
 * This tree model provides an array of CallStackFrame objects.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class CallStackTreeModel implements TreeModel {

    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('c') >= 0);
    
    private JPDADebuggerImpl    debugger;
    private Collection          listeners = new HashSet();
    private Listener            listener;
    
   
    public CallStackTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if ( parent.equals (ROOT) ||
             (parent instanceof JPDAThread) 
        ) {
            // 1) get Thread
            JPDAThread thread;
            if (parent.equals (ROOT)) {
                thread = debugger.getCurrentThread ();
            } else {
                thread = (JPDAThread) parent;
            }
            if (thread == null) {
                return new String[] {"No current thread"}; // TODO make localizable!!!
            }
            
            // 2) get StackFrames for this Thread
            try {
                CallStackFrame[] sfs = thread.getCallStack(from, to);
                return sfs;
            } catch (AbsentInformationException aiex) {
                if (aiex.getCause() instanceof IncompatibleThreadStateException) {
                    return new String[] {"Thread is running"}; // TODO make localizable!!!
                } else {
                    return new String[] {"No call stack information available."}; // TODO make localizable!!!
                }
            }
        } else
        throw new UnknownTypeException (parent);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object parent) throws UnknownTypeException {
        if ( parent.equals (ROOT) ||
             (parent instanceof JPDAThread) 
        ) {
            // 1) get Thread
            JPDAThread thread;
            if (parent.equals (ROOT)) {
                thread = debugger.getCurrentThread ();
            } else {
                thread = (JPDAThread) parent;
            }
            if (thread == null) {
                return 1; //new String [] {"No current thread"};
            }
            
            return thread.getStackDepth();
        } else
        throw new UnknownTypeException (parent);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node.equals (ROOT)) return false;
        if (node instanceof CallStackFrame) return true;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null) {
                listener = new Listener (this, debugger);
            }
        }
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0) {
                listener.destroy ();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        Object[] ls;
        synchronized (listeners) {
            ls = listeners.toArray();
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ((ModelListener) ls[i]).modelChanged (ev);
        }
    }
    
    
    /**
     * Listens on JPDADebugger on PROP_STATE
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference model;
        
        public Listener (
            CallStackTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference (tm);
            debugger.addPropertyChangeListener (this);
        }
        
        private CallStackTreeModel getModel () {
            CallStackTreeModel tm = (CallStackTreeModel) model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("CSTM cancel old task " + task);
                task = null;
            }
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( ( (e.getPropertyName () == 
                     debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == debugger.PROP_STATE)
                 ) && (debugger.getState () == debugger.STATE_STOPPED)
            ) {
                final CallStackTreeModel tm = getModel ();
                if (tm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("CSTM cancel old task " + task);
                    task = null;
                }
                task = RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        if (debugger.getState () != debugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println("CSTM cancel started task " + task);
                            return;
                        }
                        if (verbose)
                            System.out.println("CSTM do task " + task);
                        tm.fireTreeChanged ();
                    }
                }, 500);
                if (verbose)
                    System.out.println("CSTM  create task " + task);
            } else
            if ( (e.getPropertyName () == debugger.PROP_STATE) &&
                 (debugger.getState () != debugger.STATE_STOPPED) &&
                 (task != null)
            ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel ();
                if (verbose)
                    System.out.println("CSTM cancel task " + task);
                task = null;
            }
        }
    }
}

