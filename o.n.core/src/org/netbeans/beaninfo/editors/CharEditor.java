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

package org.netbeans.beaninfo.editors;

import java.beans.*;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 * Editor for Character.TYPE
 * @author  Petr Zajac, David Strupl
 */
public class CharEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {

    /**
     * Converts the char to String by either leaving
     * the single char or by creating unicode escape.
     */
    public String getAsText () {
        char value = ((Character)getValue()).charValue();
        final StringBuffer buf = new StringBuffer(6);
        switch (value) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (value >= 0x0020 && value <= 0x007f)
                    buf.append(value);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(value);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
        }
        return buf.toString() ;
    }
    /**
     * Set the property value by parsing given String.
     * @param text  The string to be parsed.
     */
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() < 1) {
            // ignore empty value
            return;
        }
        char value = 0;
        if (text.charAt(0) == '\\') {
            // backslash means unicode escape sequence
            char ch = text.length() >=2 ? text.charAt(1) : '\\';
            switch (ch) {
                case 'b': value = '\b'; break;
                case 't': value = '\t'; break;
                case 'n': value = '\n'; break;
                case 'f': value = '\f'; break;
                case 'r': value = '\r'; break;
                case '\\': value = '\\' ; break;
                case 'u' :
                    String num = text.substring(2,text.length());
                    if (num.length () > 4) {
                        // ignore longer strings
                        return;
                    }
                    try {
                        int intValue = Integer.parseInt(num,16);
                        value = (char) intValue;
                        break;
                    } catch (NumberFormatException nfe) {
                        // ignore non parsable strings
                        return;
                    }
                default:
                        // ignore non-chars after backslash
                        return;
                        
            }
        } else {
            value = text.charAt(0);
        }
        setValue(Character.valueOf(value));
    }
    
    /**
     * Accepts Character and String values. If the argument is
     * a String the first character is taken as the new value.
     * @param v new value
     */
    public void setValue(Object newValue) throws IllegalArgumentException {
        if (newValue instanceof Character ) {
            super.setValue(newValue);
            return;
        }
        if (newValue instanceof String) {
            String text = (String ) newValue;
            if (text.length() >= 1) {
                super.setValue(Character.valueOf(text.charAt(0)));
                return;
            }
        }
        if (newValue == null ) {
            super.setValue( Character.valueOf( '\u0000' ) ); // NOI18N
            return;
        }
        
        throw new IllegalArgumentException();
    }
    
    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    public String getJavaInitializationString() {
        if ( ((Character)getValue()).charValue() == '\'' )
            return "'\\''";                 // NOI18N
        else
            return "'" + getAsText() + "'"; // NOI18N
    }

    /**
     * We don't support in place custom editor.
     * @return custom property editor to be shown inside the property
     * sheet.
     */
    public java.awt.Component getInPlaceCustomEditor () {
        return null;
    }
    
    /**
     * We don't support in place custom editor.
     * @return true if this PropertyEditor provides a enhanced in-place custom
     * property editor, false otherwise
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }
    
    /**
     * @return true if this property editor provides tagged values and
     * a custom strings in the choice should be accepted too, false otherwise
     */
    public boolean supportsEditingTaggedValues () {
        return true;
    }
}
