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

package com.netbeans.developer.editors;

import java.awt.Insets;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class InsetsCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    InsetsCustomEditor.class);

  /** Initializes the Form */
  public InsetsCustomEditor(InsetsEditor editor) {
    initComponents ();
    this.editor = editor;
    Insets insets = (Insets)editor.getValue ();
    topField.setText (""+insets.top);
    leftField.setText (""+insets.left);
    bottomField.setText (""+insets.bottom);
    rightField.setText (""+insets.right);

    setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
    jPanel2.setBorder (new javax.swing.border.CompoundBorder (
      new javax.swing.border.TitledBorder (
        new javax.swing.border.EtchedBorder (), 
        " " + bundle.getString ("CTL_Insets") + " "),
      new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

    topLabel.setText (bundle.getString ("CTL_Top"));
    leftLabel.setText (bundle.getString ("CTL_Left"));
    bottomLabel.setText (bundle.getString ("CTL_Bottom"));
    rightLabel.setText (bundle.getString ("CTL_Right"));
  }

  public java.awt.Dimension getPreferredSize () {
    return new java.awt.Dimension (280, 160);
  }

  public Object getPropertyValue () throws IllegalStateException {
    try {
      int top = Integer.parseInt (topField.getText ());
      int left = Integer.parseInt (leftField.getText ());
      int bottom = Integer.parseInt (bottomField.getText ());
      int right = Integer.parseInt (rightField.getText ());
      return new Insets (top, left, bottom, right);
    } catch (NumberFormatException e) {
      throw new IllegalStateException ();
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    setLayout (new java.awt.BorderLayout ());

    jPanel2 = new javax.swing.JPanel ();
    jPanel2.setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;

      topLabel = new javax.swing.JLabel ();
      topLabel.setText ("Top:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add (topLabel, gridBagConstraints1);

      topField = new javax.swing.JTextField ();
      topField.addActionListener (new java.awt.event.ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
            updateInsets (evt);
          }
        }
      );

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
    gridBagConstraints1.weightx = 1.0;
    jPanel2.add (topField, gridBagConstraints1);

      leftLabel = new javax.swing.JLabel ();
      leftLabel.setText ("Left:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add (leftLabel, gridBagConstraints1);

      leftField = new javax.swing.JTextField ();
      leftField.addActionListener (new java.awt.event.ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
            updateInsets (evt);
          }
        }
      );

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
    gridBagConstraints1.weightx = 1.0;
    jPanel2.add (leftField, gridBagConstraints1);

      bottomLabel = new javax.swing.JLabel ();
      bottomLabel.setText ("Bottom:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add (bottomLabel, gridBagConstraints1);

      bottomField = new javax.swing.JTextField ();
      bottomField.addActionListener (new java.awt.event.ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
            updateInsets (evt);
          }
        }
      );

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
    gridBagConstraints1.weightx = 1.0;
    jPanel2.add (bottomField, gridBagConstraints1);

      rightLabel = new javax.swing.JLabel ();
      rightLabel.setText ("Right:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add (rightLabel, gridBagConstraints1);

      rightField = new javax.swing.JTextField ();
      rightField.addActionListener (new java.awt.event.ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
            updateInsets (evt);
          }
        }
      );

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
    gridBagConstraints1.weightx = 1.0;
    jPanel2.add (rightField, gridBagConstraints1);


    add (jPanel2, "Center");

  }//GEN-END:initComponents


  private void updateInsets (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateInsets
    try {
      int top = Integer.parseInt (topField.getText ());
      int left = Integer.parseInt (leftField.getText ());
      int bottom = Integer.parseInt (bottomField.getText ());
      int right = Integer.parseInt (rightField.getText ());
      editor.setValue (new Insets (top, left, bottom, right));
    } catch (NumberFormatException e) {
      // [PENDING beep]
    }
  }//GEN-LAST:event_updateInsets


// Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jPanel2;
  private javax.swing.JLabel topLabel;
  private javax.swing.JTextField topField;
  private javax.swing.JLabel leftLabel;
  private javax.swing.JTextField leftField;
  private javax.swing.JLabel bottomLabel;
  private javax.swing.JTextField bottomField;
  private javax.swing.JLabel rightLabel;
  private javax.swing.JTextField rightField;
// End of variables declaration//GEN-END:variables

  private InsetsEditor editor;

}


/*
 * Log
 *  6    Gandalf   1.5         6/30/99  Ian Formanek    Reflecting changes in 
 *       editors packages and enhanced property editor interfaces
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/2/99   Ian Formanek    Fixed event handlers
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Updated to X2 format
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
