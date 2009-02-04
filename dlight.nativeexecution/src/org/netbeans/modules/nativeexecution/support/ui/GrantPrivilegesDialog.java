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

/*
 * GrantPrivilegesDialog.java
 *
 * Created on 16.12.2008, 20:32:59
 */
package org.netbeans.modules.nativeexecution.support.ui;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class GrantPrivilegesDialog extends javax.swing.JPanel {

    /** Creates new form GrantPrivilegesDialog */
    public GrantPrivilegesDialog() {
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        suLoginLabel = new javax.swing.JLabel();
        suPasswordLabel = new javax.swing.JLabel();
        suLoginField = new javax.swing.JTextField();
        suPasswordField = new javax.swing.JPasswordField();

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(GrantPrivilegesDialog.class, "GrantPrivilegesDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setFocusable(false);
        jScrollPane1.setViewportView(jTextArea1);

        suLoginLabel.setText(org.openide.util.NbBundle.getMessage(GrantPrivilegesDialog.class, "GrantPrivilegesDialog.suLoginLabel.text")); // NOI18N

        suPasswordLabel.setText(org.openide.util.NbBundle.getMessage(GrantPrivilegesDialog.class, "GrantPrivilegesDialog.suPasswordLabel.text")); // NOI18N

        suLoginField.setText(org.openide.util.NbBundle.getMessage(GrantPrivilegesDialog.class, "GrantPrivilegesDialog.suLoginField.text")); // NOI18N

        suPasswordField.setText(org.openide.util.NbBundle.getMessage(GrantPrivilegesDialog.class, "GrantPrivilegesDialog.suPasswordField.text")); // NOI18N
        suPasswordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                hdlPasswordFieldFocus(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(suLoginLabel)
                            .add(suPasswordLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(suPasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                            .add(suLoginField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(suLoginLabel)
                    .add(suLoginField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(suPasswordLabel)
                    .add(suPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void hdlPasswordFieldFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hdlPasswordFieldFocus
        suPasswordField.selectAll();
    }//GEN-LAST:event_hdlPasswordFieldFocus

    public boolean askPassword() {
        suPasswordField.setText(""); // NOI18N

        DialogDescriptor dd = new DialogDescriptor(this,
                loc("GrantPrivilegesDialog.title"), true, // NOI18N
                new Object[]{
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.CANCEL_OPTION
                }, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.addWindowFocusListener(new WindowAdapter() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                suPasswordField.requestFocusInWindow();
            }
        });

        dialog.setVisible(true);

        return dd.getValue() == DialogDescriptor.OK_OPTION;
    }

    public String getPassword() {
        return String.valueOf(suPasswordField.getPassword());
    }

    public String getUser() {
        return String.valueOf(suLoginField.getText());
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(GrantPrivilegesDialog.class, key, params);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField suLoginField;
    private javax.swing.JLabel suLoginLabel;
    private javax.swing.JPasswordField suPasswordField;
    private javax.swing.JLabel suPasswordLabel;
    // End of variables declaration//GEN-END:variables
}
