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
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;

import org.netbeans.lib.ddl.impl.CreateView;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.*;

import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.*;

public class AddViewDialog {
    boolean result = false;
    Dialog dialog = null;
    JTextField namefld;
    JTextArea tarea;

    public AddViewDialog(final Specification spec, final DatabaseNodeInfo info) {
        try {
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Index name

            JLabel label = new JLabel(bundle.getString("AddViewName")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("AddViewName_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddViewNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Index name field

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefld = new JTextField(35);
            namefld.setToolTipText(bundle.getString("ACS_AddViewNameTextFieldA11yDesc"));
            namefld.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddViewNameTextFieldA11yName"));
            label.setLabelFor(namefld);
            layout.setConstraints(namefld, con);
            pane.add(namefld);

            // Items list title

            label = new JLabel(bundle.getString("AddViewLabel")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("AddViewLabel_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddViewLabelA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            // Editor list

            tarea = new JTextArea(5,50);
            tarea.setToolTipText(bundle.getString("ACS_AddViewTextAreaA11yDesc"));
            tarea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddViewTextAreaA11yName"));
            label.setLabelFor(tarea);

            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 2;
            JScrollPane spane = new JScrollPane(tarea);
            layout.setConstraints(spane, con);
            pane.add(spane);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        
                        try {
                            CreateView cmd = spec.createCommandCreateView(getViewName());
                            cmd.setQuery(getViewCode());
                            cmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
                            cmd.execute();
                            result = !cmd.wasException();
                            
                            if (!cmd.wasException()) {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                        } catch (CommandNotSupportedException e) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                        } catch (DDLException e) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                        } catch (Exception e) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddViewTitle"), true, listener); //NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public void setViewName(String name)
    {
        namefld.setText(name);
    }

    public String getViewName()
    {
        return namefld.getText();
    }

    public String getViewCode()
    {
        return tarea.getText();
    }
}
