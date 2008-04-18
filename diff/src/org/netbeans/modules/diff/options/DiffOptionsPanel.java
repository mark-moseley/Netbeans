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

package org.netbeans.modules.diff.options;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import java.io.File;

/**
 * Diff Options panel.
 *
 * @author  Maros Sandor
 */
class DiffOptionsPanel extends javax.swing.JPanel implements ChangeListener, DocumentListener {

    private boolean isChanged;
    
    /** Creates new form DiffOptionsPanel */
    public DiffOptionsPanel() {
        initComponents();
        internalDiff.addChangeListener(this);
        externalDiff.addChangeListener(this);
        ignoreWhitespace.addChangeListener(this);
        externalCommand.getDocument().addDocumentListener(this);
        refreshComponents();
    }

    private void refreshComponents() {
        ignoreWhitespace.setEnabled(internalDiff.isSelected());
        jLabel1.setEnabled(externalDiff.isSelected());
        externalCommand.setEnabled(externalDiff.isSelected());
        browseCommand.setEnabled(externalDiff.isSelected());
    }

    public JTextField getExternalCommand() {
        return externalCommand;
    }

    public JRadioButton getExternalDiff() {
        return externalDiff;
    }

    public JCheckBox getIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    public JCheckBox getIgnoreInnerWhitespace() {
        return ignoreAllWhitespace;
    }

    public JCheckBox getIgnoreCase() {
        return ignoreCase;
    }

    public JRadioButton getInternalDiff() {
        return internalDiff;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public boolean isChanged() {
        return isChanged;
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        internalDiff = new javax.swing.JRadioButton();
        ignoreWhitespace = new javax.swing.JCheckBox();
        ignoreAllWhitespace = new javax.swing.JCheckBox();
        ignoreCase = new javax.swing.JCheckBox();
        externalDiff = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        externalCommand = new javax.swing.JTextField();
        browseCommand = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 5));

        buttonGroup1.add(internalDiff);
        org.openide.awt.Mnemonics.setLocalizedText(internalDiff, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jRadioButton1.text")); // NOI18N
        internalDiff.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        internalDiff.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(ignoreWhitespace, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jCheckBox1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreAllWhitespace, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "DiffOptionsPanel.ignoreAllWhitespace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreCase, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "DiffOptionsPanel.ignoreCase.text")); // NOI18N

        buttonGroup1.add(externalDiff);
        org.openide.awt.Mnemonics.setLocalizedText(externalDiff, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jRadioButton2.text")); // NOI18N
        externalDiff.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        externalDiff.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setLabelFor(externalCommand);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jLabel1.text")); // NOI18N

        externalCommand.setText(org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseCommand, org.openide.util.NbBundle.getMessage(DiffOptionsPanel.class, "jButton1.text")); // NOI18N
        browseCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCommandActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(internalDiff)
                .add(33, 33, 33)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ignoreAllWhitespace)
                    .add(ignoreWhitespace)
                    .add(ignoreCase))
                .add(185, 185, 185))
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(externalDiff))
                .add(31, 31, 31)
                .add(externalCommand, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseCommand))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(internalDiff)
                    .add(ignoreWhitespace))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignoreAllWhitespace)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignoreCase)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(externalDiff)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseCommand)
                    .add(externalCommand, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseCommandActionPerformed
        String execPath = externalCommand.getText();
        File oldFile = FileUtil.normalizeFile(new File(execPath));
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(DiffOptionsPanel.class, "ACSD_BrowseFolder"), oldFile); // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(DiffOptionsPanel.class, "BrowseFolder_Title")); // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this, NbBundle.getMessage(DiffOptionsPanel.class, "BrowseFolder_OK")); // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            externalCommand.setText(f.getAbsolutePath() + " {0} {1}");
        }
    }//GEN-LAST:event_browseCommandActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseCommand;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField externalCommand;
    private javax.swing.JRadioButton externalDiff;
    private javax.swing.JCheckBox ignoreAllWhitespace;
    private javax.swing.JCheckBox ignoreCase;
    private javax.swing.JCheckBox ignoreWhitespace;
    private javax.swing.JRadioButton internalDiff;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public void stateChanged(ChangeEvent e) {
        isChanged = true;
        refreshComponents();
    }

    public void insertUpdate(DocumentEvent e) {
        isChanged = true;
    }

    public void removeUpdate(DocumentEvent e) {
        isChanged = true;
    }

    public void changedUpdate(DocumentEvent e) {
        isChanged = true;
    }
}
