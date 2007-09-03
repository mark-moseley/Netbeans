package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.openide.util.NbBundle;

public final class ConnectionPoolOptionalVisualPanel extends JPanel {
    protected final CommonAttributePanel panel;
    protected ResourceConfigHelper helper;
    protected ResourceConfigData data;
        
    /** Creates new form ConnectionPoolVisualPanel3 */
    public ConnectionPoolOptionalVisualPanel(CommonAttributePanel panel, ResourceConfigHelper helper) {
        this.panel = panel;
        this.helper = helper;
        this.data = helper.getData();
        initComponents();
    }
    
    public String getName() {
        return NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "StepName_OptionalConnectionPool"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        poolSettingsPanel = new javax.swing.JPanel();
        steadyLabel = new javax.swing.JLabel();
        maxLabel = new javax.swing.JLabel();
        waitLabel = new javax.swing.JLabel();
        resizeLabel = new javax.swing.JLabel();
        idleLabel = new javax.swing.JLabel();
        steadyField = new javax.swing.JTextField();
        maxField = new javax.swing.JTextField();
        waitField = new javax.swing.JTextField();
        resizeField = new javax.swing.JTextField();
        idleField = new javax.swing.JTextField();
        transactionPanel = new javax.swing.JPanel();
        transactionLabel = new javax.swing.JLabel();
        isolationLabel = new javax.swing.JLabel();
        transactionCombo = new javax.swing.JComboBox();
        isolationCombo = new javax.swing.JComboBox();
        validationPanel = new javax.swing.JPanel();
        validationLabel = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();
        methodCombo = new javax.swing.JComboBox();
        tableNameLabel = new javax.swing.JLabel();
        tableNameField = new javax.swing.JTextField();
        failLabel = new javax.swing.JLabel();
        failCombo = new javax.swing.JComboBox();
        validationCombo = new javax.swing.JComboBox();

        poolSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_PoolSettings_Title"))); // NOI18N

        steadyLabel.setLabelFor(steadyField);
        org.openide.awt.Mnemonics.setLocalizedText(steadyLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_SteadyPoolSize")); // NOI18N

        maxLabel.setLabelFor(maxField);
        org.openide.awt.Mnemonics.setLocalizedText(maxLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_MaxPoolSize")); // NOI18N

        waitLabel.setLabelFor(waitField);
        org.openide.awt.Mnemonics.setLocalizedText(waitLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_MaxWaitTime")); // NOI18N

        resizeLabel.setLabelFor(resizeField);
        org.openide.awt.Mnemonics.setLocalizedText(resizeLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_PoolResize")); // NOI18N

        idleLabel.setLabelFor(idleField);
        org.openide.awt.Mnemonics.setLocalizedText(idleLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_IdleTime")); // NOI18N

        steadyField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                steadyFieldKeyReleased(evt);
            }
        });

        maxField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFieldKeyReleased(evt);
            }
        });

