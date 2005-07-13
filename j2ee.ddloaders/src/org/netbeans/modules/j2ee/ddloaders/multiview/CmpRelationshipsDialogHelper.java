/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.CmpRelationshipsForm;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Type;
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
        private JCheckBox getterCheckBox;
        private JCheckBox setterCheckBox;

        private String origEjbName;
        private String origFieldName;
        private String origFieldType;
        private EntityHelper origEntityHelper;
        protected Method origGetterMethod;
        protected Method origSetterMethod;
        protected boolean origGetter;
        protected boolean origSetter;

        private String opositeEjbName;

        private String lastFieldName;
        private String lastFieldType = CLASS_COLLECTION;
        private boolean lastCreateField;
        private boolean lastGetter = true;
        private boolean lastSetter = true;
        private boolean createCmrFieldChanged = true;

        private void init() {
            ejbComboBox.setModel(new DefaultComboBoxModel(entityNames));
            fieldTypeComboBox.setModel(new DefaultComboBoxModel(FILED_TYPE_ITEMS));
            lastCreateField = isCreateCmrField();
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
            boolean getter = hasGetter();
            boolean setter = hasSetter();
            boolean ejbNameChanged = !equal(origEjbName, ejbName);
            boolean fieldChanged = !equal(origFieldName, fieldName) || !equal(origFieldType, fieldType);
            boolean getterChanged = origGetter != getter;
            boolean setterChanged = origSetter != setter;
            if (ejbNameChanged || fieldChanged || getterChanged || setterChanged) {
                if (origEntityHelper != null) {
                    if (getterChanged || fieldChanged) {
                        Utils.removeMethod(origEntityHelper.getLocalBusinessInterfaceClass(), origGetterMethod);
                    }
                    if (setterChanged || fieldChanged) {
                        Utils.removeMethod(origEntityHelper.getLocalBusinessInterfaceClass(), origSetterMethod);
                    }
                    if (fieldChanged) {
                        JavaClass beanClass = origEntityHelper.getBeanClass();
                        Utils.removeMethod(beanClass, origGetterMethod);
                        Utils.removeMethod(beanClass, origSetterMethod);
                    }
                }
                if (fieldName != null) {
                    EntityHelper entityHelper;
                    if (ejbNameChanged) {
                        Entity entity = getEntity(ejbName);
                        entityHelper = entity == null ? null : dataObject.getEntityHelper(entity);
                    } else {
                        entityHelper = origEntityHelper;
                    }
                    if (entityHelper != null) {
                        EntityMethodController entityMethodController = entityHelper.getEntityMethodController();
                        entityMethodController.beginWriteJmiTransaction();
                        boolean rollback = true;
                        try {
                            String typeName = fieldType == null ? getEntity(opositeEjbName).getLocal() : fieldType;
                            Type type = JMIUtils.resolveType(typeName);
                            Method getterMethod = entityHelper.getGetterMethod(fieldName);
                            if (getterMethod == null) {
                                getterMethod = entityHelper.createAccessMethod(fieldName, type, true);
                            }
                            Method setterMethod = entityHelper.getSetterMethod(fieldName, getterMethod);
                            if (setterMethod == null) {
                                setterMethod = entityHelper.createAccessMethod(fieldName, type, false);
                            }
                            if (getter) {
                                entityMethodController.addMethod(getterMethod, true, true);
                            }
                            if (setter) {
                                entityMethodController.addMethod(setterMethod, true, true);
                            }
                            rollback = false;
                        } finally {
                            entityMethodController.endWriteJmiTransaction(rollback);
                        }
                    }
                }
            }
        }

        private boolean equal(String s1, String s2) {
            if (s1 == s2) {
                return true;
            }
            return s1 == s2 || (s1 != null && s1.equals(s2));
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

        private boolean hasSetter() {
            return setterCheckBox.isSelected();
        }

        private boolean hasGetter() {
            return getterCheckBox.isSelected();
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
                    origGetterMethod = origEntityHelper.getGetterMethod(origFieldName);
                    origSetterMethod = origEntityHelper.getSetterMethod(origFieldName, origGetterMethod);
                    JavaClass localBusinessInterfaceClass = origEntityHelper.getLocalBusinessInterfaceClass();
                    origGetter = Utils.getMethod(localBusinessInterfaceClass, origGetterMethod) != null;
                    origSetter = Utils.getMethod(localBusinessInterfaceClass, origSetterMethod) != null;
                    lastGetter = origGetter;
                    lastSetter = origSetter;
                    setLocalGetter(origGetter);
                    setLocalSetter(origSetter);
                }
                setCreateCmrField(true);
                setFieldName(origFieldName);
                setFieldType(origFieldType);
            }
        }

        private void setLocalSetter(boolean setter) {
            setterCheckBox.setSelected(setter);
        }

        private void setLocalGetter(boolean getter) {
            getterCheckBox.setSelected(getter);
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
            lastCreateField = isCreateCmrField();
            String fieldName = getFieldName();
            if (createCmrFieldChanged) {
                createCmrFieldChanged = false;
                if (lastCreateField) {
                    if (fieldName.length() == 0) {
                        setFieldName(lastFieldName);
                    }
                    fieldNameTextField.setEnabled(true);
                    setLocalGetter(lastGetter);
                    getterCheckBox.setEnabled(true);
                    setLocalSetter(lastSetter);
                    setterCheckBox.setEnabled(true);
                } else {
                    lastGetter = getterCheckBox.isSelected();
                    lastSetter = setterCheckBox.isSelected();
                    if (fieldName.length() > 0) {
                        lastFieldName = fieldName;
                    }
                    setFieldName(null);
                    fieldNameTextField.setEnabled(false);
                    setLocalGetter(false);
                    getterCheckBox.setEnabled(false);
                    setLocalSetter(false);
                    setterCheckBox.setEnabled(false);
                }
            }
            boolean opositeMultiple = opositeRole.isMultiple();
            String fieldType = getFieldType();
            if (lastCreateField && opositeMultiple) {
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

        final DialogDescriptor dialogDescriptor = new DialogDescriptor(form, title);
        dialogDescriptor.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setFocusTraversalPolicy(
                form.createFocusTraversalPolicy(dialog.getFocusTraversalPolicy()));
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
        roleA.getterCheckBox = form.getGetterCheckBox();
        roleA.setterCheckBox = form.getSetterCheckBox();
        roleA.init();

        roleB.roleNameTextField = form.getRoleNameTextField2();
        roleB.ejbComboBox = form.getEjbComboBox2();
        roleB.multiplicityManyRadioButton = form.getMultiplicityManyRadioButton2();
        roleB.multiplicityOneRadioButton = form.getMultiplicityOneRadioButton2();
        roleB.cascadeDeleteCheckBox = form.getCascadeDeleteCheckBox2();
        roleB.createCmrFieldCheckBox = form.getCreateCmrFieldCheckBox2();
        roleB.fieldNameTextField = form.getFieldNameTextField2();
        roleB.fieldTypeComboBox = form.getFieldTypeComboBox2();
        roleB.getterCheckBox = form.getGetterCheckBox2();
        roleB.setterCheckBox = form.getSetterCheckBox2();
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

        public DialogListener(JLabel errorLabel, DialogDescriptor dialogDescriptor) {
            this.errorLabel = errorLabel;
            this.dialogDescriptor = dialogDescriptor;
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

        private void validateFields() {

            final String roleNameA = roleA.getRoleName();
            final String roleNameB = roleB.getRoleName();
            if (roleNameA != null && roleNameA.equals(roleNameB)) {
                errorLabel.setText(Utils.getBundleMessage("MSG_SameRoleNames"));
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
    }
}
