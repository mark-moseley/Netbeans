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

/*
 * VariableFormatterEditPanel.java
 *
 * Created on Apr 3, 2009, 10:20:57 AM
 */

package org.netbeans.modules.debugger.jpda.ui.options;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.debugger.jpda.ui.VariablesFormatter;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public class VariableFormatterEditPanel extends javax.swing.JPanel {

    /** Creates new form VariableFormatterEditPanel */
    public VariableFormatterEditPanel() {
        initComponents();
        initChildrenVariablesTable();
    }

    public void load(VariablesFormatter f) {
        nameTextField.setText(f.getName());
        classTypesTextField.setText(f.getClassTypesCommaSeparated());
        subtypesCheckBox.setSelected(f.isIncludeSubTypes());
        valueFormatCheckBox.setSelected(f.getValueFormatCode().trim().length() > 0);
        valueEditorPane.setText(f.getValueFormatCode());
        childrenFormatCheckBox.setSelected(f.getChildrenFormatCode().trim().length() > 0 ||
                                           f.getChildrenVariables().size() > 0);
        childrenCodeEditorPane.setText(f.getChildrenFormatCode());
        Map<String, String> childrenVariables = f.getChildrenVariables();
        int n = childrenVariables.size();
        Iterator<Map.Entry<String, String>> childrenVariablesEntries = childrenVariables.entrySet().iterator();
        String[][] tableData = new String[n][2];
        for (int i = 0; i < n; i++) {
            Map.Entry<String, String> e = childrenVariablesEntries.next();
            tableData[i][0] = e.getKey();
            tableData[i][1] = e.getValue();
        }
        DefaultTableModel childrenVarsModel = new DefaultTableModel(tableData, tableColumnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        childrenVariablesTable.setModel(childrenVarsModel);
        DisablingCellRenderer.apply(childrenVariablesTable);
        childrenCodeRadioButton.setSelected(!f.isUseChildrenVariables());
        childrenVariablesRadioButton.setSelected(f.isUseChildrenVariables());
        testChildrenCheckBox.setSelected(f.getChildrenExpandTestCode().trim().length() > 0);
        testChildrenEditorPane.setText(f.getChildrenExpandTestCode());
        valueFormatCheckBoxActionPerformed(null);
        childrenFormatCheckBoxActionPerformed(null);
        nameTextField.requestFocusInWindow();
    }

    public void store(VariablesFormatter f) {
        f.setName(nameTextField.getText());
        f.setClassTypes(classTypesTextField.getText());
        f.setIncludeSubTypes(subtypesCheckBox.isSelected());
        f.setValueFormatCode(valueFormatCheckBox.isSelected() ? valueEditorPane.getText() : "");
        f.setChildrenFormatCode(childrenCodeEditorPane.getText());
        TableModel tableModel = childrenVariablesTable.getModel();
        f.getChildrenVariables().clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            f.addChildrenVariable((String) tableModel.getValueAt(i, 0), (String) tableModel.getValueAt(i, 1));
        }
        f.setUseChildrenVariables(childrenVariablesRadioButton.isSelected());
        f.setChildrenExpandTestCode(testChildrenCheckBox.isSelected() ? testChildrenEditorPane.getText() : "");
    }

    void setFormatterNames(Set<String> formatterNames) {
        this.formatterNames = formatterNames;
    }

    void setValidityObjects(DialogDescriptor validityDescriptor,
                            NotificationLineSupport validityNotificationSupport,
                            boolean continualValidityChecks) {
        this.validityDescriptor = validityDescriptor;
        this.validityNotificationSupport = validityNotificationSupport;
        this.continualValidityChecks = continualValidityChecks;
        attachValidityChecks();
    }

    private void attachValidityChecks() {
        DocumentListener validityDocumentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkValid();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValid();
            }
            public void changedUpdate(DocumentEvent e) {
                checkValid();
            }
        };
        nameTextField.getDocument().addDocumentListener(validityDocumentListener);
        classTypesTextField.getDocument().addDocumentListener(validityDocumentListener);
        checkValid();
    }

    private boolean checkValidName() {
        String name = nameTextField.getText().trim();
        if (name.length() == 0) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_EmptyFormatterName"));
            return false;
        } else if (formatterNames.contains(name)) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_ExistingFormatterName"));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkValidClasses() {
        String name = classTypesTextField.getText().trim();
        if (name.length() == 0) {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_EmptyClassName"));
            return false;
        } else {
            int i = 0;
            char c = name.charAt(i);
            if (Character.isJavaIdentifierStart(c)) {
                boolean start = true;
                for (i++; i < name.length(); i++) {
                    c = name.charAt(i);
                    if (c == ',' || Character.isWhitespace(c)) {
                        start = true;
                        continue;
                    }
                    if (start && !Character.isJavaIdentifierStart(c) || !start && !Character.isJavaIdentifierPart(c) && c != '.') {
                        break;
                    }
                    start = false;
                }
            }
            if (i < name.length()) {
                validityDescriptor.setValid(false);
                validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_InvalidClassNameAtPos", (i+1)));
                return false;
            }
            return true;
        }
    }

    private void checkFormatterSelected() {
        boolean is = valueFormatCheckBox.isSelected() || childrenFormatCheckBox.isSelected();
        if (is) {
            validityDescriptor.setValid(true);
            validityNotificationSupport.clearMessages();
        } else {
            validityDescriptor.setValid(false);
            validityNotificationSupport.setErrorMessage(NbBundle.getMessage(VariableFormatterEditPanel.class, "MSG_NoFormatSelected"));
        }
    }

    private void checkValid() {
        if (validityNotificationSupport == null || !continualValidityChecks) {
            return ;
        }
        if (checkValidName() && checkValidClasses()) {
            checkFormatterSelected();
        }
    }

    public boolean checkValidInput() {
        continualValidityChecks = true;
        checkValid();
        if (!validityDescriptor.isValid()) {
            if (!checkValidName()) {
                nameTextField.requestFocusInWindow();
            } else if (!checkValidClasses()) {
                classTypesTextField.requestFocusInWindow();
            } else {
                valueFormatCheckBox.requestFocusInWindow();
            }
            return false;
        } else {
            return true;
        }
    }

    static Color getDisabledFieldBackground() {
        JTextField disabledField = new JTextField();
        disabledField.setEditable(false);
        disabledField.setEnabled(false);
        return disabledField.getBackground();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        childrenButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        classTypesLabel = new javax.swing.JLabel();
        classTypesTextField = new javax.swing.JTextField();
        subtypesCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        valueFormatCheckBox = new javax.swing.JCheckBox();
        valueScrollPane = new javax.swing.JScrollPane();
        valueEditorPane = new javax.swing.JEditorPane();
        childrenFormatCheckBox = new javax.swing.JCheckBox();
        childrenCodeRadioButton = new javax.swing.JRadioButton();
        childrenCodeScrollPane = new javax.swing.JScrollPane();
        childrenCodeEditorPane = new javax.swing.JEditorPane();
        childrenVariablesRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        childrenVariablesTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        addVarButton = new javax.swing.JButton();
        removeVarButton = new javax.swing.JButton();
        moveUpVarButton = new javax.swing.JButton();
        moveDownVarButton = new javax.swing.JButton();
        testChildrenCheckBox = new javax.swing.JCheckBox();
        testChildrenScrollPane = new javax.swing.JScrollPane();
        testChildrenEditorPane = new javax.swing.JEditorPane();

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.nameTextField.text")); // NOI18N

        classTypesLabel.setLabelFor(classTypesTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classTypesLabel, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesLabel.text")); // NOI18N

        classTypesTextField.setText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesTextField.text")); // NOI18N
        classTypesTextField.setToolTipText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.classTypesLabel.tooltip")); // NOI18N

        subtypesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(subtypesCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.subtypesCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classTypesLabel)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(classTypesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(subtypesCheckBox))
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classTypesLabel)
                    .add(classTypesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(subtypesCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(valueFormatCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.valueFormatCheckBox.text")); // NOI18N
        valueFormatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFormatCheckBoxActionPerformed(evt);
            }
        });

        valueScrollPane.setViewportView(valueEditorPane);

        org.openide.awt.Mnemonics.setLocalizedText(childrenFormatCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenFormatCheckBox.text")); // NOI18N
        childrenFormatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenFormatCheckBoxActionPerformed(evt);
            }
        });

        childrenButtonGroup.add(childrenCodeRadioButton);
        childrenCodeRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(childrenCodeRadioButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenCodeRadioButton.text")); // NOI18N
        childrenCodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenCodeRadioButtonActionPerformed(evt);
            }
        });

        childrenCodeScrollPane.setViewportView(childrenCodeEditorPane);

        childrenButtonGroup.add(childrenVariablesRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(childrenVariablesRadioButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesRadioButton.text")); // NOI18N
        childrenVariablesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenVariablesRadioButtonActionPerformed(evt);
            }
        });

        childrenVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(childrenVariablesTable);
        childrenVariablesTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesTable.a11y.name")); // NOI18N
        childrenVariablesTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.childrenVariablesTable.a11y.description")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.addVarButton.text")); // NOI18N
        addVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.removeVarButton.text")); // NOI18N
        removeVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveUpVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveUpVarButton.text")); // NOI18N
        moveUpVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpVarButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(moveDownVarButton, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.moveDownVarButton.text")); // NOI18N
        moveDownVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownVarButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, addVarButton)
            .add(removeVarButton)
            .add(moveUpVarButton)
            .add(moveDownVarButton)
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {addVarButton, moveDownVarButton, moveUpVarButton, removeVarButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(addVarButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeVarButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(moveUpVarButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(moveDownVarButton))
        );

        org.openide.awt.Mnemonics.setLocalizedText(testChildrenCheckBox, org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.testChildrenCheckBox.text")); // NOI18N
        testChildrenCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(VariableFormatterEditPanel.class, "VariableFormatterEditPanel.testChildrenCheckBox.tooltip")); // NOI18N
        testChildrenCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testChildrenCheckBoxActionPerformed(evt);
            }
        });

        testChildrenScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        testChildrenScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        testChildrenScrollPane.setViewportView(testChildrenEditorPane);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(valueFormatCheckBox)
                .addContainerGap(235, Short.MAX_VALUE))
            .add(jPanel2Layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(valueScrollPane))
            .add(jPanel2Layout.createSequentialGroup()
                .add(childrenFormatCheckBox)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(42, 42, 42)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
            .add(jPanel2Layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(testChildrenScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(testChildrenCheckBox)
                        .addContainerGap())
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(childrenVariablesRadioButton)
                        .addContainerGap())
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(childrenCodeScrollPane))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(childrenCodeRadioButton)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(valueFormatCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(valueScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(childrenFormatCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(childrenCodeRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(childrenCodeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(childrenVariablesRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jScrollPane1, 0, 0, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testChildrenCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testChildrenScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVarButtonActionPerformed
        final DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        model.addRow(new Object[] { "", "" });
        final int index = model.getRowCount() - 1;
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index, index);
        childrenVariablesTable.editCellAt(index, 0);
        childrenVariablesTable.requestFocus();
         //DefaultCellEditor ed = (DefaultCellEditor)
        childrenVariablesTable.getCellEditor(index, 0).shouldSelectCell(
                new ListSelectionEvent(childrenVariablesTable,
                                       index, index, true));
        addVarButton.setEnabled(false);
        removeVarButton.setEnabled(false);
        childrenVariablesTable.getCellEditor(index, 0).addCellEditorListener(new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String value = (String) model.getValueAt(index, 0);
                        if (value.trim().length() == 0) {
                            model.removeRow(index);
                        }
                    }
                });
                childrenVariablesTable.getCellEditor(index, 0).removeCellEditorListener(this);
                addVarButton.setEnabled(true);
                removeVarButton.setEnabled(childrenVariablesTable.getSelectedRow() >= 0);
            }

            public void editingCanceled(ChangeEvent e) {
                model.removeRow(index);
                childrenVariablesTable.getCellEditor(index, 0).removeCellEditorListener(this);
                addVarButton.setEnabled(true);
                removeVarButton.setEnabled(childrenVariablesTable.getSelectedRow() >= 0);
            }
        });
    }//GEN-LAST:event_addVarButtonActionPerformed

    private void removeVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index < 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        model.removeRow(index);
        if (index < childrenVariablesTable.getRowCount() || --index >= 0) {
            childrenVariablesTable.setRowSelectionInterval(index, index);
        }
    }//GEN-LAST:event_removeVarButtonActionPerformed

    private void moveUpVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index <= 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        Object[] row = new Object[] { model.getValueAt(index, 0), model.getValueAt(index, 1) };
        model.removeRow(index);
        model.insertRow(index - 1, row);
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
    }//GEN-LAST:event_moveUpVarButtonActionPerformed

    private void moveDownVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownVarButtonActionPerformed
        int index = childrenVariablesTable.getSelectedRow();
        if (index < 0) return ;
        DefaultTableModel model = (DefaultTableModel) childrenVariablesTable.getModel();
        if (index >= (model.getRowCount() - 1)) return ;
        Object[] row = new Object[] { model.getValueAt(index, 0), model.getValueAt(index, 1) };
        model.removeRow(index);
        model.insertRow(index + 1, row);
        childrenVariablesTable.getSelectionModel().setSelectionInterval(index + 1, index + 1);
    }//GEN-LAST:event_moveDownVarButtonActionPerformed

    private void valueFormatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFormatCheckBoxActionPerformed
        if (valueFormatCheckBox.isSelected()) {
            valueEditorPane.setEnabled(true);
            valueEditorPane.setBackground(nameTextField.getBackground());
            valueEditorPane.requestFocusInWindow();
        } else {
            valueEditorPane.setEnabled(false);
            valueEditorPane.setBackground(getDisabledFieldBackground());
        }
        checkValid();
    }//GEN-LAST:event_valueFormatCheckBoxActionPerformed

    private void childrenFormatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenFormatCheckBoxActionPerformed
        boolean selected = childrenFormatCheckBox.isSelected();
        childrenCodeRadioButton.setEnabled(selected);
        childrenVariablesRadioButton.setEnabled(selected);
        testChildrenCheckBox.setEnabled(selected);
        childrenCodeRadioButtonActionPerformed(null);
        childrenVariablesRadioButtonActionPerformed(null);
        testChildrenCheckBoxActionPerformed(null);
        checkValid();
    }//GEN-LAST:event_childrenFormatCheckBoxActionPerformed

    private void childrenCodeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenCodeRadioButtonActionPerformed
        if (childrenCodeRadioButton.isSelected() && childrenCodeRadioButton.isEnabled()) {
            childrenCodeEditorPane.setEnabled(true);
            childrenCodeEditorPane.setBackground(nameTextField.getBackground());
            childrenCodeEditorPane.requestFocusInWindow();
        }
        if (!childrenVariablesRadioButton.isSelected() || !childrenVariablesRadioButton.isEnabled()) {
            childrenVariablesTable.getSelectionModel().clearSelection();
            childrenVariablesTable.setEnabled(false);
            addVarButton.setEnabled(false);
            removeVarButton.setEnabled(false);
            moveUpVarButton.setEnabled(false);
            moveDownVarButton.setEnabled(false);
        }
    }//GEN-LAST:event_childrenCodeRadioButtonActionPerformed

    private void childrenVariablesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenVariablesRadioButtonActionPerformed
        if (childrenVariablesRadioButton.isSelected() && childrenVariablesRadioButton.isEnabled()) {
            childrenVariablesTable.setEnabled(true);
            childrenVariablesTable.requestFocusInWindow();            
            addVarButton.setEnabled(true);
            int row = childrenVariablesTable.getSelectedRow();
            removeVarButton.setEnabled(row >= 0);
            moveUpVarButton.setEnabled(row > 0);
            moveDownVarButton.setEnabled(row >= 0 && row < childrenVariablesTable.getRowCount() - 1);
        }
        if (!childrenCodeRadioButton.isSelected() || !childrenCodeRadioButton.isEnabled()) {
            childrenCodeEditorPane.setEnabled(false);
            childrenCodeEditorPane.setBackground(getDisabledFieldBackground());
        }
    }//GEN-LAST:event_childrenVariablesRadioButtonActionPerformed

    private void testChildrenCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testChildrenCheckBoxActionPerformed
        if (testChildrenCheckBox.isSelected() && testChildrenCheckBox.isEnabled()) {
            testChildrenEditorPane.setEnabled(true);
            testChildrenEditorPane.setBackground(nameTextField.getBackground());
            testChildrenEditorPane.requestFocusInWindow();
        } else {
            testChildrenEditorPane.setEnabled(false);
            testChildrenEditorPane.setBackground(getDisabledFieldBackground());
        }
    }//GEN-LAST:event_testChildrenCheckBoxActionPerformed

    private void initChildrenVariablesTable() {
        removeVarButton.setEnabled(false);
        moveUpVarButton.setEnabled(false);
        moveDownVarButton.setEnabled(false);
        childrenVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = childrenVariablesTable.getSelectedRow();
                removeVarButton.setEnabled(row >= 0);
                moveUpVarButton.setEnabled(row > 0);
                moveDownVarButton.setEnabled(row >= 0 && row < childrenVariablesTable.getRowCount() - 1);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addVarButton;
    private javax.swing.ButtonGroup childrenButtonGroup;
    private javax.swing.JEditorPane childrenCodeEditorPane;
    private javax.swing.JRadioButton childrenCodeRadioButton;
    private javax.swing.JScrollPane childrenCodeScrollPane;
    private javax.swing.JCheckBox childrenFormatCheckBox;
    private javax.swing.JRadioButton childrenVariablesRadioButton;
    private javax.swing.JTable childrenVariablesTable;
    private javax.swing.JLabel classTypesLabel;
    private javax.swing.JTextField classTypesTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton moveDownVarButton;
    private javax.swing.JButton moveUpVarButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeVarButton;
    private javax.swing.JCheckBox subtypesCheckBox;
    private javax.swing.JCheckBox testChildrenCheckBox;
    private javax.swing.JEditorPane testChildrenEditorPane;
    private javax.swing.JScrollPane testChildrenScrollPane;
    private javax.swing.JEditorPane valueEditorPane;
    private javax.swing.JCheckBox valueFormatCheckBox;
    private javax.swing.JScrollPane valueScrollPane;
    // End of variables declaration//GEN-END:variables
    private final String[] tableColumnNames = new String[] {
        NbBundle.getMessage(CategoryPanelFormatters.class, "CategoryPanelFormatters.formatChildrenListTable.Name"),
        NbBundle.getMessage(CategoryPanelFormatters.class, "CategoryPanelFormatters.formatChildrenListTable.Value")
    };
    private Set<String> formatterNames;
    private DialogDescriptor validityDescriptor;
    private NotificationLineSupport validityNotificationSupport;
    private boolean continualValidityChecks = false;

}
