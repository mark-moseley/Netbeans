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

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class STSConfigServicePanel extends JPanel {
    
    private ServiceProvidersTablePanel panel;

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private SectionView view;

    private static final String DEFAULT_LIFETIME = "300000";                     //NOI18N
    private static final String DEFAULT_CONTRACT_CLASS = "com.sun.xml.ws.trust.impl.IssueSamlTokenContractImpl"; //NOI18N

    private Project project;

    private boolean inSync = false;
    
    /**
     * Creates new form STSConfigServicePanel
     */
    public STSConfigServicePanel(WSDLModel model, Node node, Binding binding) {
        this.model = model;
        this.node = node;
        this.binding = binding;

        FileObject fo = (FileObject) node.getLookup().lookup(FileObject.class);
        if (fo != null) project = FileOwnerQuery.getOwner(fo);
        
        initComponents();
        
        contractButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        contractLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        contractTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        lifeTimeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        lifeTimeTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceProvidersPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        inSync = true;
        ServiceProvidersTablePanel.ServiceProvidersTableModel tablemodel = new ServiceProvidersTablePanel.ServiceProvidersTableModel();
        this.remove(serviceProvidersPanel);
        
        STSConfiguration stsConfig = ProprietarySecurityPolicyModelHelper.getSTSConfiguration(binding);
        if (stsConfig == null) {
            stsConfig = ProprietarySecurityPolicyModelHelper.createSTSConfiguration(binding);
        }
        serviceProvidersPanel = new ServiceProvidersTablePanel(tablemodel, model, node, stsConfig);
        ((ServiceProvidersTablePanel)serviceProvidersPanel).populateModel();
        inSync = false;

        sync();
        
    }

    private void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getSTSLifeTime(binding);
        if (lifeTime == null) { // no setup exists yet - set the default
            setLifeTime(DEFAULT_LIFETIME);
            ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, DEFAULT_LIFETIME);
        } else {
            setLifeTime(lifeTime);
        } 

        String issuer = ProprietarySecurityPolicyModelHelper.getSTSIssuer(binding);
        if (issuer != null) { // no setup exists yet - set the default
            setIssuer(issuer);
        } 
        
        String cclass = ProprietarySecurityPolicyModelHelper.getSTSContractClass(binding);
        if (cclass == null) { // no setup exists yet - set the default
            setContractClass(DEFAULT_CONTRACT_CLASS);
            ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, DEFAULT_CONTRACT_CLASS);
        } else {
            setContractClass(cclass);
        } 
        
        refreshPanels();
        
        inSync = false;
    }
    
    private String getLifeTime() {
        return this.lifeTimeTextField.getText();
    }

    private void setLifeTime(String time) {
        this.lifeTimeTextField.setText(time);
    }

    private String getIssuer() {
        return this.issuerField.getText();
    }

    private void setIssuer(String issuer) {
        this.issuerField.setText(issuer);
    }
    
    private void setContractClass(String classname) {
        this.contractTextField.setText(classname);
    }

    private String getContractClass() {
        return contractTextField.getText();
    }
   
    private void refreshPanels() {
        updateLayout();
    }
    
    private void updateLayout() {
        GroupLayout layout = (GroupLayout)this.getLayout();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(lifeTimeLabel))
                            .add(contractLabel)
                            .add(issuerLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractButton))
                            .add(issuerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))))
                .add(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerLabel)
                    .add(issuerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contractLabel)
                    .add(contractButton)
                    .add(contractTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        serviceProvidersPanel = new javax.swing.JPanel();
        lifeTimeLabel = new javax.swing.JLabel();
        lifeTimeTextField = new javax.swing.JTextField();
        contractLabel = new javax.swing.JLabel();
        contractTextField = new javax.swing.JTextField();
        contractButton = new javax.swing.JButton();
        issuerLabel = new javax.swing.JLabel();
        issuerField = new javax.swing.JTextField();

        org.jdesktop.layout.GroupLayout serviceProvidersPanelLayout = new org.jdesktop.layout.GroupLayout(serviceProvidersPanel);
        serviceProvidersPanel.setLayout(serviceProvidersPanelLayout);
        serviceProvidersPanelLayout.setHorizontalGroup(
            serviceProvidersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 594, Short.MAX_VALUE)
        );
        serviceProvidersPanelLayout.setVerticalGroup(
            serviceProvidersPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 132, Short.MAX_VALUE)
        );

        lifeTimeLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Lifetime")); // NOI18N

        lifeTimeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lifeTimeTextFieldKeyReleased(evt);
            }
        });

        contractLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Contract")); // NOI18N

        contractButton.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Browse")); // NOI18N
        contractButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractButtonActionPerformed(evt);
            }
        });

        issuerLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Issuer")); // NOI18N

        issuerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerFieldKeyReleased(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(lifeTimeLabel))
                            .add(contractLabel)
                            .add(issuerLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contractButton))
                            .add(issuerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))))
                .add(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issuerLabel)
                    .add(issuerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contractLabel)
                    .add(contractButton)
                    .add(contractTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lifeTimeLabel)
                    .add(lifeTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceProvidersPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void issuerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerFieldKeyReleased
        String issuer = issuerField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSIssuer(binding, issuer);
    }//GEN-LAST:event_issuerFieldKeyReleased

    private void lifeTimeTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lifeTimeTextFieldKeyReleased
        String ltime = lifeTimeTextField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, ltime);
    }//GEN-LAST:event_lifeTimeTextFieldKeyReleased

    private void contractButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "com.sun.xml.ws.trust.WSTrustContract"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setContractClass(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_contractButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton contractButton;
    private javax.swing.JLabel contractLabel;
    private javax.swing.JTextField contractTextField;
    private javax.swing.JTextField issuerField;
    private javax.swing.JLabel issuerLabel;
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JTextField lifeTimeTextField;
    private javax.swing.JPanel serviceProvidersPanel;
    // End of variables declaration//GEN-END:variables
    
}
