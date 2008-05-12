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

/*
 * HibernateConfigurationWizardPanel.java
 *
 * Created on January 9, 2008, 4:26 PM
 */
package org.netbeans.modules.hibernate.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.hibernate.framework.HibernateWebModuleExtender;

/**
 *
 * @author  gowri
 */
public class HibernateConfigurationWizardPanel extends javax.swing.JPanel implements DocumentListener, ItemListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private HibernateWebModuleExtender webModuleExtender;
    private ExtenderController controller;
    private boolean forNewProjectWizard = false;

    /** Creates new form HibernateConfigurationWizardPanel */
    public HibernateConfigurationWizardPanel() {
        initComponents();
        setDefaults();
    }

    public void setDefaults() {
        cmbDbConnection.setModel(new javax.swing.DefaultComboBoxModel(new String[0]));
        DatabaseExplorerUIs.connect(cmbDbConnection, ConnectionManager.getDefault());
    }

    public HibernateConfigurationWizardPanel(HibernateWebModuleExtender webModuleExtender,
            ExtenderController controller, boolean forNewProjectWizard) {
        this.webModuleExtender = webModuleExtender;
        this.controller = controller;
        this.forNewProjectWizard = forNewProjectWizard;
        initComponents();
        setDefaults();
        fillPanel();
        txtSessionName.getDocument().addDocumentListener(this);
        cmbDbConnection.addItemListener(this);
    }

    public void fillPanel() {
        if (forNewProjectWizard) {
            if (cmbDbConnection.getItemCount() != 0 && cmbDbConnection.getItemCount() >= 1) {
                cmbDbConnection.setSelectedIndex(1);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "LBL_HibernateConfigurationPanel_Name");
    }

    private void fillComponents() {
        DatabaseConnection dbConn = getDatabaseConnection();
        if (dbConn != null  && cmbDbConnection.getItemCount() != 0) {
            txtDialect.setText(Util.getDialectName(dbConn.getDriverClass()));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbDbConnection = new javax.swing.JComboBox();
        txtDialect = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtSessionName = new javax.swing.JTextField();

        setName(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "LBL_HibernateConfigurationPanel_Name")); // NOI18N

        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hibernate/wizards/Bundle").getString("Dialect_mnemonic").charAt(0));
        jLabel4.setLabelFor(txtDialect);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.jLabel4.text")); // NOI18N

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hibernate/wizards/Bundle").getString("DatabaseConnection_mnemonic").charAt(0));
        jLabel3.setLabelFor(cmbDbConnection);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.jLabel3.text")); // NOI18N

        cmbDbConnection.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbDbConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDbConnectionActionPerformed(evt);
            }
        });

        txtDialect.setEditable(false);
        txtDialect.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.txtDialect.text")); // NOI18N

        jLabel7.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/hibernate/wizards/Bundle").getString("SessionName_mnemonic").charAt(0));
        jLabel7.setLabelFor(txtSessionName);
        jLabel7.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.jLabel7.text")); // NOI18N

        txtSessionName.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.txtSessionName.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(txtSessionName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(26, 26, 26)
                        .add(txtDialect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cmbDbConnection, 0, 542, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtSessionName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cmbDbConnection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(txtDialect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HibernateConfigurationWizardPanel.class, "HibernateConfigurationWizardPanel.jLabel4.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void cmbDbConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDbConnectionActionPerformed
        // TODO add your handling code here:
        fillComponents();
    }//GEN-LAST:event_cmbDbConnectionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbDbConnection;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField txtDialect;
    private javax.swing.JTextField txtSessionName;
    // End of variables declaration//GEN-END:variables
    public void actionPerformed(ActionEvent e) {
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void insertUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void removeUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public void itemStateChanged(ItemEvent e) {
        webModuleExtender.fireChangeEvent();
    }

    public String getSessionName() {
        if (txtSessionName.getText() != null) {
            return txtSessionName.getText().trim();
        }
        return null;
    }

    public void setSessionName(String newSessionName) {
        txtSessionName.setText(newSessionName);
    }

    public String getSelectedDialect() {
        if (txtDialect.getText() != null) {
            return txtDialect.getText().trim();
        }
        return null;
    }

    public void setDialect(String dialectName) {
        txtDialect.setText(dialectName);
    }

    public String getSelectedDriver() {
        if (getDatabaseConnection() != null && getDatabaseConnection().getDriverClass() != null) {
            return getDatabaseConnection().getDriverClass().trim();
        }
        return null;
    }

    public String getSelectedURL() {
        if (getDatabaseConnection() != null && getDatabaseConnection().getDatabaseURL() != null) {
            return getDatabaseConnection().getDatabaseURL().trim();
        }
        return null;
    }

    public String getUserName() {
        if (getDatabaseConnection() != null && getDatabaseConnection().getUser() != null) {
            return getDatabaseConnection().getUser().trim();
        }
        return null;
    }

    public String getPassword() {
        if (getDatabaseConnection() != null && getDatabaseConnection().getPassword() != null) {
            return getDatabaseConnection().getPassword().trim();
        }
        return null;
    }

    public DatabaseConnection getDatabaseConnection() {
        return (DatabaseConnection) cmbDbConnection.getSelectedItem();

    }

    public void setDatabaseConnection(String dbConnURL) {
        for (int i = 0; i < cmbDbConnection.getItemCount(); i++) {
            if (cmbDbConnection.getItemAt(i) instanceof DatabaseConnection) {
                DatabaseConnection conn = (DatabaseConnection) cmbDbConnection.getItemAt(i);
                if (conn.getDatabaseURL().equals(dbConnURL)) {                
                    cmbDbConnection.setSelectedItem(conn);
                    break;
                }
            }
        }        
    }

    public boolean isPanelValid() {
        return true;
    }

    @Override
    public void disable() {
        super.disable();
        for (Component component : this.getComponents()) {
            component.setEnabled(false);
        }
    }
}
