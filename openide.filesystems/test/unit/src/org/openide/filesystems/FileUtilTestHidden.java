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

package org.openide.filesystems;


import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.openide.util.Utilities;

public class FileUtilTestHidden extends TestBaseHid {
    private FileObject root = null;

    protected String[] getResources(String testName) {
        return new String[]{
            "fileutildir/tofile.txt",
            "fileutildir/tofileobject.txt",
            "fileutildir/isParentOf.txt",
            "fileutildir/fileutildir2/fileutildir3",
            "fileutildir/fileutildir2/folder/file"                                
        };
    }

    protected void setUp() throws Exception {
        super.setUp();
        Repository.getDefault().addFileSystem(testedFS);
        root = testedFS.findResource(getResourcePrefix());
    }

    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(testedFS);
        super.tearDown();
    }

    /** Creates new FileObjectTestHidden */
    public FileUtilTestHidden(String name) {
        super(name);
    }

    public void testCreateFolder () throws Exception {
        if (this.testedFS instanceof JarFileSystem) return;
        
        assertNotNull(root);        
        FileObject folder = root.getFileObject("fileutildir");
        FileObject result = FileUtil.createFolder(folder, "fileutildir2/folder/fileutildir4");
        assertNotNull(result);
        assertSame(result, root.getFileObject("fileutildir/fileutildir2/folder/fileutildir4"));        
    }
    
    public void testNormalizeFile () throws Exception {
       File file = getWorkDir ();
       file = FileUtil.normalizeFile(file); 
       
       File file2 = FileUtil.normalizeFile(file); 
       assertSame(file, file2);
       
       file2 = new File (file, "test/..");
       file2 = FileUtil.normalizeFile(file); 
       assertEquals(file2, file);
    }
    
    public void testNormalizeFile2 () throws Exception {
        if (!Utilities.isWindows()) return;
        
        File rootFile = FileUtil.toFile(root);
        if (rootFile == null) return;
        assertTrue(rootFile.exists());
        
        File testFile = new File (rootFile, "abc.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());
        
        File testFile2 = new File (rootFile, "ABC.TXT");
        assertTrue(testFile2.exists());
        
        assertSame(testFile, FileUtil.normalizeFile(testFile));        
        assertNotSame(testFile2, FileUtil.normalizeFile(testFile2));        
    }   
    
    public void testFindFreeFolderName () throws Exception {
        if (this.testedFS.isReadOnly()) return;

        String name = FileUtil.findFreeFolderName(root, "fileutildir".toUpperCase());
        root.createFolder(name);        
    }
    
    public void testToFile() throws Exception {
        if (this.testedFS instanceof JarFileSystem) return;
        
        assertNotNull(root);
        FileObject testFo = root.getFileObject("fileutildir/tofile.txt");
        assertNotNull(testFo);

        File testFile = FileUtil.toFile(testFo);
        assertNotNull(testFile);
        assertTrue(testFile.exists());
    }
    
    
    public void testIsArchiveFile () throws Exception {
        final String base = getWorkDir().toURI().toURL().toExternalForm();
        URL url = new URL (base + "test.jar");    //NOI18N
        assertTrue ("test.jar has to be an archive",FileUtil.isArchiveFile (url));  //NOI18N
        url = new URL (base + ".hidden.jar");   //NOI18N
        assertTrue (FileUtil.isArchiveFile (url));  //NOI18N
        url = new URL (base + "folder");    //NOI18N
        assertFalse ("folder cannot to be an archive", FileUtil.isArchiveFile (url));   //NOI18N
        url = new URL (base + "folder/");    //NOI18N
        assertFalse ("folder cannot to be an archive", FileUtil.isArchiveFile (url));   //NOI18N
        url = new URL (base + ".hidden");   //NOI18N
        assertFalse (".hidden cannot to be an archive", FileUtil.isArchiveFile (url));  //NOI18N
        url = new URL (base + ".hidden/");   //NOI18N
        assertFalse (".hidden cannot to be an archive", FileUtil.isArchiveFile (url));  //NOI18N
    }


    public void testToFileObject () throws Exception {
        if (this.testedFS instanceof JarFileSystem) return;
        
        assertNotNull(root);
        FileObject testFo = root.getFileObject("fileutildir/tofileobject.txt");
        assertNotNull(testFo);

        File rootFile = FileUtil.toFile(root);
        assertNotNull(rootFile);
        assertTrue(rootFile.exists());

        File testFile = new File (rootFile, "fileutildir/tofileobject.txt");
        assertNotNull(testFile);
        assertTrue(testFile.exists());

        FileObject testFo2 = FileUtil.toFileObject(testFile);
        assertNotNull(testFo2);
        assertEquals(testFo2, testFo);
    }

    public void testIsParentOf () throws Exception {
        if (this.testedFS instanceof JarFileSystem) return;
        
        final List events = new ArrayList();
        assertNotNull(root);
        final FileObject parent = root.getFileObject("fileutildir");
        assertNotNull(parent);
        
        final FileObject child = root.getFileObject("fileutildir/isParentOf.txt");
        assertNotNull(child);
        
        assertTrue(FileUtil.isParentOf(parent, child));
                
        child.addFileChangeListener(new FileChangeAdapter() {
            public void fileDeleted(FileEvent fe) {
                FileObject file = fe.getFile();
                assertTrue(FileUtil.isParentOf(parent, file));
                assertEquals(parent, file.getParent());
                events.add(fe);
            }
        });
        child.delete();      
        assertTrue (events.size() == 1);
        assertNull(root.getFileObject("fileutildir/isParentOf.txt"));
        
    }


    public void testIsParentOf3 () throws Exception {
        if (this.testedFS instanceof JarFileSystem) return;
        
        final List events = new ArrayList();
        assertNotNull(root);
        final FileObject[] fileObjects = new FileObject[]{
            root.getFileObject("fileutildir"),
            root.getFileObject("fileutildir/fileutildir2"),
            root.getFileObject("fileutildir/fileutildir2/fileutildir3")
        };
        
        for (int i = 0; i < fileObjects.length; i++) {
            FileObject fo = fileObjects[i];
            assertNotNull(fo);            
        }
                
        assertTrue(FileUtil.isParentOf(root, fileObjects[0]));        
        assertTrue(FileUtil.isParentOf(fileObjects[0], fileObjects[1]));
        assertTrue(FileUtil.isParentOf(fileObjects[1], fileObjects[2]));        
                
        testedFS.addFileChangeListener(new FileChangeAdapter() {
            public void fileDeleted(FileEvent fe) {
                FileObject file = fe.getFile();
                assertNotNull(file.getPath(),file.getParent());
                assertTrue(file.getPath(), FileUtil.isParentOf(root, file));
                events.add(fe);
            }
        });
        fileObjects[1].delete();      
        assertTrue (events.size() > 0);        
    }
    
    public void testGetFileDisplayName ()  throws Exception {        
        final FileObject[] fileObjects = new FileObject[]{
            root,
            root.getFileObject("fileutildir"),
            root.getFileObject("fileutildir/fileutildir2")
        };

        for (int i = 0; i < fileObjects.length; i++) {
            FileObject fo = fileObjects[i];
            assertNotNull(fo);
            String displayName = FileUtil.getFileDisplayName(fo);
            File f = FileUtil.toFile(fo);
            if (f != null) {
                assertEquals(f.getAbsolutePath(), displayName);                
            } else {
                FileObject archivFo = FileUtil.getArchiveFile (fo);
                File archiv = (archivFo != null) ? FileUtil.toFile(archivFo) : null;
                if (archiv != null) {
                    if (fo.isRoot()) {
                        assertEquals(displayName, archiv.getAbsolutePath());                                                    
                    } else {
                        assertTrue(displayName.indexOf(archiv.getAbsolutePath()) != -1);    
                    }                    
                } else {
                    if (fo.isRoot()) {
                        assertEquals(displayName, fo.getFileSystem().getDisplayName());                                                                            
                    } else {
                        assertTrue(displayName.indexOf(fo.getFileSystem().getDisplayName()) != -1);                                                
                    }                                        
                }                
            }
        }                
    }
}
