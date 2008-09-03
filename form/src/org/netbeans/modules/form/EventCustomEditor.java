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

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.DefaultListModel;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;

/**
 *
 * @author  Pavel Buzek
 */
public class EventCustomEditor extends javax.swing.JPanel {

    static final long serialVersionUID =-4825059521634962952L;

    /** Creates new form EventCustomEditor */
    EventCustomEditor(EventProperty eventProperty) {
        this.eventProperty = eventProperty;

        initComponents();
        enableButtons();

        Mnemonics.setLocalizedText(addButton, FormUtils.getBundleString("CTL_EE_ADD")); // NOI18N
        Mnemonics.setLocalizedText(removeButton, FormUtils.getBundleString("CTL_EE_REMOVE")); // NOI18N
        Mnemonics.setLocalizedText(editButton, FormUtils.getBundleString("CTL_EE_RENAME")); // NOI18N
        Mnemonics.setLocalizedText(handlersListLabel, FormUtils.getBundleString("CTL_EE_Handlers")); // NOI18N

        handlersList.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_EE_Handlers")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_EE_ADD")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_EE_REMOVE")); // NOI18N
        editButton.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_EE_RENAME")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_EventCustomEditor")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        String[] handlers = eventProperty.getEventHandlers();
        for (int i=0; i < handlers.length; i++) {
            handlersModel.addElement(handlers[i]);
        }
        handlersList = new javax.swing.JList();
        handlersList.setModel(handlersModel);
        if (handlers.length > 0) {
            handlersList.setSelectedIndex(0);
        }
        handlersListLabel = new javax.swing.JLabel();

        editButton.setText("Rename...");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add...");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        handlersList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                handlersListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(handlersList);

        handlersListLabel.setLabelFor(handlersList);
        handlersListLabel.setText("Handlers");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                    .add(handlersListLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(handlersListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton)
                        .addContainerGap(188, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void handlersListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_handlersListValueChanged
        enableButtons();
    }//GEN-LAST:event_handlersListValueChanged

    private void enableButtons() {
        if (handlersList.isSelectionEmpty()) {
            removeButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
        }
        editButton.setEnabled(handlersList.getSelectedIndices().length == 1);
    }

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // Add your handling code here:
        int i = handlersList.getSelectedIndex();
        if (i >= 0) {
            String oldName = (String) handlersModel.get(i);
            NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                FormUtils.getBundleString("CTL_EE_RENAME_LABEL"), // NOI18N
                FormUtils.getBundleString("CTL_EE_RENAME_CAPTION")); // NOI18N
            nd.setInputText(oldName);

            if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.OK_OPTION)) {
                String newName = nd.getInputText();
                if (newName.equals(oldName)) return; // no change

                if (!org.openide.util.Utilities.isJavaIdentifier(newName)) { // invalid name
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        FormUtils.getBundleString("CTL_EE_NOT_IDENTIFIER"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }

                if (handlersModel.indexOf(newName) >= 0) { // already exists
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        FormUtils.getBundleString("CTL_EE_ALREADY_EXIST"), // NOI18N
                        NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(msg);
                    return;
                }

                int ii = changes.getAdded().indexOf(oldName);
                if (ii >= 0) { // a newly added handler was renamed
                    changes.getAdded().set(ii,newName);
                }
                else {
                    ii = changes.getRenamedNewNames().indexOf(oldName);
                    if (ii >= 0) // this handler has been already renamed
                        changes.getRenamedNewNames().set(ii, newName);
                    else {
                        changes.getRenamedOldNames().add(oldName);
                        changes.getRenamedNewNames().add(newName);
                    }
                }

                handlersModel.set(i,newName);
                handlersList.setSelectedIndex(i);
                enableButtons();
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object[] handlers = handlersList.getSelectedValues();
        for (int i=0; i < handlers.length; i++) {
            int ii = changes.getAdded().indexOf(handlers[i]);
            if (ii >= 0) { // the handler was previously added - cancel it
                changes.getAdded().remove(ii);
            }
            else {
                ii = changes.getRenamedNewNames().indexOf(handlers[i]);
                String toRemove;
                if (ii >= 0) { // the handler was previously renamed - cancel it
                    changes.getRenamedNewNames().remove(ii);
                    toRemove = changes.getRenamedOldNames().get(ii);
                    changes.getRenamedOldNames().remove(ii);
                }
                else toRemove = (String) handlers[i];

                changes.getRemoved().add(toRemove);
            }
            handlersModel.removeElement(handlers[i]);
            enableButtons();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
            FormUtils.getBundleString("CTL_EE_ADD_LABEL"), // NOI18N
            FormUtils.getBundleString("CTL_EE_ADD_CAPTION")); // NOI18N
        if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.OK_OPTION)) {
            String newHandler = nd.getInputText();
            if (!org.openide.util.Utilities.isJavaIdentifier(newHandler)) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    FormUtils.getBundleString("CTL_EE_NOT_IDENTIFIER"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            if (handlersModel.indexOf(newHandler) >= 0) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    FormUtils.getBundleString("CTL_EE_ALREADY_EXIST"), // NOI18N
                    NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            int ir = changes.getRemoved().indexOf(newHandler);
            if (ir >= 0) {
                changes.getRemoved().remove(ir);
            }
            else {
                changes.getAdded().add(newHandler);
            }
            handlersModel.addElement(newHandler);
            handlersList.setSelectedIndex(handlersModel.size() - 1);
            enableButtons();
        }
    }//GEN-LAST:event_addButtonActionPerformed

    public void doChanges() {
        try {
            eventProperty.setValue(changes);
        } catch (Exception e) { // should not happen
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JList handlersList;
    private javax.swing.JLabel handlersListLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    EventProperty eventProperty;
    DefaultListModel handlersModel = new DefaultListModel();
    EventProperty.Change changes = new EventProperty.Change();
}
