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

package org.netbeans.modules.ant.freeform.ui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;

/**
 * @author  David Konecny
 */
public class BasicProjectInfoPanel extends javax.swing.JPanel {
    
    private DocumentListener documentListener;
    private ChangeListener listener;
    private boolean modified = false;
    
    public BasicProjectInfoPanel(String projectLocation, String antScript, String projectName, String projectFolder, ChangeListener listener) {
        initComponents();
        this.projectLocation.setText(projectLocation);
        this.antScript.setText(antScript);
        this.projectName.setText(projectName);
        this.projectFolder.setText(projectFolder);
        this.listener = listener;
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
            this.projectLocation.getDocument().addDocumentListener(documentListener);
            this.antScript.getDocument().addDocumentListener(documentListener);
            this.projectName.getDocument().addDocumentListener(documentListener);
            this.projectFolder.getDocument().addDocumentListener(documentListener);
        }
        
        public File getProjectLocation() {
            return getAsFile(projectLocation.getText());
        }
        
        public File getAntScript() {
            return getAsFile(antScript.getText());
        }
        
        public String getProjectName() {
            return projectName.getText();
        }
        
        public File getProjectFolder() {
            return getAsFile(projectFolder.getText());
        }
        
        public Boolean getMainProject() {
            return Boolean.valueOf(mainProject.isSelected());
        }
        
        public String getError() {
            if (projectLocation.getText().length() == 0) {
                return "Location of your existing project must be specified";
            }
            if (!getProjectLocation().exists()) {
                return "Project location does not exist";
            }
            if (antScript.getText().length() == 0) {
                return "Your existing Ant script must be specified";
            }
            if (!getAntScript().exists()) {
                return "The Ant script does not exist";
            }
            if (getProjectName().length() == 0) {
                return "Project name must be set";
            }
            if (projectFolder.getText().length() == 0) {
                return "Project folder must be set";
            }
            if (getAsFile(projectFolder.getText()+File.separatorChar+"nbproject").exists()) {
                return "Project folder already contains NetBeans project data";
            }
            return null;
        }
        
        private File getAsFile(String filename) {
            return FileUtil.normalizeFile(new File(filename));
        }
        
        private boolean ignoreEvent = false;
        
        private void update(DocumentEvent e) {
            // XXX: add some real logic here
            if (projectLocation.getDocument() == e.getDocument() && !modified) {
                if (getProjectLocation().exists() && getProjectLocation().isDirectory() && projectLocation.getText().length() > 0) {
                    ignoreEvent = true;
                    antScript.setText(getProjectLocation().getAbsolutePath()+File.separatorChar+"build.xml"); //NOI18N
                    projectFolder.setText(getProjectLocation().getAbsolutePath()); //NOI18N
                    ignoreEvent = false;
                }
            } else {
                if (!ignoreEvent) {
                    modified = true;
                }
            }
            listener.stateChanged(null);
        }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        antScript = new javax.swing.JTextField();
        projectName = new javax.swing.JTextField();
        projectFolder = new javax.swing.JTextField();
        browseAntScript = new javax.swing.JButton();
        browseProjectFolder = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        projectLocation = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        browseProjectLocation = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mainProject = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Select folder that contains existing J2SE project and locate the build script.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Ant Script:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Specify a name and project folder for NetBeans project data.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel3, gridBagConstraints);

        jLabel4.setText("Project Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        jLabel5.setText("Project Folder:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        add(antScript, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(projectName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(projectFolder, gridBagConstraints);

        browseAntScript.setText("Browse...");
        browseAntScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseAntScriptActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(browseAntScript, gridBagConstraints);

        browseProjectFolder.setText("Browse...");
        browseProjectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectFolderActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        add(browseProjectFolder, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        add(projectLocation, gridBagConstraints);

        jLabel6.setText("Location:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel6, gridBagConstraints);

        browseProjectLocation.setText("Browse...");
        browseProjectLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(browseProjectLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jSeparator1, gridBagConstraints);

        mainProject.setText("Set as Main Project");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(mainProject, gridBagConstraints);

    }//GEN-END:initComponents

    private void browseProjectLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (getProjectLocation().exists()) {
            chooser.setSelectedFile(getProjectLocation());
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectLoc = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectLocation.setText(projectLoc.getAbsolutePath());
        }
    }//GEN-LAST:event_browseProjectLocationActionPerformed

    private void browseProjectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectFolderActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (getProjectFolder().exists()) {
            chooser.setSelectedFile(getProjectFolder());
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            projectFolder.setText(projectDir.getAbsolutePath());
        }                    
    }//GEN-LAST:event_browseProjectFolderActionPerformed

    private void browseAntScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseAntScriptActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        if (getAntScript().exists()) {
            chooser.setSelectedFile(getAntScript());
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File script = FileUtil.normalizeFile(chooser.getSelectedFile());
            antScript.setText(script.getAbsolutePath());
        }            
    }//GEN-LAST:event_browseAntScriptActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField antScript;
    private javax.swing.JButton browseAntScript;
    private javax.swing.JButton browseProjectFolder;
    private javax.swing.JButton browseProjectLocation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JTextField projectFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JTextField projectName;
    // End of variables declaration//GEN-END:variables
    
}
