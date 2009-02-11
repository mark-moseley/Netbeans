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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;

import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.spi.project.support.ant.ui.VariablesSupport;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  David Konecny
 */
public class BrokenReferencesCustomizer extends javax.swing.JPanel {

    private BrokenReferencesModel model;
    private File lastSelectedFile;
    
    /** Creates new form BrokenReferencesCustomizer */
    public BrokenReferencesCustomizer(BrokenReferencesModel model) {
        initComponents();
        this.model = model;
        errorList.setModel(model);
        errorList.setSelectedIndex(0);
        errorList.setCellRenderer(new ListCellRendererImpl(model));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        errorListLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorList = new javax.swing.JList();
        fix = new javax.swing.JButton();
        descriptionLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(450, 300));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "ACSN_BrokenReferencesCustomizer"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "ACSD_BrokenReferencesCustomizer"));
        errorListLabel.setLabelFor(errorList);
        org.openide.awt.Mnemonics.setLocalizedText(errorListLabel, org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_List"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 3, 0);
        add(errorListLabel, gridBagConstraints);
        errorListLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "ACSD_BrokenLinksCustomizer_List"));

        errorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        errorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                errorListValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(errorList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fix, org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_Fix"));
        fix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 12);
        add(fix, gridBagConstraints);
        fix.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "ACSD_BrokenLinksCustomizer_Fix"));

        descriptionLabel.setLabelFor(description);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_Description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 3, 0);
        add(descriptionLabel, gridBagConstraints);
        descriptionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenReferencesCustomizer.class, "ACSD_BrokenLinksCustomizer_Description"));

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        jScrollPane2.setViewportView(description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jScrollPane2, gridBagConstraints);

    }//GEN-END:initComponents

    private void errorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_errorListValueChanged
        updateSelection();
    }//GEN-LAST:event_errorListValueChanged

    private void fixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixActionPerformed
        int index = errorList.getSelectedIndex();
        if (index==-1) {
            return;
        }
        BrokenReferencesModel.OneReference or = model.getOneReference(index);
        if (or.getType() == BrokenReferencesModel.REF_TYPE_LIBRARY ||
            or.getType() == BrokenReferencesModel.REF_TYPE_LIBRARY_CONTENT) {
            LibrariesCustomizer.showCustomizer(null, model.getProjectLibraryManager());
        } else if (or.getType() == BrokenReferencesModel.REF_TYPE_PLATFORM) {
            PlatformsCustomizer.showCustomizer(null);
        } else if (or.getType() == BrokenReferencesModel.REF_TYPE_VARIABLE || or.getType() == BrokenReferencesModel.REF_TYPE_VARIABLE_CONTENT) {
            VariablesSupport.showVariablesCustomizer();
        } else {
            JFileChooser chooser;
            if (or.getType() == BrokenReferencesModel.REF_TYPE_PROJECT) {
                chooser = ProjectChooser.projectChooser();
                chooser.setDialogTitle(NbBundle.getMessage(BrokenReferencesCustomizer.class, 
                    "LBL_BrokenLinksCustomizer_Resolve_Project", or.getDisplayID()));
            } else {
                chooser = new JFileChooser();
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setDialogTitle(NbBundle.getMessage(BrokenReferencesCustomizer.class, 
                    "LBL_BrokenLinksCustomizer_Resolve_File", or.getDisplayID()));
            }
            if (lastSelectedFile != null) {
                chooser.setSelectedFile(lastSelectedFile);
            }
            int option = chooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                model.updateReference(errorList.getSelectedIndex(), chooser.getSelectedFile());
                lastSelectedFile = chooser.getSelectedFile();
            }
        }
        model.refresh();
        updateSelection();
    }//GEN-LAST:event_fixActionPerformed

    private void updateSelection() {
        if (errorList.getSelectedIndex() != -1 && errorList.getSelectedIndex() < model.getSize()) {
            if (model.isBroken(errorList.getSelectedIndex())) {
                description.setText(model.getDesciption(errorList.getSelectedIndex()));
                fix.setEnabled(true);
            } else {
                description.setText(NbBundle.getMessage(BrokenReferencesCustomizer.class, 
                    "LBL_BrokenLinksCustomizer_Problem_Was_Resolved"));
                // Leave the button always enabled so that user can alter 
                // resolved reference. Especially needed for automatically
                // resolved JAR references.
                fix.setEnabled(true);
            }
        } else {
            description.setText("");
            fix.setEnabled(false);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea description;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JList errorList;
    private javax.swing.JLabel errorListLabel;
    private javax.swing.JButton fix;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    private static Icon brokenRef = ImageUtilities.loadImageIcon("org/netbeans/modules/java/project/resources/broken-reference.gif", false); // NOI18N
    private static Icon resolvedRef = ImageUtilities.loadImageIcon("org/netbeans/modules/java/project/resources/resolved-reference.gif", false); // NOI18N

    private static class ListCellRendererImpl extends DefaultListCellRenderer {

        private BrokenReferencesModel model;
        
        public ListCellRendererImpl(BrokenReferencesModel model) {
            this.model = model;
        }
        
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );            
            if (model.isBroken(index)) {
                setIcon(brokenRef);
            } else {
                setIcon(resolvedRef);
            }
            
            return this;
        }
    }
    
}
