/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.modules.InstalledFileLocator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Create a sample web project by unzipping a template into some directory
 *
 * @author Martin Grebac, Tomas Zezula
 */
public class J2SESampleProjectGenerator {

    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project/1";   //NOI18N

    private J2SESampleProjectGenerator() {}


    public static FileObject createProjectFromTemplate(final FileObject template, File projectLocation, final String name) throws IOException {
        FileObject prjLoc = null;
        if (template.getExt().endsWith("zip")) {  //NOI18N
            unzip(template.getInputStream(), projectLocation);
            // update project.xml
            try {
                prjLoc = FileUtil.toFileObject(projectLocation);
                File projXml = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH));
                Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
                NodeList nlist = doc.getElementsByTagNameNS(PROJECT_CONFIGURATION_NAMESPACE, "name");       //NOI18N
                if (nlist != null) {
                    for (int i=0; i < nlist.getLength(); i++) {
                        Node n = nlist.item(i);
                        if (n.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element e = (Element)n;
                        
                        replaceText(e, name);
                    }
                    saveXml(doc, prjLoc, AntProjectHelper.PROJECT_XML_PATH);
                }
            } catch (Exception e) {
                throw new IOException(e.toString());
            }
            
//            //update private/project.properties
//            try {
//                File props = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
//                InputStream is = new FileInputStream(props);
//                EditableProperties ep = new EditableProperties();
//                ep.load(is);
//
//                // JSPC classpath
//                StringBuffer sb = new StringBuffer();
//                // Ant is needed in classpath if we are forking JspC into another process
//                sb.append(InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", null, false)); //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/servlet-api-2.4.jar", null, false)); //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jsp-api-2.0.jar", null, false));   //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-compiler-5.0.27.jar", null, false));    //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/jasper-runtime-5.0.27.jar", null, false));     //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/commons-el.jar", null, false));    //NOI18N
//                sb.append(":"); // NOI18N
//                sb.append(InstalledFileLocator.getDefault().locate("modules/autoload/ext/commons-logging-api.jar", null, false));   //NOI18N
//                ep.setProperty(JSPC_CLASSPATH, sb.toString());
//
//                OutputStream os = new FileOutputStream(props);
//                ep.store(os);
//
//            } catch (Exception e) {
//                throw new IOException(e.toString());
//            }
        
            prjLoc.refresh(false);
        }
        return prjLoc;
    }
    
    private static void unzip(InputStream source, File targetFolder) throws IOException {
        //installation
        ZipInputStream zip=new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                File f = new File(targetFolder, ent.getName());
                if (ent.isDirectory()) {
                    f.mkdirs();
                } else {
                    f.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(f);
                    try {
                        FileUtil.copy(zip, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     */
    private static void replaceText(Element parent, String name) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                text.setNodeValue(name);
                return;
            }
        }
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
}
