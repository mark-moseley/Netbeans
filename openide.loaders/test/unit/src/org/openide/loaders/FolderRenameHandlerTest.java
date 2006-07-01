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

import junit.framework.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Jan Becicka
 */
public class FolderRenameHandlerTest extends TestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName()); // NOI18N
    }

    private FileObject fo;
    private Node n;
    private FolderRenameHandlerImpl frh = new FolderRenameHandlerImpl();
    
    public FolderRenameHandlerTest (String testName) {
        super (testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        fo = FileUtil.createFolder (root, "test");// NOI18N
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof DataFolder)) {
            fail ("It should be DataFolder: " + obj);// NOI18N
        }
        
        assertNotNull(obj);
        n = obj.getNodeDelegate();
        assertNotNull(n);
    }
    
    public void tearDown() throws Exception {
        super.tearDown(); 
        fo.delete();
    }

    public void testRenameHandlerNotCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{});
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled () throws Exception {
        ((Lkp) Lkp.getDefault()).register(new Object[]{frh});
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    public static class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[0]);
        }
        public void register(Object[] instances) {
            setLookups(new Lookup[] {Lookups.fixed(instances)});
        }
    }    
    private static final class FolderRenameHandlerImpl implements FolderRenameHandler {
        boolean called  = false;
        public void handleRename(DataFolder folder, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}
