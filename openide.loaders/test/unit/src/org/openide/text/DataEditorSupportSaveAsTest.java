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


package org.openide.text;


import java.io.IOException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.TestUtilHid;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Node.Cookie;


/**
 */
public class DataEditorSupportSaveAsTest extends NbTestCase {
    
    FileSystem fs;
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportSaveAsTest$Lkp");
    }
    
    public DataEditorSupportSaveAsTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem ();
        org.openide.filesystems.Repository.getDefault ().addFileSystem (fs);
        org.openide.filesystems.FileObject root = fs.getRoot ();
    }
    
    protected void tearDown () throws Exception {
        org.openide.filesystems.Repository.getDefault ().removeFileSystem (fs);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    public void testUnmodifiedDocumentSaveAs() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileUtil.createData(fs.getRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(fs.findResource("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
        
        MyEnv env = new MyEnv( obj );
        MyDataEditorSupport des = new MyDataEditorSupport( obj, env );
        
        FileObject newFO = FileUtil.createData(fs.getRoot(), "otherFolder/newFile.newExt");
        
        des.saveAs( newFO );
        
        DataObject newObj = DataObject.find(fs.findResource("otherFolder/newFile.newExt"));
        assertEquals( MyDataObject.class, newObj.getClass());
        MyDataObject myObj = (MyDataObject)newObj;
        
        assertEquals("the original document was closed", 1, des.closeCounter );
        assertEquals("we don't ask before closing the original document", 0, des.canCloseCounter );
        assertEquals("new document was opened", 1, myObj.openCookieCalls);
    }
    
    public void testModifiedDocumentSaveAs() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileUtil.createData(fs.getRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(fs.findResource("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
        
        obj.setModified( true );
        
        MyEnv env = new MyEnv( obj );
        MyDataEditorSupport des = new MyDataEditorSupport( obj, env );
        
        FileObject newFO = FileUtil.createData(fs.getRoot(), "otherFolder/newFile.newExt");
        
        des.saveAs( newFO );
        
        DataObject newObj = DataObject.find(fs.findResource("otherFolder/newFile.newExt"));
        assertEquals( MyDataObject.class, newObj.getClass());
        MyDataObject myObj = (MyDataObject)newObj;
        
        assertEquals("the original StyledDocument was rendered (no file copy)", 1, des.renderCounter);
        assertFalse("the original document is no longer modified", obj.isModified() );
        assertEquals("the original document was closed", 1, des.closeCounter );
        assertEquals("we don't ask before closing the original document", 0, des.canCloseCounter );
        assertEquals("new document was opened", 1, myObj.openCookieCalls);
    }
    
    public void testEnvAddsSaveAsImpl() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileUtil.createData(fs.getRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(fs.findResource("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
        
        MyEnv env = new MyEnv( obj );
        MyDataObject myObj = (MyDataObject)obj;
        
        assertNotNull("we have SaveAs support for default data objects with uni file loaders", myObj.getSaveAsImpl() );
    }
    
    public void testNoSaveAsImpl() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileUtil.createData(fs.getRoot(), "someFolder/x.prima");
        FileUtil.createData(fs.getRoot(), "someFolder/x.seconda");
        
        DataObject obj = DataObject.find(fs.findResource("someFolder/x.prima"));
        assertEquals( MyMultiFileDataObject.class, obj.getClass());
        assertEquals( "we need an object with MultiFileLoader", MyMultiFileLoader.class, obj.getLoader().getClass());
        
        MyEnv env = new MyEnv( obj );
        
        assertNull("there's no default SaveAs support for multi file loaders", obj.getLookup().lookup( SaveAsCapable.class ) );
    }
    
//    public void testEnvOutputStreamTakesLock() throws Exception {
//        DataEditorSupport.Env env = (DataEditorSupport.Env)support().desEnv();
//        assertNull(env.fileLock);
//        OutputStream stream = env.outputStream();
//        assertNotNull(stream);
//        stream.close();
//        assertNotNull(env.fileLock);
//        env.fileLock.releaseLock();
//    }
//    
//    public void testFileEncodingQuery () throws Exception {
//        DES des = support();
//        FileEncodingQueryImpl.getDefault().reset();
//        StyledDocument doc = des.openDocument();
//        assertEquals(des.getDataObject().getPrimaryFile(),FileEncodingQueryImpl.getDefault().getFile());
//        FileEncodingQueryImpl.getDefault().reset();
//        doc.insertString(doc.getLength(), " Added text.", null);
//        des.saveDocument();        
//        assertEquals(des.getDataObject().getPrimaryFile(),FileEncodingQueryImpl.getDefault().getFile());
//    }
    
    private static class MyDataEditorSupport extends DataEditorSupport {
        private int renderCounter = 0;
        public MyDataEditorSupport( DataObject obj, CloneableEditorSupport.Env env ) {
            super( obj, env );
        }
        
        private int canCloseCounter = 0;
        @Override
        protected boolean canClose() {
            canCloseCounter++;
            return super.canClose();
        }

        private int closeCounter = 0;
        @Override
        protected boolean close(boolean ask) {
            closeCounter++;
            return super.close(ask);
        }

        @Override
        public StyledDocument getDocument() {
            if( getDataObject() instanceof MyDataObject ) {
                return _getDocument();
            }
            return super.getDocument();
        }

        MyStyledDocument _myDocument;
        private StyledDocument _getDocument() {
            if( null == _myDocument ) {
                _myDocument = new MyStyledDocument();
            }
            return _myDocument;
        }
        private class MyStyledDocument extends DefaultStyledDocument {
            @Override
            public void render(Runnable arg0) {
                renderCounter++;
                super.render(arg0);
            }
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
        public Lkp () throws IOException {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) throws IOException {
            super (ic);
            
            ic.add (new Repository (TestUtilHid.createLocalFileSystem (Lkp.class.getName()+System.currentTimeMillis(), new String[0])));
            ic.add (new Pool ());
        }
        
    } // end of Lkp
    
    private static final class Pool extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders () {
            return org.openide.util.Enumerations.array(DataLoader.getLoader(MyLoader.class), 
                    DataLoader.getLoader(MyMultiFileLoader.class));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        
        public static MyLoader get () {
            return (MyLoader)MyLoader.findObject (MyLoader.class, true);
        }
        
        public MyLoader() {
            super(MyDataObject.class.getName ());
            getExtensions ().addExtension ("obj");
            getExtensions ().addExtension ("newExt");
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }
    }
    
    public static final class MyDataObject extends MultiDataObject  {
        private int openCookieCalls = 0;
        
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            if( type.equals( OpenCookie.class) ) {
                OpenCookie oc = new OpenCookie() {
                    public void open() {
                        openCookieCalls++;
                    }
                };
                return type.cast(oc);
            }
            return super.getCookie(type);
        }

        SaveAsCapable getSaveAsImpl() {
            return getCookieSet().getLookup().lookup( SaveAsCapable.class );
        }
    }

    private static class MyMultiFileLoader extends MultiFileLoader {
        public MyMultiFileLoader () {
            super(MyMultiFileDataObject.class);
        }
        
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyMultiFileDataObject( primaryFile, this );
        }
    
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
            }
            return null;
        }

        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry (obj, secondaryFile);
        }
    } // end of MyDL3

    private static class MyMultiFileDataObject extends MultiDataObject {
        public MyMultiFileDataObject( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
}
