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

import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeElementDeclCustomizer extends AbstractTreeCustomizer {
    
    /** Serial Version UID */
    private static final long serialVersionUID = -4904653355576437639L;
    
    
    //
    // init
    //
    
    /** */
    public TreeElementDeclCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.getChar ("MNE_xmlName")); // NOI18N
        contentLabel.setDisplayedMnemonic (Util.getChar ("MNE_dtdContent")); // NOI18N
        
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeElementDecl getElementDecl () {
        return (TreeElementDecl)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeElementDecl.PROP_NAME)) {
            updateNameComponent ();
        } else if (pche.getPropertyName ().equals (TreeElementDecl.PROP_CONTENT_TYPE)) {
            updateContentTypeComponent ();
        }
    }
    
    /**
     */
    protected final void updateElementDeclName () {
        try {
            getElementDecl ().setName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            Util.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getElementDecl ().getName ());
    }
    
    /**
     */
    protected final void updateElementDeclContentType () {
        try {
            getElementDecl ().setContentType (contentField.getText ());
        } catch (TreeException exc) {
            updateContentTypeComponent ();
            Util.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateContentTypeComponent () {
        contentField.setText (getElementDecl ().getContentType ().toString ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateNameComponent ();
        updateContentTypeComponent ();
    }
    
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        contentField.setEditable (editable);
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
        contentLabel = new javax.swing.JLabel();
        contentField = new javax.swing.JTextField();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Util.getString ("PROP_xmlName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        nameField.setColumns(20);
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        contentLabel.setText(Util.getString ("PROP_dtdContent"));
        contentLabel.setLabelFor(contentField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(contentLabel, gridBagConstraints);

        contentField.setColumns(20);
        contentField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                contentFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(contentField, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        fillPanel.setName("null");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void contentFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_contentFieldFocusGained
        // Accessibility:
        contentField.selectAll ();
    }//GEN-LAST:event_contentFieldFocusGained
    
    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        // Accessibility:
        nameField.selectAll ();
    }//GEN-LAST:event_nameFieldFocusGained
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel contentLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField contentField;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        nameField.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_nameField"));
        nameField.selectAll ();
        contentField.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_contentField"));
        contentField.selectAll ();
        
        this.getAccessibleContext ().setAccessibleDescription (Util.getString ("ACSD_TreeElementDeclCustomizer"));
    }
}