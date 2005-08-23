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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// XXX testGetPlatform

/**
 * Test functionality of NbModuleProject.
 * @author Jesse Glick
 */
public class NbModuleProjectTest extends TestBase {
    
    public NbModuleProjectTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
        FileObject dir = nbroot.getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
        dir = nbroot.getFileObject("openide/loaders");
        assertNotNull("have openide/loaders checked out", dir);
        p = ProjectManager.getDefault().findProject(dir);
        loadersProject = (NbModuleProject)p;
    }
    
    public void testEvaluator() throws Exception {
        PropertyEvaluator eval = javaProjectProject.evaluator();
        assertEquals("right basedir", file("java/project"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("basedir")));
        assertEquals("right nb_all", nbrootF,
            javaProjectProject.getHelper().resolveFile(eval.getProperty("nb_all")));
        assertEquals("right code.name.base.dashes", "org-netbeans-modules-java-project", eval.getProperty("code.name.base.dashes"));
        assertEquals("right is.autoload", "true", eval.getProperty("is.autoload"));
        assertEquals("right manifest.mf", "manifest.mf", eval.getProperty("manifest.mf"));
        // Keep the following in synch with java/project/nbproject/project.xml etc.:
        String[] cp = {
            "nbbuild/netbeans/ide6/modules/org-apache-tools-ant-module.jar",
            "nbbuild/netbeans/platform6/core/org-openide-filesystems.jar",
            "nbbuild/netbeans/platform6/lib/org-openide-util.jar",
            "nbbuild/netbeans/platform6/lib/org-openide-modules.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-nodes.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-awt.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-dialogs.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-options.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-actions.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-io.jar",
            "nbbuild/netbeans/platform6/modules/org-openide-loaders.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-modules-java-platform.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-modules-project-ant.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-modules-project-libraries.jar",
            "nbbuild/netbeans/ide6/modules/org-openidex-util.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-modules-projectapi.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-modules-projectuiapi.jar",
            "nbbuild/netbeans/platform6/modules/org-netbeans-modules-queries.jar",
            "nbbuild/netbeans/ide6/modules/org-netbeans-api-java.jar",
        };
        assertEquals("right module.classpath", createCP(cp), eval.getProperty("module.classpath"));
        assertEquals("right core.dir", file("nbbuild/netbeans/platform6"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("core.dir")));
        assertEquals("right apisupport/project.dir", file("nbbuild/netbeans/ide6"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("apisupport/project.dir")));
        assertEquals("right module JAR", file("nbbuild/netbeans/ide6/modules/org-netbeans-modules-java-project.jar"),
            javaProjectProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
        eval = loadersProject.evaluator();
        assertEquals("right module JAR", file("nbbuild/netbeans/platform6/modules/org-openide-loaders.jar"),
            loadersProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
    }
    
    private String createCP(String[] cp) {
        StringBuffer cpS = new StringBuffer();
        for (int i = 0; i < cp.length; i++) {
            if (i > 0) {
                cpS.append(File.pathSeparatorChar);
            }
            cpS.append(file(cp[i]).getAbsolutePath());
        }
        return cpS.toString();
    }
    
    /** #56457 */
    public void testExternalSourceRoots() throws Exception {
        FileObject documentFinderJava = nbroot.getFileObject("editor/libsrc/org/netbeans/editor/DocumentFinder.java");
        assertNotNull("have DocumentFinder.java", documentFinderJava);
        FileObject editorLib = nbroot.getFileObject("editor/lib");
        assertNotNull("have editor/lib", editorLib);
        Project editorLibProject = ProjectManager.getDefault().findProject(editorLib);
        assertNotNull("have editor/lib project", editorLibProject);
        Thread.sleep(1000);
        assertEquals("correct owner of DocumentFinder.java", editorLibProject, FileOwnerQuery.getOwner(documentFinderJava));
    }
    
    public void testExternalModules() throws Exception {
        FileObject suite1 = extexamples.getFileObject("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        PropertyEvaluator eval = actionProject.evaluator();
        String[] cp = {
            "nbbuild/netbeans/platform6/modules/org-openide-dialogs.jar",
            EEP + "/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar",
        };
        assertEquals("right module.classpath", createCP(cp), eval.getProperty("module.classpath"));
        String nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull("defined netbeans.dest.dir", nbdestdir);
        assertEquals("right netbeans.dest.dir", file("nbbuild/netbeans"), PropertyUtils.resolveFile(FileUtil.toFile(action), nbdestdir));
        FileObject suite3 = extexamples.getFileObject("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        eval = dummyProject.evaluator();
        cp = new String[] {
            "random/modules/random.jar",
            "random/modules/ext/stuff.jar",
        };
        StringBuffer cpS = new StringBuffer();
        for (int i = 0; i < cp.length; i++) {
            if (i > 0) {
                cpS.append(File.pathSeparatorChar);
            }
            cpS.append(file(extexamplesF, "suite3/nbplatform/" + cp[i]).getAbsolutePath());
        }
        assertEquals("right module.classpath", cpS.toString(), eval.getProperty("module.classpath"));
        assertEquals("right netbeans.dest.dir", file(extexamplesF, "suite3/nbplatform"), PropertyUtils.resolveFile(FileUtil.toFile(dummy), eval.getProperty("netbeans.dest.dir")));
        // XXX more...
    }
    
    public void testGetType() throws Exception {
        assertEquals(NbModuleTypeProvider.NETBEANS_ORG, typeOf(javaProjectProject));
        FileObject suite1 = extexamples.getFileObject("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        assertEquals(NbModuleTypeProvider.SUITE_COMPONENT, typeOf(actionProject));
        FileObject suite3 = extexamples.getFileObject("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        assertEquals(NbModuleTypeProvider.STANDALONE, typeOf(dummyProject));
    }
    private NbModuleTypeProvider.NbModuleType typeOf(Project p) {
        return ((NbModuleTypeProvider) p.getLookup().lookup(NbModuleTypeProvider.class)).getModuleType();
    }
    
    public void testSupportsJavadoc() throws Exception {
        assertTrue(javaProjectProject.supportsJavadoc());
        FileObject dir = nbroot.getFileObject("beans");
        assertNotNull("have beans checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleProject beansProject = (NbModuleProject) p;
        assertFalse(beansProject.supportsJavadoc());
    }
    
    public void testGetNbrootFile() throws Exception {
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(extexamples.getFileObject("suite1/action-project"));
        assertEquals(file("xtest/lib/insanelib.jar"), actionProject.getNbrootFile("xtest/lib/insanelib.jar"));
    }
    
    public void testThatModuleWithOverriddenSrcDirPropertyDoesNotThrowNPE() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject srcFO = prjFO.getFileObject("src");
        FileUtil.moveFile(srcFO, prjFO, "src2");
        ProjectManager.getDefault().findProject(prjFO);
    }
}
