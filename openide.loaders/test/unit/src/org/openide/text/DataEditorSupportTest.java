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


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;


import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.actions.*;
import org.openide.cookies.EditCookie;

import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.Lookup;


/**
 */
public class DataEditorSupportTest extends NbTestCase {
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date ();
    
    MyFileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static DataEditorSupportTest RUNNING;
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportTest$Lkp");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public DataEditorSupportTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
        RUNNING = this;
        
        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem ();
        org.openide.filesystems.Repository.getDefault ().addFileSystem (fs);
        org.openide.filesystems.FileObject root = fs.getRoot ();
        fileObject = new MyFileObject (org.openide.filesystems.FileUtil.createData (root, "my.obj"));
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
    public void testItCanBeGCedIssue57565 () throws Exception {
        DES sup = support ();
        assertFalse ("It is closed now", support ().isDocumentLoaded ());
        
        Lookup lkp = sup.getLookup ();
        obj = (DataObject)lkp.lookup (DataObject.class);
        assertNotNull ("DataObject found", obj);
        
        sup.openDocument ();
        assertTrue ("It is open now", support ().isDocumentLoaded ());
        
        assertTrue ("Closed ok", sup.close ());
        
        java.lang.ref.WeakReference refLkp = new java.lang.ref.WeakReference (lkp);
        lkp = null;
    
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (sup);
        sup = null;
        
        assertGC ("Can disappear", ref);
        assertGC ("And its lookup as well", refLkp);
        
        
        
    }
    
    public void testGetOpenedPanesWorksAfterDeserialization () throws Exception {
        doGetOpenedPanesWorksAfterDeserialization (-1);
    }
    public void testGetOpenedPanesWorksAfterDeserializationIfTheFileGetsBig () throws Exception {
        doGetOpenedPanesWorksAfterDeserialization (1024 * 1024 * 10);
    }
    
    public void test68015 () throws Exception {
        DES edSupport = support();
        edSupport.open();
        
        waitEQ();
        
        edSupport.desEnv().markModified();
        
        assertTrue(edSupport.messageName().indexOf('*') != -1);
        assertTrue(edSupport.messageHtmlName().indexOf('*') != -1);
    }
    
    private void doGetOpenedPanesWorksAfterDeserialization (int size) throws Exception {
        support().open ();
        
        waitEQ ();

        CloneableEditor ed = (CloneableEditor)support().getRef ().getAnyComponent ();
        
        JEditorPane[] panes = getPanes();
        assertNotNull (panes);
        assertEquals ("One is there", 1, panes.length);
        
        NbMarshalledObject obj = new NbMarshalledObject (ed);
        ed.close ();
        
        panes = getPanes();
        assertNull ("No panes anymore", panes);

        DataObject oldObj = DataObject.find (fileObject);
        oldObj.setValid (false);
        
        expectedSize = size;
        
        ed = (CloneableEditor)obj.get ();
        
        DataObject newObj = DataObject.find (fileObject);
        
        if (oldObj == newObj) {
            fail ("Object should not be the same, new one shall be created after marking the old invalid");
        }
        
        panes = getPanes ();
        assertNotNull ("One again", panes);
        assertEquals ("One is there again", 1, panes.length);
    }

    private JEditorPane[] getPanes() throws Exception {
        return Mutex.EVENT.readAccess(new Mutex.ExceptionAction<JEditorPane[]>() {
            public JEditorPane[] run() throws Exception {
                return support().getOpenedPanes ();
            }
        });
    }
    
    public void testEnvOutputStreamTakesLock() throws Exception {
        DataEditorSupport.Env env = (DataEditorSupport.Env)support().desEnv();
        assertNull(env.fileLock);
        OutputStream stream = env.outputStream();
        assertNotNull(stream);
        stream.close();
        assertNotNull(env.fileLock);
        env.fileLock.releaseLock();
    }
    
    public void testFileEncodingQuery () throws Exception {
        DES des = support();
        FileEncodingQueryImpl.getDefault().reset();
        StyledDocument doc = des.openDocument();
        assertEquals(des.getDataObject().getPrimaryFile(),FileEncodingQueryImpl.getDefault().getFile());
        FileEncodingQueryImpl.getDefault().reset();
        doc.insertString(doc.getLength(), " Added text.", null);
        des.saveDocument();        
        assertEquals(des.getDataObject().getPrimaryFile(),FileEncodingQueryImpl.getDefault().getFile());
    }
    
