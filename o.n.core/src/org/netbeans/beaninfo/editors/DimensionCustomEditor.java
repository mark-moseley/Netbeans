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

package org.netbeans.beaninfo.editors;

import java.awt.Dimension;
import java.util.ResourceBundle;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class DimensionCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       DimensionCustomEditor.class);

    static final long serialVersionUID =3718340148720193844L;
    /** Initializes the Form */
    public DimensionCustomEditor(DimensionEditor editor) {
        initComponents ();
        this.editor = editor;
        Dimension dimension = (Dimension)editor.getValue ();
        if (dimension == null) dimension = new Dimension (0, 0);

        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
        insidePanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.TitledBorder (
                                       new javax.swing.border.EtchedBorder (),
                                       " " + bundle.getString ("CTL_Dimension") + " "),
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

        widthField.setText (""+dimension.width); // NOI18N
        heightField.setText (""+dimension.height); // NOI18N
        HelpCtx.setHelpIDString (this, DimensionCustomEditor.class.getName ());
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    public Object getPropertyValue () throws IllegalStateException {
        try {
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            if ((width < 0) || (height < 0)) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("CTL_NegativeSize"), NotifyDescriptor.ERROR_MESSAGE));
                Dimension dimension = (Dimension) editor.getValue();
                width = dimension.width;
                height = dimension.height;
            }
            return new Dimension (width, height);
        } catch (NumberFormatException e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("CTL_InvalidValue"), NotifyDescriptor.ERROR_MESSAGE));
            throw new IllegalStateException ();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        insidePanel = new javax.swing.JPanel ();
        widthLabel = new javax.swing.JLabel ();
        widthField = new javax.swing.JTextField ();
        heightLabel = new javax.swing.JLabel ();
        heightField = new javax.swing.JTextField ();
        setLayout (new java.awt.BorderLayout ());

        insidePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        insidePanel.setBorder (new javax.swing.border.CompoundBorder(
                                   new javax.swing.border.TitledBorder(
                                       new javax.swing.border.EtchedBorder(java.awt.Color.white, new java.awt.Color (134, 134, 134)),
                                       "Dimension", 1, 2, new java.awt.Font ("Dialog", 0, 11), java.awt.Color.black), // NOI18N
                                   new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));

        widthLabel.setText (org.openide.util.NbBundle.getBundle(DimensionCustomEditor.class).getString("CTL_Width"));

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add (widthLabel, gridBagConstraints1);

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

        heightLabel.setText (org.openide.util.NbBundle.getBundle(DimensionCustomEditor.class).getString("CTL_Height"));

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add (heightLabel, gridBagConstraints1);

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


        add (insidePanel, java.awt.BorderLayout.CENTER);

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
 *  13   Gandalf   1.12        1/13/00  Petr Jiricka    i18n
 *  12   Gandalf   1.11        1/13/00  Petr Jiricka    i18n
 *  11   Gandalf   1.10        1/11/00  Radko Najman    fixed bug #4910
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  8    Gandalf   1.7         8/18/99  Ian Formanek    Fixed bug 2322 - Some PE
 *       couldn't be initialized - en exception is issued
 *  7    Gandalf   1.6         7/8/99   Jesse Glick     Context help.
 *  6    Gandalf   1.5         6/30/99  Ian Formanek    Reflecting changes in 
 *       editors packages and enhanced property editor interfaces
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/2/99   Ian Formanek    Fixed event handlers
 *  3    Gandalf   1.2         5/31/99  Ian Formanek    Updated for X2 form 
 *       format
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
