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

package com.netbeans.developer.modules.loaders.form.editors;

import org.openide.nodes.PropertySupport;

/** 
 *
 * @author  Pavel Buzek
 * @version 
 */
public class CustomCodeEditor extends javax.swing.JDialog {

  private PropertySupport propertySupport;
  
  static final long serialVersionUID =-7413680598253484271L;
  /** Creates new form CustomCodeEditor */
  public CustomCodeEditor(PropertySupport propertySupport) {
    super (new javax.swing.JFrame (), true);
    this.propertySupport = propertySupport;
    initComponents ();
    pack ();
    org.openidex.util.Utilities2.centerWindow (this);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    jPanel1 = new javax.swing.JPanel ();
    okButton = new javax.swing.JButton ();
    cancelButton = new javax.swing.JButton ();
    jScrollPane1 = new javax.swing.JScrollPane ();
    codeEditorPane = new javax.swing.JEditorPane ();
    getContentPane ().setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;
    setTitle ("Property Editor: " + propertySupport.getDisplayName ());
    addWindowListener (new java.awt.event.WindowAdapter () {
      public void windowClosing (java.awt.event.WindowEvent evt) {
        closeDialog (evt);
      }
    }
    );

    jPanel1.setLayout (new java.awt.FlowLayout (2, 5, 5));

      okButton.setText ("Ok");
      okButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          okButtonActionPerformed (evt);
        }
      }
      );
  
      jPanel1.add (okButton);
  
      cancelButton.setText ("Cancel");
      cancelButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          cancelButtonActionPerformed (evt);
        }
      }
      );
  
      jPanel1.add (cancelButton);
  

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridy = 1;
    gridBagConstraints1.gridwidth = 3;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (8, 8, 8, 8);
    getContentPane ().add (jPanel1, gridBagConstraints1);


      codeEditorPane.setContentType ("text/x-java");
      /*
      try {
        codeEditorPane.setText((String) propertySupport.getValue ());
      } catch (java.lang.reflect.InvocationTargetException e1) {
        e1.printStackTrace();
      } catch (IllegalAccessException e2) {
        e2.printStackTrace();
      }
      */
  
      jScrollPane1.setViewportView (codeEditorPane);
  

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints1.insets = new java.awt.Insets (8, 8, 8, 8);
    gridBagConstraints1.weightx = 1.0;
    gridBagConstraints1.weighty = 1.0;
    getContentPane ().add (jScrollPane1, gridBagConstraints1);

  }//GEN-END:initComponents

private void okButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
// Add your handling code here:
  try {
    propertySupport.setValue (codeEditorPane.getText());
  } catch (java.lang.reflect.InvocationTargetException e1) {
    e1.printStackTrace();
  } catch (IllegalAccessException e2) {
    e2.printStackTrace();
  }
  setVisible (false);
  dispose ();
  }//GEN-LAST:event_okButtonActionPerformed

private void cancelButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
// Add your handling code here:
  setVisible (false);
  dispose ();
  }//GEN-LAST:event_cancelButtonActionPerformed

  /** Closes the dialog */
  private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
    setVisible (false);
    dispose ();
  }//GEN-LAST:event_closeDialog


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jPanel1;
  private javax.swing.JButton okButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JEditorPane codeEditorPane;
  // End of variables declaration//GEN-END:variables

}

/*
 * Log
 *  4    Gandalf   1.3         12/13/99 Pavel Buzek     scrollPane added
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         11/25/99 Ian Formanek    Uses Utilities module
 *  1    Gandalf   1.0         11/15/99 Pavel Buzek     
 * $
 */
