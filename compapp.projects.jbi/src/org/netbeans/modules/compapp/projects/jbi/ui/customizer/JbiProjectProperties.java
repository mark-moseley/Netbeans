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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import org.netbeans.modules.compapp.projects.jbi.CasaConstants;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.JbiProjectType;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.ComponentInformationParser;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentDocument;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentStatus;
import org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.UUIDGenerator;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;

import java.text.Collator;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.projects.jbi.ComponentHelper;


/**
 * Helper class. Defines constants for properties. Knows the proper place where to store the
 * properties.
 *
 * @author Petr Hrebejk, Chris Webster
 */
public class JbiProjectProperties {
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_1_3 = "1.3"; // NOI18N
    
    // Special properties of the project
    
    /**
     * DOCUMENT ME!
     */
    public static final String EJB_PROJECT_NAME = "j2ee.jbi.name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES
    
    /**
     * root of external web module sources (full path), ".." if the sources are within project
     * folder
     */
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    //public static final String JAR_NAME = "jar.name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVAC_ARGS = "javac.compilerargs"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SRC_DIR = "src.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String META_INF = "meta.inf"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_PRIVATE = "javadoc.private"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_TREE = "javadoc.notree"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_USE = "javadoc.use"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_NAVBAR = "javadoc.nonavbar"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_NO_INDEX = "javadoc.noindex"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_SPLIT_INDEX = "javadoc.splitindex"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_AUTHOR = "javadoc.author"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_VERSION = "javadoc.version"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_WINDOW_TITLE = "javadoc.windowtitle"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_ENCODING = "javadoc.encoding"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JAVADOC_PREVIEW = "javadoc.preview"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_CONTENT_ADDITIONAL = "jbi.content.additional"; //NOI18N
    
    /**
     * Stores Java EE jars only
     */
    public static final String JBI_JAVAEE_JARS = "jbi.content.javaee.jars"; //NOI18N
    
    /**
     *
     */
    public static final String JBI_JAVAEE_RESOURCE_DIRS = "jbi.javaee.res.dirs"; //NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_CONTENT_COMPONENT = "jbi.content.component"; //NOI18N
    
    // Start Test Framework
    /**
     * DOCUMENT ME!
     */
    public static final String TEST_DIR = "test.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String TEST_RESULTS_DIR = "test.results.dir"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SRC_BUILD_DIR = "src.build.dir"; // NOI18N
    
    //================== Start of JBI  =====================================//
    
    /**
     * DOCUMENT ME!
     */
    public static final String ASSEMBLY_UNIT_ALIAS = "org.netbeans.modules.compapp.jbiserver.alias.assembly-unit"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String ASSEMBLY_UNIT_UUID = "org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String ASSEMBLY_UNIT_DESCRIPTION = "org.netbeans.modules.compapp.jbiserver.description.assembly-unit"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String APPLICATION_SUB_ASSEMBLY_ALIAS = "org.netbeans.modules.compapp.jbiserver.alias.application-sub-assembly"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String APPLICATION_SUB_ASSEMBLY_DESCRIPTION = "org.netbeans.modules.compapp.jbiserver.description.application-sub-assembly"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_COMPONENT_CONF_FILE = "org.netbeans.modules.compapp.jbiserver.component.conf.file"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_COMPONENT_CONF_ROOT = "org.netbeans.modules.compapp.jbiserver.component.conf.root"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_DEPLOYMENT_CONF_FILE = "org.netbeans.modules.compapp.jbiserver.deployment.conf.file"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_DEPLOYMENT_CONF_ROOT = "org.netbeans.modules.compapp.jbiserver.deployment.conf.root"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DISPLAY_NAME_PROPERTY_KEY = "com.sun.appserver.instance.displayName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HOST_NAME_PROPERTY_KEY = "com.sun.appserver.instance.hostName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String ADMINISTRATION_PORT_PROPERTY_KEY = "com.sun.appserver.instance.administrationPort"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String DOMAIN_PROPERTY_KEY = "com.sun.appserver.instance.domain"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HTTP_MONITOR_ON_PROPERTY_KEY = "com.sun.appserver.instance.httpMonitorOn"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String HTTP_PORT_NUMBER_PROPERTY_KEY = "com.sun.appserver.instance.httpPortNumber"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String LOCATION_PROPERTY_KEY = "com.sun.appserver.instance.location"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String PASSWORD_PROPERTY_KEY = "com.sun.appserver.instance.password"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String URL_PROPERTY_KEY = "com.sun.appserver.instance.url"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String USER_NAME_PROPERTY_KEY = "com.sun.appserver.instance.userName"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    //public static final String ASSEMBLY_UNIT_GUID_KEY = "org.netbeans.modules.compapp.jbiserver.guid.assembly-unit"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_REGISTRY_COMPONENT_FILE_KEY = "com.sun.jbi.registry.component.file"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_REGISTRY_BROKER_HOST_KEY = "com.sun.jbi.messaging.brokerHost"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_ROUTING = "com.sun.jbi.routing"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String JBI_SA_INTERNAL_ROUTING = "com.sun.jbi.sa.internal.routing"; // NOI18N
    
//    public static final String JBI_TARGET_COMPONENT_LIST_KEY ="com.sun.jbi.target.component.list"; // NOI18N
    
