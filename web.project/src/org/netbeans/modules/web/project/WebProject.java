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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.openide.util.WeakListeners;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathExtender;
import org.netbeans.modules.web.project.queries.*;
import org.netbeans.modules.web.project.ui.WebPhysicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.web.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.modules.web.project.ui.BrokenServerSupport;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
public final class WebProject implements Project, AntProjectListener, FileChangeListener, PropertyChangeListener {
    
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectWebModule webModule;
    private FileObject libFolder = null;
    private CopyOnSaveSupport css;
    private WebModule apiWebModule;
    private WebProjectWebServicesSupport webProjectWebServicesSupport;
    private WebServicesSupport apiWebServicesSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private WebContainerImpl enterpriseResourceSupport;
    private FileWatch webPagesFileWatch;
    private PropertyChangeListener j2eePlatformListener;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final UpdateHelper updateHelper;
    private final AuxiliaryConfiguration aux;

    private class FileWatch implements AntProjectListener, FileChangeListener {

        private String propertyName;

        private FileObject fileObject = null;
        private boolean watchRename = false;

        public FileWatch(String property) {
            this.propertyName = property;
        }

        public void init() {
            helper.addAntProjectListener(this);
            updateFileChangeListener();
        }

        public void reset() {
            helper.removeAntProjectListener(this);
            setFileObject(null);
        }

        public void updateFileChangeListener() {
            File resolvedFile;
            FileObject fo = null;
            String propertyValue = helper.getStandardPropertyEvaluator().getProperty(propertyName);
            if (propertyValue != null) {
                String resolvedPath = helper.resolvePath(propertyValue);
                resolvedFile = new File(resolvedPath).getAbsoluteFile();
                if (resolvedFile != null) {
                    File f = resolvedFile;
                    while (f != null && (fo = FileUtil.toFileObject(f)) == null) {
                        f = f.getParentFile();
                    }
                    watchRename = f == resolvedFile;
                } else {
                    watchRename = false;
                }
            } else {
                resolvedFile = null;
                watchRename = false;
            }
            setFileObject(fo);
        }

        private void setFileObject(FileObject fo) {
            if (!isEqual(fo, fileObject)) {
                if (fileObject != null) {
                    fileObject.removeFileChangeListener(this);
                }
                fileObject = fo;
                if (fileObject != null) {
                    fileObject.addFileChangeListener(this);
                }
            }
        }

        private boolean isEqual(Object object1, Object object2) {
            if (object1 == object2) {
                return true;
            }
            if(object1 == null) {
                return false;
            }
            return object1.equals(object2);
        }

        // AntProjectListener

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        public void propertiesChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        // FileChangeListener

