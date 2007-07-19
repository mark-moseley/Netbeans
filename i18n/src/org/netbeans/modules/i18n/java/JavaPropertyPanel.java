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

package org.netbeans.modules.i18n.java;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.netbeans.modules.i18n.PropertyPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Property panel for {@code JavaI18nString}'s.
 *
 * @author  Peter Zavadsky
 */
public class JavaPropertyPanel extends PropertyPanel {

    private final ResourceBundle bundle;
    
    /** Creates new form JavaPropertyPanel */
    public JavaPropertyPanel() {
        bundle = NbBundle.getBundle(JavaPropertyPanel.class);
        initComponents();
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        argumentsButton.setVisible(true);
        argumentsButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_CTL_Arguments"));                 //NOI18N
        argumentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                argumentsButtonActionPerformed(evt);
            }
        }
        );
    }

    /** Action handler for arguments button. */
    private void argumentsButtonActionPerformed(ActionEvent evt) {
        final JavaI18nString javaI18nString = (JavaI18nString)i18nString;
        
        final Dialog[] dialogs = new Dialog[1];
        final ParamsPanel paramsPanel = new ParamsPanel();

        paramsPanel.setArguments(javaI18nString.getArguments());

        DialogDescriptor dd = new DialogDescriptor(
            paramsPanel,
            bundle.getString("CTL_ParamsPanelTitle"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        javaI18nString.setArguments(paramsPanel.getArguments());
                        updateReplaceText();
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }
           });
        dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
    }

    /** Overrides superclass method. */
    @Override
    protected void updateReplaceText() {
        super.updateReplaceText();
        
        argumentsButton.setEnabled(
                i18nString.getReplaceFormat().indexOf("{arguments}") >= 0 ); // NOI18N
    }

}
