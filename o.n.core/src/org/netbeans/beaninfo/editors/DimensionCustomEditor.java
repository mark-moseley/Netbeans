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

import java.awt.Dimension;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.NbCustomPropertyEditor;

/**
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class DimensionCustomEditor extends javax.swing.JPanel implements NbCustomPropertyEditor {

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    DimensionCustomEditor.class);

  /** Initializes the Form */
  public DimensionCustomEditor(DimensionEditor editor) {
    initComponents ();
    this.editor = editor;
    Dimension dimension = (Dimension)editor.getValue ();

    setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
    insidePanel.setBorder (new javax.swing.border.CompoundBorder (
      new javax.swing.border.TitledBorder (
        new javax.swing.border.EtchedBorder (), 
        " " + bundle.getString ("CTL_Dimension") + " "), 
      new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

    widthLabel.setText (bundle.getString ("CTL_Width"));
    heightLabel.setText (bundle.getString ("CTL_Height"));

    widthField.setText (""+dimension.width);
    heightField.setText (""+dimension.height);
  }

  public java.awt.Dimension getPreferredSize () {
    return new java.awt.Dimension (280, 160);
  }

  public Object getPropertyValue () throws IllegalStateException {
    try {
      int width = Integer.parseInt (widthField.getText ());
      int height = Integer.parseInt (heightField.getText ());
      return new Dimension (width, height);
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

    insidePanel = new javax.swing.JPanel ();
    insidePanel.setLayout (new java.awt.GridBagLayout ());
    java.awt.GridBagConstraints gridBagConstraints1;
    insidePanel.setBorder (new javax.swing.border.CompoundBorder(
  new javax.swing.border.TitledBorder(
  new javax.swing.border.EtchedBorder(), "Dimension"), 
  new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));

      widthLabel = new javax.swing.JLabel ();
      widthLabel.setText ("Width:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    insidePanel.add (widthLabel, gridBagConstraints1);

      widthField = new javax.swing.JTextField ();
      widthField.addActionListener (new java.awt.event.ActionListener () {
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
    insidePanel.add (widthField, gridBagConstraints1);

      heightLabel = new javax.swing.JLabel ();
      heightLabel.setText ("Height:");

    gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
    insidePanel.add (heightLabel, gridBagConstraints1);

      heightField = new javax.swing.JTextField ();
      heightField.addActionListener (new java.awt.event.ActionListener () {
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
    insidePanel.add (heightField, gridBagConstraints1);


    add (insidePanel, "Center");

  }//GEN-END:initComponents


  private void updateInsets (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateInsets
    try {
      int width = Integer.parseInt (widthField.getText ());
      int height = Integer.parseInt (heightField.getText ());
      editor.setValue (new Dimension (width, height));
    } catch (NumberFormatException e) {
      // [PENDING beep]
    }
  }//GEN-LAST:event_updateInsets


// Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel insidePanel;
  private javax.swing.JLabel widthLabel;
  private javax.swing.JTextField widthField;
  private javax.swing.JLabel heightLabel;
  private javax.swing.JTextField heightField;
// End of variables declaration//GEN-END:variables

  private DimensionEditor editor;

}


/*
 * Log
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/2/99   Ian Formanek    Fixed event handlers
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Updated for X2 form 
 *       format
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
