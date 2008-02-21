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
package org.netbeans.modules.mercurial.ui.clone;

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.modules.mercurial.HgModuleConfig;

/**
 *
 * @author  Padraig O'Briain
 */
public class ClonePanel extends javax.swing.JPanel implements ActionListener {

    private File                            repository;

    /** Creates new form ClonePanel */
    public ClonePanel(File repo, File to) {
        repository = repo;
        initComponents();
        browseButton.addActionListener(this);
        setMainCheckBox.addActionListener(this);
        fromTextField.setText(repo.getAbsolutePath());
        toTextField.setText(to.getParent());
        toCloneField.setText(to.getName());
        setMainCheckBox.setSelected(HgModuleConfig.getDefault().getSetMainProject());
    }

    public String getOutputFileName() {
        File target = new File(toTextField.getText(), toCloneField.getText());
        return target.getAbsolutePath();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fromLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        fromTextField = new javax.swing.JTextField();
        toNameLabel = new javax.swing.JLabel();
        toCloneField = new javax.swing.JTextField();
        destinationLabel = new javax.swing.JLabel();
        setMainCheckBox = new javax.swing.JCheckBox();

        fromLabel.setLabelFor(fromTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fromLabel, org.openide.util.NbBundle.getMessage(ClonePanel.class, "ClonePanel.fromLabel.text")); // NOI18N

        toLabel.setLabelFor(toTextField);
        org.openide.awt.Mnemonics.setLocalizedText(toLabel, org.openide.util.NbBundle.getMessage(ClonePanel.class, "ClonePanel.toLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ClonePanel.class, "ClonePanel.browseButton.text")); // NOI18N

        fromTextField.setEditable(false);

        toNameLabel.setLabelFor(toCloneField);
        org.openide.awt.Mnemonics.setLocalizedText(toNameLabel, org.openide.util.NbBundle.getMessage(ClonePanel.class, "ClonePanel.toName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(destinationLabel, org.openide.util.NbBundle.getMessage(ClonePanel.class, "destinationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(setMainCheckBox, org.openide.util.NbBundle.getMessage(ClonePanel.class, "openCheckbox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationLabel)
                    .add(fromLabel)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(toNameLabel)
                            .add(setMainCheckBox)))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(toLabel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fromTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(toCloneField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .add(toTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fromLabel)
                    .add(fromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(destinationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(toTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(toLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(toCloneField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(toNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setMainCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        toTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClonePanel.class, "ACSD_toTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClonePanel.class, "ACSD_Browse")); // NOI18N
        fromTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClonePanel.class, "ACSD_fromTextField")); // NOI18N
        toCloneField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClonePanel.class, "ACSD_toCloneField")); // NOI18N
        setMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClonePanel.class, "ACSD_setMainCheckBox")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            onBrowseClick();
        } else if (evt.getSource() == setMainCheckBox) {
            HgModuleConfig.getDefault().setSetMainProject(setMainCheckBox.isSelected());
        }
    }

    private void onBrowseClick() {
        File oldFile = defaultWorkingDirectory();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ClonePanel.class, "ACSD_BrowseFolder"), oldFile);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ClonePanel.class, "Browse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(ClonePanel.class, "Folders");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(this, NbBundle.getMessage(ClonePanel.class, "OK_Button"));                                            // NO I18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            toTextField.setText(f.getAbsolutePath());
        }
    }
    /**
     * Returns file to be initally used.
     * <ul>
     * <li>first is takes text in workTextField
     * <li>then recent project folder
     * <li>finally <tt>user.home</tt>
     * <ul>
     */
    private File defaultWorkingDirectory() {
        File defaultDir = null;
        String current = toTextField.getText();
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
            File projectFolder = ProjectChooser.getProjectsFolder();
            if (projectFolder.exists() && projectFolder.isDirectory()) {
                defaultDir = projectFolder;
            }

        }

        if (defaultDir == null) {
            defaultDir = new File(System.getProperty("user.home"));  // NOI18N
        }

        return defaultDir;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JTextField fromTextField;
    private javax.swing.JCheckBox setMainCheckBox;
    private javax.swing.JTextField toCloneField;
    private javax.swing.JLabel toLabel;
    private javax.swing.JLabel toNameLabel;
    final javax.swing.JTextField toTextField = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
    
}
