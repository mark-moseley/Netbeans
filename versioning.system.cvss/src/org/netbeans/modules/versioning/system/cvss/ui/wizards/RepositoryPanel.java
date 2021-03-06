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

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * UI for remote CVS repository selection. Components
 * are dynamically hidden.
 *
 * @author  Petr Kuzel
 */
final class RepositoryPanel extends javax.swing.JPanel implements ActionListener {

    /** Creates new form ProxyPanel */
    public RepositoryPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setName(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0006")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, bundle.getString("BK0001")); // NOI18N

        rootsLabel.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(rootsLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0002")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK2018")); // NOI18N

        pPaswordLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(pPaswordLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK0003")); // NOI18N
        pPaswordLabel.setToolTipText(bundle.getString("TT_PserverPassword")); // NOI18N

        passwordTextField.setColumns(12);

        sshButtonGroup.add(internalSshRadioButton);
        internalSshRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(internalSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1100")); // NOI18N
        internalSshRadioButton.setToolTipText(bundle.getString("TT_UseInternalSSH")); // NOI18N
        internalSshRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        internalSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        extPasswordLabel5.setLabelFor(extPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(extPasswordLabel5, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1011")); // NOI18N
        extPasswordLabel5.setToolTipText(bundle.getString("TT_SSHPassword")); // NOI18N

        extPasswordField.setColumns(12);

        org.openide.awt.Mnemonics.setLocalizedText(extREmemberPasswordCheckBox, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1012")); // NOI18N
        extREmemberPasswordCheckBox.setToolTipText(bundle.getString("TT_RememberPassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyConfigurationButton, bundle.getString("BK0005")); // NOI18N
        proxyConfigurationButton.setToolTipText(bundle.getString("TT_ProxyConfig")); // NOI18N

        sshButtonGroup.add(extSshRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(extSshRadioButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1101")); // NOI18N
        extSshRadioButton.setToolTipText(bundle.getString("TT_UseExternal")); // NOI18N
        extSshRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extSshRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        extCommandLabel.setLabelFor(extCommandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(extCommandLabel, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1013")); // NOI18N
        extCommandLabel.setToolTipText(bundle.getString("TT_ExternalCommand")); // NOI18N

        extCommandTextField.setMinimumSize(new java.awt.Dimension(80, 20));

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "LBL_RepositoryPanel_Command_Browse")); // NOI18N
        browseButton.addActionListener(this);

        rootComboBox.setEditable(true);
        rootComboBox.setMinimumSize(new java.awt.Dimension(80, 20));

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "BK1105")); // NOI18N
        editButton.setToolTipText(bundle.getString("TT_EditFields")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 415, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 111, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(rootsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(0, 0, 0)
                        .add(rootComboBox, 0, 291, Short.MAX_VALUE)
                        .add(4, 4, 4)
                        .add(editButton))
                    .add(descLabel)))
            .add(layout.createSequentialGroup()
                .add(pPaswordLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(internalSshRadioButton)
            .add(extSshRadioButton)
            .add(headerLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 308, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(layout.createSequentialGroup()
                .add(17, 17, 17)
                .add(extCommandLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extCommandTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton))
            .add(layout.createSequentialGroup()
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(proxyConfigurationButton)
                    .add(layout.createSequentialGroup()
                        .add(extPasswordLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(extPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(2, 2, 2)
                        .add(extREmemberPasswordCheckBox))))
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(headerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rootsLabel)
                    .add(editButton)
                    .add(rootComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descLabel)
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pPaswordLabel)
                    .add(passwordTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(internalSshRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(extPasswordLabel5)
                    .add(extPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(extREmemberPasswordCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(proxyConfigurationButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extSshRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(extCommandLabel)
                    .add(browseButton)
                    .add(extCommandTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RepositoryStep")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            RepositoryPanel.this.browseButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(RepositoryPanel.class, "ACSD_BrowseCommand"), defaultDir);
        fileChooser.setDialogTitle(NbBundle.getMessage(RepositoryPanel.class, "LBL_BrowseCommand_Title"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this, NbBundle.getMessage(RepositoryPanel.class, "LBL_BrowseCommand_Approve"));
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            extCommandTextField.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private File defaultWorkingDirectory() {
        File defaultDir = null;
        String current = extCommandTextField.getText();
        if (current != null && !(current.trim().equals(""))) {  // NOI18N
            File currentFile = new File(current);
            while (currentFile != null && currentFile.exists() == false) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            defaultDir = new File(System.getProperty("user.home"));  // NOI18N
        }

        return defaultDir;
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton browseButton = new javax.swing.JButton();
    final javax.swing.JLabel descLabel = new javax.swing.JLabel();
    final javax.swing.JButton editButton = new javax.swing.JButton();
    final javax.swing.JLabel extCommandLabel = new javax.swing.JLabel();
    final javax.swing.JTextField extCommandTextField = new javax.swing.JTextField();
    final javax.swing.JPasswordField extPasswordField = new javax.swing.JPasswordField();
    final javax.swing.JLabel extPasswordLabel5 = new javax.swing.JLabel();
    final javax.swing.JCheckBox extREmemberPasswordCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JRadioButton extSshRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JLabel headerLabel = new javax.swing.JLabel();
    final javax.swing.JRadioButton internalSshRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
    final javax.swing.JLabel pPaswordLabel = new javax.swing.JLabel();
    final javax.swing.JPasswordField passwordTextField = new javax.swing.JPasswordField();
    final javax.swing.JButton proxyConfigurationButton = new javax.swing.JButton();
    final javax.swing.JComboBox rootComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel rootsLabel = new javax.swing.JLabel();
    final javax.swing.ButtonGroup sshButtonGroup = new javax.swing.ButtonGroup();
    // End of variables declaration//GEN-END:variables
    
}
