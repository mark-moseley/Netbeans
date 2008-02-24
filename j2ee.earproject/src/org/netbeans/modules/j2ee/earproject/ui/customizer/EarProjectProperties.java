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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.J2eePlatformUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerRun.ApplicationUrisComboBoxModel;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Helper class. Defines constants for properties. Knows the proper
 * place where to store the properties.
 *
 * @author Petr Hrebejk
 */
public final class EarProjectProperties {
    
    public static final String J2EE_SPEC_14_LABEL =
            NbBundle.getMessage(EarProjectProperties.class, "J2EESpecLevel_14");
    public static final String JAVA_EE_SPEC_50_LABEL =
            NbBundle.getMessage(EarProjectProperties.class, "JavaEESpecLevel_50");
    
    // Special properties of the project
    public static final String WEB_PROJECT_NAME = "web.project.name"; //NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; //NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; //NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; //NOI18N
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_JAR = "dist.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String JAR_NAME = "jar.name"; //NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String JAR_CONTENT_ADDITIONAL = "jar.content.additional"; //NOI18N
    
    public static final String APPLICATION_CLIENT = "app.client"; // NOI18N
    public static final String APPCLIENT_MAIN_CLASS = "main.class"; // NOI18N
    public static final String APPCLIENT_ARGS = "application.args"; // NOI18N
    public static final String APPCLIENT_JVM_OPTIONS = "j2ee.appclient.jvmoptions"; // NOI18N
    public static final String APPCLIENT_MAINCLASS_ARGS = "j2ee.appclient.mainclass.args"; // NOI18N
    
    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String META_INF = "meta.inf"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; //NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; //NOI18N
    public static final String JAVADOC_USE="javadoc.use"; //NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; //NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; //NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; //NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; //NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; //NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; //NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; //NOI18N
    
    public static final String JAVADOC_PREVIEW="javadoc.preview"; //NOI18N
    
    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N
    
    public static final String CLIENT_NAME = "j2ee.clientName"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    
    public static final String APPCLIENT_TOOL_RUNTIME = "j2ee.appclient.tool.runtime"; // NOI18N
    public static final String APPCLIENT_TOOL_MAINCLASS = "j2ee.appclient.tool.mainclass"; // NOI18N
    public static final String APPCLIENT_TOOL_JVMOPTS = "j2ee.appclient.tool.jvmoptions";  // NOI18N
    public static final String APPCLIENT_TOOL_ARGS = "j2ee.appclient.tool.args"; // NOI18N
    
    /**
     * "API" contract between Application Client and Glassfish plugin's
     * J2eePlatformImpl implementation.
     */
    private static final String J2EE_PLATFORM_APPCLIENT_ARGS = "j2ee.appclient.args"; // NOI18N
    
    static final String APPCLIENT_WA_COPY_CLIENT_JAR_FROM = "wa.copy.client.jar.from"; // NOI18N
    
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; // NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    
    // CustomizerLibraries
    Document SHARED_LIBRARIES_MODEL;
    DefaultListModel DEBUG_CLASSPATH_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    
    // CustomizerJarContent
    Document ARCHIVE_NAME_MODEL;
    ButtonModel ARCHIVE_COMPRESS_MODEL;
    Document BUILD_CLASSES_EXCLUDES_MODEL;
    AdditionalContentTableModel EAR_CONTENT_ADDITIONAL_MODEL;
    TableCellRenderer CLASS_PATH_TABLE_RENDERER;
    
    // CustomizerRun
    ApplicationUrisComboBoxModel CLIENT_MODULE_MODEL; 
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL; 
    Document J2EE_PLATFORM_MODEL;
    ButtonModel DISPLAY_BROWSER_MODEL; 
    Document LAUNCH_URL_RELATIVE_MODEL;
    Document MAIN_CLASS_MODEL;
    Document ARUGMENTS_MODEL;
    Document VM_OPTIONS_MODEL;
    Document APPLICATION_CLIENT_MODEL;
    
    static final String UI_LOGGER_NAME = "org.netbeans.ui.ear.project"; //NOI18N
    static final Logger UI_LOGGER = Logger.getLogger(UI_LOGGER_NAME);
    
