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

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.web.project.ProjectWebModule;

import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.filesystems.FileUtil;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.J2eePlatformUiSupport;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.web.project.UpdateProjectImpl;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.web.project.WebProjectUtil;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.WebProjectType;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;

import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman
 */
public class WebProjectProperties {
    
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    public static final String J2EE_1_3 = "1.3"; // NOI18N
    
    // Special properties of the project
    public static final String WEB_PROJECT_NAME = "web.project.name"; //NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; //NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; //NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_WAR = "dist.war"; //NOI18N
    public static final String DIST_WAR_EAR = "dist.ear.war"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String JSPCOMPILATION_CLASSPATH = "jspcompilation.classpath";     //NOI18N

    public static final String WAR_NAME = "war.name"; //NOI18N
    public static final String WAR_EAR_NAME = "war.ear.name"; //NOI18N
    public static final String WAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String WAR_CONTENT_ADDITIONAL = "war.content.additional"; //NOI18N

    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String CONTEXT_PATH = "context.path"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String SRC_DIR = "src.dir"; //NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; //NOI18N
    public static final String CONF_DIR = "conf.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEBINF_DIR = "webinf.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_WEB_DIR = "build.web.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String BUILD_WEB_EXCLUDES = "build.web.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N

    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    public static final String DEBUG_CLIENT = "debug.client"; // NOI18N
    public static final String DEBUG_SERVER = "debug.server"; // NOI18N
    public static final String JSDEBUGGER_AVAILABLE = "debug.client.available"; // NOI18N
    
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
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N

    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N
    
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N

    public static final String WS_DEBUG_CLASSPATHS = "ws.debug.classpaths";     //NOI18N
    public static final String WS_WEB_DOCBASE_DIRS = "ws.web.docbase.dirs"; //NOI18N
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    
    public ClassPathSupport cs;

    //list of frameworks to add to the application
    private List newExtenders;
    
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document WEB_DOCBASE_DIR_MODEL;
    Document WEBINF_DIR_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;

    // CustomizerLibraries
    ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    TableCellRenderer CLASS_PATH_TABLE_ITEM_RENDERER;
    Document SHARED_LIBRARIES_MODEL;

    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL; 
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    ButtonModel COMPILE_JSP_MODEL;
    
    // CustomizerWar
    Document WAR_NAME_MODEL; 
    Document BUILD_CLASSES_EXCLUDES_MODEL;
    ButtonModel WAR_COMPRESS_MODEL;
    WarIncludesUiSupport.ClasspathTableModel WAR_CONTENT_ADDITIONAL_MODEL;

    // CustomizerJavadoc
    ButtonModel JAVADOC_PRIVATE_MODEL;
    ButtonModel JAVADOC_NO_TREE_MODEL;
    ButtonModel JAVADOC_USE_MODEL;
    ButtonModel JAVADOC_NO_NAVBAR_MODEL; 
    ButtonModel JAVADOC_NO_INDEX_MODEL; 
    ButtonModel JAVADOC_SPLIT_INDEX_MODEL; 
    ButtonModel JAVADOC_AUTHOR_MODEL; 
    ButtonModel JAVADOC_VERSION_MODEL;
    Document JAVADOC_WINDOW_TITLE_MODEL;
    ButtonModel JAVADOC_PREVIEW_MODEL; 
    Document JAVADOC_ADDITIONALPARAM_MODEL;

    // CustomizerRun
    Document J2EE_PLATFORM_MODEL;
    Document CONTEXT_PATH_MODEL;
    Document LAUNCH_URL_RELATIVE_MODEL;
    ButtonModel DISPLAY_BROWSER_MODEL; 
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL; 

    // CustomizerDebug
    ButtonModel DEBUG_SERVER_MODEL;
    ButtonModel DEBUG_CLIENT_MODEL;
    
    // for ui logging added frameworks
    private List<String> addedFrameworkNames;

    // Private fields ----------------------------------------------------------
    private WebProject project;
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;

    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Properties additionalProperties;

    private static boolean needsUpdate = false;
    
    private static String serverId;
    private static String cp;

    public static final String JAVA_SOURCE_BASED= "java.source.based";
    
    
    public WebProjectProperties(WebProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        
        //this is called from updatehelper when user confirms the project update
        project.getUpdateImplementation().setProjectUpdateListener(new UpdateProjectImpl.ProjectUpdateListener() {
            public void projectUpdated() {
                needsUpdate = true;
            }
        });
        
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        
        this.cs = new ClassPathSupport( evaluator, refHelper, 
                updateHelper.getAntProjectHelper(), updateHelper, 
                new ClassPathSupportCallbackImpl(updateHelper.getAntProjectHelper()));
                
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new Properties();

        init(); // Load known properties        
    }
    
