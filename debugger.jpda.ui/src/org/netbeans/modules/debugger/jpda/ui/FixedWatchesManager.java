/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import org.netbeans.spi.viewmodel.*;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.*;

import javax.swing.*;
import java.util.*;

/**
 * Manages lifecycle and presentation of fixed watches. Should be registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Maros Sandor
 */
public class FixedWatchesManager implements TreeModelFilter, NodeActionsProvider,
        NodeActionsProviderFilter, Models.ActionPerformer {

    private List            fixedWatches;
    private HashSet         listeners;
    private LookupProvider  lookupProvider; // not used at the moment

    private CreateFixedWatchActionPerformer    createFixedWatchActionPerformer;
    private DeleteFixedWatchActionPerformer    deleteFixedWatchActionPerformer;

    public FixedWatchesManager (LookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (!(node instanceof FixedWatch)) throw new UnknownTypeException(node);
    }

    public Action[] getActions(Object node) throws UnknownTypeException {
        if (node instanceof FixedWatch) {
            return new Action[] {
                Models.createAction("Delete", node, getDeleteFixedWatchActionPerformer(), true)
            };
        }
        throw new UnknownTypeException(node);
    }

    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action [] actions = original.getActions(node);
        List myActions = new ArrayList();
        if (node instanceof Variable) {
            myActions.add(Models.createAction("Create Fixed Watch", node, getCreateFixedWatchActionPerformer(), true));
        } else if (node instanceof JPDAWatch) {
            myActions.add(Models.createAction("Create Fixed Watch", node, getCreateFixedWatchActionPerformer(), true));
        } else if (node instanceof FixedWatch) {
            myActions.add(Models.createAction("Delete", node, getDeleteFixedWatchActionPerformer(), true));
        } else {
            throw new UnknownTypeException(node);
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }

    private DeleteFixedWatchActionPerformer getDeleteFixedWatchActionPerformer() {
        if (deleteFixedWatchActionPerformer == null) deleteFixedWatchActionPerformer = new DeleteFixedWatchActionPerformer();
        return deleteFixedWatchActionPerformer;
    }

    private class DeleteFixedWatchActionPerformer implements Models.ActionPerformer {
        public void perform(String action, Object node) {
            FixedWatch fw = (FixedWatch) node;
            removeFixedWatch(fw);
        }
    }

    private CreateFixedWatchActionPerformer getCreateFixedWatchActionPerformer() {
        if (createFixedWatchActionPerformer == null) createFixedWatchActionPerformer = new CreateFixedWatchActionPerformer();
        return createFixedWatchActionPerformer;
    }

    private class CreateFixedWatchActionPerformer implements Models.ActionPerformer {
        public void perform(String action, Object node) {
            if (node instanceof JPDAWatch) {
                JPDAWatch jw = (JPDAWatch) node;
                createFixedWatch(jw.getExpression(), jw.getType(), jw.getValue());
            } else {
                Variable variable = (Variable) node;
                String name = null;
                if (variable instanceof LocalVariable) {
                    name = ((LocalVariable) variable).getName();
                } else if (variable instanceof Field) {
                    name = ((Field) variable).getName();
                } else if (variable instanceof This) {
                    name = "this";
                } else if (variable instanceof ObjectVariable) {
                    name = "object";
                } else {
                    name = "unnamed";
                }
                createFixedWatch(name, variable);
            }
        }
    }

    public void perform(String action, Object node) {
        removeFixedWatch((FixedWatch)node);
    }

    private void removeFixedWatch(FixedWatch fw) {
        if (fixedWatches != null) {
            fixedWatches.remove(fw);
            fireModelChanged();
        }
    }

    private void createFixedWatch(String name, Variable variable) {
        if (fixedWatches == null) fixedWatches = new ArrayList();
        FixedWatch fw = new FixedWatch(name, variable);
        fixedWatches.add(fw);
        fireModelChanged();
    }

    private void createFixedWatch(String name, String type, String value) {
        if (fixedWatches == null) fixedWatches = new ArrayList();
        FixedWatch fw = new FixedWatch(name, type, value);
        fixedWatches.add(fw);
        fireModelChanged();
    }

    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws NoInformationException,
            ComputingException, UnknownTypeException {

        if (parent == TreeModel.ROOT) {
            Object [] children = original.getChildren(parent, from, to);
            if (fixedWatches == null) return children;

            Object [] allChildren = new Object[children.length + fixedWatches.size()];
            fixedWatches.toArray(allChildren);
            System.arraycopy(children, 0, allChildren, fixedWatches.size(), children.length);
            return allChildren;
        }
        if (parent instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) parent;
            return fw.getVariable() != null ? original.getChildren(fw.getVariable(), from, to) : new Object[0];
        }
        return original.getChildren(parent, from, to);
    }

    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) node;
            if (fw.getVariable() == null) return true;
            return original.isLeaf(fw.getVariable());
        }
        return original.isLeaf(node);
    }

    private void fireModelChanged() {
        if (listeners == null) return;
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeChanged();;
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        HashSet newListeners = (listeners == null) ? new HashSet() : (HashSet) listeners.clone();
        newListeners.add(l);
        listeners = newListeners;
    }

    public void removeTreeModelListener(TreeModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove(l);
        listeners = newListeners;
    }
}
