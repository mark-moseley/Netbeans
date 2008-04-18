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
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.MBeanOperationException;


/**
 * Class implementing the table model for the mbean operation exception
 * popup table
 *
 */
public class OperationExceptionTableModel extends AbstractJMXTableModel{

    public static final int IDX_EXCEPTION_NAME           = 0;
    public static final int IDX_EXCEPTION_DESCR          = 1;
        
    /**
     * Constructor
     */
    public OperationExceptionTableModel ()
    {
        super();
        bundle = NbBundle.getBundle(OperationExceptionTableModel.class);
        data = new ArrayList();
        columnNames = new String[2];
        String sen = bundle.getString("LBL_ExceptionClass");// NOI18N
        String sed = bundle.getString("LBL_ExceptionDescription");// NOI18N
        columnNames[IDX_EXCEPTION_NAME] = sen;
        columnNames[IDX_EXCEPTION_DESCR] = sed;
    }

    /**
     * Instantiates a new exception; called when a line is added to the 
     * popup table
     * @return MBeanOperationException the created exception
     */
    public MBeanOperationException createNewException() {
        
        return new MBeanOperationException(
                WizardConstants.METH_EXCEP_CLASS_DEFVALUE,
                WizardConstants.METH_EXCEP_DESCR_DEFVALUE);
    }
    
    /**
     * Returns the operation exception at index index
     * @return MBeanOperationException the operation exception at index index
     */
    public MBeanOperationException getException(int index) {
        return (MBeanOperationException)data.get(index);
    } 
    
    /**
     * Sets the operation exception at index index to a new object
     * @param index the index
     * @param excep the Operation exception to set
     */
    public void setException(int index, MBeanOperationException excep) {
        if (index < data.size()) {
            data.set(index, excep);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanOperationException excep = (MBeanOperationException)data.get(row);
        switch(col) {
            case 0: return excep.getExceptionClass();
            case 1: return excep.getExceptionDescription();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "OperationExceptionTableModel " + col);// NOI18N
                break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanOperationException excep = 
                    (MBeanOperationException)data.get(rowIndex);
            switch(columnIndex) {
            case 0: excep.setExceptionClass((String)aValue);
                break;
            case 1: excep.setExceptionDescription((String)aValue);
                break;
            default: System.out.println("Error setValueAt " +// NOI18N
                    "OperationExceptionTableModel " + columnIndex);// NOI18N
                break;    
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanOperationException mboe = createNewException();
        data.add(mboe);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
