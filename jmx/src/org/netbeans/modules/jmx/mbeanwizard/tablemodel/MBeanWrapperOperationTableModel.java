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

package org.netbeans.modules.jmx.mbeanwizard.tablemodel;

import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.mbeanwizard.MBeanWrapperOperation;
import org.netbeans.modules.jmx.MBeanOperation;
     
/**
 *
 * @author an156382
 */
public class MBeanWrapperOperationTableModel extends MBeanMethodTableModel {
    
    //other idx inherited
    public static final int IDX_METH_SELECTION        = 0;
    
    /** Creates a new instance of MBeanWrapperOperationTableModel */
    public MBeanWrapperOperationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanMethodTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[6];
        
        String ss = bundle.getString("LBL_MethodSelected");
        String sn = bundle.getString("LBL_MethodName");
        String st = bundle.getString("LBL_MethodReturnType");
        String sp = bundle.getString("LBL_MethodParamType");
        String se = bundle.getString("LBL_MethodExceptionType");
        String sd = bundle.getString("LBL_MethodDescription");
        
        columnNames[IDX_METH_SELECTION]              = ss;
        columnNames[super.IDX_METH_NAME +1]          = sn;
        columnNames[super.IDX_METH_TYPE +1]          = st;
        columnNames[super.IDX_METH_PARAM +1]         = sp;
        columnNames[super.IDX_METH_EXCEPTION +1]     = se;
        columnNames[super.IDX_METH_DESCRIPTION +1]   = sd;
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanWrapperOperation oper = (MBeanWrapperOperation)data.get(row);
        switch(col) {
            case 0: return oper.isSelected();
            case 1: return oper.getName();
            case 2: return oper.getReturnTypeName();
            case 3: return oper.getSignature();
            case 4: return oper.getExceptionClasses();
            case 5: return oper.getDescription();
            default: System.out.println("Error getValueAt " +
                    "MBeanWrapperOperationTableModel " + col);
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanWrapperOperation oper = (MBeanWrapperOperation)data.get(rowIndex);
            switch(columnIndex) {
                case 0: oper.setSelected((Boolean)aValue);
                break; /*
                case 1: oper.setName((String)aValue);
                break;
                case 2: oper.setReturnTypeName((String)aValue);
                break;
                case 3: oper.setSimpleSignature((String)aValue);
                break;
                case 4: oper.setExceptionClasses((String)aValue);
                break;
                        **/
                case 5: oper.setDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +
                        "MBeanWrapperOperationTableModel " + columnIndex);
                break;
            }
        }
    }
    
    public void addRow(MBeanOperation op) {
        MBeanWrapperOperation mbo = new MBeanWrapperOperation(
                true,
                op.getName(),
                op.getReturnTypeName(),
                op.getParametersList(),
                op.getExceptionsList(),
                op.getDescription());
        data.add(mbo);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
