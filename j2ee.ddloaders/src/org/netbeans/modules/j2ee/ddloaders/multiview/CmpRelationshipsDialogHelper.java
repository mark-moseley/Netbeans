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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.CmpRelationshipsForm;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author pfiala
 */
public class CmpRelationshipsDialogHelper {

    private static final String CLASS_COLLECTION = "java.util.Collection";  //NOI18N
    private static final String CLASS_SET = "java.util.Set";                //NOI18N
    private static final String[] FILED_TYPE_ITEMS = new String[]{CLASS_COLLECTION, CLASS_SET};

    private final EjbJarMultiViewDataObject dataObject;
    private final EjbJar ejbJar;

    private JTextField relationshipNameTextField;
    private JTextArea descriptionTextArea;
    private Vector entityNames;
    private RelationshipDialogActionListener listener;

    private class FormRoleHelper {

        private JTextField roleNameTextField;
        private JComboBox ejbComboBox;
        private JRadioButton multiplicityManyRadioButton;
        private JRadioButton multiplicityOneRadioButton;
        private JCheckBox cascadeDeleteCheckBox;
        private JCheckBox createCmrFieldCheckBox;
        private JTextField fieldNameTextField;
        private JComboBox fieldTypeComboBox;

        private String origEjbName;
        private String origFieldName;
        private String origFieldType;
        private EntityHelper origEntityHelper;
        protected boolean origGetter;
        protected boolean origSetter;

        private String opositeEjbName;

        private String lastFieldName;
        private String lastFieldType = CLASS_COLLECTION;
        private boolean createCmrFieldChanged = true;

