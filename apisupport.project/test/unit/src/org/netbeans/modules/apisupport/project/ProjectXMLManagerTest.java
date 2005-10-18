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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests ProjectXMLManager class.
 *
 * @author Martin Krauskopf
 */
public class ProjectXMLManagerTest extends TestBase {
    
    private final static String ANT_PROJECT_SUPPORT = "org.netbeans.modules.project.ant";
    private final static String DIALOGS = "org.openide.dialogs";
    private final static Set ASSUMED_CNBS;
    
    static {
        Set assumedCNBs = new HashSet(2);
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
                findProject(nbroot.getFileObject("libs/xerces"));
        return new ProjectXMLManager(xercesPrj.getHelper());
    }
    
    // sanity check
    public void testGeneratedProject() throws Exception {
        validate(generateTestingProject());
    }
    
    public void testGetCodeNameBase() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        assertEquals("action-project cnb", "org.example.module1", p.getCodeNameBase());
    }
    
    public void testGetDirectDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        Set deps = testingPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 2, deps.size());
        
        Set assumedCNBs = new HashSet(ASSUMED_CNBS);
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
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
    
    public void testRemoveDependency() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                testingPXM.removeDependency(DIALOGS);
                return Boolean.TRUE;
            }
        });
        assertTrue("removing dependency", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        
        final Set newDeps = testingPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 1, newDeps.size());
        Set newCNBs = new HashSet();
        newCNBs.add(ANT_PROJECT_SUPPORT);
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            String cnbToRemove = md.getModuleEntry().getCodeNameBase();
            assertTrue("unknown dependency: " + cnbToRemove, newCNBs.remove(cnbToRemove));
        }
        assertTrue("following dependencies were found: " + newCNBs, newCNBs.isEmpty());
        validate(testingProject);
    }
    
    public void testEditDependency() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        final Set deps = testingPXM.getDirectDependencies(null);
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                boolean tested = false;
                for (Iterator it = deps.iterator(); it.hasNext(); ) {
                    ModuleDependency origDep = (ModuleDependency) it.next();
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
                return Boolean.TRUE;
            }
        });
        assertTrue("editing dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        // XXX this refresh shouldn't be needed (should listen on project.xml changes)
        ProjectXMLManager freshTestingPXM = new ProjectXMLManager(testingProject.getHelper());
        
        final Set newDeps = freshTestingPXM.getDirectDependencies(null);
        boolean tested = false;
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            if (DIALOGS.equals(md.getModuleEntry().getCodeNameBase())) {
                tested = true;
                assertEquals("edited release version", "2", md.getReleaseVersion());
                assertEquals("unedited specification version", "6.2", md.getSpecificationVersion());
                break;
            }
        }
        assertTrue("org.openide.dialogs dependency tested", tested);
        validate(testingProject);
    }
    
    public void testAddDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        final Set newDeps = new HashSet();
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
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                testingPXM.addDependencies(newDeps);
                return Boolean.TRUE;
            }
        });
        assertTrue("adding dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        
        Set deps = testingPXM.getDirectDependencies(null);
        
        Set assumedCNBs = new HashSet(ASSUMED_CNBS);
        assumedCNBs.add("org.netbeans.modules.java.project");
        assumedCNBs.add("org.netbeans.modules.java.j2seplatform");
        
        assertEquals("number of dependencies", deps.size(), assumedCNBs.size());
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
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
        validate(testingProject);
    }
    
    public void testExceptionWhenAddingTheSameModuleDependencyTwice() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        ModuleEntry me = testingProject.getModuleList().getEntry(
                "org.netbeans.modules.java.project");
        final ModuleDependency md = new ModuleDependency(me, "1", null, false, true);
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                testingPXM.addDependency(md);
                testingPXM.addDependency(md);
                return Boolean.TRUE;
            }
        });
        assertTrue("adding dependencies", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        try {
            testingPXM.getDirectDependencies(null);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException ise) {
            // OK, expected exception was thrown
        }
        validate(testingProject);
    }
    
    public void testFindPublicPackages() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final File projectXML = FileUtil.toFile(
                testingProject.getProjectDirectory().getFileObject("nbproject/project.xml"));
        assert projectXML.exists();
        Element confData = (Element) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
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
        validate(testingProject);
    }
    
    public void testReplaceDependencies() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        final Set deps = testingPXM.getDirectDependencies(null);
        assertEquals("number of dependencies", 2, deps.size());
        ModuleDependency newOO = null;
        ModuleDependency oldOO = null;
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
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
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                testingPXM.replaceDependencies(deps);
                return Boolean.TRUE;
            }
        });
        assertTrue("project successfully saved", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        validate(testingProject);
        
        final ProjectXMLManager newTestingPXM = new ProjectXMLManager(testingProject.getHelper());
        final Set newDeps = newTestingPXM.getDirectDependencies(null);
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
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
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
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
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        ManifestManager.PackageExport[] publicPackages = testingPXM.getPublicPackages();
        assertEquals("number of public packages", 1, publicPackages.length);
        final String[] newPP = new String[] { publicPackages[0].getPackage(), "org.netbeans.examples.modules" };
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                testingPXM.replacePublicPackages(newPP);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace public packages", result.booleanValue());
        assertTrue("replace public packages", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        ManifestManager.PackageExport[] newPublicPackages = testingPXM.getPublicPackages();
        assertEquals("number of new public packages", 2, newPublicPackages.length);
        Collection newPPs = Arrays.asList(new String[] {"org.netbeans.examples.modules", "org.netbeans.examples.modules.misc"});
        assertTrue(newPPs.contains(newPublicPackages[0].getPackage()));
        assertTrue(newPPs.contains(newPublicPackages[1].getPackage()));
        assertNull("there must not be friend", testingPXM.getFriends());
        validate(testingProject);
    }
    
    public void testReplaceFriends() throws Exception {
        final NbModuleProject testingProject = generateTestingProject();
        final ProjectXMLManager testingPXM = new ProjectXMLManager(testingProject.getHelper());
        assertEquals("one friend", 1, testingPXM.getFriends().length);
        assertEquals("friend org.module.examplemodule", "org.module.examplemodule", testingPXM.getFriends()[0]);
        final String[] newFriends = new String[] { "org.exampleorg.somefriend", "org.exampleorg.anotherfriend" };
        
        // apply and save project
        Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            public Object run() throws IOException {
                ManifestManager.PackageExport pkgs[] = testingPXM.getPublicPackages();
                String[] packagesToExpose = new String[pkgs.length];
                for (int i = 0; i < pkgs.length; i++) {
                    packagesToExpose[i] = pkgs[i].getPackage();
                }
                testingPXM.replaceFriends(newFriends, packagesToExpose);
                return Boolean.TRUE;
            }
        });
        assertTrue("replace friends", result.booleanValue());
        ProjectManager.getDefault().saveProject(testingProject);
        final ProjectXMLManager newTestingPXM = new ProjectXMLManager(testingProject.getHelper());
        String[] actualFriends = newTestingPXM.getFriends();
        assertEquals("number of new friend", 2, actualFriends.length);
        Collection newFriendsCNBs = Arrays.asList(actualFriends);
        assertTrue(newFriendsCNBs.contains(newFriends[0]));
        assertTrue(newFriendsCNBs.contains(newFriends[1]));
        assertEquals("public packages", 1, newTestingPXM.getPublicPackages().length);
        validate(testingProject);
    }
    
    public void testGetBinaryOrigins() throws Exception {
        ProjectXMLManager xercesPXM = createXercesPXM();
        assertEquals("number of binary origins", 2, xercesPXM.getBinaryOrigins().length);
    }
    
    public void testThatFriendPackagesAreGeneratedInTheRightOrder_61882() throws Exception {
        FileObject fo = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "testing");
        FileObject projectXMLFO = fo.getFileObject("nbproject/project.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">\n" +
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
        validate(project);
    }
    
    private NbModuleProject generateTestingProject() throws Exception {
        FileObject fo = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "testing");
        FileObject projectXMLFO = fo.getFileObject("nbproject/project.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/2\">\n" +
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
        String[] URIs = new String[2];
        URIs[0] = ProjectXMLManager.class.getResource("resources/nb-module-project2.xsd").toExternalForm();
        URIs[1] = AntBasedProjectFactorySingleton.class.getResource("project.xsd").toExternalForm();
        return URIs;
    }
    
    public static void validate(Project proj) throws Exception {
        File projF = FileUtil.toFile(proj.getProjectDirectory());
        File xml = new File(new File(projF, "nbproject"), "project.xml");
        SAXParserFactory f = (SAXParserFactory)Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        if (f == null) {
            System.err.println("Validation skipped because org.apache.xerces.jaxp.SAXParserFactoryImpl was not found on classpath");
            return;
        }
        f.setNamespaceAware(true);
        f.setValidating(true);
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.XML11Configuration");// XXX #66967
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getSchemas());
        try {
            p.parse(xml.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            assertTrue("Validation of XML document " + xml + " against schema failed. Details: " +
                    e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage(), false);
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

