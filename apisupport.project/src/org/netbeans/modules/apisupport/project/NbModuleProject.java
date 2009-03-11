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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.queries.ModuleProjectClassPathExtender;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.queries.AccessibilityQueryImpl;
import org.netbeans.modules.apisupport.project.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.apisupport.project.queries.AntArtifactProviderImpl;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImpl;
import org.netbeans.modules.apisupport.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.JavadocForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SubprojectProviderImpl;
import org.netbeans.modules.apisupport.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.ui.ModuleActions;
import org.netbeans.modules.apisupport.project.ui.ModuleLogicalView;
import org.netbeans.modules.apisupport.project.ui.ModuleOperations;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
@AntBasedProjectRegistration(
    type=NbModuleProjectType.TYPE,
    iconResource="org/netbeans/modules/apisupport/project/resources/module.png", // NOI18N
    sharedName=NbModuleProjectType.NAME_SHARED,
    sharedNamespace= NbModuleProjectType.NAMESPACE_SHARED,
    privateName=NbModuleProjectType.NAME_PRIVATE,
    privateNamespace= NbModuleProjectType.NAMESPACE_PRIVATE
)
public final class NbModuleProject implements Project {
    
    public static final String NB_PROJECT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/resources/module.png"; // NOI18N
    
    private static final Icon NB_PROJECT_ICON = ImageUtilities.loadImageIcon(NB_PROJECT_ICON_PATH, false);
    
    public static final String SOURCES_TYPE_JAVAHELP = "javahelp"; // NOI18N
    static final String[] COMMON_TEST_TYPES = {"unit", "qa-functional"}; // NOI18N
    
    private final AntProjectHelper helper;
    private final Evaluator eval;
    private final Lookup lookup;
    private Map<FileObject,Element> extraCompilationUnits;
    private final GeneratedFilesHelper genFilesHelper;
    private final NbModuleProviderImpl typeProvider;
    
