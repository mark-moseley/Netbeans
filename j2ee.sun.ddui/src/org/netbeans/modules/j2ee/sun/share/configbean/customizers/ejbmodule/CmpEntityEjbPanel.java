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
/*
 * CmpEntityEjbPanel.java        November 3, 2003, 10:58 AM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class CmpEntityEjbPanel extends javax.swing.JPanel {

    private CmpEntityEjbCustomizer cmpEntityEjbCutomizer;


    /** Creates new form CmpEntityEjbPanel */
    public CmpEntityEjbPanel(CmpEntityEjbCustomizer customizer) {
        initComponents();
        this.cmpEntityEjbCutomizer = customizer;
    }


    public void setMappingProperties(String mappingProperties){
        if(mappingProperties != null){
            mappingPropertiesTextField.setText(mappingProperties);
        }
    }


    public String getMappingProperties(){
        return mappingPropertiesTextField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mappingPropertiesLabel = new javax.swing.JLabel();
        mappingPropertiesTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(292, 60));
        setPreferredSize(new java.awt.Dimension(292, 60));
        mappingPropertiesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Mapping_Properties").charAt(0));
        mappingPropertiesLabel.setLabelFor(mappingPropertiesTextField);
        mappingPropertiesLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Mapping_Properties_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(mappingPropertiesLabel, gridBagConstraints);
        mappingPropertiesLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mapping_Properties_Acsbl_Name"));
        mappingPropertiesLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mapping_Properties_Acsbl_Desc"));

        mappingPropertiesTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mapping_Properties_Tool_Tip"));
        mappingPropertiesTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingPropertiesActionPerformed(evt);
            }
        });
        mappingPropertiesTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mappingPropertiesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                mappingPropertiesFocusLost(evt);
            }
        });
        mappingPropertiesTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mappingPropertiesKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 103;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        gridBagConstraints.weightx = 1.0;
        add(mappingPropertiesTextField, gridBagConstraints);
        mappingPropertiesTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mapping_Properties_Acsbl_Name"));
        mappingPropertiesTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mapping_Properties_Acsbl_Desc"));

    }//GEN-END:initComponents

    private void mappingPropertiesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mappingPropertiesFocusLost
        // Add your handling code here:
        cmpEntityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_mappingPropertiesFocusLost

    private void mappingPropertiesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mappingPropertiesFocusGained
        // Add your handling code here:
        cmpEntityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_mappingPropertiesFocusGained

    private void mappingPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mappingPropertiesActionPerformed
        // Add your handling code here:
        cmpEntityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_mappingPropertiesActionPerformed

    private void mappingPropertiesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mappingPropertiesKeyReleased
        // Add your handling code here:
        String item = mappingPropertiesTextField.getText();
        cmpEntityEjbCutomizer.updateMappingProperties(item);
        cmpEntityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_mappingPropertiesKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel mappingPropertiesLabel;
    private javax.swing.JTextField mappingPropertiesTextField;
    // End of variables declaration//GEN-END:variables

}
