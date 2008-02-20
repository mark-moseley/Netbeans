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

package org.netbeans.modules.j2ee.ejbjarproject.api;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.common.sharability.SharabilityUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng ejb module
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class EjbJarProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6";
    
    private EjbJarProjectGenerator() {}
    
    /**
     * Create a new empty EjbJar project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @param j2eeLevel Java EE level
     * @param serverInstanceID server instance ID
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, final String name, 
            final String j2eeLevel, final String serverInstanceID) throws IOException {

        return createProject(dir, name, j2eeLevel, serverInstanceID, null, null);
    }
    
    public static AntProjectHelper createProject(File dir, final String name, 
            final String j2eeLevel, final String serverInstanceID,
            final String librariesDefinition, final String serverLibraryName) throws IOException {
        final FileObject projectDir = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = createProjectImpl(projectDir, name,
                        j2eeLevel, serverInstanceID, librariesDefinition, serverLibraryName);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private static AntProjectHelper createProjectImpl(FileObject projectDir, String name, 
            String j2eeLevel, final String serverInstanceID, final String librariesDefinition, final String serverLibraryName) throws IOException {
        
        FileObject srcRoot = projectDir.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
        srcRoot.createFolder(DEFAULT_JAVA_FOLDER); //NOI18N
        projectDir.createFolder(DEFAULT_TEST_FOLDER);
        FileObject confRoot = srcRoot.createFolder(DEFAULT_DOC_BASE_FOLDER); // NOI18N
        
        //create a default manifest
        FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-j2ee-ejbjarproject/MANIFEST.MF"), confRoot, "MANIFEST"); //NOI18N
        
        final AntProjectHelper h = setupProject(projectDir, name,
                "src", "test", null, null, null, j2eeLevel, serverInstanceID,
                librariesDefinition, serverLibraryName);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(EjbJarProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.META_INF, "${"+EjbJarProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.SRC_DIR, "${"+EjbJarProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_JAVA_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.META_INF_EXCLUDES, "sun-cmp-mappings.xml"); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(EjbJarProjectProperties.SOURCE_ENCODING, enc.name());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        EjbJarProject p = (EjbJarProject) ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);
        
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    copyRequiredLibraries(h, refHelper, serverInstanceID, serverLibraryName);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex.getException());
        }
        
        // create ejb-jar.xml
        if (!J2eeModule.JAVA_EE_5.equals(j2eeLevel)) {
            String resource = "org-netbeans-modules-j2ee-ejbjarproject/ejb-jar-2.1.xml";
            FileObject ddFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(resource), confRoot, "ejb-jar"); //NOI18N
            EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFile);
            ejbJar.setDisplayName(name);
            ejbJar.write(ddFile);
        }
        
        return h;
    }
    
    /**
     * Import project from source or exploded archive
     * @param dir root directory of project
     * @param name name of the project
     * @param sourceFolders Array of folders that hold the projects source
     * or exploded archive
     * @param testFolders folders that hold test code for the project
     * @param configFilesBase Folder that holds the projects config files
     * like deployment descriptors
     * @param libFolder the libraries associated with the project
     * @param j2eeLevel spec revision level
     * @param serverInstanceID id of target server
     * @param fromJavaSources flag whether the project is from source or
     * exploded archive of class files
     * @throws java.io.IOException if something goes wrong
     * @return The AntProjectHelper for the project
     */
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel,
            String serverInstanceID, boolean fromJavaSources) throws IOException {
        
        return importProject(dir, name, sourceFolders, testFolders, configFilesBase,
                libFolder, j2eeLevel, serverInstanceID, fromJavaSources, null, null);
    }
    
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel,
            String serverInstanceID, boolean fromJavaSources,
            String librariesDefinition, String serverLibraryName) throws IOException {
        
        AntProjectHelper retVal = importProject(dir,name,sourceFolders,testFolders,
                configFilesBase,libFolder,j2eeLevel,serverInstanceID, librariesDefinition, serverLibraryName);
        EditableProperties subEp = retVal.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        subEp.setProperty(EjbJarProjectProperties.JAVA_SOURCE_BASED,fromJavaSources+""); // NOI18N
        retVal.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
        Project subP = ProjectManager.getDefault().findProject(retVal.getProjectDirectory());
        ProjectManager.getDefault().saveProject(subP);
        return retVal;
    }    
    
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel, 
            final String serverInstanceID) throws IOException {

        return importProject(dir, name, sourceFolders, testFolders, configFilesBase,
                libFolder, j2eeLevel, serverInstanceID, null, null);
    }
    
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel, 
            final String serverInstanceID, final String librariesDefinition, final String serverLibraryName) throws IOException {
        final FileObject projectDir = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = importProjectImpl(projectDir, name, sourceFolders, testFolders,
                    configFilesBase, libFolder, j2eeLevel, serverInstanceID, librariesDefinition, serverLibraryName);
                h[0] = helper;
            }});
        return h[0];
    }    
    
    private static AntProjectHelper importProjectImpl(final FileObject projectDir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel,
            String serverInstanceID, String librariesDefinition, String serverLibraryName) throws IOException {
        
        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
        // this constructor creates only java application type
        
        final AntProjectHelper h = setupProject(projectDir,
                name,
                null,
                null,
                configFilesBase, (libFolder == null ? null : libFolder),
                null,
                j2eeLevel,
                serverInstanceID,
                librariesDefinition,
                serverLibraryName);
        final EjbJarProject p = (EjbJarProject) ProjectManager.getDefault().findProject(projectDir);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    Element data = h.getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();
                    NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots"); //NOI18N
                    assert nl.getLength() == 1;
                    Element sourceRoots = (Element) nl.item(0);
                    nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    assert nl.getLength() == 1;
                    Element testRoots = (Element) nl.item(0);
                    for (int i=0; i<sourceFolders.length; i++) {
                        String propName = "src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                        String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                        Element root = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute("id",propName);   //NOI18N
                        sourceRoots.appendChild(root);
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(propName,srcReference);
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    }

                    if (testFolders.length == 0) {
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put("test.src.dir", ""); // NOI18N
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    } else {
                        for (int i=0; i<testFolders.length; i++) {
                            if (!testFolders[i].exists()) {
                                testFolders[i].mkdirs();
                            }
                            String propName = "test.src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                            String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                            root.setAttribute("id",propName);   //NOI18N
                            testRoots.appendChild(root);
                            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                            props.put(propName,testReference);
                            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                    }
                    h.putPrimaryConfigurationData(data,true);
                    ProjectManager.getDefault().saveProject(p);
                    return null;
                }
            });
        } catch (MutexException me ) {
            IOException ex = new IOException("project creation failed");
            ex.initCause(me);
            throw ex;
        }
        
        // AB: fix for #53170: if j2eeLevel is 1.4 and ejb-jar.xml is version 2.0, we upgrade it to version 2.1
        FileObject ejbJarXml = FileUtil.toFileObject(configFilesBase).getFileObject("ejb-jar.xml"); // NOI18N
        if (ejbJarXml != null) {
            try {
                EjbJar root = DDProvider.getDefault().getDDRoot(ejbJarXml);
                boolean writeDD = false;
                if (new BigDecimal(EjbJar.VERSION_2_0).equals(root.getVersion()) && j2eeLevel.equals(ProjectProperties.J2EE_1_4)) { // NOI18N
                    root.setVersion(new BigDecimal(EjbJar.VERSION_2_1));
                    writeDD = true;
                }
                // also set the display name if not set (#55733)
                String dispName = root.getDefaultDisplayName();
                if (null == dispName || dispName.trim().length() == 0) {
                    root.setDisplayName(name);
                    writeDD = true;
                }
                if (writeDD) {
                    root.write(ejbJarXml);
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return h;
    }
    
    private static void copyRequiredLibraries(AntProjectHelper h, ReferenceHelper rh,
            String serverInstanceId, String serverlibraryName) throws IOException {

        if (!h.isSharableProject()) {
            return;
        }
        if (rh.getProjectLibraryManager().getLibrary("junit") == null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit")); // NOI18N
        }
        if (rh.getProjectLibraryManager().getLibrary("junit_4") == null) { // NOI18N
            rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit_4")); // NOI18N
        }

        if (h.isSharableProject() && serverlibraryName != null  && SharabilityUtilities.getLibrary(
                h.resolveFile(h.getLibrariesLocation()), serverlibraryName) == null) {

            SharabilityUtilities.createLibrary(
                h.resolveFile(h.getLibrariesLocation()), serverlibraryName, serverInstanceId);
        }
    }
    
    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(projectFO, referencedFO)) {
            return relativePath(projectFO, referencedFO);
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent)) {
            return "";
        }
        if (!FileUtil.isParentOf(parent, child)) {
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child);
        }
        return child.getPath().substring(parent.getPath().length() + 1);
    }
    
    private static AntProjectHelper setupProject(FileObject dirFO, String name,
            String srcRoot, String testRoot, File configFiles, File libraries, String resources,
            String j2eeLevel, String serverInstanceID, String librariesDefinition, String serverLibraryName) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, EjbJarProjectType.TYPE, librariesDefinition);
        final EjbJarProject prj = (EjbJarProject) ProjectManager.getDefault().findProject(h.getProjectDirectory());
        final ReferenceHelper referenceHelper = prj.getReferenceHelper();
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION));
        data.appendChild(minant);
        
        // TODO: ma154696: not sure if needed
        //        Element addLibs = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "ejb-module-additional-libraries"); //NOI18N
        //        data.appendChild(addLibs);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(EjbJarProjectGenerator.class, "NAME_src.dir"));
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","test.src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(EjbJarProjectGenerator.class, "NAME_test.src.dir"));
            testRoots.appendChild(root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        if (resources != null) {
            ep.setProperty(EjbJarProjectProperties.RESOURCE_DIR, resources);
        } else {
            ep.setProperty(EjbJarProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        }
        
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = v.toString();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")) {
            sourceLevel = "1.5";
        }
        ep.setProperty(EjbJarProjectProperties.JAVAC_SOURCE, sourceLevel); //NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVAC_TARGET, sourceLevel); //NOI18N
        
        ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, "");
        
        ep.setProperty(EjbJarProjectProperties.DIST_DIR, "dist");
        ep.setProperty(EjbJarProjectProperties.DIST_JAR, "${"+EjbJarProjectProperties.DIST_DIR+"}/" + "${" + EjbJarProjectProperties.JAR_NAME + "}");
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(EjbJarProjectProperties.DIST_EAR_JAR, "${"+EjbJarProjectProperties.DIST_DIR+"}/" + "${" + EjbJarProjectProperties.JAR_NAME + "}");
        ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(EjbJarProjectProperties.JAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".jar");
        ep.setProperty(EjbJarProjectProperties.JAR_COMPRESS, "false");
        //        ep.setProperty(EjbJarProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        Deployment deployment = Deployment.getDefault();
        ep.setProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID));
        
        if (h.isSharableProject() && serverLibraryName != null) {
            ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH, "${libs." + serverLibraryName + ".classpath}"); //NOI18N
        }
        ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_SHARED,
                Boolean.toString(h.isSharableProject() && serverLibraryName != null));        
        
        ep.setProperty(EjbJarProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(EjbJarProjectProperties.JAVAC_DEPRECATION, "false");
        
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
        ep.setProperty(EjbJarProjectProperties.DEBUG_TEST_CLASSPATH, new String[] {
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty(EjbJarProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR, "${build.dir}/test/classes"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.BUILD_TEST_RESULTS_DIR, "${build.dir}/test/results"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.BUILD_GENERATED_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(ProjectProperties.BUILD_CLASSES_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(EjbJarProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(EjbJarProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(EjbJarProjectProperties.DIST_JAVADOC_DIR, "${"+EjbJarProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(EjbJarProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(EjbJarProjectProperties.DEBUG_CLASSPATH, "${"+ProjectProperties.JAVAC_CLASSPATH+"}:${"+ProjectProperties.BUILD_CLASSES_DIR+"}");
        ep.setProperty(EjbJarProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_ENCODING, "${" + EjbJarProjectProperties.SOURCE_ENCODING + "}"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
        // use the default encoding
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(EjbJarProjectProperties.SOURCE_ENCODING, enc.name());
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        if (configFiles != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(configFiles));
            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(EjbJarProjectProperties.META_INF, ref);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        if (libraries != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(libraries));
            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(EjbJarProjectProperties.LIBRARIES_DIR, ref);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        // set j2ee.platform.classpath
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedSpecVersions(J2eeModule.EJB).contains(j2eeLevel)) {
            Logger.getLogger("global").log(Level.WARNING,
                    "J2EE level:" + j2eeLevel + " not supported by server " + Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID) + " for module type EJB"); // NOI18N
        }
        
        if (!h.isSharableProject() || serverLibraryName == null) {
            String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
            ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
        }
        
        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                    Utils.toClasspathString(wsClasspath));
        }
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, EjbJarProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.EJB, serverInstanceID);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(EjbJarProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    // AB: this method is also called from the enterprise application, so we can't pass UpdateHelper here
    // well, actually we can, but let's not expose too many classes
    public static void setPlatform(final AntProjectHelper helper, final String platformName, final String sourceLevel) {
        FileObject projectDir = helper.getProjectDirectory();
        if (projectDir == null) {
            return;
        }
        // issue 89278: do not fire file change events under ProjectManager.MUTEX,
        // it is deadlock-prone
        try {
            projectDir.getFileSystem().runAtomicAction(new AtomicAction() {
                public void run() throws IOException {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        public void run() {
                            try {
                                EjbJarProject project = (EjbJarProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                                UpdateHelper updateHelper = project.getUpdateHelper();
                                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                String finalPlatformName = platformName;
                                if (finalPlatformName == null) {
                                    finalPlatformName = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
                                }

                                PlatformUiSupport.storePlatform(ep, updateHelper, EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, finalPlatformName, sourceLevel != null ? new SpecificationVersion(sourceLevel) : null);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(helper.getProjectDirectory()));
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    
    }
}
