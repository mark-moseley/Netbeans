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

package org.netbeans.modules.versioning.util;

import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;

import javax.swing.*;
import java.util.*;
import java.awt.Dialog;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 * Provides chooser from list of strings.
 *
 * @author  Maros Sandor
 */
public class StringSelector extends javax.swing.JPanel implements MouseListener {

    public static String select(String title, String prompt, List<String> strings) {
        StringSelector panel = new StringSelector();
        Mnemonics.setLocalizedText(panel.promptLabel, prompt);
        panel.listValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.setChoices(strings);
        
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StringSelector.class, "ACSD_StringSelectorDialog"));  // NOI18N
        panel.putClientProperty(Dialog.class, dialog);
        panel.putClientProperty(DialogDescriptor.class, descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return null;
        
        return (String) panel.listValues.getSelectedValue();
    }

    private List<String> choices;
    
    private void setChoices(List<String> strings) {
        choices = strings;
        
        listValues.setModel(new AbstractListModel() {
            public int getSize() {
                return choices.size();
            }

            public Object getElementAt(int index) {
                return choices.get(index);
            }
        });
    }

    /** Creates new form StringSelector */
    public StringSelector() {
        initComponents();
        listValues.addMouseListener(this);
    }
    
    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() == 2) {
            Dialog dialog = (Dialog) getClientProperty(Dialog.class);
            DialogDescriptor descriptor = (DialogDescriptor) getClientProperty(DialogDescriptor.class);
            descriptor.setValue(DialogDescriptor.OK_OPTION);
            dialog.dispose();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        promptLabel = new javax.swing.JLabel();
        jScrollPane = new javax.swing.JScrollPane();
        listValues = new javax.swing.JList();

        promptLabel.setLabelFor(listValues);
        promptLabel.setText(org.openide.util.NbBundle.getMessage(StringSelector.class, "StringSelector.promptLabel.text")); // NOI18N

        listValues.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane.setViewportView(listValues);
        listValues.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StringSelector.class, "ACSN_StringSelectorMessages")); // NOI18N
        listValues.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StringSelector.class, "ACSD_StringSelectorMessages")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(promptLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .add(96, 96, 96))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(promptLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JList listValues;
    private javax.swing.JLabel promptLabel;
    // End of variables declaration//GEN-END:variables
}
