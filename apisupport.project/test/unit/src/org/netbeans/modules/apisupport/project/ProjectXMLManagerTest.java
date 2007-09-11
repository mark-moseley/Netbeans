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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import static org.netbeans.modules.apisupport.project.universe.TestModuleDependency.UNIT;
import static org.netbeans.modules.apisupport.project.universe.TestModuleDependency.QA_FUNCTIONAL;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Martin Krauskopf
 */
public class ProjectXMLManagerTest extends TestBase {
    
    private final static String ANT_PROJECT_SUPPORT = "org.netbeans.modules.project.ant";
    private final static String DIALOGS = "org.openide.dialogs";
    private final static Set<String> ASSUMED_CNBS;
    
    static {
        Set<String> assumedCNBs = new HashSet<String>(2, 1.0f);
        assumedCNBs.add(ANT_PROJECT_SUPPORT);
        assumedCNBs.add(DIALOGS);
        ASSUMED_CNBS = Collections.unmodifiableSet(assumedCNBs);
    }
    
    public ProjectXMLManagerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    private ProjectXMLManager createXercesPXM() throws IOException {
        NbModuleProject xercesPrj = (NbModuleProject) ProjectManager.getDefault().
                findProject(nbCVSRoot().getFileObject("libs/xerces"));
        return new ProjectXMLManager(xercesPrj);
    }
    
    // sanity check
    public void testGeneratedProject() throws Exception {
        validate(generateTestingProject(), false); // false - original project.xml is unordered
    }
    
