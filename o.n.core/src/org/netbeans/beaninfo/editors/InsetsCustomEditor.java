/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Insets;
import java.util.ResourceBundle;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/** Custom editor for java.awt.Insets.
*
* @author   Ian Formanek
*/
public class InsetsCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       InsetsCustomEditor.class);

    static final long serialVersionUID =-1472891501739636852L;

    /** Initializes the Form */
    public InsetsCustomEditor(InsetsEditor editor) {
        initComponents ();
        this.editor = editor;
        Insets insets = (Insets)editor.getValue ();
        if (insets == null) insets = new Insets (0, 0, 0, 0);
        topField.setText (""+insets.top); // NOI18N
        leftField.setText (""+insets.left); // NOI18N
        bottomField.setText (""+insets.bottom); // NOI18N
        rightField.setText (""+insets.right); // NOI18N

        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   " " + bundle.getString ("CTL_Insets") + " "),
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

//        HelpCtx.setHelpIDString (this, InsetsCustomEditor.class.getName ());

        topLabel.setLabelFor(topField);
        leftLabel.setLabelFor(leftField);
        bottomLabel.setLabelFor(bottomField);
        rightLabel.setLabelFor(rightField);
        
        topLabel.setDisplayedMnemonic(bundle.getString("CTL_Top_Mnemonic").charAt(0));
        leftLabel.setDisplayedMnemonic(bundle.getString("CTL_Left_Mnemonic").charAt(0));
        bottomLabel.setDisplayedMnemonic(bundle.getString("CTL_Bottom_Mnemonic").charAt(0));
        rightLabel.setDisplayedMnemonic(bundle.getString("CTL_Right_Mnemonic").charAt(0));

        topField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Top"));
        leftField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Left"));
        bottomField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Bottom"));
        rightField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Right"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_InsetsCustomEditor"));
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
            if ((top < 0) || (left < 0) || (bottom < 0) || (right < 0)) {
                IllegalStateException ise = new IllegalStateException();
                ErrorManager.getDefault().annotate(
                    ise, ErrorManager.ERROR, null, 
                    bundle.getString("CTL_NegativeSize"), null, null);
                throw ise;
            }
            return new Insets (top, left, bottom, right);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            ErrorManager.getDefault().annotate(
                ise, ErrorManager.ERROR, null, 
                bundle.getString("CTL_InvalidValue"), null, null);
            throw ise;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        topLabel = new javax.swing.JLabel();
        topField = new javax.swing.JTextField();
        leftLabel = new javax.swing.JLabel();
        leftField = new javax.swing.JTextField();
        bottomLabel = new javax.swing.JLabel();
        bottomField = new javax.swing.JTextField();
        rightLabel = new javax.swing.JLabel();
        rightField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        topLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Top"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(topLabel, gridBagConstraints);

        topField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fieldKeyPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel2.add(topField, gridBagConstraints);

        leftLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Left"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(leftLabel, gridBagConstraints);

        leftField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fieldKeyPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel2.add(leftField, gridBagConstraints);

        bottomLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Bottom"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(bottomLabel, gridBagConstraints);

        bottomField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fieldKeyPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel2.add(bottomField, gridBagConstraints);

        rightLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Right"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(rightLabel, gridBagConstraints);

        rightField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fieldKeyPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        jPanel2.add(rightField, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void fieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fieldKeyPressed
        if ( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER )
            updateInsets();
    }//GEN-LAST:event_fieldKeyPressed

    private void updateInsets() {
        try {
            int top = Integer.parseInt (topField.getText ());
            int left = Integer.parseInt (leftField.getText ());
            int bottom = Integer.parseInt (bottomField.getText ());
            int right = Integer.parseInt (rightField.getText ());
            editor.setValue (new Insets (top, left, bottom, right));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel topLabel;
    private javax.swing.JTextField topField;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JTextField leftField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JTextField rightField;
    private javax.swing.JTextField bottomField;
    // End of variables declaration//GEN-END:variables

    private InsetsEditor editor;

}

