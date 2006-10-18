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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;

/**
 *
 * @author  Martin Grebac
 */
public class ServiceProviderSelectorPanel extends javax.swing.JPanel {

    private boolean inSync = false;
    
    /**
     * Creates new form ServiceProviderSelectorPanel
     */
    public ServiceProviderSelectorPanel(String spUrl, String certAlias, String tokenType, String keyType) {
        super();
        
        initComponents();

        this.setSpUrl(spUrl);
        this.setCertAlias(certAlias);
        this.setTokenType(tokenType);
        this.setKeyType(keyType);
        
        certAliasTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        spUrlLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        spUrlTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        inSync = true;
        tokenTypeCombo.removeAllItems();
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML10);
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML20);

        keyTypeCombo.removeAllItems();
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_PUBLIC);
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC);
        inSync = false;
    }

    public String getSpUrl() {
        return spUrlTextField.getText();
    }

    public void setSpUrl(String spUrl) {
        this.spUrlTextField.setText(spUrl);
    }

    public String getCertAlias() {
        return certAliasTextField.getText();
    }

    public void setCertAlias(String certAlias) {
        this.certAliasTextField.setText(certAlias);
    }

    public String getTokenType() {
        return (String)tokenTypeCombo.getSelectedItem();
    }

    public void setTokenType(String tokenType) {
        if (tokenType != null) {
            this.tokenTypeCombo.setSelectedItem(tokenType);
        }
    }

    public String getKeyType() {
        return (String)tokenTypeCombo.getSelectedItem();
    }

    public void setKeyType(String keyType) {
        if (keyType != null) {
            this.keyTypeCombo.setSelectedItem(keyType);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        spUrlLabel = new javax.swing.JLabel();
        certAliasLabel = new javax.swing.JLabel();
        tokenTypeLabel = new javax.swing.JLabel();
        spUrlTextField = new javax.swing.JTextField();
        certAliasTextField = new javax.swing.JTextField();
        tokenTypeCombo = new javax.swing.JComboBox();
        keyTypeLabel = new javax.swing.JLabel();
        keyTypeCombo = new javax.swing.JComboBox();

        spUrlLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_ProviderURL")); // NOI18N

        certAliasLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_Alias")); // NOI18N

        tokenTypeLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_TokenType")); // NOI18N

        tokenTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tokenTypeComboActionPerformed(evt);
            }
        });

        keyTypeLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_KeyType")); // NOI18N

        keyTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyTypeComboActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spUrlLabel)
                    .add(certAliasLabel)
                    .add(tokenTypeLabel)
                    .add(keyTypeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spUrlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 288, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, certAliasTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, tokenTypeCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, keyTypeCombo, 0, 114, Short.MAX_VALUE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {certAliasTextField, keyTypeCombo, tokenTypeCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(spUrlLabel)
                    .add(spUrlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(certAliasLabel)
                    .add(certAliasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tokenTypeLabel)
                    .add(tokenTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyTypeLabel)
                    .add(keyTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void keyTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyTypeComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keyTypeComboActionPerformed

    private void tokenTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tokenTypeComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tokenTypeComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel certAliasLabel;
    private javax.swing.JTextField certAliasTextField;
    private javax.swing.JComboBox keyTypeCombo;
    private javax.swing.JLabel keyTypeLabel;
    private javax.swing.JLabel spUrlLabel;
    private javax.swing.JTextField spUrlTextField;
    private javax.swing.JComboBox tokenTypeCombo;
    private javax.swing.JLabel tokenTypeLabel;
    // End of variables declaration//GEN-END:variables
    
}
