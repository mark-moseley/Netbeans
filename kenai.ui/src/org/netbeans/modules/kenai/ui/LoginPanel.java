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

import java.awt.Cursor;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;

/**
 * @author Jan Becicka
 * @author maros
 */
public class LoginPanel extends javax.swing.JPanel {

        /** Creates new form LoginPanel */
    public LoginPanel() {
        initComponents();
    }

    public boolean isStorePassword() {
        return chkRememberMe.isSelected();
    }

    public void showError(KenaiException ex) {
        errorProgress.setVisible(true);
        progressBar.setVisible(false);
        error.setText(ex.getMessage());
        error.setVisible(true);
    }

    public void showProgress() {
        errorProgress.setVisible(true);
        error.setVisible(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

    public void clearStatus() {
        errorProgress.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        errorProgress = new javax.swing.JPanel();
        error = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        lblKenaiLogo = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        chkRememberMe = new javax.swing.JCheckBox();
        forgotPassword = new javax.swing.JLabel();
        lblNoAccount = new javax.swing.JLabel();
        register = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        errorProgress.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        errorProgress.setLayout(new java.awt.CardLayout());

        error.setForeground(java.awt.Color.red);
        error.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/error.png"))); // NOI18N
        errorProgress.add(error, "card3");
        errorProgress.add(progressBar, "card3");

        lblKenaiLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/kenai.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblKenaiLogo, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblKenaiLogo.text")); // NOI18N
        lblKenaiLogo.setMinimumSize(new java.awt.Dimension(0, 50));

        lblUserName.setLabelFor(username);
        org.openide.awt.Mnemonics.setLocalizedText(lblUserName, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblUserName.text")); // NOI18N

        username.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.username.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblPassword, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblPassword.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkRememberMe, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.chkRememberMe.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(forgotPassword, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.forgotPassword.text")); // NOI18N
        forgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPasswordMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPasswordMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                forgotPasswordMousePressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblNoAccount, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.lblNoAccount.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(register, org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.register.text")); // NOI18N
        register.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                registerMousePressed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(lblKenaiLogo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(errorProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
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
                            .add(password, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                            .add(username, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void forgotPasswordMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPasswordMouseEntered
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_forgotPasswordMouseEntered

    private void forgotPasswordMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPasswordMouseExited
        forgotPassword.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_forgotPasswordMouseExited

    private void forgotPasswordMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_forgotPasswordMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("https://kenai.com/people/forgot_password"));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_forgotPasswordMousePressed

    private void registerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerMouseEntered
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_registerMouseEntered

    private void registerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("https://kenai.com/people/new"));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_registerMousePressed

    private void registerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerMouseExited
        register.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_registerMouseExited


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JCheckBox chkRememberMe;
    javax.swing.JLabel error;
    javax.swing.JPanel errorProgress;
    javax.swing.JLabel forgotPassword;
    javax.swing.JLabel lblKenaiLogo;
    javax.swing.JLabel lblNoAccount;
    javax.swing.JLabel lblPassword;
    javax.swing.JLabel lblUserName;
    javax.swing.JPasswordField password;
    javax.swing.JProgressBar progressBar;
    javax.swing.JLabel register;
    javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables

    public char[] getPassword() {
        return password.getPassword();
    }

    public String getUsername() {
        return username.getText();
    }
    public void setUsername(String uname) {
        username.setText(uname);
        chkRememberMe.setSelected(true);
    }

    public void setPassword(char[] pwd) {
        password.setText(new String(pwd));
    }
}
