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

import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;

/**
 * @author pfiala
 */
class ResourceReferencesTableModel extends InnerTableModel {
    private Ejb ejb;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Name"),
                                                  Utils.getBundleMessage("LBL_ResourceType"),
                                                  Utils.getBundleMessage("LBL_Authentication"),
                                                  Utils.getBundleMessage("LBL_Shareable"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 200, 120, 80, 150};

    public ResourceReferencesTableModel(Ejb ejb) {
        super(COLUMN_NAMES, COLUMN_WIDTHS);
        this.ejb = ejb;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ResourceRef resourceRef = ejb.getResourceRef(rowIndex);
        switch (columnIndex) {
            case 0:
                resourceRef.setResRefName((String) value);
                break;
            case 1:
                resourceRef.setResType((String) value);
                break;
            case 2:
                resourceRef.setResAuth((String) value);
                break;
            case 3:
                resourceRef.setResSharingScope((String) value);
                break;
            case 4:
                resourceRef.setDescription((String) value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
        return ejb.getResourceRef().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ResourceRef resourceRef = ejb.getResourceRef(rowIndex);
        switch (columnIndex) {
            case 0:
                return resourceRef.getResRefName();
            case 1:
                return resourceRef.getResType();
            case 2:
                return resourceRef.getResAuth();
            case 3:
                return resourceRef.getResSharingScope();
            case 4:
                return resourceRef.getDefaultDescription();
        }
        return null;
    }

    public int addRow() {
        ResourceRef resourceRef = ejb.newResourceRef();
        ejb.addResourceRef(resourceRef);
        int row = getRowCount() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    public void removeRow(int row) {
        fireTableStructureChanged(); // cancel editing
        ejb.removeResourceRef(ejb.getResourceRef(row));
        fireTableRowsDeleted(row, row);
    }
}
