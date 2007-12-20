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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.pastetype;

import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  skini
 */
public class DnDImportPanel extends javax.swing.JPanel {

    private DialogDescriptor dd;
    private Set existingPrefixes;
    private DocumentListener listener;
    /** Creates new form DnDImportPanel */
    public DnDImportPanel() {
        initComponents();
        
        listener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validatePrefix();
            }

            public void removeUpdate(DocumentEvent e) {
                validatePrefix();
            }

            public void changedUpdate(DocumentEvent e) {
                validatePrefix();
            }

            void validatePrefix() {
                String errorMessage = null;
                String prefix = prefixTextField.getText();
                if (prefix == null || prefix.trim().length() == 0 || !org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(prefix)) {
                    errorMessage = NbBundle.getMessage(DnDImportPanel.class, "ERRMSG_InvalidPrefix", prefix);
                }
                
                if (existingPrefixes != null && existingPrefixes.contains(prefix)) {
                    errorMessage = NbBundle.getMessage(DnDImportPanel.class, "ERRMSG_PrefixAlreadyExists", prefix);
                }
                if (errorMessage != null) {
                    errorWarningMessagePanel.setErrorMessage(errorMessage);
                } else {
                    errorWarningMessagePanel.setMessage(null);
                }
                dd.setValid(errorMessage == null);
            }
        };
        prefixTextField.getDocument().addDocumentListener(listener);
    }

    String getPrefix() {
        return prefixTextField.getText();
    }

    void setDialogDescriptor(DialogDescriptor descriptor) {
        dd = descriptor;
    }

    void setFileName(FileObject primaryFile) {
        fileTextField.setText(primaryFile.getNameExt());
    }

    void setNamespace(String impNamespace) {
        namespaceTextField.setText(impNamespace);
    }

    void setPrefix(String prefix) {
        prefixTextField.getDocument().removeDocumentListener(listener);
        prefixTextField.setText(prefix);
        prefixTextField.getDocument().addDocumentListener(listener);
    }

    void setPrefixes(Set prefixes) {
        existingPrefixes = prefixes;
    }

    void setProject(Project project) {
        projectNameTextField.setText(project.getProjectDirectory().getName());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileNameLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        namespaceLabel = new javax.swing.JLabel();
        namespaceTextField = new javax.swing.JTextField();
        prefixLabel = new javax.swing.JLabel();
        prefixTextField = new javax.swing.JTextField();
        errorWarningMessagePanel = new org.netbeans.modules.xml.wsdl.ui.view.common.CommonMessagePanel();

        setName("Form"); // NOI18N

        fileNameLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.fileNameLabel.text")); // NOI18N
        fileNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.fileNameLabel.toolTipText")); // NOI18N
        fileNameLabel.setName("fileNameLabel"); // NOI18N

        fileTextField.setEditable(false);
        fileTextField.setName("fileTextField"); // NOI18N

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.projectNameLabel.text")); // NOI18N
        projectNameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.projectNameLabel.toolTipText")); // NOI18N
        projectNameLabel.setName("projectNameLabel"); // NOI18N

        projectNameTextField.setEditable(false);
        projectNameTextField.setName("projectNameTextField"); // NOI18N

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.namespaceLabel.text")); // NOI18N
        namespaceLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.namespaceLabel.toolTipText")); // NOI18N
        namespaceLabel.setName("namespaceLabel"); // NOI18N

        namespaceTextField.setEditable(false);
        namespaceTextField.setName("namespaceTextField"); // NOI18N

        prefixLabel.setLabelFor(prefixTextField);
        org.openide.awt.Mnemonics.setLocalizedText(prefixLabel, org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.prefixLabel.text")); // NOI18N
        prefixLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.prefixLabel.toolTipText")); // NOI18N
        prefixLabel.setName("prefixLabel"); // NOI18N

        prefixTextField.setName("prefixTextField"); // NOI18N

        errorWarningMessagePanel.setName("errorWarningMessagePanel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorWarningMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(projectNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(prefixLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(namespaceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(fileNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                            .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, prefixTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, namespaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileNameLabel)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(namespaceLabel)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(prefixTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(prefixLabel))
                .add(18, 18, 18)
                .add(errorWarningMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        fileTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.fileTextField.AccessibleContext.accessibleName")); // NOI18N
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.fileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.projectNameTextField.AccessibleContext.accessibleName")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        namespaceTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.namespaceTextField.AccessibleContext.accessibleName")); // NOI18N
        namespaceTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.namespaceTextField.AccessibleContext.accessibleDescription")); // NOI18N
        prefixTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.prefixTextField.AccessibleContext.accessibleName")); // NOI18N
        prefixTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DnDImportPanel.class, "DnDImportPanel.prefixTextField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.xml.wsdl.ui.view.common.CommonMessagePanel errorWarningMessagePanel;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JTextField namespaceTextField;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JTextField prefixTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
}
