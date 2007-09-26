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

package org.netbeans.modules.apisupport.project.ui.wizard.moduleinstall;

import java.awt.Component;
import java.io.IOException;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
final class ModuleInstallPanel extends BasicWizardIterator.Panel {
    
    private final DataModel data;
    private DocumentListener updateListener;
    
    /** Creates new NameAndLocationPanel */
    public ModuleInstallPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        if (data.getPackageName() != null) {
            packageName.setSelectedItem(data.getPackageName());
        }
        putClientProperty("NewFileWizard_Title", getMessage("LBL_ModuleInstallWizardTitle"));
        updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
    }
    
    private void addListeners() {
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().addDocumentListener(updateListener);
        }
    }
    
    private void removeListeners() {
        Component editorComp = packageName.getEditor().getEditorComponent();
        if (editorComp instanceof JTextComponent) {
            ((JTextComponent) editorComp).getDocument().removeDocumentListener(updateListener);
        }
    }
    
    protected void storeToDataModel() {
        removeListeners();
        updateData();
    }
    
    protected void readFromDataModel() {
        addListeners();
    }
    
    public void addNotify() {
        super.addNotify();
        updateData();
    }
    
    private void updateData() {
        data.setPackageName(packageName.getEditor().getItem().toString());
        if (checkValidity()) {
            createdFilesValue.setText(UIUtil.generateTextAreaContent(
                    data.getCreatedModifiedFiles().getCreatedPaths()));
            modifiedFilesValue.setText(UIUtil.generateTextAreaContent(
                    data.getCreatedModifiedFiles().getModifiedPaths()));
        }
    }
    
    private boolean checkValidity() {
        String moduleInstall = getModuleInstall();
        if (moduleInstall != null) {
            setError(NbBundle.getMessage(ModuleInstallPanel.class, "ERR_ModuleInstallAlreadyPresented", moduleInstall));
            return false;
        }
        // #68294 check if the paths for newly created files are valid or not..
        String pName = packageName.getEditor().getItem() == null ? ""
                : packageName.getEditor().getItem().toString().trim();
        if (pName.length() == 0 || !UIUtil.isValidPackageName(pName)) {
            setError(getMessage("ERR_PackageInvalid"));
            return false;
        }
        String[] invalid = data.getCreatedModifiedFiles().getInvalidPaths();
        if (invalid.length > 0) {
            setError(NbBundle.getMessage(ModuleInstallPanel.class,
                    "ERR_ToBeCreateFileExists", invalid[0])); // NOI18N
            return false;
        } else {
            markValid();
            return true;
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_ModuleInstallPanel_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(ModuleInstallPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(ModuleInstallPanel.class, key);
    }
    
    private void initAccessibility() {
        projectNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ProjectName"));
        packageName.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PackageName"));
        createdFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CreatedFilesValue"));
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModifiedFilesValue"));
    }
    
    private String getModuleInstall() {
        String moduleInstall = null;
        try {
            EditableManifest mf = Util.loadManifest(data.getModuleInfo().getManifestFile());
            moduleInstall = mf.getAttribute(DataModel.OPENIDE_MODULE_INSTALL, null);
        } catch (IOException e) {
            assert false : e;
        }
        return moduleInstall;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectName = new javax.swing.JLabel();
        projectNameValue = new JTextField(ProjectUtils.getInformation(this.data.getProject()).getDisplayName());
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        createdFilesValueS = new javax.swing.JScrollPane();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValueS = new javax.swing.JScrollPane();
        modifiedFilesValue = new javax.swing.JTextArea();
        packageName = UIUtil.createPackageComboBox(data.getSourceRootGroup());
        packageNameTxt = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        projectName.setLabelFor(projectNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(projectName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/moduleinstall/Bundle").getString("LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        add(projectName, gridBagConstraints);

        projectNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/moduleinstall/Bundle").getString("LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/moduleinstall/Bundle").getString("LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValueS, gridBagConstraints);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFilesValueS, gridBagConstraints);

        packageName.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(packageName, gridBagConstraints);

        packageNameTxt.setLabelFor(packageName);
        org.openide.awt.Mnemonics.setLocalizedText(packageNameTxt, org.openide.util.NbBundle.getMessage(ModuleInstallPanel.class, "LBL_PackageName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(packageNameTxt, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JScrollPane createdFilesValueS;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JScrollPane modifiedFilesValueS;
    private javax.swing.JComboBox packageName;
    private javax.swing.JLabel packageNameTxt;
    private javax.swing.JLabel projectName;
    private javax.swing.JTextField projectNameValue;
    // End of variables declaration//GEN-END:variables
    
}
