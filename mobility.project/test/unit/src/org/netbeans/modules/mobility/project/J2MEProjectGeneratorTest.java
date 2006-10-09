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

/*
 * J2MEProjectGeneratorTest.java
 * JUnit based test
 *
 * Created on April 21, 2005, 11:19 AM
 */
package org.netbeans.modules.mobility.project;

import java.beans.PropertyChangeListener;
import junit.framework.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Michal Skvor
 */
public class J2MEProjectGeneratorTest extends NbTestCase {
    
    static class MyProvider implements JavaPlatformProvider {
        final static J2MEPlatform.Device devices[];
        final static J2MEPlatform plat;
        
        
        static
        {
            devices=new J2MEPlatform.Device[] {
                new J2MEPlatform.Device("d1","d2",null,new J2MEPlatform.J2MEProfile[0] ,null)
            };
            plat=new J2MEPlatform("n1","cp","t1","d1",null,null,null,null,null,devices);
        }
        
        public JavaPlatform[] getInstalledPlatforms() {
            return new JavaPlatform[] { plat};
        }
        
        public JavaPlatform getDefaultPlatform() {
            return plat;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    
    static
    {
        TestUtil.setLookup( new Object[] {
            TestUtil.testProjectFactory(),
            TestUtil.testFileLocator(),
            TestUtil.testProjectChooserFactory(),
            new MyProvider()
        }, J2MEProjectGeneratorTest.class.getClassLoader());
    }
    
    public J2MEProjectGeneratorTest(String testName) {
        super(testName);
        TestUtil.setEnv();
    }
    
    private static final String[] createdFiles = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/genfiles.properties",
        "nbproject/private/private.properties",
        "src",
    };
    
    /*
    private static final String[] wtkProperties = {
     
    private static final String[] templProperties = {
        "configs.Test.platform.active",
        "configs.Test.platform.active.description",
        "configs.Test.platform.profile",
        "configs.Test.platform.apis",
        "configs.Test.abilities",
        "configs.Test.platform.configuration",
        "configs.Test.platform.device",
        "configs.Test.platform.bootclasspath",
    };
     */
    
    private static final String[] createdProperties = {
        //New properties
        "abilities",
        "platform.bootclasspath",
        "debug.level",
        "deployment.copy.target",
        "run.cmd.options",
        "libs.classpath",
        "javac.debug",
        "javac.deprecation",
        "javac.optimize",
        "javac.source",
        "javac.target",
        "javac.encoding",
        "obfuscation.level",
        "obfuscation.custom",
        "use.emptyapis",
        "no.dependencies",
        "jar.compress",
        "src.dir",
        "build.root.dir",
        "dist.root.dir",
        "build.dir",
        "dist.dir",
        "name",
        "build.classes.excludes",
        "preprocessed.dir",
        "build.classes.dir",
        "obfuscator.srcjar",
        "obfuscator.destjar",
        "obfuscated.classes.dir",
        "preverify.classes.dir",
        "dist.jar",
        "dist.jad",
        "dist.javadoc.dir",
        "run.method",
        "run.security.domain",
        "filter.use.standard",
        "filter.exclude.tests",
        "filter.excludes",
        "run.use.security.domain",
        "deployment.method",
        "deployment.override.jarurl",
        "deployment.jarurl",
        "deployment.instance",
        "manifest.midlets",
        "manifest.apipermissions",
        "manifest.pushregistry",
        "manifest.manifest",
        "manifest.jad",
        "manifest.others",
        "javadoc.private",
        "javadoc.notree",
        "javadoc.use",
        "javadoc.nonavbar",
        "javadoc.noindex",
        "javadoc.splitindex",
        "javadoc.author",
        "javadoc.version",
        "javadoc.windowtitle",
        "javadoc.encoding",
        "sign.enabled",
        "sign.keystore",
        "sign.alias",
        "platform.active",
        "platform.active.description",
        "platform.apis",
        "platform.configuration",
        "platform.device",
        "platform.profile",
        "platform.trigger",
    };
    
    private final static String[][] manifestData = {
        { "MIDlet-1", "Testing purpose JAD file, /icon/FooBar.png, foo.bar.jad.file" },
        { "MIDlet-Name", "NiceFooBarMIDlet" },
        { "MIDlet-Version", "1.0" },
        { "MicroEdition-Configuration", "CLDC-1.0" },
        { "MicroEdition-Profile", "MIDP-1.0" },
    };
    
    private final static String[][] jadfileData = {
        { "MIDlet-2", "Testing purpose JAD file two, /icon/FooBar2.png, foo.bar.jad.file.two" },
        { "MIDlet-Name", "NiceFooBarMIDlet" },
        { "MIDlet-Version", "1.0" },
        { "MicroEdition-Configuration", "CLDC-1.0" },
        { "MicroEdition-Profile", "MIDP-1.0" },
    };
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File.createTempFile("build",".properties",getWorkDir());
        System.setProperty("user.properties.file",getWorkDir().getAbsolutePath());
    }
    
