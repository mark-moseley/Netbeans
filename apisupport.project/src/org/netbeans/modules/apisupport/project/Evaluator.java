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

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
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
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

/**
 * Property evaluator for {@link NbModuleProject}.
 * Has two special behaviors of note:
 * 1. Does not call ModuleList until it really needs to.
 * 2. Is reset upon project.xml changes.
 * @author Jesse Glick, Martin Krauskopf
 */
final class Evaluator implements PropertyEvaluator, PropertyChangeListener, AntProjectListener {
    
    private final NbModuleProject project;
    private final NbModuleProvider typeProvider;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private PropertyEvaluator delegate;
    private boolean loadedModuleList = false;
    
    /** See issue #69440 for more details. */
    private boolean runInAtomicAction;
    
    private static class TestClasspath {
        
        private final String compile;
        private final String runtime;
        private final String testCompile;
        private final String testRuntime;
        
        public TestClasspath(String compile,String runtime,String testCompile,String testRuntime) {
            this.compile = compile;
            this.runtime = runtime;
            this.testCompile = testCompile;
            this.testRuntime = testRuntime;
        }
        
        public String getCompileClasspath() {
            return compile + ':' + testCompile;
        }
        
        public String getRuntimeClasspath() {
            return runtime + ':' + testRuntime;
        }
        
        private static TestClasspath getOrEmpty(Map testsCPs, String testtype) {
            TestClasspath tcp = (TestClasspath) testsCPs.get(testtype);
            if (tcp == null ) {
                // create with empty classpaths
                tcp = new TestClasspath("", "", "", ""); // NOI18N
            }
            return tcp;
        }
    }
    
    public Evaluator(NbModuleProject project, NbModuleProvider typeProvider) {
        this.project = project;
        this.typeProvider = typeProvider;
        delegate = createEvaluator(null);
        delegate.addPropertyChangeListener(this);
        project.getHelper().addAntProjectListener(this);
    }
    
    public String getProperty(String prop) {
        PropertyEvaluator eval = delegatingEvaluator(false);
        assert eval != this;
        String v = eval.getProperty(prop);
        if ((v == null && isModuleListDependentProperty(prop)) || isModuleListDependentValue(v)) {
            return delegatingEvaluator(true).getProperty(prop);
        } else {
            return v;
        }
    }
    
    public String evaluate(String text) {
        String v = delegatingEvaluator(false).evaluate(text);
        if (isModuleListDependentValue(v)) {
            return delegatingEvaluator(true).evaluate(text);
        } else {
            return v;
        }
    }
    
    public Map<String,String> getProperties() {
        return delegatingEvaluator(true).getProperties();
    }
    
    private boolean isModuleListDependentProperty(String p) {
        return p.equals("module.classpath") || // NOI18N
                p.equals("cp") || p.endsWith(".cp") || p.endsWith(".cp.extra") || // NOI18N
                p.equals("cluster") || // NOI18N
                // MODULENAME.dir, but not module.jar.dir or the like:
                (p.endsWith(".dir") && p.lastIndexOf('.', p.length() - 5) == -1); // NOI18N
    }
    
