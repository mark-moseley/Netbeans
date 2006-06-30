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

package org.netbeans.modules.versioning.system.cvss.ui.actions.checkout;

import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 *
 * @author  Petr Kuzel
 */
final class CheckoutCompletedPanel extends javax.swing.JPanel {

    /** Creates new form CheckoutCompletedPanel */
    public CheckoutCompletedPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CheckoutCompletedPanel.class, "BK1001"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(againCheckBox, org.openide.util.NbBundle.getMessage(CheckoutCompletedPanel.class, "BK1002"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(againCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(openButton, org.openide.util.NbBundle.getMessage(CheckoutCompletedPanel.class, "BK1003"));
        openButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/checkout/Bundle").getString("TT_OpenProject"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 3);
        add(openButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, org.openide.util.NbBundle.getMessage(CheckoutCompletedPanel.class, "BK1004"));
        createButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/checkout/Bundle").getString("TT_CreateProject"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        add(createButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(CheckoutCompletedPanel.class, "BK1005"));
        closeButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/checkout/Bundle").getString("TT_Close"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        add(closeButton, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox againCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JButton closeButton = new javax.swing.JButton();
    final javax.swing.JButton createButton = new javax.swing.JButton();
    final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    final javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
    final javax.swing.JButton openButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables
    
}
