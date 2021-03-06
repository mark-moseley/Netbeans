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


package org.netbeans.modules.identity.samples.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
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

public class SoaSampleUtils {
    
    public static String[] xlateFiles = {
            "build-impl.xml", // NOI18N
            "project.xml", // NOI18N
            "project.properties", // NOI18N
            "AssemblyInformation.xml" // NOI18N
    };

    public static void unZipFile(InputStream source, FileObject rootFolder) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                FileObject fo = FileUtil.createData(rootFolder, entry.getName());
                FileLock lock = fo.lock();
                try {
                    OutputStream out = fo.getOutputStream(lock);
                    try {
                        FileUtil.copy(str, out);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            source.close();
        }
    }

    public static void unZipFileTranslateProjectName(InputStream source, FileObject rootFolder, String name, String token) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String fname = entry.getName();
                FileObject fo = FileUtil.createData(rootFolder, fname);
                FileLock lock = fo.lock();
                try {
                    OutputStream out = fo.getOutputStream(lock);
                    try {
                        if (needTranslation(fname)) {
                            translateProjectName(str, out, name, token);
                        } else {
                            FileUtil.copy(str, out);
                        }
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            source.close();
        }
    }

    static boolean needTranslation(String fname) {
        for (int i = 0; i < xlateFiles.length; i++) {
            if (fname.endsWith(xlateFiles[i])) {
                return true;
            }
        }
        return false;
    }

    static void translateProjectName(InputStream str, OutputStream out, String name, String token) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        FileUtil.copy(str, bo);
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toString().replaceAll(token, name).getBytes());
        FileUtil.copy(bi, out);
    }

    public static FileObject getStudioUserDir() {
        Log.out("StudioUserDir: "+FileUtil.toFileObject(
                new File(System.getProperty("netbeans.user"))).getPath()); // NOI18N
        return FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); // NOI18N
    }
    
    public static FileObject getProjectFolder(FileObject parentDir, String projectDirName) {
        assert parentDir != null : parentDir+"/"+projectDirName +"doesn't exist"; // NOI18N
        assert projectDirName != null : "project name can't be empty" ; // NOI18N
        return parentDir.getFileObject(projectDirName);
    }
    
    private static FileObject getFolder(String relative) {
        return getFolder(getStudioUserDir(), relative);
    }
    
    private static FileObject getFolder(FileObject parent, String relative) {
        FileObject folder = parent.getFileObject(relative);
        if (folder != null) {
            return folder;
        }
        try {
            folder = parent.createFolder(relative);
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
        }
        return folder;
    }
    
    private static String getPropertiesPath(String name) {
        return SoaSampleProjectProperties.getDefault().isPrivateProperty(name) ? AntProjectHelper.PRIVATE_PROPERTIES_PATH : AntProjectHelper.PROJECT_PROPERTIES_PATH;
    }
    
    private static AntProjectHelper getAntProjectHelper(Project project) {
        return (AntProjectHelper) project.getLookup().lookup(AntProjectHelper.class);
    }
    
    public static void setPrivateProperty(FileObject prjLoc, String name, String value) {
        Properties properties = new Properties();
        try {
            properties.setProperty(name, value);

            FileObject propFile = FileUtil.createData(prjLoc, 
                        AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            FileLock lock = propFile.lock();
            OutputStream os = propFile.getOutputStream(lock);
            try {
                properties.store(os, null);
            } finally {
                os.close();
                lock.releaseLock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public static String getProperty(Project project, String name) {
        return getProperties(project, name).getProperty(name);
    }
    
    private static EditableProperties getProperties(Project project, String name) {
        AntProjectHelper helper = getAntProjectHelper(project);
        assert helper != null : "Can't get AntProjectHelper for project: " // NOI18N
                + project; 
        return helper.getProperties(getPropertiesPath(name));
    }
    
    /**
     * @return SunApp default server instance location
     */
    public static String getDefaultSunAppLocation() {
        String loc = null;
        String[] instances = InstanceProperties.getInstanceList();
        for (String instanceId : instances) {
            if (instanceId.indexOf(
                    SoaSampleProjectProperties.SERVER_INSTANCE_SUN_APPSERVER) != -1) {
                int endIdx = instanceId.indexOf(']');
                loc = instanceId.substring(1, endIdx);
                break;
            }
                
        }
        return loc;
    }
    
    /**
     *  Method taken from NB anagram code.
     */
    public static void setProjectName(FileObject prjLoc, String projTypeName, String name) {
        try {
            // update project.xml
            File projXml = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH));
            Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
            NodeList nlist = doc.getElementsByTagNameNS(projTypeName, "name");       //NOI18N
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
            ErrorManager.getDefault().notify(e);
        }
        
    }
    
    
    /**
     * Method taken from NB anagram game.
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
     * Method taken from NB anagram game.
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
    
    
    public static void insertParameters(FileObject dir, String bpelProjDir) {
        Enumeration files = dir.getData(true);
        
        while(files.hasMoreElements()) {
            FileObject fileObject = (FileObject) files.nextElement();
            
            if(fileObject.isFolder())
                continue;
            
            if(! ((fileObject.getExt().toLowerCase().equals("xml")  || // NOI18N
                    fileObject.getExt().toLowerCase().equals("properties")))) // NOI18N
                continue;
            
            String line;
            StringBuffer buffer = new StringBuffer();
            
            try {
                InputStream inputStream = fileObject.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                while((line = reader.readLine()) != null) {
                    line = line.replace("__BPELPROJECTNAME__", bpelProjDir); // NOI18N
                    buffer.append(line);
                    buffer.append("\n"); // NOI18N
                }
                
                File file = FileUtil.toFile(fileObject);
                OutputStream outputStream = new FileOutputStream(file);
                PrintWriter writer = new PrintWriter(outputStream);
                writer.write(buffer.toString());
                writer.flush();
                outputStream.close();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    
}
