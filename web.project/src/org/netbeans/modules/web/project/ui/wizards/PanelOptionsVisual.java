/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class PanelOptionsVisual extends javax.swing.JPanel {
    
    private PanelConfigureProject panel;
    private String sourceStructure;
    private String j2eeLevel;
    private boolean contextModified = false;
    private List serverInstanceIDs;
    
    private J2eeVersionWarningPanel warningPanel;
    
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(PanelOptionsVisual.class, "J2EESpecLevel_13"); //NOI18N
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(PanelOptionsVisual.class, "J2EESpecLevel_14"); //NOI18N

    private List earProjects;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual(PanelConfigureProject panel) {
        initComponents();
        setJ2eeVersionWarningPanel();
        this.panel = panel;
        initServerInstances();
        initJSrcStructureSpecs();
        initEnterpriseApplications();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        srcStructLabel = new javax.swing.JLabel();
        srcStructComboBox = new javax.swing.JComboBox();
        jLabelEnterprise = new javax.swing.JLabel();
        jComboBoxEnterprise = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        warningPlaceHolderPanel = new javax.swing.JPanel();
        setAsMainCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        srcStructLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SourceStructure_CheckBoxMnemonic").charAt(0));
        srcStructLabel.setLabelFor(srcStructComboBox);
        srcStructLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SourceStructure_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(srcStructLabel, gridBagConstraints);
        srcStructLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_SourceStructureComboBox_A11YDesc"));

        srcStructComboBox.setMinimumSize(new java.awt.Dimension(160, 18));
        srcStructComboBox.setPreferredSize(new java.awt.Dimension(160, 18));
        srcStructComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                srcStructComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(srcStructComboBox, gridBagConstraints);

        jLabelEnterprise.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_NWP1_AddToEnterprise_LabelMnemonic").charAt(0));
        jLabelEnterprise.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEnterprise_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelEnterprise, gridBagConstraints);
        jLabelEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jComboBoxEnterprise, gridBagConstraints);
        jComboBoxEnterprise.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_AddToEnterpriseComboBox_A11YDesc"));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        serverInstanceLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server_LabelMnemonic").charAt(0));
        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        serverInstanceLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_NWP1_Server_ComboBox_A11YDesc"));

        j2eeSpecLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_J2EESpecLevel_CheckBoxMnemonic").charAt(0));
        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        j2eeSpecLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_J2EESpecLevel_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(j2eeSpecLabel, gridBagConstraints);

        j2eeSpecComboBox.setMinimumSize(new java.awt.Dimension(100, 18));
        j2eeSpecComboBox.setPreferredSize(new java.awt.Dimension(100, 18));
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NPW1_J2EESpecLevel_A11YDesc"));

        jLabelContextPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_ContextPath_CheckBoxMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_ContextPath_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_ContextPath_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jPanel1, gridBagConstraints);

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(warningPlaceHolderPanel, gridBagConstraints);

        setAsMainCheckBox.setMnemonic(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBoxMnemonic").charAt(0));
        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_SetAsMain_CheckBox"));
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 0);
        add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACS_LBL_NWP1_SetAsMain_A11YDesc"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void serverInstanceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverInstanceComboBoxActionPerformed
        String prevSelectedItem = (String)j2eeSpecComboBox.getSelectedItem();
        String servInsID = (String)serverInstanceIDs.get(serverInstanceComboBox.getSelectedIndex());
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(servInsID);
        Set supportedVersions = j2eePlatform.getSupportedSpecVersions();
        j2eeSpecComboBox.removeAllItems();
        if (supportedVersions.contains(J2eeModule.J2EE_14)) j2eeSpecComboBox.addItem(J2EE_SPEC_14_LABEL);
        if (supportedVersions.contains(J2eeModule.J2EE_13)) j2eeSpecComboBox.addItem(J2EE_SPEC_13_LABEL);
        if (prevSelectedItem != null) {
            j2eeSpecComboBox.setSelectedItem(prevSelectedItem);
        }
    }//GEN-LAST:event_serverInstanceComboBoxActionPerformed

    private void srcStructComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_srcStructComboBoxActionPerformed
        switch (srcStructComboBox.getSelectedIndex()) {
            case 0: sourceStructure = WebProjectUtilities.SRC_STRUCT_BLUEPRINTS;
                    break;
            case 1: sourceStructure = WebProjectUtilities.SRC_STRUCT_JAKARTA;
        }
    }//GEN-LAST:event_srcStructComboBoxActionPerformed

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (getSelectedServer() == null) {
            String errMsg = NbBundle.getMessage(PanelOptionsVisual.class, "MSG_NoServer");
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", errMsg); // NOI18N
            return false;
        }
        return true;
    }

    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        d.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServer());
        d.putProperty(WizardProperties.SOURCE_STRUCTURE, sourceStructure);
        d.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        d.putProperty(WizardProperties.CONTEXT_PATH, jTextFieldContextPath.getText().trim());
        d.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getJava14PlatformName());
            d.putProperty(WizardProperties.SOURCE_LEVEL, "1.4"); // NOI18N
        }
    }
    
    void read(WizardDescriptor d) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JComboBox jComboBoxEnterprise;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelEnterprise;
    private javax.swing.JPanel jPanel1;
    protected javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JComboBox srcStructComboBox;
    private javax.swing.JLabel srcStructLabel;
    private javax.swing.JPanel warningPlaceHolderPanel;
    // End of variables declaration//GEN-END:variables

    private void initServerInstances() {
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        serverInstanceIDs = new ArrayList();
        for (int i = 0; i < servInstIDs.length; i++) {
            J2eePlatform j2eePlat = Deployment.getDefault().getJ2eePlatform(servInstIDs[i]);
            if (j2eePlat != null && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.WAR)) {
                serverInstanceIDs.add(servInstIDs[i]);
                serverInstanceComboBox.addItem(Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]));
            }
        }
        if (serverInstanceIDs.size() > 0) {
            serverInstanceComboBox.setSelectedIndex(0);
        } else {
            serverInstanceComboBox.setEnabled(false);
            j2eeSpecComboBox.setEnabled(false);
        }
    }
    
    private String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        return item == null ? null
                            : item.equals(J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14 : J2eeModule.J2EE_13;
    }
    
    private String getSelectedServer() {
        int idx = serverInstanceComboBox.getSelectedIndex();
        return idx == -1 ? null 
                         : (String)serverInstanceIDs.get(idx);
    }
    
    private void initJSrcStructureSpecs() {
        srcStructComboBox.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "SourceStructure_0")); //NOI18N
        srcStructComboBox.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "SourceStructure_1")); //NOI18N
        srcStructComboBox.setSelectedIndex(0);
    }    

    protected boolean isContextModified() {
         return contextModified;
    }

    private Project getSelectedEarApplication() {
        int idx = jComboBoxEnterprise.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        jComboBoxEnterprise.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEnterprise_None")); // TODO: AB: add to bundle
        jComboBoxEnterprise.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeModuleContainer container = (J2eeModuleContainer) allProjects[i].getLookup().lookup(J2eeModuleContainer.class);
            ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
            if (container != null) {
                earProjects.add(projectInfo.getProject());
                jComboBoxEnterprise.addItem(projectInfo.getDisplayName());
            }
        }
        if (earProjects.size() <= 0) {
            jComboBoxEnterprise.setEnabled(false);
        }
    }
    
    private void setJ2eeVersionWarningPanel() {
        String warningType = J2eeVersionWarningPanel.findWarningType();
        if (warningType == null)
            return;
        
        warningPanel = new J2eeVersionWarningPanel(warningType);
        warningPlaceHolderPanel.add(warningPanel, java.awt.BorderLayout.CENTER);
    }
}
