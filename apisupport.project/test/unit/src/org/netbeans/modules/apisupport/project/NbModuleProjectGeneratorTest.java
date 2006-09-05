/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * NbModuleProjectGenerator tests.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public class NbModuleProjectGeneratorTest extends TestBase {
    // TODO test suite module and also NetBeans CVS tree modules
    // XXX also should test content of created files (XMLs, properties)
    
    public NbModuleProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] BASIC_CREATED_FILES = {
        "build.xml",
        "manifest.mf",
        "nbproject/project.xml",
        "nbproject/build-impl.xml",
        "src/org/example/testModule/resources/Bundle.properties",
        "src/org/example/testModule/resources/layer.xml",
        "test/unit/src",
    };
    
    private static final String[] STANDALONE_CREATED_FILES = {
        "nbproject/platform.properties",
    };
    
    private static final String[] SUITE_COMP_REL_CREATED_FILES = {
        "nbproject/suite.properties",
    };
    
    public void testCreateStandAloneModule() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testModule");
        NbModuleProjectGenerator.createStandAloneModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                NbPlatform.PLATFORM_ID_DEFAULT); // platform id
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        p.open();
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        for (int i=0; i < STANDALONE_CREATED_FILES.length; i++) {
            assertNotNull(STANDALONE_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(STANDALONE_CREATED_FILES[i]));
        }
    }
    
    public void testCreateSuiteComponentModule() throws Exception {
        // create suite for the module being tested
        File suiteDir = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT);
        FileObject fo = FileUtil.toFileObject(suiteDir);
        Project suiteProject = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + suiteDir, suiteProject);
        SubprojectProvider spp = (SubprojectProvider) suiteProject.getLookup().lookup(SubprojectProvider.class);
        assertNotNull("has a SubprojectProvider", spp);
        
        // create "relative" module in suite
        File targetPrjDir = new File(suiteDir, "testModuleRel");
        NbModuleProjectGenerator.createSuiteComponentModule(
                targetPrjDir,
                "org.example.testModuleRel", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                suiteDir); // platform id
        fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        NbModuleProject moduleProjectRel = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, moduleProjectRel);
        moduleProjectRel.open();
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        for (int i=0; i < SUITE_COMP_REL_CREATED_FILES.length; i++) {
            assertNotNull(SUITE_COMP_REL_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(SUITE_COMP_REL_CREATED_FILES[i]));
        }
        assertEquals("listed as the sole suite component", Collections.singleton(moduleProjectRel), spp.getSubprojects());
        
        // create "absolute" module in suite
        targetPrjDir = new File(getWorkDir(), "testModuleAbs");
        NbModuleProjectGenerator.createSuiteComponentModule(
                targetPrjDir,
                "org.example.testModuleAbs", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml",
                suiteDir); // platform id
        fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        NbModuleProject moduleProjectAbs = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, moduleProjectAbs);
        moduleProjectAbs.open();
        // check generated module
        for (int i=0; i < BASIC_CREATED_FILES.length; i++) {
            assertNotNull(BASIC_CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(BASIC_CREATED_FILES[i]));
        }
        assertEquals("now have two suite components", new HashSet(Arrays.asList(new Project[] {moduleProjectRel, moduleProjectAbs})), spp.getSubprojects());
    }
    
    public void testCreateSuiteLibraryModule() throws Exception {
        Map/*<String,String>*/ contents = new HashMap();
        contents.put("lib/pkg/Clazz3.class", "");
        contents.put("lib/pkg2/Clazz4.class", "");
        contents.put("1.0/oldlib/Clazz5.class", ""); // #72669
        File jar = new File(getWorkDir(), "some.jar");
        createJar(jar, contents, new Manifest());
        SuiteProject sweet = generateSuite("sweet");
        File moduleDir = new File(getWorkDir(), "module");
        NbModuleProjectGenerator.createSuiteLibraryModule(
                moduleDir, "module", "Module", "module/Bundle.properties",
                sweet.getProjectDirectoryFile(), null, new File[] {jar});
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduleDir));
        ManifestManager.PackageExport[] exports = new ProjectXMLManager(p).getPublicPackages();
        Set/*<String>*/ packages = new TreeSet();
        for (int i = 0; i < exports.length; i++) {
            assertFalse(exports[i].isRecursive());
            packages.add(exports[i].getPackage());
        }
        assertEquals(Arrays.asList(new String[] {"lib.pkg", "lib.pkg2"}), new ArrayList(packages));
    }
    
    // XXX hmmm, don't know yet how to fully test this case since I don't want
    // to touch the netbeans.org CVS tree. Probably somehow simulating
    // netbeans.org CVS tree would help. I'll try to investigate it later.
//    public void testCreateNetBeansModule() throws Exception {
//        File prjDir = new File("/usr/share/java/netbeans-cvs-current/ide/projectimport/testModule");
//        NbModuleProjectGenerator.createNetBeansOrgModule(
//                prjDir,
//                "org.example.testModule", // cnb
//                "Testing Module", // display name
//                "org/example/testModule/resources/Bundle.properties",
//                "org/example/testModule/resources/layer.xml");
//    }
    
}
