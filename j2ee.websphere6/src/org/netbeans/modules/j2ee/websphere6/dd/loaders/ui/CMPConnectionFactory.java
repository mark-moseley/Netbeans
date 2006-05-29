/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import  org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
/**
 *
 * @author  dlm198383
 */
public class CMPConnectionFactory extends javax.swing.JPanel {
    
    /**
     * Creates new form LocalTransactionPanel
     */
    public CMPConnectionFactory() {
        initComponents();
        authTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(DDXmiConstants.CMP_RES_AUTH_TYPES));
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        factoryCheckBox = new javax.swing.JCheckBox();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        authTypeComboBox = new javax.swing.JComboBox();
        jndiLabel = new javax.swing.JLabel();
        jndiNameField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(150, 22));
        factoryCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_CMPConnectionFactory"));
        factoryCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        factoryCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        factoryCheckBox.setMinimumSize(new java.awt.Dimension(40, 15));
        factoryCheckBox.setPreferredSize(new java.awt.Dimension(150, 15));
        factoryCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                factoryCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 26);
        add(factoryCheckBox, gridBagConstraints);

        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 14, 0, 6);
        add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 10);
        add(nameField, gridBagConstraints);

        typeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        typeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_CmpAuthType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 14, 0, 6);
        add(typeLabel, gridBagConstraints);

        authTypeComboBox.setPreferredSize(new java.awt.Dimension(150, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 10);
        add(authTypeComboBox, gridBagConstraints);

        jndiLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_JndiName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 14, 0, 6);
        add(jndiLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 10);
        add(jndiNameField, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void factoryCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_factoryCheckBoxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_factoryCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox authTypeComboBox;
    private javax.swing.JCheckBox factoryCheckBox;
    private javax.swing.JLabel jndiLabel;
    private javax.swing.JTextField jndiNameField;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
    
    public void setEnabledComponents() {
        boolean state=factoryCheckBox.isSelected();
        nameLabel.setEnabled(state);
        nameField.setEnabled(state);
        
        jndiLabel.setEnabled(state);
        jndiNameField.setEnabled(state);
        
        typeLabel.setEnabled(state);
        authTypeComboBox.setEnabled(state);
    }
    public javax.swing.JComboBox getAuthTypeComboBox(){
        return authTypeComboBox;
    }
    public javax.swing.JCheckBox getFactoryCheckBox(){
        return factoryCheckBox;
    }
    
    public javax.swing.JTextField getNameField() {
        return nameField; 
    }
    public javax.swing.JLabel getNameLabel() {
        return nameLabel; 
    }
    public javax.swing.JTextField getJndiNameField() {
        return jndiNameField; 
    }
    public void setComponentsBackground(java.awt.Color c) {
        nameLabel.setBackground(c);
        jndiLabel.setBackground(c);
        typeLabel.setBackground(c);     
        factoryCheckBox.setBackground(c); 
        setBackground(c);
    }
}

