/*
 * JSFConfigurationPanelVisual.java
 *
 * Created on August 10, 2005, 6:59 PM
 */

package org.netbeans.modules.web.jsf.wizards;

import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  petr
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {
    
    private JSFConfigurationPanel panel;
    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, boolean customizer) {
        initComponents();
        this.panel = panel;
        tURLPattern.getDocument().addDocumentListener(this);
        if (customizer){
            cbPackageJars.setVisible(false);
            cbPackageJars.setVisible(false);
            tURLPattern.setEnabled(false);
            cbValidate.setEnabled(false);
            cbVerify.setEnabled(false);
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

        lServletName = new javax.swing.JLabel();
        tServletName = new javax.swing.JTextField();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbValidate = new javax.swing.JCheckBox();
        cbVerify = new javax.swing.JCheckBox();
        cbPackageJars = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        lServletName.setLabelFor(tServletName);
        lServletName.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Servlet_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(lServletName, gridBagConstraints);

        tServletName.setEditable(false);
        tServletName.setText("Faces Servlet");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        add(tServletName, gridBagConstraints);

        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lURLPattern, gridBagConstraints);

        tURLPattern.setText("/faces/*");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        add(tURLPattern, gridBagConstraints);

        cbValidate.setSelected(true);
        cbValidate.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Validate_XML"));
        cbValidate.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbValidate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(cbValidate, gridBagConstraints);

        cbVerify.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Verify_Objects"));
        cbVerify.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbVerify.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        add(cbVerify, gridBagConstraints);

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs"));
        cbPackageJars.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbPackageJars.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cbPackageJars, gridBagConstraints);

        jPanel1.setEnabled(false);
        jPanel1.setFocusable(false);
        jPanel1.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JCheckBox cbValidate;
    private javax.swing.JCheckBox cbVerify;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lServletName;
    private javax.swing.JLabel lURLPattern;
    private javax.swing.JTextField tServletName;
    private javax.swing.JTextField tURLPattern;
    // End of variables declaration//GEN-END:variables
 
    boolean valid(WizardDescriptor wizardDescriptor) {
        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")){
          wizardDescriptor.putProperty("WizardPanel_errorMessage",                                  // NOI18N
                NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
          return false;
        }
        if (!isPatternValid(urlPattern)){
          wizardDescriptor.putProperty("WizardPanel_errorMessage",                                  // NOI18N
                NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
          return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", null);                             // NOI18N
        return true;
    }
    
    private boolean isPatternValid(String pattern){
        if (pattern.startsWith("*.")){
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1
                    && p.indexOf('/') == -1 && !p.trim().equals(""))
                return true;
        }
        if (pattern.endsWith("/*") && pattern.startsWith("/"))
            return true;
        return false;
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
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JSFConfigurationPanelVisual.class);
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

    public String getServletName(){
        return tServletName.getText();
    }
    
    public void setServletName(String name){
        tServletName.setText(name);
    }
    
    public String getURLPattern(){
        return tURLPattern.getText();
    }
    
    public void setURLPattern(String pattern){
        tURLPattern.setText(pattern);
    }
    
    public boolean validateXML(){
        return cbVerify.isSelected();
    }
    
    public void setValidateXML(boolean ver){
        cbVerify.setSelected(ver);
    }
    
    public boolean verifyObjects(){
        return cbValidate.isSelected();
    }
    
    public void setVerifyObjects(boolean val){
        cbValidate.setSelected(val);
    }
    
    public boolean packageJars(){
        return cbPackageJars.isSelected();
    }
}
