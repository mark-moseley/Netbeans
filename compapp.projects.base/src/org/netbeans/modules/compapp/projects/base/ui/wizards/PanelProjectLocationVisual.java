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


package org.netbeans.modules.compapp.projects.base.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.modules.compapp.projects.base.ui.FoldersListSettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

class PanelProjectLocationVisual
    extends SettingsPanel
    implements DocumentListener, org.netbeans.modules.compapp.projects.base.IcanproConstants {
    
    public static final String PROP_PROJECT_NAME = "projectName";      //NOI18N
    private PanelConfigureProject panel;
    
    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectLocationVisual(PanelConfigureProject panel) {
        initComponents();
        this.panel = panel;
        
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }
    
    JTextField getProjectNameTextField() {
        return projectNameTextField;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        Button = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(projectNameLabel, gridBagConstraints);
        projectNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_ProjectName_A11YDesc")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NPW1_ProjectLocation_A11YDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(Button, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        Button.setActionCommand("BROWSE");
        Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(Button, gridBagConstraints);
        Button.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_BrowseLocation_A11YDesc")); // NOI18N

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
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
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_NWP1_CreatedProjectFolder_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        String command = evt.getActionCommand();
        
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getBundle(WIZARD_BUNDLE).getString("LBL_NWP1_SelectProjectLocation")); //NOI18N
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists())
                    chooser.setSelectedFile(f);
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(projectDir.getAbsolutePath());
            }            
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction
    
    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        String projectName = projectNameTextField.getText();
        if (projectName.length() == 0                //String indexOf is not picking up \ char if this is the
                // first char
                || projectName.substring(0,1).equals("\\")
                || projectName.indexOf('/')  >= 0         //NOI18N
                || projectName.indexOf("\\") >= 0         //NOI18N
                || projectName.indexOf(':')  >= 0
                || !isValidName(projectName)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getBundle(WIZARD_BUNDLE).getString("MSG_IllegalProjectName")); //NOI18N
            return false; // Display name not specified
        }
        

        if (projectName.indexOf(' ')  >= 0) {        //NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectNameWithWhiteSpace"));
            return false;
        }
        
        File f = new File(projectLocationTextField.getText()).getAbsoluteFile();
        if (getCanonicalFile(f)==null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation");
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }

        File destFolder = new File(createdFolderTextField.getText());
        if (getCanonicalFile(destFolder) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName");
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }
        
        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly"));
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation");
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        }

        File[] children = destFolder.listFiles();
        if (destFolder.exists() && children != null && children.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getBundle(WIZARD_BUNDLE).getString("MSG_ProjectFolderExists")); //NOI18N
            return false;
        }
                
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        return true;
    }
    
    static boolean isValidName(String fileName){
        try {
            boolean bValid = true;
            File tempFile =  new File(fileName);
            // fix 113726
//            String tempFileName = 0+fileName;
            String tempFileName ="00"+fileName;
            File actualTempFile = File.createTempFile(tempFileName, null);

            if (!FileUtil.normalizeFile(tempFile).equals(tempFile.getCanonicalFile())) {
                bValid = false;
            }
            actualTempFile.delete();
            actualTempFile = null;
            tempFile = null;
            return bValid;
        }catch (Exception e) {
            return false;
        }
    }
    
    public void store(WizardDescriptor d) {        
        String name = projectNameTextField.getText().trim();
        
        d.putProperty(    WizardProperties.PROJECT_DIR, new File(createdFolderTextField.getText().trim()));
        d.putProperty(    WizardProperties.NAME, name);
        
        File projectsDir = new File(this.projectLocationTextField.getText());

        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder (projectsDir);
        }
    }
        
    public void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty(WizardProperties.PROJECT_DIR);

        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        else {
            projectLocation = projectLocation.getParentFile();
        }
        projectLocationTextField.setText(projectLocation.getAbsolutePath());
        String projectName = (String) settings.getProperty(    WizardProperties.NAME);

        if (projectName == null) {
            int baseCount = FoldersListSettings.getDefault().getNewProjectCount() + 1;
            String formater = panel.getDefaultName();
            while ((projectName = validFreeProjectName(projectLocation, formater, baseCount)) == null)
                baseCount++;
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
        
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        return chooser;
    }

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, new Object[] {new Integer (index)});                
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }
    // End if implementation of DocumentListener -------------------------------
    
    
    /** Handles changes in the project name and project directory
     */
    private void updateTexts(DocumentEvent e) {
        Document doc = e.getDocument();
        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            String projFolderPath = FileUtil.normalizeFile(new File(projectFolder)).getAbsolutePath();
            if (projFolderPath.endsWith(File.separator)) {
                createdFolderTextField.setText(projFolderPath + projectName);
            } else {
                createdFolderTextField.setText(projFolderPath + File.separator + projectName);
            }
        }                
        panel.fireChangeEvent(); // Notify that the panel changed      
    }
    
    private String getCreatedFolderPath() {
        StringBuffer folder = new StringBuffer(projectLocationTextField.getText().trim());
        if (!projectLocationTextField.getText().endsWith(File.separator)) {
            folder.append(File.separatorChar);
        }
        folder.append(projectNameTextField.getText().trim());
        
        return folder.toString();
    }

    static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }    
}

//TODO implement check for project folder name and location
