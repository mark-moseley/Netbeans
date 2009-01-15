/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.sql.ui;

import java.awt.Dialog;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.php.editor.codegen.DatabaseURL;
import org.netbeans.modules.php.editor.codegen.DatabaseURL.Server;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SelectConnectionPanel extends javax.swing.JPanel {

    private final boolean mySQLOnly;
    private final boolean passwordRequired;

    private DialogDescriptor descriptor;
    private DatabaseConnection dbconn;

    public static DatabaseConnection selectConnection(DatabaseConnection selectedDBConn, boolean mySQLOnly, boolean passwordRequired) {
        SelectConnectionPanel panel = new SelectConnectionPanel(selectedDBConn, mySQLOnly, passwordRequired);
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(SelectConnectionPanel.class, "MSG_SelectConnection"));
        desc.createNotificationLineSupport();
        panel.initialize(desc);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectConnectionPanel.class, "ACSD_SelectConnection"));
        dialog.setVisible(true);
        dialog.dispose();

        if (desc.getValue() == DialogDescriptor.OK_OPTION) {
            return panel.dbconn;
        }

        // If the user cancels, keep the selection they started with, rather than setting it to null
        return selectedDBConn;
    }

    private SelectConnectionPanel(DatabaseConnection dbconn, boolean mySQLOnly, boolean passwordRequired) {
        this.dbconn = dbconn;
        this.mySQLOnly = mySQLOnly;
        this.passwordRequired = passwordRequired;
        initComponents();
    }

    private void initialize(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        DatabaseExplorerUIs.connect(dbconnComboBox, ConnectionManager.getDefault());
        dbconnComboBox.setSelectedItem(dbconn);
        if (dbconn == null) {
            descriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(SelectConnectionPanel.class, "ERR_SelectConnection"));
        }
    }

    private void databaseConnectionChanged() {
        dbconn = null;
        Object selected = dbconnComboBox.getSelectedItem();
        if (!(selected instanceof DatabaseConnection)) {
            return;
        }
        dbconn = (DatabaseConnection) selected;
        
        DatabaseURL url = DatabaseURL.detect(dbconn.getDatabaseURL());
        String errorMessage = null;
        if (mySQLOnly && (url == null || url.getServer() != Server.MYSQL)) {
            errorMessage = NbBundle.getMessage(SelectConnectionPanel.class, "ERR_UnknownServer");
            dbconn = null;
        }
        if (dbconn != null) {
            if (passwordRequired && dbconn.getPassword() == null) {
                errorMessage = NbBundle.getMessage(SelectConnectionPanel.class, "ERR_NoPassword");
            }
        }
        setErrorMessage(errorMessage);
    }

    private void setErrorMessage(String message) {
        if (message == null) {
            descriptor.getNotificationLineSupport().clearMessages();
        } else {
            descriptor.getNotificationLineSupport().setErrorMessage(message);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dbconnLabel = new javax.swing.JLabel();
        dbconnComboBox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(dbconnLabel, org.openide.util.NbBundle.getMessage(SelectConnectionPanel.class, "SelectConnectionPanel.dbconnLabel.text")); // NOI18N

        dbconnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbconnComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dbconnLabel)
                    .add(dbconnComboBox, 0, 518, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(dbconnLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dbconnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dbconnComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectConnectionPanel.class, "ChooseConnectionPanel.dbconnComboBox.AccessibleContext.accessibleName")); // NOI18N
        dbconnComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectConnectionPanel.class, "ChooseConnectionPanel.dbconnComboBox.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void dbconnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbconnComboBoxActionPerformed
        databaseConnectionChanged();
}//GEN-LAST:event_dbconnComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dbconnComboBox;
    private javax.swing.JLabel dbconnLabel;
    // End of variables declaration//GEN-END:variables

}
