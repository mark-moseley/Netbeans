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
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class STSIssued extends javax.swing.JPanel {

    private boolean inSync = false;

    private WSDLComponent comp;
    private SecurityProfile secProfile = null;
    
    /**
     * Creates new form STSIssued
     */
    public STSIssued(WSDLComponent comp, SecurityProfile secProfile) {
        super();
        initComponents();
        this.comp = comp;
        this.secProfile = secProfile;

        inSync = true;
        layoutCombo.removeAllItems();
        layoutCombo.addItem(ComboConstants.STRICT);
        layoutCombo.addItem(ComboConstants.LAX);
        layoutCombo.addItem(ComboConstants.LAXTSFIRST);
        layoutCombo.addItem(ComboConstants.LAXTSLAST);
        
        tokenTypeCombo.removeAllItems();
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML10);
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML11);
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML20);

        keyTypeCombo.removeAllItems();
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC);
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_PUBLIC);
       
        keySizeCombo.removeAllItems();
        keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_128);
        keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_192);
        keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_256);

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
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);
        setChBox(secConvChBox, secConv);
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            setChBox(derivedKeysChBox, SecurityPolicyModelHelper.isRequireDerivedKeys(protToken));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(p));
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(bootPolicy));
            setChBox(encryptOrderChBox, SecurityPolicyModelHelper.isEncryptBeforeSigning(bootPolicy));
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            setChBox(derivedKeysChBox, false);
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(comp));
            setChBox(encryptOrderChBox, SecurityPolicyModelHelper.isEncryptBeforeSigning(comp));
        }
        
        WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, ProtectionToken.class);
        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);

        setCombo(tokenTypeCombo, SecurityTokensModelHelper.getIssuedTokenType(token));
        setCombo(keyTypeCombo, SecurityTokensModelHelper.getIssuedKeyType(token));
        setCombo(keySizeCombo, SecurityTokensModelHelper.getIssuedKeySize(token));
        
        issuerAddressField.setText(SecurityTokensModelHelper.getIssuedIssuerAddress(token));
        issuerMetadataField.setText(SecurityTokensModelHelper.getIssuedIssuerMetadataAddress(token));

        setChBox(reqDerivedKeys, SecurityPolicyModelHelper.isRequireDerivedKeys(token));

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(secBinding));

        enableDisable();
        
        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);

        if (source.equals(secConvChBox)) {
            ((SecureConversationFeature)secProfile).enableSecureConversation(comp, secConvChBox.isSelected());
            sync();
        }

        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            if (source.equals(derivedKeysChBox)) {
                SecurityPolicyModelHelper.enableRequireDerivedKeys(protToken, derivedKeysChBox.isSelected());
            }
            if (source.equals(reqSigConfChBox)) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(
                        SecurityPolicyModelHelper.getWss11(p), reqSigConfChBox.isSelected());
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            if (source.equals(reqSigConfChBox)) {
                SecurityPolicyModelHelper.enableRequireSignatureConfirmation(SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
        }

        WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, ProtectionToken.class);
        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);

        if (source.equals(tokenTypeCombo) || source.equals(keyTypeCombo) || source.equals(keySizeCombo)) {
            SecurityTokensModelHelper.setIssuedTokenRSTAttributes(token, 
                    (String)tokenTypeCombo.getSelectedItem(), 
                    (String)keyTypeCombo.getSelectedItem(), 
                    (String)keySizeCombo.getSelectedItem());
        }

        if (source.equals(issuerAddressField) || source.equals(issuerMetadataField)) {
            SecurityTokensModelHelper.setIssuedTokenAddressAttributes(token, 
                    issuerAddressField.getText(), 
                    issuerMetadataField.getText());
        }
        
        if (source.equals(encryptSignatureChBox)) {
            SecurityPolicyModelHelper.enableEncryptSignature(secBinding, encryptSignatureChBox.isSelected());
            if (secConv) {
                SecurityPolicyModelHelper.enableEncryptSignature(topSecBinding, encryptSignatureChBox.isSelected());
            }
        }
        if (source.equals(encryptOrderChBox)) {
            SecurityPolicyModelHelper.enableEncryptBeforeSigning(secBinding, encryptOrderChBox.isSelected());
            if (secConv) {
                SecurityPolicyModelHelper.enableEncryptBeforeSigning(topSecBinding, encryptOrderChBox.isSelected());
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
        if (source.equals(reqDerivedKeys)) {
            SecurityPolicyModelHelper.enableRequireDerivedKeys(token, reqDerivedKeys.isSelected());
            return;
        }
        
        enableDisable();
    }

    private void enableDisable() {
        boolean secConvEnabled = secConvChBox.isSelected();
        derivedKeysChBox.setEnabled(secConvEnabled);
    }
    
    private void setCombo(JComboBox combo, String item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
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

        secConvChBox = new javax.swing.JCheckBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        derivedKeysChBox = new javax.swing.JCheckBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();
        encryptSignatureChBox = new javax.swing.JCheckBox();
        reqDerivedKeys = new javax.swing.JCheckBox();
        encryptOrderChBox = new javax.swing.JCheckBox();
        issuerAddressLabel = new javax.swing.JLabel();
        issuerAddressField = new javax.swing.JTextField();
        issuerMetadataLabel = new javax.swing.JLabel();
        issuerMetadataField = new javax.swing.JTextField();
        tokenTypeLabel = new javax.swing.JLabel();
        keyTypeLabel = new javax.swing.JLabel();
        keySizeLabel = new javax.swing.JLabel();
        tokenTypeCombo = new javax.swing.JComboBox();
        keyTypeCombo = new javax.swing.JComboBox();
        keySizeCombo = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(secConvChBox, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_SecConvLabel")); // NOI18N
        secConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secConvChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(derivedKeysChBox, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RequireDerivedKeysForSecConv")); // NOI18N
        derivedKeysChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        derivedKeysChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        derivedKeysChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derivedKeysChBoxActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptSignatureChBox, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_EncryptSignatureLabel")); // NOI18N
        encryptSignatureChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptSignatureChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptSignatureChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptSignatureChBox(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqDerivedKeys, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RequireDerivedKeysIssued")); // NOI18N
        reqDerivedKeys.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqDerivedKeys.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqDerivedKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqDerivedKeysActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptOrderChBox, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_EncryptOrderLabel")); // NOI18N
        encryptOrderChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptOrderChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptOrderChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptOrderChBoxActionPerformed(evt);
            }
        });

        issuerAddressLabel.setLabelFor(issuerAddressField);
        org.openide.awt.Mnemonics.setLocalizedText(issuerAddressLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_IssuerAddress")); // NOI18N

        issuerAddressField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerAddressFieldKeyReleased(evt);
            }
        });

        issuerMetadataLabel.setLabelFor(issuerMetadataField);
        org.openide.awt.Mnemonics.setLocalizedText(issuerMetadataLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_IssuerMetadataAddress")); // NOI18N

        issuerMetadataField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerMetadataFieldKeyReleased(evt);
            }
        });

        tokenTypeLabel.setLabelFor(tokenTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(tokenTypeLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RSTTokenType")); // NOI18N

        keyTypeLabel.setLabelFor(keyTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(keyTypeLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RSTKeyType")); // NOI18N

        keySizeLabel.setLabelFor(keySizeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(keySizeLabel, org.openide.util.NbBundle.getMessage(STSIssued.class, "LBL_RSTKeySize")); // NOI18N

        tokenTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tokenTypeComboActionPerformed(evt);
            }
        });

        keyTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyTypeComboActionPerformed(evt);
            }
        });

        keySizeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keySizeComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(reqDerivedKeys)
                    .add(reqSigConfChBox)
                    .add(secConvChBox)
                    .add(derivedKeysChBox)
                    .add(encryptSignatureChBox)
                    .add(encryptOrderChBox)
                    .add(layoutLabel)
                    .add(algoSuiteLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(issuerMetadataLabel)
                            .add(tokenTypeLabel)
                            .add(keyTypeLabel)
                            .add(keySizeLabel)
                            .add(issuerAddressLabel))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tokenTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(keyTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(keySizeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(issuerMetadataField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                            .add(issuerAddressField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, keySizeCombo, keyTypeCombo, layoutCombo, tokenTypeCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerAddressLabel)
                    .add(issuerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerMetadataLabel)
                    .add(issuerMetadataField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tokenTypeLabel)
                    .add(tokenTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyTypeLabel)
                    .add(keyTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keySizeLabel)
                    .add(keySizeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(algoSuiteLabel)
                    .add(algoSuiteCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layoutLabel)
                    .add(layoutCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqDerivedKeys)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secConvChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(derivedKeysChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reqSigConfChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptSignatureChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encryptOrderChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {algoSuiteCombo, keySizeCombo, keyTypeCombo, layoutCombo, tokenTypeCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void keySizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keySizeComboActionPerformed
        setValue(keySizeCombo);
    }//GEN-LAST:event_keySizeComboActionPerformed

    private void keyTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyTypeComboActionPerformed
        setValue(keyTypeCombo);
    }//GEN-LAST:event_keyTypeComboActionPerformed

    private void tokenTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tokenTypeComboActionPerformed
        setValue(tokenTypeCombo);
    }//GEN-LAST:event_tokenTypeComboActionPerformed

    private void issuerMetadataFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerMetadataFieldKeyReleased
        setValue(issuerMetadataField);
    }//GEN-LAST:event_issuerMetadataFieldKeyReleased

    private void issuerAddressFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerAddressFieldKeyReleased
        setValue(issuerAddressField);
    }//GEN-LAST:event_issuerAddressFieldKeyReleased

    private void encryptOrderChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptOrderChBoxActionPerformed
         setValue(encryptOrderChBox);
    }//GEN-LAST:event_encryptOrderChBoxActionPerformed

    private void reqDerivedKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqDerivedKeysActionPerformed
         setValue(reqDerivedKeys);
    }//GEN-LAST:event_reqDerivedKeysActionPerformed

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
         setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void derivedKeysChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derivedKeysChBoxActionPerformed
         setValue(derivedKeysChBox);
    }//GEN-LAST:event_derivedKeysChBoxActionPerformed

    private void secConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secConvChBoxActionPerformed
        setValue(secConvChBox);
    }//GEN-LAST:event_secConvChBoxActionPerformed

    private void encryptSignatureChBox(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptSignatureChBox
        setValue(encryptSignatureChBox);
    }//GEN-LAST:event_encryptSignatureChBox

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JCheckBox derivedKeysChBox;
    private javax.swing.JCheckBox encryptOrderChBox;
    private javax.swing.JCheckBox encryptSignatureChBox;
    private javax.swing.JTextField issuerAddressField;
    private javax.swing.JLabel issuerAddressLabel;
    private javax.swing.JTextField issuerMetadataField;
    private javax.swing.JLabel issuerMetadataLabel;
    private javax.swing.JComboBox keySizeCombo;
    private javax.swing.JLabel keySizeLabel;
    private javax.swing.JComboBox keyTypeCombo;
    private javax.swing.JLabel keyTypeLabel;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqDerivedKeys;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JCheckBox secConvChBox;
    private javax.swing.JComboBox tokenTypeCombo;
    private javax.swing.JLabel tokenTypeLabel;
    // End of variables declaration//GEN-END:variables
    
}
