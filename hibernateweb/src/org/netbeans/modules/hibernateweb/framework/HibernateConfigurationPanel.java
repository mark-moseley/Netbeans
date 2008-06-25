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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernateweb.framework;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.hibernate.wizards.Util;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.openide.util.NbBundle;

/**
 * Panel UI for Framework support for Hibernate. 
 * 
 * @author  Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateConfigurationPanel extends javax.swing.JPanel implements DocumentListener, ItemListener {

    private HibernateWebModuleExtender webModuleExtender;
    private ExtenderController controller;
    private boolean forNewProjectWizard = false;

    /** Creates new form HibernateConfigurationPanel */
    public HibernateConfigurationPanel(HibernateWebModuleExtender webModuleExtender,
            ExtenderController controller, boolean forNewProjectWizard) {
        this.webModuleExtender = webModuleExtender;
        this.controller = controller;
        this.forNewProjectWizard = forNewProjectWizard;
        initComponents();
        setDefaults();
        fillPanel();
        cmbDbConnection.addItemListener(this);
    }

    public void setDefaults() {
        cmbDbConnection.setModel(new javax.swing.DefaultComboBoxModel(new String[0]));
        DatabaseExplorerUIs.connect(cmbDbConnection, ConnectionManager.getDefault());
    }

    public void fillPanel() {
        if (forNewProjectWizard) {
            if (cmbDbConnection.getItemCount() != 0 && cmbDbConnection.getItemCount() > 1) {
                cmbDbConnection.setSelectedIndex(1);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(HibernateConfigurationPanel.class, "LBL_HibernateConfigurationPanel_Name");
    }

    private void fillComponents() {
        DatabaseConnection dbConn = getDatabaseConnection();
        if (dbConn != null && cmbDbConnection.getItemCount() != 0) {
            txtDialect.setText(Util.getDialectName(dbConn.getDriverClass()));
        }
    }

    public boolean isPanelValid() {
        if (forNewProjectWizard) { // Validate only in case of New Project Wizard.
        }
        return true;
    }

    @Override
    public void disable() {
        super.disable();
        for (Component component : this.getComponents()) {
            component.setEnabled(false);
        }
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        cmbDbConnection = new javax.swing.JComboBox();
        txtDialect = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();

        jLabel3.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.jLabel3.text")); // NOI18N

        cmbDbConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDbConnectionActionPerformed(evt);
            }
        });

        txtDialect.setEditable(false);
        txtDialect.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.txtDialect.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(HibernateConfigurationPanel.class, "HibernateConfigurationPanel.jLabel4.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(txtDialect)
                    .add(cmbDbConnection, 0, 281, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cmbDbConnection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(txtDialect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void cmbDbConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDbConnectionActionPerformed
// TODO add your handling code here:
    fillComponents();
}//GEN-LAST:event_cmbDbConnectionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbDbConnection;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtDialect;
    // End of variables declaration//GEN-END:variables
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
}
