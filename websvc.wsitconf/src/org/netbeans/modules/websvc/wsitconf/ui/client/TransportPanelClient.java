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

import javax.swing.JCheckBox;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class TransportPanelClient extends SectionInnerPanel {

    private WSDLModel clientModel;
    private WSDLModel serviceModel;
    
    private Node node;
    private Binding binding;
    private boolean inSync = false;
   
    public TransportPanelClient(SectionView view, Node node, Binding binding, WSDLModel serviceModel) {
        super(view);
        this.clientModel = binding.getModel();
        this.serviceModel = serviceModel;
        this.node = node;
        this.binding = binding;
        
        initComponents();

        optimalEncChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        optimalTransportChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(optimalEncChBox);
        addImmediateModifier(optimalTransportChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        setChBox(optimalEncChBox, TransportModelHelper.isAutoEncodingEnabled(binding));
        setChBox(optimalTransportChBox, TransportModelHelper.isAutoTransportEnabled(binding));
        
        enableDisable();
        
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {
            if (source.equals(optimalEncChBox)) {
                TransportModelHelper.setAutoEncoding(binding, optimalEncChBox.isSelected());
            }

            if (source.equals(optimalTransportChBox)) {
                TransportModelHelper.setAutoTransport(binding, optimalTransportChBox.isSelected());
            }
        }
    }

    private void enableDisable() {
    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
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
        optimalEncChBox = new javax.swing.JCheckBox();
        optimalTransportChBox = new javax.swing.JCheckBox();

        optimalEncChBox.setText(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalEncoding"));
        optimalEncChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optimalEncChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        optimalTransportChBox.setText(org.openide.util.NbBundle.getMessage(TransportPanelClient.class, "LBL_Transport_OptimalTransport"));
        optimalTransportChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optimalTransportChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optimalTransportChBox)
                    .add(optimalEncChBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(optimalEncChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optimalTransportChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox optimalEncChBox;
    private javax.swing.JCheckBox optimalTransportChBox;
    // End of variables declaration//GEN-END:variables
    
}
