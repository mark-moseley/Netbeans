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

import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.SuiteSubModulesListModel;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;

/**
 * Provides convenient access to a lot of Suite Module's properties.
 *
 * @author Martin Krauskopf
 */
public final class SuiteProperties extends ModuleProperties {
    
    public static final String DISABLED_MODULES_PROPERTY = "disabled.modules"; // NOI18N
    public static final String ENABLED_CLUSTERS_PROPERTY = "enabled.clusters"; // NOI18N
    public static final String DISABLED_CLUSTERS_PROPERTY = "disabled.clusters"; // NOI18N
    
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    public static final String JAVA_PLATFORM_PROPERTY = "nbjdk.active"; // NOI18N
    public static final String CLUSTER_DIR = "build/cluster";    // NOI18N
    public static final String ACTIVE_NB_PLATFORM_PROPERTY = "nbplatform.active";    // NOI18N
    public static final String ACTIVE_NB_PLATFORM_DIR_PROPERTY = "nbplatform.active.dir";    // NOI18N
    public static final String CLUSTER_PATH_PROPERTY = "cluster.path";    // NOI18N
    public static final String CLUSTER_PATH_WDC_PROPERTY = "cluster.path.with.disabled.clusters";    // NOI18N
    public static final String CLUSTER_SRC_PREFIX = "extcluster.";    // NOI18N

    private NbPlatform activePlatform;
    private JavaPlatform activeJavaPlatform;
    
    /** Project the current properties represents. */
    private SuiteProject project;
    
    /** Represent original set of sub-modules. */
    private Set<NbModuleProject> origSubModules;
    
    /** Represent currently set set of sub-modules. */
    private Set<NbModuleProject> subModules;
    
    // models
    private SuiteSubModulesListModel moduleListModel;
    
    /** disabled modules */
    private String[] disabledModules;
    /** enabled clusters */
    private String[] enabledClusters;
    /** boolean variable to remember whether there were some changes */
    private boolean changedDisabledModules, changedEnabledClusters;
    private boolean clusterPathChanged;
    
    /** keeps all information related to branding*/
    private final BasicBrandingModel brandingModel;
    private Set<ClusterInfo> clusterPath;
    private boolean refreshBuildScripts;
    
    /**
     * Creates a new instance of SuiteProperties
     */
    public SuiteProperties(SuiteProject project, AntProjectHelper helper,
            PropertyEvaluator evaluator, Set<NbModuleProject> subModules) {
        super(helper, evaluator);
        this.project = project;
        refresh(subModules);
        this.disabledModules = getArrayProperty(evaluator, DISABLED_MODULES_PROPERTY);
        this.enabledClusters = getArrayProperty(evaluator, ENABLED_CLUSTERS_PROPERTY);
        if (enabledClusters.length == 0) {
            // Compatibility.
            SortedSet<String> clusters = new TreeSet<String>();
            for (ModuleEntry module : activePlatform.getModules()) {
                clusters.add(module.getClusterDirectory().getName());
            }
            clusters.removeAll(Arrays.asList(getArrayProperty(evaluator, DISABLED_CLUSTERS_PROPERTY)));
            enabledClusters = new String[clusters.size()];
            int i = 0; for (String cluster : clusters) {
                enabledClusters[i++] = SingleModuleProperties.clusterBaseName(cluster);
            }
        }
        brandingModel = new BasicBrandingModel(this);
    }
    
    void refresh(Set<NbModuleProject> subModules) {
        reloadProperties();
        this.origSubModules = Collections.unmodifiableSet(subModules);
        this.subModules = subModules;
        this.moduleListModel = null;
        activePlatform = project.getPlatform(true);
        activeJavaPlatform = ModuleProperties.findJavaPlatformByID(getEvaluator().getProperty("nbjdk.active")); // NOI18N
        firePropertiesRefreshed();
    }
    
    public SuiteProject getProject() {
        return project;
    }
    
    Map<String,String> getDefaultValues() {
        return Collections.emptyMap(); // no default value (yet)
    }
    
    public NbPlatform getActivePlatform() {
        return activePlatform;
    }
    
    void setActivePlatform(NbPlatform newPlaf) {
        NbPlatform oldPlaf = this.activePlatform;
        this.activePlatform = newPlaf;
        clusterPath = null; // reset on platform change
        firePropertyChange(NB_PLATFORM_PROPERTY, oldPlaf, newPlaf);
    }
    
    JavaPlatform getActiveJavaPlatform() {
        return activeJavaPlatform;
    }
    
