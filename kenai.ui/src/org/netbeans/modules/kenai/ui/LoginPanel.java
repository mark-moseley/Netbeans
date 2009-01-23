/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

/**
 * @author Jan Becicka
 * @author maros
 */
public class LoginPanel extends javax.swing.JPanel {

    /** Creates new form LoginPanel */
    public LoginPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblKenaiLogo = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        chkRememberMe = new javax.swing.JCheckBox();
        forgotPassword = new javax.swing.JLabel();
        lblNoAccount = new javax.swing.JLabel();
        register = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();

        lblKenaiLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/kenai.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblKenaiLogo, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblKenaiLogo.text")); // NOI18N
        lblKenaiLogo.setMinimumSize(new java.awt.Dimension(0, 50));

        lblUserName.setLabelFor(username);
        org.openide.awt.Mnemonics.setLocalizedText(lblUserName, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblUserName.text")); // NOI18N

        username.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.username.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblPassword, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkRememberMe, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkRememberMe.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(forgotPassword, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.forgotPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblNoAccount, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblNoAccount.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(register, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.register.text")); // NOI18N

        password.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.password.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(lblKenaiLogo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblUserName)
                    .add(lblPassword))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblNoAccount)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(register))
                    .add(forgotPassword)
                    .add(chkRememberMe)
                    .add(username, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .add(password, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lblKenaiLogo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(16, 16, 16)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblUserName)
                    .add(username, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPassword)
                    .add(password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chkRememberMe)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(forgotPassword)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNoAccount)
                    .add(register))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JCheckBox chkRememberMe;
    javax.swing.JLabel forgotPassword;
    javax.swing.JLabel lblKenaiLogo;
    javax.swing.JLabel lblNoAccount;
    javax.swing.JLabel lblPassword;
    javax.swing.JLabel lblUserName;
    javax.swing.JPasswordField password;
    javax.swing.JLabel register;
    javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables

}
