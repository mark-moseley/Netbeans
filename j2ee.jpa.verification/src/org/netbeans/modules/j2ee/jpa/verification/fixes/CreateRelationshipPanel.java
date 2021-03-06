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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import java.awt.Color;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Tomasz Slota
 */
class CreateRelationshipPanel extends javax.swing.JPanel {
    public enum NameStatus {VALID, ILLEGAL_JAVA_ID, ILLEGAL_SQL_KEYWORD,  DUPLICATE};
    public enum AvailableSelection {INVERSE_ONLY, OWNING_ONLY, BOTH};
    private Collection<String> availableFields;
    private DefaultComboBoxModel mdlAvailableFields = new DefaultComboBoxModel();
    private FieldNameValidator nameValidator = null;
    private Border brdrBlack = BorderFactory.createLineBorder(Color.BLACK);
    private DialogDescriptor dlgDescriptor = null;
    
    /** Creates new form ProvideIDAnnotationPanel */
    public CreateRelationshipPanel() {
        initComponents();
        
        radioPickUpExistingField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean pickUpExisting = radioPickUpExistingField.isSelected();
                lstExistingFields.setEnabled(pickUpExisting);
                txtNewFieldName.setEnabled(!radioPickUpExistingField.isSelected());
            }
        });
        
        lstExistingFields.setModel(mdlAvailableFields);
        txtNewFieldName.setSelectionEnd(txtNewFieldName.getText().length() - 1);
        
        txtNewFieldName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }
            
            private void update(){
                NameStatus nameStatus = NameStatus.VALID;
                
                if (nameValidator != null){
                    nameStatus = nameValidator.checkName(getNewIdName());
                }
                
                setFieldNameStatus(nameStatus);
            }
        });
        
        setNameValidator(new DefaultFieldNameValidator());
        setFieldNameStatus(NameStatus.VALID);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        radioPickUpExistingField = new javax.swing.JRadioButton();
        lstExistingFields = new javax.swing.JComboBox();
        radioCreateNewField = new javax.swing.JRadioButton();
        lblName = new javax.swing.JLabel();
        txtNewFieldName = new javax.swing.JTextField();
        pnlErrorMsg = new javax.swing.JPanel();
        lblErrorMsg = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        radioOwningSide = new javax.swing.JRadioButton();
        radioInversedSide = new javax.swing.JRadioButton();

        buttonGroup1.add(radioPickUpExistingField);
        radioPickUpExistingField.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/jpa/verification/fixes/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(radioPickUpExistingField, bundle.getString("LBL_PickExistingField")); // NOI18N
        radioPickUpExistingField.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lstExistingFields.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup1.add(radioCreateNewField);
        org.openide.awt.Mnemonics.setLocalizedText(radioCreateNewField, bundle.getString("LBL_CreateNewField")); // NOI18N
        radioCreateNewField.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblName.setLabelFor(txtNewFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, bundle.getString("LBL_FieldName")); // NOI18N

        txtNewFieldName.setText(org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "PickOrCreateFieldPanel.txtNewFieldName.text")); // NOI18N
        txtNewFieldName.setEnabled(false);

        pnlErrorMsg.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlErrorMsg.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblErrorMsg, bundle.getString("MSG_IllegalJavaID")); // NOI18N

        lblError.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(lblError, bundle.getString("LBL_Error")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlErrorMsgLayout = new org.jdesktop.layout.GroupLayout(pnlErrorMsg);
        pnlErrorMsg.setLayout(pnlErrorMsgLayout);
        pnlErrorMsgLayout.setHorizontalGroup(
            pnlErrorMsgLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlErrorMsgLayout.createSequentialGroup()
                .add(lblError)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlErrorMsgLayout.setVerticalGroup(
            pnlErrorMsgLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlErrorMsgLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(lblError)
                .add(lblErrorMsg))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.jLabel1.text")); // NOI18N

        buttonGroup2.add(radioOwningSide);
        radioOwningSide.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioOwningSide, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.radioOwningSide.text")); // NOI18N
        radioOwningSide.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup2.add(radioInversedSide);
        org.openide.awt.Mnemonics.setLocalizedText(radioInversedSide, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.radioInversedSide.text")); // NOI18N
        radioInversedSide.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(radioPickUpExistingField)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lstExistingFields, 0, 398, Short.MAX_VALUE))
                    .add(radioCreateNewField)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(lblName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlErrorMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(txtNewFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)))
                    .add(radioOwningSide)
                    .add(radioInversedSide)
                    .add(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(radioPickUpExistingField)
                    .add(lstExistingFields, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioCreateNewField)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(txtNewFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlErrorMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioOwningSide)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(radioInversedSide)
                .add(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblErrorMsg;
    private javax.swing.JLabel lblName;
    private javax.swing.JComboBox lstExistingFields;
    private javax.swing.JPanel pnlErrorMsg;
    private javax.swing.JRadioButton radioCreateNewField;
    private javax.swing.JRadioButton radioInversedSide;
    private javax.swing.JRadioButton radioOwningSide;
    private javax.swing.JRadioButton radioPickUpExistingField;
    private javax.swing.JTextField txtNewFieldName;
    // End of variables declaration//GEN-END:variables
    
    public void setAvailableFields(Collection<String> availableFields){
        this.availableFields = availableFields;
        mdlAvailableFields.removeAllElements();
        
        if (availableFields.size() == 0){
            setChoosingExistingFieldEnabled(false);
        } else{
            setChoosingExistingFieldEnabled(true);
            
            for (String fieldName : availableFields) {
                mdlAvailableFields.addElement(fieldName);
            }
        }
    }
    
    public void setChoosingExistingFieldEnabled(boolean enabled){
        if (!enabled){
            radioCreateNewField.setSelected(true);
        }
        
        radioPickUpExistingField.setEnabled(enabled);
    }
    
    public boolean wasCreateNewFieldSelected(){
        return radioCreateNewField.isSelected();
    }
    
    public String getNewIdName(){
        return txtNewFieldName.getText();
    }
    
    public Object getSelectedField(){
        return lstExistingFields.getSelectedItem();
    }
    
    public void setNameValidator(FieldNameValidator nameValidator){
        this.nameValidator = nameValidator;
    }
    
    public void setExistingFieldNames(Collection<String> existingFieldNames){
        nameValidator.setExistingFieldNames(existingFieldNames);
    }
    
    void setDefaultFieldName(String defaultFieldName) {
        txtNewFieldName.setText(defaultFieldName);
    }
    
    private void setErrorPanelVisible(boolean visible){
        lblErrorMsg.setVisible(visible);
        lblError.setVisible(visible);
        pnlErrorMsg.setBorder(visible ? brdrBlack : null);
    }
    
    public void setFieldNameStatus(NameStatus nameStatus){
        boolean validName = nameStatus == nameStatus.VALID;
        setErrorPanelVisible(!validName);
        
        if (dlgDescriptor != null){
            dlgDescriptor.setValid(validName);
        }
        
        String errorMsgBundleId;
        String fieldName = txtNewFieldName.getText();
        
        switch (nameStatus){
            case ILLEGAL_JAVA_ID:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_IllegalJavaID", fieldName);
                break;
            case ILLEGAL_SQL_KEYWORD:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_IllegalSQLKeyWord", fieldName);
                break;
            case DUPLICATE:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_DuplicateVariableName", fieldName);
                break;
            default:
                errorMsgBundleId = null;
        }
        
        lblErrorMsg.setText(errorMsgBundleId);
    }
    
    public void setEntityClassNames(String entity1Name, String entity2Name){
        radioOwningSide.setText(NbBundle.getMessage(CreateRelationshipPanel.class,
                "CreateRelationshipPanel.radioOwningSide.text", entity1Name));
        
        radioInversedSide.setText(NbBundle.getMessage(CreateRelationshipPanel.class,
                "CreateRelationshipPanel.radioInversedSide.text", entity2Name));
    }
    
    public void setAvailableSelection(AvailableSelection sel){
        switch (sel){
            case INVERSE_ONLY:
                radioInversedSide.setSelected(true);
                radioOwningSide.setEnabled(false);
                break;
            case OWNING_ONLY:
                radioInversedSide.setEnabled(false);
                break;
        }
    }
    
    public boolean owningSide(){
        return radioOwningSide.isSelected();
    }
    
    public static interface FieldNameValidator{
        public NameStatus checkName(String name);
        public void setExistingFieldNames(Collection<String> existingFieldNames);
    }
    
    public static class DefaultFieldNameValidator implements FieldNameValidator{
        private Collection<String> existingFieldNames;
        
        public void setExistingFieldNames(Collection<String> existingFieldNames){
            this.existingFieldNames = existingFieldNames;
        }
        
        public NameStatus checkName(String name){
            if (!Utilities.isJavaIdentifier(name)){
                return NameStatus.ILLEGAL_JAVA_ID;
            }
            
            if (JavaPersistenceQLKeywords.isKeyword(name)){
                return NameStatus.ILLEGAL_SQL_KEYWORD;
            }
            
            if (existingFieldNames != null && existingFieldNames.contains(name)){
                return NameStatus.DUPLICATE;
            }
            
            return NameStatus.VALID;
        }
    }

    public void setDlgDescriptor(DialogDescriptor dlgDescriptor) {
        this.dlgDescriptor = dlgDescriptor;
    }
}
