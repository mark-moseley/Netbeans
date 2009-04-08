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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.CardLayout;
import javax.swing.Action;
import javax.swing.table.TableColumn;
import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.image.ComponentColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.swing.outline.Outline;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Element;

/**
 * Represents <em>Libraries</em> panel in Suite customizer.
 *
 * @author Martin Krauskopf
 */
public final class SuiteCustomizerLibraries extends NbPropertyPanel.Suite
        implements ExplorerManager.Provider, ChangeListener {
    private final ExplorerManager manager;
    private ModuleEntry[] platformModules;
    Set<ModuleEntry> extraBinaryModules = new HashSet<ModuleEntry>();
    static boolean TEST = false;
    private LibrariesChildren libChildren;
    private boolean extClustersLoaded;
    private AbstractNode realRoot;
    private AbstractNode waitRoot;

    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerLibraries(final SuiteProperties suiteProps, ProjectCustomizer.Category cat) {
        super(suiteProps, SuiteCustomizerLibraries.class, cat);
        initComponents();
        initAccessibility();
        manager = new ExplorerManager();
        waitRoot = new AbstractNode(new Children.Array() {

            @Override
            protected Collection<Node> initCollection() {
                return Collections.singleton((Node) new WaitNode());
            }
        });
        waitRoot.setName(getMessage("LBL_ModuleListClusters"));
        waitRoot.setDisplayName(getMessage("LBL_ModuleListClustersModules"));
        manager.setRootContext(waitRoot);
        refresh();

        view.setProperties(new Node.Property[] { ENABLED_PROP_TEMPLATE, ORIGIN_PROP_TEMPLATE });
        Outline outline = view.getOutline();
        outline.setRootVisible(false);
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumn col = outline.getColumnModel().getColumn(1);
        col.setMinWidth(25);
        col.setMaxWidth(200);
        col.setPreferredWidth(70);
        col = outline.getColumnModel().getColumn(2);
        col.setMinWidth(25);
        col.setMaxWidth(300);
        col.setPreferredWidth(130);

        suiteProps.getBrandingModel().addChangeListener(this);
        suiteProps.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (SuiteProperties.NB_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {
                    refresh();
                }
            }
        });

        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] nodes = (Node[]) evt.getNewValue();
                    updateButtons(nodes);
                }
            }
        });
        updateButtons(manager.getSelectedNodes());
        javaPlatformCombo.setRenderer(JavaPlatformComponentFactory.javaPlatformListCellRenderer());
    }

    private void addProjectCluster(ClusterInfo ci, boolean showMessages) throws MissingResourceException, IllegalArgumentException {
        Project project = ci.getProject();
        assert project != null;
        SuiteProject thisPrj = getProperties().getProject();

        if (project != null) {
            if (thisPrj.getProjectDirectory().equals(project.getProjectDirectory())) {
                if (showMessages)
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddMyself")));
                return;
            }
            NbModuleProvider nmtp = project.getLookup().lookup(NbModuleProvider.class);
            if (nmtp != null) {
                if (nmtp.getModuleType() == NbModuleProvider.SUITE_COMPONENT) {
                    SuiteProvider sprv = project.getLookup().lookup(SuiteProvider.class);
                    FileObject otherSuiteDir = FileUtil.toFileObject(sprv.getSuiteDirectory());
                    if (showMessages) {
                        NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(NbBundle.getMessage(UIUtil.class, "MSG_AddSuiteInstead", ProjectUtils.getInformation(project).getDisplayName(), Util.getDisplayName(otherSuiteDir)), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(confirmation);
                        if (confirmation.getValue() == NotifyDescriptor.YES_OPTION) {
                            try {
                                project = ProjectManager.getDefault().findProject(otherSuiteDir);
                            // fall through to add suite instead
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                            }
                        } else {
                            return;
                        }
                    }
                } else if (nmtp.getModuleType() == NbModuleProvider.STANDALONE) {
                    File clusterDir = ClusterUtils.getClusterDirectory(project);
                    if (libChildren.findCluster(clusterDir) != null) {
                        if (showMessages)
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UIUtil.class, "MSG_AlreadyOnClusterPath", ProjectUtils.getInformation(project).getDisplayName())));
                        return;
                    }
                    initNodes();
                    libChildren.extraNodes.add(new ClusterNode(ci, Children.LEAF));
                    libChildren.setMergedKeys();
                    return;
                } else if (nmtp.getModuleType() == NbModuleProvider.NETBEANS_ORG) {
                    if (showMessages)
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNBORGModuleOnClusterPath", ProjectUtils.getInformation(project).getDisplayName())));
                    return;
                }
            }
            SuiteProvider sprv = project.getLookup().lookup(SuiteProvider.class);
            if (sprv != null) {
                File clusterDir = sprv.getClusterDirectory();
                if (libChildren.findCluster(clusterDir) != null) {
                    if (showMessages)
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UIUtil.class, "MSG_AlreadyOnClusterPath", ProjectUtils.getInformation(project).getDisplayName())));
                    return;
                }
                initNodes();
                libChildren.extraNodes.add(createSuiteNode(ci));
                libChildren.setMergedKeys();
                return;
            }
            // not a netbeans module
            if (showMessages)
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNonNBModuleOnClusterPath", ProjectUtils.getInformation(project).getDisplayName())));
        }
        return;
    }

    private boolean isExternalCluster(Enabled en) {
        return !en.isLeaf() && en.getProject() == null && !en.isPlatformNode();
    }

    private void updateButtons(Node[] nodes) {
        boolean canRemove = nodes.length > 0;
        boolean canEdit = nodes.length == 1;
        for (Node node : nodes) {
            if (! (node instanceof Enabled)) {
                canRemove = canEdit = false;
                break;
            }
            Enabled en = (Enabled) node;
            canRemove &= en instanceof ClusterNode && ! en.isPlatformNode();
            canEdit &= isExternalCluster(en);
        }
        removeButton.setEnabled(canRemove);
        editButton.setEnabled(canEdit);
    }

    private RequestProcessor.Task refreshTask;

    void refresh() {
        refreshJavaPlatforms();
        refreshPlatforms();
        if (refreshTask == null) {
            refreshTask = RP.create(new Runnable() {
                public void run() {
                    refreshModules();
                    updateDependencyWarnings(true);
                }
            });
        }
        if (TEST) {
            refreshTask.run();
        } else {
            refreshTask.schedule(0);
        }
        updateJavaPlatformEnabled();
    }

    private void addExtCluster(ClusterInfo ci) {
        Children.SortedArray moduleCh = new Children.SortedArray();
        try {
            ModuleList ml = ModuleList.scanCluster(ci.getClusterDir(), null, false, ci);
            moduleCh.setComparator(MODULES_COMPARATOR);
            for (ModuleEntry entry : ml.getAllEntries()) {
                moduleCh.add(new Node[] { new BinaryModuleNode(entry, true) });
            }
            extraBinaryModules.addAll(ml.getAllEntries());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        initNodes();
        libChildren.extraNodes.add(new ClusterNode(ci, moduleCh));
        libChildren.setMergedKeys();
    }

    private ClusterNode createSuiteNode(ClusterInfo ci) {
        assert ci.getProject() != null;
        assert ci.getProject().getLookup().lookup(SuiteProvider.class) != null;

        Children.SortedArray moduleCh = new Children.SortedArray();
        moduleCh.setComparator(MODULES_COMPARATOR);
        Set<NbModuleProject> modules = SuiteUtils.getSubProjects(ci.getProject());
        for (NbModuleProject modPrj : modules) {
            moduleCh.add(new Node[] { new SuiteComponentNode(modPrj, true) });
        }
        return new ClusterNode(ci, moduleCh);
    }

    private final class WaitNode extends AbstractNode {
        public WaitNode() {
            super(Children.LEAF);
            setDisplayName(CustomizerComponentFactory.WAIT_VALUE);
        }
    }

    private void initNodes() {
        if (libChildren == null) {
            libChildren = new LibrariesChildren();
            realRoot = new AbstractNode(libChildren);
            realRoot.setName(getMessage("LBL_ModuleListClusters"));
            realRoot.setDisplayName(getMessage("LBL_ModuleListClustersModules"));
        }
    }

    private void refreshModules() {
        // create platform modules children first
        platformModules = getProperties().getActivePlatform().getSortedModules();
        initNodes();
        Set<String> disabledModuleCNB = new HashSet<String>(Arrays.asList(getProperties().getDisabledModules()));
        Set<String> enabledClusters = new HashSet<String>(Arrays.asList(getProperties().getEnabledClusters()));

        Map<File, ClusterNode> clusterToNode = new HashMap<File, ClusterNode>();
        libChildren.platformNodes.clear();

        boolean newPlaf = ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() >= NbPlatform.HARNESS_VERSION_67;

        for (ModuleEntry platformModule : platformModules) {
            File clusterDirectory = platformModule.getClusterDirectory();
            ClusterNode cluster = clusterToNode.get(clusterDirectory);
            if (cluster == null) {
                Children.SortedArray modules = new Children.SortedArray();
                modules.setComparator(MODULES_COMPARATOR);
                // enablement for pre-6.7 modules, for cluster path it gets resolved in load cluster.path section below
                boolean enabled = newPlaf || SingleModuleProperties.clusterMatch(enabledClusters, clusterDirectory.getName());
                ClusterInfo ci = ClusterInfo.create(clusterDirectory, true, enabled);
                cluster = new ClusterNode(ci, modules);
                clusterToNode.put(clusterDirectory, cluster);
                libChildren.platformNodes.add(cluster);
            }

            AbstractNode module = new BinaryModuleNode(platformModule,
                    ! disabledModuleCNB.contains(platformModule.getCodeNameBase()) && cluster.isEnabled());
            cluster.getChildren().add(new Node[] { module });
        }

        // next, load cluster.path
        // XXX support "universal" "${nbplatform.active.dir}/*" entry in the cluster.path?
        // meaning "all platform clusters", recognized in harness and in UI;
        // it would allow easy switch among platforms with different clusters; probably not needed
        // update also code of SuiteProjectGenerator#createPlatformProperties
        Set<ClusterInfo> clusterPath = getProperties().getClusterPath();
        if (clusterPath.size() > 0) {
            // cluster.path exists, we enable/disabled platform nodes according to
            // cluster.path, not enabled.clusterPath & disabled.clusterPath
            for (ClusterNode node : libChildren.platformNodes) {
                if (!clusterPath.contains(node.getClusterInfo())) // must not call setEnabled(true), disabled modules would be enabled
                {
                    node.setEnabled(false);
                }
            }
            if (!extClustersLoaded) {
                for (ClusterInfo ci : clusterPath) {
                    if (!ci.isPlatformCluster()) {
                        if (ci.getProject() != null) {
                            addProjectCluster(ci, false);
                        } else {
                            addExtCluster(ci);
                        }
                    }
                }
                extClustersLoaded = true;
            }
        }
        libChildren.setMergedKeys();
    }
    
    private void refreshJavaPlatforms() {
        javaPlatformCombo.setModel(JavaPlatformComponentFactory.javaPlatformListModel());
        javaPlatformCombo.setSelectedItem(getProperties().getActiveJavaPlatform());
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new PlatformComponentFactory.NbPlatformListModel(getProperties().getActivePlatform())); // refresh
        platformValue.requestFocus();
    }
    
    @Override
    public void store() {
        // TODO C.P: disable buttons/show button "Upgrade"??? on old harness
        Set<String> enabledClusters = new TreeSet<String>();
        Set<String> disabledModules = new TreeSet<String>();
        List<ClusterInfo> clusterPath = new ArrayList<ClusterInfo>();

        boolean oldPlaf = ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() < NbPlatform.HARNESS_VERSION_67;

        for (ClusterNode e : libChildren.platformNodes) {
            if (e.isEnabled()) {
                if (oldPlaf)
                    enabledClusters.add(e.getName());
                else
                    clusterPath.add(e.getClusterInfo());
                for (Node module : e.getChildren().getNodes()) {
                    Enabled m = (Enabled) module;
                    // don't add modules in disabled cluster to disabledModules
                    if (! m.isEnabled()) {
                        disabledModules.add(m.getName());
                    }
                }
            }
        }

        if (oldPlaf) {
            getProperties().setEnabledClusters(enabledClusters.toArray(new String[enabledClusters.size()]));
        } else {
            for (ClusterNode e : libChildren.extraNodes) {
                clusterPath.add(e.getClusterInfo());

                if (e.isEnabled()) {
                    for (Node module : e.getChildren().getNodes()) {
                        Enabled m = (Enabled) module;
                        // don't add modules in disabled cluster to disabledModules
                        if (! m.isEnabled()) {
                            disabledModules.add(m.getName());
                        }
                    }
                }
            }
            getProperties().setClusterPath(clusterPath);
        }
        getProperties().setDisabledModules(disabledModules.toArray(new String[disabledModules.size()]));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        platformsPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        javaPlatformLabel = new javax.swing.JLabel();
        javaPlatformCombo = new javax.swing.JComboBox();
        javaPlatformButton = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        view = new org.openide.explorer.view.OutlineView();
        viewLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        resolveButtonPanel = new javax.swing.JPanel();
        hidingPanel = new javax.swing.JPanel();
        resolveButton = new javax.swing.JButton();
        addProjectButton = new javax.swing.JButton();
        addClusterButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "jLabel1");

        setLayout(new java.awt.GridBagLayout());

        platformsPanel.setLayout(new java.awt.GridBagLayout());

        platformValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformValueItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_NetBeansPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        platformsPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_ManagePlatform_a")); // NOI18N
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        platformsPanel.add(managePlafsButton, gridBagConstraints);

        javaPlatformLabel.setLabelFor(javaPlatformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_Java_Platform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformLabel, gridBagConstraints);

        javaPlatformCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                javaPlatformComboItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformsPanel.add(javaPlatformCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformButton, NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_Manage_Java_Platforms")); // NOI18N
        javaPlatformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaPlatformButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformsPanel.add(javaPlatformButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(platformsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        view.setBorder(javax.swing.UIManager.getBorder("ScrollPane.border"));
		gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(view, gridBagConstraints);
        
        viewLabel.setLabelFor(view);
        org.openide.awt.Mnemonics.setLocalizedText(viewLabel, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_PlatformModules")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 0);
        add(viewLabel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        resolveButtonPanel.setLayout(new java.awt.CardLayout());

        hidingPanel.setLayout(null);
        resolveButtonPanel.add(hidingPanel, "card3");

        resolveButton.setForeground(java.awt.Color.red);
        org.openide.awt.Mnemonics.setLocalizedText(resolveButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ResolveButton")); // NOI18N
        resolveButton.setToolTipText(org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ResolveButtonTooltip")); // NOI18N
        resolveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveButtonActionPerformed(evt);
            }
        });
        resolveButtonPanel.add(resolveButton, "card2");

        buttonsPanel.add(resolveButtonPanel);

        org.openide.awt.Mnemonics.setLocalizedText(addProjectButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_AddProject")); // NOI18N
        addProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProjectButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(addProjectButton);

        org.openide.awt.Mnemonics.setLocalizedText(addClusterButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_AddCluster")); // NOI18N
        addClusterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClusterButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(addClusterButton);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_RemoveButton")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(removeButton);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_EditButton")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(editButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(buttonsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void javaPlatformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_javaPlatformButtonActionPerformed
        PlatformsCustomizer.showCustomizer((JavaPlatform) javaPlatformCombo.getSelectedItem());
    }//GEN-LAST:event_javaPlatformButtonActionPerformed

    private void javaPlatformComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_javaPlatformComboItemStateChanged
        getProperties().setActiveJavaPlatform((JavaPlatform) javaPlatformCombo.getSelectedItem());
    }//GEN-LAST:event_javaPlatformComboItemStateChanged
    
    private void platformValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformValueItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            manager.setRootContext(waitRoot);
            store();    // restore the same enablement of clusters for new platform
            getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
            updateJavaPlatformEnabled();
        }
    }//GEN-LAST:event_platformValueItemStateChanged
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
    }//GEN-LAST:event_managePlatforms

    private void addProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProjectButtonActionPerformed
        Project project = UIUtil.chooseProject(this);
        if (project != null)
            addProjectCluster(ClusterInfo.create(project, true),true);
    }//GEN-LAST:event_addProjectButtonActionPerformed

    private void addClusterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClusterButtonActionPerformed
        ClusterInfo ci = EditClusterPanel.showAddDialog(getProperties().getProject());
        if (ci != null) {
            if (libChildren.findCluster(ci.getClusterDir()) != null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_ClusterAlreadyOnClusterPath",
                        ci.getClusterDir())));
                return;
            }
            addExtCluster(ci);
        }
    }//GEN-LAST:event_addClusterButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        libChildren.removeClusters(manager.getSelectedNodes());
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        Node[] nodes = manager.getSelectedNodes();
        assert nodes.length == 1;
        ClusterNode node = (ClusterNode) nodes[0];
        assert isExternalCluster(node);
        ClusterInfo ci = EditClusterPanel.showEditDialog(node.getClusterInfo(), getProperties().getProject());
        if (ci != null)
            node.setClusterInfo(ci);
    }//GEN-LAST:event_editButtonActionPerformed

    private void resolveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveButtonActionPerformed
        if (resolveFixInfo == null || ! resolveFixInfo.fixable)
            return;
        for (Node node : libChildren.getNodes()) {
            ClusterNode cn = (ClusterNode) node;
            for (Node modNode : cn.getChildren().getNodes()) {
                String cnb = modNode.getName();
                if (resolveFixInfo.toAdd.contains(cnb)) {
                    Enabled en = (Enabled) modNode;
                    assert ! en.isEnabled();
                    en.setState(EnabledState.FULL_ENABLED, false);  // update cluster states only once
                }
            }
            // standalone module cluster
            if (cn.getProject() != null) {
                NbModuleProvider nbmp = cn.getProject().getLookup().lookup(NbModuleProvider.class);
                if (nbmp != null
                        && nbmp.getModuleType() == NbModuleProvider.STANDALONE
                        && resolveFixInfo.toAdd.contains(nbmp.getCodeNameBase())) {
                    assert ! cn.isEnabled();
                    ((Enabled) cn).setState(EnabledState.FULL_ENABLED, false);  // update cluster states only once
                }
            }
        }
        libChildren.setMergedKeys();    // update cluster states
        resolveButton.setEnabled(false);
        updateDependencyWarnings(false);
    }//GEN-LAST:event_resolveButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClusterButton;
    private javax.swing.JButton addProjectButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel filler;
    private javax.swing.JPanel hidingPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton javaPlatformButton;
    private javax.swing.JComboBox javaPlatformCombo;
    private javax.swing.JLabel javaPlatformLabel;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel platformsPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton resolveButton;
    private javax.swing.JPanel resolveButtonPanel;
    private org.openide.explorer.view.OutlineView view;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables
    
    
    private static final Comparator<Node> MODULES_COMPARATOR = new Comparator<Node>() {
        Collator COLL = Collator.getInstance();
        public int compare(Node n1, Node n2) {
            return COLL.compare(n1.getDisplayName(), n2.getDisplayName());
        }
    };
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public static final Set<String> DISABLED_PLATFORM_MODULES = new HashSet<String>();
    
    static {
        // Probably not needed for most platform apps, and won't even work under JNLP.
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.autoupdate.services"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.autoupdate.ui"); // NOI18N
        // XXX the following would not be shown in regular apps anyway, because they are autoloads,
        // but they *are* shown in JNLP apps because currently even unused autoloads are enabled under JNLP:
        // Just annoying; e.g. shows Runtime tab prominently.
        DISABLED_PLATFORM_MODULES.add("org.openide.execution"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.execution"); // NOI18N
        // Similar - unlikely to really be wanted by typical platform apps, and show some GUI.
        /* XXX #107870: currently org.netbeans.core.actions.LogAction needs OW:
        DISABLED_PLATFORM_MODULES.add("org.openide.io"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.output2"); // NOI18N
         */
        DISABLED_PLATFORM_MODULES.add("org.netbeans.core.multiview"); // NOI18N
        // this one is useful only for writers of apps showing local disk
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.favorites"); // NOI18N
        // And these are deprecated:
        DISABLED_PLATFORM_MODULES.add("org.openide.compat"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.openide.util.enumerations"); // NOI18N
        // See issue #112931
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.core.kit"); // NOI18N
        // #110085: some more unwanted ones...
        DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.templates"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.libs.jsr223"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.openide.options"); // NOI18N
        DISABLED_PLATFORM_MODULES.add("org.netbeans.api.visual"); // NOI18N
        
    }
    
    public void stateChanged(ChangeEvent ev) {
        if (getProperties().getBrandingModel().isBrandingEnabled()) {
            // User is turning on branded mode. Let's take a guess: they want to
            // exclude the usual suspects from the module list. We do not want to set
            // these excludes on a new suite because user might want to use real IDE as the platform
            // (i.e. not be creating an app, but rather be creating some modules for the IDE).
            // Only do this if there are no existing exclusions.
            Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
            for (Node cluster : clusters) {
                if (cluster instanceof Enabled) {
                    Enabled e = (Enabled) cluster;
                    if (!e.isEnabled()) {
                        return;
                    } else {
                        for (Node module : e.getChildren().getNodes()) {
                            if (module instanceof Enabled) {
                                Enabled m = (Enabled) module;
                                if (!m.isEnabled()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            // #64443: prompt first.
            if (!UIUtil.showAcceptCancelDialog(
                    getMessage("SuiteCustomizerLibraries.title.exclude_ide_modules"),
                    getMessage("SuiteCustomizerLibraries.text.exclude_ide_modules"),
                    getMessage("SuiteCustomizerLibraries.button.exclude"),
                    getMessage("SuiteCustomizerLibraries.button.skip"),
                    NotifyDescriptor.QUESTION_MESSAGE)) {
                return;
            }
            // OK, continue.
            for (Node cluster : clusters) {
                if (cluster instanceof Enabled) {
                    Enabled e = (Enabled) cluster;
                    if (e.getName().startsWith("platform")) { // NOI18N
                        for (Node module : e.getChildren().getNodes()) {
                            if (module instanceof Enabled) {
                                Enabled m = (Enabled) module;
                                if (DISABLED_PLATFORM_MODULES.contains(m.getName())) {
                                    m.setEnabled(false);
                                }
                            }
                        }
                    } else {
                        e.setEnabled(false);
                    }
                }
            }
        }
    }
    
    // #70724: internally, cluster nodes have 3 states now
    private enum EnabledState {
        // module / all modules in cluster selected
        FULL_ENABLED,
        // only for libChildren - some but not all modules are enabled
        PART_ENABLED,
        // module / whole cluster disabled
        DISABLED
    }

    private static final SystemAction[] NO_ACTIONS = new SystemAction[0];

    abstract class Enabled extends AbstractNode {

        private EnabledState state;

        Enabled(Children ch, boolean enabled) {
            super(ch);
            setState(enabled ? EnabledState.FULL_ENABLED : EnabledState.DISABLED, false);
            
            Sheet s = Sheet.createDefault();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            ss.put(new EnabledProp(this));
            ss.put(new OriginProp(this));
            setSheet(s);
            setIconBaseWithExtension(
                    isLeaf() ? NbModuleProject.NB_PROJECT_ICON_PATH : SuiteProject.SUITE_ICON_PATH);
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        @Override
        public SystemAction[] getActions() {
            return NO_ACTIONS;
        }

        public void setEnabled(boolean s) {
            setState(s ? EnabledState.FULL_ENABLED : EnabledState.DISABLED, true);
        }
        
        public boolean isEnabled() {
            return state != EnabledState.DISABLED;
        }

        public EnabledState getState() {
            return state;
        }

        public abstract boolean isPlatformNode();

        public abstract String getOrigin();

        protected void setState(EnabledState s, boolean propagate) {
            if (s == state) {
                return;
            }
            state = s;
            Logger logger = Logger.getLogger(EnabledProp.class.getName());
            logger.log(Level.FINE, "Node '" + getName() + "', state="
                    + (s==EnabledState.DISABLED ? "disabled" :
                        (s==EnabledState.FULL_ENABLED ? "full enabled" : "part enabled")));

            if (propagate) {
                //refresh children
                EnabledState newChildState = 
                        (s == EnabledState.PART_ENABLED) ? null : s;
//                XXX for (Node nn : standard.getNodes()) {
                for (Node nn : getChildren().getNodes()) {
                    if (nn instanceof Enabled) {
                        Enabled en = (Enabled) nn;
                        // #70724: checking/unchecking cluster node checks/unchecks all children
                        if (newChildState != null) {
                            en.setState(newChildState, false);
                        }
                        en.firePropertyChange(null, null, null);
                    }
                }
                //refresh parent
                Node n = getParentNode();
                if (n instanceof Enabled) {
                    assert s != EnabledState.PART_ENABLED : "Module node should not be passed ENABLED_PARTIALLY state";
                    Enabled par = (Enabled) n;
                    par.updateClusterState();
                    par.firePropertyChange(null, null, null);
                }
                updateDependencyWarnings(false);
            }
        }

        void updateClusterState() {
            if (getChildren() == Children.LEAF)
                return;
            boolean allEnabled = true;
            boolean allDisabled = true;
            for (Node nn : getChildren().getNodes()) {
                if (nn instanceof Enabled) {
                    Enabled ch = (Enabled) nn;
                    allEnabled &= ch.isEnabled();
                    allDisabled &= !ch.isEnabled();
                    if (!allEnabled && !allDisabled) {
                        break;
                    }
                }
            }
            if (allEnabled) {
                setState(EnabledState.FULL_ENABLED, false);
            } else if (allDisabled) {
                setState(EnabledState.DISABLED, false);
            } else {
                setState(EnabledState.PART_ENABLED, false);
            }
        }

        /**
         * @return the project if any is associated
         */
        public abstract Project getProject();
    }

    final class SuiteComponentNode extends Enabled {
        private Project project;

        public SuiteComponentNode(Project prj, boolean enabled) {
            super(Children.LEAF, enabled);
            this.project = prj;
            NbModuleProvider nbmp = prj.getLookup().lookup(NbModuleProvider.class);
            if (nbmp == null)
                throw new IllegalArgumentException("Project must be NbModuleProject");
            if (nbmp.getModuleType() != NbModuleProvider.SUITE_COMPONENT)
                throw new IllegalArgumentException("Project must be suite component project");
            final String cnb = nbmp.getCodeNameBase();
            setName(cnb);
            setDisplayName(ProjectUtils.getInformation(prj).getDisplayName());
            String desc = prj.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo().getShortDescription();
            setShortDescription(formatEntryDesc(cnb, desc));
        }

        @Override
        public boolean isPlatformNode() {
            return false;
        }

        @Override
        public String getOrigin() {
            return getMessage("LBL_SuiteComponent");
        }

        @Override
        public Project getProject() {
            return project;
        }

    }

    final class ClusterNode extends Enabled {
        private ClusterInfo ci;

        /**
         * Ctor for all types of nodes with clusterPath:
         * platform clusterPath, external clusterPath, suites and standalone modules
         * @param clusterName display name of the cluster
         * @param ch children - module nodes
         */
        public ClusterNode(ClusterInfo ci, Children ch) {
            super(ch, ci.isEnabled());
            this.ci = ci;
            final Project prj = ci.getProject();
            if (prj != null) {
                setName(ProjectUtils.getInformation(prj).getDisplayName());
                NbModuleProvider nbmp = prj.getLookup().lookup(NbModuleProvider.class);
                if (nbmp != null) {
                    // standalone module, format in the same way as other modules
                    String desc = prj.getLookup().lookup(LocalizedBundleInfo.Provider.class)
                            .getLocalizedBundleInfo().getShortDescription();
                    setShortDescription(formatEntryDesc(nbmp.getCodeNameBase(), desc));
                } else {
                    setShortDescription(ci.getClusterDir().getAbsolutePath());
                }
            } else {
                setName(ci.getClusterDir().getName());
                setShortDescription(ci.getClusterDir().getAbsolutePath());
            }
        }

        private ClusterInfo getClusterInfo() {
            return ci;
        }

        private void setClusterInfo(ClusterInfo ci) {
            if (! this.ci.equals(ci)) {
                this.ci = ci;
                refresh();
            }
        }

        @Override
        public void setState(EnabledState es, boolean propagate) {
            super.setState(es, propagate);
            if (ci != null && ci.isEnabled() != isEnabled()) {
                ci = ClusterInfo.createFromCP(ci.getClusterDir(), ci.getProject(),
                        ci.isPlatformCluster(), ci.getSourceRoots(), ci.getJavadocRoots(), isEnabled());
            }
        }

        @Override
        public boolean isPlatformNode() {
            return ci.isPlatformCluster();
        }

        @Override
        public String getOrigin() {
            if (isPlatformNode())
                return getMessage("LBL_PlatformOrigin");
            if (getProject() == null)
                return getMessage("LBL_ExtCluster");
            NbModuleProvider nbmp = getProject().getLookup().lookup(NbModuleProvider.class);
            if (nbmp == null)
                return getMessage("LBL_SuiteProject");
            if (nbmp.getModuleType() == NbModuleProvider.STANDALONE)
                return getMessage("LBL_StandaloneProject");
            assert false : "Shouldn't contain NB.org module or suite component project";
            return null;
        }

        @Override
        public Project getProject() {
            return ci.getProject();
        }

    }

    final class BinaryModuleNode extends Enabled {
        /**
         * Ctor for binary modules
         * @param entry ModuleEntry
         * @param ch children - module nodes
         * @param enabled
         */
        public BinaryModuleNode(ModuleEntry entry, boolean enabled) {
            super(Children.LEAF, enabled);
            String cnb = entry.getCodeNameBase();
            setName(cnb);
            setDisplayName(entry.getLocalizedName());
            String desc = entry.getShortDescription();
            setShortDescription(formatEntryDesc(cnb, desc));
        }

        @Override
        public boolean isPlatformNode() {
            return ((Enabled) getParentNode()).isPlatformNode();
        }

        @Override
        public String getOrigin() {
            if (isPlatformNode())
                return getMessage("LBL_PlatformOrigin");
            return getMessage("LBL_ExtCluster");
        }

        @Override
        public Project getProject() {
            return null;
        }

    }

    private static final EnabledProp ENABLED_PROP_TEMPLATE = new EnabledProp(null);
    private static final class EnabledProp extends PropertySupport.ReadWrite<Boolean> {
        
        private Enabled node;
        private PropertyEditor editor;
        
        public EnabledProp(Enabled node) {
            super("enabled", Boolean.TYPE, getMessage("LBL_ModuleListEnabled"), getMessage("LBL_ModuleListEnabledShortDescription"));
            this.node = node;
        }
        
        public void setValue(Boolean val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            node.setEnabled(val);
        }
        
        public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
            Children ch = node.getChildren();
            Logger logger = Logger.getLogger(EnabledProp.class.getName());
            if (ch == Children.LEAF) {
                logger.log(Level.FINE, "Node '" + node.getName() + "' is LEAF, enabled=" + node.isEnabled());
                return node.isEnabled();
            } else if (node.getState() == EnabledState.PART_ENABLED) {
                logger.log(Level.FINE, "Node '" + node.getName() + "', enabled=null");
                return null;
            } else {
                logger.log(Level.FINE, "Node '" + node.getName() + "', enabled=" + node.isEnabled());
                return node.isEnabled();
            }
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            if (editor == null) {
                editor = super.getPropertyEditor();
            }
            return editor;
        }
        
    }

    private static final OriginProp ORIGIN_PROP_TEMPLATE = new OriginProp(null);
    private static final class OriginProp extends PropertySupport.ReadOnly<String> {
        private Enabled node;

        @Override
        public PropertyEditor getPropertyEditor() {
            return null;
        }

        private OriginProp(Enabled node) {
            super("origin", String.class, getMessage("LBL_Origin"), getMessage("LBL_OriginShortDesc"));
            this.node = node;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return node == null ? null : node.getOrigin();
        }

    }

    private class LibrariesChildren extends Children.Keys<ClusterNode> {
        private SortedSet<ClusterNode> platformNodes = new TreeSet<ClusterNode>(MODULES_COMPARATOR);
        private List<ClusterNode> extraNodes = new ArrayList<ClusterNode>();

        @Override
        protected Node[] createNodes(ClusterNode key) {
            return new Node[] { key };
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setMergedKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<ClusterNode>emptySet());
            super.removeNotify();
        }

        private ClusterNode selfCN;

        /**
         * Searches for cluster with given cluster dir.
         * @param clusterDir
         * @return Node for found cluster or null if not found
         */
        private ClusterNode findCluster(File clusterDir) {
            for (ClusterNode node : extraNodes) {
                if (node.getClusterInfo().getClusterDir().equals(clusterDir))
                    return node;
            }
            for (ClusterNode node : platformNodes) {
                if (node.getClusterInfo().getClusterDir().equals(clusterDir))
                    return node;
            }

            // search self as well
            if (ClusterUtils.getClusterDirectory(getProperties().getProject()).equals(clusterDir)) {
                if (selfCN == null) {
                    selfCN = createSuiteNode(ClusterInfo.create(getProperties().getProject(), true));
                }
                return selfCN;
            }
            return null;
        }

        // TODO C.P rewrite cast to lookup - but there is not enough stuff in prj lookup
        // to do this now, would need custom AuxConfigImpl that translates /2 -> /3 schema
        private void getProjectModules(Set<NbModuleProject> suiteModules) {
            for (ClusterNode node : extraNodes) {
                ClusterInfo ci = node.getClusterInfo();
                final Project prj = ci.getProject();
                if (prj != null) {
                    NbModuleProvider nbmp = prj.getLookup().lookup(NbModuleProvider.class);
                    if (nbmp != null) {
                        // standalone module, add directly
                        suiteModules.add((NbModuleProject) prj);
                    } else {
                        // suite, add components
                        for (Node node2 : node.getChildren().getNodes()) {
                            SuiteComponentNode scn = (SuiteComponentNode) node2;
                            suiteModules.add((NbModuleProject) scn.getProject());
                        }
                    }
                }
            }
        }

        private void removeClusters(Node[] selectedNodes) {
            extraNodes.removeAll(Arrays.asList(selectedNodes));
            setMergedKeys();
            updateDependencyWarnings(true);
        }

        private void setMergedKeys() {
            ArrayList<ClusterNode> allNodes = new ArrayList<ClusterNode>(platformNodes);
            allNodes.addAll(extraNodes);
            for (ClusterNode cluster : allNodes) {
                cluster.updateClusterState();
            }

            setKeys(allNodes);
        }

    }

    private String formatEntryDesc(final String cnb, String desc) throws MissingResourceException {
        String tooltip;
        if (desc != null) {
            if (desc.startsWith("<html>")) {
                // NOI18N
                tooltip = "<html>" + NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_desc", cnb, desc.substring(6));
            } else {
                tooltip = NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_desc", cnb, desc);
            }
        } else {
            tooltip = NbBundle.getMessage(SuiteCustomizerLibraries.class, "SuiteCustomizerLibraries.HINT_module_no_desc", cnb);
        }
        return tooltip;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(CustomizerDisplay.class, key);
    }
    
    private void initAccessibility() {
        managePlafsButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ManagePlafsButton"));
        platformValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PlatformValue"));
        javaPlatformCombo.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformCombo"));
        javaPlatformButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformButton"));
        
        javaPlatformLabel.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_JavaPlatformLbl"));
        platform.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PlatformLbl"));
    }
    
    // #65924: show warnings if some dependencies cannot be satisfied
    
    interface UniverseModule {
        String getCodeNameBase();
        int getReleaseVersion();
        SpecificationVersion getSpecificationVersion();
        String getImplementationVersion();
        Set<String> getProvidedTokens();
        Set<String> getRequiredTokens();
        Set<Dependency> getModuleDependencies();
        File getCluster();
        String getDisplayName();
    }
    
    private static abstract class AbstractUniverseModule implements UniverseModule {
        protected final ManifestManager mm;
        protected AbstractUniverseModule(ManifestManager mm) {
            this.mm = mm;
        }
        public int getReleaseVersion() {
            String s = mm.getReleaseVersion();
            return s != null ? Integer.parseInt(s) : -1;
        }
        public String getImplementationVersion() {
            return mm.getImplementationVersion();
        }
        public Set<String> getProvidedTokens() {
            return new HashSet<String>(Arrays.asList(mm.getProvidedTokens()));
        }
        public Set<String> getRequiredTokens() {
            Set<String> s = new HashSet<String>(Arrays.asList(mm.getRequiredTokens()));
            Iterator<String> it = s.iterator();
            while (it.hasNext()) {
                String tok = it.next();
                if (tok.startsWith("org.openide.modules.ModuleFormat") || tok.startsWith("org.openide.modules.os.")) { // NOI18N
                    it.remove();
                }
            }
            s.addAll(Arrays.asList(mm.getNeededTokens()));
            return s;
        }
        @Override
        public String toString() {
            return getCodeNameBase();
        }
    }
    
    private static final class PlatformModule extends AbstractUniverseModule {
        private final ModuleEntry entry;
        public PlatformModule(ModuleEntry entry) throws IOException {
            super(ManifestManager.getInstanceFromJAR(entry.getJarLocation()));
            this.entry = entry;
        }
        public String getCodeNameBase() {
            return entry.getCodeNameBase();
        }
        public SpecificationVersion getSpecificationVersion() {
            String s = entry.getSpecificationVersion();
            return s != null ? new SpecificationVersion(s) : null;
        }
        public Set<Dependency> getModuleDependencies() {
            return mm.getModuleDependencies();
        }
        public File getCluster() {
            return entry.getClusterDirectory();
        }
        public String getDisplayName() {
            return entry.getLocalizedName();
        }
    }
    
    private static final class SuiteModule extends AbstractUniverseModule {
        private final NbModuleProject project;
        private final Set<Dependency> dependencies;
        public SuiteModule(NbModuleProject project) {
            super(ManifestManager.getInstance(project.getManifest(), false));
            this.project = project;
            dependencies = new HashSet<Dependency>();
            // Cannot use ProjectXMLManager since we need to report also deps on nonexistent modules.
            Element dataE = project.getPrimaryConfigurationData();
            Element depsE = Util.findElement(dataE, "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            assert depsE != null : "Malformed metadata in " + project;
            for (Element dep : Util.findSubElements(depsE)) {
                Element run = Util.findElement(dep, "run-dependency", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (run == null) {
                    continue;
                }
                String text = Util.findText(Util.findElement(dep, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
                Element relverE = Util.findElement(run, "release-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (relverE != null) {
                    text += '/' + Util.findText(relverE);
                }
                Element specverE = Util.findElement(run, "specification-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (specverE != null) {
                    text += " > " + Util.findText(specverE);
                } else {
                    Element implver = Util.findElement(run, "implementation-version", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                    if (implver != null) {
                        // Will special-case '*' as an impl version to mean "match anything".
                        text += " = *"; // NOI18N
                    }
                }
                dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, text));
            }
        }
        public String getCodeNameBase() {
            return project.getCodeNameBase();
        }
        public SpecificationVersion getSpecificationVersion() {
            String s = project.getSpecVersion();
            return s != null ? new SpecificationVersion(s) : null;
        }
        public Set<Dependency> getModuleDependencies() {
            return dependencies;
        }
        public File getCluster() {
            return ClusterUtils.getClusterDirectory(project);
        }
        public String getDisplayName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
    }

    private RequestProcessor.Task updateDependencyWarningsTask;
    private RequestProcessor RP = new RequestProcessor(SuiteCustomizerLibraries.class.getName(), 1);

    private void updateDependencyWarnings(final boolean refreshUniverse) {
        if (TEST || ! extClustersLoaded) {
            return;
        }
        // XXX avoid running unless and until we become visible, perhaps
        if (updateDependencyWarningsTask == null) {
            updateDependencyWarningsTask = RP.create(new Runnable() {
                public void run() {
                    if (refreshUniverse)
                        universe = null;
                    doUpdateDependencyWarnings();
                    // XXX testing NPE
                    libChildren.platformNodes.clear();
                }
            });
        }
        updateDependencyWarningsTask.schedule(0);
    }
    
    static Set<UniverseModule> loadUniverseModules(ModuleEntry[] platformModules, 
            Set<NbModuleProject> suiteModules, Set<ModuleEntry> extraBinaryModules) throws IOException {
        Set<UniverseModule> universeModules = new LinkedHashSet<UniverseModule>();
        for (NbModuleProject p : suiteModules) {
            universeModules.add(new SuiteModule(p));
        }
        for (ModuleEntry e : platformModules) {
            universeModules.add(new PlatformModule(e));
        }
        for (ModuleEntry e : extraBinaryModules) {
            universeModules.add(new PlatformModule(e));
        }
        return universeModules;
    }

    FixInfo findWarning(Set<UniverseModule> universeModules, Set<File> enabledClusters, Set<String> disabledModules) {
        SortedMap<String,UniverseModule> sortedModules = new TreeMap<String,UniverseModule>();
        Set<UniverseModule> excluded = new HashSet<UniverseModule>();
        Map<String,Set<UniverseModule>> providers = new HashMap<String,Set<UniverseModule>>();
        for (UniverseModule m : universeModules) {
            String cnb = m.getCodeNameBase();
            File cluster = m.getCluster();
            assert cluster != null;
            if (! enabledClusters.contains(cluster) || disabledModules.contains(cnb)) {
                excluded.add(m);
            }
            sortedModules.put(cnb, m);
            addProviders(m, providers);
        }

        FixInfo fixInfo = new FixInfo();
        Collection<UniverseModule> scannedModules = sortedModules.values();

        RESTART:
        for (;;) {
            for (Iterator<UniverseModule> it = scannedModules.iterator(); it.hasNext();) {
                UniverseModule m = it.next();
                if (excluded.contains(m)) {
                    continue;
                }
                fixInfo.resetCurrentAdditions();
                boolean warningFound = findWarning(m, sortedModules, providers, excluded, fixInfo);
                if (! warningFound)    // no missing dep for this module
                    continue;
                if (! fixInfo.fixable) {    // unfixable dep, bail out
                    return fixInfo;
                }
                if (!fixInfo.isEmpty()) {
                    assert fixInfo.fixable;
                    // fixable dep, sortedModules, providers and excluded already updated, 
                    // restart with copy of tailMap starting at either successor of m or 1st member of fixInfo.toAdd,
                    // whichever comes sooner in sorted modules
                    String additionCNB = (it.hasNext() ? it.next() : m).getCodeNameBase();
                    scannedModules = sortedModules.tailMap(fixInfo.getRestartPointFor(additionCNB)).values();
                    continue RESTART;
                }
            }
            break;
        }
        return fixInfo;
    }

    private void addProviders(UniverseModule m, Map<String, Set<UniverseModule>> providers) {
        for (String tok : m.getProvidedTokens()) {
            Set<UniverseModule> providersOf = providers.get(tok);
            if (providersOf == null) {
                providersOf = new TreeSet<UniverseModule>(UNIVERSE_MODULE_COMPARATOR);
                providers.put(tok, providersOf);
            }
            providersOf.add(m);
        }
    }

    private static final Comparator<UniverseModule> UNIVERSE_MODULE_COMPARATOR = new Comparator<UniverseModule>() {
        Collator COLL = Collator.getInstance();
        public int compare(UniverseModule m1, UniverseModule m2) {
            return COLL.compare(m1.getDisplayName(), m2.getDisplayName());
        }
    };
    
    private Set<UniverseModule> universe;
    private /* #71791 */ synchronized void doUpdateDependencyWarnings() {
        if (universe == null) {
            try {
                Set<NbModuleProject> suiteModules = 
                        new HashSet<NbModuleProject>(getProperties().getSubModules());
                libChildren.getProjectModules(suiteModules);
                universe = loadUniverseModules(platformModules, suiteModules, extraBinaryModules);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                return; // any warnings would probably be wrong anyway
            }
        }
        
        Set<File> enabledClusters = new TreeSet<File>();
        Set<String> disabledModules = new TreeSet<String>();
        enabledClusters.add(ClusterUtils.getClusterDirectory(getProperties().getProject()));
        
        for (Node cluster : libChildren.getNodes()) {
            if (cluster instanceof ClusterNode) {
                ClusterNode e = (ClusterNode) cluster;
                if (e.isEnabled()) {
                    enabledClusters.add(e.getClusterInfo().getClusterDir());
                    for (Node module : e.getChildren().getNodes()) {
                        if (module instanceof Enabled) {
                            Enabled m = (Enabled) module;
                            if (!m.isEnabled()) {
                                disabledModules.add(m.getName());
                            }
                        }
                    }
                }
            }
        }
        
        final FixInfo fi = findWarning(universe, enabledClusters, disabledModules);
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    CardLayout cl = (CardLayout) resolveButtonPanel.getLayout();
                    if (!fi.isEmpty()) {
                        String key = fi.warning[0];
                        String[] args = new String[fi.warning.length - 1];
                        System.arraycopy(fi.warning, 1, args, 0, args.length);
                        category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerLibraries.class, key, args));
                        resolveFixInfo = fi;
                        resolveButton.setEnabled(fi.fixable);
                        cl.last(resolveButtonPanel);
                    } else {
                        category.setErrorMessage(null);
                        cl.first(resolveButtonPanel);
                    }
                } finally {
                    manager.setRootContext(realRoot);
                }
            }
        });
        
    }

    private FixInfo resolveFixInfo;
    // package private for tests only
    static class FixInfo {

        private static void putFixable(FixInfo fi, String cnb, String[] warning) {
            if (! fi.fixable)
                throw new IllegalStateException("Cannot put fixable error on top of unfixable one.\\nExisting: " + Arrays.toString(fi.warning)
                        + "\\nNew: " + Arrays.toString(warning));
            if (fi.warning == null)
                fi.warning = warning;
            fi.toAdd.add(cnb);
            fi.currentAdditions.add(cnb);
        }

        private static void putUnfixable(FixInfo fi, String[] warning) {
            fi.fixable = false;
            // chain of original warnings ended up in unfixable problem,
            // but we have to show root cause (otherwise it can complain about unsatisifed
            // dep. of excluded module; just disable "Resolve" button; 
            // fi.warning = warning;
        }

        String[] warning;
        boolean fixable = true;
        Set<String> toAdd = new HashSet<String>();
        private SortedSet<String> currentAdditions = new TreeSet<String>();

        private boolean isEmpty() {
            return warning == null;
        }

        public SortedSet<String> getCurrentAdditions() {
            return currentAdditions;
        }

        private void resetCurrentAdditions() {
            currentAdditions.clear();
        }

        public String getRestartPointFor(String additionCNB)  {
            currentAdditions.add(additionCNB);
            return currentAdditions.first();
        }
    }

    /**
     *
     * @param m
     * @param modules
     * @param providers
     * @param excluded
     * @param fi
     * @return Returns <tt>true</tt> if issued warning for <tt>m</tt>, <tt>false</tt> if all deps for <tt>m</tt> are satisfied
     */
    private boolean findWarning(UniverseModule m,
            Map<String,UniverseModule> modules,
            Map<String,Set<UniverseModule>> providers,
            Set<UniverseModule> excluded,
            FixInfo fi) {
        // Check module dependencies:
        SortedSet<Dependency> deps = new TreeSet<Dependency>(new Comparator<Dependency>() {
            public int compare(Dependency d1, Dependency d2) {
                return d1.getName().compareTo(d2.getName());
            }
        });
        String mdn = m.getDisplayName();
        ClusterNode node = libChildren.findCluster(m.getCluster());
        if (node == null) {
            // #162155: dirty hack of race conditions; proper solution would be to copy everything for doUpdateDependencyWarnings,
            // but null here means another refresh is in progress, thus it is safe to return here, last refresh in row will get everything straight.
            return false;
        }
        String mc = node.getDisplayName();
        deps.addAll(m.getModuleDependencies());
        boolean ret = false;
        for (Dependency d : deps) {
            String codename = d.getName();
            String cnb;
            int mrvLo, mrvHi;
            int slash = codename.lastIndexOf('/');
            if (slash == -1) {
                cnb = codename;
                mrvLo = -1;
                mrvHi = -1;
            } else {
                cnb = codename.substring(0, slash);
                String mrv = codename.substring(slash + 1);
                int dash = mrv.lastIndexOf('-');
                if (dash == -1) {
                    mrvLo = mrvHi = Integer.parseInt(mrv);
                } else {
                    mrvLo = Integer.parseInt(mrv.substring(0, dash));
                    mrvHi = Integer.parseInt(mrv.substring(dash + 1));
                }
            }
            UniverseModule dep = modules.get(cnb);
            if (dep == null) {
                FixInfo.putUnfixable(fi, new String[] {"ERR_no_dep", mdn, mc, cnb});
                return true;
            }

            String ddn = dep.getDisplayName();
            if (excluded.contains(dep)) {
                node = libChildren.findCluster(dep.getCluster());
                if (node == null) // #162155
                    return false;
                String dc = node.getDisplayName();
                // currently the only fixable error
                FixInfo.putFixable(fi, cnb, new String[] {"ERR_excluded_dep", mdn, mc, ddn, dc});
                // include added module again and see what happens...
                excluded.remove(dep);
                ret = true;
            }
            if (dep.getReleaseVersion() < mrvLo || dep.getReleaseVersion() > mrvHi) {
                FixInfo.putUnfixable(fi, new String[] {"ERR_bad_dep_mrv", mdn, mc, ddn});
                return true;
            }
            if (d.getComparison() == Dependency.COMPARE_SPEC) {
                SpecificationVersion needed = new SpecificationVersion(d.getVersion());
                SpecificationVersion found = dep.getSpecificationVersion();
                if (found == null || found.compareTo(needed) < 0) {
                    FixInfo.putUnfixable(fi, new String[] {"ERR_bad_dep_spec", mdn, mc, ddn});
                    return true;
                }
            } else if (d.getComparison() == Dependency.COMPARE_IMPL) {
                String needed = d.getVersion();
                if (!needed.equals("*") && !needed.equals(dep.getImplementationVersion())) { // NOI18N
                    FixInfo.putUnfixable(fi, new String[] {"ERR_bad_dep_impl", mdn, mc, ddn});
                    return true;
                }
            }
        }
        // Now check token availability:
        for (String tok : new TreeSet<String>(m.getRequiredTokens())) {
            UniverseModule wouldBeProvider = null;
            boolean found = false;
            Set<UniverseModule> possibleProviders = providers.get(tok);
            if (possibleProviders != null) {
                for (UniverseModule p : possibleProviders) {
                    if (excluded.contains(p)) {
                        if (wouldBeProvider == null) {
                            wouldBeProvider = p;
                        }
                    } else {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (wouldBeProvider != null) {
                    node = libChildren.findCluster(wouldBeProvider.getCluster());
                    if (node == null) // #162155
                        return false;
                    String[] msg = new String[] {"ERR_only_excluded_providers", tok, mdn, mc,  // NOI18N
                        wouldBeProvider.getDisplayName(), node.getDisplayName()};
                    if (possibleProviders.size() == 1) {
                        // exactly one (disabled) provider, can be fixed automatically
                        excluded.remove(wouldBeProvider);
                        FixInfo.putFixable(fi, wouldBeProvider.getCodeNameBase(), msg);
                        ret = true;
                        continue;
                    } else {
                        // XXX may display dialog for choosing appropriate provider, turning this into fixable error
                        FixInfo.putUnfixable(fi, msg);
                        return true;
                    }
                } else {
                    FixInfo.putUnfixable(fi, new String[] {"ERR_no_providers", tok, mdn, mc}); // NOI18N
                    return true;
                }
            }
        }
        // no unfixable error for this module.
        return ret;
    }

    private void updateJavaPlatformEnabled() { // #71631
        boolean enabled = ((NbPlatform) platformValue.getSelectedItem()).getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1;
        javaPlatformCombo.setEnabled(enabled);
        javaPlatformButton.setEnabled(enabled); // #72061
    }
    
}
