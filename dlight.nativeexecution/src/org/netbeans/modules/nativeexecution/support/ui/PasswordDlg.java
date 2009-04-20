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
package org.netbeans.modules.nativeexecution.support.ui;

import java.awt.Dialog;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author vv159170
 */
public class PasswordDlg extends javax.swing.JPanel {

    /** Creates new form PasswordPanel */
    public PasswordDlg() {
        initComponents();
        // FIXUP for Mac and Ubuntu
        tfUser.setBackground(getBackground());
        tfUser.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        tfHost.setBackground(getBackground());
        tfHost.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    public boolean askPassword(ExecutionEnvironment execEnv) {
        tfUser.setText(execEnv.getUser());
        String hostName = execEnv.getHost();
        if (execEnv.getSSHPort() != 22) {
            hostName += ":" + execEnv.getSSHPort(); //NOI18N
        }
        tfHost.setText(hostName); // NOI18N

        DialogDescriptor dd = new DialogDescriptor(this,
                loc("TITLE_Password"), true, // NOI18N
                new Object[]{
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        return dd.getValue() == DialogDescriptor.OK_OPTION;
    }

    public void clearPassword() {
        tfPassword.setText(null);
    }

    public char[] getPassword() {
        return tfPassword.getPassword();
    }

    public boolean isRememberPassword() {
        return cbRememberPwd.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tfPassword = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfHost = new javax.swing.JTextField();
        cbRememberPwd = new javax.swing.JCheckBox();
        jLabel0 = new javax.swing.JLabel();
        tfUser = new javax.swing.JTextField();

        tfPassword.setText(null);
        tfPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfPasswordActionPerformed(evt);
            }
        });
        tfPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfPasswordonPwdFocus(evt);
            }
        });

        jLabel1.setLabelFor(tfHost);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(tfPassword);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.jLabel2.text")); // NOI18N

        tfHost.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background") /*NOI18N*/);
        tfHost.setEditable(false);
        tfHost.setText(null);
        tfHost.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(cbRememberPwd, org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.cbRememberPwd.text")); // NOI18N

        jLabel0.setLabelFor(tfHost);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel0, org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.jLabel0.text")); // NOI18N

        tfUser.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background") /*NOI18N*/);
        tfUser.setEditable(false);
        tfUser.setText(null);
        tfUser.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(jLabel0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tfHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(tfUser)
                                .add(tfPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 253, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(cbRememberPwd))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel0)
                    .add(tfUser))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(tfHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(tfPassword))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbRememberPwd)
                .addContainerGap())
        );

        tfPassword.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "Pwd.Password_AN")); // NOI18N
        tfPassword.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "Pwd.Password_AD")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "AN_Password")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "DESC_Password")); // NOI18N
        tfHost.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "Pwd.Host_AN")); // NOI18N
        tfHost.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "Pwd.Host_AD")); // NOI18N
        cbRememberPwd.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "AN_RememberPassword")); // NOI18N
        cbRememberPwd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "DESC_RememberPassword")); // NOI18N
        tfUser.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.tfUser.AccessibleContext.accessibleName")); // NOI18N
        tfUser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PasswordDlg.class, "PasswordDlg.tfUser.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void tfPasswordonPwdFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfPasswordonPwdFocus
        tfPassword.selectAll();
}//GEN-LAST:event_tfPasswordonPwdFocus

    private void tfPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPasswordActionPerformed

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(PasswordDlg.class, key, params);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbRememberPwd;
    private javax.swing.JLabel jLabel0;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tfHost;
    private javax.swing.JPasswordField tfPassword;
    private javax.swing.JTextField tfUser;
    // End of variables declaration//GEN-END:variables
}
