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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;
import javax.swing.Action;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Queue;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

//import org.netbeans.modules.cnd.debugger.gdb.ObjectVariable;
import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.netbeans.modules.cnd.debugger.gdb.VariablesFilter;

/*
 * VariablesTreeModelFilter.java
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */

public class VariablesTreeModelFilter implements TreeModelFilter, 
    NodeModelFilter, TableModelFilter, NodeActionsProviderFilter, Runnable {
    
    private ContextProvider  lookupProvider;
    
    private final Collection modelListeners = new HashSet();
    
    private RequestProcessor evaluationRP = new RequestProcessor();
    
    private RequestProcessor.Task evaluationTask;
    
    private LinkedList evaluationQueue = new LinkedList();
    
    
    public VariablesTreeModelFilter (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }
    
    static boolean isEvaluated(Object o) {
        if (o instanceof Refreshable) {
            return ((Refreshable) o).isCurrent();
        }
        return true;
    }
    
    private static void waitToEvaluate(Object o) {
        if (o instanceof Refreshable) {
            // waits for the evaluation, the retrieval must already be initiated
            try {
                ((Refreshable) o).refresh();
            } catch (RefreshFailedException exc) {
                // Thrown when interrupted
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void postEvaluationMonitor(Object o, Runnable whenEvaluated) {
        synchronized (evaluationQueue) {
            if (evaluationQueue.contains(o) &&
                evaluationQueue.contains(whenEvaluated)) return ;
            if (evaluationTask == null) {
                evaluationTask = evaluationRP.create(this);
            }
            evaluationQueue.add(o);
            evaluationQueue.add(whenEvaluated);
            evaluationTask.schedule(1);
        }
    }
    
    public void run() {
        Object node;
        do {
            node = null;
            Runnable whenEvaluated = null;
            synchronized (evaluationQueue) {
                if (!evaluationQueue.isEmpty()) {
                    node = evaluationQueue.removeFirst();
                    whenEvaluated = (Runnable) evaluationQueue.removeFirst();
                }
            }
            if (node != null) {
                waitToEvaluate(node);
                if (whenEvaluated != null) {
                    whenEvaluated.run();
                } else {
                    fireModelChange(new ModelEvent.NodeChanged(this, node));
                    //System.out.println("FIRE "+node+" evaluated, ID = "+node.hashCode());
                }
            }
        } while (node != null);
        evaluationTask = null;
    }
    
    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren (
        final TreeModel   original, 
        final Object      parent, 
        final int         from, 
        final int         to
    ) throws UnknownTypeException {
        Object[] ch;
        VariablesFilter vf = getFilter (parent, true, new Runnable() {
            public void run() {
                fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                                                           parent,
                                                           ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        });
        if (vf == null) 
            ch = original.getChildren (parent, from, to);
        else
            ch = vf.getChildren (original, (Variable) parent, from, to);
        return ch;
    }
    
    /**
     * Returns number of filterred children for given node.
     * 
     * @param   original the original tree model
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (
        final TreeModel   original, 
        final Object      parent
    ) throws UnknownTypeException {
        /*NM TEMPORARY COMMENTED OUT
        VariablesFilter vf = getFilter (parent, true, new Runnable() {
            public void run() {
                fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                                                           parent,
                                                           ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        });
        int count;
        if (vf == null) {
            count = original.getChildrenCount (parent);
        } else {
            count = vf.getChildrenCount (original, (Variable) parent);
        }
        */
        int count = original.getChildrenCount (parent); //NM TEMPORARY
        return count;
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf (
        TreeModel original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (vf == null) 
            return original.isLeaf (node);
        return vf.isLeaf (original, (Variable) node);
    }

    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }
    
    
    // NodeModelFilter
    
    public String getDisplayName (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        final String[] unfilteredDisplayName = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredDisplayName;
                try {
                    filteredDisplayName = vf.getDisplayName (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredDisplayName = utex.toString();
                }
                if (!filteredDisplayName.equals(unfilteredDisplayName[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.DISPLAY_NAME_MASK));
                }
            }
        });
        if (vf == null) {
            String displayName = original.getDisplayName (node);
            unfilteredDisplayName[0] = displayName;
            return displayName;
        } else {
            return vf.getDisplayName (original, (Variable) node);
        }
    }
    
    public String getIconBase (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        final String[] unfilteredIconBase = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredIconBase;
                try {
                    filteredIconBase = vf.getIconBase (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredIconBase = utex.toString();
                }
                if (!filteredIconBase.equals(unfilteredIconBase[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.ICON_MASK));
                }
            }
        });
        if (vf == null) {
            String iconBase = original.getIconBase (node);
            unfilteredIconBase[0] = iconBase;
            return iconBase;
        } else {
            return vf.getIconBase (original, (Variable) node);
        }
    }
    
    public String getShortDescription (final NodeModel original, final Object node) 
    throws UnknownTypeException {
        final String[] unfilteredShortDescription = new String[] { null };
        VariablesFilter vf = getFilter (node, true, new Runnable() {
            public void run() {
                VariablesFilter vf = getFilter (node, false, null);
                if (vf == null) return ;
                String filteredShortDescription;
                try {
                    filteredShortDescription = vf.getShortDescription (original, (Variable) node);
                } catch (UnknownTypeException utex) {
                    // still do fire
                    filteredShortDescription = utex.toString();
                }
                if (!filteredShortDescription.equals(unfilteredShortDescription[0])) {
                    fireModelChange(new ModelEvent.NodeChanged(VariablesTreeModelFilter.this,
                            node, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        if (vf == null) {
            return original.getShortDescription (node);
        } else {
            return vf.getShortDescription (original, (Variable) node);
        }
    }
    
    
    // NodeActionsProviderFilter
    
    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (vf == null) 
            return original.getActions (node);
        return vf.getActions (original, (Variable) node);
    }
    
    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (node, true, null);
        if (vf == null) 
            original.performDefaultAction (node);
        else
            vf.performDefaultAction (original, (Variable) node);
    }
    
    
    // TableModelFilter
    
    public Object getValueAt (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        Object value;
        VariablesFilter vf = getFilter (row, false, null);
        if (vf == null) {
            value = original.getValueAt (row, columnID);
        } else {
            value = vf.getValueAt (original, (Variable) row, columnID);
        }
        return value;
    }
    
    public boolean isReadOnly (
        TableModel original, 
        Object row, 
        String columnID
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, true, null);
        if (vf == null) 
            return original.isReadOnly (row, columnID);
        return vf.isReadOnly (original, (Variable) row, columnID);
    }
    
    public void setValueAt (
        TableModel original, 
        Object row, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        VariablesFilter vf = getFilter (row, false, null);
        if (vf == null)
            original.setValueAt (row, columnID, value);
        else
            vf.setValueAt (original, (Variable) row, columnID, value);
    }
    
    
    // helper methods ..........................................................
    
    private HashMap typeToFilter;
    private HashMap ancestorToFilter;
    
    /**
     * @param o The object to get the filter for
     * @param checkEvaluated Whether we should check if the object was already evaluated
     * @param whenEvaluated If the object is not yet evaluated, <code>null</code>
     *                      will be returned and <code>whenEvaluated.run()<code>
     *                      will be executed when the object becomes evaluated.
     * @return The filter or <code>null</code>.
     */
    private VariablesFilter getFilter (Object o, boolean checkEvaluated, Runnable whenEvaluated) {
        if (typeToFilter == null) {
            typeToFilter = new HashMap ();
            ancestorToFilter = new HashMap ();
            List l = lookupProvider.lookup (null, VariablesFilter.class);
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                VariablesFilter f = (VariablesFilter) l.get (i);
                String[] types = f.getSupportedAncestors ();
                int j, jj = types.length;
                for (j = 0; j < jj; j++)
                    ancestorToFilter.put (types [j], f);
                types = f.getSupportedTypes ();
                jj = types.length;
                for (j = 0; j < jj; j++)
                    typeToFilter.put (types [j], f);
            }
        }
        
        if (typeToFilter.size() == 0) return null; // Optimization for corner case
        
        if (!(o instanceof Variable)) return null;
        
        Variable v = (Variable) o;
        
        if (checkEvaluated) {
            if (!isEvaluated(v)) {
                if (whenEvaluated != null) {
                    postEvaluationMonitor(o, whenEvaluated);
                }
                return null;
            }
        }
        
        String type = v.getType ();
        VariablesFilter vf = (VariablesFilter) typeToFilter.get (type);
        if (vf != null) return vf;
        
        /*NM TEMPORARY COMMENTED OUT
        if (!(o instanceof ObjectVariable)) return null;
        ObjectVariable ov = (ObjectVariable) o;
        ov = ov.getSuper ();
        while (ov != null) {
            type = ov.getType ();
            vf = (VariablesFilter) ancestorToFilter.get (type);
            if (vf != null) return vf;
            ov = ov.getSuper ();
        }
        */
        return null;
    }

}
