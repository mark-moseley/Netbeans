/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
final class NbModuleProject implements Project {
    
    private static final Icon NB_PROJECT_ICON = new ImageIcon(Utilities.loadImage( "org/netbeans/modules/apisupport/project/resources/module.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private final ModuleList moduleList;
    private Map/*<String,String>*/ evalPredefs;
    private List/*<Map<String,String>>*/ evalDefs;
    
    NbModuleProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + getProjectDirectory() + " has no defined <code-name-base>"); // NOI18N
        }
        File nbroot = helper.resolveFile(getNbrootRel());
        moduleList = ModuleList.getModuleList(nbroot);
        eval = createEvaluator();
        FileBuiltQueryImplementation fileBuilt;
        if (supportsUnitTests()) {
            fileBuilt = helper.createGlobFileBuiltQuery(eval, new String[] {
                "${src.dir}/*.java", // NOI18N
                "${test.unit.src.dir}/*.java", // NOI18N
            }, new String[] {
                "${build.classes.dir}/*.class", // NOI18N
                "${build.test.unit.classes.dir}/*.class", // NOI18N
            });
        } else {
            fileBuilt = helper.createGlobFileBuiltQuery(eval, new String[] {
                "${src.dir}/*.java", // NOI18N
            }, new String[] {
                "${build.classes.dir}/*.class", // NOI18N
            });
        }
        SourcesHelper sourcesHelper = new SourcesHelper(helper, eval);
        // External source roots are not supported, so don't bother.
        // Temp build dir is also internal; NBM build products go elsewhere, but
        // difficult to predict statically exactly what they are!
        // XXX would be good to mark at least the module JAR as owned by this project
        // (currently FOQ/SH do not support that)
        // XXX I18N
        sourcesHelper.addTypedSourceRoot("${src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, "Source Packages", /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${test.unit.src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, "Unit Test Packages", /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${test.qa-functional.src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, "Functional Test Packages", /*XXX*/null, null);
        if (helper.resolveFileObject("javahelp/manifest.mf") == null) { // NOI18N
            // Special hack for core - ignore core/javahelp
            sourcesHelper.addTypedSourceRoot("javahelp", "javahelp", "JavaHelp Packages", null, null);
        }
        lookup = Lookups.fixed(new Object[] {
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            //new SavedHook(),
            new OpenedHook(),
            new Actions(this),
            new ClassPathProviderImpl(this),
            new SourceForBinaryImpl(this),
            new JavadocForBinaryQueryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new LogicalView(this),
            new SubprojectProviderImpl(this),
            fileBuilt,
            new AccessibilityQueryImpl(this),
            new SourceLevelQueryImpl(this, evaluator()),
            helper.createSharabilityQuery(eval, new String[0], new String[] {
                // currently these are hardcoded
                "build", // NOI18N
                "javadoc", // NOI18N
            }),
            sourcesHelper.createSources(),
            // XXX need, in rough descending order of importance:
            // AntArtifactProvider - should it run netbeans target, or all-foo/bar?
            // CustomizerProvider - ???
        });
    }
    
    public String toString() {
        return "NbModuleProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public FileObject getManifestFile() {
        return helper.resolveFileObject(eval.getProperty("manifest.mf")); // NOI18N
    }
    
