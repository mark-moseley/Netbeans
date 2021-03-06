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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Query;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public class SelectMethodsTableModel extends QueryMethodsTableModel {
    
    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
    Utils.getBundleMessage("LBL_ReturnType"),
    Utils.getBundleMessage("LBL_Query"),
    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 200, 100};
//    private JComboBox returnMethodComboBox = new JComboBox(FieldCustomizer.COMMON_TYPES);
//    private TableCellEditor returnMethodEditor = new DefaultCellEditor(returnMethodComboBox);
    
    public SelectMethodsTableModel(EntityHelper.Queries queries) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, queries);
    }
    
    public int addRow() {
//        queries.addSelectMethod();
        return getRowCount() - 1;
    }
    
    
    public boolean editRow(int row) {
        QueryMethodHelper helper = getQueryMethodHelper(row);
//        QueryCustomizer customizer = new QueryCustomizer();
//        Method method = helper.getPrototypeMethod();
//        if (method == null || method.getTypeName() == null){
//            return false;
//        }
//        method.setType(JMIUtils.resolveType(method.getTypeName().getName()));
//        Query aQuery = (Query) helper.query.clone();
//        boolean result = customizer.showSelectCustomizer(method, aQuery);
//        if (result) {
//            helper.updateSelectMethod(method, aQuery);
//        }
        return true;
    }
    
    public QueryMethodHelper getQueryMethodHelper(int row) {
        return queries.getSelectMethodHelper(row);
    }
    
    public int getRowCount() {
        return queries.getSelectMethodCount();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        QueryMethodHelper queryMethodHelper = getQueryMethodHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return queryMethodHelper.getQueryMethod().getMethodName();
            case 1:
                return queryMethodHelper.getReturnType();
            case 2:
                return queryMethodHelper.getEjbQl();
            case 3:
                return queryMethodHelper.getDefaultDescription();
        }
        return null;
    }
    
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Query query = (Query) queries.getSelecMethod(rowIndex).clone();
        if (columnIndex == 3) {
            query.setDescription((String) value);
        }
        QueryMethodHelper helper = getQueryMethodHelper(rowIndex);
//        Method method = helper.getPrototypeMethod();
//        helper.updateSelectMethod(method, query);
    }
    
    public TableCellEditor getCellEditor(int columnIndex) {
//        return columnIndex == 1 ? returnMethodEditor : super.getCellEditor(columnIndex);
        return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            return true;
        } else {
            return super.isCellEditable(rowIndex, columnIndex);
        }
    }
}
