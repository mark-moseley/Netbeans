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
package org.netbeans.modules.project.uiapi;

import java.awt.CardLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Jan Lahoda
 */
public class ProjectCopyPanel extends javax.swing.JPanel implements DocumentListener, DefaultProjectOperationsImplementation.InvalidablePanel {
    
    private Project project;
    private boolean isMove;
    private boolean invalid;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ProgressHandle handle;
    
    /**
     * Creates new form ProjectCopyPanel
     */
    public ProjectCopyPanel(ProgressHandle handle, Project project, boolean isMove) {
        this.project = project;
        this.isMove = isMove;
        this.handle = handle;
        
        
        initComponents();
        setProject();
        projectName.getDocument().addDocumentListener(this);
        projectLocation.getDocument().addDocumentListener(this);
        
        if (isMove) {
            nameLabel.setVisible(false);
            projectName.setVisible(false);
        }
        
        if (Boolean.getBoolean("org.netbeans.modules.project.uiapi.DefaultProjectOperations.showProgress")) {
            ((CardLayout) progress.getLayout()).show(progress, "progress");
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        projectName = new javax.swing.JTextField();
        browse = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        projectFolder = new javax.swing.JTextField();
        extSourcesWarning = new javax.swing.JLabel();
        errorMessage = new javax.swing.JLabel();
        progress = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        progressImpl = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSD_Copy_Move_Panel", new Object[] {new Integer(isMove ? 1 : 0)}));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Copy_Move_Dialog_Text", new Object[] {new Integer(isMove ? 1 : 0), ProjectUtils.getInformation(project).getDisplayName()}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(projectLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Project_Location"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel2, gridBagConstraints);

        projectLocation.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 5);
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSN_Project_Location", new Object[] {}));
        projectLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSD_Project_Location", new Object[] {}));

        nameLabel.setLabelFor(projectName);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Project_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(nameLabel, gridBagConstraints);

        projectName.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 5);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSN_Project_Name", new Object[] {}));
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSD_Project_Name", new Object[] {}));

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Browse", new Object[] {}));
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(browse, gridBagConstraints);
        browse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSD_Browse", new Object[] {}));

        jLabel4.setLabelFor(projectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Project_Folder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel4, gridBagConstraints);

        projectFolder.setColumns(30);
        projectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 5);
        add(projectFolder, gridBagConstraints);
        projectFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSN_Project_Folder", new Object[] {}));
        projectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "ACSD_Project_Folder", new Object[] {}));

        extSourcesWarning.setForeground(UIManager.getColor("nb.errorForeground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(extSourcesWarning, gridBagConstraints);

        errorMessage.setForeground(UIManager.getColor("nb.errorForeground"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(errorMessage, gridBagConstraints);

        progress.setLayout(new java.awt.CardLayout());

        progress.add(jPanel4, "not-progress");

        progressImpl.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ProjectCopyPanel.class, "LBL_Copying_Moving", new Object[] {isMove ? new Integer(1) : new Integer(0)}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        progressImpl.add(jLabel5, gridBagConstraints);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel3.add(ProgressHandleFactory.createProgressComponent(handle));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        progressImpl.add(jPanel3, gridBagConstraints);

        progress.add(progressImpl, "progress");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(progress, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
// TODO add your handling code here:
        File current = new File(projectLocation.getText());
        JFileChooser chooser = new JFileChooser(current);
        
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            projectLocation.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel extSourcesWarning;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel progress;
    private javax.swing.JPanel progressImpl;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables
    
    private String lastComputedName;
    
    private String computeValidProjectName(String projectLocation, String projectNamePrefix) {
        File location = new File(projectLocation);
        
        if (!location.exists()) {
            lastComputedName = projectNamePrefix;
            return projectNamePrefix;
        }
        
        int num = 1;
        String projectName = projectNamePrefix;
        if (new File(location, projectName).exists()) {
            while (new File(location, projectName = projectNamePrefix + "_" + num).exists()) {
                num++;
            }
        }
        lastComputedName = projectName;
        return projectName;
    }
    
    private void setProject() {
        FileObject parent = project.getProjectDirectory().getParent();
        File parentFile = FileUtil.toFile(parent);
        
        projectLocation.setText(parentFile.getAbsolutePath());
        
        if (isMove) {
            projectName.setText(ProjectUtils.getInformation(project).getName());
        } else {
            projectName.setText(computeValidProjectName(parentFile.getAbsolutePath(), ProjectUtils.getInformation(project).getName()));
        }
        
        updateProjectFolder();
        validateDialog();
        
        if (hasExternalSources() && !isMove) {
            extSourcesWarning.setText(NbBundle.getMessage(ProjectCopyPanel.class, "WRN_External_Sources"));
            invalid = true;
        }
    }
    
    private boolean hasExternalSources() {
        for (FileObject file : ProjectOperations.getDataFiles(project)) {
            if (!FileUtil.isParentOf(project.getProjectDirectory(), file)) {
                return true;
            }
        }
        return false;
    }
    
    public String getNewName() {
        return projectName.getText();
    }
    
    public String getProjectFolderName() {
        return project.getProjectDirectory().getNameExt();
    }
    
    public File getNewDirectory() {
        return new File(projectLocation.getText());
    }
    
    public void changedUpdate(DocumentEvent e) {
        //ignored
    }
    
    public void insertUpdate(DocumentEvent e) {
        if (e.getDocument().equals(projectLocation.getDocument())) {
            if (lastComputedName != null && lastComputedName.equals(projectName.getText())) {
                projectName.setText(computeValidProjectName(new File(projectLocation.getText()).getAbsolutePath(), 
                        ProjectUtils.getInformation(project).getName()));
            }            
        }
        updateProjectFolder();
        validateDialog();
    }
    
    public void removeUpdate(DocumentEvent e) {
        if (e.getDocument().equals(projectLocation.getDocument())) {
            if (lastComputedName != null && lastComputedName.equals(projectName.getText())) {
                projectName.setText(computeValidProjectName(new File(projectLocation.getText()).getAbsolutePath(), 
                        ProjectUtils.getInformation(project).getName()));
            }            
        }
        updateProjectFolder();
        validateDialog();
    }
    
    private void updateProjectFolder() {
        final File location = new File(projectLocation.getText());
        File projectFolderFile = location;
        if (isMove) {
            projectFolderFile = new File(location, project.getProjectDirectory().getNameExt());
        } else {
            projectFolderFile = new File(location, projectName.getText());
        }
        projectFolder.setText(projectFolderFile.getAbsolutePath());
    }
    
    public boolean isPanelValid() {
        return " ".equals(errorMessage.getText()) && !invalid;
    }

    private void validateDialog() {
        if (invalid) {
            //no reason to do anything:
            return ;
        }
        
        String newError = computeError();
        boolean changed = false;
        String currentError = errorMessage.getText();
        
        newError = newError != null ? newError : " ";
        changed = !currentError.equals(newError);
        
        errorMessage.setText(newError);
        
        if (changed) {
            changeSupport.fireChange();
        }
    }
    
    private String computeError() {
        File location = new File(projectLocation.getText());
        return DefaultProjectOperationsImplementation.computeError(location, projectName.getText(), projectFolder.getText(), false);
    }
    
    public void showProgress() {
        projectFolder.setEnabled(false);
        projectLocation.setEnabled(false);
        projectName.setEnabled(false);
        browse.setEnabled(false);
        
        ((CardLayout) progress.getLayout()).show(progress, "progress");
    }
}
