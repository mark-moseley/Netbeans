/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.J2eeProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.earproject.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.earproject.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.dd.api.application.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 */
public class EarProjectProperties {
    
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
    public static final String DIST_JAR = "dist.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String RUN_CLASSPATH = "run.classpath";
    public static final String JAR_NAME = "jar.name"; //NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String JAR_CONTENT_ADDITIONAL = "jar.content.additional"; //NOI18N

    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String SRC_DIR = "src.dir"; //NOI18N
    public static final String META_INF = "meta.inf"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_ARCHIVE_DIR = "build.archive.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
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
    
    // Properties stored in the PRIVATE.PROPERTIES

    // Shortcuts 
    private static final String PROJECT = AntProjectHelper.PROJECT_PROPERTIES_PATH;
    private static final String PRIVATE = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
    
//    public static final String TAG_WEB_MODULE_LIBRARIES = "j2ee-module-libraries"; // NOI18N
//    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "j2ee-module-additional-libraries"; //NOI18N
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N

    
    static final PropertyParser STRING_PARSER = new StringParser();
    private static final BooleanParser BOOLEAN_PARSER = new BooleanParser();
    private static final InverseBooleanParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    private final PropertyParser PATH_PARSER = new PathParser();
    private final PropertyParser JAVAC_CLASSPATH_PARSER = new PathParser(TAG_WEB_MODULE_LIBRARIES);
    private final PropertyParser WAR_CONTENT_ADDITIONAL_PARSER =
            new PathParser(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
    private static final PlatformParser PLATFORM_PARSER = new PlatformParser();
    
    // Info about the property destination
    private PropertyDescriptor PROPERTY_DESCRIPTORS[] = {
        new PropertyDescriptor( WEB_PROJECT_NAME, null, STRING_PARSER ),
        new PropertyDescriptor( J2EE_PLATFORM, PROJECT, STRING_PARSER ),
                
        new PropertyDescriptor( SOURCE_ROOT, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_FILE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( LIBRARIES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_CLASSPATH, PROJECT, JAVAC_CLASSPATH_PARSER ),
        new PropertyDescriptor( COMPILE_JSPS, PROJECT, BOOLEAN_PARSER ),
        //new PropertyDescriptor( JSP_COMPILER_CLASSPATH, PRIVATE, PATH_PARSER ),
        new PropertyDescriptor( DEBUG_CLASSPATH, PROJECT, PATH_PARSER ),
        new PropertyDescriptor( RUN_CLASSPATH, PROJECT, PATH_PARSER ),

        new PropertyDescriptor( JAR_NAME, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAR_COMPRESS, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAR_CONTENT_ADDITIONAL, PROJECT, WAR_CONTENT_ADDITIONAL_PARSER ),
        
        new PropertyDescriptor( LAUNCH_URL_RELATIVE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( CLIENT_MODULE_URI, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DISPLAY_BROWSER, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_TYPE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( J2EE_SERVER_INSTANCE, PRIVATE, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_SOURCE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVAC_DEBUG, PROJECT, BOOLEAN_PARSER ),       
        new PropertyDescriptor( JAVAC_DEPRECATION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVAC_TARGET, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( SRC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( RESOURCE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( BUILD_CLASSES_EXCLUDES, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( DIST_JAVADOC_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( WEB_DOCBASE_DIR, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( NO_DEPENDENCIES, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVA_PLATFORM, PROJECT, PLATFORM_PARSER ),
        
        new PropertyDescriptor( JAVADOC_PRIVATE, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_TREE, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_USE, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_NAVBAR, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_NO_INDEX, PROJECT, INVERSE_BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_SPLIT_INDEX, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_AUTHOR, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_VERSION, PROJECT, BOOLEAN_PARSER ),
        new PropertyDescriptor( JAVADOC_WINDOW_TITLE, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVADOC_ENCODING, PROJECT, STRING_PARSER ),
        new PropertyDescriptor( JAVADOC_PREVIEW, PROJECT, BOOLEAN_PARSER ),
    };
    
    // Private fields ----------------------------------------------------------
    
    private Project project;
    protected HashMap properties;    
    protected AntProjectHelper antProjectHelper;
    protected ReferenceHelper refHelper;
    private AntBasedProjectType abpt;
    private UpdateHelper updateHelper;
    private EarProject earProject;
    
    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport (this);
    
    public EarProjectProperties(Project project, UpdateHelper updateHelper, PropertyEvaluator eval, ReferenceHelper refHelper, AntBasedProjectType abpt) {
        this.project = project;
        this.properties = new HashMap();
        this.updateHelper = updateHelper;
        this.antProjectHelper = updateHelper.getAntProjectHelper();
        this.refHelper = refHelper;
        this.abpt = abpt;
        this.updateHelper = updateHelper;
        earProject = (EarProject) project;
        read();
    }
    
    public EarProjectProperties(EarProject project, ReferenceHelper refHelper, AntBasedProjectType abpt ) {
        this (project, project.getUpdateHelper(), project.getUpdateHelper().getAntProjectHelper().getStandardPropertyEvaluator(), refHelper, abpt );
    }
    
    public EarProjectProperties(Project project, AntProjectHelper aph, ReferenceHelper refHelper, AntBasedProjectType abtp) {
        this((EarProject)project, refHelper, abtp );
    }
        
    protected void updateContentDependency(Set deleted, Set added) {
        Application app = null;
        try {
            app = DDProvider.getDefault().getDDRoot(earProject.getAppModule().getDeploymentDescriptor());
        } catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
        }
        if (null != app) {
            // delete the old entries out of the application
            Iterator iter = deleted.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                removeItemFromAppDD(app,vcpi);
            }
            // add the new stuff "back"
            iter = added.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                addItemToAppDD(app,vcpi);
            }
            try {
                app.write(earProject.getAppModule().getDeploymentDescriptor());
            } catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
            }
            
        }
    }
    
    private void removeItemFromAppDD(Application dd, VisualClassPathItem vcpi) {
        String path = vcpi.getCompletePathInArchive();
        Module m = searchForModule(dd,path);
        if (null != m) {
            dd.removeModule(m);
            setClientModuleUri("");
            Object obj = vcpi.getObject();
            AntArtifact aa;
            Project p;
            if (obj instanceof AntArtifact) {
                aa = (AntArtifact) obj;
                p = aa.getProject();
            } else {
                return;
            }
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm)
                    earProject.getAppModule().removeModuleProvider(jmp,path);
            }
                return;
            }
        }
    
    private Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getConnector();
            if (null != val && val.equals(path))
                return mods[i];
            val = mods[i].getJava();
            if (null != val && val.equals(path))
                return mods[i];
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w)
                val = w.getWebUri();
            if (null != val && val.equals(path))
                return mods[i];
        }
        return null;
    }
    
    private void addItemToAppDD(Application dd, VisualClassPathItem vcpi) {
        Object obj = vcpi.getObject();
        AntArtifact aa;
        Project p;
        String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
        Module mod = null;
        if (obj instanceof AntArtifact) {
            mod = getModFromAntArtifact((AntArtifact) obj, dd, path);
        }
        else if (obj instanceof File) {
            mod = getModFromFile((File) obj, dd, path);
        }
        if (mod != null && mod.getWeb() != null)
            replaceEmptyClientModuleUri(path);
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod)
            dd.addModule(mod);
    }
    
    
    private Module getModFromAntArtifact(AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                jmp.setServerInstanceID(earProject.getServerInstanceID());
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    earProject.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean("Module");
                if (jm.getModuleType() == J2eeModule.EJB) {
                    mod.setEjb(path); // NOI18N
                }
                else if (jm.getModuleType() == J2eeModule.WAR) {
                    Web w = (Web) mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    org.openide.filesystems.FileObject tmp = aa.getScriptFile();
                    if (null != tmp)
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    WebModule wm = null;
                    if (null != tmp)
                        wm = (WebModule) WebModule.getWebModule(tmp);
                    if (null != wm) {
                        w.setContextRoot(wm.getContextPath());
                    }
                    else {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot(path.substring(0,endex));
                    }
                     mod.setWeb(w);
                }
                else if (jm.getModuleType() == J2eeModule.CONN) {
                    mod.setConnector(path);
                }
                else if (jm.getModuleType() == J2eeModule.CLIENT) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
        }
        return mod;
    }
    
    private void setClientModuleUri(String newVal) {
        put(EarProjectProperties.CLIENT_MODULE_URI,newVal);        
    }
    
    private void replaceEmptyClientModuleUri(String path) {
        // set the context path if it is not set...
        Object foo = get(EarProjectProperties.CLIENT_MODULE_URI);
        if (null == foo) {
            setClientModuleUri(path);
        }
        if (foo instanceof String) {
            String bar = (String) foo;
            if (bar.length() < 1) {
                setClientModuleUri(path);
            }
        }
        
    }
    
    private Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile((File) f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); //NOI18N
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean("Module"); // NOI18N
                    Web w = (Web) mod.newWeb(); 
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
            }
            catch (ClassNotFoundException cnfe) {
                org.openide.ErrorManager.getDefault ().log (cnfe.getLocalizedMessage ());
            }
            catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
            }
            finally {
                try {
                    if (null != jar)
                        jar.close();
                }
                catch (java.io.IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }
    
    /**
     * Called when a change was made to a properties file that might be shared with Ant.
     * <p class="nonnormative">
     * Note: normally you would not use this event to detect property changes.
     * Use the property change listener from {@link PropertyEvaluator} instead to find
     * changes in the interpreted values of Ant properties, possibly coming from multiple
     * properties files.
     * </p>
     * @param ev an event with details of the change
     */
    public void propertiesChanged(AntProjectEvent ev) {
        
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {

        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
    public Map getModuleMap() {
        Map mods = new HashMap();
        Object o = properties.get(JAR_CONTENT_ADDITIONAL);
        if (null != o && o instanceof PropertyInfo) {
            PropertyInfo pi = (PropertyInfo) o;
            List newV = (List) pi.getValue();
            
            Iterator iter = newV.iterator();
            while (iter.hasNext()) {
                VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
                String path = vcpi.getCompletePathInArchive(); //   computePath(vcpi);
                Object obj = vcpi.getObject();
                AntArtifact aa;
                Project p;
                if (obj instanceof AntArtifact) {
                    aa = (AntArtifact) obj;
                    p = aa.getProject();
                } else {
                    continue;
                }
                J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        mods.put(path, jmp);
                    }
                }
            }
        }
        return mods; // earProject.getAppModule().setModules(mods);
    }


    public void addJ2eeSubprojects(Project[] moduleProjects) {
        List artifactList = new ArrayList();
        for (int i = 0; i < moduleProjects.length; i++) {
            AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                    moduleProjects[i],
                    J2eeProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE); //the artifact type is the some for both ejb and war projects
            if (null != artifacts)
                artifactList.addAll(Arrays.asList(artifacts));
            
        }
        // create the vcpis
        List newVCPIs = new ArrayList();
        Iterator iter = artifactList.iterator();
        while (iter.hasNext()) {
            AntArtifact art = (AntArtifact) iter.next();
            VisualClassPathItem vcpi = VisualClassPathItem.create(art,VisualClassPathItem.PATH_IN_WAR_APPLET);
            vcpi.setRaw(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
            newVCPIs.add(vcpi);
        }
        Object t = get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        newVCPIs.addAll(vcpis);
        put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newVCPIs);
        store();
        try {
            org.netbeans.api.project.ProjectManager.getDefault().saveProject(getProject());
        } catch ( java.io.IOException ex ) {
            org.openide.ErrorManager.getDefault().notify( ex );
        }
    }
    
    String[] getWebUris() {
        Application app = null;
        try {
            app = DDProvider.getDefault ().getDDRoot (earProject.getAppModule().getDeploymentDescriptor ());
        }
        catch (java.io.IOException ioe) {
            org.openide.ErrorManager.getDefault ().log (ioe.getLocalizedMessage ());
        }
        Module mods[] = app.getModule();
        int len = 0;
        if (null != mods)
            len = mods.length;
        ArrayList retList = new ArrayList();
        for (int i = 0; i < len; i++) {
            Web w = mods[i].getWeb();
            if (null != w) {
                retList.add(w.getWebUri());
            }
        }
        return (String[]) retList.toArray(new String[retList.size()]);
        
    }
    
    // XXX - remove this method after completing 54179
    private  boolean projectClosed() {
        Project[] projects = org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(earProject))
                return false;
        }
        return true;
    }
    
     /** XXX to be deleted when introduced in AntPropertyHeleper API
     */    
    static String getAntPropertyName( String property ) {
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
    
   public void put( String propertyName, Object value ) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        pi.setValue( value );
        if (J2EE_SERVER_INSTANCE.equals (propertyName)) {
            put (J2EE_SERVER_TYPE, Deployment.getDefault ().getServerID ((String) value));
        }
    }
    
    public Object get(String propertyName) {
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        PropertyInfo pi = (PropertyInfo) properties.get(propertyName);
        return pi == null ? null : pi.getValue();
    }
    
    public boolean isModified( String propertyName ) {
        PropertyInfo pi = (PropertyInfo)properties.get( propertyName );
        assert propertyName != null : "Unknown property " + propertyName; // NOI18N
        return pi.isModified();
    }
    
    public List getSortedSubprojectsList() {
             
        ArrayList subprojects = new ArrayList( 5 );
        addSubprojects( project, subprojects ); // Find the projects recursively
         
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            Project p = (Project)subprojects.get( i );           
            subprojects.set(i, ProjectUtils.getInformation(p).getDisplayName());
        }
        
        // Sort the list
        Collections.sort( subprojects, Collator.getInstance() );
        
        return subprojects;
    }
    
    public Project getProject() {
        return project;
    }
    
    /** Gets all subprojects recursively
     */
    private void addSubprojects( Project project, List result ) {
        
        SubprojectProvider spp = (SubprojectProvider)project.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator/*<Project>*/ it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project sp = (Project)it.next(); 
            if ( !result.contains( sp ) ) {
                result.add( sp );
            }
            addSubprojects( sp, result );            
        }
        
    }

    /** Reads all the properties of the project and converts them to objects
     * suitable for usage in the GUI controls.
     */    
    protected void read() {
        
        // Read the properties from the project        
        HashMap eProps = new HashMap( 2 );
        eProps.put( PROJECT, updateHelper.getProperties( PROJECT ) ); 
        eProps.put( PRIVATE, updateHelper.getProperties( PRIVATE ) );
   
        // Initialize the property map with objects
        for ( int i = 0; i < PROPERTY_DESCRIPTORS.length; i++ ) {
            PropertyDescriptor pd = PROPERTY_DESCRIPTORS[i];
            final String propertyName = pd.name;
            if ( pd.dest == null ) {
                // Specialy handled properties
                if ( WEB_PROJECT_NAME.equals( propertyName ) ) {
                    String projectName = ProjectUtils.getInformation(project).getDisplayName();
                    PropertyInfo pi = (PropertyInfo) properties.get(propertyName);
                    if (null == pi)
                        properties.put( propertyName, new PropertyInfo( pd, projectName, projectName ) );
                    else
                        pi.update(pd,projectName,projectName);
                }
            }
            else {
                // Standard properties
                String raw = ((EditableProperties)eProps.get( pd.dest )).getProperty( propertyName );
                String eval = antProjectHelper.getStandardPropertyEvaluator ().getProperty ( propertyName );
                PropertyInfo pi = (PropertyInfo) properties.get(propertyName);
                if (null == pi) {
                    final PropertyInfo propertyInfo = new PropertyInfo( pd, raw, eval );
                    properties.put(propertyName, propertyInfo);
                }
                else
                    pi.update(pd,raw,eval);
            }
        }
    }

    void initProperty(final String propertyName, final PropertyInfo propertyInfo) {
        properties.put(propertyName, propertyInfo);
    }

    /** Transforms all the Objects from GUI controls into String Ant
     * properties and stores them in the project
     */    
    public void store() {

        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {

                    resolveProjectDependencies();
                    Boolean defaultPlatform = null;
                    
                    // Some properties need special handling e.g. if the 
                    // property changes the project.xml files                   
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        pi.encode();
                        String newValueEncoded = pi.getNewValueEncoded();
                        if(pd.saver != null) {
                            pd.saver.save(pi);
                        }
                        if (pd.dest == null && newValueEncoded != null) {
                            // Specialy handled properties
                            if (WEB_PROJECT_NAME.equals(pd.name)) {
                                assert false : "No support yet for changing name of J2SEProject; cf. J2SEProject.setName";  //NOI18N
                            }
                        }
                                                
                        if (JAVA_PLATFORM.equals(pd.name) && newValueEncoded != null) {
                            defaultPlatform =
                                    Boolean.valueOf(pi.getNewValueEncoded().equals(JavaPlatformManager.getDefault()
                                    .getDefaultPlatform()
                                    .getProperties()
                                    .get("platform.ant.name"))); // NOI18N
                            setPlatform(defaultPlatform.booleanValue(), pi.getNewValueEncoded());
                        }
                    }
                    
                    // Reread the properties. It may have changed when
                    // e.g. when setting references to another projects
                    HashMap eProps = new HashMap(2);
                    eProps.put(PROJECT, updateHelper.getProperties(PROJECT));
                    eProps.put(PRIVATE, updateHelper.getProperties(PRIVATE));
        
                    //generate library content references into private.properties
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        if(JAR_CONTENT_ADDITIONAL.equals(pd.name)
                                && pi.newValue != null) {
                            //add an entry into private properties
                            Iterator newItems  = ((ArrayList)pi.newValue).iterator();
                            Iterator oldItems = ((ArrayList)pi.value).iterator();
                            storeLibrariesLocations(newItems, oldItems, (EditableProperties)eProps.get(PRIVATE));
                            break;
                        }
                    }
                    
                    // Set the changed properties
                    for (Iterator it = properties.values().iterator(); it.hasNext();) {
                        PropertyInfo pi = (PropertyInfo) it.next();
                        PropertyDescriptor pd = pi.getPropertyDescriptor();
                        String newValueEncoded = pi.getNewValueEncoded();
                        if (newValueEncoded != null) {
                            if (pd.dest != null) {
                                // Standard properties
                                EditableProperties ep = (EditableProperties) eProps.get(pd.dest);
              //                  if (PATH_PARSER.equals(pd.parser)) {
                                if (pd.parser instanceof PathParser) {
                                    // XXX: perhaps PATH_PARSER could return List of paths so that
                                    // tokenizing could be omitted here:
                                    String[] items = PropertyUtils.tokenizePath(newValueEncoded);
                                    for (int i = 0; i < items.length - 1; i++) {
                                        items[i] += File.pathSeparatorChar;
                                    }
                                    ep.setProperty(pd.name, items);
                                } else if (NO_DEPENDENCIES.equals(pd.name) && newValueEncoded.equals("false")) { // NOI18N
                                    ep.remove(pd.name);
                                } else {
                                    if (JAVA_PLATFORM.equals(pd.name)) { // update javac.source and javac.target
                                        assert defaultPlatform != null;
                                        updateSourceLevel(defaultPlatform.booleanValue(), newValueEncoded, ep);
                                    } else if (JAVAC_CLASSPATH.equals(pd.name)) {
                                        writeWebLibraries(antProjectHelper, refHelper, (List) pi.getValue(),
                                                TAG_WEB_MODULE_LIBRARIES);
                                    } else if (JAR_CONTENT_ADDITIONAL.equals(pd.name)) {
                                        writeWebLibraries(antProjectHelper, refHelper, (List) pi.getValue(),
                                                TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                                    }
                                    ep.setProperty(pd.name, newValueEncoded);
                                }
                            }
                        }
                    }
                    
                    // Store the property changes into the project
                    updateHelper.putProperties(PROJECT, (EditableProperties) eProps.get(PROJECT));
                    updateHelper.putProperties(PRIVATE, (EditableProperties) eProps.get(PRIVATE));
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    private void updateSourceLevel(boolean defaultPlatform, String platform, EditableProperties ep) {
        if (defaultPlatform) {
            ep.setProperty(JAVAC_SOURCE, "${default.javac.source}"); //NOI18N
            ep.setProperty(JAVAC_TARGET, "${default.javac.target}"); //NOI18N
        } else {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for( int i = 0; i < platforms.length; i++ ) {
                Specification spec = platforms[i].getSpecification();
                if (!("j2se".equalsIgnoreCase(spec.getName()))) { // NOI18N
                    continue;
                }
                if (platform.equals(platforms[i].getProperties().get("platform.ant.name"))) { //NOI18N
                    String ver = platforms[i].getSpecification().getVersion().toString();
                    ep.setProperty(JAVAC_SOURCE, ver);
                    ep.setProperty(JAVAC_TARGET, ver);
                    return;
                }
            }
            // The platform does not exist. Perhaps this is project with broken references?
            // Do not update target and source because nothing is known about the platform.
        }
    }
    
    private final SpecificationVersion JDKSpec13 = new SpecificationVersion("1.3"); // NOI18N
    
    private void setPlatform(boolean isDefault, String platformAntID) {
        Element pcd = updateHelper.getPrimaryConfigurationData( true );
        NodeList sps = pcd.getElementsByTagName( "explicit-platform" ); // NOI18N
        if (isDefault && sps.getLength() > 0) {
            pcd.removeChild(sps.item(0));
        } else if (!isDefault) {
            Element el;
            if (sps.getLength() == 0) {
                el = pcd.getOwnerDocument().createElement("explicit-platform"); // NOI18N
                pcd.appendChild(el);
            } else {
                el = (Element)sps.item(0);
            }
            boolean explicitSource = true;
            JavaPlatform platform = findPlatform(platformAntID);
            if ((platform != null && platform.getSpecification().getVersion().compareTo(JDKSpec13) <= 0) || platform == null) {
                explicitSource = false;
            }
            el.setAttribute("explicit-source-supported", explicitSource ? "true" : "false"); // NOI18N
        }
        updateHelper.putPrimaryConfigurationData(pcd, true);
    }
            
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
    
        String allPaths[] = { JAVAC_CLASSPATH, JAR_CONTENT_ADDITIONAL, RUN_CLASSPATH };
        
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        Set newArtifacts = new HashSet();
        for ( int i = 0; i < allPaths.length; i++ ) {            
            PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );

            // Get original artifacts
            List oldList = (List)pi.getOldValue();
            if ( oldList != null ) {
                oldArtifacts.addAll(oldList);
            }
            
            // Get artifacts after the edit
            List newList = (List)pi.getValue();
            if ( newList != null ) {
                newArtifacts.addAll(newList);
            }
                        
        }

        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set added = new HashSet(newArtifacts);
        added.removeAll(oldArtifacts);
        
        Set deletedContent = new HashSet((List)((PropertyInfo) properties.get(JAR_CONTENT_ADDITIONAL)).getOldValue());
        Set addedContent = new HashSet((List) ((PropertyInfo) properties.get(JAR_CONTENT_ADDITIONAL)).getValue());
        deletedContent.removeAll(addedContent);
        addedContent.removeAll((List)((PropertyInfo) properties.get(JAR_CONTENT_ADDITIONAL)).getOldValue());
        
        updateContentDependency(deletedContent, addedContent);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if ( vcpi.getType() == VisualClassPathItem.TYPE_ARTIFACT ||
                 vcpi.getType() == VisualClassPathItem.TYPE_JAR ) {
                     boolean used = false; // now check if the file reference isn't used anymore
                     for (int i=0; i < allPaths.length; i++) {
                        PropertyInfo pi = (PropertyInfo)properties.get( allPaths[i] );
                        List values = (List)pi.getValue();
                        if (values == null) break;
                        for (Iterator v = values.iterator(); v.hasNext(); ) {
                            VisualClassPathItem valcpi = (VisualClassPathItem)v.next();
                            if (valcpi.getRaw().indexOf(vcpi.getRaw()) > -1) {
                                used = true;
                                break;
                            }
                        }
                     }
                     if (!used) {
                        refHelper.destroyReference(vcpi.getRaw());
                     }

            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( PROJECT );
        boolean changed = false;
        
        for( Iterator it = removed.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = vcpi.getRaw();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        File projDir = FileUtil.toFile(antProjectHelper.getProjectDirectory());
        for( Iterator it = added.iterator(); it.hasNext(); ) {
            VisualClassPathItem vcpi = (VisualClassPathItem)it.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                // add property to project.properties pointing to relativized 
                // library jar(s) if possible
                String prop = vcpi.getRaw();
                prop = prop.substring(2, prop.length()-1);
                String value = relativizeLibraryClasspath(prop, projDir);
                if (value != null) {
                    ep.setProperty(prop, value);
                    ep.setComment(prop, new String[]{
                        "# Property "+prop+" is set here just to make sharing of project simpler.", // NOI18N
                        "# The library definition has always preference over this property."}, false); // NOI18N
                    changed = true;
                }
            }
        }
        if (changed) {
            updateHelper.putProperties(PROJECT, ep);
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
            File f = antProjectHelper.resolveFile(paths[i]);
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
    
  protected class PropertyInfo {
        
        private PropertyDescriptor propertyDesciptor;
        private String rawValue;
        private String evaluatedValue;
        private Object value;
        private Object newValue;
        private String newValueEncoded;
        
        public PropertyInfo( PropertyDescriptor propertyDescriptor, String rawValue, String evaluatedValue ) {
            update(propertyDescriptor, rawValue, evaluatedValue); 
        }
        
        void update (PropertyDescriptor propertyDescriptor, String rawValue, String evaluatedValue ) {
            this.propertyDesciptor = propertyDescriptor;
            this.rawValue = rawValue;
            this.evaluatedValue = evaluatedValue;
            this.value = propertyDesciptor.parser.decode( rawValue, antProjectHelper, refHelper );
        }
        
        public PropertyDescriptor getPropertyDescriptor() {
            return propertyDesciptor;
        }
        
        public void encode() {            
            if ( isModified() ) {
                newValueEncoded = propertyDesciptor.parser.encode( newValue, antProjectHelper, refHelper);                
            }
            else {
                newValueEncoded = null;
            }
        }
        
        public Object getValue() {
            return isModified() ? newValue : value; 
        }
        
        public void setValue( Object value ) {
            newValue = value;
        }
        
        public String getNewValueEncoded() {
            return newValueEncoded;
        }
        
        public boolean isModified() {
            return newValue != null;
        }
        
        public Object getOldValue() {
            return value;
        }
    }
    
    static class PropertyDescriptor {
        interface Saver {
            public void save(PropertyInfo propertyInfo);
        }

        final PropertyParser parser;
        final String name;
        final String dest;
        final Saver saver;


        PropertyDescriptor(String name, String dest, PropertyParser parser, Saver saver) {
            this.name = name;
            this.dest = dest;
            this.saver = saver;
            this.parser = parser;
        }

        PropertyDescriptor( String name, String dest, PropertyParser parser ) {
            this(name, dest, parser, null);
        }

    }
    
    
    private static abstract class PropertyParser {
        
        public abstract Object decode( String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
        
        public abstract String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper );
        
    }
    
    private static class StringParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return raw;
        }        
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return (String)value;
        }
        
    }
    
    private static class BooleanParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            
            if ( raw != null ) {
               String lowecaseRaw = raw.toLowerCase();
               
               if ( lowecaseRaw.equals( "true") || // NOI18N
                    lowecaseRaw.equals( "yes") || // NOI18N
                    lowecaseRaw.equals( "enabled") ) // NOI18N
                   return Boolean.TRUE;                   
            }
            
            return Boolean.FALSE;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return ((Boolean)value).booleanValue() ? "true" : "false"; // NOI18N
        }
        
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {                    
            return ((Boolean)super.decode( raw, antProjectHelper, refHelper )).booleanValue() ? Boolean.FALSE : Boolean.TRUE;           
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper, refHelper );
        }
        
    }
    
    // XXX Define in the LibraryManager
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
        
    // Contains well known paths in the J2SEProject
    private static final String[][] WELL_KNOWN_PATHS = new String[][] {
        { JAVAC_CLASSPATH, NbBundle.getMessage( ArchiveProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) }, //NOI18N
        { BUILD_CLASSES_DIR, NbBundle.getMessage( ArchiveProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) } //NOI18N
    };
    
    private class PathParser extends PropertyParser {
        private String webLibraryElementName;
        private static final String TAG_PATH_IN_WAR = "path-in-war"; //NOI18N
        private static final String TAG_FILE = "file"; //NOI18N
        private static final String TAG_LIBRARY = "library"; //NOI18N

        public PathParser() {
            this(null);
        }

        public PathParser(String webLibraryElementName) {
            this.webLibraryElementName = webLibraryElementName;
        }

        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            Map warIncludesMap = createWarIncludesMap(antProjectHelper);
            if (raw != null) {
                String pe[] = PropertyUtils.tokenizePath( raw );
                for( int i = 0; i < pe.length; i++ ) {
                    final String pathItem = pe[i];
                    if (!warIncludesMap.containsKey(pathItem)) {
                        warIncludesMap.put(pathItem, VisualClassPathItem.PATH_IN_WAR_APPLET); // NONE);
                    }
                }
            }
            List cpItems = new ArrayList(warIncludesMap.size() );
            for (Iterator it = warIncludesMap.keySet().iterator(); it.hasNext();) {
                String pathItem = (String) it.next();
                String pathInWar = (String) warIncludesMap.get(pathItem);
                cpItems.add(createVisualClassPathItem(antProjectHelper, refHelper, pathItem, pathInWar));
            }
            return cpItems;
        }

        public String encode( Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            Element data = null;
            Element webModuleLibs = null;
            Document doc = null;
            if(webLibraryElementName != null) {
                final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
                data = updateHelper.getPrimaryConfigurationData(true);
                doc = data.getOwnerDocument();
                webModuleLibs = (Element) data.getElementsByTagNameNS(ns,
                                    webLibraryElementName).item(0);
                //prevent NPE thrown from older projects
                if (webModuleLibs == null) {
                    webModuleLibs = doc.createElementNS(ns, webLibraryElementName); //NOI18N
                    data.appendChild(webModuleLibs);
                }
                while (webModuleLibs.hasChildNodes()) {
                    webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
                }
            }
            StringBuffer sb = new StringBuffer();
            for ( Iterator it = ((List)value).iterator(); it.hasNext(); ) {
                VisualClassPathItem visualClassPathItem = (VisualClassPathItem)it.next();
                String pathItem = getPathItem(visualClassPathItem, refHelper);
                if(webLibraryElementName != null) {
                    webModuleLibs.appendChild(createLibraryElement(doc, pathItem, visualClassPathItem));
                }
                sb.append(pathItem);
                if ( it.hasNext() ) {
                    sb.append( File.pathSeparatorChar );
                }
            }
            if(webLibraryElementName != null) {
                updateHelper.putPrimaryConfigurationData(data, true);
            }
            return sb.toString();
        }

        private Element createLibraryElement(Document doc, String pathItem,
                VisualClassPathItem visualClassPathItem) {
            final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
            Element libraryElement = doc.createElementNS(ns,
                    TAG_LIBRARY);
            
            ArrayList files = new ArrayList();
            ArrayList dirs = new ArrayList();
            getFilesForItem(visualClassPathItem, files, dirs);
            if (files.size() > 0) {
                libraryElement.setAttribute(ATTR_FILES, "" + files.size());
            }
            if (dirs.size() > 0) {
                libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size());
            }           
            
            Element webFile = doc.createElementNS(ns, TAG_FILE);
            libraryElement.appendChild(webFile);
            webFile.appendChild(doc.createTextNode(pathItem));
            if (visualClassPathItem.getPathInWAR() != VisualClassPathItem.PATH_IN_WAR_NONE) {
                Element pathInWar = doc.createElementNS(ns,
                        TAG_PATH_IN_WAR);
                pathInWar.appendChild(doc.createTextNode(visualClassPathItem.getPathInWAR()));
                libraryElement.appendChild(pathInWar);
            }
            return libraryElement;
        }

        private Map createWarIncludesMap(AntProjectHelper antProjectHelper) {
            Map warIncludesMap = new LinkedHashMap();
            if (webLibraryElementName != null) {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                final String ns = abpt.getPrimaryConfigurationDataElementNamespace(true);
//                final String ns = WebProjectType.PROJECT_CONFIGURATION_NAMESPACE;
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webLibraryElementName).item(0);
                NodeList ch = webModuleLibs.getChildNodes();
                for (int i = 0; i < ch.getLength(); i++) {
                    if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element library = (Element) ch.item(i);
                        Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                        NodeList pathInWarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_WAR); 
                        warIncludesMap.put(findText(webFile), pathInWarElements.getLength() > 0 ?
                                findText(pathInWarElements.item(0)) : VisualClassPathItem.PATH_IN_WAR_NONE);
                    }
                }
            }
            return warIncludesMap;
        }

        private VisualClassPathItem createVisualClassPathItem(AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper, String pathItem, String pathInWar) {
            // First try to find out whether the item is well known classpath
            // in the J2SE project type
            for (int j = 0; j < WELL_KNOWN_PATHS.length; j++) {
                final String[] wellKnownPath = WELL_KNOWN_PATHS[j];
                if (wellKnownPath[0].equals(getAntPropertyName(pathItem))) {
                    return new VisualClassPathItem(pathItem, VisualClassPathItem.TYPE_CLASSPATH, pathItem,
                            wellKnownPath[1], pathInWar);
                }
            }
            if (pathItem.startsWith(LIBRARY_PREFIX)) {
                // Library from library manager
                // String eval = antProjectHelper.evaluate(getAntPropertyName(pathItem));
                String eval = pathItem.substring(LIBRARY_PREFIX.length(), pathItem.lastIndexOf('.')); //NOI18N
                Library lib = LibraryManager.getDefault().getLibrary(eval);
                if (lib != null) {
                    return new VisualClassPathItem(lib, VisualClassPathItem.TYPE_LIBRARY, pathItem, eval, pathInWar);
                } else {
                    return new VisualClassPathItem(null, VisualClassPathItem.TYPE_LIBRARY, pathItem, null,
                            pathInWar);
                }
            } else if (pathItem.startsWith(ANT_ARTIFACT_PREFIX)) {
                AntArtifact artifact = (AntArtifact) refHelper.findArtifactAndLocation(pathItem)[0];
                if (artifact != null) {
                    // Sub project artifact
                    String eval = artifact.getArtifactLocations()[0].toString();
                    return new VisualClassPathItem(artifact, VisualClassPathItem.TYPE_ARTIFACT, pathItem, eval,
                            pathInWar);
                } else {
                    return new VisualClassPathItem(null, VisualClassPathItem.TYPE_ARTIFACT, pathItem, null,
                            pathInWar);
                }
            } else {
                // Standalone jar or property
                String eval;
                if (isAntProperty(pathItem)) {
                    eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(getAntPropertyName(pathItem));
                } else {
                    eval = pathItem;
                }
                File f = (eval == null) ? null : antProjectHelper.resolveFile(eval);
                return new VisualClassPathItem(f, VisualClassPathItem.TYPE_JAR, pathItem, eval, pathInWar);
            }
        }

        private String getPathItem(VisualClassPathItem vcpi, ReferenceHelper refHelper) {
            switch (vcpi.getType()) {
                case VisualClassPathItem.TYPE_JAR:
                    String pathItem = vcpi.getRaw();
                    if (pathItem == null) {
                        // New file
                        return refHelper.createForeignFileReference((File) vcpi.getObject(),
                                JavaProjectConstants.ARTIFACT_TYPE_JAR);
                    } else {
                        return pathItem;
                    }
                case VisualClassPathItem.TYPE_ARTIFACT:
                    if (vcpi.getObject() != null) {
                        AntArtifact aa = (AntArtifact) vcpi.getObject();
                        return (String) refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                    } else {
                        return vcpi.getRaw();
                    }
                case VisualClassPathItem.TYPE_LIBRARY:
                case VisualClassPathItem.TYPE_CLASSPATH:
                    return vcpi.getRaw();
            }
            assert false: "unexpected type of classpath element";
            return null;
        }
    }
    
    /**
     * Extract nested text from a node.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent node
     * @return the nested text, or null if none was found
     */
    private static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }

    
    private static JavaPlatform findPlatform(String platformAntID) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();            
        for(int i = 0; i < platforms.length; i++) {
            String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name"); // NOI18N
            if (normalizedName != null && normalizedName.equals(platformAntID)) {
                return platforms[i];
            }
        }
        return null;
    }
    
    private static class PlatformParser extends PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform platform = findPlatform(raw);
            if (platform != null) {
                return platform.getDisplayName();
            }
            // if platform does not exist then return raw reference.
            return raw;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms ((String)value,
                    new Specification ("j2se",null)); // NOI18N
            if (platforms.length == 0) {
                // platform for this project does not exist. broken reference? its displayname should 
                // correspond to platform ID. so just return it:
                return (String)value;
            } else {
                return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
            }
        }
    }

    private void writeWebLibraries(AntProjectHelper antProjectHelper, ReferenceHelper refHelper, List value,
            final String elementName) {
        Element data = updateHelper.getPrimaryConfigurationData(true);
        org.w3c.dom.Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(abpt.getPrimaryConfigurationDataElementNamespace(true),
                elementName).item(0); //NOI18N

        //prevent NPE thrown from older projects
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), elementName); //NOI18N
            data.appendChild(webModuleLibs);
        }

        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }

        for (Iterator it = value.iterator(); it.hasNext();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) it.next();
            String library_tag_value = "";
            
            //TODO: prevent NPE from CustomizerCompile - need to investigate
            if (vcpi == null) {
                return;
            }

            switch (vcpi.getType()) {
                case VisualClassPathItem.TYPE_JAR:
                    String raw = vcpi.getRaw();

                    if (raw == null) {
                        // New file
                        File file = (File) vcpi.getObject();
                        String reference = refHelper.createForeignFileReference(file,
                                JavaProjectConstants.ARTIFACT_TYPE_JAR);
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
                    String reference = (String) refHelper.addReference(aa, aa.getArtifactLocations()[0]);
                    library_tag_value = reference;
                    break;
                case VisualClassPathItem.TYPE_CLASSPATH:
                    library_tag_value = vcpi.getRaw();
                    break;
            }

            Element library = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "library"); //NOI18N
            webModuleLibs.appendChild(library);
            Element webFile = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "file"); //NOI18N
            library.appendChild(webFile);
            webFile.appendChild(doc.createTextNode(library_tag_value));
            String piw = vcpi.getPathInWAR();
            if (piw != VisualClassPathItem.PATH_IN_WAR_NONE) {
                Element pathInWar = doc.createElementNS(abpt.getPrimaryConfigurationDataElementNamespace(true), "path-in-war"); //NOI18N
                pathInWar.appendChild(doc.createTextNode(vcpi.getPathInWAR()));
                library.appendChild(pathInWar);
            }
        }
        updateHelper.putPrimaryConfigurationData(data, true);
    }
    
    /** Store locations of libraries in the classpath param that have more the one
     * file into the properties in the following format:
     * 
     * <ul>
     * <li>libs.foo.classpath.libdir.1=C:/foo
     * <li>libs.foo.classpath.libdirs=1
     * <li>libs.foo.classpath.libfile.1=C:/bar/a.jar
     * <li>libs.foo.classpath.libfile.2=C:/bar/b.jar
     * <li>libs.foo.classpath.libfiles=2
     * </ul>
     * This is needed for the Ant copy task as it cannot copy more the one file
     * and it needs different handling for files and directories.
     * <br>
     * It removes all properties that match this format that were in the {@link #properties}
     * but are not in the {@link #classpath}.
     */
    public static void storeLibrariesLocations (Iterator /*<VisualClassPathItem>*/ classpath, Iterator /*<VisualClassPathItem>*/ oldClasspath, EditableProperties privateProps) {
        ArrayList exLibs = new ArrayList ();
        Iterator propKeys = privateProps.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = (String) propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            VisualClassPathItem item = (VisualClassPathItem)classpath.next();
            ArrayList /*File*/ files = new ArrayList ();
            ArrayList /*File*/ dirs = new ArrayList ();
            getFilesForItem (item, files, dirs);
            String key;
            String ref = item.getRaw();
            if (files.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                for (int i = 0; i < files.size(); i++) {
                    File f = (File) files.get(i);
                    key = getAntPropertyName(ref)+".libfile." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
            if (dirs.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                for (int i = 0; i < dirs.size(); i++) {
                    File f = (File) dirs.get(i);
                    key = getAntPropertyName(ref)+".libdir." + (i+1); //NOI18N
                    privateProps.setProperty (key, "" + f.getAbsolutePath()); //NOI18N
                    exLibs.remove(key);
                }
            }
        }
        Iterator unused = exLibs.iterator();
        while (unused.hasNext()) {
            privateProps.remove(unused.next());
        }
    }
    
    public static final void getFilesForItem (VisualClassPathItem item, List/*File*/ files, List/*File*/ dirs) {
        if (item.getType() == VisualClassPathItem.TYPE_LIBRARY) {
            List/*<URL>*/ roots = ((Library)item.getObject()).getContent("classpath");  //NOI18N
            for (Iterator it = roots.iterator(); it.hasNext();) {
                URL rootUrl = (URL) it.next();
                FileObject root = URLMapper.findFileObject (rootUrl);
                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                    root = FileUtil.getArchiveFile (root);
                }
                File f = FileUtil.toFile(root);
                if (f != null) {
                    if (f.isFile()) {
                        files.add(f); 
                    } else {
                        dirs.add(f);
                    }
                }
            }
        }
        if (item.getType() == VisualClassPathItem.TYPE_JAR) {
            File root = (File)item.getObject();
            if (root != null) {
                if (root.isFile()) {
                    files.add(root); 
                } else {
                    dirs.add(root);
                }
            }
        }
        if (item.getType() == VisualClassPathItem.TYPE_ARTIFACT) {
            AntArtifact artifact = (AntArtifact)item.getObject();
            String artifactFolder = artifact.getScriptLocation().getParent();
            URI roots[] = artifact.getArtifactLocations();
            for (int i = 0; i < roots.length; i++) {
                String root = artifactFolder + File.separator + roots [i];
                if (root.endsWith(File.separator)) {
                    dirs.add(new File (root));
                } else {
                    files.add(new File (root));
                }
            }
        }
    }

}
