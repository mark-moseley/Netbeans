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

package org.apache.tools.ant.module.bridge.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author David Konecny, Dusan Balek, Jesse Glick
 */
final class NbInputHandler implements InputHandler {
    
    private JComboBox combo = null;
    private JTextField input = null;
    
    public void handleInput(InputRequest request) throws BuildException {
        
        // #30196 - for one Ant script containing several <input> tasks there will be created
        // just one instance of the NbInputHandler. So it is necessary to cleanup the instance
        // used by the previous <input> task first.
        combo = null;
        input = null;
        
        JPanel panel = createPanel(request);
        DialogDescriptor dlg = new DialogDescriptor(panel,
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); //NOI18N
        do {
            DialogDisplayer.getDefault().createDialog(dlg).show();
            if (dlg.getValue() != NotifyDescriptor.OK_OPTION) {
                throw new BuildException(NbBundle.getMessage(NbInputHandler.class, "MSG_input_aborted")); //NOI18N
            }
            String value;
            if (combo != null) {
                value = (String) combo.getSelectedItem();
            } else {
                value = input.getText();
            }
            request.setInput(value);
        } while (!request.isInputValid());
    }
    
    private JPanel createPanel(InputRequest request) {
        
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        
        JLabel jLabel1 = new javax.swing.JLabel(request.getPrompt());
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 6);
        pane.add(jLabel1, gridBagConstraints);
        
        JComponent comp = null;
        if (request instanceof MultipleChoiceInputRequest) {
            combo = new javax.swing.JComboBox(((MultipleChoiceInputRequest)request).getChoices());
            comp = combo;
        } else {
            input = new JTextField(25);
            comp = input;
        }
        
        comp.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 11, 6);
        pane.add(comp, gridBagConstraints);
        
        jLabel1.setLabelFor(comp);
        if (request.getPrompt().length() > 0)
            jLabel1.setDisplayedMnemonic(request.getPrompt().charAt(0));
        
        pane.getAccessibleContext().setAccessibleName(
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); // NOI18N
        pane.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N
        
        HelpCtx.setHelpIDString(pane, "org.apache.tools.ant.module.run.NBInputHandler"); // NOI18N
        
        return pane;
    }
    
}
