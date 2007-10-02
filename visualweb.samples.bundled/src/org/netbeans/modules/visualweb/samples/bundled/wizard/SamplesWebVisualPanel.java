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
package org.netbeans.modules.visualweb.samples.bundled.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


public final class SamplesWebVisualPanel extends JPanel implements DocumentListener {
    private SamplesWebWizardPanel panel;

    /**
     * Creates new form SamplesWebVisualPanel
     */
    public SamplesWebVisualPanel(SamplesWebWizardPanel panel) {
        initComponents();
        this.panel = panel;
        // Register listener on the textFields to make the automatic updates
        this.projectNameField.getDocument().addDocumentListener(this);
        this.projectLocationField.getDocument().addDocumentListener(this);
    }
    
    public String getName() {
        return "Name and Location";
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
    
    private void update(DocumentEvent de) {
        if ( this.projectNameField.getDocument() == de.getDocument() ) {
            firePropertyChange( WizardProperties.NAME, null, projectNameField.getText() );
        }
        if ( this.projectLocationField.getDocument() == de.getDocument() ) {
            firePropertyChange( WizardProperties.PROJ_DIR, null, projectLocationField.getText() );
        }
        Document doc = de.getDocument();
        if ( doc == projectNameField.getDocument() || doc == projectLocationField.getDocument() ) {
            // Change in the project name
            String projectName = projectNameField.getText();
            String projectFolder = projectLocationField.getText();
            projectFolderField.setText(projectFolder + File.separatorChar + projectName);
        }
        this.panel.fireChangeEvent();
    }
    
    void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty( WizardProperties.PROJ_DIR );
        if (projectLocation == null || projectLocation.getParentFile() == null || ! projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationField.setText(projectLocation.getAbsolutePath());
        String projectName = (String) settings.getProperty(WizardProperties.NAME);
        if (projectName == null) {
            projectName = "Sample"; //NOI18N
        }
        this.projectNameField.setText(projectName);
        this.projectNameField.selectAll();
        Boolean isSetMainProject = (Boolean) settings.getProperty( WizardProperties.SET_MAIN_PROJ );
        this.setAsMainProject.setSelected(isSetMainProject.booleanValue());
    }
    
    void store(WizardDescriptor settings) {
        String name = projectNameField.getText().trim();
        settings.putProperty( WizardProperties.NAME, name );
        String folder = projectFolderField.getText().trim();
        settings.putProperty( WizardProperties.PROJ_DIR, new File(folder) );
        boolean isSetMainProject = setAsMainProject.isSelected();
        settings.putProperty( WizardProperties.SET_MAIN_PROJ, Boolean.valueOf(isSetMainProject) );
    }
    
    public String getProjectName() {
        return this.projectNameField.getText();
    }
    
    public String getProjectFolder() {
        return this.projectFolderField.getText();
    }
    
    public String getProjectLocation() {
        return this.projectLocationField.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        projectNameLabel = new javax.swing.JLabel();
        projectNameField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationField = new javax.swing.JTextField();
        projectFolderLabel = new javax.swing.JLabel();
        projectFolderField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        setAsMainProject = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/samples/bundled/Bundle").getString("LBL_ProjectName"));

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/samples/bundled/Bundle").getString("LBL_ProjectLocation"));

        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/samples/bundled/Bundle").getString("LBL_ProjectFolder"));

        projectFolderField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/samples/bundled/Bundle").getString("LBL_Browse"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        setAsMainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainProject, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/samples/bundled/Bundle").getString("LBL_ProjectLocationBrowse"));
        setAsMainProject.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setAsMainProject.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectNameLabel)
                            .add(projectLocationLabel)
                            .add(projectFolderLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .add(projectFolderField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(projectLocationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(setAsMainProject))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(browseButton)
                    .add(projectLocationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectFolderLabel)
                    .add(projectFolderField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setAsMainProject)
                .addContainerGap(176, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(SamplesWebVisualPanel.class, "LBL_TITLE"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.projectLocationField.getText();
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            this.projectLocationField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
        panel.fireChangeEvent();
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField projectFolderField;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectLocationField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectNameField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JCheckBox setAsMainProject;
    // End of variables declaration//GEN-END:variables
    
}

