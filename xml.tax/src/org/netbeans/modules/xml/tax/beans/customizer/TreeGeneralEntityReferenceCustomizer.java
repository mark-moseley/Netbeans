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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeGeneralEntityReference;
import org.netbeans.tax.TreeEntityReference;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeGeneralEntityReferenceCustomizer extends AbstractTreeCustomizer {
    
    /** Serial Version UID */
    private static final long serialVersionUID = 6668177697987096689L;
    
    
    //
    // init
    //
    
    /** */
    public TreeGeneralEntityReferenceCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.getChar ("MNE_geRef_name")); // NOI18N
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeGeneralEntityReference getGeneralEntityReference () {
        return (TreeGeneralEntityReference)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeEntityReference.PROP_NAME)) {
            updateNameComponent ();
        }
    }
    
    /**
     */
    protected final void updateGeneralEntityReferenceName () {
        try {
            getGeneralEntityReference ().setName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            Util.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getGeneralEntityReference ().getName ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateNameComponent ();
    }
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
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
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Util.getString ("PROP_geRef_name"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
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
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    /**
     */
    private void nameFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateGeneralEntityReferenceName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    /**
     */
    private void nameFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateGeneralEntityReferenceName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_TreeGeneralEntityReferenceCustomizer"));
        nameField.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_nameField6"));
    }
}
