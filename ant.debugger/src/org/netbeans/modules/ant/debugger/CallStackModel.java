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

package org.netbeans.modules.ant.debugger;

import java.util.Vector;
import javax.swing.Action;

import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.text.Annotatable;

import org.openide.text.Line;

/**
 *
 * @author   Jan Jancura
 */
public class CallStackModel implements TreeModel, NodeModel, 
NodeActionsProvider, TableModel {
    
    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";
    
    private AntDebugger debugger;
    private Vector listeners = new Vector ();
    
    
    public CallStackModel (ContextProvider contextProvider) {
        debugger = (AntDebugger) contextProvider.lookupFirst 
            (null, AntDebugger.class);
    }
    
    
    // TreeModel implementation ................................................
    
    /** 
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    public Object getRoot () {
        return ROOT;
    }
    
    /** 
     * Returns children for given parent on given indexes.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException {
        if (parent == ROOT)
            return debugger.getCallStack ();
        throw new UnknownTypeException (parent);
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT)
            return false;
        if (node instanceof TargetLister.Target)
            return true;
        if (node instanceof Task)
            return true;
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     * @since 1.1
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        if (node == ROOT)
            return debugger.getCallStack ().length;
        throw new UnknownTypeException (node);
    }

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
        listeners.add (l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
        listeners.remove (l);
    }
    
    
    // NodeModel implementation ................................................
    
    /**
     * Returns display name for given node.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public String getDisplayName (Object node) throws UnknownTypeException {
        if (node instanceof TargetLister.Target) 
            return ((TargetLister.Target) node).getName ();
        if (node instanceof Task) 
            return ((Task) node).getTaskStructure ().getName ();
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns icon for given node.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof TargetLister.Target) 
            return CALL_STACK;
        if (node instanceof Task) 
            return CURRENT_CALL_STACK;
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns tooltip for given node.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public String getShortDescription (Object node) 
    throws UnknownTypeException {
        if (node instanceof TargetLister.Target) 
            return null;
        if (node instanceof Task) 
            return null;
        throw new UnknownTypeException (node);
    }
        
     
    // NodeActionsProvider implementation ......................................
    
    /**
     * Performs default action for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  display name for given node
     */
    public void performDefaultAction (Object node) 
    throws UnknownTypeException {
        if (node instanceof TargetLister.Target) {
            Utils.showLine (Utils.getLine ((TargetLister.Target) node, null));
            return;
        }
        if (node instanceof Task) {
            Utils.showLine (((Task) node).getLine ());
            return;
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns set of actions for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  display name for given node
     */
    public Action[] getActions (Object node) 
    throws UnknownTypeException {
        return new Action [] {};
    }
        
     
    // TableModel implementation ...............................................
    
    /**
     * Returns value to be displayed in column <code>columnID</code>
     * and row identified by <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
     *
     * @param node a object returned from 
     *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws ComputingException if the value is not known yet and will 
     *         be computed later
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    public Object getValueAt (Object node, String columnID) throws 
    UnknownTypeException {
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) {
            if (node instanceof TargetLister.Target) {
                TargetLister.Target target = (TargetLister.Target) node;
                return target.getScript ().getFile ().getName () + ":" + 
                    Utils.getLineNumber (
                        Utils.getLine ((TargetLister.Target) node, null)
                    );
            }
            if (node instanceof Task) {
                Task task = (Task) node;
                return task.getFile ().getName () + ":" + 
                    Utils.getLineNumber (task.getLine ()) + 1;
            }
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns true if value displayed in column <code>columnID</code>
     * and row <code>node</code> is read only. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public boolean isReadOnly (Object node, String columnID) throws 
    UnknownTypeException {
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) {
            if (node instanceof TargetLister.Target) {
                return true;
            }
            if (node instanceof Task) {
                return true;
            }
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @param value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public void setValueAt (Object node, String columnID, Object value) 
    throws UnknownTypeException {
        throw new UnknownTypeException (node);
    }

    
    // other mothods ...........................................................

    void fireChanges () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((TreeModelListener) v.get (i)).treeChanged ();
    }
}
