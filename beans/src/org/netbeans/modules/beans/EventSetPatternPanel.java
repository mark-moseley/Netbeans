/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.beans;

import java.awt.Dialog;
import java.util.ResourceBundle;

import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/** Customizer for new Multicast Event Set
 *
 * @author Petr Hrebejk
 */
public class EventSetPatternPanel extends javax.swing.JPanel 
                      implements java.awt.event.ActionListener {

  private static final ResourceBundle bundle = NbBundle.getBundle( EventSetPatternPanel.class );
  private boolean forInterface; 
  
  private Dialog dialog;  

static final long serialVersionUID =-6439362166672698327L;
  /** Initializes the Form */
  public EventSetPatternPanel() {
    initComponents ();

    for( int i = 0; i < EventSetPattern.WELL_KNOWN_LISTENERS.length; i++ ) {
      typeComboBox.addItem( EventSetPattern.WELL_KNOWN_LISTENERS[i] );
    }
    typeComboBox.getEditor().setItem( "" );
    
    javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
    bg.add( emptyRadioButton );
    bg.add( alRadioButton );
    bg.add( ellRadioButton );
  
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
    private void initComponents () {//GEN-BEGIN:initComponents
      setLayout (new java.awt.BorderLayout ());

      mainPanel = new javax.swing.JPanel ();
      mainPanel.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints1;
      mainPanel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

      propertyPanel = new javax.swing.JPanel ();
      propertyPanel.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints2;
      propertyPanel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(), "Event Set"));

      jLabel1 = new javax.swing.JLabel ();
      jLabel1.setText ("Type:");

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.insets = new java.awt.Insets (2, 4, 2, 2);
      gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints2.weighty = 1.0;
      propertyPanel.add (jLabel1, gridBagConstraints2);

      typeComboBox = new javax.swing.JComboBox ();
      typeComboBox.setEditable (true);

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.gridwidth = 0;
      gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
      gridBagConstraints2.weightx = 1.0;
      propertyPanel.add (typeComboBox, gridBagConstraints2);

      jLabel2 = new javax.swing.JLabel ();
      jLabel2.setText ("(Fully qualified listener interface name)");

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.gridwidth = 0;
      gridBagConstraints2.insets = new java.awt.Insets (0, 4, 2, 2);
      gridBagConstraints2.weightx = 1.0;
      gridBagConstraints2.weighty = 1.0;
      propertyPanel.add (jLabel2, gridBagConstraints2);

      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridwidth = 0;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 1.0;
      mainPanel.add (propertyPanel, gridBagConstraints1);

      optionsPanel = new javax.swing.JPanel ();
      optionsPanel.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints3;
      optionsPanel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(), "Options"));

      emptyRadioButton = new javax.swing.JRadioButton ();
      emptyRadioButton.setSelected (true);
      emptyRadioButton.setText ("Generate empty");
      emptyRadioButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          emptyRadioButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints3 = new java.awt.GridBagConstraints ();
      gridBagConstraints3.gridwidth = 0;
      gridBagConstraints3.insets = new java.awt.Insets (0, 4, 2, 4);
      gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints3.weightx = 1.0;
      gridBagConstraints3.weighty = 1.0;
      optionsPanel.add (emptyRadioButton, gridBagConstraints3);

      alRadioButton = new javax.swing.JRadioButton ();
      alRadioButton.setText ("Generate ArrayList implementation");
      alRadioButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          alRadioButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints3 = new java.awt.GridBagConstraints ();
      gridBagConstraints3.gridwidth = 0;
      gridBagConstraints3.insets = new java.awt.Insets (0, 4, 2, 4);
      gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints3.weightx = 1.0;
      gridBagConstraints3.weighty = 1.0;
      optionsPanel.add (alRadioButton, gridBagConstraints3);

      ellRadioButton = new javax.swing.JRadioButton ();
      ellRadioButton.setText ("Generate EventListenerList implementation");
      ellRadioButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          ellRadioButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints3 = new java.awt.GridBagConstraints ();
      gridBagConstraints3.gridwidth = 0;
      gridBagConstraints3.insets = new java.awt.Insets (0, 4, 2, 4);
      gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints3.weightx = 1.0;
      gridBagConstraints3.weighty = 1.0;
      optionsPanel.add (ellRadioButton, gridBagConstraints3);

      fireCheckBox = new javax.swing.JCheckBox ();
      fireCheckBox.setEnabled (false);
      fireCheckBox.setText ("Generate event firing methods ");
      fireCheckBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          fireCheckBoxActionPerformed (evt);
        }
      }
      );

      gridBagConstraints3 = new java.awt.GridBagConstraints ();
      gridBagConstraints3.gridwidth = 0;
      gridBagConstraints3.insets = new java.awt.Insets (2, 4, 2, 4);
      gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints3.weightx = 1.0;
      gridBagConstraints3.weighty = 1.0;
      optionsPanel.add (fireCheckBox, gridBagConstraints3);

      passEventCheckBox = new javax.swing.JCheckBox ();
      passEventCheckBox.setEnabled (false);
      passEventCheckBox.setText ("Pass event as parameter");
      passEventCheckBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          passEventCheckBoxActionPerformed (evt);
        }
      }
      );

      gridBagConstraints3 = new java.awt.GridBagConstraints ();
      gridBagConstraints3.gridwidth = 0;
      gridBagConstraints3.insets = new java.awt.Insets (0, 10, 2, 4);
      gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints3.weightx = 1.0;
      gridBagConstraints3.weighty = 1.0;
      optionsPanel.add (passEventCheckBox, gridBagConstraints3);

      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridwidth = 0;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 1.0;
      mainPanel.add (optionsPanel, gridBagConstraints1);


      add (mainPanel, "Center");

    }//GEN-END:initComponents

  private void emptyRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emptyRadioButtonActionPerformed
    protectControls();
  }//GEN-LAST:event_emptyRadioButtonActionPerformed

  private void fireCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireCheckBoxActionPerformed
    protectControls();
  
  }//GEN-LAST:event_fireCheckBoxActionPerformed

  private void ellRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellRadioButtonActionPerformed
    protectControls();
  
  }//GEN-LAST:event_ellRadioButtonActionPerformed

  private void alRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alRadioButtonActionPerformed
    protectControls();
  }//GEN-LAST:event_alRadioButtonActionPerformed


  private void passEventCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passEventCheckBoxActionPerformed
    protectControls();
  }//GEN-LAST:event_passEventCheckBoxActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mainPanel;
  private javax.swing.JPanel propertyPanel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JComboBox typeComboBox;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JRadioButton emptyRadioButton;
  private javax.swing.JRadioButton alRadioButton;
  private javax.swing.JRadioButton ellRadioButton;
  private javax.swing.JCheckBox fireCheckBox;
  private javax.swing.JCheckBox passEventCheckBox;
  // End of variables declaration//GEN-END:variables


  class Result {

    String type;
    int    implementation = 0;

    boolean firing = false;
    boolean passEvent = false;
  }

  EventSetPatternPanel.Result getResult( ) {
    Result result = new Result();


    result.type = typeComboBox.getEditor().getItem().toString();

    if ( alRadioButton.isSelected() )
      result.implementation = 1;

    else if ( ellRadioButton.isSelected() )
      result.implementation = 2;

    if ( fireCheckBox.isSelected() )
      result.firing = true;

    if ( passEventCheckBox.isSelected() )
      result.passEvent = true;

    return result;
  }

  private void protectControls() {
    alRadioButton.setEnabled( !forInterface );
    ellRadioButton.setEnabled( !forInterface );
    
    fireCheckBox.setEnabled( !emptyRadioButton.isSelected() );
    passEventCheckBox.setEnabled( !emptyRadioButton.isSelected() && fireCheckBox.isSelected() );
  }
  
  void setDialog( Dialog dialog ) {
    this.dialog = dialog;
  }
  
  void setForInterface( boolean forInterface ) {
    this.forInterface = forInterface;
    protectControls();
  }

  public void actionPerformed( java.awt.event.ActionEvent e ) {
    if ( dialog != null ) {
      
       if ( e.getActionCommand().equals( "OK" ) ) { 

         try {
          org.openide.src.Type.parse( typeComboBox.getEditor().getItem().toString() );
         } 
         catch ( IllegalArgumentException ex ) {
           TopManager.getDefault().notify(
              new NotifyDescriptor.Message(
                                      bundle.getString("MSG_Not_Valid_Type"),
                                      NotifyDescriptor.ERROR_MESSAGE) );
           typeComboBox.requestFocus();
           return;
         } 
       }
      
      
      dialog.setVisible( false );
      dialog.dispose();
    }
  } 

}
