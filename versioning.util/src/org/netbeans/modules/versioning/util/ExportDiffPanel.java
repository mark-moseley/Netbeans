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
 * ExportDiffPanel.java
 *
 * Created on Mar 6, 2009, 10:43:25 PM
 */

package org.netbeans.modules.versioning.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author tomas
 */
public class ExportDiffPanel extends ExportDiffSupport.AbstractExportDiffPanel implements ActionListener {
    private JComponent attachComponent;

    /** Creates new form ExportDiffPanel */
    public ExportDiffPanel(JComponent attachComponent) {
        initComponents();
        this.attachComponent = attachComponent;
        asFileRadioButton.addActionListener(this);
        attachRadioButton.addActionListener(this);
        attachPanel.add(attachComponent);

        attachComponent.setEnabled(false);
        fileTextField.setEnabled(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();

        buttonGroup1.add(asFileRadioButton);
        asFileRadioButton.setSelected(true);
        asFileRadioButton.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.asFileRadioButton.text")); // NOI18N
        asFileRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asFileRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(attachRadioButton);
        attachRadioButton.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.attachRadioButton.text")); // NOI18N

        attachPanel.setLayout(new java.awt.BorderLayout());

        fileTextField.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.fileTextField.text")); // NOI18N
        fileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTextFieldActionPerformed(evt);
            }
        });

        browseButton.setText(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.browseButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(attachPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(asFileRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)
                        .add(20, 20, 20))
                    .add(layout.createSequentialGroup()
                        .add(attachRadioButton)
                        .addContainerGap(372, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(asFileRadioButton)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(attachRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(attachPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void asFileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_asFileRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_asFileRadioButtonActionPerformed

    private void fileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fileTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JRadioButton asFileRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JPanel attachPanel = new javax.swing.JPanel();
    final javax.swing.JRadioButton attachRadioButton = new javax.swing.JRadioButton();
    final javax.swing.JButton browseButton = new javax.swing.JButton();
    private javax.swing.ButtonGroup buttonGroup1;
    final javax.swing.JTextField fileTextField = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        if(asFileRadioButton.isSelected()) {
            attachComponent.setEnabled(false);
            fileTextField.setEnabled(true);
        } else {
            attachComponent.setEnabled(true);
            fileTextField.setEnabled(false);
        }
    }

    @Override
    public String getOutputFileText() {
        return fileTextField.getText();
    }

    @Override
    public void setOutputFileText(String text) {
        fileTextField.setText(text);
    }

    @Override
    public void addOutputFileTextDocumentListener(DocumentListener list) {
        fileTextField.getDocument().addDocumentListener(list);
    }

    @Override
    public void addBrowseActionListener(ActionListener actionListener) {
        browseButton.addActionListener(actionListener);
    }

    @Override
    public boolean isFileOutputSelected() {
        return asFileRadioButton.isSelected();
    }

}
