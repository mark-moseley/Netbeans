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

/*
 * RepositoryPanel.java
 *
 * Created on Oct 14, 2008, 5:08:33 PM
 */

package org.netbeans.modules.bugzilla.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryPanel extends javax.swing.JPanel implements DocumentListener, ActionListener {
    private RepositoryController controller;

    /** Creates new form RepositoryPanel */
    public RepositoryPanel(RepositoryController controller) {
        initComponents();
        this.controller = controller;
        urlField.getDocument().addDocumentListener(this);
        validateLabel.setVisible(false);
        progressPanel.setVisible(false);
        httpCheckBox.addActionListener(this);
        enableHttpFields();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // XXX use controler.opened() instead
        controller.populate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.urlLabel.text_1")); // NOI18N

        urlField.setColumns(30);

        userLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.userLabel.text_1")); // NOI18N

        psswdLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.psswdLabel.text_1")); // NOI18N

        userField.setColumns(15);
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });

        psswdField.setColumns(15);

        nameLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.nameLabel.text_1")); // NOI18N

        nameField.setColumns(30);

        validateButton.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.validateButton.text_1")); // NOI18N
        validateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateButtonActionPerformed(evt);
            }
        });

        progressPanel.setLayout(new java.awt.BorderLayout());

        validateLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.validateLabel.text_1")); // NOI18N

        httpCheckBox.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.httpCheckBox.text")); // NOI18N
        httpCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                httpCheckBoxActionPerformed(evt);
            }
        });

        userLabel1.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.userLabel1.text")); // NOI18N

        httpUserField.setColumns(15);
        httpUserField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                httpUserFieldActionPerformed(evt);
            }
        });

        psswdLabel1.setText(org.openide.util.NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.psswdLabel1.text")); // NOI18N

        httpPsswdField.setColumns(15);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(httpCheckBox)
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(psswdLabel1)
                    .add(userLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, httpPsswdField)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, httpUserField)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                    .add(validateButton)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(validateLabel)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(progressPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .addContainerGap())
                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(urlLabel)
                        .add(psswdLabel)
                        .add(userLabel)
                        .add(nameLabel))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, psswdField)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, userField))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, urlField)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, nameField)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(urlLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(psswdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(psswdLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(httpCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel1)
                    .add(httpUserField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(httpPsswdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(psswdLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(validateLabel)
                    .add(progressPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(layout.createSequentialGroup()
                .add(239, 239, 239)
                .add(validateButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void userFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_userFieldActionPerformed

    private void validateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateButtonActionPerformed
    }//GEN-LAST:event_validateButtonActionPerformed

    private void httpUserFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_httpUserFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_httpUserFieldActionPerformed

    private void httpCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_httpCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_httpCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox httpCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPasswordField httpPsswdField = new javax.swing.JPasswordField();
    final javax.swing.JTextField httpUserField = new javax.swing.JTextField();
    final javax.swing.JTextField nameField = new javax.swing.JTextField();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    final javax.swing.JPanel progressPanel = new javax.swing.JPanel();
    final javax.swing.JPasswordField psswdField = new javax.swing.JPasswordField();
    final javax.swing.JLabel psswdLabel = new javax.swing.JLabel();
    final javax.swing.JLabel psswdLabel1 = new javax.swing.JLabel();
    final javax.swing.JTextField urlField = new javax.swing.JTextField();
    final javax.swing.JLabel urlLabel = new javax.swing.JLabel();
    final javax.swing.JTextField userField = new javax.swing.JTextField();
    final javax.swing.JLabel userLabel = new javax.swing.JLabel();
    final javax.swing.JLabel userLabel1 = new javax.swing.JLabel();
    final javax.swing.JButton validateButton = new javax.swing.JButton();
    final javax.swing.JLabel validateLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent e) {
        validateText();
    }

    public void removeUpdate(DocumentEvent e) {
        validateText();
    }

    public void changedUpdate(DocumentEvent e) {
        validateText();
    }

    private void validateText() {
        String txt = urlField.getText();
        validateButton.setEnabled(txt != null && !txt.equals(""));              // NOI18N
    }

    void enableFields(boolean bl) {
        psswdLabel.setEnabled(bl);
        psswdField.setEnabled(bl);
        userField.setEnabled(bl);
        userLabel.setEnabled(bl);
        nameField.setEnabled(bl);
        nameLabel.setEnabled(bl);
        urlField.setEnabled(bl);
        urlLabel.setEnabled(bl);
        httpCheckBox.setEnabled(bl);
        enableHttpFields();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == httpCheckBox) {
            enableHttpFields();
        }
    }

    private void enableHttpFields() {
        httpUserField.setEnabled(httpCheckBox.isSelected());
        httpPsswdField.setEnabled(httpCheckBox.isSelected());
    }
}
