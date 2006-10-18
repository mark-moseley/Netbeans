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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class STSClientPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private boolean inSync = false;

    public STSClientPanel(SectionView view, Node node, Binding binding) {
        super(view);
        this.model = binding.getModel();
        this.node = node;
        this.binding = binding;
        
        initComponents();

        endpointLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        endpointTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        endpointLabel.setText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint"));  //NOI18N
        namespaceLabel.setText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace"));//NOI18N
        portNameLabel.setText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName"));  //NOI18N
        serviceNameLabel.setText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName"));    //NOI18N
        wsdlLocationLabel.setText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation"));  //NOI18N

        addImmediateModifier(endpointTextField);
        addImmediateModifier(namespaceTextField);
        addImmediateModifier(portNameTextField);
        addImmediateModifier(serviceNameTextField);
        addImmediateModifier(wsdlLocationTextField);
        addImmediateModifier(metadataField);

        sync();
    }

    public void sync() {
        inSync = true;

        String endpoint = ProprietarySecurityPolicyModelHelper.getPreSTSEndpoint(binding);
        if (endpoint != null) {
            setEndpoint(endpoint);
        }

        String metadata = ProprietarySecurityPolicyModelHelper.getPreSTSMetadata(binding);
        if (metadata != null) {
            setMetadata(metadata);
        }
        
        String namespace = ProprietarySecurityPolicyModelHelper.getPreSTSNamespace(binding);
        if (namespace != null) {
            setNamespace(namespace);
        } 

        String portName = ProprietarySecurityPolicyModelHelper.getPreSTSPortName(binding);
        if (portName != null) {
            setPortName(portName);
        } 

        String serviceName = ProprietarySecurityPolicyModelHelper.getPreSTSServiceName(binding);
        if (serviceName != null) {
            setServiceName(serviceName);
        } 

        String wsdlLocation = ProprietarySecurityPolicyModelHelper.getPreSTSWsdlLocation(binding);
        if (wsdlLocation != null) {
            setWsdlLocation(wsdlLocation);
        } 
        
        inSync = false;
    }

    private String getEndpoint() {
        return this.endpointTextField.getText();
    }

    private void setEndpoint(String url) {
        this.endpointTextField.setText(url);
    }

    private String getMetadata() {
        return this.metadataField.getText();
    }

    private void setMetadata(String url) {
        this.metadataField.setText(url);
    }
    
    private String getNamespace() {
        return this.namespaceTextField.getText();
    }

    private void setNamespace(String ns) {
        this.namespaceTextField.setText(ns);
    }
    
    private String getServiceName() {
        return this.serviceNameTextField.getText();
    }

    private void setServiceName(String sname) {
        this.serviceNameTextField.setText(sname);
    }
    
    private String getPortName() {
        return this.portNameTextField.getText();
    }

    private void setPortName(String pname) {
        this.portNameTextField.setText(pname);
    }

    private String getWsdlLocation() {
        return this.wsdlLocationTextField.getText();
    }

    private void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocationTextField.setText(wsdlLocation);
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(endpointTextField)) {
                String endpoint = getEndpoint();
                if ((endpoint != null) && (endpoint.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, endpoint);
                }
                return;
            }

            if (source.equals(metadataField)) {
                String metad = getMetadata();
                if ((metad != null) && (metad.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, metad);
                }
                return;
            }

            if (source.equals(namespaceTextField)) {
                String ns = getNamespace();
                if ((ns != null) && (ns.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, ns);
                }
                return;
            }

            if (source.equals(serviceNameTextField)) {
                String sname = getServiceName();
                if ((sname != null) && (sname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, sname);
                }
                return;
            }

            if (source.equals(portNameTextField)) {
                String pname = getPortName();
                if ((pname != null) && (pname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, pname);
                }
                return;
            }

            if (source.equals(wsdlLocationTextField)) {
                String wsdlLoc = getWsdlLocation();
                if ((wsdlLoc != null) && (wsdlLoc.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, wsdlLoc);
                }
                return;
            }
        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        endpointLabel = new javax.swing.JLabel();
        wsdlLocationLabel = new javax.swing.JLabel();
        endpointTextField = new javax.swing.JTextField();
        wsdlLocationTextField = new javax.swing.JTextField();
        serviceNameLabel = new javax.swing.JLabel();
        serviceNameTextField = new javax.swing.JTextField();
        portNameLabel = new javax.swing.JLabel();
        namespaceLabel = new javax.swing.JLabel();
        portNameTextField = new javax.swing.JTextField();
        namespaceTextField = new javax.swing.JTextField();
        metadataLabel = new javax.swing.JLabel();
        metadataField = new javax.swing.JTextField();

        endpointLabel.setText("Endpoint:");
        endpointLabel.setToolTipText("The maximum number of seconds the time stamp remains valid.");

        wsdlLocationLabel.setText("WSDL Location:");
        wsdlLocationLabel.setToolTipText("The maximum number of seconds the sending clock can deviate from the receiving clock.");

        endpointTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        wsdlLocationTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        serviceNameLabel.setText("Service Name:");

        serviceNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        portNameLabel.setText("Port Name:");

        namespaceLabel.setText("Namespace:");

        portNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        namespaceTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        metadataLabel.setText(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(namespaceLabel)
                    .add(endpointLabel)
                    .add(wsdlLocationLabel)
                    .add(metadataLabel)
                    .add(serviceNameLabel)
                    .add(portNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(portNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(wsdlLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(endpointTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(metadataField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 383, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {endpointTextField, metadataField, namespaceTextField, portNameTextField, serviceNameTextField, wsdlLocationTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(endpointLabel)
                    .add(endpointTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsdlLocationLabel)
                    .add(wsdlLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(metadataLabel)
                    .add(metadataField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceNameLabel)
                    .add(serviceNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portNameLabel)
                    .add(portNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(namespaceLabel)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel endpointLabel;
    private javax.swing.JTextField endpointTextField;
    private javax.swing.JTextField metadataField;
    private javax.swing.JLabel metadataLabel;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JTextField namespaceTextField;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JTextField portNameTextField;
    private javax.swing.JLabel serviceNameLabel;
    private javax.swing.JTextField serviceNameTextField;
    private javax.swing.JLabel wsdlLocationLabel;
    private javax.swing.JTextField wsdlLocationTextField;
    // End of variables declaration//GEN-END:variables
    
}
