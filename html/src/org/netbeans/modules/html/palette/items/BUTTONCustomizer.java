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

package org.netbeans.modules.html.palette.items;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;




/**
 *
 * @author  Libor Kotouc
 */
public class BUTTONCustomizer extends javax.swing.JPanel {
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;

    BUTTON button;
            
    /** Creates new form TABLECustomizerPanel */
    public BUTTONCustomizer(BUTTON button) {
        this.button = button;
        
        initComponents();
    }
    
    public boolean showDialog() {
        
        dialogOK = false;
        
        String displayName = "";
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.html.palette.items.resources.Bundle").getString("NAME_html-BUTTON"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(BUTTONCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void evaluateInput() {
        
        String value = jTextField1.getText();
        button.setValue(value);
        
        if (jRadioButton1.isSelected())
            button.setType(BUTTON.TYPE_SUBMIT);
        else if (jRadioButton2.isSelected())
            button.setType(BUTTON.TYPE_RESET);
        else if (jRadioButton3.isSelected())
            button.setType(BUTTON.TYPE_BUTTON);

        button.setDisabled(jCheckBox1.isSelected());
        
        String name = jTextField2.getText();
        button.setName(name);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jTextField1.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel1.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_Label"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Label"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_Type"));
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Type"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_InitState"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_InitState"));
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_InitState"));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_submit"));
        jRadioButton1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jRadioButton1, gridBagConstraints);
        jRadioButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_submit"));
        jRadioButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Submit"));

        buttonGroup1.add(jRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_reset"));
        jRadioButton2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jRadioButton2, gridBagConstraints);
        jRadioButton2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_reset"));
        jRadioButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Reset"));

        buttonGroup1.add(jRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton3, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_standard"));
        jRadioButton3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jRadioButton3, gridBagConstraints);
        jRadioButton3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_standard"));
        jRadioButton3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Standard"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_disabled"));
        jCheckBox1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_disabled"));
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_disabled"));

        jTextField2.setColumns(30);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(jTextField2, gridBagConstraints);

        jLabel4.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "LBL_BUTTON_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSN_BUTTON_Name"));
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BUTTONCustomizer.class, "ACSD_BUTTON_Name"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
    
}
