/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.customizer;

import java.awt.Component;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeNamedObjectMap;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.beans.Lib;
import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeElementCustomizer extends AbstractTreeCustomizer {
    
    /** Serial Version UID */
    private static final long serialVersionUID = -2086561604130767387L;
    
    
    //
    // init
    //
    
    /** */
    public TreeElementCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_xmlName")); // NOI18N
        tableLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_element_attributelist_label")); // NOI18N
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeElement getElement () {
        return (TreeElement)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeElement.PROP_TAG_NAME)) {
            updateNameComponent ();
        }
    }
    
    /**
     */
    protected final void updateElementName () {
        try {
            getElement ().setQName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getElement ().getQName ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateNameComponent ();
    }
    
    /**
     */
    protected void ownInitComponents () {
        TreeNamedObjectMap attributes = getElement ().getAttributes ();
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeElementCustomizer::ownInitComponents: attributes = " + attributes); // NOI18N

        attributesCustomizer = Lib.getCustomizer (TreeElement.class, attributes, "attributes"); // "attributes" - name of TreeElement property // NOI18N
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeElementCustomizer::ownInitComponents: attributesCustomizer = " + attributesCustomizer); // NOI18N

        if (attributesCustomizer != null) {
            attributeListPanel.add (attributesCustomizer, BorderLayout.CENTER);
        }
    }
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        if ( attributesCustomizer != null )
            attributesCustomizer.setEnabled (editable); //???
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        tableLabel = new javax.swing.JLabel();
        attributeListPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 230));
        nameLabel.setText(Util.THIS.getString ("PROP_xmlName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        nameField.setColumns(20);
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        tableLabel.setText(Util.THIS.getString ("TEXT_element_attributelist_label"));
        tableLabel.setLabelFor(attributeListPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        jPanel1.add(tableLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel1, gridBagConstraints);

        attributeListPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(attributeListPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

    }//GEN-END:initComponents

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            nameField.selectAll();
        }
    }//GEN-LAST:event_nameFieldFocusGained
    
    /**
     */
    private void nameFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateElementName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    /**
     */
    private void nameFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateElementName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JPanel attributeListPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
    private Component attributesCustomizer;
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeElementCustomizer"));
        nameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_nameField7"));
    }
    
}
