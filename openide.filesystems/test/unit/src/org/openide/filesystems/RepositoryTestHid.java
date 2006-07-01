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

package org.openide.filesystems;
import java.io.*;
/**
 *
 * @author  rm111737
 */
public class RepositoryTestHid extends TestBaseHid {
    Repository repo;
    FileSystem defFs;
    String pkg = "/root/folder1/folder2";
    String name = "resource";
    String ext = "ext";
    String resource1 = pkg+"/"+name+"."+ext;
    /** Creates new TT */
    public RepositoryTestHid(String testName) {
        super (testName);
    }

    /** Test can require some resources to be part of filesystem that will be tested
     * @return array of resources
     */
    protected String[] getResources(String testName) {
        return new String[] {resource1};
    }    

    protected void setUp() throws java.lang.Exception {
        super.setUp();
        defFs = TestUtilHid.createLocalFileSystem("defaultFs", new String[] {} );
        repo = new Repository (defFs);
    }
    
    /** Test of getDefault method, of class org.openide.filesystems.Repository. 
     * No able to test this method in external execution
     */
    /*public void testGetDefault() {
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    /** Test of getDefaultFileSystem method, of class org.openide.filesystems.Repository. */
    public void testGetDefaultFileSystem() {
        FileSystem retFs = repo.getDefaultFileSystem();
        fsAssert("Default file system should not be null", retFs != null);        
        fsAssert(defFs + " should be default file system", defFs.equals(retFs));
    }
    

    /** Test of addFileSystem method, of class org.openide.filesystems.Repository. */
    public void testAddFileSystem() {
        repo.addFileSystem(testedFS);
        repo.addFileSystem(testedFS);        
        int count = 0;
        java.util.Enumeration en = repo.getFileSystems();
        while (en.hasMoreElements()) {
            if (en.nextElement().equals(testedFS))
                count++;
        }
        fsAssert(testedFS+" can be present in Repository only once.",count == 1);
    }
    
    /** Test of addFileSystem method, of class org.openide.filesystems.Repository. */
    public void testAddFileSystem2() {
        if (!testedFS.getSystemName().equals("")) {               
            repo.addFileSystem(testedFS);        
            Repository repo2 = new Repository (testedFS);
            int count = 0;
            if (repo.findFileSystem(testedFS.getSystemName()) != null)
                count++;
            if (repo2.findFileSystem(testedFS.getSystemName()) != null)
                count++;

            fsAssert(" FileSystem can be included only in one Repository.",count == 1);
        }
    }

        
    /** Test of removeFileSystem method, of class org.openide.filesystems.Repository. */
    public void testRemoveFileSystem() {
        if (!testedFS.getSystemName().equals("")) {       
            repo.addFileSystem(testedFS);
            FileSystem retFs = repo.findFileSystem(testedFS.getSystemName());        
            fsAssert(testedFS+" was added to Repository and was not found.",testedFS.equals(retFs));
            repo.removeFileSystem(retFs);
            retFs = repo.findFileSystem(retFs.getSystemName());
            fsAssert(testedFS + " " +  testedFS.getSystemName() +" was removed from Repository and is still present.",retFs == null);        
        }
    }
    
