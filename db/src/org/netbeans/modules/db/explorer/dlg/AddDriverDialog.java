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
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.*;

public class AddDriverDialog {
    boolean result = false;
    Dialog dialog = null;
    String drv = null, name = null, prefix = null;
    JTextField namefield, drvfield, prefixfield;

    public AddDriverDialog() {
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);
            final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

            // Driver name

            label = new JLabel(bundle.getString("AddDriverDriverName")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("AddDriverDriverName_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddDriverDriverNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefield = new JTextField(35);
            namefield.setToolTipText(bundle.getString("ACS_AddDriverDriverNameTextFieldA11yDesc"));
            namefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddDriverDriverNameTextFieldA11yName"));
            label.setLabelFor(namefield);
            layout.setConstraints(namefield, con);
            pane.add(namefield);

            // Driver label and field

            label = new JLabel(bundle.getString("AddDriverDriverURL")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("AddDriverDriverURL_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddDriverDriverURLA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 1;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            drvfield = new JTextField(35);
            drvfield.setToolTipText(bundle.getString("ACS_AddDriverDriverURLTextFieldA11yDesc"));
            drvfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddDriverDriverURLTextFieldA11yName"));
            label.setLabelFor(drvfield);
            layout.setConstraints(drvfield, con);
            pane.add(drvfield);

            // Database prefix title and field

            label = new JLabel(bundle.getString("AddDriverDatabasePrefix")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("AddDriverDatabasePrefix_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddDriverDatabasePrefixA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 2;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            prefixfield = new JTextField(35);
            prefixfield.setToolTipText(bundle.getString("ACS_AddDriverDatabasePrefixTextFieldA11yDesc"));
            prefixfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddDriverDatabasePrefixTextFieldA11yName"));
            label.setLabelFor(prefixfield);
            layout.setConstraints(prefixfield, con);
            pane.add(prefixfield);

            // Blah blah about driver accessibility

            JTextArea notes = new JTextArea(bundle.getString("AddDriverURLNotes"), 2, 50); //NOI18N
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            notes.setFont(javax.swing.UIManager.getFont("Label.font"));  // NOI18N
            notes.setEditable(false);
            notes.setEnabled(false);
            notes.setOpaque(false);
            notes.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));  // NOI18N
            notes.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddDriverURLNotesA11yName"));
            notes.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddDriverURLNotesA11yDesc"));
            con.weightx = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 3;
            layout.setConstraints(notes, con);
            pane.add(notes);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    boolean dispcond = true;
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        result = true;
                        name = namefield.getText();
                        drv = drvfield.getText();
                        prefix = prefixfield.getText();
                        if (prefix == null)
                            prefix = ""; //NOI18N
                        dispcond = (drv != null && drv.trim().length() > 0 && name != null && name.trim().length() > 0);
                    } else
                        result = false;

                    if (dispcond) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    } else
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(bundle.getString("AddDriverErrorMessage"), NotifyDescriptor.ERROR_MESSAGE));
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddDriverDialogTitle"), true, listener); //NOI18N
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public DatabaseDriver getDriver() {
        return new DatabaseDriver(name, drv, prefix);
    }
}
