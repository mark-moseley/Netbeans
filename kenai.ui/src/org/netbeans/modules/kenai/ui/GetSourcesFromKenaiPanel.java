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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiProjectFeature;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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

        //setupCombo();
        
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
        return new GetSourcesInfo(((KenaiFeatureListItem) kenaiRepoComboBox.getSelectedItem()).feature,
                localFolderTextField.getText(), relPaths);
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

        loggedInLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loggedInLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 12, 4);
        add(loggedInLabel, gridBagConstraints);

        usernameLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetFromKenaiPanel.notLoggedIn")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 12, 0);
        add(usernameLabel, gridBagConstraints);

        loginButton.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.loginButton.text")); // NOI18N
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

        kenaiRepoLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.kenaiRepoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(kenaiRepoLabel, gridBagConstraints);

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

        browseKenaiButton.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseKenaiButton.text")); // NOI18N
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

        projectPreviewLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.projectPreviewLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 16, 0);
        add(projectPreviewLabel, gridBagConstraints);

        repoFolderLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(repoFolderLabel, gridBagConstraints);

        repoFolderTextField.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.repoFolderTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(repoFolderTextField, gridBagConstraints);

        browseRepoButton.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseRepoButton.text")); // NOI18N
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

        localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(24, 0, 6, 0);
        add(localFolderDescLabel, gridBagConstraints);

        localFolderLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.localFolderLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(localFolderLabel, gridBagConstraints);

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

        browseLocalButton.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.browseLocalButton.text")); // NOI18N
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

        proxyConfigButton.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class, "GetSourcesFromKenaiPanel.proxyConfigButton.text")); // NOI18N
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
            KenaiProject selProject[] = browsePanel.getSelectedProjects();
            if (null != selProject && selProject.length > 0) {
                KenaiProjectFeature features[] = selProject[0].getFeatures(KenaiFeature.SOURCE);
                for (KenaiProjectFeature feature : features) {
                    KenaiFeatureListItem item = new KenaiFeatureListItem(selProject[0], feature);
                    comboModel.addElement(item);
                    comboModel.setSelectedItem(item);
                }
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
            String repoUrl = featureItem.feature.getLocation().toExternalForm();
            try {
                svnFolders = Subversion.selectRepositoryFolders(title, repoUrl,
                        passwdAuth.getUserName(), new String(passwdAuth.getPassword()));
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
            Kenai.getDefault().addPropertyChangeListener(this);
            if (prjAndFeature != null) {
                try {
                    KenaiProject prj = Kenai.getDefault().getProject(prjAndFeature.projectName);
                    KenaiFeatureListItem item = new KenaiFeatureListItem(prj, prjAndFeature.feature);
                    addElement(item);
                    setSelectedItem(item);
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (Utilities.isUserLoggedIn()) {
                addAllMyProjects();
            }
        }

        private void addAllMyProjects() {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Iterator<KenaiProject> myProjectsIter = null;
                    try {
                        myProjectsIter = Kenai.getDefault().getMyProjects().iterator();
                    } catch (KenaiException ex) {
                        // XXX
                        Exceptions.printStackTrace(ex);
                    }
                    if (myProjectsIter != null) {
                        while (myProjectsIter.hasNext() ) {
                            final KenaiProject project = myProjectsIter.next();
                            KenaiProjectFeature features[] = project.getFeatures(KenaiFeature.SOURCE);
                            for (final KenaiProjectFeature feature : features) {
                                EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        addElement(new KenaiFeatureListItem(project, feature));
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }

        // listening for user login
        public void propertyChange(PropertyChangeEvent evt) {
            if (Kenai.PROP_LOGIN.equals(evt.getPropertyName())) {
                PasswordAuthentication oldAuth = (PasswordAuthentication) evt.getOldValue();
                PasswordAuthentication newAuth = (PasswordAuthentication) evt.getNewValue();
                if (newAuth != null && !newAuth.equals(oldAuth)) {
                    addAllMyProjects();
                }
            }
        }

    }


    private class KenaiRepositoriesModel extends DefaultComboBoxModel implements PropertyChangeListener {

        public KenaiRepositoriesModel(final Iterator<KenaiProject> projects) {
            if (prjAndFeature != null) {
                try {
                    KenaiProject prj = Kenai.getDefault().getProject(prjAndFeature.projectName);
                    KenaiFeatureListItem item = new KenaiFeatureListItem(prj, prjAndFeature.feature);
                    addElement(item);
                    setSelectedItem(item);
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if (projects != null) {
                        while (projects.hasNext() ) {
                            KenaiProject project = projects.next();
                            KenaiProjectFeature features[] = project.getFeatures(KenaiFeature.SOURCE);
                            for (KenaiProjectFeature feature : features) {
                                addElement(new KenaiFeatureListItem(project, feature));
                            }
                        }
                    }
                }
            });
        }

        // listening for user login
        public void propertyChange(PropertyChangeEvent evt) {
            
        }

    }

    public static class KenaiFeatureListItem {

        KenaiProject project;
        KenaiProjectFeature feature;

        public KenaiFeatureListItem(KenaiProject prj, KenaiProjectFeature ftr) {
            project = prj;
            feature = ftr;
        }

        @Override
        public String toString() {
            return feature.getLocation().toString();
        }

    }

    public static class GetSourcesInfo {

        public KenaiProjectFeature feature;
        public String localFolderPath;
        public String relativePaths[];

        public GetSourcesInfo(KenaiProjectFeature ftr, String lcl, String[] rel) {
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
            if (Utilities.SVN_REPO.equals(serviceName)) {
                enableFolderToGetUI(true);
                localFolderDescLabel.setText(NbBundle.getMessage(GetSourcesFromKenaiPanel.class,
                        "GetSourcesFromKenaiPanel.localFolderDescLabel.svnText"));
                projectPreviewLabel.setText("(" + featureItem.project.getDisplayName() +
                        "; Subversion " + repositoryText + ")"); // NOI18N
            } else if (Utilities.HG_REPO.equals(serviceName)) {
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
            String urlString = selItem.feature.getLocation().toExternalForm();
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

//    private void setupCombo() {
//        if (Utilities.isUserLoggedIn()) {
//            RequestProcessor.getDefault().post(new Runnable() {
//                public void run() {
//                    Iterator<KenaiProject> myProjectsIter = null;
//                    try {
//                        myProjectsIter = Kenai.getDefault().getMyProjects().iterator();
//                    } catch (KenaiException ex) {
//                        // XXX
//                        Exceptions.printStackTrace(ex);
//                    }
//                    final ComboBoxModel model = (ComboBoxModel) new KenaiRepositoriesModel(myProjectsIter);
//                    //setComboModel((DefaultComboBoxModel) new KenaiRepositoriesModel(myProjectsIter));
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run() {
//                            kenaiRepoComboBox.setModel(model);
//                            updatePanelUI();
//                            updateRepoPath();
//                        }
//                    });
//                }
//            });
//        } else { // user not logged in
//            setComboModel(new DefaultComboBoxModel());
//        }
//
//        KenaiFeatureCellRenderer renderer = new KenaiFeatureCellRenderer();
//        kenaiRepoComboBox.setRenderer(renderer);
//    }

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
