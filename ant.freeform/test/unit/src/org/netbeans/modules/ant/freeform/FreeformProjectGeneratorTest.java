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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests for FreeformProjectGenerator.
 *
 * @author David Konecny
 */
public class FreeformProjectGeneratorTest extends NbTestCase {

    private File lib1;
    private File lib2;
    private File src;
    private File test;
    
    public FreeformProjectGeneratorTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public AntProjectHelper createEmptyProject(String projectFolder, String projectName) throws Exception {
        File base = new File(getWorkDir(), projectFolder);
        base.mkdir();
        File antScript = new File(base, "build.xml");
        antScript.createNewFile();
        src = new File(base, "src");
        src.mkdir();
        test = new File(base, "test");
        test.mkdir();
        File libs = new File(base, "libs");
        libs.mkdir();
        lib1 = new File(libs, "some.jar");
        createRealJarFile(lib1);
        lib2 = new File(libs, "some2.jar");
        createRealJarFile(lib2);
        
// XXX: might need to call refresh here??
//        FileObject fo = FileUtil.toFileObject(getWorkDir());
//        fo.refresh();
        
        ArrayList sources = new ArrayList();
        ArrayList compUnits = new ArrayList();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, projectName, null);
        return helper;
    }
    
    public void testCreateProject() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj1", "proj-1");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ProjectInformation pi = (ProjectInformation)p.getLookup().lookup(ProjectInformation.class);
        assertEquals("Project name was not set", "proj-1", pi.getName());
    }
    
    public void testTargetMappings() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj2", "proj-2");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ActionProvider ap = (ActionProvider)p.getLookup().lookup(ActionProvider.class);
        assertNotNull("Project does not have ActionProvider", ap);
        assertEquals("Project cannot have any action", 0, ap.getSupportedActions().length);
        
        List list = FreeformProjectGenerator.getTargetMappings(helper);
        assertNotNull("getTargetMappings() cannot return null", list);
        assertEquals("Project cannot have any action", 0, list.size());
        
        list = new ArrayList();
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "clean";
        tm.targets = Collections.singletonList("clean-target");
        list.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "build";
        tm.targets = Collections.singletonList("build-target");
        tm.script = "${ant.script.two}";
        list.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "rebuild";
        tm.targets = Arrays.asList(new String[]{"clean-target", "build-target"});
        tm.script = "${ant.script.three}";
        list.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        FreeformProjectGenerator.putTargetMappings(helper, list);
        List list2 = FreeformProjectGenerator.getTargetMappings(helper);
        // once again: put and get
        FreeformProjectGenerator.putTargetMappings(helper, list2);
        list2 = FreeformProjectGenerator.getTargetMappings(helper);
        assertNotNull("getTargetMappings() cannot return null", list2);
        assertEquals("Project must have 3 actions", 3, list2.size());
        assertEquals("Script was not correctly saved", null, ((FreeformProjectGenerator.TargetMapping)list2.get(0)).script);
        assertEquals("Script was not correctly saved", "${ant.script.two}", ((FreeformProjectGenerator.TargetMapping)list2.get(1)).script);
        assertEquals("Script was not correctly saved", "${ant.script.three}", ((FreeformProjectGenerator.TargetMapping)list2.get(2)).script);
        assertEquals("Project must have 3 actions plus 4 extras (run, javadoc, test, redeploy): " + Arrays.asList(ap.getSupportedActions()), 7, ap.getSupportedActions().length);
        assertTrue("Action clean must be enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        assertTrue("Action build must be enabled", ap.isActionEnabled("build", Lookup.EMPTY));
        assertTrue("Action rebuild must be enabled", ap.isActionEnabled("rebuild", Lookup.EMPTY));
        boolean ok = false;
        try {
            assertFalse("Action twiddle must be disabled", ap.isActionEnabled("twiddle", Lookup.EMPTY));
        } catch (IllegalArgumentException ex) {
            ok = true;
        }
        assertTrue("Exception must be thrown for non-existing actions", ok);
        ProjectManager.getDefault().saveAllProjects();
    }

    public void testRawTargetMappings() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List mappings = new ArrayList();
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "first-targetName";
        tm.script = "antScript";
        tm.targets = new ArrayList();
        tm.targets.add("target-1");
        tm.targets.add("target-2");
        tm.targets.add("target-3");
        tm.properties = new EditableProperties(false);
        tm.properties.setProperty("k1", "v1");
        tm.properties.setProperty("k2", "v2");
        FreeformProjectGenerator.TargetMapping.Context ctx = new FreeformProjectGenerator.TargetMapping.Context();
        ctx.folder = "someFolder1";
        ctx.format = "relative-path";
        ctx.property = "someProperty1";
        ctx.pattern = "somePattern1";
        tm.context = ctx;
        mappings.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "second-targetName";
        tm.script = "second-antScript";
        tm.targets = new ArrayList();
        tm.targets.add("second-target-1");
        tm.targets.add("second-target-2");
        tm.targets.add("second-target-3");
        tm.properties = new EditableProperties(false);
        tm.properties.setProperty("second-k1", "second-v1");
        tm.properties.setProperty("second-k2", "second-v2");
        ctx = new FreeformProjectGenerator.TargetMapping.Context();
        ctx.folder = "second-someFolder1";
        ctx.format = "java-name";
        ctx.property = "second-someProperty1";
        ctx.separator = "someSeparator1";
        tm.context = ctx;
        mappings.add(tm);
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
        // test getter and setter here:
        mappings = FreeformProjectGenerator.getTargetMappings(helper);
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        Element el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "ide-actions", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        List subElements = Util.findSubElements(el);
        assertEquals(2, subElements.size());
        // compare first target mapping
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "action", null, "name", "first-targetName");
        List l1 = Util.findSubElements(el2);
        assertEquals(7, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "target", "target", "target", "property", "property", "context"}, 
            new String[]{"antScript", "target-1", "target-2", "target-3", "v1", "v2", null});
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "v1", "name", "k1");
        el2 = (Element)l1.get(5);
        assertElement(el2, "property", "v2", "name", "k2");
        el2 = (Element)l1.get(6);
        List l2 = Util.findSubElements(el2);
        assertEquals(5, l2.size());
        assertElementArray(l2, 
            new String[]{"property", "folder", "pattern", "format", "arity"}, 
            new String[]{"someProperty1", "someFolder1", "somePattern1", "relative-path", null});
        assertNotNull("have <one-file-only>", Util.findElement((Element) l2.get(4), "one-file-only", FreeformProjectType.NS_GENERAL));
        // compare second target mapping
        el2 = (Element)subElements.get(1);
        assertElement(el2, "action", null, "name", "second-targetName");
        l1 = Util.findSubElements(el2);
        assertEquals(7, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "target", "target", "target", "property", "property", "context"},
            new String[]{"second-antScript", "second-target-1", "second-target-2", "second-target-3", "second-v1", "second-v2", null});
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "second-v1", "name", "second-k1");
        el2 = (Element)l1.get(5);
        assertElement(el2, "property", "second-v2", "name", "second-k2");
        el2 = (Element)l1.get(6);
        l2 = Util.findSubElements(el2);
        assertEquals(4, l2.size());
        assertElementArray(l2, 
            new String[]{"property", "folder", "format", "arity"}, 
            new String[]{"second-someProperty1", "second-someFolder1", "java-name", null});
        Element sepFilesEl = Util.findElement((Element) l2.get(3), "separated-files", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <separated-files>", sepFilesEl);
        assertEquals("right separator", "someSeparator1", Util.findText(sepFilesEl));
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
            
        // test updating
            
        mappings = new ArrayList();
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "foo";
        tm.script = "antScript";
        tm.targets = new ArrayList();
        tm.targets.add("target-1");
        tm.targets.add("target-2");
        mappings.add(tm);
        tm.properties = new EditableProperties(false);
        tm.properties.setProperty("key1", "value1");
        tm.properties.setProperty("key2", "value2");
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "ide-actions", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        subElements = Util.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare first target mapping
        el2 = (Element)subElements.get(0);
        assertElement(el2, "action", null, "name", "foo");
        l1 = Util.findSubElements(el2);
        assertEquals(5, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "target", "target", "property", "property"}, 
            new String[]{"antScript", "target-1", "target-2", "value1", "value2"});
        el2 = (Element)l1.get(3);
        assertElement(el2, "property", "value1", "name", "key1");
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "value2", "name", "key2");
        mappings = new ArrayList();
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "foo";
        tm.script = "diff-script";
        tm.targets = new ArrayList();
        tm.targets.add("target-1");
        tm.targets.add("target-B");
        tm.properties = new EditableProperties(false);
        tm.properties.setProperty("key-1", "value-1");
        tm.properties.setProperty("key-2", "value-2");
        mappings.add(tm);
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "ide-actions", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        subElements = Util.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare first target mapping
        el2 = (Element)subElements.get(0);
        assertElement(el2, "action", null, "name", "foo");
        l1 = Util.findSubElements(el2);
        assertEquals(5, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "target", "target", "property", "property"}, 
            new String[]{"diff-script", "target-1", "target-B", "value-1", "value-2"});
        el2 = (Element)l1.get(3);
        assertElement(el2, "property", "value-1", "name", "key-1");
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "value-2", "name", "key-2");
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
    }

    public void testRawContextMenuActions() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List mappings = new ArrayList();
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "first-targetName";
        mappings.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "second-targetName";
        mappings.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "context-sensitive";
        tm.context = new FreeformProjectGenerator.TargetMapping.Context();
        mappings.add(tm);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        Element el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        el = Util.findElement(el, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        List subElements = Util.findSubElements(el);
        assertEquals(2, subElements.size());
        assertElementArray(subElements, 
            new String[]{"ide-action", "ide-action"}, 
            new String[]{null, null},
            new String[]{"name", "name"}, 
            new String[]{"first-targetName", "second-targetName"}
            );
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
            
        // test updating
            
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "foo";
        mappings.add(tm);
        tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "bar";
        mappings.add(tm);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        el = Util.findElement(el, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        subElements = Util.findSubElements(el);
        assertEquals(4, subElements.size());
        assertElementArray(subElements, 
            new String[]{"ide-action", "ide-action", "ide-action", "ide-action"},
            new String[]{null, null, null, null},
            new String[]{"name", "name", "name", "name"}, 
            new String[]{"first-targetName", "second-targetName", "foo", "bar"}
            );
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
    }

    public void testRawCustomContextMenuActions() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List customActions = new ArrayList();
        FreeformProjectGenerator.CustomTarget ct = new FreeformProjectGenerator.CustomTarget();
        ct.label = "customAction1";
        ct.script = "customScript1";
        ct.targets = new ArrayList();
        ct.targets.add("customTarget1");
        ct.targets.add("customTarget2");
        ct.properties = new EditableProperties(false);
        ct.properties.setProperty("k1", "v1");
        ct.properties.setProperty("k2", "v2");
        customActions.add(ct);
        ct = new FreeformProjectGenerator.CustomTarget();
        ct.label = "customAction2";
        ct.script = "customScript2";
        ct.targets = new ArrayList();
        ct.targets.add("second-customTarget1");
        ct.targets.add("second-customTarget2");
        ct.properties = new EditableProperties(false);
        ct.properties.setProperty("kk1", "vv1");
        ct.properties.setProperty("kk2", "vv2");
        customActions.add(ct);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
        // test getter and setter here:
        customActions = FreeformProjectGenerator.getCustomContextMenuActions(helper);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
//        ProjectManager.getDefault().saveAllProjects();
        Element el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        el = Util.findElement(el, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        List subElements = Util.findSubElements(el);
        assertEquals(2, subElements.size());
        assertElementArray(subElements, 
            new String[]{"action", "action"}, 
            new String[]{null, null});
        // compare first custom action
        Element el2 = (Element)subElements.get(0);
        List l1 = Util.findSubElements(el2);
        assertEquals(6, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "label", "target", "target", "property", "property"}, 
            new String[]{"customScript1", "customAction1", "customTarget1", "customTarget2", "v1", "v2"});
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "v1", "name", "k1");
        el2 = (Element)l1.get(5);
        assertElement(el2, "property", "v2", "name", "k2");
        // compare second custom action
        el2 = (Element)subElements.get(1);
        l1 = Util.findSubElements(el2);
        assertEquals(6, l1.size());
        assertElementArray(l1, 
            new String[]{"script", "label", "target", "target", "property", "property"}, 
            new String[]{"customScript2", "customAction2", "second-customTarget1", "second-customTarget2", "vv1", "vv2"});
        el2 = (Element)l1.get(4);
        assertElement(el2, "property", "vv1", "name", "kk1");
        el2 = (Element)l1.get(5);
        assertElement(el2, "property", "vv2", "name", "kk2");
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
            
        // test updating
            
        customActions = new ArrayList();
        ct = new FreeformProjectGenerator.CustomTarget();
        ct.label = "fooLabel";
        customActions.add(ct);
        ct = new FreeformProjectGenerator.CustomTarget();
        ct.label = "barLabel";
        customActions.add(ct);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
