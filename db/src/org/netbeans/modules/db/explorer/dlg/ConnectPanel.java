/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseConnection;

public class ConnectPanel extends javax.swing.JPanel implements DocumentListener, ListDataListener {

    private DatabaseConnection connection;

    /** Creates new form ConnectPanel
     * @deprecated use ConnectPanel(DatabaseConnection connection)
     */
    public ConnectPanel(String loginname) {
    }
    
    /** Creates new form ConnectPanel
     * @param connection instance of DatabaseConnection object
     */
    public ConnectPanel(DatabaseConnection connection) {
        this.connection = connection;
        initComponents();
        connectProgressBar.setBorderPainted(false);
        initAccessibility();

        PropertyChangeListener connectionListener = new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (event.getPropertyName().equals("connecting")) { //NOI18N
                            startProgress();
                        }
                        if (event.getPropertyName().equals("connected")) { //NOI18N
                            stopProgress(true);
                        }
                        if (event.getPropertyName().equals("failed")) { //NOI18N
                            stopProgress(false);
                        }
                    }
                });
            }
        };
        this.connection.addPropertyChangeListener(connectionListener);

        userTextField.setText(connection.getUser());

        userTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
    }

    private void initAccessibility() {
        userLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameA11yDesc")); //NOI18N
        userTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordTextFieldA11yName")); //NOI18N
        connectProgressBar.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yName")); //NOI18N
        connectProgressBar.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();
        userTextField = new javax.swing.JTextField();
        connectProgressBar = new javax.swing.JProgressBar();

        setLayout(new java.awt.GridBagLayout());

        userLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogUserName_Mnemonic").charAt(0));
        userLabel.setLabelFor(userTextField);
        userLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogUserName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(userLabel, gridBagConstraints);

        passwordLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogPassword_Mnemonic").charAt(0));
        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passwordLabel, gridBagConstraints);

        passwordCheckBox.setMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogRememberPassword_Mnemonic").charAt(0));
        passwordCheckBox.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogRememberPassword"));
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogRememberPasswordA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordCheckBox, gridBagConstraints);

        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordField, gridBagConstraints);

        userTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(userTextField, gridBagConstraints);

        connectProgressBar.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc"));
        connectProgressBar.setString("");
        connectProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(connectProgressBar, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar connectProgressBar;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public String getUser() {
        return userTextField.getText();
    }

    public String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }
	
	public void showRememberPasswordOption(boolean flag) {
		passwordCheckBox.setVisible(flag);
	}

    public boolean rememberPassword() {
        return passwordCheckBox.isSelected();
    }

    public String getTitle() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogTitle"); // NOI18N
    }

    private void startProgress() {
        connectProgressBar.setBorderPainted(true);
        connectProgressBar.setIndeterminate(true);
        connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Connecting")); //NOI18N
    }

    private void stopProgress(boolean connected) {
        connectProgressBar.setIndeterminate(false);
        if (connected) {
            connectProgressBar.setValue(connectProgressBar.getMaximum());
            connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Established")); //NOI18N
        } else {
            connectProgressBar.setValue(connectProgressBar.getMinimum());
            connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Failed")); //NOI18N
        }
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void contentsChanged(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    private void fireChange() {
        firePropertyChange("argumentChanged", null, null);
        connectProgressBar.setBorderPainted(false);
        connectProgressBar.setValue(connectProgressBar.getMinimum());
        connectProgressBar.setString(""); //NOI18N
    }
}
