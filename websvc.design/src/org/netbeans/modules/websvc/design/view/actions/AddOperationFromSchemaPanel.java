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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkuchtiak
 */
public class AddOperationFromSchemaPanel extends javax.swing.JPanel {
    private File wsdlFile;
    private List<URL> schemaFiles;
    
    /** Creates new form NewJPanel */
    public AddOperationFromSchemaPanel(File wsdlFile) {
        this.wsdlFile=wsdlFile;
        System.out.println("initComponents");
        initComponents();
        opNameTxt.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultOperationName"));        
        wsdlTextField.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultSchmas", wsdlFile.getName()));
        browseButton.setEnabled(false);
        try{
            populate();
        }catch(CatalogModelException e){
            ErrorManager.getDefault().notify(e);
        }

        SchemaPanelListCellRenderer renderer = new SchemaPanelListCellRenderer();
        returnCombo.setRenderer(renderer);
    }   
        /** Creates new form NewJPanel */
        public AddOperationFromSchemaPanel() {
            initComponents();
            opNameTxt.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultOperationName"));
        }
        
        public File getWsdlFile() {
            return wsdlFile;
        }
        
        public String getOperationName(){
            return opNameTxt.getText();
        }
        
        public List<URL> getSchemaFiles() {
            return schemaFiles;
        }
        
        public List<ParamModel> getParameterTypes() {
            return parametersPanel.getParameters();
        }
        
        public ReferenceableSchemaComponent getReturnType() {
            Object returnType = returnCombo.getSelectedItem();
            if (returnType instanceof ReferenceableSchemaComponent) {
                return (ReferenceableSchemaComponent) returnType;
            } else {
                return null;
            }
        }
        
        public List<ReferenceableSchemaComponent> getFaultTypes() {
            return null;
        }
        
        private void populate()throws CatalogModelException {
            WSDLModel model = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
            populateWithTypes(model);
        }
        
        
        class SchemaPanelListCellRenderer extends JLabel implements ListCellRenderer{
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                    if (value instanceof ReferenceableSchemaComponent) {
                        setText(Utils.getDisplayName((ReferenceableSchemaComponent)value));
                    } else if (value instanceof String) {
                        setText((String)value);
                    }
                return this;
            }
        }
        
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        wsdlLabel = new javax.swing.JLabel();
        returnLabel = new javax.swing.JLabel();
        opNameTxt = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        returnCombo = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        parametersPanel = new org.netbeans.modules.websvc.design.view.actions.ParametersPanel(getWsdlModel());
        parametersPanel1 = new org.netbeans.modules.websvc.design.view.actions.ParametersPanel();
        jPanel1 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        wsdlTextField = new javax.swing.JTextField();

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_OperationName_mnem").charAt(0));
        nameLabel.setLabelFor(opNameTxt);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_OperationName")); // NOI18N

        wsdlLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_SchemaFiles_mnem").charAt(0));
        wsdlLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_SchemaTypes")); // NOI18N

        returnLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_ReturnType_mnem").charAt(0));
        returnLabel.setLabelFor(returnCombo);
        returnLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_ReturnType")); // NOI18N

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_AddSchema_mnem").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_AddSchema")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_BindingStyle")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.parametersPanel.TabConstraints.tabTitle"), parametersPanel); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.parametersPanel1.TabConstraints.tabTitle"), parametersPanel1); // NOI18N

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("RB_DOCUMENT_LITERAL_mnem").charAt(0));
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "RB_DOCUMENT_LITERAL")); // NOI18N
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(jRadioButton1);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("RB_RPC_LITERAL_mnem").charAt(0));
        jRadioButton2.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "RB_RPC_LITERAL")); // NOI18N
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(jRadioButton2);

        wsdlTextField.setEditable(false);
        wsdlTextField.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.wsdlTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 842, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(wsdlLabel)
                            .add(jLabel6)
                            .add(returnLabel))
                        .add(14, 14, 14)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(returnCombo, 0, 724, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 314, Short.MAX_VALUE)
                                .add(browseButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, opNameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
                            .add(wsdlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(opNameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsdlLabel)
                    .add(wsdlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(browseButton)
                        .add(25, 25, 25))
                    .add(layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(10, 10, 10))
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(1, 1, 1)))
                .add(13, 13, 13)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnLabel)
                    .add(returnCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    private void populateWithTypes(WSDLModel wsdlModel) {
        returnCombo.addItem("void"); //NOI18N
        try {
            List<ReferenceableSchemaComponent> schemaTypes = Utils.getSchemaTypes(wsdlModel);
            for (ReferenceableSchemaComponent schemaType:schemaTypes) {
                returnCombo.addItem(schemaType);
            }
        } catch (CatalogModelException ex) {
            
        }

    }
    
    private WSDLModel getWsdlModel() {
        return Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField opNameTxt;
    private org.netbeans.modules.websvc.design.view.actions.ParametersPanel parametersPanel;
    private org.netbeans.modules.websvc.design.view.actions.ParametersPanel parametersPanel1;
    private javax.swing.JComboBox returnCombo;
    private javax.swing.JLabel returnLabel;
    private javax.swing.JLabel wsdlLabel;
    private javax.swing.JTextField wsdlTextField;
    // End of variables declaration//GEN-END:variables
    
    }