    protected void tearDown() throws Exception {
        // Take out trash
        super.tearDown();
    }
    
    private void createMain(FileObject fo) throws Exception {
        FileLock lock = fo.lock();
        PrintWriter pw = new PrintWriter(fo.getOutputStream(lock));
        pw.println("package foo;");
        pw.println("import javax.microedition.midlet.MIDlet;");
        pw.println("public class Main extends MIDlet { public static void main(String[] args){System.out.println(\"main\"); }");
        pw.println("public void startApp() {} public void pauseApp() {} public void destroyApp(boolean un) {}}");
        pw.flush();
        pw.close();
        lock.releaseLock();
    }
    
    private boolean checkFiles(FileObject dir,AntProjectHelper aph, boolean all) {
        try {
            dir.getFileSystem().refresh(true);
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace();
        }
        for (int i=0; i<createdFiles.length; i++) {
            assertNotNull(createdFiles[i]+" file/folder cannot be found", dir.getFileObject(createdFiles[i]));
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ArrayList l = new ArrayList(props.keySet());
        for (int i=0; i<createdProperties.length; i++) {
            assertNotNull(createdProperties[i]+" property cannot be found in project.properties", props.getProperty(createdProperties[i]));
            l.remove(createdProperties[i]);
        }
        
        if (all==true)
            assertEquals("found unexpected property: "+l,l.size(), 0);
        
        return true;
    }
    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEProjectGeneratorTest.class);
        
