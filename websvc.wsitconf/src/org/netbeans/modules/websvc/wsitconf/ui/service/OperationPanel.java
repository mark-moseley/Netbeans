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

import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TxModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Node;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author Martin Grebac
 */
public class OperationPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private BindingOperation operation;
    private UndoManager undoManager;
    private boolean inSync = false;
    private Project project;
    private JaxWsModel jaxwsmodel;
    
    public OperationPanel(SectionView view, Node node, Project p, BindingOperation operation, UndoManager undoManager, JaxWsModel jaxwsmodel) {
        super(view);
        this.model = operation.getModel();
        this.node = node;
        this.binding = (Binding)operation.getParent();
        this.undoManager = undoManager;
        this.project = p;
        this.operation = operation;
        this.jaxwsmodel = jaxwsmodel;
        initComponents();
        
        txCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        txLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(txCombo);

        inSync = true;
        txCombo.removeAllItems();
//        txCombo.addItem(ComboConstants.TX_NEVER);
        txCombo.addItem(ComboConstants.TX_NOTSUPPORTED);
        txCombo.addItem(ComboConstants.TX_MANDATORY);
        txCombo.addItem(ComboConstants.TX_REQUIRED);
        txCombo.addItem(ComboConstants.TX_REQUIRESNEW);
        txCombo.addItem(ComboConstants.TX_SUPPORTED);

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
        
        String txValue = TxModelHelper.getTx(operation, node);
        txCombo.setSelectedItem(txValue);
                
        enableDisable();
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        if (source.equals(txCombo)) {
            String selected = (String) txCombo.getSelectedItem();
            if ((selected != null) && (!selected.equals(TxModelHelper.getTx(operation, node)))) {
                TxModelHelper.setTx(operation, node, selected);
            }
        }

        enableDisable();
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
    
        boolean isTomcat = Util.isTomcat(project);
        boolean isWebProject = Util.isWebProject(project);
        
        boolean txConfigEnabled = !isTomcat && isWebProject;
        txCombo.setEnabled(txConfigEnabled);
        txLbl.setEnabled(txConfigEnabled);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        txLbl = new javax.swing.JLabel();
        txCombo = new javax.swing.JComboBox();

        txLbl.setLabelFor(txCombo);
        org.openide.awt.Mnemonics.setLocalizedText(txLbl, org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_Section_Operation_Tx")); // NOI18N

        txCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Not Supported", "Required", "Requires New", "Mandatory", "Supported" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(txLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txLbl)
                    .add(txCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox txCombo;
    private javax.swing.JLabel txLbl;
    // End of variables declaration//GEN-END:variables
    
}
