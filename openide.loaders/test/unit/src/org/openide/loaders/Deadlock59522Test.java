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

package org.openide.loaders;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.RequestProcessor;

/** MDR listens on FileChanges and wants to acquire its lock then.
 * Also it does rename under holding its lock in other thread.
 *
 * @author Jaroslav Tulach
 */
public class Deadlock59522Test extends NbTestCase implements FileChangeListener {
    FileObject toolbars;
    FileSystem fs;
    DataFolder toolbarsFolder;
    DataFolder anotherFolder;
    DataObject obj;
    DataObject anotherObj;
    
    
    Exception assigned;
    boolean called;
    boolean ok;
    
    private Object BIG_MDR_LOCK = new Object();
    
    public Deadlock59522Test(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject root = fs.getRoot ();
        toolbars = FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        FileObject fo = FileUtil.createData (root, "Ahoj.txt");
        obj = DataObject.find (fo);
        fo = FileUtil.createFolder (root, "Another");
        anotherFolder = DataFolder.findFolder (fo);
        fo = FileUtil.createData (root, "Another.txt");
        anotherObj = DataObject.find (fo);
        
        fs.addFileChangeListener (this);
    }

    protected void tearDown() throws Exception {
        fs.removeFileChangeListener (this);
        
        assertTrue ("The doRenameAObjectWhileHoldingMDRLock must be called", called);
        
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        if (assigned != null) {
            throw assigned;
        }
    }
    private static int cnt = 0;
    private void startRename() throws Exception {
        synchronized (BIG_MDR_LOCK) {
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    synchronized (BIG_MDR_LOCK) {
                        try {
                            called = true;
                            BIG_MDR_LOCK.notify();
                            BIG_MDR_LOCK.wait(); // for notification
                            // in some thread try to rename some object while holding mdr lock
                            anotherObj.rename ("mynewname" + cnt++);
                            ok = true;
                        } catch (Exception ex) {
                            assigned = ex;
                        } finally {
                            // end this all
                            BIG_MDR_LOCK.notifyAll();
                        }
                    }
                }
            });
            BIG_MDR_LOCK.wait();
        }
    }
    
    private void lockMdr() {
        // no more callbacks
        fs.removeFileChangeListener(this);
        
        synchronized (BIG_MDR_LOCK) {
            BIG_MDR_LOCK.notify(); // notified from herer
            try {
                BIG_MDR_LOCK.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                fail ("No InterruptedExceptions");
            }
            assertTrue ("Rename finished ok", ok);
        }
    }
    

    public void testMove () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.move (anotherFolder);
        }
    }

    public void testCopy () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.copy (anotherFolder);
        }
    }
    
    public void testRename () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.rename ("NewName.txt");
        }
    }
    
    public void testCreateShadow () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createShadow (anotherFolder);
        }
    }
    
    public void testTemplate () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createFromTemplate (anotherFolder);
        }
    }

    public void testTemplate2 () throws Exception {
        synchronized (BIG_MDR_LOCK) {
            startRename();
            obj.createFromTemplate (anotherFolder, "AhojVole.txt");
        }
    }

    //
    // Listener triggers creation of the node
    //

    public void fileRenamed (FileRenameEvent fe) {
        lockMdr ();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }

    public void fileFolderCreated (FileEvent fe) {
        lockMdr ();
    }

    public void fileDeleted (FileEvent fe) {
        lockMdr ();
    }

    public void fileDataCreated (FileEvent fe) {
        lockMdr ();
    }

    public void fileChanged (FileEvent fe) {
        lockMdr ();
    }
    
}
