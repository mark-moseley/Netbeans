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

import java.awt.Point;
import java.awt.Dimension;
import java.util.ResourceBundle;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/** Custom property editor for Point and Dimension
*
* @author   Ian Formanek
* @version  1.00, 01 Sep 1998
*/
public class PointCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       PointCustomEditor.class);

    static final long serialVersionUID =-4067033871196801978L;
    
    private boolean dimensionMode = false;
    
    /** Initializes the Form */
    public PointCustomEditor(PointEditor editor) {
        initComponents ();
        this.editor = editor;
        Point point = (Point)editor.getValue ();
        if (point == null) point = new Point (0, 0);
        xField.setText (""+point.x); // NOI18N
        yField.setText (""+point.y); // NOI18N
        
        xLabel.setDisplayedMnemonic(bundle.getString("CTL_X_Mnemonic").charAt(0));
        yLabel.setDisplayedMnemonic(bundle.getString("CTL_Y_Mnemonic").charAt(0));

        xField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_X"));
        yField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Y"));
        
        commonInit( bundle.getString ("CTL_Point") );
    }
    
    public PointCustomEditor(DimensionEditor editor) {
        dimensionMode = true;
        
        initComponents();
        this.editor = editor;
        Dimension dimension = (Dimension)editor.getValue ();
        if (dimension == null) dimension = new Dimension (0, 0);
        xField.setText ("" + dimension.width);    // NOI18N
        yField.setText ("" + dimension.height);  // NOI18N
        
        xLabel.setText (bundle.getString("CTL_Width"));
        xLabel.setDisplayedMnemonic(bundle.getString("CTL_Width_mnemonic").charAt(0));
        xLabel.setLabelFor(xField);
        yLabel.setText (bundle.getString("CTL_Height"));
        yLabel.setDisplayedMnemonic(bundle.getString("CTL_Height_mnemonic").charAt(0));
        yLabel.setLabelFor(yField);

        xField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Width"));
        yField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Height"));
        
        commonInit( bundle.getString ("CTL_Dimension") );
    }
    
    private void commonInit( String panelTitle ) {
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PointCustomEditor"));
        
        setBorder (new javax.swing.border.EmptyBorder(12, 12, 0, 11));
        insidePanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.TitledBorder (
                                       new javax.swing.border.EtchedBorder (),
                                       " " + panelTitle + " "
                                   ),
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    public Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            if ((x < 0) || (y < 0)) {
                IllegalStateException ise = new IllegalStateException();
                ErrorManager.getDefault().annotate(
                    ise, ErrorManager.ERROR, null, 
                    bundle.getString("CTL_NegativeSize"), null, null);
                throw ise;
            }
            if ( dimensionMode )
                return new Dimension (x, y);
            else
                return new Point (x, y);
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
        insidePanel = new javax.swing.JPanel();
        xLabel = new javax.swing.JLabel();
        xField = new javax.swing.JTextField();
        yLabel = new javax.swing.JLabel();
        yField = new javax.swing.JTextField();
        
        setLayout(new java.awt.BorderLayout());
        
        insidePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        xLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_X"));
        xLabel.setLabelFor(xField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(xLabel, gridBagConstraints1);
        
        xField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        insidePanel.add(xField, gridBagConstraints1);
        
        yLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Y"));
        yLabel.setLabelFor(yField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(yLabel, gridBagConstraints1);
        
        yField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateInsets(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        insidePanel.add(yField, gridBagConstraints1);
        
        add(insidePanel, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents


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
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yField;
    // End of variables declaration//GEN-END:variables

    private ArrayOfIntSupport editor;

}

