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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * Test {@link ModuleListParser}.
 * @author Jesse Glick
 */
public class ModuleListParserTest extends TestCase {

    public ModuleListParserTest(String name) {
        super(name);
    }

    private File nball;

    private File file(File root, String relpath) {
        return new File(root, relpath.replace('/', File.separatorChar));
    }
    
    private String filePath(File root, String relpath) {
        return file(root, relpath).getAbsolutePath();
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        String prop = System.getProperty("nb_all");
        assertNotNull("${nb_all} defined", prop);
        nball = new File(prop);
        new File(nball, "nbproject/private/scan-cache-full.ser").delete();
        new File(nball, "nbproject/private/scan-cache-standard.ser").delete();
    }
    
    public void testScanSourcesInNetBeansOrg() throws Exception {
        Hashtable<String,String> properties = new Hashtable<String,String>();
        properties.put("nb_all", nball.getAbsolutePath());
        File build = file(nball, "build");
        properties.put("netbeans.dest.dir", build.getAbsolutePath());
        properties.put("nb.cluster.foo", "beans,clazz");
        properties.put("nb.cluster.foo.dir", "foodir");
        properties.put("nb.cluster.bar", "core/startup");
        properties.put("nb.cluster.bar.dir", "bardir");
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_NB_ORG, null);
        System.err.println("Scanned " + nball + " sources in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.modules.beans");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.beans", e.getCnb());
        assertEquals(file(build, "foodir/modules/org-netbeans-modules-beans.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found module in a subdir", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals("unknown module put in extra cluster by default", file(build, "extra/modules/org-netbeans-libs-xerces.jar"), e.getJar());
        assertEquals("correct CP extensions (using <binary-origin> and relative paths)",
            Collections.singletonList(file(nball, "libs/external/xerces-2.8.0.jar")),
            Arrays.asList(e.getClassPathExtensions()));
        /*
        e = p.findByCodeNameBase("javax.jmi.model");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #1)", Arrays.asList(new File[] {
            file(nball, "mdr/external/mof.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
         */
        e = p.findByCodeNameBase("org.netbeans.modules.css");
        assertNotNull(e);
        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #2)", Arrays.asList(new File[] {
            file(nball, "xml/external/flute.jar"),
            file(nball, "xml/external/sac.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.swing.tabcontrol");
        assertNotNull("found module in a subsubdir", e);
        e = p.findByCodeNameBase("org.netbeans.core.startup");
        assertNotNull(e);
        assertEquals("org.netbeans.core.startup", e.getCnb());
        assertEquals("handling special JAR names correctly", file(build, "bardir/core/core.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull("found xml/tax", e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(file(build, "extra/modules/org-netbeans-modules-xml-tax.jar"), e.getJar());
        assertEquals("correct CP extensions (using runtime-relative-path)", Arrays.asList(new File[] {
            file(build, "extra/modules/ext/org-netbeans-tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.masterfs");
        assertNotNull(e);
        String testDeps[] = e.getTestDependencies().get("unit");
        assertNotNull(testDeps);
        assertEquals("depends on one test entry",1,testDeps.length);
        assertEquals("org.openide.filesystems",testDeps[0]);
    }
    
    public void testScanSourcesAndBinariesForExternalSuite() throws Exception {
        Project fakeproj = new Project();
        fakeproj.addBuildListener(new BuildListener() {
            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }
            public void taskStarted(BuildEvent buildEvent) {}
            public void taskFinished(BuildEvent buildEvent) {}
            public void targetStarted(BuildEvent buildEvent) {}
            public void targetFinished(BuildEvent buildEvent) {}
            public void buildStarted(BuildEvent buildEvent) {}
            public void buildFinished(BuildEvent buildEvent) {}
        });
        Hashtable<String,String> properties = new Hashtable<String,String>();
        properties.put("netbeans.dest.dir", filePath(nball, "nbbuild/netbeans"));
        properties.put("basedir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/action-project"));
        properties.put("suite.dir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite1"));
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_SUITE, fakeproj);
        System.err.println("Scanned " + nball + " binaries in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.action");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.action", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.examples.modules.lib");
        assertNotNull("found sister project in suite", e);
        assertEquals("org.netbeans.examples.modules.lib", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar"), e.getJar());
        File jar = file(nball, "nbbuild/netbeans/ide9/modules/org-netbeans-libs-xerces.jar");
        assertTrue("Build all-libs/xerces first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found netbeans.org module by its binary", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals("correct CP extensions (using Class-Path header in manifest)",
                Collections.singletonList(file(nball, "nbbuild/netbeans/ide9/modules/ext/xerces-2.8.0.jar")),
                Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.openide.loaders");
        assertNotNull(e);
        assertEquals("org.openide.loaders", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform8/modules/org-openide-loaders.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.bootstrap");
        assertNotNull(e);
        assertEquals("org.netbeans.bootstrap", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform8/lib/boot.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        jar = file(nball, "nbbuild/netbeans/ide9/modules/org-netbeans-modules-xml-tax.jar");
        assertTrue("Build all-xml/tax first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals(Arrays.asList(new File[] {
            file(nball, "nbbuild/netbeans/ide9/modules/ext/org-netbeans-tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
    }
    
    public void testScanSourcesAndBinariesForExternalStandaloneModule() throws Exception {
        Hashtable<String,String> properties = new Hashtable<String,String>();
        properties.put("netbeans.dest.dir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform"));
        properties.put("basedir", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project"));
        properties.put("project", filePath(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project"));
        ModuleListParser p = new ModuleListParser(properties, ParseProjectXml.TYPE_STANDALONE, null);
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.dummy");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.dummy", e.getCnb());
        assertEquals(file(nball, "apisupport/project/test/unit/data/example-external-projects/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.classfile");
        assertNotNull("found (fake) netbeans.org module by its binary", e);
        assertEquals("org.netbeans.modules.classfile", e.getCnb());
    }
    
}
