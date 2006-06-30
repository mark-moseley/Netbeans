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
 * PropertyDialogPanel.java
 *
 * Created on October 13, 2003, 12:14 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ResourceBundle;

//AWT
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.Constants;

/**
 *
 * @author  Rajeshwar Patil
 */
public class PropertyDialogPanel extends javax.swing.JPanel{

    String name;
    String value;


    // Validation support
    ValidationSupport validationSupport;

    /** Creates new form PropertyDialogPanel */
    public PropertyDialogPanel() {
        initComponents();

        validationSupport = new ValidationSupport();
        ///markRequiredFields();
    }


    public PropertyDialogPanel(Object[] values) {
        initComponents();

        validationSupport = new ValidationSupport();
        ///markRequiredFields();

        name = (String)values[0];
        value = (String)values[1];
        
        setComponentValues();
    }


    private void setComponentValues(){
        nameTextField.setText(name);
        valueTextField.setText(value);
    }


    public String getName(){
        return name;
    }


    public String getValue(){
        return value;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        valueLabel = new javax.swing.JLabel();
        valueTextField = new javax.swing.JTextField();
        nameReqFlag = new javax.swing.JLabel();
        valueReqFlag = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Name").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 0);
        add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Name_Acsbl_Desc"));

        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Name_Tool_Tip"));
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                msgDstnNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 4);
        add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Name_Acsbl_Desc"));

        valueLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Value").charAt(0));
        valueLabel.setLabelFor(valueTextField);
        valueLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Value_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 11, 0);
        add(valueLabel, gridBagConstraints);
        valueLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Value_Acsbl_Name"));
        valueLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Value_Acsbl_Desc"));

        valueTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Value_Tool_Tip"));
        valueTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jndiNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 11, 4);
        add(valueTextField, gridBagConstraints);
        valueTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Value_Acsbl_Name"));
        valueTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Property_Value_Acsbl_Desc"));

        nameReqFlag.setLabelFor(nameTextField);
        nameReqFlag.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/Bundle").getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 0);
        add(nameReqFlag, gridBagConstraints);

        valueReqFlag.setLabelFor(valueTextField);
        valueReqFlag.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/Bundle").getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 11, 0);
        add(valueReqFlag, gridBagConstraints);

    }//GEN-END:initComponents

    private void jndiNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jndiNameKeyReleased
        // Add your handling code here:
        // get the text from the field
        value = valueTextField.getText();
        firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
    }//GEN-LAST:event_jndiNameKeyReleased

    private void msgDstnNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_msgDstnNameKeyReleased
        // Add your handling code here:
        // get the text from the field
        name = nameTextField.getText();
        firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
    }//GEN-LAST:event_msgDstnNameKeyReleased
    
    //This method appends "*  "  to the label of the field, if it is a mandatory field.
    private void markRequiredFields(){
        if(validationSupport.iSRequiredProperty("/sun-ejb-jar/enterprise-beans/cmp-resource/property/name")){  //NOI18N
            nameLabel.setText(validationSupport.getMarkedLabel(nameLabel.getText()));
        }

        if(validationSupport.iSRequiredProperty("/sun-ejb-jar/enterprise-beans/cmp-resource/property/value")){  //NOI18N
            valueLabel.setText(validationSupport.getMarkedLabel(valueLabel.getText()));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nameReqFlag;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JLabel valueReqFlag;
    private javax.swing.JTextField valueTextField;
    // End of variables declaration//GEN-END:variables
}
