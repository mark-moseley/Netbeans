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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  pb97924
 */
public class ImportLocationVisual extends javax.swing.JPanel implements DocumentListener {
    
    private ImportWebProjectWizardIterator.ThePanel panel;
    private Document moduleDocument;
    private Document locationDocument;
    private Document nameDocument;
    private boolean contextModified = false;
    
    /** Creates new form TestPanel */
    public ImportLocationVisual (ImportWebProjectWizardIterator.ThePanel panel) {
        this.panel = panel;
        initComponents ();
        
        setName(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_IW_ImportTitle")); //NOI18N
        
        locationDocument = projectLocationTextField.getDocument ();
        locationDocument.addDocumentListener (this);
        projectNameTextField.getDocument ().addDocumentListener (this);
        moduleDocument = moduleLocationTextField.getDocument ();
        moduleDocument.addDocumentListener (this);
        nameDocument = projectNameTextField.getDocument();
        nameDocument.addDocumentListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSrcLocationDesc = new javax.swing.JLabel();
        jLabelSrcLocation = new javax.swing.JLabel();
        moduleLocationTextField = new javax.swing.JTextField();
        jButtonSrcLocation = new javax.swing.JButton();
        jLabelPrjLocationDesc = new javax.swing.JLabel();
        jLabelPrjName = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        jLabelPrjLocation = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        jButtonPrjLocation = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabelSrcLocationDesc.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrcDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jLabelSrcLocationDesc, gridBagConstraints);

        jLabelSrcLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSrcLocation.setLabelFor(moduleLocationTextField);
        jLabelSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationSrc_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelSrcLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(moduleLocationTextField, gridBagConstraints);

        jButtonSrcLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonSrcLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSrcLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonSrcLocation, gridBagConstraints);

        jLabelPrjLocationDesc.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_IW_LocationPrjDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 0);
        add(jLabelPrjLocationDesc, gridBagConstraints);

        jLabelPrjName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjName.setLabelFor(projectNameTextField);
        jLabelPrjName.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelPrjName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectNameTextField, gridBagConstraints);

        jLabelPrjLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelPrjLocation.setLabelFor(projectLocationTextField);
        jLabelPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ProjectLocation_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelPrjLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(projectLocationTextField, gridBagConstraints);

        jButtonPrjLocation.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_BrowseLocation_Button"));
        jButtonPrjLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrjLocationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonPrjLocation, gridBagConstraints);

        createdFolderLabel.setLabelFor(createdFolderTextField);
        createdFolderLabel.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(createdFolderLabel, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(createdFolderTextField, gridBagConstraints);

        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_ContextPath_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jTextFieldContextPath, gridBagConstraints);

    }//GEN-END:initComponents

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void jButtonPrjLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrjLocationActionPerformed
        JFileChooser chooser = createChooser();    
        if (chooser.APPROVE_OPTION == chooser.showDialog(this, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SelectProjectLocation"))) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonPrjLocationActionPerformed

    private void jButtonSrcLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSrcLocationActionPerformed
        JFileChooser chooser = createChooser();    
        if (JFileChooser.APPROVE_OPTION == chooser.showDialog(this, NbBundle.getMessage(ImportLocationVisual.class, "LBL_NWP1_SelectWebModuleRootLocation"))) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            moduleLocationTextField.setText( projectDir.getAbsolutePath());
        }            
    }//GEN-LAST:event_jButtonSrcLocationActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFolderLabel;
    public javax.swing.JTextField createdFolderTextField;
    private javax.swing.JButton jButtonPrjLocation;
    private javax.swing.JButton jButtonSrcLocation;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelPrjLocation;
    private javax.swing.JLabel jLabelPrjLocationDesc;
    private javax.swing.JLabel jLabelPrjName;
    private javax.swing.JLabel jLabelSrcLocation;
    private javax.swing.JLabel jLabelSrcLocationDesc;
    protected javax.swing.JTextField jTextFieldContextPath;
    public javax.swing.JTextField moduleLocationTextField;
    public javax.swing.JTextField projectLocationTextField;
    public javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        return chooser;
    }
    
    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    // End if implementation of DocumentListener -------------------------------
    
    /** Handles changes in the project name and project directory
     */
    private void updateTexts(DocumentEvent e) {
        if (e.getDocument() == moduleDocument) {
            String moduleFolder = moduleLocationTextField.getText().trim();
            FileObject fo;
            try {
                fo= FileUtil.toFileObject(new File(moduleFolder));
            } catch (IllegalArgumentException exc) {
                return;
            }
            if (fo != null && panel.isSuitableProjectRoot(fo)) {
                projectLocationTextField.setText (moduleFolder);
                createdFolderTextField.setText (moduleFolder);
            }
        } else if (e.getDocument() == locationDocument || !projectLocationTextField.getText ().equals (moduleLocationTextField.getText ())) {
            StringBuffer folder = new StringBuffer(projectLocationTextField.getText().trim());
            if (!folder.toString ().endsWith(File.separator))
                folder.append(File.separatorChar);
            folder.append(projectNameTextField.getText().trim());
            createdFolderTextField.setText (folder.toString());
        }

        if (e.getDocument() == nameDocument && !contextModified) {
            jTextFieldContextPath.setText("/" + projectNameTextField.getText());
        }

        panel.fireChangeEvent ();
    }
    
}
