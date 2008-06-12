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

package org.netbeans.modules.apisupport.project.ui.wizard.codegenerator;

import java.awt.Component;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.codegenerator.NewCodeGeneratorIterator.DataModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * CodeGenerator API Support wizard Panel
 * @author Max Sauer
 */
public class CodeGeneratorPanel extends BasicWizardIterator.Panel {
    
    private DataModel data;

    /** Creates new form CodeGeneratorPanel */
    CodeGeneratorPanel(WizardDescriptor settings, NewCodeGeneratorIterator.DataModel data) {
        super(settings);
        this.data = data;
        initComponents();
        
        putClientProperty("NewFileWizard_Title", getMessage("LBL_CodeGeneratorPanel_Title"));
        
        DocumentListener dListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };
        
        if (data.getPackageName() != null) {
            packageNameCombo.setSelectedItem(data.getPackageName());
        }
        
        fileNametextField.getDocument().addDocumentListener(dListener);
        cpFileNameField.getDocument().addDocumentListener(dListener);
        mimeTypeTextField.getDocument().addDocumentListener(dListener);
        Component editorComp = packageNameCombo.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(dListener);
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileNameLabel = new javax.swing.JLabel();
        fileNametextField = new javax.swing.JTextField();
        mimeTypeLabel = new javax.swing.JLabel();
        mimeTypeTextField = new javax.swing.JTextField();
        cpCheckBox = new javax.swing.JCheckBox();
        cpFileNameLabel = new javax.swing.JLabel();
        cpFileNameField = new javax.swing.JTextField();
        packageNameCombo = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        packageNameLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.fileNameLabel.text")); // NOI18N

        fileNametextField.setText(org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.fileNametextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mimeTypeLabel, org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.mimeTypeLabel.text")); // NOI18N

        mimeTypeTextField.setText(org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.mimeTypeTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cpCheckBox, org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.cpCheckBox.text")); // NOI18N
        cpCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cpCheckBoxStateChanged(evt);
            }
        });

        cpFileNameLabel.setLabelFor(cpFileNameField);
        org.openide.awt.Mnemonics.setLocalizedText(cpFileNameLabel, org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.cpFileNameLabel.text")); // NOI18N

        cpFileNameField.setEditable(cpCheckBox.isSelected());
        cpFileNameField.setText(org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.cpFileNameField.text")); // NOI18N

        packageNameCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(packageNameLabel, org.openide.util.NbBundle.getMessage(CodeGeneratorPanel.class, "CodeGeneratorPanel.packageNameLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fileNameLabel)
                                    .add(mimeTypeLabel)
                                    .add(packageNameLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fileNametextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                                    .add(mimeTypeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                                    .add(packageNameCombo, 0, 304, Short.MAX_VALUE)))
                            .add(cpCheckBox)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(33, 33, 33)
                        .add(cpFileNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cpFileNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileNameLabel)
                    .add(fileNametextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageNameCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(packageNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mimeTypeLabel)
                    .add(mimeTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(cpCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cpFileNameLabel)
                    .add(cpFileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void cpCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cpCheckBoxStateChanged
    cpFileNameField.setEditable(cpCheckBox.isSelected());
    checkValidity();
}//GEN-LAST:event_cpCheckBoxStateChanged

    @Override
    protected String getPanelName() {
        return NbBundle.getMessage(CodeGeneratorPanel.class,"LBL_CodeGeneratorPanel_Title"); // NOI18N
    }

    @Override
    protected void storeToDataModel() {
        data.setMimeType(mimeTypeTextField.getText().trim());
        data.setFileName(normalize(fileNametextField.getText().trim()));
        data.setContextProviderRequired(cpCheckBox.isSelected());
        data.setProviderFileName(cpFileNameField.getText().trim());
        data.setPackageName(packageNameCombo.getEditor().getItem().toString());
        NewCodeGeneratorIterator.generateFileChanges(data);
    }

    @Override
    protected void readFromDataModel() {
        mimeTypeTextField.setText(data.getMimeType());
        fileNametextField.setText(data.getFileName());
        cpFileNameField.setText(data.getProviderFileName());
        cpCheckBox.setSelected(data.isContextProviderRequired());
        packageNameCombo.setSelectedItem(data.getPackageName());
    }

    @Override
    protected HelpCtx getHelp() {
        return new HelpCtx(CodeGeneratorPanel.class);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cpCheckBox;
    private javax.swing.JTextField cpFileNameField;
    private javax.swing.JLabel cpFileNameLabel;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNametextField;
    private javax.swing.JLabel mimeTypeLabel;
    private javax.swing.JTextField mimeTypeTextField;
    private javax.swing.JComboBox packageNameCombo;
    private javax.swing.JLabel packageNameLabel;
    // End of variables declaration//GEN-END:variables

    private String normalize(String trim) {
        if(trim.endsWith(".java"))
            return trim.substring(0, trim.length()-5);
        else return trim;
    }

    
    private boolean checkValidity() {
        final String fileName = fileNametextField.getText().trim();
        final String mimeType = mimeTypeTextField.getText().trim();
        if(fileName.length() == 0) {
            setError(getMessage("ERR_FN_EMPTY"));
            return false;
        }
        if(!Utilities.isJavaIdentifier(normalize(fileName))) {
            setError(getMessage("ERR_FN_INVALID"));
            return false;
        }
        if(mimeType.length() == 0) {
            setError(getMessage("ERR_MT_EMPTY"));
            return false;
        }
        
        String packName = packageNameCombo.getEditor().getItem().toString();
        if(packName.equals("")) {
            setWarning(getMessage("EMPTY_PACKAGE"));
            return true;
        }
        
        if (cpCheckBox.isSelected()) {
            String cpFileName = cpFileNameField.getText().trim();
            if (cpFileName.length() == 0) {
                setError(getMessage("ERR_FN_EMPTY"));
                return false;
            }
            if (!Utilities.isJavaIdentifier(normalize(cpFileName))) {
                setError(getMessage("ERR_FN_INVALID"));
                return false;
            }
        }
        markValid();
        return true;
    }
    
    private String getMessage(String key) {
        return NbBundle.getMessage(CodeGeneratorPanel.class, key);
    }
}
