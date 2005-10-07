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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.AdminHandler;

import java.io.File;
import java.io.IOException;

/**
 * Settings panel for the Switch To Branch action.
 * 
 * @author Maros Sandor
 */
class SwitchBranchPanel extends javax.swing.JPanel {
    
    private final File[] roots;

    /** Creates new form SwitchBranchPanel */
    public SwitchBranchPanel(File [] roots) {
        this.roots = roots;
        initComponents();
        rbSwitchToTrunk.setSelected(CvsModuleConfig.getDefault().getDefaultValue("SwitchBranchSettings.switchToTrunk", true));
        rbSwitchToBranch.setSelected(CvsModuleConfig.getDefault().getDefaultValue("SwitchBranchSettings.switchToBranch", false));
        tfBranchName.setText(CvsModuleConfig.getDefault().getDefaultValue("SwitchBranchSettings.branchName", "existing_branch"));
        refreshComponents();
    }

    public void saveSettings() {
        CvsModuleConfig.getDefault().setDefaultValue("SwitchBranchSettings.switchToTrunk", rbSwitchToTrunk.isSelected());
        CvsModuleConfig.getDefault().setDefaultValue("SwitchBranchSettings.switchToBranch", rbSwitchToBranch.isSelected());
        CvsModuleConfig.getDefault().setDefaultValue("SwitchBranchSettings.branchName", tfBranchName.getText());
    }

    private void refreshComponents() {
        if (rbSwitchToTrunk.isSelected()) {
            tfBranchName.setEnabled(false);
            bBrowse.setEnabled(false);
        } else {
            tfBranchName.setEnabled(true);
            bBrowse.setEnabled(true);
        }
    }
    
    public String getBranchName() {
        return tfBranchName.getText();
    }

    public boolean isSwitchToTrunk() {
        return rbSwitchToTrunk.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgSwitchRadio = new javax.swing.ButtonGroup();
        rbSwitchToTrunk = new javax.swing.JRadioButton();
        rbSwitchToBranch = new javax.swing.JRadioButton();
        tfBranchName = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        bgSwitchRadio.add(rbSwitchToTrunk);
        org.openide.awt.Mnemonics.setLocalizedText(rbSwitchToTrunk, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_ToTrunk"));
        rbSwitchToTrunk.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_ToTrunk"));
        rbSwitchToTrunk.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 2, 0));
        rbSwitchToTrunk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbSwitchToTrunk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSwitch(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(rbSwitchToTrunk, gridBagConstraints);

        bgSwitchRadio.add(rbSwitchToBranch);
        org.openide.awt.Mnemonics.setLocalizedText(rbSwitchToBranch, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_ToBranch"));
        rbSwitchToBranch.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_ToBranch"));
        rbSwitchToBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbSwitchToBranch.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbSwitchToBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSwitch(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(rbSwitchToBranch, gridBagConstraints);

        tfBranchName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(tfBranchName, gridBagConstraints);
        tfBranchName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("ACSN_SwitchBranchForm_BranchName"));
        tfBranchName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("ACSD_SwitchBranchForm_BranchName"));

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_BrowseBranch"));
        bBrowse.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_BrowseBranch"));
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBranches(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(bBrowse, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void browseBranches(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBranches
        String module = null;
        CVSRoot root = null;
        for (int i = 0; i < roots.length; i++) {
            try {
                root = CVSRoot.parse(Utils.getCVSRootFor(roots[i]));
                AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
                module = ah.getRepositoryForDirectory(roots[i].getAbsolutePath(), "").substring(1);
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
        if (root == null) {
            return;
        }
        BranchSelector selector = new BranchSelector();
        String tag = selector.selectTag(root, module, null);
        if (tag != null) {
            tfBranchName.setText(tag);
        }
    }//GEN-LAST:event_browseBranches

    private void radioSwitch(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSwitch
        refreshComponents();
    }//GEN-LAST:event_radioSwitch
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.ButtonGroup bgSwitchRadio;
    private javax.swing.JRadioButton rbSwitchToBranch;
    private javax.swing.JRadioButton rbSwitchToTrunk;
    private javax.swing.JTextField tfBranchName;
    // End of variables declaration//GEN-END:variables
    
}
