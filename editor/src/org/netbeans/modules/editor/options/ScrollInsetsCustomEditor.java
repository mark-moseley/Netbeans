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

package org.netbeans.modules.editor.options;

import java.awt.Insets;

import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/** Custom editor for java.awt.Insets allowing to set per cent values
 *  as negative numbers.
 *
 * @author   Petr Nejedly
 * @author   Ian Formanek
 */
public class ScrollInsetsCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {


    static final long serialVersionUID =-1472891501739636852L;

    private ScrollInsetsEditor editor;

    /** Initializes the Form */
    public ScrollInsetsCustomEditor(ScrollInsetsEditor editor) {
        initComponents ();
        this.editor = editor;
        Insets insets = (Insets)editor.getValue();

        if (insets == null) insets = new Insets( 0, 0, 0, 0 );

        getAccessibleContext ().setAccessibleDescription (getBundleString("ACSD_SICE")); // NOI18N
        topLabel.setDisplayedMnemonic (getBundleString("SICE_Top_Mnemonic").charAt(0)); // NOI18N
        bottomLabel.setDisplayedMnemonic (getBundleString("SICE_Bottom_Mnemonic").charAt(0)); // NOI18N
        leftLabel.setDisplayedMnemonic (getBundleString("SICE_Left_Mnemonic").charAt(0)); // NOI18N
        rightLabel.setDisplayedMnemonic (getBundleString("SICE_Right_Mnemonic").charAt(0)); // NOI18N
        topField.setText (int2percent (insets.top ));
        leftField.setText (int2percent (insets.left ));
        bottomField.setText (int2percent (insets.bottom));
        rightField.setText (int2percent (insets.right));
        topField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Top")); // NOI18N
        leftField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Left")); // NOI18N
        bottomField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Bottom")); // NOI18N
        rightField.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SICE_Right")); // NOI18N
        
        /*
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   getBundleString().getString ("SICE_Insets")), // NOI18N
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(12, 12, 11, 11))));
                               */

        setPreferredSize(new java.awt.Dimension(320, getPreferredSize().height));
    }

    private String getBundleString(String s) {
        return NbBundle.getMessage(ScrollInsetsCustomEditor.class, s);
    }        
    
    
    public Object getPropertyValue () throws IllegalStateException {
        try {
            return getValue();
        } catch (NumberFormatException e) {
            org.openide.DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(
                                                getBundleString("SIC_InvalidValue"), // NOI18N
                                                NotifyDescriptor.ERROR_MESSAGE
                                            ) );
            throw new IllegalStateException();
        }
    }



    public static String int2percent( int i ) {
        if( i < 0 ) return( "" + (-i) + "%" ); // NOI18N
        else return( "" + i );
    }

    private int percent2int( String val ) throws NumberFormatException {
        val = val.trim();
        if( val.endsWith( "%" ) ) { // NOI18N
            return -Math.abs( Integer.parseInt( val.substring( 0, val.length() - 1 ) ) );
        } else {
            return Integer.parseInt( val );
        }
    }

    Insets getValue() throws NumberFormatException {
        int top = percent2int( topField.getText() );
        int left = percent2int( leftField.getText() );
        int bottom = percent2int( bottomField.getText() );
        int right = percent2int( rightField.getText() );
        return new Insets( top, left, bottom, right );
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

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        setBorder(new javax.swing.border.EmptyBorder( new java.awt.Insets( 12, 12, 11, 11) ) );
        jPanel2.setLayout(new java.awt.GridBagLayout());

        topLabel.setLabelFor(topField);
        topLabel.setText(getBundleString("SICE_Top"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(topLabel, gridBagConstraints);

        topField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(topField, gridBagConstraints);

        leftLabel.setLabelFor(leftField);
        leftLabel.setText(getBundleString("SICE_Left"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(leftLabel, gridBagConstraints);

        leftField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(leftField, gridBagConstraints);

        bottomLabel.setLabelFor(bottomField);
        bottomLabel.setText(getBundleString("SICE_Bottom"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        jPanel2.add(bottomLabel, gridBagConstraints);

        bottomField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(bottomField, gridBagConstraints);

        rightLabel.setLabelFor(rightField);
        rightLabel.setText(getBundleString("SICE_Right"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 12);
        jPanel2.add(rightLabel, gridBagConstraints);

        rightField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(rightField, gridBagConstraints);

        add(jPanel2);

    }//GEN-END:initComponents


    private void updateInsets (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateInsets
        try {
            editor.setValue( getValue() );
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }//GEN-LAST:event_updateInsets


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

}
