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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.ParseException;

import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
public class WatchesModel implements TreeModel {

    
    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('w') >= 0);

    private static boolean      USE_CACHE = false;
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private Vector              listeners = new Vector ();
    private ContextProvider     lookupProvider;
    private Map                 watchToJPDAWatch = new HashMap ();
    
    
    public WatchesModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }

    /**
     *
     * @return watches contained in this group of watches
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException, NoInformationException {
        if (parent == ROOT) {
            
            // 1) get Watches
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            Watch[] fws = new Watch [to - from];
            System.arraycopy (ws, from, fws, 0, to - from);
            
            // 2) create JPDAWatches for Watches
            int i, k = fws.length;
            Object[] result = new Object [k];
            for (i = 0; i < k; i++) {
                if (watchToJPDAWatch.containsKey (fws [i]))
                    result [i] = watchToJPDAWatch.get (fws [i]);
                else
                    result [i] = fws [i];
            }
            
            if (listener == null)
                listener = new Listener (this, debugger);
            return result;
        }
        if (parent instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildren (parent, from, to);
        }
        return getLocalsTreeModel ().getChildren (parent, from, to);
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
    public int getChildrenCount (Object node) throws UnknownTypeException, 
    NoInformationException {
        if (node == ROOT) {
            if (listener == null)
                listener = new Listener (this, debugger);
            return DebuggerManager.getDebuggerManager ().getWatches ().length;
        }
        if (node instanceof JPDAWatchImpl) {
            return getLocalsTreeModel ().getChildrenCount (node);
        }
        return getLocalsTreeModel ().getChildrenCount (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof JPDAWatchImpl) 
            return ((JPDAWatchImpl) node).isPrimitive ();
        if (node instanceof Watch) 
            return true;
        return getLocalsTreeModel ().isLeaf (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
    
    void fireNodeChanged (JPDAWatch w) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeNodeChanged (w);
    }
    
    
    // other methods ...........................................................
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    private LocalsTreeModel localsTreeModel;

    LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }

    // map from expression to Expression or Exception
    private WeakHashMap         watchToExpression = new WeakHashMap();

    private void evaluate (Watch watch) {
        String text = watch.getExpression ();
        
        // 1) resolve text expression to Expression or Exception
        Object expression = watchToExpression.get (text);
        if (expression == null) {
            try {
                expression = Expression.parse (
                    text, 
                    Expression.LANGUAGE_JAVA_1_5
                );
            } catch (ParseException e) {
                expression = e;
            }
            watchToExpression.put (
                text, 
                expression
            );
        }

        // 2) create a new JPDAWatch
        JPDAWatch jpdaWatch = null;
        if (expression instanceof Exception)
            jpdaWatch = new JPDAWatchImpl 
                (this, watch, (Exception) expression);
        else
            jpdaWatch = evaluate (watch, (Expression) expression);
        
        watchToJPDAWatch.put (watch, jpdaWatch);
        fireTreeChanged ();
    }
    
    JPDAWatch evaluate (Watch w, Expression expr) {
        try {
            Value v = debugger.evaluateIn (expr);
            if (v instanceof ObjectReference)
                return new JPDAObjectWatchImpl (this, w, (ObjectReference) v);
            return new JPDAWatchImpl (this, w, v);
        } catch (InvalidExpressionException e) {
            return new JPDAWatchImpl (this, w, e);
        }
    }

/*
    JPDAWatch evaluate (Watch w) {
        Value v = null;
        String exception = null;
        try {
            v = debugger.evaluateIn (w.getExpression ());
        } catch (InvalidExpressionException e) {
            e.printStackTrace ();
            exception = e.getMessage ();
        }
        JPDAWatch wi;
        if (exception != null)
            wi = new JPDAWatchImpl (this, w, exception);
        else
            if (v instanceof ObjectReference)
                wi = new JPDAObjectWatchImpl (this, w, (ObjectReference) v);
            else
                wi = new JPDAWatchImpl (this, w, v);
        return wi;
    }
*/

    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference model;
        private WeakReference debugger;
        
        private Listener (
            WatchesModel tm,
            JPDADebuggerImpl debugger
        ) {
            model = new WeakReference (tm);
            this.debugger = new WeakReference (debugger);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            debugger.addPropertyChangeListener (this);
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].addPropertyChangeListener (this);
        }
        
        private WatchesModel getModel () {
            WatchesModel m = (WatchesModel) model.get ();
            if (m == null) destroy ();
            return m;
        }
        
        public void watchAdded (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.addPropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        public void watchRemoved (Watch watch) {
            WatchesModel m = getModel ();
            if (m == null) return;
            watch.removePropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        // currently waiting / running refresh tasks
        // there is at most one
        private RequestProcessor.Task[] tasks = new RequestProcessor.Task [0];
        
        public void propertyChange (PropertyChangeEvent evt) {
            final WatchesModel m = getModel ();
            if (m == null) return;
            if (m.debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
                destroy ();
                return;
            }
            
            if (evt.getSource () instanceof Watch) {
                m.fireTreeChanged ();
            }

            synchronized (tasks) {
                // cancel old tasks
                int i, k = tasks.length;
                for (i = 0; i < k; i++) {
                    tasks [i].cancel ();
                    if (verbose)
                        System.out.println("WM cancel old task " + tasks [i]);
                }

                Watch[] ws = DebuggerManager.getDebuggerManager ().
                    getWatches ();
                k = ws.length;
                tasks = new RequestProcessor.Task [k];
                for (i = 0; i < k; i++) {
                    final Watch watch = ws [i];
                    final int in = i;
                    tasks [i] = RequestProcessor.
                        getDefault ().post (
                            new Runnable () {
                                public void run () {
                                    if (verbose)
                                        System.out.println 
                                            ("WM do task " + tasks [in]);
                                    m.evaluate (watch);
                                }
                            }, 
                            500
                        );
                    if (verbose)
                        System.out.println("WM  create task " + tasks [i]);
                } // for
            } // synchronized
        }
        
        private void destroy () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            JPDADebugger d = (JPDADebugger) debugger.get ();
            if (d != null)
                d.removePropertyChangeListener (this);

            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].removePropertyChangeListener (this);

            synchronized (tasks) {
                // cancel old tasks
                k = tasks.length;
                for (i = 0; i < k; i++) {
                    tasks [i].cancel ();
                    if (verbose)
                        System.out.println("WM cancel old task " + tasks [i]);
                }
            }
        }
    }
}
