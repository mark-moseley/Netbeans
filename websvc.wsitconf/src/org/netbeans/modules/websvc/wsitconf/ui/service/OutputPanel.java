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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.awt.Dialog;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TargetsPanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TxModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;

import javax.swing.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class OutputPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private BindingOperation operation;
    private BindingOutput output;
    private Binding binding;
    private UndoManager undoManager;
    private boolean inSync = false;

    private boolean signed = false;
    private boolean endorsing = false;
    
    public OutputPanel(SectionView view, Node node, BindingOutput output, UndoManager undoManager) {
        super(view);
        this.model = output.getModel();
        this.node = node;
        this.output = output;
        this.operation = (BindingOperation)output.getParent();
        this.binding = (Binding)output.getParent().getParent();
        this.undoManager = undoManager;
        initComponents();
        
        tokenComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        targetsButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        configButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        signedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        endorsingChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(tokenCombo);
        addImmediateModifier(signedChBox);
        addImmediateModifier(endorsingChBox);

        inSync = true;
        tokenCombo.removeAllItems();
        tokenCombo.addItem(ComboConstants.NONE);
        tokenCombo.addItem(ComboConstants.X509);
        tokenCombo.addItem(ComboConstants.SAML);
        tokenCombo.addItem(ComboConstants.ISSUED);
        tokenCombo.addItem(ComboConstants.KERBEROS);
        inSync = false;

        sync();
    }

    private void sync() {
        inSync = true;

        WSDLComponent token = null;
        
        WSDLComponent t = SecurityTokensModelHelper.getSupportingToken(output, SecurityTokensModelHelper.SUPPORTING);
        if (t != null) {
            token = t;
            signed = false;
            endorsing = false;
        }
        t = SecurityTokensModelHelper.getSupportingToken(output, SecurityTokensModelHelper.SIGNED_SUPPORTING);
        if (t != null) {
            token = t;
            signed = true;
            endorsing = false;
        }
        t = SecurityTokensModelHelper.getSupportingToken(output, SecurityTokensModelHelper.ENDORSING);
        if (t != null) {
            token = t;
            signed = false;
            endorsing = true;
        }
        t = SecurityTokensModelHelper.getSupportingToken(output, SecurityTokensModelHelper.SIGNED_ENDORSING);
        if (t != null) {
            token = t;
            signed = true;
            endorsing = true;
        }

        signedChBox.setSelected(signed);
        endorsingChBox.setSelected(endorsing);
        tokenCombo.setSelectedItem(SecurityTokensModelHelper.getTokenType(token));

        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(tokenCombo)) {
                String token = (String) tokenCombo.getSelectedItem();
                if (token != null) {
                    SecurityTokensModelHelper.setSupportingTokens(output, token, getSuppType(signed, endorsing));
                }
                enableDisable();
            }
            if (source.equals(signedChBox)) {
                String token = (String) tokenCombo.getSelectedItem();
                signed = signedChBox.isSelected();
                SecurityTokensModelHelper.setSupportingTokens(output, token, getSuppType(signed, endorsing));
                enableDisable();
            }
            if (source.equals(endorsingChBox)) {
                String token = (String) tokenCombo.getSelectedItem();
                endorsing = endorsingChBox.isSelected();
                SecurityTokensModelHelper.setSupportingTokens(output, token, getSuppType(signed, endorsing));
                enableDisable();
            }
        }
    }
    
    private int getSuppType(boolean signed, boolean endorsing) {
        if (signed && endorsing) return SecurityTokensModelHelper.SIGNED_ENDORSING;
        if (signed) return SecurityTokensModelHelper.SIGNED_SUPPORTING;
        if (endorsing) return SecurityTokensModelHelper.ENDORSING;
        return SecurityTokensModelHelper.SUPPORTING;
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        SectionView view = getSectionView();
        enableDisable();
        if (view != null) {
            view.getErrorPanel().clearError();
        }
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() { }

    public void linkButtonPressed(Object ddBean, String ddProperty) { }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return new JButton();
    }
    
    private void enableDisable() {
        
        boolean bSecurityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        boolean oSecurityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(operation);
        
        String profile = null;
        if (bSecurityEnabled) {
             profile = ProfilesModelHelper.getSecurityProfile(binding);
        }
        if (oSecurityEnabled) {
            profile = ProfilesModelHelper.getSecurityProfile(operation);
        }
        
        boolean securityEnabled = bSecurityEnabled || oSecurityEnabled;
        boolean genericProfile = false;
        if (ComboConstants.PROF_GENERIC.equals(profile)) {
            genericProfile = true;
        }
        
        tokenCombo.setEnabled(securityEnabled && genericProfile);
        tokenComboLabel.setEnabled(securityEnabled && genericProfile);
        configButton.setEnabled(securityEnabled && genericProfile);
        targetsButton.setEnabled(securityEnabled);
        boolean tokenSelected = !ComboConstants.NONE.equals((String)tokenCombo.getSelectedItem());
        signedChBox.setEnabled(securityEnabled && tokenSelected && genericProfile);
        endorsingChBox.setEnabled(securityEnabled && tokenSelected && genericProfile);       
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tokenComboLabel = new javax.swing.JLabel();
        tokenCombo = new javax.swing.JComboBox();
        targetsButton = new javax.swing.JButton();
        configButton = new javax.swing.JButton();
        signedChBox = new javax.swing.JCheckBox();
        endorsingChBox = new javax.swing.JCheckBox();

        tokenComboLabel.setLabelFor(tokenCombo);
        tokenComboLabel.setText(org.openide.util.NbBundle.getMessage(OutputPanel.class, "LBL_tokenComboLabel")); // NOI18N

        tokenCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "X509", "Username" }));

        targetsButton.setText(org.openide.util.NbBundle.getMessage(OutputPanel.class, "LBL_SignEncrypt")); // NOI18N
        targetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetsButtonActionPerformed(evt);
            }
        });

        configButton.setText(org.openide.util.NbBundle.getMessage(OutputPanel.class, "LBL_keyConfigButton")); // NOI18N

        signedChBox.setText(org.openide.util.NbBundle.getMessage(OutputPanel.class, "LBL_Token_Signed")); // NOI18N
        signedChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signedChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        endorsingChBox.setText(org.openide.util.NbBundle.getMessage(OutputPanel.class, "LBL_Token_Endorsing")); // NOI18N
        endorsingChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        endorsingChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(signedChBox)
                            .add(endorsingChBox)))
                    .add(layout.createSequentialGroup()
                        .add(tokenComboLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(configButton))
                    .add(targetsButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tokenComboLabel)
                    .add(tokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(signedChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(endorsingChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(targetsButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void targetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetsButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        TargetsPanel targetsPanel = new TargetsPanel(output); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(targetsPanel, 
                NbBundle.getMessage(InputPanel.class, "LBL_Targets_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == dlgDesc.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_targetsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton configButton;
    private javax.swing.JCheckBox endorsingChBox;
    private javax.swing.JCheckBox signedChBox;
    private javax.swing.JButton targetsButton;
    private javax.swing.JComboBox tokenCombo;
    private javax.swing.JLabel tokenComboLabel;
    // End of variables declaration//GEN-END:variables
    
}
