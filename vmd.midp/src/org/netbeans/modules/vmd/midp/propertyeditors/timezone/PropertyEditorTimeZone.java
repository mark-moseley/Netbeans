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

package org.netbeans.modules.vmd.midp.propertyeditors.timezone;

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTimeZone extends PropertyEditorUserCode {

    private PropertyEditorTimeZone.TimeZoneEditor timeZoneEditor;

    private PropertyEditorTimeZone() {
        super(NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_TIME_ZONE_UCLABEL")); // NOI18N

        timeZoneEditor = new TimeZoneEditor ();
        initElements(Collections.<PropertyEditorElement>singleton (timeZoneEditor));
    }
    
    public static PropertyEditorTimeZone createInstance() {
        return new PropertyEditorTimeZone();
    }
    
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        return (String) value.getPrimitiveValue();
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (timeZoneEditor.getRadioButton().isSelected())
            saveValue(timeZoneEditor.getTextForPropertyValue ());
    }
    
    private final class TimeZoneEditor implements PropertyEditorElement, ActionListener {
        private JRadioButton radioButton;
        private TimeZoneComboboxModel model;
        private JComboBox combobox;
        
        public TimeZoneEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_TIMEZONE")); // NOI18N
            model = new TimeZoneComboboxModel();
            combobox = new JComboBox(model);
            combobox.setEditable (true);
            combobox.addActionListener(this);
        }
        
        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String timeZone;
                for (int i = 0; i < model.getSize(); i++) {
                    timeZone = (String) model.getElementAt(i);
                    if (timeZone.equals(value.getPrimitiveValue())) {
                        model.setSelectedItem(timeZone);
                        break;
                    }
                }
            }
        }
        
        public void setTextForPropertyValue (String text) {
            saveValue(text);
        }
        
        public String getTextForPropertyValue () {
            return (String) combobox.getSelectedItem();
        }
        
        public JComponent getCustomEditorComponent() {
            return combobox;
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
        
        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
    
}
