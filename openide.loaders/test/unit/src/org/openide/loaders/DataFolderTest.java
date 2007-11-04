/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.loaders;

import java.util.Set;
import org.openide.filesystems.*;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import junit.framework.Test;

import org.netbeans.junit.*;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.test.MockPropertyChangeListener;

/** Test recognition of objects in folders, and folder ordering.
 *
 * @author  Vita Stejskal, Jesse Glick
 */
public class DataFolderTest extends LoggingTestCaseHid {
    private ArrayList hold = new ArrayList();

    /** Creates new DataFolderTest */
    public DataFolderTest (String name) {
        super (name);
    }
    
    public static Test suite() {
        return new NbTestSuite(DataFolderTest.class);
        //return new DataFolderTest("testMoveFolderWithReadOnlyFile");
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
    }
    
    public void testPossibleToCallFindDataObjectDirectly () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource ("AA");
        DataFolder df = (DataFolder)DataLoaderPool.getFolderLoader().findDataObject(fo, new DataLoader.RecognizedFiles () {  
            public void markRecognized (FileObject fo) {
            }
        });
        assertEquals ("Found the right one", fo, df.getPrimaryFile());
        
        CharSequence log = Log.enable("org.openide.loaders", Level.WARNING);
        Object index = df.getLookup().lookup(Index.class);
        assertEquals("Check folder", "", log.toString());
        assertNull("No index on DataFolder itself", index);
        
        
        DataObject obj = DataObject.find(FileUtil.createData(fo, "x.txt"));
        obj.getLookup();
        assertEquals("Check default", "", log.toString());
        
