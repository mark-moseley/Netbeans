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

package org.netbeans.modules.web.project.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.Repository;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.w3c.dom.NodeList;


/**
 * Create a fresh WebProject from scratch or by importing and exisitng web module
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class WebProjectUtilities {
    
    /**
     * BluePrints source structure
     */
    public static final String SRC_STRUCT_BLUEPRINTS = "BluePrints"; //NOI18N
    
    /**
     * Jakarta source structure
     */
    public static final String SRC_STRUCT_JAKARTA = "Jakarta"; //NOI18N
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_CONF_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    
    private static final String WEB_INF = "WEB-INF"; //NOI18N
    private static final String SOURCE_ROOT_REF = "${" + WebProjectProperties.SOURCE_ROOT + "}"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6.5";
    
    private static final Logger LOGGER = Logger.getLogger(WebProjectUtilities.class.getName());
    private static String RESOURCE_FOLDER = "org/netbeans/modules/web/project/ui/resources/"; //NOI18N
    private WebProjectUtilities() {}
    
    /**
     * Create a new empty web project.
     *
     * @deprecated Use {@link #createProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper createProject(File dir, String name, String serverInstanceID, String sourceStructure, String j2eeLevel, String contextPath)
            throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setServerInstanceID(serverInstanceID);
        createData.setSourceStructure(sourceStructure);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setContextPath(contextPath);
        return createProject(createData);
    }
    
    /**
     * Creates a new empty web project.
     * @param createData the object encapsulating necessary data to create the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final WebProjectCreateData createData) throws IOException {
        final AntProjectHelper[] h = new AntProjectHelper[1];
        File dir = createData.getProjectDir();
        assert dir != null: "Project folder can't be null"; //NOI18N
        final FileObject projectDir = FileUtil.createFolder(dir);
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = createProjectImpl(createData, projectDir);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private static AntProjectHelper createProjectImpl(final WebProjectCreateData createData, 
            final FileObject projectDir) throws IOException {
        String name = createData.getName();
        String serverInstanceID = createData.getServerInstanceID();
        String sourceStructure = createData.getSourceStructure();
        String j2eeLevel = createData.getJavaEEVersion();
        String contextPath = createData.getContextPath();
        String javaPlatformName = createData.getJavaPlatformName();
        String sourceLevel = createData.getSourceLevel();
        
        assert name != null: "Project name can't be null"; //NOI18N
        assert serverInstanceID != null: "Server instance ID can't be null"; //NOI18N
        assert sourceStructure != null: "Source structure can't be null"; //NOI18N
        assert j2eeLevel != null: "Java EE version can't be null"; //NOI18N
        
        final boolean createBluePrintsStruct = SRC_STRUCT_BLUEPRINTS.equals(sourceStructure);
        final boolean createJakartaStructure = SRC_STRUCT_JAKARTA.equals(sourceStructure);

        final String serverLibraryName = configureServerLibrary(createData.getLibrariesDefinition(),
                serverInstanceID, projectDir, createData.getServerLibraryName() != null);

        final AntProjectHelper h = setupProject(projectDir, name, serverInstanceID,
                j2eeLevel, createData.getLibrariesDefinition(), serverLibraryName);
        
        FileObject srcFO = projectDir.createFolder(DEFAULT_SRC_FOLDER);
        FileObject confFolderFO = null;
        
        if (createBluePrintsStruct) {
            srcFO.createFolder(DEFAULT_JAVA_FOLDER);
            confFolderFO = srcFO.createFolder(DEFAULT_CONF_FOLDER);
        }
        
        if(createJakartaStructure) {
            confFolderFO = projectDir.createFolder(DEFAULT_CONF_FOLDER);
        }
        
        //create default manifest
        if(confFolderFO != null) {
            String manifestText = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + "MANIFEST.MF")); //NOI18N
            FileObject manifest = FileUtil.createData(confFolderFO, "MANIFEST.MF"); //NOI18N
            FileLock lock = manifest.lock();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(manifest.getOutputStream(lock)));
            try {
                bw.write(manifestText);
            } finally {
                bw.close();
                lock.releaseLock();
            }
        }
        
        //test folder
        FileUtil.createFolder(projectDir, DEFAULT_TEST_FOLDER);
        
        FileObject webFO = projectDir.createFolder(DEFAULT_DOC_BASE_FOLDER);
        final FileObject webInfFO = webFO.createFolder(WEB_INF);
        // create web.xml
        // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
        String webXMLContent = null;
        if (J2eeModule.JAVA_EE_5.equals(j2eeLevel))
            webXMLContent = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + "web-2.5.xml")); //NOI18N
        else if (WebModule.J2EE_14_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + "web-2.4.xml")); //NOI18N
        else if (WebModule.J2EE_13_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + "web-2.3.xml")); //NOI18N
        assert webXMLContent != null : "Cannot find web.xml template for J2EE specification level:" + j2eeLevel;
        final String webXmlText = webXMLContent;
        FileObject webXML = FileUtil.createData(webInfFO, "web.xml");//NOI18N
        FileLock lock = webXML.lock();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(webXML.getOutputStream(lock)));
        try {
            bw.write(webXmlText);
        } finally {
            bw.close();
            lock.releaseLock();
        }
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        
        Element rootSrc = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootSrc.setAttribute("id",WebProjectProperties.SRC_DIR);   //NOI18N
        rootSrc.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
        sourceRoots.appendChild(rootSrc);
        if (createBluePrintsStruct)
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER + "/" + DEFAULT_JAVA_FOLDER); // NOI18N
        else
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER); // NOI18N
        
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        
        Element rootTest = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootTest.setAttribute("id",WebProjectProperties.TEST_SRC_DIR);   //NOI18N
        rootTest.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
        testRoots.appendChild(rootTest);
        ep.setProperty(WebProjectProperties.TEST_SRC_DIR, DEFAULT_TEST_FOLDER); // NOI18N
        
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        ep.put(WebProjectProperties.SOURCE_ROOT, createBluePrintsStruct ? DEFAULT_SRC_FOLDER : "."); //NOI18N
        
        ep.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, DEFAULT_DOC_BASE_FOLDER);
        if (createBluePrintsStruct) {
            ep.setProperty(WebProjectProperties.SRC_DIR, "${" + WebProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_JAVA_FOLDER);
            ep.setProperty(WebProjectProperties.CONF_DIR, "${" + WebProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_CONF_FOLDER);
        } else {
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER);
        }
        
        if(createJakartaStructure) {
            ep.setProperty(WebProjectProperties.CONF_DIR, DEFAULT_CONF_FOLDER);
        }
        // Default to conf.dir
        ep.setProperty(WebProjectProperties.PERSISTENCE_XML_DIR, "${"+WebProjectProperties.CONF_DIR+"}"); //NOI18N
        
        ep.setProperty(WebProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        ep.setProperty(WebProjectProperties.LIBRARIES_DIR, "${" + WebProjectProperties.WEB_DOCBASE_DIR + "}/" + WEB_INF + "/lib"); //NOI18N
        
        ep.setProperty(WebProjectProperties.WEBINF_DIR, DEFAULT_DOC_BASE_FOLDER + "/" + WEB_INF);
        
        WebProject p = (WebProject)ProjectManager.getDefault().findProject(h.getProjectDirectory());
        UpdateHelper updateHelper = ((WebProject) p).getUpdateHelper();
        
        // #119052
        if (sourceLevel == null) {
            sourceLevel = "1.5"; // NOI18N
        }
        PlatformUiSupport.storePlatform(ep, updateHelper, WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, javaPlatformName, new SpecificationVersion(sourceLevel));
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ProjectManager.getDefault().saveProject(p);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    copyRequiredLibraries(h, refHelper, createData);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex.getException());
        }
        
        ProjectWebModule pwm = (ProjectWebModule) p.getLookup().lookup(ProjectWebModule.class);
        if (pwm != null) //should not be null
            pwm.setContextPath(contextPath);
        
        return h;
    }
    
    public static Set<FileObject> ensureWelcomePage(FileObject webRoot, FileObject dd) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        try {
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
            if (welcomeFiles == null) {
                welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                ddRoot.setWelcomeFileList(welcomeFiles);
            }
            if (welcomeFiles.sizeWelcomeFile() == 0) {
                //create default index.jsp
                FileObject indexJSPFo = createIndexJSP(webRoot);
                if (indexJSPFo != null) {
                    // Returning FileObject of index.jsp, will be called its preferred action
                    resultSet.add(indexJSPFo);
                    welcomeFiles.addWelcomeFile("index.jsp"); //NOI18N
                    ddRoot.write(dd);
                }
            }
        } catch (ClassNotFoundException cnfe) {
            LOGGER.log(Level.SEVERE, cnfe.getLocalizedMessage(), cnfe);
        }
        return resultSet;
    }
    
    private static FileObject createIndexJSP(FileObject webFolder) throws IOException {
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N
        
        if (jspTemplate == null)
            return null; // Don't know the template
        
        DataObject mt = DataObject.find(jspTemplate);
        DataFolder webDf = DataFolder.findFolder(webFolder);
        return mt.createFromTemplate(webDf, "index").getPrimaryFile(); // NOI18N
    }
    
    
    /**
     * Creates a web project from esisting sources.
     *
     * @deprecated Use {@link #importProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper importProject(File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setWebModuleFO(wmFO);
        createData.setSourceFolders(new File[] {FileUtil.toFile(javaRoot)});
        createData.setTestFolders(null);
        createData.setDocBase(docBase);
        createData.setLibFolder(libFolder);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile(buildfile);
        return importProject(createData);
    }
    
    /**
     * Creates a web project from esisting sources.
     *
     * @deprecated Use {@link #importProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper importProject(final File dir, String name, FileObject wmFO, final File[] sourceFolders, File[] tstFolders, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setWebModuleFO(wmFO);
        createData.setSourceFolders(sourceFolders);
        createData.setTestFolders(tstFolders);
        createData.setDocBase(docBase);
        createData.setLibFolder(libFolder);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile(buildfile);
        return importProject(createData);
    }
    
    /**
     * Creates a web project from existing sources.
     * @param createData the object encapsulating necessary data to create the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper importProject(final WebProjectCreateData createData) throws IOException {
        final AntProjectHelper[] h = new AntProjectHelper[1];
        File dir = createData.getProjectDir();
        assert dir != null: "Project folder can't be null"; //NOI18N
        final FileObject projectDir = FileUtil.createFolder(dir);
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = importProjectImpl(createData, projectDir);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private static AntProjectHelper importProjectImpl(final WebProjectCreateData createData, 
            final FileObject projectDir) throws IOException {
        String name = createData.getName();
        FileObject wmFO = createData.getWebModuleFO();
        final File[] sourceFolders = createData.getSourceFolders();
        File[] tstFolders = createData.getTestFolders();
        FileObject docBase = createData.getDocBase();
        FileObject libFolder = createData.getLibFolder();
        String j2eeLevel = createData.getJavaEEVersion();
        String serverInstanceID = createData.getServerInstanceID();
        String buildfile = createData.getBuildfile();
        String javaPlatformName = createData.getJavaPlatformName();
        String sourceLevel = createData.getSourceLevel();
        boolean javaSourceBased = createData.getJavaSourceBased();
        FileObject webInfFolder = createData.getWebInfFolder();
        
        assert name != null: "Project name can't be null"; //NOI18N
        assert wmFO != null: "File object representation of the imported web project location can't be null";   //NOI18N
        assert sourceFolders != null: "Source package root can't be null";   //NOI18N
        assert docBase != null: "Web Pages folder can't be null";   //NOI18N
        assert serverInstanceID != null: "Server instance ID can't be null"; //NOI18N
        assert j2eeLevel != null: "Java EE version can't be null"; //NOI18N
        
        final String serverLibraryName = configureServerLibrary(createData.getLibrariesDefinition(),
                serverInstanceID, projectDir, createData.getServerLibraryName() != null);
        
        final AntProjectHelper antProjectHelper = setupProject(projectDir, name,
                serverInstanceID, j2eeLevel, createData.getLibrariesDefinition(), serverLibraryName);
        
        final WebProject p = (WebProject) ProjectManager.getDefault().findProject(antProjectHelper.getProjectDirectory());
        final ReferenceHelper referenceHelper = p.getReferenceHelper();
        EditableProperties ep = new EditableProperties(true);
        
        if (FileUtil.isParentOf(projectDir, wmFO) || projectDir.equals(wmFO)) {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT, "."); //NOI18N
        } else {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT,
                    referenceHelper.createForeignFileReference(FileUtil.toFile(wmFO), null));
        }
        ep.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, createFileReference(referenceHelper, projectDir, wmFO, docBase));
        
        final File[] testFolders = tstFolders;
        try {
            ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    Element data = antProjectHelper.getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();

                    Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                    data.appendChild(sourceRoots);
                    Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    data.appendChild(testRoots);

                    NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
                    assert nl.getLength() == 1;
                    sourceRoots = (Element) nl.item(0);
                    nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    assert nl.getLength() == 1;
                    testRoots = (Element) nl.item(0);
                    for (int i=0; i<sourceFolders.length; i++) {
                        String propName = "src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                        String srcReference = referenceHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                        Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute("id",propName);   //NOI18N
                        root.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
                        sourceRoots.appendChild(root);
                        EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(propName,srcReference);
                        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    }

                    if (testFolders == null || testFolders.length == 0) {
                        EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put("test.src.dir", "");
                        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    } else {
                        for (int i=0; i<testFolders.length; i++) {
                            if (!testFolders[i].exists()) {
                                FileUtil.createFolder(testFolders[i]);
                            }

                            String name = testFolders[i].getName();
                            String propName = "test." + name + ".dir";    //NOI18N
                            int rootIndex = 1;
                            EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            while (props.containsKey(propName)) {
                                rootIndex++;
                                propName = "test." + name + rootIndex + ".dir";   //NOI18N
                            }
                            String testReference = referenceHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                            root.setAttribute("id",propName);   //NOI18N
                            root.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
                            testRoots.appendChild(root);
                            props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                            props.put(propName,testReference);
                            antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                    }
                    antProjectHelper.putPrimaryConfigurationData(data,true);
                    ProjectManager.getDefault().saveProject(p);
                    copyRequiredLibraries(antProjectHelper, referenceHelper, createData);
                    return null;
                }
            });
        } catch (MutexException me ) {
            IOException ex = new IOException("project creation failed");
            ex.initCause(me);
            throw ex;
        }
        
        if (libFolder != null) {
            ep.setProperty(WebProjectProperties.LIBRARIES_DIR, createFileReference(referenceHelper, projectDir, wmFO, libFolder));
            
            //add libraries from the specified folder in the import wizard
            if (libFolder.isFolder()) {
                FileObject children [] = libFolder.getChildren();
                List<URL> libs = new LinkedList<URL>();
                for (int i = 0; i < children.length; i++) {
                    if (FileUtil.isArchiveFile(children[i])) {
                        libs.add(URLMapper.findURL(FileUtil.getArchiveRoot(children[i]), URLMapper.EXTERNAL));
                    }
                }
                p.getClassPathModifier().addRoots(libs.toArray(new URL[libs.size()]), ProjectProperties.JAVAC_CLASSPATH);
                //do we really need to add the listener? commenting it out
                //libFolder.addFileChangeListener(p);
            }
        }
        
        if (!GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(WebProjectProperties.BUILD_FILE, buildfile);
        }
        
        //creates conf.dir property and tries to simply guess it
        //(it would be nice to have a possibily to set this property in the wizard)
        Enumeration ch = FileSearchUtility.getChildrenToDepth(projectDir, 4, true);
        String confDir = ""; //NOI18N
        while (ch.hasMoreElements()) {
            FileObject f = (FileObject) ch.nextElement();
            if (f.isFolder() && f.getName().equalsIgnoreCase("conf")) { //NOI18N
                confDir = FileUtil.getRelativePath(projectDir, f);
                break;
            }
        }
        if (confDir.equals("")) { //NOI18N
            // if no conf directory was found, create default directory (#82147)
            projectDir.createFolder(DEFAULT_CONF_FOLDER);
            ep.setProperty(WebProjectProperties.CONF_DIR, DEFAULT_CONF_FOLDER);
        } else {
            ep.setProperty(WebProjectProperties.CONF_DIR, confDir); //NOI18N
        }
        // Default to conf.dir
        ep.setProperty(WebProjectProperties.PERSISTENCE_XML_DIR, "${"+WebProjectProperties.CONF_DIR+"}"); //NOI18N
        
        // #142164: try to find persistence.xml under project's source roots - that's where Eclipse stores is by default
        for (int i=0; i<sourceFolders.length; i++) {
            if (new File(sourceFolders[i], "META-INF"+File.separatorChar+"persistence.xml").exists()) { //NOI18N
                ep.setProperty(WebProjectProperties.PERSISTENCE_XML_DIR, "${src.dir}" + (i == 0 ? "" : Integer.toString(i+1))+"/META-INF"); //NOI18N
                break;
            }
        }
        
        //create resource.dir property, by default set to "setup"
        //(it would be nice to have a possibily to set this property in the wizard)
        ep.setProperty(WebProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        
        String webInfDir;
        if (webInfFolder != null) {
            webInfDir = createFileReference(referenceHelper, projectDir, wmFO, webInfFolder);
        } else {
            webInfDir = "web/WEB-INF"; // NOI18N
        }
        ep.setProperty(WebProjectProperties.WEBINF_DIR, webInfDir);
        
        ep.setProperty(WebProjectProperties.JAVA_SOURCE_BASED,javaSourceBased+"");
        
        UpdateHelper updateHelper = ((WebProject) p).getUpdateHelper();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel != null && (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")))
            sourceLevel = "1.5";
        PlatformUiSupport.storePlatform(ep, updateHelper, WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, javaPlatformName, sourceLevel != null ? new SpecificationVersion(sourceLevel) : null);
        
        // Utils.updateProperties() prevents problems caused by modification of properties in AntProjectHelper
        // (e.g. during createForeignFileReference()) when local copy of properties is concurrently modified
        Utils.updateProperties(antProjectHelper, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ProjectManager.getDefault().saveProject(p);
        
        return antProjectHelper;
    }
    
    private static void copyRequiredLibraries(AntProjectHelper h, ReferenceHelper rh, WebProjectCreateData data) throws IOException {
        if (!h.isSharableProject()) {
            return;
        }
        if (rh.getProjectLibraryManager().getLibrary("junit") == null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit")); // NOI18N
        }
        if (rh.getProjectLibraryManager().getLibrary("junit_4") == null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit_4")); // NOI18N
        }
        ClassPathSupport.makeSureProjectHasCopyLibsLibrary(h, rh);
    }

    private static String configureServerLibrary(final String librariesDefinition,
            final String serverInstanceId, final FileObject projectDir, final boolean serverLibrary) {

        String serverLibraryName = null;
        if (librariesDefinition != null && serverLibrary) {
            try {
                serverLibraryName = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<String>() {
                    public String run() throws Exception {
                        return SharabilityUtility.findOrCreateLibrary(
                                PropertyUtils.resolveFile(FileUtil.toFile(projectDir), librariesDefinition),
                                serverInstanceId).getName();
                    }
                });
            } catch (MutexException ex) {
                Exceptions.printStackTrace(ex.getException());
            }
        }
        return serverLibraryName;
    }

    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO, FileObject sourceprojectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(projectFO, referencedFO)) {
            return relativePath(projectFO, referencedFO);
        } else if (FileUtil.isParentOf(sourceprojectFO, referencedFO)) {
            String s = relativePath(sourceprojectFO, referencedFO);
            return s.length() > 0 ? SOURCE_ROOT_REF + "/" + s : SOURCE_ROOT_REF; //NOI18N
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent))
            return ""; // NOI18N
        if (!FileUtil.isParentOf(parent, child))
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath().substring(parent.getPath().length() + 1);
    }
    
    private static AntProjectHelper setupProject(FileObject dirFO, String name, 
            String serverInstanceID, String j2eeLevel, String librariesDefinition, String serverLibraryName) throws IOException {

        Utils.logUI(NbBundle.getBundle(WebProjectUtilities.class), "UI_WEB_PROJECT_CREATE_SHARABILITY", // NOI18N
                new Object[]{Boolean.valueOf(librariesDefinition != null), Boolean.valueOf(serverLibraryName != null)});

        AntProjectHelper h = ProjectGenerator.createProject(dirFO, WebProjectType.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries"); //NOI18N
        data.appendChild(wmLibs);
        
        Element addLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries"); //NOI18N
        data.appendChild(addLibs);
        
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty(WebProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(WebProjectProperties.DIST_WAR, "${"+WebProjectProperties.DIST_DIR+"}/${" + WebProjectProperties.WAR_NAME + "}"); // NOI18N
        ep.setProperty(WebProjectProperties.DIST_WAR_EAR, "${" + WebProjectProperties.DIST_DIR+"}/${" + WebProjectProperties.WAR_EAR_NAME + "}"); //NOI18N
        
        Deployment deployment = Deployment.getDefault();
        String serverType = deployment.getServerID(serverInstanceID);
        
        if (h.isSharableProject() && serverLibraryName != null) {
            // TODO constants
            ep.setProperty(ProjectProperties.JAVAC_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "classpath" + "}"); // NOI18N
            ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "classpath" + "}"); //NOI18N
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "wscompile" + "}"); //NOI18N
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "wsimport" + "}"); //NOI18N
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "wsgenerate" + "}"); //NOI18N
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH, 
                    "${libs." + serverLibraryName + "." + "wsinterop" + "}"); //NOI18N
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH, 
                    "${libs." + serverLibraryName + "." + "wsjwsdp" + "}"); //NOI18N
        } else {
            ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, ""); // NOI18N
        }
        
        
        ep.setProperty(WebProjectProperties.JSPCOMPILATION_CLASSPATH, "${jspc.classpath}:${javac.classpath}");
        
        ep.setProperty(WebProjectProperties.J2EE_PLATFORM, j2eeLevel);
        
        ep.setProperty(WebProjectProperties.WAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".war"); // NOI18N
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(WebProjectProperties.WAR_EAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".war"); // NOI18N
        
        ep.setProperty(WebProjectProperties.WAR_COMPRESS, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.LAUNCH_URL_RELATIVE, ""); // NOI18N
        ep.setProperty(WebProjectProperties.DISPLAY_BROWSER, "true"); // NOI18N

        // deploy on save since nb 6.5
        ep.setProperty(WebProjectProperties.DISABLE_DEPLOY_ON_SAVE, "false"); // NOI18N
        
        ep.setProperty(WebProjectProperties.J2EE_SERVER_TYPE, serverType);
        
        ep.setProperty(WebProjectProperties.JAVAC_DEBUG, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        
        ep.setProperty(ProjectProperties.JAVAC_TEST_CLASSPATH, new String[] {
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}:", // NOI18N
            "${libs.junit_4.classpath}", // NOI18N
        });
        ep.setProperty(ProjectProperties.RUN_TEST_CLASSPATH, new String[] {
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty(WebProjectProperties.DEBUG_TEST_CLASSPATH, new String[] {
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty(WebProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR, "${build.dir}/test/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_TEST_RESULTS_DIR, "${build.dir}/test/results"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_WEB_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/web"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_GENERATED_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/generated"); // NOI18N
        ep.setProperty(ProjectProperties.BUILD_CLASSES_DIR, "${"+WebProjectProperties.BUILD_WEB_DIR+"}/WEB-INF/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_WEB_EXCLUDES, "${"+ WebProjectProperties.BUILD_CLASSES_EXCLUDES +"}"); //NOI18N
        ep.setProperty(WebProjectProperties.DIST_JAVADOC_DIR, "${"+WebProjectProperties.DIST_DIR+"}/javadoc"); // NOI18N
        ep.setProperty(WebProjectProperties.NO_DEPENDENCIES, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        // #113297, #118187
        ep.setProperty(WebProjectProperties.DEBUG_CLASSPATH, Utils.getDefaultDebugClassPath());
        
        ep.setProperty(WebProjectProperties.RUNMAIN_JVM_ARGS, ""); // NOI18N
        ep.setComment(WebProjectProperties.RUNMAIN_JVM_ARGS, new String[] { // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_runmain.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_runmain.jvmargs_2"), // NOI18N
        }, false);
        
        ep.setProperty(WebProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_ENCODING, "${" + WebProjectProperties.SOURCE_ENCODING + "}"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.COMPILE_JSPS, "false"); // NOI18N
        
        // use the default encoding
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(WebProjectProperties.SOURCE_ENCODING, enc.name());
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        // set j2ee.platform.classpath
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedSpecVersions(J2eeModule.WAR).contains(j2eeLevel)) {
            Logger.getLogger("global").log(Level.WARNING,
                    "J2EE level:" + j2eeLevel + " not supported by server " + Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID) + " for module type WAR"); // NOI18N
        }
        
        if (!h.isSharableProject() || serverLibraryName == null) {
            String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
            ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);

            // set j2ee.platform.wscompile.classpath
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
                ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                        Utils.toClasspathString(wsClasspath));
            }

            // set j2ee.platform.wsimport.classpath
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT);
                ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH,
                        Utils.toClasspathString(wsClasspath));
            }

            // set j2ee.platform.wsgen.classpath
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSGEN)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSGEN);
                ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH,
                        Utils.toClasspathString(wsClasspath));
            }
            
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIT);
                ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            }

            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_JWSDP);
                ep.setProperty(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            }           
        }
        
        // set j2ee.platform.jsr109 support
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109)) {
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_JSR109_SUPPORT,
                    "true"); //NOI18N
        }
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, WebProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.WAR, serverInstanceID);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(WebProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        
        return h;
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator"); // NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(lineSep);
                line = br.readLine();
            }
        } finally {
            br.close();
        }

        return sb.toString();
    }

}
