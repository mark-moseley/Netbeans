/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.io.File;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;

import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
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
            LibrariesCustomizer.showCustomizer(null);
        } else if (or.getType() == BrokenReferencesModel.REF_TYPE_PLATFORM) {
            PlatformsCustomizer.showCustomizer(null);
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

    private static Icon brokenRef = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/project/resources/broken-reference.gif")); // NOI18N
    private static Icon resolvedRef = new ImageIcon(Utilities.loadImage("org/netbeans/modules/java/project/resources/resolved-reference.gif")); // NOI18N

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
