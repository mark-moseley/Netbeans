/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import javax.swing.event.ChangeEvent;
import junit.framework.AssertionFailedError;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;

/* 
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed 
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
public class OperationListenerTest extends LoggingTestCaseHid
implements OperationListener {
    private ArrayList events = new ArrayList ();
    private FileSystem fs;
    private DataLoaderPool pool;
    private org.openide.ErrorManager err;
    
    /** Creates the test */
    public OperationListenerTest(String name) {
        super(name);
    }
    
    // For each test setup a FileSystem and DataObjects
    protected void setUp() throws Exception {
        registerIntoLookup(new Pool());
        
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        
        err = ErrManager.getDefault().getInstance("TEST-" + getName());
        
        pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (Pool.class, pool.getClass ());
        
        Pool.setExtra(null);
        
        err.log("setUp is over");
    }
    
    //Clear all stuff when the test finish
    protected void tearDown() throws Exception {
        err.log("entering tearDown");
        
        pool.removeOperationListener(this);
        
//        AddLoaderManuallyHid.addRemoveLoader (ALoader.getLoader (ALoader.class), false);
//        AddLoaderManuallyHid.addRemoveLoader (BLoader.getLoader (BLoader.class), false);
    }
    
    //
    // Tests
    //
    
    public void testRecognizeFolder () {
        pool.addOperationListener(this);
        DataFolder df = DataFolder.findFolder (fs.findResource ("fake"));
        DataObject[] arr = df.getChildren ();
        
        assertEquals ("One child", 1, arr.length);
        
        assertEvents ("Recognized well", new OperationEvent[] {
            new OperationEvent (df),
            new OperationEvent (arr[0])
        });
    }

    public void testCopyFile() throws Exception {
        err.log("Before add listener");
        pool.addOperationListener(this);
        err.log("after add listener");
        DataObject obj = DataObject.find (fs.findResource ("fake/A.instance"));
        err.log("object found: " + obj);
        DataFolder df = DataFolder.findFolder (fs.findResource ("dir"));
        err.log("folder found: " + df);
        DataObject n = obj.copy (df);
        err.log("copy done: " + n);
        assertEquals ("Copy successfull", n.getFolder(), df);
        
        err.log("Comparing events");
        assertEvents ("All well", new OperationEvent[] {
            new OperationEvent (obj),
            new OperationEvent (df),
            new OperationEvent (n),
            new OperationEvent.Copy (n, obj)
        });
    }
    
    public void testBrokenLoader () throws Exception {
        BrokenLoader loader = (BrokenLoader)DataLoader.getLoader(BrokenLoader.class);
        
        try {
            err.log("before setExtra: " + loader);
            Pool.setExtra(loader);
            
            err.log("before addOperationListener");
            pool.addOperationListener(this);
            
            loader.acceptableFO = fs.findResource ("source/A.attr");
            err.log("File object found: " + loader.acceptableFO);
            try {
                DataObject obj = DataObject.find (fs.findResource ("source/A.attr"));
                fail ("The broken loader throws exception and cannot be created");
            } catch (IOException ex) {
                // ok
                err.log("Exception thrown correctly:");
                err.notify(ex);
            }
            assertEquals ("Loader created an object", loader, loader.obj.getLoader());
            
            err.log("brefore waitFinished");
            // and the task can be finished
            loader.recognize.waitFinished ();
            
            err.log("waitFinished done");
            
            assertEvents ("One creation notified even if the object is broken", new OperationEvent[] {
                new OperationEvent (loader.obj),
            });
        } finally {
            Pool.setExtra(null);
        }
    }
    
    //
    // helper methods
    //
    
    private void assertEvents (String txt, OperationEvent[] expected) {
        boolean failure = false;
        if (expected.length != events.size ()) {
            failure = true;
        } else {
            for (int i = 0; i < expected.length; i++) {
                OperationEvent e = expected[i];
                OperationEvent r = (OperationEvent)events.get (i);
                if (e.getClass  () != r.getClass ()) {
                    failure = true;
                    break;
                }
                if (e.getObject () != r.getObject()) {
                    failure = true;
                    break;
                }
            }
        }
        
        
        if (failure) {
            StringBuffer sb = new StringBuffer ();
            
            int till = Math.max (expected.length, events.size ());
            sb.append ("Expected events: " + expected.length + " was: " + events.size () + "\n");
            for (int i = 0; i < till; i++) {
                sb.append ("  Expected: ");
                if (i < expected.length) {
                    sb.append (expected[i].getClass () + " source: " + expected[i].getObject ());
                }
                sb.append ('\n');
                sb.append ("  Was     : ");
                if (i < events.size ()) {
                    OperationEvent ev = (OperationEvent)events.get (i);
                    sb.append (ev.getClass () + " source: " + ev.getObject ());
                }
                sb.append ('\n');
            }
            
            fail (sb.toString ());
        }
        
        events.clear();
    }
    
    //
    // Listener implementation
    //
    
    public void operationCopy(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        err.log ("  operationCopy: " + ev);
    }
    
    public void operationCreateFromTemplate(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        err.log ("  operationCreateFromTemplate: " + ev);
    }
    
    public void operationCreateShadow(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        err.log ("  operationCreateShadow: " + ev);
    }
    
    public void operationDelete(OperationEvent ev) {
        events.add (ev);
        err.log ("  operationDelete: " + ev);
    }
    
    public void operationMove(org.openide.loaders.OperationEvent.Move ev) {
        events.add (ev);
        err.log ("  operationMove: " + ev);
    }
    
    public void operationPostCreate(OperationEvent ev) {
        events.add (ev);
        err.log ("  operationPostCreate: " + ev);
    }
    
    public void operationRename(org.openide.loaders.OperationEvent.Rename ev) {
        events.add (ev);
        err.log ("  operationRename: " + ev);
    }

    
    //
    // Own loader
    //
    public static final class BrokenLoader extends UniFileLoader {
        public FileObject acceptableFO;
        public RequestProcessor.Task recognize;
        public MultiDataObject obj;
        
        public BrokenLoader() {
            super(MultiDataObject.class.getName ());
        }
        protected String displayName() {
            return "BrokenLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (acceptableFO != null && acceptableFO.equals(fo)) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(final FileObject primaryFile) throws DataObjectExistsException, IOException {
            obj = new MultiDataObject (primaryFile, this);
            
            assertNull ("Only one invocation of this code allowed", recognize);
            
            class R implements Runnable {
                public DataObject found;
                public void run () {
                    synchronized (this) {
                        notify ();
                    }
                    // this basicly means another call to createMultiObject method
                    // of this loader again, but the new MultiDataObject will throw
                    // DataObjectExistsException and will block in its
                    // getDataObject method
                    try {
                        found = DataObject.find (primaryFile);
                    } catch (IOException ex) {
                        fail ("Unexepcted exception: " + ex);
                    }
                    
                    assertEquals ("DataObjects are the same", found, obj);
                }
            }
            R run = new R ();
            synchronized (run) {
                recognize = RequestProcessor.getDefault ().post (run);
                try {
                    run.wait ();
                } catch (InterruptedException ex) {
                    fail ("Unexepcted ex: " + ex);
                }
            }
                
            
            throw new IOException ("I am broken!");
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        
        public void run() {
        }
        
    }
    
    private static final class Pool extends DataLoaderPool {
        private static DataLoader extra;
        
        
        protected Enumeration loaders () {
            if (extra == null) {
                return Enumerations.empty ();
            } else {
                return Enumerations.singleton (extra);
            }
        }

        public static void setExtra(DataLoader aExtra) {
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault();
            p.fireChangeEvent(new ChangeEvent(p));
        }
    }
}
