/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.jmx.mbeanwizard.tablemodel;


import java.util.ArrayList;
import javax.swing.JTable;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.WizardConstants;


/**
 * Class implementing the table model for the mbean operation parameter popup table
 *
 */
public class OperationParameterTableModel extends AbstractJMXTableModel{

    public static final int IDX_OP_PARAM_NAME          = 0;
    public static final int IDX_OP_PARAM_TYPE          = 1;
    public static final int IDX_OP_PARAM_DESCRIPTION   = 2;
        
    /**
     * Constructor
     */
    public OperationParameterTableModel ()
    {
        super();
        bundle = NbBundle.getBundle(OperationParameterTableModel.class);
        data = new ArrayList();

        columnNames = new String[3];
        String sopn = bundle.getString("LBL_OperationParameterName");// NOI18N
        String sopt = bundle.getString("LBL_OperationParameterType");// NOI18N
        String sopd = bundle.getString("LBL_OperationParameterDescription");// NOI18N
        
        columnNames[IDX_OP_PARAM_NAME]        = sopn;
        columnNames[IDX_OP_PARAM_TYPE]        = sopt;        
        columnNames[IDX_OP_PARAM_DESCRIPTION] = sopd;
    }
    
    /**
     * Instantiates a new parameter; called when a line is added to the 
     * popup table
     * @return MBeanOperationParameter the created parameter
     */
    public MBeanOperationParameter createNewParameter() {
        
        return new MBeanOperationParameter(
                WizardConstants.METH_PARAM_NAME_DEFVALUE + this.getRowCount(),
                WizardConstants.STRING_OBJ_NAME,
                WizardConstants.METH_PARAM_DESCR_DEFVALUE_PREFIX + 
                this.getRowCount() +
                        WizardConstants.METH_PARAM_DESCR_DEFVALUE_SUFFIX);
    }
    
    /**
     * Returns the operation parameter at index index
     * @return MBeanOperationParameter the operation parameter at index index
     */
    public MBeanOperationParameter getParameter(int index) {
        return (MBeanOperationParameter)data.get(index);
    } 
    
    /**
     * Sets the operation parameter at index index to a new object
     * @param index the index
     * @param param the Operation parameter to set
     */
    public void setParameter(int index, MBeanOperationParameter param) {
        if (index < data.size()) {
            data.set(index, param);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperationParameter param = (MBeanOperationParameter)data.get(row);
        switch(col) {
            case 0: return param.getParamName();
            case 1: return param.getParamType();
            case 2: return param.getParamDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "OperationParameterTableModel " + col);// NOI18N
                break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperationParameter param = 
                    (MBeanOperationParameter)data.get(rowIndex);
            switch(columnIndex) {
                case 0: param.setParamName((String)aValue);
                break;
                case 1: param.setParamType((String)aValue);
                break;
                case 2: param.setParamDescription((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "OperationParameterTableModel " + columnIndex);// NOI18N
                break;    
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperationParameter mbop = createNewParameter();
        data.add(mbop);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
