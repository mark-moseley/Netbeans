/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsScript extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -5593481225914071L;
    private final String displayName;
    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final SourcesFolderProvider sourcesFolderProvider;

    public RunAsScript(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider) {
        this(manager, sourcesFolderProvider, NbBundle.getMessage(RunAsScript.class, "LBL_ConfigScript"));
    }

    private RunAsScript(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider, String displayName) {
        super(manager);
        this.displayName = displayName;
        this.sourcesFolderProvider = sourcesFolderProvider;

        initComponents();

        addListeners();
        labels = new JLabel[] {
            indexFileLabel,
        };
        textFields = new JTextField[] {
            indexFileTextField,
        };
        propertyNames = new String[] {
            RunConfigurationPanel.INDEX_FILE,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
    }

    private void addListeners() {
        interpreterTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                changeSupport.fireChange();
            }
        });
        runAsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void loadPhpInterpreter() {
        interpreterTextField.setText(PhpOptions.getInstance().getPhpInterpreter());
    }

    @Override
    protected RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.SCRIPT;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox getRunAsCombo() {
        return runAsCombo;
    }

    protected void loadFields() {
        loadPhpInterpreter();
    }

    protected void validateFields() {
        // validation is done in RunConfigurationPanel
        changeSupport.fireChange();
    }

    public void addRunAsScriptListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsScriptListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        protected final String getDefaultValue() {
            return RunAsScript.this.getDefaultValue(getPropName());
        }
    }

    public String getIndexFile() {
        return indexFileTextField.getText().trim();
    }

    public void setIndexFile(String indexFile) {
        indexFileTextField.setText(indexFile);
    }

    public void hideIndexFile() {
        indexFileLabel.setVisible(false);
        indexFileTextField.setVisible(false);
        indexFileBrowseButton.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        interpreterLabel = new JLabel();
        interpreterTextField = new JTextField();
        runAsLabel = new JLabel();
        runAsCombo = new JComboBox();
        configureButton = new JButton();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();

        setFocusTraversalPolicy(null);

        interpreterLabel.setLabelFor(interpreterTextField);

        Mnemonics.setLocalizedText(interpreterLabel, NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N
        interpreterTextField.setEditable(false);

        runAsLabel.setLabelFor(runAsCombo);

        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsScript.class, "LBL_RunAs"));
        Mnemonics.setLocalizedText(configureButton, NbBundle.getMessage(RunAsScript.class, "LBL_Configure"));
        configureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        indexFileLabel.setLabelFor(indexFileTextField);

        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsScript.class, "LBL_IndexFile")); // NOI18N
        indexFileTextField.setEditable(false);
        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsScript.class, "LBL_BrowseIndex"));
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(runAsLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(interpreterLabel)
                    .add(indexFileLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, runAsCombo, 0, 222, Short.MAX_VALUE)
                    .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(indexFileTextField, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                            .add(interpreterTextField, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, configureButton)
                            .add(GroupLayout.TRAILING, indexFileBrowseButton))))
                .add(0, 0, 0))
        
        );

        layout.linkSize(new Component[] {configureButton, indexFileBrowseButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(interpreterLabel)
                    .add(interpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(configureButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(indexFileLabel)
                    .add(indexFileBrowseButton)
                    .add(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        
        );

        interpreterLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleName")); // NOI18N
        interpreterLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterLabel.AccessibleContext.accessibleDescription")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleName")); // NOI18N
        interpreterTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.interpreterTextField.AccessibleContext.accessibleDescription")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleName")); // NOI18N
        runAsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.runAsCombo.AccessibleContext.accessibleDescription")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleName")); // NOI18N
        configureButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.configureButton.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsScript.class, "RunAsScript.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        Utils.browseFolderFile(sourcesFolderProvider.getSourcesFolder(), indexFileTextField);
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configureButton;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JLabel interpreterLabel;
    private JTextField interpreterTextField;
    private JComboBox runAsCombo;
    private JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables

    public String getPhpInterpreter() {
        return interpreterTextField.getText().trim();
    }
}
