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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModuleContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 *
 * @author  pb97924, Martin Adamek
 */
public class ImportLocationVisual extends javax.swing.JPanel /*implements DocumentListener */{
    
    private ImportLocation panel;
    private String buildfileName = GeneratedFilesHelper.BUILD_XML_PATH;
    private java.util.List serverInstanceIDs;
    private static final String J2EE_SPEC_14_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_14"); //NOI18N
    private static final String J2EE_SPEC_13_LABEL = NbBundle.getMessage(ImportLocationVisual.class, "J2EESpecLevel_13"); //NOI18N
    private ChangeListener listener;
    private DocumentListener documentListener;
    /** Was projectFolder property edited by user? */
    private boolean projectFolderTouched = false;
    /** Was projectName property edited by user? */
    private boolean projectNameTouched = false;
    private List earProjects;
    private BigDecimal ejbJarXmlVersion;
    private WizardDescriptor wizardDescriptor;
    private J2eeVersionWarningPanel warningPanel;
        
    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportLocation panel) {
        this.panel = panel;
        initComponents ();
        setJ2eeVersionWarningPanel();
        initServerInstances();
        initEnterpriseApplications();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportLocationVisual.class, "ACS_NWP1_NamePanel_A11YDesc"));  // NOI18N
        setName(NbBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_IW_ImportTitle")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(ImportLocationVisual.class, "TXT_ImportEJBModule")); //NOI18N
        this.listener = panel;
        this.projectName.setText("");
        documentListener = new DocumentListener() {           
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        this.projectName.getDocument().addDocumentListener(documentListener);
        this.projectFolder.getDocument().addDocumentListener(documentListener);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        browseProjectLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        browseProjectFolder = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        addToAppLabel = new javax.swing.JLabel();
        addToAppComboBox = new javax.swing.JComboBox();
        warningPlaceHolderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setNextFocusableComponent(projectLocation);
        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_ImportLocationVisual_A11Name"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_ImportLocationVisual_A11Desc"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSrcLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelSrcLocation, gridBagConstraints);
        jLabelSrcLocation.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_Location_A11Desc"));

        projectLocation.setNextFocusableComponent(browseProjectLocation);
        projectLocation.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                projectLocationFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocation_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        browseProjectLocation.setNextFocusableComponent(projectName);
        browseProjectLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(browseProjectLocation, gridBagConstraints);
        browseProjectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_IW_ImportLocationBrowse_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocationDesc, NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelPrjLocationDesc, gridBagConstraints);

        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjName, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjName, gridBagConstraints);
        jLabelPrjName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_ProjectName_A11Desc"));

        projectName.setNextFocusableComponent(projectFolder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc"));

        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPrjLocation, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjLocation, gridBagConstraints);
        jLabelPrjLocation.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_ProjectFolder_A11Desc"));

        projectFolder.setNextFocusableComponent(browseProjectFolder);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectFolder, gridBagConstraints);
        projectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectFolder, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        browseProjectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectFolderActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(browseProjectFolder, gridBagConstraints);
        browseProjectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jSeparator1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_IW_SetAsMainProject_CheckBox"));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_SetAsMainProject_A11YDesc"));

        serverInstanceLabel.setLabelFor(serverInstanceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverInstanceLabel, org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        jPanel1.add(serverInstanceLabel, gridBagConstraints);

        serverInstanceComboBox.setMinimumSize(new java.awt.Dimension(150, 24));
        serverInstanceComboBox.setNextFocusableComponent(j2eeSpecComboBox);
        serverInstanceComboBox.setPreferredSize(new java.awt.Dimension(150, 24));
        serverInstanceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverInstanceComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(serverInstanceComboBox, gridBagConstraints);
        serverInstanceComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_SelectServerInstance_A11YDesc"));

        jLabel7.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_SelectJ2EEVersion_LabelMnemonic").charAt(0));
        jLabel7.setLabelFor(j2eeSpecComboBox);
        jLabel7.setText(org.openide.util.NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_J2EESpecLevel_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(jLabel7, gridBagConstraints);

        j2eeSpecComboBox.setMinimumSize(new java.awt.Dimension(100, 24));
        j2eeSpecComboBox.setNextFocusableComponent(jCheckBox1);
        j2eeSpecComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(j2eeSpecComboBox, gridBagConstraints);
        j2eeSpecComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("ACS_LBL_IW_SelectJ2EEVersion_A11YDesc"));

        addToAppLabel.setLabelFor(addToAppComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(addToAppLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_NWP1_AddToEApp_CheckBox"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(addToAppLabel, gridBagConstraints);

        addToAppComboBox.setNextFocusableComponent(serverInstanceComboBox);
        addToAppComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToAppComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel1.add(addToAppComboBox, gridBagConstraints);

        warningPlaceHolderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(warningPlaceHolderPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void projectLocationFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectLocationFocusLost
        if (projectLocation.getText().trim().length() > 0) {
            updateProjectName();
            updateProjectFolder();
            File f = new File(projectLocation.getText().trim());
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                FileObject configFilesPath = FileSearchUtility.guessConfigFilesPath(fo);
                if (configFilesPath != null) {
                    FileObject ejbJarXml = configFilesPath.getFileObject("ejb-jar.xml"); // NOI18N
                    checkEjbJarXmlJ2eeVersion(ejbJarXml);
                }
                listener.stateChanged(null);
            }
        }
    }//GEN-LAST:event_projectLocationFocusLost

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        String errorMessage;
        String selectedItem = (String)j2eeSpecComboBox.getSelectedItem();
        
        if (J2EE_SPEC_14_LABEL.equals(selectedItem) && new BigDecimal(EjbJar.VERSION_2_0).equals(ejbJarXmlVersion)) {
            errorMessage = NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "MSG_EjbJarXMLNotSupported");
        } else {
            errorMessage = null;
        }
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", errorMessage); //NOI18N
        }
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed

    private void addToAppComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToAppComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addToAppComboBoxActionPerformed

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

    private void browseProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectFolderActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectFolder.getText().length() > 0 && getProjectFolder().exists()) {
            chooser.setSelectedFile(getProjectFolder());
        } else if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
        }
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_SelectNewLocation")); // NOI18N
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectFolder.setText(projectDir.getAbsolutePath());
        }                    
    }//GEN-LAST:event_browseProjectFolderActionPerformed

    private void browseProjectLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (projectLocation.getText().length() > 0 && getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        } else {
            // honor the contract in issue 58987
            File currentDirectory = null;
            FileObject existingSourcesFO = Templates.getExistingSourcesFolder(wizardDescriptor);
            if (existingSourcesFO != null) {
                File existingSourcesFile = FileUtil.toFile(existingSourcesFO);
                if (existingSourcesFile != null && existingSourcesFile.isDirectory()) {
                    currentDirectory = existingSourcesFile;
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory(currentDirectory);
            } else {
                chooser.setSelectedFile(ProjectChooser.getProjectsFolder());
            }
        }
        
        chooser.setDialogTitle(NbBundle.getMessage(ImportLocationVisual.class, "LBL_SelectExistingLocation")); // NOI18N
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectLoc = FileUtil.normalizeFile(chooser.getSelectedFile());
            FileObject configFilesPath = FileSearchUtility.guessConfigFilesPath(FileUtil.toFileObject(projectLoc));
            if (configFilesPath != null) {
                FileObject ejbJarXml = configFilesPath.getFileObject("ejb-jar.xml"); // NOI18N
                checkEjbJarXmlJ2eeVersion(ejbJarXml);
            }
            projectLocation.setText(projectLoc.getAbsolutePath());
            updateProjectName();
            updateProjectFolder();
            listener.stateChanged(null);

        }
    }//GEN-LAST:event_browseProjectLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox addToAppComboBox;
    private javax.swing.JLabel addToAppLabel;
    private javax.swing.JButton browseProjectFolder;
    private javax.swing.JButton browseProjectLocation;
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JTextField projectFolder;
    public javax.swing.JTextField projectLocation;
    public javax.swing.JTextField projectName;
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    private javax.swing.JPanel warningPlaceHolderPanel;
    // End of variables declaration//GEN-END:variables
    
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        return chooser;
    }
    
    private static JFileChooser createChooser(String path) {
        JFileChooser chooser = new JFileChooser(path);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        return chooser;
    }
    
    private boolean ignoreEvent = false;

    private void update(DocumentEvent e) {
        if (ignoreEvent) {
            // side-effect of changes done in this handler
            return;
        }

        // start ignoring events
        ignoreEvent = true;

        if (projectLocation.getDocument() == e.getDocument()) {
            updateProjectName();
            updateProjectFolder();
        }

        // stop ignoring events
        ignoreEvent = false;

        if (projectFolder.getDocument() == e.getDocument()) {
            projectFolderTouched = !"".equals(projectFolder.getText());
        }
        if (projectName.getDocument() == e.getDocument()) {
            projectNameTouched = !"".equals(projectName.getText());
        }
        listener.stateChanged(null);
    }

    private void updateProjectName() {
        if (projectNameTouched) {
            return;
        }
        FileObject fo = FileUtil.toFileObject(getProjectLocation());
        if (fo != null) {
            projectName.setText(fo.getName()); // NOI18N
        }
    }

    private void updateProjectFolder() {
        if (projectFolderTouched) {
            return;                                                                
        }
        if (isValidProjectLocation()) {
            projectFolder.setText(getProjectLocation().getAbsolutePath());
        } else {
            projectFolder.setText(""); // NOI18N
        }
    }

    private boolean isValidProjectLocation() {
        return (getProjectLocation().exists() && getProjectLocation().isDirectory() &&
                projectLocation.getText().length() > 0 && (!projectLocation.getText().endsWith(":"))); // NOI18N
    }

    private boolean isEjbJarModule(FileObject dir) {
        return FileSearchUtility.guessConfigFilesPath(dir) != null && FileSearchUtility.guessJavaRoots(dir) != null;
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        File f = new File(projectLocation.getText().trim());
        File prjFolder = new File(projectFolder.getText().trim());
        String prjName = projectName.getText().trim();
        
        if (getSelectedServerInstanceID() == null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class,"MSG_NoServer")); //NOI18N
            return false;
        }
            
        if (!f.isDirectory()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class,"MSG_ProvideExistingSourcesLocation")); //NOI18N
            return false; //Existing sources location not specified
        }
        
        //Do we need this check?
        //            if (!prjFolder.isDirectory()) {
        //                wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class,"MSG_ProjectFolderDoesNotExists")); //NOI18N
        //                return false; //Project folder not specified
        //            }
        
        if (!isEjbJarModule(FileUtil.toFileObject(f))) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class,"MSG_NoEjbJarModule")); //NOI18N
            return false; //No ejb jar module location
        }
        
        if (prjName == null || prjName.length() == 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class,"MSG_ProvideProjectName")); //NOI18N
            return false; //Project name not specified
        }
        
        String result = checkValidity (this.projectName.getText(), this.projectFolder.getText());
        if (result != null) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", result); //NOI18N
            return false;
        }

        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        
        return true;
    }

    static String checkValidity (final String projectName, final String projectLocation) {
        File projLoc = new File (projectLocation).getAbsoluteFile();

        if (PanelProjectLocationVisual.getCanonicalFile(projLoc) == null) {
            return NbBundle.getMessage (PanelProjectLocationVisual.class,"MSG_IllegalProjectLocation");
        }

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(PanelSourceFolders.class,"MSG_ProjectFolderReadOnly");
        }

        File destFolder = FileUtil.normalizeFile(new File( projectLocation ));
        File[] kids = destFolder.listFiles();
        if ( destFolder.exists() && kids != null && kids.length > 0) {
            String file = null;
            for (int i=0; i< kids.length; i++) {
                String childName = kids[i].getName();
                if ("nbproject".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_NetBeansProject");
                }
                else if ("build".equals(childName)) {    //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildFolder");
                }
                else if ("dist".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_DistFolder");
                }
                else if ("build.xml".equals(childName)) {   //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_BuildXML");
                }
                else if ("manifest.mf".equals(childName)) { //NOI18N
                    file = NbBundle.getMessage (PanelSourceFolders.class,"TXT_Manifest");
                }
                if (file != null) {
                    String format = NbBundle.getMessage (PanelSourceFolders.class,"MSG_ProjectFolderInvalid");
                    return MessageFormat.format(format, new Object[] {file});
                }
            }
        }

        if (destFolder.isDirectory()) {
            FileObject destFO = FileUtil.toFileObject(destFolder);
            assert destFO != null : "No FileObject for " + destFolder;
            boolean clear = false;
            try {
                clear = ProjectManager.getDefault().findProject(destFO) == null;
            } catch (IOException e) {
                // need not report here; clear remains false -> error
            }
            if (!clear) {
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_ProjectFolderHasDeletedProject");
            }
        }
        return null;
    }        

    void read (WizardDescriptor d) {
        wizardDescriptor = d;
    }
    
    void store( WizardDescriptor d ) {
        String name = projectName.getText().trim();
        String moduleLoc = projectLocation.getText().trim();

        if (name.equals("") || moduleLoc.equals("")) {
            return;
        }
        
        d.putProperty(WizardProperties.PROJECT_DIR, new File(projectFolder.getText().trim()));
        File moduleLocFile =  new File(moduleLoc);
        d.putProperty(WizardProperties.SOURCE_ROOT, moduleLocFile);
        d.putProperty(WizardProperties.NAME, name);
        d.putProperty(WizardProperties.JAVA_ROOT, FileSearchUtility.guessJavaRootsAsFiles(FileUtil.toFileObject(moduleLocFile)));
        d.putProperty(WizardProperties.SERVER_INSTANCE_ID, getSelectedServerInstanceID());
        d.putProperty(WizardProperties.J2EE_LEVEL, getSelectedJ2eeSpec());
        d.putProperty(WizardProperties.EAR_APPLICATION, getSelectedEarApplication());
        if (warningPanel != null && warningPanel.getDowngradeAllowed()) {
            d.putProperty(WizardProperties.JAVA_PLATFORM, warningPanel.getJava14PlatformName());
            d.putProperty(WizardProperties.SOURCE_LEVEL, "1.4"); // NOI18N
        }
        
        // TODO: ma154696: add also search for test roots
    }
    
    //extra finish dialog
    private Dialog dialog;
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        File dirF = new File(projectFolder.getText());
        JButton ok = new JButton(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_Buildfile_OK")); //NOI18N
        ok.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic")); //NOI18N
        ok.setMnemonic(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_BuildFileDialog_OK_LabelMnemonic").charAt(0)); //NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_Buildfile_Cancel")); //NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic")); //NOI18N
        cancel.setMnemonic(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic").charAt(0)); //NOI18N
        
        final ImportBuildfile ibf = new ImportBuildfile(dirF.getAbsolutePath(), ok);
        if ((new File(dirF, GeneratedFilesHelper.BUILD_XML_PATH)).exists()) {
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Object src = event.getSource();
                    if (src instanceof JButton) {
                        String name = ((JButton) src).getText();
                        if (name.equals(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_Buildfile_OK"))) { //NOI18N
                            closeDialog();
                        } else if (name.equals(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_Buildfile_Cancel"))) { //NOI18N
                            NotifyDescriptor ndesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_Buildfile_CancelConfirmation"), NotifyDescriptor.YES_NO_OPTION); //NOI18N
                            Object ret = DialogDisplayer.getDefault().notify(ndesc);
                            if (ret == NotifyDescriptor.YES_OPTION) {
                                closeDialog();
                            }
                        }
                    }
                }
            };
            
            DialogDescriptor descriptor = new DialogDescriptor(
                    ibf,
                    NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_BuildfileTitle"), //NOI18N
                    true,
                    new Object[] {ok, cancel},
                    DialogDescriptor.OK_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            actionListener
                            );
                    
                    dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    dialog.show();
        } else
            return;
    }

    private void closeDialog() {
        dialog.dispose();
    }

    //use it as a project root iff it is not sources or document root
    // NOTE: the order of the searches in kind of important in this method for
    //   performance reasons
    public boolean isSuitableProjectRoot(FileObject dir) {
        FileObject configFilesRoot = FileSearchUtility.guessConfigFilesPath(dir);
        if (configFilesRoot != null && !FileUtil.isParentOf(dir, configFilesRoot))
            return false;
        FileObject[] srcRoots = FileSearchUtility.guessJavaRoots(dir);
        if (srcRoots != null && !isParentOf(dir, srcRoots))
            return false;
        return true;
    }
    
    private boolean isParentOf(FileObject dir, FileObject[] fos) {
        boolean result = true;
        if (fos != null) {
            for (int i = 0; i < fos.length; i++) {
                result = FileUtil.isParentOf(dir, fos[i]);
                if (!result) {
                    return result;
                }
            }
        }
        return result;
    }
    
    private void initServerInstances() {
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        serverInstanceIDs = new ArrayList();
        for (int i = 0; i < servInstIDs.length; i++) {
            J2eePlatform j2eePlat = Deployment.getDefault().getJ2eePlatform(servInstIDs[i]);
            String servInstDisplayName = Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]);
            if (servInstDisplayName != null && !servInstDisplayName.equals("")
                && j2eePlat != null && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                serverInstanceIDs.add(servInstIDs[i]);
                serverInstanceComboBox.addItem(servInstDisplayName);
            }
        }
        if (serverInstanceIDs.size() > 0) {
            serverInstanceComboBox.setSelectedIndex(0);
        } else {
            serverInstanceComboBox.setEnabled(false);
            j2eeSpecComboBox.setEnabled(false);
        }
    }
    
    public String getSelectedServerInstanceID() {
        int idx = serverInstanceComboBox.getSelectedIndex();
        return idx == -1 ? null 
                         : (String)serverInstanceIDs.get(idx);
    }
    
    public String getSelectedJ2eeSpec() {
        Object item = j2eeSpecComboBox.getSelectedItem();
        return item == null ? null
                            : item.equals(J2EE_SPEC_14_LABEL) ? J2eeModule.J2EE_14 : J2eeModule.J2EE_13;
    }

    private File getAsFile(String filename) {
        return FileUtil.normalizeFile(new File(filename));
    }

    public File getProjectLocation() {
        return getAsFile(projectLocation.getText());
    }

    public File getProjectFolder() {
        return getAsFile(projectFolder.getText());
    }

    public String getProjectName() {
        return projectName.getText();
    }

    private Project getSelectedEarApplication() {
        int idx = addToAppComboBox.getSelectedIndex();
        return (idx <= 0) ? null : (Project) earProjects.get(idx - 1);
    }
    
    private void initEnterpriseApplications() {
        addToAppComboBox.addItem(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_AddToEApp_None"));
        addToAppComboBox.setSelectedIndex(0);
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        earProjects = new ArrayList();
        for (int i = 0; i < allProjects.length; i++) {
            J2eeModuleContainer container = (J2eeModuleContainer) allProjects[i].getLookup().lookup(J2eeModuleContainer.class);
            ProjectInformation projectInfo = ProjectUtils.getInformation(allProjects[i]);
            if (container != null) {
                earProjects.add(projectInfo.getProject());
                addToAppComboBox.addItem(projectInfo.getDisplayName());
            }
        }
        if (earProjects.size() <= 0) {
            addToAppComboBox.setEnabled(false);
        }
    }
    
    private BigDecimal getEjbJarXmlVersion(FileObject ejbJarXml) throws IOException {
        if (ejbJarXml != null) {
            return DDProvider.getDefault().getDDRoot(ejbJarXml).getVersion();
        } else {
            return null;
        }
    }

    private void checkEjbJarXmlJ2eeVersion(FileObject ejbJarXml) {
        try {
            BigDecimal version = getEjbJarXmlVersion(ejbJarXml);
            ejbJarXmlVersion = version;
            if (version == null)
                return;
            
            if(new BigDecimal(EjbJar.VERSION_2_0).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(J2EE_SPEC_13_LABEL);
            } else if(new BigDecimal(EjbJar.VERSION_2_1).equals(version)) {
                j2eeSpecComboBox.setSelectedItem(J2EE_SPEC_14_LABEL);
            }
        } catch (IOException e) {
            final ErrorManager errorManager = ErrorManager.getDefault();
            String message = NbBundle.getMessage(ImportLocationVisual.class, "MSG_EjbJarXmlCorrupted"); // NOI18N
            errorManager.notify(errorManager.annotate(e, message));
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
