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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

/**
 * Tests module dependencies in a suite.
 * @author Jesse Glick
 */
public class SuiteCustomizerLibrariesTest extends TestBase {
    
    public SuiteCustomizerLibrariesTest(String name) {
        super(name);
    }
    
    private NbPlatform platform;
    private SuiteProject suite;

    protected void setUp() throws Exception {
        noDataDir = true;   // self-contained test; prevents name clash with 'custom' platform in data dir
        super.setUp();
        // PLATFORM SETUP
        File install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // MODULE foo
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo/1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.0");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION, "foo-1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "foo/Bundle.properties");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok1, tok1a");
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("foo/Bundle.properties", "OpenIDE-Module-Name=Foo Module");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "foo.jar"), contents, mani);
        // MODULE bar
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "bar");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok1");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bar.jar"), Collections.EMPTY_MAP, mani);
        // MODULE foo2
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo2");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok1b");
        contents = new HashMap<String,String>();
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "foo2.jar"), contents, mani);
        // MODULE bar2
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "bar2");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_NEEDS, "tok1b");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bar2.jar"), Collections.EMPTY_MAP, mani);
        // MODULE baz
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "baz");
        mani.getMainAttributes().putValue("OpenIDE-Module-Module-Dependencies", "foo/1 > 1.0");
        mani.getMainAttributes().putValue("OpenIDE-Module-Requires", "org.openide.modules.ModuleFormat1, org.openide.modules.os.Windows");
        TestBase.createJar(new File(new File(new File(install, "anothercluster"), "modules"), "baz.jar"), Collections.EMPTY_MAP, mani);
        platform = NbPlatform.addPlatform("custom", install, "custom");
        // SUITE setup
        suite = TestBase.generateSuite(getWorkDir(), "suite", "custom");
        // MODULE org.example.module1
        NbModuleProject module = TestBase.generateSuiteComponent(suite, "module1");
        EditableManifest em = Util.loadManifest(module.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE, "org.example.module1/2", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "2.0", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        LocalizedBundleInfo lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module One");
        lbinfo.store();
        // MODULE org.example.module2
        module = TestBase.generateSuiteComponent(suite, "module2");
        em = Util.loadManifest(module.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Two");
        lbinfo.store();
        // MODULE org.example.module3
        module = TestBase.generateSuiteComponent(suite, "module3");
        Util.addDependency(module, "org.example.module2");
        Util.addDependency(module, "bar");
        lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Three");
        lbinfo.store();
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testUniverseModules() throws Exception { // #65924
        Map<String,SuiteCustomizerLibraries.UniverseModule> modulesByName = new HashMap<String,SuiteCustomizerLibraries.UniverseModule>();
        Set<SuiteCustomizerLibraries.UniverseModule> modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getModules(), SuiteUtils.getSubProjects(suite));
        for (SuiteCustomizerLibraries.UniverseModule m : modules) {
            modulesByName.put(m.getCodeNameBase(), m);
        }
        assertEquals(modules.size(), modulesByName.size());
        SuiteCustomizerLibraries.UniverseModule m = modulesByName.get("core");
        assertNotNull(m);
        // core.jar is just a dummy JAR, nothing interesting to test
        m = modulesByName.get("foo");
        assertNotNull(m);
        assertEquals("somecluster", m.getCluster());
        assertEquals("Foo Module", m.getDisplayName());
        assertEquals(1, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("1.0"), m.getSpecificationVersion());
        assertEquals("foo-1", m.getImplementationVersion());
        assertEquals(new HashSet<String>(Arrays.asList("tok1", "tok1a")), m.getProvidedTokens());
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = modulesByName.get("bar");
        assertNotNull(m);
        assertEquals(Collections.EMPTY_SET, m.getProvidedTokens());
        assertEquals(Collections.singleton("tok1"), m.getRequiredTokens());
        m = modulesByName.get("baz");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "foo/1 > 1.0"), m.getModuleDependencies());
        m = modulesByName.get("org.example.module1");
        assertNotNull(m);
        assertNull(m.getCluster());
        assertEquals("Module One", m.getDisplayName());
        assertEquals(2, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("2.0"), m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.singleton("tok2"), m.getProvidedTokens());
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = modulesByName.get("org.example.module2");
        assertNotNull(m);
        assertEquals(-1, m.getReleaseVersion());
        assertNull(m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.EMPTY_SET, m.getProvidedTokens());
        assertEquals(Collections.singleton("tok2"), m.getRequiredTokens());
        m = modulesByName.get("org.example.module3");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "org.example.module2, bar"), m.getModuleDependencies());
    }
    
    public void testDependencyWarnings() throws Exception { // #65924
        Set<SuiteCustomizerLibraries.UniverseModule> modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getModules(), SuiteUtils.getSubProjects(suite));
        Set<String> bothClusters = new HashSet<String>(Arrays.asList("somecluster", "anothercluster"));
        assertEquals(null, join(SuiteCustomizerLibraries.findWarning(modules, bothClusters, Collections.<String>emptySet())));
        assertEquals("[ERR_platform_excluded_dep, baz, anothercluster, Foo Module, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, Collections.singleton("anothercluster"), Collections.<String>emptySet())));
        assertNull(join(SuiteCustomizerLibraries.findWarning(modules, Collections.singleton("somecluster"), Collections.<String>emptySet())));
        assertEquals("[ERR_suite_excluded_dep, Module Three, bar, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, Collections.<String>emptySet(), Collections.<String>emptySet())));
        assertEquals("[ERR_platform_only_excluded_providers, tok1, bar, somecluster, Foo Module, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, bothClusters, Collections.singleton("foo"))));
        assertEquals("[ERR_platform_only_excluded_providers, tok1b, bar2, somecluster, foo2, somecluster]",
                join(SuiteCustomizerLibraries.findWarning(modules, bothClusters, Collections.singleton("foo2"))));
        // XXX much more could be tested; check coverage results
    }
    
    private static String join(String[] elements) {
        if (elements != null) {
            return Arrays.asList(elements).toString();
        } else {
            return null;
        }
    }
    
}
