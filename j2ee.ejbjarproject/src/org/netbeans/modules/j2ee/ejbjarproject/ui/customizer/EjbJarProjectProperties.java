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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProvider;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 * @author Chris Webster
 * @author Andrei Badea
 */
public class EjbJarProjectProperties {
    
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    public static final String J2EE_1_3 = "1.3"; // NOI18N
    
    // Special properties of the project
    public static final String EJB_PROJECT_NAME = "j2ee.ejbjarproject.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    public static final String DIST_EAR_JAR = "dist.ear.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N

    public static final String JAR_NAME = "jar.name"; // NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; // NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; // NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N

    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N    
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; // NOI18N
    public static final String META_INF = "meta.inf"; // NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; // NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N    
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    public static final String JAVADOC_PRIVATE="javadoc.private"; // NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; // NOI18N
    public static final String JAVADOC_USE="javadoc.use"; // NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; // NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; // NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; // NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; // NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; // NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; // NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; // NOI18N
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N
    
    public static final String META_INF_EXCLUDES="meta.inf.excludes"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N
    
    
    public static final String[] WELL_KNOWN_PATHS = new String[] {
        "${" + JAVAC_CLASSPATH + "}", // NOI18N
        "${" + JAVAC_TEST_CLASSPATH + "}", // NOI18N
        "${" + RUN_TEST_CLASSPATH + "}", // NOI18N
        "${" + BUILD_CLASSES_DIR + "}", // NOI18N
        "${" + BUILD_TEST_CLASSES_DIR + "}" // NOI18N
    };    
   
    // Prefixes and suffixes of classpath
    public static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    public static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
    
    private ClassPathSupport cs;    
    
    
    // SOURCE ROOTS
    // public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    // public static final String TEST_ROOTS = "__virtual_test_roots__";   //NOI18N
    
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
     
    // CustomizerLibraries
    ClassPathUiSupport.ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    
    //DefaultListModel RUN_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    EjbJarClassPathUi.ClassPathTableCellItemRenderer CLASS_PATH_TABLE_ITEM_RENDERER;
    
    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL; 
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    
    // CustomizerCompileTest
                
    // CustomizerJar
    Document DIST_JAR_MODEL; 
    Document BUILD_CLASSES_EXCLUDES_MODEL; 
    ButtonModel JAR_COMPRESS_MODEL;
                
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
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL;
    ComboBoxModel J2EE_PLATFORM_MODEL;

    // CustomizerRunTest
    
    // Private fields ----------------------------------------------------------
    private EjbJarProject project;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    private EjbJarProvider ejbJarProvider;
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Properties additionalProperties;
    
    Project getProject() {
        return project;
    }

    /** Creates a new instance of EjbJarProjectProperties and initializes them */
    public EjbJarProjectProperties(EjbJarProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper ) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        
        cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getAntProjectHelper(), WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX );
        
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new Properties();
        
        init(); // Load known properties
    }
    
    /** Initializes the visual models 
     */
    private void init() {
        
        CLASS_PATH_LIST_RENDERER = new EjbJarClassPathUi.ClassPathListCellRenderer( evaluator );
        CLASS_PATH_TABLE_ITEM_RENDERER = new EjbJarClassPathUi.ClassPathTableCellItemRenderer( evaluator );
        
        // CustomizerSources
        SOURCE_ROOTS_MODEL = EjbJarSourceRootsUi.createModel( project.getSourceRoots() );
        TEST_ROOTS_MODEL = EjbJarSourceRootsUi.createModel( project.getTestSourceRoots() );
                
        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        JAVAC_CLASSPATH_MODEL = ClassPathUiSupport.createTableModel( cs.itemsIterator( (String)projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES ) );
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( JAVAC_TEST_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( RUN_TEST_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel (PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE));
                
        // CustomizerCompile
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVAC_DEPRECATION );
        JAVAC_DEBUG_MODEL = privateGroup.createToggleButtonModel( evaluator, JAVAC_DEBUG );
        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );
        
        // CustomizerJar
        DIST_JAR_MODEL = projectGroup.createStringDocument( evaluator, DIST_JAR );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        JAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        
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
        JAVADOC_ADDITIONALPARAM_MODEL = privateGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );    
        // CustomizerRun
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel( 
            privateProperties.getProperty( J2EE_SERVER_INSTANCE ));
        J2EE_PLATFORM_MODEL = J2eePlatformUiSupport.createSpecVersionComboBoxModel(
            projectProperties.getProperty( J2EE_PLATFORM ));
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
        resolveProjectDependenciesNew();
        
        // Encode all paths (this may change the project properties)
        String[] javac_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES  );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getIterator( RUN_TEST_CLASSPATH_MODEL ), null );
                
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
        
        //Handle platform selection
        SpecificationVersion sourceLevel = (SpecificationVersion) JAVAC_SOURCE_MODEL.getSelectedItem();
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, (String) PLATFORM_MODEL.getSelectedItem(), sourceLevel);
        
        //Save javac.source
        if (JAVAC_SOURCE_MODEL.getSelectedItem()!=null) {
            //Not broken platform
            projectProperties.setProperty(JAVAC_SOURCE, JAVAC_SOURCE_MODEL.getSelectedItem().toString());
        }
                
        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }
        
        // Set new server instance ID
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            setNewServerInstanceValue(J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem()), project, projectProperties, privateProperties);
        }
        
        // Update the deployment descriptor if upgrading from J2EE 1.3 to 1.4 and set the new J2EE spec version
        String oldJ2eeVersion = projectProperties.getProperty(J2EE_PLATFORM);
        String newJ2eeVersion = J2eePlatformUiSupport.getSpecVersion(J2EE_PLATFORM_MODEL.getSelectedItem());
        if (oldJ2eeVersion != null && newJ2eeVersion != null) {
            if (oldJ2eeVersion.equals(J2EE_1_3) && newJ2eeVersion.equals(J2EE_1_4)) {
                EjbJarImplementation ejbJarImplementation = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
                if (ejbJarImplementation != null) {
                    FileObject ddFo = ejbJarImplementation.getDeploymentDescriptor();
                    if (ddFo != null)
                        DDProvider.getDefault().getDDRoot(ddFo).setVersion(new BigDecimal(EjbJar.VERSION_2_1));
                }
            }
            
            // Set the new J2EE spec version 
            projectProperties.setProperty(J2EE_PLATFORM, newJ2eeVersion);
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
    
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */   
    private void resolveProjectDependenciesNew() {
            
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( JAVAC_CLASSPATH ), ClassPathSupport.ELEMENT_INCLUDED_LIBRARIES ) );
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
                        "# Property "+prop+" is set here just to make sharing of project simpler.",  // NOI18N
                        "# The library definition has always preference over this property."}, false); // NOI18N
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
    
    public static void setServerInstance(final Project project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    setNewServerInstanceValue(serverInstanceID, project, projectProps, privateProps);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify();
                }
            }
        });
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
                ((EjbJarProject)project).unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        ((EjbJarProject)project).registerJ2eePlatformListener(j2eePlatform);
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
    
    public static String getProperty(final String property, final AntProjectHelper helper, final String path) {
        EditableProperties props = helper.getProperties(path);
        return props.getProperty(property);
    }
    
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
}
