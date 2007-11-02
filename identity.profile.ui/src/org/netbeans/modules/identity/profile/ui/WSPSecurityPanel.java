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

package org.netbeans.modules.identity.profile.ui;

import javax.swing.JComponent;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator;
import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Configurable;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanismHelper;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.netbeans.modules.identity.server.manager.api.ServerManager;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * Visual panel for the WSC security panel.
 *
 * Created on April 14, 2006, 3:03 PM
 *
 * @author  ptliu
 */
public class WSPSecurityPanel extends SectionNodeInnerPanel {
    
    private static final String URN = "urn:";       //NOI18N
    
    private ProviderConfigurator configurator;
    private J2eeProjectHelper helper;
    private boolean disabled = false;
    
    /** Creates new form WSPSecurityPanel */
    public WSPSecurityPanel(SectionNodeView view, J2eeProjectHelper helper) {
        super(view);
        initComponents();
        
        errorLabel.setText("");     //NOI18N
        
        this.helper = helper;
        
        try {
            configurator = ProviderConfigurator.getConfigurator(helper.getServiceDescriptionName(),
                    Type.WSP, AccessMethod.FILE, helper.getConfigPath(), helper.getServerID());
        } catch (ConfiguratorException ex) {
            errorLabel.setText(ex.getMessage());
            disabled = true;
        }
        
        if (!disabled) {
            if (helper.isSecurityEnabled()) {
                enableSecurityCB.setSelected(true);
            } else {
                enableSecurityCB.setSelected(false);
            }
            
            configurator.addModifier(Configurable.SECURITY_MECH, requestSecMechCB,
                    configurator.getSecMechHelper().getAllWSPSecurityMechanisms());
            //(helper.getVersion() == Version.VERSION_1_4) ?
            //    SecurityMechanismHelper.getDefault().getAllWSPSecurityMechanisms() :
            //SecurityMechanismHelper.getDefault().getAllMessageLevelSecurityMechanisms());
            
        }
        
        updateVisualState();
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        
    }
    
