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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a fresh EarProject from scratch or imports an existing Enterprise
 * Application.
 *
 * @author vince kraemer
 */
public final class EarProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "src/conf"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    
    private static final String SOURCE_ROOT_REF = "${" + EarProjectProperties.SOURCE_ROOT + "}"; //NOI18N
    
    private final File prjDir;
    private final String name;
    private final String j2eeLevel;
    private final String serverInstanceID;
    private final String sourceLevel;
    private final FileObject prjDirFO;
    
    private EarProjectGenerator(File prjDir, FileObject prjDirFO, String name, String j2eeLevel,
            String serverInstanceID, String sourceLevel) {
        this.prjDir = prjDir;
        this.prjDirFO = prjDirFO;
        this.name = name;
        this.j2eeLevel = j2eeLevel;
        this.serverInstanceID = serverInstanceID;
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel != null && (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")))
            sourceLevel = "1.5";       
        this.sourceLevel = sourceLevel;
    }
    
    /**
     * Creates a new empty Enterprise Application project.
     *
     * @param prjDir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File prjDir, String name, String j2eeLevel,
            String serverInstanceId, String sourceLevel) throws IOException {
        FileObject projectDir = FileUtil.createFolder(prjDir);
        final EarProjectGenerator earGen = new EarProjectGenerator(prjDir, projectDir, name, j2eeLevel,
                serverInstanceId, sourceLevel);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = earGen.doCreateProject();
                h[0] = helper;
            }});
        return h[0];
    }
    
    public static AntProjectHelper importProject(File pDir, final File sDir, String name,
            String j2eeLevel, String serverInstanceID, final String platformName,
            String sourceLevel, final Map<FileObject, ModuleType> userModules)
            throws IOException {
        FileObject projectDir = FileUtil.createFolder(pDir);
        final EarProjectGenerator earGen = new EarProjectGenerator(pDir, projectDir, name,
                j2eeLevel, serverInstanceID, sourceLevel);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                AntProjectHelper helper = earGen.doImportProject(sDir, userModules, platformName);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private AntProjectHelper doCreateProject() throws IOException {
        AntProjectHelper h = setupProject();
        FileObject docBase = FileUtil.createFolder(prjDirFO, DEFAULT_DOC_BASE_FOLDER);
        
        // create a default manifest
        FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(
                "org-netbeans-modules-j2ee-earproject/MANIFEST.MF"), docBase, "MANIFEST"); // NOI18N
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(EarProjectProperties.SOURCE_ROOT, "."); //NOI18N
        ep.setProperty(EarProjectProperties.META_INF, DEFAULT_DOC_BASE_FOLDER);
        ep.setProperty(EarProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);
        EarProject earProject = p.getLookup().lookup(EarProject.class);
        assert earProject != null;
        setupDD(j2eeLevel, docBase, earProject);
        
        return h;
    }
    
    private AntProjectHelper doImportProject(final File srcPrjDir,
            Map<FileObject, ModuleType> userModules,
            String platformName) throws IOException {
        FileObject srcPrjDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(srcPrjDir));
        FileObject docBase = FileUtil.createFolder(srcPrjDirFO, DEFAULT_DOC_BASE_FOLDER);
        
        AntProjectHelper earHelper = setupProject();
        EditableProperties ep = earHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ReferenceHelper referenceHelper = new ReferenceHelper(earHelper,
                earHelper.createAuxiliaryConfiguration(), earHelper.getStandardPropertyEvaluator());
        ep.setProperty(EarProjectProperties.SOURCE_ROOT,
                referenceHelper.createForeignFileReference(srcPrjDir, null));
        ep.setProperty(EarProjectProperties.META_INF, createFileReference(referenceHelper, srcPrjDirFO, docBase));
        earHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        FileObject earDirFO = earHelper.getProjectDirectory();
        EarProject earProject = ProjectManager.getDefault().findProject(earDirFO).getLookup().lookup(EarProject.class);
        
        if (null != earProject) {
            Application app = null;
            try {
                FileObject appXml = earProject.getAppModule().getDeploymentDescriptor();
                FileObject fileBeingCopied = null;
                if (null != appXml) {
                    // make a backup copy of the application.xml and its siblings
                    Enumeration filesToBackup =
                            appXml.getParent().getChildren(false);
                    while (null != filesToBackup &&
                            filesToBackup.hasMoreElements()) {
                        fileBeingCopied =
                                (FileObject) filesToBackup.nextElement();
                        if (fileBeingCopied.isData() &&
                                fileBeingCopied.canRead()) {
                            try {
                                FileUtil.copyFile(fileBeingCopied,
                                        appXml.getParent(),
                                        "original_"+fileBeingCopied.getName(), // NOI18N
                                        fileBeingCopied.getExt());
                            } catch (IOException ioe) {
                                // this is not fatal
                            }
                        }
                    }
                    app = DDProvider.getDefault().getDDRoot(appXml);
                    Module m[] = app.getModule();
                    if (null != m && m.length > 0) {
                        // make sure the config object has told us what to listen to...
                        earProject.getAppModule().getConfigSupport().ensureConfigurationReady();
                        // delete the modules
                        for (int k = 0; k < m.length; k++) {
                            app.removeModule(m[k]);
                        }
                        if (EarProjectUtil.isDDWritable(earProject)) {
                            app.write(earProject.getAppModule().getDeploymentDescriptor());
                        }
                        // notify the user here....
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(EarProjectGenerator.class, "MESSAGE_CheckContextRoots"),
                                NotifyDescriptor.WARNING_MESSAGE));
                    }
                }
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            }
        }
        
        setupDD(j2eeLevel, docBase, earProject);
        
        if (userModules == null || userModules.isEmpty()) {
            userModules = ModuleType.detectModules(srcPrjDirFO);
        }
        addUserModules(userModules, platformName, earHelper, earProject);
        
        // XXX all web module URI-to-ContextRoot mapping should happen here
        
        ProjectManager.getDefault().saveProject(earProject);
        
        earProject.getAppModule().getConfigSupport().createInitialConfiguration();
        if (sourceLevel != null) {
            EarProjectGenerator.setPlatformSourceLevel(earHelper, sourceLevel);
        }
        
        return earHelper;
    }
    
    private void addUserModules(final Map<FileObject, ModuleType> userModules,
            final String platformName, final AntProjectHelper h, final EarProject earProject) throws IOException {
        
        AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(h, aux, h.getStandardPropertyEvaluator());
        EarProjectProperties epp = new EarProjectProperties(earProject, refHelper, new EarProjectType());
        
        Set<Project> ejbs = new HashSet<Project>();
        Set<Project> webAndCars = new HashSet<Project>();
        for (Map.Entry<FileObject, ModuleType> entry : userModules.entrySet()) {
            FileObject subprojectDir = entry.getKey();
            ModuleType type = entry.getValue();
            Project subProject = addModule(type, epp, platformName, subprojectDir);
            assert subProject != null : "Directory " + subprojectDir + " does not contain valid project";
            switch (type) {
                case EJB:
                    ejbs.add(subProject);
                    break;
                case WEB:
                case CLIENT:
                    webAndCars.add(subProject);
                    break;
                default:
                    assert false : "Unknown module type: " + type;
            }
        }
        Project[] webAndCarsArray = webAndCars.toArray(new Project[webAndCars.size()]);
        for (Project ejb : ejbs) {
            addEJBToClassPaths(ejb, webAndCarsArray); // #74123
        }
    }
    
    /**
     * Adds EJB's artifact to Web and Application Client projects' classpaths.
     *
     * @param ejbJarProject must not be <code>null</code>
     * @param projects may contains also <code>null</code> elements
     */
    public static void addEJBToClassPaths(final Project ejbJarProject,
            final Project... projects) throws IOException {
        assert ejbJarProject != null;
        AntArtifact[] ejbArtifacts = AntArtifactQuery.findArtifactsByType(
                ejbJarProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        for (AntArtifact artifact : ejbArtifacts) {
            for (Project project : projects) {
                if (project == null) {
                    continue;
                }
                ProjectClassPathExtender pcpe = project.getLookup().lookup(ProjectClassPathExtender.class);
                URI[] locations = artifact.getArtifactLocations();
                if (pcpe != null && locations.length > 0) { // sanity check
                    pcpe.addAntArtifact(artifact, locations[0].normalize());
                }
            }
        }
    }
    
    private Project addModule(final ModuleType type, final EarProjectProperties epp,
            final String platformName, final FileObject subprojectRoot)
            throws IllegalArgumentException, IOException {
        
        FileObject javaRoot = getJavaRoot(subprojectRoot);
        File srcFolders[] = getSourceFolders(javaRoot);
        File subProjDir = FileUtil.normalizeFile(
                new File(prjDir, subprojectRoot.getNameExt()));
        AntProjectHelper subProjHelper = null;
        switch (type) {
            case WEB:
                subProjHelper = addWebModule(subprojectRoot, srcFolders, subProjDir, platformName);
                break;
            case EJB:
                subProjHelper = addEJBModule(javaRoot, subprojectRoot, subProjDir, platformName);
                break;
            case CLIENT:
                subProjHelper = addAppClientModule(javaRoot, subprojectRoot, subProjDir, platformName);
                break;
            default:
                assert false : "Unknown module type: " + type;
        }
        Project subProject = null;
        if (null != subProjHelper) {
            subProject = ProjectManager.getDefault().findProject(
                    subProjHelper.getProjectDirectory());
            epp.addJ2eeSubprojects(new Project[] { subProject });
        }
        return subProject;
    }
    
    private AntProjectHelper addAppClientModule(final FileObject javaRoot, final FileObject subprojectRoot, final File subProjDir, final String platformName) throws IOException {
        FileObject docBaseFO = FileUtil.createFolder(subprojectRoot, DEFAULT_DOC_BASE_FOLDER);
        File docBase = FileUtil.toFile(docBaseFO);
        AntProjectHelper subProjHelper = AppClientProjectGenerator.importProject(
                subProjDir, subprojectRoot.getName(),
                new File[] { FileUtil.toFile(javaRoot) },
                new File[0], docBase,
                null, checkJ2eeVersion(j2eeLevel, serverInstanceID, J2eeModule.CLIENT), serverInstanceID);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(subProjHelper, platformName, sourceLevel);
        }
        return subProjHelper;
    }
    
    private AntProjectHelper addEJBModule(final FileObject javaRoot, final FileObject subprojectRoot, final File subProjDir, final String platformName) throws IOException {
        FileObject docBaseFO = FileUtil.createFolder(subprojectRoot, DEFAULT_DOC_BASE_FOLDER);
        File docBase = FileUtil.toFile(docBaseFO);
        AntProjectHelper subProjHelper = EjbJarProjectGenerator.importProject(
                subProjDir, subprojectRoot.getName(),
                new File[] {FileUtil.toFile(javaRoot)},
                new File[0], docBase,
                null, checkJ2eeVersion(j2eeLevel, serverInstanceID, J2eeModule.EJB), serverInstanceID);
        if (platformName != null || sourceLevel != null) {
            EjbJarProjectGenerator.setPlatform(subProjHelper, platformName, sourceLevel);
        }
        return subProjHelper;
    }
    
    private AntProjectHelper addWebModule(final FileObject subprojectRoot, final File srcFolders[], final File subProjDir, final String platformName) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(subProjDir);
        createData.setName(subprojectRoot.getName());
        createData.setWebModuleFO(subprojectRoot);
        createData.setSourceFolders(srcFolders);
        createData.setTestFolders(new File[0]);
        createData.setDocBase(FileUtil.createFolder(subprojectRoot, "web")); //NOI18N
        createData.setLibFolder(null);
        createData.setJavaEEVersion(checkJ2eeVersion(j2eeLevel, serverInstanceID, J2eeModule.WAR));
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile("build.xml"); //NOI18N
        createData.setJavaPlatformName(platformName);
        createData.setSourceLevel(sourceLevel);
        // # 109128, BluePrints structure
        createData.setWebInfFolder(subprojectRoot.getFileObject("web/WEB-INF"));
        return WebProjectUtilities.importProject(createData);
    }
    
    static FileObject setupDD(final String j2eeLevel, final FileObject docBase,
            final EarProject earProject) throws IOException {
        return setupDD(j2eeLevel, docBase, earProject, false);
    }

    /**
     * Generate deployment descriptor (<i>application.xml</i>) if needed or forced (applies for JAVA EE 5).
     * <p>
     * For J2EE 1.4 or older the deployment descriptor is always generated if missing.
     * For JAVA EE 5 it is only generated if missing and forced as well.
     * @param j2eeLevel J2EE level, see {@link J2eeModule J2eeModule constants}.
     * @param docBase Configuration directory.
     * @param earProject EAR project instance.
     * @param force if <code>true</code> <i>application.xml</i> is generated even if it's not needed
     *              (applies only for JAVA EE 5).
     * @return {@link FileObject} of the deployment descriptor or <code>null</code>.
     * @throws java.io.IOException if any error occurs.
     */
    public static FileObject setupDD(final String j2eeLevel, final FileObject docBase,
            final Project earProject, boolean force) throws IOException {
        FileObject dd = docBase.getFileObject(ProjectEar.FILE_DD);
        if (dd != null) {
            return dd; // already created
        }
        FileObject template = null;
        if (EarProjectUtil.isDDCompulsory(earProject)) {
            template = Repository.getDefault().getDefaultFileSystem().findResource(
                    "org-netbeans-modules-j2ee-earproject/ear-1.4.xml"); // NOI18N
        } else if (J2eeModule.JAVA_EE_5.equals(j2eeLevel)) {
            if (force) {
                template = Repository.getDefault().getDefaultFileSystem().findResource(
                        "org-netbeans-modules-j2ee-earproject/ear-5.xml"); // NOI18N
            } else {
                String newLine = System.getProperty("line.separator");
                /*StringBuilder sb = new StringBuilder();
                for (StackTraceElement ste : new RuntimeException().getStackTrace()) {
                    sb.append("\t");
                    sb.append(ste.toString());
                    sb.append(newLine);
                }*/

                Logger.getLogger("global").log(Level.FINE,
                        "Deployment descriptor (application.xml) is not compulsory for JAVA EE 5." + newLine +
                        "If it\'s *really* needed, set force param to true." + newLine);
            }
        } else {
            assert false : "Unknown j2eeLevel: " + j2eeLevel;
        }
        if (template != null) {
            dd = FileUtil.copyFile(template, docBase, "application"); // NOI18N
            Application app = DDProvider.getDefault().getDDRoot(dd);
            app.setDisplayName(ProjectUtils.getInformation(earProject).getDisplayName());
            //#118047 avoiding the use of EarProject not possible here.
            // API for retrieval of getJarContentAdditional() not present.
            EarProject defInst = earProject.getLookup().lookup(EarProject.class);
            if (defInst != null) {
                EarProjectProperties epp = defInst.getProjectProperties();
                for (VisualClassPathItem vcpi : epp.getJarContentAdditional()) {
                    epp.addItemToAppDD(app, vcpi);
                }
            }
            app.write(dd);
        }
        return dd;
    }
    
    /** Check that the J2EE version requested for the EAR is also supported for
     * the module type and if not suggest a different version.
     * For now the only check is to use J2EE 1.4 if JavaEE5 is not supported.
     * Otherwise use the requestedVersion.
     */
    public static String checkJ2eeVersion(String requestedVersion, String serverInstanceID, Object moduleType) {
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        Set <String> versions = platform.getSupportedSpecVersions(moduleType);
        if (!versions.contains(requestedVersion) && (versions.contains(J2eeModule.J2EE_14))) {
            return J2eeModule.J2EE_14;
        }
        return requestedVersion;
    }
    
    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent)) {
            return "";
        }
        if (!FileUtil.isParentOf(parent, child)) {
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child); // NOI18N
        }
        return child.getPath().substring(parent.getPath().length() + 1);
    }
    
    private AntProjectHelper setupProject() throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(prjDirFO, EarProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, EarProjectProperties.TAG_WEB_MODULE_LIBRARIES); //NOI18N
        data.appendChild(wmLibs);
        
        Element addLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, EarProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES); //NOI18N
        data.appendChild(addLibs);
        
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty(EarProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(EarProjectProperties.DIST_JAR, "${"+EarProjectProperties.DIST_DIR+"}/" + name + ".ear"); // NOI18N
        
        ep.setProperty(EarProjectProperties.J2EE_PLATFORM, j2eeLevel);
        
        ep.setProperty(EarProjectProperties.JAR_NAME, name + ".ear"); // NOI18N
        ep.setProperty(EarProjectProperties.JAR_COMPRESS, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, "");
        ep.setProperty(EarProjectProperties.LAUNCH_URL_RELATIVE, "");
        ep.setProperty(EarProjectProperties.DISPLAY_BROWSER, "true"); // NOI18N
        Deployment deployment = Deployment.getDefault();
        ep.setProperty(EarProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID));
        
        String srcLevel = sourceLevel;
        if (srcLevel == null) {
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
            srcLevel = v.toString();
            // #89131: these levels are not actually distinct from 1.5.
            if (srcLevel.equals("1.6") || srcLevel.equals("1.7"))
                srcLevel = "1.5";       
        }
        ep.setProperty(EarProjectProperties.JAVAC_SOURCE, srcLevel); //NOI18N
        ep.setProperty(EarProjectProperties.JAVAC_DEBUG, "true"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N
        
        //xxx Default should be 1.2
        //http://projects.netbeans.org/buildsys/j2se-project-ui-spec.html#Build_Compiling_Sources
        ep.setProperty(EarProjectProperties.JAVAC_TARGET, srcLevel); //NOI18N
        
        ep.setProperty(EarProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(EarProjectProperties.BUILD_GENERATED_DIR, "${"+EarProjectProperties.BUILD_DIR+"}/generated"); // NOI18N
        ep.setProperty(EarProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs"); // NOI18N
        ep.setProperty(EarProjectProperties.NO_DEPENDENCIES, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        ep.setProperty(EarProjectProperties.DEBUG_CLASSPATH,
                "${"+EarProjectProperties.JAVAC_CLASSPATH+"}::${"+ // NOI18N
                EarProjectProperties.JAR_CONTENT_ADDITIONAL+"}:${"+ // NOI18N
                EarProjectProperties.RUN_CLASSPATH+"}"); // NOI18N
        
        J2eePlatform j2eePlatform = deployment.getJ2eePlatform(serverInstanceID);
        EarProjectProperties.setACProperties(j2eePlatform, ep);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(EarProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(EarProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        EarProjectProperties.setACPrivateProperties(j2eePlatform, serverInstanceID, ep);
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(prjDirFO);
        ProjectManager.getDefault().saveProject(p);
        // ant deployment support
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(prjDir,
                    EarProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.EAR, serverInstanceID);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        return h;
    }
    
    private String createFileReference(ReferenceHelper refHelper,
            FileObject sourceprojectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(prjDirFO, referencedFO)) {
            return relativePath(prjDirFO, referencedFO);
        } else if (FileUtil.isParentOf(sourceprojectFO, referencedFO)) {
            String s = relativePath(sourceprojectFO, referencedFO);
            return s.length() > 0 ? SOURCE_ROOT_REF + '/' + s : SOURCE_ROOT_REF;
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    public static void setPlatformSourceLevel(final AntProjectHelper helper, final String sourceLevel) {
        FileObject projectDir = helper.getProjectDirectory();
        if (projectDir == null) {
            return;
        }
        try {
            projectDir.getFileSystem().runAtomicAction(new AtomicAction() {
                public void run() throws IOException {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        public void run() {
                            try {
                                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                // #89131: these levels are not actually distinct from 1.5.
                                String srcLevel = sourceLevel;
                                if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
                                    srcLevel = "1.5";
                                ep.setProperty(EarProjectProperties.JAVAC_SOURCE, srcLevel);
                                ep.setProperty(EarProjectProperties.JAVAC_TARGET, srcLevel);
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
    
    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(':');
            }
        }
        return classpath.toString();
    }
    
    private FileObject getJavaRoot(final FileObject moduleRoot) throws IOException {
        FileObject javaRoot = moduleRoot.getFileObject("src/java"); // NOI18N
        // XXX this is a hack. Remove once 56487 is resolved
        if (null == javaRoot) {
            FileObject srcDir = moduleRoot.getFileObject("src"); // NOI18N
            if (null == srcDir) {
                srcDir = moduleRoot.createFolder("src"); // NOI18N
            }
            javaRoot = srcDir.createFolder("java"); // NOI18N
        }
        return javaRoot;
        // end hack for 56487
    }
    
    private File[] getSourceFolders(final FileObject javaRoot) {
        return null == javaRoot ? new File[0] :
            new File[] { FileUtil.toFile(javaRoot) };
    }
    
}