    public void testGetCodeNameBase() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        assertEquals("action-project cnb", "org.example.module1", p.getCodeNameBase());
    }
    
    public void testGetDirectDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        Set<ModuleDependency> deps = testingPXM.getDirectDependencies();
        assertEquals("number of dependencies", 2, deps.size());
        
        Set<String> assumedCNBs = new HashSet<String>(ASSUMED_CNBS);
        for (ModuleDependency md : deps) {
            if (md.getModuleEntry().getCodeNameBase().equals(DIALOGS)) {
                assertNotNull("module entry", md.getModuleEntry());
                assertEquals("release version", null, md.getReleaseVersion());
                assertEquals("specification version", "6.2", md.getSpecificationVersion());
            }
            if (md.getModuleEntry().getCodeNameBase().equals(ANT_PROJECT_SUPPORT)) {
                assertNotNull("module entry", md.getModuleEntry());
                assertEquals("release version", "1", md.getReleaseVersion());
                assertEquals("specification version", "1.10", md.getSpecificationVersion());
            }
            String cnbToRemove = md.getModuleEntry().getCodeNameBase();
            assertTrue("unknown dependency: " + cnbToRemove, assumedCNBs.remove(cnbToRemove));
        }
        assertTrue("following dependencies were found: " + assumedCNBs, assumedCNBs.isEmpty());
    }
    
    public void testGetDirectDependenciesForCustomPlatform() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        Set deps = testingPXM.getDirectDependencies();
        assertEquals("number of dependencies", 2, deps.size());
        Set depsWithCustom = testingPXM.getDirectDependencies(
                NbPlatform.getPlatformByID("custom"));
        assertEquals("number of dependencies", 0, depsWithCustom.size());
    }
    
    public void testRemoveDependency() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                testingPXM.removeDependency(DIALOGS);
                return true;
            }
        });
        assertTrue("removing dependency", result);
        ProjectManager.getDefault().saveProject(testingProject);
        
        final Set<ModuleDependency> newDeps = testingPXM.getDirectDependencies();
        assertEquals("number of dependencies", 1, newDeps.size());
        Set<String> newCNBs = new HashSet<String>();
        newCNBs.add(ANT_PROJECT_SUPPORT);
        for (ModuleDependency md : newDeps) {
            String cnbToRemove = md.getModuleEntry().getCodeNameBase();
            assertTrue("unknown dependency: " + cnbToRemove, newCNBs.remove(cnbToRemove));
        }
        assertTrue("following dependencies were found: " + newCNBs, newCNBs.isEmpty());
        validate(testingProject, true);
    }
    
    public void testEditDependency() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        final Set<ModuleDependency> deps = testingPXM.getDirectDependencies();
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                boolean tested = false;
                for (ModuleDependency origDep : deps) {
                    if (DIALOGS.equals(origDep.getModuleEntry().getCodeNameBase())) {
                        tested = true;
                        ModuleDependency newDep = new ModuleDependency(
                                origDep.getModuleEntry(),
                                "2",
                                origDep.getSpecificationVersion(),
                                origDep.hasCompileDependency(),
                                origDep.hasImplementationDepedendency());
                        testingPXM.editDependency(origDep, newDep);
                    }
                }
                assertTrue("org.openide.dialogs dependency tested", tested);
                return true;
            }
        });
        assertTrue("editing dependencies", result);
        ProjectManager.getDefault().saveProject(testingProject);
        // XXX this refresh shouldn't be needed (should listen on project.xml changes)
        ProjectXMLManager freshTestingPXM = new ProjectXMLManager(testingProject);
        
        final Set<ModuleDependency> newDeps = freshTestingPXM.getDirectDependencies();
        boolean tested = false;
        for (ModuleDependency md : newDeps) {
            if (DIALOGS.equals(md.getModuleEntry().getCodeNameBase())) {
                tested = true;
                assertEquals("edited release version", "2", md.getReleaseVersion());
                assertEquals("unedited specification version", "6.2", md.getSpecificationVersion());
                break;
            }
        }
        assertTrue("org.openide.dialogs dependency tested", tested);
        validate(testingProject, false); // false - order is not touched after editing
    }
    
    public void testAddDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        final Set<ModuleDependency> newDeps = new HashSet<ModuleDependency>();
        ModuleEntry me = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        assertNotNull("java/project must be built", me);
        String javaProjectRV = me.getReleaseVersion();
        String javaProjectSV = me.getSpecificationVersion();
        newDeps.add(new ModuleDependency(me));
        me = testingProject.getModuleList().getEntry("org.netbeans.modules.java.j2seplatform");
        assertNotNull("java/j2seplatform must be built", me);
        newDeps.add(new ModuleDependency(me, "1", null, false, true));
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                testingPXM.addDependencies(newDeps);
                return true;
            }
        });
        assertTrue("adding dependencies", result);
        ProjectManager.getDefault().saveProject(testingProject);
        
        Set<ModuleDependency> deps = testingPXM.getDirectDependencies();
        
        Set<String> assumedCNBs = new HashSet<String>(ASSUMED_CNBS);
        assumedCNBs.add("org.netbeans.modules.java.project");
        assumedCNBs.add("org.netbeans.modules.java.j2seplatform");
        
        assertEquals("number of dependencies", deps.size(), assumedCNBs.size());
        for (ModuleDependency md : deps) {
            assertTrue("unknown dependency",
                    assumedCNBs.remove(md.getModuleEntry().getCodeNameBase()));
            if ("org.netbeans.modules.java.project".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("initial release version", javaProjectRV, md.getReleaseVersion());
                assertEquals("initial specification version", javaProjectSV, md.getSpecificationVersion());
            }
            if ("org.netbeans.modules.java.j2seplatform".equals(md.getModuleEntry().getCodeNameBase())) {
                assertEquals("edited release version", "1", md.getReleaseVersion());
                assertFalse("has compile depedendency", md.hasCompileDependency());
                assertTrue("has implementation depedendency", md.hasImplementationDepedendency());
            }
        }
        assertTrue("following dependencies were found: " + assumedCNBs, assumedCNBs.isEmpty());
        validate(testingProject, true);
    }
    
    public void testExceptionWhenAddingTheSameModuleDependencyTwice() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        ModuleEntry me = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        final ModuleDependency md = new ModuleDependency(me, "1", null, false, true);
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                Element confData = testingProject.getPrimaryConfigurationData();
                Element moduleDependencies = ProjectXMLManager.findModuleDependencies(confData);
                ProjectXMLManager.createModuleDependencyElement(moduleDependencies, md, null);
                ProjectXMLManager.createModuleDependencyElement(moduleDependencies, md, null);
                testingProject.putPrimaryConfigurationData(confData);
                return true;
            }
        });
        assertTrue("adding dependencies", result);
        ProjectManager.getDefault().saveProject(testingProject);
        try {
            testingPXM.getDirectDependencies();
            fail("IllegalStateException was expected");
        } catch (IllegalStateException ise) {
            // OK, expected exception was thrown
        }
        validate(testingProject, false); // false - we are not using regular way for adding
    }
    
    public void testFindPublicPackages() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final File projectXML = FileUtil.toFile(
                testingProject.getProjectDirectory().getFileObject("nbproject/project.xml"));
        assert projectXML.exists();
        Element confData = ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                Element data = null;
                try {
                    Document doc = XMLUtil.parse(new InputSource(projectXML.toURI().toString()),
                            false, true, null, null);
                    Element project = doc.getDocumentElement();
                    Element config = Util.findElement(project, "configuration", null); // NOI18N
                    data = Util.findElement(config, "data", NbModuleProjectType.NAMESPACE_SHARED);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } catch (SAXException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                return data;
            }
        });
        assertNotNull("finding configuration data element", confData);
        ManifestManager.PackageExport[] pp = ProjectXMLManager.findPublicPackages(confData);
        assertEquals("number of public packages", 1, pp.length);
        assertEquals("public package", "org.netbeans.examples.modules.misc", pp[0].getPackage());
        validate(testingProject, false); // false - just looking around
    }
    
    public void testReplaceDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        final SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(testingPXM.getDirectDependencies());
        assertEquals("number of dependencies", 2, deps.size());
        ModuleDependency newOO = null;
        ModuleDependency oldOO = null;
        for (Iterator<ModuleDependency> it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = it.next();
            if (DIALOGS.equals(md.getModuleEntry().getCodeNameBase())) {
                oldOO = md;
                ModuleEntry me = md.getModuleEntry();
                newOO = new ModuleDependency(me,
                        "", // will be check if it is not written
                        oldOO.getSpecificationVersion(),
                        md.hasCompileDependency(),
                        md.hasImplementationDepedendency());
                it.remove();
                break;
            }
        }
        deps.add(newOO);
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                testingPXM.replaceDependencies(deps);
                return true;
            }
        });
        assertTrue("project successfully saved", result);
        ProjectManager.getDefault().saveProject(testingProject);
        validate(testingProject, true);
        
        final ProjectXMLManager newTestingPXM = new ProjectXMLManager(testingProject);
        final Set<ModuleDependency> newDeps = newTestingPXM.getDirectDependencies();
        for (ModuleDependency md : newDeps) {
            if (DIALOGS.equals(md.getModuleEntry().getCodeNameBase())) {
                assertNull("empty(null) release version", md.getReleaseVersion());
                assertEquals("unedited specification version",
                        oldOO.getSpecificationVersion(),
                        md.getSpecificationVersion());
                break;
            }
        }
    }
    
    public void testGetPublicPackages() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        assertEquals("number of public packages", 1, testingPXM.getPublicPackages().length);
        assertEquals("package name", "org.netbeans.examples.modules.misc", testingPXM.getPublicPackages()[0].getPackage());
        assertFalse("not recursive", testingPXM.getPublicPackages()[0].isRecursive());
        
        ProjectXMLManager xercesPXM = createXercesPXM();
        assertEquals("number of binary origins", 1, xercesPXM.getPublicPackages().length);
        assertEquals("package name", "org", xercesPXM.getPublicPackages()[0].getPackage());
        assertTrue("recursive", xercesPXM.getPublicPackages()[0].isRecursive());
    }
    
    public void testReplacePublicPackages() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        ManifestManager.PackageExport[] publicPackages = testingPXM.getPublicPackages();
        assertEquals("number of public packages", 1, publicPackages.length);
        final String[] newPP = new String[] { publicPackages[0].getPackage(), "org.netbeans.examples.modules" };
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                testingPXM.replacePublicPackages(newPP);
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(testingProject);
        ManifestManager.PackageExport[] newPublicPackages = testingPXM.getPublicPackages();
        assertEquals("number of new public packages", 2, newPublicPackages.length);
        Collection newPPs = Arrays.asList(new String[] {"org.netbeans.examples.modules", "org.netbeans.examples.modules.misc"});
        assertTrue(newPPs.contains(newPublicPackages[0].getPackage()));
        assertTrue(newPPs.contains(newPublicPackages[1].getPackage()));
        assertNull("there must not be friend", testingPXM.getFriends());
        validate(testingProject, false); // false - just replacing public packages
    }
    
    public void testReplaceFriends() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        assertEquals("one friend", 1, testingPXM.getFriends().length);
        assertEquals("friend org.module.examplemodule", "org.module.examplemodule", testingPXM.getFriends()[0]);
        final String[] newFriends = new String[] { "org.exampleorg.somefriend", "org.exampleorg.anotherfriend" };
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ManifestManager.PackageExport pkgs[] = testingPXM.getPublicPackages();
                String[] packagesToExpose = new String[pkgs.length];
                for (int i = 0; i < pkgs.length; i++) {
                    packagesToExpose[i] = pkgs[i].getPackage();
                }
                testingPXM.replaceFriends(newFriends, packagesToExpose);
                return true;
            }
        });
        assertTrue("replace friends", result);
        ProjectManager.getDefault().saveProject(testingProject);
        final ProjectXMLManager newTestingPXM = new ProjectXMLManager(testingProject);
        String[] actualFriends = newTestingPXM.getFriends();
        assertEquals("number of new friend", 2, actualFriends.length);
        Collection newFriendsCNBs = Arrays.asList(actualFriends);
        assertTrue(newFriendsCNBs.contains(newFriends[0]));
        assertTrue(newFriendsCNBs.contains(newFriends[1]));
        assertEquals("public packages", 1, newTestingPXM.getPublicPackages().length);
        validate(testingProject, false); // false - just replacing friends
    }
    
    public void testGetBinaryOrigins() throws Exception {
        ProjectXMLManager xercesPXM = createXercesPXM();
        assertEquals("number of binary origins", 1, xercesPXM.getBinaryOrigins().length);
    }
    
    public void testThatFriendPackagesAreGeneratedInTheRightOrder_61882() throws Exception {
        FileObject fo = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "testing");
        FileObject projectXMLFO = fo.getFileObject("nbproject/project.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
                "<code-name-base>org.netbeans.modules.j2eeapis</code-name-base>\n" +
                "<standalone/>\n" +
                "<module-dependencies/>\n" +
                "<public-packages>\n" +
                "<subpackages>javax.enterprise.deploy</subpackages>\n" +
                "</public-packages>\n" +
                "<class-path-extension>\n" +
                "<runtime-relative-path>ext/jsr88javax.jar</runtime-relative-path>\n" +
                "<binary-origin>../external/jsr88javax.jar</binary-origin>\n" +
                "</class-path-extension>\n" +
                "</data>\n" +
                "</configuration>\n" +
                "</project>\n";
        TestBase.dump(projectXMLFO, xml);
        NbModuleProject project = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
        validate(project, true);
    }
    
    public void testDependenciesOrder() throws Exception { // #62003
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        ModuleEntry me = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        final ModuleDependency md = new ModuleDependency(me, "1", null, false, true);
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                testingPXM.addDependency(md);
                return true;
            }
        });
        assertTrue("adding dependencies", result);
        ProjectManager.getDefault().saveProject(testingProject);
        validate(testingProject, true);
    }
    
  
    public void testRemoveTestDependency() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager pxm = new ProjectXMLManager(testingProject);
        final String cnb = "org.netbeans.modules.java.project";
        final String cnb2 = "org.netbeans.modules.project.ant";
        File projectDir = FileUtil.toFile(testingProject.getProjectDirectory());
        ModuleList ml = ModuleList.getModuleList(projectDir);
        ModuleEntry meJP = testingProject.getModuleList().getEntry(cnb);
        ModuleEntry meAnt = testingProject.getModuleList().getEntry(cnb2);
        TestModuleDependency tdJP_001 = new TestModuleDependency(meJP, false, false, true);
        TestModuleDependency tdAnt_111 = new TestModuleDependency(meAnt, true, true, true);
        //add two unit test dependencies
        pxm.addTestDependency(UNIT, tdJP_001);
        pxm.addTestDependency(UNIT, tdAnt_111);
        ProjectManager.getDefault().saveProject(testingProject);
        //try wrong usage of remove
        assertFalse("no such cnb under QA func.", pxm.removeTestDependency(QA_FUNCTIONAL, cnb));
        assertFalse("no such cnb under UNIT func.", pxm.removeTestDependency(UNIT, "someCNB"));
        Set<TestModuleDependency> setBefore = pxm.getTestDependencies(ml).get(UNIT);
        assertEquals("unit test type contains two TD", 2 , setBefore.size());
        //remove first one
        assertTrue("one should be found && removed", pxm.removeTestDependency(UNIT, cnb));        
        ProjectManager.getDefault().saveProject(testingProject);
        //try to remove just removed
        assertFalse("this was just removed", pxm.removeTestDependency(UNIT, cnb));        
        Set<TestModuleDependency> setNow = pxm.getTestDependencies(ml).get(UNIT);
        assertEquals("unit test type contains one TD", 1 , setNow.size());
        //remove last one
        assertTrue("all unit test deps have been removed", pxm.removeTestDependency(UNIT, cnb2));
        ProjectManager.getDefault().saveProject(testingProject);
        Set<TestModuleDependency> setAfter = pxm.getTestDependencies(ml).get(UNIT);
        assertTrue("unit test type is empty now", setAfter.isEmpty());
    }
    
    public void testAddTestDependency () throws Exception{
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager pxm = new ProjectXMLManager(testingProject);
        File projectDir = FileUtil.toFile(testingProject.getProjectDirectory());
        ModuleList ml = ModuleList.getModuleList(projectDir);
        ModuleEntry meJP = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        ModuleEntry meAnt = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.project.ant");
        ModuleEntry meDialogs = testingProject.getModuleList().getEntry(
                "org.openide.dialogs");
        
        TestModuleDependency tdJP_001 = new TestModuleDependency(meJP, false, false, true);
        TestModuleDependency tdJP_010 = new TestModuleDependency(meJP, false, true, false);
        TestModuleDependency tdAnt_111 = new TestModuleDependency(meAnt, true, true, true);
        TestModuleDependency tdDialogs_000 = new TestModuleDependency(meDialogs, false, false, false);
        
        Map<String,Set<TestModuleDependency>> mapOfTD = pxm.getTestDependencies(ml);
        
        assertTrue("currently no TD", mapOfTD.isEmpty());
        //first, add one unit test dep
        pxm.addTestDependency(UNIT, tdJP_001);
        ProjectManager.getDefault().saveProject(testingProject);
        mapOfTD = pxm.getTestDependencies(ml);
        assertEquals("map has already unit test type", 1, mapOfTD.size());
        Set<TestModuleDependency> unitTD = mapOfTD.get(UNIT);
        Set<TestModuleDependency> qafuncTD = mapOfTD.get(QA_FUNCTIONAL);
        assertEquals("set with unit TD has one TD", 1, unitTD.size());
        assertNull("set with qafunc TD does not exist", qafuncTD);
        //now add 2 other  unit test dep;
        pxm.addTestDependency(UNIT, tdDialogs_000);
        ProjectManager.getDefault().saveProject(testingProject);
        pxm.addTestDependency(UNIT, tdAnt_111);
        ProjectManager.getDefault().saveProject(testingProject);
        mapOfTD = pxm.getTestDependencies(ml);
        assertEquals("map still has only unit test type", 1, mapOfTD.size());
        unitTD = mapOfTD.get(UNIT);
        assertEquals("set with unit TD has now three TD", 3, unitTD.size());
        //now add qa-func test dependency
        pxm.addTestDependency(QA_FUNCTIONAL, tdJP_010);
        ProjectManager.getDefault().saveProject(testingProject);
        mapOfTD = pxm.getTestDependencies(ml);
        unitTD = mapOfTD.get(UNIT);
        qafuncTD = mapOfTD.get(QA_FUNCTIONAL);
        assertEquals("map has both test types", 2, mapOfTD.size());
        assertEquals("set with unit TD has still three TD", 3, unitTD.size());
        assertEquals("set with qafunc TD has one TD", 1, qafuncTD.size());
        //TODO: rewrite order checking method to be able to check properly test dependencies order
        validate(testingProject, false);
    }

    
    public void testIssue92363FixAddDependencyWhereSomeIsAlreadyPresent() throws Exception{
        String testDependencies = "\n" +
                "<test-type>\n" +
                "<name>unit</name>\n" +
                "<test-dependency>\n" +
                "<code-name-base>org.netbeans.core</code-name-base>\n" +
                "</test-dependency>\n" +
                "</test-type>\n";
        //create a project that already contains testdependency
        final NbModuleProject testingProject = generateTestingProject(testDependencies);
        final ProjectXMLManager pxm = new ProjectXMLManager(testingProject);
        ModuleList ml = ModuleList.getModuleList(testingProject.getProjectDirectoryFile());
        Map<String,Set<TestModuleDependency>> testDeps = pxm.getTestDependencies(ml);
        assertEquals("map has already unit test type", 1, testDeps.size());
        Set<TestModuleDependency> setUnit = testDeps.get(UNIT);
        assertEquals("contains one dependency", 1, setUnit.size());
        //now add one more testdependency
        ModuleEntry meJP = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        TestModuleDependency tdJP = new TestModuleDependency(meJP, false, false, true);
        pxm.addTestDependency(UNIT, tdJP);
        ProjectManager.getDefault().saveProject(testingProject);
        testDeps = pxm.getTestDependencies(ml);
        assertEquals("map has already unit test type", 1, testDeps.size());
        setUnit = testDeps.get(UNIT);
        assertEquals("contains two dependencies now", 2, setUnit.size());
        validate(testingProject, false);
    }

    /** add dependency from newDepProj to testingProject
     * Used in core/tasklist/.../TodoTest.java
     */
    public static void addDependecy(final NbModuleProject testingProject, NbModuleProject newDepPrj) throws IOException, MutexException, Exception {
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject);
        ModuleEntry me = testingProject.getModuleList().getEntry(
                newDepPrj.getCodeNameBase());
        final ModuleDependency md = new ModuleDependency(me, "1", null, false, true);
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                Element confData = testingProject.getPrimaryConfigurationData();
                Element moduleDependencies = ProjectXMLManager.findModuleDependencies(confData);
                ProjectXMLManager.createModuleDependencyElement(moduleDependencies, md, null);
                ProjectXMLManager.createModuleDependencyElement(moduleDependencies, md, null);
                testingProject.putPrimaryConfigurationData(confData);
                return true;
            }
        });
        assertTrue("adding dependencies", result);
        ProjectManager.getDefault().saveProject(testingProject);
        validate(testingProject, false); // false - we are not using regular way for adding
    }

    private NbModuleProject generateTestingProject() throws Exception {
        return generateTestingProject("");
    }
    
    private NbModuleProject generateTestingProject(final String testDependencies) throws Exception {
        FileObject fo = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "testing");
        FileObject projectXMLFO = fo.getFileObject("nbproject/project.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
                "<code-name-base>org.example.testing</code-name-base>\n" +
                "<standalone/>\n" +
                "<module-dependencies>\n" +
                "<dependency>\n" +
                "<code-name-base>" + DIALOGS + "</code-name-base>\n" +
                "<build-prerequisite/>\n" +
                "<compile-dependency/>\n" +
                "<run-dependency>\n" +
                "<specification-version>6.2</specification-version>\n" +
                "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                "<code-name-base>" + ANT_PROJECT_SUPPORT + "</code-name-base>\n" +
                "<build-prerequisite/>\n" +
                "<compile-dependency/>\n" +
                "<run-dependency>\n" +
                "<release-version>1</release-version>\n" +
                "<specification-version>1.10</specification-version>\n" +
                "</run-dependency>\n" +
                "</dependency>\n" +
                "</module-dependencies>\n" +
                "<test-dependencies>"+ testDependencies + "</test-dependencies>\n" +
                "<friend-packages>\n" +
                "<friend>org.module.examplemodule</friend>\n" +
                "<package>org.netbeans.examples.modules.misc</package>\n" +
                "</friend-packages>\n" +
                "<class-path-extension>\n" +
                "<runtime-relative-path>ext/jsr88javax.jar</runtime-relative-path>\n" +
                "<binary-origin>../external/jsr88javax.jar</binary-origin>\n" +
                "</class-path-extension>\n" +
                "</data>\n" +
                "</configuration>\n" +
                "</project>\n";
        TestBase.dump(projectXMLFO, xml);
        return (NbModuleProject) ProjectManager.getDefault().findProject(fo);
    }
    
    // below is stolen from ant/freeform
    private static String[] getSchemas() throws Exception {
        String[] URIs = new String[3];
        URIs[0] = ProjectXMLManager.class.getResource("resources/nb-module-project2.xsd").toExternalForm();
        URIs[1] = ProjectXMLManager.class.getResource("resources/nb-module-project3.xsd").toExternalForm();
        URIs[2] = AntBasedProjectFactorySingleton.class.getResource("project.xsd").toExternalForm();
        return URIs;
    }
    
    public static void validate(final Project proj, final boolean checkOrder) throws Exception {
        File projF = FileUtil.toFile(proj.getProjectDirectory());
        File xml = new File(new File(projF, "nbproject"), "project.xml");
        SAXParserFactory f = (SAXParserFactory)Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        if (f == null) {
            System.err.println("Validation skipped because org.apache.xerces.jaxp.SAXParserFactoryImpl was not found on classpath");
            return;
        }
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getSchemas());
        try {
            p.parse(xml.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            fail("Validation of XML document " + xml + " against schema failed. Details: " +
                    e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage());
        }
        if (checkOrder) {
            checkDependenciesOrder(proj);
        }
    }
    
    private static void checkDependenciesOrder(final Project proj) throws Exception {
        FileObject projectXML = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
        BufferedReader r = new BufferedReader(new InputStreamReader(projectXML.getInputStream()));
        try {
            String previousCNB = null;
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.matches("<code-name-base>.+</code-name-base>")) {
                    String currentCNB = line.substring(16, line.length() - 17);
                    assertTrue("dependencies order, previous = \"" + previousCNB + "\", current = \"" + currentCNB + "\"",
                            previousCNB == null || previousCNB.compareTo(currentCNB) < 0);
                    previousCNB = currentCNB;
                }
            }
        } finally {
            r.close();
        }
    }
    
    private static final class Handler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
        
}