    public NbModuleProject(AntProjectHelper helper) throws IOException {
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        for (int v = 4; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/nb-module-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(NbModuleProject.class, "NbModuleProject.too_new", FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " has no defined <code-name-base>"); // NOI18N
        }
        typeProvider = new NbModuleProviderImpl();
        if (typeProvider.getModuleType() == NbModuleProvider.NETBEANS_ORG && ModuleList.findNetBeansOrg(getProjectDirectoryFile()) == null) {
            // #69097: preferable to throwing an assertion error later...
            throw new IOException("netbeans.org-type module requires at least nbbuild: " + FileUtil.getFileDisplayName(helper.getProjectDirectory())); // NOI18N
        }
        eval = new Evaluator(this, typeProvider);
        // XXX could add globs for other package roots too
        List<String> from = new ArrayList<String>();
        List<String> to = new ArrayList<String>();
        from.add("${src.dir}/*.java"); // NOI18N
        to.add("${build.classes.dir}/*.class"); // NOI18N
        for (String type : supportedTestTypes()) {
            from.add("${test." + type + ".src.dir}/*.java"); // NOI18N
            to.add("${build.test." + type + ".classes.dir}/*.class"); // NOI18N
        }
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(
                eval,from.toArray(new String[0]), to.toArray(new String[0]));
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, eval);
        // Temp build dir is always internal; NBM build products go elsewhere, but
        // difficult to predict statically exactly what they are!
        // XXX would be good to mark at least the module JAR as owned by this project
        // (currently FOQ/SH do not support that)
        sourcesHelper.addPrincipalSourceRoot("${src.dir}", NbBundle.getMessage(NbModuleProject.class, "LBL_source_packages"), null, null); // #56457
        for (String type : supportedTestTypes()) {
            sourcesHelper.addPrincipalSourceRoot("${test." + type + ".src.dir}", NbBundle.getMessage(NbModuleProject.class, "LBL_" + type + "_test_packages"), null, null); // #68727
        }
        sourcesHelper.addTypedSourceRoot("${src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_source_packages"), null, null);
        // XXX other principal source roots, as needed...
        for (String type : supportedTestTypes()) {
            sourcesHelper.addTypedSourceRoot("${test." + type + ".src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_" + type + "_test_packages"), null, null);
        }
        if (helper.resolveFileObject("javahelp/manifest.mf") == null) { // NOI18N
            // Special hack for core - ignore core/javahelp
            sourcesHelper.addTypedSourceRoot("javahelp", SOURCES_TYPE_JAVAHELP, NbBundle.getMessage(NbModuleProject.class, "LBL_javahelp_packages"), null, null);
        }
        for (Map.Entry<FileObject,Element> entry : getExtraCompilationUnits().entrySet()) {
            Element pkgrootEl = Util.findElement(entry.getValue(), "package-root", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            String pkgrootS = Util.findText(pkgrootEl);
            sourcesHelper.addTypedSourceRoot(pkgrootS, JavaProjectConstants.SOURCES_TYPE_JAVA,
                    /* XXX should schema incl. display name? */entry.getKey().getNameExt(), null, null);
        }
        // #56457: support external source roots too.
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        lookup = createLookup(new Info(), aux, helper, fileBuilt, sourcesHelper);
    }

    public @Override String toString() {
        return "NbModuleProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(ProjectInformation info, AuxiliaryConfiguration aux, AntProjectHelper helper, FileBuiltQueryImplementation fileBuilt, final SourcesHelper sourcesHelper) {
        Object[] basicContent = new Object[] {
            this,
            info,
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            new SavedHook(),
            UILookupMergerSupport.createProjectOpenHookMerger(new OpenedHook()),
            new ModuleActions(this),
            new ClassPathProviderImpl(this),
            new SourceForBinaryImpl(this),
            new JavadocForBinaryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new ModuleLogicalView(this),
            new SubprojectProviderImpl(this),
            fileBuilt,
            new AccessibilityQueryImpl(this),
            new SourceLevelQueryImpl(this),
            helper.createSharabilityQuery(evaluator(), new String[0], new String[] {
                // currently these are hardcoded
                "build", // NOI18N
            }),
            sourcesHelper.createSources(),
            new AntArtifactProviderImpl(this, helper, evaluator()),
            new CustomizerProviderImpl(this, getHelper(), evaluator()),
            typeProvider,
            new PrivilegedTemplatesImpl(),
            new ModuleProjectClassPathExtender(this),
            new LocalizedBundleInfoProvider(),
            new ModuleOperations(this),
            LookupProviderSupport.createSourcesMerger(),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            new TemplateAttributesProvider(getHelper(), getModuleType() == NbModuleType.NETBEANS_ORG),
            new FileEncodingQueryImpl()
        };
        Object[] lookupContent = basicContent;
        if (getModuleType() == NbModuleType.SUITE_COMPONENT) {
            lookupContent = new Object[basicContent.length + 1];
            System.arraycopy(basicContent, 0, lookupContent, 0, basicContent.length);
            lookupContent[basicContent.length] = new SuiteProviderImpl();
        }
        Lookup baseLookup = Lookups.fixed(lookupContent);
        return  LookupProviderSupport.createCompositeLookup(baseLookup, "Projects/org-netbeans-modules-apisupport-project/Lookup"); //NOI18N
    }



    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public File getProjectDirectoryFile() {
        return FileUtil.toFile(getProjectDirectory());
    }
    
    /**
     * Get the minimum harness version required to work with this module.
     */
    public int getMinimumHarnessVersion() {
        if (helper.createAuxiliaryConfiguration().getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true) != null) {
            return NbPlatform.HARNESS_VERSION_50;
        } else {
            return NbPlatform.HARNESS_VERSION_55u1;
        }
    }

    /**
     * Replacement for {@link AntProjectHelper#getPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public Element getPrimaryConfigurationData() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                Element data = ac.getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true);
                if (data != null) {
                    return Util.translateXML(data, NbModuleProjectType.NAMESPACE_SHARED);
                } else {
                    return helper.getPrimaryConfigurationData(true);
                }
            }
        });
    }

    /**
     * Replacement for {@link AntProjectHelper#putPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public void putPrimaryConfigurationData(final Element data) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                if (ac.getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true) != null) {
                    ac.putConfigurationFragment(Util.translateXML(data, NbModuleProjectType.NAMESPACE_SHARED_2), true);
                } else {
                    helper.putPrimaryConfigurationData(data, true);
                }
                return null;
            }
        });
    }

    /** Returns a relative path to a project's source directory. */
    public String getSourceDirectoryPath() {
        return evaluator().getProperty("src.dir"); // NOI18N
    }
    
    private NbModuleProvider.NbModuleType getModuleType() {
        Element data = getPrimaryConfigurationData();
        if (Util.findElement(data, "suite-component", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleProvider.SUITE_COMPONENT;
        } else if (Util.findElement(data, "standalone", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleProvider.STANDALONE;
        } else {
            return NbModuleProvider.NETBEANS_ORG;
        }
    }
    
    public FileObject getManifestFile() {
        return helper.resolveFileObject(evaluator().getProperty("manifest.mf")); // NOI18N
    }
    
    public Manifest getManifest() {
        return Util.getManifest(getManifestFile());
    }

    public AntProjectHelper getHelper() {
        return helper;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final Map<String,FileObject> directoryCache = new WeakHashMap<String,FileObject>();
    
    private FileObject getDir(String prop) {
        // XXX also add a PropertyChangeListener to eval and clear the cache of changed props
        if (directoryCache.containsKey(prop)) {
            return directoryCache.get(prop);
        } else {
            String v = evaluator().getProperty(prop);
            if (v == null)
                throw new NullPointerException("Property ${" + prop + "} returned null, probably undefined.");
            FileObject f = helper.resolveFileObject(v);
            directoryCache.put(prop, f);
            return f;
        }
    }

    public FileObject getSourceDirectory() {
        return getDir("src.dir"); // NOI18N
    }
    
    public FileObject getTestSourceDirectory(String type) {
        return getDir("test." + type + ".src.dir"); // NOI18N
    }
    
    public File getClassesDirectory() {
        String classesDir = evaluator().getProperty("build.classes.dir"); // NOI18N
        return classesDir != null ? helper.resolveFile(classesDir) : null;
    }
    
    public File getTestClassesDirectory(String type) {
        String testClassesDir = evaluator().getProperty("build.test." + type + ".classes.dir"); // NOI18N
        return testClassesDir != null ? helper.resolveFile(testClassesDir) : null;
    }
    
    public FileObject getJavaHelpDirectory() {
        if (helper.resolveFileObject("javahelp/manifest.mf") != null) { // NOI18N
            // Special hack for core.
            return null;
        }
        return helper.resolveFileObject("javahelp"); // NOI18N
    }
    
    public File getModuleJarLocation() {
        // XXX could use ModuleList here instead
        return helper.resolveFile(evaluator().evaluate("${cluster}/${module.jar}")); // NOI18N
    }
    
    public File getTestUserDirLockFile() {
        return getHelper().resolveFile(evaluator().evaluate("${test.user.dir}/lock"));
    }

    public String getCodeNameBase() {
        Element config = getPrimaryConfigurationData();
        Element cnb = Util.findElement(config, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (cnb != null) {
            return Util.findText(cnb);
        } else {
            return null;
        }
    }
    
    public String getSpecVersion() {
        //TODO shall we check for illegal cases like "none-defined" or "both-defined" here?
        Manifest m = getManifest();
        if (m != null) {
            String manVersion = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version"); //NOI18N
            if (manVersion != null) {
                return stripExcessZeros(manVersion);
            }
        }
        return stripExcessZeros(evaluator().getProperty(SingleModuleProperties.SPEC_VERSION_BASE));
    }
    private static String stripExcessZeros(String spec) { // #72826
        return spec != null ? spec.replaceAll("(\\.[0-9]+)\\.0$", "$1") : null; // NOI18N
    }
    
    /**
     * Slash-separated path inside netbeans.org sources, or null for external modules.
     */
    public String getPathWithinNetBeansOrg() {
        FileObject nbroot = getNbrootFileObject(null);
        if (nbroot != null) {
            return FileUtil.getRelativePath(nbroot, getProjectDirectory());
        } else {
            return null;
        }
    }
    
    private File getNbroot() {
        File dir = getProjectDirectoryFile();
        File nbroot = ModuleList.findNetBeansOrg(dir);
        if (nbroot != null) {
            return nbroot;
        } else {
            // OK, not it.
            NbPlatform platform = getPlatform();
            if (platform != null) {
                URL[] roots = platform.getSourceRoots();
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].getProtocol().equals("file")) { // NOI18N
                        File f = new File(URI.create(roots[i].toExternalForm()));
                        if (ModuleList.isNetBeansOrg(f)) {
                            return f;
                        }
                    }
                }
            }
            // Did not find it.
            return null;
        }
    }
    
    public File getNbrootFile(String path) {
        File nbroot = getNbroot();
        if (nbroot != null) {
            return new File(nbroot, path.replace('/', File.separatorChar));
        } else {
            return null;
        }
    }
    
    public FileObject getNbrootFileObject(String path) {
        File f = path != null ? getNbrootFile(path) : getNbroot();
        if (f != null) {
            return FileUtil.toFileObject(f);
        } else {
            return null;
        }
    }
    
    public ModuleList getModuleList() throws IOException {
        NbPlatform p = getPlatform(false);
        if (p == null || ! p.isValid()) {
            // #67148: have to use something... (and getEntry(codeNameBase) will certainly fail!)

            // TODO dealing with nonexistent platforms probably not complete / 100% correct yet,
            // see #61227; but project with unresolved platform may also load as result
            // of suite-chaining; perhaps resolve already in loadProject
            Util.err.log(ErrorManager.WARNING, "Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) // NOI18N
                    + " is missing its platform '" + evaluator().getProperty("nbplatform.active") + "', switching to default platform");    // NOI18N
            NbPlatform p2 = NbPlatform.getDefaultPlatform();
            return ModuleList.getModuleList(getProjectDirectoryFile(), p2 != null ? p2.getDestDir() : null);
        }
        ModuleList ml;
        try {
            ml = ModuleList.getModuleList(getProjectDirectoryFile(), p.getDestDir());
        } catch (IOException x) {
            // #69029: maybe invalidated platform? Try the default platform instead.
            Logger.getLogger(NbModuleProject.class.getName()).log(Level.FINE, null, x);
            NbPlatform p2 = NbPlatform.getDefaultPlatform();
            return ModuleList.getModuleList(getProjectDirectoryFile(), p2 != null ? p2.getDestDir() : null);
        }
        if (ml.getEntry(getCodeNameBase()) == null) {
            ModuleList.refresh();
            ml = ModuleList.getModuleList(getProjectDirectoryFile());
            if (ml.getEntry(getCodeNameBase()) == null) {
                // XXX try to give better diagnostics - as examples are discovered
                Util.err.log(ErrorManager.WARNING, "Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " does not appear to be listed in its own module list; some sort of misconfiguration (e.g. not listed in its own suite)"); // NOI18N
            }
        }
        return ml;
    }
    
    /**
     * Get the platform which this project is currently associated with.
     * @param fallback if true, fall back to the default platform if necessary
     * @return the current platform; or null if fallback is false and there is no
     *         platform specified, or an invalid platform is specified, or even if
     *         fallback is true but even the default platform is not available
     */
    public NbPlatform getPlatform(boolean fallback) {
        NbPlatform p = getPlatform();
        if (fallback && (p == null || !p.isValid())) {
            p = NbPlatform.getDefaultPlatform();
        }
        return p;
    }
    
    private NbPlatform getPlatform() {
        File file = getPlatformFile();
        if (file == null) {
            return null;
        }
        return NbPlatform.getPlatformByDestDir(file);
    }
    
    private File getPlatformFile() {
        String prop = evaluator().getProperty("netbeans.dest.dir"); // NOI18N
        if (prop == null) {
            return null;
        }
        return getHelper().resolveFile(prop);
    }

    /**
     * Check whether Javadoc generation is possible.
     */
    public boolean supportsJavadoc() {
        if (evaluator().getProperty("module.javadoc.packages") != null) {
            return true;
        }
        Element config = getPrimaryConfigurationData();
        Element pubPkgs = Util.findElement(config, "public-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (pubPkgs == null) {
            // Try <friend-packages> too.
            pubPkgs = Util.findElement(config, "friend-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        }
        return pubPkgs != null && !Util.findSubElements(pubPkgs).isEmpty();
    }
    
    public List<String> supportedTestTypes() {
        List<String> types = new ArrayList<String>();
        for (String type : COMMON_TEST_TYPES) {
            if (getTestSourceDirectory(type) != null && !Boolean.parseBoolean(evaluator().getProperty("disable." + type + ".tests"))) {
                types.add(type);
            }
        }
        // XXX could look for others in project.xml, in which case fix Evaluator to use that
        return types;
    }
    
    /**
     * Find marked extra compilation units.
     * Gives a map from the package root to the defining XML element.
     */
    public Map<FileObject,Element> getExtraCompilationUnits() {
        if (extraCompilationUnits == null) {
            extraCompilationUnits = new HashMap<FileObject,Element>();
            for (Element ecu : Util.findSubElements(getPrimaryConfigurationData())) {
                if (ecu.getLocalName().equals("extra-compilation-unit")) { // NOI18N
                    Element pkgrootEl = Util.findElement(ecu, "package-root", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                    String pkgrootS = Util.findText(pkgrootEl);
                    String pkgrootEval = evaluator().evaluate(pkgrootS);
                    FileObject pkgroot = getHelper().resolveFileObject(pkgrootEval);
                    if (pkgroot == null) {
                        Util.err.log(ErrorManager.WARNING, "Could not find package-root " + pkgrootEval + " for " + getCodeNameBase());
                        continue;
                    }
                    extraCompilationUnits.put(pkgroot, ecu);
                }
            }
        }
        return extraCompilationUnits;
    }
    
    /** Get the Java source level used for this module. Default is 1.4. */
    public String getJavacSource() {
        String javacSource = evaluator().getProperty(SingleModuleProperties.JAVAC_SOURCE);
        assert javacSource != null;
        return javacSource;
    }
    
    private ClassPath[] boot, source, compile;
    private final class OpenedHook extends ProjectOpenedHook {
        OpenedHook() {}
        protected void projectOpened() {
            open();
        }
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(NbModuleProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            // XXX could discard caches, etc.
            // unregister project's classpaths to GlobalClassPathRegistry
            assert boot != null && source != null && compile != null : "#46802: project being closed which was never opened?? " + NbModuleProject.this;
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, boot);
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, source);
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, compile);
            boot = null;
            source = null;
            compile = null;
        }
    }
    /**
     * Run the open hook.
     * For use from unit tests.
     */
    public void open() {
        // write user.properties.file=$userdir/build.properties to platform-private.properties
        if (getModuleType() == NbModuleProvider.STANDALONE) {
            // XXX skip this in case nbplatform.active is not defined
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                public Void run() {
                    String path = "nbproject/private/platform-private.properties"; // NOI18N
                    EditableProperties ep = getHelper().getProperties(path);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    getHelper().putProperties(path, ep);
                    try {
                        ProjectManager.getDefault().saveProject(NbModuleProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
        }
        // register project's classpaths to GlobalClassPathRegistry
        ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
        ClassPath[] _boot = cpProvider.getProjectClassPaths(ClassPath.BOOT);
        assert _boot != null : "No BOOT path";
        ClassPath[] _source = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        assert _source != null : "No SOURCE path";
        ClassPath[] _compile = cpProvider.getProjectClassPaths(ClassPath.COMPILE);
        assert _compile != null : "No COMPILE path";
        // Possible cause of #68414: do not change instance vars until after the dangerous stuff has been computed.
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, _boot);
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, _source);
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, _compile);
        boot = _boot;
        source = _source;
        compile = _compile;
        // refresh build.xml and build-impl.xml for external modules
        if (getModuleType() != NbModuleProvider.NETBEANS_ORG) {
            try {
                refreshBuildScripts(true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /**
     * <strong>For use from unit tests only.</strong> Returns {@link
     * LocalizedBundleInfo} for this project.
     */
    public LocalizedBundleInfo getBundleInfo() {
        return getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
    }
    
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        eval.setRunInAtomicAction(runInAtomicAction);
    }
    
    /** See issue #69440 for more details. */
    public boolean isRunInAtomicAction() {
        return eval.isRunInAtomicAction();
    }
    
    private final class Info implements ProjectInformation, PropertyChangeListener {
        
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        private String displayName;
        
        Info() {}
        
        public String getName() {
            String cnb = getCodeNameBase();
            return cnb != null ? cnb : /* #70490 */getProjectDirectory().toString();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                LocalizedBundleInfo bundleInfo = getBundleInfo();
                if (bundleInfo != null) {
                    displayName = bundleInfo.getDisplayName();
                }
            }
            if (/* #70490 */displayName == null) {
                displayName = getName();
            }
            assert displayName != null : NbModuleProject.this;
            return displayName;
        }
        
        private void setDisplayName(String newDisplayName) {
            String oldDisplayName = getDisplayName();
            displayName = newDisplayName == null ? getName() : newDisplayName;
            firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, displayName);
        }
        
        public Icon getIcon() {
            return NB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return NbModuleProject.this;
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

        public void propertyChange(PropertyChangeEvent evt) {
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                setDisplayName((String) evt.getNewValue());
            }
        }
        
    }
    
    public void notifyDeleting() {
        eval.removeListeners();
    }
        
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml for external modules
            if (getModuleType() != NbModuleProvider.NETBEANS_ORG) {
                refreshBuildScripts(false);
            }
        }
        
    }
    
    public void refreshBuildScripts(boolean checkForProjectXmlModified) throws IOException {
        refreshBuildScripts(checkForProjectXmlModified, getPlatform(true));
    }
    
    public void refreshBuildScripts(boolean checkForProjectXmlModified, NbPlatform customPlatform) throws IOException {
        String buildImplPath =
                    customPlatform.getHarnessVersion() <= NbPlatform.HARNESS_VERSION_65
                    || eval.getProperty(SuiteProperties.CLUSTER_PATH_PROPERTY) == null
                    ? "build-impl-65.xsl" : "build-impl.xsl";    // NOI18N
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                NbModuleProject.class.getResource("resources/" + buildImplPath), // NOI18N
                checkForProjectXmlModified);
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                NbModuleProject.class.getResource("resources/build.xsl"), // NOI18N
                checkForProjectXmlModified);
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {

        public File getSuiteDirectory() {
            String suiteDir = evaluator().getProperty("suite.dir"); // NOI18N
            return suiteDir == null ? null : helper.resolveFile(suiteDir);
        }

        public File getClusterDirectory() {
            return getModuleJarLocation().getParentFile().getParentFile().getAbsoluteFile();
        }
        
    }
    
    private class NbModuleProviderImpl implements NbModuleProvider, AntProjectListener {
        
        private NbModuleType type;
        
        public NbModuleProviderImpl() {
            getHelper().addAntProjectListener(this);
        }
        
        public NbModuleType getModuleType() {
            if (type == null) {
                type = NbModuleProject.this.getModuleType();
            }
            return type;
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
                type = null;
            }
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // do not need to react here, type is encoded in project.xml
        }
        public String getSpecVersion() {
            return NbModuleProject.this.getSpecVersion();
        }
        
        public String getCodeNameBase() {
            return NbModuleProject.this.getCodeNameBase();
        }
        
        public String getSourceDirectoryPath() {
            return NbModuleProject.this.getSourceDirectoryPath();
        }
        
        public FileObject getSourceDirectory() {
            return NbModuleProject.this.getSourceDirectory();
        }
        
        public FileObject getManifestFile() {
            return NbModuleProject.this.getManifestFile();
        }
        
        public String getResourceDirectoryPath(boolean inTests) {
            return evaluator().getProperty(inTests ? "test.unit.src.dir" : "src.dir");
        }
        
        public boolean addDependency(String codeNameBase, String releaseVersion,
                SpecificationVersion version,
                boolean useInCompiler) throws IOException {
            return Util.addDependency(NbModuleProject.this, codeNameBase, releaseVersion, version, useInCompiler);
        }
        
        public SpecificationVersion getDependencyVersion(String codenamebase) throws IOException {
            ModuleList moduleList = getModuleList();
            ModuleEntry entry = moduleList.getEntry(codenamebase); // NOI18N
            SpecificationVersion current = new SpecificationVersion(entry.getSpecificationVersion());
            return current;
            
        }
        
        public String getProjectFilePath() {
            return "nbproject/project.xml";
        }
        
        public File getActivePlatformLocation() {
            return NbModuleProject.this.getPlatformFile();
        }

        public File getModuleJarLocation() {
            return NbModuleProject.this.getModuleJarLocation();
        }
        
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            //"Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/JUnit/SimpleJUnitTest.java", // NOI18N
            "Templates/NetBeansModuleDevelopment/newAction", // NOI18N
            "Templates/NetBeansModuleDevelopment/emptyLibraryDescriptor", // NOI18N
            "Templates/NetBeansModuleDevelopment/newLoader", // NOI18N
            "Templates/NetBeansModuleDevelopment/newProject", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWindow", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWizard", // NOI18N
            //"Templates/Other/properties.properties", // NOI18N
        };
        static {
            assert PRIVILEGED_NAMES.length <= 10 : "Too many privileged templates to fit! extras will be ignored: " +
                    Arrays.asList(PRIVILEGED_NAMES).subList(10, PRIVILEGED_NAMES.length);
        }
        
        private static final String[] RECOMMENDED_TYPES = new String[] {         
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "junit",                // NOI18N                    
            "simple-files",         // NOI18N
            "nbm-specific",         // NOI18N
            "nbm-specific2",         // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }    

    private final class LocalizedBundleInfoProvider implements LocalizedBundleInfo.Provider {

        private LocalizedBundleInfo bundleInfo;

        public LocalizedBundleInfo getLocalizedBundleInfo() {
            if (bundleInfo == null) {
                Manifest mf = getManifest();
                FileObject srcFO = getSourceDirectory();
                if (mf != null && srcFO != null) {
                    bundleInfo = Util.findLocalizedBundleInfo(srcFO, getManifest());
                }
                if (bundleInfo != null) {
                    bundleInfo.addPropertyChangeListener(getLookup().lookup(Info.class));
                }
                if (mf != null) {
                    getManifestFile().addFileChangeListener(new FileChangeAdapter() {
                        public @Override void fileChanged(FileEvent fe) {
                            // cannot reload manifest-dependent things immediately (see 67961 for more details)
                            bundleInfo = null;
                        }
                    });
                }
            }
            return bundleInfo;
        }
    }

}
