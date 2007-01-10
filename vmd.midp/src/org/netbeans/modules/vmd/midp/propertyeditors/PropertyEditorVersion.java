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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorVersion extends DesignPropertyEditor {
    
    private final CustomEditor customEditor;
    
    private PropertyEditorVersion() {
        customEditor = new CustomEditor();
    }
    
    public static final PropertyEditorVersion createInstance() {
        return new PropertyEditorVersion();
    }
    
    public Component getCustomEditor() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value != null && value.getKind() != PropertyValue.Kind.NULL) {
            customEditor.setText((String) value.getPrimitiveValue ());
        }
        return customEditor;
    }
    
    public boolean supportsCustomEditor() {
        return false;
    }
    
    public boolean canEditAsText() {
        return false;
    }
    
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        if (value == null || value.getKind() == PropertyValue.Kind.NULL) {
            return PropertyEditorUserCode.NULL_TEXT;
        }
        return (String) value.getPrimitiveValue ();
    }
    
    public void setAsText(String text) {
        if (!PropertyEditorUserCode.NULL_TEXT.equals(text)) {
            saveValue(text);
        }
    }
    
    private void saveValue(final String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }
    
    public void customEditorOKButtonPressed() {
        saveValue(customEditor.getText());
    }
    
    public boolean supportsDefaultValue() {
        return false;
    }
    
    private final class CustomEditor extends JPanel {
        private JTextField textField;
        
        public CustomEditor() {
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            JLabel label = new JLabel("Version:");
            constraints.insets = new Insets(12, 12, 12, 6);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(label, constraints);
            
            textField = new JTextField();
            constraints.insets = new Insets(12, 6, 12, 12);
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.weightx = 1.0;
            constraints.weighty = 0.0;
            constraints.fill = GridBagConstraints.BOTH;
            add(textField, constraints);
            
            setPreferredSize(new Dimension(300, 40));
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
    }
}
