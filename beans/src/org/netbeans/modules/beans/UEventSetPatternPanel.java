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
import java.text.MessageFormat;
import javax.swing.border.TitledBorder;

import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.src.Type;

/** Customizer for new Unicast Event Set Pattern
 *
 * @author Petr Hrebejk
 */
public class UEventSetPatternPanel extends javax.swing.JPanel 
                      implements java.awt.event.ActionListener {

  /** The resource bundle */                      
  private static final ResourceBundle bundle = NbBundle.getBundle( UEventSetPatternPanel.class );                        
  
  /** Dialog for displaiyng this panel */
  private Dialog dialog;  
  /** Group node under which the new pattern will below */
  private PatternGroupNode groupNode;
  /** Geneartion for interface/class */
  private boolean forInterface = false;

  static final long serialVersionUID =4317314528606244073L;
  

  /** Initializes the Form */
  public UEventSetPatternPanel() {
    initComponents ();

    for( int i = 0; i < EventSetPattern.WELL_KNOWN_LISTENERS.length; i++ ) {
      typeComboBox.addItem( EventSetPattern.WELL_KNOWN_LISTENERS[i] );
    }
    typeComboBox.setSelectedItem( "" );
    
    javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
    bg.add( emptyRadioButton );
    bg.add( implRadioButton );
    
    ((TitledBorder)eventSetPanel.getBorder()).setTitle( 
      bundle.getString( "CTL_UEventSetPanel_eventSetPanel" ));
    ((TitledBorder)optionsPanel.getBorder()).setTitle( 
      bundle.getString( "CTL_UEventSetPanel_optionsPanel" ) );    
    typeLabel.setText( bundle.getString( "CTL_UEventSetPanel_typeLabel" ) );
    textLabel.setText( bundle.getString( "CTL_UEventSetPanel_textLabel" ) );
    emptyRadioButton.setText( bundle.getString( "CTL_UEventSetPanel_emptyRadioButton" ) );
    implRadioButton.setText( bundle.getString( "CTL_UEventSetPanel_implRadioButton" ) );
    fireCheckBox.setText( bundle.getString( "CTL_UEventSetPanel_fireCheckBox" ) );
    passEventCheckBox.setText( bundle.getString( "CTL_UEventSetPanel_passEventCheckBox" ) );
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    mainPanel = new javax.swing.JPanel ();
    eventSetPanel = new javax.swing.JPanel ();
    typeLabel = new javax.swing.JLabel ();
    typeComboBox = new javax.swing.JComboBox ();
    textLabel = new javax.swing.JLabel ();
    optionsPanel = new javax.swing.JPanel ();
    emptyRadioButton = new javax.swing.JRadioButton ();
    implRadioButton = new javax.swing.JRadioButton ();
    fireCheckBox = new javax.swing.JCheckBox ();
    passEventCheckBox = new javax.swing.JCheckBox ();
    setLayout (new java.awt.BorderLayout ());

    mainPanel.setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;
    mainPanel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

      eventSetPanel.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints2;
      eventSetPanel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (149, 142, 130)),
      "eventSetPanel", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
  
        typeLabel.setText ("typeLabel");
    
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.insets = new java.awt.Insets (2, 4, 2, 2);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints2.weighty = 1.0;
        eventSetPanel.add (typeLabel, gridBagConstraints2);
    
        typeComboBox.setEditable (true);
    
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (2, 2, 2, 2);
        gridBagConstraints2.weightx = 1.0;
        eventSetPanel.add (typeComboBox, gridBagConstraints2);
    
        textLabel.setText ("textLabel");
    
        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.insets = new java.awt.Insets (0, 4, 2, 2);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        eventSetPanel.add (textLabel, gridBagConstraints2);
    
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridwidth = 0;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 1.0;
      mainPanel.add (eventSetPanel, gridBagConstraints1);
  
      optionsPanel.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints3;
      optionsPanel.setBorder (new javax.swing.border.TitledBorder(
      new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (149, 142, 130)),
      "optionsPanel", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black));
  
        emptyRadioButton.setSelected (true);
        emptyRadioButton.setText ("emptyRadioButton");
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
    
        implRadioButton.setText ("implRadioButton");
        implRadioButton.addActionListener (new java.awt.event.ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
            implRadioButtonActionPerformed (evt);
          }
        }
        );
    
        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.insets = new java.awt.Insets (0, 4, 2, 4);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        optionsPanel.add (implRadioButton, gridBagConstraints3);
    
        fireCheckBox.setText ("fireCheckBox");
        fireCheckBox.setEnabled (false);
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
    
        passEventCheckBox.setText ("passEventCheckBox");
        passEventCheckBox.setEnabled (false);
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
  

    add (mainPanel, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents

  private void fireCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireCheckBoxActionPerformed
    protectControls();
  }//GEN-LAST:event_fireCheckBoxActionPerformed

  private void implRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_implRadioButtonActionPerformed
    protectControls();
  }//GEN-LAST:event_implRadioButtonActionPerformed

  private void emptyRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emptyRadioButtonActionPerformed
    protectControls();
  }//GEN-LAST:event_emptyRadioButtonActionPerformed

  private void passEventCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passEventCheckBoxActionPerformed
    protectControls();
  }//GEN-LAST:event_passEventCheckBoxActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mainPanel;
  private javax.swing.JPanel eventSetPanel;
  private javax.swing.JLabel typeLabel;
  private javax.swing.JComboBox typeComboBox;
  private javax.swing.JLabel textLabel;
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JRadioButton emptyRadioButton;
  private javax.swing.JRadioButton implRadioButton;
  private javax.swing.JCheckBox fireCheckBox;
  private javax.swing.JCheckBox passEventCheckBox;
  // End of variables declaration//GEN-END:variables


  class Result {

    String type;
    int    implementation = 0;

    boolean firing = false;
    boolean passEvent = false;
  }

  UEventSetPatternPanel.Result getResult( ) {
    Result result = new Result();


    result.type = typeComboBox.getEditor().getItem().toString();

    if ( implRadioButton.isSelected() )
      result.implementation = 1;

    if ( fireCheckBox.isSelected() )
      result.firing = true;

    if ( passEventCheckBox.isSelected() )
      result.passEvent = true;

    return result;
  }

  private void protectControls() {
    implRadioButton.setEnabled( !forInterface );
    
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
  
  void setGroupNode( PatternGroupNode groupNode ) {
    this.groupNode = groupNode;
  }
  
  public void actionPerformed( java.awt.event.ActionEvent e ) {
    if ( dialog != null ) {
      
      if ( e.getActionCommand().equals( "OK" ) ) { 
        
         //Test wether the string is empty 
         if ( typeComboBox.getEditor().getItem().toString().trim().length() <= 0) {
           TopManager.getDefault().notify(
              new NotifyDescriptor.Message(
                                      bundle.getString("MSG_Not_Valid_Type"),
                                      NotifyDescriptor.ERROR_MESSAGE) );
           typeComboBox.requestFocus();
           return;
         } 
        
         try {
          Type type = Type.parse( typeComboBox.getEditor().getItem().toString() );
          // Test wheter property with this name already exists 
           if ( groupNode.eventSetExists( type ) ) {
             String msg = MessageFormat.format( bundle.getString("MSG_EventSet_Exists"),
                                                new Object[] { type.toString() } );
             TopManager.getDefault().notify(
               new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE) ); 

             typeComboBox.requestFocus();
             return;          
           }
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

/*
 * Log
 *  8    Gandalf   1.7         1/4/00   Petr Hrebejk    Various bugfixes - 5036,
 *       5044, 5045
 *  7    Gandalf   1.6         11/10/99 Petr Hrebejk    Canged to work with 
 *       DialogDescriptor.setClosingOptions()
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  4    Gandalf   1.3         8/17/99  Petr Hrebejk    Combo box with well 
 *       known Listener interfaces
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         7/21/99  Petr Hrebejk    Bug fixes interface 
 *       bodies, is for boolean etc
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $
 */