        public void fileFolderCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDataCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileChanged(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileDeleted(FileEvent fe) {
            updateFileChangeListener();
        }

        public void fileRenamed(FileRenameEvent fe) {
            if(watchRename && fileObject.isValid()) {
                File f = new File(helper.getStandardPropertyEvaluator().getProperty(propertyName));
                if(f.getName().equals(fe.getName())) {
                    EditableProperties properties = new EditableProperties();
                    properties.setProperty(propertyName, new File(f.getParentFile(), fe.getFile().getName()).getPath());
                    Utils.updateProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
                    getWebProjectProperties().store();
                }
            }
            updateFileChangeListener();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };
    
    WebProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
        webModule = new ProjectWebModule (this, helper);
        apiWebModule = WebModuleFactory.createWebModule (webModule);
        webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiWebServicesClientSupport = WebServicesSupportFactory.createWebServicesClientSupport (webProjectWebServicesSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        this.updateHelper = new UpdateHelper (this, this.helper, this.aux, this.genFilesHelper, UpdateHelper.createDefaultNotifier());
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
        css = new CopyOnSaveSupport();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        PropertyEvaluator e = helper.getStandardPropertyEvaluator();
        e.addPropertyChangeListener(WeakListeners.propertyChange(this, e));
        return e;
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }
    
    ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new ProjectWebModuleProvider (),
			new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            enterpriseResourceSupport,
            new WebActionProvider( this, this.updateHelper ),
            new WebPhysicalViewProvider(this, this.updateHelper, evaluator (), spp, refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper),        
            new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()),
            new CompiledSourceForBinaryQuery(this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            new JavadocForBinaryQueryImpl(this.helper, evaluator()),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new UnitTestForSourceQueryImpl(getSourceRoots(),getTestSourceRoots()),
            new SourceLevelQueryImpl(evaluator()),
            new WebSources (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new WebSharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl(this.updateHelper),
            new WebFileBuiltQuery (this.helper, evaluator(),getSourceRoots(),getTestSourceRoots()),
            helper.createSharabilityQuery(evaluator(), new String[] {
                "${src.dir}", // NOI18N
                "${web.docbase.dir}", // NOI18N
                "${" + WebProjectProperties.TEST_SRC_DIR + "}", // NOI18N
            }, new String[] {
                "${dist.dir}", // NOI18N
                "${build.dir}", // NOI18N
            }),
            new WebProjectClassPathExtender(this, updateHelper, evaluator(), refHelper),
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */    
    public synchronized SourceRoots getSourceRoots() {        
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    
    
    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(WebProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }
    
    ProjectWebModule getWebModule () {
        return webModule;
    }

    public WebModule getAPIWebModule () {
        return apiWebModule;
    }
    
	WebServicesSupport getAPIWebServicesSupport () {
		return apiWebServicesSupport;
	}	
    
	WebServicesClientSupport getAPIWebServicesClientSupport () {
		return apiWebServicesClientSupport;
	}	

    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
    }    
    
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public WebProjectProperties getWebProjectProperties() {
        return new WebProjectProperties (this, updateHelper, eval, refHelper);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (!FileUtil.isArchiveFile(fo))
            return;
        
        if (fo.getParent ().equals (libFolder)) {
            WebProjectClassPathExtender cpExt = (WebProjectClassPathExtender)getLookup ().lookup (WebProjectClassPathExtender.class);
            if (cpExt == null) {
                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: " + getProjectDirectory().getPath()); //NOI18N
                return;
            }

            try {
                cpExt.addArchiveFile(fo);
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Mutex.Action() {
                        public Object run() {
                            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            String classpath = Utils.toClasspathString(platform.getClasspathEntries());
                            ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                            } catch (IOException e) {
                                ErrorManager.getDefault().notify(e);
                            }
                            return null;
                        }
                    });
                }
            }
        };
        platform.addPropertyChangeListener(j2eePlatformListener);
    }
    
