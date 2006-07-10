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
public class AccountUserInfoPanel extends WizardPanelBase {
    // Variables declaration - do not modify
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLbl;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JLabel firstNameLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JLabel lastNameLbl;

    /**
     *
     *
     */
    public AccountUserInfoPanel() {
        super(NbBundle.getMessage(AccountUserInfoPanel.class, "LBL_AccountUserInfoPanel_Name")); // NOI18N

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

        firstNameField.getDocument().addDocumentListener(docListener);
        lastNameField.getDocument().addDocumentListener(docListener);
        emailField.getDocument().addDocumentListener(docListener);
    }

    /**
     *
     *
     */
    public void readSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailField.setText(account.getEmail());
    }

    /**
     *
     *
     */
    public void storeSettings(Object object) {
        Account account = AccountWizardSettings.narrow(object).getAccount();
        account.setFirstName(firstNameField.getText().trim());
        account.setLastName(lastNameField.getText().trim());
        account.setEmail(emailField.getText().trim());
    }

    /**
     *
     *
     */
    protected void checkValidity() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        boolean valid = (firstName.length() > 0) && (lastName.length() > 0) && (email.length() > 0);
        setValid(valid);
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
                    firstNameField.requestFocus();
                }
            }
        );
    }

    public void initAccessibility() {
        emailLbl.setDisplayedMnemonic(NbBundle.getMessage(AccountUserInfoPanel.class,
            "LBL_AccountUserInfoPanel_Email_Mnemonic").charAt(0));
        emailLbl.setLabelFor(emailField);
        firstNameLbl.setDisplayedMnemonic(NbBundle.getMessage(AccountUserInfoPanel.class,
            "LBL_AccountUserInfoPanel_FirstName_Mnemonic").charAt(0));
        firstNameLbl.setLabelFor(firstNameField);
        jLabel1.setLabelFor(null);
        lastNameLbl.setDisplayedMnemonic(NbBundle.getMessage(AccountUserInfoPanel.class,
            "LBL_AccountUserInfoPanel_LastName_Mnemonic").charAt(0));
        lastNameLbl.setLabelFor(lastNameField);

        emailField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_EmailField")
        ); // NOI18N
        firstNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_FirstNameField")
        ); // NOI18N
        lastNameField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_DESC_AccountUserInfoPanel_LastNameField")
        ); // NOI18N   

        emailField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_EmailField")
        ); // NOI18N
        firstNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_FirstNameField")
        ); // NOI18N
        lastNameField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AccountUserInfoPanel.class, "ACSD_NAME_AccountUserInfoPanel_LastNameField")
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        firstNameLbl = new javax.swing.JLabel();
        firstNameField = new javax.swing.JTextField();
        lastNameLbl = new javax.swing.JLabel();
        lastNameField = new javax.swing.JTextField();
        emailLbl = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(
            new javax.swing.border.CompoundBorder(
                null, new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))
            )
        );
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountUserInfoPanel_Message"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        firstNameLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountUserInfoPanel_FirstName"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(firstNameLbl, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(firstNameField, gridBagConstraints);

        lastNameLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountUserInfoPanel_LastName"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(lastNameLbl, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(lastNameField, gridBagConstraints);

        emailLbl.setText(
            java.util.ResourceBundle.getBundle("org/netbeans/modules/collab/ui/wizard/Bundle").getString(
                "LBL_AccountUserInfoPanel_Email"
            )
        );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(emailLbl, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        jPanel1.add(emailField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }//GEN-END:initComponents

    // End of variables declaration                   
}
