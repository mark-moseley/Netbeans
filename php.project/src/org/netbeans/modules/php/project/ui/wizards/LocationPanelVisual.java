/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;


import java.io.File;
import javax.swing.JPanel;

import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.LocalServerController;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
class LocationPanelVisual extends JPanel implements DocumentListener, ChangeListener {

    private static final long serialVersionUID = 9464147466826L;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final LocalServerController localServerComponent;

    public LocationPanelVisual() {
        initComponents();
        localServerComponent = LocalServerController.create(localServerComboBox, localServerButton,
                NbBundle.getMessage(LocationPanelVisual.class, "LBL_SelectSourceFolderTitle"));
        init();
    }

    private void init() {
        projectLocationTextField.getDocument().addDocumentListener(this);
        projectNameTextField.getDocument().addDocumentListener(this);
        localServerComponent.addChangeListener(this);
        urlTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    void addLocationListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeLocationListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        sourcesLabel = new javax.swing.JLabel();
        localServerComboBox = new javax.swing.JComboBox();
        localServerButton = new javax.swing.JButton();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        urlInfoLabel = new javax.swing.JLabel();

        projectNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_ProjectName")); // NOI18N
        projectNameLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_BrowseProject")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_CreatedProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(sourcesLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_Sources")); // NOI18N
        sourcesLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        localServerComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(localServerButton, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_LocalServerBrowse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "LBL_Url")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlInfoLabel, org.openide.util.NbBundle.getMessage(LocationPanelVisual.class, "TXT_Url")); // NOI18N
        urlInfoLabel.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLocationLabel)
                    .add(projectNameLabel)
                    .add(createdFolderLabel)
                    .add(sourcesLabel)
                    .add(urlLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(localServerComboBox, 0, 269, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(localServerButton))
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                    .add(urlInfoLabel)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {browseButton, localServerButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(browseButton)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderLabel)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourcesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(localServerButton)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(urlInfoLabel))
        );

        projectNameTextField.getAccessibleContext().setAccessibleName("Project Name");
        projectNameTextField.getAccessibleContext().setAccessibleDescription("null");
        projectLocationTextField.getAccessibleContext().setAccessibleName("Project Location");
        projectLocationTextField.getAccessibleContext().setAccessibleDescription("null");
        browseButton.getAccessibleContext().setAccessibleName("Browse Project Location");
        browseButton.getAccessibleContext().setAccessibleDescription("null");
        createdFolderTextField.getAccessibleContext().setAccessibleName("Project Folder");
        createdFolderTextField.getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String newLocation = Utils.browseLocationAction(this, getProjectLocation(),
                NbBundle.getMessage(LocationPanelVisual.class, "LBL_SelectProjectLocation"));
        if (newLocation != null) {
            setProjectLocation(newLocation);
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JButton localServerButton;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    protected javax.swing.JTextField projectNameTextField;
    private javax.swing.JLabel sourcesLabel;
    private javax.swing.JLabel urlInfoLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getFullProjectPath() {
        File projectPath = FileUtil.normalizeFile(new File(getProjectLocation(), getProjectName()));
        return projectPath.getAbsolutePath();
    }

    public String getProjectLocation() {
        return projectLocationTextField.getText().trim();
    }

    public void setProjectName(String projectName) {
        projectNameTextField.setText(projectName);
        projectNameTextField.selectAll();
    }

    public void setProjectLocation(String projectLocation) {
        projectLocationTextField.setText(projectLocation);
    }

    public LocalServer getSourcesLocation() {
        return localServerComponent.getLocalServer();
    }

    public MutableComboBoxModel getLocalServerModel() {
        return localServerComponent.getLocalServerModel();
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
        localServerComponent.setLocalServerModel(localServers);
    }

    public void selectSourcesLocation(LocalServer localServer) {
        localServerComponent.selectLocalServer(localServer);
    }

    public String getUrl() {
        return urlTextField.getText().trim();
    }

    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    // listeners
    public void insertUpdate(DocumentEvent e) {
        processUpdate();
    }

    public void removeUpdate(DocumentEvent e) {
        processUpdate();
    }

    public void changedUpdate(DocumentEvent e) {
        processUpdate();
    }

    private void processUpdate() {
        createdFolderTextField.setText(getFullProjectPath());
        changeSupport.fireChange();
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
