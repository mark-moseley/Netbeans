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

package org.netbeans.modules.java.project;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

public class BrokenReferencesAlertPanel extends JPanel {
    
    public BrokenReferencesAlertPanel() {
        initComponents();
        notAgain.setSelected(!JavaSettings.getDefault().isShowAgainBrokenRefAlert());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        notAgain = new javax.swing.JCheckBox();
        message = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 120));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "ACSN_BrokenReferencesAlertPanel"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "ACSD_BrokenReferencesAlertPanel"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/java/project/Bundle").getString("MSG_Broken_References_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(notAgain, org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_BrokenReferencesAlertPanel_notAgain"));
        notAgain.setMargin(new java.awt.Insets(0, 0, 0, 0));
        notAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notAgainActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 0, 0);
        add(notAgain, gridBagConstraints);
        notAgain.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "ACSN_BrokenReferencesAlertPanel_notAgain"));
        notAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "ACSD_BrokenReferencesAlertPanel_notAgain"));

        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_Broken_References"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 0, 0);
        add(message, gridBagConstraints);

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    private void notAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notAgainActionPerformed
        JavaSettings.getDefault().setShowAgainBrokenRefAlert(!notAgain.isSelected());
    }//GEN-LAST:event_notAgainActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel message;
    private javax.swing.JCheckBox notAgain;
    // End of variables declaration//GEN-END:variables


}
