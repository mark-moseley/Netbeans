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

package org.netbeans.modules.j2ee.common.ui;

import javax.swing.JPanel;


/**
 * Broken/missing server alert panel.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * servers. Once BrokenReferencesSupport will support it, this class should be 
 * removed.
 */
public class BrokenServerAlertPanel extends JPanel {
    
    public BrokenServerAlertPanel() {
        initComponents();
        notAgain.setSelected(!FoldersListSettings.getDefault().isShowAgainBrokenServerAlert());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        notAgain = new javax.swing.JCheckBox();
        message = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 100));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel"));
        org.openide.awt.Mnemonics.setLocalizedText(notAgain, org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server_Again"));
        notAgain.setMargin(new java.awt.Insets(0, 0, 0, 0));
        notAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notAgainActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 0, 0);
        add(notAgain, gridBagConstraints);
        notAgain.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel_notAgain"));
        notAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel_notAgain"));

        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        add(message, gridBagConstraints);
        message.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel"));
        message.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel"));

    }//GEN-END:initComponents

    private void notAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notAgainActionPerformed
        FoldersListSettings.getDefault().setShowAgainBrokenServerAlert(!notAgain.isSelected());
    }//GEN-LAST:event_notAgainActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel message;
    private javax.swing.JCheckBox notAgain;
    // End of variables declaration//GEN-END:variables


}
