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

package org.netbeans.modules.web.project.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.web.project.ui.*;
import org.netbeans.modules.web.project.Utils;

import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class PanelProjectLocationVisual extends SettingsPanel implements DocumentListener {
    
    private PanelConfigureProject panel;
    
    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectLocationVisual(PanelConfigureProject panel) {
        initComponents();
        this.panel = panel;
        
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        Button = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_LabelMnemonic").charAt(0));
        projectNameLabel.setLabelFor(projectNameTextField);
        projectNameLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(projectNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc"));

        projectLocationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_LabelMnemonic").charAt(0));
        projectLocationLabel.setLabelFor(projectLocationTextField);
        projectLocationLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(projectLocationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc"));

        Button.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        Button.setActionCommand("BROWSE");
        Button.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(Button, gridBagConstraints);
        Button.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc"));

        createdFolderLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_LablelMnemonic").charAt(0));
        createdFolderLabel.setLabelFor(createdFolderTextField);
        createdFolderLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(createdFolderLabel, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(createdFolderTextField, gridBagConstraints);
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_CreatedProjectFolder_A11YDesc"));

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == Button) {
                PanelProjectLocationVisual.this.browseLocationAction(evt);
            }
        }
    }//GEN-END:initComponents

    private static String getBundleResource(final String resourceName) {
        return NbBundle.getMessage(PanelProjectLocationVisual.class, resourceName);
    }

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        String command = evt.getActionCommand();
        
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = FileChooser.createDirectoryChooser(
                    "PanelProjectLocationVisual.browseLocationAction", projectLocationTextField.getText()); //NOI18N
            chooser.setDialogTitle(getBundleResource("LBL_NWP1_SelectProjectLocation")); //NOI18N
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(projectDir.getAbsolutePath());
            }
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction
    
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        File f = new File(projectLocationTextField.getText().trim());
        String createdFolderText = f.getAbsolutePath();
        f = getCanonicalFile(f);
        if(f == null) {
            final String message = getBundleResource("MSG_IllegalProjectLocation");
            createdFolderTextField.setText(createdFolderText + " - " + message);
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false; // Invalid project location
        }
        final String projectName = projectNameTextField.getText().trim();
        f = new File(f, projectName);
        createdFolderText = f.getAbsolutePath();
        f = getCanonicalFile(f);
        if(f == null || !projectName.equals(f.getName())) {
            final String message = getBundleResource("MSG_IllegalProjectName");
            createdFolderTextField.setText(createdFolderText + " - " + message);
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false; // Invalid project name
        }
        createdFolderText = f.getAbsolutePath();
        createdFolderTextField.setText(createdFolderText);
        if(!f.exists()) {
            if(!Utils.getRoot(f).exists()) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage", getBundleResource("MSG_IllegalDisk"));
                return false; // Invalid disk
            }
        }
        File prjParent = new File(projectLocationTextField.getText().trim());
        if (prjParent.exists() && !prjParent.canWrite()) {
            // Read only project location
            wizardDescriptor.putProperty("WizardPanel_errorMessage", getBundleResource("MSG_ProjectLocationRO")); //NOI18N
            return false;
        }
        
        File destFolder = new File(createdFolderTextField.getText());
        File[] children = destFolder.listFiles();
        if (destFolder.exists() && children != null && children.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty("WizardPanel_errorMessage", getBundleResource("MSG_ProjectFolderExists")); //NOI18N
            return false;
        }
                
        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        return true;
    }

    public static File getCanonicalFile(File f) {
        File f1;
        try {
            f1 = f.getCanonicalFile();
        } catch (IOException e) {
            f1 = null;
        }
        return f1;
    }

    public void validateProjectLocation() throws WizardValidationException {
        final File dir = new File(createdFolderTextField.getText());

        final File parentDir = dir.getParentFile();
        if(!parentDir.exists()) {
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Confirmation(
                    parentDir.getAbsolutePath() + "\n" + getBundleResource("MSG_CreateDir"),

                    NotifyDescriptor.YES_NO_OPTION);
            if (!NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(notifyDescriptor))) {
                throw new WizardValidationException(projectLocationTextField, "", "");
            }
            try {
                Utils.getValidDir(parentDir);
            } catch (IOException ex) {
                throw new WizardValidationException(projectLocationTextField, ex.getMessage(), ex.getLocalizedMessage());
            }
        }
        try {
            Utils.getValidEmptyDir(dir);
        } catch (IOException ex) {
            throw new WizardValidationException(projectNameTextField, ex.getMessage(), ex.getLocalizedMessage());
        }
    }

    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        
        d.putProperty(WizardProperties.PROJECT_DIR, new File(createdFolderTextField.getText().trim()));
        d.putProperty(WizardProperties.NAME, name);
        
        File projectsDir = new File(this.projectLocationTextField.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder (projectsDir);
        }
    }
        
    void read (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty(WizardProperties.PROJECT_DIR);
        if (projectLocation == null || projectLocation.getParentFile() == null)
            projectLocation = ProjectChooser.getProjectsFolder();
        else
            projectLocation = projectLocation.getParentFile();
        
        projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty(WizardProperties.NAME);
        if (projectName == null) {
            int baseCount = FoldersListSettings.getDefault().getNewProjectCount() + 1;
            String formater = getBundleResource("LBL_NPW1_DefaultProjectName");
            while ((projectName = validFreeProjectName(projectLocation, formater, baseCount)) == null)
                baseCount++;
            settings.putProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX, new Integer(baseCount));
        }
        
        projectNameTextField.setText(projectName);                
        projectNameTextField.selectAll();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Button;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    protected javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, new Object[] {new Integer (index)});                
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts();
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts();
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts();
    }
    // End if implementation of DocumentListener -------------------------------
    
    
    /** Handles changes in the project name and project directory
     */
    private void updateTexts() {
        panel.fireChangeEvent(); // Notify that the panel changed
    }
}
