/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class InsetsCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor, KeyListener {
    static final long serialVersionUID =-1472891501739636852L;
   
    //XXX this is just a copy of RectangleEditor with the fields and value 
    //type changed.  A proper solution would be have an equivalent of 
    //ArrayOfIntSupport for custom editors, which would just produce the
    //required number of fields
    
    private HashMap labelMap = new HashMap();
    private PropertyEnv env;
    /** Initializes the Form */
    public InsetsCustomEditor(InsetsEditor editor, PropertyEnv env) {
        this.env=env;
        initComponents ();
        this.editor = editor;
        Insets insets = (Insets)editor.getValue ();
        if (insets == null) insets = new Insets (0, 0, 0, 0);
        xField.setText (Integer.toString(insets.top)); // NOI18N
        yField.setText (Integer.toString(insets.left)); // NOI18N
        widthField.setText (Integer.toString(insets.bottom)); // NOI18N
        heightField.setText (Integer.toString(insets.right)); // NOI18N

        setBorder (new javax.swing.border.EmptyBorder (new Insets(5, 5, 5, 5)));
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   " " + NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Insets") + " "),
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

        xLabel.setText (NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Top"));
        yLabel.setText (NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Left"));
        widthLabel.setText (NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Bottom"));
        heightLabel.setText (NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Right"));

        xLabel.setLabelFor(xField);
        yLabel.setLabelFor(yField);
        widthLabel.setLabelFor(widthField);
        heightLabel.setLabelFor(heightField);

        xLabel.setDisplayedMnemonic(NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Top_Mnemonic").charAt(0));
        yLabel.setDisplayedMnemonic(NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Left_Mnemonic").charAt(0));
        widthLabel.setDisplayedMnemonic(NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Bottom_Mnemonic").charAt(0));
        heightLabel.setDisplayedMnemonic(NbBundle.getMessage (InsetsCustomEditor.class, "CTL_Right_Mnemonic").charAt(0));

        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Top"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Left"));
        widthField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Bottom"));
        heightField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CTL_Right"));
        
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (InsetsCustomEditor.class, "ACSD_CustomRectangleEditor"));

        labelMap.put(widthField,widthLabel);
        labelMap.put(xField,xLabel);
        labelMap.put(yField,yLabel);
        labelMap.put(heightField,heightLabel);
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    public Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            if ((x < 0) || (y < 0) || (width < 0) || (height < 0)) {
                IllegalStateException ise = new IllegalStateException();
                ErrorManager.getDefault().annotate(
                    ise, ErrorManager.USER, null, 
                    NbBundle.getMessage (InsetsCustomEditor.class, "CTL_NegativeSize"), null, null);
                throw ise;
            }
            return new Insets (x, y, width, height);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            ErrorManager.getDefault().annotate(
                ise, ErrorManager.USER, null, 
                NbBundle.getMessage (InsetsCustomEditor.class, "CTL_InvalidValue"), null, null);
            throw ise;
        }
    }


    private void initComponents () {
        setLayout (new java.awt.BorderLayout ());

        jPanel2 = new javax.swing.JPanel ();
        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        xLabel = new javax.swing.JLabel ();
        xLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (xLabel, gridBagConstraints1);

        xField = new javax.swing.JTextField ();
        xField.addKeyListener (this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (xField, gridBagConstraints1);

        yLabel = new javax.swing.JLabel ();
        yLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (yLabel, gridBagConstraints1);

        yField = new javax.swing.JTextField ();
        yField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (yField, gridBagConstraints1);

        widthLabel = new javax.swing.JLabel ();
        widthLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (widthLabel, gridBagConstraints1);

        widthField = new javax.swing.JTextField ();
        widthField.addKeyListener(this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (widthField, gridBagConstraints1);

        heightLabel = new javax.swing.JLabel ();
        heightLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (heightLabel, gridBagConstraints1);

        heightField = new javax.swing.JTextField ();
        heightField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (heightField, gridBagConstraints1);


        add (jPanel2, "Center"); // NOI18N

    }


    private void updateRectangle () {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            editor.setValue (new Rectangle (x, y, width, height));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }

    public void keyPressed(java.awt.event.KeyEvent e) {
    }    

    public void keyReleased(java.awt.event.KeyEvent e) {
        if (checkValues()) {
            updateRectangle();
        }
    }
    
    public void keyTyped(java.awt.event.KeyEvent e) {
    }
    
    private boolean checkValues() {
        Component[] c = jPanel2.getComponents();
        boolean valid=true;
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof JTextField) {
                valid &= validFor((JTextField) c[i]);
            }
        }
        if (env != null) {
           env.setState(valid ? env.STATE_VALID : env.STATE_INVALID);
        }
        return valid;
    }
    
    private boolean validFor(JTextField c) {
        String s = c.getText().trim();
        try {
            Integer.parseInt(s);
            handleValid(c);
            return true;
        } catch (NumberFormatException e) {
            handleInvalid(c);
            return false;
        }
    }
    
    private void handleInvalid(JTextField c) {
        c.setForeground(getErrorColor());
        findLabelFor(c).setForeground(getErrorColor());
    }
    
    private void handleValid(JTextField c) {
        c.setForeground(getForeground());
        findLabelFor(c).setForeground(getForeground());
    }
    
    private Color getErrorColor() {
        Color c=UIManager.getColor("nb.errorForeground");
        if (c == null) {
            c = Color.RED;
        }
        return c;
    }
    
    private JLabel findLabelFor(JTextField c) {
        return (JLabel) labelMap.get(c);
    }
    
    // Variables declaration - do not modify
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yField;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightField;
    // End of variables declaration

    private InsetsEditor editor;

}

