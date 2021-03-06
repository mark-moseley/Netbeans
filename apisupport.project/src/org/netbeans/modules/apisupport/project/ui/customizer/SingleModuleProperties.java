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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.DependencyListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.FriendListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.RequiredTokenListModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Provides convenient access to a lot of NetBeans Module's properties.
 *
 * @author Martin Krauskopf
 */
public final class SingleModuleProperties extends ModuleProperties {
    
    private static final String[] IDE_TOKENS = new String[] {
        "org.openide.modules.os.Windows", // NOI18N
        "org.openide.modules.os.Unix",  // NOI18N
        "org.openide.modules.os.MacOSX", // NOI18N
        "org.openide.modules.os.PlainUnix",  // NOI18N
        "org.openide.modules.os.OS2" // NOI18N
    };
    
    // property keys for project.properties
    public static final String BUILD_COMPILER_DEBUG = "build.compiler.debug"; // NOI18N
    public static final String BUILD_COMPILER_DEPRECATION = "build.compiler.deprecation"; // NOI18N
    public static final String CLUSTER_DIR = "cluster.dir"; // NOI18N
    public static final String IS_AUTOLOAD = "is.autoload"; // NOI18N
    public static final String IS_EAGER = "is.eager"; // NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVADOC_TITLE = "javadoc.title"; // NOI18N
    public static final String LICENSE_FILE = "license.file"; // NOI18N
    public static final String NBM_HOMEPAGE = "nbm.homepage"; // NOI18N
    public static final String NBM_MODULE_AUTHOR = "nbm.module.author"; // NOI18N
    public static final String NBM_NEEDS_RESTART = "nbm.needs.restart"; // NOI18N
    public static final String SPEC_VERSION_BASE = "spec.version.base"; // NOI18N
    /** @see "#66278" */
    public static final String JAVAC_COMPILERARGS = "javac.compilerargs"; // NOI18N
    
    static final String[] SOURCE_LEVELS = {"1.4", "1.5"}; // NOI18N
    
    private final static Map<String, String> DEFAULTS;
    
    private boolean majorReleaseVersionChanged;
    private boolean specificationVersionChanged;
    private boolean implementationVersionChange;
    private boolean providedTokensChanged;
    private boolean autoUpdateShowInClientChanged;

    private boolean moduleListRefreshNeeded;
    
    static {
        // setup defaults
        Map<String, String> map = new HashMap<String, String>();
        map.put(BUILD_COMPILER_DEBUG, "true"); // NOI18N
        map.put(BUILD_COMPILER_DEPRECATION, "true"); // NOI18N
        map.put(IS_AUTOLOAD, "false"); // NOI18N
        map.put(IS_EAGER, "false"); // NOI18N
        map.put(JAVAC_SOURCE, "1.4"); // NOI18N
        map.put(NBM_NEEDS_RESTART, "false"); // NOI18N
        DEFAULTS = Collections.unmodifiableMap(map);
    }
    
    // helpers for storing and retrieving real values currently stored on the disk
    private NbModuleProvider.NbModuleType moduleType;
    private SuiteProvider suiteProvider;
    private ProjectXMLManager projectXMLManager;
    private final LocalizedBundleInfo.Provider bundleInfoProvider;
    private LocalizedBundleInfo bundleInfo;
    
    // keeps current state of the user changes
    private String majorReleaseVersion;
    private String specificationVersion;
    private String implementationVersion;
    private String provTokensString;
    private SortedSet<String> requiredTokens;
    private Boolean autoUpdateShowInClient;
    private NbPlatform activePlatform;
    private NbPlatform originalPlatform;
    private JavaPlatform activeJavaPlatform;
    private boolean javaPlatformChanged; // #115989
    
    /** package name / selected */
    private SortedSet<String> availablePublicPackages;
    
    private String[] allTokens;
    
