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

package org.netbeans.modules.web.struts.wizards;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.StrutsConfigUtilities;

public class ActionPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider {
    
    static final String DEFAULT_ACTION = "org.apache.struts.action.Action"; //NOI18N
    static final String DISPATCH_ACTION = "org.apache.struts.actions.DispatchAction"; //NOI18N
    static final String MAPPING_DISPATCH_ACTION = "org.apache.struts.actions.MappingDispatchAction"; //NOI18N
    static final String LOOKUP_DISPATCH_ACTION = "org.apache.struts.actions.LookupDispatchAction"; //NOI18N

    private static final String[] SUPERCLASS_LIST = {DEFAULT_ACTION, DISPATCH_ACTION, MAPPING_DISPATCH_ACTION, LOOKUP_DISPATCH_ACTION};
    
    /** Creates new form ActionPanelVisual */
    public ActionPanelVisual(ActionPanel panel) {    
        initComponents();
        jComboBoxSuperclass.setModel(new javax.swing.DefaultComboBoxModel(SUPERCLASS_LIST));
        Project proj = panel.getProject();
        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        String[] configFiles = StrutsConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
        jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelSuperclass = new javax.swing.JLabel();
        jComboBoxSuperclass = new javax.swing.JComboBox();
        jLabelConfigFile = new javax.swing.JLabel();
        jComboBoxConfigFile = new javax.swing.JComboBox();
        jLabelPath = new javax.swing.JLabel();
        jTextFieldPath = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLabelSuperclass.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_Superlass_mnem").charAt(0));
        jLabelSuperclass.setText(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_Superclass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelSuperclass, gridBagConstraints);

        jComboBoxSuperclass.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jComboBoxSuperclass, gridBagConstraints);

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_ConfigFile_mnem").charAt(0));
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_ConfigFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabelConfigFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jComboBoxConfigFile, gridBagConstraints);

        jLabelPath.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_ActionPath").charAt(0));
        jLabelPath.setLabelFor(jTextFieldPath);
        jLabelPath.setText(org.openide.util.NbBundle.getMessage(ActionPanelVisual.class, "LBL_ActionPath"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabelPath, gridBagConstraints);

        jTextFieldPath.setText("/");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jTextFieldPath, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxSuperclass;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JLabel jLabelSuperclass;
    private javax.swing.JTextField jTextFieldPath;
    // End of variables declaration//GEN-END:variables
 
    boolean valid(WizardDescriptor wizardDescriptor) {
        return true;
    }
    
    void read (WizardDescriptor settings) {
    }

    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.ACTION_PATH, jTextFieldPath.getText()); //NOI18N
        settings.putProperty(WizardProperties.ACTION_SUPERCLASS, (String)jComboBoxSuperclass.getSelectedItem()); //NOI18N
        settings.putProperty(WizardProperties.ACTION_CONFIG_FILE, (String)jComboBoxConfigFile.getSelectedItem()); //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ActionPanelVisual.class);
    }

}
