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
 * EntityEjbPanel.java        October 23, 2003, 3:31 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class EntityEjbPanel extends javax.swing.JPanel {

    private EntityEjbCustomizer entityEjbCutomizer;


    /** Creates new form EntityEjbPanel */
    public EntityEjbPanel(EntityEjbCustomizer customizer) {
        initComponents();
        this.entityEjbCutomizer = customizer;
    }


    public void setIsreadOnlyBean(String isReadOnlyBean){
        if(isReadOnlyBean != null){
            isReadOnlyBeanComboBox.setSelectedItem(isReadOnlyBean);
        }
    }


    public void setRefreshPeriodInSeconds(String refPeriodInSecs){
        if(refPeriodInSecs != null){
            refreshPeriodInSecondsTextField.setText(refPeriodInSecs);
        }
    }


    public void setCommitOption(String commitOption){
        if(commitOption != null){
            commitOptionComboBox.setSelectedItem(commitOption);
        }
    }

    
    public String getIsreadOnlyBean(){
        return (String)isReadOnlyBeanComboBox.getSelectedItem();
    }


    public String getRefreshPeriodInSeconds(){
        return refreshPeriodInSecondsTextField.getText();
    }


    public String getCommitOption(){
        return (String)commitOptionComboBox.getSelectedItem();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        isReadOnlyBeanLabel = new javax.swing.JLabel();
        isReadOnlyBeanComboBox = new javax.swing.JComboBox();
        refreshPeriodInSecondsLabel = new javax.swing.JLabel();
        refreshPeriodInSecondsTextField = new javax.swing.JTextField();
        commitOptionLabel = new javax.swing.JLabel();
        commitOptionComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        isReadOnlyBeanLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Is_Read_Only_Bean").charAt(0));
        isReadOnlyBeanLabel.setLabelFor(isReadOnlyBeanComboBox);
        isReadOnlyBeanLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Is_Read_Only_Bean_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(isReadOnlyBeanLabel, gridBagConstraints);
        isReadOnlyBeanLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Read_Only_Bean_Acsbl_Name"));
        isReadOnlyBeanLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Read_Only_Bean_Acsbl_Desc"));

        isReadOnlyBeanComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        isReadOnlyBeanComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Read_Only_Bean_Tool_Tip"));
        isReadOnlyBeanComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                isReadOnlyBeanItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(isReadOnlyBeanComboBox, gridBagConstraints);
        isReadOnlyBeanComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Read_Only_Bean_Acsbl_Name"));
        isReadOnlyBeanComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Read_Only_Bean_Acsbl_Desc"));

        refreshPeriodInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Refresh_Period_In_Seconds").charAt(0));
        refreshPeriodInSecondsLabel.setLabelFor(refreshPeriodInSecondsTextField);
        refreshPeriodInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Refresh_Period_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(refreshPeriodInSecondsLabel, gridBagConstraints);
        refreshPeriodInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Refresh_Period_In_Seconds_Acsbl_Name"));
        refreshPeriodInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Refresh_Period_In_Seconds_Acsbl_Desc"));

        refreshPeriodInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Refresh_Period_In_Seconds_Tool_Tip"));
        refreshPeriodInSecondsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshPeriodInSecondsActionPerformed(evt);
            }
        });
        refreshPeriodInSecondsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                refreshPeriodInSecondsFocusGained(evt);
            }
        });
        refreshPeriodInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                refreshPeriodInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(refreshPeriodInSecondsTextField, gridBagConstraints);
        refreshPeriodInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Refresh_Period_In_Seconds_Acsbl_Name"));
        refreshPeriodInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Refresh_Period_In_Seconds_Acsbl_Desc"));

        commitOptionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Commit_Option").charAt(0));
        commitOptionLabel.setLabelFor(commitOptionComboBox);
        commitOptionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Commit_Option_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(commitOptionLabel, gridBagConstraints);
        commitOptionLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Commit_Option_Acsbl_Name"));
        commitOptionLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Commit_Option_Acsbl_Desc"));

        commitOptionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "B", "C" }));
        commitOptionComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Commit_Option_Tool_Tip"));
        commitOptionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                commitOptionItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(commitOptionComboBox, gridBagConstraints);
        commitOptionComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Commit_Option_Acsbl_Name"));
        commitOptionComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Commit_Option_Acsbl_Desc"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void refreshPeriodInSecondsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshPeriodInSecondsActionPerformed
        // Add your handling code here:
        entityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_refreshPeriodInSecondsActionPerformed

    private void refreshPeriodInSecondsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_refreshPeriodInSecondsFocusGained
        // Add your handling code here:
        entityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_refreshPeriodInSecondsFocusGained

    private void commitOptionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_commitOptionItemStateChanged
        // Add your handling code here:
        String item = (String)commitOptionComboBox.getSelectedItem();
        entityEjbCutomizer.updateSetCommitOption(item);
        entityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_commitOptionItemStateChanged

    private void refreshPeriodInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refreshPeriodInSecondsKeyReleased
        // Add your handling code here:
        String item = refreshPeriodInSecondsTextField.getText();
        entityEjbCutomizer.updateRefreshPeriodInSeconds(item);
        entityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_refreshPeriodInSecondsKeyReleased

    private void isReadOnlyBeanItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_isReadOnlyBeanItemStateChanged
        // Add your handling code here:
        String item = (String)isReadOnlyBeanComboBox.getSelectedItem();
        entityEjbCutomizer.updateIsReadOnlyBean(item);
        entityEjbCutomizer.validateEntries();
    }//GEN-LAST:event_isReadOnlyBeanItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox commitOptionComboBox;
    private javax.swing.JLabel commitOptionLabel;
    private javax.swing.JComboBox isReadOnlyBeanComboBox;
    private javax.swing.JLabel isReadOnlyBeanLabel;
    private javax.swing.JLabel refreshPeriodInSecondsLabel;
    private javax.swing.JTextField refreshPeriodInSecondsTextField;
    // End of variables declaration//GEN-END:variables

}
