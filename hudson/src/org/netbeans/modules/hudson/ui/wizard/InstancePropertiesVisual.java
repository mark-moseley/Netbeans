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

package org.netbeans.modules.hudson.ui.wizard;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

class InstancePropertiesVisual extends JPanel {
    
    public InstancePropertiesVisual() {
        initComponents();
        DocumentListener l = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                check();
            }
            public void removeUpdate(DocumentEvent e) {
                check();
            }
            public void changedUpdate(DocumentEvent e) {}
        };
        nameTxt.getDocument().addDocumentListener(l);
        urlTxt.getDocument().addDocumentListener(l);
        checkProgress.setVisible(false);
    }

    private NotificationLineSupport msgs;
    private JButton addButton;

    void init(NotificationLineSupport msgs, JButton addButton) {
        assert msgs != null;
        this.msgs = msgs;
        this.addButton = addButton;
        check();
    }

    void showChecking() {
        checkProgress.setVisible(true);
        nameTxt.setEnabled(false);
        urlTxt.setEnabled(false);
        autoSyncCheckBox.setEnabled(false);
        autoSyncSpinner.setEnabled(false);
        proxyButton.setEnabled(false);
    }

    void checkFailed(String explanation) {
        msgs.setErrorMessage(explanation);
        checkProgress.setVisible(false);
        nameTxt.setEnabled(true);
        urlTxt.setEnabled(true);
        autoSyncCheckBox.setEnabled(true);
        autoSyncSpinner.setEnabled(autoSyncCheckBox.isSelected());
        proxyButton.setEnabled(true);
        urlTxt.requestFocusInWindow();
    }

    String getDisplayName() {
        return nameTxt.getText().trim();
    }
    
    String getUrl() {
        return urlTxt.getText().trim();
    }
    
    int getSyncTime() {
        return autoSyncCheckBox.isSelected() ? (Integer) autoSyncSpinner.getValue() : 0;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlTxt = new javax.swing.JTextField();
        autoSyncCheckBox = new javax.swing.JCheckBox();
        autoSyncSpinner = new javax.swing.JSpinner();
        autoSyncLabel = new javax.swing.JLabel();
        proxyButton = new javax.swing.JButton();
        checkProgress = new javax.swing.JProgressBar();

        nameLabel.setLabelFor(nameTxt);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Name")); // NOI18N

        urlLabel.setLabelFor(urlTxt);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Url")); // NOI18N

        urlTxt.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.urlTxt.text")); // NOI18N

        autoSyncCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoSyncCheckBox, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSync")); // NOI18N
        autoSyncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoSyncCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSyncCheckBoxActionPerformed(evt);
            }
        });

        autoSyncSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(1), null, Integer.valueOf(1)));
        autoSyncSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoSyncSpinnerStateChanged(evt);
            }
        });

        autoSyncLabel.setLabelFor(autoSyncSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(autoSyncLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSyncMinutes")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyButton, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Proxy")); // NOI18N
        proxyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyButtonActionPerformed(evt);
            }
        });

        checkProgress.setIndeterminate(true);
        checkProgress.setString(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.checkProgress.string")); // NOI18N
        checkProgress.setStringPainted(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(checkProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(autoSyncCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoSyncSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoSyncLabel))
                    .add(proxyButton)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(urlLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(urlTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                            .add(nameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(autoSyncCheckBox)
                    .add(autoSyncSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(autoSyncLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(proxyButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(checkProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
    OptionsDisplayer.getDefault().open("General"); // NOI18N
}//GEN-LAST:event_proxyButtonActionPerformed

private void autoSyncSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoSyncSpinnerStateChanged
    check();
}//GEN-LAST:event_autoSyncSpinnerStateChanged

private void autoSyncCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoSyncCheckBoxActionPerformed
    autoSyncSpinner.setEnabled(autoSyncCheckBox.isSelected());
    check();
}//GEN-LAST:event_autoSyncCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSyncCheckBox;
    private javax.swing.JLabel autoSyncLabel;
    private javax.swing.JSpinner autoSyncSpinner;
    private javax.swing.JProgressBar checkProgress;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton proxyButton;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTxt;
    // End of variables declaration//GEN-END:variables
    
    private void check() {
        addButton.setEnabled(false);
        String name = getDisplayName();
        String url = getUrl();
        if (name.length() == 0) {
            msgs.setInformationMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_EmptyName"));
            return;
        }
        if (HudsonManagerImpl.getInstance().getInstanceByName(name) != null) {
            msgs.setErrorMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_ExistName"));
            return;
        }
        if (url.length() == 0 || url.endsWith("//")) {
            msgs.setInformationMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_EmptyUrl"));
            return;
        }
        if (!url.endsWith("/")) { // NOI18N
            msgs.setInformationMessage(NbBundle.getMessage(InstancePropertiesVisual.class, "InstanceDialog.end_with_slash"));
            return;
        }
        try {
            URL u = new URL(url);
            if (!u.getProtocol().matches("https?")) { // NOI18N
                msgs.setErrorMessage(NbBundle.getMessage(InstancePropertiesVisual.class, "InstanceDialog.http_protocol"));
                return;
            }
        } catch (MalformedURLException x) {
            msgs.setErrorMessage(x.getLocalizedMessage());
            return;
        }
        if (HudsonManagerImpl.getDefault().getInstance(url) != null) {
            msgs.setErrorMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_ExistUrl"));
            return;
        }
        msgs.clearMessages();
        addButton.setEnabled(true);
    }

}
