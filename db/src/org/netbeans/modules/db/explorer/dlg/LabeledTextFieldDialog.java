/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.openide.util.NbBundle;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.explorer.*;

/**
* @author Slavek Psenicka
*/
public class LabeledTextFieldDialog {
    boolean result = false;
    Dialog dialog = null;
    Object combosel = null;
    JTextField field;
    JTextArea notesarea;
    JButton edButton;
    JLabel label;
    final String original_notes;
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public LabeledTextFieldDialog(String title, String lab, String notes) {
        original_notes = notes;
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Title

            label = new JLabel(lab);
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableNewNameA11yDesc"));  // NOI18N
            label.setDisplayedMnemonic(bundle.getString("RecreateTableNewName_Mnemonic").charAt(0));  // NOI18N
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Textfield

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            field = new JTextField(35);
            field.getAccessibleContext().setAccessibleName(bundle.getString("ACS_RecreateTableNewNameTextFieldA11yName"));  // NOI18N
            field.setToolTipText(bundle.getString("ACS_RecreateTableNewNameTextFieldA11yDesc"));  // NOI18N
            label.setLabelFor(field);
            layout.setConstraints(field, con);
            pane.add(field);

            // Descr.

            JLabel desc = new JLabel(bundle.getString("RecreateTableRenameNotes")); // NOI18N
            desc.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableRenameNotesA11yDesc"));  // NOI18N
            desc.setDisplayedMnemonic(bundle.getString("RecreateTableRenameNotes_Mnemonic").charAt(0));  // NOI18N
            con.anchor = GridBagConstraints.WEST;
            con.gridx = 0;
            con.gridy = 2;
            con.weighty = 2.0;
            layout.setConstraints(desc, con);
            pane.add(desc);

            // Notes

            notesarea = new JTextArea(notes, 10, 50);
            notesarea.setEditable(false);
            notesarea.setLineWrap(true);
            notesarea.setWrapStyleWord(true);
            notesarea.setFont(label.getFont());
            notesarea.setBackground(label.getBackground()); // grey
            notesarea.setEnabled(false);
            notesarea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
            notesarea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_RecreateTableTableScriptTextAreaA11yName"));  // NOI18N
            notesarea.setToolTipText(bundle.getString("ACS_RecreateTableTableScriptTextAreaA11yDesc"));  // NOI18N
            desc.setLabelFor(notesarea);
            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (10, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 3;
            notesarea.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            JScrollPane spane = new JScrollPane(notesarea);
            layout.setConstraints(spane, con);
            pane.add(spane);
            
            // edit button
            edButton = new JButton(bundle.getString("EditCommand")); // NOI18N
            edButton.setMnemonic(bundle.getString("EditCommand_Mnemonic").charAt(0));  // NOI18N
            edButton.setToolTipText(bundle.getString("ACS_EditCommandA11yDesc"));  // NOI18N
            con.fill = GridBagConstraints.WEST;
            con.weighty = 0.0;
            con.weightx = 0.0;
            con.gridx = 0;
            con.gridy = 5;
            layout.setConstraints(edButton, con);
            pane.add(edButton);

            edButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(edButton.getText().startsWith(bundle.getString("EditCommand"))) { // NOI18N
                        // set to edit 
                        edButton.setText(bundle.getString("ReloadCommand")); // NOI18N
                        edButton.setMnemonic(bundle.getString("ReloadCommand_Mnemonic").charAt(0));  // NOI18N
                        edButton.setToolTipText(bundle.getString("ACS_ReloadCommandA11yDesc"));  // NOI18N
                        notesarea.setEditable( true );
                        notesarea.setEnabled(true);
                        notesarea.setBackground(field.getBackground()); // white
                        notesarea.requestFocus();
                        field.setEditable( false );
                        field.setBackground(label.getBackground()); // grey
                    } else {
                        // reload script from file
                        edButton.setText(bundle.getString("EditCommand")); // NOI18N
                        edButton.setMnemonic(bundle.getString("EditCommand_Mnemonic").charAt(0));  // NOI18N
                        edButton.setToolTipText(bundle.getString("ACS_EditCommandA11yDesc"));  // NOI18N
                        notesarea.setText(original_notes);
                        notesarea.setEditable( false );
                        notesarea.setEnabled(false);
                        notesarea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
                        field.setEditable( true );
                        field.setBackground(notesarea.getBackground()); // grey
                        notesarea.setBackground(label.getBackground()); // white
                        field.requestFocus();
                    }
                }
            });

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION)
                        result = true;
                    else
                        result = false;;
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_RecreateTableDialogA11yDesc"));

            DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null)
            dialog.setVisible(true);
        
        return result;
    }

    public String getStringValue() {
        return field.getText();
    }

    public String getEditedCommand() {
        return notesarea.getText();
    }

    public boolean isEditable() {
        return notesarea.isEditable();
    }

    public void setStringValue(String val) {
        field.setText(val);
    }
}
