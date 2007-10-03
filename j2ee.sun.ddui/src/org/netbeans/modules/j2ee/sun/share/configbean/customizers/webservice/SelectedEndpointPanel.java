/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * SelectedEndpointPanel.java
 *
 * Created on April 7, 2006, 2:31 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;

import org.openide.ErrorManager;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;


/**
 *
 * @author Peter Williams
 */
public class SelectedEndpointPanel extends javax.swing.JPanel {

    private static final int SECURITY_NONE = 0; // No security settings
    private static final int SECURITY_AUTHENTICATION = 1; // login-config/authentication is set
    private static final int SECURITY_MESSAGE = 2; // message level security is set.
    
    private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); // NOI18N

    private static final ResourceBundle webserviceBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"); // NOI18N

    /** xml <--> ui mapping for authorization method combo box */
    private static final TextMapping [] authMethodTypes = {
        new TextMapping("", ""), // NOI18N
        new TextMapping("BASIC", webserviceBundle.getString("AUTHORIZATION_Basic")),	// NOI18N
        new TextMapping("CLIENT-CERT", webserviceBundle.getString("AUTHORIZATION_ClientCert")),	// NOI18N
    };

    /** xml <--> ui mapping for transport guarantee combo box */
    private static final TextMapping [] transportTypes = {
        new TextMapping("", ""), // NOI18N
        new TextMapping("NONE", webserviceBundle.getString("TRANSPORT_None")),	// NOI18N
        new TextMapping("INTEGRAL", webserviceBundle.getString("TRANSPORT_Integral")),	// NOI18N
        new TextMapping("CONFIDENTIAL", webserviceBundle.getString("TRANSPORT_Confidential")),	// NOI18N
    };
    
    private WebServiceDescriptorCustomizer masterPanel;

    // selected endpoint management
    private EndpointMapping selectedEndpointMap;
    private WebserviceEndpoint selectedEndpoint;
    private boolean selectedEndpointSetup;

    // For ejb endpoints, holds prior config binding if the user is switching
    // the radio buttons.  Only the last selected item will be saved into the
    // endpoint though.
    private LoginConfig loginConfig;
    private MessageSecurityBinding messageBinding;
    
    // authorization method combo box model
    private DefaultComboBoxModel authMethodModel;

    // transport guarantee combo box model
    private DefaultComboBoxModel transportGuaranteeModel;

    // true if AS 9.0+ fields are visible.
    private boolean as90FeaturesVisible;

    /** Creates new form SelectedEndpointPanel */
    public SelectedEndpointPanel(WebServiceDescriptorCustomizer src) {
        masterPanel = src;
        selectedEndpointSetup = false;

        initComponents();
        initUserComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgSecurity = new javax.swing.ButtonGroup();
        jLblEndpointAddressURI = new javax.swing.JLabel();
        jTxtEndpointAddressURI = new javax.swing.JTextField();
        jLblTransportGuarantee = new javax.swing.JLabel();
        jCbxTransportGuarantee = new javax.swing.JComboBox();
        jLblDebugEnabled = new javax.swing.JLabel();
        jChkDebugEnabled = new javax.swing.JCheckBox();
        jLblSecuritySettings = new javax.swing.JLabel();
        jRBnNoSecurity = new javax.swing.JRadioButton();
        jRBnMessageSecurity = new javax.swing.JRadioButton();
        jLblEnableMsgSecurity = new javax.swing.JLabel();
        jChkEnableMsgSecurity = new javax.swing.JCheckBox();
        jBtnEditBindings = new javax.swing.JButton();
        jRBnLoginConfig = new javax.swing.JRadioButton();
        jLblRealm = new javax.swing.JLabel();
        jTxtRealm = new javax.swing.JTextField();
        jLblAuthentication = new javax.swing.JLabel();
        jCbxAuthentication = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLblEndpointAddressURI.setLabelFor(jTxtEndpointAddressURI);
        jLblEndpointAddressURI.setText(WebServiceDescriptorCustomizer.bundle.getString("LBL_EndpointAddressURI_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblEndpointAddressURI, gridBagConstraints);

        jTxtEndpointAddressURI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtEndpointAddressURIKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtEndpointAddressURI, gridBagConstraints);
        jTxtEndpointAddressURI.getAccessibleContext().setAccessibleName(WebServiceDescriptorCustomizer.bundle.getString("ACSN_EndpointAddressURI"));
        jTxtEndpointAddressURI.getAccessibleContext().setAccessibleDescription(WebServiceDescriptorCustomizer.bundle.getString("ACSD_EndpointAddressURI"));

        jLblTransportGuarantee.setLabelFor(jCbxTransportGuarantee);
        jLblTransportGuarantee.setText(WebServiceDescriptorCustomizer.bundle.getString("LBL_TransportGuarantee_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblTransportGuarantee, gridBagConstraints);

        jCbxTransportGuarantee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxTransportGuaranteeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxTransportGuarantee, gridBagConstraints);
        jCbxTransportGuarantee.getAccessibleContext().setAccessibleName(WebServiceDescriptorCustomizer.bundle.getString("ACSN_TransportGuarantee"));
        jCbxTransportGuarantee.getAccessibleContext().setAccessibleDescription(WebServiceDescriptorCustomizer.bundle.getString("ACSD_TransportGuarantee"));

        jLblDebugEnabled.setLabelFor(jChkDebugEnabled);
        jLblDebugEnabled.setText(WebServiceDescriptorCustomizer.bundle.getString("LBL_DebugEnabled_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblDebugEnabled, gridBagConstraints);

        jChkDebugEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jChkDebugEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkDebugEnabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkDebugEnabledItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jChkDebugEnabled, gridBagConstraints);
        jChkDebugEnabled.getAccessibleContext().setAccessibleName(WebServiceDescriptorCustomizer.bundle.getString("ACSN_DebugEnabled"));
        jChkDebugEnabled.getAccessibleContext().setAccessibleDescription(WebServiceDescriptorCustomizer.bundle.getString("ACSD_DebugEnabled"));

        jLblSecuritySettings.setText("Security Settings :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 0, 0);
        add(jLblSecuritySettings, gridBagConstraints);

        bgSecurity.add(jRBnNoSecurity);
        jRBnNoSecurity.setText("No Security");
        jRBnNoSecurity.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBnNoSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnNoSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnNoSecurityActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(jRBnNoSecurity, gridBagConstraints);

        bgSecurity.add(jRBnMessageSecurity);
        jRBnMessageSecurity.setText("Message Security");
        jRBnMessageSecurity.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBnMessageSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnMessageSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnMessageSecurityActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 5, 0);
        add(jRBnMessageSecurity, gridBagConstraints);

        jLblEnableMsgSecurity.setLabelFor(jChkEnableMsgSecurity);
        jLblEnableMsgSecurity.setText(WebServiceDescriptorCustomizer.bundle.getString("LBL_EnableMsgSecurity_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblEnableMsgSecurity, gridBagConstraints);

        jChkEnableMsgSecurity.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jChkEnableMsgSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkEnableMsgSecurity.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkEnableMsgSecurityItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 12);
        add(jChkEnableMsgSecurity, gridBagConstraints);
        jChkEnableMsgSecurity.getAccessibleContext().setAccessibleName(WebServiceDescriptorCustomizer.bundle.getString("ACSN_EnableMsgSecurity"));
        jChkEnableMsgSecurity.getAccessibleContext().setAccessibleDescription(WebServiceDescriptorCustomizer.bundle.getString("ACSD_EnableMsgSecurity"));

        jBtnEditBindings.setText(WebServiceDescriptorCustomizer.bundle.getString("LBL_EditMsgSecBindings"));
        jBtnEditBindings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditBindingsActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jBtnEditBindings, gridBagConstraints);
        jBtnEditBindings.getAccessibleContext().setAccessibleName(WebServiceDescriptorCustomizer.bundle.getString("ACSN_EditMsgSecBindings"));
        jBtnEditBindings.getAccessibleContext().setAccessibleDescription(WebServiceDescriptorCustomizer.bundle.getString("ACSD_EditMsgSecBindings"));

        bgSecurity.add(jRBnLoginConfig);
        jRBnLoginConfig.setText("Login Configuration");
        jRBnLoginConfig.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBnLoginConfig.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnLoginConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnLoginConfigActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 12, 0, 0);
        add(jRBnLoginConfig, gridBagConstraints);

        jLblRealm.setLabelFor(jTxtRealm);
        jLblRealm.setText("Realm :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 6, 0, 0);
        add(jLblRealm, gridBagConstraints);

        jTxtRealm.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtRealmKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 6, 0, 5);
        add(jTxtRealm, gridBagConstraints);

        jLblAuthentication.setLabelFor(jCbxAuthentication);
        jLblAuthentication.setText("Authentication Method :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblAuthentication, gridBagConstraints);

        jCbxAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxAuthenticationActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jCbxAuthentication, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jRBnLoginConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnLoginConfigActionPerformed
        if(!selectedEndpointSetup) {
            selectedEndpoint.setLoginConfig((LoginConfig) loginConfig.clone());
            
            String authMethod = loginConfig.getAuthMethod();
            jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(authMethod));
            if(as90FeaturesVisible) {
                try {                
                    String realm = loginConfig.getRealm();
                    jTxtRealm.setText(realm);
                } catch(VersionNotSupportedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            enableMessageSecurityUI(false);
            enableAuthenticationUI(true);
            masterPanel.getBean().firePropertyChange("loginConfig", null, loginConfig); // NOI18N
        }
    }//GEN-LAST:event_jRBnLoginConfigActionPerformed

    private void jRBnMessageSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnMessageSecurityActionPerformed
        if(!selectedEndpointSetup) {
            try {
                selectedEndpoint.setMessageSecurityBinding((MessageSecurityBinding) messageBinding.clone());
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
            jTxtRealm.setText("");

            enableMessageSecurityUI(true);
            enableAuthenticationUI(false);
            masterPanel.getBean().firePropertyChange("messageSecurity", null, messageBinding); // NOI18N
        }
    }//GEN-LAST:event_jRBnMessageSecurityActionPerformed

    private void jRBnNoSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnNoSecurityActionPerformed
        if(!selectedEndpointSetup) {
            selectedEndpoint.setLoginConfig(null);
            try {
                selectedEndpoint.setMessageSecurityBinding(null);
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
            jTxtRealm.setText("");

            enableMessageSecurityUI(false);
            enableAuthenticationUI(false);
            masterPanel.getBean().setDirty();
        }
    }//GEN-LAST:event_jRBnNoSecurityActionPerformed

    private void jTxtRealmKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtRealmKeyReleased
        if(!selectedEndpointSetup && jRBnLoginConfig.isSelected() && jRBnLoginConfig.isVisible()) {
            String newRealm = jTxtRealm.getText();
            if(newRealm != null) {
                newRealm = newRealm.trim();
            }
            try {
                String oldRealm = loginConfig.getRealm();
                if(!Utils.strEquivalent(newRealm, oldRealm)) {
                    loginConfig.setRealm(newRealm);
                    selectedEndpoint.getLoginConfig().setRealm(newRealm);
                    masterPanel.getBean().firePropertyChange("loginConfig-realm", oldRealm, newRealm);
                }
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }//GEN-LAST:event_jTxtRealmKeyReleased

    private void jCbxAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxAuthenticationActionPerformed
        if(!selectedEndpointSetup && jRBnLoginConfig.isSelected() && jRBnLoginConfig.isVisible()) {
            TextMapping authMapping = (TextMapping) authMethodModel.getSelectedItem();
            String oldAuthMethod = loginConfig.getAuthMethod();
            String newAuthMethod = authMapping.getXMLString();
            if(!Utils.strEquals(newAuthMethod, oldAuthMethod)) {
                loginConfig.setAuthMethod(newAuthMethod);
                selectedEndpoint.getLoginConfig().setAuthMethod(newAuthMethod);
                masterPanel.getBean().firePropertyChange("loginConfig-authMethod", oldAuthMethod, newAuthMethod); // NOI18N
            }
        }
    }//GEN-LAST:event_jCbxAuthenticationActionPerformed

    private void jCbxTransportGuaranteeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxTransportGuaranteeActionPerformed
        if(!selectedEndpointSetup) {
            TextMapping transportMapping = (TextMapping) transportGuaranteeModel.getSelectedItem();
            String oldTransportGuarantee = selectedEndpoint.getTransportGuarantee();
            String newTransportGuarantee = transportMapping.getXMLString();
            if(!Utils.strEquals(newTransportGuarantee, oldTransportGuarantee)) {
                selectedEndpoint.setTransportGuarantee(newTransportGuarantee);
                masterPanel.getBean().firePropertyChange("transportGuarantee", oldTransportGuarantee, newTransportGuarantee); // NOI18N
            }
        }
    }//GEN-LAST:event_jCbxTransportGuaranteeActionPerformed

    private void jChkEnableMsgSecurityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkEnableMsgSecurityItemStateChanged
        if(!selectedEndpointSetup) {
            boolean hasMessageSecurity = Utils.interpretCheckboxState(evt);
            
            try {
                MessageSecurityBinding oldBinding = null, newBinding = null;
                if(hasMessageSecurity) {
                    if(messageBinding != null) {
                        newBinding = (MessageSecurityBinding) messageBinding.clone();
                        selectedEndpoint.setMessageSecurityBinding(newBinding);
                    }
                } else {
                    oldBinding = messageBinding = selectedEndpoint.getMessageSecurityBinding();
                    selectedEndpoint.setMessageSecurityBinding(null);
                }
                
                masterPanel.getBean().firePropertyChange("messageSecurityBinding", oldBinding, newBinding); // NOI18N
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            enableMessageSecurityUI(hasMessageSecurity);
        }
    }//GEN-LAST:event_jChkEnableMsgSecurityItemStateChanged

    private void jChkDebugEnabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkDebugEnabledItemStateChanged
        if(!selectedEndpointSetup) {
            boolean newDebugEnabledFlag = Utils.interpretCheckboxState(evt);
            try {
                String oldDebugEnabled = selectedEndpoint.getDebuggingEnabled();
                String newDebugEnabled = newDebugEnabledFlag ? "true" : null; // NOI18N
                selectedEndpoint.setDebuggingEnabled(newDebugEnabled);
                masterPanel.getBean().firePropertyChange("debugEnabled", oldDebugEnabled, newDebugEnabled); // NOI18N
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }//GEN-LAST:event_jChkDebugEnabledItemStateChanged

    private void jTxtEndpointAddressURIKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtEndpointAddressURIKeyReleased
        if(!selectedEndpointSetup) {
            String oldEndpointAddressUri = selectedEndpoint.getEndpointAddressUri();
            String newEndpointAddressUri = jTxtEndpointAddressURI.getText().trim();
            if(!Utils.strEquals(newEndpointAddressUri, oldEndpointAddressUri)) {
                selectedEndpoint.setEndpointAddressUri(newEndpointAddressUri);
                masterPanel.getBean().firePropertyChange("endpointAddressUri", oldEndpointAddressUri, newEndpointAddressUri); // NOI18N
            }
        }
    }//GEN-LAST:event_jTxtEndpointAddressURIKeyReleased

    private void jBtnEditBindingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditBindingsActionPerformed
        if(!selectedEndpointSetup) {
            try {
                WebServiceDescriptor theBean = masterPanel.getBean();
                ASDDVersion asVersion = theBean.getAppServerVersion();
                String asCloneVersion = "";

                if(theBean.isWarModule()) {
                    asCloneVersion = asVersion.getWebAppVersionAsString();
                } else if(theBean.isEjbModule()) {
                    asCloneVersion = asVersion.getEjbJarVersionAsString();
                }
                
                MessageSecurityBinding binding = selectedEndpoint.getMessageSecurityBinding();
                if(binding == null) {
                    binding = selectedEndpoint.newMessageSecurityBinding();
                    selectedEndpoint.setMessageSecurityBinding(binding);
                }

                EditBinding.editMessageSecurityBinding(masterPanel, theBean, false, binding, asVersion, asCloneVersion);
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }//GEN-LAST:event_jBtnEditBindingsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgSecurity;
    private javax.swing.JButton jBtnEditBindings;
    private javax.swing.JComboBox jCbxAuthentication;
    private javax.swing.JComboBox jCbxTransportGuarantee;
    private javax.swing.JCheckBox jChkDebugEnabled;
    private javax.swing.JCheckBox jChkEnableMsgSecurity;
    private javax.swing.JLabel jLblAuthentication;
    private javax.swing.JLabel jLblDebugEnabled;
    private javax.swing.JLabel jLblEnableMsgSecurity;
    private javax.swing.JLabel jLblEndpointAddressURI;
    private javax.swing.JLabel jLblRealm;
    private javax.swing.JLabel jLblSecuritySettings;
    private javax.swing.JLabel jLblTransportGuarantee;
    private javax.swing.JRadioButton jRBnLoginConfig;
    private javax.swing.JRadioButton jRBnMessageSecurity;
    private javax.swing.JRadioButton jRBnNoSecurity;
    private javax.swing.JTextField jTxtEndpointAddressURI;
    private javax.swing.JTextField jTxtRealm;
    // End of variables declaration//GEN-END:variables
    
    private void initUserComponents() {
        as90FeaturesVisible = true;

        // Setup authorization method combobox
        authMethodModel = new DefaultComboBoxModel();
        for(int i = 0; i < authMethodTypes.length; i++) {
                authMethodModel.addElement(authMethodTypes[i]);
        }
        jCbxAuthentication.setModel(authMethodModel);		
        
        // Setup transport guarantee combobox
        transportGuaranteeModel = new DefaultComboBoxModel();
        for(int i = 0; i < transportTypes.length; i++) {
                transportGuaranteeModel.addElement(transportTypes[i]);
        }
        jCbxTransportGuarantee.setModel(transportGuaranteeModel);		
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new javax.swing.JPanel(), gridBagConstraints);
    }
    
    public void setEndpointMapping(EndpointMapping endpointMapping) {
        try {
            selectedEndpointSetup = true;
            
            WebServiceDescriptor theBean = masterPanel.getBean();
            ASDDVersion asVersion = theBean.getAppServerVersion();
            String asCloneVersion = "";
            selectedEndpointMap = endpointMapping;
            
            if(theBean.isWarModule()) {
                asCloneVersion = asVersion.getWebAppVersionAsString();
            } else if(theBean.isEjbModule()) {
                asCloneVersion = asVersion.getEjbJarVersionAsString();
            }

            handleAS90FieldVisibility(ASDDVersion.SUN_APPSERVER_9_0.compareTo(asVersion) <= 0);

            // Relies on version field initialization from handleAS90FieldVisibility(), above
            showSecurityUI(theBean.isWarModule(), theBean.isEjbModule());
            
            if(selectedEndpointMap != null) {
                selectedEndpoint = selectedEndpointMap.getEndpoint();
            } else {
                selectedEndpoint = null;
            }

            // initialize standard data
            if(selectedEndpoint != null) {
                // endpoint uri
                jTxtEndpointAddressURI.setText(selectedEndpoint.getEndpointAddressUri());

                // transport guarantee
                String transportGuarantee = selectedEndpoint.getTransportGuarantee();
                jCbxTransportGuarantee.setSelectedItem(getTransportGuaranteeMapping(transportGuarantee));
                
                // debug enabled
                if(as90FeaturesVisible) {
                    boolean debugEnabled;
                    try {
                        debugEnabled = Utils.booleanValueOf(selectedEndpoint.getDebuggingEnabled());
                    } catch (VersionNotSupportedException ex) {
                        debugEnabled = false;
                    }
                    jChkDebugEnabled.setSelected(debugEnabled);
                }
                
                // security (all)
                boolean authenticationEnabled = false;
                boolean messageSecurityEnabled = false;

                loginConfig = selectedEndpoint.getLoginConfig();
                if(loginConfig != null) {
                    loginConfig = (LoginConfig) loginConfig.cloneVersion(asCloneVersion);
                    authenticationEnabled = true;
                } else {
                    loginConfig = selectedEndpoint.newLoginConfig();
                }
                
                try {
                    messageBinding = selectedEndpoint.getMessageSecurityBinding();
                    if(messageBinding != null) {
                        messageBinding = (MessageSecurityBinding) messageBinding.cloneVersion(asCloneVersion);
                        messageSecurityEnabled = true;
                    } else {
                        messageBinding = masterPanel.getBean().getConfig().getStorageFactory().createMessageSecurityBinding();
                    }
                } catch (VersionNotSupportedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    messageBinding = null;
                }
                
                // security (web)
                if(theBean.isWarModule()) {
                    jChkEnableMsgSecurity.setSelected(messageSecurityEnabled);
                    enableMessageSecurityUI(messageSecurityEnabled);
                }
                // security (ejb)
                else if(theBean.isEjbModule()) {
                    if(messageSecurityEnabled) {
                        jRBnMessageSecurity.setSelected(true);
                        jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
                        jTxtRealm.setText("");
                    } else if(authenticationEnabled) {
                        jRBnLoginConfig.setSelected(true);
                        String authMethod = loginConfig.getAuthMethod();
                        jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(authMethod));

                        if(as90FeaturesVisible) {
                            try {
                                String realm = loginConfig.getRealm();
                                jTxtRealm.setText(realm);
                            } catch(VersionNotSupportedException ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                        }
                    } else {
                        jRBnNoSecurity.setSelected(true);
                        jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
                        jTxtRealm.setText("");
                    }
                    
                    enableMessageSecurityUI(messageSecurityEnabled);
                    enableAuthenticationUI(authenticationEnabled);
                }
            } else {
                jTxtEndpointAddressURI.setText(""); // NOI18N
                jCbxTransportGuarantee.setSelectedItem(getTransportGuaranteeMapping(null));
                jChkDebugEnabled.setSelected(false);
                enableMessageSecurityUI(false);
            }
        } finally {
                selectedEndpointSetup = false;
        }
    }

    private void handleAS90FieldVisibility(boolean visible) {
        if(as90FeaturesVisible != visible) {
            jLblDebugEnabled.setVisible(visible);
            jChkDebugEnabled.setVisible(visible);
            as90FeaturesVisible = visible;
        }
    }
    
    void reloadEndpointMapping() {
        setEndpointMapping(selectedEndpointMap);
    }
    
    /** This method displays the correct security related UI based on whether the
     *  host is a web module or an ejb jar.
     */
    private void showSecurityUI(boolean showForWebApp, boolean showForEjbJar) {
        // Show web app security fields.
        jLblEnableMsgSecurity.setVisible(showForWebApp);
        jChkEnableMsgSecurity.setVisible(showForWebApp);
        
        // Hide ejb-jar security fields.
        jLblSecuritySettings.setVisible(showForEjbJar);
        jRBnNoSecurity.setVisible(showForEjbJar);
        jRBnMessageSecurity.setVisible(showForEjbJar);
        jRBnLoginConfig.setVisible(showForEjbJar);
        jLblRealm.setVisible(showForEjbJar && as90FeaturesVisible);
        jTxtRealm.setVisible(showForEjbJar && as90FeaturesVisible);
        jLblAuthentication.setVisible(showForEjbJar);
        jCbxAuthentication.setVisible(showForEjbJar);
        
        // This button is shown for both, but if both are false, then we want to hide it
        jBtnEditBindings.setVisible(showForWebApp || showForEjbJar);
    }

    private void enableMessageSecurityUI(boolean enable) {
        jBtnEditBindings.setEnabled(enable);
    }
    
    private void enableAuthenticationUI(boolean enable) {
        jLblRealm.setEnabled(enable);
        jTxtRealm.setEnabled(enable);
        jLblAuthentication.setEnabled(enable);
        jCbxAuthentication.setEnabled(enable);
    }
  
    private TextMapping getAuthorizationMethodMapping(String xmlKey) {
        TextMapping result = authMethodTypes[0]; // Default to BLANK
        if(xmlKey == null) {
            xmlKey = ""; // NOI18N
        }
        for(int i = 0; i < authMethodTypes.length; i++) {
            if(authMethodTypes[i].getXMLString().compareTo(xmlKey) == 0) {
                result = authMethodTypes[i];
                break;
            }
        }

        return result;
    }

    private TextMapping getTransportGuaranteeMapping(String xmlKey) {
        TextMapping result = transportTypes[0]; // Default to BLANK
        if(xmlKey == null) {
            xmlKey = ""; // NOI18N
        }
        for(int i = 0; i < transportTypes.length; i++) {
            if(transportTypes[i].getXMLString().compareTo(xmlKey) == 0) {
                result = transportTypes[i];
                break;
            }
        }

        return result;
    }
    
    public void setContainerEnabled(Container container, boolean enabled) {
        Component [] components = container.getComponents();
        for(int i = 0; i < components.length; i++) {
            components[i].setEnabled(enabled);
            if(components[i] instanceof Container) {
                setContainerEnabled((Container) components[i], enabled);
            }
        }
    }
}
