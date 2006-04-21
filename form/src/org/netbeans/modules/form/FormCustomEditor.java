/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

import java.awt.*;
import java.beans.PropertyEditor;
import javax.swing.*;

/** 
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditor extends JPanel
                              implements EnhancedCustomPropertyEditor
{
    private static final int DEFAULT_WIDTH  = 350;
    private static final int DEFAULT_HEIGHT = 350;

    // -----------------------------------------------------------------------------
    // Private variables

    private FormPropertyEditor editor;
    private PropertyEditor[] allEditors;
    private Component[] allCustomEditors;
    private boolean[] validValues;

    private String preCode;
    private String postCode;

    /** Creates new form FormCustomEditor */
    public FormCustomEditor(FormPropertyEditor editor,
                            Component currentCustomEditor)
    {
        initComponents();

        advancedButton.setText(FormUtils.getBundleString("CTL_Advanced")); // NOI18N
//        advancedButton.setMnemonic(FormUtils.getBundleString(
//                                      "CTL_Advanced_mnemonic").charAt(0)); // NOI18N
        if (editor.getProperty() instanceof RADProperty)
            advancedButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showAdvancedSettings();
                }
            });
        else
            advancedButton.setEnabled(false);

        Mnemonics.setLocalizedText(jLabel1, FormUtils.getBundleString("LAB_SelectMode")); // NOI18N
        jLabel1.setLabelFor(editorsCombo);
        
        this.editor = editor;
        preCode = editor.getProperty().getPreCode();
        postCode = editor.getProperty().getPostCode();
        allEditors = editor.getAllEditors();

        PropertyEditor currentEditor = editor.getCurrentEditor();
        int currentIndex;

        if (currentEditor != null) {
            currentIndex = -1;
            for (int i=0; i < allEditors.length; i++)
                if (currentEditor.getClass().equals(allEditors[i].getClass())) {
                    currentIndex = i;
                    allEditors[i] = currentEditor;
                    break;
                }
            if (currentIndex == -1) {
                // this should not happen, but we cannot exclude it
                PropertyEditor[] editors = new PropertyEditor[allEditors.length+1];
                editors[0] = currentEditor;
                System.arraycopy(allEditors, 0, editors, 1, allEditors.length);
                allEditors = editors;
                currentIndex = 0;
            }
        }
        else currentIndex = 0;

        allCustomEditors = new Component[allEditors.length];
        validValues = new boolean[allEditors.length];

        PropertyEnv env = editor.getPropertyEnv();
        Object currentValue = editor.getValue();

        // go through all available property editors, set their values and
        // setup their custom editors
        for (int i=0; i < allEditors.length; i++) {
            PropertyEditor prEd = allEditors[i];
            editor.getPropertyContext().initPropertyEditor(prEd);

            boolean valueSet = false;
            if (i == currentIndex) { // this is the currently used editor
                valueSet = true;
            }
            else {
                if (env != null && prEd instanceof ExPropertyEditor)
                    ((ExPropertyEditor)prEd).attachEnv(env);

                if (currentValue != null) {
                    try {
                        if (editor.getPropertyType().isAssignableFrom(
                                               currentValue.getClass()))
                        {   // currentValue is a real property value corresponding
                            // to property editor value type
                            prEd.setValue(currentValue);
                            valueSet = true;
                        }
                        else if (currentValue instanceof FormDesignValue) {
                            Object realValue = // get real value of the design value
                                ((FormDesignValue)currentValue).getDesignValue();
                            if (realValue != FormDesignValue.IGNORED_VALUE) {
                                // there is a known real value
                                prEd.setValue(realValue); 
                                valueSet = true;
                            }
                        }
                    }
                    catch (IllegalArgumentException ex) {} // ignore
                }
                // [null value should not be set?]

                if (!valueSet) {
                    // no reasonable value for this property editor, try to
                    // set the default value
                    Object defaultValue = editor.getProperty().getDefaultValue();
                    if (defaultValue != BeanSupport.NO_VALUE) {
                        prEd.setValue(defaultValue);
                        valueSet = true;
                    }
                    // [but if there's no default value it is not possible to
                    // switch to this property editor and enter something - see
                    // getPropertyValue() - it returns BeanSupport.NO_VALUE]
                }
            }
            validValues[i] = valueSet;

            String editorName = prEd instanceof NamedPropertyEditor ?
                        ((NamedPropertyEditor)prEd).getDisplayName() :
                        Utilities.getShortClassName(prEd.getClass());

            Component custEd = null;
            if (i == currentIndex)
                custEd = currentCustomEditor;
            else if (prEd.supportsCustomEditor())
                custEd = prEd.getCustomEditor();

            if (custEd == null || custEd instanceof Window) {
                JPanel p = new JPanel(new GridBagLayout());
                JLabel label = new JLabel(
                    FormUtils.getBundleString("CTL_PropertyEditorDoesNot")); // NOI18N
                p.add(label);
                p.getAccessibleContext().setAccessibleDescription(label.getText());
                custEd = p;
            }

            allCustomEditors[i] = custEd;
            cardPanel.add(editorName, custEd);
            editorsCombo.addItem(editorName);
        }

        editorsCombo.setSelectedIndex(currentIndex);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, (String) editorsCombo.getSelectedItem());

        editorsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CardLayout cl2 = (CardLayout) cardPanel.getLayout();
                cl2.show(cardPanel, (String) editorsCombo.getSelectedItem());

                int i = editorsCombo.getSelectedIndex();
                HelpCtx helpCtx = i < 0 ? null :
                                  HelpCtx.findHelp(cardPanel.getComponent(i));
                String helpID = helpCtx != null ? helpCtx.getHelpID() : ""; // NOI18N
                HelpCtx.setHelpIDString(FormCustomEditor.this, helpID);
                
                updateAccessibleDescription(i < 0 ? null : cardPanel.getComponent(i));
            }
        });

        updateAccessibleDescription(cardPanel.getComponent(currentIndex));
        advancedButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_Advanced")); // NOI18N
        editorsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_BTN_SelectMode")); // NOI18N
    }
    
    private void updateAccessibleDescription(Component comp) {
        if (comp instanceof javax.accessibility.Accessible
            && comp.getAccessibleContext().getAccessibleDescription() != null) {

            getAccessibleContext().setAccessibleDescription(
                FormUtils.getFormattedBundleString(
                    "ACSD_FormCustomEditor", // NOI18N
                    new Object[] {
                        comp.getAccessibleContext().getAccessibleDescription()
                    }
                )
            );
        } else {
            getAccessibleContext().setAccessibleDescription(null);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        editorsCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cardPanel = new javax.swing.JPanel();
        advancedButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(editorsCombo, gridBagConstraints);

        jLabel1.setLabelFor(editorsCombo);
        jLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cardPanel.setLayout(new java.awt.CardLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 7, 7);
        jPanel1.add(cardPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(jPanel1, gridBagConstraints);

        advancedButton.setText("jButton1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(advancedButton, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advancedButton;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JComboBox editorsCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
    public Dimension getPreferredSize() {
        Dimension inh = super.getPreferredSize();
        return new Dimension(Math.max(inh.width, DEFAULT_WIDTH), Math.max(inh.height, DEFAULT_HEIGHT));
    }
    
    private void showAdvancedSettings() {
        FormCustomEditorAdvanced fcea = new FormCustomEditorAdvanced(preCode, postCode);
        DialogDescriptor dd = new DialogDescriptor(
            fcea,
            FormUtils.getFormattedBundleString(
                "FMT_CTL_AdvancedInitializationCode", // NOI18N
                 new Object[] { editor.getProperty().getName() }));

        dd.setHelpCtx(new HelpCtx("gui.source.modifying.property")); // NOI18N
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            preCode = fcea.getPreCode();
            postCode = fcea.getPostCode();
        }
    }
    
    // -----------------------------------------------------------------------------
    // EnhancedCustomPropertyEditor implementation

    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does
     * not contain a valid property value (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        int currentIndex = editorsCombo.getSelectedIndex();
        PropertyEditor currentEditor = currentIndex > -1 ?
                                       allEditors[currentIndex] : null;
        Component currentCustomEditor = currentIndex > -1 ?
                                        allCustomEditors[currentIndex] : null;
        Object value;

        if (currentCustomEditor instanceof EnhancedCustomPropertyEditor) {
            // current editor is EnhancedCustomPropertyEditor too
            value = ((EnhancedCustomPropertyEditor) currentCustomEditor)
                                                        .getPropertyValue();
        }
        else if (currentIndex > -1) {
            value = validValues[currentIndex] ? currentEditor.getValue() :
                                                BeanSupport.NO_VALUE;
        }
        else value = editor.getValue();

        // set the current property editor to FormPropertyEditor (to be used as
        // the custom editor provider next time; and also for code generation);
        // it should be set for all properties (of all nodes selected)
        if (currentIndex > -1) {
            Object[] nodes = editor.getPropertyEnv().getBeans();
            if (nodes == null || nodes.length <= 1) {
                FormProperty prop = editor.getProperty();

                prop.setPreCode(preCode);
                prop.setPostCode(postCode);

                value = new FormProperty.ValueWithEditor(value, currentEditor);

                I18nSupport.propertyEditorChanging(prop, currentEditor);
            }
            else { // there are more nodes selected
                String propName = editor.getProperty().getName();

                for (int i=0; i < nodes.length; i++) {
                    if (!(nodes[i] instanceof Node))
                        break; // these are not nodes...

                    Node node = (Node) nodes[i];
                    FormPropertyCookie propCookie = (FormPropertyCookie)
                        node.getCookie(FormPropertyCookie.class);
                    if (propCookie == null)
                        break; // not form nodes...

                    FormProperty prop = propCookie.getProperty(propName);
                    if (prop == null)
                        continue; // property not known

                    prop.setPreCode(preCode);
                    prop.setPostCode(postCode);
                }

                value = new FormProperty.ValueWithEditor(value, currentIndex);
            }
        }

        return value;
    }
}
