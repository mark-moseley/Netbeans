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

package org.netbeans.modules.subversion.ui.copy;

import org.netbeans.modules.subversion.ui.wizards.*;

/**
 *
 * @author  Petr Kuzel
 */
public class CreateCopyPanel extends javax.swing.JPanel {

    /** Creates new form WorkdirPanel */
    public CreateCopyPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Name")); // NOI18N

        jLabel1.setLabelFor(messageTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Description")); // NOI18N

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);
        messageTextArea.getAccessibleContext().setAccessibleName("Copy Description");
        messageTextArea.getAccessibleContext().setAccessibleDescription("Copy Description");

        copyToLabel.setLabelFor(urlComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(copyToLabel, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_toFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseRepositoryButton, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Browse")); // NOI18N

        urlComboBox.setEditable(true);

        warningLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/ui/resources/warning.png")));
        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Warning")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(switchToCheckBox, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Switch")); // NOI18N
        switchToCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        switchToCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        copyFromLocalTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Source:");

        buttonGroup1.add(localRadioButton);
        localRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(localRadioButton, "Local Folder/File:");
        localRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        copyFromRemoteTextField.setEditable(false);

        buttonGroup1.add(remoteRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(remoteRadioButton, "Repository Folder/File:");
        remoteRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        remoteRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Revision:");

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, "Search...");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "Destination:");

        org.openide.awt.Mnemonics.setLocalizedText(skipCheckBox, org.openide.util.NbBundle.getMessage(CreateCopyPanel.class, "CTL_CopyForm_Skip")); // NOI18N
        skipCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        skipCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "(Empty means repository HEAD)");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "Preview:");

        previewTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(remoteRadioButton)
                            .add(localRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel4)
                                    .add(copyFromRemoteLabel))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(copyFromRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(searchButton))
                            .add(jLabel6)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, copyFromLocalTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                                    .add(copyFromRemoteTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(skipCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(copyToLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 688, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 739, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(switchToCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 704, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 702, Short.MAX_VALUE)
                        .add(113, 113, 113))
                    .add(layout.createSequentialGroup()
                        .add(jLabel7)
                        .add(85, 85, 85)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(previewTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(urlComboBox, 0, 584, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseRepositoryButton))))
                    .add(warningLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(copyFromLocalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(localRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(copyFromRemoteLabel))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(copyFromRemoteTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(remoteRadioButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(copyFromRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(searchButton)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(skipCheckBox)
                .add(31, 31, 31)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(copyToLabel)
                    .add(urlComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseRepositoryButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(previewTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(switchToCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warningLabel)
                .addContainerGap())
        );

        jLabel1.getAccessibleContext().setAccessibleDescription("Copy Description");
        browseRepositoryButton.getAccessibleContext().setAccessibleDescription("Browse Repository Folders");
        switchToCheckBox.getAccessibleContext().setAccessibleDescription("Switch to new copy after creation");
    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton browseRepositoryButton = new javax.swing.JButton();
    private javax.swing.ButtonGroup buttonGroup1;
    final javax.swing.JTextField copyFromLocalTextField = new javax.swing.JTextField();
    final javax.swing.JLabel copyFromRemoteLabel = new javax.swing.JLabel();
    final javax.swing.JTextField copyFromRemoteTextField = new javax.swing.JTextField();
    final javax.swing.JTextField copyFromRevisionTextField = new javax.swing.JTextField();
    final javax.swing.JLabel copyToLabel = new javax.swing.JLabel();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JRadioButton localRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JTextArea messageTextArea = new javax.swing.JTextArea();
    final javax.swing.JTextField previewTextField = new javax.swing.JTextField();
    final javax.swing.JRadioButton remoteRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JCheckBox skipCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox switchToCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JComboBox urlComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel warningLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables
    
}
