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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class MessageAuthentication extends javax.swing.JPanel {

    private boolean inSync = false;

    private WSDLComponent comp;
    private SecurityProfile secProfile = null;
    
    /**
     * Creates new form MessageAuthentication
     */
    public MessageAuthentication(WSDLComponent comp, SecurityProfile secProfile) {
        super();
        initComponents();
        this.comp = comp;
        this.secProfile = secProfile;

        inSync = true;
        supportTokenCombo.removeAllItems();
        supportTokenCombo.addItem(ComboConstants.X509);
        supportTokenCombo.addItem(ComboConstants.USERNAME);

        wssVersionCombo.removeAllItems();
        wssVersionCombo.addItem(ComboConstants.WSS10);
        wssVersionCombo.addItem(ComboConstants.WSS11);

        layoutCombo.removeAllItems();
        layoutCombo.addItem(ComboConstants.STRICT);
        layoutCombo.addItem(ComboConstants.LAX);
        layoutCombo.addItem(ComboConstants.LAXTSFIRST);
        layoutCombo.addItem(ComboConstants.LAXTSLAST);
        
        algoSuiteCombo.removeAllItems();
        algoSuiteCombo.addItem(ComboConstants.BASIC256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192);
        algoSuiteCombo.addItem(ComboConstants.BASIC128);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDES);
        algoSuiteCombo.addItem(ComboConstants.BASIC256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESRSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256RSA15);
        
        inSync = false;
        
        sync();
    }
    
    private void sync() {
        inSync = true;

        WSDLComponent secBinding = null;

        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
        boolean secConv = (endToken != null);

        WSDLComponent secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(secConvT, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            setChBox(secConvChBox, true);
            setChBox(derivedKeysSecConvChBox, SecurityPolicyModelHelper.isRequireDerivedKeys(secConvT));
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(p));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(p));
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            setChBox(secConvChBox, false);
            setChBox(derivedKeysSecConvChBox, false);
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(comp));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        }

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(secBinding));

        enableDisable();

        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        
        
        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
        boolean secConv = (endToken != null);

        WSDLComponent secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);

        if (source.equals(secConvChBox)) {
            ((SecureConversationFeature)secProfile).enableSecureConversation(comp, secConvChBox.isSelected());
            sync();
        }
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(secConvT, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            if (source.equals(derivedKeysSecConvChBox)) {
                SecurityPolicyModelHelper.enableRequireDerivedKeys(secConvT, derivedKeysSecConvChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = SecurityPolicyModelHelper.enableWss(p, wss11);
                if (wss11) {
                        SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(p), reqSigConfChBox.isSelected());
                }
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            }
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class);
            if (source.equals(supportTokenCombo)) {
                SecurityTokensModelHelper.setSupportingTokens(p, 
                        (String)supportTokenCombo.getSelectedItem(), 
                        SecurityTokensModelHelper.SIGNED_SUPPORTING);
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            if (source.equals(reqSigConfChBox)) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                        SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = SecurityPolicyModelHelper.enableWss(comp, wss11);
                if (wss11) {
                    SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
                }
                SecurityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
            }
            if (source.equals(supportTokenCombo)) {
                SecurityTokensModelHelper.setSupportingTokens(comp, 
                        (String)supportTokenCombo.getSelectedItem(), 
                        SecurityTokensModelHelper.SIGNED_SUPPORTING);
            }
        }

        if (source.equals(layoutCombo)) {
            SecurityPolicyModelHelper.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
            if (secConv) {
                SecurityPolicyModelHelper.setLayout(topSecBinding, (String) layoutCombo.getSelectedItem());
            }
        }
        if (source.equals(algoSuiteCombo)) {
            AlgoSuiteModelHelper.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
            if (secConv) {
                AlgoSuiteModelHelper.setAlgorithmSuite(topSecBinding, (String) algoSuiteCombo.getSelectedItem());
            }
        }
        
        enableDisable();
    }

    private void enableDisable() {
        boolean secConvEnabled = secConvChBox.isSelected();
        derivedKeysSecConvChBox.setEnabled(secConvEnabled);
        reqSigConfChBox.setEnabled(!secConvEnabled);
        
        boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
        reqSigConfChBox.setEnabled(wss11);
    }
    
    private void setCombo(JComboBox combo, String item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
    }

    private void setCombo(JComboBox combo, boolean second) {
        combo.setSelectedIndex(second ? 1 : 0);
    }
        
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        supportTokenLabel = new javax.swing.JLabel();
        supportTokenCombo = new javax.swing.JComboBox();
        secConvChBox = new javax.swing.JCheckBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        derivedKeysSecConvChBox = new javax.swing.JCheckBox();
        wssVersionLabel = new javax.swing.JLabel();
        wssVersionCombo = new javax.swing.JComboBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();

        supportTokenLabel.setLabelFor(supportTokenCombo);
        org.openide.awt.Mnemonics.setLocalizedText(supportTokenLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AuthToken")); // NOI18N

        supportTokenCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supportTokenComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(secConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_SecConvLabel")); // NOI18N
        secConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secConvChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(derivedKeysSecConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireDerivedKeysForSecConv")); // NOI18N
        derivedKeysSecConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        derivedKeysSecConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        derivedKeysSecConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derivedKeysSecConvChBoxActionPerformed(evt);
            }
        });

        wssVersionLabel.setLabelFor(wssVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(wssVersionLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_WSSVersionLabel")); // NOI18N

        wssVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wssVersionComboActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(reqSigConfChBox)
                    .add(secConvChBox)
                    .add(derivedKeysSecConvChBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layoutLabel)
                            .add(supportTokenLabel)
                            .add(wssVersionLabel)
                            .add(algoSuiteLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, supportTokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, wssVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(33, 33, 33))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, layoutCombo, supportTokenCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(supportTokenLabel)
                    .add(supportTokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wssVersionLabel)
                    .add(wssVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algoSuiteLabel)
                    .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layoutLabel)
                    .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secConvChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(derivedKeysSecConvChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqSigConfChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {layoutCombo, supportTokenCombo, wssVersionCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
         setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void derivedKeysSecConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derivedKeysSecConvChBoxActionPerformed
         setValue(derivedKeysSecConvChBox);
    }//GEN-LAST:event_derivedKeysSecConvChBoxActionPerformed

    private void secConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secConvChBoxActionPerformed
        setValue(secConvChBox);
    }//GEN-LAST:event_secConvChBoxActionPerformed

    private void wssVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wssVersionComboActionPerformed
        setValue(wssVersionCombo);
    }//GEN-LAST:event_wssVersionComboActionPerformed

    private void supportTokenComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supportTokenComboActionPerformed
        setValue(supportTokenCombo);
    }//GEN-LAST:event_supportTokenComboActionPerformed

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JCheckBox derivedKeysSecConvChBox;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JCheckBox secConvChBox;
    private javax.swing.JComboBox supportTokenCombo;
    private javax.swing.JLabel supportTokenLabel;
    private javax.swing.JComboBox wssVersionCombo;
    private javax.swing.JLabel wssVersionLabel;
    // End of variables declaration//GEN-END:variables
    
}