    WebProject getProject() {
        return project;
    }

    /** Initializes the visual models 
     */
    private void init() {
        
        CLASS_PATH_LIST_RENDERER = ProjectProperties.createClassPathListRendered(evaluator, project.getProjectDirectory());
        CLASS_PATH_TABLE_ITEM_RENDERER = ProjectProperties.createClassPathTableRendered(evaluator, project.getProjectDirectory());
        
        // CustomizerSources
        SOURCE_ROOTS_MODEL = WebSourceRootsUi.createModel( project.getSourceRoots() );
        TEST_ROOTS_MODEL = WebSourceRootsUi.createModel( project.getTestSourceRoots() );
        WEB_DOCBASE_DIR_MODEL = projectGroup.createStringDocument( evaluator, WEB_DOCBASE_DIR );
        WEBINF_DIR_MODEL = projectGroup.createStringDocument( evaluator, WEBINF_DIR );

        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );

        JAVAC_CLASSPATH_MODEL = ClassPathTableModel.createTableModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES) );
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        SpecificationVersion minimalSourceLevel = null;
        if (evaluator.getProperty(J2EE_PLATFORM).equals(J2eeModule.JAVA_EE_5)) {
            minimalSourceLevel = new SpecificationVersion(J2eeModule.JAVA_EE_5);
        }
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel (PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE), evaluator.getProperty(JAVAC_TARGET), minimalSourceLevel);
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer ();
        SHARED_LIBRARIES_MODEL = new PlainDocument(); 
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getAntProjectHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // CustomizerCompile
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVAC_DEPRECATION );
        JAVAC_DEBUG_MODEL = privateGroup.createToggleButtonModel( evaluator, JAVAC_DEBUG );
        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );
        COMPILE_JSP_MODEL = projectGroup.createToggleButtonModel( evaluator, COMPILE_JSPS );
        
        // CustomizerWar
        WAR_NAME_MODEL = projectGroup.createStringDocument( evaluator, WAR_NAME );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        WAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, WAR_COMPRESS );
        WAR_CONTENT_ADDITIONAL_MODEL = WarIncludesUiSupport.createTableModel( cs.itemsList( (String)projectProperties.get( WAR_CONTENT_ADDITIONAL ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));

        // CustomizerJavadoc
        JAVADOC_PRIVATE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_PRIVATE );
        JAVADOC_NO_TREE_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_TREE );
        JAVADOC_USE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_USE );
        JAVADOC_NO_NAVBAR_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_NAVBAR );
        JAVADOC_NO_INDEX_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_INDEX ); 
        JAVADOC_SPLIT_INDEX_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_SPLIT_INDEX );
        JAVADOC_AUTHOR_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_AUTHOR );
        JAVADOC_VERSION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_VERSION );
        JAVADOC_WINDOW_TITLE_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_WINDOW_TITLE );
        JAVADOC_PREVIEW_MODEL = privateGroup.createToggleButtonModel( evaluator, JAVADOC_PREVIEW );
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );
        
        // CustomizerRun
        J2EE_PLATFORM_MODEL = projectGroup.createStringDocument(evaluator, J2EE_PLATFORM);
        LAUNCH_URL_RELATIVE_MODEL = projectGroup.createStringDocument(evaluator, LAUNCH_URL_RELATIVE);
        DISPLAY_BROWSER_MODEL = projectGroup.createToggleButtonModel(evaluator, DISPLAY_BROWSER);
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(privateProperties.getProperty( J2EE_SERVER_INSTANCE ), projectProperties.getProperty(J2EE_PLATFORM));
        try {
            CONTEXT_PATH_MODEL = new PlainDocument();
            CONTEXT_PATH_MODEL.remove(0, CONTEXT_PATH_MODEL.getLength());
            ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
            String contextPath = wm.getContextPath();
            if (contextPath != null) {
                CONTEXT_PATH_MODEL.insertString(0, contextPath, null);
            }
        } catch (BadLocationException exc) {
            //ignore
        }
        
        // CustomizerDebug
        DEBUG_CLIENT_MODEL = projectGroup.createToggleButtonModel(evaluator, DEBUG_CLIENT);
        DEBUG_SERVER_MODEL = projectGroup.createToggleButtonModel(evaluator, DEBUG_SERVER);
    }

    public void save() {
        try {
            saveLibrariesLocation();
            // Store properties 
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    storeProperties();
                    return null;
                }
            });
            // and save the project        
            ProjectManager.getDefault().saveProject(project);
            
            // extend project with selected frameworks
            // It should be called outside mutex, which is used above. See issue#68118
            if (newExtenders != null) {
                // #120108
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        for (int i = 0; i < newExtenders.size(); i++) {
                            ((WebModuleExtender) newExtenders.get(i)).extend(project.getAPIWebModule());
                        }
                        newExtenders.clear();
                        project.resetTemplates();
                        
                        // ui logging of the added frameworks
                        if ((addedFrameworkNames != null) && (addedFrameworkNames.size() > 0)) {
                            Utils.logUI(NbBundle.getBundle(WebProjectProperties.class),"UI_WEB_PROJECT_FRAMEWORK_ADDED", // NOI18N
                                    addedFrameworkNames.toArray());
                        }
                    }
                });
            }
            
            //prevent deadlock reported in the issue #54643
            //cp and serverId values are read in setNewContextPathValue() method which is called from storeProperties() before this code
            //it is easier to preset them instead of reading them here again
            if (cp != null) {
                ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
                String oldCP = wm.getContextPath(serverId);
                if (!cp.equals(oldCP))
                    wm.setContextPath(serverId, cp);
            }
        } 
        catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
        catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
        
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
       
        // Store source roots
        storeRoots( project.getSourceRoots(), SOURCE_ROOTS_MODEL );
        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );

        //test whether user wants to update his project to newest version
        if(needsUpdate) {
            //remove servlet24 and jsp20 libraries (they are not used in 4.1)
            ClassPathTableModel cptm = getJavaClassPathModel();

            ArrayList cpItemsToRemove = new ArrayList();
            for(int i = 0; i < cptm.getRowCount(); i++) {
                Object item = cptm.getValueAt(i,0);
                if (item instanceof ClassPathSupport.Item) {
                    ClassPathSupport.Item cpti = (ClassPathSupport.Item)item;
                    String propertyName = cpti.getReference();
                    if(propertyName != null) {
                        String libname = propertyName.substring("${libs.".length());
                        if(libname.indexOf(".classpath}") != -1) libname = libname.substring(0, libname.indexOf(".classpath}"));
                                
                        if("servlet24".equals(libname) || "jsp20".equals(libname)) { //NOI18N
                            cpItemsToRemove.add(cpti);
                        }
                    }
                }
            } 
            
            //remove selected libraries
            Iterator remove = cpItemsToRemove.iterator();
            while(remove.hasNext()) {
                ClassPathSupport.Item cpti = (ClassPathSupport.Item)remove.next();
                cptm.getDefaultListModel().removeElement(cpti);
            }
            
            //commented out, one more check follows
            //needsUpdate = false;
        }
        
        // Encode all paths (this may change the project properties)
        List<ClassPathSupport.Item> javaClasspathList = ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel());
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            final String instanceId = J2eePlatformUiSupport.getServerInstanceID(
                    J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            final String oldServInstID = project.getAntProjectHelper().getProperties(
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(J2EE_SERVER_INSTANCE);

            SharabilityUtility.switchServerLibrary(instanceId, oldServInstID, javaClasspathList, updateHelper);
        }
        
        String[] javac_cp = cs.encodeToStrings(javaClasspathList, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES  );        
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ), null );
        String[] war_includes = cs.encodeToStrings( WarIncludesUiSupport.getList( WAR_CONTENT_ADDITIONAL_MODEL ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES  );

        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        // Assure inegrity which can't shound not be assured in UI
        if ( !JAVADOC_NO_INDEX_MODEL.isSelected() ) {
            JAVADOC_SPLIT_INDEX_MODEL.setSelected( false ); // Can't split non existing index
        }
                                
        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );

        //test whether user wants to update his project to newest version
        if(needsUpdate) {
            //add items for test classpath (they are not used in 4.1)
            javac_test_cp = new String[] {
                "${javac.classpath}:", // NOI18N
                "${build.classes.dir}:", // NOI18N
                "${libs.junit.classpath}:", // NOI18N
                "${libs.junit_4.classpath}", // NOI18N
            };
            run_test_cp = new String[] {
                "${javac.test.classpath}:", // NOI18N
                "${build.test.classes.dir}", // NOI18N
            };
            projectProperties.setProperty(DEBUG_TEST_CLASSPATH, new String[] {
                "${run.test.classpath}", // NOI18N
            });
            
            needsUpdate = false;
        }
        
        // Save all paths
        projectProperties.setProperty( ProjectProperties.JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp );
        
        projectProperties.setProperty( WAR_CONTENT_ADDITIONAL, war_includes );
        
        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, PLATFORM_MODEL.getSelectedItem(), JAVAC_SOURCE_MODEL.getSelectedItem());

        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }

        // Configure new server instance
        boolean serverLibUsed = ProjectProperties.isUsingServerLibrary(projectProperties,
                WebProjectProperties.J2EE_PLATFORM_CLASSPATH);

        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            final String instanceId = J2eePlatformUiSupport.getServerInstanceID(
                    J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            setNewServerInstanceValue(instanceId, project, projectProperties, privateProperties, !serverLibUsed);
        }

        // Configure server libraries (if any)
        boolean configured = setServerClasspathProperties(projectProperties, privateProperties,
                cs, javaClasspathList);

        // Configure classpath from server (no server libraries)
        if (!configured && J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            setNewServerInstanceValue(J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()),
                    project, projectProperties, privateProperties, true);
        }
        
        // Set new context path
        try {
	    String cp = CONTEXT_PATH_MODEL.getText(0, CONTEXT_PATH_MODEL.getLength());
	    if (cp == null) {
		cp = "/" + PropertyUtils.getUsablePropertyName(project.getName()); //NOI18N
            } else if (!isCorrectCP(cp)) {
		if (cp.startsWith("/")) //NOI18N
		    cp = cp.substring(1);
		cp = "/" + PropertyUtils.getUsablePropertyName(cp); //NOI18N
	    }
	    
            setNewContextPathValue(cp, project, projectProperties, privateProperties);
        } catch (BadLocationException exc) {
            //ignore
        }

        storeAdditionalProperties(projectProperties);
        
        List<ClassPathSupport.Item> libs = new ArrayList<ClassPathSupport.Item>();
        libs.addAll(ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel()));
        libs.addAll(WarIncludesUiSupport.getList(WAR_CONTENT_ADDITIONAL_MODEL));
        
        ProjectProperties.storeLibrariesLocations (project.getAntProjectHelper(), libs.iterator(), 
                project.getAntProjectHelper().isSharableProject() ? projectProperties : privateProperties);
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );
        
        String value = (String)additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }

    private static boolean isCorrectCP(String contextPath) {
        if (contextPath.length() == 0) {
            return true;
        } else if (!contextPath.startsWith("/")) { //NOI18N
	    return false;
        } else if (contextPath.endsWith("/")) {     //NOI18N
            return false;
        } else if (contextPath.indexOf("//") >= 0) { //NOI18N
	    return false;
        } else if (contextPath.indexOf(' ') >= 0) {  //NOI18N
	    return false;
        }
	return true;
    }
    
    private void storeAdditionalProperties(EditableProperties projectProperties) {
        for (Iterator i = additionalProperties.keySet().iterator(); i.hasNext();) {
            String key = i.next().toString();
            projectProperties.put(key, additionalProperties.getProperty(key));
        }
    }
    
    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */    
    static boolean isAntProperty (String string) {
        return string != null && string.startsWith( "${" ) && string.endsWith( "}" ); //NOI18N
    }
        
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );

        Set newArtifacts = new HashSet();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
                
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
            
    private void storeRoots( SourceRoots roots, DefaultTableModel tableModel ) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String []rootLabels = new String[data.size()];
        for (int i=0; i<data.size();i++) {
            File f = ((File)((Vector)data.elementAt(i)).elementAt(0));
            rootURLs[i] = WebProjectUtil.getRootURL(f,null);
            rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
        }
        roots.putRoots(rootURLs,rootLabels);
    }

    public Object get(String propertyName) {
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );

        if (J2EE_SERVER_INSTANCE.equals(propertyName))
            return privateProperties.getProperty(J2EE_SERVER_INSTANCE);
        else
            return projectProperties.getProperty(propertyName);
        
