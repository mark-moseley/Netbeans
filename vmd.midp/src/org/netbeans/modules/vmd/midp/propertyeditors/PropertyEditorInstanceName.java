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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.ref.WeakReference;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.points.MethodPointCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorInstanceName extends DesignPropertyEditor {
    
    private TypeID typeID;
    private final CustomEditor customEditor;
    private long componentID;
    private boolean canWrite;
    private WeakReference<DesignDocument> document;
    
    private PropertyEditorInstanceName(TypeID typeID) {
        this.typeID = typeID;
        customEditor = new CustomEditor();
    }
    
    public static final DesignPropertyEditor createInstance(TypeID typeID) {
        return new PropertyEditorInstanceName(typeID);
    }
    
    @Override
    public Component getCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            customEditor.setText((String) value.getPrimitiveValue());
        }
        return customEditor;
    }
    
    @Override
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null) {
            return PropertyEditorUserCode.NULL_TEXT;
        }
        return (String) value.getPrimitiveValue();
    }
    
    @Override
    public void setAsText(String text) {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null) {
            return;
        }
        Object pv = value.getPrimitiveValue();
        if (pv != null && pv.equals(text)) {
            return;
        }
        String suggestedName = saveValue(text);
        customEditor.setText(suggestedName);
    }
    
    private String saveValue(final String text) {
        final String[] str = new String[1];
        if (document.get() != null) {
            document.get().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    DesignComponent component = document.get().getComponentByUID(componentID);
                    PropertyValue newInstanceName = InstanceNameResolver.createFromSuggested(component, text);
                    PropertyEditorInstanceName.super.setValue(newInstanceName);
                    str[0] = (String) newInstanceName.getPrimitiveValue();
                }
            });
        }
        return str[0];
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        String text = customEditor.getText();
        if (text.length() > 0) {
            saveValue(text);
        }
    }
    
    @Override
    public void init(DesignComponent component) {
        document = new WeakReference<DesignDocument>(component.getDocument());
        this.componentID = component.getComponentID();
    }
    
    @Override
    public boolean supportsDefaultValue() {
        return false;
    }
    
    @Override
    public boolean canWrite() {
        if (document.get() == null) {
            return false;
        }
        document.get().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (document.get().getSelectedComponents().size() > 1) {
                    canWrite = false;
                } else {
                    canWrite = true;
                }
            }
        });
        
        return canWrite;
    }
    
    // TODO i18nalize it
    private String getLabelName() {
        if (typeID.equals(MethodPointCD.TYPEID)) {
            return "Method Name";
        }
        return "Instance Name";
    }
    
    private final class CustomEditor extends JPanel {
        private JTextField textField;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel label = new JLabel(getLabelName() + ':'); // NOI18N
            constraints.insets = new Insets(12, 12, 3, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.NONE;
            add(label, constraints);
            
            textField = new JTextField();
            constraints.insets = new Insets(0, 12, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            add(textField, constraints);
            
            JPanel spacer = new JPanel();
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(spacer, constraints);
            setPreferredSize(new Dimension(300, 64));
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
    }
    
}
