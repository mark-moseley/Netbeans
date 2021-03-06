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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tomcat5.customizer;

import org.openide.awt.Mnemonics;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.accessibility.AccessibleContext;
import java.awt.Font;

/**
 * Customizer general (connection) tab.
 *
 * @author  sherold
 */
public class CustomizerGeneral extends javax.swing.JPanel {
    
    private final CustomizerDataSupport custData;
    private boolean passwordVisible;
    private char originalEchoChar;
    private Font originalFont;

    /** Creates new form CustomizerGeneral */
    public CustomizerGeneral(CustomizerDataSupport custData) {
        this.custData = custData;        
        initComponents();

        JTextField serverPortTextField = ((JSpinner.NumberEditor) serverPortSpinner.getEditor()).getTextField();
        AccessibleContext ac = serverPortTextField.getAccessibleContext();
        ac.setAccessibleName(NbBundle.getMessage(CustomizerGeneral.class, "ASCN_ServerPort"));
        ac.setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ASCD_ServerPort"));
        
        JTextField shutdownPortTextField = ((JSpinner.NumberEditor) shutdownPortSpinner.getEditor()).getTextField();
        ac = shutdownPortTextField.getAccessibleContext();
        ac.setAccessibleName(NbBundle.getMessage(CustomizerGeneral.class, "ASCN_ShutdownPort"));
        ac.setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ASCD_ShutdownPort"));
        
        // work-around for jspinner incorrect fonts
        Font font = usernameTextField.getFont();
        serverPortSpinner.setFont(font);
        shutdownPortSpinner.setFont(font);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        homeLabel = new javax.swing.JLabel();
        homeTextField = new javax.swing.JTextField();
        baseLabel = new javax.swing.JLabel();
        baseTextField = new javax.swing.JTextField();
        credentialsLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        showButton = new javax.swing.JButton();
        serverPortLabel = new javax.swing.JLabel();
        serverPortSpinner = new javax.swing.JSpinner();
        shutdownPortLabel = new javax.swing.JLabel();
        shutdownPortSpinner = new javax.swing.JSpinner();
        monitorCheckBox = new javax.swing.JCheckBox();
        NoteChangesLabel = new javax.swing.JLabel();

        homeLabel.setLabelFor(homeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(homeLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.homeLabel.text")); // NOI18N

        homeTextField.setColumns(30);
        homeTextField.setDocument(custData.getCatalinaHomeModel());
        homeTextField.setEditable(false);

        baseLabel.setLabelFor(baseTextField);
        org.openide.awt.Mnemonics.setLocalizedText(baseLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.baseLabel.text")); // NOI18N

        baseTextField.setColumns(30);
        baseTextField.setDocument(custData.getCatalinaBaseModel());
        baseTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(credentialsLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.credentialsLabel.text")); // NOI18N
        credentialsLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.credentialsLabel.toolTipText")); // NOI18N

        usernameLabel.setLabelFor(usernameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.usernameLabel.text")); // NOI18N
        usernameLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.usernameLabel.toolTipText")); // NOI18N

        usernameTextField.setColumns(15);
        usernameTextField.setDocument(custData.getUsernameModel());

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.passwordLabel.text")); // NOI18N
        passwordLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.passwordLabel.toolTipText")); // NOI18N

        passwordField.setColumns(15);
        passwordField.setDocument(custData.getPasswordModel());

        org.openide.awt.Mnemonics.setLocalizedText(showButton, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.showButton.text")); // NOI18N
        showButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.showButton.toolTipText")); // NOI18N
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        serverPortLabel.setLabelFor(serverPortSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(serverPortLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.serverPortLabel.text")); // NOI18N

        serverPortSpinner.setModel(custData.getServerPortModel());
        serverPortSpinner.setEditor(new JSpinner.NumberEditor(serverPortSpinner, "#"));

        shutdownPortLabel.setLabelFor(shutdownPortSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(shutdownPortLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.shutdownPortLabel.text")); // NOI18N

        shutdownPortSpinner.setModel(custData.getShutdownPortModel());
        shutdownPortSpinner.setEditor(new JSpinner.NumberEditor(shutdownPortSpinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(monitorCheckBox, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.monitorCheckBox.text")); // NOI18N
        monitorCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        monitorCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        monitorCheckBox.setModel(custData.getMonitorModel());

        org.openide.awt.Mnemonics.setLocalizedText(NoteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.NoteChangesLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(homeLabel)
                            .add(baseLabel)
                            .add(serverPortLabel)
                            .add(shutdownPortLabel)
                            .add(usernameLabel)
                            .add(passwordLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(shutdownPortSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(serverPortSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(baseTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .add(homeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, usernameTextField, 0, 0, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(showButton)
                                .add(59, 59, 59))))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(credentialsLabel))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(monitorCheckBox))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(NoteChangesLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(homeLabel)
                    .add(homeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(baseLabel)
                    .add(baseTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(25, 25, 25)
                .add(credentialsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(showButton)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwordLabel))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(serverPortLabel)
                    .add(serverPortSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shutdownPortLabel)
                    .add(shutdownPortSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(monitorCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(NoteChangesLabel)
                .addContainerGap())
        );

        homeTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CatalinaHome")); // NOI18N
        homeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CatalinaHome")); // NOI18N
        baseTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CatalinaBase")); // NOI18N
        baseTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CatalinaBase")); // NOI18N
        usernameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Username")); // NOI18N
        usernameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Username")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_Password")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_Password")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
    if (!passwordVisible) {
        passwordVisible = true;
        originalFont = passwordField.getFont();
        passwordField.setFont(usernameTextField.getFont());
        originalEchoChar = passwordField.getEchoChar();
        passwordField.setEchoChar((char) 0);
        Mnemonics.setLocalizedText(showButton, NbBundle.getMessage(CustomizerGeneral.class, "LBL_ShowButtonHide"));
        showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, "LBL_ShowButtonHide_ToolTip"));
    } else {
        passwordVisible = false;
        passwordField.setFont(originalFont);
        passwordField.setEchoChar(originalEchoChar);
        Mnemonics.setLocalizedText(showButton, NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.showButton.text"));
        showButton.setToolTipText(NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.showButton.toolTipText"));
        
    }
}//GEN-LAST:event_showButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel NoteChangesLabel;
    private javax.swing.JLabel baseLabel;
    private javax.swing.JTextField baseTextField;
    private javax.swing.JLabel credentialsLabel;
    private javax.swing.JLabel homeLabel;
    private javax.swing.JTextField homeTextField;
    private javax.swing.JCheckBox monitorCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSpinner serverPortSpinner;
    private javax.swing.JButton showButton;
    private javax.swing.JLabel shutdownPortLabel;
    private javax.swing.JSpinner shutdownPortSpinner;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

}
