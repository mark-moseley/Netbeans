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


package org.openide.text;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.actions.*;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.WindowManager;


/**
 */
public class PeterZMoveTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date ();

    FileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static PeterZMoveTest RUNNING;
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.PeterZMoveTest$Lkp");
    }
    
    public PeterZMoveTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
        RUNNING = this;
        
        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem ();
        org.openide.filesystems.Repository.getDefault ().addFileSystem (fs);
        org.openide.filesystems.FileObject root = fs.getRoot ();
        fileObject = org.openide.filesystems.FileUtil.createData (root, "my.obj");
    }
    
    protected void tearDown () throws Exception {
        waitEQ ();
        
        RUNNING = null;
        org.openide.filesystems.Repository.getDefault ().removeFileSystem (fs);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    private void waitEQ () throws Exception {
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () { } });
    }

    DES support () throws Exception {
        DataObject obj = DataObject.find (fileObject);
        
        assertEquals ("My object was created", MyDataObject.class, obj.getClass ());
        Object cookie = obj.getCookie (org.openide.cookies.OpenCookie.class);
        assertNotNull ("Our object has this cookie", cookie);
        assertEquals ("It is my cookie", DES.class, cookie.getClass ());
        
        return (DES)cookie;
    }
    
    /** holds the instance of the object so insane is able to find the reference */
    private DataObject obj;
    public void testWhenMovingAFileNoLockshallBetaken () throws Exception {
        DES sup = support ();
        assertFalse ("It is closed now", support ().isDocumentLoaded ());
        
        Lookup lkp = sup.getLookup ();
        obj = (DataObject)lkp.lookup (DataObject.class);
        assertNotNull ("DataObject found", obj);
        
        Document d = sup.openDocument ();
        assertTrue ("It is open now", support ().isDocumentLoaded ());

        d.insertString(0, "Ahoj", null);
        
        assertTrue("Really modified", sup.isModified());
        
        sup.saveDocument();
        
        FileObject fo = FileUtil.createFolder(obj.getFolder().getPrimaryFile(), "newfold");
        DataFolder nf = DataFolder.findFolder(fo);
        
        obj.move(nf);
        
        FileLock lock = obj.getPrimaryFile().lock();
        assertNotNull("It is possible to take another lock", lock);
    }
    
    /** Implementation of the DES */
    private static final class DES extends DataEditorSupport 
    implements OpenCookie, EditCookie {
        public DES (DataObject obj, Env env) {
            super (obj, env);
        }
        
        public org.openide.windows.CloneableTopComponent.Ref getRef () {
            return allEditors;
        }
        
    }
    
    /** MyEnv that uses DataEditorSupport.Env */
    private static final class MyEnv extends DataEditorSupport.Env {
        static final long serialVersionUID = 1L;
        
        public MyEnv (DataObject obj) {
            super (obj);
        }
        
        protected FileObject getFile () {
            return super.getDataObject ().getPrimaryFile ();
        }

        protected FileLock takeLock () throws IOException {
            return super.getDataObject ().getPrimaryFile ().lock ();
        }
        
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup  {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            
            ic.add (new Pool ());
        }
        
    } // end of Lkp
    
    private static final class Pool extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders () {
            return org.openide.util.Enumerations.singleton(MyLoader.get ());
        }
    }
    
    public static final class MyLoader extends org.openide.loaders.UniFileLoader {
        public int primary;
        
        public static MyLoader get () {
            return (MyLoader)MyLoader.findObject (MyLoader.class, true);
        }
        
        public MyLoader() {
            super(MyDataObject.class.getName ());
            getExtensions ().addExtension ("obj");
        }
        protected String displayName() {
            return "MyPart";
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            primary++;
            return new org.openide.loaders.FileEntry (obj, primaryFile);
        }
    }
    public static final class MyDataObject extends MultiDataObject 
    implements CookieSet.Factory {
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet ().add (OpenCookie.class, this);
        }

        public org.openide.nodes.Node.Cookie createCookie (Class klass) {
            return new DES (this, new MyEnv (this)); 
        }
        
        
        
    }
    
}
