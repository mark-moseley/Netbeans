/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.JList;
import javax.swing.DefaultListModel;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  Pavel Buzek
 */
public class EventCustomEditor extends javax.swing.JPanel {

    static final long serialVersionUID =-4825059521634962952L;

    /** Creates new form EventCustomEditor */
    public EventCustomEditor(EventProperty eventProperty) {
        this.eventProperty = eventProperty;
        changes = eventProperty.new HandlerSetChange();

        initComponents();
        enableButtons();

        org.openide.util.HelpCtx.setHelpIDString(this, "gui.csh.handlers"); // NOI18N
        
        addButton.setMnemonic(
            FormUtils.getBundleString("CTL_EE_ADD_Mnemonic").charAt(0)); // NOI18N
        removeButton.setMnemonic(
            FormUtils.getBundleString("CTL_EE_REMOVE_Mnemonic").charAt(0)); // NOI18N
        editButton.setMnemonic(
            FormUtils.getBundleString("CTL_EE_RENAME_Mnemonic").charAt(0)); // NOI18N
        handlersListLabel.setDisplayedMnemonic(
            FormUtils.getBundleString("CTL_EE_Handlers_Mnemonic").charAt(0)); // NOI18N

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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        handlersListLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        List l = eventProperty.event.getHandlers();
        for (int i=0, n=l.size (); i < n; i++) {
            handlersModel.addElement(((EventHandler)l.get(i)).getName());
        }
        handlersList = new javax.swing.JList();
        handlersList.setModel(handlersModel);
        if (l.size() > 0) {
            handlersList.setSelectedIndex(0);
        }
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(300, 300));
        handlersListLabel.setLabelFor(handlersList);
        handlersListLabel.setText(FormUtils.getBundleString("CTL_EE_Handlers"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 0);
        add(handlersListLabel, gridBagConstraints);

        handlersList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                handlersListValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(handlersList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(jScrollPane1, gridBagConstraints);

        addButton.setText(FormUtils.getBundleString("CTL_EE_ADD"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(addButton, gridBagConstraints);

        removeButton.setText(FormUtils.getBundleString("CTL_EE_REMOVE"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 11);
        add(removeButton, gridBagConstraints);

        editButton.setText(FormUtils.getBundleString("CTL_EE_RENAME"));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        add(editButton, gridBagConstraints);

    }//GEN-END:initComponents

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

            if (TopManager.getDefault().notify(nd).equals(NotifyDescriptor.OK_OPTION)) {
                String newName = nd.getInputText();
                if (newName.equals(oldName)) return; // no change

                if (!org.openide.util.Utilities.isJavaIdentifier(newName)) { // invalid name
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        FormUtils.getBundleString("CTL_EE_NOT_IDENTIFIER"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                    TopManager.getDefault().notify(msg);
                    return;
                }

                if (handlersModel.indexOf(newName) >= 0) { // already exists
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        FormUtils.getBundleString("CTL_EE_ALREADY_EXIST"), // NOI18N
                        NotifyDescriptor.INFORMATION_MESSAGE);
                    TopManager.getDefault().notify(msg);
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
                    toRemove = (String) changes.getRenamedOldNames().get(ii);
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
        if (TopManager.getDefault().notify(nd).equals(NotifyDescriptor.OK_OPTION)) {
            String newHandler = nd.getInputText();
            if (!org.openide.util.Utilities.isJavaIdentifier(newHandler)) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    FormUtils.getBundleString("CTL_EE_NOT_IDENTIFIER"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
                TopManager.getDefault().notify(msg);
                return;
            }

            if (handlersModel.indexOf(newHandler) >= 0) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    FormUtils.getBundleString("CTL_EE_ALREADY_EXIST"), // NOI18N
                    NotifyDescriptor.INFORMATION_MESSAGE);
                TopManager.getDefault().notify(msg);
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
    private javax.swing.JLabel handlersListLabel;
    private javax.swing.JButton addButton;
    private javax.swing.JList handlersList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton editButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    EventProperty eventProperty;
    DefaultListModel handlersModel = new DefaultListModel();
    EventProperty.HandlerSetChange changes;
}
