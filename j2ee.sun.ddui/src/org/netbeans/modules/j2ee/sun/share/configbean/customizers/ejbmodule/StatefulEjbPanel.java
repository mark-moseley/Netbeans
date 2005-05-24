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
/*
 * EntityEjbPanel.java        Feb 20, 2005, 6:11 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class StatefulEjbPanel extends javax.swing.JPanel {

    private StatefulEjbCustomizer statefulEjbCutomizer;


    /** Creates new form StatefulEjbPanel */
    public StatefulEjbPanel(StatefulEjbCustomizer customizer) {
        initComponents();
        this.statefulEjbCutomizer = customizer;
    }


    public void setAvailabilityEnabled(String availabilityEnabled){
        if(availabilityEnabled != null){
            availabilityEnabledComboBox.setSelectedItem(availabilityEnabled);
        }
    }


    public String getAvailabilityEnabled(){
        return (String)availabilityEnabledComboBox.getSelectedItem();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        availabilityEnabledLabel = new javax.swing.JLabel();
        availabilityEnabledComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        availabilityEnabledLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Avaiability_Enabled").charAt(0));
        availabilityEnabledLabel.setLabelFor(availabilityEnabledComboBox);
        availabilityEnabledLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Availability_Enabled"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        add(availabilityEnabledLabel, gridBagConstraints);
        availabilityEnabledLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Availability_Enabled_Acsbl_Name"));
        availabilityEnabledLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Availability_Enabled_Acsbl_Desc"));

        availabilityEnabledComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        availabilityEnabledComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Availability_Enabled_Tool_Tip"));
        availabilityEnabledComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                availabilityEnabledItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 5);
        add(availabilityEnabledComboBox, gridBagConstraints);
        availabilityEnabledComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Availability_Enabled_Acsbl_Name"));
        availabilityEnabledComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Availability_Enabled_Acsbl_Desc"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void availabilityEnabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_availabilityEnabledItemStateChanged
        // Add your handling code here:
        String item = (String)availabilityEnabledComboBox.getSelectedItem();
        statefulEjbCutomizer.updateAvailabilityEnabled(item);
        statefulEjbCutomizer.validateEntries();
    }//GEN-LAST:event_availabilityEnabledItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox availabilityEnabledComboBox;
    private javax.swing.JLabel availabilityEnabledLabel;
    // End of variables declaration//GEN-END:variables

}
