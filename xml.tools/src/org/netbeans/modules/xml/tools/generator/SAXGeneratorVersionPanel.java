/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
public class SAXGeneratorVersionPanel extends SAXGeneratorAbstractPanel implements ActionListener {

    /** Serial Version UID */
    private static final long serialVersionUID =-3731567998368428526L;    
    
    
    /** Creates new form SAXGeneratorVersionPanel */
    public SAXGeneratorVersionPanel() {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jaxpLabel = new javax.swing.JLabel();
        jaxpVersionComboBox = new javax.swing.JComboBox();
        versionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox();
        propagateSAXCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(480, 350));
        jaxpLabel.setText(Util.getString ("SAXGeneratorVersionPanel.jaxpLabel.text"));
        jaxpLabel.setLabelFor(jaxpVersionComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jaxpLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(jaxpVersionComboBox, gridBagConstraints);

        versionLabel.setText(Util.getString ("SAXGeneratorCustomizer.versionLabel.text"));
        versionLabel.setLabelFor(versionComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(versionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(versionComboBox, gridBagConstraints);

        propagateSAXCheckBox.setText(Util.getString ("SAXGeneratorVersionPanel.propagateSAXCheckBox.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(propagateSAXCheckBox, gridBagConstraints);

    }//GEN-END:initComponents
    
    protected void updateModel() {
        model.setSAXversion(versionComboBox.getSelectedIndex() + 1);
        model.setJAXPversion(jaxpVersionComboBox.getSelectedIndex() + 1);
        model.setPropagateSAX(propagateSAXCheckBox.isSelected());
    }
    
    protected void initView() {
        initComponents();
	        
        //**** set mnemonics
        jaxpLabel.setDisplayedMnemonic(Util.getChar("SAXGeneratorVersionPanel.jaxpLabel.mne")); // NOI18N
        versionLabel.setDisplayedMnemonic(Util.getChar("SAXGeneratorCustomizer.versionLabel.mne")); // NOI18N
        propagateSAXCheckBox.setMnemonic(Util.getChar("SAXGeneratorVersionPanel.propagateSAXCheckBox.mne")); // NOI18N
        //****
        
        String items[] = new String[] {"SAX 1.0", "SAX 2.0"};  // NOI18N
        ComboBoxModel cbModel = new DefaultComboBoxModel(items);
        versionComboBox.setModel(cbModel);
        cbModel.setSelectedItem(items[model.getSAXversion() - 1]);
        
        items = new String[] {"JAXP 1.0", "JAXP 1.1"}; // NOI18N
        cbModel = new DefaultComboBoxModel(items);
        jaxpVersionComboBox.setModel(cbModel);
        cbModel.setSelectedItem(items[model.getJAXPversion() - 1]);
        
        initAccessibility();
    }
    
    protected void updateView() {
    }
    
    public void actionPerformed(java.awt.event.ActionEvent p1) {
        updateModel();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jaxpVersionComboBox;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JCheckBox propagateSAXCheckBox;
    private javax.swing.JLabel jaxpLabel;
    private javax.swing.JComboBox versionComboBox;
    // End of variables declaration//GEN-END:variables

    /** Initialize accesibility
     */
    public void initAccessibility(){

        propagateSAXCheckBox.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_propagateSAXCheckBox"));
        propagateSAXCheckBox.getAccessibleContext().setAccessibleName(Util.getString("ACSN_propagateSAXCheckBox"));
        
        jaxpVersionComboBox.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_jaxpVersionComboBox"));
        propagateSAXCheckBox.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_propagateSAXCheckBox"));
        
        this.getAccessibleContext().setAccessibleDescription(Util.getString("ACSD_SAXGeneratorVersionPanel"));
    }    
}
