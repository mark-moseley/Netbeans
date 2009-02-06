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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class ParserConfigurationPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private ParserConfigurationDescriptorPanel sourceFoldersDescriptorPanel;
    private boolean first = true;
    private boolean lastApplicable;

    public ParserConfigurationPanel(ParserConfigurationDescriptorPanel sourceFoldersDescriptorPanel) {
        initComponents();
        this.sourceFoldersDescriptorPanel = sourceFoldersDescriptorPanel;

        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("INCLUDE_LABEL_AD"));
        includeTextField.getAccessibleContext().setAccessibleDescription(getString("INCLUDE_LABEL_AD"));
        includeEditButton.getAccessibleContext().setAccessibleDescription(getString("INCLUDE_BROWSE_BUTTON_AD"));
        macroTextField.getAccessibleContext().setAccessibleDescription(getString("MACRO_LABEL_AD"));
        macroEditButton.getAccessibleContext().setAccessibleDescription(getString("MACRO_EDIT_BUTTON_AD"));
        configurationComboBox.addItem(new ConfigutationItem("project", getString("CONFIGURATION_LEVEL_project"))); // NOI18N
        configurationComboBox.addItem(new ConfigutationItem("folder", getString("CONFIGURATION_LEVEL_folder"))); // NOI18N
        configurationComboBox.addItem(new ConfigutationItem("file", getString("CONFIGURATION_LEVEL_file"))); // NOI18N
        configurationComboBox.setSelectedIndex(2);
        addListeners();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewMakeWizardP4"); // NOI18N
    }

    private boolean isApplicable(WizardDescriptor settings) {
        if (NewMakeProjectWizardIterator.USE_SIMPLE_IMPORT_PROJECT){
            librariesLabel.setVisible(false);
            librariesTextField.setVisible(false);
            additionalLibrariesButton.setVisible(false);
            automaticButton.setVisible(false);
            manualButton.setVisible(false);
            return true;
        } else {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                boolean res = extension.isApplicable(settings);
                String providerID = extension.getProviderID(settings);
                if ("dwarf-executable".equals(providerID)) { // NOI18N
                    additionalLibrariesButton.setVisible(true);
                    librariesLabel.setVisible(true);
                    librariesTextField.setVisible(true);
                } else { // NOI18N
                    additionalLibrariesButton.setVisible(false);
                    librariesLabel.setVisible(false);
                    librariesTextField.setVisible(false);
                }
                return res;
            }
            return false;
        }
    }

    private void addListeners() {
        DocumentListener documentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        librariesTextField.getDocument().addDocumentListener(documentListener);
    }

    private void update(DocumentEvent e) {
        sourceFoldersDescriptorPanel.stateChanged(null);
    }

    void read(WizardDescriptor settings) {
        lastApplicable = isApplicable(settings);
        if (!NewMakeProjectWizardIterator.USE_SIMPLE_IMPORT_PROJECT){
            if (lastApplicable) {
                manualButton.setEnabled(true);
                automaticButton.setEnabled(true);
                if (first) {
                    automaticButton.setSelected(true);
                    togglePanel(false);
                }
            } else {
                manualButton.setEnabled(true);
                automaticButton.setEnabled(true);
                manualButton.setSelected(true);
                togglePanel(true);
            }
            first = false;
        }
    }

    void store(WizardDescriptor wizardDescriptor) {
        if (!NewMakeProjectWizardIterator.USE_SIMPLE_IMPORT_PROJECT){
            if (manualButton.isSelected()) {
                wizardDescriptor.putProperty("includeTextField", includeTextField.getText()); // NOI18N
                wizardDescriptor.putProperty("macroTextField", macroTextField.getText()); // NOI18N
            } else {
                wizardDescriptor.putProperty("includeTextField", ""); // NOI18N
                wizardDescriptor.putProperty("macroTextField", ""); // NOI18N
            }
            if (automaticButton.isSelected()) {
                ConfigutationItem level = (ConfigutationItem) configurationComboBox.getSelectedItem();
                wizardDescriptor.putProperty("consolidationLevel", level.getID()); // NOI18N
                wizardDescriptor.putProperty("additionalLibraries", librariesTextField.getText()); // NOI18N
            } else {
                wizardDescriptor.putProperty("consolidationLevel", ""); // NOI18N
                wizardDescriptor.putProperty("additionalLibraries", ""); // NOI18N
            }
        } else {
            wizardDescriptor.putProperty("includeTextField", includeTextField.getText()); // NOI18N
            wizardDescriptor.putProperty("macroTextField", macroTextField.getText()); // NOI18N
            ConfigutationItem level = (ConfigutationItem) configurationComboBox.getSelectedItem();
            wizardDescriptor.putProperty("consolidationLevel", level.getID()); // NOI18N
        }
    }

    boolean valid(WizardDescriptor settings) {
        if (!NewMakeProjectWizardIterator.USE_SIMPLE_IMPORT_PROJECT){
            if (automaticButton.isSelected()) {
                if (!lastApplicable) {
                    String selectedExecutable = (String) settings.getProperty("outputTextField"); // NOI18N
                    if (selectedExecutable == null || selectedExecutable.length() == 0) {
                        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("Automatic.Error.NoOutputResult")); // NOI18N
                        return false;
                    }
                    File file = new File(selectedExecutable);
                    if (!file.exists()) {
                        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("Automatic.Error.OutputResultNotExist")); // NOI18N
                        return false;
                    }
                    settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("Automatic.Error.NoDebugOutputResult")); // NOI18N
                    return false;
                }
                StringTokenizer st = new StringTokenizer(librariesTextField.getText(), ";"); // NOI18N
                while (st.hasMoreTokens()) {
                    String path = st.nextToken();
                    File file = new File(path);
                    if (!(file.exists() && file.isFile())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        codeModelPanel = new javax.swing.JPanel();
        includeLabel = new javax.swing.JLabel();
        includeTextField = new javax.swing.JTextField();
        includeEditButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        macroTextField = new javax.swing.JTextField();
        codeModelLabel = new javax.swing.JLabel();
        macroEditButton = new javax.swing.JButton();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        manualButton = new javax.swing.JRadioButton();
        automaticButton = new javax.swing.JRadioButton();
        discoveryPanel = new javax.swing.JPanel();
        configurationComboBox = new javax.swing.JComboBox();
        configurationLabel = new javax.swing.JLabel();
        librariesLabel = new javax.swing.JLabel();
        librariesTextField = new javax.swing.JTextField();
        additionalLibrariesButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(300, 158));
        setPreferredSize(new java.awt.Dimension(323, 223));
        setLayout(new java.awt.GridBagLayout());

        codeModelPanel.setLayout(new java.awt.GridBagLayout());

        includeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("INCLUDE_LABEL_MN").charAt(0));
        includeLabel.setLabelFor(includeTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        includeLabel.setText(bundle.getString("INCLUDE_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        codeModelPanel.add(includeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        codeModelPanel.add(includeTextField, gridBagConstraints);

        includeEditButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("INCLUDE_BROWSE_BUTTON_MN").charAt(0));
        includeEditButton.setText(bundle.getString("INCLUDE_BROWSE_BUTTON_TXT")); // NOI18N
        includeEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeEditButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        codeModelPanel.add(includeEditButton, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MACRO_LABEL_MN").charAt(0));
        jLabel2.setLabelFor(macroTextField);
        jLabel2.setText(bundle.getString("MACRO_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        codeModelPanel.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        codeModelPanel.add(macroTextField, gridBagConstraints);

        codeModelLabel.setText(bundle.getString("CODEMODEL_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        codeModelPanel.add(codeModelLabel, gridBagConstraints);

        macroEditButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MACRO_EDIT_BUTTON_MN").charAt(0));
        macroEditButton.setText(bundle.getString("MACRO_EDIT_BUTTON_TXT")); // NOI18N
        macroEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                macroEditButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        codeModelPanel.add(macroEditButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(codeModelPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(bundle.getString("SourceFoldersInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "INFO_AREA_AN")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "INFO_AREA_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        buttonGroup1.add(manualButton);
        manualButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("ParserManualConfiguration_MN").charAt(0));
        manualButton.setText(bundle.getString("ParserManualConfiguration")); // NOI18N
        manualButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        manualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(manualButton, gridBagConstraints);
        manualButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "ParserManualConfiguration_AD")); // NOI18N

        buttonGroup1.add(automaticButton);
        automaticButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("ParserAutomaticConfiguration_MN").charAt(0));
        automaticButton.setText(bundle.getString("ParserAutomaticConfiguration")); // NOI18N
        automaticButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        automaticButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                automaticButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(automaticButton, gridBagConstraints);
        automaticButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "ParserAutomaticConfiguration_AD")); // NOI18N

        discoveryPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(configurationComboBox, gridBagConstraints);
        configurationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParserConfigurationPanel.class, "ConfigurationCombobox_AD")); // NOI18N

        configurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("ConfigurationLevelLabel_MN").charAt(0));
        configurationLabel.setLabelFor(configurationComboBox);
        configurationLabel.setText(bundle.getString("ConfigurationLevelLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        discoveryPanel.add(configurationLabel, gridBagConstraints);

        librariesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("AdditionalLibrariesLabel_MN").charAt(0));
        librariesLabel.setLabelFor(librariesTextField);
        librariesLabel.setText(bundle.getString("AdditionalLibrariesLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        discoveryPanel.add(librariesLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(librariesTextField, gridBagConstraints);

        additionalLibrariesButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("LIBRARY_EDIT_BUTTON_MN").charAt(0));
        additionalLibrariesButton.setText(bundle.getString("LIBRARY_EDIT_BUTTON_TXT")); // NOI18N
        additionalLibrariesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                additionalLibrariesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(additionalLibrariesButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(discoveryPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void automaticButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticButtonActionPerformed
        togglePanel(false);
        update((DocumentEvent) null);
    }//GEN-LAST:event_automaticButtonActionPerformed

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualButtonActionPerformed
        togglePanel(true);
        update((DocumentEvent) null);
    }//GEN-LAST:event_manualButtonActionPerformed

    private void togglePanel(boolean manual) {
        for (Component component : codeModelPanel.getComponents()) {
            component.setEnabled(manual);
        }
        for (Component component : discoveryPanel.getComponents()) {
            component.setEnabled(!manual);
        }
        if (manual) {
            instructionsTextArea.setText(getString("SourceFoldersInstructions")); // NOI18N
        } else {
            instructionsTextArea.setText(getString("DiscoveryInstructions")); // NOI18N
        }
    }

    private void additionalLibrariesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_additionalLibrariesButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(librariesTextField.getText(), ";"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        AdditionalLibrariesListPanel panel = new AdditionalLibrariesListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addOuterPanel(panel), getString("ADDITIONAL_LIBRARIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Vector newList = panel.getListData();
            StringBuilder includes = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    includes.append(";"); // NOI18N
                }
                includes.append(newList.elementAt(i));
            }
            librariesTextField.setText(includes.toString());
        }
    }//GEN-LAST:event_additionalLibrariesButtonActionPerformed

    private void macroEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_macroEditButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(macroTextField.getText(), "; "); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken().trim());
        }
        MacrosListPanel panel = new MacrosListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addOuterPanel(panel), "Macro Definitions"); // NOI18N
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Vector newList = panel.getListData();
            StringBuilder macros = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    macros.append(";"); // NOI18N
                }
                macros.append(newList.elementAt(i));
            }
            macroTextField.setText(macros.toString());
        }
    }//GEN-LAST:event_macroEditButtonActionPerformed

    private void includeEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeEditButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(includeTextField.getText(), ";"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        IncludesListPanel panel = new IncludesListPanel(list);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(addOuterPanel(panel), getString("INCLUDE_DIRIRECTORIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Vector newList = panel.getListData();
            StringBuilder includes = new StringBuilder();
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0) {
                    includes.append(";"); // NOI18N
                }
                includes.append(newList.elementAt(i));
            }
            includeTextField.setText(includes.toString());
        }
    }//GEN-LAST:event_includeEditButtonActionPerformed

    private JPanel addOuterPanel(JPanel innerPanel) {
        JPanel outerPanel = new JPanel();
        outerPanel.getAccessibleContext().setAccessibleDescription(getString("DIALOG_AD"));
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(innerPanel, gridBagConstraints);
        outerPanel.setPreferredSize(new Dimension(500, 250));
        return outerPanel;
    }

    private static class AdditionalLibrariesListPanel extends ListEditorPanel<String> {

        public AdditionalLibrariesListPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            getCopyButton().setVisible(false);
        }

        @Override
        public String addAction() {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = System.getProperty("user.home"); // NOI18N
            }
            FileFilter[] filters;
            if (Utilities.isWindows()) {
                filters = new FileFilter[]{PeExecutableFileFilter.getInstance(),
                            ElfStaticLibraryFileFilter.getInstance(),
                            PeDynamicLibraryFileFilter.getInstance()};
            } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                filters = new FileFilter[]{MacOSXExecutableFileFilter.getInstance(),
                            ElfStaticLibraryFileFilter.getInstance(),
                            MacOSXDynamicLibraryFileFilter.getInstance()};
            } else {
                filters = new FileFilter[]{ElfExecutableFileFilter.getInstance(),
                            ElfStaticLibraryFileFilter.getInstance(),
                            ElfDynamicLibraryFileFilter.getInstance()};
            }
            FileChooser fileChooser = new FileChooser(
                    getString("LIBRARY_CHOOSER_TITLE_TXT"),
                    getString("LIBRARY_CHOOSER_BUTTON_TXT"),
                    JFileChooser.FILES_ONLY,
                    filters,
                    seed,
                    false);
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            itemPath = FilePathAdaptor.normalize(itemPath);
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("LIBRARY_LIST_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("LIBRARY_LIST_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            Vector<String> vector = getListData();
            Object[] arr = getListData().toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }
    }

    private static class IncludesListPanel extends ListEditorPanel<String> {

        public IncludesListPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = System.getProperty("user.home"); // NOI18N
            }
            FileChooser fileChooser = new FileChooser(getString("INCLUDE_DIR_DIALOG_TITLE_TXT"), getString("INCLUDE_DIR_DIALOG_BUTTON_TXT"), JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            itemPath = FilePathAdaptor.normalize(itemPath);
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("DIR_LIST_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("DIR_LIST_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return new String(o);
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            Vector<String> vector = getListData();
            Object[] arr = getListData().toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }
    }

    private static class MacrosListPanel extends ListEditorPanel<String> {

        public MacrosListPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("ADD_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            String newS = notifyDescriptor.getInputText();
            return newS;
        }

        @Override
        public String getListLabelText() {
            return getString("MACROS_LIST_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("MACROS_LIST_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            Vector<String> vector = getListData();
            Object[] arr = getListData().toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }
    }

    private static class ConfigutationItem {

        private String ID;
        private String name;

        private ConfigutationItem(String ID, String name) {
            this.ID = ID;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getID() {
            return ID;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton additionalLibrariesButton;
    private javax.swing.JRadioButton automaticButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel codeModelLabel;
    private javax.swing.JPanel codeModelPanel;
    private javax.swing.JComboBox configurationComboBox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel discoveryPanel;
    private javax.swing.JButton includeEditButton;
    private javax.swing.JLabel includeLabel;
    private javax.swing.JTextField includeTextField;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JTextField librariesTextField;
    private javax.swing.JButton macroEditButton;
    private javax.swing.JTextField macroTextField;
    private javax.swing.JRadioButton manualButton;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getBundle(PanelProjectLocationVisual.class).getString(s);
    }
}
