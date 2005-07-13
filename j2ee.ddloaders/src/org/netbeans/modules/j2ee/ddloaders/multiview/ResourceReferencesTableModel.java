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
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public class ResourceReferencesTableModel extends InnerTableModel {

    private Ejb ejb;
    private static final int COLUMN_AUTHENTICATION = 2;
    private static final int COLUMN_SHAREABLE = 3;
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Name"),
                                                  Utils.getBundleMessage("LBL_ResourceType"),
                                                  Utils.getBundleMessage("LBL_Authentication"),
                                                  Utils.getBundleMessage("LBL_Shareable"),
                                                  Utils.getBundleMessage("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{100, 200, 120, 80, 150};

    public ResourceReferencesTableModel(XmlMultiViewDataObject dataObject, Ejb ejb) {
        super(dataObject, COLUMN_NAMES, COLUMN_WIDTHS);
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
        modelUpdatedFromUI();
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
        String text = Utils.getBundleMessage("LBL_ReferenceName");
        String title = Utils.getBundleMessage("LBL_AddResourceReference");
        final NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(text, title);
        DialogDisplayer.getDefault().notify(inputLine);
        final String name = inputLine.getInputText();
        if (name != null && name.trim().length() > 0) {
            ResourceRef resourceRef = ejb.newResourceRef();
            resourceRef.setResRefName(name);
            ejb.addResourceRef(resourceRef);
            modelUpdatedFromUI();
        }
        int row = getRowCount() - 1;
        return row;
    }

    public void removeRow(int row) {
        ejb.removeResourceRef(ejb.getResourceRef(row));
        modelUpdatedFromUI();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    public TableCellEditor getTableCellEditor(int column) {
        if (column == COLUMN_AUTHENTICATION) {
            return createComboBoxCellEditor(new Object[]{"Application", "Container"});
        } else if (column == COLUMN_SHAREABLE) {
            return createComboBoxCellEditor(new Object[]{"Shareable", "Unshareable"});
        } else {
            return null;
        }
    }
}
