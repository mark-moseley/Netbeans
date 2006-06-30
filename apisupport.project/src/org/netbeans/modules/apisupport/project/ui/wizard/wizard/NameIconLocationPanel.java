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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The second panel in the <em>New Wizard Wizard</em>.
 *
 * @author Martin Krauskopf
 */
final class NameIconLocationPanel extends BasicWizardIterator.Panel {
    
    private static final Map PURE_TEMPLATES_FILTER = new HashMap(2);
    
    private static final String TEMPLATES_DIR = "Templates"; // NOI18N
    private static final String DEFAULT_CATEGORY_PATH = TEMPLATES_DIR + "/Other"; // NOI18N
    
    
    static {
        PURE_TEMPLATES_FILTER.put("template", Boolean.TRUE); // NOI18N
        PURE_TEMPLATES_FILTER.put("simple", Boolean.FALSE); // NOI18N
    }
    
    private boolean categoriesLoaded;
    private boolean firstTime = true;
    private DataModel data;
    
    private static final String ENTER_LABEL = getMessage("CTL_EnterLabel");
    private static final String NONE_LABEL = getMessage("CTL_None");
    
    /** Creates new NameIconLocationPanel */
    public NameIconLocationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_WizardWizardTitle"));
        DocumentListener updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
        classNamePrefix.getDocument().addDocumentListener(updateListener);
        displayName.getDocument().addDocumentListener(updateListener);
        icon.getDocument().addDocumentListener(updateListener);        
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(updateListener);
        }
        if (category.getEditor().getEditorComponent() instanceof JTextField) {
            JTextComponent txt = (JTextComponent) category.getEditor().getEditorComponent();
            txt.getDocument().addDocumentListener(updateListener);
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_NameIconLocation_Title");
    }
    
    private String getCategoryPath() {
        String path = UIUtil.getSFSPath(category, TEMPLATES_DIR);
        return path == null ? DEFAULT_CATEGORY_PATH : path;
    }
    
    protected void storeToDataModel() {
        data.setClassNamePrefix(getClassNamePrefix());
        data.setPackageName(packageName.getEditor().getItem().toString());
        if (data.isFileTemplateType()) {
            data.setDisplayName(displayName.getText());
            if (icon.getText().trim().length() > 0) {
                data.setIcon(icon.getText().equals(NONE_LABEL) ? null : icon.getText());
            }
            data.setCategory(getCategoryPath());
        }
    }
    
    protected void readFromDataModel() {
        boolean isFileTemplate = data.isFileTemplateType();
        displayName.setVisible(isFileTemplate);
        displayNameTxt.setVisible(isFileTemplate);
        category.setVisible(isFileTemplate);
        categoryTxt.setVisible(isFileTemplate);
        icon.setVisible(isFileTemplate);
        iconButton.setVisible(isFileTemplate);
        iconTxt.setVisible(isFileTemplate);
        if (isFileTemplate && !categoriesLoaded) {
            category.setModel(UIUtil.createLayerPresenterComboModel(
                    data.getProject(), TEMPLATES_DIR, PURE_TEMPLATES_FILTER));
            categoriesLoaded = true;
        }
        if (firstTime) {
            if (data.getPackageName() != null) {
                packageName.setSelectedItem(data.getPackageName());
            }
            firstTime = false;
            markInvalid();
        } else {
            updateData();
        }
        
    }
    
    private void updateData() {
        storeToDataModel();
        if (checkValidity()) {
            CreatedModifiedFiles files = data.getCreatedModifiedFiles();
            createdFiles.setText(UIUtil.generateTextAreaContent(files.getCreatedPaths()));
            modifiedFiles.setText(UIUtil.generateTextAreaContent(files.getModifiedPaths()));
        }
    }
    
    private boolean checkValidity() {
        boolean valid = false;
        String pName = packageName.getEditor().getItem().toString().trim();
        if (!Utilities.isJavaIdentifier(getClassNamePrefix())) {
            setError(getMessage("MSG_ClassNameMustBeValidJavaIdentifier"));
        } else if (data.isFileTemplateType() &&
                (getDisplayName().equals("") || getDisplayName().equals(ENTER_LABEL))) {
            setError(getMessage("MSG_DisplayNameMustBeEntered"));
        } else if (pName.length() == 0 || !UIUtil.isValidPackageName(pName)) {
            setError(getMessage("ERR_Package_Invalid"));
        } else if (data.getCreatedModifiedFiles().getInvalidPaths().length > 0) {
            //#68294 check if the paths for newly created files are valid or not..
            String[] invalid  = data.getCreatedModifiedFiles().getInvalidPaths();
            setError(NbBundle.getMessage(NameIconLocationPanel.class, "ERR_ToBeCreateFileExists", invalid[0]));
        } else if (!Util.isValidSFSPath(getCategoryPath())) {
            setError(getMessage("ERR_Category_Invalid"));
        } else  {
            String path = icon.getText().trim();
            File iconFile = (path.length() == 0) ? null : new File(path);
            if (icon.isVisible() && (iconFile == null || !iconFile.exists())) {
                setWarning(UIUtil.getNoIconSelectedWarning(16,16));
            } else if (icon.isVisible() && !UIUtil.isValidIcon(iconFile,16,16)) {
                setWarning(UIUtil.getIconDimensionWarning(iconFile,16,16));
            } else {
                markValid();
            }
            valid = true;
        }
        return valid;
    }
    
    private String getDisplayName() {
        return displayName.getText().trim();
    }
    
    private String getClassNamePrefix() {
        return classNamePrefix.getText().trim();
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameIconLocationPanel.class);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_NameIconLocationPanel"));
        classNamePrefix.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ClassNamePrefix"));
        displayName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_DisplayName"));
        category.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Category"));
        icon.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Icon"));
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_IconBrowse"));
        project.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ProjectName"));
        packageName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_PackageName"));
        createdFiles.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_CreatedFiles"));
        modifiedFiles.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ModifiedFiles"));
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NameIconLocationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classNamePrefixTxt = new javax.swing.JLabel();
        classNamePrefix = new javax.swing.JTextField();
        displayNameTxt = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        categoryTxt = new javax.swing.JLabel();
        category = new javax.swing.JComboBox();
        iconTxt = new javax.swing.JLabel();
        icon = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        projectTxt = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        packageNameTxt = new javax.swing.JLabel();
        packageName = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        createdFilesTxt = new javax.swing.JLabel();
        modifiedFilesTxt = new javax.swing.JLabel();
        createdFilesSP = new javax.swing.JScrollPane();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesSP = new javax.swing.JScrollPane();
        modifiedFiles = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        classNamePrefixTxt.setLabelFor(classNamePrefix);
        org.openide.awt.Mnemonics.setLocalizedText(classNamePrefixTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ClassNamePrefix"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(classNamePrefixTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(classNamePrefix, gridBagConstraints);

        displayNameTxt.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(displayNameTxt, gridBagConstraints);

        displayName.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_EnterLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(displayName, gridBagConstraints);

        categoryTxt.setLabelFor(category);
        org.openide.awt.Mnemonics.setLocalizedText(categoryTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 12);
        add(categoryTxt, gridBagConstraints);

        category.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(category, gridBagConstraints);

        iconTxt.setLabelFor(icon);
        org.openide.awt.Mnemonics.setLocalizedText(iconTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(iconTxt, gridBagConstraints);

        icon.setEditable(false);
        icon.setText(org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "CTL_None"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(icon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_Icon_Browse"));
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(18, 12, 0, 0);
        add(iconButton, gridBagConstraints);

        projectTxt.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectTxt, gridBagConstraints);

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        packageNameTxt.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageNameTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_PackageName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 36, 12);
        add(packageNameTxt, gridBagConstraints);

        packageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 36, 0);
        add(packageName, gridBagConstraints);

        createdFilesTxt.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(createdFilesTxt, gridBagConstraints);

        modifiedFilesTxt.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesTxt, org.openide.util.NbBundle.getMessage(NameIconLocationPanel.class, "LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(modifiedFilesTxt, gridBagConstraints);

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        createdFilesSP.setViewportView(createdFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(createdFilesSP, gridBagConstraints);

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setToolTipText("modifiedFilesValue");
        modifiedFiles.setBorder(null);
        modifiedFilesSP.setViewportView(modifiedFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(modifiedFilesSP, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser(icon.getText());
        int ret = chooser.showDialog(this, getMessage("LBL_Select"));
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            icon.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_iconButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox category;
    private javax.swing.JLabel categoryTxt;
    private javax.swing.JTextField classNamePrefix;
    private javax.swing.JLabel classNamePrefixTxt;
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JScrollPane createdFilesSP;
    private javax.swing.JLabel createdFilesTxt;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameTxt;
    private javax.swing.JTextField icon;
    private javax.swing.JButton iconButton;
    private javax.swing.JLabel iconTxt;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JScrollPane modifiedFilesSP;
    private javax.swing.JLabel modifiedFilesTxt;
    private javax.swing.JComboBox packageName;
    private javax.swing.JLabel packageNameTxt;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectTxt;
    // End of variables declaration//GEN-END:variables
    
}
