/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Rob Englander
 */
public class LabeledTextFieldDialog extends javax.swing.JPanel {
    
    /** Creates new form LabeledTextFieldDialog */
    public LabeledTextFieldDialog(String notes) 
    {
        String title = NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableRenameTable"); // NOI18N
        String lab = NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableNewName"); // NOI18N
        original_notes = notes;

        initComponents();
        
        try
        {
            Mnemonics.setLocalizedText(titleLabel, lab);
            titleLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameA11yDesc"));  // NOI18N

            Mnemonics.setLocalizedText(descLabel, NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableRenameNotes")); // NOI18N
            descLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableRenameNotesA11yDesc"));  // NOI18N
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "EditCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_EditCommandA11yDesc"));  // NOI18N

            updateState();

            ActionListener listener = new ActionListener() 
            {
                public void actionPerformed(ActionEvent event) 
                {
                    result = event.getSource() == DialogDescriptor.OK_OPTION;
                }
            };

            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableDialogA11yDesc")); // NOI18N

            DialogDescriptor descriptor = new DialogDescriptor(this, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        }
        catch (MissingResourceException e)
        {
            e.printStackTrace();
        }

    }
    
    public boolean run() 
    {
        if (dialog != null)
        {
            dialog.setVisible(true);
        }
        
        return result;
    }

    public String getStringValue() 
    {
        return textField.getText();
    }

    public String getEditedCommand() 
    {
        return notesArea.getText();
    }

    public boolean isEditable() 
    {
        return notesArea.isEditable();
    }

    public void setStringValue(String val) 
    {
        textField.setText(val);
    }

    private void updateState()
    {
        isEditMode = !isEditMode;
        
        if (isEditMode) { // NOI18N
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "ReloadCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_ReloadCommandA11yDesc"));  // NOI18N
            notesArea.setEditable( true );
            notesArea.setEnabled(true);
            notesArea.setBackground(textField.getBackground()); // white
            notesArea.requestFocus();
            textField.setEditable( false );
            textField.setBackground(titleLabel.getBackground()); // grey
        } else {
            // reload script from file
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "EditCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_EditCommandA11yDesc"));  // NOI18N
            notesArea.setText(original_notes);
            notesArea.setEditable( false );
            notesArea.setEnabled(false);
            notesArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground")); // NOI18N
            textField.setEditable( true );
            textField.setBackground(notesArea.getBackground()); // grey
            notesArea.setBackground(titleLabel.getBackground()); // white
            textField.requestFocus();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        descLabel = new javax.swing.JLabel();
        notesAreaScrollPane = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        editButton = new javax.swing.JButton();

        titleLabel.setLabelFor(textField);
        titleLabel.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.titleLabel.text")); // NOI18N

        textField.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.textField.text")); // NOI18N

        descLabel.setLabelFor(notesArea);
        descLabel.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.descLabel.text")); // NOI18N

        notesArea.setColumns(20);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setRows(5);
        notesArea.setWrapStyleWord(true);
        notesArea.setEnabled(false);
        notesAreaScrollPane.setViewportView(notesArea);
        notesArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableTableScriptTextAreaA11yName")); // NOI18N
        notesArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableTableScriptTextAreaA11yDesc")); // NOI18N

        editButton.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.editButton.text")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(notesAreaScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(titleLabel)
                        .add(18, 18, 18)
                        .add(textField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, descLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, editButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(titleLabel)
                    .add(textField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(notesAreaScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(editButton)
                .addContainerGap())
        );

        textField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameTextFieldA11yName")); // NOI18N
        textField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameTextFieldA11yDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        updateState();
}                                          
    
    // init edit mode to true so that the first call to
    // updateState will toggle into read mode
    boolean isEditMode = true;
    boolean result = false;
    Dialog dialog = null;
    private String original_notes;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JScrollPane notesAreaScrollPane;
    private javax.swing.JTextField textField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    
}
