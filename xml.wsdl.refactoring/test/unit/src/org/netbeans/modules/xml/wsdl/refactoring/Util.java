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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class Util {
    public static final String EMPTY_XSD = "resources/Empty.wsdl";
    
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }
    
    public static Document loadDocument(InputStream in) throws Exception {
	Document sd = new org.netbeans.editor.BaseDocument(
            org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    public static int count = 0;
    public static WSDLModel loadWSDLModel(String resourcePath) throws Exception {
        NamespaceLocation nl = NamespaceLocation.valueFromResourcePath(resourcePath);
        if (nl != null) {
            return TestCatalogModel.getDefault().getWSDLModel(nl);
        }
        String location = resourcePath.substring(resourcePath.lastIndexOf('/')+1);
        URI locationURI = new URI(location);
        TestCatalogModel.getDefault().addURI(locationURI, getResourceURI(resourcePath));
        return TestCatalogModel.getDefault().getWSDLModel(locationURI);
    }
    
    public static WSDLModel createEmptyWSDLModel() throws Exception {
        return loadWSDLModel(EMPTY_XSD);
    }
    
    /*public static WSDLModel loadWSDLModel(Document doc) throws Exception {
        return WSDLModelFactory.getDefault().getModel(doc);
    }*/
    
    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static void dumpToFile(Document doc, File f) throws Exception {
        if (! f.exists()) {
            f.createNewFile();
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static File dumpToTempFile(Document doc) throws Exception {
        File f = File.createTempFile("xsm", "xsd");
        dumpToFile(doc, f);
        return f;
    }
    
    public static WSDLModel dumpAndReloadModel(Document doc) throws Exception {
        File f = dumpToTempFile(doc);
        URI dumpURI = new URI("dummyDump" + count++);
        TestCatalogModel.getDefault().addURI(dumpURI, f.toURI());
        return TestCatalogModel.getDefault().getWSDLModel(dumpURI);
    }
    
    public static Document loadDocument(File f) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return loadDocument(in);
    }
    
    public static URI getResourceURI(String path) throws RuntimeException {
        try {
            return Util.class.getResource(path).toURI();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static File getTempDir(String path) throws Exception {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), path);
        tempdir.mkdirs();
        return tempdir;
    }

    public static GlobalSimpleType getPrimitiveType(String typeName){
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for(GlobalSimpleType ptype: primitives){
            if(ptype.getName().equals(typeName)){
                return ptype;
            }
        }
        return null;
    }

    public static Document setDocumentContentTo(Document doc, InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        doc.remove(0, doc.getLength());
        doc.insertString(0,sbuf.toString(),null);
        return doc;
    }
    
    public static Document setDocumentContentTo(Document doc, String resourcePath) throws Exception {
        return setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
    }

    public static void setDocumentContentTo(WSDLModel model, String resourcePath) throws Exception {
        Document doc = ((AbstractDocumentModel)model).getBaseDocument();
        setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
    }

    public static FileObject copyResource(String path, FileObject destFolder) throws Exception {
        String filename = getFileName(path);
        
        FileObject dest = destFolder.getFileObject(filename);
        if (dest == null) {
            dest = destFolder.createData(filename);
        }
        FileLock lock = dest.lock();
        OutputStream out = dest.getOutputStream(lock);
        InputStream in = Util.class.getResourceAsStream(path); 
        try {
            FileUtil.copy(in, out);
        } finally {
            out.close();
            in.close();
            if (lock != null) lock.releaseLock();
        }
        return dest;
    }
    
    public static String getFileName(String path) {
        int i = path.lastIndexOf('/');
        if (i > -1) {
            return path.substring(i+1);
        } else {
            return path;
        }
    }
   
    public static ReferenceableSchemaComponent findGlobalComponentByName(Schema schema, String name) {
        for (ReferenceableSchemaComponent t : schema.getChildren(ReferenceableSchemaComponent.class)) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static WSDLComponent findByXpath(WSDLModel model, String xpath) {
        return findByXpath(model.getDefinitions(), xpath);
    }

    public static WSDLComponent findByXpath(Definitions root, String xpath) {
        return (WSDLComponent) new FindWSDLComponent().findComponent(root, xpath);
    }

    public static <T extends Object> List<T> toList(Collection<T> c) {
        return new ArrayList<T>(c);
    } 
    
}
