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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class PanelProjectLocationVisual extends SettingsPanel
        implements DocumentListener, HelpCtx.Provider {

    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    private PanelConfigureProject panel;
    private String templateName;
    private String name;
    private boolean makefileNameChanged = false;
    private int type;

    /** Creates new form PanelProjectLocationVisual */
    public PanelProjectLocationVisual(PanelConfigureProject panel, String name, boolean showMakefileTextField, int type) {
        initComponents();
        this.panel = panel;
        this.name = name;
        this.templateName = name;
        this.type = type;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        if (showMakefileTextField) {
            makefileTextField.getDocument().addDocumentListener(this);
            makefileTextField.getDocument().addDocumentListener(new MakefileDocumentListener());
        } else {
            makefileTextField.setVisible(false);
            makefileLabel.setVisible(false);
        }

        // Accessibility
        makefileTextField.getAccessibleContext().setAccessibleDescription(getString("AD_MAKEFILE"));

        setAsMainCheckBox.setVisible(true);

        createMainTextField.setText("main"); // NOI18N
        createMainComboBox.addItem("C"); // NOI18N
        createMainComboBox.addItem("C++"); // NOI18N
        createMainComboBox.setSelectedIndex(0);

        if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
        } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(false);
        } else {
            createMainCheckBox.setSelected(false);
            createMainCheckBox.setVisible(false);
            createMainTextField.setVisible(false);
            createMainComboBox.setVisible(false);
        }
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewAppWizard"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        makefileLabel = new javax.swing.JLabel();
        makefileTextField = new javax.swing.JTextField();
        createMainCheckBox = new javax.swing.JCheckBox();
        createMainTextField = new javax.swing.JTextField();
        createMainComboBox = new javax.swing.JComboBox();
        setAsMainCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectNameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectNameLabel")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectNameLabel")); // NOI18N

        projectNameTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectNameTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectLocationLabel")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectLocationLabel")); // NOI18N

        projectLocationTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectLocationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_browseButton")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_browseButton")); // NOI18N

        createdFolderLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("MN_NWP1_CreatedProjectFolder_Lablel").charAt(0));
        createdFolderLabel.setLabelFor(createdFolderTextField);
        createdFolderLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(createdFolderLabel, gridBagConstraints);
        createdFolderLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_createdFolderLabel")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_createdFolderLabel")); // NOI18N

        createdFolderTextField.setColumns(20);
        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        add(createdFolderTextField, gridBagConstraints);

        makefileLabel.setLabelFor(makefileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(makefileLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_MAKEFILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(makefileLabel, gridBagConstraints);

        makefileTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(makefileTextField, gridBagConstraints);

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, bundle.getString("LBL_createMainfile")); // NOI18N
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        createMainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMainCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 0);
        add(createMainCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(createMainTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(createMainComboBox, gridBagConstraints);

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, bundle.getString("LBL_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(setAsMainCheckBox, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSN_PanelProjectLocationVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSD_PanelProjectLocationVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_SelectProjectLocation")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocationTextField.getText();
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(projectDir.getAbsolutePath());
        }
        panel.fireChangeEvent();
    }//GEN-LAST:event_browseLocationAction

    private void createMainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMainCheckBoxActionPerformed
        // TODO add your handling code here:
        createMainTextField.setEnabled(createMainCheckBox.isSelected());
        createMainComboBox.setEnabled(createMainCheckBox.isSelected());
}//GEN-LAST:event_createMainCheckBoxActionPerformed

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    private boolean isValidProjectName(String text) {
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0 || text.startsWith(" ") || // NOI18N
                text.contains("\\") || // NOI18N
                text.contains("/") || // NOI18N
                text.contains(":") || // NOI18N
                text.contains("*") || // NOI18N
                text.contains("?") || // NOI18N
                text.contains("\"") || // NOI18N
                text.contains("<") || // NOI18N
                text.contains(">") || // NOI18N
                text.contains("|")) {  // NOI18N
            return false;
        }
        // check ability to create file with specified name on target OS
        boolean ok = false;
        try {
            File file = File.createTempFile(text + "dummy", "");// NOI18N
            ok = true;
            file.delete();
        } catch (Exception ex) {
            // failed to create
        }
        return ok;
    }

    boolean valid(WizardDescriptor wizardDescriptor) {

        if (!isValidProjectName(projectNameTextField.getText())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName")); // NOI18N
            return false; // Display name not specified
        }
        File f = new File(projectLocationTextField.getText()).getAbsoluteFile();
        if (getCanonicalFile(f) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
            return false;
        }
        final File destFolder = getCanonicalFile(new File(createdFolderTextField.getText()).getAbsoluteFile());
        if (destFolder == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName"); // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
            return false;
        }
        if (makefileTextField.getText().indexOf(" ") >= 0) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_SpacesInMakefile")); // NOI18N
            return false;
        }
        if (makefileTextField.getText().indexOf("/") >= 0 || makefileTextField.getText().indexOf("\\") >= 0) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMakefileName")); // NOI18N
            return false;
        }

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly")); // NOI18N
            return false;
        }
        File[] kids = destFolder.listFiles();
        if (destFolder.exists()) {
            if (destFolder.isFile()) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_NotAFolder", makefileTextField.getText()));  // NOI18N
                return false;
            }
            if (new File(destFolder.getPath() + File.separator + makefileTextField.getText()).exists()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_MakefileExists", makefileTextField.getText()));  // NOI18N
                return false;
            }
            if (new File(destFolder.getPath() + File.separator + "nbproject").exists() || // NOI18N
                    new File(destFolder.getPath() + File.separator + MakeConfiguration.BUILD_FOLDER).exists() ||
                    new File(destFolder.getPath() + File.separator + MakeConfiguration.DIST_FOLDER).exists()) {
                // Folder exists and is not empty
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists")); // NOI18N
                return false;
            }
        }

        /*
        if (destFolder.getPath().indexOf(' ') >= 0) {
        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
        NbBundle.getMessage(PanelProjectLocationVisual.class,"MSG_NoSpaces"));
        return false;
        }
         **/

        return true;
    }

    void store(WizardDescriptor d) {

        String projectName = projectNameTextField.getText().trim();
        String location = projectLocationTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        d.putProperty( /*XXX Define somewhere */"projdir", new File(folder)); // NOI18N
        d.putProperty( /*XXX Define somewhere */"name", projectName); // NOI18N
        d.putProperty( /*XXX Define somewhere */"makefilename", makefileTextField.getText()); // NOI18N
        File projectsDir = new File(this.projectLocationTextField.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder(projectsDir);
        }

        d.putProperty( /*XXX Define somewhere */"setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        d.putProperty( /*XXX Define somewhere */"mainClass", null); // NOI18N

        MIMEExtensions cExtensions = MIMEExtensions.get("text/x-c"); // NOI18N
        MIMEExtensions ccExtensions = MIMEExtensions.get("text/x-c++"); // NOI18N

        d.putProperty("createMainFile", createMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE); // NOI18N
        if (createMainCheckBox.isSelected() && createMainTextField.getText().length() > 0) {
            if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
                if (((String) createMainComboBox.getSelectedItem()).equals("C")) { // NOI18N
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + cExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cFiles/main.c"); // NOI18N
                } else {
                    d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                    d.putProperty("mainFileTemplate", "Templates/cppFiles/main.cc"); // NOI18N
                }
            } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
                d.putProperty("mainFileName", createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                d.putProperty("mainFileTemplate", "Templates/qtFiles/main.cc"); // NOI18N
            }
        }
    }

    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir");  //NOI18N
        if (projectLocation == null) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = (String) settings.getProperty("displayName"); //NOI18N
        if (projectName == null) {
            String workingDir = (String) settings.getProperty("buildCommandWorkingDirTextField"); //NOI18N
            if (workingDir != null && workingDir.length() > 0 && templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)) {
                name = IpeUtils.getBaseName(workingDir);
            }
            int baseCount = 1;
            String formater = name + "_{0}"; // NOI18N
            while ((projectName = validFreeProjectName(projectLocation, formater, baseCount)) == null) {
                baseCount++;
            }
            settings.putProperty(NewMakeProjectWizardIterator.PROP_NAME_INDEX, Integer.valueOf(baseCount));
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JComboBox createMainComboBox;
    private javax.swing.JTextField createMainTextField;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel makefileLabel;
    private javax.swing.JTextField makefileTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JCheckBox setAsMainCheckBox;
    // End of variables declaration//GEN-END:variables

    // Private methods ---------------------------------------------------------
    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setName("Select Project Directory"); // XXX // NOI18N
        return chooser;
    }

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String projectName = MessageFormat.format(formater, new Object[]{Integer.valueOf(index)});
        File file = new File(parentFolder, projectName);
        return file.exists() ? null : projectName;
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    class MakefileDocumentListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }

        public void insertUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }

        public void removeUpdate(DocumentEvent e) {
            makefileNameChanged = true;
        }
    }

    private String contructProjectMakefileName(int count) {
        String makefileName = projectNameTextField.getText() + "-" + MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME; // NOI18N
        if (count > 0) {
            makefileName += "" + count + ".mk"; // NOI18N
        } else {
            makefileName += ".mk"; // NOI18N
        }
        return makefileName;
    }

    /** Handles changes in the Project name and project directory
     */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            String projectName = projectNameTextField.getText().trim();
            String projectFolder = projectLocationTextField.getText().trim();
            while (projectFolder.endsWith("/")) { // NOI18N
                projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
            }
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);

            if (!makefileNameChanged) {
                // re-evaluate name of master project file.
                String makefileName;
                if (!templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)) // NOI18N
                {
                    makefileName = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
                } else {
                    makefileName = contructProjectMakefileName(0);
                }

                for (int count = 0;;) {
                    String proposedMakefile = createdFolderTextField.getText() + File.separatorChar + makefileName;
                    if (!new File(proposedMakefile).exists() && !new File(proposedMakefile.toLowerCase()).exists() && !new File(proposedMakefile.toUpperCase()).exists()) {
                        break;
                    }
                    makefileName = contructProjectMakefileName(count++);
                }
                makefileTextField.setText(makefileName);
                makefileNameChanged = false;
            }
        }
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PanelProjectLocationVisual.class);
        }
        return bundle.getString(s);
    }
}
