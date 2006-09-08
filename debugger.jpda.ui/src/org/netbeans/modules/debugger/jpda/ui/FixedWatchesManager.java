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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import javax.swing.KeyStroke;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeModel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;

/**
 * Manages lifecycle and presentation of fixed watches. Should be
 * registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Jan Jancura, Maros Sandor
 */
public class FixedWatchesManager implements TreeModelFilter, 
NodeActionsProviderFilter, NodeModelFilter {
            
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch";
    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle (FixedWatchesManager.class).getString 
            ("CTL_DeleteFixedWatch_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    fixedWatches.remove (nodes [i]);
                fireModelChanged(new ModelEvent.NodeChanged(
                        FixedWatchesManager.this,
                        TreeModel.ROOT,
                        ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private final Action CREATE_FIXED_WATCH_ACTION = Models.createAction (
        NbBundle.getBundle (FixedWatchesManager.class).getString 
            ("CTL_CreateFixedWatch_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    createFixedWatch (nodes [i]);
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
        
        
    private Map             fixedWatches = new HashMap ();
    private HashSet         listeners;
    private ContextProvider contextProvider;

    
    public FixedWatchesManager (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }
    

    // TreeModelFilter .........................................................
    
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }

    public Object[] getChildren (
        TreeModel original, 
        Object parent, 
        int from, 
        int to
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            if (fixedWatches.size () == 0) 
                return original.getChildren (parent, from, to);

            int fixedSize = fixedWatches.size ();
            int originalFrom = from - fixedSize;
            int originalTo = to - fixedSize;
            if (originalFrom < 0) originalFrom = 0;

            Object[] children;
            if (originalTo > originalFrom) {
                children = original.getChildren
                    (parent, originalFrom, originalTo);
            } else {
                children = new Object [0];
            }
            Object [] allChildren = new Object [children.length + fixedSize];

            fixedWatches.keySet ().toArray (allChildren);
            System.arraycopy (
                children, 
                0, 
                allChildren, 
                fixedSize,
                children.length
            );
            Object[] fallChildren = new Object [to - from];
            System.arraycopy (allChildren, from, fallChildren, 0, to - from);
            return fallChildren;
        }
        return original.getChildren (parent, from, to);
    }

    public int getChildrenCount (
        TreeModel original, 
        Object parent
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            int chc = original.getChildrenCount (parent);
            if (chc < Integer.MAX_VALUE) {
                chc += fixedWatches.size ();
            }
            return chc;
        }
        return original.getChildrenCount (parent);
    }

    public boolean isLeaf (TreeModel original, Object node) 
    throws UnknownTypeException {
        return original.isLeaf (node);
    }

    public synchronized void addModelListener (ModelListener l) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(l);
    }

    public synchronized void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        listeners.remove (l);
    }

    
    // NodeActionsProviderFilter ...............................................

    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        List myActions = new ArrayList();
        if (fixedWatches.containsKey (node)) {
            return new Action[] {
                DELETE_ACTION
            };
        }
        if (node instanceof Variable) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
        if (node instanceof JPDAWatch) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
            return actions;
        myActions.addAll (Arrays.asList (actions));
        return (Action[]) myActions.toArray (new Action [myActions.size ()]);
    }
    
    
    // NodeModel ...............................................................
    
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node))
            return (String) fixedWatches.get (node);
        return original.getDisplayName (node);
    }
    
    public String getShortDescription (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node)) {
            Variable v = (Variable) node;
            return ((String) fixedWatches.get (node)) + 
                " = (" + v.getType () + ") " + 
                v.getValue ();
        }
        return original.getShortDescription (node);
    }
    
    public String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException {
        if (fixedWatches.containsKey (node))
            return FIXED_WATCH;
        return original.getIconBase (node);
    }
    
    
    // other methods ...........................................................
    
    private void createFixedWatch (Object node) {
        if (node instanceof JPDAWatch) {
            JPDAWatch jw = (JPDAWatch) node;
            addFixedWatch (jw.getExpression (), jw);
        } else {
            Variable variable = (Variable) node;
            String name = null;
            if (variable instanceof LocalVariable) {
                name = ((LocalVariable) variable).getName ();
            } else if (variable instanceof Field) {
                name = ((Field) variable).getName();
            } else if (variable instanceof This) {
                name = "this";
            } else if (variable instanceof ObjectVariable) {
                name = "object";
            } else {
                name = "unnamed";
            }
            addFixedWatch (name, variable);
        }
    }

    private void addFixedWatch (String name, Variable variable) {
        // Clone the variable to assure that it's unique and sticks to the JDI value.
        if (variable instanceof Cloneable) {
            try { // terrible code to invoke the clone() method
                java.lang.reflect.Method cloneMethod = variable.getClass().getMethod("clone", new Class[] {});
                cloneMethod.setAccessible(true);
                Object newVar = cloneMethod.invoke(variable, new Object[] {});
                if (newVar instanceof Variable) {
                    variable = (Variable) newVar;
                }
            } catch (Exception ex) {} // Ignore any exceptions
        }
        fixedWatches.put (variable, name);
        fireModelChanged (new ModelEvent.NodeChanged(
                this,
                TreeModel.ROOT,
                ModelEvent.NodeChanged.CHILDREN_MASK));
    }

    private void fireModelChanged (ModelEvent event) {
        HashSet listenersCopy;
        synchronized (this) {
            if (listeners == null) return;
            listenersCopy = new HashSet(listeners);
        }
        for (Iterator i = listenersCopy.iterator (); i.hasNext ();) {
            ModelListener listener = (ModelListener) i.next();
            listener.modelChanged(event);
        }
    }
    
}
