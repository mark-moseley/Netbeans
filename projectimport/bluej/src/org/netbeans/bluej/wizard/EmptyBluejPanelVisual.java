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
package org.netbeans.bluej.wizard;

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

public class EmptyBluejPanelVisual extends JPanel implements DocumentListener {
    
    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    private static final String WIZ_PANEL_ERROR = "WizardPanel_errorMessage"; // NOI18N
    private EmptyBluejWizardPanel panel;
    
    /** Creates new form PanelProjectLocationVisual */
    public EmptyBluejPanelVisual(EmptyBluejWizardPanel panel) {
        initComponents();
        this.panel = panel;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }
    
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(EmptyBluejPanelVisual.class, "projectNameLabel"));

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(EmptyBluejPanelVisual.class, "projectLocationLabel"));

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(EmptyBluejPanelVisual.class, "browseButton"));
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(EmptyBluejPanelVisual.class, "createdFolderLabel"));

        createdFolderTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectNameLabel)
                    .add(projectLocationLabel)
                    .add(createdFolderLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .addContainerGap())
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
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderLabel)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(213, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) { // NOI18N
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(NbBundle.getMessage(EmptyBluejPanelVisual.class, "browse_dialog_title"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            panel.fireChangeEvent();
        }
        
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    
    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        
        d.putProperty("projdir", new File(folder)); // NOI18N
        d.putProperty("name", name); // NOI18N
    }
    
    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir"); // NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty("name"); // NOI18N
        if(projectName == null) {
            projectName = "EmptyBluej"; // NOI18N
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }
    
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    public boolean valid(WizardDescriptor wizardDescriptor) {
        
        if (projectNameTextField.getText().length() == 0) {
            wizardDescriptor.putProperty(WIZ_PANEL_ERROR,
                    NbBundle.getMessage(EmptyBluejPanelVisual.class, "ERROR_wrongName"));
            return false; // Display name not specified
        }
        File f = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = NbBundle.getMessage(EmptyBluejPanelVisual.class, "ERROR_wrongFolder");
            wizardDescriptor.putProperty(WIZ_PANEL_ERROR, message);
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());
        
        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WIZ_PANEL_ERROR,
                    NbBundle.getMessage(EmptyBluejPanelVisual.class, "ERROR_cannot_create_folder"));
            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null) {
            String message = NbBundle.getMessage(EmptyBluejPanelVisual.class, "ERROR_wrong_path");
            wizardDescriptor.putProperty(WIZ_PANEL_ERROR, message);
            return false;
        }
        
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WIZ_PANEL_ERROR,
                    NbBundle.getMessage(EmptyBluejPanelVisual.class, "ERROR_exists"));
            return false;
        }
        wizardDescriptor.putProperty(WIZ_PANEL_ERROR, "");
        return true;
    }
    
    // Implementation of DocumentListener --------------------------------------
    
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }
    
    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e) {
        
        Document doc = e.getDocument();
        
        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}
            
        }
        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
}
