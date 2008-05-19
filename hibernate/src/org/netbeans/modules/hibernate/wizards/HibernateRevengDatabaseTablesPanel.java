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
package org.netbeans.modules.hibernate.wizards;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.wizards.support.Table;
import org.netbeans.modules.hibernate.wizards.support.TableClosure;
import org.netbeans.modules.hibernate.wizards.support.TableProvider;
import org.netbeans.modules.hibernate.wizards.support.TableUISupport;
import org.netbeans.modules.hibernate.wizards.support.DBSchemaManager;
import org.netbeans.modules.hibernate.wizards.support.DBSchemaTableProvider;
import org.netbeans.modules.hibernate.wizards.support.EmptyTableProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateRevengDatabaseTablesPanel extends javax.swing.JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final DBSchemaManager dbschemaManager = new DBSchemaManager();
    private DatabaseConnection dbconn;
    private boolean sourceSchemaUpdateEnabled;
    private Project project;
    org.netbeans.modules.hibernate.service.HibernateEnvironment env;
    ArrayList<FileObject> configFileObjects;
    List<String> databaseTables;
    private TableClosure tableClosure;
    private SchemaElement sourceSchemaElement;

    public HibernateRevengDatabaseTablesPanel(Project project) {
        initComponents();
        this.project = project;

        ListSelectionListener selectionListener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        };
        availableTablesList.getSelectionModel().addListSelectionListener(selectionListener);
        selectedTablesList.getSelectionModel().addListSelectionListener(selectionListener);
    }

    public void initialize(Project project) {
        this.project = project;
        fillConfiguration();

        sourceSchemaUpdateEnabled = true;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                fillDatabaseTables();
            }
        });
    }

    private void fillConfiguration() {
        env = project.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
        String[] configFiles = getConfigFilesFromProject(project);
        this.cmbDatabaseConn.setModel(new DefaultComboBoxModel(configFiles));
    }

    // Gets the list of Config files from HibernateEnvironment.
    public String[] getConfigFilesFromProject(Project project) {
        ArrayList<String> configFiles = new ArrayList<String>();
        configFileObjects = env.getAllHibernateConfigFileObjects();
        for (FileObject fo : configFileObjects) {
            configFiles.add(fo.getNameExt());
        }
        return configFiles.toArray(new String[]{});
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }


    // This method updates the AvailableList with a set of tables 
    // based on the configuration file selection.
    private void fillDatabaseTables() {
        if (!sourceSchemaUpdateEnabled) {
            return;
        }

        TableProvider tableProvider = null;
        sourceSchemaElement = null;
        dbconn = null;
        Object item = cmbDatabaseConn.getSelectedItem();

        HibernateConfiguration hibConf = null;
        try {
            hibConf = ((HibernateCfgDataObject) DataObject.find(configFileObjects.get(cmbDatabaseConn.getSelectedIndex()))).getHibernateConfiguration();
            dbconn = HibernateUtil.getDBConnection(hibConf);
            if (dbconn != null) {
                sourceSchemaElement = dbschemaManager.getSchemaElement(dbconn);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DatabaseException e) {
            Exceptions.printStackTrace(e);
        } catch (SQLException e) {
            notify("Error");
        }

        if (sourceSchemaElement != null) {
            tableProvider = new DBSchemaTableProvider(sourceSchemaElement);
        } else {
            tableProvider = new EmptyTableProvider();
        }

        tableClosure = new TableClosure(tableProvider);
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());

        TableUISupport.connectAvailable(availableTablesList, tableClosure);
        TableUISupport.connectSelected(selectedTablesList, tableClosure);

        updateButtons();
        changeSupport.fireChange();
    }

    private void updateButtons() {
        Set<Table> addTables = TableUISupport.getSelectedTables(availableTablesList);
        addButton.setEnabled(tableClosure.canAddAllTables(addTables));

        addAllButton.setEnabled(tableClosure.canAddSomeTables(tableClosure.getAvailableTables()));

        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        removeButton.setEnabled(tableClosure.canRemoveAllTables(tables));

        removeAllButton.setEnabled(tableClosure.getSelectedTables().size() > 0);
        tableError.setText("");
        for (Table t : addTables) {
            if (t.isDisabled()) {
                if (t.getDisabledReason() instanceof Table.ExistingDisabledReason) {
                    String existingClass = ((Table.ExistingDisabledReason) t.getDisabledReason()).getFQClassName();
                    tableError.setText(
                            NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "MSG_Already_Mapped", new Object[]{t.getName(), existingClass}));
                    break;

                } else if (t.getDisabledReason() instanceof Table.NoPrimaryKeyDisabledReason) {
                    tableError.setText(NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "MSG_No_Primary_Key", new Object[]{t.getName()}));
                    break;

                }
            }
        }
    }
    
    public FileObject getConfigurationFile() {
        if (cmbDatabaseConn.getSelectedIndex() != -1) {
            return configFileObjects.get(cmbDatabaseConn.getSelectedIndex());
        }
        return null;
    }

    public TableClosure getTableClosure() {
        return tableClosure;
    }

    private static void notify(String message) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        schemaSource = new javax.swing.ButtonGroup();
        cmbDatabaseConn = new javax.swing.JComboBox();
        tablesPanel = new TablesPanel();
        availableTablesLabel = new javax.swing.JLabel();
        availableTablesScrollPane = new javax.swing.JScrollPane();
        availableTablesList = TableUISupport.createTableList();
        selectedTablesLabel = new javax.swing.JLabel();
        selectedTablesScrollPane = new javax.swing.JScrollPane();
        selectedTablesList = TableUISupport.createTableList();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addAllButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        tableClosureCheckBox = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableError = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_DatabaseTables")); // NOI18N

        cmbDatabaseConn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDatabaseConnActionPerformed(evt);
            }
        });

        tablesPanel.setLayout(new java.awt.GridBagLayout());

        availableTablesLabel.setLabelFor(availableTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(availableTablesLabel, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_AvailableTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(availableTablesLabel, gridBagConstraints);
        availableTablesLabel.getAccessibleContext().setAccessibleName("");

        availableTablesList.setNextFocusableComponent(addButton);
        availableTablesScrollPane.setViewportView(availableTablesList);
        availableTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "ACSN_AvailableTables")); // NOI18N
        availableTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "ACSD_AvailableTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(availableTablesScrollPane, gridBagConstraints);

        selectedTablesLabel.setLabelFor(selectedTablesList);
        org.openide.awt.Mnemonics.setLocalizedText(selectedTablesLabel, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_SelectedTables")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        tablesPanel.add(selectedTablesLabel, gridBagConstraints);
        selectedTablesLabel.getAccessibleContext().setAccessibleName("");

        selectedTablesScrollPane.setViewportView(selectedTablesList);
        selectedTablesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "ACSN_SelectedTables")); // NOI18N
        selectedTablesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "ACSD_SelectedTables")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tablesPanel.add(selectedTablesScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_Remove")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_AddAll")); // NOI18N
        addAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        buttonPanel.add(addAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_RemoveAll")); // NOI18N
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(removeAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 11);
        tablesPanel.add(buttonPanel, gridBagConstraints);

        tableClosureCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tableClosureCheckBox, org.openide.util.NbBundle.getMessage(HibernateRevengDatabaseTablesPanel.class, "LBL_IncludeRelatedTables")); // NOI18N
        tableClosureCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableClosureCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tableClosureCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableClosureCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        tablesPanel.add(tableClosureCheckBox, gridBagConstraints);
        tableClosureCheckBox.getAccessibleContext().setAccessibleName("");

        jScrollPane3.setBorder(null);

        tableError.setEditable(false);
        tableError.setOpaque(false);
        jScrollPane3.setViewportView(tableError);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Configuration File:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmbDatabaseConn, 0, 385, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tablesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cmbDatabaseConn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 23, Short.MAX_VALUE)
                .add(tablesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 235, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableClosureCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableClosureCheckBoxItemStateChanged
        tableClosure.setClosureEnabled(tableClosureCheckBox.isSelected());
    }//GEN-LAST:event_tableClosureCheckBoxItemStateChanged

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        tableClosure.removeAllTables();
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        tableClosure.addAllTables();
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(selectedTablesList);
        tableClosure.removeTables(tables);
        selectedTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Set<Table> tables = TableUISupport.getSelectedTables(availableTablesList);
        tableClosure.addTables(tables);
        availableTablesList.clearSelection();
        updateButtons();

        changeSupport.fireChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void cmbDatabaseConnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDatabaseConnActionPerformed
        fillDatabaseTables();
}//GEN-LAST:event_cmbDatabaseConnActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton addButton;
    private javax.swing.JLabel availableTablesLabel;
    private javax.swing.JList availableTablesList;
    private javax.swing.JScrollPane availableTablesScrollPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JComboBox cmbDatabaseConn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.ButtonGroup schemaSource;
    private javax.swing.JLabel selectedTablesLabel;
    private javax.swing.JList selectedTablesList;
    private javax.swing.JScrollPane selectedTablesScrollPane;
    private javax.swing.JCheckBox tableClosureCheckBox;
    private javax.swing.JTextPane tableError;
    private javax.swing.JPanel tablesPanel;
    // End of variables declaration//GEN-END:variables

    private final class TablesPanel extends JPanel {

        @Override
        public void doLayout() {
            super.doLayout();

            Rectangle availableBounds = availableTablesScrollPane.getBounds();
            Rectangle selectedBounds = selectedTablesScrollPane.getBounds();

            if (Math.abs(availableBounds.width - selectedBounds.width) > 1) {
                GridBagConstraints buttonPanelConstraints = ((GridBagLayout) getLayout()).getConstraints(buttonPanel);
                int totalWidth = getWidth() - buttonPanel.getWidth() - buttonPanelConstraints.insets.left - buttonPanelConstraints.insets.right;
                int equalWidth = totalWidth / 2;
                int xOffset = equalWidth - availableBounds.width;

                availableBounds.width = equalWidth;
                availableTablesScrollPane.setBounds(availableBounds);

                Rectangle buttonBounds = buttonPanel.getBounds();
                buttonBounds.x += xOffset;
                buttonPanel.setBounds(buttonBounds);

                Rectangle labelBounds = selectedTablesLabel.getBounds();
                labelBounds.x += xOffset;
                selectedTablesLabel.setBounds(labelBounds);

                selectedBounds.x += xOffset;
                selectedBounds.width = totalWidth - equalWidth;
                selectedTablesScrollPane.setBounds(selectedBounds);

                Rectangle tableClosureBounds = tableClosureCheckBox.getBounds();
                tableClosureBounds.x += xOffset;
                tableClosureBounds.width = totalWidth - equalWidth;
                tableClosureCheckBox.setBounds(tableClosureBounds);
            }
        }
    }
}
