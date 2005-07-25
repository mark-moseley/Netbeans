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

package org.netbeans.modules.versioning.system.cvss.ui.history;

/**
 * Packages search criteria in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchCriteriaPanel extends javax.swing.JPanel {
    
    /** Creates new form SearchCriteriaPanel */
    public SearchCriteriaPanel() {
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        refreshComponents();
    }

    private void refreshComponents() {
        tfCommitMessage.setEnabled(cbUseCommitMessage.isSelected());
        tfFrom.setEnabled(cbUseFrom.isSelected());
        tfTo.setEnabled(cbUseTo.isSelected());
        tfUsername.setEnabled(cbUseCommitMessage1.isSelected());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbUseCommitMessage = new javax.swing.JCheckBox();
        tfCommitMessage = new javax.swing.JTextField();
        cbUseCommitMessage1 = new javax.swing.JCheckBox();
        tfUsername = new javax.swing.JTextField();
        cbUseFrom = new javax.swing.JCheckBox();
        cbUseTo = new javax.swing.JCheckBox();
        tfFrom = new javax.swing.JTextField();
        tfTo = new javax.swing.JTextField();
        bBrowseFrom = new javax.swing.JButton();
        bBrowseTo = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 12, 0, 11)));
        cbUseCommitMessage.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseCommitMessage"));
        cbUseCommitMessage.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbUseCommitMessage.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseCommitMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComponents(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(cbUseCommitMessage, gridBagConstraints);

        tfCommitMessage.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tfCommitMessage, gridBagConstraints);

        cbUseCommitMessage1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseUsername"));
        cbUseCommitMessage1.setBorder(null);
        cbUseCommitMessage1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseCommitMessage1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComponents(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(cbUseCommitMessage1, gridBagConstraints);

        tfUsername.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tfUsername, gridBagConstraints);

        cbUseFrom.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseFrom"));
        cbUseFrom.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbUseFrom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComponents(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(cbUseFrom, gridBagConstraints);

        cbUseTo.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_UseTo"));
        cbUseTo.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbUseTo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbUseTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComponents(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        add(cbUseTo, gridBagConstraints);

        tfFrom.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfFrom, gridBagConstraints);

        tfTo.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfTo, gridBagConstraints);

        bBrowseFrom.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_BrowseFrom"));
        bBrowseFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onFromBrowse(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(bBrowseFrom, gridBagConstraints);

        bBrowseTo.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_BrowseTo"));
        bBrowseTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onToBrowse(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        add(bBrowseTo, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void refreshComponents(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshComponents
        refreshComponents();
    }//GEN-LAST:event_refreshComponents

    private void onToBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onToBrowse
// TODO add your handling code here:
    }//GEN-LAST:event_onToBrowse

    private void onFromBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onFromBrowse
// TODO add your handling code here:
    }//GEN-LAST:event_onFromBrowse
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowseFrom;
    private javax.swing.JButton bBrowseTo;
    private javax.swing.JCheckBox cbUseCommitMessage;
    private javax.swing.JCheckBox cbUseCommitMessage1;
    private javax.swing.JCheckBox cbUseFrom;
    private javax.swing.JCheckBox cbUseTo;
    private javax.swing.JTextField tfCommitMessage;
    private javax.swing.JTextField tfFrom;
    private javax.swing.JTextField tfTo;
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
    
}