//        ProjectManager.getDefault().saveAllProjects();
        el = helper.getPrimaryConfigurationData(true);
        el = Util.findElement(el, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        el = Util.findElement(el, "context-menu", FreeformProjectType.NS_GENERAL);
        assertNotNull("Target mapping were not saved correctly",  el);
        subElements = Util.findSubElements(el);
        assertEquals(2, subElements.size());
        assertElementArray(subElements, 
            new String[]{"action", "action"}, 
            new String[]{null, null});
        // compare first custom action
        el2 = (Element)subElements.get(0);
        l1 = Util.findSubElements(el2);
        assertEquals(1, l1.size());
        assertElementArray(l1, 
            new String[]{"label"}, 
            new String[]{"fooLabel"});
        // compare second custom action
        el2 = (Element)subElements.get(1);
        l1 = Util.findSubElements(el2);
        assertEquals(1, l1.size());
        assertElementArray(l1, 
            new String[]{"label"}, 
            new String[]{"barLabel"});
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
    }

    /**
     * Asserts that given Element has expected name and its text match expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     */
    public static void assertElement(Element element, String expectedName, String expectedValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+expectedValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, Util.findText(element));
        }
    }

    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names. 
     */
    public static void assertElementArray(List/*<Element>*/ elements, String[] expectedNames, String[] expectedValues) {
        for (int i=0; i<elements.size(); i++) {
            assertElement((Element)elements.get(i), expectedNames[i], expectedValues[i]);
        }
    }
    
    /**
     * Asserts that given Element has expected name and its text match expected value and
     * it also has expect attribute with expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     * @param expectedAttrName expected name of attribute; cannot be null
     * @param expectedAttrValue expected value of attribute; cannot be null
     */
    public static void assertElement(Element element, String expectedName, String expectedValue, String expectedAttrName, String expectedAttrValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+
            expectedValue+", attr="+expectedAttrName+", attrvalue="+expectedAttrValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, Util.findText(element));
        }
        String val = element.getAttribute(expectedAttrName);
        assertEquals(expectedAttrValue, val);
    }
    
    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names
     * and expected attributes.
     */
    public static void assertElementArray(List/*<Element>*/ elements, String[] expectedNames, String[] expectedValues, String[] expectedAttrName, String[] expectedAttrValue) {
        assertEquals(expectedNames.length, elements.size());
        for (int i=0; i<elements.size(); i++) {
            assertElement((Element)elements.get(i), expectedNames[i], expectedValues[i], expectedAttrName[i], expectedAttrValue[i]);
        }
    }


    // create real Jar otherwise FileUtil.isArchiveFile returns false for it
    public void createRealJarFile(File f) throws Exception {
        OutputStream os = new FileOutputStream(f);
        try {
            JarOutputStream jos = new JarOutputStream(os);
//            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry("foo.txt");
//            entry.setSize(0L);
//            entry.setTime(System.currentTimeMillis());
//            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
    }

    private static String[] getSchemas() throws Exception {
        String[] URIs = new String[2];
        URIs[0] = FreeformProjectGenerator.class.getResource("resources/freeform-project-general.xsd").toExternalForm();
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
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getSchemas());
        try {
            p.parse(xml.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            assertTrue("Validation of XML document "+xml+" against schema failed. Details: "+
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
