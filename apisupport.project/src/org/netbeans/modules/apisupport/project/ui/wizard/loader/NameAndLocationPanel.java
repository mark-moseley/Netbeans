/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * the second panel in loaders wizard.
 *
 * @author Milos Kleint
 */
final class NameAndLocationPanel extends BasicWizardIterator.Panel {
    
    private NewLoaderIterator.DataModel data;
    
    /** Creates new NameAndLocationPanel */
    public NameAndLocationPanel(final WizardDescriptor setting, final NewLoaderIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_LoaderWizardTitle"));
        
        DocumentListener dListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                if (checkValidity()) {
                    updateData();
                }
            }
        };
        txtPrefix.getDocument().addDocumentListener(dListener);
        txtIcon.getDocument().addDocumentListener(dListener);
        
        if (comPackageName.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField txt = (JTextField)comPackageName.getEditor().getEditorComponent();
            txt.getDocument().addDocumentListener(dListener);
        }
    }
    
    protected void storeToDataModel() {
        updateData();
    }
    
    private void updateData() {
        data.setPackageName(comPackageName.getEditor().getItem().toString());
        String icon = txtIcon.getText().trim();
        data.setIconPath(icon.length() == 0 ? (String)null : icon);
        data.setPrefix(txtPrefix.getText().trim());
        NewLoaderIterator.generateFileChanges(data);
        createdFilesValue.setText(UIUtil.generateTextAreaContent(
                data.getCreatedModifiedFiles().getCreatedPaths()));
        modifiedFilesValue.setText(UIUtil.generateTextAreaContent(
                data.getCreatedModifiedFiles().getModifiedPaths()));
        //#68294 check if the paths for newly created files are valid or not..
        String[] invalid  = data.getCreatedModifiedFiles().getInvalidPaths();
        if (invalid.length > 0) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class, "ERR_ToBeCreateFileExists", invalid[0]));
        }
    }
    
    protected void readFromDataModel() {
        if (data.getPackageName() != null) {
            comPackageName.setSelectedItem(data.getPackageName());
        }
        txtPrefix.setText(data.getPrefix());
        txtIcon.setText(data.getPrefix());
        checkValidity();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_NameLocation_Title");
    }
    
    private boolean checkValidity() {
        if (txtPrefix.getText().trim().length() == 0) {
            setError(getMessage("ERR_Name_Prefix_Empty"));
            return false;
        }        
        if (!Utilities.isJavaIdentifier(txtPrefix.getText().trim())) {
            setError(getMessage("ERR_Name_Prefix_Invalid"));
            return false;
        }
        String path = txtIcon.getText().trim();
        if (path.length() != 0) {
            File fil = new File(path);
            if (!fil.exists()) {
                setError(NbBundle.getMessage(getClass(), "ERR_Icon_Invalid"));
                return false;
            }
        }
        String packageName = comPackageName.getEditor().getItem().toString().trim();
        
        if (packageName.length() == 0 || !UIUtil.isValidPackageName(packageName)) { //NOI18N
            setError(NbBundle.getMessage(getClass(), "ERR_Package_Invalid"));
            return false;
        }
        
        File icon = (path.length() == 0) ? null : new File(path);
        if (icon == null || !icon.exists()) {
            setWarning(UIUtil.getNoIconSelectedWarning(16,16));
        } else if (!UIUtil.isValidIcon(icon,16,16)) {
            setWarning(UIUtil.getIconDimensionWarning(icon,16,16));
        } else {        
            markValid();
        }
        return true;
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameAndLocationPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NameAndLocationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblPrefix = new javax.swing.JLabel();
        txtPrefix = new javax.swing.JTextField();
        lblIcon = new javax.swing.JLabel();
        txtIcon = new javax.swing.JTextField();
        btnIcon = new javax.swing.JButton();
        lblProjectName = new javax.swing.JLabel();
        txtProjectName = new JTextField(ProjectUtils.getInformation(this.data.getProject()).getDisplayName());
        lblPackageName = new javax.swing.JLabel();
        comPackageName = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        filler = new javax.swing.JLabel();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValue = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        lblPrefix.setLabelFor(txtPrefix);
        org.openide.awt.Mnemonics.setLocalizedText(lblPrefix, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_Prefix"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        add(lblPrefix, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        add(txtPrefix, gridBagConstraints);

        lblIcon.setLabelFor(txtIcon);
        org.openide.awt.Mnemonics.setLocalizedText(lblIcon, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_Icon"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblIcon, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(txtIcon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnIcon, org.openide.util.NbBundle.getMessage(NameAndLocationPanel.class, "LBL_Icon_Browse"));
        btnIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIconActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(btnIcon, gridBagConstraints);

        lblProjectName.setLabelFor(txtProjectName);
        org.openide.awt.Mnemonics.setLocalizedText(lblProjectName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(lblProjectName, gridBagConstraints);

        txtProjectName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(txtProjectName, gridBagConstraints);

        lblPackageName.setLabelFor(comPackageName);
        org.openide.awt.Mnemonics.setLocalizedText(lblPackageName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_PackageName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(lblPackageName, gridBagConstraints);

        comPackageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(comPackageName, gridBagConstraints);

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValue, gridBagConstraints);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(modifiedFilesValue, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void btnIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIconActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser(txtIcon.getText());
        int ret = chooser.showDialog(this, getMessage("LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            txtIcon.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_btnIconActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIcon;
    private javax.swing.JComboBox comPackageName;
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JLabel filler;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblPackageName;
    private javax.swing.JLabel lblPrefix;
    private javax.swing.JLabel lblProjectName;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JTextField txtIcon;
    private javax.swing.JTextField txtPrefix;
    private javax.swing.JTextField txtProjectName;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_NameAndLocationPanel"));
        comPackageName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PackageName"));
        txtIcon.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Icon"));
        txtPrefix.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Prefix"));
        btnIcon.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconButton"));
        txtProjectName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ProjectName"));
        createdFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CreatedFilesValue"));
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModifiedFilesValue"));
    }

}