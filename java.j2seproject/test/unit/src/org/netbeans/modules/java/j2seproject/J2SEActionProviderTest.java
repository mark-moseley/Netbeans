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

package org.netbeans.modules.java.j2seproject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.applet.AppletSupport;
import org.netbeans.modules.java.j2seproject.ui.customizer.MainClassChooser;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

/**
 * Tests for J2SEActionProvider
 *
 * @author David Konecny
 */
public class J2SEActionProviderTest extends NbTestCase {
    
    public J2SEActionProviderTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject build;
    private FileObject tests;
    private ProjectManager pm;
    private Project pp;
    private AntProjectHelper helper;
    private J2SEActionProvider actionProvider;
    private DataFolder sourcePkg1;
    private DataFolder sourcePkg2;
    private DataFolder testPkg1;
    private DataFolder testPkg2;
    private DataObject someSource1;
    private DataObject someSource2;
    private DataObject someSource3;
    private DataObject someTest1;
    private DataObject someTest2;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new org.netbeans.modules.java.j2seproject.J2SEProjectType(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation(),
            new SimplePlatformProvider (),
        });
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj","foo.Main","manifest.mf"); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        actionProvider = (J2SEActionProvider)pp.getLookup().lookup(J2SEActionProvider.class);              
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
//        projdir.createData("build.xml");
        build = projdir.createFolder("build");
        build.createFolder("classes");
        FileObject pkg = sources.createFolder("foo");        
        FileObject fo = pkg.createData("Bar.java");
        sourcePkg1 = DataFolder.findFolder (pkg);
        pkg = sources.createFolder("foo2");
        sourcePkg2 = DataFolder.findFolder (pkg);
        someSource1 = DataObject.find(fo);
        fo = sources.getFileObject("foo").createData("Main.java");
        createMain(fo);
        someSource2 = DataObject.find(fo);
        fo = sources.getFileObject("foo").createData("Third.java");
        someSource3 = DataObject.find(fo);
        pkg = tests.createFolder("foo");
        fo = pkg.createData("BarTest.java");
        testPkg1 = DataFolder.findFolder (pkg);
        pkg = tests.createFolder("foo2");
        testPkg2 = DataFolder.findFolder (pkg);
        someTest1 = DataObject.find(fo);
        fo = tests.getFileObject("foo").createData("MainTest.java");
        someTest2 = DataObject.find(fo);
        assertNotNull(someSource1);
        assertNotNull(someSource2);
        assertNotNull(someTest1);
        assertNotNull(someTest2);
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    private void createMain(FileObject fo) throws Exception {
        FileLock lock = fo.lock();
        PrintWriter pw = new PrintWriter(fo.getOutputStream(lock));
        pw.println("package foo;");
        pw.println("public class Main { public static void main(String[] args){}; };");
        pw.flush();
        pw.close();
        lock.releaseLock();
    }
    
    public void testGetTargetNames() throws Exception {
        implTestGetTargetNames();
    }

    public void testGetTargetNamesMultiRoots () throws Exception {
        SourceRootsTest.addSourceRoot(helper, projdir, "src.other.dir","other");
        implTestGetTargetNames();
    }

    public void implTestGetTargetNames () throws Exception {
        Properties p;
        Lookup context;
        String[] targets;

        // test COMMAND_COMPILE_SINGLE

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource1});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Bar.java", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someTest1,someTest2});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-test-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/BarTest.java,foo/MainTest.java", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {sourcePkg1});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/**", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {sourcePkg1, sourcePkg2});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/**,foo2/**", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {DataFolder.findFolder(sources)});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "**", p.getProperty("javac.includes"));
        
        p = new Properties();
        context = Lookups.fixed(new Object[] {sourcePkg1, new NonRecursiveFolderImpl (sourcePkg1)});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/*", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new Object[] {sourcePkg1, sourcePkg2, new NonRecursiveFolderImpl(sourcePkg1), new NonRecursiveFolderImpl(sourcePkg2)});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/*,foo2/*", p.getProperty("javac.includes"));
        p = new Properties();
        context = Lookups.fixed(new Object[] {DataFolder.findFolder(sources), new NonRecursiveFolderImpl(sources)});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "*", p.getProperty("javac.includes"));
        
        // test COMMAND_TEST_SINGLE

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource1});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_TEST_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_TEST_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_TEST_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "test-single", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("javac.includes"));
        assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("test.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource1,someSource2});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_TEST_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_TEST_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_TEST_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "test-single", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/BarTest.java,foo/MainTest.java", p.getProperty("javac.includes"));
        assertEquals("There must be be target parameter", "foo/BarTest.java,foo/MainTest.java", p.getProperty("test.includes"));        

        // test COMMAND_DEBUG_TEST_SINGLE

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource1});
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context, p);
        assertNotNull("Must found some targets for COMMAND_DEBUG_TEST_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_TEST_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "debug-test", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo.BarTest", p.getProperty("test.class"));

        // test COMMAND_DEBUG_FIX

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource1});
        targets = actionProvider.getTargetNames(JavaProjectConstants.COMMAND_DEBUG_FIX, context, p);
        assertNotNull("Must found some targets for COMMAND_DEBUG_FIX", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_FIX", 1, targets.length);
        assertEquals("Unexpected target name", "debug-fix", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Bar", p.getProperty("fix.includes"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someTest1});
        targets = actionProvider.getTargetNames(JavaProjectConstants.COMMAND_DEBUG_FIX, context, p);
        assertNotNull("Must found some targets for COMMAND_DEBUG_FIX", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_FIX", 1, targets.length);
        assertEquals("Unexpected target name", "debug-fix-test", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/BarTest", p.getProperty("fix.includes"));

        // test COMMAND_RUN_SINGLE

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource2});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
        }
        assertNotNull("Must found some targets for COMMAND_RUN_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_RUN_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "run-single", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Main.java", p.getProperty("javac.includes"));
        assertEquals("There must be be target parameter", "foo.Main", p.getProperty("run.class"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource2});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.FALSE;
        AppletSupport.unitTestingSupport_isApplet = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
            AppletSupport.unitTestingSupport_isApplet = null;
        }
        assertNotNull("Must found some targets for COMMAND_RUN_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_RUN_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "run-applet", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Main.java", p.getProperty("javac.includes"));
        FileObject appletHtml = build.getFileObject("Main", "html");
        assertNotNull("Applet HTML page must be generated", appletHtml);
        URL appletUrl = URLMapper.findURL(appletHtml, URLMapper.EXTERNAL);
        assertEquals("There must be be target parameter", appletUrl.toExternalForm(), p.getProperty("applet.url"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someTest1});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        AppletSupport.unitTestingSupport_isApplet = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
            AppletSupport.unitTestingSupport_isApplet = null;
        }
        assertNotNull("Must found some targets for COMMAND_RUN_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_RUN_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "test-single", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("javac.includes"));
        assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("test.includes"));

        // test COMMAND_DEBUG_SINGLE

        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource2});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
        }
        assertNotNull("Must found some targets for COMMAND_DEBUG_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "debug-single", targets[0]);
        assertEquals("There must be one target parameter", 2, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Main.java", p.getProperty("javac.includes"));
        assertEquals("There must be be target parameter", "foo.Main", p.getProperty("debug.class"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someSource2});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.FALSE;
        AppletSupport.unitTestingSupport_isApplet = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
            AppletSupport.unitTestingSupport_isApplet = null;
        }
        assertNotNull("Must found some targets for COMMAND_DEBUG_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "debug-applet", targets[0]);
        assertEquals("There must be one target parameter", 3, p.keySet().size());
        assertEquals("There must be be target parameter", "foo/Main.java", p.getProperty("javac.includes"));
        appletHtml = build.getFileObject("Main", "html");
        assertNotNull("Applet HTML page must be generated", appletHtml);
        appletUrl = URLMapper.findURL(appletHtml, URLMapper.EXTERNAL);
        assertEquals("There must be be target parameter", appletUrl.toExternalForm(), p.getProperty("applet.url"));
        p = new Properties();
        context = Lookups.fixed(new DataObject[] {someTest1});
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        AppletSupport.unitTestingSupport_isApplet = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG_SINGLE, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
            AppletSupport.unitTestingSupport_isApplet = null;
        }
        assertNotNull("Must found some targets for COMMAND_DEBUG_SINGLE", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "debug-test", targets[0]);
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo.BarTest", p.getProperty("test.class"));

        // test COMMAND_RUN

        p = new Properties();
        context = Lookup.EMPTY;
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
        }
        assertNotNull("Must found some targets for COMMAND_RUN", targets);
        assertEquals("There must be one target for COMMAND_RUN", 1, targets.length);
        assertEquals("Unexpected target name", "run", targets[0]);
        //The project is saved after the main.class property was added into the project's properties,
        //it is no more needed to pass the main.class in the properties.
        //See issue #61244: Main class setting not saved for J2SE Project during IDE session
        assertEquals("There must be no target parameter", 0, p.keySet().size());
        
        // test COMMAND_DEBUG

        p = new Properties();
        context = Lookup.EMPTY;
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
        }
        assertNotNull("Must found some targets for COMMAND_DEBUG", targets);
        assertEquals("There must be one target for COMMAND_DEBUG", 1, targets.length);
        assertEquals("Unexpected target name", "debug", targets[0]);
        //The project is saved after the main.class property was added into the project's properties,
        //it is no more needed to pass it in the properties.
        //See issue #61244: Main class setting not saved for J2SE Project during IDE session
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo.Main", p.getProperty("debug.class"));

        // test COMMAND_DEBUG_STEP_INTO

        p = new Properties();
        context = Lookup.EMPTY;
        MainClassChooser.unitTestingSupport_hasMainMethodResult = Boolean.TRUE;
        try {
            targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG_STEP_INTO, context, p);
        } finally {
            MainClassChooser.unitTestingSupport_hasMainMethodResult = null;
        }
        assertNotNull("Must found some targets for COMMAND_DEBUG_STEP_INTO", targets);
        assertEquals("There must be one target for COMMAND_DEBUG_STEP_INTO", 1, targets.length);
        assertEquals("Unexpected target name", "debug-stepinto", targets[0]);
        //The project is saved after the main.class property was added into the project's properties,
        //it is no more needed to pass it in the properties.
        //See issue #61244: Main class setting not saved for J2SE Project during IDE session
        assertEquals("There must be one target parameter", 1, p.keySet().size());
        assertEquals("There must be be target parameter", "foo.Main", p.getProperty("debug.class"));
    }
    
    public void testGetTargetNamesFromConfig() throws Exception {
        final FileObject projdirFO = scratch.createFolder("projectwithconfigs");
        J2SEProjectGenerator.createProject(FileUtil.toFile(projdirFO), "projectwithconfigs", null, null);
        final J2SEProject proj = (J2SEProject) ProjectManager.getDefault().findProject(projdirFO);
        final ProjectConfigurationProvider pcp = proj.getLookup().lookup(ProjectConfigurationProvider.class);
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                Properties props = new Properties();
                props.setProperty("main.class", "foo.Bar");
                props.setProperty("$target.build", "");
                props.setProperty("$target.run", "runtarget");
                props.setProperty("$target.debug", "debugtarget1 debugtarget2");
                write(props, projdirFO, "nbproject/configs/test.properties");
                props = new Properties();
                write(props, projdirFO, "nbproject/private/configs/test.properties");
                props = new Properties();
                props.setProperty("config", "test");
                write(props, projdirFO, "nbproject/private/config.properties");
                ProjectManager.getDefault().saveProject(proj);
                List<ProjectConfiguration> configs = new ArrayList<ProjectConfiguration>(pcp.getConfigurations());
                pcp.setActiveConfiguration(configs.get(1));
                return null;
            }
        });
        J2SEActionProvider actionProvider = (J2SEActionProvider) proj.getLookup().lookup(J2SEActionProvider.class);
        PropertyEvaluator eval = proj.evaluator();
        String config = eval.getProperty("config");
        assertEquals("Name of active config from Evaluator is test", "test", config);
        FileObject sources = projdirFO.getFileObject("src");
        FileObject pkg = sources.createFolder("foo");
        FileObject file = pkg.createData("Bar.java");
        DataObject srcDO = DataObject.find(file);
        Lookup context = Lookups.fixed(new DataObject[] { srcDO });
        // test of targets defined in config
        String[] targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG, context, new Properties());
        assertEquals("There must be two Debug targets in test config", 2, targets.length);
        assertEquals("First Debug target name is debugtarget1", "debugtarget1", targets[0]);
        assertEquals("Second Debug target name is debugtarget2", "debugtarget2", targets[1]);
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_BUILD, context, new Properties());
        assertEquals("There must be 1 Build target in test config", 1, targets.length);
        // target is not in fact from the config, config contains empty string
        assertEquals("Build target name is jar", "jar", targets[0]); 
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN, context, new Properties());
        assertEquals("There must be 1 Run target in test config", 1, targets.length);
        assertEquals("Run target name is runtarget", "runtarget", targets[0]);
        // test of targets not in config
        targets = actionProvider.getTargetNames(ActionProvider.COMMAND_CLEAN, context, new Properties());
        assertEquals("There must be 1 Clean target", 1, targets.length);
        assertEquals("Clean target name is runtarget", "clean", targets[0]);
    }
    
    public void testIsActionEnabled() throws Exception {    
        implTestIsActionEnabled();
    }

    public void testIsActionEnabledMultiRoot() throws Exception {
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper, projdir, "src.other.dir","other");
        implTestIsActionEnabled();
        Lookup context = Lookups.fixed(new DataObject[] {sourcePkg1, DataFolder.findFolder(newRoot)});
        boolean enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertFalse ("COMMAND_COMPILE_SINGLE must be disabled on multiple src packages from different roots", enabled);
    }

    private void implTestIsActionEnabled () throws Exception {
        Lookup context;
        boolean enabled;

        // test COMMAND_COMPILE_SINGLE

        context = Lookups.fixed(new DataObject[] {someSource1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue("COMMAND_COMPILE_SINGLE must be enabled on one source", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue("COMMAND_COMPILE_SINGLE must be enabled on multiple sources", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue("COMMAND_COMPILE_SINGLE must be enabled on multiple tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertFalse("COMMAND_COMPILE_SINGLE must be disabled on mixed files", enabled);

        context = Lookups.fixed(new DataObject[] {sourcePkg1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on one src package", enabled);

        context = Lookups.fixed(new DataObject[] {sourcePkg1, sourcePkg2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on multiple src packages", enabled);

        context = Lookups.fixed(new DataObject[] {sourcePkg1, someSource1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on mixed src packages/files", enabled);


        context = Lookups.fixed(new DataObject[] {testPkg1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on one test package", enabled);

        context = Lookups.fixed(new DataObject[] {testPkg1, testPkg2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on multiple test packages", enabled);

        context = Lookups.fixed(new DataObject[] {testPkg1, someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertTrue ("COMMAND_COMPILE_SINGLE must be enabled on mixed test packages/files", enabled);

        context = Lookups.fixed(new DataObject[] {DataFolder.findFolder(projdir)});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertFalse ("COMMAND_COMPILE_SINGLE must not be enabled on non source folder", enabled);


        context = Lookups.fixed(new DataObject[] {sourcePkg1, testPkg1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context);
        assertFalse ("COMMAND_COMPILE_SINGLE must not be enabled on non mixed packages", enabled);

        // test COMMAND_TEST_SINGLE

        context = Lookups.fixed(new DataObject[] {someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertFalse("COMMAND_TEST_SINGLE must be disabled on one test", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertFalse("COMMAND_TEST_SINGLE must be disabled on multiple tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource3});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertFalse("COMMAND_TEST_SINGLE must be disabled on non-test file which does not have associated test", enabled);

        context = Lookups.fixed(new DataObject[] {someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertTrue("COMMAND_TEST_SINGLE must be enabled on source file which has associated test", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertTrue("COMMAND_TEST_SINGLE must be enabled on source files which has associated tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource3});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertFalse("COMMAND_TEST_SINGLE must be disabled on mixture of source files when some files do not have tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, context);
        assertFalse("COMMAND_TEST_SINGLE must be disabled on mixture of source files and test files", enabled);

        // test COMMAND_DEBUG_TEST_SINGLE

        context = Lookups.fixed(new DataObject[] {someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context);
        assertFalse("COMMAND_DEBUG_TEST_SINGLE must be disabled on test files", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context);
        assertFalse("COMMAND_DEBUG_TEST_SINGLE must be disabled on multiple tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource3});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context);
        assertFalse("COMMAND_DEBUG_TEST_SINGLE must be disabled on non-test file which does not have associated test", enabled);

        context = Lookups.fixed(new DataObject[] {someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context);
        assertTrue("COMMAND_DEBUG_TEST_SINGLE must be enabled on source file which has associated test", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, context);
        assertFalse("COMMAND_DEBUG_TEST_SINGLE must be disabled on multiple source files", enabled);

        // test COMMAND_DEBUG_FIX

        context = Lookups.fixed(new DataObject[] {someTest1});
        enabled = actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, context);
        assertTrue("COMMAND_DEBUG_FIX must be enabled on one test", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, context);
        assertFalse("COMMAND_DEBUG_FIX must be disabled on multiple tests", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1});
        enabled = actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, context);
        assertTrue("COMMAND_DEBUG_FIX must be enabled on one source", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, context);
        assertFalse("COMMAND_DEBUG_FIX must be disabled on multiple source files", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someTest1});
        enabled = actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, context);
        assertFalse("COMMAND_DEBUG_FIX must be disabled on multiple mixed files", enabled);

        // test COMMAND_RUN_SINGLE

        context = Lookups.fixed(new DataObject[] {someSource1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, context);
        assertTrue("COMMAND_RUN_SINGLE must be enabled on one source", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, context);
        assertFalse("COMMAND_RUN_SINGLE must be disabled on multiple sources", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, context);
        assertTrue("COMMAND_RUN_SINGLE must be enabled on test file", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, context);
        assertFalse("COMMAND_RUN_SINGLE must be disabled on multiple test files", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, context);
        assertFalse("COMMAND_RUN_SINGLE must be disabled on mixed multiple test files", enabled);

        // test COMMAND_DEBUG_SINGLE

        context = Lookups.fixed(new DataObject[] {someSource1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, context);
        assertTrue("COMMAND_DEBUG_SINGLE must be enabled on one source", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someSource2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, context);
        assertFalse("COMMAND_DEBUG_SINGLE must be disabled on multiple sources", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, context);
        assertTrue("COMMAND_DEBUG_SINGLE must be enabled on test file", enabled);

        context = Lookups.fixed(new DataObject[] {someTest1, someTest2});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, context);
        assertFalse("COMMAND_DEBUG_SINGLE must be disabled on multiple test files", enabled);

        context = Lookups.fixed(new DataObject[] {someSource1, someTest1});
        enabled = actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, context);
        assertFalse("COMMAND_DEBUG_SINGLE must be disabled on mixed multiple test files", enabled);
    }
    
    
    private static final class NonRecursiveFolderImpl implements NonRecursiveFolder {
        
        private FileObject fobj;
        
        public NonRecursiveFolderImpl (DataObject dobj) {
            assert dobj != null;
            this.fobj = dobj.getPrimaryFile();
        }
        
        public NonRecursiveFolderImpl (FileObject fobj) {
            assert fobj != null;
            this.fobj = fobj;
        }
                
        public FileObject getFolder() {
            return this.fobj;
        }        
    }
    
    private static class SimplePlatformProvider implements org.netbeans.modules.java.platform.JavaPlatformProvider {
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        public org.netbeans.api.java.platform.JavaPlatform[] getInstalledPlatforms() {
            return new org.netbeans.api.java.platform.JavaPlatform[] {
                getDefaultPlatform ()
            };
        }

        public org.netbeans.api.java.platform.JavaPlatform getDefaultPlatform() {
            return new TestDefaultPlatform ();
        }
        
    }
    
    private static class TestDefaultPlatform extends org.netbeans.api.java.platform.JavaPlatform {
        
        public TestDefaultPlatform () {
            
        }

        public FileObject findTool(String toolName) {
            return null;
        }

        public String getDisplayName() {
            return "Default Platform";
        }

        public org.netbeans.api.java.classpath.ClassPath getBootstrapLibraries() {
            return null;
        }

        public java.util.Collection getInstallFolders() {
            return null;
        }

        public org.netbeans.api.java.classpath.ClassPath getStandardLibraries() {
            return null;
        }

        public String getVendor() {
            return null;
        }

        public org.netbeans.api.java.platform.Specification getSpecification() {
            return new org.netbeans.api.java.platform.Specification ("j2se", new SpecificationVersion ("1.4"));
        }

        public org.netbeans.api.java.classpath.ClassPath getSourceFolders() {
            return null;
        }

        public java.util.List getJavadocFolders() {
            return null;
        }

        public java.util.Map getProperties() {
            return Collections.singletonMap("platform.ant.name","default_platform");
        }
        
    }
    
    private void write(Properties p, FileObject d, String path) throws IOException {
        FileObject f = FileUtil.createData(d, path);
        OutputStream os = f.getOutputStream();
        p.store(os, null);
        os.close();
    }
    
    private static Collection<? extends ProjectConfiguration> getConfigurations(ProjectConfigurationProvider<?> pcp) {
        return pcp.getConfigurations();
    }

    @SuppressWarnings("unchecked")
    private static void setActiveConfiguration(ProjectConfigurationProvider<?> pcp, ProjectConfiguration pc) throws IOException {
        ProjectConfigurationProvider _pcp = pcp;
        _pcp.setActiveConfiguration(pc);
    }

}
