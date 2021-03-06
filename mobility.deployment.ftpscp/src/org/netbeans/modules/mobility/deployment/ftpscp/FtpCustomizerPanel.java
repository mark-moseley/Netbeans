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

/*
 * FtpCustomizerPanel.java
 *
 * Created on 8. prosinec 2004, 17:28
 */
package org.netbeans.modules.mobility.deployment.ftpscp;

import java.awt.Font;
import javax.swing.JSpinner;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam
 */
public class FtpCustomizerPanel extends javax.swing.JPanel {
    
    /** Creates new form FtpCustomizerPanel */
    public FtpCustomizerPanel() {
        initComponents();
        jSpinnerPort.setEditor(new JSpinner.NumberEditor(jSpinnerPort, "#0")); //NOI18N
        jTextArea1.setFont(jTextArea1.getFont().deriveFont(Font.ITALIC));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jTextFieldServer = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxSeparator = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerPort = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldUser = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPasswordField = new javax.swing.JPasswordField();
        jCheckBoxPassive = new javax.swing.JCheckBox();
        jTextArea1 = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jTextFieldServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Server")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jTextFieldServer.setName(FtpDeploymentPlugin.PROP_SERVER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jTextFieldServer, gridBagConstraints);
        jTextFieldServer.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FtpCustomizerPanel.class, "ACSD_Ftp_Server")); // NOI18N

        jLabel6.setLabelFor(jComboBoxSeparator);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Separator")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(jLabel6, gridBagConstraints);

        jComboBoxSeparator.setEditable(true);
        jComboBoxSeparator.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "/", "\\\\" }));
            jComboBoxSeparator.setName(FtpDeploymentPlugin.PROP_SEPARATOR);
            jComboBoxSeparator.setPreferredSize(new java.awt.Dimension(54, 20));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
            add(jComboBoxSeparator, gridBagConstraints);
            jComboBoxSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FtpCustomizerPanel.class, "ACSD_Ftp_Separator")); // NOI18N

            jLabel2.setLabelFor(jSpinnerPort);
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Port")); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
            add(jLabel2, gridBagConstraints);
            jLabel2.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FtpCustomizerPanel.class, "ACSD_Ftp_Port")); // NOI18N

            jSpinnerPort.setName(FtpDeploymentPlugin.PROP_PORT);
            jSpinnerPort.setPreferredSize(new java.awt.Dimension(54, 20));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
            add(jSpinnerPort, gridBagConstraints);

            jLabel3.setLabelFor(jTextFieldUser);
            org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_User")); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
            add(jLabel3, gridBagConstraints);

            jTextFieldUser.setName(FtpDeploymentPlugin.PROP_USERID);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
            add(jTextFieldUser, gridBagConstraints);
            jTextFieldUser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FtpCustomizerPanel.class, "ACSD_Ftp_user")); // NOI18N

            jLabel4.setLabelFor(jPasswordField);
            org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Password")); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
            add(jLabel4, gridBagConstraints);

            jPasswordField.setName(FtpDeploymentPlugin.PROP_PASSWORD);
            jPasswordField.setPreferredSize(new java.awt.Dimension(6, 20));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
            add(jPasswordField, gridBagConstraints);
            jPasswordField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FtpCustomizerPanel.class, "ACSD_Ftp_Password")); // NOI18N

            org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxPassive, NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Passive")); // NOI18N
            jCheckBoxPassive.setName(FtpDeploymentPlugin.PROP_PASSIVE);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
            add(jCheckBoxPassive, gridBagConstraints);

            jTextArea1.setBackground(getBackground());
            jTextArea1.setEditable(false);
            jTextArea1.setLineWrap(true);
            jTextArea1.setText(NbBundle.getMessage(FtpCustomizerPanel.class, "LBL_Ftp_Notice")); // NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
            add(jTextArea1, gridBagConstraints);
        }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JCheckBox jCheckBoxPassive;
    javax.swing.JComboBox jComboBoxSeparator;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel6;
    javax.swing.JPasswordField jPasswordField;
    javax.swing.JSpinner jSpinnerPort;
    javax.swing.JTextArea jTextArea1;
    javax.swing.JTextField jTextFieldServer;
    javax.swing.JTextField jTextFieldUser;
    // End of variables declaration//GEN-END:variables
    
}
