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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 * @author  Petr Slechta
 */
final class DatasourceComboBoxCustomizer extends javax.swing.JPanel {
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    private final HashMap<String, Datasource> datasources;
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public DatasourceComboBoxCustomizer(Set<Datasource> datasources) {
        this.datasources = new HashMap<String, Datasource>();
        if (datasources != null) { // transform Set to Map for faster searching
            for (Iterator it = datasources.iterator(); it.hasNext();) {
                Datasource datasource = (Datasource) it.next();
                if (datasource.getJndiName() != null)
                    this.datasources.put(datasource.getJndiName(), datasource);
            }
        }
        initComponents();
        
        DatabaseExplorerUIs.connect(connCombo, ConnectionManager.getDefault());

        connCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
        jndiNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verify();
            }
        });
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verify();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verify();
            }
            public void ancestorMoved(AncestorEvent event) {
                verify();
            }
        });
    }
    
    public boolean showDialog() {
        descriptor = new DialogDescriptor
            (this, NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DatasourceCustomizer"), true, // NOI18N
             DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
             DialogDescriptor.DEFAULT_ALIGN,
             new HelpCtx("DatasourceUIHelper_DatasourceCustomizer"), // NOI18N
             new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean close = true;
                    if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                        boolean valid = handleConfirmation();
                        close = valid;
                        dialogOK = valid;
                    }
                    if (close) {
                        dialog.dispose();
                    }
                 }
             });
        statusLine = descriptor.createNotificationLineSupport();
        descriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        
        verify();

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private boolean handleConfirmation() {
        jndiName = jndiNameField.getText().trim();

        DatabaseConnection conn = (DatabaseConnection)connCombo.getSelectedItem();
        if (conn.getPassword() == null) {
            ConnectionManager.getDefault().showConnectionDialog(conn);
        }
        if (conn.getPassword() == null) {
            //user did not provide the password
            statusLine.setErrorMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NoPassword")); // NOI18N
            return false;
        }
        url = conn.getDatabaseURL();
        username = conn.getUser();
        password = conn.getPassword();
        driverClassName = conn.getDriverClass();
        return true;
    }
    
    private boolean verify() {
        boolean isValid = verifyJndiName();
        if (isValid)
            isValid = verifyConnection();
        
        return isValid;
    }
    
    private boolean verifyJndiName() {
        boolean valid = true;
        
        String jndiName = jndiNameField.getText().trim();
        if (jndiName.length() == 0) {
            statusLine.setInformationMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_JNDI_NAME_EMPTY")); // NOI18N
            valid = false;
        }
        else
        if (datasourceAlreadyExists(jndiName)) {
            statusLine.setErrorMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_DS_EXISTS")); // NOI18N
            valid = false;
        }
        else {
            statusLine.clearMessages();
        }

        descriptor.setValid(valid);
        return valid;
    }
    
    private boolean verifyConnection() {
        boolean valid = true;
        
        if (!(connCombo.getSelectedItem() instanceof DatabaseConnection)) {
            statusLine.setInformationMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NO_CONN_SELECTED")); // NOI18N
            valid = false;
        }
        else {
            statusLine.clearMessages();
        }

        descriptor.setValid(valid);
        return valid;
    }
    
    // TODO this is incorrect - it is needed to normalize jndiName (e.g. "DefaultDs" vs. "java:DefaultDs" vs. "java:/DefaultDs")
    private boolean datasourceAlreadyExists(String jndiName) {
        return datasources.containsKey(jndiName);
    }
    
    String getJndiName() {
        return jndiName;
    }

    String getUrl() {
        return url;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getDriverClassName() {
        return driverClassName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jndiNameField = new javax.swing.JTextField();
        connCombo = new javax.swing.JComboBox();

        setForeground(new java.awt.Color(255, 0, 0));

        jLabel1.setLabelFor(jndiNameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_JndiName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_DbConn")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jndiNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .add(connCombo, 0, 337, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jndiNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(connCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jndiNameField;
    // End of variables declaration//GEN-END:variables
    
}
