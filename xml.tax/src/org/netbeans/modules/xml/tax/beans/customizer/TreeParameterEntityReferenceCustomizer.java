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

import org.netbeans.tax.TreeEntityReference;
import org.netbeans.tax.TreeParameterEntityReference;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeParameterEntityReferenceCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 6668177697987096689L;
    

    //
    // init
    //
    
    /** */
    public TreeParameterEntityReferenceCustomizer () {
	super();

        initComponents();
        nameLabel.setDisplayedMnemonic(Util.getChar("MNE_peRef_name")); // NOI18N
    }


    //
    // itself
    //

    /**
     */
    protected final TreeParameterEntityReference getParameterEntityReference () {
        return (TreeParameterEntityReference)getTreeObject();
    }

    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
	if (pche.getPropertyName().equals (TreeEntityReference.PROP_NAME)) {
	    updateNameComponent();
	}
    }

    /**
     */
    protected final void updateParameterEntityReferenceName () {
  	try {
  	    getParameterEntityReference().setName (nameField.getText());
  	} catch (TreeException exc) {
	    updateNameComponent();
  	    Util.notifyTreeException (exc);
  	}
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getParameterEntityReference().getName());
    }
    
    /**
     */
    protected final void initComponentValues () {
	updateNameComponent();
    }


    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
    }    


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        nameLabel.setText(Util.getString("PROP_peRef_name"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weighty = 1.0;
        add(nameLabel, gridBagConstraints1);
        
        nameField.setColumns(23);
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
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        add(nameField, gridBagConstraints1);
        
    }//GEN-END:initComponents

    /**
     */
    private void nameFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
	// Add your handling code here:
        updateParameterEntityReferenceName();
    }//GEN-LAST:event_nameFieldFocusLost

    /**
     */
    private void nameFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
	// Add your handling code here:
        updateParameterEntityReferenceName();
    }//GEN-LAST:event_nameFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables

}
