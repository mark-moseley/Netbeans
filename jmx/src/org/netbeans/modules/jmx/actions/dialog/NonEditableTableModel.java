/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import javax.swing.table.DefaultTableModel;

/**
 * Allows to have a non editable table.
 * @author tl156378
 */
public class NonEditableTableModel extends DefaultTableModel {
    
   public NonEditableTableModel(Object[][] cells, String[]columnNames) {
       super(cells,columnNames);
   }
   
   /**
     * Returns false regardless of parameter values.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return                  true
     * @see #setValueAt
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    } 
    
}
