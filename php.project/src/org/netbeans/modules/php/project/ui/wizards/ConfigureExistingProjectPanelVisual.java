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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.ProjectNameProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.Utils.EncodingModel;
import org.netbeans.modules.php.project.ui.Utils.EncodingRenderer;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

class ConfigureExistingProjectPanelVisual extends JPanel implements ConfigurableProjectPanel, ProjectNameProvider, DocumentListener, ChangeListener, ActionListener {

    private static final long serialVersionUID = 976589754123313213L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ProjectFolder projectFolderComponent;

    ConfigureExistingProjectPanelVisual(ConfigureProjectPanel wizardPanel) {
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "LBL_ProjectNameLocation"));
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0); // NOI18N
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps()); // NOI18N

        initComponents();
        projectFolderComponent = new ProjectFolder(this);
        projectFolderPanel.add(BorderLayout.NORTH, projectFolderComponent);
        init();
    }

    private void init() {
        sourcesTextField.getDocument().addDocumentListener(this);
        projectNameTextField.getDocument().addDocumentListener(this);

        encodingComboBox.setModel(new EncodingModel());
        encodingComboBox.setRenderer(new EncodingRenderer());

        projectFolderComponent.addProjectFolderListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    public void addConfigureProjectListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeConfigureProjectListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourcesLabel = new JLabel();
        sourcesTextField = new JTextField();
        sourcesBrowseButton = new JButton();
        sourcesInfoLabel = new JLabel();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        encodingLabel = new JLabel();
        encodingComboBox = new JComboBox();
        separator = new JSeparator();
        projectFolderPanel = new JPanel();

        Mnemonics.setLocalizedText(sourcesLabel, NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "LBL_Sources")); // NOI18N
        sourcesLabel.setVerticalAlignment(SwingConstants.TOP);
        Mnemonics.setLocalizedText(sourcesBrowseButton, NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "LBL_LocalServerBrowse"));
        sourcesBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sourcesBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(sourcesInfoLabel, NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "TXT_ExistingSourcesHint"));
        sourcesInfoLabel.setEnabled(false);

        projectNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "LBL_ProjectName"));
        projectNameLabel.setVerticalAlignment(SwingConstants.TOP);

        encodingLabel.setLabelFor(encodingComboBox);

        Mnemonics.setLocalizedText(encodingLabel, NbBundle.getMessage(ConfigureExistingProjectPanelVisual.class, "LBL_Encoding"));
        projectFolderPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(separator, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
            .add(projectFolderPanel, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(encodingLabel)
                    .add(projectNameLabel)
                    .add(sourcesLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(sourcesInfoLabel)
                    .add(layout.createSequentialGroup()
                        .add(sourcesTextField, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(sourcesBrowseButton))
                    .add(GroupLayout.TRAILING, projectNameTextField, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                    .add(encodingComboBox, 0, 309, Short.MAX_VALUE)))
        
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(sourcesLabel, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                    .add(sourcesBrowseButton)
                    .add(sourcesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(sourcesInfoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(encodingLabel)
                    .add(encodingComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(projectFolderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sourcesBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sourcesBrowseButtonActionPerformed
        String newLocation = Utils.browseLocationAction(this, sourcesTextField.getText(), NbBundle.getMessage(ProjectFolder.class, "LBL_SelectProjectFolder"));
        if (newLocation != null) {
            sourcesTextField.setText(newLocation);
        }
    }//GEN-LAST:event_sourcesBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox encodingComboBox;
    private JLabel encodingLabel;
    private JPanel projectFolderPanel;
    private JLabel projectNameLabel;
    protected JTextField projectNameTextField;
    private JSeparator separator;
    private JButton sourcesBrowseButton;
    private JLabel sourcesInfoLabel;
    private JLabel sourcesLabel;
    private JTextField sourcesTextField;
    // End of variables declaration//GEN-END:variables

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public void setProjectName(String projectName) {
        projectNameTextField.setText(projectName);
        projectNameTextField.selectAll();
    }

    public String getProjectFolder() {
        return projectFolderComponent.getProjectFolder();
    }

    public void setProjectFolder(String projectFolder) {
        projectFolderComponent.setProjectFolder(projectFolder);
    }

    public boolean isProjectFolderUsed() {
        return projectFolderComponent.isProjectFolderUsed();
    }

    public void setProjectFolderUsed(boolean used) {
        projectFolderComponent.setProjectFolderUsed(used);
    }

    // because of compatibility with ConfigureNewProjectPanelVisual
    public LocalServer getSourcesLocation() {
        return new LocalServer(sourcesTextField.getText());
    }

    public MutableComboBoxModel getLocalServerModel() {
        return null;
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
    }

    public void selectSourcesLocation(LocalServer localServer) {
    }

    public Charset getEncoding() {
        return (Charset) encodingComboBox.getSelectedItem();
    }

    public void setEncoding(Charset encoding) {
        encodingComboBox.setSelectedItem(encoding);
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
        changeSupport.fireChange();
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public void actionPerformed(ActionEvent e) {
        changeSupport.fireChange();
    }
}