    private static final Pattern ANT_PROP_REGEX = Pattern.compile("\\$\\{([a-zA-Z0-9._-]+)\\}"); // NOI18N
    private boolean isModuleListDependentValue(String v) {
        if (v == null) {
            return false;
        }
        Matcher m = ANT_PROP_REGEX.matcher(v);
        while (m.find()) {
            if (isModuleListDependentProperty(m.group(1))) {
                return true;
            }
        }
        return false;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private PropertyEvaluator delegatingEvaluator(final boolean reset) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<PropertyEvaluator>() {
            public PropertyEvaluator run() {
                synchronized (Evaluator.this) {
                    if (reset && !loadedModuleList) {
                        reset();
                        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                            Util.err.log("Needed to reset evaluator in " + project + "due to use of module-list-dependent property; now cp=" + delegate.getProperty("cp"));
                        }
                    }
                    return delegate;
                }
            }
        });
    }
    
    private void reset() {
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            public Void run() {
                ModuleList moduleList;
                try {
                    moduleList = project.getModuleList();
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    // but leave old evaluator in place for now
                    return null;
                }
                synchronized (Evaluator.this) {
                    loadedModuleList = true;
                    delegate.removePropertyChangeListener(Evaluator.this);
                    delegate = createEvaluator(moduleList);
                    delegate.addPropertyChangeListener(Evaluator.this);
                    // XXX better to compute diff between previous and new values and fire just those
                    pcs.firePropertyChange(null, null, null);
                    return null;
                }
            }
        });
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("netbeans.dest.dir".equals(evt.getPropertyName()) || evt.getPropertyName() == null) {
            // Module list may have changed.
            reset();
        } else {
            Util.err.log("Refiring property change from delegate in " + evt.getPropertyName() + " for " + project);
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (!runInAtomicAction && ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            reset();
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        /* TODO: Not needed now? Put here at least some comment. */
    }
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        assert ProjectManager.mutex().isWriteAccess();
        this.runInAtomicAction = runInAtomicAction;
    }
    
    public void removeListeners() {
        project.getHelper().removeAntProjectListener(this);
        delegate.removePropertyChangeListener(this);
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     * @param ml this module list, or may be left null to skip all properties which require knowledge of other modules
     */
    private PropertyEvaluator createEvaluator(ModuleList ml) {
        // XXX a lot of this duplicates ModuleList.parseProperties... can they be shared?
        PropertyProvider predefs = project.getHelper().getStockPropertyPreprovider();
        Map<String,String> stock = new HashMap<String,String>();
        File dir = project.getProjectDirectoryFile();
        NbModuleProvider.NbModuleType type = typeProvider.getModuleType();
        File nbroot;
        if (type == NbModuleProvider.NETBEANS_ORG) {
            nbroot = ModuleList.findNetBeansOrg(dir);
            assert nbroot != null : "netbeans.org-type module not in a complete netbeans.org source root " + dir;
            stock.put("nb_all", nbroot.getAbsolutePath()); // NOI18N
            // Only needed for netbeans.org modules, since for external modules suite.properties suffices.
            stock.put("netbeans.dest.dir", new File(nbroot, ModuleList.DEST_DIR_IN_NETBEANS_ORG).getAbsolutePath()); // NOI18N
        } else {
            nbroot = null;
        }
        String codeNameBase = project.getCodeNameBase();
        if (ml != null) {
            // Register *.dir for nb.org modules. There is no equivalent for external modules.
            for (ModuleEntry e : ml.getAllEntriesSoft()) {
                String nborgPath = e.getNetBeansOrgPath();
                if (nborgPath != null) {
                    // #48449: intern these; number is (size of modules.xml) * (# of loaded module projects)
                    stock.put((nborgPath + ".dir").intern(), e.getClusterDirectory().getAbsolutePath().intern()); // NOI18N
                }
            }
            ModuleEntry thisEntry = ml.getEntry(codeNameBase);
            if (thisEntry != null) { // can be null e.g. for a broken suite component module
                assert nbroot == null ^ thisEntry.getNetBeansOrgPath() != null : thisEntry;
                File clusterDir = thisEntry.getClusterDirectory();
                stock.put("cluster", clusterDir.getAbsolutePath()); // NOI18N
            }
        }
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(PropertyUtils.fixedPropertyProvider(stock));
        // XXX should listen to changes in values of properties which refer to property files:
        if (type == NbModuleProvider.SUITE_COMPONENT) {
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/suite-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/suite.properties")); // NOI18N
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
            String suiteDirS = baseEval.getProperty("suite.dir"); // NOI18N
            if (suiteDirS != null) {
                File suiteDir = PropertyUtils.resolveFile(dir, suiteDirS);
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (type == NbModuleProvider.STANDALONE) {
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/platform.properties")); // NOI18N
        }
        if (type == NbModuleProvider.SUITE_COMPONENT || type == NbModuleProvider.STANDALONE) {
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
            providers.add(new Util.UserPropertiesFileProvider(baseEval, dir));
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
            class DestDirProvider extends Util.ComputedPropertyProvider {
                public DestDirProvider(PropertyEvaluator eval) {
                    super(eval);
                }
                protected Map<String,String> getProperties(Map<String,String> inputPropertyValues) {
                    String platformS = inputPropertyValues.get("nbplatform.active"); // NOI18N
                    if (platformS != null) {
                        return Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"); // NOI18N
                    } else {
                        return Collections.emptyMap();
                    }
                }
                protected Set<String> inputProperties() {
                    return Collections.singleton("nbplatform.active"); // NOI18N
                }
            }
            providers.add(new DestDirProvider(baseEval));
        }
        if (type == NbModuleProvider.NETBEANS_ORG) {
            // For local definitions of nbjdk.* properties:
            File nbbuild = new File(nbroot, "nbbuild"); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "user.build.properties"))); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "site.build.properties"))); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(System.getProperty("user.home"), ".nbbuild.properties"))); // NOI18N
        }
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
        providers.add(new NbJdkProvider(baseEval));
        providers.add(project.getHelper().getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(project.getHelper().getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map<String,String> defaults = new HashMap<String,String>();
        if (codeNameBase != null) { // #121856
            defaults.put("code.name.base.dashes", codeNameBase.replace('.', '-')); // NOI18N
        }
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.classes.dir", "build/classes"); // NOI18N
        defaults.put("javac.source", "1.4"); // NOI18N
        defaults.put("test.user.dir", new File(dir, "build/testuserdir").getAbsolutePath()); // NOI18N
        Set<String> testTypes = new HashSet<String>(Arrays.asList(NbModuleProject.COMMON_TEST_TYPES));
        // XXX would be good to add in any other types defined in project.xml
        for (String testType : testTypes) {
            defaults.put("test." + testType + ".src.dir", "test/" + testType + "/src"); // NOI18N
            defaults.put("build.test." + testType + ".classes.dir", "build/test/" + testType + "/classes"); // NOI18N
        }
        providers.add(PropertyUtils.fixedPropertyProvider(defaults));
        if (ml != null) {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("module.classpath", computeModuleClasspath(ml)))); // NOI18N
            Map<String,String> buildDefaults = new HashMap<String,String>();
            buildDefaults.put("cp.extra", ""); // NOI18N
            buildDefaults.put("cp", "${module.classpath}:${cp.extra}"); // NOI18N
            buildDefaults.put("run.cp", computeRuntimeModuleClasspath(ml) + ":${cp.extra}:${build.classes.dir}"); // NOI18N
            
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));

            Map<String,TestClasspath> testsCPs = computeTestingClassPaths(ml, baseEval, testTypes);
            testTypes.addAll(testsCPs.keySet());
            for (String testType : testTypes) {
                buildDefaults.put("test." + testType + ".cp.extra", ""); // NOI18N
                TestClasspath tcp = TestClasspath.getOrEmpty(testsCPs, testType);
                buildDefaults.put("test." + testType + ".cp", "${cp}:${cluster}/${module.jar}:${test." + testType + ".cp.extra}:" + tcp.getCompileClasspath()); // NOI18N
                buildDefaults.put("test." + testType + ".run.cp.extra", ""); // NOI18N
                buildDefaults.put("test." + testType + ".run.cp", "${test." + testType + ".cp}:${build.test." + testType + ".classes.dir}:${test." + testType + ".run.cp.extra}:" + tcp.getRuntimeClasspath()); // NOI18N
            }

            providers.add(PropertyUtils.fixedPropertyProvider(buildDefaults));
        }
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    private final class NbJdkProvider implements PropertyProvider, PropertyChangeListener { // #63541: JDK selection
        
        private final PropertyEvaluator eval;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final PropertyChangeListener weakListener = WeakListeners.propertyChange(this, null);
        
        public NbJdkProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(weakListener);
            JavaPlatformManager.getDefault().addPropertyChangeListener(weakListener);
        }
        
        public final Map<String,String> getProperties() {
            Map<String,String> props = new HashMap<String,String>();
            String home = eval.getProperty("nbjdk.home"); // NOI18N
            if (home == null) {
                String active = eval.getProperty("nbjdk.active"); // NOI18N
                if (active != null && !active.equals("default")) { // NOI18N
                    home = eval.getProperty("platforms." + active + ".home"); // NOI18N
                    if (home != null) {
                        props.put("nbjdk.home", home); // NOI18N
                    }
                }
            }
            if (home == null) {
                JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
                if (platform != null) {
                    Collection<FileObject> installs = platform.getInstallFolders();
                    if (installs.size() == 1) {
                        home = FileUtil.toFile(installs.iterator().next()).getAbsolutePath();
                    }
                }
            }
            String bootcp = null;
            if (home != null) {
                FileObject homeFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(home)));
                if (homeFO != null) {
                    for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                        if (new HashSet<FileObject>(platform.getInstallFolders()).equals(Collections.singleton(homeFO))) {
                            // Matching JDK is registered, so look up its real bootcp.
                            ClassPath boot = platform.getBootstrapLibraries();
                            boot.removePropertyChangeListener(weakListener);
                            boot.addPropertyChangeListener(weakListener);
                            bootcp = boot.toString(ClassPath.PathConversionMode.WARN);
                            break;
                        }
                    }
                }
                if (bootcp == null) {
                    if (Utilities.isMac()) {
                        bootcp = "${nbjdk.home}/../Classes/classes.jar";    //NOI18N
                    }
                    else {
                        bootcp = "${nbjdk.home}/jre/lib/rt.jar".replace('/', File.separatorChar); // NOI18N
                    }
                }
            }
            if (bootcp == null) {
                // Real fallback...
                bootcp = "${sun.boot.class.path}"; // NOI18N
            }
            props.put("nbjdk.bootclasspath", bootcp); // NOI18N
            if (home != null && !Utilities.isMac()) {   //On Mac everything is in classes.jar, there is no tools.jar
                props.put("tools.jar", home + "/lib/tools.jar".replace('/', File.separatorChar)); // NOI18N
            }
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Map<String,String> _props = new TreeMap<String,String>(eval.getProperties());
                Iterator<String> it = _props.keySet().iterator();
                while (it.hasNext()) {
                    String k = it.next();
                    if (!k.startsWith("nbjdk.") && !k.startsWith("platforms.")) { // NOI18N
                        it.remove();
                    }
                }
                _props.putAll(props);
                Util.err.log("JDK-related properties of " + project + ": " + _props);
            }
            return props;
        }
        
        public final void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public final void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        public final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !p.startsWith("nbjdk.") && !p.startsWith("platforms.") && // NOI18N
                    !p.equals(ClassPath.PROP_ENTRIES) && !p.equals(JavaPlatformManager.PROP_INSTALLED_PLATFORMS)) {
                return;
            }
            if (!changeSupport.hasListeners()) {
                return;
            }
            final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                public Void run() {
                    changeSupport.fireChange();
                    return null;
                }
            };
            // See ProjectProperties.PP.fireChange for explanation of this threading stuff:
            if (ProjectManager.mutex().isWriteAccess()) {
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                action.run();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
    }
    
    /**
     * Should be similar to impl in ParseProjectXml.
     */
    private String computeModuleClasspath(ModuleList ml) {
        Element data = project.getPrimaryConfigurationData();
        Element moduleDependencies = Util.findElement(data,
            "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        assert moduleDependencies != null : "Malformed metadata in " + project;
        StringBuffer cp = new StringBuffer();
        for (Element dep : Util.findSubElements(moduleDependencies)) {
            if (Util.findElement(dep, "compile-dependency", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED) == null) {
                continue;
            }
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleEntry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                continue;
            }
            File moduleJar = module.getJarLocation();
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            cp.append(moduleJar.getAbsolutePath());
            cp.append(module.getClassPathExtensions());
        }
        ModuleEntry myself = ml.getEntry(project.getCodeNameBase());
        if (myself == null) {
            // ???
            return "";
        }
        cp.append(myself.getClassPathExtensions());
        return cp.toString();
    }
    
    /**
     * Follows transitive runtime dependencies.
     * @see "issue #70206"
     */
    private String computeRuntimeModuleClasspath(ModuleList ml) {
        Set<String> unprocessed = new HashSet<String>();
        unprocessed.add(project.getCodeNameBase());
        Set<String> processed = new HashSet<String>();
        StringBuffer cp = new StringBuffer();
        while (!unprocessed.isEmpty()) { // crude breadth-first search
            Iterator<String> it = unprocessed.iterator();
            String cnb = it.next();
            it.remove();
            if (processed.add(cnb)) {
                ModuleEntry module = ml.getEntry(cnb);
                if (module == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                    continue;
                }
                if (!cnb.equals(project.getCodeNameBase())) { // build/classes for this is special
                    if (cp.length() > 0) {
                        cp.append(File.pathSeparatorChar);
                    }
                    cp.append(module.getJarLocation().getAbsolutePath());
                    cp.append(module.getClassPathExtensions());
                }
                String[] newDeps = module.getRunDependencies();
                unprocessed.addAll(Arrays.asList(newDeps));
            }
        }
        ModuleEntry myself = ml.getEntry(project.getCodeNameBase());
        if (myself != null) {
            // #76341: must include <class-path-extension>s in ${run.cp} too.
            cp.append(myself.getClassPathExtensions());
        }
        return cp.toString();
    }

    /**
     * Gives a map from test type (e.g. <em>unit</em> or <em>qa-functional</em>)
     * to the {@link TestClasspath test classpath} according to the content in
     * the project's metadata (<em>project.xml<em>).
     */
    private Map<String,TestClasspath> computeTestingClassPaths(ModuleList ml, PropertyEvaluator evaluator, Set<String> extraTestTypes) {
        Map<String, TestClasspath> classpaths = new HashMap<String,TestClasspath>();
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        Map<String, Set<TestModuleDependency>> testDependencies = pxm.getTestDependencies(ml);
        
        String testDistDir =  evaluator.getProperty("test.dist.dir"); // NOI18N
        if (testDistDir == null) {
            NbModuleProvider.NbModuleType type = typeProvider.getModuleType();
            if (type == NbModuleProvider.NETBEANS_ORG) {
                // test.dist.dir = ${nb_all}/nbbuild/build/testdist
                String nball = evaluator.getProperty("nb_all"); // NOI18N
                testDistDir = nball + File.separatorChar + "nbbuild" + File.separatorChar + "build" + File.separatorChar + "testdist"; // NOI18N
            } else if ( type == NbModuleProvider.SUITE_COMPONENT) {
                // test.dist.dir = ${suite.dir}/build/testdist
                String suiteDir = evaluator.getProperty("suite.dir"); // NOI18N
                testDistDir = suiteDir + File.separatorChar + "build" + File.separatorChar + "testdist"; // NOI18N
            } else {
                // standalone module
                // test.dist.dir = ${module.dir}/build/testdist
                String moduleDir = evaluator.getProperty("module.dir"); // NOI18N
                testDistDir = moduleDir + File.separatorChar + "build" + File.separatorChar + "testdist"; // NOI18N
            }
        }
        for (Map.Entry<String,Set<TestModuleDependency>> entry : testDependencies.entrySet()) {
            computeTestType(entry.getKey(), new File(testDistDir), entry.getValue(), classpaths, ml);
        }
        for (String testType : extraTestTypes) {
            if (!testDependencies.containsKey(testType)) {
                // No declared dependencies of this type, so will definitely need to add in compatibility libraries.
                computeTestType(testType, new File(testDistDir), Collections.<TestModuleDependency>emptySet(), classpaths, ml);
            }
        }
        return classpaths;
    }
    
    private void computeTestType(String ttName, File testDistDir, Set<TestModuleDependency> ttModules, Map<String,TestClasspath> classpaths, ModuleList ml) {
        Set<String> compileCnbs = new HashSet<String>();
        Set<String> runtimeCnbs = new HashSet<String>();
        Set<String> testCompileCnbs = new HashSet<String>();
        Set<String> testRuntimeCnbs = new HashSet<String>();
        
        Set<String> processedRecursive = new HashSet<String>();
        boolean fullySpecified = false;
        for (TestModuleDependency td : ttModules) {
            String cnb = td.getModule().getCodeNameBase();
            fullySpecified |= cnb.equals("org.netbeans.libs.junit4");
            if (td.isTest()) {
                if (td.isCompile()) {
                    testCompileCnbs.add(cnb);
                } 
                testRuntimeCnbs.add(cnb);
            }
            if (td.isRecursive()) {
                // scan cp recursively
                processTestEntryRecursive(td,compileCnbs,runtimeCnbs,processedRecursive,ml);         
            } else {
                runtimeCnbs.add(cnb);
                if (td.isCompile()) {
                    compileCnbs.add(cnb);
                }
            }
        }

        StringBuilder extra = new StringBuilder();
        if (!fullySpecified) {
            // Old module which failed to specify all its test dependencies.
            if (ml.getEntry("org.netbeans.libs.junit4") == null) {
                // Old platform. For compatibility, compute a basic unit test lib classpath.
                String[] testLibJars = {
                    "${nb_all}/xtest/lib/insanelib.jar", // NOI18N
                    "${nb_all}/xtest/lib/junit.jar", // NOI18N
                    "${nb_all}/xtest/lib/nbjunit-ide.jar", // NOI18N
                    "${nb_all}/xtest/lib/nbjunit.jar", // NOI18N
                    "${netbeans.dest.dir}/../../xtest/lib/insanelib.jar", // NOI18N
                    "${netbeans.dest.dir}/../../xtest/lib/junit.jar", // NOI18N
                    "${netbeans.dest.dir}/../../xtest/lib/nbjunit.jar", // NOI18N
                    "${netbeans.dest.dir}/ide6/modules/ext/junit-3.8.1.jar", // NOI18N
                    "${netbeans.dest.dir}/java2/modules/ext/junit-3.8.2.jar", // NOI18N
                    "${netbeans.dest.dir}/java2/modules/ext/junit-4.1.jar", // NOI18N
                    "${netbeans.dest.dir}/testtools/modules/ext/insanelib.jar", // NOI18N
                    "${netbeans.dest.dir}/testtools/modules/ext/nbjunit.jar", // NOI18N
                    "${netbeans.dest.dir}/testtools/modules/org-netbeans-modules-nbjunit-ide.jar", // NOI18N
                    "${netbeans.dest.dir}/testtools/modules/org-netbeans-modules-nbjunit.jar", // NOI18N
                    "${netbeans.home}/../ide6/modules/ext/junit-3.8.1.jar", // NOI18N
                    "${netbeans.home}/../java2/modules/ext/junit-3.8.2.jar", // NOI18N
                    "${netbeans.home}/../java2/modules/ext/junit-4.1.jar", // NOI18N
                    "${netbeans.home}/../testtools/modules/ext/insanelib.jar", // NOI18N
                    "${netbeans.home}/../testtools/modules/ext/nbjunit.jar", // NOI18N
                    "${netbeans.home}/../testtools/modules/org-netbeans-modules-nbjunit-ide.jar", // NOI18N
                    "${netbeans.home}/../testtools/modules/org-netbeans-modules-nbjunit.jar", // NOI18N
                    "${netbeans.user}/modules/ext/insanelib.jar", // NOI18N
                    "${netbeans.user}/modules/ext/nbjunit.jar", // NOI18N
                    "${netbeans.user}/modules/org-netbeans-modules-nbjunit-ide.jar", // NOI18N
                    "${netbeans.user}/modules/org-netbeans-modules-nbjunit.jar", // NOI18N
                };
                for (String jar : testLibJars) {
                    extra.append(":");
                    extra.append(jar);
                }
                if (ttName.startsWith("qa-")) {
                    extra.append(":${nb_all}/jemmy/builds/jemmy.jar:${nb_all}/jellytools/builds/jelly2-nb.jar");
                }
            } else {
                // Basic dependencies many tests use:
                for (String library : new String[] {"org.netbeans.libs.junit4", "org.netbeans.modules.nbjunit", "org.netbeans.insane"}) {
                    compileCnbs.add(library);
                    runtimeCnbs.add(library);
                }
                if (ttName.startsWith("qa-")) {
                    // ProjectSupport moved from the old nbjunit.ide:
                    testCompileCnbs.add("org.netbeans.modules.java.j2seproject");
                    testRuntimeCnbs.add("org.netbeans.modules.java.j2seproject");
                    // Common GUI testing tools:
                    for (String library : new String[] {"org.netbeans.modules.jemmy", "org.netbeans.modules.jellytools"}) {
                        compileCnbs.add(library);
                        runtimeCnbs.add(library);
                    }
                }
            }
        }

        TestClasspath testClasspath = new TestClasspath(
                mergePaths(compileCnbs,false,ttName,testDistDir, ml) + extra,
                mergePaths(runtimeCnbs,false,ttName,testDistDir,ml) + extra,
                mergePaths(testCompileCnbs,true,ttName,testDistDir,ml),
                mergePaths(testRuntimeCnbs,true,ttName,testDistDir,ml));

        classpaths.put(ttName,testClasspath);
    }
  
  private void processTestEntryRecursive(TestModuleDependency td,
                                        Set<String> compileCnds,
                                        Set<String> runtimeCnds,
                                        Set<String> processedRecursive,
                                        ModuleList ml) {
        Set<String> unprocessed = new HashSet<String>();
        
        unprocessed.add(td.getModule().getCodeNameBase()); 
        while (!unprocessed.isEmpty()) { // crude breadth-first search
            Iterator<String> it = unprocessed.iterator();
            String cnb = it.next();
            it.remove();
            if (processedRecursive.add(cnb)) {
                ModuleEntry module = ml.getEntry(cnb);
                if (module == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                    continue;
                }
                if (!cnb.equals(project.getCodeNameBase())) { // build/classes for this is special
                    runtimeCnds.add(cnb);
                    if (td.isCompile()) {
                        compileCnds.add(cnb);
                    }
                }
                String[] newDeps = module.getRunDependencies();
                unprocessed.addAll(Arrays.asList(newDeps));
            }
        }
    }

   private static final Set<String> warnedModules = Collections.synchronizedSet(new HashSet<String>());
    private String mergePaths(Set<String> cnbs, boolean test,String testtype,File testDistDir,ModuleList ml) {
        StringBuffer cps = new StringBuffer();
        for (String cnb : cnbs) {
                ModuleEntry module = ml.getEntry(cnb);
                if (module == null) {
                    if (warnedModules.add(cnb)) {
                        Logger.getLogger(Evaluator.class.getName()).warning("Cannot find test module dependency: " + cnb);
                    }
                    continue;
                }
                if (cps.length() > 0) {
                    cps.append(':');
                }
                if (test) {
                    // we need to get cluster name
                    File clusterDir = module.getClusterDirectory();
                    if (clusterDir != null) {
                        String clusterName = clusterDir.getName();
                        char s = File.separatorChar;
                        File jarFile = new File(
                                          testDistDir, testtype + s + clusterName + s + cnb.replace('.','-') + s + "tests.jar"); // NOI18N
                        cps.append(jarFile.getPath());
                        // See ParseProjectXml:
                        if (!testtype.equals("unit")) {
                            cps.append(':');
                            jarFile = new File(testDistDir, "unit" + s + clusterName + s + cnb.replace('.', '-') + s + "tests.jar"); // NOI18N
                            cps.append(jarFile.getPath());
                        }
                    }
                     
                } else {
                    cps.append(module.getJarLocation().getPath());
                    cps.append(module.getClassPathExtensions()); // #105621
                }
        }
        return cps.toString();
    }
   
}