        waitField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                waitFieldKeyReleased(evt);
            }
        });

        resizeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resizeFieldKeyReleased(evt);
            }
        });

        idleField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                idleFieldKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout poolSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(poolSettingsPanel);
        poolSettingsPanel.setLayout(poolSettingsPanelLayout);
        poolSettingsPanelLayout.setHorizontalGroup(
            poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(poolSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(steadyLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(waitLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resizeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(idleLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, maxField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, resizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, idleField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, waitField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(steadyField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                .addContainerGap())
        );
        poolSettingsPanelLayout.setVerticalGroup(
            poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(poolSettingsPanelLayout.createSequentialGroup()
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(steadyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(steadyLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(waitField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(waitLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resizeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(idleField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(idleLabel))
                .addContainerGap())
        );

        transactionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TranIsol_Title"))); // NOI18N

        transactionLabel.setLabelFor(transactionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(transactionLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TranIsolation")); // NOI18N

        isolationLabel.setLabelFor(isolationCombo);
        org.openide.awt.Mnemonics.setLocalizedText(isolationLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_Guarantee")); // NOI18N

        transactionCombo.setModel(getComboBoxModel("transaction-isolation-level"));
        transactionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transactionComboActionPerformed(evt);
            }
        });

        isolationCombo.setModel(getComboBoxModel("is-isolation-level-guaranteed"));
        isolationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isolationComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout transactionPanelLayout = new org.jdesktop.layout.GroupLayout(transactionPanel);
        transactionPanel.setLayout(transactionPanelLayout);
        transactionPanelLayout.setHorizontalGroup(
            transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(transactionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(isolationLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transactionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, isolationCombo, 0, 252, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, transactionCombo, 0, 252, Short.MAX_VALUE))
                .addContainerGap())
        );
        transactionPanelLayout.setVerticalGroup(
            transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(transactionPanelLayout.createSequentialGroup()
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transactionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transactionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(isolationCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(isolationLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        validationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_ConnValid_Title"))); // NOI18N

        validationLabel.setLabelFor(validationCombo);
        org.openide.awt.Mnemonics.setLocalizedText(validationLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_ConnValidationReq")); // NOI18N

        methodLabel.setLabelFor(methodCombo);
        org.openide.awt.Mnemonics.setLocalizedText(methodLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_ValidationMethod")); // NOI18N

        methodCombo.setModel(getComboBoxModel("connection-validation-method"));
        methodCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodComboActionPerformed(evt);
            }
        });

        tableNameLabel.setLabelFor(tableNameField);
        org.openide.awt.Mnemonics.setLocalizedText(tableNameLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TableName")); // NOI18N

        tableNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableNameFieldKeyReleased(evt);
            }
        });

        failLabel.setLabelFor(failCombo);
        org.openide.awt.Mnemonics.setLocalizedText(failLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_FailAll")); // NOI18N

        failCombo.setModel(getComboBoxModel("fail-all-connections"));
        failCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                failComboActionPerformed(evt);
            }
        });

        validationCombo.setModel(getComboBoxModel("is-connection-validation-required"));
        validationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validationComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout validationPanelLayout = new org.jdesktop.layout.GroupLayout(validationPanel);
        validationPanel.setLayout(validationPanelLayout);
        validationPanelLayout.setHorizontalGroup(
            validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationPanelLayout.createSequentialGroup()
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, validationPanelLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(validationLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(validationPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(methodLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(validationPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(tableNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(validationPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(failLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(methodCombo, 0, 209, Short.MAX_VALUE)
                    .add(tableNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .add(failCombo, 0, 209, Short.MAX_VALUE)
                    .add(validationCombo, 0, 209, Short.MAX_VALUE))
                .addContainerGap())
        );
        validationPanelLayout.setVerticalGroup(
            validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationPanelLayout.createSequentialGroup()
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(validationCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(methodLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tableNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(failCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(failLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, validationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, transactionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, poolSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(poolSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void steadyFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_steadyFieldKeyReleased
        String itemValue = steadyField.getText();
        updateFieldValue("steady-pool-size", itemValue);
    }//GEN-LAST:event_steadyFieldKeyReleased

    private void maxFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxFieldKeyReleased
        String itemValue = maxField.getText();
        updateFieldValue("max-pool-size", itemValue);
    }//GEN-LAST:event_maxFieldKeyReleased

    private void waitFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_waitFieldKeyReleased
        String itemValue = waitField.getText();
        updateFieldValue("max-wait-time-in-millis", itemValue);
    }//GEN-LAST:event_waitFieldKeyReleased

    private void resizeFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resizeFieldKeyReleased
        String itemValue = resizeField.getText();
        updateFieldValue("pool-resize-quantity", itemValue);
    }//GEN-LAST:event_resizeFieldKeyReleased

    private void idleFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idleFieldKeyReleased
        String itemValue = idleField.getText();
        updateFieldValue("idle-timeout-in-seconds", itemValue);
    }//GEN-LAST:event_idleFieldKeyReleased

    private void tableNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableNameFieldKeyReleased
        String itemValue = tableNameField.getText();
        updateFieldValue("validation-table-name", itemValue);
    }//GEN-LAST:event_tableNameFieldKeyReleased

    private void transactionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transactionComboActionPerformed
        String itemValue = (String)transactionCombo.getSelectedItem();
        updateFieldValue("transaction-isolation-level", itemValue);
    }//GEN-LAST:event_transactionComboActionPerformed

    private void isolationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isolationComboActionPerformed
        String itemValue = (String)isolationCombo.getSelectedItem();
        updateFieldValue("is-isolation-level-guaranteed", itemValue);
    }//GEN-LAST:event_isolationComboActionPerformed

    private void validationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validationComboActionPerformed
        String itemValue = (String)validationCombo.getSelectedItem();
        updateFieldValue("is-connection-validation-required", itemValue);
}//GEN-LAST:event_validationComboActionPerformed

    private void methodComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodComboActionPerformed
        String itemValue = (String)methodCombo.getSelectedItem();
        updateFieldValue("connection-validation-method", itemValue);
    }//GEN-LAST:event_methodComboActionPerformed

    private void failComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_failComboActionPerformed
        String itemValue = (String)failCombo.getSelectedItem();
        updateFieldValue("fail-all-connections", itemValue);
    }//GEN-LAST:event_failComboActionPerformed
    
    private void updateFieldValue(String fieldName, String itemValue){
        String val = data.getString(fieldName);
        if (!itemValue.equals(val)) { 
            data.setString(fieldName, itemValue);
        }
        panel.fireChange(this);
    }
    
    private ComboBoxModel getComboBoxModel(String fieldName) {
        ComboBoxModel model = new javax.swing.DefaultComboBoxModel(new String[] {});
        FieldGroup group1 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting-2");
        FieldGroup group2 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting-3");
        Field field = FieldHelper.getField(group1, fieldName);
        if(field == null){
            field = FieldHelper.getField(group2, fieldName);
        }
        if (field != null) {
            String[] tags = FieldHelper.getTags(field);
            model = new javax.swing.DefaultComboBoxModel(tags);
        }
        return model;
    }
    public boolean isNewResourceSelected() {
        return false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox failCombo;
    private javax.swing.JLabel failLabel;
    private javax.swing.JTextField idleField;
    private javax.swing.JLabel idleLabel;
    private javax.swing.JComboBox isolationCombo;
    private javax.swing.JLabel isolationLabel;
    private javax.swing.JTextField maxField;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JComboBox methodCombo;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JPanel poolSettingsPanel;
    private javax.swing.JTextField resizeField;
    private javax.swing.JLabel resizeLabel;
    private javax.swing.JTextField steadyField;
    private javax.swing.JLabel steadyLabel;
    private javax.swing.JTextField tableNameField;
    private javax.swing.JLabel tableNameLabel;
    private javax.swing.JComboBox transactionCombo;
    private javax.swing.JLabel transactionLabel;
    private javax.swing.JPanel transactionPanel;
    private javax.swing.JComboBox validationCombo;
    private javax.swing.JLabel validationLabel;
    private javax.swing.JPanel validationPanel;
    private javax.swing.JTextField waitField;
    private javax.swing.JLabel waitLabel;
    // End of variables declaration//GEN-END:variables
    
}

