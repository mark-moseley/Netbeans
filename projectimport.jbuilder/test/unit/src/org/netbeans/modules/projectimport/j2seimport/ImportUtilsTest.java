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

package org.netbeans.modules.projectimport.j2seimport;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/**
 *
 * @author Radek Matous
 */
public class ImportUtilsTest  extends NbTestCase {
    protected AbstractProject testProject;
    
    static {
        System.setProperty("projectimport.logging.level", "FINEST");
    }
    
    
    public ImportUtilsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        try {
            testProject = new AbstractProject(getName(), FileUtil.toFileObject(getWorkDir()));
        } catch(IOException iex) {
            assert false : iex.getLocalizedMessage();
            throw new IllegalStateException(iex.getLocalizedMessage());
        }
    }
    
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ImportUtilsTest.class);
        
        return suite;
    }
    
    /**
     * Test of importProjectWithoutDependencies method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.ImportUtils.
     */
    public void testSourceRoots() throws Exception  {
        File src1 = new File(getWorkDir(), "src1");
        assertTrue(src1.mkdir() );
        
        File src2 = new File(getWorkDir(), "src2");
        assertTrue(src2.mkdir() );
        
        File projectDir = new File(getWorkDir(), "projectDir");
        assertTrue(projectDir.mkdir() );
        
        
        AbstractProject.SourceRoot srcE1 = new AbstractProject.SourceRoot(src1.getName(), src1);
        assertTrue(srcE1.isValid());
        AbstractProject.SourceRoot srcE2 = new AbstractProject.SourceRoot(src2.getName(), src2);
        assertTrue(srcE2.isValid());
        
        testProject.addSourceRoot(srcE1);
        testProject.addSourceRoot(srcE2);
        
        WarningContainer projectDefinitionWarnings = testProject.getWarnings();
        assertTrue(projectDefinitionWarnings.isEmpty());
        
        
        WarningContainer importingWarnings = new WarningContainer();
        J2SEProject nbProject = ImportUtils.createInstance().importProjectWithoutDependencies(FileUtil.toFileObject(projectDir), testProject, importingWarnings, false);
        assertTrue(importingWarnings.isEmpty());
        
        testProject.setAsImported();
        ImportUtilsTest.testSourceRoots(testProject, nbProject);
        ImportUtilsTest.testSourceRoots2(testProject, nbProject);
        
    }
    
    public void testSourceRootsWithWarning() throws Exception  {
        File src1 = new File(getWorkDir(), "src1");
        assertTrue(src1.mkdir() );
        
        File src2 = new File(getWorkDir(), "src2");
        
        //not created to cause warning
        //assertTrue(src2.mkdir() );
        
        File projectDir = new File(getWorkDir(), "projectDir");
        assertTrue(projectDir.mkdir() );
        
        
        AbstractProject.SourceRoot srcE1 = new AbstractProject.SourceRoot(src1.getName(), src1);
        assertTrue(srcE1.isValid());
        AbstractProject.SourceRoot srcE2 = new AbstractProject.SourceRoot(src2.getName(), src2);
        assertFalse(srcE2.isValid());
        
        testProject.addSourceRoot(srcE1);
        testProject.addSourceRoot(srcE2);
        
        WarningContainer projectDefinitionWarnings = testProject.getWarnings();
        assertTrue(projectDefinitionWarnings.size() == 1);
        
        
        WarningContainer importingWarnings = new WarningContainer();
        J2SEProject nbProject = ImportUtils.createInstance().importProjectWithoutDependencies(FileUtil.toFileObject(projectDir), testProject, importingWarnings, false);
        assertTrue(importingWarnings.toString(), importingWarnings.isEmpty());
        
        testProject.setAsImported();
        ImportUtilsTest.testSourceRoots(testProject, nbProject);
        ImportUtilsTest.testSourceRoots2(testProject, nbProject);
    }
    
    public void testLibraries() throws Exception  {
        File src1 = new File(getWorkDir(), "src1");
        assertTrue(src1.mkdir() );
        
        AbstractProject.SourceRoot srcE1 = new AbstractProject.SourceRoot(src1.getName(), src1);
        assertTrue(srcE1.isValid());
        
        AbstractProject.Library library1 =
                new AbstractProject.Library(AbstractProjectDefinitionTest.createArchivFile(getWorkDir(),"lib1.jar"));//NOI18N
        assertTrue(library1.isValid());
        
        
        AbstractProject.Library library2 =
                new AbstractProject.Library(AbstractProjectDefinitionTest.createArchivFile(getWorkDir(),"lib2.jar"));//NOI18N
        assertTrue(library2.isValid());
        
        
        File projectDir = new File(getWorkDir(), "projectDir");
        assertTrue(projectDir.mkdir() );
        
        
        testProject.addSourceRoot(srcE1);
        testProject.addLibrary(library1);
        testProject.addLibrary(library2);
        
        WarningContainer projectDefinitionWarnings = testProject.getWarnings();
        assertTrue(projectDefinitionWarnings.isEmpty());
        
        
        WarningContainer importingWarnings = new WarningContainer();
        J2SEProject nbProject = ImportUtils.createInstance().importProjectWithoutDependencies(FileUtil.toFileObject(projectDir), testProject, importingWarnings, false);
        assertTrue(importingWarnings.isEmpty());
        
        testProject.setAsImported();
        
        ImportUtilsTest.testSourceRoots(testProject, nbProject);
        ImportUtilsTest.testSourceRoots2(testProject, nbProject);
        ImportUtilsTest.testLibraries(testProject, nbProject);
    }
    
    /**
     * Test of addDependencies method, of class org.netbeans.modules.projectimport.jbuilder.j2seimport.ImportUtils.
     */
    public void testAddDependency() throws Exception{
        File projectDir = new File(getWorkDir(), "projectDir");
        File subPrjDir = new File(getWorkDir(), "subPrjDir");
        assertTrue(subPrjDir.mkdir());
        
        testLibraries();//for setting src root and some libraries
        
        AbstractProject subPrj = new AbstractProject("sub1", FileUtil.toFileObject(getWorkDir()));
        assertNotNull(subPrj);

        FileObject subPrjDirFo = FileUtil.toFileObject(subPrjDir);
        assertNotNull(subPrjDirFo);
        
        ImportUtils importInstance = ImportUtils.createInstance();
        J2SEProject subNbProject = importInstance.importProjectWithoutDependencies(subPrjDirFo, subPrj,new WarningContainer(), false);
        assertNotNull(subNbProject);
        
        //testProject.addDependency(subPrj);
        
        J2SEProject nbProject = (J2SEProject)ProjectManager.getDefault().findProject(FileUtil.toFileObject(projectDir));
        assertNotNull(nbProject);

        SubprojectProvider sProvider = (SubprojectProvider)nbProject.getLookup().lookup(SubprojectProvider.class);        
        assertNotNull(sProvider);
        
        
        assertFalse(sProvider.getSubprojects().contains(subNbProject));        
        importInstance.addDependency(nbProject, subNbProject);                
        assertTrue(sProvider.getSubprojects().contains(subNbProject));
    }
    
    
    private static void testLibraries(ProjectModel projectDefinition, J2SEProject nbProject) throws Exception{
        SourceRoots roots = nbProject.getSourceRoots();
        Sources src = ProjectUtils.getSources(nbProject);
        //workaround for unit code tests (isn't necessary for IDE run)
        src.getSourceGroups(Sources.TYPE_GENERIC);
        assertTrue(roots.getRoots().length > 0);
        ClassPath cls = ClassPath.getClassPath(roots.getRoots()[0], ClassPath.COMPILE);
        assertTrue(projectDefinition.getSourceRoots().size() > 0);
        //ProjectDefinition.SourceRootEntry srcEntry = (ProjectDefinition.SourceRootEntry )projectDefinition.getSourceRootEntries().iterator().next();
        //boolean isValid = ((AbstractProjectDefinition.AbstractSourceEntry)srcEntry).isValid();
        //if (isValid) {
        List rootList = Arrays.asList(cls.getRoots());
        for (Iterator it2 = projectDefinition.getLibraries().iterator(); it2.hasNext(); ) {
            ProjectModel.Library lEntry = (ProjectModel.Library)it2.next();
            FileObject archive = FileUtil.toFileObject(lEntry.getArchiv());
            assertNotNull(archive);
            FileObject archiveRoot = FileUtil.getArchiveRoot(archive);
            assertNotNull(archiveRoot);
            assertTrue(rootList.contains(archiveRoot));
        }
        //}
        
    }
    
    private static void testSourceRoots2(ProjectModel projectDefinition, J2SEProject nbProject) throws Exception{
        ClassPathProvider cp = (ClassPathProvider)nbProject.getLookup().lookup(ClassPathProvider.class);
        assertNotNull(cp);
        
        for (Iterator it = projectDefinition.getSourceRoots().iterator(); it.hasNext(); ) {
            ProjectModel.SourceRoot srcEntry = (ProjectModel.SourceRoot)it.next();
            boolean isValid = ((AbstractProject.SourceRoot)srcEntry).isValid();
            if (isValid) {
                FileObject sourceFolder = FileUtil.toFileObject(srcEntry.getDirectory());
                assertNotNull(sourceFolder);
                ClassPath clsPath = cp.findClassPath(sourceFolder, ClassPath.SOURCE);
                assertNotNull(sourceFolder.getPath(),cp.findClassPath(sourceFolder, ClassPath.SOURCE));
                List roots = Arrays.asList(clsPath.getRoots());
                assertTrue(roots.contains(sourceFolder));
                
            }
        }
    }
    
    //private static void testJavaPlatform(ProjectDefinition projectDefinition, J2SEProject nbProject) throws Exception{
    
    
    private static void testSourceRoots(ProjectModel projectDefinition, J2SEProject nbProject) throws Exception{
        SourceRoots roots = nbProject.getSourceRoots();
        
        List rootFObjects = Arrays.asList(roots.getRoots());
        List rootNames = Arrays.asList(roots.getRootNames());
        List rootURLs = Arrays.asList(roots.getRootURLs());
        
        
        for (Iterator it = projectDefinition.getSourceRoots().iterator(); it.hasNext(); ) {
            ProjectModel.SourceRoot srcEntry = (ProjectModel.SourceRoot)it.next();
            FileObject srcFolder = FileUtil.toFileObject(srcEntry.getDirectory());
            boolean isValid = ((AbstractProject.SourceRoot)srcEntry).isValid();
            assertEquals(isValid, srcFolder != null);
            assertTrue(!isValid || rootFObjects.contains(srcFolder));
            assertTrue(!isValid || rootURLs.contains(srcEntry.getDirectory().toURI().toURL()));
            assertTrue(rootNames.contains(srcEntry.getLabel()));
        }
        
        assertEquals(rootNames.size(),projectDefinition.getSourceRoots().size());
        assertEquals(rootURLs.size(),projectDefinition.getSourceRoots().size());
    }        
}
