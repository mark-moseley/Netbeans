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

/*
 * OperationConfigurationPanel.java
 *
 * Created on August 25, 2006, 1:15 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;


/**
 *
 * @author  radval
 */
public class NotificationOperationPanel extends javax.swing.JPanel implements OperationConfigurationPanel.OperationConfiguration {
    
    private Project mProject = null;
    private Document mCommonOperationTextFieldDocument;
    private Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
    private boolean mIsShowMessageComboBoxes = false;
    private WSDLModel mModel;
    
    /** Creates new form OperationConfigurationPanel 
     * @param project */
    
    
    
    public NotificationOperationPanel(Project project, 
                                      Document operationNameTextFieldDocument, 
                                      Map<String, String> namespaceToPrefixMap,
                                      boolean isShowMessageComboBoxes, WSDLModel model) {
        this.mProject = project;
        this.mCommonOperationTextFieldDocument = operationNameTextFieldDocument;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
        this.mIsShowMessageComboBoxes = isShowMessageComboBoxes;
        mModel = model;
        initComponents();
        initGUI();
    }
    
    /** Mattise require default constructor otherwise will not load in design view of mattise
     **/
    public NotificationOperationPanel() {
        initComponents();
        initGUI();
    }   
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        OperationNameLabel = new javax.swing.JLabel();
        operationNameTextField = new javax.swing.JTextField();
        if(mCommonOperationTextFieldDocument != null) {
            operationNameTextField.setDocument(mCommonOperationTextFieldDocument);
        }
        operationTypeLabel = new javax.swing.JLabel();
        operationTypeComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        outputMessagePartsConfigurationTable = new org.netbeans.modules.xml.wsdl.ui.view.CommonMessageConfigurationPanel(mProject, namespaceToPrefixMap, mModel);
        outputMessageNameConfigurationPanel1 = new MessageNameConfigurationPanel(outputMessagePartsConfigurationTable);

        OperationNameLabel.setLabelFor(operationNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(OperationNameLabel, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.OperationNameLabel.text")); // NOI18N

        operationTypeLabel.setLabelFor(operationTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(operationTypeLabel, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.operationTypeLabel.text")); // NOI18N

        operationTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Request-Response Operation", "One-Way Operation" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputMessagePartsConfigurationTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 354, Short.MAX_VALUE)
            .add(outputMessageNameConfigurationPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(outputMessageNameConfigurationPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessagePartsConfigurationTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(20, 20, 20))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(OperationNameLabel)
                    .add(operationTypeLabel)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, operationTypeComboBox, 0, 354, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, operationNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(OperationNameLabel)
                    .add(operationNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(operationTypeLabel)
                    .add(operationTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(97, 97, 97))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    public void setOutputMessages(String[] existingMessages, String newMessageName, javax.swing.event.DocumentListener msgNameDocumentListener) {
        outputMessageNameConfigurationPanel1.setMessages(existingMessages, newMessageName, msgNameDocumentListener);
    }
    
    public boolean isNewOutputMessage() {
       return outputMessageNameConfigurationPanel1.isNewMessage();
    }
    
    public boolean isNewInputMessage() {
       return false;
    }
    
    
    public boolean isNewFaultMessage() {
       return false;
    }
    
    public String getOutputMessageName() {
        return this.outputMessageNameConfigurationPanel1.getMessageName();
    }
    
    
    public String getInputMessageName() {
        return null;
    }


    public String getFaultMessageName() {
        return null;
    }
    
    public String getOperationName() {
        return this.operationNameTextField.getText();
    }
    
    public void setOperationName(String operationName) {
        this.operationNameTextField.setText(operationName);
    }
    
    public OperationType getOperationType() {
        return (OperationType) this.operationTypeComboBox.getSelectedItem();
    }
    
    public JComboBox getOperationTypeComboBox() {
        return this.operationTypeComboBox;
    }
    
    public List<PartAndElementOrTypeTableModel.PartAndElementOrType> getInputMessageParts() {
        return null;
    }
        
    public List<PartAndElementOrTypeTableModel.PartAndElementOrType> getOutputMessageParts() {
        return outputMessagePartsConfigurationTable.getPartAndElementOrType();
    }

    public List<PartAndElementOrTypeTableModel.PartAndElementOrType> getFaultMessageParts() {
        return null;
    }
        
    private void initGUI() {
        outputMessagePartsConfigurationTable.addNewRow();
        outputMessagePartsConfigurationTable.clearSelection();
        outputMessageNameConfigurationPanel1.setVisible(this.mIsShowMessageComboBoxes);
        if (mIsShowMessageComboBoxes) {
            jLabel1.setLabelFor(outputMessageNameConfigurationPanel1);
        } else {
            jLabel1.setLabelFor(outputMessagePartsConfigurationTable);
        }
    }
    
    public JTextField getOperationNameTextField() {
        return this.operationNameTextField;
    }
        
    
    public static void main(String[] args) {
        
/*        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        OperationConfigurationPanel p = new OperationConfigurationPanel();
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(200, 200);
        frame.setVisible(true);
        */
        
    }
    
    private  OperationType selectedOperationType;
    private JPanel operationCardPanel;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel OperationNameLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField operationNameTextField;
    private javax.swing.JComboBox operationTypeComboBox;
    private javax.swing.JLabel operationTypeLabel;
    private org.netbeans.modules.xml.wsdl.ui.view.MessageNameConfigurationPanel outputMessageNameConfigurationPanel1;
    private org.netbeans.modules.xml.wsdl.ui.view.CommonMessageConfigurationPanel outputMessagePartsConfigurationTable;
    // End of variables declaration//GEN-END:variables
    
}
