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

package org.netbeans.modules.php.project.connections.common;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous, Tomas Mysik
 */
public final class PasswordPanel extends JPanel {
    private static final long serialVersionUID = -12116662158021638L;
    private static enum Type { USER, CERTIFICATE };

    private final String configurationName;

    private PasswordPanel(String configurationName, Type type, String userName) {
        assert configurationName != null;
        assert type != null;

        this.configurationName = configurationName;

        initComponents();
        switch (type) {
            case USER:
                certificateLabel.setVisible(false);
                usernameField.setText(userName);
                break;
            case CERTIFICATE:
                usernameLabel.setVisible(false);
                usernameField.setVisible(false);
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    public static PasswordPanel forUser(String configurationName, String userName) {
        assert userName != null;
        return new PasswordPanel(configurationName, Type.USER, userName);
    }

    public static PasswordPanel forCertificate(String configurationName) {
        return new PasswordPanel(configurationName, Type.CERTIFICATE, null);
    }

    public boolean open() {
        DialogDescriptor input = new DialogDescriptor(
                this,
                NbBundle.getMessage(PasswordPanel.class, "LBL_EnterPassword", configurationName),
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.OK_OPTION,
                null);
        return DialogDisplayer.getDefault().notify(input) == NotifyDescriptor.OK_OPTION;
    }

    public String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        passwordField.requestFocusInWindow();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernameLabel = new JLabel();
        usernameField = new JTextField();
        certificateLabel = new JLabel();
        passwordLabel = new JLabel();
        passwordField = new JPasswordField();

        setFocusTraversalPolicy(null);

        usernameLabel.setLabelFor(usernameField);

        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.usernameLabel.text")); // NOI18N
        usernameField.setEditable(false);

        certificateLabel.setLabelFor(passwordField);

        certificateLabel.setText(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.certificateLabel.text")); // NOI18N
        passwordLabel.setLabelFor(passwordField);

        Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.passwordLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.TRAILING)
                            .add(usernameLabel)
                            .add(passwordLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(passwordField, GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .add(usernameField, GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)))
                    .add(certificateLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(usernameLabel)
                    .add(usernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(certificateLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );

        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        usernameField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.usernameField.AccessibleContext.accessibleName")); // NOI18N
        usernameField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.usernameField.AccessibleContext.accessibleDescription")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.passwordLabel.AccessibleContext.accessibleName")); // NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.passwordLabel.AccessibleContext.accessibleDescription")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.passwordField.AccessibleContext.accessibleName")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.passwordField.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PasswordPanel.class, "PasswordPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel certificateLabel;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JTextField usernameField;
    private JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