        private void init() {
            ejbComboBox.setModel(new DefaultComboBoxModel(entityNames));
            fieldTypeComboBox.setModel(new DefaultComboBoxModel(FILED_TYPE_ITEMS));
            multiplicityOneRadioButton.addActionListener(listener);
            multiplicityManyRadioButton.addActionListener(listener);
            createCmrFieldCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createCmrFieldChanged = true;
                    listener.validate();
                }
            });
        }

        private void processResult(RelationshipHelper.RelationshipRoleHelper helper) {
            String ejbName = getEjbName();
            String roleName = getRoleName();
            helper.setEjbName(ejbName);
            helper.setRoleName(roleName);
            helper.setMultiple(isMultiple());
            helper.setCascadeDelete(isCascadeDelete());
            String fieldName;
            String fieldType;
            if (isCreateCmrField()) {
                fieldName = getFieldName();
                fieldType = getFieldType();
                helper.setCmrField(fieldName, fieldType);
            } else {
                fieldName = null;
                fieldType = null;
                helper.setCmrField(null);
            }
        }

        private boolean equal(String s1, String s2) {
            return s1 == null ? s2 == null : s1.equals(s2);
        }

        private boolean isCascadeDelete() {
            return cascadeDeleteCheckBox.isSelected();
        }

        private void setCascadeDelete(boolean cascadeDelete) {
            cascadeDeleteCheckBox.setSelected(cascadeDelete);
        }

        private boolean isMultiple() {
            return multiplicityManyRadioButton.isSelected();
        }

        private void setMultiple(boolean multiple) {
            if (multiple) {
                multiplicityManyRadioButton.setSelected(true);
            } else {
                multiplicityOneRadioButton.setSelected(true);
            }
        }

        private boolean isCreateCmrField() {
            return createCmrFieldCheckBox.isSelected();
        }

        private String getFieldType() {
            return (String) fieldTypeComboBox.getSelectedItem();
        }

        private void setFieldType(String fieldType) {
            if (fieldType != null) {
                lastFieldType = fieldType;
            }
            fieldTypeComboBox.setSelectedItem(fieldType);
        }

        private String getRoleName() {
            String roleName = roleNameTextField.getText().trim();
            return roleName.length() == 0 ? null : roleName;
        }

        private void setRoleName(String roleName) {
            roleNameTextField.setText(roleName);
        }

        private String getEjbName() {
            return (String) ejbComboBox.getSelectedItem();
        }

        private void setEjbName(String ejbName) {
            ejbComboBox.setSelectedItem(ejbName);
        }

        private void populateFormFields(RelationshipHelper.RelationshipRoleHelper helper) {
            setRoleName(helper.getRoleName());
            origEjbName = helper.getEjbName();
            Entity entity = getEntity(origEjbName);
            origEntityHelper = entity == null ? null : dataObject.getEntityHelper(entity);
            setEjbName(origEjbName);
            setMultiple(helper.isMultiple());
            setCascadeDelete(helper.isCascadeDelete());

            CmrField field = helper.getCmrField();
            if (field == null) {
                origFieldName = null;
                origFieldType = null;
                setCreateCmrField(false);
                setFieldName(null);
                setFieldType(null);
            } else {
                origFieldName = field.getCmrFieldName();
                origFieldType = field.getCmrFieldType();
                if (origEntityHelper != null) {
                }
                setCreateCmrField(true);
                setFieldName(origFieldName);
                setFieldType(origFieldType);
            }
        }


        private void setCreateCmrField(boolean selected) {
            createCmrFieldCheckBox.setSelected(selected);
        }

        public String getFieldName() {
            return fieldNameTextField.getText().trim();
        }

        private void setFieldName(String fieldName) {
            if (fieldName != null && fieldName.length() > 0) {
                lastFieldName = fieldName;
            }
            fieldNameTextField.setText(fieldName);
        }

        public void setFieldStates(FormRoleHelper opositeRole) {
            boolean createCmrField = isCreateCmrField();
            String fieldName = getFieldName();
            if (createCmrFieldChanged) {
                createCmrFieldChanged = false;
                if (createCmrField) {
                    if (fieldName.length() == 0) {
                        setFieldName(lastFieldName);
                    }
                    fieldNameTextField.setEnabled(true);
                } else {
                    if (fieldName.length() > 0) {
                        lastFieldName = fieldName;
                    }
                    setFieldName(null);
                    fieldNameTextField.setEnabled(false);
                }
            }
            boolean opositeMultiple = opositeRole.isMultiple();
            String fieldType = getFieldType();
            if (createCmrField && opositeMultiple) {
                if (fieldType == null) {
                    setFieldType(lastFieldType);
                }
                fieldTypeComboBox.setEnabled(true);
            } else {
                if (fieldType != null) {
                    lastFieldType = fieldType;
                }
                setFieldType(null);
                fieldTypeComboBox.setEnabled(false);
            }
            opositeEjbName = opositeRole.getEjbName();
        }

        private String validateFieldName() {
            if(isCreateCmrField()) {
                String fieldName = getFieldName();
                if (!Utils.isJavaIdentifier(fieldName)) {
                    return Utils.getBundleMessage("MSG_InvalidFieldName");
                }
            }
            return null;
        }
    }

    FormRoleHelper roleA = new FormRoleHelper();
    FormRoleHelper roleB = new FormRoleHelper();

    public CmpRelationshipsDialogHelper(EjbJarMultiViewDataObject dataObject, EjbJar ejbJar) {
        this.dataObject = dataObject;
        this.ejbJar = ejbJar;
    }

    public boolean showCmpRelationshipsDialog(String title, EjbRelation relation) {
        final CmpRelationshipsForm form = initForm();
        final JLabel errorLabel = form.getErrorLabel();

        RelationshipHelper helper;
        if (relation != null) {
            helper = new RelationshipHelper(relation);
            populateFormFields(helper);
        } else {
            helper = null;
        }

        listener.validate();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(form, title);
        dialogDescriptor.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setFocusTraversalPolicy(form.createFocusTraversalPolicy());
        DialogListener dialogListener = new DialogListener(errorLabel, dialogDescriptor);
        form.getFieldNameTextField().getDocument().addDocumentListener(dialogListener);
        form.getFieldNameTextField2().getDocument().addDocumentListener(dialogListener);
        form.getCreateCmrFieldCheckBox().addActionListener(dialogListener);
        form.getCreateCmrFieldCheckBox2().addActionListener(dialogListener);
        form.getRoleNameTextField().getDocument().addDocumentListener(dialogListener);
        form.getRoleNameTextField2().getDocument().addDocumentListener(dialogListener);
        form.getEjbComboBox().addActionListener(dialogListener);
        form.getEjbComboBox2().addActionListener(dialogListener);
        dialogListener.validateFields();
        if (dataObject.getEjbJar().getEnterpriseBeans().getEntity().length==0)
            dialogDescriptor.setValid(false);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
//            listener.validate();
            if (helper == null) {
                Relationships relationships = ejbJar.getSingleRelationships();
                if (relationships == null) {
                    relationships = ejbJar.newRelationships();
                    ejbJar.setRelationships(relationships);
                }
                helper = new RelationshipHelper(relationships);
            }
            processResult(helper);
            return true;
        } else {
            return false;
        }
    }

    private Vector getEntities() {
        Vector entityNames = new Vector();
        Entity[] entities = ejbJar.getEnterpriseBeans().getEntity();
        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];
            if (entity.getLocal() != null) {
                entityNames.add(entity.getEjbName());
            }
        }
        return entityNames;
    }

    private void processResult(RelationshipHelper helper) {
        String relationName = relationshipNameTextField.getText().trim();
        if (relationName.length() == 0) {
            relationName = roleA.getEjbName() + "-" + roleB.getEjbName(); //NOI18N
        }
        helper.setRelationName(relationName);
        helper.setDescription(descriptionTextArea.getText().trim());
        roleA.processResult(helper.roleA);
        roleB.processResult(helper.roleB);

    }

    private void populateFormFields(RelationshipHelper helper) {
        relationshipNameTextField.setText(helper.getRelationName());
        descriptionTextArea.setText(helper.getDescription());
        roleA.populateFormFields(helper.roleA);
        roleB.populateFormFields(helper.roleB);
    }

    private Entity getEntity(String entityName) {
        if (entityName == null){
            return null;
        }
        Entity[] entities = ejbJar.getEnterpriseBeans().getEntity();
        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];
            if (entityName.equals(entity.getEjbName())) {
                return entity;
            }
        }
        return null;
    }

    private CmpRelationshipsForm initForm() {
        listener = new RelationshipDialogActionListener();
        entityNames = getEntities();
        CmpRelationshipsForm form = new CmpRelationshipsForm();
        relationshipNameTextField = form.getRelationshipNameTextField();
        descriptionTextArea = form.getDescriptionTextArea();

        roleA.roleNameTextField = form.getRoleNameTextField();
        roleA.ejbComboBox = form.getEjbComboBox();
        roleA.multiplicityManyRadioButton = form.getMultiplicityManyRadioButton();
        roleA.multiplicityOneRadioButton = form.getMultiplicityOneRadioButton();
        roleA.cascadeDeleteCheckBox = form.getCascadeDeleteCheckBox();
        roleA.createCmrFieldCheckBox = form.getCreateCmrFieldCheckBox();
        roleA.fieldNameTextField = form.getFieldNameTextField();
        roleA.fieldTypeComboBox = form.getFieldTypeComboBox();
        roleA.init();

        roleB.roleNameTextField = form.getRoleNameTextField2();
        roleB.ejbComboBox = form.getEjbComboBox2();
        roleB.multiplicityManyRadioButton = form.getMultiplicityManyRadioButton2();
        roleB.multiplicityOneRadioButton = form.getMultiplicityOneRadioButton2();
        roleB.cascadeDeleteCheckBox = form.getCascadeDeleteCheckBox2();
        roleB.createCmrFieldCheckBox = form.getCreateCmrFieldCheckBox2();
        roleB.fieldNameTextField = form.getFieldNameTextField2();
        roleB.fieldTypeComboBox = form.getFieldTypeComboBox2();
        roleB.init();
        return form;
    }

    private class RelationshipDialogActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            validate();
        }

        public void validate() {
            roleA.setFieldStates(roleB);
            roleB.setFieldStates(roleA);
        }
    }

    private class DialogListener implements DocumentListener, ActionListener {

        private final JLabel errorLabel;
        private final DialogDescriptor dialogDescriptor;
//        protected JMIUtils.ClassPathScanHelper classPathScanHelper;

        public DialogListener(JLabel errorLabel, DialogDescriptor dialogDescriptor) {
            this.errorLabel = errorLabel;
            this.dialogDescriptor = dialogDescriptor;
//                classPathScanHelper = new JMIUtils.ClassPathScanHelper() {
//                    public void scanFinished() {
//                        validateFields();
//                    }
//                };
            }

        public void changedUpdate(DocumentEvent e) {
            validateFields();
        }

        public void insertUpdate(DocumentEvent e) {
            validateFields();
        }

        public void removeUpdate(DocumentEvent e) {
            validateFields();
        }

        public void actionPerformed(ActionEvent e) {
            validateFields();
        }

        private boolean isCmrFieldSpecified() {
            boolean a = roleA.createCmrFieldCheckBox.isSelected() && !roleA.fieldNameTextField.getText().trim().equals("");
            boolean b = roleB.createCmrFieldCheckBox.isSelected() && !roleB.fieldNameTextField.getText().trim().equals("");
            return a || b;
        }
        
        private void validateFields() {

            final String roleNameA = roleA.getRoleName();
            final String roleNameB = roleB.getRoleName();
            if (false /*classPathScanHelper.isScanInProgress()*/) {
                errorLabel.setText(Utils.getBundleMessage("LBL_ScanningInProgress"));
            } else if (roleNameA != null && roleNameA.equals(roleNameB)) {
                errorLabel.setText(Utils.getBundleMessage("MSG_SameRoleNames"));
                dialogDescriptor.setValid(false);
            } else if (!isCmrFieldSpecified()) {
                errorLabel.setText(Utils.getBundleMessage("MSG_NoCmrDefined"));
                dialogDescriptor.setValid(false);
            } else if (ejbJar.getEnterpriseBeans().getEntity() == null || ejbJar.getEnterpriseBeans().getEntity().length == 0){
                errorLabel.setText(Utils.getBundleMessage("MSG_NoEntitiesFound"));
                dialogDescriptor.setValid(false);
            } else if (isEmpty(roleA.getEjbName()) || isEmpty(roleB.getEjbName())){
                errorLabel.setText(Utils.getBundleMessage("MSG_NoEJbNameSpecified"));
                dialogDescriptor.setValid(false);
            } else {
                String s1 = roleA.validateFieldName();
                if (s1 != null) {
                    errorLabel.setText(s1);
                    dialogDescriptor.setValid(false);
                } else {
                    String s2 = roleB.validateFieldName();
                    if (s2 != null) {
                        errorLabel.setText(s2);
                        dialogDescriptor.setValid(false);
                    } else {
                        errorLabel.setText(" ");
                        dialogDescriptor.setValid(true);
                    }
                }
            }
        }
        
        private boolean isEmpty(String str){
            return null == str || "".equals(str.trim());
        }
    }
}
