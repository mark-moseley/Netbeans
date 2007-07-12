/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.samples.bankapp;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class UMLBankAppSamplePanelVisual extends JPanel implements DocumentListener
{
    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    public static final String PROP_UML_PROJECT_NAME = "umlProjectName"; // NOI18N
    
    private UMLBankAppSampleWizardPanel panel;
    
    /** Creates new form PanelProjectLocationVisual */
    public UMLBankAppSamplePanelVisual(UMLBankAppSampleWizardPanel panel)
    {
        initComponents();
        this.panel = panel;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        umlProjectNameTextField.getDocument().addDocumentListener(this);
    }
    
    
    public String getProjectName()
    {
        return this.projectNameTextField.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        umlProjectNameLabel = new javax.swing.JLabel();
        umlProjectNameTextField = new javax.swing.JTextField();
        createdUmlFolderLabel = new javax.swing.JLabel();
        createdUmlFolderTextField = new javax.swing.JTextField();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_JavaProjectNameLabel")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_ProjectsLocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_BrowseButton")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_JavaProjectFolderLable")); // NOI18N

        createdFolderTextField.setEditable(false);

        umlProjectNameLabel.setLabelFor(umlProjectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(umlProjectNameLabel, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_UMLProjectNameLabel")); // NOI18N

        createdUmlFolderLabel.setLabelFor(createdUmlFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdUmlFolderLabel, org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "LBL_UMLProjectFolderLabel")); // NOI18N

        createdUmlFolderTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectNameLabel)
                    .add(projectLocationLabel)
                    .add(umlProjectNameLabel)
                    .add(createdFolderLabel)
                    .add(createdUmlFolderLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                            .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(createdUmlFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                            .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                            .add(umlProjectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                        .add(95, 95, 95))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(browseButton)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderLabel)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(umlProjectNameLabel)
                    .add(umlProjectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdUmlFolderLabel)
                    .add(createdUmlFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSN_JavaProjectName")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSD_JavaProjectName")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSN_ProjectsLocation")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSD_ProjectsLocation")); // NOI18N
        createdFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSN_JavaProjectFolder")); // NOI18N
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSD_JavaProjectFolder")); // NOI18N
        umlProjectNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSN_UMLProjectName")); // NOI18N
        umlProjectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSD_UMLProjectName")); // NOI18N
        createdUmlFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSN_UMLProjectFolder")); // NOI18N
        createdUmlFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLBankAppSamplePanelVisual.class, "ACSD_UMLProjectFolder")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        
        if ("BROWSE".equals(command)) // NOI18N
        {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            
            chooser.setDialogTitle(NbBundle.getMessage(
                UMLBankAppSamplePanelVisual.class, 
                "MSG_SelectProjectLocation")); // NOI18N
            
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            
            if (path.length() > 0)
            {
                File f = new File(path);
                
                if (f.exists())
                {
                    chooser.setSelectedFile(f);
                }
            }
            
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this))
            {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(
                    FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            
            panel.fireChangeEvent();
        }
        
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel createdUmlFolderLabel;
    private javax.swing.JTextField createdUmlFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JLabel umlProjectNameLabel;
    private javax.swing.JTextField umlProjectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    public void addNotify()
    {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    boolean valid(WizardDescriptor wizardDescriptor)
    {
        if (projectNameTextField.getText().length() == 0)
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_JavaProjectNameNotValidFolder")); // NOI18N
            
            return false; // Display name not specified
        }
        
        File f = FileUtil.normalizeFile(
            new File(projectLocationTextField.getText()).getAbsoluteFile());
        
        if (!f.isDirectory())
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_JavaProjectFolderNotValidPath")); // NOI18N
            
            return false;
        }
        
        final File destFolder = FileUtil.normalizeFile(
            new File(createdFolderTextField.getText()).getAbsoluteFile());
        
        File projLoc = destFolder;
        
        while (projLoc != null && !projLoc.exists())
        {
            projLoc = projLoc.getParentFile();
        }
        
        if (projLoc == null || !projLoc.canWrite())
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_CantCreateJavaProjectFolder")); // NOI18N
            
            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null)
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_JavaProjectFolderNotValidPath")); // NOI18N
            
            return false;
        }
        
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0)
        {
            // Folder exists and is not empty
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_JavaProjectFolderExists")); // NOI18N
            
            return false;
        }
        
        
        // UML project name/location validation
        if (umlProjectNameTextField.getText().length() == 0)
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_UMLProjectNameNotValidFolder")); // NOI18N
            
            return false; // Display name not specified
        }

        File f2 = FileUtil.normalizeFile(
            new File(projectLocationTextField.getText()).getAbsoluteFile());
       
        final File umlDestFolder = FileUtil.normalizeFile(
            new File(createdUmlFolderTextField.getText()).getAbsoluteFile());
        
        File umlProjLoc = umlDestFolder;
        
        while (umlProjLoc != null && !umlProjLoc.exists())
        {
            umlProjLoc = umlProjLoc.getParentFile();
        }
        
        if (umlProjLoc == null || !umlProjLoc.canWrite())
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_CantCreateUMLProjectFolder")); // NOI18N

            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null)
        {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_UMLProjectFolderNotValidPath")); // NOI18N
                
            return false;
        }
        
        File[] kidz = umlDestFolder.listFiles();
        if (umlDestFolder.exists() && kidz != null && kidz.length > 0)
        {
            // Folder exists and is not empty
            wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(UMLBankAppSamplePanelVisual.class,
                    "MSG_UMLProjectFolderExists")); // NOI18N
            
            return false;
        }
        
        wizardDescriptor.putProperty("WizardPanel_errorMessage", ""); // NOI18N
        return true;
    }
    
    void store(WizardDescriptor d)
    {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        String umlname = umlProjectNameTextField.getText().trim();
        String umlfolder = createdUmlFolderTextField.getText().trim();
        
        d.putProperty("projdir", new File(folder)); // NOI18N
        d.putProperty("name", name); // NOI18N
        d.putProperty("umlprojdir", new File(umlfolder)); // NOI18N
        d.putProperty("umlname", umlname); // NOI18N
    }
    
    void read(WizardDescriptor settings)
    {
        File projectLocation = (File) settings.getProperty("projdir"); // NOI18N
        
        if (projectLocation == null ||
            projectLocation.getParentFile() == null ||
            !projectLocation.getParentFile().isDirectory())
        {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        
        else
        {
            projectLocation = projectLocation.getParentFile();
        }
        
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String)settings.getProperty("name"); // NOI18N
        
        if (projectName == null)
        {
            projectName = NbBundle.getMessage(
                UMLBankAppSamplePanelVisual.class, "MSG_ProjectBaseName"); // NOI18N
        }
        
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        
        
        // UML fields
        
        String umlProjectName = (String)settings.getProperty("umlname"); // NOI18N
        
        if (umlProjectName == null)
        {
            umlProjectName = projectName + NbBundle.getMessage(
                UMLBankAppSamplePanelVisual.class, "MSG_UMLProjectSuffix"); // NOI18N
        }
        
        this.umlProjectNameTextField.setText(umlProjectName);
    }
    
    void validate(WizardDescriptor d) throws WizardValidationException
    {
        // nothing to validate
    }
    
    // Implementation of DocumentListener --------------------------------------
    
    public void changedUpdate(DocumentEvent e)
    {
        updateTexts(e);
        if (projectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_PROJECT_NAME, null, 
                projectNameTextField.getText());
        }

        else if (umlProjectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_UML_PROJECT_NAME, null, 
                umlProjectNameTextField.getText());
        }
    }
    
    public void insertUpdate(DocumentEvent e)
    {
        updateTexts(e);
        if (projectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_PROJECT_NAME, null, 
                projectNameTextField.getText());
        }

        else if (umlProjectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_UML_PROJECT_NAME, null, 
                umlProjectNameTextField.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent e)
    {
        updateTexts(e);
        if (projectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_PROJECT_NAME, null, 
                projectNameTextField.getText());
        }

        else if (umlProjectNameTextField.getDocument() == e.getDocument())
        {
            firePropertyChange(PROP_UML_PROJECT_NAME, null, 
                umlProjectNameTextField.getText());
        }
    }
    
    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e)
    {
        
        Document doc = e.getDocument();
        
        if (doc == projectNameTextField.getDocument() || 
            doc == projectLocationTextField.getDocument() ||
            doc == umlProjectNameTextField.getDocument())
        {
            if (doc == projectNameTextField.getDocument() || 
                doc == projectLocationTextField.getDocument())
            {
                String projectName = projectNameTextField.getText();
                String projectFolder = projectLocationTextField.getText();
                
                // Java project folder
                createdFolderTextField.setText(
                    projectFolder + File.separatorChar + projectName);

                // UML project name
                umlProjectNameTextField.setText(
                    projectNameTextField.getText() + 
                    NbBundle.getMessage(
                        UMLBankAppSamplePanelVisual.class, 
                        "MSG_UMLProjectSuffix")); // NOI18N
            }
            
            // UML project folder
            createdUmlFolderTextField.setText(
                projectLocationTextField.getText() + File.separatorChar + 
                umlProjectNameTextField.getText());
        }
        
        panel.fireChangeEvent(); // Notify that the panel changed
    }

}
