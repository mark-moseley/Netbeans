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

package org.netbeans.modules.uml.project.ui.wizards;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.uml.project.ProjectUtil;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.netbeans.modules.uml.project.ui.UMLProjectSettings;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectPanel;

/**
 *
 * @author  Craig Conover, craig.conover@sun.com
 */
public class ProjectLocationVisualPanel extends SettingsPanel
    implements DocumentListener
{
    public ProjectLocationVisualPanel(PanelConfigureProject panel, int type)
    {
        initComponents();
        this.panel = panel;
        this.type = type;

        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }
    
    
    public String getProjectName()
    {
        return this.projectNameTextField.getText().trim();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        modelTypeButtonGroup = new javax.swing.ButtonGroup();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, bundle.getString("LBL_NWP1_ProjectName_Label")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName("");
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectLocationVisualPanel.class, "ACSD_NWP1_ProjectName")); // NOI18N

        projectNameTextField.getAccessibleContext().setAccessibleName("");
        projectNameTextField.getAccessibleContext().setAccessibleDescription("");

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, bundle.getString("LBL_NWP1_ProjectLocation_Label")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleName("");
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectLocationVisualPanel.class, "ACSD_NWP1_ProjectLocation")); // NOI18N

        projectLocationTextField.getAccessibleContext().setAccessibleName("");
        projectLocationTextField.getAccessibleContext().setAccessibleDescription("");

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, bundle.getString("LBL_NWP1_BrowseLocation_Button")); // NOI18N
        browseButton.setActionCommand("BROWSE");
        browseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                browseLocationAction(evt);
            }
        });

        browseButton.getAccessibleContext().setAccessibleName("");
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_browseButton")); // NOI18N

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, bundle.getString("LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleName("");
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectLocationVisualPanel.class, "ACSD_NWP1_CreatedProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);
        createdFolderTextField.getAccessibleContext().setAccessibleName("");
        createdFolderTextField.getAccessibleContext().setAccessibleDescription("");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(projectNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(createdFolderLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(projectLocationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLocationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createdFolderLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseLocationAction
    {//GEN-HEADEREND:event_browseLocationAction
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) // NOI18N
        {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            
            chooser.setDialogTitle(
                bundle.getString("LBL_NWP1_SelectProjectLocation")); // NOI18N
            
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            
            if (path.length() > 0)
            {
                File f = new File(path);

                if (f.exists())
                    chooser.setSelectedFile(f);
            }
            
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this))
            {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(projectDir.getAbsolutePath());
            }
            
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction
    
    boolean valid(WizardDescriptor wizardDescriptor)
    {
        String projectName = projectNameTextField.getText();
        String trimmedName = projectName.trim();
        int trimmedLen = trimmedName.length();
        
        // Ported the fix from coco CR# 6458824 to coke.
        // Not allowing leading/trailing spaces in project name and project folder name
        if (trimmedLen == 0 || trimmedLen < projectName.length())
        {
            String errorMsg = (trimmedLen == 0 ?
                bundle.getString("MSG_IllegalProjectName") :
                bundle.getString("MSG_SpacesInProjectName") );
            
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,
                errorMsg);
            
            return false; // Display name is invalid
        }
        
        File f = new File(projectLocationTextField.getText()).getAbsoluteFile();
        
        if (getCanonicalFile(f)==null)
        {
            String message = bundle.getString("MSG_IllegalProjectLocation"); // NOI18N
            
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE, message);
            return false;
        }
        
        // if none of the source groups are checked then the panel is invalid
//        if (ReferencedJavaProjectPanel.mIsImplementationMode
//            // no more Rose Import support
//            //  && PanelRoseImport.roseModelFileExist == false
//            )
//        {
//            return false;
//        }
        
        final File destFolder = getCanonicalFile(new File(
            createdFolderTextField.getText()).getAbsoluteFile());
        
        if (destFolder == null)
        {
            String message = bundle.getString("MSG_IllegalProjectName"); // NOI18N
            
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,message);
            
            return false;
        }
        
        File projLoc = destFolder;
        boolean readonly = true;
        
        while (projLoc != null && !projLoc.exists())
            projLoc = projLoc.getParentFile();
        
        // workaround for File.canWrite() 99009
        if (projLoc != null)
        {
            try
            {
                File temp = File.createTempFile("temp", "", projLoc);
                if (temp.exists())
                {
                    readonly = false;
                    temp.delete();
                }
            }
            catch (IOException e)
            {
                // nothing special to be handled
            }
        }
        
        if (readonly)
        {
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,
                bundle.getString("MSG_ProjectFolderReadOnly")); // NOI18N
            
            return false;
        }
        
        if (FileUtil.toFileObject(projLoc) == null)
        {
            String message = bundle.getString("MSG_IllegalProjectLocation"); // NOI18N
            
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE, message);
            
            return false;
        }
        
        File[] kids = destFolder.listFiles();
        
        if (destFolder.exists() && kids != null && kids.length > 0)
        {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,
                bundle.getString("MSG_ProjectFolderExists")); // NOI18N
            
            return false;
        }
        
        return true;
    }
    
    void store(WizardDescriptor wizDesc)
    {
        wizDesc.putProperty(
            NewUMLProjectWizardIterator.PROP_PROJECT_NAME, 
            projectNameTextField.getText().trim());

        wizDesc.putProperty(
            NewUMLProjectWizardIterator.PROP_PROJECT_DIR, 
            new File(createdFolderTextField.getText().trim()));

        wizDesc.putProperty(
            NewUMLProjectWizardIterator.PROP_WIZARD_TYPE, 
            Integer.valueOf(type));
    }
    
    void read(WizardDescriptor wizDesc)
    {
        File projectLocation = (File)wizDesc.getProperty(
            NewUMLProjectWizardIterator.PROP_PROJECT_DIR);
        
        if (projectLocation == null ||
            projectLocation.getParentFile() == null ||
            !projectLocation.getParentFile().isDirectory())
        {
            projectLocation = ProjectChooser.getProjectsFolder();
        }
        
        else
            projectLocation = projectLocation.getParentFile();
        
        projectLocationTextField.setText(projectLocation.getAbsolutePath());
        
        String projectName = (String)wizDesc.getProperty(
            NewUMLProjectWizardIterator.PROP_PROJECT_NAME);
        
        if (projectName == null)
        {
            String baseName = bundle.getString("TXT_UMLProject"); // NOI18N

            projectName = ProjectUtil.createUniqueProjectName(
                projectLocation, baseName, false);

            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_NAME_INDEX,
                new Integer(UMLProjectSettings.getDefault().getNewProjectCount()));
        }
        
        projectNameTextField.setText(projectName);
        projectNameTextField.selectAll();
        
        firePropertyChange(PanelOptionsVisual.MODE_CHANGED_PROP, null, 
            wizDesc.getProperty(NewUMLProjectWizardIterator.PROP_WIZARD_TYPE));
    }
    
    void validate(WizardDescriptor d) throws WizardValidationException
    {
        // nothing to validate
    }
    
    
    private static JFileChooser createChooser()
    {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        chooser.setName(NbBundle.getMessage(
            ProjectLocationVisualPanel.class,
            "LBL_NWP1_SelectProjectLocation"));  // NOI18N
        
        return chooser;
    }
    

    public void changedUpdate(DocumentEvent event)
    {
        updateTexts(event);
        
        if (this.projectNameTextField.getDocument() == event.getDocument())
        {
            firePropertyChange(NewUMLProjectWizardIterator.PROP_PROJECT_NAME,
                null,this.projectNameTextField.getText());
        }
    }
    
    public void insertUpdate( DocumentEvent event )
    {
        updateTexts(event);
        
        if (this.projectNameTextField.getDocument() == event.getDocument())
        {
            firePropertyChange(NewUMLProjectWizardIterator.PROP_PROJECT_NAME,
                null,this.projectNameTextField.getText());
        }
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        updateTexts(event);
        
        if (this.projectNameTextField.getDocument() == event.getDocument())
        {
            firePropertyChange(NewUMLProjectWizardIterator.PROP_PROJECT_NAME,
                null,this.projectNameTextField.getText());
        }
    }
    
    
    /** Handles changes in the Project name and project directory
     */
    private void updateTexts(DocumentEvent event)
    {
        Document doc = event.getDocument();
        
        if (doc == projectNameTextField.getDocument() ||
            doc == projectLocationTextField.getDocument())
        {
            // Change in the project name
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            
            //if ( projectFolder.trim().length() == 0 ||
            //	projectFolder.equals( oldName )  ) {
            createdFolderTextField.setText(
                projectFolder + File.separatorChar + projectName);
            //}
        }
        
        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
    static File getCanonicalFile(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        
        catch (IOException e)
        {
            return null;
        }
    }


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.ButtonGroup modelTypeButtonGroup;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    private java.util.ResourceBundle bundle = 
        NbBundle.getBundle(ProjectLocationVisualPanel.class);
    
    private PanelConfigureProject panel;
    private int type;
}
