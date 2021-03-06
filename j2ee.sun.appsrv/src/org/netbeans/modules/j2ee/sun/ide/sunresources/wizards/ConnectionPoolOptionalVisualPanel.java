package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        poolSettingsPanel = new javax.swing.JPanel();
        steadyLabel = new javax.swing.JLabel();
        maxLabel = new javax.swing.JLabel();
        waitLabel = new javax.swing.JLabel();
        resizeLabel = new javax.swing.JLabel();
        idleLabel = new javax.swing.JLabel();
        steadyField = new javax.swing.JTextField(Util.getNumericDocument(), null, 0);
        maxField = new javax.swing.JTextField(Util.getNumericDocument(), null, 0);
        waitField = new javax.swing.JTextField(Util.getNumericDocument(), null, 0);
        resizeField = new javax.swing.JTextField(Util.getNumericDocument(), null, 0);
        idleField = new javax.swing.JTextField(Util.getNumericDocument(), null, 0);
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
        nonTransactionalLabel = new javax.swing.JLabel();
        allowCallersLabel = new javax.swing.JLabel();
        nonTransactionalCombo = new javax.swing.JComboBox();
        allowCallersCombo = new javax.swing.JComboBox();

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

        steadyField.setText(getDefaultValue("steady-pool-size"));
        steadyField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                steadyFieldKeyReleased(evt);
            }
        });

        maxField.setText(getDefaultValue("max-pool-size"));
        maxField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxFieldKeyReleased(evt);
            }
        });

        waitField.setText(getDefaultValue("max-wait-time-in-millis"));
        waitField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                waitFieldKeyReleased(evt);
            }
        });

        resizeField.setText(getDefaultValue("pool-resize-quantity"));
        resizeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resizeFieldKeyReleased(evt);
            }
        });

        idleField.setText(getDefaultValue("idle-timeout-in-seconds"));
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
                    .add(steadyLabel)
                    .add(maxLabel)
                    .add(waitLabel)
                    .add(resizeLabel)
                    .add(idleLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, maxField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, resizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, idleField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, waitField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .add(steadyField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
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

        steadyLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_steady-pool-size")); // NOI18N
        maxLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_max-pool-size")); // NOI18N
        waitLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_max-wait-time-in-millis")); // NOI18N
        resizeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_pool-resize-quantity")); // NOI18N
        idleLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_idle-timeout-in-seconds")); // NOI18N

        transactionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TranIsol_Title"))); // NOI18N

        transactionLabel.setLabelFor(transactionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(transactionLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TranIsolation")); // NOI18N

        isolationLabel.setLabelFor(isolationCombo);
        org.openide.awt.Mnemonics.setLocalizedText(isolationLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_Guarantee")); // NOI18N

        transactionCombo.setModel(getComboBoxModel("transaction-isolation-level"));
        transactionCombo.setSelectedItem(getDefaultValue("transaction-isolation-level"));
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
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(isolationLabel)
                    .add(transactionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, isolationCombo, 0, 258, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, transactionCombo, 0, 258, Short.MAX_VALUE))
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

        transactionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_transaction-isolation-level")); // NOI18N
        isolationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_is-isolation-level-guaranteed")); // NOI18N

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

        tableNameField.setText(getDefaultValue("validation-table-name"));
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

        nonTransactionalLabel.setLabelFor(nonTransactionalCombo);
        org.openide.awt.Mnemonics.setLocalizedText(nonTransactionalLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_NonTransactional")); // NOI18N

        allowCallersLabel.setLabelFor(allowCallersCombo);
        org.openide.awt.Mnemonics.setLocalizedText(allowCallersLabel, org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_AllowCallers")); // NOI18N

        nonTransactionalCombo.setModel(getComboBoxModel("non-transactional-connections"));
        nonTransactionalCombo.setSelectedItem(getDefaultValue("nonTransactionalCombo"));
        nonTransactionalCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nonTransactionalComboActionPerformed(evt);
            }
        });

        allowCallersCombo.setModel(getComboBoxModel("allow-non-component-callers"));
        allowCallersCombo.setSelectedItem(getDefaultValue("allow-non-component-callers"));
        allowCallersCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allowCallersComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout validationPanelLayout = new org.jdesktop.layout.GroupLayout(validationPanel);
        validationPanel.setLayout(validationPanelLayout);
        validationPanelLayout.setHorizontalGroup(
            validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(validationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(allowCallersLabel)
                    .add(methodLabel)
                    .add(tableNameLabel)
                    .add(failLabel)
                    .add(nonTransactionalLabel)
                    .add(validationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(failCombo, 0, 232, Short.MAX_VALUE)
                    .add(methodCombo, 0, 232, Short.MAX_VALUE)
                    .add(tableNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                    .add(validationCombo, 0, 232, Short.MAX_VALUE)
                    .add(nonTransactionalCombo, 0, 232, Short.MAX_VALUE)
                    .add(allowCallersCombo, 0, 232, Short.MAX_VALUE))
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
                    .add(methodLabel)
                    .add(methodCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tableNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(failLabel)
                    .add(failCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nonTransactionalLabel)
                    .add(nonTransactionalCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(allowCallersLabel)
                    .add(allowCallersCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        validationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_is-connection-validation-required")); // NOI18N
        methodLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_connection-validation-method")); // NOI18N
        tableNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_validation-table-name")); // NOI18N
        failLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_fail-all-connections")); // NOI18N
        nonTransactionalLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_NonTransactional")); // NOI18N
        nonTransactionalLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_non-transactional-connections")); // NOI18N
        allowCallersLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_AllowCallers")); // NOI18N
        allowCallersLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ToolTip_allow-non-component-callers")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(poolSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(transactionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(validationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(poolSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transactionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(validationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        poolSettingsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_PoolSettings_Title")); // NOI18N
        poolSettingsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ACSD_LBL_PoolSettings_Title")); // NOI18N
        transactionPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_TranIsol_Title")); // NOI18N
        transactionPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ACSD_LBL_TranIsol_Title")); // NOI18N
        validationPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "LBL_ConnValid_Title")); // NOI18N
        validationPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ACSD_LBL_ConnValid_Title")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "StepName_OptionalConnectionPool")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionPoolOptionalVisualPanel.class, "ACSD_OptionalConnectionPool_panel")); // NOI18N
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

    private void allowCallersComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allowCallersComboActionPerformed
        String itemValue = (String)allowCallersCombo.getSelectedItem();
        updateFieldValue("allow-non-component-callers", itemValue);
    }//GEN-LAST:event_allowCallersComboActionPerformed

    private void nonTransactionalComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nonTransactionalComboActionPerformed
        String itemValue = (String)nonTransactionalCombo.getSelectedItem();
        updateFieldValue("non-transactional-connections", itemValue);
    }//GEN-LAST:event_nonTransactionalComboActionPerformed
    
    private void updateFieldValue(String fieldName, String itemValue){
        String val = data.getString(fieldName);
        if (!itemValue.equals(val)) { 
            data.setString(fieldName, itemValue);
        }
        panel.fireChange(this);
    }

    private ComboBoxModel getComboBoxModel(String fieldName) {
        ComboBoxModel model = new javax.swing.DefaultComboBoxModel(new String[] {});
        FieldGroup group1 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting-2"); //NOI18N
        FieldGroup group2 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting-3"); //NOI18N
        Field field = FieldHelper.getField(group1, fieldName);
        if(field == null){
            field = FieldHelper.getField(group2, fieldName);
        }
        if (field != null) {
            String[] tags = FieldHelper.getTags(field);
            if(fieldName.equals("transaction-isolation-level")){ //NOI18N
                String[] updateTags = updateTags(tags);
                model = new javax.swing.DefaultComboBoxModel(updateTags);
            }else{
                model = new javax.swing.DefaultComboBoxModel(tags);
            }   
        }
        return model;
    }

    private String[] updateTags(String[] tags){
        int size = tags.length;
        String[] updatedTags = new String[size + 1];
        for(int i=0; i<size; i++){
            updatedTags[i] = tags[i];
        }
        updatedTags[size] = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default"); //NOI18N
        return updatedTags;
    }
    
    private String getDefaultValue(String fieldName) {
        String value = ""; //NOI18N
        FieldGroup group1 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting"); //NOI18N
        FieldGroup group2 = FieldGroupHelper.getFieldGroup(this.panel.getWizard(), "pool-setting-3"); //NOI18N
        Field field = FieldHelper.getField(group1, fieldName);
        if(field == null){
            field = FieldHelper.getField(group2, fieldName);
        }
        if (field != null) {
            value = FieldHelper.getDefaultValue(field);
        }
        if (fieldName.equals("transaction-isolation-level")) { //NOI18N
            value = ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle").getString("LBL_driver_default"); //NOI18N
        }
        return value;
    }
    
    public boolean isNewResourceSelected() {
        return false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox allowCallersCombo;
    private javax.swing.JLabel allowCallersLabel;
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
    private javax.swing.JComboBox nonTransactionalCombo;
    private javax.swing.JLabel nonTransactionalLabel;
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