    public void unregisterJ2eePlatformListener(J2eePlatform platform) {
        if (j2eePlatformListener != null) {
            platform.removePropertyChangeListener(j2eePlatformListener);
        }
    }
    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return WebProject.this.getName();
        }
        
        public String getDisplayName() {
            return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
                public Object run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
        }
        
        public Icon getIcon() {
            return WEB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return WebProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                WebProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.LIBRARIES_DIR);
                
                //DDDataObject initialization to be ready to listen on changes (#45771)
                try {
                    FileObject ddFO = webModule.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject dobj = DataObject.find(ddFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING
                }
                
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    WebProjectClassPathExtender cpExt = (WebProjectClassPathExtender) WebProject.this.getLookup().lookup(WebProjectClassPathExtender.class);
                    libFolder = helper.resolveFileObject(libFolderName);
                    if (cpExt != null) {
                        FileObject children [] = libFolder.getChildren ();
                        List libs = new LinkedList();
                        for (int i = 0; i < children.length; i++) {
                            if (FileUtil.isArchiveFile(children[i]))
                                libs.add(children[i]);
                        }
                        FileObject[] libsArray = new FileObject[libs.size()];
                        libs.toArray(libsArray);
                        cpExt.addArchiveFiles(WebProjectProperties.JAVAC_CLASSPATH, libsArray, ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                        libFolder.addFileChangeListener (WebProject.this);
                    }
                    else
                        ErrorManager.getDefault().log("Cannot find WebProjectClassPathExtender in the lookup of the project " + WebProject.this.getProjectDirectory() + "."); // NOI18N
                }

                // Register copy on save support
                css.initialize();
                
                
                // Check up on build scripts.
                if (updateHelper.isCurrent()) {
                    //Refresh build-impl.xml only for webproject/2
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        WebProject.class.getResource("resources/build-impl.xsl"),
                        true);
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        WebProject.class.getResource("resources/build.xsl"),
                        true);
                    
                    WebProjectProperties wpp = getWebProjectProperties();
                    String servInstID = (String) wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                        wpp.setServerInstance(servInstID);
                        wpp.store();
                    } else {
                        // if there is some server instance of the type which was used
                        // previously do not ask and use it
                        String serverType = (String) wpp.get(WebProjectProperties.J2EE_SERVER_TYPE);
                        if (serverType != null) {
                            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                            if (servInstIDs.length > 0) {
                                wpp.setServerInstance(servInstIDs[0]);
                                wpp.store();
                                platform = Deployment.getDefault().getJ2eePlatform(servInstIDs[0]);
                            }
                        }
                        if (platform == null) {
                            BrokenServerSupport.showAlert();
                        }
                    }
                }
                
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null) {
                String sysName = "/" + getProjectDirectory ().getName (); //NOI18N
                sysName = sysName.replace (' ', '_'); //NOI18N
                webModule.setContextPath (sysName);
            }
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (WebPhysicalViewProvider.hasBrokenLinks(updateHelper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
            webPagesFileWatch.init();
        }

        protected void projectClosed() {
            webPagesFileWatch.reset();

            // listen to j2ee platform classpath changes
            WebProjectProperties wpp = getWebProjectProperties();
            String servInstID = (String)wpp.get(WebProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // Unregister copy on save support
            try {
                css.cleanup();
            } 
            catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR, "dist.war", evaluator(), "dist", "clean"), // NOI18N
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE, "dist.ear.war", evaluator(), "dist-ear", "clean-ear") // NOI18N
            };
        }

    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        RecommendedTemplatesImpl (UpdateHelper helper) {
            this.helper = helper;
        }
        
        private UpdateHelper helper;

        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] { 
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            "web-types",            // NOI18N
            "junit",                // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            
            "Templates/JSP_Servlet/JSP.jsp",
            "Templates/JSP_Servlet/Html.html",
            "Templates/JSP_Servlet/Servlet.java",
            "Templates/Classes/Class.java",
            "Templates/Other/Folder",
            "Templates/Classes/Package", // NOI18N
            "Templates/JSP_Servlet/WebService",
            "Templates/JSP_Servlet/MessageHandler",
            "Templates/JSP_Servlet/WebServiceClient"
        };
        
        public String[] getRecommendedTypes() {
//            EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//            // if the project has no main class, it's not really an application
//            boolean isLibrary = ep.getProperty (WebProjectProperties.MAIN_CLASS) == null || "".equals (ep.getProperty (WebProjectProperties.MAIN_CLASS)); // NOI18N
//            return isLibrary ? LIBRARY_TYPES : APPLICATION_TYPES;
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }

    public class CopyOnSaveSupport extends FileChangeAdapter implements PropertyChangeListener {
        private FileObject docBase = null;

        /** Creates a new instance of CopyOnSaveSupport */
        public CopyOnSaveSupport() {
        }

        public void initialize() throws FileStateInvalidException {
            docBase = getWebModule().getDocumentBase();
            if (docBase != null) {
                docBase.getFileSystem().addFileChangeListener(this);
            }
            ProjectInformation info = (ProjectInformation) getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                info.addPropertyChangeListener (this);
            }
        }

        public void cleanup() throws FileStateInvalidException {
            if (docBase != null) {
                docBase.getFileSystem().removeFileChangeListener(this);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WebProjectProperties.WEB_DOCBASE_DIR)) {
                try {
                    cleanup();
                    initialize();
                } catch (org.openide.filesystems.FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    
        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged (FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        public void fileDataCreated (FileEvent fe) {
            try {
                handleCopyFileToDestDir(fe.getFile());
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject docBase = getWebModule().getDocumentBase();
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    handleCopyFileToDestDir(fo);
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(docBase, parent)) {
                        path = FileUtil.getRelativePath(docBase, fo.getParent()) +
                            "/" + fe.getName() + "." + fe.getExt();
                    }
                    else {
                        path = fe.getName() + "." + fe.getExt();
                    }
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    handleDeleteFileInDestDir(path);
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        public void fileDeleted(FileEvent fe) {
            try {
                FileObject fo = fe.getFile();
                FileObject docBase = getWebModule().getDocumentBase();
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    handleDeleteFileInDestDir(path);
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        private boolean isSynchronizationAppropriate(String filePath) {
            if (filePath.startsWith("WEB-INF/classes")) {
                return false;
            }
            if (filePath.startsWith("WEB-INF/src")) {
                return false;
            }
            if (filePath.startsWith("WEB-INF/lib")) {
                return false;
            }
            return true;
        }
        
        private void handleDeleteFileInDestDir(String resourcePath) throws IOException {
            FileObject webBuildBase = getWebModule().getContentDirectory();
            if (webBuildBase != null) {
                // project was built
                FileObject toDelete = webBuildBase.getFileObject(resourcePath);
                if (toDelete != null) {
                    toDelete.delete();
                }
            }
        }
        
        /** Copies a content file to an appropriate  destination directory, 
         * if applicable and relevant.
         */
        private void handleCopyFileToDestDir(FileObject fo) throws IOException {
            if (!fo.isVirtual()) {
                FileObject docBase = getWebModule().getDocumentBase();
                if (FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) 
                        return;
                    FileObject webBuildBase = getWebModule().getContentDirectory();
                    if (webBuildBase != null) {
                        // project was built
                        FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                        if (!fo.isFolder()) {
                            InputStream is = null;
                            OutputStream os = null;
                            FileLock fl = null;
                            try {
                                is = fo.getInputStream();
                                fl = destFile.lock();
                                os = destFile.getOutputStream(fl);
                                FileUtil.copy(is, os);
                            }
                            finally {
                                if (is != null) {
                                    is.close();
                                }
                                if (os != null) {
                                    os.close();
                                }
                                if (fl != null) {
                                    fl.releaseLock();
                                }
                            }
                            //System.out.println("copied + " + FileUtil.copy(fo.getInputStream(), destDir, fo.getName(), fo.getExt()));
                        }
                    }
                }
            }
        }

        /** Returns the destination (parent) directory needed to create file with relative path path under webBuilBase
         */
        private FileObject ensureDestinationFileExists(FileObject webBuildBase, String path, boolean isFolder) throws IOException {
            FileObject current = webBuildBase;
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String pathItem = st.nextToken();
                FileObject newCurrent = current.getFileObject(pathItem);
                if (newCurrent == null) {
                    // need to create it
                    if (isFolder || st.hasMoreTokens()) {
                        // create a folder
                        newCurrent = FileUtil.createFolder(current, pathItem);
                    }
                    else {
                        newCurrent = FileUtil.createData(current, pathItem);
                    }
                }
                current = newCurrent;
            }
            return current;
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WebProjectProperties.JAVAC_CLASSPATH) ||
                evt.getPropertyName().equals(WebProjectProperties.WAR_CONTENT_ADDITIONAL)) {
            ProjectManager.mutex().postWriteRequest(new Runnable () {
                public void run() {
                    ClassPathSupport cs = new ClassPathSupport( evaluator(), refHelper, helper, 
                                                WebProjectProperties.WELL_KNOWN_PATHS, 
                                                WebProjectProperties.LIBRARY_PREFIX, 
                                                WebProjectProperties.LIBRARY_SUFFIX, 
                                                WebProjectProperties.ANT_ARTIFACT_PREFIX );

                    EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                    //update lib references in private properties
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ArrayList l = new ArrayList ();
                    l.addAll(cs.itemsList(props.getProperty(WebProjectProperties.JAVAC_CLASSPATH),  WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
                    l.addAll(cs.itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL),  WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
                    WebProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            });
        }
    }
}