    void setActiveJavaPlatform(JavaPlatform nue) {
        JavaPlatform old = activeJavaPlatform;
        if (nue != old) {
            activeJavaPlatform = nue;
            firePropertyChange(JAVA_PLATFORM_PROPERTY, old, nue);
        }
    }
    
    String[] getEnabledClusters() {
        return enabledClusters;
    }
    
    String[] getDisabledModules() {
        return disabledModules;
    }
    
    public void setEnabledClusters(String[] value) {
        if (Arrays.asList(enabledClusters).equals(Arrays.asList(value))) {
            return;
        }
        this.enabledClusters = value;
        this.changedEnabledClusters = true;
    }
    
    public void setDisabledModules(String[] value) {
        if (Arrays.asList(disabledModules).equals(Arrays.asList(value))) {
            return;
        }
        this.disabledModules = value;
        this.changedDisabledModules = true;
    }
    
    public static String[] getArrayProperty(PropertyEvaluator evaluator, String p) {
        String s = evaluator.getProperty(p);
        String[] arr = null;
        if (s != null) {
            StringTokenizer tok = new StringTokenizer(s, ","); // NOI18N
            arr = new String[tok.countTokens()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = tok.nextToken().trim();
            }
        }
        return arr == null ? new String[0] : arr;
    }
    
