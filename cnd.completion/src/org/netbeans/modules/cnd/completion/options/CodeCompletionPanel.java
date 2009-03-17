/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.options;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CodeCompletionPanel extends javax.swing.JPanel implements DocumentListener {

    private final Preferences preferences;

    /** Creates new form CodeCompletionPanel */
    public CodeCompletionPanel(Preferences preferences) {
        this.preferences = preferences;
        initComponents();
        autoInsertIncludeDirectives.setSelected(preferences.getBoolean(CsmCompletionUtils.CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, true));
        autoCompletionTriggersField.setText(preferences.get(CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS, ".->::")); //NOI18N
        autoCompletionTriggersPreprocField.setText(preferences.get(CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS, "\"<")); //NOI18N
        autoCompletionTriggersField.getDocument().addDocumentListener(this);
        autoCompletionTriggersPreprocField.getDocument().addDocumentListener(this);
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCusromizer(preferences);
            }
        };
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoCompletionTriggersLabel = new javax.swing.JLabel();
        autoCompletionTriggersField = new javax.swing.JTextField();
        autoInsertIncludeDirectives = new javax.swing.JCheckBox();
        autoCompletionTriggersPreprocLabel = new javax.swing.JLabel();
        autoCompletionTriggersPreprocField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

        autoCompletionTriggersLabel.setLabelFor(autoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(autoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTriggersLabel.text")); // NOI18N

        autoCompletionTriggersField.setAlignmentX(1.0F);

        autoInsertIncludeDirectives.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoInsertIncludeDirectives, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoInclude.text")); // NOI18N
        autoInsertIncludeDirectives.setBorder(null);
        autoInsertIncludeDirectives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoInsertIncludeDirectivesActionPerformed(evt);
            }
        });

        autoCompletionTriggersPreprocLabel.setLabelFor(autoCompletionTriggersPreprocField);
        org.openide.awt.Mnemonics.setLocalizedText(autoCompletionTriggersPreprocLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTriggersPreprocLabel.text")); // NOI18N

        autoCompletionTriggersPreprocField.setAlignmentX(1.0F);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(autoCompletionTriggersLabel)
                    .add(autoCompletionTriggersPreprocLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(autoCompletionTriggersPreprocField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(autoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(33, 33, 33))
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(autoInsertIncludeDirectives)
                .addContainerGap(37, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoCompletionTriggersLabel)
                    .add(autoCompletionTriggersField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoInsertIncludeDirectives)
                .add(24, 24, 24)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoCompletionTriggersPreprocLabel)
                    .add(autoCompletionTriggersPreprocField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(168, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void autoInsertIncludeDirectivesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoInsertIncludeDirectivesActionPerformed
        preferences.putBoolean(CsmCompletionUtils.CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, autoInsertIncludeDirectives.isSelected());
}//GEN-LAST:event_autoInsertIncludeDirectivesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField autoCompletionTriggersField;
    private javax.swing.JLabel autoCompletionTriggersLabel;
    private javax.swing.JTextField autoCompletionTriggersPreprocField;
    private javax.swing.JLabel autoCompletionTriggersPreprocLabel;
    private javax.swing.JCheckBox autoInsertIncludeDirectives;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    private void update(DocumentEvent e) {
        if (e.getDocument() == autoCompletionTriggersField.getDocument()) {
            preferences.put(CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersField.getText());
        } else if (e.getDocument() == autoCompletionTriggersPreprocField.getDocument()) {
            preferences.put(CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersPreprocField.getText());
        }
    }

    private static class CodeCompletionPreferencesCusromizer implements PreferencesCustomizer {

        private Preferences preferences;

        private CodeCompletionPreferencesCusromizer(Preferences p) {
            preferences = p;
        }

        public String getId() {
            return "org.netbeans.modules.cnd.completion.options"; //NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanelName"); // NOI18N
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion.cpp"); //NOI18N
        }

        public JComponent getComponent() {
            return new CodeCompletionPanel(preferences);
        }
    }
}
