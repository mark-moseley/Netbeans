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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.awt.Dialog;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TargetsPanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class InputPanel<T extends WSDLComponent> extends SectionInnerPanel {

    private WSDLModel model;
    private T input;
    private BindingOperation operation;
    private Binding binding;
    private UndoManager undoManager;
    private boolean inSync = false;

    private boolean signed = false;
    private boolean endorsing = false;

    private WSDLComponent tokenElement = null;
    
    public InputPanel(SectionView view, T input, UndoManager undoManager) {
        super(view);
        this.model = input.getModel();
        this.input = input;
        this.operation = (BindingOperation)input.getParent();
        this.binding = (Binding)input.getParent().getParent();
        this.undoManager = undoManager;
        initComponents();
        
        tokenComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        signedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        endorsingChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(tokenCombo);
        addImmediateModifier(signedChBox);
        addImmediateModifier(endorsingChBox);

        inSync = true;
        tokenCombo.removeAllItems();
        tokenCombo.addItem(ComboConstants.NONE);
        tokenCombo.addItem(ComboConstants.USERNAME);
        tokenCombo.addItem(ComboConstants.X509);
        tokenCombo.addItem(ComboConstants.SAML);
        tokenCombo.addItem(ComboConstants.ISSUED);
//        tokenCombo.addItem(ComboConstants.KERBEROS);
        inSync = false;

        sync();

        model.addComponentListener(new ComponentListener() {
            public void valueChanged(ComponentEvent evt) {
                sync();
            }
            public void childrenAdded(ComponentEvent evt) {
                sync();
            }
            public void childrenDeleted(ComponentEvent evt) {
                sync();
            }
        });
        
    }

    private void sync() {
        inSync = true;

        WSDLComponent t = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SUPPORTING);
        if (t != null) {
            tokenElement = t;
            signed = false;
            endorsing = false;
        }
        t = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_SUPPORTING);
        if (t != null) {
            tokenElement = t;
            signed = true;
            endorsing = false;
        }
        t = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.ENDORSING);
        if (t != null) {
            tokenElement = t;
            signed = false;
            endorsing = true;
        }
        t = SecurityTokensModelHelper.getSupportingToken(input, SecurityTokensModelHelper.SIGNED_ENDORSING);
        if (t != null) {
            tokenElement = t;
            signed = true;
            endorsing = true;
        }

        signedChBox.setSelected(signed);
        endorsingChBox.setSelected(endorsing);
        tokenCombo.setSelectedItem(SecurityTokensModelHelper.getTokenType(tokenElement));

        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(tokenCombo)) {
                String token = (String) tokenCombo.getSelectedItem();
                if (token != null) {
                    SecurityTokensModelHelper.removeSupportingTokens(input);
                    if (ComboConstants.USERNAME.equals(token)) {
                        tokenElement = SecurityTokensModelHelper.setSupportingTokens(input, token, getSuppType(signed, false));
                    } else {
                        tokenElement = SecurityTokensModelHelper.setSupportingTokens(input, token, getSuppType(signed, endorsing));
                    }
                }
                enableDisable();
            }
            if (source.equals(signedChBox)) {
                String token = (String) tokenCombo.getSelectedItem();
                signed = signedChBox.isSelected();
                SecurityTokensModelHelper.removeSupportingTokens(input);
                tokenElement = SecurityTokensModelHelper.setSupportingTokens(input, token, getSuppType(signed, endorsing));
                enableDisable();
            }
            if (source.equals(endorsingChBox)) {
                String token = (String) tokenCombo.getSelectedItem();
                endorsing = endorsingChBox.isSelected();
                SecurityTokensModelHelper.removeSupportingTokens(input);
                tokenElement = SecurityTokensModelHelper.setSupportingTokens(input, token, getSuppType(signed, endorsing));
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
        //TODO - enable when generic profile is enabled
        boolean bSecurityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        boolean oSecurityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(operation);
        
        String profile = null;
        if (bSecurityEnabled) {
             profile = ProfilesModelHelper.getSecurityProfile(binding);
        }
        if (oSecurityEnabled) {
            profile = ProfilesModelHelper.getSecurityProfile(operation);
        }

        boolean secConversation = ProfilesModelHelper.isSCEnabled(binding);
        boolean bindingScopeTokenPresent = SecurityTokensModelHelper.getSupportingToken(binding, 
                SecurityTokensModelHelper.SIGNED_SUPPORTING) != null;
        boolean isUsernameToken = ComboConstants.USERNAME.equals(tokenCombo.getSelectedItem());
        boolean securityEnabled = bSecurityEnabled || oSecurityEnabled;
        boolean isSSL = ProfilesModelHelper.isSSLProfile(profile);
                
        tokenCombo.setEnabled(securityEnabled && !secConversation && !bindingScopeTokenPresent);
        tokenComboLabel.setEnabled(securityEnabled && !secConversation && !bindingScopeTokenPresent);
        
        targetOverrideChBox.setEnabled(securityEnabled && !isSSL && !PolicyModelHelper.isSharedPolicy(input));
        targetsButton.setEnabled(securityEnabled && !isSSL);

        boolean tokenSelected = !ComboConstants.NONE.equals((String)tokenCombo.getSelectedItem());
        signedChBox.setEnabled(securityEnabled && tokenSelected && !secConversation && !bindingScopeTokenPresent);
        endorsingChBox.setEnabled(securityEnabled && tokenSelected && 
                !secConversation && !bindingScopeTokenPresent && !isUsernameToken);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tokenComboLabel = new javax.swing.JLabel();
        tokenCombo = new javax.swing.JComboBox();
        targetsButton = new javax.swing.JButton();
        signedChBox = new javax.swing.JCheckBox();
        endorsingChBox = new javax.swing.JCheckBox();
        targetOverrideChBox = new javax.swing.JCheckBox();

        tokenComboLabel.setLabelFor(tokenCombo);
        org.openide.awt.Mnemonics.setLocalizedText(tokenComboLabel, org.openide.util.NbBundle.getMessage(InputPanel.class, "LBL_tokenComboLabel")); // NOI18N

        tokenCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "X509", "Username" }));

        org.openide.awt.Mnemonics.setLocalizedText(targetsButton, org.openide.util.NbBundle.getMessage(InputPanel.class, "LBL_SignEncrypt")); // NOI18N
        targetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(signedChBox, org.openide.util.NbBundle.getMessage(InputPanel.class, "LBL_Token_Signed")); // NOI18N
        signedChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(endorsingChBox, org.openide.util.NbBundle.getMessage(InputPanel.class, "LBL_Token_Endorsing")); // NOI18N
        endorsingChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(targetOverrideChBox, org.openide.util.NbBundle.getMessage(InputPanel.class, "LBL_OverrideTargetDefaults")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(tokenComboLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tokenCombo, 0, 278, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(endorsingChBox)
                            .add(signedChBox))
                        .add(216, 216, 216))
                    .add(layout.createSequentialGroup()
                        .add(targetOverrideChBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(targetsButton)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tokenComboLabel)
                    .add(tokenCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(signedChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(endorsingChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetOverrideChBox)
                    .add(targetsButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void targetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetsButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        TargetsPanel targetsPanel = new TargetsPanel(input); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(targetsPanel, 
                NbBundle.getMessage(InputPanel.class, "LBL_Targets_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        
        dlg.setVisible(true); 
        if (dlgDesc.getValue() == DialogDescriptor.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        } else {
            SecurityPolicyModelHelper.setTargets(input, targetsPanel.getTargetsModel());
        }
        
        model.removeUndoableEditListener(undoCounter);
    }//GEN-LAST:event_targetsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox endorsingChBox;
    private javax.swing.JCheckBox signedChBox;
    private javax.swing.JCheckBox targetOverrideChBox;
    private javax.swing.JButton targetsButton;
    private javax.swing.JComboBox tokenCombo;
    private javax.swing.JLabel tokenComboLabel;
    // End of variables declaration//GEN-END:variables
    
}