    private void updateVisualState() {
        if (disabled) {
            disableAll();
            enableSecurityCB.setEnabled(false);
            return;
        }
        
        if (helper.isWsitSecurityEnabled()) {
            //System.out.println("wsit enabled");
            enableSecurityCB.setEnabled(false);
            errorLabel.setText(NbBundle.getMessage(WSCSecurityPanel.class,
                    "MSG_WsitEnabled"));
        } else {
            //System.out.println("wsit disabled");
            enableSecurityCB.setEnabled(true);
            errorLabel.setText("");         //NOI18N
        }
        
        if (enableSecurityCB.isSelected() &&
                enableSecurityCB.isEnabled()) {
            secMechLabel.setEnabled(true);
            requestLabel.setEnabled(true);
            requestSecMechCB.setEnabled(true);
            userNameInfoLabel.setEnabled(true);
            certSettingsLabel.setEnabled(true);
            certSettingsInfoLabel.setEnabled(true);
            
            certSettingsInfoLabel.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));  //NOI18N
            certSettingsInfoLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.foreground"));  //NOI18N
        } else {
            secMechLabel.setEnabled(false);
            requestLabel.setEnabled(false);
            requestSecMechCB.setEnabled(false);
            userNameInfoLabel.setEnabled(false);
            certSettingsLabel.setEnabled(false);
            certSettingsInfoLabel.setEnabled(false);
            
            certSettingsInfoLabel.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.disabledShadow"));  //NOI18N
            certSettingsInfoLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground")); //NOI18N
        }
        
        SecurityMechanism secMech = (SecurityMechanism) requestSecMechCB.getSelectedItem();
        
        if (secMech.isPasswordCredentialRequired() &&
                requestSecMechCB.isEnabled()) {
            userNameInfoLabel.setVisible(true);
        } else {
            userNameInfoLabel.setVisible(false);
        }
    }
    
    private void disableAll() {
        secMechLabel.setEnabled(false);
        requestLabel.setEnabled(false);
        requestSecMechCB.setEnabled(false);
        userNameInfoLabel.setEnabled(false);
        certSettingsLabel.setEnabled(false);
        certSettingsInfoLabel.setEnabled(false);
        
        certSettingsInfoLabel.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.disabledShadow"));  //NOI18N
        certSettingsInfoLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground")); //NOI18N
  
        userNameInfoLabel.setVisible(false);
    }
    
    public void save() {
        if (!disabled) {
            if (enableSecurityCB.isSelected()) {
                configurator.save();
                
                SecurityMechanism secMech = (SecurityMechanism) configurator.getValue(Configurable.SECURITY_MECH);
                helper.enableWSPSecurity(secMech.getName());
            } else {
                helper.disableWSPSecurity();
                configurator.disable();
                configurator.save();
            }
        }
        
        configurator.close();
        helper.clearTransientState();
    }
    
    public void cancel() {
        configurator.close();
        helper.clearTransientState();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        enableSecurityCB = new javax.swing.JCheckBox();
        secMechLabel = new javax.swing.JLabel();
        requestLabel = new javax.swing.JLabel();
        userNameInfoLabel = new javax.swing.JLabel();
        requestSecMechCB = new javax.swing.JComboBox();
        certSettingsInfoLabel = new javax.swing.JLabel();
        certSettingsLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        errorLabel = new javax.swing.JLabel();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/profile/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(enableSecurityCB, bundle.getString("LBL_EnableSecurity")); // NOI18N
        enableSecurityCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableSecurityCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableSecurityCB.setOpaque(false);
        enableSecurityCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableSecurityCBActionPerformed(evt);
            }
        });

        secMechLabel.setText(bundle.getString("LBL_SecurityMechanisms")); // NOI18N

        requestLabel.setLabelFor(requestSecMechCB);
        org.openide.awt.Mnemonics.setLocalizedText(requestLabel, bundle.getString("LBL_Request")); // NOI18N

        userNameInfoLabel.setText(bundle.getString("MSG_UserNameInfo")); // NOI18N

        requestSecMechCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestSecMechCBActionPerformed(evt);
            }
        });

        certSettingsInfoLabel.setText(bundle.getString("MSG_CertificateSettingsInfo")); // NOI18N
        certSettingsInfoLabel.setFocusable(false);

        certSettingsLabel.setText(bundle.getString("LBL_CertificateSettings")); // NOI18N

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        errorLabel.setText("Error:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(certSettingsInfoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(userNameInfoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(enableSecurityCB)
                            .add(secMechLabel)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(requestLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(requestSecMechCB, 0, 321, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(certSettingsLabel))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(errorLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enableSecurityCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secMechLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(requestLabel)
                    .add(requestSecMechCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(userNameInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(certSettingsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(certSettingsInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_formFocusGained
    
    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        // TODO add your handling code here:
        //requestFocusInWindow();
    }//GEN-LAST:event_formAncestorAdded
    
    private void requestSecMechCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestSecMechCBActionPerformed
        // TODO add your handling code here:
        updateVisualState();
        
        SecurityMechanism secMech = (SecurityMechanism) requestSecMechCB.getSelectedItem();
        
        if (secMech.isLiberty()) {
            configurator.setValue(Configurable.SERVICE_TYPE, URN + helper.getEndpointURI().get(0));
        } else {
            configurator.setValue(Configurable.SERVICE_TYPE, null);
        }
    }//GEN-LAST:event_requestSecMechCBActionPerformed
    
    private void enableSecurityCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableSecurityCBActionPerformed
        // TODO add your handling code here:
        if (enableSecurityCB.isSelected()) {
            configurator.enable();
            helper.setTransientState(true);
        } else {
            configurator.disable();
            helper.setTransientState(false);
        }
        
        updateVisualState();
    }//GEN-LAST:event_enableSecurityCBActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel certSettingsInfoLabel;
    private javax.swing.JLabel certSettingsLabel;
    private javax.swing.JCheckBox enableSecurityCB;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel requestLabel;
    private javax.swing.JComboBox requestSecMechCB;
    private javax.swing.JLabel secMechLabel;
    private javax.swing.JLabel userNameInfoLabel;
    // End of variables declaration//GEN-END:variables
    
}
