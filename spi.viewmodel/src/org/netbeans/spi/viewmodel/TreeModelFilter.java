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

package org.netbeans.spi.viewmodel;



/**
 * Filters content of some original tree of nodes (represented by 
 * {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public interface TreeModelFilter {
    

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public abstract Object getRoot (TreeModel original);
    
    /** 
     * Returns filtered children for given parent on given indexes.
     * Typically you should get original nodes 
     * (<code>original.getChildren (...)</code>), and modify them, or return
     * it without modifications. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param   original the original tree model
     * @param   parent a parent of returned nodes
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getChildren (...)</code> method call only!
     *
     * @return  children for given parent on given indexes
     */
    public abstract Object[] getChildren (
        TreeModel   original, 
        Object      parent, 
        int         from, 
        int         to
    ) throws NoInformationException, ComputingException, UnknownTypeException;
    
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
    public abstract int getChildrenCount (
        TreeModel original,
        Object node
    ) throws NoInformationException, ComputingException, UnknownTypeException;
    
    /**
     * Returns true if node is leaf. You should not throw UnknownTypeException
     * directly from this method!
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isLeaf (...)</code> method call only!
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (
        TreeModel original, 
        Object node
    ) throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addTreeModelListener (TreeModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeTreeModelListener (TreeModelListener l);
}
