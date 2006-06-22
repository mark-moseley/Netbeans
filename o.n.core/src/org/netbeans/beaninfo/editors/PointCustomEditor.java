/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Point;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.UIException;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/** Custom property editor for Point and Dimension
*
* @author   Ian Formanek
*/
public class PointCustomEditor extends javax.swing.JPanel
implements PropertyChangeListener {

    static final long serialVersionUID =-4067033871196801978L;
    
    private boolean dimensionMode = false;

    private PropertyEnv env;
    
    /** Initializes the Form */
    public PointCustomEditor(PointEditor editor, PropertyEnv env) {
        initComponents ();
        this.editor = editor;
        Point point = (Point)editor.getValue ();
        if (point == null) point = new Point (0, 0);
        xField.setText (Integer.toString(point.x)); // NOI18N
        yField.setText (Integer.toString(point.y)); // NOI18N
        
        xLabel.setDisplayedMnemonic(NbBundle.getMessage(PointCustomEditor.class, "CTL_X_Mnemonic").charAt(0));
        yLabel.setDisplayedMnemonic(NbBundle.getMessage(PointCustomEditor.class, "CTL_Y_Mnemonic").charAt(0));

        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_X"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Y"));
        
        commonInit( NbBundle.getMessage(PointCustomEditor.class, "CTL_Point"), env );
    }
    
    public PointCustomEditor(DimensionEditor editor, PropertyEnv env) {
        dimensionMode = true;

        initComponents();
        this.editor = editor;
        Dimension dimension = (Dimension)editor.getValue ();
        if (dimension == null) dimension = new Dimension (0, 0);
        xField.setText (Integer.toString(dimension.width));    // NOI18N
        yField.setText (Integer.toString(dimension.height));  // NOI18N
        
        xLabel.setText (NbBundle.getMessage(PointCustomEditor.class, "CTL_Width"));
        xLabel.setDisplayedMnemonic(NbBundle.getMessage(PointCustomEditor.class, "CTL_Width_mnemonic").charAt(0));
        xLabel.setLabelFor(xField);
        yLabel.setText (NbBundle.getMessage(PointCustomEditor.class, "CTL_Height"));
        yLabel.setDisplayedMnemonic(NbBundle.getMessage(PointCustomEditor.class, "CTL_Height_mnemonic").charAt(0));
        yLabel.setLabelFor(yField);

        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Width"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Height"));
        
        commonInit( NbBundle.getMessage(PointCustomEditor.class, "CTL_Dimension"), env );
    }
    
    private void commonInit( String panelTitle, PropertyEnv env ) {
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_PointCustomEditor"));
        
        setBorder (new javax.swing.border.EmptyBorder(12, 12, 0, 11));
        insidePanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.TitledBorder (
                                       new javax.swing.border.EtchedBorder (),
                                       " " + panelTitle + " "
                                   ),
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));


        this.env = env;
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    private Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            if ((x < 0) || (y < 0)) {
                IllegalStateException ise = new IllegalStateException();
                UIException.annotateUser(ise, null,
                                         NbBundle.getMessage(PointCustomEditor.class,
                                                             "CTL_NegativeSize"),
                                         null, null);
                throw ise;
            }
            if ( dimensionMode )
                return new Dimension (x, y);
            else
                return new Point (x, y);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            UIException.annotateUser(ise, null,
                                     NbBundle.getMessage(PointCustomEditor.class,
                                                         "CTL_InvalidValue"),
                                     null, null);
            throw ise;
        }
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (
            PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
            &&
            PropertyEnv.STATE_VALID.equals(evt.getNewValue())
        ) {
            editor.setValue(getPropertyValue());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        
        insidePanel = new javax.swing.JPanel();
        xLabel = new javax.swing.JLabel();
        xField = new javax.swing.JTextField();
        yLabel = new javax.swing.JLabel();
        yField = new javax.swing.JTextField();
        
        setLayout(new java.awt.BorderLayout());
        
        insidePanel.setLayout(new java.awt.GridBagLayout());
        
        xLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_X"));
        xLabel.setLabelFor(xField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(xLabel, gridBagConstraints);
        
        xField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        insidePanel.add(xField, gridBagConstraints);
        
        yLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Y"));
        yLabel.setLabelFor(yField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(yLabel, gridBagConstraints);
        
        yField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        insidePanel.add(yField, gridBagConstraints);
        
        add(insidePanel, java.awt.BorderLayout.CENTER);
        
    }
// </editor-fold>//GEN-END:initComponents


    private void updateInsets (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateInsets
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            if ( dimensionMode )
                editor.setValue (new Dimension (x, y));
            else
                editor.setValue (new Point (x, y));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }//GEN-LAST:event_updateInsets


// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel insidePanel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField yField;
    private javax.swing.JLabel yLabel;
// End of variables declaration//GEN-END:variables

    private ArrayOfIntSupport editor;

}

