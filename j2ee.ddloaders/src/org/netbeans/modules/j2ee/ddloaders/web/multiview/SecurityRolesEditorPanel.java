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


package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle;

/**
 * Editor panel for selecting security roles.
 *
 * @author  ptliu
 */
public class SecurityRolesEditorPanel extends javax.swing.JPanel {
    
    /** Creates new form SecurityRolesEditorPanel */
    public SecurityRolesEditorPanel(String [] allRoles, String[] selectedRoles) {
        initComponents();
        
        initTable(allRolesTable, getRemainingRoles(allRoles, selectedRoles),
                NbBundle.getMessage(SecurityConstraintPanel.class,"LBL_AllSecurityRoles"));
        initTable(selectedRolesTable, selectedRoles,
                NbBundle.getMessage(SecurityConstraintPanel.class,"LBL_AllSecurityRoles"));
    }
    
    public String[] getSelectedRoles() {
        DefaultTableModel model = (DefaultTableModel) selectedRolesTable.getModel();
        int rowCount = model.getRowCount();
        String[] selectedRoles = new String[rowCount];
        
        for (int i = 0; i < rowCount; i++) {
            selectedRoles[i] = (String) model.getValueAt(i, 0);
        }
        
        return selectedRoles;
    }
    
    private void initTable(JTable table, String[] data, String columnName) {
        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        model.addColumn(columnName);
         
        for (int i = 0; i < data.length; i++) {
            model.addRow(new Object[] {data[i]});
        }
        
        table.setModel(model);
    }
        
    private String[] getRemainingRoles(String[] allRoles, String[] selectedRoles) {
        ArrayList result = new ArrayList();
        
        for (int i = 0; i < allRoles.length; i++) {
            String roleName = allRoles[i];
            boolean found = false;
            
            for (int j = 0; j < selectedRoles.length; j++) {
                if (roleName.equals(selectedRoles[j])) {
                    found = true;
                    break;
                }
            }
            
            if (!found) result.add(roleName);
        }
        
        String[] remainingRoles = new String[result.size()];
        return (String[]) result.toArray(remainingRoles);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        allRolesTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedRolesTable = new javax.swing.JTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle"); // NOI18N
        addButton.setText(bundle.getString("LBL_AddSecurityRole")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText(bundle.getString("LBL_RemoveSecurityRole")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        allRolesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(allRolesTable);

        selectedRolesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(selectedRolesTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(85, 85, 85)
                        .add(addButton)
                        .add(35, 35, 35)
                        .add(removeButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
// TODO add your handling code here:
        int[] selectedRows = selectedRolesTable.getSelectedRows();
        DefaultTableModel allRolesTableModel = (DefaultTableModel) allRolesTable.getModel();
        DefaultTableModel selectedRolesTableModel = (DefaultTableModel) selectedRolesTable.getModel();
        
        // Get the list of selected role names
        for (int i = 0; i < selectedRows.length; i++) {
            String roleName = (String) selectedRolesTableModel.getValueAt(selectedRows[i], 0);
            allRolesTableModel.addRow(new Object[] {roleName});
        }
        
        // Now remove the selected rows from the allRolesTable
        for (int i = selectedRows.length-1; i >=0; i--) {
            selectedRolesTableModel.removeRow(selectedRows[i]);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
// TODO add your handling code here:
        int[] selectedRows = allRolesTable.getSelectedRows();
        DefaultTableModel allRolesTableModel = (DefaultTableModel) allRolesTable.getModel();
        DefaultTableModel selectedRolesTableModel = (DefaultTableModel) selectedRolesTable.getModel();
        
        // Get the list of selected role names
        for (int i = 0; i < selectedRows.length; i++) {
            String roleName = (String) allRolesTableModel.getValueAt(selectedRows[i], 0);
            selectedRolesTableModel.addRow(new Object[] {roleName});
        }
        
        // Now remove the selected rows from the allRolesTable
        for (int i = selectedRows.length-1; i >=0; i--) {
            allRolesTableModel.removeRow(selectedRows[i]);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTable allRolesTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeButton;
    private javax.swing.JTable selectedRolesTable;
    // End of variables declaration//GEN-END:variables
    
}
