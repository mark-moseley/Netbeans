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

package org.netbeans.modules.subversion.ui.repository;

/**
 *
 * @author  Petr Kuzel
 */
public class HttpPanel extends javax.swing.JPanel {

    /** Creates new form RepositoryPanel */
    public HttpPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/repository/Bundle"); // NOI18N
        setName(bundle.getString("BK2018")); // NOI18N
        setVerifyInputWhenFocusTarget(false);

        userPasswordField.setMinimumSize(new java.awt.Dimension(11, 22));
        userPasswordField.setPreferredSize(new java.awt.Dimension(11, 22));

        passwordLabel.setLabelFor(userPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0004")); // NOI18N
        passwordLabel.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "TT_Password")); // NOI18N

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0003")); // NOI18N
        userLabel.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "TT_UserName")); // NOI18N

        userTextField.setMinimumSize(new java.awt.Dimension(11, 22));
        userTextField.setPreferredSize(new java.awt.Dimension(11, 22));

        leaveBlankLabel.setText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0005")); // NOI18N

        savePasswordCheckBox.setMnemonic('v');
        savePasswordCheckBox.setText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0007")); // NOI18N
        savePasswordCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sslPanel.setVerifyInputWhenFocusTarget(false);

        certPasswordField.setMinimumSize(new java.awt.Dimension(11, 22));
        certPasswordField.setPreferredSize(new java.awt.Dimension(11, 22));

        certFileTextField.setMinimumSize(new java.awt.Dimension(11, 22));
        certFileTextField.setPreferredSize(new java.awt.Dimension(11, 22));

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.browseButton.text")); // NOI18N
        browseButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.browseButton.toolTipText")); // NOI18N

        userLabel1.setLabelFor(certFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel1, org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.userLabel1.text")); // NOI18N
        userLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.userLabel1.toolTipText")); // NOI18N

        userLabel2.setLabelFor(certPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel2, org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.userLabel2.text")); // NOI18N
        userLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.userLabel2.toolTipText")); // NOI18N

        org.jdesktop.layout.GroupLayout sslPanelLayout = new org.jdesktop.layout.GroupLayout(sslPanel);
        sslPanel.setLayout(sslPanelLayout);
        sslPanelLayout.setHorizontalGroup(
            sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sslPanelLayout.createSequentialGroup()
                .add(sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(userLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(userLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sslPanelLayout.createSequentialGroup()
                        .add(certFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(sslPanelLayout.createSequentialGroup()
                        .add(certPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        sslPanelLayout.setVerticalGroup(
            sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sslPanelLayout.createSequentialGroup()
                .add(sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(userLabel1)
                    .add(certFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sslPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel2)
                    .add(certPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        certPasswordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.certPasswordField.AccessibleContext.accessibleName")); // NOI18N
        certPasswordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.certPasswordField.AccessibleContext.accessibleDescription")); // NOI18N
        certFileTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.certFileTextField.AccessibleContext.accessibleName")); // NOI18N
        certFileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.certFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HttpPanel.class, "HttpPanel.proxySettingsButton1.AccessibleContext.accessibleName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxySettingsButton, org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0006")); // NOI18N
        proxySettingsButton.setToolTipText(org.openide.util.NbBundle.getMessage(HttpPanel.class, "ACSD_ProxyDialog")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(userLabel)
                    .add(passwordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(userPasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(userTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(leaveBlankLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(savePasswordCheckBox)
                        .addContainerGap())))
            .add(sslPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(proxySettingsButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(leaveBlankLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(userPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(savePasswordCheckBox)
                .add(18, 18, 18)
                .add(sslPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proxySettingsButton))
        );

        userPasswordField.getAccessibleContext().setAccessibleParent(this);
        passwordLabel.getAccessibleContext().setAccessibleParent(this);
        userLabel.getAccessibleContext().setAccessibleParent(this);
        userTextField.getAccessibleContext().setAccessibleParent(this);
        leaveBlankLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpPanel.class, "ACSD_InfoLabel")); // NOI18N
        leaveBlankLabel.getAccessibleContext().setAccessibleParent(this);
        savePasswordCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpPanel.class, "BK0011")); // NOI18N
        proxySettingsButton.getAccessibleContext().setAccessibleParent(this);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HttpPanel.class, "ACSD_RepositoryPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton browseButton = new javax.swing.JButton();
    final javax.swing.JTextField certFileTextField = new javax.swing.JTextField();
    final javax.swing.JPasswordField certPasswordField = new javax.swing.JPasswordField();
    final javax.swing.JLabel leaveBlankLabel = new javax.swing.JLabel();
    final javax.swing.JLabel passwordLabel = new javax.swing.JLabel();
    final javax.swing.JButton proxySettingsButton = new javax.swing.JButton();
    final javax.swing.JCheckBox savePasswordCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPanel sslPanel = new javax.swing.JPanel();
    final javax.swing.JLabel userLabel = new javax.swing.JLabel();
    final javax.swing.JLabel userLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel userLabel2 = new javax.swing.JLabel();
    final javax.swing.JPasswordField userPasswordField = new javax.swing.JPasswordField();
    final javax.swing.JTextField userTextField = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

}
