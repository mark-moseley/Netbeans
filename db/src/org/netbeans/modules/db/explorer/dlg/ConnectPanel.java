/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseConnection;

public class ConnectPanel extends ConnectionDialog.FocusablePanel implements DocumentListener, ListDataListener {

    private ConnectionDialogMediator mediator;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;

    /**
     * Creates new form ConnectPanel
     * 
     * @param connection instance of DatabaseConnection object
     */
    public ConnectPanel(ConnectionDialogMediator mediator, DatabaseConnection connection) {
        this.mediator = mediator;
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

        userTextField.setText(connection.getUser());
        if (connection.rememberPassword()) {
            passwordField.setText(connection.getPassword());
            passwordCheckBox.setSelected(true);
        }

        userTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
    }

    private void initAccessibility() {
        userLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameA11yDesc")); //NOI18N
        userTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordTextFieldA11yName")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yName")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); //NOI18N
    }

    public void initializeFocus() {
        getInitiallyFocusedComponent().requestFocusInWindow();
    }

    private JComponent getInitiallyFocusedComponent() {
        if (userTextField.getText().length() == 0) {
            return userTextField;
        }
        if (passwordField.getPassword().length == 0) {
            return passwordField;
        }
        // fall back to the user field
        return userTextField;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();
        userTextField = new javax.swing.JTextField();
        connectProgressPanel = new javax.swing.JPanel();
        progressMessageLabel = new javax.swing.JLabel();
        progressContainerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogUserName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(userLabel, gridBagConstraints);

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogPassword")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passwordLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectDialogRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogRememberPasswordA11yDesc")); // NOI18N
        passwordCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordCheckBox, gridBagConstraints);

        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogPasswordTextFieldA11yDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordField, gridBagConstraints);

        userTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectDialogUserNameTextFieldA11yDesc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(userTextField, gridBagConstraints);

        connectProgressPanel.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); // NOI18N
        connectProgressPanel.setLayout(new java.awt.BorderLayout(0, 5));

        org.openide.awt.Mnemonics.setLocalizedText(progressMessageLabel, " ");
        connectProgressPanel.add(progressMessageLabel, java.awt.BorderLayout.NORTH);

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());
        connectProgressPanel.add(progressContainerPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(connectProgressPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectProgressPanel;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel progressMessageLabel;
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Connecting"));
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
                    progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Established"));
                } else {
                    progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Failed"));
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
}
