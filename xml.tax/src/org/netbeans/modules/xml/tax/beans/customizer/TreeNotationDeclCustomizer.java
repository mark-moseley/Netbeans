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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeNotationDecl;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNotationDeclCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 844910700645886601L;


    //
    // init
    //

    /** */
    public TreeNotationDeclCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_dtdNotationName")); // NOI18N
        publicLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_dtdNotationPublicId")); // NOI18N
        systemLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_dtdNotationSystemId")); // NOI18N
        
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeNotationDecl getNotationDecl () {
        return (TreeNotationDecl)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeNotationDecl.PROP_NAME)) {
            updateNameComponent ();
        } else if (pche.getPropertyName ().equals (TreeNotationDecl.PROP_PUBLIC_ID)) {
            updatePublicIdComponent ();
        } else if (pche.getPropertyName ().equals (TreeNotationDecl.PROP_SYSTEM_ID)) {
            updateSystemIdComponent ();
        }
    }
    
    /**
     */
    protected final void updateNotationDeclName () {
        try {
            getNotationDecl ().setName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getNotationDecl ().getName ());
    }
    
    /**
     */
    protected final void updateNotationDeclPublicId () {
        try {
            getNotationDecl ().setPublicId (text2null (publicField.getText ()));
        } catch (TreeException exc) {
            updatePublicIdComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updatePublicIdComponent () {
        publicField.setText (null2text (getNotationDecl ().getPublicId ()));
    }
    
    /**
     */
    protected final void updateNotationDeclSystemId () {
        try {
            getNotationDecl ().setSystemId (text2null (systemField.getText ()));
        } catch (TreeException exc) {
            updateSystemIdComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateSystemIdComponent () {
        systemField.setText (null2text (getNotationDecl ().getSystemId ()));
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateNameComponent ();
        updatePublicIdComponent ();
        updateSystemIdComponent ();
    }
    
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        systemField.setEditable (editable);
        publicField.setEditable (editable);
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
        publicLabel = new javax.swing.JLabel();
        publicField = new javax.swing.JTextField();
        systemLabel = new javax.swing.JLabel();
        systemField = new javax.swing.JTextField();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Util.THIS.getString ("PROP_dtdNotationName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        publicLabel.setText(Util.THIS.getString ("PROP_dtdNotationPublicId"));
        publicLabel.setLabelFor(publicField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(publicLabel, gridBagConstraints);

        publicField.setColumns(20);
        publicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicFieldActionPerformed(evt);
            }
        });

        publicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                publicFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                publicFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(publicField, gridBagConstraints);

        systemLabel.setText(Util.THIS.getString ("PROP_dtdNotationSystemId"));
        systemLabel.setLabelFor(systemField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(systemLabel, gridBagConstraints);

        systemField.setColumns(20);
        systemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemFieldActionPerformed(evt);
            }
        });

        systemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                systemFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                systemFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(systemField, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void systemFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_systemFieldFocusGained
        // Accessibility:
        systemField.selectAll ();
    }//GEN-LAST:event_systemFieldFocusGained
    
    private void publicFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_publicFieldFocusGained
        // Accessibility:
        publicField.selectAll ();
    }//GEN-LAST:event_publicFieldFocusGained
    
    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        // Accessibility:
        nameField.selectAll ();
    }//GEN-LAST:event_nameFieldFocusGained
    
    private void systemFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_systemFieldFocusLost
        // Add your handling code here:
        updateNotationDeclSystemId ();
    }//GEN-LAST:event_systemFieldFocusLost
    
    private void systemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemFieldActionPerformed
        // Add your handling code here:
        updateNotationDeclSystemId ();
    }//GEN-LAST:event_systemFieldActionPerformed
    
    private void publicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_publicFieldFocusLost
        // Add your handling code here:
        updateNotationDeclPublicId ();
    }//GEN-LAST:event_publicFieldFocusLost
    
    private void publicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicFieldActionPerformed
        // Add your handling code here:
        updateNotationDeclPublicId ();
    }//GEN-LAST:event_publicFieldActionPerformed
    
    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateNotationDeclName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateNotationDeclName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel publicLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField publicField;
    private javax.swing.JLabel systemLabel;
    private javax.swing.JTextField systemField;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeNotationDeclCustomizer"));
        
        nameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_nameField3"));
        nameField.selectAll ();
        
        publicField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_publicField2"));
        publicField.selectAll ();
        
        systemField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_systemField1"));
        systemField.selectAll ();
    }
}
