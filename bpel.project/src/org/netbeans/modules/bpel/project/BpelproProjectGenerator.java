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

package org.netbeans.modules.bpel.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.bpel.project.ui.customizer.IcanproProjectProperties;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class BpelproProjectGenerator {

    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N

    private BpelproProjectGenerator() {}

    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject(fo, name, j2eeLevel);
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
// Bing bpelasa        FileObject bpelasaRoot = srcRoot.createFolder(DEFAULT_BPELASA_FOLDER); //NOI18N
        FileObject bpelasaRoot = srcRoot;

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.META_INF, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
// Bing bpelasa       ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_BPELASA_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}"); //NOI18N
        ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);
        return h;
    }

    public static AntProjectHelper importProject(File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject configFilesBase, String j2eeLevel, String buildfile) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        // XXX add code to set meta inf directory  (meta-inf and java src)
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        AntProjectHelper h = setupProject(fo, name, j2eeLevel);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
            ep.put(IcanproProjectProperties.SOURCE_ROOT, "."); //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, relativePath(fo, javaRoot)); //NOI18N
            ep.setProperty(IcanproProjectProperties.META_INF, relativePath(fo, configFilesBase)); //NOI18N
        } else {
            File wmRoot = FileUtil.toFile(wmFO);
            ep.put(IcanproProjectProperties.SOURCE_ROOT, wmRoot.getAbsolutePath());
            String configFilesPath = relativePath(wmFO, configFilesBase);
            configFilesPath = configFilesPath.length() > 0 ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + configFilesPath : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            String javaPath = relativePath(wmFO, javaRoot);
            javaPath = javaPath.length() > 0 ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + javaPath : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, javaPath);
            ep.setProperty(IcanproProjectProperties.META_INF, configFilesPath);
        }
        if (! GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(IcanproProjectProperties.BUILD_FILE, buildfile);
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }

    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent))
            return "";
        if (!FileUtil.isParentOf(parent, child))
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath().substring(parent.getPath().length() + 1);
    }

    private static AntProjectHelper setupProject(FileObject dirFO, String name, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, BpelproProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // ep.setProperty(IcanproProjectProperties.JAVAC_CLASSPATH, "${libs.j2ee14.classpath}");
        ep.setProperty(IcanproProjectProperties.DIST_DIR, "dist");
        ep.setProperty(IcanproProjectProperties.DIST_JAR, "${"+IcanproProjectProperties.DIST_DIR+"}/" + name + ".zip");
        ep.setProperty(IcanproProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(IcanproProjectProperties.JAR_NAME, name + ".jar");
        ep.setProperty(IcanproProjectProperties.JAR_COMPRESS, "false");
//        ep.setProperty(IcanproProjectProperties.JAR_CONTENT_ADDITIONAL, "");

        Deployment deployment = Deployment.getDefault();
        String serverInstanceID = deployment.getDefaultServerInstanceID();
        ep.setProperty(IcanproProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID));
        ep.setProperty(IcanproProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEPRECATION, "false");
        ep.setProperty(IcanproProjectProperties.VALIDATION_FLAG, "false");
        ep.setProperty(IcanproProjectProperties.JAVAC_TARGET, "1.4");

        ep.setProperty(IcanproProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR, "${"+IcanproProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH, "${"+IcanproProjectProperties.JAVAC_CLASSPATH+"}:${"+IcanproProjectProperties.BUILD_CLASSES_DIR+"}");

        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX, "sun-bpel-engine"); // NOI18N     //FIXME? REPACKAGING
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_ALIAS, "This Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, "Represents this Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_ALIAS, "This Application Sub-Assembly"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION, "This represents the Application Sub-Assembly"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N

        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "BCDeployment.jar");
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "SEDeployment.jar");
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(IcanproProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_FILE, "ComponentInformation.xml"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_FILE, "default.xml"); // NOI18N
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }

    private static void createBPELFile(String bpelName, FileObject folderFO, String projectName) throws IOException {
        if (bpelName == null) {
            return;
        }

        bpelName = bpelName.trim();

        if (bpelName.length() == 0) {
            return;
        }

        if (bpelName.endsWith(".bpel")) { // NOI18N
            bpelName = bpelName.substring(0, bpelName.length() - 5);
        }

        FileObject templateFO =
                Repository.getDefault().getDefaultFileSystem().findResource("Templates/SOA/Process.bpel" ); // NOI18N

        if (templateFO == null) {
            return; // Don't know the template
        }

        DataObject templateDO = DataObject.find(templateFO);
        DataFolder folderDO = DataFolder.findFolder(folderFO);
        DataObject newBpelDO = templateDO.createFromTemplate(folderDO, bpelName);

        FileObject newFO = newBpelDO.getPrimaryFile();
        String namespace;
        namespace = "http://enterprise.netbeans.org/bpel";
        namespace += "/" + projectName;
        namespace += "/" + bpelName;
        initialiseNames(newFO, bpelName, namespace, projectName);  // NOI18N
    }

    /**
     *   Basically acts like a xslt tranformer by
     *   replacing _PROCNAME_ in fileObject contents with 'name'.
     *   replaceing _NS_ in fileObject contents with 'namespace'
     */
    private static void initialiseNames(FileObject fileObject, String name, String namespace, String projectName) {
        String line;
        StringBuffer buffer = new StringBuffer();

        try {
            InputStream inputStream = fileObject.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while((line = reader.readLine()) != null) {
                line = line.replace("_PROCNAME_", name);
                line = line.replace("_NS_", namespace);
                line = line.replace("_PROJNAME_", projectName);
                buffer.append(line);
                buffer.append("\n");
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

    private static void createWSDLFile(String wsdlName, FileObject folderFO, String projectName) throws IOException {
        if (wsdlName == null) {
            return;
        }

        wsdlName = wsdlName.trim();

        if (wsdlName.length() == 0) {
            return;
        }

//        if (wsdlName.endsWith(".wsdl")) { // NOI18N
//            wsdlName = wsdlName.substring(0, wsdlName.length() - 5);
//        }

        FileObject templateFO =
//                Repository.getDefault().getDefaultFileSystem().findResource("Templates/XML/untitled.wsdl" ); // NOI18N
                Repository.getDefault().getDefaultFileSystem().findResource("BPEL/BPELEmptyWSDL" ); // NOI18N

        if (templateFO == null) {
            return; // Don't know the template
        }

        DataObject templateDO = DataObject.find(templateFO);
        DataFolder folderDO = DataFolder.findFolder(folderFO);
        DataObject newWsdlDO = templateDO.createFromTemplate(folderDO, wsdlName + ".wsdl");

        FileObject newFO = newWsdlDO.getPrimaryFile();
        if (wsdlName.endsWith(".wsdl")) { // NOI18N
            wsdlName = wsdlName.substring(0, wsdlName.length() - 5);
        }
        String namespace;
        namespace = "http://enterprise.netbeans.org/bpel"; // NOI18N
        namespace += "/" + projectName; // NOI18N
        namespace += "/" + wsdlName; // NOI18N
        initialiseNames(newFO, wsdlName, namespace, projectName); // NOI18N
    }
}