    // Private fields ----------------------------------------------------------
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper refHelper;
    private final UpdateHelper updateHelper;
    private final EarProject project;
    private final GeneratedFilesHelper genFilesHelper;
    private PropertyEvaluator evaluator;
    public ClassPathSupport cs;
    
    /** Utility field used by bound properties. */
    private final PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);

    public EarProjectProperties(EarProject project, UpdateHelper updateHelper, 
            PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = project.getUpdateHelper();
        this.antProjectHelper = updateHelper.getAntProjectHelper();
        this.refHelper = refHelper;
        this.genFilesHelper = project.getGeneratedFilesHelper();
        this.evaluator = evaluator;
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        cs = project.getClassPathSupport();
        init();
    }

    private void init() {
        
        // CustomizerLibraries
        SHARED_LIBRARIES_MODEL = new PlainDocument(); 
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getAntProjectHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        DEBUG_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.RUN_CLASSPATH ), null ) );
        CLASS_PATH_LIST_RENDERER = ProjectProperties.createClassPathListRendered(evaluator, project.getProjectDirectory());

        // CustomizerJarContent
        ARCHIVE_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        ARCHIVE_NAME_MODEL = projectGroup.createStringDocument( evaluator, JAR_NAME );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        EAR_CONTENT_ADDITIONAL_MODEL = AdditionalContentTableModel.createTableModel( cs.itemsIterator( (String)projectProperties.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES) );
        CLASS_PATH_TABLE_RENDERER = ProjectProperties.createClassPathTableRendered(evaluator, project.getProjectDirectory());

        // CustomizerRun
        J2EE_PLATFORM_MODEL = projectGroup.createStringDocument(evaluator, J2EE_PLATFORM);
        LAUNCH_URL_RELATIVE_MODEL = projectGroup.createStringDocument(evaluator, LAUNCH_URL_RELATIVE);
        DISPLAY_BROWSER_MODEL = projectGroup.createToggleButtonModel(evaluator, DISPLAY_BROWSER);
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(privateProperties.getProperty( J2EE_SERVER_INSTANCE ), projectProperties.getProperty(J2EE_PLATFORM));
        MAIN_CLASS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_MAIN_CLASS);
        ARUGMENTS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_ARGS);
        VM_OPTIONS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_JVM_OPTIONS);
        APPLICATION_CLIENT_MODEL = projectGroup.createStringDocument(evaluator, APPLICATION_CLIENT);
        CLIENT_MODULE_MODEL = CustomizerRun.createApplicationUrisComboBoxModel(project, this);
    }

    private void saveLibrariesLocation() throws IOException, IllegalArgumentException {
        try {
            String str = SHARED_LIBRARIES_MODEL.getText(0, SHARED_LIBRARIES_MODEL.getLength()).trim();
            if (str.length() == 0) {
                str = null;
            }
            String old = project.getAntProjectHelper().getLibrariesLocation();
            if ((old == null && str == null) || (old != null && old.equals(str))) {
                //ignore, nothing changed..
            } else {
                project.getAntProjectHelper().setLibrariesLocation(str);
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (BadLocationException x) {
            Exceptions.printStackTrace(x);
        }
    }
    
    private void storeProperties() throws IOException {
        // Store special properties
        
        // Modify the project dependencies properly        
        resolveProjectDependencies();
       
        // Encode all paths (this may change the project properties)
        String[] debug_cp = cs.encodeToStrings(ClassPathUiSupport.getList(DEBUG_CLASSPATH_MODEL), null );
        String[] additional_content = cs.encodeToStrings(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);

        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );

        // Save all paths
        projectProperties.setProperty( ProjectProperties.RUN_CLASSPATH, debug_cp );
        projectProperties.setProperty( JAR_CONTENT_ADDITIONAL, additional_content );
        
        // Set new server instance ID
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            setNewServerInstanceValue(J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()), 
                    project, projectProperties, privateProperties);
        }
        
        ArrayList libs = new ArrayList ();
        libs.addAll(ClassPathUiSupport.getList(EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));
        ProjectProperties.storeLibrariesLocations (libs.iterator(), projectProperties, project.getProjectDirectory());
        
        if (CLIENT_MODULE_MODEL.getSelectedItem() != null) {
            CLIENT_MODULE_MODEL.storeSelectedItem(projectProperties);
        }
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );
        
    }

    public static void setServerInstance(final Project project, final UpdateHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    setNewServerInstanceValue(serverInstanceID, project, projectProps, privateProps);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    
    private static void setNewServerInstanceValue(String newServInstID, Project project, 
            EditableProperties projectProps, EditableProperties privateProps) {
        String oldServInstID = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        if (oldServInstID != null) {
            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
            if (oldJ2eePlatform != null) {
                ((EarProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            // probably missing server error
            Logger.getLogger("global").log(Level.INFO, "J2EE platform is null."); // NOI18N
            
            // update j2ee.server.instance
            privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
            
            // remove J2eePlatform.TOOL_APP_CLIENT_RUNTIME classpath
            privateProps.remove(APPCLIENT_TOOL_RUNTIME);
            
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
            return;
        }
        
        ((EarProject)project).registerJ2eePlatformListener(j2eePlatform);
        
        storeJ2EEServerProperties(newServInstID, project, projectProps, privateProps, null);
        
        // ui log for the server change
        if(newServInstID != null && !newServInstID.equals(oldServInstID)) {
            LogRecord logRecord = new LogRecord(Level.INFO, "UI_EAR_PROJECT_SERVER_CHANGED");  //NOI18N
            logRecord.setLoggerName(UI_LOGGER_NAME); //NOI18N
            logRecord.setResourceBundle(NbBundle.getBundle(EarProjectProperties.class));
            logRecord.setParameters(new Object[] { 
                Deployment.getDefault().getServerID(oldServInstID),
                oldServInstID,
                Deployment.getDefault().getServerID(newServInstID),
                newServInstID });
                
            UI_LOGGER.log(logRecord);
        }
    }
    
    public static void storeJ2EEServerProperties(String newServInstID, Project project, 
            EditableProperties projectProps, EditableProperties privateProps, String serverLibraryName) {
        
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        
        if (ProjectProperties.isUsingServerLibrary(projectProps, EarProjectProperties.J2EE_PLATFORM_CLASSPATH)) {         
            if (serverLibraryName != null) {
                projectProps.setProperty(J2EE_PLATFORM_CLASSPATH,
                    "${libs." + serverLibraryName + "." + "classpath" + "}"); //NOI18N
                projectProps.setProperty(APPCLIENT_TOOL_RUNTIME,
                    "${libs." + serverLibraryName + "." + "appclient" + "}");
            }
        } else {
            String classpath = EarProjectGenerator.toClasspathString(j2eePlatform.getClasspathEntries());
            privateProps.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);
            
            // update j2ee.appclient.tool.runtime
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_APP_CLIENT_RUNTIME)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
                privateProps.setProperty(APPCLIENT_TOOL_RUNTIME, EarProjectGenerator.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(APPCLIENT_TOOL_RUNTIME);
            }
        }
        
        // update j2ee.server.type
        projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);

        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            projectProps.setProperty(APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
            projectProps.remove(CLIENT_NAME);
        } else if ((mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, CLIENT_NAME)) != null) {
            projectProps.setProperty(CLIENT_NAME, mainClassArgs);
            projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
        } else {
            projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
            projectProps.remove(CLIENT_NAME);
        }
        setAppClientPrivateProperties(j2eePlatform, newServInstID, privateProps);
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(project.getProjectDirectory());
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ANT_DEPLOY_BUILD_SCRIPT), J2eeModule.WAR, newServInstID); // NOI18N
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File antDeployPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(newServInstID);
        if (antDeployPropsFile == null) {
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
        } else {
            privateProps.setProperty(DEPLOY_ANT_PROPS_FILE, antDeployPropsFile.getAbsolutePath());
        }
    }
    
    
    /** <strong>Package private for unit test only</strong>. */
    void updateContentDependency(List<ClassPathSupport.Item> oldContent, List<ClassPathSupport.Item> newContent,
            EditableProperties props) {
        Application app = project.getAppModule().getApplication();
        
        Set<ClassPathSupport.Item> deleted = new HashSet<ClassPathSupport.Item>(oldContent);
        deleted.removeAll(newContent);
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newContent);
        added.removeAll(oldContent);
        
        boolean saveNeeded = false;
        // delete the old entries out of the application
        for (ClassPathSupport.Item item : deleted) {
            removeItemFromAppDD(app,item, props);
            saveNeeded = true;
        }
        // add the new stuff "back"
        for (ClassPathSupport.Item item : added) {
            addItemToAppDD(project, app,item);
            saveNeeded = true;
        }
        
        if (saveNeeded && EarProjectUtil.isDDWritable(project)) {
            try {
                app.write(project.getAppModule().getDeploymentDescriptor());
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            }
        }
    }
    
    private void removeItemFromAppDD(final Application dd,
            final ClassPathSupport.Item item, EditableProperties props) {
        String pathInEAR = getCompletePathInArchive(project, item);
        Module m = searchForModule(dd, pathInEAR);
        if (null != m) {
            dd.removeModule(m);
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                AntArtifact aa = item.getArtifact();
                Project p = aa.getProject();
                // update clientModule / appCLient properties:
                ApplicationUrisComboBoxModel.moduleWasRemove(p, props);
                J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        project.getAppModule().removeModuleProvider(jmp, pathInEAR);
                    }
                }
            }
        }
    }
    
    private static Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods) {
            len = mods.length;
        }
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getConnector();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getJava();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w) {
                val = w.getWebUri();
            }
            if (null != val && val.equals(path)) {
                return mods[i];
            }
        }
        return null;
    }
    
    public static void addItemToAppDD(EarProject project, Application dd, ClassPathSupport.Item item) {
        String path = getCompletePathInArchive(project, item);
        Module mod = null;
        if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
            mod = getModFromAntArtifact(project, item.getArtifact(), dd, path);
            // TODO: init clientModule / appCLient here
        } else if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
           mod = getModFromFile(item.getResolvedFile(), dd, path);
        }
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod) {
            dd.addModule(mod);
        }
    }
    
    
    private static Module getModFromAntArtifact(EarProject project, AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                String serverInstanceId = project.getServerInstanceID();
                if (serverInstanceId != null) {
                    jmp.setServerInstanceID(serverInstanceId);
                }
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    project.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean(Application.MODULE);
                if (jm.getModuleType() == J2eeModule.EJB) {
                    mod.setEjb(path); // NOI18N
                } else if (jm.getModuleType() == J2eeModule.WAR) {
                    Web w = mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    FileObject tmp = aa.getScriptFile();
                    if (null != tmp) {
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    }
                    WebModule wm = null;
                    if (null != tmp) {
                        wm = WebModule.getWebModule(tmp);
                    }
                    String contextPath = null;
                    if (null != wm) {
                        contextPath = wm.getContextPath();
                    } 
                    if (contextPath == null) {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        contextPath = path.substring(0,endex);
                    }
                    w.setContextRoot(contextPath);
                    mod.setWeb(w);
                } else if (jm.getModuleType() == J2eeModule.CONN) {
                    mod.setConnector(path);
                } else if (jm.getModuleType() == J2eeModule.CLIENT) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            Exceptions.printStackTrace(cnfe);
        }
        return mod;
    }
    
    private static Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile(f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    Web w = mod.newWeb(); 
                    w.setWebUri(path);
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot("/"+path.substring(0,endex)); // NOI18N
                    mod.setWeb(w);
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application.xml"); //NOI18N
                if (null != ddf) {
                    return null;
                }
            } catch (ClassNotFoundException cnfe) {
                Logger.getLogger("global").log(Level.INFO, cnfe.getLocalizedMessage());
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            } finally {
                try {
                    if (null != jar) {
                        jar.close();
                    }
                } catch (IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }
    
    public static List<ClassPathSupport.Item> getJarContentAdditional(final EarProject project) {
        EditableProperties ep = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return project.getClassPathSupport().itemsList(
                ep.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
    }
    
    /**
     * Acquires modules form the earproject's metadata (properties files).
     */
    public static Map<String, J2eeModuleProvider> getModuleMap(EarProject project) {
        Map<String, J2eeModuleProvider> mods = new HashMap<String, J2eeModuleProvider>();
        for (ClassPathSupport.Item item : getJarContentAdditional(project)) {
            Project p;
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                AntArtifact aa = item.getArtifact();
                p = aa.getProject();
            } else {
                continue;
            }
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    String path = item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
                    mods.put(path, jmp);
                }
            }
        }
        return mods; // project.getAppModule().setModules(mods);
    }


    public static void addJ2eeSubprojects(final EarProject project, final Project[] moduleProjects) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    List<ClassPathSupport.Item> l = project.getClassPathSupport().itemsList(
                            ep.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                    for (int i = 0; i < moduleProjects.length; i++) {
                        AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                                moduleProjects[i],
                                EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE); //the artifact type is the some for both ejb and war projects
                        for (AntArtifact artifact : artifacts) {
                            ClassPathSupport.Item item = ClassPathSupport.Item.create(artifact, artifact.getArtifactLocations()[0], null);
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, "/"); // NOI18N
                            l.add(item);
                        }
                    }
                    String[] newValue = project.getClassPathSupport().encodeToStrings(l, TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                    ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.setProperty(JAR_CONTENT_ADDITIONAL, newValue);
                    project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }

    /**
     * @see #getApplicationSubprojects(Object)
     */
    static List<Project> getApplicationSubprojects(EarProject p) {
        return getApplicationSubprojects(p, null);
    }

    /**
     * Acquires modules (in the form of projects) from "JAVA EE Modules" not from the deployment descriptor (application.xml).
     * <p>
     * The reason is that for JAVA EE 5 the deployment descriptor is not compulsory.
     * @param moduleType the type of module, see {@link J2eeModule J2eeModule constants}. 
     *                   If it is <code>null</code> then all modules are returned.
     * @return list of EAR project subprojects.
     */
    static List<Project> getApplicationSubprojects(EarProject p, Object moduleType) {
        List<ClassPathSupport.Item> items = getJarContentAdditional(p);
        List<Project> projects = new ArrayList<Project>(items.size());
        for (ClassPathSupport.Item item : items) {
            if (item.getType() != ClassPathSupport.Item.TYPE_ARTIFACT || item.getArtifact() == null) {
                continue;
            }
            Project vcpiProject = item.getArtifact().getProject();
            J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (jmp == null) {
                continue;
            }
            if (moduleType == null) {
                projects.add(vcpiProject);
            } else if (moduleType.equals(jmp.getJ2eeModule().getModuleType())) {
                projects.add(vcpiProject);
            }
        }
        return projects;
    }
    
    static public List getSortedSubprojectsList(EarProject project) {
        List<Project> subprojects = new ArrayList<Project>();
        addSubprojects( project, subprojects ); // Find the projects recursively
        String[] displayNames = new String[subprojects.size()];
         
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            displayNames[i] = ProjectUtils.getInformation(subprojects.get(i)).getDisplayName();
        }

        Arrays.sort(displayNames, Collator.getInstance());
        return Arrays.asList(displayNames);
    }
    
    /** Gets all subprojects recursively
     */
    static private void addSubprojects( Project project, List<Project> result ) {
        SubprojectProvider spp = project.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator/*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project sp = (Project) it.next();
            if (ProjectUtils.hasSubprojectCycles(project, sp)) {
                Logger.getLogger("global").log(Level.WARNING, "There would be cyclic " + // NOI18N
                        "dependencies if the " + sp + " would be added. Skipping..."); // NOI18N
                continue;
            }
            if ( !result.contains( sp ) ) {
                result.add( sp );
            }
            addSubprojects( sp, result );            
        }
    }

    /**
     * Transforms all the Objects from GUI controls into String Ant properties
     * and stores them in the project.
     */
    public void store() {
        try {
            // Store properties
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    saveLibrariesLocation();
                    URL buildImplXSL = EarProject.class.getResource("resources/build-impl.xsl");
                    int state = genFilesHelper.getBuildScriptState(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH, buildImplXSL);
                    if ((state & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) {
                        if (showModifiedMessage(NbBundle.getMessage(EarProjectProperties.class,"TXT_ModifiedTitle"))) {
                            //Delete user modified build-impl.xml
                            FileObject fo = updateHelper.getAntProjectHelper().getProjectDirectory().
                                    getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                            if (fo != null) {
                                fo.delete();
                                genFilesHelper.refreshBuildScript(
                                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                        buildImplXSL,
                                        false);
                            }
                        } else {
                            return false;
                        }
                    }
                    storeProperties();
                    return true;
                }
            });
            // and save the project
            if (result) {
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static void setAppClientPrivateProperties(final J2eePlatform j2eePlatform,
            final String serverInstanceID, final EditableProperties ep) {
        // XXX rather hotfix for #75518. Get rid of it with fixing or #75574
        if (!j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.CLIENT)) {
            return;
        }
        String mainClass = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (mainClass != null) {
            ep.setProperty(APPCLIENT_TOOL_MAINCLASS, mainClass);
        }
        
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            ep.setProperty(APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            ep.setProperty(APPCLIENT_TOOL_ARGS, args);
        }    
        
        //WORKAROUND for --retrieve option in asadmin deploy command
        //works only for local domains
        //see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        File asRoot = j2eePlatform.getPlatformRoots()[0];
        InstanceProperties ip = InstanceProperties.getInstanceProperties(serverInstanceID);
        //check if we have AS
        if (ip != null && new File(asRoot, "lib/admin-cli.jar").exists()) { // NOI18N
            File exFile = new File(asRoot, "lib/javaee.jar"); // NOI18N
            if (exFile.exists()) {
                ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/generated/xml/j2ee-apps").getAbsolutePath()); // NOI18N
            } else {
                ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/applications/j2ee-apps").getAbsolutePath()); // NOI18N
            }
        } else {
            ep.remove(APPCLIENT_WA_COPY_CLIENT_JAR_FROM);
        }
        
    }
    
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(DEBUG_CLASSPATH), null));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(JAR_CONTENT_ADDITIONAL), null));

        Set newArtifacts = new HashSet();
        newArtifacts.addAll(ClassPathUiSupport.getList( DEBUG_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));

        updateContentDependency(
            cs.itemsList(projectProperties.get(JAR_CONTENT_ADDITIONAL)), 
            ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()),
            projectProperties);
        
        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set added = new HashSet(newArtifacts);
        added.removeAll(oldArtifacts);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)it.next();
            if ( item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT ||
                    item.getType() == ClassPathSupport.Item.TYPE_JAR ) {
                refHelper.destroyReference(item.getReference());
                if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                    //oh well, how do I do this otherwise??
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    if (item.getJavadocProperty() != null) {
                        ep.remove(item.getJavadocProperty());
                    }
                    if (item.getSourceProperty() != null) {
                        ep.remove(item.getSourceProperty());
                    }
                    updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                }
            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        boolean changed = false;
        
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)it.next();
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = item.getReference();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        if (changed) {
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
    
    
    private static boolean showModifiedMessage(final String title) {
        String message = NbBundle.getMessage(EarProjectProperties.class,"TXT_Regenerate");
        JButton regenerateButton = new JButton(NbBundle.getMessage(EarProjectProperties.class,"CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EarProjectProperties.class,"AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }

    public static String getCompletePathInArchive(EarProject project, ClassPathSupport.Item item) {
        String full = "";
        if (item.getReference() == null) {
            switch (item.getType()) {
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    full = item.getArtifact().getArtifactLocations()[0].getPath();
                    break;
                case ClassPathSupport.Item.TYPE_JAR:
                    full = item.getResolvedFile().getPath();
                    break;
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    full = item.getLibrary().getName();
                    break;
            }
        } else {
            full = project.evaluator().evaluate(item.getReference());
        }
        int lastSlash = full.lastIndexOf('/'); // NOI18N
        String trimmed = null;
        trimmed = (lastSlash != -1) ? full.substring(lastSlash+1) : full;
        String path = item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
        return (null != path && path.length() > 1)
                ? path + '/' + trimmed : trimmed; // NOI18N
    }

    public EarProject getProject() {
        return project;
    }
    
    public void removeAdditionalContentItem(ClassPathSupport.Item item) {
        EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel().removeElement(item);
    }
    
}
