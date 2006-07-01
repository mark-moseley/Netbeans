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

import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** Simulates the deadlock between copy/move operation and the creation
 * of node.
 *
 * @author Jaroslav Tulach
 */
public class Deadlock51637Test extends NbTestCase implements FileChangeListener {
    FileObject toolbars;
    FileSystem fs;
    DataFolder toolbarsFolder;
    DataFolder anotherFolder;
    DataObject obj;
    
    Node node;
    Exception assigned;
    boolean called;
    
    public Deadlock51637Test(String testName) {
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
        
        fs.addFileChangeListener (this);
    }

    protected void tearDown() throws Exception {
        fs.removeFileChangeListener (this);
        
        assertTrue ("The doCreateNode must be called", called);
        
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
    }
    
    private void doCreateNode () {
        if (node != null) {
            assertNotNull ("Node is not null, but it was not assigned", assigned);
            
            AssertionFailedError a = new AssertionFailedError ("Node cannot be null");
            a.initCause (assigned);
            throw a;
        }
        // just one event is enough
        fs.removeFileChangeListener (this);
        
        called = true;
        
        boolean ok;
        try {
            final Exception now = new Exception ("Calling to rp");
            ok = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    node = obj.getNodeDelegate ();
                    
                    assigned = new Exception ("Created in RP");
                    assigned.initCause (now);
                }
            }).waitFinished (100000);
        } catch (InterruptedException ex) {
            AssertionFailedError a = new AssertionFailedError (ex.getMessage ());
            a.initCause (ex);
            throw a;
        }
        
        if (node == null) {
            fail ("Node is still null and the waitFinished was " + ok);
        }
    }
    

    public void testMove () throws Exception {
        obj.move (anotherFolder);
    }

    public void testCopy () throws Exception {
        obj.copy (anotherFolder);
    }
    
    public void testRename () throws Exception {
        obj.rename ("NewName.txt");
    }
    
    public void testCreateShadow () throws Exception {
        obj.createShadow (anotherFolder);
    }
    
    public void testTemplate () throws Exception {
        obj.createFromTemplate (anotherFolder);
    }

    public void testTemplate2 () throws Exception {
        obj.createFromTemplate (anotherFolder, "AhojVole.txt");
    }

    //
    // Listener triggers creation of the node
    //

    public void fileRenamed (FileRenameEvent fe) {
        doCreateNode ();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }

    public void fileFolderCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDeleted (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDataCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileChanged (FileEvent fe) {
        doCreateNode ();
    }
    
}
