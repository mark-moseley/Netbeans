/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.*;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.web.project.ProjectWebModule;

import org.netbeans.modules.web.project.SourceRoots;
import org.netbeans.modules.web.project.WebSources;
import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.web.project.WebProjectType;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_WAR = "dist.war"; //NOI18N
    public static final String DIST_WAR_EAR = "dist.ear.war"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
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
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_WEB_DIR = "build.web.dir"; //NOI18N
    public static final String BUILD_EAR_WEB_DIR = "build.ear.web.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String BUILD_WEB_EXCLUDES = "build.web.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N

    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
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
    
    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N
    
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    private static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N

    // Well known paths
    public static final String[] WELL_KNOWN_PATHS = new String[] {            
            "${" + JAVAC_CLASSPATH + "}", 
            "${" + JAVAC_TEST_CLASSPATH  + "}", 
            "${" + RUN_TEST_CLASSPATH  + "}", 
            "${" + BUILD_CLASSES_DIR  + "}", 
            "${" + BUILD_TEST_CLASSES_DIR  + "}", 
    };
    
    // Prefixes and suffixes of classpath
    public static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    public static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    ClassPathSupport cs;

    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document WEB_DOCBASE_DIR_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;

    // CustomizerLibraries
    ClassPathUiSupport.ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    WebClassPathUi.ClassPathTableCellItemRenderer CLASS_PATH_TABLE_ITEM_RENDERER;

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

    // CustomizerRun
    Document J2EE_PLATFORM_MODEL;
    Document CONTEXT_PATH_MODEL;
    Document LAUNCH_URL_RELATIVE_MODEL;
    ButtonModel DISPLAY_BROWSER_MODEL; 
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL; 

    // Private fields ----------------------------------------------------------
    private WebProject project;
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;

    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Properties additionalProperties;

    public WebProjectProperties(WebProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        
        this.cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getAntProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX);
                
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
        
        CLASS_PATH_LIST_RENDERER = new WebClassPathUi.ClassPathListCellRenderer( evaluator );
        CLASS_PATH_TABLE_ITEM_RENDERER = new WebClassPathUi.ClassPathTableCellItemRenderer( evaluator );
        
        // CustomizerSources
        SOURCE_ROOTS_MODEL = WebSourceRootsUi.createModel( project.getSourceRoots() );
        TEST_ROOTS_MODEL = WebSourceRootsUi.createModel( project.getTestSourceRoots() );
        WEB_DOCBASE_DIR_MODEL = projectGroup.createStringDocument( evaluator, WEB_DOCBASE_DIR );

        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );

        JAVAC_CLASSPATH_MODEL = ClassPathUiSupport.createTableModel( cs.itemsIterator( (String)projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES) );
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( JAVAC_TEST_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( RUN_TEST_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel (PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE));
        
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
        WAR_CONTENT_ADDITIONAL_MODEL = WarIncludesUiSupport.createTableModel( cs.itemsList( (String)projectProperties.get( WAR_CONTENT_ADDITIONAL ), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));

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
        
        // CustomizerRun
        J2EE_PLATFORM_MODEL = projectGroup.createStringDocument(evaluator, J2EE_PLATFORM);
        CONTEXT_PATH_MODEL = projectGroup.createStringDocument(evaluator, CONTEXT_PATH);
        LAUNCH_URL_RELATIVE_MODEL = projectGroup.createStringDocument(evaluator, LAUNCH_URL_RELATIVE);
        DISPLAY_BROWSER_MODEL = projectGroup.createToggleButtonModel(evaluator, DISPLAY_BROWSER);
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(privateProperties.getProperty( J2EE_SERVER_INSTANCE ));

    }

    public void save() {
        try {
            // Store properties 
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    storeProperties();
                    return null;
                }
            });
            // and save the project        
            ProjectManager.getDefault().saveProject(project);
            
            //temporary fix for issue #54454 - deadlock when upgrading project.xml
            WebSources ws = (WebSources) project.getLookup().lookup(WebSources.class);
            ws.fireChange();
        } 
        catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        catch ( IOException ex ) {
            ErrorManager.getDefault().notify( ex );
        }
    }

    private void storeProperties() throws IOException {
        // Store special properties
        
        // Modify the project dependencies properly        
        resolveProjectDependencies();
        
        // Encode all paths (this may change the project properties)
        String[] javac_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES  );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( RUN_TEST_CLASSPATH_MODEL ), null );
                
        String[] war_includes = cs.encodeToStrings( WarIncludesUiSupport.getIterator( WAR_CONTENT_ADDITIONAL_MODEL ), ClassPathSupport.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES  );

        // Store source roots
        storeRoots( project.getSourceRoots(), SOURCE_ROOTS_MODEL );
        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );
                
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
                
        // Save all paths
        projectProperties.setProperty( JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( RUN_TEST_CLASSPATH, run_test_cp );
        
        projectProperties.setProperty( WAR_CONTENT_ADDITIONAL, war_includes );
        
        //Handle platform selection
        SpecificationVersion sourceLevel = (SpecificationVersion) JAVAC_SOURCE_MODEL.getSelectedItem();
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, (String) PLATFORM_MODEL.getSelectedItem(), sourceLevel);

        //Save javac.source
        if (sourceLevel!=null) {
            //Not broken platform
            projectProperties.setProperty(JAVAC_SOURCE, sourceLevel.toString());
        }

        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }
        
        // Set new server instance ID
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            setNewServerInstanceValue(J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()), project, projectProperties, privateProperties);
        }
        
        // Set new context path
        try {
            setNewContextPathValue(CONTEXT_PATH_MODEL.getText(0, CONTEXT_PATH_MODEL.getLength()), project, projectProperties, privateProperties);
        } catch (BadLocationException exc) {
            //PENDING
        }

        storeAdditionalProperties(projectProperties);
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );        
        
    }
    
    private void storeAdditionalProperties(EditableProperties projectProperties) {
        for (Iterator i = additionalProperties.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            projectProperties.put(key, additionalProperties.get(key));
        }
    }

    private static String getDocumentText( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        }
        catch( BadLocationException e ) {
            return ""; // NOI18N
        }
    }

    
    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */    
    public static String getAntPropertyName( String property ) {
        if ( property != null && 
             property.startsWith( "${" ) && // NOI18N
             property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 ); 
        }
        else {
            return property;
        }
    }
    
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
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.Item.PATH_IN_WAR_LIB ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( JAVAC_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( RUN_TEST_CLASSPATH ), null ) );

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
        File projDir = FileUtil.toFile(updateHelper.getAntProjectHelper().getProjectDirectory());
        for( Iterator it = added.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)it.next();
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // add property to project.properties pointing to relativized 
                // library jar(s) if possible
                String prop = cs.getLibraryReference( item );
                prop = prop.substring(2, prop.length()-1); // XXX make a PropertyUtils method for this!
                String value = relativizeLibraryClasspath(prop, projDir);
                if (value != null) {
                    ep.setProperty(prop, value);
                    ep.setComment(prop, new String[]{
                        // XXX this should be I18N! Not least because the English is wrong...
                        "# Property "+prop+" is set here just to make sharing of project simpler.",
                        "# The library definition has always preference over this property."}, false);
                    changed = true;
                }
            }
        }
        if (changed) {
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
        
    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param property library property name ala "libs.someLib.classpath"
     * @param projectDir project dir for relativization
     * @return relativized library classpath or null if some jar is not collocated
     */
    private String relativizeLibraryClasspath(String property, File projectDir) {
        String value = PropertyUtils.getGlobalProperties().getProperty(property);
        // bugfix #42852, check if the classpath property is set, otherwise return null
        if (value == null) {
            return null;
        }
        String[] paths = PropertyUtils.tokenizePath(value);
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<paths.length; i++) {
            File f = updateHelper.getAntProjectHelper().resolveFile(paths[i]);
            if (CollocationQuery.areCollocated(f, projectDir)) {
                sb.append(PropertyUtils.relativizeFile(projectDir, f));
            } else {
                return null;
            }
            if (i+1<paths.length) {
                sb.append(File.pathSeparatorChar);
            }
        }
        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }
    
    private void storeRoots( SourceRoots roots, DefaultTableModel tableModel ) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String []rootLabels = new String[data.size()];
        for (int i=0; i<data.size();i++) {
            rootURLs[i] = ((File)((Vector)data.elementAt(i)).elementAt(0)).toURI().toURL();
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
    
    public void put( String propertyName, Object value ) {
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        projectProperties.put(propertyName, value);
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            projectProperties.put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }

    public void store() {
        save();
    }
    
    /**
     * TODO: AB: temporary fix for #54544. We need a way to set properties
     * without resorting to WPP.
     */
    public void setServerInstance(String serverInstanceID) {
        J2eePlatformUiSupport.setSelectedPlatform(J2EE_SERVER_INSTANCE_MODEL, serverInstanceID);
    }

    /* This is used by CustomizerWSServiceHost */
    void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.setProperty(propertyName, propertyValue);
    }
    
    private static void setNewServerInstanceValue(String newServInstID, Project project, EditableProperties projectProps, EditableProperties privateProps) {
        // update j2ee.platform.classpath
        String oldServInstID = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        if (oldServInstID != null) {
            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
            if (oldJ2eePlatform != null) {
                ((WebProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        ((WebProject) project).registerJ2eePlatformListener(j2eePlatform);
        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
        privateProps.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);

        // update j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(WebServicesConstants.WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(WebServicesConstants.WSCOMPILE);
            privateProps.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                    Utils.toClasspathString(wsClasspath));
        } else {
            privateProps.remove(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
        }
        
        // update j2ee.server.type
        projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
    }
    
    private static void setNewContextPathValue(String contextPath, Project project, EditableProperties projectProps, EditableProperties privateProps) {
        if (contextPath == null || contextPath.length() == 0)
            return;

        ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
        String serverInstId = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        wm.setContextPath(serverInstId, contextPath);
        projectProps.setProperty(CONTEXT_PATH, contextPath);
    }
}
