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

package org.netbeans.modules.web.project.ui.customizer;

import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.Utils;

import javax.swing.*;

import org.openide.util.NbBundle;
import org.openide.WizardValidationException;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.util.HelpCtx;

public class CustomizerRun extends JPanel implements WebCustomizer.ValidatingPanel, HelpCtx.Provider {
    
    // Helper for storing properties
    private VisualPropertySupport vps;
    private ProjectWebModule wm;

    String[] serverInstanceIDs;
    String[] serverNames;
    String[] serverURLs;
    boolean initialized = false;

    private WebProjectProperties webProperties;

    /** Creates new form CustomizerCompile */
    public CustomizerRun(WebProjectProperties webProperties, ProjectWebModule wm) {
        this.webProperties = webProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACS_CustomizeRun_A11YDesc")); //NOI18N

        this.wm = wm;        
        vps = new VisualPropertySupport(webProperties);
    }

    public void validatePanel() throws WizardValidationException {
        final String message = contextPathValidation();
        if(message != null) {
            throw new WizardValidationException(jTextFieldContextPath, message, message);
        }
    }


    public void initValues() {
        initialized = false;
        Deployment deployment = Deployment.getDefault ();
        serverInstanceIDs = deployment.getServerInstanceIDs ();
        serverNames = new String[serverInstanceIDs.length];
        serverURLs = new String[serverInstanceIDs.length];
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            String serverInstanceDisplayName = 
                    deployment.getServerInstanceDisplayName(serverInstanceIDs [i]);
            // if displayName not set use instanceID instead
            if (serverInstanceDisplayName == null) {                
                serverInstanceDisplayName = serverInstanceIDs [i];
            }
            serverNames[i] = deployment.getServerDisplayName (deployment.getServerID (serverInstanceIDs [i])) 
             + " (" + serverInstanceDisplayName + ")"; //NOI18N
        }

