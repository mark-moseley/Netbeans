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
package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGeneratorTest;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Checks building of ZIP support.
 * @author Jaroslav Tulach
 */
public class BuildZipDistributionTest extends TestBase {
    
    private SuiteProject suite;
    
    public BuildZipDistributionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        SuiteProjectGeneratorTest.openProject(suite);
        proj.open();
    }
    
    public void testBuildThezipAppWhenAppNamePropIsNotSet() throws Exception {
        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here", p);
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-zip: " + l, l.contains("build-zip"));
        
        ExecutorTask task = p.invokeActionImpl("build-zip", suite.getLookup());
        
        assertNotNull("Task was started", task);
        assertEquals("There is a failure as app.name is not set", 1, task.result());
        
        org.openide.filesystems.FileObject[] arr = suite.getProjectDirectory().getChildren();
        List subobj = new ArrayList (Arrays.asList(arr));
        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
    }
    
    public void testBuildThezipAppWhenAppNamePropIsSet() throws Exception {
        FileObject x = suite.getProjectDirectory().getFileObject("nbproject/project.properties");
        EditableProperties ep = org.netbeans.modules.apisupport.project.Util.loadProperties(x);
        ep.setProperty("app.name", "fakeapp");
        
        StringBuffer exclude = new StringBuffer();
        String sep = "";
        String[] possibleClusters = destDirF.list();
        for (int i = 0; i < possibleClusters.length; i++) {
            if (possibleClusters[i].startsWith("platform")) {
                continue;
            }
            exclude.append(sep);
            exclude.append(possibleClusters[i]);
            sep = ",";
        }
        ep.setProperty("disabled.clusters", exclude.toString());
        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
            "org.openide.compat," +
            "org.netbeans.api.progress," +
            "org.netbeans.core.multiview," +
            "org.openide.util.enumerations" +
            "");
        org.netbeans.modules.apisupport.project.Util.storeProperties(x, ep);
        
        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here", p);
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-zip: " + l, l.contains("build-zip"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        ExecutorTask task = p.invokeActionImpl("build-zip", suite.getLookup());
        
        assertNotNull("Task was started", task);
        assertEquals("Finished ok", 0, task.result());
        
        org.openide.filesystems.FileObject[] arr = suite.getProjectDirectory().getChildren();
        List subobj = new ArrayList (Arrays.asList(arr));
        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
        assertNotNull("dist created", dist);
        subobj.remove(dist);
        
        if (!subobj.isEmpty()) {
            fail("There should be no created directories in the suite dir: " + subobj);
        }   
        
        FileObject zip = dist.getFileObject("fakeapp.zip");
        assertNotNull("ZIP file created: " + zip, zip);
        
        File zipF = org.openide.filesystems.FileUtil.toFile(zip);
        JarFile zipJ = new JarFile(zipF);
        Enumeration en = zipJ.entries();
        int cntzip = 0;
        
        StringBuffer sb = new StringBuffer();
        StringBuffer hidden = new StringBuffer();
        while (en.hasMoreElements()) {
            JarEntry entry = (JarEntry)en.nextElement();
            sb.append("\n");
            sb.append(entry.getName());
            cntzip++;
            
            if (entry.getName().endsWith("_hidden")) {
                hidden.append("\n");
                hidden.append(entry.getName());
            }
        }
        
        if (cntzip == 0) {
            fail("There should be at least one zip entry: " + sb);
        }
        
        if (hidden.length() != 0) {
            fail("There should be no hidden files in the zip file: " + hidden);
        }
    }
    
    private File createNewJarFile (String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) {
                f.createNewFile();
                return f;
            }
        }
    }
}
