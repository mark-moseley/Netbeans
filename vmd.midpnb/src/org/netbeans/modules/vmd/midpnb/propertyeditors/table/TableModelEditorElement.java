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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;

/**
 *
 * @author Anton Chechel
 */
public class TableModelEditorElement extends PropertyEditorResourceElement {
    
    private static final int DEAFULT_TABLE_SIZE = 3;
    
    private long componentID;
    private CustomEditorTableModel tableModel;
    
    public TableModelEditorElement() {
        resetTableModel();
        initComponents();
    }
    
    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return SimpleTableModelCD.TYPEID;
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            setTableValues(null, null);
            setAllEnabled(false);
            return;
        }
        
        this.componentID = wrapper.getComponentID();
        final PropertyValue[] columns = new PropertyValue[1];
        final PropertyValue[] values = new PropertyValue[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) { // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    columns[0] = component.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES);
                    values[0] = component.readProperty(SimpleTableModelCD.PROP_VALUES);
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (SimpleTableModelCD.PROP_VALUES.equals(propertyName)) {
                    values[0] = propertyValue;
                } else if (SimpleTableModelCD.PROP_COLUMN_NAMES.equals(propertyName)) {
                    columns[0] = propertyValue;
                }
            }
        }

        // UI stuff
        setAllEnabled(true);
        setTableValues(columns[0], values[0]);
    }
    
    private void setTableValues(PropertyValue columns, PropertyValue values) {
        if (values == null) {
            resetTableModel();
        } else {
//            JTableHeader jth = new JTableHeader();
//            TableColumnModel tcm = table.getTableHeader().getColumnModel();
//            int count = tcm.getColumnCount();
//            for (int i = 0; i < count; i++) {
//                tcm.removeColumn(tcm.getColumn(i));
//            }
//            
//            List<PropertyValue> array = columns.getArray();
//            for (PropertyValue pv : array) {
//                tcm.addColumn(MidpTypes.getString(pv));
//            }
        }
    }
    
    private void resetTableModel() {
        tableModel = new CustomEditorTableModel();
        tableModel.setColumnCount(DEAFULT_TABLE_SIZE);
        tableModel.setRowCount(DEAFULT_TABLE_SIZE);
        if (table != null) {
            table.setModel(tableModel);
        }
    }

    private void setAllEnabled(boolean isEnabled) {
        addColButton.setEnabled(isEnabled);
        addRowButton.setEnabled(isEnabled);
        removeColButton.setEnabled(isEnabled);
        removeRowButton.setEnabled(isEnabled);
        tableLabel.setEnabled(isEnabled);
        table.setEnabled(isEnabled);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addColButton = new javax.swing.JButton();
        removeColButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();
        addRowButton = new javax.swing.JButton();

        tableLabel.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.tableLabel.text")); // NOI18N
        tableLabel.setEnabled(false);

        table.setModel(tableModel);
        table.setEnabled(false);
        jScrollPane1.setViewportView(table);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
        );

        addColButton.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.addColButton.text")); // NOI18N
        addColButton.setEnabled(false);
        addColButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColButtonActionPerformed(evt);
            }
        });

        removeColButton.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.removeColButton.text")); // NOI18N
        removeColButton.setEnabled(false);
        removeColButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColButtonActionPerformed(evt);
            }
        });

        removeRowButton.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.removeRowButton.text")); // NOI18N
        removeRowButton.setEnabled(false);
        removeRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRowButtonActionPerformed(evt);
            }
        });

        addRowButton.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.addRowButton.text")); // NOI18N
        addRowButton.setEnabled(false);
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableLabel)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(addColButton)
                    .add(removeColButton, 0, 0, Short.MAX_VALUE)
                    .add(removeRowButton)
                    .add(addRowButton)))
        );

        layout.linkSize(new java.awt.Component[] {addColButton, addRowButton, removeColButton, removeRowButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addColButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeColButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addRowButton)
                        .addContainerGap())
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColButtonActionPerformed
        tableModel.addColumn(null);
    }//GEN-LAST:event_addColButtonActionPerformed

    private void removeColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeColButtonActionPerformed
        tableModel.removeLastColumn();
    }//GEN-LAST:event_removeColButtonActionPerformed

    private void removeRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowButtonActionPerformed
        int rowCount = table.getRowCount();
        if (rowCount > 0) {
            tableModel.removeRow(rowCount - 1);
        }
    }//GEN-LAST:event_removeRowButtonActionPerformed

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed
        tableModel.addRow(new String[table.getColumnCount()]);
    }//GEN-LAST:event_addRowButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addColButton;
    private javax.swing.JButton addRowButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeColButton;
    private javax.swing.JButton removeRowButton;
    private javax.swing.JTable table;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
    
}
