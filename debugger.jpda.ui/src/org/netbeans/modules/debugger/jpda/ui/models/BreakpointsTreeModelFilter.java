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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * Filters content of some original tree of nodes (represented by 
 * {@link TreeModel}).
 *
 * @author   Jan Jancura
 */
public class BreakpointsTreeModelFilter implements TreeModelFilter {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.show_hidden_breakpoints") != null;

    /** 
     * Returns filtered root of hierarchy.
     *
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
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
        TreeModel   original, 
        Object      parent, 
        int         from, 
        int         to
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        Object[] ch = original.getChildren (parent, from, to);
        List l = new ArrayList ();
        int i, k = ch.length;
        for (i = 0; i < k; i++) {
            if ( (!verbose) &&
                 (ch [i] instanceof JPDABreakpoint) &&
                 ((JPDABreakpoint) ch [i]).isHidden ()
            ) continue;
            l.add (ch [i]); 
        }
        l = l.subList (from, to);
        Object[] bs = new Object [l.size ()];
        return l.toArray (bs);
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
        TreeModel original,
        Object node
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        int j = original.getChildrenCount (node);
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if ( (!verbose) &&
                 (bs [i] instanceof JPDABreakpoint) &&
                 ((JPDABreakpoint) bs [i]).isHidden ()
            ) j--;
        }
        return j;
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
        return original. isLeaf (node);
    }

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
}
