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
package org.netbeans.modules.cnd.ui.options;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
        
public class AddCompilerSetPanel extends javax.swing.JPanel implements DocumentListener {
    private DialogDescriptor dialogDescriptor = null;
    private CompilerSetManager csm;
    
    /** Creates new form AddCompilerSetPanel */
    public AddCompilerSetPanel(CompilerSetManager csm) {
        initComponents();
        this.csm = csm;
        
        List<CompilerFlavor> list = CompilerFlavor.getFlavors();
        for (CompilerFlavor cf : list) {
            cbFamily.addItem(cf);
        }
        tfName.setText(""); // NOI18N
        taInfo.setBackground(getBackground());
        validateData();
        
        setPreferredSize(new Dimension(700, 300));
        
        tfBaseDirectory.getDocument().addDocumentListener(this);
        tfName.getDocument().addDocumentListener(this);
    }

    private static String getString(String key) {
        return NbBundle.getMessage(AddCompilerSetPanel.class, key);
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        dialogDescriptor.setValid(false);
    }
    
    private void updateDataBaseDir() {
        File dirFile = new File(tfBaseDirectory.getText());
        ArrayList<String> list = new ArrayList<String>();
        if (new File(dirFile, "cc").exists()) // NOI18N
            list.add("cc"); // NOI18N
        if (new File(dirFile, "gcc").exists()) // NOI18N
            list.add("gcc"); // NOI18N
        CompilerSet.CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dirFile.getAbsolutePath(), (String[])list.toArray(new String[list.size()]));
        cbFamily.setSelectedItem(flavor);
        updateDataFamily();
        if (!dialogDescriptor.isValid()) {
            tfName.setText("");
        }
    }
    
    private void updateDataFamily() {
        CompilerSet.CompilerFlavor flavor = (CompilerSet.CompilerFlavor)cbFamily.getSelectedItem();
        int n = 0;
        String suggestedName = null;
        while (true) {
            suggestedName = flavor.toString() + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (csm.getCompilerSet(suggestedName) != null) {
                n++;
            }
            else {
                break;
            }
        }
        tfName.setText(suggestedName);
        
        updateDataName();
    }
    
    private void updateDataName() {
        validateData();
    }
    
    private void validateData() {
        boolean valid = true;
        lbError.setText(""); // NOI18N
        
        File dirFile = new File(tfBaseDirectory.getText());
        if (valid && !dirFile.exists() || !dirFile.isDirectory() || !IpeUtils.isPathAbsolute(dirFile.getPath())) {
            valid = false;
            lbError.setText(getString("BASE_INVALID"));
            cbFamily.setEnabled(false);
            tfName.setEnabled(false);
        }
        else {
            cbFamily.setEnabled(true);
            tfName.setEnabled(true);
        }
        
        String compilerSetName = IpeUtils.replaceOddCharacters(tfName.getText().trim(), '_');
        if (valid && compilerSetName.length() == 0 || compilerSetName.contains("|")) { // NOI18N
            valid = false;
            lbError.setText(getString("NAME_INVALID"));
        }
        
        if (valid && csm.getCompilerSet(compilerSetName.trim()) != null) {
            valid = false;
            lbError.setText(getString("TOOLNAME_ALREADY_EXISTS"));
        }
        
        if (dialogDescriptor != null)
            dialogDescriptor.setValid(valid);
    }
    
    private void handleUpdate(DocumentEvent e) {
        if (e.getDocument() == tfBaseDirectory.getDocument()) {
            updateDataBaseDir();
        }
        else {
            updateDataName();
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        //validateData();
    }
    
    public String getBaseDirectory() {
        return tfBaseDirectory.getText();
    }
    
    public CompilerSet.CompilerFlavor getFamily() {
        return (CompilerSet.CompilerFlavor)cbFamily.getSelectedItem();
    }
    
    public String getCompilerSetName() {
        return IpeUtils.replaceOddCharacters(tfName.getText().trim(), '_');
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbBaseDirectory = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        lbFamily = new javax.swing.JLabel();
        cbFamily = new javax.swing.JComboBox();
        lbName = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbError = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taInfo = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        lbBaseDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("lbBaseDirectory_MN").charAt(0));
        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 0);
        add(lbBaseDirectory, gridBagConstraints);

        tfName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 16, 0);
        add(tfName, gridBagConstraints);
        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfName.AccessibleContext.accessibleDescription")); // NOI18N

        btBaseDirectory.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("btBrowse").charAt(0));
        btBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.text")); // NOI18N
        btBaseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBaseDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 4, 0, 16);
        add(btBaseDirectory, gridBagConstraints);
        btBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        lbFamily.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("lbFamily_MN").charAt(0));
        lbFamily.setLabelFor(cbFamily);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle"); // NOI18N
        lbFamily.setText(bundle.getString("AddCompilerSetPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("AddCompilerSetPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(lbFamily, gridBagConstraints);

        cbFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFamilyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(cbFamily, gridBagConstraints);

        lbName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("lbToolSetName_MN").charAt(0));
        lbName.setLabelFor(tfName);
        lbName.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 16, 0);
        add(lbName, gridBagConstraints);

        tfBaseDirectory.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 4, 0, 0);
        add(tfBaseDirectory, gridBagConstraints);
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        lbError.setForeground(new java.awt.Color(255, 51, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 16, 16);
        add(lbError, gridBagConstraints);

        jScrollPane1.setBorder(null);

        taInfo.setColumns(20);
        taInfo.setEditable(false);
        taInfo.setLineWrap(true);
        taInfo.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.text")); // NOI18N
        taInfo.setWrapStyleWord(true);
        taInfo.setBorder(null);
        jScrollPane1.setViewportView(taInfo);
        taInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.AccessibleContext.accessibleName")); // NOI18N
        taInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 16);
        add(jScrollPane1, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    String seed = null;
    if (tfBaseDirectory.getText().length() > 0) {
        seed = tfBaseDirectory.getText();
    }
    else if (FileChooser.getCurrectChooserFile() != null) {
        seed = FileChooser.getCurrectChooserFile().getPath();
    }
    else {
        seed = System.getProperty("user.home"); // NOI18N
    }
    FileChooser fileChooser = new FileChooser(getString("SELECT_BASE_DIRECTORY_TITLE"), null, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    String dirPath = fileChooser.getSelectedFile().getPath();
    tfBaseDirectory.setText(dirPath);
    
    updateDataBaseDir();
}//GEN-LAST:event_btBaseDirectoryActionPerformed

private void cbFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFamilyActionPerformed
    updateDataFamily();
}//GEN-LAST:event_cbFamilyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBaseDirectory;
    private javax.swing.JComboBox cbFamily;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbError;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbName;
    private javax.swing.JTextArea taInfo;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
