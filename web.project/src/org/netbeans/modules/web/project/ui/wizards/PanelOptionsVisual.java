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

package org.netbeans.modules.web.project.ui.wizards;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class PanelOptionsVisual extends javax.swing.JPanel {
    
//    private static boolean lastMainClassCheck = false; // XXX Store somewhere
    
    private PanelConfigureProject panel;
    private String j2eeLevel;
    private boolean contextModified = false;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual(PanelConfigureProject panel) {
        initComponents();
        this.panel = panel;
        initJ2EESpecLevels();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        j2eeSpecLabel = new javax.swing.JLabel();
        j2eeSpecComboBox = new javax.swing.JComboBox();
        j2eeSpecDescLabel = new javax.swing.JLabel();
        j2eeSpecDescTextPane = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_NWP1_SetAsMain_CheckBox"));
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(setAsMainCheckBox, gridBagConstraints);

        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_ContextPath_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldContextPathKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jTextFieldContextPath, gridBagConstraints);

        j2eeSpecLabel.setLabelFor(j2eeSpecComboBox);
        j2eeSpecLabel.setText(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_NWP1_J2EESpecLevel_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(j2eeSpecLabel, gridBagConstraints);

        j2eeSpecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j2eeSpecComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(j2eeSpecComboBox, gridBagConstraints);

        j2eeSpecDescLabel.setLabelFor(j2eeSpecDescTextPane);
        j2eeSpecDescLabel.setText(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_NWP1_J2EESpecLevelDesc_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(j2eeSpecDescLabel, gridBagConstraints);

        j2eeSpecDescTextPane.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        j2eeSpecDescTextPane.setEditable(false);
        j2eeSpecDescTextPane.setFocusable(false);
        j2eeSpecDescTextPane.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(j2eeSpecDescTextPane, gridBagConstraints);

    }//GEN-END:initComponents

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        contextModified = true;
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void j2eeSpecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_j2eeSpecComboBoxActionPerformed
        StringBuffer str = new StringBuffer("J2EESpecLevel_Desc_"); //NOI18N
        str.append(j2eeSpecComboBox.getSelectedIndex());
        j2eeSpecDescTextPane.setText(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString(str.toString())); //NOI18N
        switch (j2eeSpecComboBox.getSelectedIndex()) {
            case 0: j2eeLevel = WebModule.J2EE_14_LEVEL;
                    break;
            case 1: j2eeLevel = WebModule.J2EE_13_LEVEL;
        }            
    }//GEN-LAST:event_j2eeSpecComboBoxActionPerformed
    
    boolean valid() {
        return true;
    }

    void store(WizardDescriptor d) {
        d.putProperty(WizardProperties.SET_AS_MAIN, setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE );
        d.putProperty(WizardProperties.J2EE_LEVEL, j2eeLevel);
        d.putProperty(WizardProperties.CONTEXT_PATH, jTextFieldContextPath.getText().trim());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox j2eeSpecComboBox;
    private javax.swing.JLabel j2eeSpecDescLabel;
    private javax.swing.JTextPane j2eeSpecDescTextPane;
    private javax.swing.JLabel j2eeSpecLabel;
    private javax.swing.JLabel jLabelContextPath;
    protected javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JCheckBox setAsMainCheckBox;
    // End of variables declaration//GEN-END:variables

    private void initJ2EESpecLevels() {
       j2eeSpecComboBox.addItem(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("J2EESpecLevel_0")); //NOI18N
       j2eeSpecComboBox.addItem(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("J2EESpecLevel_1")); //NOI18N
       j2eeSpecComboBox.setSelectedIndex(0);
    }

    protected boolean isContextModified() {
        return contextModified;
    }
    
}

