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
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;

import org.netbeans.lib.ddl.DBConnection;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.util.DriverListUtil;

import org.openide.util.NbBundle;

public class NewConnectionPanel extends javax.swing.JPanel implements DocumentListener, ListDataListener {

    private ConnectionDialogMediator mediator;
    private Vector templates;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;

    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    public NewConnectionPanel(ConnectionDialogMediator mediator, Vector templates, DatabaseConnection connection) {
        this.mediator = mediator;
        Vector wrapperTemplates = new Vector();
        for (int i = 0; i < templates.size(); i++) {
            wrapperTemplates.add(new DriverWrapper((JDBCDriver)templates.elementAt(i)));
        }
        this.templates = wrapperTemplates;
        this.connection = connection;
        initComponents();
        initAccessibility();
        
        ConnectionProgressListener progressListener = new ConnectionProgressListener() {
            public void connectionStarted() {
                startProgress();
            }
            
            public void connectionStep(String step) {
                setProgressMessage(step);
            }

            public void connectionFinished() {
                stopProgress(true);
            }

            public void connectionFailed() {
                stopProgress(false);
            }
        };
        mediator.addConnectionProgressListener(progressListener);
        
        driverTextField.setText(connection.getDriver());
//        urlTextField.setText(connection.getDatabase());       
        urlComboBox.setSelectedItem(connection.getDatabase());
        userTextField.setText(connection.getUser());

        String driver = connection.getDriver();
        String driverName = connection.getDriverName();
        if (driver != null && driverName != null) {
            JDBCDriver dbDriver;
            for (int i = 0; i < templates.size(); i++) {
                dbDriver = (JDBCDriver) templates.elementAt(i);
                if (dbDriver.getClassName().equals(driver) && dbDriver.getName().equals(driverName)) {
                    templateComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        driverTextField.getDocument().addDocumentListener(this);
        userTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        templateComboBox.getModel().addListDataListener(this);
        urlComboBox.getModel().addListDataListener(this);
    }

    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(BUNDLE);
        templateLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        driverLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDriverClassA11yDesc")); //NOI18N
        driverTextField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDriverClassComboBoxA11yName")); //NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDatabaseURLA11yDesc")); //NOI18N
//        urlTextField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDatabaseURLTextFieldA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userTextField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleName(b.getString("ACS_ConnectionProgressBarA11yName")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_ConnectionProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        templateLabel = new javax.swing.JLabel();
        templateComboBox = new javax.swing.JComboBox(templates);
        driverLabel = new javax.swing.JLabel();
        driverTextField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlComboBox = new javax.swing.JComboBox();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        passwordCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        connectProgressPanel = new javax.swing.JPanel();
        progressMessageLabel = new javax.swing.JLabel();
        progressContainerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        templateLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName_Mnemonic").charAt(0));
        templateLabel.setLabelFor(templateComboBox);
        templateLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(templateLabel, gridBagConstraints);

        templateComboBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameComboBoxA11yDesc"));
        templateComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                templateComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(templateComboBox, gridBagConstraints);

        driverLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverClass_Mnemonic").charAt(0));
        driverLabel.setLabelFor(driverTextField);
        driverLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(driverLabel, gridBagConstraints);

        driverTextField.setColumns(50);
        driverTextField.setEditable(false);
        driverTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverClassComboBoxA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(driverTextField, gridBagConstraints);

        urlLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL_Mnemonic").charAt(0));
        urlLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(urlLabel, gridBagConstraints);

        urlComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(urlComboBox, gridBagConstraints);

        userLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName_Mnemonic").charAt(0));
        userLabel.setLabelFor(userTextField);
        userLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(userLabel, gridBagConstraints);

        userTextField.setColumns(50);
        userTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(userTextField, gridBagConstraints);

        passwordLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword_Mnemonic").charAt(0));
        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passwordLabel, gridBagConstraints);

        passwordField.setColumns(50);
        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordField, gridBagConstraints);

        passwordCheckBox.setMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword_Mnemonic").charAt(0));
        passwordCheckBox.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword"));
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionRememberPasswordA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        connectProgressPanel.setLayout(new java.awt.BorderLayout(0, 5));

        connectProgressPanel.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc"));
        progressMessageLabel.setText(" ");
        connectProgressPanel.add(progressMessageLabel, java.awt.BorderLayout.NORTH);

        progressContainerPanel.setLayout(new java.awt.BorderLayout());

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        connectProgressPanel.add(progressContainerPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(connectProgressPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed
        JDBCDriver drv = ((DriverWrapper) templateComboBox.getSelectedItem()).getDriver();
        List urls = null;
        String driver = null;
        if (drv != null) {
           driver = drv.getClassName();           
           urls = DriverListUtil.getURLs(driver);
        }
        
        urlComboBox.removeAllItems();
        if (urls != null)
            for (int i = 0; i < urls.size(); i++)
                urlComboBox.addItem((String) urls.get(i));
        
        if (driver != null)
           driverTextField.setText(driver);
    }//GEN-LAST:event_templateComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectProgressPanel;
    private javax.swing.JLabel driverLabel;
    private javax.swing.JTextField driverTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel progressMessageLabel;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JComboBox urlComboBox;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    private String getSelectedDriver() {
        return ((DriverWrapper) templateComboBox.getSelectedItem()).getDriver().getClassName();
    }

    public void setConnectionInfo() {
        connection.setDriver(getSelectedDriver());
        connection.setDatabase((String) urlComboBox.getSelectedItem());
        connection.setUser(userTextField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
    }

    public DBConnection getConnection() {
        return connection;
    }

    public String getDriver() {
        return getSelectedDriver();
    }

    public String getDatabase() {
        return (String) urlComboBox.getSelectedItem();
    }

    public String getUser() {
        return userTextField.getText();
    }

    public String getPassword() {
        String password;
        String tempPassword = new String(passwordField.getPassword());
        if (tempPassword.length() > 0)
            password = tempPassword;
        else
            password = null;

        return password;
    }

    public boolean rememberPassword() {
        return passwordCheckBox.isSelected();
    }

    public String getTitle() {
        return NbBundle.getBundle(BUNDLE).getString("NewConnectionDialogTitle"); //NOI18N
    }

    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getBundle(BUNDLE).getString("ConnectionProgress_Connecting"));
            }
        });
    }
    
    private void setProgressMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressMessageLabel.setText(message);
            }
        });
    }

    private void stopProgress(final boolean connected) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle.finish();
                progressContainerPanel.remove(progressComponent);
                // without this, the removed progress component remains painted on its parent... why?
                progressContainerPanel.repaint();
                if (connected) {
                    progressMessageLabel.setText(NbBundle.getBundle(BUNDLE).getString("ConnectionProgress_Established"));
                } else {
                    progressMessageLabel.setText(NbBundle.getBundle(BUNDLE).getString("ConnectionProgress_Failed"));
                }
            }
        });
    }
    
    private void resetProgress() {
        progressMessageLabel.setText(""); // NOI18N
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
        resetProgress();
    }
    
    private static final class DriverWrapper {
        
        private JDBCDriver driver;
        
        public DriverWrapper(JDBCDriver driver) {
            this.driver = driver;
        }
        
        public JDBCDriver getDriver() {
            return driver;
        }
        
        public String toString() {
            return driver.getName();
        }
    }
}
