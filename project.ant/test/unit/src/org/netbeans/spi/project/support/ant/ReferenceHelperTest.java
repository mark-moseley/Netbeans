/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/* XXX tests needed
 * - testAddArtifactCollocation
 * check that collocated foreign projects update project.properties
 * while noncollocated foreign projects update private.properties
 * and that absolute artifact locns -> private.properties
 * also addForeignFileReference(File,String) on plain files needs to
 * check collocation
 * - testUniquifyProjectNames
 * check that foreign project names are uniquified correctly
 * both in addReference and addForeignFileReference etc.
 * - testVcsFriendliness
 * check that refs are added in a predictable order
 */

/**
 * Test functionality of ReferenceHelper.
 * @author Jesse Glick
 */
public class ReferenceHelperTest extends NbTestCase {

    /**
     * Create the test suite.
     * @param name suite name
     */
    public ReferenceHelperTest(String name) {
        super(name);
    }
    
    /** Scratch directory. */
    private FileObject scratch;
    /** Directory of master project (proj). */
    private FileObject projdir;
    /**
     * Directory of a collocated sister project (proj2).
     * Has artifacts build.jar=dist/proj2.jar and
     * build.javadoc=build/javadoc as well as
     * build.javadoc.complete=build/complete-javadoc.
     */
    private FileObject sisterprojdir;
    /**
     * The same structure as sisterprojdir but in different folder.
     * Useful for testing that referenceIDs are uniquified.
     */
    private FileObject sisterprojdir2;
    /**
     * Directory of a noncollocated project (proj3).
     * Has artifact build.jar=d i s t/p r o j 3.jar.
     */
    private FileObject sepprojdir;
    /** The project manager singleton. */
    private ProjectManager pm;
    /** The master project. */
    private Project p;
    /** The master project's helper. */
    private AntProjectHelper h;
    /** The collocated sister project's helper. */
    private AntProjectHelper sisterh;
    /** The collocated sister2 project's helper. */
    private AntProjectHelper sisterh2;
    /** The noncollocated project's helper. */
    private AntProjectHelper seph;
    /** The master project's reference helper. */
    private ReferenceHelper r;
    //private AntBasedTestUtil.TestListener l;
    private PropertyEvaluator pev;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            AntBasedTestUtil.testAntBasedProjectType(),
            AntBasedTestUtil.testCollocationQueryImplementation(getWorkDir()),
        }, ReferenceHelperTest.class.getClassLoader());
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(ReferenceHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        assertNotNull("found project in " + projdir, p);
        h = (AntProjectHelper)p.getLookup().lookup(AntProjectHelper.class);
        assertNotNull("found helper for " + p, h);
        r = (ReferenceHelper)p.getLookup().lookup(ReferenceHelper.class);
        assertNotNull("found ref helper for " + p, r);
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        assertTrue("projdir and sisterprojdir collocated",
            CollocationQuery.areCollocated(FileUtil.toFile(projdir), FileUtil.toFile(sisterprojdir)));
        sisterh = ProjectGenerator.createProject(sisterprojdir, "test", "proj2");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        props.setProperty("build.javadoc.complete", "build/complete-javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        // Probably unnecessary: pm.saveProject(pm.findProject(sisterprojdir));
        
        sisterprojdir2 = FileUtil.createFolder(scratch, "proj2-copy");
        assertTrue("projdir and sisterprojdir2 collocated",
            CollocationQuery.areCollocated(FileUtil.toFile(projdir), FileUtil.toFile(sisterprojdir2)));
        sisterh2 = ProjectGenerator.createProject(sisterprojdir2, "test", "proj2");
        props = sisterh2.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        props.setProperty("build.javadoc.complete", "build/complete-javadoc");
        sisterh2.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        
        sepprojdir = FileUtil.createFolder(scratch, "separate/proj3");
        // If following assert fails then there is a global impl of collocation
        // query which says that these two files are collocated. Hidden it
        // similarly as it is done for SampleCVSCollocationQueryImpl
        // in META-INF/services.
        assertFalse("projdir and separate cannot be collocated",
            CollocationQuery.areCollocated(FileUtil.toFile(projdir), FileUtil.toFile(sepprojdir)));
        seph = ProjectGenerator.createProject(sepprojdir, "test", "proj3");
        props = seph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "d i s t/p r o j 3.jar");
        seph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        //l = new AntBasedTestUtil.TestListener();
        
        pev = h.getStandardPropertyEvaluator();
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sisterprojdir = null;
        sisterh = null;
        //sepprojdir = null;
        pm = null;
        p = null;
        h = null;
        //l = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    /**
     * Check that the raw add, get, and remove calls work.
     * @throws Exception in case of unexpected failures
     */
    public void testRawReferenceManipulation() throws Exception {
        assertEquals("starting with no raw references", Collections.EMPTY_LIST, Arrays.asList(r.getRawReferences()));
        // Test simple adding of a reference.
        ReferenceHelper.RawReference ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("successfully added a raw ref to otherproj.dojar", r.addRawReference(ref));
        assertNull("project.properties not changed", pev.getProperty("project.otherproj"));
        assertTrue("project is modified", pm.isModified(p));
        ref = r.getRawReference("otherproj", "dojarID");
        assertNotNull("found otherproj.dojar", ref);
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Nonexistent references are not returned.
        ref = r.getRawReference("otherproj2", "dojarID");
        assertNull("no such ref otherproj2.dojar", ref);
        ref = r.getRawReference("otherproj", "dojar2");
        assertNull("no such ref otherproj.dojar2", ref);
        ref = r.getRawReference("otherproj2", "dojar2");
        assertNull("no such ref otherproj2.dojar2", ref);
        // The reference is found now.
        ReferenceHelper.RawReference[] refs = r.getRawReferences();
        assertEquals("one reference here", 1, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Test removing it.
        assertTrue("successfully removed otherproj.dojar", r.removeRawReference("otherproj", "dojarID"));
        refs = r.getRawReferences();
        assertEquals("no references here", 0, refs.length);
        ref = r.getRawReference("otherproj", "dojar");
        assertNull("otherproj.dojar is gone", ref);
        // Test adding several references.
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("added ref to otherproj.dojar", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar2", "clean", "dojar2ID");
        assertTrue("added ref to otherproj.dojar2", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj2", "ear", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("added ref to otherproj2.dojar", r.addRawReference(ref));
        assertEquals("have three refs", 3, r.getRawReferences().length);
        // Test no-op adds and removes.
        pm.saveProject(p);
        assertFalse("project is saved", pm.isModified(p));
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertFalse("already had ref to otherproj.dojar", r.addRawReference(ref));
        assertFalse("project is not modified by no-op add", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        assertFalse("did not have ref to foo.bar", r.removeRawReference("foo", "bar"));
        assertFalse("project is not modified by no-op remove", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        // Test modifications.
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        assertTrue("project is modified by changed ref", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        ref = r.getRawReference("otherproj", "dojarID");
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build2.xml"), "dojar", "clean", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build2.xml"), "dojar", "clean2", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        ref = r.getRawReference("otherproj", "dojarID");
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build2.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean2", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        // More removals and adds.
        assertTrue("now removing otherproj.dojar2", r.removeRawReference("otherproj", "dojar2ID"));
        assertNull("otherproj.dojar2 is gone", r.getRawReference("otherproj", "dojar2ID"));
        assertNotNull("otherproj.jar is still there", r.getRawReference("otherproj", "dojarID"));
        assertNotNull("otherproj2.dojar is still there", r.getRawReference("otherproj2", "dojarID"));
        assertEquals("down to two refs", 2, r.getRawReferences().length);
        ref = new ReferenceHelper.RawReference("aardvark", "jar", URI.create("build.xml"), "jar", "clean", "jarID");
        assertTrue("added ref to aardvark.jar", r.addRawReference(ref));
        // Check list of refs.
        refs = r.getRawReferences();
        assertEquals("back to three refs", 3, refs.length);
        // NOTE on undocumented constraint: getRawReferences should sort results by proj then target
        ref = refs[0];
        assertEquals("correct foreign project name", "aardvark", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "jar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "jarID", ref.getID());
        ref = refs[1];
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build2.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean2", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        ref = refs[2];
        assertEquals("correct foreign project name", "otherproj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "ear", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Try saving and checking that project.xml is correct.
        assertTrue("Project is still modified", pm.isModified(p));
        pm.saveProject(p);
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element config = Util.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("have <configuration>", config);
        Element references = Util.findElement(config, ReferenceHelper.REFS_NAME, ReferenceHelper.REFS_NS);
        assertNotNull("have <references>", references);
        NodeList nl = references.getElementsByTagNameNS(ReferenceHelper.REFS_NS, "reference");
        assertEquals("have three <reference>s", 3, nl.getLength());
        String[] elementNames = {
            "foreign-project",
            "artifact-type",
            "script",
            "target",
            "clean-target",
            "id",
        };
        String[][] values = {
            {
                "aardvark",
                "jar",
                "build.xml",
                "jar",
                "clean",
                "jarID",
            },
            {
                "otherproj",
                "war",
                "build2.xml",
                "dojar",
                "clean2",
                "dojarID",
            },
            {
                "otherproj2",
                "ear",
                "build.xml",
                "dojar",
                "clean",
                "dojarID",
            },
        };
        for (int i = 0; i < 3; i++) {
            Element reference = (Element)nl.item(i);
            for (int j = 0; j < 6; j++) {
                String elementName = elementNames[j];
                Element element = Util.findElement(reference, elementName, ReferenceHelper.REFS_NS);
                assertNotNull("had element " + elementName + " in ref #" + i, element);
                assertEquals("correct text in " + elementName + " in ref #" + i, values[i][j], Util.findText(element));
            }
        }
    }
    
    /**
     * Check that the adding and removing artifact objects updates everything it should.
     * @throws Exception in case of unexpected failures
     */
    public void testAddRemoveArtifact() throws Exception {
        // Add one artifact. Check that the raw reference is there.
        assertFalse("project not initially modified", pm.isModified(p));
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertTrue("added a ref to proj2.dojar", r.addReference(art));
        assertTrue("project now modified", pm.isModified(p));
        ReferenceHelper.RawReference[] refs = r.getRawReferences();
        assertEquals("one ref now", 1, refs.length);
        ReferenceHelper.RawReference ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        // Check that the project properties are correct.
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("correct evaluated ${project.proj2}",
            FileUtil.toFile(sisterprojdir),
            h.resolveFile(pev.getProperty("project.proj2")));
        assertEquals("correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct evaluated ${reference.proj2.dojar}",
            new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "proj2.jar"),
            h.resolveFile(pev.getProperty("reference.proj2.dojar")));
        // Check no-op adds.
        pm.saveProject(p);
        assertFalse("no-op add", r.addReference(art));
        assertFalse("project not modified by no-op add", pm.isModified(p));
        // Try another artifact from the same project.
        art = sisterh.createSimpleAntArtifact("javadoc", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertTrue("added a ref to proj2.dojavadoc", r.addReference(art));
        assertTrue("project now modified", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("two refs now", 2, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct artifact type", "javadoc", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("still correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        pm.saveProject(p);
        assertFalse("no-op add", r.addReference(art));
        assertFalse("project not modified by no-op add", pm.isModified(p));
        // Try modifying the second artifact in some way.
        // Note that only changes in the type, clean target, and artifact path count as modifications.
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertTrue("successful modification of proj2.dojavadoc by type", r.addReference(art));
        assertTrue("project modified by ref mod", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("still two refs", 2, refs.length);
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "realclean");
        assertTrue("successful modification of proj2.dojavadoc by clean target", r.addReference(art));
        pm.saveProject(p);
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc.complete", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "realclean");
        assertTrue("successful modification of proj2.dojavadoc by artifact location property", r.addReference(art));
        assertTrue("project modified by ref mod", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("still two refs", 2, refs.length);
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct modified clean target name", "realclean", ref.getCleanTargetName());
        // Check that changing the artifact location property changed the reference property too.
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("still correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/complete-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "complete-javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        // Check that changing the value of the artifact location property
        // in the subproject modifies this project.
        pm.saveProject(p);
        assertFalse("no-op add", r.addReference(art));
        assertFalse("project not modified by no-op add", pm.isModified(p));
        props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.javadoc.complete", "build/total-javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertTrue("add ref modifying just because artifact location changed", r.addReference(art));
        assertTrue("project modified by new ${reference.proj2.dojavadoc}", pm.isModified(p));
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/total-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "total-javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        // Now try removing first ref. Should remove raw ref, ref property, but not project property.
        pm.saveProject(p);
        assertTrue("remove proj2.dojar succeeded", r.removeReference("proj2", "dojar"));
        assertTrue("remove ref modified project", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("now have just one ref", 1, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct modified clean target name", "realclean", ref.getCleanTargetName());
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("no more ${reference.proj2.dojar}", null,
            props.getProperty("reference.proj2.dojar"));
        assertEquals("still correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/total-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        pm.saveProject(p);
        assertFalse("no-op remove proj2.dojar failed", r.removeReference("proj2", "dojar"));
        assertFalse("no-op remove did not modify project", pm.isModified(p));
        // Try removing second ref. Should now remove project property.
        assertTrue("remove proj2.dojavadoc succeeded", r.removeReference("proj2", "dojavadoc"));
        assertTrue("remove ref modified project", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("now have no refs", 0, refs.length);
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("no more ${project.proj2}", null,
            props.getProperty("project.proj2"));
        assertEquals("no more ${reference.proj2.dojar}", null,
            props.getProperty("reference.proj2.dojar"));
        assertEquals("no more ${reference.proj2.dojavadoc}", null,
            props.getProperty("reference.proj2.dojavadoc"));
        // XXX check add ref not coming from project gives IAE
    }

    /**
     * Check that the {@link SubprojectProvider} implementation behaves correctly.
     * @throws Exception in case of unexpected failures
     */
    public void testSubprojectProviderImpl() throws Exception {
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertTrue("added a ref to proj2.dojar", r.addReference(art));
        art = sisterh.createSimpleAntArtifact("javadoc", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertTrue("added a ref to proj2.dojavadoc", r.addReference(art));
        art = seph.createSimpleAntArtifact("jar", "build.jar", seph.getStandardPropertyEvaluator(), "dojar", "clean");
        assertTrue("added a ref to proj3.dojar", r.addReference(art));
        SubprojectProvider sp = r.createSubprojectProvider();
        Set/*<Project>*/ subprojs = sp.getSubprojects();
        assertEquals("two subprojects", 2, subprojs.size());
        Project[] subprojsA = (Project[])subprojs.toArray(new Project[2]);
        Project proj2, proj3;
        if (ProjectUtils.getInformation(subprojsA[0]).getName().equals("proj2")) {
            proj2 = subprojsA[0];
            proj3 = subprojsA[1];
        } else {
            proj2 = subprojsA[1];
            proj3 = subprojsA[0];
        }
        assertEquals("proj2 was found correctly", pm.findProject(sisterprojdir), proj2);
        assertEquals("proj3 was found correctly", pm.findProject(sepprojdir), proj3);
    }

    /**
     * Check that methods to add foreign file references really work.
     * @throws Exception in case of unexpected failure
     */
    public void testForeignFileReferences() throws Exception {
        // test collocated foreign project reference
        File f = new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "proj2.jar");
        assertEquals("can add a ref to an artifact", "${reference.proj2.dojar}", r.createForeignFileReference(f, "jar"));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj2.dojar}", r.createForeignFileReference(f, "jar"));
        assertNotNull("ref added", r.getRawReference("proj2", "dojar"));
        String refval = pev.getProperty("reference.proj2.dojar");
        assertEquals("reference correctly evaluated", "../proj2/dist/proj2.jar", refval);
        assertEquals("reference correctly evaluated", f, h.resolveFile(refval));
        AntArtifact art = r.getForeignFileReferenceAsArtifact("${reference.proj2.dojar}");
        assertNotNull("got the reference back", art);
        assertEquals("correct project", sisterprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct artifact location", URI.create("dist/proj2.jar"), art.getArtifactLocation());
        art = r.getForeignFileReferenceAsArtifact("reference.proj2.dojar");
        assertNull("bad format", art);
        art = r.getForeignFileReferenceAsArtifact("${reference.proj2.doojar}");
        assertNull("wrong target name", art);
        File f2 = new File(new File(FileUtil.toFile(sisterprojdir2), "dist"), "proj2.jar");
        assertEquals("reference ID must be unique", "${reference.proj2-1.dojar}", r.createForeignFileReference(f2, "jar"));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj2-1.dojar}", r.createForeignFileReference(f2, "jar"));
        r.destroyForeignFileReference("${reference.proj2-1.dojar}");
        assertNull("ref removed", r.getRawReference("proj2-1", "dojar"));
        r.destroyForeignFileReference("${reference.proj2.dojar}");
        assertNull("ref removed", r.getRawReference("proj2", "dojar"));
        assertNull("project ref property removed", pev.getProperty("reference.proj2.dojar"));
        assertEquals("no refs remaining", 0, r.getRawReferences().length);
        
        // test non-collocated foreign project reference
        FileObject nonCollocatedProjectLib = scratch.getFileObject("separate/proj3").createFolder("d i s t").createData("p r o j 3.jar");
        f = FileUtil.toFile(nonCollocatedProjectLib);
        art = AntArtifactQuery.findArtifactByID(pm.findProject(sepprojdir), "dojar");
        assertNotNull("have an artifact proj3.dojar", art);
        assertEquals("can add a reference to a direct artifact", "${reference.proj3.dojar}", r.createForeignFileReference(art));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj3.dojar}", r.createForeignFileReference(art));
        assertNotNull("ref added", r.getRawReference("proj3", "dojar"));
        refval = pev.getProperty("reference.proj3.dojar");
        String val = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty("reference.proj3.dojar");
        assertEquals("reference was not correctly set", "${project.proj3}/d i s t/p r o j 3.jar", val);
        assertEquals("reference correctly evaluated", f, h.resolveFile(refval));
        val = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty("project.proj3");
        assertEquals("reference correctly evaluated", FileUtil.toFile(sepprojdir).getAbsolutePath(), val);
        art = r.getForeignFileReferenceAsArtifact("${reference.proj3.dojar}");
        assertNotNull("got the reference back", art);
        assertEquals("correct project", sepprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct target name", "dojar", art.getTargetName());
        r.destroyForeignFileReference("${reference.proj3.dojar}");
        assertNull("ref removed", r.getRawReference("proj3", "dojar"));
        assertNull("project ref property removed", pev.getProperty("reference.proj3.dojar"));
        assertEquals("no refs remaining", 0, r.getRawReferences().length);

        // test foreign file reference for collocated jar
        FileObject collocatedLib = scratch.createFolder("j a r s").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        String ref = r.createForeignFileReference(f, "jar");
        String ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", "../j a r s/m y l i b.jar", refval);
        assertEquals("Reference was not correctly evaluated", f, h.resolveFile(refval));
        collocatedLib = scratch.createFolder("jars2").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar-1}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", "../jars2/m y l i b.jar", refval);
        collocatedLib = scratch.createFolder("jars3").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar-2}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", "../jars3/m y l i b.jar", refval);
        assertTrue("Reference was not removed", r.removeReference(ref));
        assertFalse("There should not be any reference", r.removeReference(ref));
        refval = pev.evaluate(ref);
        assertEquals("Reference was not removed", ref, refval);
        
        // test foreign file reference for non-collocated jar
        FileObject nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar}", ref);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        assertEquals("Reference was not correctly evaluated", f, h.resolveFile(refval));
        nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars2").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar-1}", ref);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars3").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar-2}", ref);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        r.destroyForeignFileReference(ref);
        refval = pev.evaluate(ref);
        assertEquals("Reference was not removed", ref, refval);
        
    }
    
    public void testToAntArtifact() throws Exception {
        ReferenceHelper.RawReference ref = new ReferenceHelper.RawReference(
            "proj2", "irrelevant", new URI("also-irrelevant"), "dojar", "totally-irrelevant", "dojar");
        AntArtifact art = ref.toAntArtifact(r);
        assertNull("${project.proj2} not set, will not be found", art);
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("project.proj2", "../proj2");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        art = ref.toAntArtifact(r);
        assertNotNull("now artifact will be found", art);
        assertEquals("correct directory", sisterprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct artifact location", URI.create("dist/proj2.jar"), art.getArtifactLocation());
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        ref = new ReferenceHelper.RawReference(
            "proj2", "irrelevant", new URI("also-irrelevant"), "doojar", "totally-irrelevant", "doojar");
        art = ref.toAntArtifact(r);
        assertNull("wrong target name, will not be found", art);
    }

}
