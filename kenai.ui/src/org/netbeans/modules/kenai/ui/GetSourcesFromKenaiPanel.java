/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * GetFromKenaiPanel.java
 *
 * Created on Feb 24, 2009, 3:36:03 PM
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.KenaiSearchPanel.KenaiProjectSearchInfo;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 */
public class GetSourcesFromKenaiPanel extends javax.swing.JPanel {

    private ProjectAndFeature prjAndFeature;
    private boolean localFolderPathEdited = false;

    private DefaultComboBoxModel comboModel;

    public GetSourcesFromKenaiPanel(ProjectAndFeature prjFtr) {

        this.prjAndFeature = prjFtr;
        initComponents();

        refreshUsername();

        comboModel = new KenaiRepositoriesComboModel();
        kenaiRepoComboBox.setModel(comboModel);
        kenaiRepoComboBox.setRenderer(new KenaiFeatureCellRenderer());

        updatePanelUI();
        updateRepoPath();

    }

    public GetSourcesFromKenaiPanel() {
        this(null);
    }

    public GetSourcesInfo getSelectedSourcesInfo() {

        StringTokenizer stok = new StringTokenizer(repoFolderTextField.getText(), ","); // NOI18N
        ArrayList<String> repoFolders = new ArrayList<String>();
        while (stok.hasMoreTokens()) {
            repoFolders.add(stok.nextToken().trim());
        }
        String relPaths[] = repoFolders.size() == 0 ? new String[] { "" } : repoFolders.toArray(new String[repoFolders.size()]); // NOI18N
        KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();

        return (featureItem != null) ? new GetSourcesInfo(featureItem.feature,
                localFolderTextField.getText(), relPaths) : null;
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        loggedInLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        kenaiRepoLabel = new JLabel();
        kenaiRepoComboBox = new JComboBox();
        browseKenaiButton = new JButton();
        projectPreviewLabel = new JLabel();
        repoFolderLabel = new JLabel();
        repoFolderTextField = new JTextField();
        browseRepoButton = new JButton();
        localFolderDescLabel = new JLabel();
        localFolderLabel = new JLabel();
        localFolderTextField = new JTextField();
        browseLocalButton = new JButton();
        proxyConfigButton = new JButton();

        setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        setPreferredSize(new Dimension(700, 350));
        setLayout(new GridBagLayout());
        Mnemonics.setLocalizedText(loggedInLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loggedInLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 12, 4);
        add(loggedInLabel, gridBagConstraints);

        loggedInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetFromKenaiPanel.notLoggedIn"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 12, 0);
        add(usernameLabel, gridBagConstraints);

        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(loginButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loginButton.text"));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(4, 0, 12, 0);
        add(loginButton, gridBagConstraints);

        loginButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loginButton.AccessibleContext.accessibleDescription")); // NOI18N
        kenaiRepoLabel.setLabelFor(kenaiRepoComboBox);
        Mnemonics.setLocalizedText(kenaiRepoLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(kenaiRepoLabel, gridBagConstraints);

        kenaiRepoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        kenaiRepoComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                kenaiRepoComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(kenaiRepoComboBox, gridBagConstraints);

        kenaiRepoComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoComboBox.AccessibleContext.accessibleName")); // NOI18N
        kenaiRepoComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(browseKenaiButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseKenaiButton.text"));
        browseKenaiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseKenaiButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseKenaiButton, gridBagConstraints);

        browseKenaiButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseKenaiButton.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(projectPreviewLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.projectPreviewLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 16, 0);
        add(projectPreviewLabel, gridBagConstraints);

        repoFolderLabel.setLabelFor(repoFolderTextField);
        Mnemonics.setLocalizedText(repoFolderLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(repoFolderLabel, gridBagConstraints);

        repoFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        repoFolderTextField.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(repoFolderTextField, gridBagConstraints);

        repoFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        repoFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(browseRepoButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseRepoButton.text"));
        browseRepoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseRepoButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseRepoButton, gridBagConstraints);

        browseRepoButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseRepoButton.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(localFolderDescLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(24, 0, 6, 0);
        add(localFolderDescLabel, gridBagConstraints);

        localFolderDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderDescLabel.AccessibleContext.accessibleDescription")); // NOI18N
        localFolderLabel.setLabelFor(localFolderTextField);
        Mnemonics.setLocalizedText(localFolderLabel, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(localFolderLabel, gridBagConstraints);

        localFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        localFolderTextField.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.text")); // NOI18N
        localFolderTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                localFolderTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(localFolderTextField, gridBagConstraints);

        localFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        localFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(browseLocalButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseLocalButton.text"));
        browseLocalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseLocalButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(browseLocalButton, gridBagConstraints);

        browseLocalButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseLocalButton.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(proxyConfigButton, NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.proxyConfigButton.text"));
        proxyConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxyConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(proxyConfigButton, gridBagConstraints);

        proxyConfigButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.proxyConfigButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = UIUtils.showLogin();
        if (loginSuccess) {
            refreshUsername();
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void proxyConfigButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_proxyConfigButtonActionPerformed
        OptionsDisplayer.getDefault().open("General"); // NOI18N
}//GEN-LAST:event_proxyConfigButtonActionPerformed

    private void browseKenaiButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseKenaiButtonActionPerformed
        
        KenaiSearchPanel browsePanel = new KenaiSearchPanel(KenaiSearchPanel.PanelType.BROWSE, false);
        String title = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                "GetSourcesFromKenaiPanel.BrowseKenaiProjectsTitle");
        DialogDescriptor dialogDesc = new DialogDescriptor(browsePanel, title, true, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);

        if (NotifyDescriptor.OK_OPTION.equals(option)) {
            KenaiProjectSearchInfo selProjectInfo = browsePanel.getSelectedProjectSearchInfo();
            int modelSize = comboModel.getSize();
            boolean inList = false;
            KenaiFeatureListItem inListItem = null;
            for (int i = 0; i < modelSize; i++) {
                inListItem = (KenaiFeatureListItem) comboModel.getElementAt(i);
                if (inListItem.project.getName().equals(selProjectInfo.kenaiProject.getName()) &&
                    inListItem.feature.getName().equals(selProjectInfo.kenaiFeature.getName())) {
                    inList = true;
                    break;
                }
            }
            if (selProjectInfo != null && !inList) {
                KenaiFeatureListItem item = new KenaiFeatureListItem(selProjectInfo.kenaiProject, selProjectInfo.kenaiFeature);
                comboModel.addElement(item);
                comboModel.setSelectedItem(item);
            } else if (inList && inListItem != null) {
                comboModel.setSelectedItem(inListItem);
            }
        }

}//GEN-LAST:event_browseKenaiButtonActionPerformed

    private void browseRepoButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseRepoButtonActionPerformed
        
        PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();

        KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
        String svnFolders[] = null;
        if (featureItem != null) {
            String title = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                    "GetSourcesFromKenaiPanel.SelectRepositoryFolderTitle");
            String repoUrl = featureItem.feature.getLocation();
            try {
                if (passwdAuth != null) {
                    svnFolders = Subversion.selectRepositoryFolders(title, repoUrl,
                        passwdAuth.getUserName(), new String(passwdAuth.getPassword()));
                } else {
                    svnFolders = Subversion.selectRepositoryFolders(title, repoUrl);
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (svnFolders != null) {
            repoFolderTextField.setText(svnFolders[0]);
        }
        
    }//GEN-LAST:event_browseRepoButtonActionPerformed

    private void browseLocalButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_browseLocalButtonActionPerformed

        JFileChooser chooser = new JFileChooser();
        File uFile = new File(localFolderTextField.getText());
        if (uFile.exists()) {
            chooser.setCurrentDirectory(FileUtil.normalizeFile(uFile));
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selFile = chooser.getSelectedFile();
            localFolderTextField.setText(selFile.getAbsolutePath());
        }
        
    }//GEN-LAST:event_browseLocalButtonActionPerformed

    private void kenaiRepoComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_kenaiRepoComboBoxActionPerformed
        updatePanelUI();
        updateRepoPath();
    }//GEN-LAST:event_kenaiRepoComboBoxActionPerformed

    private void localFolderTextFieldKeyTyped(KeyEvent evt) {//GEN-FIRST:event_localFolderTextFieldKeyTyped
        localFolderPathEdited = true;
    }//GEN-LAST:event_localFolderTextFieldKeyTyped

    private class KenaiRepositoriesComboModel extends DefaultComboBoxModel implements PropertyChangeListener {

        public KenaiRepositoriesComboModel() {
            Dashboard.getDefault().addPropertyChangeListener(this);
            addOpenedProjects();
        }

        private void addOpenedProjects() {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    ProjectHandle[] openedProjects = Dashboard.getDefault().getOpenProjects();
                        for (ProjectHandle prjHandle : openedProjects) {
                            KenaiProject kProject = null;
                            if (prjHandle != null) {
                                try {
                                    kProject = Kenai.getDefault().getProject(prjHandle.getId());
                                } catch (KenaiException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            final KenaiProject project = kProject;
                            if (project != null) {
                            try {
                                KenaiFeature features[] = project.getFeatures(Type.SOURCE);
                                for (final KenaiFeature feature : features) {
                                    EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            if (KenaiService.Names.MERCURIAL.equals(feature.getService()) ||
                                                KenaiService.Names.SUBVERSION.equals(feature.getService())) {
                                                KenaiFeatureListItem item = new KenaiFeatureListItem(project, feature);
                                                addElement(item);
                                                if (prjAndFeature != null && 
                                                    prjAndFeature.projectName.equals(project.getName()) &&
                                                    prjAndFeature.feature.equals(feature)) {
                                                    setSelectedItem(item);
                                                }
                                            }
                                        }
                                    });
                                }
                            } catch (KenaiException kenaiException) {
                                Exceptions.printStackTrace(kenaiException);
                            }
                        }
                    }
                }
            });
        }

        // listening for opened projects in dashboard
        public void propertyChange(PropertyChangeEvent evt) {
            if (Dashboard.PROP_OPENED_PROJECTS.equals(evt.getPropertyName())) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        removeAllElements();
                    }
                });
                addOpenedProjects();
            }
        }

    }

    public static class KenaiFeatureListItem {

        KenaiProject project;
        KenaiFeature feature;

        public KenaiFeatureListItem(KenaiProject prj, KenaiFeature ftr) {
            project = prj;
            feature = ftr;
        }

        @Override
        public String toString() {
            return feature.getLocation();
        }

    }

    public static class GetSourcesInfo {

        public KenaiFeature feature;
        public String localFolderPath;
        public String relativePaths[];

        public GetSourcesInfo(KenaiFeature ftr, String lcl, String[] rel) {
            feature = ftr;
            localFolderPath = lcl;
            relativePaths = rel;
        }

    }

    private void updatePanelUI() {
        KenaiFeatureListItem featureItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
        if (featureItem != null) {
            String serviceName = featureItem.feature.getService(); // XXX service or name
            String repositoryText = NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                    "GetSourcesFromKenaiPanel.RepositoryLabel");
            if (KenaiService.Names.SUBVERSION.equals(serviceName)) {
                enableFolderToGetUI(true);
                localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText"));
                projectPreviewLabel.setText("(" + featureItem.project.getDisplayName() +
                        "; Subversion " + repositoryText + ")"); // NOI18N
            } else if (KenaiService.Names.MERCURIAL.equals(serviceName)) {
                enableFolderToGetUI(false);
                localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.localFolderDescLabel.hgText"));
                projectPreviewLabel.setText("(" + featureItem.project.getDisplayName() +
                        "; Mercurial " + repositoryText + ")"); // NOI18N
            } else {
                enableFolderToGetUI(false);
            }
        }
    }

    private void updateRepoPath() {
        KenaiFeatureListItem selItem = (KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem();
        if (!localFolderPathEdited && selItem != null) {
            String urlString = selItem.feature.getLocation();
            String repoName = urlString.substring(urlString.lastIndexOf("/") + 1); // NOI18N
            localFolderTextField.setText(Utilities.getDefaultRepoFolder().getPath() + File.separator + repoName);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseKenaiButton;
    private JButton browseLocalButton;
    private JButton browseRepoButton;
    private JComboBox kenaiRepoComboBox;
    private JLabel kenaiRepoLabel;
    private JLabel localFolderDescLabel;
    private JLabel localFolderLabel;
    private JTextField localFolderTextField;
    private JLabel loggedInLabel;
    private JButton loginButton;
    private JLabel projectPreviewLabel;
    private JButton proxyConfigButton;
    private JLabel repoFolderLabel;
    private JTextField repoFolderTextField;
    private JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables

    private void refreshUsername() {
        PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();
        if (passwdAuth != null) {
            setUsername(passwdAuth.getUserName());
        } else {
            setUsername(null);
        }
    }

    private void setUsername(String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(Color.BLUE);
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                    "GetFromKenaiPanel.notLoggedIn"));
            usernameLabel.setForeground(Color.BLACK);
            usernameLabel.setEnabled(false);
        }
    }

    private synchronized void setComboModel(DefaultComboBoxModel model) {
        comboModel = model;
    }

    private synchronized DefaultComboBoxModel getComboModel() {
        return comboModel;
    }

    private void enableFolderToGetUI(boolean enable) {
        repoFolderLabel.setEnabled(enable);
        repoFolderTextField.setEnabled(enable);
        browseRepoButton.setEnabled(enable);
    }

}