    public void storeProperties() throws IOException {
        assert ProjectManager.mutex().isWriteAccess();
        NbPlatform plaf = getActivePlatform();
        ModuleProperties.storePlatform(getHelper(), plaf);
        ModuleProperties.storeJavaPlatform(getHelper(), getEvaluator(), getActiveJavaPlatform(), false);
        getBrandingModel().store();
        
        // store submodules if they've changed
        SuiteSubModulesListModel model = getModulesListModel();
        if (model.isChanged()) {
            SuiteUtils.replaceSubModules(this);
        }
        
        if (changedDisabledModules || changedEnabledClusters || clusterPathChanged) {
            EditableProperties ep = getHelper().getProperties("nbproject/platform.properties"); // NOI18N
            if (changedDisabledModules) {
                String[] separated = disabledModules.clone();
                for (int i = 0; i < disabledModules.length - 1; i++) {
                    separated[i] = disabledModules[i] + ',';
                }
                ep.setProperty(DISABLED_MODULES_PROPERTY, separated);
                // Do not want it left in project.properties if it was there before (from 5.0):
                setProperty(DISABLED_MODULES_PROPERTY, (String) null);
            }
            if (changedEnabledClusters) {
                String[] separated = new String[enabledClusters.length];
                for (int i = 0; i < enabledClusters.length; i++) {
                    separated[i] = representationOfCluster(enabledClusters[i], plaf);
                    if (i < enabledClusters.length - 1) {
                        separated[i] = separated[i] + ',';
                    }
                }
                ep.setProperty(ENABLED_CLUSTERS_PROPERTY, separated);
                setProperty(ENABLED_CLUSTERS_PROPERTY, (String) null);
                if (plaf == null || plaf.getHarnessVersion() < NbPlatform.HARNESS_VERSION_50u1) {
                    // Compatibility.
                    SortedSet<String> disabledClusters = new TreeSet<String>();
                    Set<ModuleEntry> modules = activePlatform.getModules();
                    for (ModuleEntry entry : modules) {
                        disabledClusters.add(entry.getClusterDirectory().getName());
                    }
                    disabledClusters.removeAll(Arrays.asList(enabledClusters));
                    separated = disabledClusters.toArray(new String[disabledClusters.size()]);
                    for (int i = 0; i < separated.length - 1; i++) {
                        separated[i] = separated[i] + ',';
                    }
                    ep.setProperty(DISABLED_CLUSTERS_PROPERTY, separated);
                    ep.setComment(DISABLED_CLUSTERS_PROPERTY, new String[] {"# Deprecated since 5.0u1; for compatibility with 5.0:"}, false); // NOI18N
                }
            }
            if (clusterPathChanged) {
                ArrayList<String> cp = new ArrayList<String>();
                ArrayList<String> cpwdc = new ArrayList<String>();
                boolean anyDisabled = false;

                for (ClusterInfo ci : clusterPath) {
                    if (ci.isPlatformCluster()) {
                        String cluster = ci.getClusterDir().getName();
                        String entry = toPlatformClusterEntry(cluster);
                        cp.add(entry);
                        cpwdc.add(entry);
                    } else {
                        String entry = PropertyUtils.relativizeFile(getProjectDirectoryFile(), ci.getClusterDir());
                        if (ci.isEnabled()) {
                            cp.add(entry);
                        } else {
                            anyDisabled = true;
                        }
                        cpwdc.add(entry);

                        if (ci.isExternalCluster()) {
                            if (ci.getSourceRoots() != null) {
                                String propName = CLUSTER_SRC_PREFIX + entry + NbPlatform.PLATFORM_SOURCES_SUFFIX;
                                ep.setProperty(propName, Util.urlsToAntPath(ci.getSourceRoots()));
                            }
                            if (ci.getJavadocRoots() != null) {
                                String propName = CLUSTER_SRC_PREFIX + entry + NbPlatform.PLATFORM_JAVADOC_SUFFIX;
                                ep.setProperty(propName, Util.urlsToAntPath(ci.getJavadocRoots()));
                            }
                            ModuleList.refreshClusterModuleList(ci.getClusterDir());
                        }
                    }
                }
                if (anyDisabled) {
                    ep.setProperty(CLUSTER_PATH_WDC_PROPERTY, SuiteUtils.getAntProperty(cpwdc));
                } else {
                    ep.remove(CLUSTER_PATH_WDC_PROPERTY);
                }
                ep.setProperty(CLUSTER_PATH_PROPERTY, SuiteUtils.getAntProperty(cp));
                ep.remove(ENABLED_CLUSTERS_PROPERTY);
            }
            getHelper().putProperties("nbproject/platform.properties", ep); // NOI18N
            if (refreshBuildScripts) {
                // setting cluster.path for the 1st time, needs to adjust build-impl.xm alul of all sub-projects
                try {
                    Set<NbModuleProject> subprojects = SuiteUtils.getSubProjects(getProject());
                    for (NbModuleProject prj : subprojects) {
                        prj.refreshBuildScripts(false, plaf);
                    }
                    getProject().refreshBuildScripts(false);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            changedDisabledModules = false;
            changedEnabledClusters = false;
            clusterPathChanged = false;
            refreshBuildScripts = false;
        }
        
        super.storeProperties();
    }
    private static String representationOfCluster(String physicalName, NbPlatform platform) { // #73706
        if (platform != null && platform.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_65) {
            return SingleModuleProperties.clusterBaseName(physicalName);
        } else {
            return physicalName;
        }
    }

    // TODO C.P tests once SuiteProjectGenerator.createSuiteProject generates new
    /**
     * Returns set of clusters.
     * Content is read from cluster.path and cluster.path.with.disabled.clusters
     * properties.
     * @return Set of clusters. Set is iterable in the same order as read from properties.
     */
    public Set<ClusterInfo> getClusterPath() {
        if (clusterPath == null) {
            clusterPath = ClusterUtils.evaluateClusterPath(getProjectDirectoryFile(), getEvaluator(), getActivePlatform().getDestDir());
        }
        return clusterPath;
    }

    public void setClusterPath(List<ClusterInfo> clusterPathList) {
        Set<ClusterInfo> newClusterPath = new LinkedHashSet<ClusterInfo>(clusterPathList);
        if (newClusterPath.equals(getClusterPath()))
            return;
        if (clusterPath.isEmpty()) {
            // setting cluster.path for the 1st time, needs to adjust build-impl.xml of all sub-projects
            refreshBuildScripts = true;
        }
        clusterPath = newClusterPath;
        clusterPathChanged = true;
    }

    public static String toPlatformClusterEntry(String cluster) {
        return "${" + ACTIVE_NB_PLATFORM_DIR_PROPERTY + "}/" + SingleModuleProperties.clusterBaseName(cluster);
    }


    /**
     * Converts "raw" cluster.path entry (possibly with nbplatform.active.dir,
     * bare cluster names and project dir-relative path) into valid absolute file path.
     * @param rawEntry
     * @return
     */
    File evaluateClusterPathEntry(String rawEntry) {
        return ClusterUtils.evaluateClusterPathEntry(rawEntry, getProjectDirectoryFile(), getEvaluator(), getActivePlatform().getDestDir());
    }

    Set<NbModuleProject> getSubModules() {
        return getModulesListModel().getSubModules();
    }
    
    Set<NbModuleProject> getOrigSubModules() {
        return origSubModules;
    }
    
    /**
     * Returns list model of module's dependencies regarding the currently
     * selected platform.
     */
    SuiteSubModulesListModel getModulesListModel() {
        if (moduleListModel == null) {
            moduleListModel = new SuiteSubModulesListModel(subModules);
        }
        return moduleListModel;
    }
    
    public BasicBrandingModel getBrandingModel() {
        return brandingModel;
    }

}

