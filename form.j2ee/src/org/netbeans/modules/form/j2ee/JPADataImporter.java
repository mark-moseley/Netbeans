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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.j2ee;

import java.awt.Dialog;
import java.sql.Connection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.DataImporter;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormJavaSource;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Importer of list of JPA entities.
 *
 * @author Jan Stola
 */
public class JPADataImporter extends JPanel implements DataImporter {
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        importerLabel = new javax.swing.JLabel();
        connectionLabel = new javax.swing.JLabel();
        tableLabel = new javax.swing.JLabel();
        tableCombo = new javax.swing.JComboBox();
        connectionCombo = new javax.swing.JComboBox();

        importerLabel.setText(org.openide.util.NbBundle.getMessage(JPADataImporter.class, "MSG_ImportData")); // NOI18N

        connectionLabel.setText(org.openide.util.NbBundle.getMessage(JPADataImporter.class, "LBL_ImportDBConnection")); // NOI18N

        tableLabel.setText(org.openide.util.NbBundle.getMessage(JPADataImporter.class, "LBL_ImportDBTable")); // NOI18N

        tableCombo.setEnabled(false);
        tableCombo.setRenderer(J2EEUtils.DBColumnInfo.getRenderer());

        connectionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(importerLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(connectionLabel)
                            .add(tableLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tableCombo, 0, 238, Short.MAX_VALUE)
                            .add(connectionCombo, 0, 238, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(importerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(connectionLabel)
                    .add(connectionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableLabel)
                    .add(tableCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void connectionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionComboActionPerformed
    Object selItem = connectionCombo.getSelectedItem();
    if (selItem instanceof DatabaseConnection) {
        DatabaseConnection connection = (DatabaseConnection)selItem;
        Connection con = J2EEUtils.establishConnection(connection);
        if (con == null) return; // User canceled the connection dialog
        fillTableCombo(connection);
    } else {
        assert (selItem == null);
    }
}//GEN-LAST:event_connectionComboActionPerformed

    /**
     * Fills the content of <code>tableCombo</code>.
     *
     * @param connection database connection.
     */
    private void fillTableCombo(DatabaseConnection connection) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (J2EEUtils.DBColumnInfo table : J2EEUtils.tableNamesForConnection(connection)) {
            model.addElement(table);
        }
        tableCombo.setModel(model);
        tableCombo.setEnabled(tableCombo.getModel().getSize() != 0);
        tableCombo.setSelectedItem(tableCombo.getSelectedItem());
    }

    /**
     * Imports list of JPA entities that correspond to selected DB table.
     * 
     * @param formModel form to import the data into.
     * @return the component encapsulating the imported data.
     */
    public RADComponent importData(FormModel formModel) {
        removeAll();
        if (FormJavaSource.isInDefaultPackage(formModel)) {
            // 97982: default package
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(getClass(), "MSG_ImportToDefaultPackage"))); // NOI18N
            return null;
        }
        initComponents();
        DatabaseExplorerUIs.connect(connectionCombo, ConnectionManager.getDefault());
        DialogDescriptor dd = new DialogDescriptor(
                this,
                NbBundle.getMessage(getClass(), "TITLE_ImportData"), // NOI18N
                true,
                null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() != DialogDescriptor.OK_OPTION) return null;
        RADComponent resultList = null;
        try {
            J2EEUtils.DBColumnInfo table = (J2EEUtils.DBColumnInfo)tableCombo.getSelectedItem();
            if ((table == null) || !table.isValid()) return null;
            String tableName = table.getName();
            DatabaseConnection connection = (DatabaseConnection)connectionCombo.getSelectedItem();
            FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
            Project project = FileOwnerQuery.getOwner(formFile);

            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);

            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, connection);

            // Initializes project's classpath
            JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(connection.getDriverClass());
            J2EEUtils.updateProjectForUnit(persistenceXML, unit, driver[0]);

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(formFile);
            MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());

            // Find entity that corresponds to the dragged table
            String[] entityInfo = J2EEUtils.findEntity(mappings, tableName);

            // Create a new entity (if there isn't one that corresponds to the dragged table)
            if (entityInfo == null) {
                // Generates a Java class for the entity
                J2EEUtils.createEntity(formFile.getParent(), scope, unit, connection, tableName, null);

                mappings = scope.getEntityMappingsModel(unit.getName());
                entityInfo = J2EEUtils.findEntity(mappings, tableName);
            }

            String puName = unit.getName();
            RADComponent entityManager = J2EEUtils.findEntityManager(formModel, puName);
            if (entityManager == null) {
                entityManager = J2EEUtils.createEntityManager(formModel, puName);
            }
            RADComponent queryBean = DBTableDrop.createQueryBean(formModel, entityManager, entityInfo[0]);
            resultList = DBTableDrop.createResultListBean(formModel, queryBean, entityInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultList;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connectionCombo;
    private javax.swing.JLabel connectionLabel;
    private javax.swing.JLabel importerLabel;
    private javax.swing.JComboBox tableCombo;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
    
}