    /** Test of reorder method, of class org.openide.filesystems.Repository. */
    public void testReorder() {
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] {testedFS});
        repo.addFileSystem(testedFS);
        repo.addFileSystem(mfs);        
        repo.reorder(new int[] {2,0,1});
        FileSystem[] fss = repo.toArray();
        fsAssert("Wrong reordered",fss[0] == mfs && fss[1] == defFs && fss[2] == testedFS);
    }
    
    /** Test of getFileSystems method, of class org.openide.filesystems.Repository. */
    public void testGetFileSystems() {
        repo.addFileSystem(testedFS);
        java.util.Enumeration en = repo.getFileSystems();
        FileSystem[]  fss = new FileSystem[2];
        for (int i = 0; en.hasMoreElements(); i++) {
            fss[i] = (FileSystem)en.nextElement();
        }
        fsAssert("Expected two elements in enumeration",fss.length == 2);                    
        fsAssert("Expected two different elements in enumeration",fss[0] != fss[1]);                            
        fsAssert("Wrong filesystems in enumeration",
        (fss[0] == defFs && fss[1] == testedFS) || (fss[1] == defFs && fss[0] == testedFS)); 
    }
    
    /** Test of fileSystems method, of class org.openide.filesystems.Repository. */
    public void testFileSystems() {
        testGetFileSystems();
    }
    
    /** Test of toArray method, of class org.openide.filesystems.Repository. */
    public void testToArray() {
        FileSystem[] fss;
        fss = repo.toArray();
        fsAssert("Expected one element in enumeration",fss.length == 1 && fss[0] == defFs);

        repo.addFileSystem(testedFS);
        fss = repo.toArray();        
        fsAssert("Expected two elements in enumeration",fss.length == 2 && fss[1] == testedFS);        
        
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] {testedFS});
        repo.addFileSystem(mfs);        
        fss = repo.toArray();        
        fsAssert("Expected two elements in enumeration",fss.length == 3 && fss[2] == mfs);        
    }
    
    /** Test of findFileSystem method, of class org.openide.filesystems.Repository. */
    public void testFindFileSystem() {
        fsAssert("Default file system expected to in Repository",
        repo.findFileSystem(defFs.getSystemName()) != null);
        
    }
        
    /** Test of find method, of class org.openide.filesystems.Repository. * /
    public void testFind() {
        repo.addFileSystem(testedFS);
        fsAssert("Method find should find resource: " + resource1,repo.find(pkg.replace('/','.'), name, ext) != null);
    }
    */
    
    /** Test of findResource method, of class org.openide.filesystems.Repository. * /
    public void testFindResource() {
        repo.addFileSystem(testedFS);
        fsAssert("Method find should find resource: " + resource1,repo.findResource(resource1) != null);
    }
    */
    
    /** Test of findAllResources method, of class org.openide.filesystems.Repository. * /
    public void testFindAllResources() {
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] {testedFS});
        repo.addFileSystem(testedFS);        
        repo.addFileSystem(mfs);        
        java.util.Enumeration en = repo.findAllResources(resource1);
        int count;
        for (count = 0; en.hasMoreElements();count++) {
          en.nextElement();  
        }        
        fsAssert("findAllResource should returned 2 elements",count == 2);
    }
    */
    
    /** Test of findAll method, of class org.openide.filesystems.Repository. * /
    public void testFindAll() {
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] {testedFS});
        repo.addFileSystem(testedFS);        
        repo.addFileSystem(mfs);        
        java.util.Enumeration en = repo.findAll(pkg.replace('/','.'),name,ext);
        int count;
        for (count = 0; en.hasMoreElements();count++) {
          en.nextElement();  
        }        
        fsAssert("findAllResource should returned 2 elements",count == 2);
    }
    */
    
    /** Test of addRepositoryListener method, of class org.openide.filesystems.Repository. */
    public void testAddRepositoryListener() {
        RepList  repList = new RepList ();
        repo.addRepositoryListener(repList);                
        repo.addFileSystem(testedFS);               
        fsAssert("Expected one event",repList.getAdded () == 1);
        fsAssert("Unexpected event",repList.getRemoved () == 0);        
        fsAssert("Unexpected event",repList.getReordered () == 0);                
    }
    
    /** Test of removeRepositoryListener method, of class org.openide.filesystems.Repository. */
    public void testRemoveRepositoryListener() {
        RepList  repList = new RepList ();
        repo.addRepositoryListener(repList);                
        repo.removeRepositoryListener(repList);                        
        repo.addFileSystem(testedFS);               
        fsAssert("Expected one event",repList.getAdded () == 0);
        fsAssert("Unexpected event",repList.getRemoved () == 0);        
        fsAssert("Unexpected event",repList.getReordered () == 0);        
    }
    
    
    public void testRepfileFolderCreated() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        repo.addFileSystem(fs);
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (repo);            
            root.createFolder("testtset");
            fileFolderCreatedAssert ("unexpecetd event count",1);
        }
    }
    
    public void testRepfileDataCreated() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        repo.addFileSystem(fs);        
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (repo);            
            FileObject newF = root.createData("testfile","txe");
            fileDataCreatedAssert ("unexpecetd event count",1);            
        }
        
    }

    public void testRepfileRenamed() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        repo.addFileSystem(fs);        
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (repo);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.rename(fLock,"obscure","uni");                               
            } finally {
                fLock.releaseLock();               
            }

            fileRenamedAssert("unexpecetd event count",1);                                    
        }
        
    }

    public void testRepfileDeleted() throws IOException {
        FileSystem fs = this.testedFS;
        FileObject root = fs.getRoot ();
        repo.addFileSystem(fs);        
        if (!fs.isReadOnly () && !root.isReadOnly()) {
            root.getChildren();
            registerDefaultListener (repo);            
            FileObject newF = root.createData("testfile","txe");
            FileLock fLock = newF.lock();            
            try {
                newF.delete(fLock);                               
            } finally {
                fLock.releaseLock();               
            }

            fileDeletedAssert("unexpecetd event count",1);                                    
        }
        
    }
    
    public class RepList implements RepositoryListener{
        int added = 0;
        int removed = 0;        
        int reordered = 0;        
        public void fileSystemAdded(RepositoryEvent ev) {
            added++;
        }
        public void fileSystemRemoved(RepositoryEvent ev) {
            removed++;
        }
        public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
            reordered++;
        }               
        
        int getAdded () {
            return added;
        }
        int getRemoved () {
            return removed;
        }
        int getReordered () {
            return reordered;
        }        
    }            
}