    private Manifest getManifest() {
        FileObject manifestFO = getManifestFile();
        if (manifestFO != null) {
            try {
                InputStream is = manifestFO.getInputStream();
                try {
                    return new Manifest(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
    public AntProjectHelper getHelper() {
        return helper;
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     */
    private PropertyEvaluator createEvaluator() {
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        Map/*<String,String>*/ stock = new HashMap();
        stock.put("nb_all", getNbrootRel()); // NOI18N
        ModuleList ml = getModuleList();
        Iterator it = ml.getAllEntries().iterator();
        while (it.hasNext()) {
            ModuleList.Entry e = (ModuleList.Entry)it.next();
            stock.put(e.getPath() + ".dir", e.getClusterDirectory().getAbsolutePath()); // NOI18N
        }
        stock.putAll(ml.getDirectoriesProperties());
        stock.put("netbeans.dest.dir", "${nb_all}/nbbuild/netbeans"); // NOI18N
        ModuleList.Entry thisEntry = ml.getEntry(getCodeNameBase());
        if (thisEntry != null) {
            stock.put("cluster.dir", thisEntry.getCluster()); // NOI18N
        }
        Map/*<String,String>*/ defaults = new HashMap();
        defaults.put("code.name.base.dashes", getCodeNameBase().replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "${nb.modules.dir}"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.classes.dir", "build/classes"); // NOI18N
        defaults.put("test.unit.src.dir", "test/unit/src"); // NOI18N
        defaults.put("test.qa-functional.src.dir", "test/qa-functional/src"); // NOI18N
        defaults.put("build.test.unit.classes.dir", "build/test/unit/classes"); // NOI18N
        defaults.put("module.classpath", computeModuleClasspath());
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return PropertyUtils.sequentialPropertyEvaluator(predefs, new PropertyProvider[] {
            PropertyUtils.fixedPropertyProvider(stock),
            helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            PropertyUtils.fixedPropertyProvider(defaults),
        });
    }
    
    /**
     * Should be similar to impl in ParseProjectXml.
     */
    private String computeModuleClasspath() {
        Element data = getHelper().getPrimaryConfigurationData(true);
        Element moduleDependencies = Util.findElement(data,
            "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        Iterator it = deps.iterator();
        StringBuffer cp = new StringBuffer();
        ModuleList ml = getModuleList();
        File nbroot = getHelper().resolveFile(getNbrootRel());
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            if (Util.findElement(dep, "compile-dependency", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED) == null) {
                continue;
            }
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleList.Entry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + this);
                continue;
            }
            // XXX if that module is projectized, check its public packages;
            // if it has none, skip it and issue a warning, unless we are
            // declaring an impl dependency
            File moduleJar = module.getJarLocation();
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            cp.append(moduleJar.getAbsolutePath());
            /* Really necessary? Probably not.
            File[] classPathExtensions = module.getClassPathExtensions(nbroot);
            for (int i = 0; i < classPathExtensions.length; i++) {
                cp.append(File.pathSeparatorChar);
                cp.append(classPathExtensions[i].getAbsolutePath());
            }
             */
        }
        return cp.toString();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final Map/*<String,FileObject>*/ directoryCache = new WeakHashMap();
    
    private FileObject getDir(String prop) {
        // XXX also add a PropertyChangeListener to eval and clear the cache of changed props
        if (directoryCache.containsKey(prop)) {
            return (FileObject)directoryCache.get(prop);
        } else {
            String v = evaluator().getProperty(prop);
            assert v != null : "No value for " + prop;
            FileObject f = helper.resolveFileObject(v);
            directoryCache.put(prop, f);
            return f;
        }
    }

    public FileObject getSourceDirectory() {
        return getDir("src.dir"); // NOI18N
    }
    
    public FileObject getTestSourceDirectory() {
        return getDir("test.unit.src.dir"); // NOI18N
    }
    
    public FileObject getFunctionalTestSourceDirectory() {
        return getDir("test.qa-functional.src.dir"); // NOI18N
    }
    
    public File getClassesDirectory() {
        String classesDir = eval.getProperty("build.classes.dir"); // NOI18N
        return helper.resolveFile(classesDir);
    }
    
    public File getTestClassesDirectory() {
        String testClassesDir = eval.getProperty("build.test.unit.classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir);
    }
    
    public FileObject getJavaHelpDirectory() {
        if (helper.resolveFileObject("javahelp/manifest.mf") != null) { // NOI18N
            // Special hack for core.
            return null;
        }
        return helper.resolveFileObject("javahelp"); // NOI18N
    }
    
    public File getModuleJarLocation() {
        return helper.resolveFile(eval.evaluate("${netbeans.dest.dir}/${cluster.dir}/${module.jar}")); // NOI18N
    }
    
    public URL getModuleJavadocDirectoryURL() {
        String moduleJavadoc = "javadoc/" + eval.getProperty("javadoc.name"); // NOI18N
        File f = helper.resolveFile(moduleJavadoc);
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    public String getCodeNameBase() {
        Element config = helper.getPrimaryConfigurationData(true);
        Element cnb = Util.findElement(config, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (cnb != null) {
            return Util.findText(cnb);
        } else {
            return null;
        }
    }
    
    public String getPath() {
        Element config = helper.getPrimaryConfigurationData(true);
        Element path = Util.findElement(config, "path", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (path != null) {
            return Util.findText(path);
        } else {
            return null;
        }
    }
    
    private String getNbrootRel() {
        String path = getPath();
        if (path != null) {
            return path.replaceAll("[^/]+", ".."); // NOI18N
        }
        Util.err.log(ErrorManager.WARNING, "Could not compute relative path to nb_all for " + this);
        return ".."; // NOI18N
    }
    
    public FileObject getNbroot() {
        String nbrootRel = getNbrootRel();
        FileObject nbroot = getHelper().resolveFileObject(nbrootRel);
        if (nbroot == null) {
            Util.err.log(ErrorManager.WARNING, "Warning - cannot find nb_all for " + this);
        }
        return nbroot;
    }
    
    public FileObject getNbrootFile(String path) {
        FileObject nbroot = getNbroot();
        if (nbroot != null) {
            return nbroot.getFileObject(path);
        } else {
            return null;
        }
    }
    
    public ModuleList getModuleList() {
        return moduleList;
    }
    
    public FileObject getArchXml() {
        String loc = eval.getProperty("javadoc.arch"); // NOI18N
        if (loc != null) {
            return helper.resolveFileObject(loc);
        } else {
            return null;
        }
    }
    
    public boolean supportsJavadoc() {
        return supportsFeature("javadoc"); // NOI18N
    }
    
    public boolean supportsUnitTests() {
        return getTestSourceDirectory() != null;
    }
    
    private boolean supportsFeature(String name) {
        Element config = helper.getPrimaryConfigurationData(true);
        NodeList nl = config.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, name);
        int length = nl.getLength();
        assert length < 2;
        return length == 1;
    }
    
    private final class Info implements ProjectInformation {
        
        private String displayName;
        
        Info() {}
        
        public String getName() {
            return getCodeNameBase();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                Manifest mf = getManifest();
                if (mf != null) {
                    String locBundleResource = mf.getMainAttributes().
                    getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
                    if (locBundleResource != null) {
                        String locBundleResourceBase, locBundleResourceExt;
                        int idx = locBundleResource.lastIndexOf('.');
                        if (idx != -1 && idx > locBundleResource.lastIndexOf('/')) {
                            locBundleResourceBase = locBundleResource.substring(0, idx);
                            locBundleResourceExt = locBundleResource.substring(idx);
                        } else {
                            locBundleResourceBase = locBundleResource;
                            locBundleResourceExt = "";
                        }
                        FileObject srcFO = getSourceDirectory();
                        if (srcFO != null) {
                            Iterator it = NbBundle.getLocalizingSuffixes();
                            while (it.hasNext()) {
                                String suffix = (String)it.next();
                                String resource = locBundleResourceBase + suffix +
                                locBundleResourceExt;
                                FileObject bundleFO = srcFO.getFileObject(resource);
                                if (bundleFO != null) {
                                    Properties p = new Properties();
                                    try {
                                        InputStream is = bundleFO.getInputStream();
                                        try {
                                            p.load(is);
                                        } finally {
                                            is.close();
                                        }
                                        displayName = p.getProperty("OpenIDE-Module-Name"); // NOI18N
                                        if (displayName != null) {
                                            break;
                                        }
                                    } catch (IOException e) {
                                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (displayName == null) {
                displayName = getName();
                if (displayName.equals("org.netbeans")) { // NOI18N
                    // Special case.
                    displayName = "Core Bootstrap";
                }
            }
            return displayName;
        }
        
        public Icon getIcon() {
            return NB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return NbModuleProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
    private final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
            // register project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(NbModuleProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            
            // XXX could discard caches, etc.
            
            // unregister project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
}
