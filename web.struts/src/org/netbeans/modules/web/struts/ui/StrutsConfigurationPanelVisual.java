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

package org.netbeans.modules.web.struts.ui;

import javax.swing.event.DocumentListener;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class StrutsConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener {
    
    private StrutsConfigurationPanel panel;
    
    /** Creates new form StrutsConfigurationPanelVisual */
    public StrutsConfigurationPanelVisual(StrutsConfigurationPanel panel, boolean customizer) {
        this.panel = panel;
        initComponents();
        
        jTextFieldAppResource.getDocument().addDocumentListener(this);
        
        if (customizer) {
            jCheckBoxTLD.setVisible(false);
            jCheckBoxWAR.setVisible(false);
            enableComponents(false);
        }        
        else {
            jCheckBoxTLD.setVisible(true);
            jCheckBoxWAR.setVisible(true);
            enableComponents(true);
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

        jLabelServletName = new javax.swing.JLabel();
        jTextFieldServletName = new javax.swing.JTextField();
        jLabelURLPattern = new javax.swing.JLabel();
        jComboBoxURLPattern = new javax.swing.JComboBox();
        jLabelAppResource = new javax.swing.JLabel();
        jTextFieldAppResource = new javax.swing.JTextField();
        jCheckBoxTLD = new javax.swing.JCheckBox();
        jCheckBoxWAR = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabelServletName.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "MNE_ConfigPanel_ActionServletName_Mnemonic").charAt(0));
        jLabelServletName.setLabelFor(jTextFieldServletName);
        jLabelServletName.setText(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "LBL_ConfigPanel_ActionServletName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelServletName, gridBagConstraints);

        jTextFieldServletName.setEditable(false);
        jTextFieldServletName.setText("action");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jTextFieldServletName, gridBagConstraints);

        jLabelURLPattern.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "MNE_ConfigPanel_URLPattern_Mnemonic").charAt(0));
        jLabelURLPattern.setLabelFor(jComboBoxURLPattern);
        jLabelURLPattern.setText(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "LBL_ConfigPanel_URLPattern"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelURLPattern, gridBagConstraints);

        jComboBoxURLPattern.setEditable(true);
        jComboBoxURLPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "*.do", "/do/*" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jComboBoxURLPattern, gridBagConstraints);

        jLabelAppResource.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "MNE_ConfigPanel_ApplicationResource_Mnemonic").charAt(0));
        jLabelAppResource.setLabelFor(jTextFieldAppResource);
        jLabelAppResource.setText(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "LBL_ConfigPanel_ApplicationResource"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelAppResource, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jTextFieldAppResource, gridBagConstraints);

        jCheckBoxTLD.setMnemonic(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "MNE_ConfigPanel_InstallStrutsTLDs_Mnemonic").charAt(0));
        jCheckBoxTLD.setText(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "LBL_ConfigPanel_InstallStrutsTLDs"));
        jCheckBoxTLD.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jCheckBoxTLD.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jCheckBoxTLD, gridBagConstraints);

        jCheckBoxWAR.setMnemonic(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "MNE_ConfigPanel_PackageStrutsJars_Mnemonic").charAt(0));
        jCheckBoxWAR.setSelected(true);
        jCheckBoxWAR.setText(org.openide.util.NbBundle.getMessage(StrutsConfigurationPanelVisual.class, "LBL_ConfigPanel_PackageStrutsJars"));
        jCheckBoxWAR.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jCheckBoxWAR.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(jCheckBoxWAR, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxTLD;
    private javax.swing.JCheckBox jCheckBoxWAR;
    private javax.swing.JComboBox jComboBoxURLPattern;
    private javax.swing.JLabel jLabelAppResource;
    private javax.swing.JLabel jLabelServletName;
    private javax.swing.JLabel jLabelURLPattern;
    private javax.swing.JTextField jTextFieldAppResource;
    private javax.swing.JTextField jTextFieldServletName;
    // End of variables declaration//GEN-END:variables
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        //NOT IMPLEMENTED YET
        return true;
    }

    void validate (WizardDescriptor d) throws WizardValidationException {
//        projectLocationPanel.validate (d);
    }
    
    void read (WizardDescriptor d) {
//        projectLocationPanel.read(d);
//        optionsPanel.read(d);
    }

    void store(WizardDescriptor d) {
//        projectLocationPanel.store(d);
//        optionsPanel.store(d);
    }
    
    void enableComponents(boolean enable) {
        jComboBoxURLPattern.setEnabled(enable);
        jTextFieldAppResource.setEnabled(enable);
        jTextFieldServletName.setEnabled(enable);
        jCheckBoxTLD.setEnabled(enable);
        jCheckBoxWAR.setEnabled(enable);
        jLabelAppResource.setEnabled(enable);
        jLabelServletName.setEnabled(enable);
        jLabelURLPattern.setEnabled(enable);
                
    }

    public String getURLPattern(){
        return (String)jComboBoxURLPattern.getSelectedItem();
    }
    
    public void setURLPattern(String pattern){
        jComboBoxURLPattern.setSelectedItem(pattern);
    }
    
    public String getServletName(){
        return jTextFieldServletName.getText();
    }
    
    public void setServletName(String name){
        jTextFieldServletName.setText(name);
    }
    
    public String getAppResource(){
        return jTextFieldAppResource.getText();
    }
    
    public void setAppResource(String resource){
        jTextFieldAppResource.setText(resource);
    }
    
    public boolean addTLDs(){
        return jCheckBoxTLD.isSelected();
    }
    
    public boolean packageWars(){
        return jCheckBoxWAR.isSelected();
    }
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(StrutsConfigurationPanelVisual.class);
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

}
