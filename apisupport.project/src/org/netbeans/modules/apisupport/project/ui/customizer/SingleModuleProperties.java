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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.DependencyListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.FriendListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.RequiredTokenListModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;

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
    private SuiteProvider suiteProvider;
    private ProjectXMLManager projectXMLManager;
    private LocalizedBundleInfo bundleInfo;
    
    // keeps current state of the user changes
    private String majorReleaseVersion;
    private String specificationVersion;
    private String implementationVersion;
    private String provTokensString;
    private SortedSet requiredTokens;
    private boolean isStandalone;
    private NbPlatform platform;
    private NbPlatform originalPlatform;
    
    /** package name / selected */
    private SortedSet/*<String>*/ availablePublicPackages;
    
    private String[] allTokens;
    private SortedSet/*<String>*/ modCategories;
    private SortedSet/*<ModuleDependency>*/ universeDependencies;
    
    // models
    private PublicPackagesTableModel publicPackagesModel;
    private DependencyListModel dependencyListModel;
    private FriendListModel friendListModel;
    private RequiredTokenListModel requiredTokensListModel;
    
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    public static final String DEPENDENCIES_PROPERTY = "moduleDependencies"; // NOI18N
    public static final String PROPERTIES_REFRESHED = "propertiesRefreshed"; // NOI18N
    
    /**
     * Creates a new instance of SingleModuleProperties
     */
    SingleModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator,
            SuiteProvider sp, boolean isStandalone, LocalizedBundleInfo bundleInfo) {
        super(helper, evaluator);
        this.suiteProvider = sp;
        this.bundleInfo = bundleInfo;
        this.isStandalone = isStandalone;
        this.changeSupport = new PropertyChangeSupport(this);
        this.refresh();
    }
    
    protected void refresh() {
        reloadProperties();
        // reset
        availablePublicPackages = null;
        dependencyListModel = null;
        friendListModel = null;
        requiredTokensListModel = null;
        ManifestManager manifestManager = ManifestManager.getInstance(getManifestFile(), false);
        majorReleaseVersion = manifestManager.getReleaseVersion();
        specificationVersion = manifestManager.getSpecificationVersion();
        implementationVersion = manifestManager.getImplementationVersion();
        provTokensString = manifestManager.getProvidedTokensString();
        originalPlatform = this.platform = NbPlatform.getPlatformByID(
                getEvaluator().getProperty("nbplatform.active")); // NOI18N
        getPublicPackagesModel().reloadData(loadPublicPackages());
        this.requiredTokens = new TreeSet(
                Arrays.asList(manifestManager.getRequiredTokens()));
        if (isFileBundle()) {
            try {
                bundleInfo.reload();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        firePropertyChange(PROPERTIES_REFRESHED, null, null);
    }
    
    /** i.e. whether the bundle can be stored/reload etc. */
    private boolean isFileBundle() {
        return bundleInfo != null && bundleInfo.getPath() != null;
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
    
    String getSuiteDirectory() {
        return (suiteProvider != null && suiteProvider.getSuiteDirectory() != null) ?
            suiteProvider.getSuiteDirectory().getPath() : null;
    }
    
    // ---- READ ONLY end
    
    boolean isActivePlatformValid() {
        // check the platform for standalone and component modules since it is
        // needed for proper finding module dependencies. For NetBeans.org
        // module case plaf == null, which is OK
        NbPlatform plaf = getActivePlatform();
        return plaf == null || plaf.isValid();
    }
    
    NbPlatform getActivePlatform() {
        return platform;
    }
    
    void setActivePlatform(NbPlatform newPlaf) {
        if (this.platform != newPlaf) {
            NbPlatform oldPlaf = this.platform;
            this.platform = newPlaf;
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
        this.majorReleaseVersion = ver;
    }
    
    String getSpecificationVersion() {
        return specificationVersion;
    }
    
    void setSpecificationVersion(String ver) {
        this.specificationVersion = ver;
    }
    
    String getImplementationVersion() {
        return implementationVersion;
    }
    
    void setImplementationVersion(String ver) {
        this.implementationVersion = ver;
    }
    
    String getProvidedTokens() {
        return provTokensString;
    }
    
    void setProvidedTokens(String tokens) {
        this.provTokensString = tokens;
    }
    
    private SortedSet getRequiredTokens() {
        return requiredTokens;
    }
    
    boolean isStandalone() {
        return isStandalone;
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
     * Always returns fresh list model of all dependencies in the module's
     * universe regarding the currently selected platform with <code>
     * alreadyAdded</code> dependencies excluded.
     */
    DependencyListModel getUniverseDependenciesListModel(Collection/*<ModuleDependency>*/ alreadyAdded) {
        // when you get here platform is always valid
        if (universeDependencies == null) {
            reloadModuleListInfo();
        }
        SortedSet set = new TreeSet(universeDependencies);
        set.removeAll(alreadyAdded);
        return new DependencyListModel(set);
    }
    
    FriendListModel getFriendListModel() {
        if (friendListModel == null) {
            friendListModel = new FriendListModel(getProjectXMLManager().getFriends());
        }
        return friendListModel;
    }
    
    RequiredTokenListModel getRequiredTokenListModel() {
        if (requiredTokensListModel == null) {
            requiredTokensListModel = new RequiredTokenListModel(getRequiredTokens());
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
            addNonEmptyPackages(availablePublicPackages, FileUtil.toFileObject(srcDir), "java", false);
            
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
        if (isFileBundle()) {
            bundleInfo.store();
        } // XXX else ignore for now but we could save into some default location
        
        // Store project.xml changes
        // store module dependencies
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
        if (friends.length > 0) { // store friends packages
            if (getFriendListModel().isChanged()) {
                getProjectXMLManager().replaceFriendPackages(friends, publicPkgs);
            }
        } else { // store public packages
            if (getPublicPackagesModel().isChanged()) {
                getProjectXMLManager().replacePublicPackages(publicPkgs);
            }
        }
        
        if (isStandalone) {
            ModuleProperties.storePlatform(getHelper(), platform);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
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
        String module = "".equals(getMajorReleaseVersion()) ? // NOI18N
            getCodeNameBase() :
            getCodeNameBase() + "/" + getMajorReleaseVersion(); // NOI18N
        setManifestAttribute(em, ManifestManager.OPENIDE_MODULE, module);
        setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION,
                getSpecificationVersion());
        setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION,
                getImplementationVersion());
        setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_PROVIDES,
                getProvidedTokens());
        
        String[] reqTokens = new String[getRequiredTokens().size()];
        getRequiredTokens().toArray(reqTokens);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < reqTokens.length; i++) {
            if (i != 0) {
                result.append(", "); // NOI18N
            }
            result.append(reqTokens[i]);
        }
        setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_REQUIRES, result.toString());
        Util.storeManifest(manifestFO, em);
    }
    
    // XXX should be something similar provided be EditableManifest?
    private void setManifestAttribute(EditableManifest em, String key, String value) {
        assert value != null;
        if ("".equals(value)) { // NOI18N
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

    // XXX shareable model
    SortedSet getModuleCategories() {
        if (modCategories == null && !reloadModuleListInfo()) {
            return new TreeSet();
        }
        return modCategories;
    }
    
    /**
     * Prepare all ModuleDependencies from this module's universe. Also prepare
     * all categories.
     */
    boolean reloadModuleListInfo() {
        if (isActivePlatformValid()) {
            try {
                SortedSet/*<String>*/ allCategories = new TreeSet(Collator.getInstance());
                SortedSet/*<ModuleDependency>*/ allDependencies = new TreeSet();
                for (Iterator it = getModuleList().getAllEntries().iterator(); it.hasNext(); ) {
                    ModuleEntry me = (ModuleEntry) it.next();
                    allDependencies.add(new ModuleDependency(me));
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
            addNonEmptyPackages(pkgs, jfs.getRoot(), "class", true);
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
    private void addNonEmptyPackages(Set/*<String>*/ pkgs, FileObject root, String ext, boolean isJarRoot) {
        if (root == null) {
            return;
        }
        for (Enumeration en1 = root.getFolders(true); en1.hasMoreElements(); ) {
            FileObject subDir = (FileObject) en1.nextElement();
            for (Enumeration en2 = subDir.getData(false); en2.hasMoreElements(); ) {
                FileObject kid = (FileObject) en2.nextElement();
                if (kid.hasExt(ext)) { // NOI18N
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
     * instance manage.
     */
    private ModuleList getModuleList() throws IOException {
        if (getActivePlatform() != this.originalPlatform) {
            return ModuleList.getModuleList(
                    FileUtil.toFile(getHelper().getProjectDirectory()), getActivePlatform().getDestDir());
        } else {
            return ModuleList.getModuleList(FileUtil.toFile(getHelper().getProjectDirectory()));
        }
    }
}
