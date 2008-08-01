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

package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  gordonp
 */
public class AddServerDialog extends JPanel implements DocumentListener {
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private boolean valid;
    private final JButton btnOK;
    /** Creates new form AddServerDialog */
    public AddServerDialog() {
        initComponents();
        btnOK = new JButton(NbBundle.getMessage(AddServerDialog.class, "BTN_OK"));
        btnOK.setEnabled(false);
        valid = false;
    }
    
    public boolean createNewRecord() {
        DialogDescriptor dd = new DialogDescriptor((Object) this, NbBundle.getMessage(EditServerListDialog.class, "TITLE_AddNewServer"), true, 
                    new Object[] { btnOK, DialogDescriptor.CANCEL_OPTION},
                    btnOK, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        return dd.getValue() == btnOK;
    }

    public String getServerName() {
        return tfServer.getText();
    }
    
    public String getLoginName() {
        return tfLogin.getText();
    }
    
    public String getPassword() {
        return String.valueOf(tfPassword.getPassword());
    }
    
    public boolean isDefault() {
        return cbxSetAsDefault.isSelected();
    }
    
    public boolean isOkValid() {
        return valid;
    }
    
    public void insertUpdate(DocumentEvent e) {
        boolean ovalid = valid;
        valid = tfServer.getText().length() > 0 && tfLogin.getText().length() > 0;
        if (valid != ovalid) {
            firePropertyChange(PROP_VALID, ovalid, valid);            
            if (btnOK != null) {
                btnOK.setEnabled(valid);
            }
        }
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
    }
    
    public class PasswordSourceModel extends DefaultComboBoxModel {
        
        public PasswordSourceModel() {
            addElement(NbBundle.getMessage(AddServerDialog.class, "LBL_PSM_TypeitOnce"));
            addElement(NbBundle.getMessage(AddServerDialog.class, "LBL_PSM_TypeitAlways"));
            
            for (RemoteServerRecord record : RemoteServerList.getInstance()) {
                String user = record.getUserName();
                if (user != null) {
                    addElement(NbBundle.getMessage(AddServerDialog.class, "FMT_SharedPasswordSource", record.getServerName(), user));
                }
            }
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

        lbServer = new javax.swing.JLabel();
        tfServer = new javax.swing.JTextField();
        tfServer.getDocument().addDocumentListener(this);
        lbLogin = new javax.swing.JLabel();
        tfLogin = new javax.swing.JTextField();
        tfLogin.getDocument().addDocumentListener(this);
        lbPasswordSource = new javax.swing.JLabel();
        cbPasswordSource = new javax.swing.JComboBox();
        cbxSetAsDefault = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        tfPassword = new javax.swing.JPasswordField();

        lbServer.setLabelFor(tfServer);
        org.openide.awt.Mnemonics.setLocalizedText(lbServer, org.openide.util.NbBundle.getMessage(AddServerDialog.class, "LBL_ServerTF")); // NOI18N
        lbServer.setToolTipText(org.openide.util.NbBundle.getMessage(AddServerDialog.class, "DESC_ServerTF")); // NOI18N

        lbLogin.setLabelFor(tfLogin);
        org.openide.awt.Mnemonics.setLocalizedText(lbLogin, org.openide.util.NbBundle.getMessage(AddServerDialog.class, "LBL_LoginTF")); // NOI18N
        lbLogin.setToolTipText(org.openide.util.NbBundle.getMessage(AddServerDialog.class, "DESC_LoginTF")); // NOI18N

        tfLogin.setText(System.getProperty("user.name"));
        tfLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                onLoginFocus(evt);
            }
        });

        lbPasswordSource.setLabelFor(cbPasswordSource);
        org.openide.awt.Mnemonics.setLocalizedText(lbPasswordSource, org.openide.util.NbBundle.getMessage(AddServerDialog.class, "LBL_PasswordSource")); // NOI18N

        cbPasswordSource.setModel(new PasswordSourceModel());

        org.openide.awt.Mnemonics.setLocalizedText(cbxSetAsDefault, org.openide.util.NbBundle.getMessage(AddServerDialog.class, "LBL_SetAsDefault")); // NOI18N
        cbxSetAsDefault.setMargin(new java.awt.Insets(2, 0, 2, 2));
        cbxSetAsDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSetAsDefaultActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(tfPassword);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddServerDialog.class, "AddServerDialog.jLabel1.text")); // NOI18N

        tfPassword.setText(org.openide.util.NbBundle.getMessage(AddServerDialog.class, "AddServerDialog.tfPassword.text")); // NOI18N
        tfPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                onPwdFocus(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbxSetAsDefault)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbServer)
                            .add(lbPasswordSource)
                            .add(jLabel1)
                            .add(lbLogin))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.CENTER, tfPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.CENTER, cbPasswordSource, 0, 246, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.CENTER, tfServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                            .add(tfLogin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbServer)
                    .add(tfServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfLogin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbLogin))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(tfPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbPasswordSource)
                    .add(cbPasswordSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbxSetAsDefault)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {cbPasswordSource, tfLogin, tfPassword, tfServer}, org.jdesktop.layout.GroupLayout.VERTICAL);

        lbPasswordSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddServerDialog.class, "DESC_PasswordSource")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbxSetAsDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSetAsDefaultActionPerformed
// TODO addServer your handling code here:
}//GEN-LAST:event_cbxSetAsDefaultActionPerformed

private void onLoginFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onLoginFocus
    tfLogin.selectAll();
}//GEN-LAST:event_onLoginFocus

private void onPwdFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onPwdFocus
    tfPassword.selectAll();
}//GEN-LAST:event_onPwdFocus


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbPasswordSource;
    private javax.swing.JCheckBox cbxSetAsDefault;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbLogin;
    private javax.swing.JLabel lbPasswordSource;
    private javax.swing.JLabel lbServer;
    private javax.swing.JTextField tfLogin;
    private javax.swing.JPasswordField tfPassword;
    private javax.swing.JTextField tfServer;
    // End of variables declaration//GEN-END:variables

}
