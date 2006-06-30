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

package org.netbeans.modules.jmx.mbeanwizard.renderer;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author an156382
 */
public class CheckBoxRenderer extends  DefaultTableCellRenderer {

    private JCheckBox comp;
    private Object obj;

    /** Creates a new instance of CheckBoxRenderer */
    public CheckBoxRenderer(JCheckBox comp) {
        this.comp = comp;
    }

    /**
         * Method returning the modified component (component + rendering)
         * @param table the table in which the component is contained
         * @param value the value of the component
         * @param isSelected true if the component is selected
         * @param hasFocus true if the component has the focus
         * @param row the row of the component in the table
         * @param column the column of the component in the table
         * @return Component the modified component
         */
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            //obj = table.getModel().getValueAt(row,column);
            //comp.setSelectedItem(obj);
            comp.setHorizontalAlignment(SwingConstants.CENTER);
            comp.setSelected(((Boolean)table.getModel().getValueAt(row,column)));
	    return comp;
	}

        /**
         * Returns simply the component
         * @return Component the component
         */
	public Component getComponent() {
	    return comp;
	}
    
}
