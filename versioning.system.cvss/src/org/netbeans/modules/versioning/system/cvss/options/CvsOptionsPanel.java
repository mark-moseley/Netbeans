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
package org.netbeans.modules.versioning.system.cvss.options;

import org.netbeans.modules.versioning.util.StringSelector;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;

/**
 * UI panel for CVS settings.
 * 
 * @author Maros Sandor
 */
class CvsOptionsPanel extends javax.swing.JPanel {
    
    /** Creates new form CvsOptionsPanel */
    public CvsOptionsPanel() {
        initComponents();
        refreshComponents();
    }
    
    private void refreshComponents() {
        wrapCharCount.setEnabled(wrapCommitMessages.isSelected());
    }
    
    public JCheckBox getExcludeNewFiles() {
        return excludeNewFiles;
    }

    public JTextField getStatusLabelFormat() {
        return statusLabelFormat;
    }

    public JButton getStatusVarsBrowse() {
        return statusVarsBrowse;
    }

    public JTextField getWrapCharCount() {
        return wrapCharCount;
    }

    public JCheckBox getWrapCommitMessages() {
        return wrapCommitMessages;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        statusLabelFormat = new javax.swing.JTextField();
        statusVarsBrowse = new javax.swing.JButton();
        excludeNewFiles = new javax.swing.JCheckBox();
        wrapCommitMessages = new javax.swing.JCheckBox();
        wrapCharCount = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 5));

        jLabel1.setLabelFor(statusLabelFormat);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.jLabel1.toolTipText")); // NOI18N

        statusLabelFormat.setText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.statusLabelFormat.text")); // NOI18N
        statusLabelFormat.setToolTipText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.statusLabelFormat.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(statusVarsBrowse, org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.statusVarsBrowse.text")); // NOI18N
        statusVarsBrowse.setToolTipText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.statusVarsBrowse.toolTipText")); // NOI18N
        statusVarsBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusVarsBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(excludeNewFiles, org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.excludeNewFiles.text")); // NOI18N
        excludeNewFiles.setToolTipText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.excludeNewFiles.toolTipText")); // NOI18N
        excludeNewFiles.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        excludeNewFiles.setMargin(new java.awt.Insets(0, 0, 0, 0));
        excludeNewFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeNewFilesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wrapCommitMessages, org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.wrapCommitMessages.text")); // NOI18N
        wrapCommitMessages.setToolTipText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.wrapCommitMessages.toolTipText")); // NOI18N
        wrapCommitMessages.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        wrapCommitMessages.setMargin(new java.awt.Insets(0, 0, 0, 0));
        wrapCommitMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapCommitMessagesActionPerformed(evt);
            }
        });

        wrapCharCount.setColumns(4);
        wrapCharCount.setText(org.openide.util.NbBundle.getMessage(CvsOptionsPanel.class, "CvsOptionsPanel.wrapCharCount.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabelFormat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusVarsBrowse))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(excludeNewFiles)
                    .add(layout.createSequentialGroup()
                        .add(wrapCommitMessages)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(wrapCharCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(165, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(statusVarsBrowse)
                    .add(statusLabelFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(excludeNewFiles)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wrapCommitMessages)
                    .add(wrapCharCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void statusVarsBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusVarsBrowseActionPerformed
    List<String> formatVars = new ArrayList<String>();
    formatVars.add(NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_1"));
    formatVars.add(NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_2"));
    formatVars.add(NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_3"));
    formatVars.add(NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_4"));
    String newVariable = StringSelector.select(NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_Title"),
            NbBundle.getMessage(CvsOptionsPanel.class, "LBL_FormatVariable_Prompt"),
            formatVars);
    if (newVariable != null) {
        statusLabelFormat.replaceSelection(newVariable.substring(0, newVariable.lastIndexOf('}') + 1));
    }
}//GEN-LAST:event_statusVarsBrowseActionPerformed

private void excludeNewFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeNewFilesActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_excludeNewFilesActionPerformed

private void wrapCommitMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapCommitMessagesActionPerformed
    refreshComponents();
}//GEN-LAST:event_wrapCommitMessagesActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox excludeNewFiles;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField statusLabelFormat;
    private javax.swing.JButton statusVarsBrowse;
    private javax.swing.JTextField wrapCharCount;
    private javax.swing.JCheckBox wrapCommitMessages;
    // End of variables declaration//GEN-END:variables

}
