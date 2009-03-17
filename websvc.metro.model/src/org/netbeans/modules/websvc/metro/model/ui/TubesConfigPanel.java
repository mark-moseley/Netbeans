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
package org.netbeans.modules.websvc.metro.model.ui;

import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class TubesConfigPanel extends JPanel {

    private Project project;
    private TubeFactoryList tubeList;
    private boolean isChanged;
    private boolean client;
    private boolean overrideDefaults;

    /** Creates new form TubesConfigPanel */
    public TubesConfigPanel(Project project, TubeFactoryList tubeList, boolean client, boolean overrideDefault) {
        this.project = project;
        this.client = client;
        this.tubeList = tubeList;
        this.overrideDefaults = overrideDefault;
        initComponents();
        populateValues();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn.addActionListener(new RemoveButtonActionListener());
        isChanged = false;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isOverride() {
        return overrideDefChBox.isSelected();
    }

    public List<String> getTubeList() {
        List<String> retList = new ArrayList<String>();
        for (int i=0; i < tubeTableModel.getRowCount(); i++) {
            retList.add((String)tubeTableModel.getValueAt(i, 0));
        }
        return retList;
    }

    private void populateValues() {

        overrideDefChBox.setSelected(overrideDefaults);

        List<TubeFactoryConfig> tubeFacConfigs = tubeList.getTubeFactoryConfigs();
        for (TubeFactoryConfig cfg : tubeFacConfigs) {
            tubeTableModel.addRow(new Object[]{cfg.getClassName()});
        }
        if (tubeTableModel.getRowCount() > 0) {
            ((ListSelectionModel) tubeTable.getSelectionModel()).setSelectionInterval(0, 0);
        }

        enableDisable();
    }

    class RemoveButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = tubeTable.getSelectedRows();
            Arrays.sort(selectedRows);
            if ((selectedRows == null) || (selectedRows.length <= 0)) {
                return;
            }
            String className = "";
            for (int i : selectedRows) {
                className += (String) tubeTableModel.getValueAt(i, 0) + ", \n";
            }
            if (confirmDeletion(className)) {
                for (int i = selectedRows.length-1; i >= 0; i--) {
                    tubeTableModel.removeRow(i);
                }
                int newSelectedRow = selectedRows[0] - 1;
                tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
                isChanged = true;
            }
        }

        private boolean confirmDeletion(String className) {
            NotifyDescriptor.Confirmation notifyDesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(TubesConfigPanel.class, "MSG_CONFIRM_DELETE", className), NbBundle.getMessage(TubesConfigPanel.class, "TTL_CONFIRM_DELETE"), NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return notifyDesc.getValue() == NotifyDescriptor.YES_OPTION;
        }
    }

    class AddButtonActionListener implements ActionListener {

        DialogDescriptor dlgDesc = null;

        public void actionPerformed(ActionEvent evt) {
            ClassDialog classDialog = new ClassDialog(project, null); //NOI18N
            classDialog.show();
            int newSelectedRow = 0;
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    tubeTableModel.addRow(new Object[]{selectedClass});
                    newSelectedRow = tubeTableModel.getRowCount() - 1;
                }
            }
            tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
            isChanged = true;
        }
    }

    class TubeTable extends JTable {
        public TubeTable() {
            JTableHeader header = getTableHeader();
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
            ListSelectionModel model = getSelectionModel();
            model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            model.addListSelectionListener(new TubeListSelectionListener());
        }

        @Override
        public Component prepareRenderer (final TableCellRenderer renderer, int row, int column) {
            Component comp = super.prepareRenderer (renderer, row, column);
            getTableHeader().setEnabled(isEnabled());
            comp.setEnabled (isEnabled ());
            return comp;
        }

    }

    class TubeListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = getSelectedRow();
                if (selectedRow == 0) {
                    upBtn.setEnabled(false);
                } else {
                    if (!upBtn.isEnabled()) {
                        upBtn.setEnabled(true);
                    }
                }
                if (selectedRow == tubeTableModel.getRowCount() - 1) {
                    downBtn.setEnabled(false);
                } else {
                    if (!downBtn.isEnabled()) {
                        downBtn.setEnabled(true);
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        upBtn = new javax.swing.JButton();
        downBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tubeTable = new TubeTable();
        overrideDefChBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(addBtn, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Add")); // NOI18N
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Remove")); // NOI18N
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upBtn, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Up")); // NOI18N
        upBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Up")); // NOI18N
        upBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downBtn, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Down")); // NOI18N
        downBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Down")); // NOI18N
        downBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownHandler(evt);
            }
        });

        tubeTableModel = new TubeTableModel(new String[]{NbBundle.getMessage(TubesProjectConfigPanel.class, "HEADING_TUBES")}, 0);
        tubeTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tubeTable.setModel(tubeTableModel);
        jScrollPane2.setViewportView(tubeTable);
        tubeTable.getAccessibleContext().setAccessibleName("null");
        tubeTable.getAccessibleContext().setAccessibleDescription("null");

        org.openide.awt.Mnemonics.setLocalizedText(overrideDefChBox, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_OverrideDefaults")); // NOI18N
        overrideDefChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideDefChBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(addBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .add(upBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .add(removeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .add(downBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
                    .add(overrideDefChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(overrideDefChBox)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(addBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeBtn)
                        .add(23, 23, 23)
                        .add(upBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(downBtn))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                .addContainerGap())
        );

        addBtn.getAccessibleContext().setAccessibleName("null");
    }// </editor-fold>//GEN-END:initComponents

private void moveUpHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpHandler
    int selectedRow = getSelectedRow();
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow - 1;
    tubeTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveUpHandler

private void moveDownHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownHandler
    int selectedRow = getSelectedRow();
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow + 1;
    tubeTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveDownHandler

private void overrideDefChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideDefChBoxActionPerformed
    isChanged = true;
    enableDisable();
}//GEN-LAST:event_overrideDefChBoxActionPerformed

    private void enableDisable() {
        boolean override = overrideDefChBox.isSelected();
        this.tubeTable.setEnabled(override);
        this.tubeTable.setFocusable(override);
        this.tubeTable.setOpaque(!override);
        this.addBtn.setEnabled(override);
        this.downBtn.setEnabled(override);
        this.jScrollPane2.setEnabled(override);
        this.removeBtn.setEnabled(override);
        this.upBtn.setEnabled(override);
    }

    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel) tubeTable.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        } else {
            return lsm.getMinSelectionIndex();
        }
    }

    class TubeTableModel extends DefaultTableModel {

        public TubeTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton downBtn;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox overrideDefChBox;
    private javax.swing.JButton removeBtn;
    private javax.swing.JTable tubeTable;
    private javax.swing.table.DefaultTableModel tubeTableModel;
    private javax.swing.JButton upBtn;
    // End of variables declaration//GEN-END:variables
}