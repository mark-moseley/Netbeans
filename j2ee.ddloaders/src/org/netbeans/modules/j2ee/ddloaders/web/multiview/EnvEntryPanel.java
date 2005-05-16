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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.openide.util.NbBundle;

/**
 * EnvEntryPanel.java
 * Panel for adding/editing env entries
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
public class EnvEntryPanel extends javax.swing.JPanel {
    
    /** Creates new form FilterMappingPanel */
    public EnvEntryPanel() {
        initComponents();
        org.netbeans.modules.xml.multiview.Utils.makeTextAreaLikeTextField(descriptionTA, nameTF);
    }
    
    void setEnvEntryName(String name) {
        nameTF.setText(name);
    }
    
    void setEnvEntryType(String type) {
        typeCB.setSelectedItem(type);
    }
    
    void setEnvEntryValue(String val) {
        valueTF.setText(val);
    }
    
    void setDescription(String val) {
        descriptionTA.setText(val);
    }
    
    String getEnvEntryName() {
        return nameTF.getText();
    }
    
    String getEnvEntryType() {
        return (String)typeCB.getSelectedItem();
    }
    
    String getEnvEntryValue() {
        return valueTF.getText();
    }
    
    String getDescription() {
        return descriptionTA.getText();
    }
    
    javax.swing.JTextField getNameTF() {
        return nameTF;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTF = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeCB = new javax.swing.JComboBox();
        valueLabel = new javax.swing.JLabel();
        valueTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryName_mnem").charAt(0));
        nameLabel.setLabelFor(nameTF);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        nameTF.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(nameTF, gridBagConstraints);

        typeLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryType_mnem").charAt(0));
        typeLabel.setLabelFor(typeCB);
        typeLabel.setText(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(typeLabel, gridBagConstraints);

        typeCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "java.lang.String", "java.lang.Boolean", "java.lang.Character", "java.lang.Integer", "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Float", "java.lang.Double" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(typeCB, gridBagConstraints);

        valueLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryValue_mnem").charAt(0));
        valueLabel.setLabelFor(valueTF);
        valueLabel.setText(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_EnvEntryValue"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(valueLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(valueTF, gridBagConstraints);

        descriptionLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_description_mnem").charAt(0));
        descriptionLabel.setLabelFor(descriptionTA);
        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(EnvEntryPanel.class, "LBL_description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(descriptionLabel, gridBagConstraints);

        descriptionTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(descriptionTA, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTF;
    private javax.swing.JComboBox typeCB;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTF;
    // End of variables declaration//GEN-END:variables
    
}
