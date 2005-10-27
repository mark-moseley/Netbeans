/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.DependencyListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.FriendListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.RequiredTokenListModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
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
    public static final String JAVAC_SOURCES = "javac.source"; // NOI18N
    public static final String JAVADOC_TITLE = "javadoc.title"; // NOI18N
    public static final String LICENSE_FILE = "license.file"; // NOI18N
    public static final String NBM_HOMEPAGE = "nbm.homepage"; // NOI18N
    public static final String NBM_IS_GLOBAL = "nbm.is.global"; // NOI18N
    public static final String NBM_MODULE_AUTHOR = "nbm.module.author"; // NOI18N
    public static final String NBM_NEEDS_RESTART = "nbm.needs.restart"; // NOI18N
    public static final String SPEC_VERSION_BASE = "spec.version.base"; // NOI18N
    
    static final String[] SOURCE_LEVELS = {"1.4", "1.5"}; // NOI18N
    
    private final static Map/*<String, String>*/ DEFAULTS;
    
    private boolean majorReleaseVersionChanged;
    private boolean specificationVersionChanged;
    private boolean implementationVersionChange;
    private boolean providedTokensChanged;
    
    static {
        // setup defaults
        Map map = new HashMap();
        map.put(BUILD_COMPILER_DEBUG, "true"); // NOI18N
        map.put(BUILD_COMPILER_DEPRECATION, "true"); // NOI18N
        map.put(IS_AUTOLOAD, "false"); // NOI18N
        map.put(IS_EAGER, "false"); // NOI18N
        map.put(JAVAC_SOURCES, "1.4"); // NOI18N
        map.put(NBM_IS_GLOBAL, "false"); // NOI18N
        map.put(NBM_NEEDS_RESTART, "false"); // NOI18N
        DEFAULTS = Collections.unmodifiableMap(map);
    }
    
    // helpers for storing and retrieving real values currently stored on the disk
    private NbModuleTypeProvider.NbModuleType moduleType;
    private SuiteProvider suiteProvider;
    private ProjectXMLManager projectXMLManager;
    private final LocalizedBundleInfo.Provider bundleInfoProvider;
    private LocalizedBundleInfo bundleInfo;
    
    // keeps current state of the user changes
    private String majorReleaseVersion;
    private String specificationVersion;
    private String implementationVersion;
    private String provTokensString;
    private SortedSet requiredTokens;
    private NbPlatform activePlatform;
    private NbPlatform originalPlatform;
    
    /** package name / selected */
    private SortedSet/*<String>*/ availablePublicPackages;
    
    private String[] allTokens;
    
    /** Unmodifiable sorted set of all categories in the module's universe. */
    private SortedSet/*<String>*/ modCategories;
    
    /** Unmodifiable sorted set of all dependencies in the module's universe. */
    private SortedSet/*<ModuleDependency>*/ universeDependencies;
    
    // models
    private PublicPackagesTableModel publicPackagesModel;
    private DependencyListModel dependencyListModel;
    private FriendListModel friendListModel;
    private RequiredTokenListModel requiredTokensListModel;
    
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    public static final String DEPENDENCIES_PROPERTY = "moduleDependencies"; // NOI18N
    
    /**
     * Creates a new instance of SingleModuleProperties
     */
    SingleModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator,
            SuiteProvider sp, NbModuleTypeProvider.NbModuleType moduleType,
            LocalizedBundleInfo.Provider bundleInfoProvider) {
        super(helper, evaluator);
        this.bundleInfoProvider = bundleInfoProvider;
        refresh(moduleType, sp);
    }
    
    protected void refresh(NbModuleTypeProvider.NbModuleType moduleType,
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
        originalPlatform = activePlatform = NbPlatform.getPlatformByID(
                getEvaluator().getProperty("nbplatform.active")); // NOI18N
        getPublicPackagesModel().reloadData(loadPublicPackages());
        requiredTokens = Collections.unmodifiableSortedSet(
                new TreeSet(Arrays.asList(manifestManager.getRequiredTokens())));
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
    
    Map/*<String, String>*/ getDefaultValues() {
        return DEFAULTS;
    }
    
    LocalizedBundleInfo getBundleInfo() {
        return bundleInfo;
    }
    
    // ---- READ ONLY start
    
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
            suite = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(getSuiteDirectory()));
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
        if (moduleType != NbModuleTypeProvider.NETBEANS_ORG
                && (activePlatform == null || !NbPlatform.getPlatforms().contains(activePlatform))) {
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
    
    String getMajorReleaseVersion() {
        return majorReleaseVersion;
    }
    
    void setMajorReleaseVersion(String ver) {
        if (majorReleaseVersion != ver) {
            majorReleaseVersion = ver;
            majorReleaseVersionChanged = true;
        }
    }
    
    String getSpecificationVersion() {
        return specificationVersion;
    }
    
    void setSpecificationVersion(String ver) {
        if (specificationVersion != ver) {
            specificationVersion = ver;
            specificationVersionChanged = true;
        }
    }
    
    String getImplementationVersion() {
        return implementationVersion;
    }
    
    void setImplementationVersion(String ver) {
        if (implementationVersion != ver) {
            implementationVersion = ver;
            implementationVersionChange = true;
        }
    }
    
    String getProvidedTokens() {
        return provTokensString;
    }
    
    void setProvidedTokens(String tokens) {
        if (provTokensString != tokens) {
            provTokensString = tokens;
            providedTokensChanged = true;
        }
    }
    
    boolean isStandalone() {
        return moduleType == NbModuleTypeProvider.STANDALONE;
    }
    
    boolean isNetBeansOrg() {
        return moduleType == NbModuleTypeProvider.NETBEANS_ORG;
    }
    
    boolean isSuiteComponent() {
        return moduleType == NbModuleTypeProvider.SUITE_COMPONENT;
    }
    
    boolean dependingOnImplDependency() {
        Set/*<ModuleDependency>*/ deps = getDependenciesListModel().getDependencies();
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
            projectXMLManager = new ProjectXMLManager(getHelper());
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
                            getProjectXMLManager().getDirectDependencies(getActivePlatform()));
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
                    dependencyListModel = ComponentFactory.getInvalidDependencyListModel();
                }
            } else {
                dependencyListModel = ComponentFactory.getInvalidDependencyListModel();
            }
        }
        return dependencyListModel;
    }
    
    /**
     * Returns a set of available modules dependencies ({@link
     * ModuleDependency}) in the module's universe according to the currently
     * selected platform ({@link #getActivePlatform()}). If the
     * <code>filterExcludedModules</code> is set to <code>true</code> and this
     * module is a suite component, modules exclude from the suite's module
     * list will be excluded from the returned set.<p>
     * <strong>Note:</strong> Don't call this method from EDT, since it may be
     * really slow. The {@link AssertionError} will be thrown if you try to do
     * so.
     */
    SortedSet/*<ModuleDependency>*/ getUniverseDependencies(final boolean filterExcludedModules) {
        assert !SwingUtilities.isEventDispatchThread() :
            "SingleModuleProperties.getUniverseDependencies() cannot be called from EDT"; // NOI18N
        if (universeDependencies == null) {
            reloadModuleListInfo();
        }
        SortedSet/*<ModuleDependency>*/ result = null;
        if (filterExcludedModules && isSuiteComponent()) {
            SuiteProject suite = getSuite();
            String[] disableModules = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY);
            String[] disableClusters = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY);
            SortedSet/*<ModuleDependency>*/ filtered = new TreeSet(universeDependencies);
            for (Iterator it = filtered.iterator(); it.hasNext();) {
                ModuleDependency dep = (ModuleDependency) it.next();
                if (isExcluded(dep.getModuleEntry(), disableModules, disableClusters)) {
                    it.remove();
                }
            }
            result = Collections.unmodifiableSortedSet(filtered);
        }
        if (result == null) {
            result = universeDependencies;
        }
        return result;
    }
    
    private static boolean isExcluded(final ModuleEntry me,
            final String[] disableModules, final String[] disableClusters) {
        return Arrays.binarySearch(disableModules, me.getCodeNameBase()) >= 0 ||
                Arrays.binarySearch(disableClusters, me.getClusterDirectory().getName()) >= 0;
    }
    
    /**
     * Returns sorted arrays of CNBs of available friends for this module.
     */
    String[] getAvailableFriends() {
        SortedSet set = new TreeSet();
        if (isSuiteComponent()) {
            SuiteProject suite = getSuite();
            SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
            Set/*<Project>*/ subModules = spp.getSubprojects();
            for (Iterator it = subModules.iterator(); it.hasNext();) {
                Project prj = (Project) it.next();
                String cnb = ProjectUtils.getInformation(prj).getName();
                if (!getCodeNameBase().equals(cnb)) {
                    set.add(cnb);
                }
            }
        } else if (isNetBeansOrg()) {
            SortedSet/*<ModuleDependency>*/ deps = getUniverseDependencies(false);
            for (Iterator it = deps.iterator(); it.hasNext();) {
                ModuleDependency dep = (ModuleDependency) it.next();
                set.add(dep.getModuleEntry().getCodeNameBase());
            }
        } // else standalone module - leave empty (see the UI spec)
        return (String[]) set.toArray(new String[0]);
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
                SortedSet/*<String>*/ provTokens = new TreeSet();
                provTokens.addAll(Arrays.asList(IDE_TOKENS));
                for (Iterator it = getModuleList().getAllEntries().iterator(); it.hasNext(); ) {
                    ModuleEntry me = (ModuleEntry) it.next();
                    provTokens.addAll(Arrays.asList(me.getProvidedTokens()));
                }
                String[] result = new String[provTokens.size()];
                return (String[]) provTokens.toArray(result);
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
    private Map/*<String, Boolean>*/ loadPublicPackages() {
        Collection/*<String>*/ selectedPackages = getSelectedPackages();
        Map/*<String, Boolean>*/ publicPackages = new TreeMap();
        for (Iterator it = getAvailablePublicPackages().iterator(); it.hasNext(); ) {
            String pkg = (String) it.next();
            publicPackages.put(pkg, Boolean.valueOf(selectedPackages.contains(pkg)));
        }
        return publicPackages;
    }
    
    private Collection getSelectedPackages() {
        Collection/*<String>*/ sPackages = new HashSet();
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
    private Set/*<String>*/ getAvailablePublicPackages() {
        if (availablePublicPackages == null) {
            availablePublicPackages = new TreeSet();
            
            // find all available public packages in a source root
            File srcDir = getHelper().resolveFile(getEvaluator().getProperty("src.dir")); // NOI18N
            SingleModuleProperties.addNonEmptyPackages(availablePublicPackages, 
                    FileUtil.toFileObject(srcDir), "java", false); // NOI18N
            
            // find all available public packages in classpath extensions
            String[] libsPaths = getProjectXMLManager().getBinaryOrigins();
            for (int i = 0; i < libsPaths.length; i++) {
                addNonEmptyPackagesFromJar(availablePublicPackages, getHelper().resolveFile(libsPaths[i]));
            }
        }
        return availablePublicPackages;
    }
    
    void storeProperties() throws IOException {
        super.storeProperties();
        
        // Store chnages in manifest
        storeManifestChanges();
        
        // store localized info
        if (bundleInfo != null) {
            bundleInfo.store();
        } // XXX else ignore for now but we could save into some default location
        
        // Store project.xml changes
        // store module dependencies
        DependencyListModel dependencyListModel = getDependenciesListModel();
        if (dependencyListModel.isChanged()) {
            Set/*<ModuleDependency>*/ depsToSave =
                    new TreeSet(ModuleDependency.CODE_NAME_BASE_COMPARATOR);
            depsToSave.addAll(dependencyListModel.getDependencies());
            
            // process removed modules
            depsToSave.removeAll(dependencyListModel.getRemovedDependencies());
            
            // process added modules
            depsToSave.addAll(dependencyListModel.getAddedDependencies());
            
            // process edited modules
            Map/*<ModuleDependency, ModuleDependency>*/ toEdit
                    = dependencyListModel.getEditedDependencies();
            for (Iterator it = toEdit.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                depsToSave.remove(entry.getKey());
                depsToSave.add(entry.getValue());
            }
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
        }
        
        if (isStandalone()) {
            ModuleProperties.storePlatform(getHelper(), getActivePlatform());
        }
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
        String module = "".equals(getMajorReleaseVersion()) ?
            getCodeNameBase() :
            getCodeNameBase() + '/' + getMajorReleaseVersion();
        if (majorReleaseVersionChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE, module);
        }
        if (specificationVersionChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION,
                    getSpecificationVersion());
        }
        if (implementationVersionChange) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION,
                    getImplementationVersion());
        }
        if (providedTokensChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_PROVIDES,
                    getProvidedTokens());
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
        }
        Util.storeManifest(manifestFO, em);
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
                SortedSet/*<String>*/ allCategories = new TreeSet(Collator.getInstance());
                SortedSet/*<ModuleDependency>*/ allDependencies = new TreeSet();
                for (Iterator it = getModuleList().getAllEntries().iterator(); it.hasNext(); ) {
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
                universeDependencies = Collections.unmodifiableSortedSet(allDependencies);
                return true;
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        return false;
    }

    private void addNonEmptyPackagesFromJar(Set/*<String>*/ pkgs, File jarFile) {
        if (!jarFile.isFile()) {
            // Broken classpath entry, perhaps.
            return;
        }
        try {
            JarFileSystem jfs = new JarFileSystem();
            jfs.setJarFile(jarFile);
            SingleModuleProperties.addNonEmptyPackages(pkgs, jfs.getRoot(), "class", true); // NOI18N
        } catch (PropertyVetoException pve) {
            ErrorManager.getDefault().notify(pve);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    /**
     * Goes recursively through all folders in the given <code>root</code> and
     * add every directory/package, which contains at least one file with the
     * given extension (probably class or java), into the given
     * <code>pkgs</code> set. Added entries are in the form of regular java
     * package (x.y.z) <code>isJarRoot</code> specifies if a given root is root
     * of a jar file.
     */
    static void addNonEmptyPackages(Set/*<String>*/ pkgs, FileObject root, String ext, boolean isJarRoot) {
        if (root == null) {
            return;
        }
        for (Enumeration en1 = root.getFolders(true); en1.hasMoreElements(); ) {
            FileObject subDir = (FileObject) en1.nextElement();
            for (Enumeration en2 = subDir.getData(false); en2.hasMoreElements(); ) {
                FileObject kid = (FileObject) en2.nextElement();
                if (kid.hasExt(ext) && Utilities.isJavaIdentifier(kid.getName())) {
                    String pkg = isJarRoot ? subDir.getPath() :
                        PropertyUtils.relativizeFile(
                            FileUtil.toFile(root),
                            FileUtil.toFile(subDir));
                    pkgs.add(pkg.replace('/', '.'));
                    break;
                }
            }
        }
    }
    
    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage. <strong>Package-private only for unit tests.</strong>
     */
    ModuleList getModuleList() throws IOException {
        if (getActivePlatform() != this.originalPlatform) {
            return ModuleList.getModuleList(
                    FileUtil.toFile(getHelper().getProjectDirectory()), getActivePlatform().getDestDir());
        } else {
            return ModuleList.getModuleList(FileUtil.toFile(getHelper().getProjectDirectory()));
        }
    }
}
