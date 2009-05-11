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
 * SourceAndIssuesWizardPanelGUI.java
 *
 * Created on Feb 6, 2009, 11:17:49 AM
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiService;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 */
public class SourceAndIssuesWizardPanelGUI extends javax.swing.JPanel {

    private WizardDescriptor settings;

    private SourceAndIssuesWizardPanel panel;

    private List<KenaiService> repositoriesList = null;
    private List<KenaiService> issuesList = null;
    
    // {0} - hg or svn
    // {1} - project name
    // {2} - repository name
    static final String REPO_NAME_PREVIEW_MSG = "https://kenai.com/{0}/{1}~{2}";

    // names used in repository name preview
    private static final String HG_REPO_NAME =  "hg"; // NOI18N
    static final String SVN_REPO_NAME = "svn"; // NOI18N

    // {0} - project name
    // {1} - repository name
    private static final String DEFAULT_REPO_FOLDER = "{0}~{1}"; // NOI18N

    // XXX maybe move to bundle
    private static final String SVN_REPO_ITEM = "Subversion (on Kenai.com)";
    private static final String HG_REPO_ITEM = "Mercurial (on Kenai.com)";
    private static final String EXT_REPO_ITEM = "External";
    private static final String NO_REPO_ITEM = "None";

    // XXX maybe move to bundle
    private static final String BGZ_ISSUES_ITEM = "Bugzilla (on Kenai.com)";
    private static final String JIRA_ISSUES_ITEM = "JIRA (on Kenai.com)";
    private static final String EXT_ISSUES_ITEM = "External";
    private static final String NO_ISSUES_ITEM = "None";

    static final String SVN_DEFAULT_NAME = "subversion";
    private static final String HG_DEFAULT_NAME = "mercurial";

    private static final int PANEL_HEIGHT = 110;

    private boolean localFolderPathEdited = false;

    // will be used for KenaiException messages
    private String criticalMessage = null;

    /** Creates new form SourceAndIssuesWizardPanelGUI */
    public SourceAndIssuesWizardPanelGUI(SourceAndIssuesWizardPanel pnl) {

        panel = pnl;
        initComponents();

        // hack to show the UI still the same way
        spacerPanel.setPreferredSize(localFolderBrowseButton.getPreferredSize());

        DocumentListener firingDocListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            public void changedUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
        };

