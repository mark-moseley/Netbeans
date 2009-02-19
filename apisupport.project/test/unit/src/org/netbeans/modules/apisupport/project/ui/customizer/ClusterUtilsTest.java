/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;

/**
 * @author Richard Michalsky
 */
public class ClusterUtilsTest extends TestBase {

    public ClusterUtilsTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsValidCluster() throws IOException {
        File f = new File(getWorkDir(), "nonexistent");
        assertFalse("Nonexistent folder is not a valid cluster", ClusterUtils.isValidCluster(f));
        f = new File(getWorkDir(), "cluster1");
        new File(f, "modules").mkdirs();
        assertFalse("Folder only with \"modules\" folder is not a valid cluster", ClusterUtils.isValidCluster(f));
        f = new File(getWorkDir(), "cluster2");
        new File(f, "config/Modules").mkdirs();
        assertTrue("Folder with \"config/Modules\" folder is a valid cluster", ClusterUtils.isValidCluster(f));
    }

    // TODO C.P tests, this one fails, see BrokenPlatformReferenceTest how to setup user.properties.file
    @Test
    public void testGetClusterDirectory() throws IOException {
        clearWorkDir();
        File suiteDir = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT, false);
        SuiteProject prj = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
        prj.open(); // necessary for project files to be created
        assertEquals(new File(suiteDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj).getAbsoluteFile());

        File scDir = new File(suiteDir, "module1");
        NbModuleProjectGenerator.createSuiteComponentModule(
                scDir, "test.module1", "Module 1", "test/module1/Bundle.properties", null, suiteDir);
        NbModuleProject prj2 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(scDir));
        prj2.open(); // necessary for project files to be created
        assertEquals(new File(suiteDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj2).getAbsoluteFile());

        File standaloneDir = new File(getWorkDir(), "module2");
        NbModuleProjectGenerator.createStandAloneModule(
                standaloneDir, "test.module2", "Module 2", "test/module2/Bundle.properties", null, NbPlatform.PLATFORM_ID_DEFAULT);
        NbModuleProject prj3 = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(standaloneDir));
        prj3.open(); // necessary for project files to be created
        assertEquals(new File(standaloneDir, "build/cluster"), ClusterUtils.getClusterDirectory(prj3).getAbsoluteFile());
    }

    @Test
    public void testEvaluateClusterPathEntry() {
        System.out.println("evaluateClusterPathEntry");
        String rawEntry = "";
        File root = null;
        PropertyEvaluator eval = null;
        File nbPlatformRoot = null;
        File expResult = null;
        File result = ClusterUtils.evaluateClusterPathEntry(rawEntry, root, eval, nbPlatformRoot);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    @Test
    public void testEvaluateClusterPath() {
        System.out.println("evaluateClusterPath");
        File root = null;
        PropertyEvaluator eval = null;
        File nbPlatformRoot = null;
        Set<ClusterInfo> expResult = null;
        Set<ClusterInfo> result = ClusterUtils.evaluateClusterPath(root, eval, nbPlatformRoot);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

}