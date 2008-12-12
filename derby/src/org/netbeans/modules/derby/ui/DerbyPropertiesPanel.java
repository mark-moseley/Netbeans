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

package org.netbeans.modules.derby.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.RegisterDerby;
import org.netbeans.modules.derby.Util;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Despite the name, serves as a settings dialog for Derby (not only
 * system home, but also database location).
 * 
 * @author Andrei Badea
 */
public class DerbyPropertiesPanel extends javax.swing.JPanel {
    
    private DialogDescriptor descriptor;
    private Color nbErrorForeground;
   
    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validatePanel();
        }
    };
    
    public static boolean showDerbyProperties() {
        assert SwingUtilities.isEventDispatchThread();
        
        DerbyPropertiesPanel panel = new DerbyPropertiesPanel();
        String title = NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_SetDerbySystemHome");

        DialogDescriptor desc = new DialogDescriptor(panel, title);
        desc.createNotificationLineSupport();
        panel.setDialogDescriptor(desc);

        for (;;) {                    
            Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
            String acsd = NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel");
            dialog.getAccessibleContext().setAccessibleDescription(acsd);
            dialog.setVisible(true);
            dialog.dispose();

            if (!DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                return false; // NOI18N
            }

            File derbySystemHome = new File(panel.getDerbySystemHome());
            if (!derbySystemHome.exists()) {
                boolean success = derbySystemHome.mkdirs();
                if (!success) {
                    String message = NbBundle.getMessage(DerbyPropertiesPanel.class, "ERR_DerbySystemHomeCantCreate");
                    NotifyDescriptor ndesc = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(ndesc);
                    continue;
                }
            }
            new RegisterSampleDatabase();
            DerbyOptions.getDefault().setSystemHome(panel.getDerbySystemHome());
            DerbyOptions.getDefault().setLocation(panel.getInstallLocation());
            return true;
        }
    }
    
    private DerbyPropertiesPanel() {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        initComponents();
        derbyInstallInfo.setBackground(getBackground());
        derbySystemHomeTextField.getDocument().addDocumentListener(docListener);
        derbySystemHomeTextField.setText(DerbyOptions.getDefault().getSystemHome());
        derbyInstall.getDocument().addDocumentListener(docListener);
        derbyInstall.setText(DerbyOptions.getDefault().getLocation());
    }
    
    private void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validatePanel();
    }

    private String getDerbySystemHome() {
        return derbySystemHomeTextField.getText().trim();
    }
    
    private String getInstallLocation() {
        return derbyInstall.getText().trim();
    }
    
    private void setInstallLocation(String location) {
        derbyInstall.setText(location);
    }
    
    private void setDerbySystemHome(String derbySystemHome) {
        derbySystemHomeTextField.setText(derbySystemHome);
    }


    
    private void validatePanel() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        
        String location = getInstallLocation();
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", locationFile);
            }
            if (!Util.isDerbyInstallLocation(locationFile)) {
                error = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", locationFile);
            }
        }

        if (error == null) {
            File derbySystemHome = new File(getDerbySystemHome());
            if (derbySystemHome.getPath().length() <= 0) {
                warning = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotEntered");
            }

            if (derbySystemHome.exists() && !derbySystemHome.isDirectory()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotDirectory");
            } else if (!derbySystemHome.getPath().equals("") && !derbySystemHome.isAbsolute()) {
                error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DerbySystemHomeNotAbsolute");
            }
        }
        
        if (error != null) {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setErrorMessage(error);
        } else if (warning != null) {
            descriptor.setValid(false);
            descriptor.getNotificationLineSupport().setWarningMessage(warning);
        } else {
            descriptor.setValid(true);
            descriptor.getNotificationLineSupport().clearMessages();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        derbySystemHomeLabel = new javax.swing.JLabel();
        derbySystemHomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        installLabel = new javax.swing.JLabel();
        derbyInstall = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        derbyInstallInfo = new javax.swing.JTextPane();

        derbySystemHomeLabel.setLabelFor(derbySystemHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(derbySystemHomeLabel, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_DerbySystemHome")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        installLabel.setLabelFor(derbyInstall);
        org.openide.awt.Mnemonics.setLocalizedText(installLabel, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Install")); // NOI18N

        derbyInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derbyInstallActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_Browse2")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        derbyInstallInfo.setEditable(false);
        derbyInstallInfo.setText(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "LBL_InstallationInfo")); // NOI18N
        derbyInstallInfo.setAutoscrolls(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, derbyInstallInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(derbySystemHomeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(layout.createSequentialGroup()
                        .add(installLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(derbyInstall, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(derbyInstallInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(installLabel)
                    .add(derbyInstall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(derbySystemHomeLabel)
                    .add(derbySystemHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        derbySystemHomeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSN_CreateDatabasePanel_databaseLocationTextField")); // NOI18N
        derbySystemHomeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_browseButton")); // NOI18N
        derbyInstall.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSD_DerbySystemHomePanel_derbySystemHomeTextField")); // NOI18N
        derbyInstallInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DerbyPropertiesPanel.class, "ACSN_DerbySystemHomePanel_derbyInstallInfoTextField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String location = getInstallLocation();
        if (location.length() > 0) {
            chooser.setSelectedFile(new File(location));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setInstallLocation(chooser.getSelectedFile().getAbsolutePath());
}//GEN-LAST:event_jButton1ActionPerformed

    private void derbyInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derbyInstallActionPerformed
    // TODO add your handling code here:
    
}//GEN-LAST:event_derbyInstallActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        String derbySystemHome = getDerbySystemHome();
        if (derbySystemHome.length() > 0) {
            chooser.setSelectedFile(new File(derbySystemHome));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // NOI18N
        }
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setDerbySystemHome(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton browseButton;
    public javax.swing.JTextField derbyInstall;
    public javax.swing.JTextPane derbyInstallInfo;
    public javax.swing.JLabel derbySystemHomeLabel;
    public javax.swing.JTextField derbySystemHomeTextField;
    public javax.swing.JLabel installLabel;
    public javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
    
    
    private static class RegisterSampleDatabase {

        private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
        private static final String CONN_NAME = "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/sample [app on APP]";  // NOI18N
        private boolean registered;

        RegisterSampleDatabase() {
            if (JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET).length == 0) {
                JDBCDriverManager.getDefault().addDriverListener(jdbcDriverListener);
            }
        }
        private final JDBCDriverListener jdbcDriverListener = new JDBCDriverListener() {
            public void driversChanged() {
                registerDatabase();
            }
        };

        void registerDatabase() {
            synchronized (this) {
                if (registered) {
                    return;
                }

                // We do this ahead of time to prevent another thread from
                // double-registering the connections.
                registered = true;
            }

            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
                        if ((drvsArray.length > 0) && (ConnectionManager.getDefault().getConnection(CONN_NAME) == null)) {
                            DerbyDatabases.createSampleDatabase();
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (DatabaseException de) {
                        Exceptions.printStackTrace(de);
                    } finally {
                        JDBCDriverManager.getDefault().removeDriverListener(jdbcDriverListener);
                    }
                }
            });
        }
    }
}
