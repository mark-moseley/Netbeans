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

package org.openide.loaders;

import org.openide.filesystems.*;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.netbeans.junit.*;

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
    
    protected void setUp () throws Exception {
        clearWorkDir ();
    }
    
    public void testPossibleToCallFindDataObjectDirectly () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource ("AA");
        DataObject df = DataLoaderPool.getFolderLoader().findDataObject(fo, new DataLoader.RecognizedFiles () {  
            public void markRecognized (FileObject fo) {
            }
        });
        
        assertEquals ("Found the right one", fo, df.getPrimaryFile());
    }
    
    /** Tests whether children are updated immediatelly.
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

        //System.out.println("dfA " + dfA.getPrimaryFile ().getAttribute (DataFolder.EA_ORDER));
        //System.out.println("dfB " + dfB.getPrimaryFile ().getAttribute (DataFolder.EA_ORDER));
        
        MFS mfs = new MFS (new FileSystem [] { lfsA, lfsB });
        DataFolder df = DataFolder.findFolder (mfs.findResource ("AA"));
        
        arr = df.getChildren ();
        //System.out.println("df " + df.getPrimaryFile ().getAttribute (DataFolder.EA_ORDER));
        
        OrderListener l = new OrderListener();
        df.addPropertyChangeListener(l);
        
        // change layers -> change attributes
        mfs.set ( new FileSystem [] { lfsB, lfsA });
        
        assertTrue(l.gotSomething());
        //System.out.println("df " + df.getPrimaryFile ().getAttribute (DataFolder.EA_ORDER));
        assertChildrenArrays ("", arr, df.getChildren (), false);
    }
    
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
