/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.loaders.*;
import org.openide.TopManager;
import org.openide.cookies.*;
import org.openide.util.*;

import java.beans.*;
import java.io.*;
import java.util.*;

import org.netbeans.junit.*;

/**
 * @author Jan Pokorsky
 */
public class SerialDataConvertorTest extends NbTestCase {
    /** folder to create instances in */
    private DataFolder folder;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public SerialDataConvertorTest(String name) {
        super (name);
    }
    
    public static void main (String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite (SerialDataConvertorTest.class));
    }
    
    /** Setups variables.
     */
    protected void setUp () throws Exception {
        lfs = Repository.getDefault().getDefaultFileSystem();
        org.openide.filesystems.FileUtil.createFolder(lfs.getRoot(), "BB/AAA");
        org.openide.filesystems.FileUtil.createFolder(lfs.getRoot(), "system/Services/lookupTest");
        org.openide.filesystems.FileUtil.createFolder(lfs.getRoot(), "testCreateInstance");
        
        
        String fsstruct [] = new String [] {
            "BB/AAA/",
            "system/Services/lookupTest/",
            "testCreateInstance/",
        };
        
        FileObject bb = lfs.findResource("/BB");
        FileObject bb_aaa = lfs.findResource("/BB/AAA");
        
        DataObject dest = DataObject.find(bb_aaa);
        
        assertTrue("Destination folder doesn't exist.", dest != null);
        assertTrue("Destination folder is not valid.", dest.isValid ());
        
        folder = DataFolder.findFolder (bb);
    }
    
    /** Checks whether the instance is the same.
     */
    public void testSame() throws Exception {

        Ser ser = new Ser ("1");
        
        InstanceDataObject i = InstanceDataObject.create (folder, null, ser, null);
        
        Object n = i.instanceCreate ();
        if (n != ser) {
            fail ("instanceCreate is not the same: " + ser + " != " + n);
        }
        
        i.delete ();
    }
    
    /** Test whether instances survive garbage collection.
     */
    public void testSameWithGC () throws Exception {
        Object ser = new java.awt.Button();
        
        FileObject prim = InstanceDataObject.create (folder, "MyName", ser, null).getPrimaryFile ();
        String name = prim.getName ();
        String ext = prim.getExt ();
        prim = null;

        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        
        FileObject fo = folder.getPrimaryFile ().getFileObject (name, ext);
        assertTrue ("MyName.settings not found", fo != null);
        
        DataObject obj = DataObject.find (fo);
        
        InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
        assertTrue ("Object: " + obj + " does not have instance cookie", ic != null);
        
        Object value = ic.instanceCreate ();
        if (value != ser) {
            fail ("Value is different than serialized: " + System.identityHashCode (ser) + " value: " + System.identityHashCode (value));
        }
        
        obj.delete ();
    }
    
    /** Tests the creation in atomic section.
     */
    public void testSameInAtomicSection () throws Exception {
        class Test extends FileChangeAdapter 
        implements FileSystem.AtomicAction {
            
            private java.awt.Button testSer = new java.awt.Button ();
            
            private FileObject data;
            private InstanceDataObject obj;
            
            public void run () throws IOException {
                folder.getPrimaryFile ().addFileChangeListener (this);
                data = folder.getPrimaryFile ().createData ("SomeData");
                
                
                obj = InstanceDataObject.create (folder, null, testSer, null);
            }
            
            public void doTest () throws Exception {
                Object now = obj.instanceCreate ();
                if (now != testSer) {
                    fail ("Different values. Original: " + testSer + " now: " + now);
                }
            }
            
            public void cleanUp () throws Exception {
                data.delete ();
                obj.delete ();
            }
            
            public void fileDataCreated (FileEvent ev) {
                try {
                    Thread.sleep (500);
                } catch (Exception ex) {
                }
            }
        }

        
        Test t = new Test ();
        try {
            folder.getPrimaryFile().getFileSystem ().runAtomicAction (t);

            t.doTest ();
        } finally {
            t.cleanUp ();
        }
    }

    /** Tests whether createFromTemplate works correctly.
    */
    public void testCreateFromTemplateForSettingsFile () throws Exception {
        Object ser = new java.awt.Button ();

        InstanceDataObject obj = InstanceDataObject.create (folder, "SomeName", ser, null);
        obj.setTemplate (true);

        DataObject newObj = obj.createFromTemplate(folder, "NewName");
        
        if (!newObj.getName().equals ("NewName")) {
            fail ("Wrong name of new data object: " + newObj.getName ());
        }

        InstanceCookie ic = (InstanceCookie)newObj.getCookie (InstanceCookie.class);
        
        if (ic == null) {
            fail ("No instance cookie for " + newObj);
        }

        if (ic.instanceCreate () != ser) {
            fail ("created instance is different than the original in template");
        }
        
        if (ic.instanceCreate () == obj.instanceCreate ()) {
            fail ("Instance of the new object is same as the current of the template");
        }
    }
    
    /** Test if the Lookup reflects IDO' cokie changes. */
    public void testLookupRefreshOfInstanceCookieChanges() throws Exception {
//        Object ser = new java.awt.Button ();
        Object ser = new java.beans.beancontext.BeanContextChildSupport();

        FileObject lookupFO = lfs.findResource("/system/Services/lookupTest");
        FileObject systemFO = lfs.findResource("/system");
        
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(systemFO));
        Lookup l = lookup.getLookup();
        DataFolder folderTest = DataFolder.findFolder(lookupFO);
        
        InstanceDataObject ido = InstanceDataObject.create (folderTest, "testLookupRefresh", ser, null);
        Lookup.Result res = l.lookup(new Lookup.Template(ser.getClass()));
        Collection col = res.allInstances ();
        InstanceCookie ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertEquals("IDO did not create new InstanceCookie", ser, ic.instanceCreate());
        
        Set origSet = new HashSet(Arrays.asList(new Object[] {ser}));
        assertEquals("wrong lookup result", origSet, new HashSet(col));
        
        assertTrue("Lookup is not finished and surprisingly returned a result", lookup.isFinished ());
        
        Object found = col.iterator().next();
        assertEquals("found wrong object instance", ser, found);
        
        // due to #14795 workaround
        Thread.sleep(1000);
        
        // external file change forcing IDO to create new InstanceCookie
        final FileObject fo = ido.getPrimaryFile();
        lfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileLock lock = null;
                try {
                    InputStream in = fo.getInputStream();
                    byte[] buf = new byte[(int)fo.getSize()];
                    in.read(buf);
                    in.close();

                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out.write(buf);
                    out.write(32);
                    out.flush();
                    out.close();
                    
                } finally {
                    if (lock != null) lock.releaseLock();
                }
            }
        });
        
        col = res.allInstances ();
        ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        origSet = new HashSet(Arrays.asList(new Object[] {ic.instanceCreate()}));
        
        assertEquals("wrong lookup result", origSet, new HashSet(col));
        
        found = col.iterator().next();
        assertTrue("IDO did not create new InstanceCookie", ser != ic.instanceCreate());
        assertTrue("Lookup did not refresh changed InstanceCookie", ser != found);
    }
    /*
    private void assertEquals(boolean b1, boolean b2) {
        assertEquals(new Boolean(b1), new Boolean(b2));
    }
    */
    /** Checks whether the instance is not saved multiple times.
     *
    public void testMultiSave () throws Exception {
        Ser ser1 = new Ser ("1");
        Ser ser2 = new Ser ("2");
        
        InstanceDataObject i = InstanceDataObject.create (folder, null, ser1, null);
        
        Thread.sleep (3000);
        
        InstanceDataObject j = InstanceDataObject.create (folder, null, ser2, null);
        Thread.sleep (3000);
        
        Object n = i.instanceCreate ();
        if (n != ser1) {
            fail ("instanceCreate is not the same: ");
        }
        i.instanceCreate ();
        j.instanceCreate ();
        j.instanceCreate ();
        
    } */
    
    public static final class Ser extends Object implements Externalizable {
        static final long serialVersionUID = -123456;
        public int deserialized;
        public int serialized;
        private String name;
        
        private int property;
        
        private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
        
        public Ser (String name) {
            this.name = name;
        }
        
        public synchronized void readExternal(java.io.ObjectInput objectInput) 
        throws java.io.IOException, java.lang.ClassNotFoundException {
//            System.err.println(name + " deserialized");
            deserialized++;
        }
        
        public synchronized void writeExternal(java.io.ObjectOutput objectOutput) 
        throws java.io.IOException {
//            System.err.println(name + " serialized");
            serialized++;
        }
        
        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            propertyChangeSupport.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
        
        public int getProperty() {
            return this.property;
        }
        
        public void setProperty(int property) {
            int oldProperty = this.property;
            this.property = property;
            propertyChangeSupport.firePropertyChange("property", new Integer(oldProperty), new Integer(property));
        }
        
    }
    
    /** Tests creating .settings file (<code>IDO.create</code>) using parameter
     * <code>create</code>
     */
    public void testCreateSettings() throws Exception {
        FileObject fo = lfs.findResource("/testCreateInstance");
        assertNotNull("missing folder /testCreateInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testCreateInstance", folder);
        
        // test non null filename
        String filename = "testCreateSettings";
        Object obj = new javax.swing.JButton();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        InstanceDataObject ido2 = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido2);
        assertEquals("InstanceDataObject.create(..., false) must reuse existing file: ",
            ido.getPrimaryFile(), ido2.getPrimaryFile());
        
        for (int i = 0; i < 3; i++) {
            ido2 = InstanceDataObject.create(folder, filename, obj, null, true);
            assertNotNull("InstanceDataObject.create cannot return null!", ido2);
            assertTrue("InstanceDataObject.create(..., true) must create new file: "
                + "step: " + i + ", "
                + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        }
        
        // test null filename
        ido = InstanceDataObject.create(folder, null, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        ido2 = InstanceDataObject.create(folder, null, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido2);
        // filename == null => always create new file (ignore create parameter) => backward compatibility
        assertTrue("InstanceDataObject.create(..., false) must create new file: "
            + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        
        for (int i = 0; i < 3; i++) {
            ido2 = InstanceDataObject.create(folder, null, obj, null, true);
            assertNotNull("InstanceDataObject.create cannot return null!", ido2);
            assertTrue("InstanceDataObject.create(..., true) must create new file: "
                + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        }
    }
    
    public void testDeleteSettings() throws Exception {
        FileObject root = lfs.getRoot();
        DataFolder folder = DataFolder.findFolder(root);
        
        String filename = "testDeleteSettings";
        javax.swing.JButton obj = new javax.swing.JButton();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        // test if file object does not remain locked when ido is deleted and
        // the storing is not rescheduled in consequence of the serialization 
        obj.setForeground(java.awt.Color.black);
        Thread.sleep(500);
        ido.delete();
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        Thread.sleep(3000);
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        
        filename = "testDeleteSettings2";
        Ser ser = new Ser("bla");
        ido = InstanceDataObject.create(folder, filename, ser, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        ser.setProperty(10);
        ido.delete();
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        Thread.sleep(3000);
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
    }
}
