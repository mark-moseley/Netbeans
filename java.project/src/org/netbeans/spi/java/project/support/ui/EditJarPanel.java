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

package org.netbeans.spi.java.project.support.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.spi.java.project.support.ui.EditJarSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkleint
 */
class EditJarPanel extends javax.swing.JPanel {

    private EditJarSupport.Item item;
    private AntProjectHelper helper;

    /** Creates new form EditJarPanel */
    private EditJarPanel() {
        initComponents();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditJarPanel.class, "ACSD_EditJarPanel"));
    }

    EditJarPanel(EditJarSupport.Item item, AntProjectHelper helper) {
        this();
        this.item = item;
        this.helper = helper;
        txtJar.setText(stripOffVariableMarkup(item.getJarFile()));
        if (item.getSourceFile() != null) {
            txtSource.setText(stripOffVariableMarkup(item.getSourceFile()));
        }
        if (item.getJavadocFile() != null) {
            txtJavadoc.setText(stripOffVariableMarkup(item.getJavadocFile()));
        }
    }

    private String stripOffVariableMarkup(String v) {
        if (!v.startsWith("${var.")) { // NOI18N
            return v;
        }
        int i = v.replace('\\', '/').indexOf('/'); // NOI18N
        if (i == -1) {
            i = v.length();
        }
        return v.substring(6, i-1)+v.substring(i);
    }
    
    private Set<String> getVariableNames() {
        Set<String> names = new HashSet<String>();
        for (String v : PropertyUtils.getGlobalProperties().keySet()) {
            if (!v.startsWith("var.")) { // NOI18N
                continue;
            }
            names.add(v.substring(4));
        }
        return names;
    }
    
    private String addVariableMarkup(String v) {
        int i = v.replace('\\', '/').indexOf('/'); // NOI18N
        if (i == -1) {
            i = v.length();
        }
        String varName = v.substring(0, i);
        if (!getVariableNames().contains(varName)) {
            return v;
        }
        return "${var." + varName + "}" + v.substring(i); // NOI18N
    }
    
    EditJarSupport.Item assignValues() {
        if (txtSource.getText() != null && txtSource.getText().trim().length() > 0) {
            item.setSourceFile(addVariableMarkup(txtSource.getText().trim()));
        } else {
            item.setSourceFile(null);
        }
        if (txtJavadoc.getText() != null && txtJavadoc.getText().trim().length() > 0) {
            item.setJavadocFile(addVariableMarkup(txtJavadoc.getText().trim()));
        } else {
            item.setJavadocFile(null);
        }
        return item;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJar = new javax.swing.JLabel();
        txtJar = new javax.swing.JTextField();
        lblJavadoc = new javax.swing.JLabel();
        txtJavadoc = new javax.swing.JTextField();
        btnJavadoc = new javax.swing.JButton();
        lblSource = new javax.swing.JLabel();
        txtSource = new javax.swing.JTextField();
        btnSource = new javax.swing.JButton();

        lblJar.setLabelFor(txtJar);
        org.openide.awt.Mnemonics.setLocalizedText(lblJar, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblJar.text")); // NOI18N

        txtJar.setEditable(false);

        lblJavadoc.setLabelFor(txtJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavadoc, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblJavadoc.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnJavadoc, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.btnJavadoc.text")); // NOI18N
        btnJavadoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJavadocActionPerformed(evt);
            }
        });

        lblSource.setLabelFor(txtSource);
        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSource, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.btnSource.text")); // NOI18N
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblJar)
                    .add(lblJavadoc)
                    .add(lblSource))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtSource, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                            .add(txtJavadoc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btnSource)
                            .add(btnJavadoc)))
                    .add(txtJar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblJar)
                    .add(txtJar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblJavadoc)
                    .add(btnJavadoc)
                    .add(txtJavadoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblSource)
                    .add(btnSource)
                    .add(txtSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblJar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJar")); // NOI18N
        txtJar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJar")); // NOI18N
        lblJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJavadoc")); // NOI18N
        txtJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJavadoc")); // NOI18N
        btnJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_btnJavadoc")); // NOI18N
        lblSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblSource")); // NOI18N
        txtSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblSource")); // NOI18N
        btnSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_btnSource")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void btnJavadocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJavadocActionPerformed
        // Let user search for the Jar file
        FileChooser chooser;
        if (helper.isSharableProject()) {
            chooser = new FileChooser(helper, true);
        } else {
            chooser = new FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
        }
        chooser.enableVariableBasedSelection(true);
        chooser.setFileHidingEnabled(false);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(NbBundle.getMessage(EditJarPanel.class, "LBL_Edit_Jar_Panel_browse"));
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new SimpleFileFilter(
                "Javadoc Entry (folder, ZIP or JAR file)", 
                new String[]{"ZIP", "JAR"}));   // NOI18N 
        File curDir = helper.resolveFile(helper.getStandardPropertyEvaluator().evaluate(item.getJarFile()));
        chooser.setCurrentDirectory(curDir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // Sow the chooser

        if (option == JFileChooser.APPROVE_OPTION) {
            String files[];
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }
            txtJavadoc.setText(chooser.getSelectedPathVariables() != null ? stripOffVariableMarkup(chooser.getSelectedPathVariables()[0]) : files[0]);
        }
        
    }//GEN-LAST:event_btnJavadocActionPerformed

    private void btnSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceActionPerformed
        FileChooser chooser;
        if (helper.isSharableProject()) {
            chooser = new FileChooser(helper, true);
        } else {
            chooser = new FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
        }
        chooser.enableVariableBasedSelection(true);
        chooser.setFileHidingEnabled(false);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(NbBundle.getMessage(EditJarPanel.class, "LBL_Edit_Jar_Panel_browse"));
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new SimpleFileFilter(
                "Source Entry (folder, ZIP or JAR file)", 
                new String[]{"ZIP", "JAR"}));   // NOI18N 
        File curDir = helper.resolveFile(helper.getStandardPropertyEvaluator().evaluate(item.getJarFile()));
        chooser.setCurrentDirectory(curDir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // Sow the chooser

        if (option == JFileChooser.APPROVE_OPTION) {
            String files[];
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }
            txtSource.setText(chooser.getSelectedPathVariables() != null ? stripOffVariableMarkup(chooser.getSelectedPathVariables()[0]) : files[0]);
        }

    }//GEN-LAST:event_btnSourceActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnJavadoc;
    private javax.swing.JButton btnSource;
    private javax.swing.JLabel lblJar;
    private javax.swing.JLabel lblJavadoc;
    private javax.swing.JLabel lblSource;
    private javax.swing.JTextField txtJar;
    private javax.swing.JTextField txtJavadoc;
    private javax.swing.JTextField txtSource;
    // End of variables declaration//GEN-END:variables
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;

        public SimpleFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index == name.length() - 1) {
                return false;
            }
            String extension = name.substring(index + 1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
}
