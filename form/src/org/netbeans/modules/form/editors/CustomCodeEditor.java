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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;

/** Customizer for "code properties" used by JavaCodeGenerator.
 *
 * @author  vzboril
 */

public class CustomCodeEditor extends javax.swing.JPanel implements PropertyChangeListener {
    private PropertyEditor propertyEditor;

    public CustomCodeEditor(PropertyEditor propertyEditor, PropertyEnv env,
                            javax.swing.JEditorPane editorPane) {
        this.propertyEditor = propertyEditor;
        this.editorPane = editorPane;        
        initComponents();                
        try {
            codeEditorPane.setText((String) propertyEditor.getValue()); 
        }
        catch (Exception ex) { // ignore - should not happen
            ex.printStackTrace();
        }
        Object um = editorPane.getDocument().getProperty(BaseDocument.UNDO_MANAGER_PROP);
        if (um instanceof UndoManager) {
            ((UndoManager)um).discardAllEdits();
        }

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

        java.util.ResourceBundle bundle =
            org.openide.util.NbBundle.getBundle(CustomCodeEditor.class);

        Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_InsertCode")); // NOI18N
        jLabel1.setLabelFor(codeEditorPane);        
        codeEditorPane.setPreferredSize(new java.awt.Dimension(440, 200));
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
        
        codeEditorPane.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CustomCodeEditor.label1")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CustomCodeEditor")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        codeEditorPane = this.editorPane;
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        codeEditorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                codeEditorPaneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                codeEditorPaneFocusLost(evt);
            }
        });

        jScrollPane1.setViewportView(codeEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
        add(jScrollPane1, gridBagConstraints);

        jLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(jLabel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void codeEditorPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codeEditorPaneFocusLost
        // Add your handling code here:
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_codeEditorPaneFocusLost

    private void codeEditorPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codeEditorPaneFocusGained
        // Add your handling code here:
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_codeEditorPaneFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // Add your handling code here:
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(true); // true is HARDCODED here due to BUG in MAC OS X
    }//GEN-LAST:event_formFocusGained

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        // Add your handling code here:
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_formComponentShown

    private javax.swing.JEditorPane editorPane;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane codeEditorPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            propertyEditor.setValue(codeEditorPane.getText());
        }
    }
}
