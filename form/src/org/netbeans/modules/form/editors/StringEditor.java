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

package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * Property editor for String class. Not used directly, but wrapped in
 * StringEditor from editors2 package, allowing to create resource values.
 * 
 * @author Tomas Pavek
 */
public class StringEditor extends PropertyEditorSupport
        implements FormAwareEditor, ExPropertyEditor, DocumentListener, Runnable
{
    private boolean editable = true;   
    private Component customEditor;
    private JTextComponent textComp;
    private boolean htmlText;

    private boolean valueUpdateInvoked;

    public void setValue(Object value) {
        super.setValue(value);
        if (!valueUpdateInvoked && textComp != null && textComp.isShowing())
            setValueToCustomEditor();
    }

    public void setAsText(String text) {
        setValue(text);
    }

    public String getJavaInitializationString () {
        String s = (String) getValue();
        return "\"" + toAscii(s) + "\""; // NOI18N
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {
        if (customEditor == null) {
            JTextArea textArea = new JTextArea();
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setColumns(60);
            textArea.setRows(8);
            textArea.getDocument().addDocumentListener(this);

            JScrollPane scroll = new JScrollPane();
            scroll.setViewportView(textArea);

            JLabel htmlTipLabel = new JLabel(NbBundle.getMessage(StringEditor.class, "StringEditor.htmlTipLabel.text")); // NOI18N

            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            layout.setAutocreateGaps(true);
            panel.setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup()
                        .add(scroll)
                        .add(htmlTipLabel))
                    .addContainerGap());
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .addContainerGap().add(scroll).add(htmlTipLabel));

            customEditor = panel;
            textComp = textArea;
            htmlTipLabel.setVisible(htmlText);
        }

        textComp.setEditable(editable);
        setValueToCustomEditor();

        return customEditor;
    }

    private static String toAscii(String str) {
        StringBuilder buf = new StringBuilder(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\"': buf.append("\\\""); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020/* && c <= 0x007f*/)
                    buf.append(c);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
            }
        }
        return buf.toString();
    }

    // FormAwareEditor
    public void setContext(FormModel formModel, FormProperty property) {
        htmlText = FormUtils.isHTMLTextProperty(property);
    }

    // FormAwareEditor
    public void updateFormVersionLevel() {
    }

    // ExPropertyEditor
    public void attachEnv(PropertyEnv env) {        
        FeatureDescriptor desc = env.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            editable = prop.canWrite();
            if (textComp != null)
                textComp.setEditable(editable);
        }
    }

    private void setValueToCustomEditor() {
        valueUpdateInvoked = true;
        textComp.setText(getAsText());
        valueUpdateInvoked = false;
    }

    // DocumentListener
    public void insertUpdate(DocumentEvent e) {
        if (!valueUpdateInvoked) {
            valueUpdateInvoked = true;
            EventQueue.invokeLater(this);
        }
    }

    // DocumentListener
    public void removeUpdate(DocumentEvent e) {
        if (!valueUpdateInvoked) {
            valueUpdateInvoked = true;
            EventQueue.invokeLater(this);
        }
    }

    // DocumentListener
    public void changedUpdate(DocumentEvent e) {
    }

    // updates value from the custom editor
    public void run() {
        if (textComp != null)
            setValue(textComp.getText());
        valueUpdateInvoked = false;
    }

}
