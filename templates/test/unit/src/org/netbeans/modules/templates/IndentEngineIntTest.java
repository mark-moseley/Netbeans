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
 * Software is Sun Microsystems, Inc. 
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.awt.Dialog;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.text.IndentEngine;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach
 */
public class IndentEngineIntTest extends NbTestCase {
    
    public IndentEngineIntTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    

    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        MockServices.setServices(DD.class, Pool.class, IEImpl.class);
        FileUtil.setMIMEType("txt", "text/jarda");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFromTemplateUsingFreemarker() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo = FileUtil.createData(root, "simpleObject.txt");
        OutputStream os = fo.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        fo.setAttribute("javax.script.ScriptEngine", "freemarker");
        
        
        DataObject obj = DataObject.find(fo);
        
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        
        Map<String,String> parameters = Collections.singletonMap("title", "Nazdar");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        
        assertEquals("Created in right place", folder, n.getFolder());
        assertEquals("Created with right name", "complex.txt", n.getName());
        
        String exp = ">lmth/<>1h/<radzaN>1h<>lmth<";
        assertEquals(exp, readFile(n.getPrimaryFile()));
        
    }
    
    private static String readFile(FileObject fo) throws IOException {
        byte[] arr = new byte[(int)fo.getSize()];
        int len = fo.getInputStream().read(arr);
        assertEquals("Fully read", arr.length, len);
        return new String(arr);
    }
    
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(final DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prima")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            fail("I do not want to be called");
            return null;
        }

        
        
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }

    public static final class IEImpl extends IndentEngine {
        
        
        public int indentLine(Document doc, int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int indentNewLine(Document doc, int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean acceptMimeType(String mime) {
            return "text/jarda".equals(mime); // NOI18N
        }

        public Writer createWriter(Document doc, int offset, final Writer writer) {
            class Rotate extends StringWriter {
                @Override
                public void close() throws IOException {
                    super.close();
                    
                    String s = toString();
                    StringBuilder sb = new StringBuilder(s.length());
                    for (int i = s.length() - 1; i >= 0; i--) {
                        sb.append(s.charAt(i));
                    }
                    
                    writer.write(sb.toString());
                    writer.close();
                }
            }
            
            assertNotNull("There is some document", doc);
            assertEquals("Its length is 0", 0, doc.getLength());
            assertEquals("Offset is 0", 0, offset);
            
            return new Rotate();
        }
}
}
