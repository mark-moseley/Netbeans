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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;

/**
 * Class responsible for the attribute table shown when you use Add Attributes...
 * popup action in the contextual management menu.
 * @author tl156378
 */
public class AddAttributeTable extends AttributeTable {
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the panel to notify for events
     */
    public AddAttributeTable(AbstractTableModel model, FireEvent wiz) {
        super(model,wiz);
    }
        
   /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        AddMBeanAttributeTableModel addAttrModel = 
                (AddMBeanAttributeTableModel) this.getModel();
        int firstEditable = addAttrModel.getFirstEditable();
        
        if(row < firstEditable) {
            switch (column) {
                case AddMBeanAttributeTableModel.IDX_ATTR_NAME :
                    return new TextFieldRenderer(new JTextField(), true, false);
                case AddMBeanAttributeTableModel.IDX_ATTR_TYPE :
                    return new TextFieldRenderer(new JTextField(),true,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_ACCESS :
                    return new TextFieldRenderer(new JTextField(),true,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_DESCRIPTION :
                    return new TextFieldRenderer(new JTextField(), true, false);
                default : 
                    return super.getCellRenderer(row,column);
            }
        }
            
        return super.getCellRenderer(row,column);
    }
}
