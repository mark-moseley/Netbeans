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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * FolderObjTest.java
 * @author Radek Matous
 */
public class FolderObjTest extends NbTestCase {
    File testFile;
    
    
    public FolderObjTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        testFile = getWorkDir();        
    }

   public void testFileObjectDistributionWorksAccuratelyAccordingToChildrenCache() throws IOException  {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workDirFo);        
        File fold = new File(getWorkDir(),"fold");//NOI18N
        assertNull(FileUtil.toFileObject(fold));
        FileObject foldFo = workDirFo.createFolder(fold.getName());
        assertNotNull(foldFo);
        
        foldFo.delete();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));        
        assertNull(FileBasedFileSystem.getFileObject(fold));
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                
        assertNull(workDirFo.getFileObject(fold.getName()));                
        assertFalse(existsChild(workDirFo, fold.getName()));
        fold.mkdir();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));   
        assertNull(FileBasedFileSystem.getFileObject(fold));        
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                
        assertNull(workDirFo.getFileObject(fold.getName()));                        
        assertFalse(existsChild(workDirFo, fold.getName()));        
        workDirFo.refresh();
        assertNotNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));
        assertNotNull(FileBasedFileSystem.getFileObject(fold));        
        assertNotNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                
        assertNotNull(workDirFo.getFileObject(fold.getName()));        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        fold.delete();
        assertNotNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        assertNotNull(FileBasedFileSystem.getFileObject(fold));                
        assertNotNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        workDirFo.refresh();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));     
        assertNull(FileBasedFileSystem.getFileObject(fold));                
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNull(workDirFo.getFileObject(fold.getName()));                                        
        assertFalse(existsChild(workDirFo, fold.getName()));        
        fold.mkdir();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        assertNull(FileBasedFileSystem.getFileObject(fold));                        
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNull(workDirFo.getFileObject(fold.getName()));
        assertFalse(existsChild(workDirFo, fold.getName()));        
        workDirFo.getFileSystem().refresh(false);
        assertNotNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        assertNotNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNotNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        foldFo.delete();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));     
        assertNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNull(workDirFo.getFileObject(fold.getName()));                                        
        assertFalse(existsChild(workDirFo, fold.getName()));        
        fold.mkdir();
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        assertNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNull(workDirFo.getFileObject(fold.getName()));                                        
        assertFalse(existsChild(workDirFo, fold.getName()));        
        workDirFo.getFileSystem().refresh(false);
        assertNotNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));                
        assertNotNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNotNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        fold.delete();
        assertNotNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));           
        assertNotNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNotNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNotNull(workDirFo.getFileObject(fold.getName()));                                        
        assertTrue(existsChild(workDirFo, fold.getName()));        
        workDirFo.getFileSystem().refresh(false);
        assertNull(((FileBasedFileSystem)workDirFo.getFileSystem()).findFileObject(fold));              
        assertNull(FileBasedFileSystem.getFileObject(fold));                                
        assertNull((workDirFo.getFileSystem()).findResource(fold.getAbsolutePath()));                        
        assertNull(workDirFo.getFileObject(fold.getName()));                                
        assertFalse(existsChild(workDirFo, fold.getName()));                
    }
   
   private static boolean existsChild(final FileObject folder, final String childName) {
       FileObject[] childs = folder.getChildren();
       for (int i = 0; i < childs.length; i++) {
           if (childs[i].getNameExt().equals(childName)) {
               return true;
           } 
       }
       return false;
   }
        
    public void testChildren() throws Exception {
        final FolderObj testRoot = (FolderObj)FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        for (int i = 0; i < 5; i++) {
            assertTrue(new File (getWorkDir(), "file"+String.valueOf(i)).createNewFile());
        }                
        assertEquals(5,testRoot.getChildren().length);
    }
    
    public void testSize() throws Exception {
        final FolderObj testRoot = (FolderObj)FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        FileObj bfo = (FileObj)testRoot.createData(getName());

        assertSize("",Collections.singleton(bfo),32 ,new Object[] {bfo.getFileName()});        
        assertSize("",Collections.singleton(testRoot),32 ,new Object[] {testRoot.getFileName(), 
        testRoot.getChildrenCache()});                        
    }
    
    public void testCreateNbAttrs() throws IOException  {
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);        
        try {            
            testRoot.createData(".nbattrs");
            fail();
        } catch (IOException ex) {}        
    }
    
    public void testCreateWriteLock() throws IOException  {
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);        
        File file2lock = new File(getWorkDir(),"aa.txt");
        File associatedLock = WriteLockUtils.getAssociatedLockFile(file2lock);//NOI18N
        
        try {            
            testRoot.createData(associatedLock.getName());
            fail();
        } catch (IOException ex) {}        
    }
    
    
    public void testCaseInsensitivity() throws Exception {
        if (!Utilities.isWindows()) return;
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        
        File testa = new File(getWorkDir(), "a");
        File testA = new File(getWorkDir(), "A");
        
        if (testA.exists()) {
            assertTrue(testA.delete());
        }
        if (!testa.exists()) {
            assertTrue(testa.createNewFile());
        }

        //FileBasedFileSystem's case sensitivity depends on platform. This is different behaviour
        // than originally provided by AbstractFileSystem.
        FileObject A = testRoot.getFileObject("A");
        assertNotNull(A);
        assertNotNull(testRoot.getFileObject("a"));
        assertSame(testRoot.getFileObject("A"), testRoot.getFileObject("a"));
        assertSame(URLMapper.findFileObject(testa.toURI().toURL()), 
                URLMapper.findFileObject(testA.toURI().toURL()));
        
        //but 
        testRoot.getChildren();
        assertEquals("A",testRoot.getFileObject("A").getName());
        assertEquals("A",testRoot.getFileObject("a").getName());        
        BaseFileObj bobj = (BaseFileObj)testRoot.getFileObject("a");
        NamingFactory.checkCaseSensitivity(bobj.getFileName(),testa);
        assertEquals("a",testRoot.getFileObject("a").getName());                
        assertEquals("a",testRoot.getFileObject("A").getName());                        
    }
    
    private class TestListener extends FileChangeAdapter {
        private List fileObjects;
        TestListener(List fileObjects) {
            this.fileObjects = fileObjects;
        }
        public void fileFolderCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        public void fileDeleted(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        public void fileDataCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }        
    }
    
    public void testSimulatesRefactoringRename() throws Exception {
        if (!Utilities.isWindows()) return;
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(getWorkDir());
        assertNotNull(fs);
        final FileObject root = fs.findFileObject(getWorkDir());
        assertNotNull(root);
        FileObject main = root.createData("Main.java");
        FileUtil.createData(root,"subpackage/newclass.java");
        final List fileObjects = new ArrayList() {
            public boolean add(Object o) {
                assertNotNull(o);
                return super.add(o);
            }
            
        };
        final TestListener tl = new TestListener(fileObjects);
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage = root.getFileObject("subpackage");
                    FileObject newclass = subpackage.getFileObject("newclass.java");
                    FileObject subpackage1 = root.createFolder("subpackage1");
                    fileObjects.add(subpackage1);
                    FileObject newclass1 = subpackage1.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage.delete();
                    fileObjects.add(subpackage);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.toString(),fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("subpackage1"));
        assertNotNull(root.getFileObject("subpackage1/newclass.java"));
        assertNull(root.getFileObject("subpackage"));
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage1 = root.getFileObject("subpackage1");
                    FileObject newclass = root.getFileObject("subpackage1/newclass.java");
                    FileObject Subpackage = root.createFolder("Subpackage");
                    fileObjects.add(Subpackage);
                    FileObject newclass1 = Subpackage.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage1.delete();
                    fileObjects.add(subpackage1);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.toString(), fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("Subpackage/newclass.java"));
        assertNull(root.getFileObject("subpackage1"));
    }
    
    public void testRename() throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);

        final List l = new ArrayList ();
        FileChangeListener fcl = new FileChangeAdapter () {
             public void fileRenamed(FileRenameEvent fe) {
                 FileObject fold = (FileObject)fe.getFile();
                 assertTrue(fold.getChildren().length > 0);
                 l.add(fe);
             }
        };
        
        FileObject fo = fs.findResource(f.getAbsolutePath().replace('\\','/'));
        assertNotNull(fo);
        FileObject folder =fo.createFolder("testRename");
        assertNotNull(folder);
        
        FileObject file =folder.createData("test.txt");
        assertNotNull(file);
        folder.addFileChangeListener(fcl);
        assertTrue(folder.getChildren().length > 0);
        FileLock lock = folder.lock();
        try {
            folder.rename(lock,"renfolder","");            
            assertTrue(folder.getChildren().length > 0);
            assertTrue(!l.isEmpty());            
            
            l.clear();
            
            folder.rename(lock,"testRename","");            
            assertTrue(folder.getChildren().length > 0);
            assertTrue(!l.isEmpty());            
            
        } finally {
            lock.releaseLock();
        }
        
        
     }

     public void testRename2 () throws Exception {
        File test = new File (getWorkDir(), "testrename.txt");
        if (!test.exists()) {
            assertTrue(test.createNewFile());
        }
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(test);
        assertNotNull(fs);
        
        FileObject testFo = fs.findFileObject(test);
        assertNotNull (testFo);

        FileLock lock = testFo.lock();
        assertNotNull (lock);
        try {
         testFo.rename(lock, "TESTRENAME", "TXT");           
        } finally {
            lock.releaseLock();
        }
    }    

    /**
     * Test of getChildren method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetChildren() throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        assertNotNull(f.getAbsolutePath(),fs.findResource(f.getAbsolutePath()));
        
        while (f != null) {
            if (new FileInfo (f).isWindowsFloppy()) continue;
            FileObject fo0 = fs.findResource(f.getAbsolutePath());
            assertNotNull(f.getAbsolutePath(),fo0);
            
            FileObject fo = fs.getRoot().getFileObject(f.getAbsolutePath().replace('\\','/'));
            assertNotNull(f.getAbsolutePath(),fo);
            if (fo0 != fo) {
                fs.getRoot().getFileObject(f.getAbsolutePath().replace('\\','/'));
            }
            assertSame(fo.toString(), fo0, fo);
            
            if (f.getParentFile() != null) {
                FileObject parent = fo.getParent();
                assertNotNull(parent);
                String nameExt = fo.getNameExt();
                FileObject fo2 = parent.getFileObject(nameExt);
                assertNotNull(fo2);
                assertSame(fo,  fo2);
                FileObject[] fos = parent.getChildren();
                List list = Arrays.asList(fos);
                assertTrue((fo.toString()+ "  " + System.identityHashCode(fo)),list.contains(fo));

               
                WeakReference ref = new WeakReference (fo);
                String msg = fo.toString();
                fo = null; fo0 = null; fo2 = null; parent = null;fos = null; list = null;
                assertGC(msg, ref);                
            } else {
                //dsik roots are kept by hard reference
                WeakReference ref = new WeakReference (fo);
                String msg = fo.toString();
                fo = null; fo0 = null; 
                assertNotNull(msg, ref.get());                                
            }
            
            f = f.getParentFile();
        }        
    }
    
    public void testGetChildrenStackOwerflow() throws Exception {
        File f = testFile;
        assertTrue(f.exists());
        assertTrue(f.isDirectory());        
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        final FileObject fo = fs.findResource(f.getAbsolutePath()); 
        assertNotNull(f.getAbsolutePath(),fo);        
        assertTrue(fo.isFolder());
        assertEquals(0,fo.getChildren().length);
        assertTrue(new File(f,"child1").createNewFile());
        assertTrue(new File(f,"child2").createNewFile());
        final File child3 = new File(f,"child3");
        assertTrue(child3.createNewFile());
        final List keepThem = new ArrayList();
        fo.addFileChangeListener(new FileChangeAdapter(){
            public void fileDeleted(FileEvent fe) {
                for (Iterator it = keepThem.iterator(); it.hasNext();) {
                    FileObject fodel = (FileObject) it.next();
                    FileObject[] all =  fo.getChildren();
                    for (int i = 0; i < all.length; i++) {
                        all[i].refresh();
                    }
                    
                }
            }
            
            public void fileDataCreated(FileEvent fe) {
                FileObject ffoo = fe.getFile(); 
                keepThem.add(ffoo);
                ((BaseFileObj)ffoo).getFileName().getFile().delete();
                ffoo.refresh();
            }            
        } );

        fo.refresh();
        assertEquals(0,fo.getChildren().length);
    }
    
    /**
     * Test of isData method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsData() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        
        while (f != null) {            
            FileObject fo = fs.findResource(f.getAbsolutePath());
            assertNotNull(f.getAbsolutePath(),fo);
            assertEquals(f.isFile(), fo.isData());
            if (fo.isData ()) {
                assertTrue(fo instanceof FileObj);
            }
            
            f = f.getParentFile();
        }
    }
    
    /**
     * Test of isFolder method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsFolder() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        
        while (f != null) {            
            FileObject fo = fs.findResource(f.getAbsolutePath());
            assertNotNull(f.getAbsolutePath(),fo);
            if (fo.isFolder() && !fo.isRoot()) {
                assertTrue(fo instanceof FolderObj);
            }
           
            f = f.getParentFile();            
        }
    }
    
    /**
     * Test of isRoot method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsRoot() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        FileObject fo = null;
        while (f != null) {            
            fo = fs.findResource(f.getAbsolutePath());
            assertNotNull(f.getAbsolutePath(),fo);
            f = f.getParentFile();            
        }
        assertNotNull(fo.toString(), fo);        
        FileObject root = fo.getParent ();
        
        assertNotNull(root.toString(), root);                        
        assertTrue(root.isRoot());
        assertTrue(root instanceof RootObj);
        assertSame(root, fs.getRoot());        
        assertSame(fo, root.getFileObject(fo.getNameExt()));        
    }
    
    /**
     * Test of getFileObject method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetFileObject() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        
        while (f != null) {            
            FileObject fo = fs.findResource(f.getAbsolutePath());
            assertNotNull(f.getAbsolutePath(),fo);
            FileObject parent = fo.getParent();
            while (parent != null && parent != fo) {                
                assertNotNull(parent);
                String relativePath = FileUtil.getRelativePath(parent, fo);
                assertNotNull(relativePath);
                FileObject fo2 = parent.getFileObject(relativePath);
                assertNotNull((relativePath + " not found in " + parent.toString()), fo2);
                assertSame (fo, fo2);
                parent = parent.getParent();
            }

            assertNotNull(fs.getRoot().getFileObject(TestUtils.getFileObjectPath(((BaseFileObj)fo).getFileName().getFile ())));           
            f = f.getParentFile();            
        }        
    }
    
    
    /**
     * Test of createFolder method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testCreateFolder() throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);

        FileObject fo = fs.findResource(TestUtils.getFileObjectPath(testFile));
        
        File f2 = new File (testFile, "newfoldercreated");
        FileObject nfo = fo.createFolder (f2.getName());
        assertNotNull(nfo);
        assertTrue(nfo.isFolder());
        File nfile = ((BaseFileObj)nfo).getFileName().getFile ();
        assertSame(nfo, fs.findResource(TestUtils.getFileObjectPath(nfile)));
        assertSame(fo, nfo.getParent());
        
        try {
            FileObject nfo2 = fo.createFolder (f2.getName());    
            fail ();
        } catch (IOException iox) {
            
        }
        
    }

    /**
     * Test of createData method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testCreateData() throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);

        final FileObject fo = fs.findResource(TestUtils.getFileObjectPath(testFile));
        
        File f2 = new File (testFile, "newdatacreated.txt");
        final FileObject nfo = fo.createData (f2.getName());
        assertNotNull(nfo);
        assertTrue(nfo.isData());
        File nfile = ((BaseFileObj)nfo).getFileName().getFile ();
        assertEquals(nfo.getClass(), fs.findResource(TestUtils.getFileObjectPath(nfile)).getClass());
        assertSame(nfo, fs.findResource(TestUtils.getFileObjectPath(nfile)));
        /*if (nfo.getParent() != fo) {
            nfo.getParent();
        }*/
        System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
        FileObject pp = nfo.getParent();
        assertEquals(((BaseFileObj)pp).getFileName().getFile(), ((BaseFileObj)fo).getFileName().getFile());
        assertEquals(((BaseFileObj)pp).getFileName().getId(), ((BaseFileObj)fo).getFileName().getId());
        
        assertSame(((BaseFileObj)fo).getFileName().getId() + " | " + ((BaseFileObj)nfo.getParent()).getFileName().getId(), fo, pp);        
        
        try {
            FileObject nfo2 = fo.createData (f2.getName());    
            fail ();
        } catch (IOException iox) {
            
        }
    }

    
    /**
     * Test of delete method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testDelete() throws IOException {
        File f = testFile;
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(testFile);
        assertNotNull(testFo);
        
        final List l = new ArrayList ();
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
            }            
        };
        FileObject fo = FileUtil.createData(testFo, "delete/the/whole/structure/in/depth/todelete.txt");
        fo.addFileChangeListener(fcl);
        
        FileObject toDelete = testFo.getFileObject("delete");        
        assertNotNull(toDelete);
        toDelete.addFileChangeListener(fcl);

        FileObject toGC = testFo.getFileObject("delete/the/whole/structure");
        assertNotNull(toGC);
        Reference toGCRef = new WeakReference (toGC);
        toGC.addFileChangeListener(fcl);
        toGC = null;
        
        assertGC("", toGCRef);
        toDelete.delete();
        assertEquals(0,testFo.getChildren().length);
        toDelete = testFo.getFileObject("delete");        
        assertNull(toDelete);        
        assertEquals(2, l.size());
    }

    public void testDelete2() throws IOException {
        File f = testFile;
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(testFile);
        assertNotNull(testFo);
        assertEquals(0,testFo.getChildren().length);
        
        FileObject newFolder = testFo.createFolder("testDelete2");
        assertNotNull(newFolder);
        assertEquals(1,testFo.getChildren().length);
        
        newFolder.delete();
        assertFalse(newFolder.isValid());
        assertEquals(0,testFo.getChildren().length);
        assertNull(testFo.getFileObject(newFolder.getNameExt()));
    }
    
    public void testExternalDelete2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.refresh();
        assertEquals(1, l.size());        
    }

    public void testExternalDelete2_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        final FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().equals(testFo)) {
                    l.add(fe);
                }
                fe.getFile().refresh();
            }
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
        testFo.getFileSystem().removeFileChangeListener(fcl);
    }

    public void testExternalDelete2_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }    
            public void fileChanged(FileEvent fe) {
                fail();
            }            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
    }
    
    public void testExternalDelete3() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            

            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();

        //testFo.lastModified();
        //testFo.refresh();
        testFo.refresh();
        
        assertEquals(1, l.size());        
    }

    public void testExternalDelete3_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
        testFo.getFileSystem().removeFileChangeListener(fcl);

    }
    
    public void testExternalDelete3_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject testFo = fs.findFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
    }
    
    
    public void testExternalDelete4() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);        
        FileObject testFolder = fs.findFileObject(f);        
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile = testFolder.getFileObject(f.getName());        
        assertNotNull(testFile);
        assertTrue(testFile.isData());
                        

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        assertTrue(f.delete());
        testFolder.refresh();
        assertEquals(1, l.size());        
    }

    public void testExternalDelete4_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);        
        FileObject testFolder = fs.findFileObject(f);        
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile = testFolder.getFileObject(f.getName());  
        assertNotNull(testFile);
        assertTrue(testFile.isData());
                        

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.getFileSystem().refresh(true);
       testFolder.getFileSystem().removeFileChangeListener(fcl);        
        assertEquals(1, l.size());        

    }

    public void testExternalDelete4_1_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);        
        FileObject testFolder = fs.findFileObject(f);        
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile = fs.findFileObject(f);//!!!!!!
        assertNotNull(testFile);
        assertTrue(testFile.isData());
                        

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.getFileSystem().refresh(true);
       testFolder.getFileSystem().removeFileChangeListener(fcl);        
        assertEquals(1, l.size());        

    }
    
    public void testExternalDelete4_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(f);        
        FileObject testFolder = fs.findFileObject(f);        
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile = testFolder.getFileObject(f.getName());
        assertNotNull(testFile);
        assertTrue(testFile.isData());
                        

        final List l = new ArrayList ();        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.refresh(true);
        assertEquals(1, l.size());        
        testFolder.getFileSystem().removeFileChangeListener(fcl);

    }
    
    
    /**
     * Test of getInputStream method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetInputStream() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject root = fs.getRoot();
        assertNotNull(root);
        
        Enumeration en = root.getFolders(true);
        for (int i = 0; i < 10 && en.hasMoreElements(); i++) {
            FileObject fo = (FileObject) en.nextElement();
            assertTrue(fo.isFolder());
            assertFalse(fo.isData());
            try {
                fo.getInputStream();
                fail ();
            } catch (FileNotFoundException e) {
                
            }

        }
    }

    /*public void testRefresh () throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        FileObject testFo = fs.findResource(f.getAbsolutePath().replace('\\', '/'));
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());
        
        //testFo.getChildren();
        String childName = "testRefresh";
        File fooClassF = new File (testFile, childName);
        OutputStream os = new FileOutputStream(fooClassF);
        os.write(69); // force Mac OS X to update timestamp
        os.close();

        fs.refresh(true);
        final FileObject childFo = testFo.getFileObject(childName);
        childFo.lastModified();
        assertNotNull(childFo);
        Reference ref = new WeakReference (testFo);
        testFo = null;
        assertGC("", ref) ;
        
        final List l = new ArrayList ();
        fs.addFileChangeListener(new FileChangeAdapter () {
            public void fileChanged(FileEvent fe) {
                if (fe.getFile().equals(childFo)) {
                    l.add(childFo);
                }
            }            
        });
        Thread.sleep(2000);
        os = new FileOutputStream(fooClassF);
        os.write(69); // force Mac OS X to update timestamp
        os.close();
        
        fs.refresh(true);
        assertTrue(!l.isEmpty());
    }*/

    //see issue: #43231 and #56285
    public void testRefresh43231() throws Exception {
        File thisTest = new File(getWorkDir(),getName());
        thisTest.createNewFile();
        FileObject testf = FileBasedFileSystem.getFileObject(thisTest);
        final List l = new ArrayList(); 
        testf.addFileChangeListener(new FileChangeAdapter(){
            public void fileChanged(FileEvent fe) {
                if (l.isEmpty()) {                    
                    fail();
                }
                l.clear();
            }            
        });
        //first refresh after initialization compares
        //lastModified with oldLastModified this way:
        //if (lastModified > oldLastModified) the fileChange
        thisTest.setLastModified(1000000);        
        testf.refresh();
        assertTrue(l.isEmpty());        

        //every next refresh compares
        //lastModified with oldLastModified this way:
        //if (lastModified != oldLastModified) the fileChange                
        l.add("not empty");        
        thisTest.setLastModified(10000);
        testf.refresh();
        assertTrue(l.isEmpty());
    }
    
    public void testRefresh69744() throws Exception {
        File thisTest = new File(getWorkDir(),"thisTest");
        thisTest.createNewFile();
        FileObject testf = FileBasedFileSystem.getFileObject(thisTest);
        assertNotNull(testf);
        assertGC("",new WeakReference(testf.getParent()));        
        modifyFileObject(testf, "abc");
        FileSystem fs = testf.getFileSystem();
        final List l = new ArrayList();
        FileChangeListener fcl = new FileChangeAdapter() {
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
        };
        Thread.sleep(1500);
        fs.addFileChangeListener(fcl);
        try {
            modifyFileObject(testf, "def");
            assertFalse(l.isEmpty());
        } finally {
            fs.removeFileChangeListener(fcl);
        }
    }

    private void modifyFileObject(final FileObject testf, String content) throws IOException {
        FileLock lock = null;
        OutputStream os = null;
        try {
            lock = testf.lock();
            os = testf.getOutputStream(lock);
            os.write(content.getBytes());
        } finally {
            if (os != null) os.close();
            if (lock != null) lock.releaseLock();            
        }
    }
    
    public void testFileTypeChanged() throws Exception {
        String newFileName = "test";
        File f = new File(testFile, "testFileTypeNotRemembered/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(testFile);
        FileObject parent = fs.findFileObject(testFile);
        
        assertNotNull(parent);
        assertTrue(parent.isFolder());
        FileObject fo = parent.getFileObject("testFileTypeNotRemembered");
        assertTrue(fo.isFolder());
        
        fo.delete();
        FileObject fo2 = parent.createFolder("testFileTypeNotRemembered");
        assertNotNull(fo);
        assertTrue(fo != fo2);
        
        fo2 = fs.findFileObject(f);
        assertNotNull(fo);
        assertTrue(fo != fo2);
    }
    
    public void testRefresh2 () throws Exception {
        String childName = "refreshtest.txt";
        FileSystem fs = FileBasedFileSystem.getInstance(testFile);
        final File file = new File (testFile, childName);
        FileObject parent = fs.findResource(testFile.getAbsolutePath().replace('\\', '/'));
        assertNotNull(parent);

        file.createNewFile();
        parent.getFileObject(childName);        
        parent.getChildren();
        fs.refresh(true);

        final ArrayList deleted = new ArrayList ();
        final ArrayList created = new ArrayList ();
        
        FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) {
                    String p = fo.toString();
                    deleted.add(fo);
                }
            }

            public void fileDataCreated(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) {
                    String p = fo.toString();
                    created.add(fo);
                }
            }
            
        };
        fs.addFileChangeListener(fcl);
        int stepsCount = 10;
        for (int i = 0; i < stepsCount; i++) {
            assertTrue(file.delete());
            fs.refresh(true);
            
            assertTrue(file.createNewFile());
            fs.refresh(true);            
        }
        
        fs.removeFileChangeListener(fcl);
        assertEquals(stepsCount,deleted.size());
        assertEquals(stepsCount,created.size());
        
    }

    public void testRefresh3 () throws Exception {
        String childName = "refreshtest2.txt";
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance(testFile);
        final File file = new File (testFile, childName);
        FileObject parent = fs.findResource(testFile.getAbsolutePath().replace('\\', '/'));
        assertNotNull(parent);


        final ArrayList events = new ArrayList ();

        final ArrayList deletedIncrement = new ArrayList ();
        final ArrayList createdIncrement = new ArrayList ();
        
        final ArrayList hardRef = new ArrayList ();
        final FileChangeListener fcl = new FileChangeAdapter () {
            public void fileDeleted(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) 
                {
                    String p = fo.toString();
                    assertEquals(0, events.size());
                    assertTrue(!fo.isValid());                    
                    events.add("fo");
                    deletedIncrement.add("fo");
                    fo.removeFileChangeListener(this);
                    fo.getParent().addFileChangeListener(this);
                    hardRef.clear();                    
                    hardRef.add(fo.getParent());                                        
                    fo.getParent().getChildren ();
                }
            }


            public void fileDataCreated(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) 
                {
                    String p = fo.toString();
                    assertEquals(1,events.size());
                    assertTrue(fo.isValid());
                    assertTrue(events.remove("fo"));
                    createdIncrement.add("fo");
                    fo.getParent().removeFileChangeListener(this);                    
                    fo.addFileChangeListener(this);
                    hardRef.clear();                    
                    hardRef.add(fo);
                    
                }
            }

            
        };
        fs.refresh(true);
        file.createNewFile();
        hardRef.add(parent.getFileObject(childName));        
        parent.getFileObject(childName).addFileChangeListener(fcl);        
        parent = null;
        int stepsCount = 10;
        Reference ref2 = new WeakReference (fs.findFileObject(file.getParentFile()));
        assertGC("", ref2);                                
        
        for (int i = 0; i < stepsCount; i++) {
            assertTrue(file.delete());
            fs.refresh(true);
            Reference ref = new WeakReference (fs.findFileObject(file));
            assertGC("", ref);                    
            
            
            assertTrue(file.createNewFile());
            fs.refresh(true);            
                        
            ref = new WeakReference (fs.findFileObject(file.getParentFile()));
            assertGC(file.getParentFile().getAbsolutePath(), ref);                                                
        }
        
        fs.removeFileChangeListener(fcl);
        assertEquals(0,events.size());
        assertEquals(stepsCount,createdIncrement.size());
        assertEquals(stepsCount,deletedIncrement.size());
        
    }
    
    /**
     * Test of getOutputStream method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetOutputStream() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        
        FileObject root = fs.getRoot();
        assertNotNull(root);
        
        Enumeration en = root.getFolders(true);
        for (int i = 0; i < 10 && en.hasMoreElements(); i++) {
            FileObject fo = (FileObject) en.nextElement();
            assertTrue(fo.isFolder());
            assertFalse(fo.isData());
            try {
                fo.getOutputStream(fo.lock());
                fail ();
            } catch (IOException e) {
                
            } finally {
                
            }

        }
    }
    
    public void testReadWrite ( ) throws Exception{
        String content = "content of data file";
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        
        BaseFileObj fo = (BaseFileObj)fs.findResource(testFile.getAbsolutePath().replace('\\','/'));
        assertNotNull(fo);
        File dFile = new File (fo.getFileName().getFile(),"newreadwrite.txt");
        BaseFileObj data = (BaseFileObj)fo.createData(dFile.getName());
                
        FileLock lock = data.lock();
        try {
            OutputStream os = data.getOutputStream(lock);
            os.write(content.getBytes());            
            os.close();
        } finally {
            lock.releaseLock();
        }
        
        InputStream is = data.getInputStream();
        byte[] b = new byte [content.length()];
        assertEquals(content.length(), is.read(b));
        assertEquals(new String (b),new String (b), content);                        
    }

    public void testIsLightWeightLockRequiredRequired() throws Exception {
        File f = getWorkDir();
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        FolderObj fo = (FolderObj)fs.findResource(f.getAbsolutePath());        
        assertNotNull(fo);
        
        assertFalse(fo.isLightWeightLockRequired());
        FileLock fakeLock = (FileLock)fo.getAttribute(FolderObj.LIGHTWEIGHT_LOCK_SET);
        assertNotNull(fakeLock);
        assertTrue(fo.isLightWeightLockRequired());
        fakeLock.releaseLock();
        assertFalse(fo.isLightWeightLockRequired());        
        fakeLock = (FileLock)fo.getAttribute(FolderObj.LIGHTWEIGHT_LOCK_SET);
        assertNotNull(fakeLock);        assertTrue(fo.isLightWeightLockRequired());
        fakeLock.releaseLock();
        assertFalse(fo.isLightWeightLockRequired());                
    }
    
    public void testLightWeigtLock() throws Exception {
        File f = new File(getWorkDir(), "testFile.txt");
        if (!f.exists()) {
            assertTrue(f.createNewFile());
        }
        File lckFile = WriteLockUtils.getAssociatedLockFile(f);
        
        FileSystem fs = FileBasedFileSystem.getInstance(f);
        assertNotNull(fs);
        FileObject foChild = fs.findResource(f.getAbsolutePath());
        assertNotNull(foChild);
        
        FileObject folder = foChild.getParent();
        assertNotNull(folder);
        
        FileLock lock = foChild.lock();
        assertTrue(lckFile.exists());        
        lock.releaseLock();

        FileLock fakeLock = (FileLock)folder.getAttribute(FolderObj.LIGHTWEIGHT_LOCK_SET);
        assertNotNull(fakeLock);
        lock = foChild.lock();
        assertFalse(lckFile.exists());        
        lock.releaseLock();        

        fakeLock.releaseLock();
        lock = foChild.lock();
        assertTrue(lckFile.exists());
        lock.releaseLock();                
    }
    
    
    public File getWorkDir() throws IOException {
        return super.getWorkDir();
    }

}
