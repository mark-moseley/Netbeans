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
package org.netbeans.modules.subversion.ui.update;

/**
 *
 * @author  Tomas Stupka
 */
public class RevertModificationsPanel extends javax.swing.JPanel {

    /** Creates new form ReverModificationsPanel */
    public RevertModificationsPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        buttonGroup.add(localChangesRadioButton);
        localChangesRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(localChangesRadioButton, "Revert &Local Changes");
        localChangesRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        localChangesRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        localChangesRadioButton.getAccessibleContext().setAccessibleDescription("Revert Local Changes");

        buttonGroup.add(moreCommitsRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(moreCommitsRadioButton, "Revert Modifications from &Previous Commits");
        moreCommitsRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        moreCommitsRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        moreCommitsRadioButton.getAccessibleContext().setAccessibleDescription("Revert Modifications from Previous Commits");

        jLabel2.setLabelFor(startRevisionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "S&tarting Revision:");
        jLabel2.getAccessibleContext().setAccessibleDescription("Starting Revision for Revert");

        startRevisionTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(startSearchButton, "Se&arch...");
        startSearchButton.setEnabled(false);
        startSearchButton.getAccessibleContext().setAccessibleDescription("Search Starting Revision");

        jLabel9.setText("(empty means repository HEAD)");

        endRevisionTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(endSearchButton, "Sear&ch...");
        endSearchButton.setEnabled(false);
        endSearchButton.getAccessibleContext().setAccessibleDescription("Search Ending Revision");

        jLabel3.setLabelFor(endRevisionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "En&ding Revision:");
        jLabel3.getAccessibleContext().setAccessibleDescription("Ending Revision for Revert");

        buttonGroup.add(oneCommitRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(oneCommitRadioButton, "Revert Modifications from &Single Commit");
        oneCommitRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oneCommitRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        oneCommitRadioButton.getAccessibleContext().setAccessibleDescription("Revert Modifications from Single Commit");

        jLabel4.setLabelFor(oneRevisionTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "&Revision:");
        jLabel4.getAccessibleContext().setAccessibleDescription("Desired Revision for Revert");

        oneRevisionTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(oneRevisionSearchButton, "S&earch...");
        oneRevisionSearchButton.setEnabled(false);
        oneRevisionSearchButton.getAccessibleContext().setAccessibleDescription("Search Revision");

        jLabel10.setText("(empty means repository HEAD)");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(moreCommitsRadioButton)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, endRevisionTextField)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, startRevisionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(startSearchButton)
                                    .add(endSearchButton)))
                            .add(layout.createSequentialGroup()
                                .add(oneRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(oneRevisionSearchButton))))
                    .add(oneCommitRadioButton)
                    .add(localChangesRadioButton)
                    .add(layout.createSequentialGroup()
                        .add(141, 141, 141)
                        .add(jLabel9)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(localChangesRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(oneCommitRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(oneRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(oneRevisionSearchButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(moreCommitsRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(startRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(startSearchButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(endRevisionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(endSearchButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel10)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.ButtonGroup buttonGroup = new javax.swing.ButtonGroup();
    final javax.swing.JTextField endRevisionTextField = new javax.swing.JTextField();
    final javax.swing.JButton endSearchButton = new javax.swing.JButton();
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    final javax.swing.JRadioButton localChangesRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JRadioButton moreCommitsRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JRadioButton oneCommitRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JButton oneRevisionSearchButton = new javax.swing.JButton();
    final javax.swing.JTextField oneRevisionTextField = new javax.swing.JTextField();
    final javax.swing.JTextField startRevisionTextField = new javax.swing.JTextField();
    final javax.swing.JButton startSearchButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables
    
}