    //================== End of JBI  =======================================//
    // Shortcuts
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    private static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private static final PathParser PATH_PARSER = new PathParser();
    private static final PathParser SEMICOLON_PATH_PARSER = new SemiColonPathParser();
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    private static final StringListParser STRING_LIST_PARSER = new StringListParser();
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        {
            JAVAC_CLASSPATH,
                    NbBundle.getMessage(JbiProjectProperties.class, "LBL_JavacClasspath_DisplayName") // NOI18N
        },
        {
            BUILD_CLASSES_DIR,
                    NbBundle.getMessage(JbiProjectProperties.class, "LBL_BuildClassesDir_DisplayName") // NOI18N
        }
    };
    
    /*
       private static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
       private final PropertyParser WAR_CONTENT_ADDITIONAL_PARSER =
               new JbiPathParser(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
     */
    
    // Info about the property destination
    // XXX only properties which are visually set should be described here
    // XXX refactor this list
    private PropertyDescriptor[] PROPERTY_DESCRIPTORS = {
        new PropertyDescriptor(EJB_PROJECT_NAME, null, STRING_PARSER),
        new PropertyDescriptor(J2EE_PLATFORM, PROJECT, STRING_PARSER),
        
        new PropertyDescriptor(SOURCE_ROOT, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_FILE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_JAR, PROJECT, PATH_PARSER),
        new PropertyDescriptor(JAVAC_CLASSPATH, PROJECT, PATH_PARSER),
        new PropertyDescriptor(DEBUG_CLASSPATH, PROJECT, PATH_PARSER),
        
        //new PropertyDescriptor(JAR_NAME, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAR_COMPRESS, PROJECT, BOOLEAN_PARSER),
        
        new PropertyDescriptor(J2EE_SERVER_TYPE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JAVAC_SOURCE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVAC_TARGET, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVAC_ARGS, PROJECT, STRING_PARSER),
        new PropertyDescriptor(SRC_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(META_INF, PROJECT, PATH_PARSER),
        new PropertyDescriptor(BUILD_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_CLASSES_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER),
        new PropertyDescriptor(DIST_JAVADOC_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVA_PLATFORM, PROJECT, PLATFORM_PARSER),
        
        new PropertyDescriptor(JAVADOC_PRIVATE, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_TREE, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_USE, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_NAVBAR, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_NO_INDEX, PROJECT, INVERSE_BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_SPLIT_INDEX, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_AUTHOR, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_VERSION, PROJECT, BOOLEAN_PARSER),
        new PropertyDescriptor(JAVADOC_WINDOW_TITLE, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVADOC_ENCODING, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JAVADOC_PREVIEW, PROJECT, BOOLEAN_PARSER),
        
        // This should be OS-agnostic
        new PropertyDescriptor(JBI_CONTENT_ADDITIONAL, PROJECT, SEMICOLON_PATH_PARSER),
        new PropertyDescriptor(JBI_JAVAEE_JARS, PROJECT, SEMICOLON_PATH_PARSER),
        new PropertyDescriptor(JBI_JAVAEE_RESOURCE_DIRS, PROJECT, STRING_LIST_PARSER),
        new PropertyDescriptor(JBI_CONTENT_COMPONENT, PROJECT, STRING_LIST_PARSER),
        
        // Start Test Framework
        new PropertyDescriptor(TEST_DIR, PROJECT, STRING_PARSER),
        new PropertyDescriptor(TEST_RESULTS_DIR, PROJECT, STRING_PARSER),
        // End Test Framework
        
        //================== Start of JBI  =====================================//
        new PropertyDescriptor(JBI_ROUTING, PROJECT, STRING_PARSER),
        new PropertyDescriptor(ASSEMBLY_UNIT_ALIAS, PROJECT, STRING_PARSER),
        new PropertyDescriptor(ASSEMBLY_UNIT_UUID, PROJECT, STRING_PARSER),
        new PropertyDescriptor(ASSEMBLY_UNIT_DESCRIPTION, PROJECT, STRING_PARSER),
        new PropertyDescriptor(APPLICATION_SUB_ASSEMBLY_ALIAS, PROJECT, STRING_PARSER),
        new PropertyDescriptor(APPLICATION_SUB_ASSEMBLY_DESCRIPTION, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JBI_COMPONENT_CONF_ROOT, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JBI_DEPLOYMENT_CONF_ROOT, PROJECT, STRING_PARSER),
        new PropertyDescriptor(JBI_COMPONENT_CONF_FILE, PROJECT, STRING_PARSER),
//            new PropertyDescriptor(JBI_TARGET_COMPONENT_LIST_KEY, PROJECT, STRING_PARSER),
        
        new PropertyDescriptor(JBI_DEPLOYMENT_CONF_FILE, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(DISPLAY_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HOST_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(ADMINISTRATION_PORT_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(DOMAIN_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HTTP_MONITOR_ON_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(HTTP_PORT_NUMBER_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(LOCATION_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(PASSWORD_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(URL_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(USER_NAME_PROPERTY_KEY, PRIVATE, STRING_PARSER),
        //new PropertyDescriptor(ASSEMBLY_UNIT_GUID_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JBI_REGISTRY_COMPONENT_FILE_KEY, PRIVATE, STRING_PARSER),
        new PropertyDescriptor(JBI_REGISTRY_BROKER_HOST_KEY, PRIVATE, STRING_PARSER),
        //================== End of JBI  =======================================//
    };
    
    // Private fields ----------------------------------------------------------
    private Project project;
    private HashMap<String, PropertyInfo> properties;
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    private AntBasedProjectType abpt;
    private List<VisualClassPathItem> bindingList = new Vector();
    private List<AntArtifact> sunresourceProjs;
    javax.swing.text.Document DIST_JAR_MODEL;
    
    /**
     * Creates a new JbiProjectProperties object.
     *
     * @param project DOCUMENT ME!
     * @param antProjectHelper DOCUMENT ME!
     * @param refHelper DOCUMENT ME!
     */
    public JbiProjectProperties(
            Project project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
            ) {
        this.project = project;
        this.properties = new HashMap<String, PropertyInfo>();
        this.antProjectHelper = antProjectHelper;
        this.refHelper = refHelper;
        this.abpt = ((JbiProject) project).getAntBasedProjectType();
        read();
        
        PropertyEvaluator evaluator = antProjectHelper.getStandardPropertyEvaluator();
        StoreGroup projectGroup = new StoreGroup();
        DIST_JAR_MODEL = projectGroup.createStringDocument(evaluator, DIST_JAR);
    }
    
    /**
     * XXX to be deleted when introduced in AntPropertyHeleper API
     *
     * @param property DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    static String getAntPropertyName(String property) {
        if ((property != null) && property.startsWith("${") && // NOI18N
                property.endsWith("}")) { // NOI18N
            
            return property.substring(2, property.length() - 1);
        } else {
            return property;
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List<VisualClassPathItem> getBindingList() {
        return bindingList;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void put(String propertyName, Object value) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        //        if (JAVAC_CLASSPATH.equals (propertyName)) {
        //            assert value instanceof List : "Wrong format of property " + propertyName; //NOI18N
        //            writeJavacClasspath ((List) value, antProjectHelper, refHelper);
        //        }
        PropertyInfo pi = (PropertyInfo) properties.get(propertyName);
        
        if (pi == null) {
            PropertyDescriptor pd = null;
            
            for (int i = 0; i < PROPERTY_DESCRIPTORS.length; i++) {
                pd = PROPERTY_DESCRIPTORS[i];
                
                if (pd.name.compareTo(propertyName) == 0) {
                    break;
                }
                
                pd = null;
            }
            
            if (pd == null) {
                return;
            }
            
            // todo: assuming the new prop value is string...
            pi = new PropertyInfo(pd, (String) value, (String) value);
            properties.put(pd.name, pi);
        }
        
        pi.setValue(value);
        
        if (J2EE_SERVER_INSTANCE.equals(propertyName)) {
            put(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID((String) value));
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object get(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        //        if (JAVAC_CLASSPATH.equals (propertyName)) {
        //            return readJavacClasspath (antProjectHelper, refHelper);
        //        }
        PropertyInfo pi = properties.get(propertyName);
        
        if (pi == null) {
            return null;
        }
        
        return pi.getValue();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param propertyName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isModified(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        
        PropertyInfo pi = properties.get(propertyName);
        
        if (pi == null) {
            return false;
        }
        
        return pi.isModified();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getSortedSubprojectsList() {
        ArrayList subprojects = new ArrayList(5);
        addSubprojects(project, subprojects); // Find the projects recursively
        
        // Replace projects in the list with formated names
        for (int i = 0; i < subprojects.size(); i++) {
            Project p = (Project) subprojects.get(i);
            subprojects.set(i, ProjectUtils.getInformation(p).getDisplayName());
        }
        
        // Sort the list
        Collections.sort(subprojects, Collator.getInstance());
        
        return subprojects;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    Project getProject() {
        return project;
    }
    
    /**
     * Gets all subprojects recursively
     *
     * @param project DOCUMENT ME!
     * @param result DOCUMENT ME!
     */
    private void addSubprojects(Project project, List result) {
        SubprojectProvider spp = (SubprojectProvider) project.getLookup().lookup(
                SubprojectProvider.class
                );
        
        if (spp == null) {
            return;
        }
        
        for (Iterator /*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext();) {
            Project sp = (Project) it.next();
            
            if (!result.contains(sp)) {
                result.add(sp);
                addSubprojects(sp, result);
            }
        }
    }
    
    /**
     * Reads all the properties of the project and converts them to objects suitable for usage in
     * the GUI controls.
     */
    private void read() {
        // Read the properties from the project
        HashMap eProps = new HashMap(2);
        eProps.put(PROJECT, antProjectHelper.getProperties(PROJECT));
        eProps.put(PRIVATE, antProjectHelper.getProperties(PRIVATE));
        
        // Initialize the property map with objects
        for (int i = 0; i < PROPERTY_DESCRIPTORS.length; i++) {
            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
            
            if (pd.dest == null) {
                // Specialy handled properties
                if (EJB_PROJECT_NAME.equals(pd.name)) {
                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
                    properties.put(pd.name, new PropertyInfo(pd, projectName, projectName));
                }
            } else {
                // Standard properties
                String raw = ((EditableProperties) eProps.get(pd.dest)).getProperty(pd.name);
                String eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(pd.name);
                properties.put(pd.name, new PropertyInfo(pd, raw, eval));
            }
        }
    }
    
    public void addSunResourceProject(AntArtifact aa){
        if (this.sunresourceProjs == null){
            this.sunresourceProjs = new ArrayList<AntArtifact>();
        }
        this.sunresourceProjs.add(aa);
    }
    
    public void removeSunResourceProject(AntArtifact aa){
        if (this.sunresourceProjs != null){
            this.sunresourceProjs.remove(aa);
        }
    }
    
    /**
     * Transforms all the Objects from GUI controls into String Ant  properties and stores them in
     * the project
     */
    public void store() {
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    resolveProjectDependencies();
                    
                    // Some properties need special handling e.g. if the
                    // property changes the project.xml files
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        pi.encode();
                        
                        String newValueEncoded = pi.getNewValueEncoded();
                        
                        if ((pd.dest == null) && (newValueEncoded != null)) {
                            // Specialy handled properties
                            if (EJB_PROJECT_NAME.equals(pd.name)) {
                                assert false : "No support yet for changing name of EJBProject; cf. EJBProject.setName"; // NOI18N
                            }
                        }
                        
                        if (JAVA_PLATFORM.equals(pd.name) && (newValueEncoded != null)) {
                            setPlatform(
                                    pi.getNewValueEncoded().equals(
                                    JavaPlatformManager.getDefault().getDefaultPlatform()
                                    .getProperties().get(
                                    "platform.ant.name" // NOI18N
                                    )
                                    )
                                    );
                        }
                    }
                    
                    try {
                        updateAssemblyInfoAndCasa();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    HashMap eProps = new HashMap(2);
                    eProps.put(PROJECT, antProjectHelper.getProperties(PROJECT));
                    eProps.put(PRIVATE, antProjectHelper.getProperties(PRIVATE));
                    
                    // Set the changed properties
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        String newValueEncoded = pi.getNewValueEncoded();
                        
                        if (newValueEncoded != null) {
                            if (pd.dest != null) {
                                // Standard properties
                                ((EditableProperties) eProps.get(pd.dest)).setProperty(
                                        pd.name, newValueEncoded
                                        );
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    antProjectHelper.putProperties(
                            PROJECT, (EditableProperties) eProps.get(PROJECT)
                            );
                    antProjectHelper.putProperties(
                            PRIVATE, (EditableProperties) eProps.get(PRIVATE)
                            );
                    ProjectManager.getDefault().saveProject(project);
                    
                    return null;
                }
            }
            );
            
            if (this.sunresourceProjs != null){
                Iterator<AntArtifact> itr = this.sunresourceProjs.iterator();
                AntArtifact aa = null;
                while (itr.hasNext()){
                    aa = itr.next();
                    SunResourcesUtil.addJavaEEResourceMetaData(this.getProject(), aa);
                }
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    private void updateAssemblyInfoAndCasa() throws Exception {
        
        FileObject casaFO = CasaHelper.getCasaFileObject(project, true);
        if (casaFO == null) {
            return;
        }
        
        List<VisualClassPathItem> oldContentList =
                (List) properties.get(JBI_CONTENT_ADDITIONAL).getOldValue();
        List<VisualClassPathItem> newContentList =
                (List) properties.get(JBI_CONTENT_ADDITIONAL).getValue();
        
        List<String> removedList = new ArrayList<String>();
        for (VisualClassPathItem content : oldContentList) {
            if (!newContentList.contains(content)) {
                removedList.add(content.toString());
            }
        }
        
        List<String> addedList = new ArrayList<String>();
        for (VisualClassPathItem content : newContentList) {
            if (!oldContentList.contains(content)) {
                addedList.add(content.toString());
            }
        }
        
        if (addedList.size() == 0 && removedList.size() == 0) {
            return; // no casa/asi change needed
        }
        
        saveAssemblyInfo();
        
        File file = FileUtil.toFile(casaFO);
        String fileLoc = file.getPath();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document casaDocument = builder.parse(file);
        
        Element sus = (Element) casaDocument.getElementsByTagName(
                CasaConstants.CASA_SERVICE_UNITS_ELEM_NAME).item(0);
        NodeList seSUs = sus.getElementsByTagName(
                CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
        
        // Remove deleted service units from casa
        for (String artifactName : removedList) {
            for (int i = 0; i < seSUs.getLength(); i++) {
                Element seSU = (Element) seSUs.item(i);
                if (seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME).
                        equals(artifactName)) {
                    sus.removeChild(seSU);
                    break;
                }
            }
        }
        
        // Add new service units to casa
        if (addedList.size() > 0) {
            
            List<String> newTargetIDs =
                    (List) properties.get(JBI_CONTENT_COMPONENT).getValue();
            
            for (String artifactName : addedList) {
                boolean found = false;
                for (int i = 0; i < seSUs.getLength(); i++) {
                    Element seSU = (Element) seSUs.item(i);
                    if (seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME).
                            equals(artifactName)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    String targetCompID = "unknown"; // NOI18N
                    for (int j = 0; j < newContentList.size(); j++) {
                        if (newContentList.get(j).toString().equals(artifactName)) {
                            targetCompID = newTargetIDs.get(j);
                            break;
                        }
                    }
                    Element seSU = casaDocument.createElement(
                            CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                    String compProjName = artifactName.substring(0, artifactName.length() - 4);
                    seSU.setAttribute(CasaConstants.CASA_X_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_Y_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_INTERNAL_ATTR_NAME, "true"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DEFINED_ATTR_NAME, "true"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_UNKNOWN_ATTR_NAME, "false"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_COMPONENT_NAME_ATTR_NAME, targetCompID); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DESCRIPTION_ATTR_NAME, "some description"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactName);
                    
                    sus.appendChild(seSU);
                }
            }
        }
        
        XmlUtil.writeToFile(fileLoc, casaDocument);
        casaFO.refresh();
    }
    
    private void setPlatform(boolean isDefault) {
        Element pcd = antProjectHelper.getPrimaryConfigurationData(true);
        
        NodeList sps = pcd.getElementsByTagName("explicit-platform"); // NOI18N
        
        if (isDefault && (sps.getLength() > 0)) {
            pcd.removeChild(sps.item(0));
        } else if (!isDefault && (sps.getLength() == 0)) {
            pcd.appendChild(pcd.getOwnerDocument().createElement("explicit-platform")); // NOI18N
        }
        
        antProjectHelper.putPrimaryConfigurationData(pcd, true);
    }
    
    /**
     * Finds out what are new and removed project dependencies and  applyes the info to the project
     */
    private void resolveProjectDependencies() {
        String[] allPaths = {JBI_CONTENT_ADDITIONAL}; // JAVAC_CLASSPATH,  DEBUG_CLASSPATH };
        
        // Create a set of old and new artifacts.
        Set<VisualClassPathItem> oldArtifacts = new HashSet<VisualClassPathItem>();
        Set<VisualClassPathItem> newArtifacts = new HashSet<VisualClassPathItem>();
        
        for (int i = 0; i < allPaths.length; i++) {
            PropertyInfo pi = (PropertyInfo) properties.get(allPaths[i]);
            
            // Get original artifacts
            List<VisualClassPathItem> oldList = (List) pi.getOldValue();
            if (oldList != null) {
                for (VisualClassPathItem vcpi : oldList) {
                    if (vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT) {
                        oldArtifacts.add(vcpi);
                    }
                }
            }
            
            // Get artifacts after the edit
            List<VisualClassPathItem> newList = (List) pi.getValue();
            if (newList != null) {
                for (VisualClassPathItem vcpi : newList) {
                    if (vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT) {
                        newArtifacts.add(vcpi);
                    }
                }
            }
        }
        
        // Create set of removed artifacts and remove them
        Set<VisualClassPathItem> removed =
                new HashSet<VisualClassPathItem>(oldArtifacts);
        removed.removeAll(newArtifacts);
        
        for (VisualClassPathItem vcpi : removed) {
            refHelper.destroyReference(vcpi.getRaw());
        }
    }
    
    // AssemblyInfo methods ------------------------------------
    private Element generateIdentification(Document document, String name, String description) {
        Element IdElement = document.createElement("identification"); // NOI18N
        
        // Name
        Element NameElement = document.createElement("name"); // NOI18N
        NameElement.appendChild(document.createTextNode(name));
        IdElement.appendChild(NameElement);
        
        // Description
        Element DescElement = document.createElement("description"); // NOI18N
        DescElement.appendChild(document.createTextNode(description));
        IdElement.appendChild(DescElement);
        
        return IdElement;
    }
    
    private Element generateArtifactsInfo(Document document, String label, String info) {
        Element ArtifactsElement = document.createElement(label);
        ArtifactsElement.appendChild(document.createTextNode(info));
        
        return ArtifactsElement;
    }
    
    private Element generateServiceUnit(
            Document document, VisualClassPathItem vi, String target, boolean isEngine
            ) {
        Element ASAElement = document.createElement("service-unit"); // NOI18N
        
        // String alias = vi.getAsaAlias();
        String uuid = vi.getAsaUUID();
        String desc = vi.getAsaDescription();
        String shortName = vi.getShortName();
        AntArtifact aa = (AntArtifact) vi.getObject();
        
        if (desc == null) { // if needed, use default one...
            desc = (String) this.get(JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION);
            vi.setAsaDescription(desc);
        }
        
        if (uuid == null) { // if needed, create a new one
            uuid = UUIDGenerator.getNUID();
            vi.setAsaUUID(uuid);
        }
        
        vi.setAsaTarget(target);
        
        String projectName = ((JbiProject) project).getName();
        String name;
        String jarName;
        if (isEngine) {
            name = projectName + "-" + vi.getProjectName(); // NOI18N
            jarName = vi.getProjectName() + ".jar"; // e.x., SynchronousSample.jar // NOI18N
        } else {
            name = projectName + "-" + target; // NOI18N
            jarName = target + ".jar"; // e.x., sun-http-binding.jar // NOI18N
        }
        ASAElement.appendChild(generateIdentification(document, name, desc));
        
        // Target
        Element TargetElement = document.createElement("target"); // NOI18N
        TargetElement.appendChild(generateArtifactsInfo(document, "artifacts-zip", jarName)); // NOI18N
        
        Element ComponentIdElement = document.createElement("component-name"); // NOI18N
        ComponentIdElement.appendChild(document.createTextNode(target));
        TargetElement.appendChild(ComponentIdElement);
        ASAElement.appendChild(TargetElement);
        
        return ASAElement;
    }
    
    private List<VisualClassPathItem> loadBindingComponentInfo(String compFileDst) {
        List<VisualClassPathItem> bindingList = new ArrayList<VisualClassPathItem>();
        AntArtifact bcjar = antProjectHelper.createSimpleAntArtifact(
                "CAPS.jbi:bpelse", "build/BCDeployment.jar", // NOI18N
                antProjectHelper.getStandardPropertyEvaluator(), "dist_bc", "clean" // NOI18N
                );
        
        try {
            File dst = new File(compFileDst);
            
            if (dst.exists()) {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(dst);
                List compList = compDoc.getJbiComponentList();
                Iterator iterator = compList.iterator();
                JBIComponentStatus component = null;
                
                // Added compNames Set to avoid duplicate entries in ASI.xml
                // caused by problems due to "incorrect" order of NB 5.5 to 6.0
                // upgrade and component name changes.
                Set<String> compNames = new HashSet<String>();
                
                while ((iterator != null) && (iterator.hasNext() == true)) {
                    component = (JBIComponentStatus) iterator.next();
                    
                    String compName = component.getName();
                    
                    if (!compNames.contains(compName)) {
                        compNames.add(compName);
                        
                        // update the target combo model..
                        if (component.getType().compareToIgnoreCase("Binding") == 0) { // NOI18N
                            VisualClassPathItem vi = new VisualClassPathItem(
                                    bcjar, VisualClassPathItem.TYPE_ARTIFACT, "BCDeployment.jar", null, // NOI18N
                                    true
                                    );
                            vi.setAsaTarget(component.getName());
                            bindingList.add(vi);
                            
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        
        return bindingList;
    }
    
    /**
     * DOCUMENT ME!
     */
    public void saveAssemblyInfo() {
        List os = (List) this.get(JbiProjectProperties.META_INF);
        String compFileDst = null;
        String jbiFileLoc = null;
        
        if ((os != null) && (os.size() > 0)) {
            String path = FileUtil.toFile(project.getProjectDirectory()).getPath() + "/" + os.get(0).toString(); // NOI18N
            /*
            if ((path.indexOf(':') < 0) && (!path.startsWith("/"))) {
                path = "/" + path; // In unix, it returns an incorrect path..
            }
             */
            compFileDst = path + "/" + "ComponentInformation.xml"; // NOI18N
            jbiFileLoc = path + "/" + "AssemblyInformation.xml"; // NOI18N
        }
        
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("jbi"); // NOI18N
            root.setAttribute("version", "1.0"); // NOI18N
            root.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi"); // NOI18N
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
            root.setAttribute("xsi:schemaLocation", "http://java.sun.com/xml/ns/jbi ./jbi.xsd"); // NOI18N
            document.appendChild(root);
            
            // Service Assembly ...
            Element serviceAssemblyElement = document.createElement("service-assembly"); // NOI18N
            serviceAssemblyElement.appendChild(
                    generateIdentification(
                    document, ((JbiProject)project).getName(), //auid,
                    (String) this.get(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION)
                    )
                    );
            
            // for each SE jar..
            List<VisualClassPathItem> items =
                    (List) this.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            List<String> targetIDs =
                    (List) this.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);
            
            assert items.size() == targetIDs.size() : 
                "Corrupted project.properties file: mismatching service unit artifacts and target components."; // NOI18N
                      
            for (int i = 0, size = items.size(); i < size; i++) {
                VisualClassPathItem vi = items.get(i);
                String targetID = targetIDs.get(i);
                assert (vi != null) && (targetID != null);
                
                serviceAssemblyElement.appendChild(
                        generateServiceUnit(document, vi, targetID, true));
            }
            
            // for each BC jar...
            bindingList = loadBindingComponentInfo(compFileDst);
            
            for (int i = 0, size = bindingList.size(); i < size; i++) {
                VisualClassPathItem vi = (VisualClassPathItem) bindingList.get(i);
                String targetID = vi.getAsaTarget();
                
                if ((vi != null) && (targetID != null) &&
                        vi.isInDeployment().booleanValue()) {
                    serviceAssemblyElement.appendChild(
                            generateServiceUnit(document, vi, targetID, false));
                }
            }
            
            root.appendChild(serviceAssemblyElement);
            document.getDocumentElement().normalize();
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(jbiFileLoc));
            
            //tFactory.setAttribute("indent-number", new Integer(4));
            // indent the output to make it more legible...
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); // NOI18N
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            
            transformer.transform(source, result);
            
        } catch (Exception e) {
            //ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            e.printStackTrace();
        }
    }
    
    public void fixComponentTargetList() {
        
        List<VisualClassPathItem> items =
                (List) this.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        List<String> targetIDs =
                (List) this.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);
        
        boolean fixNeeded = false;
        if (items.size() != targetIDs.size()) {
            fixNeeded = true;
        } else {
            for (String targetID : targetIDs) {
                if (targetID.startsWith("com.sun.") || targetID.equals("JavaEEServiceEngine")) { // NOI18N
                    fixNeeded = true;
                    break;
                }
            }
        }
        
        if (fixNeeded) {
            List<String> newTargetIDs = new ArrayList<String>();
            
            ComponentHelper componentHelper = new ComponentHelper(project);
            
            for (VisualClassPathItem item : items) {
                String asaType = item.getAsaType(); // sun-bpel-engine, or old com.sun.bpelse
                String target = componentHelper.getDefaultTarget(asaType);
                if (target == null) {
                    throw new RuntimeException("Unknown component target name for asaType of \"" + asaType + "\".");
                }
                newTargetIDs.add(target);
            }
            
            put(JBI_CONTENT_COMPONENT, newTargetIDs);
            store();
        }
    }
    
    /**
     * Extract nested text from an element. Currently does not handle coalescing text nodes, CDATA
     * sections, etc.
     *
     * @param parent a parent element
     *
     * @return the nested text, or null if none was found
     */
    public static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) l.item(i);
                
                return text.getNodeValue();
            }
        }
        
        return null;
    }
    
    private static List librariesInDeployment(AntProjectHelper helper) {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList libs = data.getElementsByTagNameNS(
                JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                );
        List cpItems = new ArrayList(libs.getLength());
        
        for (int i = 0; i < libs.getLength(); i++) {
            Element library = (Element) libs.item(i);
            cpItems.add(findText(library));
        }
        
        return cpItems;
    }
    
    private class PropertyInfo {
        private PropertyDescriptor propertyDesciptor;
        private String rawValue;
        private String evaluatedValue;
        private Object value;
        private Object newValue;
        private String newValueEncoded;
        
        /**
         * Creates a new PropertyInfo object.
         *
         * @param propertyDesciptor DOCUMENT ME!
         * @param rawValue DOCUMENT ME!
         * @param evaluatedValue DOCUMENT ME!
         */
        public PropertyInfo(
                PropertyDescriptor propertyDesciptor, String rawValue, String evaluatedValue
                ) {
            this.propertyDesciptor = propertyDesciptor;
            this.rawValue = rawValue;
            this.evaluatedValue = evaluatedValue;
            this.value = propertyDesciptor.parser.decode(rawValue, antProjectHelper, refHelper);
            this.newValue = null;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        /**
         * DOCUMENT ME!
         */
        public void encode() {
            if (isModified()) {
                newValueEncoded = propertyDesciptor.parser.encode(
                        newValue, antProjectHelper, refHelper, getOldValue()
                        );
            } else {
                newValueEncoded = null;
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getValue() {
            return isModified() ? newValue : value;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         */
        public void setValue(Object value) {
            newValue = value;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getNewValueEncoded() {
            return newValueEncoded;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean isModified() {
            return newValue != null;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getOldValue() {
            return value;
        }
    }
    
    private static class PropertyDescriptor {
        /**
         * DOCUMENT ME!
         */
        final PropertyParser parser;
        
        /**
         * DOCUMENT ME!
         */
        final String name;
        
        /**
         * DOCUMENT ME!
         */
        final String dest;
        
        /**
         * Creates a new PropertyDescriptor object.
         *
         * @param name DOCUMENT ME!
         * @param dest DOCUMENT ME!
         * @param parser DOCUMENT ME!
         */
        PropertyDescriptor(String name, String dest, PropertyParser parser) {
            this.name = name;
            this.dest = dest;
            this.parser = parser;
        }
    }
    
    private static abstract class PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public abstract Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                );
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public abstract String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                );
        
        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, Object oldValue) {
            return encode(value, antProjectHelper, refHelper);
        };
        
    }
    
    private static class StringParser extends PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return raw;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return (String) value;
        }
    }
    
    private static class BooleanParser extends PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            if (raw != null) {
                String lowecaseRaw = raw.toLowerCase();
                
                if (
                        lowecaseRaw.equals("true") || lowecaseRaw.equals("yes") || // NOI18N
                        lowecaseRaw.equals("enabled") // NOI18N
                        ) {
                    return Boolean.TRUE;
                }
            }
            
            return Boolean.FALSE;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return ((Boolean) value).booleanValue() ? "true" : "false"; // NOI18N
        }
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return ((Boolean) super.decode(raw, antProjectHelper, refHelper)).booleanValue()
            ? Boolean.FALSE : Boolean.TRUE;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return super.encode(
                    ((Boolean) value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper,
                    refHelper
                    );
        }
    }
    
    private static class PlatformParser extends PropertyParser {
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            
            for (int i = 0; i < platforms.length; i++) {
                String normalizedName = (String) platforms[i].getProperties().get(
                        "platform.ant.name" // NOI18N
                        );
                
                if ((normalizedName != null) && normalizedName.equals(raw)) {
                    return platforms[i].getDisplayName();
                }
            }
            
            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                    (String) value, new Specification("j2se", null) // NOI18N
                    );
            
            if (platforms.length == 0) {
                return null;
            } else {
                return (String) platforms[0].getProperties().get("platform.ant.name"); //NOI18N
            }
        }
    }
    
    private static class PathParser extends PropertyParser {
        
        protected String getPathSeparator() {
            return File.pathSeparator;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param raw DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object decode(
                String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            if ((raw == null) || (raw.trim().length() < 1)) {
                return new ArrayList();
            }
            
            EditableProperties ep = antProjectHelper.getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH
                    );
            String classpath = raw; // ep.getProperty(JbiProjectProperties.JAVAC_CLASSPATH);
            
            if (classpath == null) {
                return new ArrayList();
            }
            
            String[] classPathElement = classpath.split(getPathSeparator());
            List cpItems = new ArrayList();
            List manifestItems = librariesInDeployment(antProjectHelper);
            
            for (int i = 0; i < classPathElement.length; i++) {
                String file = classPathElement[i];
                String propertyName = getAntPropertyName(file);
                boolean inDeployment = manifestItems.contains(propertyName);
                VisualClassPathItem cpItem;
                
                // First try to find out whether the item is well known classpath
                // in the J2SE project type
                int wellKnownPathIndex = -1;
                
                for (int j = 0; j < WELL_KNOWN_PATHS.length; j++) {
                    if (WELL_KNOWN_PATHS[j][0].equals(propertyName)) {
                        wellKnownPathIndex = j;
                        
                        break;
                    }
                }
                
                if (wellKnownPathIndex != -1) {
                    cpItem = new VisualClassPathItem(
                            file, VisualClassPathItem.TYPE_CLASSPATH, file,
                            WELL_KNOWN_PATHS[wellKnownPathIndex][1], inDeployment
                            );
                } else if (file.startsWith(LIBRARY_PREFIX)) {
                    // Library from library manager
                    String eval = file.substring(LIBRARY_PREFIX.length(), file.lastIndexOf('.')); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary(eval);
                    
                    if (lib != null) {
                        cpItem = new VisualClassPathItem(
                                lib, VisualClassPathItem.TYPE_LIBRARY, file, eval, inDeployment
                                );
                    } else {
                        //Invalid library. The lbirary was probably removed from system.
                        cpItem = null;
                    }
                } else {
                    Object os[] = refHelper.findArtifactAndLocation( file );
                    if ((os != null) && (os.length > 0) ) {
                        AntArtifact artifact = (AntArtifact) os[0];
                        // Sub project artifact
                        String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(
                                file
                                );
                        cpItem = new VisualClassPathItem(
                                artifact, VisualClassPathItem.TYPE_ARTIFACT, file, eval,
                                inDeployment
                                );
                    } else {
                        // Standalone jar or property
                        String eval = antProjectHelper.getStandardPropertyEvaluator().evaluate(
                                file
                                );
                        cpItem = new VisualClassPathItem(
                                file, VisualClassPathItem.TYPE_JAR, file, eval, inDeployment
                                );
                    }
                }
                
                if (cpItem != null) {
                    cpItems.add(cpItem);
                }
            }
            
            return cpItems;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param antProjectHelper DOCUMENT ME!
         * @param refHelper DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
                ) {
            return encode(value, antProjectHelper, refHelper, value);
        }
        public String encode(
                Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper, Object oldValue
                ) {
            StringBuffer sb = new StringBuffer();
            Element data = antProjectHelper.getPrimaryConfigurationData(true);
            org.w3c.dom.Document doc = data.getOwnerDocument();
            NodeList libs = data.getElementsByTagNameNS(
                    JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                    ); //NOI18N
            
            // 03/24/05, fixed a bug in removing libray entries
            int ns = libs.getLength();
            
            for (int i = ns; i > 0; i--) {
                Node n = libs.item(i - 1);
                n.getParentNode().removeChild(n);
            }
            
            
            boolean bDeleteProperty = false;
            List removedItemsList = new ArrayList();
            Object tempObject;
            if(value != null) {
                for (Iterator it = ((List) oldValue).iterator(); it.hasNext();) {
                    tempObject = it.next();
//                    try {
                    if(((List) value).indexOf(tempObject) == -1) {  // If the newValue doesn't contain any oldValue element, then
                        removedItemsList.add(tempObject);           // that element got removed
                    }
//                    } catch (Exception e) {
//                        removedItemsList.add(tempObject);
//                    }
                }
            }
            for (Iterator it = ((List) removedItemsList).iterator(); it.hasNext();) {   //Remove the references
                VisualClassPathItem vcpi = (VisualClassPathItem) it.next();
                switch (vcpi.getType()) {
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        refHelper.destroyReference(vcpi.getRaw());
                        break;
                }
                
            }
            
            String pathSeparator = getPathSeparator();
            
            for (Iterator it = ((List) value).iterator(); it.hasNext();) {
                VisualClassPathItem vcpi = (VisualClassPathItem) it.next();
                
                String library_tag_value = ""; // NOI18N
                
                switch (vcpi.getType()) {
                    case VisualClassPathItem.TYPE_JAR:
                        
                        String raw = vcpi.getRaw();
                        
                        if (raw == null) {
                            // New file
                            File file = (File) vcpi.getObject();
                            String reference = refHelper.createForeignFileReference(
                                    file, JavaProjectConstants.ARTIFACT_TYPE_JAR
                                    );
                            library_tag_value = reference;
                        } else {
                            // Existing property
                            library_tag_value = raw;
                        }
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_LIBRARY:
                        library_tag_value = vcpi.getRaw();
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        
                        AntArtifact aa = (AntArtifact) vcpi.getObject();
                        // String reference = refHelper.addReference( aa, null );
                        String reference = aa == null ? vcpi.getRaw() : // prevent NPE thrown from older projects
                            (String) refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                        library_tag_value = reference;
                        
                        break;
                        
                    case VisualClassPathItem.TYPE_CLASSPATH:
                        library_tag_value = vcpi.getRaw();
                        
                        break;
                }
                
                sb.append(library_tag_value);
                sb.append(pathSeparator);
                
                if (vcpi.isInDeployment().booleanValue()) {
                    Element library = doc.createElementNS(
                            JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "included-library" // NOI18N
                            );
                    library.appendChild(doc.createTextNode(getAntPropertyName(library_tag_value)));
                    data.appendChild(library);
                }
            }
            
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            
            antProjectHelper.putPrimaryConfigurationData(data, true);
            
            return sb.toString();
        }
    }
    
    private static class SemiColonPathParser extends PathParser {
        protected String getPathSeparator() {
            return ";";
        }
    }
    
    private static class StringListParser extends PropertyParser {
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            if ((raw == null) || (raw.trim().length() < 1)) {
                return new ArrayList();
            }
            String[] result = raw.split(";");
            return Arrays.asList(result);
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            List<String> list = (List<String>) value;
            String result = "";
            for (Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
                String str = iter.next();
                if (iter.hasNext()) {
                    result = result + str + ";";
                } else {
                    result = result + str;
                }
            }
            return result;
        }
        
    }
}
