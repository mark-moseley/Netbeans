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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.wizard;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.util.NbBundle;

import com.sun.collablet.Account;

/**
 *
 *
 */
public class AccountDisplayNamePanel extends WizardPanelBase {
    // Variables declaration - do not modify
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JLabel exampleLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel messageLabel;

    /**
     *
     *
     */
    public AccountDisplayNamePanel() {
        super(NbBundle.getMessage(AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_Name")); // NOI18N

        initComponents();
        initAccessibility();

        DocumentListener docListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    checkValidity();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkValidity();
                }
            };

        displayNameField.getDocument().addDocumentListener(docListener);
    }

    /**
     *
     *
     */
    public void readSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();

        displayNameField.setText(account.getDisplayName());

        if (account.getAccountType() == Account.EXISTING_ACCOUNT) {
            String msg = NbBundle.getMessage(
                    AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_ExistingAccount_Message"
                ); // NOI18N
            messageLabel.setText(msg);
        } else {
            String msg = NbBundle.getMessage(AccountDisplayNamePanel.class, "LBL_AccountDisplayNamePanel_Message"); // NOI18N
            messageLabel.setText(msg);
        }

        /*
                        if (account.getDisplayName() != null &&
                                account.getDisplayName().trim().length()>0)
                        {
                                displayNameField.setText(account.getDisplayName());
                        }
                        else
                        {
                                displayNameField.setText(
                                        NbBundle.getMessage(AccountDisplayNamePanel.class,
                                        "LBL_AccountDisplayNamePanel_Name"));
                                displayNameField.selectAll();
                        }
                        displayNameField.requestFocus();
         */
    }

    /**
     *
     *
     */
    public void storeSettings(Object object) {
        if (object instanceof AccountWizardSettings) {
            Account account = AccountWizardSettings.narrow(object).getAccount();
            account.setDisplayName(displayNameField.getText().trim());
        }
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        setValid(displayNameField.getText().trim().length() > 0);
    }

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    displayNameField.requestFocus();
                }
            }
        );
    }

    public void initAccessibility() {
        displayNameLabel.setDisplayedMnemonic(NbBundle.getMessage(AccountUserInfoPanel.class,
            "LBL_AccountDisplayNamePanel_ServerURL_Mnemonic").charAt(0));
        displayNameLabel.setLabelFor(displayNameField);
        exampleLabel.setLabelFor(null);
        messageLabel.setLabelFor(null);

        displayNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountDisplayNamePanel_DisplayNameField")
        ); // NOI18N
        displayNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountDisplayNamePanel_DisplayNameField")
        ); // NOI18N       
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        exampleLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                null, new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        jPanel2.setLayout(new java.awt.GridBagLayout());

        messageLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountDisplayNamePanel_Message"
            )
        );
        messageLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel2.add(messageLabel, gridBagConstraints);

        displayNameLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountDisplayNamePanel_ServerURL"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel2.add(displayNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel2.add(displayNameField, gridBagConstraints);

        exampleLabel.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "MSG_AccountDisplayNamePanel_Example"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(exampleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }//GEN-END:initComponents

    // End of variables declaration                   
}
