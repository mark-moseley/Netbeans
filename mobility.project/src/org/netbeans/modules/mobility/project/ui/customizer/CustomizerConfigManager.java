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
 * ConfigManager.java
 *
 * Created on February 4, 2004, 5:07 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import org.openide.util.NbBundle;

/**
 *
 * @author  gc149856
 */
public class CustomizerConfigManager extends javax.swing.JPanel {
    
    J2MEProjectProperties j2meProperties=null;
    
    private VisualConfigSupport vcs=null;
    
    /** Creates new form ConfigManager */
    public CustomizerConfigManager(J2MEProjectProperties props, String configuration) {
        initComponents();
        initAccessibility();
        
        this.j2meProperties = props;
        
        if (vcs == null) {
            this.vcs = new VisualConfigSupport(
                    jListConfigs,
                    jButtonAddConfig,
                    jButtonAddMore,
                    jButtonRename,
                    jButtonRemoveConfig,
                    jButtonDuplicate,
                    jButtonSave);
            
            
            // don't need to register with VisualPropertySupport, because we're not modifying properties from this panel
            register(vcs);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListConfigs = new javax.swing.JList();
        jButtonAddConfig = new javax.swing.JButton();
        jButtonAddMore = new javax.swing.JButton();
        jButtonDuplicate = new javax.swing.JButton();
        jButtonRename = new javax.swing.JButton();
        jButtonRemoveConfig = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(450, 300));
        setPreferredSize(new java.awt.Dimension(450, 300));
        setLayout(new java.awt.GridBagLayout());

        configsLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CustomizerConfigManager.class).getString("MNM_CustConfigs_Configurations").charAt(0));
        configsLabel.setLabelFor(jListConfigs);
        configsLabel.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_Configurations")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(configsLabel, gridBagConstraints);

        jScrollPane1.setViewportView(jListConfigs);
        jListConfigs.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CfgManager_Configurations")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 0);
        add(jScrollPane1, gridBagConstraints);

        jButtonAddConfig.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerConfigManager.class).getString("MNM_CustConfigs_Add").charAt(0));
        jButtonAddConfig.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_Add")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 5, 12);
        add(jButtonAddConfig, gridBagConstraints);
        jButtonAddConfig.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CfgManager_Add")); // NOI18N

        jButtonAddMore.setMnemonic(NbBundle.getMessage(CustomizerConfigManager.class, "MNM_CustConfigs_AddMore").charAt(0));
        jButtonAddMore.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_AddMore")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(jButtonAddMore, gridBagConstraints);

        jButtonDuplicate.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerConfigManager.class).getString("MNM_CustConfigs_Duplicate").charAt(0));
        jButtonDuplicate.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_Duplicate")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        add(jButtonDuplicate, gridBagConstraints);
        jButtonDuplicate.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CfgManager_Duplicate")); // NOI18N

        jButtonRename.setMnemonic(NbBundle.getMessage(CustomizerConfigManager.class, "MNM_CustConfigs_Rename").charAt(0));
        jButtonRename.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_Rename")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(jButtonRename, gridBagConstraints);

        jButtonRemoveConfig.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerConfigManager.class).getString("MNM_CustConfigs_Remove").charAt(0));
        jButtonRemoveConfig.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfigs_Remove")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(jButtonRemoveConfig, gridBagConstraints);
        jButtonRemoveConfig.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CfgManager_Remove")); // NOI18N

        jButtonSave.setMnemonic(NbBundle.getMessage(CustomizerConfigManager.class, "MNM_CustConfig_SaveAs").charAt(0));
        jButtonSave.setText(NbBundle.getMessage(CustomizerConfigManager.class, "LBL_CustConfig_SaveAs")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(jButtonSave, gridBagConstraints);
        jButtonSave.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CfgManager_SaveAs")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerConfigManager.class, "ACSN_CustConfig"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerConfigManager.class, "ACSD_CustConfig"));
    }
    
    
    /** Registers VisualConfigSupport containing ConfigPanel items and accompanying
     *  buttons for handling the configs
     */
    private void register(final VisualConfigSupport vcs) {
        vcs.setPropertyMap(j2meProperties);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel configsLabel;
    private javax.swing.JButton jButtonAddConfig;
    private javax.swing.JButton jButtonAddMore;
    private javax.swing.JButton jButtonDuplicate;
    private javax.swing.JButton jButtonRemoveConfig;
    private javax.swing.JButton jButtonRename;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JList jListConfigs;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    
}