//        return evaluator.getProperty(propertyName);
    }
    
    public void put( String propertyName, String value ) {
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        projectProperties.put(propertyName, value);
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            projectProperties.put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }

    public void store() {
        save();
    }
    
    public static void setServerInstance(final Project project, final UpdateHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    boolean serverLibUsed = ProjectProperties.isUsingServerLibrary(projectProps, J2EE_PLATFORM_CLASSPATH);
                    setNewServerInstanceValue(serverInstanceID, project, projectProps,
                            privateProps, !serverLibUsed);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    /* This is used by CustomizerWSServiceHost */
    void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.setProperty(propertyName, propertyValue);
    }
    
    private static void setNewServerInstanceValue(String newServInstID, Project project,
            EditableProperties projectProps, EditableProperties privateProps, boolean setFromServer) {

        assert newServInstID != null : "Server isntance id to set can't be null"; // NOI18N

        // update j2ee.platform.classpath
        String oldServInstID = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        if (oldServInstID != null) {
            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
            if (oldJ2eePlatform != null) {
                ((WebProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            // probably missing server error
            Logger.getLogger("global").log(Level.INFO, "J2EE platform is null."); // NOI18N
            
            // update j2ee.server.type (throws NPE)
            //projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
            
            // update j2ee.server.instance
            privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
            removeServerClasspathProperties(privateProps);
            privateProps.remove(WebServicesConstants.J2EE_PLATFORM_JSR109_SUPPORT);

            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
            return;
        }
        ((WebProject) project).registerJ2eePlatformListener(j2eePlatform);
        if (setFromServer) {
            String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
            privateProps.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);

            // update j2ee.platform.wscompile.classpath
            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
                privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
            }

            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSGEN)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSGEN);
                privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH);
            }

            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT);
                privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH);
            }

            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIT);
                privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH);
            }

            if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP)) {
                File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_JWSDP);
                privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH, 
                        Utils.toClasspathString(wsClasspath));
            } else {
                privateProps.remove(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH);
            }
        }
        // set j2ee.platform.jsr109 support
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109)) { 
            privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_JSR109_SUPPORT, 
                    "true"); //NOI18N
        } else {
            privateProps.remove(WebServicesConstants.J2EE_PLATFORM_JSR109_SUPPORT);
        }
        
        // update j2ee.server.type
        projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
        
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
        // ui log for the server change
        if(newServInstID != null && !newServInstID.equals(oldServInstID)) {
            Utils.logUI(NbBundle.getBundle(WebProjectProperties.class), "UI_WEB_PROJECT_SERVER_CHANGED", // NOI18N
                    new Object[] { Deployment.getDefault().getServerID(oldServInstID),
                        oldServInstID,
                        Deployment.getDefault().getServerID(newServInstID),
                        newServInstID });
        }
    }

    private static void removeServerClasspathProperties(EditableProperties props) {
        props.remove(J2EE_PLATFORM_CLASSPATH);
        props.remove(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
        props.remove(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH);
        props.remove(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH);
        props.remove(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH);
        props.remove(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH);
    }

    private static boolean setServerClasspathProperties(EditableProperties props,
            EditableProperties privateProps, ClassPathSupport cs, Iterable<ClassPathSupport.Item> items) {

        List<ClassPathSupport.Item> serverItems = new ArrayList<ClassPathSupport.Item>();
        for (ClassPathSupport.Item item : items) {
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY
                    && !item.isBroken()
                    && item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                serverItems.add(ClassPathSupport.Item.create(item.getLibrary(), null));
            }
        }

        if (serverItems.isEmpty()) {
            removeServerClasspathProperties(props);
            return false;
        }
        removeServerClasspathProperties(privateProps);

        props.setProperty(J2EE_PLATFORM_CLASSPATH, cs.encodeToStrings(serverItems, null, "classpath")); // NOI18N
        removeReferences(serverItems);
        props.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                cs.encodeToStrings(serverItems, null, "wscompile")); // NOI18N
        removeReferences(serverItems);
        props.setProperty(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH,
                cs.encodeToStrings(serverItems, null, "wsgenerate")); // NOI18N
        removeReferences(serverItems);
        props.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH,
                cs.encodeToStrings(serverItems, null, "wsimport")); // NOI18N
        removeReferences(serverItems);
        props.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIT_CLASSPATH,
                cs.encodeToStrings(serverItems, null, "wsinterop")); // NOI18N
        removeReferences(serverItems);
        props.setProperty(WebServicesConstants.J2EE_PLATFORM_JWSDP_CLASSPATH,
                cs.encodeToStrings(serverItems, null, "wsjwsdp")); // NOI18N
        return true;
    }
    
    private static void removeReferences(Iterable<ClassPathSupport.Item> items) {
        for (ClassPathSupport.Item item : items) {
            item.setReference(null);
        }
    }

    private static void setNewContextPathValue(String contextPath, Project project, EditableProperties projectProps, EditableProperties privateProps) {
        if (contextPath == null)
            return;

        cp = contextPath;    
        serverId = privateProps.getProperty(J2EE_SERVER_INSTANCE);
    }
    
    public ClassPathTableModel getJavaClassPathModel() {
        return JAVAC_CLASSPATH_MODEL;
    }
    
    public void setNewExtenders(List extenders) {
        newExtenders = extenders;
    }
    
    public void setNewFrameworksNames(List<String> names) {
        addedFrameworkNames = names;
    }
}