        return suite;
    }
    
    public void testProjectUtil() throws Exception {
        File f=getGoldenFile("Studio/MIDletSuite.jar");
        File ch=f.getParentFile().getParentFile().getParentFile();
        URL url=f.toURL();
        URL churl=ch.toURL();
        URL u=J2MEProjectUtils.deJar(url);
        assertEquals(url,u);
        u=J2MEProjectUtils.wrapJar(url);
        assertNotSame(url,u);
        u=J2MEProjectUtils.wrapJar(churl);
        boolean b=J2MEProjectUtils.isParentOf(churl,u);
        assertTrue(b);
        String  s=J2MEProjectUtils.detectConfiguration(churl,url);
        assertNull(s);
    }
    
    public void testCreateProjectFromSuite() throws Exception {
        File studiodemo=getGoldenFile("Studio");
        FileObject projectDir=FileUtil.toFileObject(getWorkDir()).createFolder("Studio");
        TestUtil.cpDir(FileUtil.toFileObject(studiodemo),projectDir);
        
        AntProjectHelper aph=null;
        try {
            aph=J2MEProjectGenerator.
                    createProjectFromSuite(FileUtil.toFile(projectDir),"Suite",null,FileUtil.toFile(projectDir).getAbsolutePath()+"/MIDletSuite.adContent",".");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assertNotNull(aph);
        
        /* Test ProjectUtils */
        Project prj=ProjectManager.getDefault().findProject(projectDir);
        FileObject fo=projectDir.getFileObject("Midlet.java");
        DataObject dob=DataObject.find(fo);
        EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
        Document doc=ec.openDocument();
        ProjectConfigurationsHelper cfghlp=J2MEProjectUtils.getCfgHelperForDoc(doc);
        assertNotNull(cfghlp);
        ProjectConfigurationProvider pprov=J2MEProjectUtils.getConfigProviderForDoc(doc);
        assertNotNull(pprov);
        Project p=J2MEProjectUtils.getProjectForDocument(doc);
        assertEquals(p,prj);
        String s=J2MEProjectUtils.evaluateProperty(aph,"test.ant.home");
        assertEquals(s,System.getProperty("test.ant.home"));
        TopComponent tc=new TopComponent();
        Node n=new AbstractNode(Children.LEAF, Lookups.singleton(DataObject.find(fo)));
        tc.setActivatedNodes(new Node[] {n});
        p=J2MEProjectUtils.getActiveProject(tc);
        assertEquals(p,prj);
        p=J2MEProjectUtils.getActiveProject();
        assertNull(p);
    }
    
    public void testCreateProjectFromTemplate() throws Exception {
        FileObject projectDir=FileUtil.toFileObject(getWorkDir()).createFolder("Suite");
        File tmpl=getGoldenFile("converter.zip");
        AntProjectHelper aph=null;
        try {
            aph=J2MEProjectGenerator.
                    createProjectFromTemplate(FileUtil.toFileObject(tmpl),FileUtil.toFile(projectDir),"Suite",null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        assertNotNull(aph);
        assertTrue(checkFiles(projectDir,aph,false));
    }
    
    public void testCreateProjectFromWTK() throws Exception {
        File wtkdemo=getGoldenFile("WTKDemo");
        FileObject projectDir=FileUtil.toFileObject(getWorkDir()).createFolder("Demo");
        TestUtil.cpDir(FileUtil.toFileObject(wtkdemo),projectDir);
        AntProjectHelper aph=null;
        aph=J2MEProjectGenerator.
                createProjectFromWtkProject(FileUtil.toFile(projectDir),"WTKDemo",null,FileUtil.toFile(projectDir).getAbsolutePath());
        assertNotNull(aph);
        assertTrue(checkFiles(projectDir,aph,false));
    }
    
    public void testCreateProjectFromSource() throws Exception {
        FileObject scratchDir = TestUtil.makeScratchDir( this );
        FileObject projectDir = scratchDir.createFolder( "testProject" );
        FileObject dupldir = scratchDir.createFolder( "duplProject" );
        FileObject sources = projectDir.createFolder( "src" );
        File jad=getGoldenFile("MobileApplication.jad");
        FileObject main=sources.createData("Main.java");
        createMain(main);
        AntProjectHelper aph=null;
        try {
            
            aph=J2MEProjectGenerator.
                    createProjectFromSources(FileUtil.toFile(projectDir),"Test",
                    null,FileUtil.toFile(sources).getAbsolutePath(),jad.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        J2MEProject p1=(J2MEProject)ProjectManager.getDefault().findProject(projectDir);
        AntProjectHelper dph=J2MEProjectGenerator.duplicateProject(p1,FileUtil.toFile(dupldir),"Duplicate",true);
        J2MEProject p2=(J2MEProject)ProjectManager.getDefault().findProject(dupldir);
        assertNotNull(aph);
        assertNotNull(p2);
        assertTrue(checkFiles(projectDir,aph,true));
        assertNotNull(dph);
        assertTrue(checkFiles(dupldir,dph,true));
    }
    
    public void testCreateProject() throws Exception {
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        FileObject root = TestUtil.makeScratchDir(this);
        
        FileObject fo=FileUtil.toFileObject(getGoldenFile("Test_template.cfg"));
        ArrayList list=new ArrayList();
        list.add(fo);
        
        ProjectManager pm = ProjectManager.getDefault();
        AntProjectHelper aph =
                J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,list);
        assertNotNull(aph);
        File build=File.createTempFile("build",".properties",FileUtil.toFile(root));
        System.setProperty("user.properties.file",build.getAbsolutePath());
        TestUtil.setHelper(aph);
        /* To avoid raise conditions - save projects */
        pm.saveAllProjects();
        fo = aph.getProjectDirectory();
        assertTrue(checkFiles(fo,aph,false));
    }
    
    public void testLoadJadAndManifest() throws Exception {
        Map map = new HashMap();
        
        File wrkDir = getWorkDir();
        
        File jad = createTestJad(wrkDir);
        File manifest = createTestManifest(wrkDir);
        
        // Test dummy call
        map = new HashMap();
        J2MEProjectGenerator.loadJadAndManifest( map, null, null );
        assertTrue( "map not is empty", map.size() == 0 );
        
        // Test loading of JAD file
        map = new HashMap();
        J2MEProjectGenerator.loadJadAndManifest( map, jad, null );
        List l = new ArrayList(map.keySet());
        for( int i = 0; i < jadfileData.length; i++ ) {
            assertTrue( "keys are not identical", jadfileData[i][1].equals( map.get( jadfileData[i][0] )));
            l.remove( jadfileData[i][0] );
        }
        assertEquals("found unexpected property : " + l, jadfileData.length, map.keySet().size());
        
        // Test loading of manifest file
        map = new HashMap();
        J2MEProjectGenerator.loadJadAndManifest( map, null, manifest );
        l = new ArrayList(map.keySet());
        for( int i = 0; i < manifestData.length; i++ ) {
            assertTrue( "keys are not identical", manifestData[i][1].equals( map.get( manifestData[i][0] )));
            l.remove( manifestData[i][0] );
        }
        assertEquals("found unexpected property : " + l, manifestData.length, map.keySet().size());
    }
    
    /**
     * Create simple jad file for testing purposes
     */
    private File createTestJad(File workDir) throws Exception {
        FileObject jad = FileUtil.toFileObject( workDir ).createData( "test", "jad" );  // NOI18N
        FileLock lock = jad.lock();
        PrintWriter pw = new PrintWriter(jad.getOutputStream(lock));
        for( int i = 0; i < jadfileData.length; i++ ) {
            pw.println( jadfileData[i][0] + ": " + jadfileData[i][1] );
        }
        pw.flush();
        pw.close();
        lock.releaseLock();
        
        return FileUtil.toFile( jad );
    }
    
    /**
     * Create simple manifest file for testing purposes
     */
    private File createTestManifest(File workDir) throws Exception {
        FileObject manifest = FileUtil.toFileObject( workDir ).createData( "MANIFEST", "MF" );  // NOI18N
        FileLock lock = manifest.lock();
        PrintWriter pw = new PrintWriter(manifest.getOutputStream(lock));
        for( int i = 0; i < manifestData.length; i++ ) {
            pw.println( manifestData[i][0] + ": " + manifestData[i][1] );
        }
        pw.flush();
        pw.close();
        lock.releaseLock();
        
        return FileUtil.toFile( manifest );
    }
    
}
