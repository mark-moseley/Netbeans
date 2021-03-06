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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties.CodebaseComboBoxModel;
import org.netbeans.modules.javawebstart.CustomizerRunComponent;

/**
 *
 * @author  Milan Kubec
 */
public class JWSCustomizerPanel extends JPanel implements HelpCtx.Provider {
    
    private JWSProjectProperties jwsProps;
    private File lastImageFolder = null;
    
    public static CustomizerRunComponent runComponent;
    static {
        runComponent = new CustomizerRunComponent();
    }
    
    /** Creates new form JWSCustomizerPanel */
    public JWSCustomizerPanel(JWSProjectProperties props) {
        
        this.jwsProps = props;
        
        initComponents();
        
        enableCheckBox.setModel(jwsProps.enabledModel);
        enableCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.enableCheckBox.mnemonic").toCharArray()[0]);
        offlineCheckBox.setModel(jwsProps.allowOfflineModel);
        offlineCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.offlineCheckBox.mnemonic").toCharArray()[0]);
        signedCheckBox.setModel(jwsProps.signedModel);
        signedCheckBox.setMnemonic(NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.signedCheckBox.mnemonic").toCharArray()[0]);
        iconTextField.setDocument(jwsProps.iconDocument);
        codebaseComboBox.setModel(jwsProps.codebaseModel);
        codebaseTextField.setDocument(jwsProps.codebaseURLDocument);
        
        setCodebaseComponents();
        boolean enableSelected = enableCheckBox.getModel().isSelected();
        setEnabledAllComponents(enableSelected);
        setEnabledRunComponent(enableSelected);
        
    }
    
    private static void setEnabledRunComponent(boolean enable) {
        runComponent.setCheckboxEnabled(enable);
        runComponent.setHintVisible(!enable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        enableCheckBox = new javax.swing.JCheckBox();
        iconLabel = new javax.swing.JLabel();
        codebaseLabel = new javax.swing.JLabel();
        iconTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        codebaseComboBox = new javax.swing.JComboBox();
        codebaseTextField = new javax.swing.JTextField();
        offlineCheckBox = new javax.swing.JCheckBox();
        panelDescLabel = new javax.swing.JLabel();
        signedCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(enableCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.enableCheckBox.text")); // NOI18N
        enableCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 2));
        enableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(enableCheckBox, gridBagConstraints);
        enableCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_EnableWebStart_CheckBox")); // NOI18N
        enableCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_EnableWebStart_Label")); // NOI18N

        iconLabel.setLabelFor(iconTextField);
        org.openide.awt.Mnemonics.setLocalizedText(iconLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.iconLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(iconLabel, gridBagConstraints);
        iconLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Icon_Label")); // NOI18N
        iconLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Icon_Label")); // NOI18N

        codebaseLabel.setLabelFor(codebaseComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(codebaseLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.codebaseLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(codebaseLabel, gridBagConstraints);
        codebaseLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_Label")); // NOI18N
        codebaseLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_Label")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        add(iconTextField, gridBagConstraints);
        iconTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Icon_TextField")); // NOI18N
        iconTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Icon_TextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Browse_Button")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Browse_Button")); // NOI18N

        codebaseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codebaseComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(codebaseComboBox, gridBagConstraints);
        codebaseComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_Combobox")); // NOI18N
        codebaseComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_Combobox")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(codebaseTextField, gridBagConstraints);
        codebaseTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_Codebase_TextField")); // NOI18N
        codebaseTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_Codebase_TextField")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(offlineCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.offlineCheckBox.text")); // NOI18N
        offlineCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        offlineCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 0);
        add(offlineCheckBox, gridBagConstraints);
        offlineCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_AllowOffline_Checkbox")); // NOI18N
        offlineCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_AllowOffline_Checkbox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(panelDescLabel, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.panelDescLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(panelDescLabel, gridBagConstraints);
        panelDescLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_WebStartTitle_Label")); // NOI18N
        panelDescLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_WebStartTitle_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(signedCheckBox, org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "JWSCustomizerPanel.signedCheckBox.text")); // NOI18N
        signedCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signedCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 0);
        add(signedCheckBox, gridBagConstraints);
        signedCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSN_SelfSigned_Checkbox")); // NOI18N
        signedCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JWSCustomizerPanel.class, "ACSD_SelfSigned_Checkbox")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void codebaseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codebaseComboBoxActionPerformed
        setCodebaseComponents();
    }//GEN-LAST:event_codebaseComboBoxActionPerformed

    private void enableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCheckBoxActionPerformed
        boolean isSelected = enableCheckBox.getModel().isSelected();
        setEnabledAllComponents(isSelected);
        setEnabledRunComponent(isSelected);
    }//GEN-LAST:event_enableCheckBoxActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new IconFileFilter());
        if (lastImageFolder != null) {
            chooser.setSelectedFile(lastImageFolder);
        } else { // ???
            // workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
            // chooser.setSelectedFile(new File(workDir));
        }
        chooser.setDialogTitle(NbBundle.getMessage(JWSCustomizerPanel.class, "LBL_Select_Icon_Image"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            iconTextField.setText(file.getAbsolutePath());
            lastImageFolder = file.getParentFile();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    public HelpCtx getHelpCtx() {
        return new HelpCtx(JWSCustomizerPanel.class);
    }
    
    private static class IconFileFilter extends FileFilter {
        
        // XXX should check size of images?
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');
            if (index > 0 && index < name.length() - 1) {
                String ext = name.substring(index+1).toLowerCase();
                if ("gif".equals(ext) || "png".equals(ext) || "jpg".equals(ext)) { // NOI18N
                    return true;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(JWSCustomizerPanel.class, "MSG_IconFileFilter_Description");
        }
        
    }
    
    private CodebaseComboBoxModel getCBModel() {
        return (CodebaseComboBoxModel) codebaseComboBox.getModel();
    }
    
    private void setCodebaseComponents() {
        String value = getCBModel().getSelectedCodebaseItem();
        if (JWSProjectProperties.CB_TYPE_LOCAL.equals(value)) {
            codebaseTextField.setText(jwsProps.getProjectDistDir());
            codebaseTextField.setEditable(false);
        } else if (JWSProjectProperties.CB_TYPE_WEB.equals(value)) {
            codebaseTextField.setText(JWSProjectProperties.CB_URL_WEB);
            codebaseTextField.setEditable(false);
        } else if (JWSProjectProperties.CB_TYPE_USER.equals(value)) {
            codebaseTextField.setText(jwsProps.getCodebaseLocation());
            codebaseTextField.setEditable(true);
        }
    }
    
    private void setEnabledAllComponents(boolean b) {
        iconLabel.setEnabled(b);
        iconTextField.setEnabled(b);
        browseButton.setEnabled(b);
        codebaseLabel.setEnabled(b);
        codebaseComboBox.setEnabled(b);
        codebaseTextField.setEnabled(b);
        offlineCheckBox.setEnabled(b);
        signedCheckBox.setEnabled(b);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox codebaseComboBox;
    private javax.swing.JLabel codebaseLabel;
    private javax.swing.JTextField codebaseTextField;
    private javax.swing.JCheckBox enableCheckBox;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JTextField iconTextField;
    private javax.swing.JCheckBox offlineCheckBox;
    private javax.swing.JLabel panelDescLabel;
    private javax.swing.JCheckBox signedCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
