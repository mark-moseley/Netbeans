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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author nn136682
 */
public class Util {
    static {
        //JEditorPane.registerEditorKitForContentType(SchemaDataLoader.MIME_TYPE, XMLKit.class.getName());
        registerXMLKit();
    }
    
    public static void registerXMLKit() {
        String[] path = new String[] { "Editors", "application", "x-schema+xml" };
        FileObject target = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            for (int i=0; i<path.length; i++) {
                FileObject f = target.getFileObject(path[i]);
                if (f == null) {
                    f = target.createFolder(path[i]);
                }
                target = f;
            }
            String name = "EditorKit.instance";
            if (target.getFileObject(name) == null) {
                FileObject f = target.createData(name);
                f.setAttribute("instanceClass", "org.netbeans.modules.xml.text.syntax.XMLKit");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }

    public static Document loadDocument(InputStream in) throws Exception {
//	Document sd = new PlainDocument();
        Document sd = new org.netbeans.editor.BaseDocument(
                org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
        return setDocumentContentTo(sd, in);
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

    public static void setDocumentContentTo(DocumentModel model, String resourcePath) throws Exception {
        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(), resourcePath);
    }
    
    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static void dumpToFile(Document doc, File f) throws Exception {
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
    
    public static Document loadDocument(File f) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return loadDocument(in);
    }
    
    public static SchemaComponent findComponent(Schema schema, String xpath) {
        return (new FindSchemaComponentFromDOM().findComponent(schema, xpath));
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

    public static Project createJavaTestProject() throws Exception {
        FileObject projectDir = FileUtil.toFileObject(getTempDir("TestJavaProject").getCanonicalFile());
        copyResource("resources/TestJavaProject/build.xml", projectDir);
        copyResource("resources/TestJavaProject/manifest.mf", projectDir);
        FileObject nbprojectDir = FileUtil.createFolder(projectDir, "nbproject");
        copyResource("resources/TestJavaProject/nbproject/build-impl.xml", nbprojectDir);
        copyResource("resources/TestJavaProject/nbproject/project.xml", nbprojectDir);
        copyResource("resources/TestJavaProject/nbproject/project.properties", nbprojectDir);
        return ProjectManager.getDefault().findProject(projectDir);
    }

    public static FileObject populateProject(Project project, String xsd, String path) throws Exception {
        FileObject srcDir = FileUtil.createFolder(project.getProjectDirectory(), path);
        return copyResource(xsd, srcDir);
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
            if(out != null)
                out.close();
            if(in != null)
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
    
}