    /** File object that let us know what is happening and delegates to certain
     * instance variables of the test.
     */
    private static final class MyFileObject extends org.openide.filesystems.FileObject {
        private org.openide.filesystems.FileObject delegate;
        
        public MyFileObject (org.openide.filesystems.FileObject del) {
            delegate = del;
        }

        public java.io.OutputStream getOutputStream (FileLock lock) throws IOException {
            class ContentStream extends java.io.ByteArrayOutputStream {
                public void close () throws java.io.IOException {
                    super.close ();
                    RUNNING.content = new String (toByteArray ());
                }
            }

            return new ContentStream ();
        }

        public void delete (FileLock lock) throws IOException {
            delegate.delete (lock);
        }

        public void setImportant (boolean b) {
            delegate.setImportant (b);
        }

        public void addFileChangeListener (org.openide.filesystems.FileChangeListener fcl) {
            delegate.addFileChangeListener (fcl);
        }

        public void removeFileChangeListener (org.openide.filesystems.FileChangeListener fcl) {
            delegate.removeFileChangeListener (fcl);
        }

        public Object getAttribute (String attrName) {
            return delegate.getAttribute (attrName);
        }

        public FileObject createFolder (String name) throws IOException {
            throw new IOException ("Not supported");
        }

        public void rename (FileLock lock, String name, String ext) throws IOException {
            throw new IOException ("Not supported");
        }

        public void setAttribute (String attrName, Object value) throws IOException {
            delegate.setAttribute (attrName, value);
        }

        public String getName () {
            return delegate.getName ();
        }

        public java.io.InputStream getInputStream () throws java.io.FileNotFoundException {
            return new java.io.ByteArrayInputStream (RUNNING.content.getBytes ());
        }

        public FileSystem getFileSystem () throws FileStateInvalidException {
            return delegate.getFileSystem ();
        }

        public FileObject getFileObject (String name, String ext) {
            return null;
        }

        public String getExt () {
            return delegate.getExt ();
        }

        public FileObject[] getChildren () {
            return null;
        }

        public java.util.Enumeration getAttributes () {
            return delegate.getAttributes ();
        }

        public FileObject createData (String name, String ext) throws IOException {
            throw new IOException ("Not supported");
        }

        public FileObject getParent () {
            return delegate.getParent ();
        }

        public long getSize () {
            return RUNNING.expectedSize;
        }

        public boolean isData () {
            return true;
        }

        public boolean isFolder () {
            return false;
        }

        public boolean isReadOnly () {
            return false;
        }

        public boolean isRoot () {
            return false;
        }

        public boolean isValid () {
            return delegate.isValid ();
        }

        public java.util.Date lastModified () {
            return RUNNING.date;
        }

        public FileLock lock () throws IOException {
            return delegate.lock ();
        }
        
        public Object writeReplace () {
            return new Replace ();
        }
    }
    
    private static final class Replace extends Object implements java.io.Serializable {
        static final long serialVersionUID = 2L;
        
        public Object readResolve () {
            return RUNNING.fileObject;
        }
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
    
    private static final class FileEncodingQueryImpl extends FileEncodingQueryImplementation {
        
        private static FileEncodingQueryImpl instance;
        
        private FileObject file;
        
        private FileEncodingQueryImpl () {
            
        }
            
        public Charset getEncoding(FileObject file) {
            this.file = file;
            return Charset.defaultCharset();
        }
        
        public void reset () {
            this.file = null;
        }
        
        public FileObject getFile () {
            return this.file;
        }
        
        public synchronized static FileEncodingQueryImpl getDefault () {
            if (instance == null) {
                instance = new FileEncodingQueryImpl ();
            }
            return instance;
        }                
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup  {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            
            ic.add (new Pool ());
            ic.add (FileEncodingQueryImpl.getDefault());
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
        
        protected Node createNodeDelegate() {
            return new MyNode(this, Children.LEAF); 
        }
    }

    /* Node which always returns non-null getHtmlDisplayName */
    public static final class MyNode extends DataNode {
        
        public MyNode (DataObject obj, Children ch) {
            super(obj, ch);
        }
        
        public String getHtmlDisplayName() {
            return "<b>" + getDisplayName() + "</b>";
        }
    }
    
}
