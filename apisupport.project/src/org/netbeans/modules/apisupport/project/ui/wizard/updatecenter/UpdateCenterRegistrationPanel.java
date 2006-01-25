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

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicVisualPanel;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The panel in the <em>New Update Center Wizard</em>.
 *
 * @author Jiri Rechtacek
 */
final class UpdateCenterRegistrationPanel extends BasicWizardIterator.Panel {
    
    private static final String ENTER_LABEL = getMessage("CTL_EnterLabel"); //NOI18N
    private static final String NONE_LABEL = getMessage("CTL_None"); //NOI18N
    
    private DataModel data;
    private DocumentListener updateListener;
    
    /**
     * Creates new UpdateCenterRegistrationPanel
     */
    public UpdateCenterRegistrationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();

        putClientProperty("NewFileWizard_Title", getMessage("LBL_NewUpdateCenterWizardTitle")); //NOI18N
        
        updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
    }
    
    private void addListeners() {
        ucUrl.getDocument ().addDocumentListener (updateListener);
        displayName.getDocument ().addDocumentListener (updateListener);
    }
    
    private void removeListeners() {
        ucUrl.getDocument ().removeDocumentListener (updateListener);
        displayName.getDocument ().removeDocumentListener (updateListener);
    }
    
    protected String getPanelName() {
        return getMessage("LBL_UpdateCenterRegistrationPanel_Title"); //NOI18N
    }
    
    protected void storeToDataModel() {
        removeListeners();
        storeBaseData();
    }
    
    protected void readFromDataModel() {
        updateData();
        addListeners();
    }
    
    void updateData() {
        storeBaseData ();
        if (checkValidity ()) {
            CreatedModifiedFiles files = data.refreshCreatedModifiedFiles();
            createdFiles.setText(UIUtil.generateTextAreaContent(files.getCreatedPaths()));
            modifiedFiles.setText(UIUtil.generateTextAreaContent(files.getModifiedPaths()));
        }
    }
    
    /** Data needed to compute CMF. ClassName, packageName, icon. */
    private void storeBaseData() {
        data.setUpdateCenterURL (ucUrl.getText ().trim ());
        data.setUpdateCenterIconPath (getIconPath());
        data.setUpdateCenterDisplayName (displayName.getText ().trim ());
    }
    
    private String getIconPath() {
        return icon.getText().equals(NONE_LABEL) ? null : icon.getText();
    }
    
    private boolean checkValidity() {
        boolean result = false;
        if (data.getUpdateCenterURL ().length () == 0) {
            setErrorMessage (getMessage ("ERR_Url_Is_Empty")); //NOI18N
        } else if (data.getUpdateCenterDisplayName ().length () == 0) {
            setErrorMessage (getMessage ("ERR_Empty_Display_Name")); //NOI18N
        } else {
            setValid (Boolean.TRUE);
            result = true;
            if (! data.getUpdateCenterURL ().endsWith (".xml")) {
                setErrorMessage (getMessage ("WRN_Url_dont_xml_file"), false); //NOI18N
            } else {
                
                try {
                    // try transform to URL
                    new URL (data.getUpdateCenterURL ());

                    // all is ok
                    setErrorMessage (null);
                } catch (MalformedURLException ex) {
                    setErrorMessage (NbBundle.getMessage (UpdateCenterRegistrationPanel.class, "WRN_Url_cannot_be_created", ex.getLocalizedMessage ()), false);
                }
                
            }
        }

        return result;
    }
    
    private String getDisplayName() {
        return displayName.getText().trim();
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(UpdateCenterRegistrationPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(UpdateCenterRegistrationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ucUrlLabel = new javax.swing.JLabel();
        ucUrl = new javax.swing.JTextField();
        displayNameLabel = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        iconLabel = new javax.swing.JLabel();
        icon = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        projectLabel = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        createdFilesLabel = new javax.swing.JLabel();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesLabel = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACN_UpdateCenterRegistrationPanel", new Object[] {}));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel", new Object[] {}));
        ucUrlLabel.setLabelFor(ucUrl);
        org.openide.awt.Mnemonics.setLocalizedText(ucUrlLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_UpdateCenterURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(ucUrlLabel, gridBagConstraints);
        ucUrlLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_ucUrlLabel", new Object[] {}));

        ucUrl.setText(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "CTL_SampleURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(ucUrl, gridBagConstraints);

        displayNameLabel.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(displayNameLabel, gridBagConstraints);
        displayNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_displayNameLabel", new Object[] {}));

        displayName.setText(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "CTL_SampleName", new Object[] {project.getText ()}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(displayName, gridBagConstraints);

        iconLabel.setLabelFor(icon);
        org.openide.awt.Mnemonics.setLocalizedText(iconLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_Icon"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(iconLabel, gridBagConstraints);
        iconLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_IconLabel", new Object[] {}));

        icon.setEditable(false);
        icon.setText(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "CTL_None"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(icon, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_Icon_Browse"));
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(iconButton, gridBagConstraints);
        iconButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_iconButton", new Object[] {}));

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectLabel, gridBagConstraints);
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_projectLabel", new Object[] {}));

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        createdFilesLabel.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFilesLabel, gridBagConstraints);
        createdFilesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_createdFilesLabel", new Object[] {}));

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFiles, gridBagConstraints);

        modifiedFilesLabel.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFilesLabel, gridBagConstraints);
        modifiedFilesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_modifiedFilesLabel", new Object[] {}));

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setToolTipText("modifiedFilesValue");
        modifiedFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFiles, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser (icon.getText ());
        int ret = chooser.showDialog(this, getMessage ("LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile ();
            icon.setText (file.getAbsolutePath());
            updateData ();
        }
    }//GEN-LAST:event_iconButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel createdFilesLabel;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField icon;
    private javax.swing.JButton iconButton;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JLabel modifiedFilesLabel;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField ucUrl;
    private javax.swing.JLabel ucUrlLabel;
    // End of variables declaration//GEN-END:variables
    
}