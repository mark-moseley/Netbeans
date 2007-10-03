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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package com.sun.jsfcl.std;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
// bugfix# 9219 for attachEnv() method
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/**
 *This is a clone of the code from org.netbeans.beaninfo.editors.StringCustomEditor.  Its the cheezy
 *way to do it, but since the class was not built to be subclasseable, I cloned it to get same behavior
 *and hope it reduces risk of regressions.
 *
 * @author eric
 */

/** A property editor for String class.
 * @author   Ian Formanek
 * @version  1.00, 18 Sep, 1998
 * @deprecated
 */
public class RaveStringEditor extends PropertyEditorSupport implements ExPropertyEditor {
    private static boolean useRaw = Boolean.getBoolean("netbeans.stringEditor.useRawCharacters");

    // bugfix# 9219 added editable field and isEditable() "getter" to be used in StringCustomEditor
    private boolean editable = true;
    /** gets information if the text in editor should be editable or not */
    public boolean isEditable() {
        return (editable);
    }

    protected boolean forceOneline() {
        return false;
    }
    
    // <RAVE> String property editor should be changed to not use "null" to show null values,
    //        but instead show a blank "" string for this.
    public String getAsText() {
        if (getValue() == null) {
            return "";
        } else {
            return super.getAsText();
        }
    }

    /** sets new value */
    public void setAsText(String s) {
        setValue(s);
    }

    public void setValue(Object value) {
        String s = (String)value;
        if ("".equals(s) && getValue() == null) { // NOI18N
            return;
        }
        super.setValue(s);
    }
    
    protected void superSetValue(Object value) {
        super.setValue(value);
    }
    
    public String getJavaInitializationString() {
        String s = (String)getValue();
        if (s == null) {
            return "\"\""; // NOI18N
        } else {
            return "\"" + toAscii(s) + "\""; // NOI18N
        }
    }

    // </RAVE>

    public boolean supportsCustomEditor() {
        return customEd;
    }

    public java.awt.Component getCustomEditor() {
        return new RaveStringCustomEditor(getAsText(), isEditable(), oneline, instructions, getIgnoreCrs(), this); // NOI18N
    }

    public Object getCustomEditorValue(String string) {
        return string;
    }
    
    private static String toAscii(String str) {
        StringBuffer buf = new StringBuffer(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\b':
                    buf.append("\\b");
                    break; // NOI18N
                case '\t':
                    buf.append("\\t");
                    break; // NOI18N
                case '\n':
                    buf.append("\\n");
                    break; // NOI18N
                case '\f':
                    buf.append("\\f");
                    break; // NOI18N
                case '\r':
                    buf.append("\\r");
                    break; // NOI18N
                case '\"':
                    buf.append("\\\"");
                    break; // NOI18N
                    //        case '\'': buf.append("\\'"); break; // NOI18N
                case '\\':
                    buf.append("\\\\");
                    break; // NOI18N
                default:
                    if (c >= 0x0020 && (useRaw || c <= 0x007f)) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N
                        String hex = Integer.toHexString(c);
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            buf.append('0');
                        }
                        buf.append(hex);
                    }
            }
        }
        return buf.toString();
    }

    // EAT modified - made protected
    protected String instructions = null;
    private boolean oneline = false;
    private boolean customEd = true;
    // bugfix# 9219 added attachEnv() method checking if the user canWrite in text box
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property) {
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
            //enh 29294 - support one-line editor & suppression of custom editor
            if (useOriginalShortDescriptionForInstructions()) {
                instructions = (String)prop.getValue("originalShortDescription"); // NOI18N
                if (instructions == null) {
                    instructions = prop.getShortDescription();
                }
            } else {
                instructions = (String)prop.getValue("instructions"); //NOI18N
            }
            if (forceOneline()) {
                oneline = true;
            } else {
                oneline = Boolean.TRUE.equals(prop.getValue("oneline")); //NOI18N
            }
            customEd = !Boolean.TRUE.equals(prop.getValue
                ("suppressCustomEditor")); //NOI18N
        }
    }
    
    protected boolean useOriginalShortDescriptionForInstructions() {
        return false;
    }
    
    protected boolean getIgnoreCrs() {

        return false;
    }
}
