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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
public class NotificationOperationPanel extends javax.swing.JPanel implements OperationConfiguration {
    
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OperationNameLabel = new javax.swing.JLabel();
        operationNameTextField = new javax.swing.JTextField();
        if(mCommonOperationTextFieldDocument != null) {
            operationNameTextField.setDocument(mCommonOperationTextFieldDocument);
        }
        operationTypeLabel = new javax.swing.JLabel();
        operationTypeComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        outputMessagePanel = new javax.swing.JPanel();
        outputMessagePartsConfigurationTable = new org.netbeans.modules.xml.wsdl.ui.view.CommonMessageConfigurationPanel(mProject, namespaceToPrefixMap, mModel);
        outputMessageNameConfigurationPanel1 = new MessageNameConfigurationPanel(outputMessagePartsConfigurationTable);
        generatePartnerLinkTypeCheckbox = new javax.swing.JCheckBox();

        setName("Form"); // NOI18N

        OperationNameLabel.setLabelFor(operationNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(OperationNameLabel, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.OperationNameLabel.text")); // NOI18N
        OperationNameLabel.setName("OperationNameLabel"); // NOI18N

        operationNameTextField.setName("operationNameTextField"); // NOI18N

        operationTypeLabel.setLabelFor(operationTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(operationTypeLabel, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.operationTypeLabel.text")); // NOI18N
        operationTypeLabel.setName("operationTypeLabel"); // NOI18N

        operationTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Request-Response Operation", "One-Way Operation" }));
        operationTypeComboBox.setName("operationTypeComboBox"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "NotificationOperationPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        outputMessagePanel.setName("outputMessagePanel"); // NOI18N

        outputMessagePartsConfigurationTable.setName("outputMessagePartsConfigurationTable"); // NOI18N

        outputMessageNameConfigurationPanel1.setName("outputMessageNameConfigurationPanel1"); // NOI18N

        org.jdesktop.layout.GroupLayout outputMessagePanelLayout = new org.jdesktop.layout.GroupLayout(outputMessagePanel);
        outputMessagePanel.setLayout(outputMessagePanelLayout);
        outputMessagePanelLayout.setHorizontalGroup(
            outputMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputMessageNameConfigurationPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
            .add(outputMessagePartsConfigurationTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
        );
        outputMessagePanelLayout.setVerticalGroup(
            outputMessagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputMessagePanelLayout.createSequentialGroup()
                .add(outputMessageNameConfigurationPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputMessagePartsConfigurationTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(generatePartnerLinkTypeCheckbox, org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "LBL_autoGeneratePartnerLinktypeCheckBox.text")); // NOI18N
        generatePartnerLinkTypeCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(NotificationOperationPanel.class, "TT_autoGeneratePartnerLinktypeCheckBox.toolTipText")); // NOI18N
        generatePartnerLinkTypeCheckbox.setName("generatePartnerLinkTypeCheckbox"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(generatePartnerLinkTypeCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(OperationNameLabel)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, operationTypeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(outputMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, operationTypeComboBox, 0, 354, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, operationNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))))
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
                    .add(outputMessagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(generatePartnerLinkTypeCheckbox)
                .add(87, 87, 87))
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
    private javax.swing.JCheckBox generatePartnerLinkTypeCheckbox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField operationNameTextField;
    private javax.swing.JComboBox operationTypeComboBox;
    private javax.swing.JLabel operationTypeLabel;
    private org.netbeans.modules.xml.wsdl.ui.view.MessageNameConfigurationPanel outputMessageNameConfigurationPanel1;
    private javax.swing.JPanel outputMessagePanel;
    private org.netbeans.modules.xml.wsdl.ui.view.CommonMessageConfigurationPanel outputMessagePartsConfigurationTable;
    // End of variables declaration//GEN-END:variables

    public String getPortTypeName() {
        // TODO Auto-generated method stub
        return null;
    }

    public JTextField getPortTypeNameTextField() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPortTypeName(String portTypeName) {
        // TODO Auto-generated method stub
        
    }

    public boolean isAutoGeneratePartnerLinkType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAutoGeneratePartnerLinkType(boolean autoGenPartnerLinkType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
