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
package org.netbeans.modules.xml.catalog.impl.sun;

import java.beans.*;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Catalog customizer. It allows to customize catalog location and
 * preferences.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class CatalogCustomizer extends javax.swing.JPanel implements Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID =-1437233290256708364L;    

    Catalog model = null;
    
    /** Creates new customizer XCatalogCustomizer */
    public CatalogCustomizer() {
        initComponents ();
        
        // A11Y
        this.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_CatalogCustomizer"));        
        
        locationLabel.setDisplayedMnemonic((Util.THIS.getString("CatalogCustomizer.locationLabel.mne")).charAt(0));
        locationTextField.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_locationTextField"));

        preferCheckBox.setMnemonic(Util.THIS.getString("MNE_preference").charAt(0));
        preferCheckBox.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_preference"));
        
        selectButton.setMnemonic(Util.THIS.getString("MNE_file").charAt(0));
        selectButton.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_file"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        locationLabel = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();
        selectButton = new javax.swing.JButton();
        preferCheckBox = new javax.swing.JCheckBox();
        descTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        locationLabel.setForeground(java.awt.Color.black);
        locationLabel.setText(Util.THIS.getString ("CatalogCustomizer.locationLabel.text"));
        locationLabel.setLabelFor(locationTextField);
        add(locationLabel, new java.awt.GridBagConstraints());

        locationTextField.setColumns(20);
        locationTextField.setPreferredSize(new java.awt.Dimension(220, 20));
        locationTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationTextFieldActionPerformed(evt);
            }
        });

        locationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                locationTextFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(locationTextField, gridBagConstraints);

        selectButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/catalog/impl/sun/Bundle").getString("PROP_choose_file"));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(selectButton, gridBagConstraints);

        preferCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/catalog/impl/sun/Bundle").getString("LBL_preference"));
        preferCheckBox.setToolTipText(Util.THIS.getString("HINT_pp"));
        preferCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                preferCheckBoxStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(preferCheckBox, gridBagConstraints);

        descTextArea.setEditable(false);
        descTextArea.setFont(javax.swing.UIManager.getFont ("Label.font"));
        descTextArea.setForeground(new java.awt.Color(102, 102, 153));
        descTextArea.setLineWrap(true);
        descTextArea.setText(Util.THIS.getString("DESC_catalog_fmts"));
        descTextArea.setWrapStyleWord(true);
        descTextArea.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        descTextArea.setEnabled(false);
        descTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(descTextArea, gridBagConstraints);

    }//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        File f = org.netbeans.modules.xml.catalog.lib.Util.selectCatalogFile("txt xml cat catalog"); // NOI18N
        if (f == null) return;
        try {
            String location = f.toURL().toExternalForm();
            locationTextField.setText(location);
            model.setLocation(location);
        } catch (MalformedURLException ex) {
            // ignore
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    private void preferCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_preferCheckBoxStateChanged
        if (model != null) model.setPreferPublic(preferCheckBox.isSelected());
    }//GEN-LAST:event_preferCheckBoxStateChanged

    //!!! find out whether action performed is not enough
    
    private void locationTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_locationTextFieldFocusLost
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("FocusLost-setting location: " + locationTextField.getText()); // NOI18N
        model.setLocation(locationTextField.getText());
    }//GEN-LAST:event_locationTextFieldFocusLost

    private void locationTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationTextFieldActionPerformed
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("ActionPerformed-setting location: " + locationTextField.getText()); // NOI18N
        model.setLocation(locationTextField.getText());
    }//GEN-LAST:event_locationTextFieldActionPerformed

    /**
     * Set model for this customizer.
     */
    public void setObject(java.lang.Object peer) {
        if ((peer instanceof Catalog) == false) {
            throw new IllegalArgumentException("Catalog instance expected (" + peer.getClass() + ").");  // NOI18N
        }
        
        model = (Catalog) peer;        
        locationTextField.setText(model.getLocation());
        preferCheckBox.setSelected(model.isPreferPublic());
    }    

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JCheckBox preferCheckBox;
    private javax.swing.JButton selectButton;
    private javax.swing.JTextArea descTextArea;
    // End of variables declaration//GEN-END:variables

}