    /** Unmodifiable sorted set of all categories in the module's universe. */
    private SortedSet<String> modCategories;
    
    /** Unmodifiable sorted set of all dependencies in the module's universe. */
    private Set<ModuleDependency> universeDependencies;
    
    // models
    private PublicPackagesTableModel publicPackagesModel;
    private DependencyListModel dependencyListModel;
    private FriendListModel friendListModel;
    private RequiredTokenListModel requiredTokensListModel;
    
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    public static final String JAVA_PLATFORM_PROPERTY = "nbjdk.active"; // NOI18N
    public static final String DEPENDENCIES_PROPERTY = "moduleDependencies"; // NOI18N
    
    /**
     * Returns an instance of SingleModuleProperties for the given project.
     */
    public static SingleModuleProperties getInstance(final NbModuleProject project) {
        SuiteProvider sp = project.getLookup().lookup(SuiteProvider.class);
        return new SingleModuleProperties(project.getHelper(), project.evaluator(), sp, Util.getModuleType(project),
                project.getLookup().lookup(LocalizedBundleInfo.Provider.class));
    }
    
    /**
     * Creates a new instance of SingleModuleProperties
     */
    SingleModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator,
            SuiteProvider sp, NbModuleProvider.NbModuleType moduleType,
            LocalizedBundleInfo.Provider bundleInfoProvider) {
        // XXX consider SingleModuleProperties(NbModuleProject) constructor. Life would be easier.
        super(helper, evaluator);
        this.bundleInfoProvider = bundleInfoProvider;
        refresh(moduleType, sp);
    }
    
    protected void refresh(NbModuleProvider.NbModuleType moduleType,
            SuiteProvider suiteProvider) {
        reloadProperties();
        // reset
        this.suiteProvider = suiteProvider;
        this.moduleType = moduleType;
        universeDependencies = null;
        modCategories = null;
        availablePublicPackages = null;
        dependencyListModel = null;
        friendListModel = null;
        requiredTokensListModel = null;
        projectXMLManager = null;
        if (isSuiteComponent()) {
            assert getSuiteDirectory() != null;
            ModuleList.refreshSuiteModuleList(getSuiteDirectory());
        }
        ManifestManager manifestManager = ManifestManager.getInstance(getManifestFile(), false);
        majorReleaseVersion = manifestManager.getReleaseVersion();
        specificationVersion = manifestManager.getSpecificationVersion();
        implementationVersion = manifestManager.getImplementationVersion();
        provTokensString = manifestManager.getProvidedTokensString();
        autoUpdateShowInClient = manifestManager.getAutoUpdateShowInClient();
        String nbDestDirS = getEvaluator().getProperty("netbeans.dest.dir"); // NOI18N
        if (nbDestDirS != null) {
            originalPlatform = activePlatform = NbPlatform.getPlatformByDestDir(
                    getHelper().resolveFile(nbDestDirS));
        }
        String activeJdk = getEvaluator().getProperty("nbjdk.active"); // NOI18N
        if (activeJdk != null) {
            activeJavaPlatform = ModuleProperties.findJavaPlatformByID(activeJdk); // NOI18N
        } else {
            String activeJdkHome = getEvaluator().getProperty("nbjdk.home"); // NOI18N
            activeJavaPlatform = ModuleProperties.findJavaPlatformByLocation(activeJdkHome);
        }
        javaPlatformChanged = false;
        getPublicPackagesModel().reloadData(loadPublicPackages());
        requiredTokens = Collections.unmodifiableSortedSet(
                new TreeSet<String>(Arrays.asList(manifestManager.getRequiredTokens())));
        bundleInfo = bundleInfoProvider.getLocalizedBundleInfo();
        if (bundleInfo != null) {
            try {
                bundleInfo.reload();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        firePropertiesRefreshed();
    }
    
    void libraryWrapperAdded() {
        // presuambly we do not need to reset anything else
        universeDependencies = null;
    }
    
    Map<String, String> getDefaultValues() {
        return DEFAULTS;
    }
    
    LocalizedBundleInfo getBundleInfo() {
        return bundleInfo;
    }
    
    // ---- READ ONLY start
    
    /** Returns code name base of the module this instance managing. */
    String getCodeNameBase() {
        return getProjectXMLManager().getCodeNameBase();
    }
    
    String getJarFile() {
        return getHelper().resolveFile(getEvaluator().evaluate("${cluster}/${module.jar}")).getAbsolutePath(); // NOI18N
    }
    
    String getSuiteDirectoryPath() {
        return getSuiteDirectory() != null ? getSuiteDirectory().getPath() : null;
    }
    
    File getSuiteDirectory() {
        return suiteProvider != null ? suiteProvider.getSuiteDirectory() : null;
    }
    
    /** Call only for suite component modules. */
    SuiteProject getSuite() {
        assert isSuiteComponent();
        SuiteProject suite = null;
        try {
            FileObject suiteDir = FileUtil.toFileObject(getSuiteDirectory());
            if (suiteDir != null) {
                suite = (SuiteProject) ProjectManager.getDefault().findProject(suiteDir);
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return suite;
    }
    
    // ---- READ ONLY end
    
    /** Check whether the active platform is valid. */
    boolean isActivePlatformValid() {
        NbPlatform plaf = getActivePlatform();
        return plaf == null || plaf.isValid();
    }
    
    /**
     * Returns currently set platform. i.e. platform set in the
     * <em>Libraries</em> panel. Note that it could be <code>null</code> for
     * NetBeans.org modules.
     */
    NbPlatform getActivePlatform() {
        if (moduleType != NbModuleProvider.NETBEANS_ORG && activePlatform == null) {
            ModuleProperties.reportLostPlatform(activePlatform);
            activePlatform = NbPlatform.getDefaultPlatform();
        }
        return activePlatform;
    }
    
    void setActivePlatform(NbPlatform newPlaf) {
        if (this.activePlatform != newPlaf) {
            NbPlatform oldPlaf = this.activePlatform;
            this.activePlatform = newPlaf;
            this.dependencyListModel = null;
            this.universeDependencies = null;
            this.modCategories = null;
            firePropertyChange(NB_PLATFORM_PROPERTY, oldPlaf, newPlaf);
        }
    }
    
    JavaPlatform getActiveJavaPlatform() {
        return activeJavaPlatform;
    }
    
    void setActiveJavaPlatform(JavaPlatform nue) {
        JavaPlatform old = activeJavaPlatform;
        if (nue != old) {
            activeJavaPlatform = nue;
            firePropertyChange(JAVA_PLATFORM_PROPERTY, old, nue);
            javaPlatformChanged = true;
        }
    }
    
    String getMajorReleaseVersion() {
        return majorReleaseVersion;
    }
    
    void setMajorReleaseVersion(String ver) {
        if (!Utilities.compareObjects(majorReleaseVersion, ver)) {
            majorReleaseVersion = ver;
            majorReleaseVersionChanged = true;
        }
    }
    
    String getSpecificationVersion() {
        return specificationVersion;
    }
    
    void setSpecificationVersion(String ver) {
        if (!Utilities.compareObjects(specificationVersion, ver)) {
            specificationVersion = ver;
            specificationVersionChanged = true;
        }
    }
    
    String getImplementationVersion() {
        return implementationVersion;
    }
    
    void setImplementationVersion(String ver) {
        if (!Utilities.compareObjects(implementationVersion, ver)) {
            implementationVersion = ver;
            implementationVersionChange = true;
        }
    }
    
    String getProvidedTokens() {
        return provTokensString;
    }
    
    void setProvidedTokens(String tokens) {
        if (!Utilities.compareObjects(provTokensString, tokens)) {
            provTokensString = tokens;
            providedTokensChanged = true;
        }
    }

    public Boolean getAutoUpdateShowInClient() {
        return autoUpdateShowInClient;
    }

    public void setAutoUpdateShowInClient(Boolean autoUpdateShowInClient) {
        if (!Utilities.compareObjects(this.autoUpdateShowInClient, autoUpdateShowInClient)) {
            this.autoUpdateShowInClient = autoUpdateShowInClient;
            autoUpdateShowInClientChanged = true;
        }
    }
    
    boolean isStandalone() {
        return moduleType == NbModuleProvider.STANDALONE;
    }
    
    boolean isNetBeansOrg() {
        return moduleType == NbModuleProvider.NETBEANS_ORG;
    }
    
    boolean isSuiteComponent() {
        return moduleType == NbModuleProvider.SUITE_COMPONENT;
    }

    public void setModuleListRefreshNeeded(boolean moduleListRefreshNeeded) {
        this.moduleListRefreshNeeded = moduleListRefreshNeeded;
    }
    
    boolean isModuleListRefreshNeeded() {
        return moduleListRefreshNeeded;
    }
    
    boolean dependingOnImplDependency() {
        DependencyListModel depsModel = getDependenciesListModel();
        if (depsModel == CustomizerComponentFactory.INVALID_DEP_LIST_MODEL) {
            return false;
        }
        Set<ModuleDependency> deps = depsModel.getDependencies();
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency dep = (ModuleDependency) it.next();
            if (dep.hasImplementationDepedendency()) {
                return true;
            }
        }
        return false;
    }
    
    private ProjectXMLManager getProjectXMLManager() {
        if (projectXMLManager == null) {
            try {
                projectXMLManager = ProjectXMLManager.getInstance(getProjectDirectoryFile());
            } catch (IOException e) {
                assert false : e;
            }
        }
        return projectXMLManager;
    }
    
    /**
     * Returns list model of module's dependencies regarding the currently
     * selected platform.
     */
    DependencyListModel getDependenciesListModel() {
        if (dependencyListModel == null) {
            if (isActivePlatformValid()) {
                try {
                    dependencyListModel = new DependencyListModel(
                            getProjectXMLManager().getDirectDependencies(getActivePlatform()), true);
                    // add listener and fire DEPENDENCIES_PROPERTY when deps are changed
                    dependencyListModel.addListDataListener(new ListDataListener() {
                        public void contentsChanged(ListDataEvent e) {
                            firePropertyChange(DEPENDENCIES_PROPERTY, null,
                                    getDependenciesListModel());
                        }
                        public void intervalAdded(ListDataEvent e) {
                            contentsChanged(null);
                        }
                        public void intervalRemoved(ListDataEvent e) {
                            contentsChanged(null);
                        }
                    });
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                    dependencyListModel = CustomizerComponentFactory.getInvalidDependencyListModel();
                }
            } else {
                dependencyListModel = CustomizerComponentFactory.getInvalidDependencyListModel();
            }
        }
        return dependencyListModel;
    }
    
    /**
     * Returns a set of available {@link ModuleDependency modules dependencies}
     * in the module's universe according to the currently selected {@link
     * #getActivePlatform() platform}.<p>
     *
     * <strong>Note:</strong> Don't call this method from EDT, since it may be
     * really slow. The {@link AssertionError} will be thrown if you try to do
     * so.
     *
     * @param filterExcludedModules if <code>true</code> and this module is a
     *        suite component, modules excluded from the suite's module list
     *        will be excluded from the returned set.
     * @param apiProvidersOnly if <code>true</code> only modules which provide
     *        public packages and have friendly relationship with this module
     *        will be included in the returned set
     */
    Set<ModuleDependency> getUniverseDependencies(
            final boolean filterExcludedModules, final boolean apiProvidersOnly) {
        assert !SwingUtilities.isEventDispatchThread() :
            "SingleModuleProperties.getUniverseDependencies() cannot be called from EDT"; // NOI18N
        if (universeDependencies == null) {
            reloadModuleListInfo();
        }
        if (universeDependencies == null) {
            // Broken platform.
            return Collections.emptySet();
        }
        Set<ModuleDependency> result = new HashSet<ModuleDependency>(universeDependencies);
        if (filterExcludedModules && isSuiteComponent()) {
            SuiteProject suite = getSuite();
            if (suite == null) {
                DialogDisplayer.getDefault().notify(new Message(NbBundle.getMessage(SingleModuleProperties.class,
                        "SingleModuleProperties.incorrectSuite", getSuiteDirectoryPath(), getProjectDisplayName()),
                        Message.WARNING_MESSAGE));
                return Collections.emptySet();
            }
            String[] disableModules = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY);
            String[] enableClusters = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.ENABLED_CLUSTERS_PROPERTY);
            String[] disableClusters = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY);
            String suiteClusterProp = getEvaluator().getProperty("cluster"); // NOI18N
            File suiteClusterDir = suiteClusterProp != null ? getHelper().resolveFile(suiteClusterProp) : null;
            for (Iterator<ModuleDependency> it = result.iterator(); it.hasNext();) {
                ModuleDependency dep = it.next();
                ModuleEntry me = dep.getModuleEntry();
                if (me.getClusterDirectory().equals(suiteClusterDir)) {
                    // #72124: do not filter other modules in the same suite.
                    continue;
                }
                if (isExcluded(me, disableModules, enableClusters, disableClusters)) {
                    it.remove();
                }
            }
        }
        if (apiProvidersOnly) { // remove module without public/friend API
            for (Iterator<ModuleDependency> it = result.iterator(); it.hasNext();) {
                ModuleDependency dep = it.next();
                ModuleEntry me = dep.getModuleEntry();
                if (me.getPublicPackages().length == 0 || !me.isDeclaredAsFriend(getCodeNameBase())) {
                    it.remove();
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Delegates to {@link #getUniverseDependencies(boolean, boolean)} with
     * <code>false</code> as a second parameter.
     */
    Set<ModuleDependency> getUniverseDependencies(final boolean filterExcludedModules) {
        return getUniverseDependencies(filterExcludedModules, false);
    }
    
    private static boolean isExcluded(final ModuleEntry me,
            final String[] disableModules, final String[] enableClusters, final String[] disableClusters) {
        if (Arrays.binarySearch(disableModules, me.getCodeNameBase()) >= 0) {
            return true;
        }
        if (enableClusters.length != 0 && Arrays.binarySearch(enableClusters, me.getClusterDirectory().getName()) < 0) {
            return true;
        }
        if (enableClusters.length == 0 && Arrays.binarySearch(disableClusters, me.getClusterDirectory().getName()) >= 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns sorted arrays of CNBs of available friends for this module.
     */
    String[] getAvailableFriends() {
        SortedSet<String> set = new TreeSet<String>();
        if (isSuiteComponent()) {
            for (Iterator it = SuiteUtils.getSubProjects(getSuite()).iterator(); it.hasNext();) {
                Project prj = (Project) it.next();
                String cnb = ProjectUtils.getInformation(prj).getName();
                if (!getCodeNameBase().equals(cnb)) {
                    set.add(cnb);
                }
            }
        } else if (isNetBeansOrg()) {
            Set<ModuleDependency> deps = getUniverseDependencies(false);
            for (Iterator it = deps.iterator(); it.hasNext();) {
                ModuleDependency dep = (ModuleDependency) it.next();
                set.add(dep.getModuleEntry().getCodeNameBase());
            }
        } // else standalone module - leave empty (see the UI spec)
        return set.toArray(new String[set.size()]);
    }
    
    FriendListModel getFriendListModel() {
        if (friendListModel == null) {
            friendListModel = new FriendListModel(getProjectXMLManager().getFriends());
        }
        return friendListModel;
    }
    
    RequiredTokenListModel getRequiredTokenListModel() {
        if (requiredTokensListModel == null) {
            requiredTokensListModel = new RequiredTokenListModel(requiredTokens);
        }
        return requiredTokensListModel;
    }
    
    // XXX should be probably moved into ModuleList
    String[] getAllTokens() {
        if (allTokens == null) {
            try {
                SortedSet<String> provTokens = new TreeSet<String>();
                provTokens.addAll(Arrays.asList(IDE_TOKENS));
                for (Iterator it = getModuleList().getAllEntriesSoft().iterator(); it.hasNext(); ) {
                    ModuleEntry me = (ModuleEntry) it.next();
                    provTokens.addAll(Arrays.asList(me.getProvidedTokens()));
                }
                String[] result = new String[provTokens.size()];
                return provTokens.toArray(result);
            } catch (IOException e) {
                allTokens = new String[0];
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return allTokens;
    }
    
    PublicPackagesTableModel getPublicPackagesModel() {
        if (publicPackagesModel == null) {
            publicPackagesModel = new PublicPackagesTableModel(loadPublicPackages());
        }
        return publicPackagesModel;
    }
    
    /** Loads a map of package-isSelected entries. */
    private Map<String, Boolean> loadPublicPackages() {
        Collection<String> selectedPackages = getSelectedPackages();
        Map<String, Boolean> publicPackages = new TreeMap<String, Boolean>();
        for (Iterator<String> it = getAvailablePublicPackages().iterator(); it.hasNext(); ) {
            String pkg = it.next();
            publicPackages.put(pkg, Boolean.valueOf(selectedPackages.contains(pkg)));
        }
        return publicPackages;
    }
    
    private Collection<String> getSelectedPackages() {
        Collection<String> sPackages = new HashSet<String>();
        ManifestManager.PackageExport[] pexports = getProjectXMLManager().getPublicPackages();
        for (int i = 0; i < pexports.length; i++) {
            ManifestManager.PackageExport pexport = pexports[i];
            if (pexport.isRecursive()) {
                for (Iterator it = getAvailablePublicPackages().iterator(); it.hasNext(); ) {
                    String p = (String) it.next();
                    if (p.startsWith(pexport.getPackage())) {
                        sPackages.add(p);
                    }
                }
            } else {
                sPackages.add(pexport.getPackage());
            }
        }
        return sPackages;
    }
    
    /**
     * Returns set of all available public packages for the project.
     */
    Set<String> getAvailablePublicPackages() {
        if (availablePublicPackages == null) {
            availablePublicPackages = Util.scanProjectForPackageNames(getProjectDirectoryFile());
        }
        return availablePublicPackages;
    }
    
    @Override void storeProperties() throws IOException {
        super.storeProperties();
        
        // Store chnages in manifest
        storeManifestChanges();
        
        // store localized info
        if (bundleInfo != null && bundleInfo.isModified()) {
            bundleInfo.store();
        } // XXX else ignore for now but we could save into some default location
        
        // Store project.xml changes
        // store module dependencies
        DependencyListModel dependencyModel = getDependenciesListModel();
        if (dependencyModel.isChanged()) {
            Set<ModuleDependency> depsToSave = new TreeSet<ModuleDependency>(dependencyModel.getDependencies());
            
            // process removed modules
            depsToSave.removeAll(dependencyModel.getRemovedDependencies());
            
            // process added modules
            depsToSave.addAll(dependencyModel.getAddedDependencies());
            
            // process edited modules
            for (Map.Entry<ModuleDependency,ModuleDependency> entry : dependencyModel.getEditedDependencies().entrySet()) {
                depsToSave.remove(entry.getKey());
                depsToSave.add(entry.getValue());
            }
            
            logNetBeansAPIUsage("DEPENDENCIES", dependencyModel.getDependencies()); // NOI18N
            
            getProjectXMLManager().replaceDependencies(depsToSave);
        }
        String[] friends = getFriendListModel().getFriends();
        String[] publicPkgs = getPublicPackagesModel().getSelectedPackages();
        if (getPublicPackagesModel().isChanged() || getFriendListModel().isChanged()) {
            if (friends.length > 0) { // store friends packages
                getProjectXMLManager().replaceFriends(friends, publicPkgs);
            } else { // store public packages
                getProjectXMLManager().replacePublicPackages(publicPkgs);
            }
            setModuleListRefreshNeeded(true);
        }
        
        if (isStandalone()) {
            ModuleProperties.storePlatform(getHelper(), getActivePlatform());
            if (javaPlatformChanged) {
                ModuleProperties.storeJavaPlatform(getHelper(), getEvaluator(), getActiveJavaPlatform(), false);
            }
        } else if (isNetBeansOrg() && javaPlatformChanged) {
            ModuleProperties.storeJavaPlatform(getHelper(), getEvaluator(), getActiveJavaPlatform(), true);
        }
    }
    
    /** UI Logger for apisupport */
    static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.apisupport"); // NOI18N

    /** Sends info to UI handler about NetBeans APIs in use
     */
    private static void logNetBeansAPIUsage(String msg, Collection<ModuleDependency> deps) {
        List<String> cnbs = new ArrayList<String>();
        for (ModuleDependency moduleDependency : deps) {
            String cnb = moduleDependency.getModuleEntry().getCodeNameBase();
            // observe just NetBeans API module usage
            if (cnb.startsWith("org.openide") || cnb.startsWith("org.netbeans")) { // NOI18N
                cnbs.add(cnb);
            }
        }
        
        if (cnbs.isEmpty()) {
            return;
        }
        
        LogRecord rec = new LogRecord(Level.CONFIG, msg);
        rec.setParameters(cnbs.toArray(new String[0]));
        rec.setResourceBundleName(SingleModuleProperties.class.getPackage().getName() + ".Bundle"); // NOI18N
        rec.setResourceBundle(NbBundle.getBundle(SingleModuleProperties.class));
        rec.setLoggerName(UI_LOG.getName());
        UI_LOG.log(rec);
    }
    
    /**
     * Store appropriately properties regarding the manifest file.
     */
    private void storeManifestChanges() throws IOException {
        FileObject manifestFO = FileUtil.toFileObject(getManifestFile());
        EditableManifest em;
        if (manifestFO != null) {
            em = Util.loadManifest(manifestFO);
        } else { // manifest doesn't exist yet
            em = new EditableManifest();
            manifestFO = FileUtil.createData(
                    getHelper().getProjectDirectory(), "manifest.mf"); // NOI18N
        }
        boolean changed = false;
        if (majorReleaseVersionChanged) {
            String module = "".equals(getMajorReleaseVersion()) ?
                getCodeNameBase() :
                getCodeNameBase() + '/' + getMajorReleaseVersion();
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE, module);
            changed = true;
        }
        if (specificationVersionChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION,
                    getSpecificationVersion());
            changed = true;
        }
        if (implementationVersionChange) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION,
                    getImplementationVersion());
            changed = true;
        }
        if (providedTokensChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_PROVIDES,
                    getProvidedTokens());
            changed = true;
        }
        if (getRequiredTokenListModel().isChanged()) {
            String[] reqTokens = getRequiredTokenListModel().getTokens();
            StringBuffer result = new StringBuffer(reqTokens.length > 1 ? "\n  " : ""); // NOI18N
            for (int i = 0; i < reqTokens.length; i++) {
                if (i != 0) {
                    result.append(",\n  "); // NOI18N
                }
                result.append(reqTokens[i]);
            }
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_REQUIRES, result.toString());
            changed = true;
        }
        if (autoUpdateShowInClientChanged) {
            setManifestAttribute(em, ManifestManager.AUTO_UPDATE_SHOW_IN_CLIENT, autoUpdateShowInClient != null ? autoUpdateShowInClient.toString() : "");
            changed = true;
        }
        if (changed) {
            Util.storeManifest(manifestFO, em);
        }
    }
    
    // XXX should be something similar provided be EditableManifest?
    private void setManifestAttribute(EditableManifest em, String key, String value) {
        assert value != null;
        if ("".equals(value)) {
            if (em.getAttribute(key, null) != null) {
                em.removeAttribute(key, null);
            }
        } else {
            em.setAttribute(key, value, null);
        }
    }
    
    // package provide for unit test
    File getManifestFile() {
        return getHelper().resolveFile(getEvaluator().getProperty("manifest.mf")); // NOI18N
    }
    
    /**
     * Returns a set of all available categories in the module's universe
     * according to the currently selected platform ({@link
     * #getActivePlatform()})<p>
     * <strong>Note:</strong> Don't call this method from EDT, since it may be
     * really slow. The {@link AssertionError} will be thrown if you try to do
     * so.
     */
    SortedSet getModuleCategories() {
        assert !SwingUtilities.isEventDispatchThread() :
            "SingleModuleProperties.getModuleCategories() cannot be called from EDT"; // NOI18N
        if (modCategories == null && !reloadModuleListInfo()) {
            return new TreeSet();
        }
        return modCategories;
    }
    
    /**
     * Prepare all ModuleDependencies from this module's universe. Also prepare
     * all categories. <strong>Package-private only for unit tests.</strong>
     */
    boolean reloadModuleListInfo() {
        assert !SwingUtilities.isEventDispatchThread() :
            "SingleModuleProperties.reloadModuleListInfo() cannot be called from EDT"; // NOI18N
        if (isActivePlatformValid()) {
            try {
                SortedSet<String> allCategories = new TreeSet<String>(Collator.getInstance());
                Set<ModuleDependency> allDependencies = new HashSet<ModuleDependency>();
                for (Iterator it = getModuleList().getAllEntriesSoft().iterator(); it.hasNext(); ) {
                    ModuleEntry me = (ModuleEntry) it.next();
                    if (!me.getCodeNameBase().equals(getCodeNameBase())) {
                        allDependencies.add(new ModuleDependency(me));
                    }
                    String cat = me.getCategory();
                    if (cat != null) {
                        allCategories.add(cat);
                    }
                }
                modCategories = Collections.unmodifiableSortedSet(allCategories);
                universeDependencies = Collections.unmodifiableSet(allDependencies);
                return true;
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        return false;
    }
    
    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage. <strong>Package-private only for unit tests.</strong>
     */
    ModuleList getModuleList() throws IOException {
        if (getActivePlatform() != this.originalPlatform) {
            try {
                return ModuleList.getModuleList(getProjectDirectoryFile(), getActivePlatform().getDestDir());
            } catch (IOException x) {
                // #69029: maybe invalidated platform? Try the default platform instead.
                Logger.getLogger(SingleModuleProperties.class.getName()).log(Level.FINE, null, x);
                return ModuleList.getModuleList(getProjectDirectoryFile(), NbPlatform.getDefaultPlatform().getDestDir());
            }
        } else {
            return ModuleList.getModuleList(getProjectDirectoryFile());
        }
    }
    
    /**
     * Just use a combination of evaluator and resolver. May return
     * <code>null</code> if evaluating fails.
     */
    File evaluateFile(final String currentLicence) {
        String evaluated = getEvaluator().evaluate(currentLicence);
        return evaluated == null ? null : getHelper().resolveFile(evaluated);
    }
    
    Project getProject() {
        Project p = null;
        try {
            p = ProjectManager.getDefault().findProject(getHelper().getProjectDirectory());
        } catch (IOException e) {
            assert false : e;
        }
        return p;
    }
    
}
