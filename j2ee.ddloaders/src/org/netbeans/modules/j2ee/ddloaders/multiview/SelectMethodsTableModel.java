/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.QueryCustomizer;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.FieldCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.src.MethodElement;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
class SelectMethodsTableModel extends QueryMethodsTableModel {

    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
                                                    Utils.getBundleMessage("LBL_ReturnType"),
                                                    Utils.getBundleMessage("LBL_Query"),
                                                    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 200, 100};
    private JComboBox returnMethodComboBox = new JComboBox(FieldCustomizer.COMMON_TYPES);
    private TableCellEditor returnMethodEditor = new DefaultCellEditor(returnMethodComboBox);

    public SelectMethodsTableModel(FileObject ejbJarFile, Entity entity, EntityHelper entityHelper) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, ejbJarFile, entity, entityHelper);
    }

    public int addRow() {
        entityHelper.addSelectMethod();
        initMethods();
        fireTableRowsInserted(-1, -1);
        return getRowCount() - 1;
    }


    public void editRow(int row) {
        Query query = (Query) getQueries().get(row);
        QueryMethodHelper helper = getQueryMethodHelper(query);
        QueryCustomizer customizer = new QueryCustomizer();
        MethodElement methodElement = (MethodElement) helper.getPrototypeMethod().clone();
        query = (Query) query.clone();
        boolean result = customizer.showSelectCustomizer(methodElement, query);
        if (result) {
            helper.updateSelectMethod(methodElement, query);
            fireTableRowsUpdated(row, row);
        }
    }

    protected boolean isSupportedMethod(Query query) {
        return query.getQueryMethod().getMethodName().startsWith("ejbSelectBy"); //NOI18N
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Query query = (Query) getQueries().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return query.getQueryMethod().getMethodName();
            case 1:
                return getQueryMethodHelper(query).getReturnType();
            case 2:
                return query.getEjbQl();
            case 3:
                return query.getDefaultDescription();
        }
        return null;
    }

    public TableCellEditor getCellEditor(int columnIndex) {
        return columnIndex == 1 ? returnMethodEditor : super.getCellEditor(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return super.isCellEditable(rowIndex, columnIndex);
    }
}
