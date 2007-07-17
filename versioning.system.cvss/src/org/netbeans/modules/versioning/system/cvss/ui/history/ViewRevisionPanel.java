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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.CVSRoot;

import java.io.IOException;
import java.io.File;
import java.util.*;

/**
 * View Revision customization panel.
 *
 * @author Maros Sandor
 */
class ViewRevisionPanel extends javax.swing.JPanel {
    
    private final String VIEW_TRUNK_HEAD = "ViewRevisionPanel.ViewTrunkHEAD"; // NOI18N
    private final String VIEW_BRANCH = "ViewRevisionPanel.ViewBranch"; // NOI18N
    private final String VIEW_BRANCH_NAME = "ViewRevisionPanel.ViewBranchName"; // NOI18N

    private final VCSContext ctx;

    public ViewRevisionPanel(VCSContext ctx) {
        this.ctx = ctx;
        initComponents();
        rbBranch.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean(VIEW_BRANCH, true));
        rbTrunk.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean(VIEW_TRUNK_HEAD, false));
        tfTagName.setText(CvsModuleConfig.getDefault().getPreferences().get(VIEW_BRANCH_NAME, ""));
        refreshComponents();
    }

    String getRevision() {
        return rbTrunk.isSelected() ? "HEAD" : tfTagName.getText(); // NOI18N
    }
    
    void saveSettings() {
        CvsModuleConfig.getDefault().getPreferences().putBoolean(VIEW_TRUNK_HEAD, rbTrunk.isSelected());
        CvsModuleConfig.getDefault().getPreferences().putBoolean(VIEW_BRANCH, rbBranch.isSelected());
        CvsModuleConfig.getDefault().getPreferences().put(VIEW_BRANCH_NAME, tfTagName.getText());
    }
    
    void refreshComponents() {
        tfTagName.setEnabled(rbBranch.isSelected());
        bBrowse.setEnabled(rbBranch.isSelected());
    }
    
    private String browseBranches() {
        Set<File> roots = ctx.getRootFiles();
        for (File root : roots) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(root));  // raises exception
                BranchSelector selector = new BranchSelector();
                return selector.selectTag(root);
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        rbTrunk = new javax.swing.JRadioButton();
        rbBranch = new javax.swing.JRadioButton();
        tfTagName = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();

        buttonGroup1.add(rbTrunk);
        org.openide.awt.Mnemonics.setLocalizedText(rbTrunk, NbBundle.getMessage(ViewRevisionPanel.class, "ViewRevisionPanel.rbTrunk.text")); // NOI18N
        rbTrunk.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbTrunk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbTrunk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTrunkActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbBranch);
        org.openide.awt.Mnemonics.setLocalizedText(rbBranch, NbBundle.getMessage(ViewRevisionPanel.class, "ViewRevisionPanel.rbBranch.text")); // NOI18N
        rbBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbBranch.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbBranchActionPerformed(evt);
            }
        });

        tfTagName.setText(NbBundle.getMessage(ViewRevisionPanel.class, "ViewRevisionPanel.tfTagName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, NbBundle.getMessage(ViewRevisionPanel.class, "ViewRevisionPanel.bBrowse.text")); // NOI18N
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bBrowseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(rbBranch)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tfTagName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bBrowse))
                    .add(rbTrunk))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbBranch)
                    .add(bBrowse)
                    .add(tfTagName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbTrunk)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rbTrunk.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSN_ViewRevisionPanel.rbTrunk.text")); // NOI18N
        rbTrunk.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSD_ViewRevisionPanel.rbTrunk.text")); // NOI18N
        rbBranch.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSN_ViewRevisionPanel.rbBranch.text")); // NOI18N
        rbBranch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSD_ViewRevisionPanel.rbBranch.text")); // NOI18N
        tfTagName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSN_ViewRevisionPanel.tfTagName")); // NOI18N
        tfTagName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSD_ViewRevisionPanel.tfTagName")); // NOI18N
        bBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSN_ViewRevisionPanel.bBrowse.text")); // NOI18N
        bBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ViewRevisionPanel.class, "ACSD_ViewRevisionPanel.bBrowse.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void rbBranchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbBranchActionPerformed
        refreshComponents();
    }//GEN-LAST:event_rbBranchActionPerformed

    private void rbTrunkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbTrunkActionPerformed
        refreshComponents();
    }//GEN-LAST:event_rbTrunkActionPerformed

    private void bBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBrowseActionPerformed
        String tag = browseBranches();
        if (tag != null) {
            tfTagName.setText(tag);
        }
    }//GEN-LAST:event_bBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton rbBranch;
    private javax.swing.JRadioButton rbTrunk;
    private javax.swing.JTextField tfTagName;
    // End of variables declaration//GEN-END:variables

}
