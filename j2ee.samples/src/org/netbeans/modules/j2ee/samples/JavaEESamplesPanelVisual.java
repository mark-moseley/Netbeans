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

package org.netbeans.modules.j2ee.samples;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class JavaEESamplesPanelVisual extends JPanel implements DocumentListener {
    private boolean withDB = false;
    
    private JavaEESamplesWizardPanel panel;
    
    /** Creates new form PanelProjectLocationVisual */
    public JavaEESamplesPanelVisual(JavaEESamplesWizardPanel panel, boolean withDB) {
        initComponents();
        this.panel = panel;
        this.withDB = withDB;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        txtDBName.getDocument().addDocumentListener(this);
        
        if (!withDB){
            txtDBName.setVisible(false);
            lblDBName.setVisible(false);
            txtDBLocation.setVisible(false);
            lblDBLocation.setVisible(false);
            infoDBLocation.setVisible(false);
        }
    }
    
    
    public String getProjectName() {
        return this.projectNameTextField.getText();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        lblDBName = new javax.swing.JLabel();
        txtDBName = new javax.swing.JTextField();
        lblDBLocation = new javax.swing.JLabel();
        txtDBLocation = new javax.swing.JTextField();
        infoDBLocation = new javax.swing.JTextArea();

        projectNameLabel.setLabelFor(projectNameTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/samples/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, bundle.getString("LBL_ProjectName")); // NOI18N

        projectNameTextField.setEditable(false);
        projectNameTextField.setEnabled(false);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, bundle.getString("LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, bundle.getString("LBL_Browse")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, bundle.getString("LBL_ProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);
        createdFolderTextField.setEnabled(false);

        lblDBName.setLabelFor(txtDBName);
        org.openide.awt.Mnemonics.setLocalizedText(lblDBName, org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_DBName")); // NOI18N

        lblDBLocation.setLabelFor(txtDBLocation);
        org.openide.awt.Mnemonics.setLocalizedText(lblDBLocation, org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_DBLocation")); // NOI18N

        txtDBLocation.setEditable(false);
        txtDBLocation.setEnabled(false);

        infoDBLocation.setColumns(20);
        infoDBLocation.setEditable(false);
        infoDBLocation.setLineWrap(true);
        infoDBLocation.setRows(5);
        infoDBLocation.setText(org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "CreateDatabasePanelVisual.infoTextArea.text")); // NOI18N
        infoDBLocation.setWrapStyleWord(true);
        infoDBLocation.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, infoDBLocation)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectNameLabel)
                            .add(projectLocationLabel)
                            .add(createdFolderLabel)
                            .add(lblDBName)
                            .add(lblDBLocation))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtDBName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .add(txtDBLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                            .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderLabel)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblDBName)
                    .add(txtDBName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblDBLocation)
                    .add(txtDBLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(24, 24, 24)
                .add(infoDBLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_ProjectLocation_A11YDesc")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_Browse_A11YDesc")); // NOI18N
        txtDBName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_DBName_A11YDesc")); // NOI18N
        txtDBLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_DBLocation_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) { //NOI18N
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(NbBundle.getMessage(JavaEESamplesPanelVisual.class, "LBL_TITLE"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            panel.fireChangeEvent();
        }
        
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JTextArea infoDBLocation;
    private javax.swing.JLabel lblDBLocation;
    private javax.swing.JLabel lblDBName;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JTextField txtDBLocation;
    private javax.swing.JTextField txtDBName;
    // End of variables declaration//GEN-END:variables
    
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        
        if (!isJavaEECapableServerRegistered()){
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG,
                    NbBundle.getMessage(JavaEESamplesPanelVisual.class, "ERR_MissingJavaEE5AppServer"));
            
            return false;
        }
        
        if (projectNameTextField.getText().length() == 0) {
            
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG,
                    NbBundle.getMessage(JavaEESamplesPanelVisual.class, "MSG_InvalidProjectName"));
            
            return false; // Display name not specified
        }
        String projectLocation = projectLocationTextField.getText();
        File f = FileUtil.normalizeFile(new File(projectLocation).getAbsoluteFile());
        if (!f.isDirectory() || projectLocation.length() == 0) {
            String message = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "MSG_InvalidPath");
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG, message);
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());
        
        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG,
                    NbBundle.getMessage(JavaEESamplesPanelVisual.class, "MSG_FolderCannotBeCreated"));
            
            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null) {
            String message = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "MSG_InvalidPath");
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG, message);
            return false;
        }
        
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG,
                    NbBundle.getMessage(JavaEESamplesPanelVisual.class, "MSG_FolderAlreadyExists"));
            
            return false;
        }
        
        wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG, null);
        
        return withDB ? validDBData(wizardDescriptor) : true;
    }
    
    boolean validDBData(WizardDescriptor wizardDescriptor){
        String dbName = txtDBName.getText();
        String errorMsg = null;
        
        if (DerbyDatabases.isDerbyRegistered()){
            int illegalChar = DerbyDatabases.getFirstIllegalCharacter(dbName);
            
            if (illegalChar > -1){
                errorMsg = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "ERR_DatabaseNameIllegalChar", (char)illegalChar);
            } else if (DerbyDatabases.databaseExists(dbName)){
                errorMsg = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "ERR_DatabaseDirectoryExists", dbName);
            } else if (dbName.length() == 0){
                errorMsg = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "ERR_DatabaseNameEmpty");
            }
        } else{
            errorMsg = NbBundle.getMessage(JavaEESamplesPanelVisual.class, "ERR_JavaDBNotRegistered");
        }
        wizardDescriptor.putProperty(WizardProperties.WIZARD_ERROR_MSG, errorMsg);
        return errorMsg == null;
    }
    
    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        
        d.putProperty(WizardProperties.PROJ_DIR, new File(folder));
        d.putProperty(WizardProperties.NAME, name);
        
        if (withDB){
            String dbName = txtDBName.getText().trim();
            d.putProperty(WizardProperties.DB_NAME, dbName);
        }
    }
    
    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty(WizardProperties.PROJ_DIR);
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String) settings.getProperty(WizardProperties.NAME);
        if(projectName == null) {
            projectName = "sample"; //NOI18N
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        
        if (withDB){
            String dbName = DerbyDatabases.getFirstFreeDatabaseName(projectName);
            txtDBName.setText(dbName);
            txtDBName.selectAll();
            updateDBPath(dbName);
        }
    }
    
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }
    
    // Implementation of DocumentListener --------------------------------------
    
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }
    
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }
    
    /** Handles changes in the Project name and project directory, */
    private void update(DocumentEvent e) {
        
        if (projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(WizardProperties.NAME, null, projectNameTextField.getText());
        }
        
        if (projectLocationTextField.getDocument() == e.getDocument()){
            firePropertyChange(WizardProperties.PROJ_DIR, null, projectLocationTextField.getText());
        }
        
        Document doc = e.getDocument();
        
        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            //}
            
        }
        else if (doc == txtDBName.getDocument()){
            String dbName = txtDBName.getText();
            firePropertyChange(WizardProperties.DB_NAME, null, dbName);
            updateDBPath(dbName);
        }
        
        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
    private void updateDBPath(String dbName){
        String dbPath = new File(DerbyDatabases.getSystemHome(), dbName).getPath();
        txtDBLocation.setText(dbPath);
    }

    private boolean isJavaEECapableServerRegistered() {
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()){
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            
            if (j2eePlatform.getSupportedSpecVersions().contains(J2eeModule.JAVA_EE_5)){
                return true;
            }
        }
        
        return false;
    }
}