        DocumentListener updatingDocListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateRepoNamePreview();
                updateRepoPath();
            }
            public void removeUpdate(DocumentEvent e) {
                updateRepoNamePreview();
                updateRepoPath();
            }
            public void changedUpdate(DocumentEvent e) {
                updateRepoNamePreview();
                updateRepoPath();
            }
        };

        repoNameTextField.getDocument().addDocumentListener(updatingDocListener);
        repoNameTextField.getDocument().addDocumentListener(firingDocListener);
        localFolderTextField.getDocument().addDocumentListener(firingDocListener);
        repoUrlTextField.getDocument().addDocumentListener(firingDocListener);
        issuesUrlTextField.getDocument().addDocumentListener(firingDocListener);

        setupServicesListModels();

        // XXX set the defaults
        // XXX here will be some condition ???
        showRepoOnKenaiGUI();
        showIssuesOnKenaiGUI();
        createChatRoom.setVisible(false);
        chatSeparator.setVisible(false);
        if (!System.getProperty("kenai.com.url", "https://kenai.com").endsWith("testkenai.com")) {
            createChatRoom.setSelected(false);
        }
        setPreferredSize(new Dimension(Math.max(700, getPreferredSize().width), 450));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class,
                "SourceAndIssuesWizardPanelGUI.panelName"); // NOI18N
    }

    private void setupServicesListModels() {

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Collection<KenaiService> services = null;
                try {
                    services = Kenai.getDefault().getServices();
                } catch (KenaiException ex) {
                    // OK, no services
                    // XXX or show message that "Cannot connect to Kenai.com server" ???
                }
                boolean someServicesFound = false;
                List<KenaiService> repoList = new ArrayList<KenaiService>();
                final DefaultComboBoxModel repoModel = new DefaultComboBoxModel();
                List<KenaiService> issuesList = new ArrayList<KenaiService>();
                final DefaultComboBoxModel issuesModel = new DefaultComboBoxModel();
                if (services != null) {
                    someServicesFound = true;
                    Iterator<KenaiService> serviceIter = services.iterator();
                    while (serviceIter.hasNext()) {
                        KenaiService service = serviceIter.next();
                        if (service.getType() == Type.SOURCE) {
                            repoList.add(service);
                        }
                    }
                    serviceIter = services.iterator();
                    while (serviceIter.hasNext()) {

                        KenaiService service = serviceIter.next();
                        if (service.getType() == Type.ISSUES) {
                            issuesList.add(service);
                        }
                    }
                } else { // no services available
                    someServicesFound = false;
                    repoModel.addElement(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class,
                            "SourceAndIssuesWizardPanelGUI.noRepoAvailable")); // NOI18N
                    issuesModel.addElement(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class,
                            "SourceAndIssuesWizardPanelGUI.noIssuesAvailable")); // NOI18N
                }

                if (!repoList.isEmpty()) {
                    for (Iterator<KenaiService> iter = repoList.iterator(); iter.hasNext();) {
                        KenaiService service = iter.next();
                        String serviceName = service.getName();
                        if (KenaiService.Names.SUBVERSION.equals(serviceName)) {
                            repoModel.addElement(new KenaiServiceItem(service, SVN_REPO_ITEM));
                        } else if (KenaiService.Names.MERCURIAL.equals(serviceName)) {
                            repoModel.addElement(new KenaiServiceItem(service, HG_REPO_ITEM));
                        } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(serviceName)) {
                            repoModel.addElement(new KenaiServiceItem(service, EXT_REPO_ITEM));
                        }
                    }
                    repoModel.addElement(new KenaiServiceItem(null, NO_REPO_ITEM));
                    setRepositories(repoList);
                }
                
                if (!issuesList.isEmpty()) {
                    for (Iterator<KenaiService> iter = issuesList.iterator(); iter.hasNext();) {
                        KenaiService service = iter.next();
                        String serviceName = service.getName();
                        if (KenaiService.Names.BUGZILLA.equals(serviceName)) {
                            issuesModel.addElement(new KenaiServiceItem(service, BGZ_ISSUES_ITEM));
                        } else if (KenaiService.Names.JIRA.equals(serviceName)) {
                            issuesModel.addElement(new KenaiServiceItem(service, JIRA_ISSUES_ITEM));
                        } else if (KenaiService.Names.EXTERNAL_ISSUES.equals(serviceName)) {
                            issuesModel.addElement(new KenaiServiceItem(service, EXT_ISSUES_ITEM));
                        }
                    }
                    issuesModel.addElement(new KenaiServiceItem(null, NO_ISSUES_ITEM));
                    setIssues(issuesList);
                }

                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        repoComboBox.setModel(repoModel);
                        repoComboBox.setEnabled(true);
                        issuesComboBox.setModel(issuesModel);
                        issuesComboBox.setEnabled(true);
                    }
                });                
            }
        });

    }

    // ----------

    private synchronized void setRepositories(List<KenaiService> list) {
        repositoriesList = list;
    }

    private synchronized List<KenaiService> getRepositories() {
        return repositoriesList;
    }

    private synchronized void setIssues(List<KenaiService> list) {
        issuesList = list;
    }

    private synchronized List<KenaiService> getIssues() {
        return issuesList;
    }

    // ----------

    private void updateRepoNamePreview() {
        String prjName = (String) settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME);
        String repoTypeName;
        if (KenaiService.Names.SUBVERSION.equals(getRepoType())) {
            repoTypeName = SVN_REPO_NAME;
        } else if (KenaiService.Names.MERCURIAL.equals(getRepoType())) {
            repoTypeName = HG_REPO_NAME;
        } else {
            return;
        }
        String message = MessageFormat.format(REPO_NAME_PREVIEW_MSG, repoTypeName, prjName, repoNameTextField.getText());
        repoNamePreviewLabel.setText(message);
    }

    private void updateRepoPath() {
        if (!localFolderPathEdited) {
            String prjName = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME);
            setRepoLocal(Utilities.getDefaultRepoFolder().getPath() + File.separator +
                    MessageFormat.format(DEFAULT_REPO_FOLDER, prjName, getRepoName()));
        }
    }

    // XXX should check whether user did some edit in the field also
    private void setDefaultRepoName() {
        if (KenaiService.Names.SUBVERSION.equals(getRepoType()) &&
                (HG_DEFAULT_NAME.equals(getRepoName()) || "".equals(getRepoName()))) {
            setRepoName(SVN_DEFAULT_NAME);
        } else if (KenaiService.Names.MERCURIAL.equals(getRepoType()) &&
                (SVN_DEFAULT_NAME.equals(getRepoName()) || "".equals(getRepoName()))) {
            setRepoName(HG_DEFAULT_NAME);
        }
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

        sourceCodeLabel = new JLabel();
        repoComboBox = new JComboBox();
        issueTrackingLabel = new JLabel();
        issuesComboBox = new JComboBox();
        otherFeaturesLabel = new JLabel();
        downloadsLabel = new JLabel();
        forumsLabel = new JLabel();
        mListsLabel = new JLabel();
        wikiLabel = new JLabel();
        featuresDescLabel = new JLabel();
        scSeparator = new JSeparator();
        itSeparator = new JSeparator();
        repoNameLabel = new JLabel();
        repoNameTextField = new JTextField();
        localRepoFolderLabel = new JLabel();
        repoNamePreviewLabel = new JLabel();
        localFolderTextField = new JTextField();
        chatSeparator = new JSeparator();
        createChatRoom = new JCheckBox();
        localFolderBrowseButton = new JButton();
        spacerPanel = new JPanel();
        repoUrlLabel = new JLabel();
        repoUrlTextField = new JTextField();
        noneRepoDescLabel = new JLabel();
        issueTrackingUrlLabel = new JLabel();
        issuesUrlTextField = new JTextField();
        noIssueTrackingDescLabel = new JLabel();
        issuesSpacerPanel = new JPanel();
        repoSpacerPanel = new JPanel();

        setLayout(new GridBagLayout());

        sourceCodeLabel.setLabelFor(repoComboBox);
        Mnemonics.setLocalizedText(sourceCodeLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.sourceCodeLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(sourceCodeLabel, gridBagConstraints);

        sourceCodeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.sourceCodeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        repoComboBox.setEnabled(false);
        repoComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                repoComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 4, 4, 0);
        add(repoComboBox, gridBagConstraints);

        repoComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoComboBox.AccessibleContext.accessibleName")); // NOI18N
        repoComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        issueTrackingLabel.setLabelFor(issuesComboBox);
        Mnemonics.setLocalizedText(issueTrackingLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issueTrackingLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        add(issueTrackingLabel, gridBagConstraints);

        issueTrackingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issueTrackingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        issuesComboBox.setEnabled(false);
        issuesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                issuesComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 0);
        add(issuesComboBox, gridBagConstraints);

        issuesComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issuesComboBox.AccessibleContext.accessibleName")); // NOI18N
        issuesComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issuesComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(otherFeaturesLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.otherFeaturesLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(otherFeaturesLabel, gridBagConstraints);

        otherFeaturesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.otherFeaturesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        downloadsLabel.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/dot.png"))); // NOI18N
        Mnemonics.setLocalizedText(downloadsLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.downloadsLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(downloadsLabel, gridBagConstraints);

        forumsLabel.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/dot.png"))); // NOI18N
        Mnemonics.setLocalizedText(forumsLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.forumsLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(forumsLabel, gridBagConstraints);

        mListsLabel.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/dot.png"))); // NOI18N
        Mnemonics.setLocalizedText(mListsLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.mListsLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(mListsLabel, gridBagConstraints);

        wikiLabel.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/dot.png"))); // NOI18N
        Mnemonics.setLocalizedText(wikiLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.wikiLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(wikiLabel, gridBagConstraints);
        Mnemonics.setLocalizedText(featuresDescLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.featuresDescLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(8, 8, 0, 0);
        add(featuresDescLabel, gridBagConstraints);

        featuresDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.featuresDescLabel.AccessibleContext.accessibleDescription")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        add(scSeparator, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        add(itSeparator, gridBagConstraints);

        repoNameLabel.setLabelFor(repoNameTextField);
        Mnemonics.setLocalizedText(repoNameLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNameLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(8, 0, 0, 0);
        add(repoNameLabel, gridBagConstraints);

        repoNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        repoNameTextField.setText(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNameTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(8, 4, 0, 0);
        add(repoNameTextField, gridBagConstraints);

        repoNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNameTextField.AccessibleContext.accessibleName")); // NOI18N
        repoNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        localRepoFolderLabel.setLabelFor(localFolderTextField);
        Mnemonics.setLocalizedText(localRepoFolderLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localRepoFolderLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(localRepoFolderLabel, gridBagConstraints);

        localRepoFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localRepoFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(repoNamePreviewLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNamePreviewLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 8, 16, 0);
        add(repoNamePreviewLabel, gridBagConstraints);

        repoNamePreviewLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNamePreviewLabel.AccessibleContext.accessibleName")); // NOI18N
        repoNamePreviewLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoNamePreviewLabel.AccessibleContext.accessibleDescription")); // NOI18N
        localFolderTextField.setText(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.text")); // NOI18N
        localFolderTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                localFolderTextFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(localFolderTextField, gridBagConstraints);

        localFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        localFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 0, 4, 0);
        add(chatSeparator, gridBagConstraints);

        createChatRoom.setSelected(true);
        Mnemonics.setLocalizedText(createChatRoom, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.createChatRoom.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(createChatRoom, gridBagConstraints);

        createChatRoom.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.createChatRoom.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(localFolderBrowseButton, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderBrowseButton.text"));
        localFolderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                localFolderBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(localFolderBrowseButton, gridBagConstraints);

        localFolderBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.localFolderBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(spacerPanel, gridBagConstraints);

        repoUrlLabel.setLabelFor(repoUrlTextField);
        Mnemonics.setLocalizedText(repoUrlLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoUrlLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(8, 0, 0, 0);
        add(repoUrlLabel, gridBagConstraints);

        repoUrlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoUrlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        repoUrlTextField.setText(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoUrlTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(8, 4, 0, 0);
        add(repoUrlTextField, gridBagConstraints);

        repoUrlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoUrlTextField.AccessibleContext.accessibleName")); // NOI18N
        repoUrlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.repoUrlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(noneRepoDescLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.noneRepoDescLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(noneRepoDescLabel, gridBagConstraints);

        noneRepoDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.noneRepoDescLabel.AccessibleContext.accessibleDescription")); // NOI18N
        issueTrackingUrlLabel.setLabelFor(issuesUrlTextField);
        Mnemonics.setLocalizedText(issueTrackingUrlLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issueTrackingUrlLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(8, 0, 0, 0);
        add(issueTrackingUrlLabel, gridBagConstraints);

        issueTrackingUrlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issueTrackingUrlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        issuesUrlTextField.setText(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issuesUrlTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(8, 4, 0, 0);
        add(issuesUrlTextField, gridBagConstraints);

        issuesUrlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issuesUrlTextField.AccessibleContext.accessibleName")); // NOI18N
        issuesUrlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.issuesUrlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(noIssueTrackingDescLabel, NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.noIssueTrackingDescLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        add(noIssueTrackingDescLabel, gridBagConstraints);

        noIssueTrackingDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.noIssueTrackingDescLabel.AccessibleContext.accessibleDescription")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(issuesSpacerPanel, gridBagConstraints);

        repoSpacerPanel.setMinimumSize(new Dimension(1, 1));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(repoSpacerPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SourceAndIssuesWizardPanelGUI.class, "SourceAndIssuesWizardPanelGUI.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void repoComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_repoComboBoxActionPerformed

        KenaiServiceItem selObject = (KenaiServiceItem) repoComboBox.getSelectedItem();

        if (selObject != null && selObject.getService() != null) {
            if (KenaiService.Names.SUBVERSION.equals(selObject.getService().getName()) ||
                    KenaiService.Names.MERCURIAL.equals(selObject.getService().getName())) {
                showRepoOnKenaiGUI();
                setDefaultRepoName();
                updateRepoNamePreview();
                updateRepoPath();
            } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(selObject.getService().getName())) {
                showExtRepoGUI();
            } 
        } else {
            showNoRepoGUI();
        }

        panel.fireChangeEvent();

}//GEN-LAST:event_repoComboBoxActionPerformed

    private void localFolderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_localFolderBrowseButtonActionPerformed

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
            localFolderPathEdited = true;
        }

        panel.fireChangeEvent();
        
}//GEN-LAST:event_localFolderBrowseButtonActionPerformed

    private void issuesComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_issuesComboBoxActionPerformed

        KenaiServiceItem selObject = (KenaiServiceItem) issuesComboBox.getSelectedItem();

        if (selObject != null && selObject.getService() != null) {
            if (KenaiService.Names.BUGZILLA.equals(selObject.getService().getName()) ||
                    KenaiService.Names.JIRA.equals(selObject.getService().getName())) {
                showIssuesOnKenaiGUI();
            } else if (KenaiService.Names.EXTERNAL_ISSUES.equals(selObject.getService().getName())) {
                showExtIssuesGUI();
            } 
        } else {
            showNoIssuesGUI();
        }

        panel.fireChangeEvent();

    }//GEN-LAST:event_issuesComboBoxActionPerformed

    private void localFolderTextFieldKeyTyped(KeyEvent evt) {//GEN-FIRST:event_localFolderTextFieldKeyTyped
        localFolderPathEdited = true;
    }//GEN-LAST:event_localFolderTextFieldKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JSeparator chatSeparator;
    private JCheckBox createChatRoom;
    private JLabel downloadsLabel;
    private JLabel featuresDescLabel;
    private JLabel forumsLabel;
    private JLabel issueTrackingLabel;
    private JLabel issueTrackingUrlLabel;
    private JComboBox issuesComboBox;
    private JPanel issuesSpacerPanel;
    private JTextField issuesUrlTextField;
    private JSeparator itSeparator;
    private JButton localFolderBrowseButton;
    private JTextField localFolderTextField;
    private JLabel localRepoFolderLabel;
    private JLabel mListsLabel;
    private JLabel noIssueTrackingDescLabel;
    private JLabel noneRepoDescLabel;
    private JLabel otherFeaturesLabel;
    private JComboBox repoComboBox;
    private JLabel repoNameLabel;
    private JLabel repoNamePreviewLabel;
    private JTextField repoNameTextField;
    private JPanel repoSpacerPanel;
    private JLabel repoUrlLabel;
    private JTextField repoUrlTextField;
    private JSeparator scSeparator;
    private JLabel sourceCodeLabel;
    private JPanel spacerPanel;
    private JLabel wikiLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        panel.fireChangeEvent();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        this.putClientProperty(NewKenaiProjectWizardIterator.PROP_EXC_ERR_MSG, null);
    }

    public boolean valid() {

        String message = checkForErrors();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        message = checkForWarnings();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
        }

        message = checkForInfos();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        }

        return true;

    }

    private String checkForErrors() {
        String excErrMsg = (String) this.getClientProperty(NewKenaiProjectWizardIterator.PROP_EXC_ERR_MSG);
        if (excErrMsg != null) {
            return excErrMsg;
        }
        // invalid repo name
        // invalid local repo path
        // invalid ext repo URL
        // invalid ext issues URL
        return null;
    }

    private String checkForWarnings() {
        // No warnings so far
        return null;
    }

    private String checkForInfos() {
        String localRepoPath = getRepoLocal();
        if ((KenaiService.Names.SUBVERSION.equals(getRepoType()) ||
                KenaiService.Names.MERCURIAL.equals(getRepoType())) &&
                ("".equals(localRepoPath) || localRepoPath == null)) {
            return "Local repository folder path is required";
        }
        String extRepoUrl = getRepoUrl();
        if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(getRepoType()) &&
                ("".equals(extRepoUrl) || extRepoUrl == null)) {
            return "External repository URL is required";
        }
        String extIssuesUrl = getIssuesUrl();
        if (KenaiService.Names.EXTERNAL_ISSUES.equals(getIssuesType()) &&
                ("".equals(extIssuesUrl) || extIssuesUrl == null)) {
            return "External issue tracking URL is required";
        }
        return null;
    }

    public void validateWizard() throws WizardValidationException {
        // XXX
    }

    public void read(WizardDescriptor settings) {

        this.settings = settings;
        String scmType = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_SCM_TYPE);
        // XXX
        String repoName = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_SCM_NAME);
        if (repoName == null || "".equals(repoName.trim())) {
            setDefaultRepoName();            
        } else {
            setRepoName(repoName);
        }
        String repoUrl = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_SCM_URL);
        if (repoUrl == null || "".equals(repoUrl.trim())) {
            // external repo url will be empty by default
            setRepoUrl("");
        } else {
            setRepoUrl(repoUrl);
        }
        String repoLocal = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_SCM_LOCAL);
        if (repoLocal == null || "".equals(repoLocal.trim())) {
            String prjName = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME);
            setRepoLocal(Utilities.getDefaultRepoFolder().getPath() + File.separator +
                    MessageFormat.format(DEFAULT_REPO_FOLDER, prjName, getRepoName()));
        } else {
            setRepoLocal(repoLocal);
        }
        String issuesType = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_ISSUES);
        // XXX
        String issuesUrl = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_ISSUES_URL);
        if (issuesUrl == null || "".equals(issuesUrl.trim())) {
            // external issues tracking url will be empty by default
            setRepoUrl("");
        } else {
            setRepoUrl(issuesUrl);
        }

        Boolean createChat = (Boolean) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_CREATE_CHAT);
        if (createChat!=null) {
            createChatRoom.setSelected(createChat);
        }
    }

    public void store(WizardDescriptor settings) {
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_SCM_TYPE, getRepoType());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_SCM_NAME, getRepoName());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_SCM_URL, getRepoUrl());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_SCM_LOCAL, getRepoLocal());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_ISSUES, getIssuesType());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_ISSUES_URL, getIssuesUrl());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_CREATE_CHAT, createChatRoom.isSelected());
    }

    private void setRepoType(String repo) {
        
    }

    private String getRepoType() {
        KenaiServiceItem selItem = (KenaiServiceItem) repoComboBox.getSelectedItem();
        if (selItem != null && selItem.getService() != null) {
            return selItem.getService().getName();
        }
        return NewKenaiProjectWizardIterator.NO_REPO;
    }

    private void setRepoName(String name) {
        repoNameTextField.setText(name);
    }

    private String getRepoName() {
        return repoNameTextField.getText();
    }

    private void setRepoUrl(String url) {
        repoUrlTextField.setText(url);
    }

    private String getRepoUrl() {
        return repoUrlTextField.getText();
    }

    private void setRepoLocal(String localPath) {
        localFolderTextField.setText(localPath);
    }

    private String getRepoLocal() {
        return localFolderTextField.getText();
    }

    private void setIssuesType(String issues) {

    }

    private String getIssuesType() {
        KenaiServiceItem selItem = (KenaiServiceItem) issuesComboBox.getSelectedItem();
        if (selItem != null && selItem.getService() != null) {
            return selItem.getService().getName();
        }
        return NewKenaiProjectWizardIterator.NO_ISSUES;
    }

    private void setIssuesUrl(String issues) {
        issuesUrlTextField.setText(issues);
    }

    private String getIssuesUrl() {
        return issuesUrlTextField.getText();
    }

    // ----------

    private static class KenaiServiceItem {

        private KenaiService kenaiService;
        private String displayName;

        public KenaiServiceItem(KenaiService service, String dName) {
            kenaiService = service;
            displayName = dName;
        }

        public KenaiService getService() {
            return kenaiService;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

    // ----------

    private void showRepoOnKenaiGUI() {
        _setRepoOnKenaiGUIVisible(true);
        hideExtRepoGUI();
        hideNoRepoGUI();
        int h1 = repoNameTextField.getPreferredSize().height + 8;
        int h2 = repoNamePreviewLabel.getPreferredSize().height + 16;
        int h3 = localFolderTextField.getPreferredSize().height;
        int spacerHeight = PANEL_HEIGHT - (h1 + h2 + h3) - 1; // -1 is the "Special constant"
        repoSpacerPanel.setPreferredSize(new Dimension(10, spacerHeight));
        revalidate();
        repaint();
    }

    private void hideRepoOnKenaiGUI() {
        _setRepoOnKenaiGUIVisible(false);
    }

    private void _setRepoOnKenaiGUIVisible(boolean show) {
        repoNameLabel.setVisible(show);
        repoNameTextField.setVisible(show);
        repoNamePreviewLabel.setVisible(show);
        localRepoFolderLabel.setVisible(show);
        localFolderTextField.setVisible(show);
        localFolderBrowseButton.setVisible(show);
    }

    private void showExtRepoGUI() {
        hideRepoOnKenaiGUI();
        _setExtRepoGUIVisible(true);
        hideNoRepoGUI();
        int h1 = repoUrlTextField.getPreferredSize().height + 8;
        int spacerHeight = PANEL_HEIGHT - h1;
        repoSpacerPanel.setPreferredSize(new Dimension(10, spacerHeight));
        revalidate();
        repaint();
    }

    private void hideExtRepoGUI() {
        _setExtRepoGUIVisible(false);
    }

    private void _setExtRepoGUIVisible(boolean show) {
        repoUrlLabel.setVisible(show);
        repoUrlTextField.setVisible(show);
    }

    private void showNoRepoGUI() {
        hideRepoOnKenaiGUI();
        hideExtRepoGUI();
        _setNoRepoGUIVisible(true);
        int h1 = noneRepoDescLabel.getPreferredSize().height;
        int spacerHeight = PANEL_HEIGHT - h1;
        repoSpacerPanel.setPreferredSize(new Dimension(10, spacerHeight));
        revalidate();
        repaint();
    }

    private void hideNoRepoGUI() {
        _setNoRepoGUIVisible(false);
    }

    private void _setNoRepoGUIVisible(boolean show) {
        noneRepoDescLabel.setVisible(show);
    }

    // ----------

    private void showIssuesOnKenaiGUI() {
        hideExtIssuesGUI();
        hideNoIssuesGUI();
        issuesSpacerPanel.setPreferredSize(new Dimension(10, PANEL_HEIGHT));
        revalidate();
        repaint();
    }

    private void hideIssuesOnKenaiGUI() {

    }

    private void _setIssuesOnKenaiGUIVisible(boolean show) {

    }

    private void showExtIssuesGUI() {
        _setExtIssuesGUIVisible(true);
        hideNoIssuesGUI();
        int h1 = issuesUrlTextField.getPreferredSize().height + 8;
        int spacerHeight = PANEL_HEIGHT - h1;
        issuesSpacerPanel.setPreferredSize(new Dimension(10, spacerHeight));
        revalidate();
        repaint();
    }

    private void hideExtIssuesGUI() {
        _setExtIssuesGUIVisible(false);
    }

    private void _setExtIssuesGUIVisible(boolean show) {
        issueTrackingUrlLabel.setVisible(show);
        issuesUrlTextField.setVisible(show);
    }

    private void showNoIssuesGUI() {
        hideExtIssuesGUI();
        _setNoIssuesGUIVisible(true);
        int h1 = noIssueTrackingDescLabel.getPreferredSize().height;
        int spacerHeight = PANEL_HEIGHT - h1;
        issuesSpacerPanel.setPreferredSize(new Dimension(10, spacerHeight));
        revalidate();
        repaint();
    }

    private void hideNoIssuesGUI() {
        _setNoIssuesGUIVisible(false);
    }

    private void _setNoIssuesGUIVisible(boolean show) {
        noIssueTrackingDescLabel.setVisible(show);
    }

}
