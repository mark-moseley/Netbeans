/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.wizard;

import java.beans.*;
import java.util.*;
import javax.swing.event.*;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;

/**
 * The UI component of the ConnectionWizardPanel2.
 *
 * @author Tomas Pavek
 */

class ConnectionPanel2 extends javax.swing.JPanel {

    private ConnectionWizardPanel2 wizardPanel;

    private Object[] propertyListData;
    private Object[] methodListData;
    private MethodDescriptor[] methodDescriptors;
    private PropertyDescriptor[] propDescriptors;

    java.util.ResourceBundle bundle;

    /** Creates new form ConnectionPanel2 */
    ConnectionPanel2(ConnectionWizardPanel2 wizardPanel) {
        this.wizardPanel = wizardPanel;

        initComponents();

        bundle = NbBundle.getBundle(ConnectionPanel2.class);

        setName(bundle.getString("CTL_CW_Step2_Title")); // NOI18N

        javax.swing.ButtonGroup gr = new javax.swing.ButtonGroup();
        gr.add(propertyButton);
        gr.add(methodButton);
        gr.add(codeButton);

        targetComponentName.setText(wizardPanel.getTargetComponent().getName());

        actionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        actionList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting())
                    ConnectionPanel2.this.wizardPanel.fireStateChanged();
            }
        });

        // localization code
        Mnemonics.setLocalizedText(targetNameLabel, bundle.getString("CTL_CW_TargetComponent")); // NOI18N
        targetComponentName.setToolTipText(
            bundle.getString("CTL_CW_TargetComponent_Hint")); // NOI18N
        Mnemonics.setLocalizedText(propertyButton, bundle.getString("CTL_CW_SetProperty")); // NOI18N
        propertyButton.setToolTipText(
            bundle.getString("CTL_CW_SetProperty_Hint")); // NOI18N
        Mnemonics.setLocalizedText(methodButton, bundle.getString("CTL_CW_MethodCall")); // NOI18N
        methodButton.setToolTipText(bundle.getString("CTL_CW_MethodCall_Hint")); // NOI18N
        Mnemonics.setLocalizedText(codeButton, bundle.getString("CTL_CW_XUserCode")); // NOI18N
        codeButton.setToolTipText(
            bundle.getString("CTL_CW_XUserCode_Hint")); // NOI18N

        targetComponentName.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_TargetComponent")); // NOI18N
        propertyButton.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_SetProperty")); // NOI18N
        methodButton.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_MethodCall")); // NOI18N
        codeButton.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_XUserCode")); // NOI18N
        actionList.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ActionList")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ConnectionPanel2")); // NOI18N

        updateActionList();

        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); // NOI18N
    }

    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(450, 300);
    }

    int getActionType() {
        if (methodButton.isSelected())
            return ConnectionWizardPanel2.METHOD_TYPE;
        else if (propertyButton.isSelected())
            return ConnectionWizardPanel2.PROPERTY_TYPE;
        else
            return ConnectionWizardPanel2.CODE_TYPE;
    }

    MethodDescriptor getSelectedMethod() {
        if (!methodButton.isSelected() || actionList.getSelectedIndex() == -1)
            return null;
        return methodDescriptors[actionList.getSelectedIndex()];
    }

    PropertyDescriptor getSelectedProperty() {
        if (!propertyButton.isSelected() || actionList.getSelectedIndex() == -1)
            return null;
        return propDescriptors[actionList.getSelectedIndex()];
    }

    private void updateActionList() {
        if (codeButton.isSelected()) {
            actionList.setListData(new String [] {
                bundle.getString("CTL_CW_UserCodeText1"), // NOI18N
                bundle.getString("CTL_CW_UserCodeText2") }); // NOI18N
            actionList.setEnabled(false);
            actionList.getAccessibleContext().setAccessibleName(codeButton.getText());
        } 
        else if (propertyButton.isSelected()) {
            // properties list
            actionList.setEnabled(true);
            if (propertyListData == null) {
                BeanInfo targetBeanInfo =
                    wizardPanel.getTargetComponent().getBeanInfo();
                PropertyDescriptor[] descs = targetBeanInfo.getPropertyDescriptors();

                // filter out read-only properties // [FUTURE: provide also indexed properties]
                ArrayList list = new ArrayList();
                for (int i = 0; i < descs.length; i++) {
                    if (descs[i].getWriteMethod() != null) {
                        list.add(descs[i]);
                    }
                }

                // sort the properties by name
                Collections.sort(list, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return((PropertyDescriptor)o1).getName().compareTo(((PropertyDescriptor)o2).getName());
                    }
                });

                propDescriptors = new PropertyDescriptor [list.size()];
                list.toArray(propDescriptors);

                propertyListData = new String [propDescriptors.length];
                for (int i = 0; i < propDescriptors.length; i++) {
                    propertyListData [i] = propDescriptors [i].getName();
                }
            }
            actionList.setListData(propertyListData);
            actionList.getAccessibleContext().setAccessibleName(propertyButton.getText());
        } 
        else {
            // methods list
            actionList.setEnabled(true);
            if (methodListData == null) {
                BeanInfo targetBeanInfo =
                    wizardPanel.getTargetComponent().getBeanInfo();
                methodDescriptors = targetBeanInfo.getMethodDescriptors();
                ArrayList list = new ArrayList();
                for (int i = 0; i < methodDescriptors.length; i++) {
                    list.add(methodDescriptors[i]);
                }

                // sort the methods by name
                Collections.sort(list, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return((MethodDescriptor)o1).getName().compareTo(((MethodDescriptor)o2).getName());
                    }
                });

                // copy it back to the array as it is used later
                list.toArray(methodDescriptors);

                methodListData = new String [list.size()];
                int i = 0;
                for (Iterator it = list.iterator(); it.hasNext();) {
                    methodListData [i++] = getMethodName((MethodDescriptor)it.next());
                }
            }
            actionList.setListData(methodListData);
            actionList.getAccessibleContext().setAccessibleName(methodButton.getText());
        }
        actionList.revalidate();
        actionList.repaint();
    }

    private static String getMethodName(MethodDescriptor desc) {
        StringBuffer sb = new StringBuffer(desc.getName());
        Class[] params = desc.getMethod().getParameterTypes();
        if ((params == null) ||(params.length == 0)) {
            sb.append("()"); // NOI18N
        } else {
            for (int i = 0; i < params.length; i++) {
                if (i == 0) sb.append("("); // NOI18N
                else sb.append(", "); // NOI18N
                sb.append(org.openide.util.Utilities.getShortClassName(params[i]));
            }
            sb.append(")"); // NOI18N
        }

        return sb.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        targerInfoPanel = new javax.swing.JPanel();
        targetNamePanel = new javax.swing.JPanel();
        targetNameLabel = new javax.swing.JLabel();
        targetComponentName = new javax.swing.JTextField();
        actionTypePanel = new javax.swing.JPanel();
        propertyButton = new javax.swing.JRadioButton();
        methodButton = new javax.swing.JRadioButton();
        codeButton = new javax.swing.JRadioButton();
        actionPanel = new javax.swing.JScrollPane();
        actionList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout(0, 2));

        targerInfoPanel.setLayout(new java.awt.BorderLayout(0, 6));

        targetNamePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        targetNameLabel.setLabelFor(targetComponentName);
        targetNameLabel.setText("Target Component:");
        targetNameLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 6)));
        targetNamePanel.add(targetNameLabel);

        targetComponentName.setEditable(false);
        targetComponentName.setText("jTextField1");
        targetNamePanel.add(targetComponentName);

        targerInfoPanel.add(targetNamePanel, java.awt.BorderLayout.NORTH);

        actionTypePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        propertyButton.setSelected(true);
        propertyButton.setText("Set Property");
        propertyButton.setMargin(new java.awt.Insets(2, 2, 2, 10));
        propertyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectionPanel2.this.actionTypeButtonPressed(evt);
            }
        });

        actionTypePanel.add(propertyButton);

        methodButton.setText("Method Call");
        methodButton.setMargin(new java.awt.Insets(2, 2, 2, 10));
        methodButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectionPanel2.this.actionTypeButtonPressed(evt);
            }
        });

        actionTypePanel.add(methodButton);

        codeButton.setText("User Code");
        codeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectionPanel2.this.actionTypeButtonPressed(evt);
            }
        });

        actionTypePanel.add(codeButton);

        targerInfoPanel.add(actionTypePanel, java.awt.BorderLayout.CENTER);

        add(targerInfoPanel, java.awt.BorderLayout.NORTH);

        actionPanel.setViewportView(actionList);

        add(actionPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void actionTypeButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionTypeButtonPressed
        updateActionList();
        wizardPanel.fireStateChanged();
        if (evt.getSource() != codeButton)
            actionList.requestFocus();
    }//GEN-LAST:event_actionTypeButtonPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton codeButton;
    private javax.swing.JList actionList;
    private javax.swing.JRadioButton methodButton;
    private javax.swing.JScrollPane actionPanel;
    private javax.swing.JPanel targetNamePanel;
    private javax.swing.JRadioButton propertyButton;
    private javax.swing.JPanel targerInfoPanel;
    private javax.swing.JLabel targetNameLabel;
    private javax.swing.JTextField targetComponentName;
    private javax.swing.JPanel actionTypePanel;
    // End of variables declaration//GEN-END:variables
}
