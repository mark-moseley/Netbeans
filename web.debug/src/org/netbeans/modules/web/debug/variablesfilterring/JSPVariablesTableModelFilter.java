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

package org.netbeans.modules.web.debug.variablesfilterring;

import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.TableModel;

import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesTableModelFilter implements TableModelFilter {
    
    public JSPVariablesTableModelFilter() {
    }

    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param   original the original table model
     * @param   node a object returned from {@TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getValueAt (...)</code> method call only!
     *
     * @return value of variable representing given position in tree table.
     */
    public Object getValueAt(TableModel original, Object node, String columnID)
    throws ComputingException, UnknownTypeException
    {
        
        Object colValue = "";
        if (node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            colValue = original.getValueAt(((AttributeMap.Attribute)node).getValue(), columnID);
        else if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals)
            colValue = "";
        else
            colValue = original.getValueAt(node, columnID);
        
        return colValue;
    }
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.setValueAt (...)</code> method call only!
     */
    public void setValueAt(TableModel original, Object node, String columnID, Object value)
    throws UnknownTypeException
    {
            original.setValueAt(node, columnID, value);
    }

    /**
     * Filters original isReadOnly value from given table model. You should 
     * not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isReadOnly (...)</code> method call only!
     *
     * @return true if variable on given position is read only
     */
    public boolean isReadOnly(TableModel original, Object node, String columnID)
    throws UnknownTypeException
    {
        boolean ro = true;
        if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals ||
                 node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            ro = true;
        else
            ro = original.isReadOnly(node, columnID);
        
        return ro;
    }

    public void removeTreeModelListener(org.netbeans.spi.viewmodel.TreeModelListener l) {
    }

    public void addTreeModelListener(org.netbeans.spi.viewmodel.TreeModelListener l) {
    }

}