        DataObject shadow = obj.createShadow(df);
        shadow.getLookup();
        assertEquals("Check shadow", "", log.toString());
        
    }

    public void testMoveFolderWithReadOnlyFile () throws Exception {
        String fsstruct [] = new String [] {
            "AA/A.txt",
            "AA/B.txt",
            "AA/C.txt",
            "AA/D.txt",
            "target/",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource ("AA");
        DataFolder folder = DataFolder.findFolder(fo);
        
        FileObject c = lfs.findResource("AA/C.txt");
        File cFile = FileUtil.toFile(c);
        assertNotNull(cFile);
        
        cFile.setReadOnly();
        
        assertFalse("Read only", c.canWrite());
        
        
        DataFolder target = DataFolder.findFolder(lfs.findResource("target"));

        CharSequence log = Log.enable("org.openide.loaders.DataFolder", Level.INFO);
        folder.move(target);
//        if (log.toString().indexOf("C.txt") == -1) {
//            fail("There should be warning about C.txt:\n" + log);
//        }
        FileObject newFO = lfs.findResource("target/AA");
        assertNotNull("New folder created", newFO);
        DataFolder newFolder = DataFolder.findFolder(newFO);
        assertEquals("New folder has three DO", 3, newFolder.getChildren().length);
        
        
        assertEquals("Folder keeps pointing to old file", "AA", folder.getPrimaryFile().getPath());
        assertEquals("It has one DO", 1, folder.getChildren().length);
        
        DataFolder original = DataFolder.findFolder(lfs.findResource("AA"));
        assertSame("Old folder retains identity", original, folder);
        
        if (newFolder == folder) {
            fail("newFolder should be created");
        }
        
        Node[] oldNodes = folder.getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One node remains", 1, oldNodes.length);
        
        
        Node[] newNodes = newFolder.getNodeDelegate().getChildren().getNodes(true);
        assertEquals("Three nodes created", 3, newNodes.length);
    }
    
    /** Tests whether children are updated immediately.
     */
    public void testChildren () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));
        
        if (df.getChildren ().length != 0) {
            fail ("Children are not empty");
        }
        
        FileObject fo = df.getPrimaryFile ().createData ("X.instance");
        
        DataObject[] arr = df.getChildren ();
        if (arr.length != 1) {
            fail ("Children does not contain one element but " + arr.length);
        }
        
        if (!fo.equals (arr[0].getPrimaryFile ())) {
            fail ("Primary file of only element is diffent");
        }
        
        fo.delete ();
        
        if (arr[0].isValid ()) {
            fail ("The element is still valid even fileobject has been deleted");
        }
        
        arr = df.getChildren ();
        if (arr.length != 0) {
            fail ("Still there is something in children - length is " + arr.length);
        }
    }
    
    /** Tests whether children are gced if not needed. This test
     * uses getNodeDelegate to obtain the children - if we use
     * regular DataFolder.getChildren issue #30153 did not occur.
     */
    public void testChildrenAreGCed () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
            "AA/a.txt",
            "AA/b.txt"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));
        java.lang.ref.WeakReference wr[] = new java.lang.ref.WeakReference[2];
        
        hold.add(df);
        hold.add(lfs);
        
        
        org.openide.nodes.Node [] na = df.getNodeDelegate().getChildren().getNodes(true);
        wr[0] = new java.lang.ref.WeakReference(na[0].getCookie(DataObject.class));
        wr[1] = new java.lang.ref.WeakReference(na[1].getCookie(DataObject.class));
        na = null;
        assertGC("First object can go away", wr[0]);
        assertGC("Second object can go away", wr[1]);
    }
    
    /** Tests whether children are updated immediatelly.
     */
    public void testAddToChildren () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));

        int expected = 0;
        int count = 5;
        while (count-- > 0) {
        
            int len = df.getChildren ().length;
            if (len != expected) {
                fail ("Children are not of size: " + expected + " but " + len);
            }
        
            FileObject fo = df.getPrimaryFile ().createData ("X" + expected + ".instance");
        
            DataObject[] arr = df.getChildren ();
            
            expected++;
            
            if (arr.length != expected) {
                fail ("Children does not contain " + expected + "element(s) but " + arr.length);
            }
        
            DataObject last = arr[expected - 1];
            FileObject prim = last.getPrimaryFile ();
            if (!fo.equals (prim)) {
                fail ("Primary file of " + last + " is diffent than " + fo);
            }
        }
    }
    
    /** Tests whether children are updated immediatelly.
     */
    public void testOrderInAtomicAction () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));

        int expected = 0;
        int count = 5;
        while (count-- > 0) {
            class Code implements FileSystem.AtomicAction {
                private DataFolder folder;
                private int cnt;
                
                private DataObject[] origArr;
                private DataObject[] desireArr;
                
                
                public Code (DataFolder folder, int cnt) {
                    this.folder = folder;
                    this.cnt = cnt;
                }
                
                public void init () {
                    origArr = folder.getChildren ();
                    
                    if (origArr.length != cnt) {
                        fail ("Unexpected length " + cnt + " != " + Arrays.asList (origArr));
                    }
                }
                
                public void run () throws IOException {
                    DataObject obj = InstanceDataObject.create (folder, "X" + cnt, Object.class);
                    
                    // the children should still remain unchanged
                    DataObject[] currArr = folder.getChildren ();
                    assertChildrenArrays ("After create", origArr, currArr, true);
                    
                    ArrayList arr = new ArrayList (currArr.length + 1);
                    arr.add (obj);
                    arr.addAll (Arrays.asList (currArr));
                    desireArr = (DataObject[])arr.toArray (new DataObject[0]);
                    
                    folder.setOrder (desireArr);
                    
                    // should not be changed, still
                    DataObject[] afterArr = folder.getChildren ();
                    assertChildrenArrays ("End of atomic", afterArr, origArr, true);
                }
                    
                
                public void check () {
                    DataObject[] currArr = folder.getChildren ();
                    assertChildrenArrays ("After atomic", desireArr, currArr, true);
                }
                
            }
            
            Code code = new Code (df, expected++);
            code.init ();
            df.getPrimaryFile().getFileSystem ().runAtomicAction (code);
            code.check ();
        }
    }
    
    /** Testing order of folder. Needed in order to survive switching of
     * layers on system filesystem.
     */
    public void testOrderWhenAttributeIsChanged () throws Exception {
//        if (System.getProperty("netbeans.user") == null)
//            fail("Test can't run in this environment, use -Dxtest.mode=ide");
        
        String fsstruct [] = new String [] {
            "AA/AAA/",
            "AA/BBB/",
            "AA/CCC/"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));
        DataObject[] arr = df.getChildren ();

        String append = "";
        StringBuffer sb = new StringBuffer (255);
        for (int i = arr.length - 1; i >= 0; i--) {
            sb.append (append);
            sb.append (arr[i].getPrimaryFile ().getNameExt ());
            append = "/";
        }
        
        OrderListener l = new OrderListener();
        df.addPropertyChangeListener(l);
        
        // set order attribute
        df.getPrimaryFile ().setAttribute (DataFolder.EA_ORDER, sb.toString ());
        
        assertTrue(l.gotSomething());
        
        assertChildrenArrays ("", arr, df.getChildren (), false);
    }
    
    public void testRenameFolderDoesNotPrintWarning() throws Exception {
        String fsstruct [] = new String [] {
            "AA/AAA/",
            "AA/BBB/",
            "AA/CCC/"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));
        Set<FileObject> files = df.files();
        assertEquals("One", 1, files.size());
        assertEquals("The primary", df.getPrimaryFile(), files.iterator().next());

        CharSequence seq = Log.enable("org.openide.nodes", Level.WARNING);
        df.getNodeDelegate().setName("BBBB");
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {}
        });
        
        assertEquals("new name", "BBBB", df.getName());
        if (seq.length() > 0) {
            fail("No warnings:\n" + seq);
        }
    }
    
    private static final class OrderListener implements PropertyChangeListener {
        public int count = 0;
        public synchronized void propertyChange(PropertyChangeEvent ev) {
            if (DataFolder.PROP_CHILDREN.equals(ev.getPropertyName())) {
                count++;
                notifyAll();
            }
        }
        public synchronized boolean gotSomething() throws InterruptedException {
            if (count > 0) return true;
            wait(3000);
            return count > 0;
        }
    }
    
    public void testOrderWhenSet () throws Exception {
        String fsstruct [] = new String [] {
            "AA/AAA/",
            "AA/BBB/",
            "AA/CCC/"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        
        DataFolder df = DataFolder.findFolder (lfs.findResource ("AA"));
        DataObject[] arr = df.getChildren ();
        DataObject[] rev = new DataObject [arr.length];
        
        for (int i = 0; i < arr.length; i++) {
            rev [arr.length - 1 - i] = arr [i];
        }

        OrderListener l = new OrderListener();
        df.addPropertyChangeListener(l);
        
        // set new order
        df.setOrder (rev);
        
        assertTrue(l.gotSomething());
        
        assertChildrenArrays ("", arr, df.getChildren (), false);
    }

    /* Fails, apparently due to bug in MultiFileObject; see issue #106242
    public void testOrderWhenMultiFileSystemSetDelegatesIsCalled () throws Exception {
        String fsstruct [] = new String [] {
            "AA/AAA/",
            "AA/BBB/",
            "AA/CCC/"
        };

        FileSystem lfsA = TestUtilHid.createLocalFileSystem(new File(getWorkDir(), "A"), fsstruct);
        FileSystem lfsB = TestUtilHid.createLocalFileSystem(new File(getWorkDir(), "B"), fsstruct);
        
        DataFolder dfA = DataFolder.findFolder (lfsA.findResource ("AA"));
        DataFolder dfB = DataFolder.findFolder (lfsB.findResource ("AA"));

        DataObject[] arr = dfB.getChildren ();
        DataObject[] rev = new DataObject [arr.length];
        
        for (int i = 0; i < arr.length; i++) {
            rev [arr.length - 1 - i] = arr [i];
        }

        // set new order - force attr write
        dfA.setOrder (dfA.getChildren ());
        dfB.setOrder (rev);
        System.err.println("AAA #1 position=" + dfA.getPrimaryFile().getFileObject("AAA").getAttribute("position"));
        System.err.println("AAA #2 position=" + dfB.getPrimaryFile().getFileObject("AAA").getAttribute("position"));

        MFS mfs = new MFS (new FileSystem [] { lfsA, lfsB });
        DataFolder df = DataFolder.findFolder (mfs.findResource ("AA"));
        System.err.println("AAA merged position=" + df.getPrimaryFile().getFileObject("AAA").getAttribute("position"));
        
        arr = df.getChildren ();
        
        OrderListener l = new OrderListener();
        df.addPropertyChangeListener(l);
        
        // change layers -> change attributes
        mfs.set ( new FileSystem [] { lfsB, lfsA });
        System.err.println("AAA merged position=" + df.getPrimaryFile().getFileObject("AAA").getAttribute("position"));
        
        assertTrue(l.gotSomething());
        assertChildrenArrays ("", arr, df.getChildren (), false);
    }
     */
    
    // #13820:
    public void testOrderWhenFileRenamed() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/a1/",
            "folder/b2/",
            "folder/c3/",
        });
        DataFolder folder = DataFolder.findFolder(fs.findResource("folder"));
        assertEquals("initial order is alphabetical", "a1/b2/c3", childrenOrder(folder));
        OrderListener l = new OrderListener();
        folder.addPropertyChangeListener(l);
        DataObject.find(fs.findResource("folder/b2")).rename("d4");
        assertTrue("Renaming a file fires PROP_CHILDREN on folder", l.gotSomething());
        assertEquals("order after rename is still alphabetical", "a1/c3/d4", childrenOrder(folder));
    }
    
    public void testSortMode() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/a/",
            "folder/b.xml",
            "folder/c/",
            "folder/e.xml",
            "folder/d.instance",
        });
        assertTrue(fs.findResource("folder/a").isFolder());
        assertTrue(fs.findResource("folder/b.xml").isData());
        assertTrue(fs.findResource("folder/c").isFolder());
        assertTrue(fs.findResource("folder/e.xml").isData());
        assertTrue(fs.findResource("folder/d.instance").isData());
        DataFolder folder = DataFolder.findFolder(fs.findResource("folder"));
        assertEquals("initial order is alphabetical, folders first", "a/c/b.xml/d.instance/e.xml", childrenOrder(folder));
        folder.setSortMode(DataFolder.SortMode.NAMES);
        assertEquals("next order is alphabetical", "a/b.xml/c/d.instance/e.xml", childrenOrder(folder));
        folder.setSortMode(DataFolder.SortMode.CLASS);
        assertEquals("last order is by type", "d.instance/a/c/b.xml/e.xml", childrenOrder(folder));
    }

    public void testPositionalSort() throws Exception {
        FileObject dir = FileUtil.createMemoryFileSystem().getRoot();
        FileObject apex = dir.createData("apex");
        FileObject ball = dir.createFolder("ball");
        FileObject cone = dir.createData("cone");
        FileObject dent = dir.createData("dent");
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        DataFolder folder = DataFolder.findFolder(dir);
        assertEquals("dent/ball/apex/cone", childrenOrder(folder));
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        folder.addPropertyChangeListener(l);
        cone.setAttribute("position", 16);
        assertEquals("dent/ball/cone/apex", childrenOrder(folder));
        // Events are asynchronous unless getChildren forces them.
        l.assertEvents(DataFolder.PROP_CHILDREN);
    }

    /** Produce a string representation of the order of children
     * in a folder: primary filenames separated by slashes.
     * Useful for comparing against expected values.
     */
    private static String childrenOrder(DataFolder folder) {
        DataObject[] kids = folder.getChildren();
        StringBuffer buf = new StringBuffer(kids.length * 20);
        for (int i = 0; i < kids.length; i++) {
            if (i > 0) buf.append('/');
            buf.append(kids[i].getPrimaryFile().getNameExt());
        }
        return buf.toString();
    }
    
    private static class MFS extends MultiFileSystem {
        public MFS (FileSystem [] fs) {
            super (fs);
        }
        public void set (FileSystem [] fs) {
            setDelegates (fs);
        }
    }

    private void assertChildrenArrays (
        String msg, DataObject orig[], DataObject reverted[], boolean same
    ) {
        if (orig.length != reverted.length) {
            StringBuffer buf = new StringBuffer (500);
            buf.append (msg);
            buf.append (" different length!? ");
            buf.append (orig.length);
            buf.append (" != ");
            buf.append (reverted.length);
            buf.append ("\nOrig: "); 
            buf.append (Arrays.asList (orig));
            buf.append ("\nNew : ");
            buf.append (Arrays.asList (reverted));
            fail (buf.toString ());
        }
       
        for (int i = 0; i < orig.length; i++) {
            int indx = same ? i : orig.length - 1 - i;
            
            if (orig[i] != reverted [indx]) {
                StringBuffer buf = new StringBuffer (500);
                for (int j = 0; j < orig.length; j++) {
                    buf.append ("orig[" + j + "] = " + orig[j] + "\n");
                }
                for (int j = 0; j < orig.length; j++) {
                    buf.append ("reverted[" + j + "] = " + reverted[j] + "\n");
                }
        
                buf.insert (0, "Order of children is not " +
                    (same ? "preserved " : "reverted ")
                );
                fail (msg + " " + buf.toString ());
            }
        }
    }
    
    /** Test that DataFolder fires PROP_CHILDREN when a loader is added/removed
     * and this would cause some objects to be invalidated somehow.
     * Inspired by unexpected failure in FolderInstanceTest.testFolderInstanceNeverPassesInvObjects.
     */
    public void testPropChildrenFiredAfterInvalidation() throws Exception {
        String[] names = new String[10];
        for (int i = 0; i < names.length; i++) {
            names[i] = "folder/file" + i + ".simple";
        }
        TestUtilHid.destroyLocalFileSystem(getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        // Adding it to the repository is necessary for the test to work.
        // Otherwise PROP_CHILDREN is never fired, and getChildren sticks
        // to whatever it last had. #15572
        Repository.getDefault().addFileSystem(lfs);
        try {
            FileObject folder = lfs.findResource("folder");
            DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
            DataFolder f = DataFolder.findFolder(folder);
            f.getChildren();
            OrderListener ol = new OrderListener();
            f.addPropertyChangeListener(ol);
            assertEquals(0, ol.count);
            DataObject old0 = DataObject.find(lfs.findResource(names[0]));
            assertTrue(old0.isValid());
            assertEquals("org.openide.loaders.DefaultDataObject", old0.getClass().getName());
            //System.err.println("adding a loader");
            AddLoaderManuallyHid.addRemoveLoader(l, true);
            try {
                //System.err.println("added it");
                Thread.sleep(5000); // give it time to refresh
                //System.err.println("5 secs later");
                //System.err.println("loader pool: " + java.util.Arrays.asList(LDataLoaderPool.getDefault ()).toArray()));
                //System.err.println("our old object: " + old0);
                //System.err.println("loader recog says: " + DataObject.find(lfs.findResource(names[0])));
                //System.err.println("but on next one loader recog says: " + DataObject.find(lfs.findResource(names[1])));
                assertTrue("After adding a loader, the old object is invalid", ! old0.isValid());
                DataObject[] kids = f.getChildren();
                //System.err.println("kids=" + java.util.Arrays.asList(kids));
                assertEquals("Adding the loader refreshed a sample file after a while", "org.openide.loaders.DataLoaderOrigTest$SimpleDataObject", DataObject.find(lfs.findResource(names[0])).getClass().getName());
                assertEquals("After adding the loader, we have the correct number of objects", names.length, kids.length);
                assertEquals("getChildren gives us the new data objects", "org.openide.loaders.DataLoaderOrigTest$SimpleDataObject", kids[names.length - 1].getClass().getName());
                assertTrue("Adding a useful loader causes PROP_CHILDREN to be fired", ol.gotSomething());
                ol.count = 0;
            } finally {
                AddLoaderManuallyHid.addRemoveLoader(l, false);
            }
            Thread.sleep(1000);
            assertEquals("org.openide.loaders.DefaultDataObject", DataObject.find(lfs.findResource(names[0])).getClass().getName());
            assertTrue("Removing a loader also triggers PROP_CHILDREN", ol.gotSomething());
        } finally {
            Repository.getDefault().removeFileSystem(lfs);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
}
