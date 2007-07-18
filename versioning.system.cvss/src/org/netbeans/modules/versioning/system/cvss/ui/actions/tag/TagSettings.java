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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import java.io.IOException;
import java.io.File;

/**
 * Settings panel for the Tag command.
 *
 * @author Maros Sandor
 */
public class TagSettings extends javax.swing.JPanel implements DocumentListener {
    
    private final File[] roots;

    public TagSettings(File [] roots) {
        this.roots = roots;
        initComponents();
        tfName.getDocument().addDocumentListener(this);
    }
    
    void refreshComponents() {
        cbMoveTag.setEnabled(!cbDeleteTag.isSelected());
        cbDeleteTag.setEnabled(!cbMoveTag.isSelected());
        cbCheckModified.setEnabled(!cbDeleteTag.isSelected());
        JButton okButton = (JButton) getClientProperty("OKButton"); // NOI18N
        if (okButton != null) {
            okButton.setEnabled(Utils.isTagValid(tfName.getText()));
        }
    }

    public void insertUpdate(DocumentEvent e) {
        refreshComponents();
    }

    public void removeUpdate(DocumentEvent e) {
        refreshComponents();
    }

    public void changedUpdate(DocumentEvent e) {
        refreshComponents();
    }

    public void setCommand(TagCommand cmd) {
        cbMoveTag.setSelected(cmd.isOverrideExistingTag());
        cbCheckModified.setSelected(cmd.isCheckThatUnmodified());
        cbDeleteTag.setSelected(cmd.isDeleteTag());
        tfName.setText(cmd.getTag());
    }

    public void updateCommand(TagCommand cmd) {
        cmd.setOverrideExistingTag(cbMoveTag.isSelected());
        cmd.setCheckThatUnmodified(cbCheckModified.isSelected());
        cmd.setDeleteTag(cbDeleteTag.isSelected());
        cmd.setTag(tfName.getText());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbCheckModified = new javax.swing.JCheckBox();
        cbMoveTag = new javax.swing.JCheckBox();
        nameLabel = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        cbDeleteTag = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cbCheckModified, bundle.getString("CTL_TagForm_EnsureUptodate")); // NOI18N
        cbCheckModified.setToolTipText(bundle.getString("TT_TagForm_EnsureUptodate")); // NOI18N
        cbCheckModified.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCheckModifiedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbMoveTag, bundle.getString("CTL_TagForm_MoveExisting")); // NOI18N
        cbMoveTag.setToolTipText(bundle.getString("TT_TagForm_MoveExisting")); // NOI18N
        cbMoveTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMoveTagActionPerformed(evt);
            }
        });

        nameLabel.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, bundle.getString("CTL_TagForm_TagName")); // NOI18N

        tfName.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, bundle.getString("CTL_BrowseTag")); // NOI18N
        jButton1.setToolTipText(bundle.getString("TT_BrowseTag")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBrowseTag(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbDeleteTag, bundle.getString("CTL_TagForm_DeleteExisting")); // NOI18N
        cbDeleteTag.setToolTipText(bundle.getString("TT_TagForm_DeleteExisting")); // NOI18N
        cbDeleteTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDeleteTagActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(cbMoveTag, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .add(159, 159, 159))
            .add(layout.createSequentialGroup()
                .add(nameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1))
            .add(layout.createSequentialGroup()
                .add(cbCheckModified, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(159, 159, 159))
            .add(layout.createSequentialGroup()
                .add(cbDeleteTag, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .add(159, 159, 159))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(jButton1)
                    .add(tfName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbCheckModified, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbMoveTag)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbDeleteTag))
        );

        tfName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TagSettings.class, "ACSN_BrowseTag")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbDeleteTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDeleteTagActionPerformed
        refreshComponents();
    }//GEN-LAST:event_cbDeleteTagActionPerformed

    private void cbMoveTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMoveTagActionPerformed
        refreshComponents();
    }//GEN-LAST:event_cbMoveTagActionPerformed

    private void onBrowseTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onBrowseTag
        for (int i = 0; i < roots.length; i++) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(roots[i]));  // raises exception
                BranchSelector selector = new BranchSelector();
                String tag = selector.selectTag(roots[i]);
                if (tag != null) {
                    tfName.setText(tag);
                }
                return;
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
    }//GEN-LAST:event_onBrowseTag

    private void cbCheckModifiedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCheckModifiedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbCheckModifiedActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCheckModified;
    private javax.swing.JCheckBox cbDeleteTag;
    private javax.swing.JCheckBox cbMoveTag;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
