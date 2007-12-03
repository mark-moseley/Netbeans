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
package org.netbeans.modules.beans.addproperty;

import java.awt.Rectangle;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A simple GUI for Add Property action.
 *
 * @author  Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class AddPropertyPanel extends javax.swing.JPanel {

    private boolean propNameModified = false;
    private boolean generatePCSModified = false;
    private boolean generateVCSModified = false;
    
    private DocumentListener propNameTextFieldDocumentListener;
    private FileObject file;
    private List<String> existingFields;
    private String[] pcsName;
    private String[] vcsName;
    private JButton okButton;
    
    public AddPropertyPanel(FileObject file, List<String> existingFields, String[] pcsName, String[] vcsName, JButton okButton) {
        this.file = file;
        this.existingFields = existingFields;
        this.pcsName = pcsName;
        this.vcsName = vcsName;
        this.okButton = okButton;
        initComponents();
        previewScrollPane.putClientProperty(
                "HighlightsLayerExcludes", // NOI18N
                "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
                );

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                showPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                showPreview();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        propNameTextFieldDocumentListener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                propNameModified = true;
                showPreview();
            }

            public void removeUpdate(DocumentEvent e) {
                propNameModified = true;
                showPreview();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        propNameTextField.getDocument().addDocumentListener(propNameTextFieldDocumentListener);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        previewEditorPane.setText("");
        generatePropertyChangeSupportCheckBox.setSelected(false);
        generateVetoablePropertyChangeSupportCheckBox.setSelected(false);
        showPreview();
    }

    private void showPreview() {
        if (!propNameModified) {
            propNameTextField.getDocument().removeDocumentListener(propNameTextFieldDocumentListener);
            propNameTextField.setText("PROP_" + nameTextField.getText().toUpperCase()); // NOI18N
            propNameTextField.getDocument().addDocumentListener(propNameTextFieldDocumentListener);
        }
        
        int i = staticCheckBox.isSelected() ? 1 : 0;
        
        generatePropertyChangeSupportCheckBox.setEnabled(pcsName[i] == null);
        generateVetoablePropertyChangeSupportCheckBox.setEnabled(vcsName[i] == null);
        
        if (staticCheckBox.isSelected()) {
            generatePropertyChangeSupportCheckBox.setSelected(false);
            generateVetoablePropertyChangeSupportCheckBox.setSelected(false);
            generatePropertyChangeSupportCheckBox.setEnabled(false);
            generateVetoablePropertyChangeSupportCheckBox.setEnabled(false);
        } else {
            if (!generatePCSModified && pcsName == null) {
                generatePropertyChangeSupportCheckBox.setEnabled(true);
                generatePropertyChangeSupportCheckBox.setSelected(boundCheckBox.isSelected());
            }

            if (!generateVCSModified && vcsName == null) {
                generateVetoablePropertyChangeSupportCheckBox.setEnabled(true);
                generateVetoablePropertyChangeSupportCheckBox.setSelected(vetoableCheckBox.isSelected());
            }
        }
        
        final String previewTemplate = new AddPropertyGenerator().generate(getAddPropertyConfig());
        previewEditorPane.setText(previewTemplate);
        
        String error = resolveError();
        
        if (error != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/error-glyph.gif"))); // NOI18N
            errorLabel.setText(error);
        }
        
        okButton.setEnabled(error == null);
        
        String warning = resolveWarning();
        
        if (warning != null) {
            errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/warning.gif"))); // NOI18N
            errorLabel.setText(warning);
        }
        
        errorLabel.setVisible(error != null || warning != null);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                previewEditorPane.setCaretPosition(0);
                previewEditorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });
    }
    
    private String resolveError() {
        if (nameTextField.getText().length() == 0) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldIsEmpty");
        }
        
        if (existingFields.contains(nameTextField.getText())) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(nameTextField.getText())});
        }
        
        if (boundCheckBox.isSelected() && existingFields.contains(propNameTextField.getText())) {
            return NbBundle.getMessage(AddPropertyPanel.class, "ERR_FieldAlreadyExists", new Object[]{String.valueOf(propNameTextField.getText())});
        }
        
        return null;
    }

    private String resolveWarning() {
        if (staticCheckBox.isSelected() && boundCheckBox.isSelected() && pcsName[1] == null) {
            return NbBundle.getMessage(AddPropertyPanel.class, "WARN_CannotPCS");
        }
        
        if (staticCheckBox.isSelected() && boundCheckBox.isSelected() && vetoableCheckBox.isSelected() && vcsName[1] == null) {
            return NbBundle.getMessage(AddPropertyPanel.class, "WARN_CannotVCS");
        }
        
        return null;
    }
    
    public AddPropertyConfig getAddPropertyConfig() {
        final String type = typeComboBox.getSelectedItem().toString().trim();
        final String name = nameTextField.getText().trim();
        AddPropertyConfig.ACCESS access = AddPropertyConfig.ACCESS.PACKAGE;
        if (privateRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PRIVATE;
        } else if (protectedRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PROTECTED;
        } else if (publicRadioButton.isSelected()) {
            access = AddPropertyConfig.ACCESS.PUBLIC;
        }

        AddPropertyConfig.GENERATE generate = AddPropertyConfig.GENERATE.GETTER_AND_SETTER;
        if (generateGetterAndSetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.GETTER_AND_SETTER;
        } else if (generateGetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.GETTER;
        } else if (generateSetterRadioButton.isSelected()) {
            generate = AddPropertyConfig.GENERATE.SETTER;
        }

        int i = staticCheckBox.isSelected() ? 1 : 0;
        AddPropertyConfig addPropertyConfig = new AddPropertyConfig(
                name, type, access, staticCheckBox.isSelected(), finalCheckBox.isSelected(), generate, generateJavadocCheckBox.isSelected(), boundCheckBox.isSelected(), propNameTextField.getText().trim(), vetoableCheckBox.isSelected(), indexedCheckBox.isSelected(), pcsName[i], vcsName[i], generatePropertyChangeSupportCheckBox.isSelected(), generateVetoablePropertyChangeSupportCheckBox.isSelected());
        return addPropertyConfig;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accessGroup = new javax.swing.ButtonGroup();
        getterSetterGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        browseTypeButton = new javax.swing.JButton();
        privateRadioButton = new javax.swing.JRadioButton();
        packageRadioButton = new javax.swing.JRadioButton();
        protectedRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        staticCheckBox = new javax.swing.JCheckBox();
        finalCheckBox = new javax.swing.JCheckBox();
        generateGetterAndSetterRadioButton = new javax.swing.JRadioButton();
        generateGetterRadioButton = new javax.swing.JRadioButton();
        generateSetterRadioButton = new javax.swing.JRadioButton();
        generateJavadocCheckBox = new javax.swing.JCheckBox();
        boundCheckBox = new javax.swing.JCheckBox();
        propNameTextField = new javax.swing.JTextField();
        vetoableCheckBox = new javax.swing.JCheckBox();
        indexedCheckBox = new javax.swing.JCheckBox();
        generatePropertyChangeSupportCheckBox = new javax.swing.JCheckBox();
        generateVetoablePropertyChangeSupportCheckBox = new javax.swing.JCheckBox();
        previewLabel = new javax.swing.JLabel();
        previewScrollPane = new javax.swing.JScrollPane();
        previewEditorPane = new javax.swing.JEditorPane();
        errorLabel = new javax.swing.JLabel();

        nameLabel.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.nameTextField.text")); // NOI18N

        typeLabel.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.typeLabel.text")); // NOI18N

        typeComboBox.setEditable(true);
        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "String", "int", "boolean", "long", "double", "long", "char", "short", "float" }));
        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        browseTypeButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.browseTypeButton.text")); // NOI18N
        browseTypeButton.setToolTipText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.browseTypeButton.toolTipText")); // NOI18N
        browseTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTypeButtonActionPerformed(evt);
            }
        });

        accessGroup.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.privateRadioButton.text")); // NOI18N
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(packageRadioButton);
        packageRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.packageRadioButton.text")); // NOI18N
        packageRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(protectedRadioButton);
        protectedRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.protectedRadioButton.text")); // NOI18N
        protectedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protectedRadioButtonActionPerformed(evt);
            }
        });

        accessGroup.add(publicRadioButton);
        publicRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.publicRadioButton.text")); // NOI18N
        publicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioButtonActionPerformed(evt);
            }
        });

        staticCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.staticCheckBox.text")); // NOI18N
        staticCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticCheckBoxActionPerformed(evt);
            }
        });

        finalCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.finalCheckBox.text")); // NOI18N
        finalCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalCheckBoxActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateGetterAndSetterRadioButton);
        generateGetterAndSetterRadioButton.setSelected(true);
        generateGetterAndSetterRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateGetterAndSetterRadioButton.text")); // NOI18N
        generateGetterAndSetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateGetterAndSetterRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateGetterRadioButton);
        generateGetterRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateGetterRadioButton.text")); // NOI18N
        generateGetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateGetterRadioButtonActionPerformed(evt);
            }
        });

        getterSetterGroup.add(generateSetterRadioButton);
        generateSetterRadioButton.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateSetterRadioButton.text")); // NOI18N
        generateSetterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSetterRadioButtonActionPerformed(evt);
            }
        });

        generateJavadocCheckBox.setSelected(true);
        generateJavadocCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateJavadocCheckBox.text")); // NOI18N
        generateJavadocCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateJavadocCheckBoxActionPerformed(evt);
            }
        });

        boundCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.boundCheckBox.text")); // NOI18N
        boundCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundCheckBoxActionPerformed(evt);
            }
        });

        propNameTextField.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.propNameTextField.text")); // NOI18N

        vetoableCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.vetoableCheckBox.text")); // NOI18N
        vetoableCheckBox.setEnabled(false);
        vetoableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vetoableCheckBoxActionPerformed(evt);
            }
        });

        indexedCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.indexedCheckBox.text")); // NOI18N
        indexedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexedCheckBoxActionPerformed(evt);
            }
        });

        generatePropertyChangeSupportCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generatePropertyChangeSupportCheckBox.text")); // NOI18N
        generatePropertyChangeSupportCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatePropertyChangeSupportCheckBoxActionPerformed(evt);
            }
        });

        generateVetoablePropertyChangeSupportCheckBox.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.generateVetoablePropertyChangeSupportCheckBox.text")); // NOI18N
        generateVetoablePropertyChangeSupportCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateVetoablePropertyChangeSupportCheckBoxActionPerformed(evt);
            }
        });

        previewLabel.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewLabel.text")); // NOI18N

        previewEditorPane.setContentType(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.contentType")); // NOI18N
        previewEditorPane.setEditable(false);
        previewEditorPane.setText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.text")); // NOI18N
        previewEditorPane.setToolTipText(org.openide.util.NbBundle.getMessage(AddPropertyPanel.class, "AddPropertyPanel.previewEditorPane.toolTipText")); // NOI18N
        previewScrollPane.setViewportView(previewEditorPane);

        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/beans/resources/error-glyph.gif"))); // NOI18N
        errorLabel.setText(org.openide.util.NbBundle.getBundle(AddPropertyPanel.class).getString("AddPropertyPanel.errorLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(typeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(staticCheckBox)
                                    .add(privateRadioButton))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(finalCheckBox)
                                    .add(layout.createSequentialGroup()
                                        .add(packageRadioButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(protectedRadioButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(publicRadioButton))))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(boundCheckBox)
                                        .add(35, 35, 35)
                                        .add(propNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                                        .add(27, 27, 27))
                                    .add(layout.createSequentialGroup()
                                        .add(21, 21, 21)
                                        .add(vetoableCheckBox))
                                    .add(layout.createSequentialGroup()
                                        .add(generateGetterAndSetterRadioButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(generateGetterRadioButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(generateSetterRadioButton))
                                    .add(generateJavadocCheckBox)
                                    .add(indexedCheckBox)
                                    .add(layout.createSequentialGroup()
                                        .add(generatePropertyChangeSupportCheckBox)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(generateVetoablePropertyChangeSupportCheckBox))))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(typeComboBox, 0, 0, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseTypeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(errorLabel)
                            .add(previewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {finalCheckBox, packageRadioButton, privateRadioButton, protectedRadioButton, publicRadioButton, staticCheckBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {nameLabel, previewLabel, typeLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabel)
                    .add(browseTypeButton)
                    .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(packageRadioButton)
                            .add(protectedRadioButton)
                            .add(publicRadioButton)
                            .add(privateRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(staticCheckBox))
                    .add(finalCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generateGetterAndSetterRadioButton)
                    .add(generateSetterRadioButton)
                    .add(generateGetterRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(generateJavadocCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(boundCheckBox)
                    .add(propNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(vetoableCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(indexedCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generatePropertyChangeSupportCheckBox)
                    .add(generateVetoablePropertyChangeSupportCheckBox))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(previewLabel)
                    .add(previewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorLabel)
                .add(17, 17, 17))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void finalCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalCheckBoxActionPerformed
        generateGetterAndSetterRadioButton.setEnabled(!finalCheckBox.isSelected());
        generateSetterRadioButton.setEnabled(!finalCheckBox.isSelected());
        if (finalCheckBox.isSelected()) {
            generateGetterRadioButton.setSelected(true);
        }
        showPreview();
    }//GEN-LAST:event_finalCheckBoxActionPerformed

    private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_privateRadioButtonActionPerformed

    private void packageRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_packageRadioButtonActionPerformed

    private void protectedRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protectedRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_protectedRadioButtonActionPerformed

    private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_publicRadioButtonActionPerformed

    private void staticCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_staticCheckBoxActionPerformed

    private void indexedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexedCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_indexedCheckBoxActionPerformed

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void generateJavadocCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateJavadocCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_generateJavadocCheckBoxActionPerformed

    private void boundCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundCheckBoxActionPerformed
        vetoableCheckBox.setEnabled(boundCheckBox.isSelected());
        showPreview();
    }//GEN-LAST:event_boundCheckBoxActionPerformed

    private void vetoableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vetoableCheckBoxActionPerformed
        showPreview();
    }//GEN-LAST:event_vetoableCheckBoxActionPerformed

    private void generateGetterAndSetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateGetterAndSetterRadioButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_generateGetterAndSetterRadioButtonActionPerformed

    private void generateSetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSetterRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_generateSetterRadioButtonActionPerformed

    private void generateGetterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateGetterRadioButtonActionPerformed
        showPreview();
}//GEN-LAST:event_generateGetterRadioButtonActionPerformed

    private void generateVetoablePropertyChangeSupportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateVetoablePropertyChangeSupportCheckBoxActionPerformed
        generateVCSModified = true;
        showPreview();
    }//GEN-LAST:event_generateVetoablePropertyChangeSupportCheckBoxActionPerformed

    private void generatePropertyChangeSupportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatePropertyChangeSupportCheckBoxActionPerformed
        generatePCSModified = true;
        showPreview();
    }//GEN-LAST:event_generatePropertyChangeSupportCheckBoxActionPerformed

    private void browseTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseTypeButtonActionPerformed
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(file), null);

        if (type != null) {
            String fqn = type.getQualifiedName().toString();

            typeComboBox.setSelectedItem(fqn);
        }
    }//GEN-LAST:event_browseTypeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JCheckBox boundCheckBox;
    private javax.swing.JButton browseTypeButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JCheckBox finalCheckBox;
    private javax.swing.JRadioButton generateGetterAndSetterRadioButton;
    private javax.swing.JRadioButton generateGetterRadioButton;
    private javax.swing.JCheckBox generateJavadocCheckBox;
    private javax.swing.JCheckBox generatePropertyChangeSupportCheckBox;
    private javax.swing.JRadioButton generateSetterRadioButton;
    private javax.swing.JCheckBox generateVetoablePropertyChangeSupportCheckBox;
    private javax.swing.ButtonGroup getterSetterGroup;
    private javax.swing.JCheckBox indexedCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton packageRadioButton;
    private javax.swing.JEditorPane previewEditorPane;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JScrollPane previewScrollPane;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JTextField propNameTextField;
    private javax.swing.JRadioButton protectedRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JCheckBox staticCheckBox;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JCheckBox vetoableCheckBox;
    // End of variables declaration//GEN-END:variables
}