        vps.register(jCheckBoxDisplayBrowser, WebProjectProperties.DISPLAY_BROWSER);
        vps.register(jTextFieldRelativeURL, WebProjectProperties.LAUNCH_URL_RELATIVE);
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, WebProjectProperties.J2EE_SERVER_INSTANCE);

        WebProjectProperties.PropertyDescriptor.Saver contextPathSaver = new WebProjectProperties.PropertyDescriptor.Saver() {
            public void save(WebProjectProperties.PropertyInfo propertyInfo) {
                final String serverInstId = (String) webProperties.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
                final String path = (String) propertyInfo.getValue();
                final String oldValue = (String) propertyInfo.getOldValue();
                if(path != null && !path.equals(oldValue)) {
                    wm.setContextPath(serverInstId, path);
                }
            }
        };
        if (webProperties.get(WebProjectProperties.CONTEXT_PATH) == null) {
            WebProjectProperties.PropertyDescriptor propertyDescriptor = new WebProjectProperties.PropertyDescriptor(
                    WebProjectProperties.CONTEXT_PATH, null, WebProjectProperties.STRING_PARSER, contextPathSaver);
            final String contextPath = wm.getContextPath();
            WebProjectProperties.PropertyInfo propertyInfo =
                    webProperties.new PropertyInfo(propertyDescriptor, contextPath, contextPath);
            webProperties.initProperty(WebProjectProperties.CONTEXT_PATH, propertyInfo);
        }
        // disable editing context path if deployment descriptor does not exist
        jTextFieldContextPath.setEnabled(wm.getDeploymentDescriptor() != null);

        vps.register(jTextFieldContextPath, WebProjectProperties.CONTEXT_PATH);

        jTextFieldRelativeURL.setEditable(jCheckBoxDisplayBrowser.isSelected());

        errorLabel.setForeground(Utils.getErrorColor());
        initialized = true;
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelContextPath = new javax.swing.JLabel();
        jTextFieldContextPath = new javax.swing.JTextField();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        jLabelURLExample = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EtchedBorder());
        jLabelContextPath.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_ContextPath_LabelMnemonic").charAt(0));
        jLabelContextPath.setLabelFor(jTextFieldContextPath);
        jLabelContextPath.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPath_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 0);
        add(jLabelContextPath, gridBagConstraints);

        jTextFieldContextPath.addKeyListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 11, 11);
        add(jTextFieldContextPath, gridBagConstraints);
        jTextFieldContextPath.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_ContextPath_A11YDesc"));

        jLabelServer.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_Server_LabelMnemonic").charAt(0));
        jLabelServer.setLabelFor(jComboBoxServer);
        jLabelServer.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(jComboBoxServer, gridBagConstraints);
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_Server_A11YDesc"));

        jCheckBoxDisplayBrowser.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_DisplayBrowser_LabelMnemonic").charAt(0));
        jCheckBoxDisplayBrowser.setSelected(true);
        jCheckBoxDisplayBrowser.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox"));
        jCheckBoxDisplayBrowser.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(jCheckBoxDisplayBrowser, gridBagConstraints);
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_DisplayBrowser_A11YDesc"));

        jLabelContextPathDesc.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 11, 11);
        add(jLabelContextPathDesc, gridBagConstraints);

        jLabelRelativeURL.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeRun_RelativeURL_LabelMnemonic").charAt(0));
        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        jLabelRelativeURL.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 11, 0);
        add(jLabelRelativeURL, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 11);
        add(jTextFieldRelativeURL, gridBagConstraints);
        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("ACS_CustomizeRun_RelativeURL_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 12);
        add(errorLabel, gridBagConstraints);

        jLabelURLExample.setText(NbBundle.getMessage(CustomizerRun.class, "LBL_RelativeURLExample"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 11);
        add(jLabelURLExample, gridBagConstraints);
        jLabelURLExample.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_RelativeURLExample_A11YDesc"));

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.KeyListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jComboBoxServer) {
                CustomizerRun.this.jComboBoxServerActionPerformed(evt);
            }
            else if (evt.getSource() == jCheckBoxDisplayBrowser) {
                CustomizerRun.this.jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == jTextFieldContextPath) {
                CustomizerRun.this.jTextFieldContextPathKeyReleased(evt);
            }
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
        }
    }//GEN-END:initComponents

    private void jTextFieldContextPathKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldContextPathKeyReleased
        checkContextPath();
    }//GEN-LAST:event_jTextFieldContextPathKeyReleased

    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)
            return;
        String newCtxPath = wm.getContextPath(serverInstanceIDs [jComboBoxServer.getSelectedIndex ()]);
        if (newCtxPath != null) {
            jTextFieldContextPath.setText(newCtxPath);
        }
    }//GEN-LAST:event_jComboBoxServerActionPerformed

    private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
        boolean editable = jCheckBoxDisplayBrowser.isSelected();
        
        jLabelContextPathDesc.setEnabled(editable);
        jLabelRelativeURL.setEnabled(editable);
        jTextFieldRelativeURL.setEditable(editable);
    }//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JLabel jLabelURLExample;
    private javax.swing.JTextField jTextFieldContextPath;
    private javax.swing.JTextField jTextFieldRelativeURL;
    // End of variables declaration//GEN-END:variables

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }

    private boolean checkContextPath() {
        String message = contextPathValidation();
        errorLabel.setText(message);
        return message != null;
    }

    private String contextPathValidation() {
        String contextPath = jTextFieldContextPath.getText();
        String message = null;
        if (contextPath.length() > 0) {
            if (!contextPath.startsWith("/")) {
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_DOES_NOT_START_WITH_SLASH");
            } else if (contextPath.contains("//")) {
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_CONTAINS_DOUBLE_SLASH");
                message = "Context path should not contain \"//\"";
            } else if (contextPath.endsWith("/")) {
                message = NbBundle.getMessage (CustomizerRun.class, "MSG_INVALID_CP_ENDS_WITH_SLASH");
                message = "Context path should not end with \"/\"";
            }
        }
        return message;
    }

}
