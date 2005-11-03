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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.StrutsConfigUtilities;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class FormBeanNewPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, ListDataListener {
    
    /**
     * Creates new form PropertiesPanelVisual
     */
    public FormBeanNewPanelVisual(Project proj) {
        initComponents();
        
        jComboBoxSuperclass.getModel().addListDataListener(this);
        WebModule wm = WebModule.getWebModule(proj.getProjectDirectory());
        if (wm!=null){
            String[] configFiles = StrutsConfigUtilities.getConfigFiles(wm.getDeploymentDescriptor());
            jComboBoxConfigFile.setModel(new javax.swing.DefaultComboBoxModel(configFiles));
        }
        
        
//        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FormBeanNewPanelVisual.class, "ACS_BeanFormProperties"));  // NOI18N
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

        setLayout(new java.awt.GridBagLayout());

        jLabelSuperclass.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(FormBeanNewPanelVisual.class, "LBL_Superlass_mnem").charAt(0));
        jLabelSuperclass.setLabelFor(jComboBoxSuperclass);
        jLabelSuperclass.setText(org.openide.util.NbBundle.getMessage(FormBeanNewPanelVisual.class, "LBL_Superclass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelSuperclass, gridBagConstraints);

        jComboBoxSuperclass.setEditable(true);
        jComboBoxSuperclass.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "org.apache.struts.action.ActionForm", "org.apache.struts.validator.ValidatorForm", "org.apache.struts.validator.ValidatorActionForm" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxSuperclass, gridBagConstraints);

        jLabelConfigFile.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(FormBeanNewPanelVisual.class, "LBL_ConfigFile_mnem").charAt(0));
        jLabelConfigFile.setLabelFor(jComboBoxConfigFile);
        jLabelConfigFile.setText(org.openide.util.NbBundle.getMessage(FormBeanNewPanelVisual.class, "LBL_ConfigFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelConfigFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jComboBoxConfigFile, gridBagConstraints);
        jComboBoxConfigFile.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/struts/wizards/Bundle").getString("ACSD_ConfiguratioFile"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxConfigFile;
    private javax.swing.JComboBox jComboBoxSuperclass;
    private javax.swing.JLabel jLabelConfigFile;
    private javax.swing.JLabel jLabelSuperclass;
    // End of variables declaration//GEN-END:variables
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String superclass = (String) jComboBoxSuperclass.getEditor().getItem();
        String configFile = (String) jComboBoxConfigFile.getSelectedItem();
        
        if (superclass == null || superclass.trim().equals("")){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(FormBeanNewPanelVisual.class, "MSG_NoSuperClassSelected"));
        }
        if (configFile == null || configFile.trim().equals("")){
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(FormBeanNewPanelVisual.class, "MSG_NoConfFileSelectedForBean"));
            return false;
        }
        return true;
    }
    
    void read(WizardDescriptor settings) {
    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.FORMBEAN_SUPERCLASS, jComboBoxSuperclass.getSelectedItem());
        settings.putProperty(WizardProperties.FORMBEAN_CONFIG_FILE, jComboBoxConfigFile.getSelectedItem());
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(FormBeanNewPanelVisual.class);
    }
    
    public void intervalRemoved(ListDataEvent e) {
    }
    
    public void intervalAdded(ListDataEvent e) {
    }
    
    public void contentsChanged(ListDataEvent e) {
        //System.out.println("xxx");
    }
    
}
