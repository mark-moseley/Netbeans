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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @author Karol Harezlak
 */
public final class PropertyEditorComboBox extends PropertyEditorUserCode implements PropertyEditorElement {
    
    private long componentID;
    private final Map<String, PropertyValue> values;
    private String[] tags;
    
    private TypeID typeID;
    private TypeID enableTypeID;
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    
    private PropertyEditorComboBox(Map<String, PropertyValue> values, TypeID typeID, TypeID enableTypeID) {
        super();
        
        this.values = values;
        this.typeID = typeID;
        this.enableTypeID = enableTypeID;
        createTags();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static PropertyEditorComboBox createInstance(Map<String, PropertyValue> values, TypeID typeID) {
        return createInstance(values, typeID, null);
    }
    
    public static PropertyEditorComboBox createInstance(Map<String, PropertyValue> values, TypeID typeID, TypeID enableTypeID) {
        if (values == null) {
            throw new IllegalArgumentException("Argument values can't be null"); // NOI18N
        }
        for (String key : values.keySet()) {
            PropertyValue value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("PropertyValue for " + key + " key can't be null"); // NOI18N
            }
        }
        
        PropertyEditorComboBox instance = new PropertyEditorComboBox(values, typeID, enableTypeID);
        return instance;
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorComboBox.class, "LBL_VALUE_STR")); // NOI18N
        customEditor = new CustomEditor();
        customEditor.updateModel();
    }
    
    public JComponent getCustomEditorComponent() {
        return customEditor;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return true;
    }
    
    public boolean isVerticallyResizable() {
        return false;
    }
    
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        for (String key : values.keySet()) {
            PropertyValue tmpValue = values.get(key);
            if (value.getPrimitiveValue().equals(tmpValue.getPrimitiveValue())) {
                return key;
            }
        }
        return "n/a"; // NOI18N
    }
    
    public void setText(String text) {
        saveValue(text);
    }
    
    public String getText() {
        return null;
    }
    
    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            // clear customEditor if needed
        } else {
            customEditor.setValue(value);
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            PropertyValue value = values.get(text);
            if (value != null) {
                super.setValue(value);
            }
        }
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    @Override
    public String[] getTags() {
        return isCurrentValueAUserCodeType() ? null : tags;
    }
    
    private void createTags() {
        int i = 0;
        tags = new String[values.size()];
        for (String valueAsText : values.keySet()) {
            tags[i++] = valueAsText;
        }
    }
    
    @Override
    public void init(DesignComponent component) {
        super.init(component);
        this.componentID = component.getComponentID();
    }
    
    @Override
    public Boolean canEditAsText() {
        if (isCurrentValueAUserCodeType()) {
            return super.canEditAsText();
        }
        return null;
    }
    
    @Override
    public boolean canWrite() {
        final boolean[] canWrite = new boolean[] { true };
        if (!MidpPropertyEditorSupport.singleSelectionEditAsTextOnly()) {
            canWrite[0] = false;
        } else if (enableTypeID != null) {
            if (enableTypeID == FontCD.TYPEID) {
                final DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
                if (document != null) {
                    document.getTransactionManager().readAccess( new Runnable() {
                        public void run() {
                            DesignComponent component = document.getComponentByUID(componentID);
                            int kind = MidpTypes.getInteger(component.readProperty(FontCD.PROP_FONT_KIND));
                            canWrite[0] = kind == FontCD.VALUE_KIND_CUSTOM;
                        }
                    });
                }
            }
        }
        return canWrite[0];
    }
    
    private class CustomEditor extends JPanel implements ActionListener {
        private JComboBox combobox;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox();
            combobox.setModel(new DefaultComboBoxModel());
            combobox.addActionListener(this);
            add(combobox, BorderLayout.CENTER);
        }
        
        public void setValue(PropertyValue value) {
            for (String key : values.keySet()) {
                if (values.get(key).getPrimitiveValue().equals(value.getPrimitiveValue())) {
                    combobox.setSelectedItem(key);
                    break;
                }
            }
        }
        
        public String getText() {
            return (String) combobox.getSelectedItem();
        }
        
        public void updateModel() {
            DefaultComboBoxModel model = (DefaultComboBoxModel) combobox.getModel();
            model.removeAllElements();
            for (String tag : tags) {
                model.addElement(tag);
            }
        }
        
        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
