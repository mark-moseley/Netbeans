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

package org.netbeans.modules.web.project;

import java.awt.Dialog;
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
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.openide.filesystems.FileLock;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.web.project.ui.WebCustomizerProvider;
import org.netbeans.modules.web.project.ui.WebPhysicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
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
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
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
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.ui.BrokenReferencesAlertPanel;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.modules.web.project.queries.SourceLevelQueryImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;


/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
final class WebProject implements Project, AntProjectListener, FileChangeListener {
    
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
    private FileWatch javaSourceFileWatch;

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
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator ());
        genFilesHelper = new GeneratedFilesHelper(helper);
        webModule = new ProjectWebModule (this, helper);
        apiWebModule = WebModuleFactory.createWebModule (webModule);
        webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiWebServicesClientSupport = WebServicesSupportFactory.createWebServicesClientSupport (webProjectWebServicesSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
        css = new CopyOnSaveSupport();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
        javaSourceFileWatch = new FileWatch(WebProjectProperties.SRC_DIR);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator (), new String[] {
            "${src.dir}/*.java" // NOI18N
        }, new String[] {
            "${build.classes.dir}/*.class" // NOI18N
        });
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
        String webModuleLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_WebModule"); //NOI18N
        String webPagesLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_DocBase"); //NOI18N
        String srcJavaLabel = org.openide.util.NbBundle.getMessage(WebCustomizerProvider.class, "LBL_Node_Sources"); //NOI18N
        
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addPrincipalSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}", webPagesLabel, /*XXX*/null, null);
        
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.SRC_DIR+"}", JavaProjectConstants.SOURCES_TYPE_JAVA, srcJavaLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}", WebProjectConstants.TYPE_DOC_ROOT, webPagesLabel, /*XXX*/null, null);
        sourcesHelper.addTypedSourceRoot("${"+WebProjectProperties.WEB_DOCBASE_DIR+"}/WEB-INF", WebProjectConstants.TYPE_WEB_INF, /*XXX I18N*/ "WEB-INF", /*XXX*/null, null);
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new ProjectWebModuleProvider (),
			new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            enterpriseResourceSupport,
            new WebActionProvider( this, helper, refHelper ),
            new WebPhysicalViewProvider(this, helper, evaluator (), spp, refHelper),
            new WebCustomizerProvider( this, helper, refHelper ),
            new ClassPathProviderImpl(helper, evaluator ()),
            new CompiledSourceForBinaryQuery(helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            new SourceLevelQueryImpl(helper, evaluator()),
            fileBuilt,
            new RecommendedTemplatesImpl(),
            sourcesHelper.createSources(),
            helper.createSharabilityQuery(evaluator(), new String[] {
                "${src.dir}", // NOI18N
                "${web.docbase.dir}", // NOI18N
            }, new String[] {
                "${dist.dir}", // NOI18N
                "${build.dir}", // NOI18N
            }),
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
        // currently ignored
        //TODO: should not be ignored!
    }
    
    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    ProjectWebModule getWebModule () {
        return webModule;
    }

    WebModule getAPIWebModule () {
        return apiWebModule;
    }
    
	WebServicesSupport getAPIWebServicesSupport () {
		return apiWebServicesSupport;
	}	
    
	WebServicesClientSupport getAPIWebServicesClientSupport () {
		return apiWebServicesClientSupport;
	}	

    FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator ().getProperty ("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
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
        return new WebProjectProperties (this, helper, refHelper);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (fo.getParent ().equals (libFolder)) {
            WebProjectProperties wpp = getWebProjectProperties();
            List cpItems = (List) wpp.get (WebProjectProperties.JAVAC_CLASSPATH);
            if (addLibrary (cpItems, fo)) {
                wpp.put (WebProjectProperties.JAVAC_CLASSPATH, cpItems);
                wpp.store ();
                try {
                    ProjectManager.getDefault ().saveProject (this);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    }

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private static synchronized void showBrokenReferencesAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown || 
            brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() ||
            !FoldersListSettings.getDefault().isShowAgainBrokenRefAlert()) {
                return;
        }
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        Object ok = NbBundle.getMessage(BrokenReferencesAlertPanel.class,"MSG_Broken_References_OK");
                        DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(), 
                            NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_Broken_References_Title"),
                            true, new Object[] {ok}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
                        Dialog dlg = null;
                        try {
                            dlg = DialogDisplayer.getDefault().createDialog(dd);
                            dlg.setVisible(true);
                        } finally {
                            if (dlg != null)
                                dlg.dispose();
                        }
                    } finally {
                        synchronized (WebProject.class) {
                            brokenAlertLastTime = System.currentTimeMillis();
                            brokenAlertShown = false;
                        }
                    }
                }
            });
    }
    
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
    
    /** Store configured project name. * /
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, / * OK if null * /data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
     */
    
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
            return WebProject.this.getName();
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
    
    private boolean addLibrary (List cpItems, FileObject lib) {
        boolean needsAdding = true;
        if (!lib.getExt().equalsIgnoreCase("jar") && !lib.getExt().equalsIgnoreCase("zip")) {
            return false;
        }
        for (Iterator vcpsIter = cpItems.iterator (); vcpsIter.hasNext ();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) vcpsIter.next ();

            if (vcpi.getType () != VisualClassPathItem.TYPE_JAR) {
                continue;
            }
            FileObject fo = helper.resolveFileObject(helper.getStandardPropertyEvaluator ().evaluate (vcpi.getEvaluated ()));
            if (lib.equals (fo)) {
                needsAdding = false;
                break;
            }
        }
        if (needsAdding) {
            String file = "${"+WebProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt ();
            String eval = helper.getStandardPropertyEvaluator ().evaluate (file);
            File f = null;
            if (eval != null) {
                f = helper.resolveFile(eval);
            }
            VisualClassPathItem cpItem = VisualClassPathItem.create ( f, VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add (cpItem);
        }
        return needsAdding;
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.LIBRARIES_DIR);
                WebProjectProperties wpp = getWebProjectProperties();
                    //DDDataObject initialization to be ready to listen on changes (#45771)
                    try {
                        FileObject ddFO = webModule.getDeploymentDescriptor();
                        if (ddFO != null) {
                            DataObject dobj = DataObject.find(ddFO);
                        }
                    } catch (org.openide.loaders.DataObjectNotFoundException ex) {}
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    List cpItems = (List) wpp.get (WebProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = helper.resolveFileObject(libFolderName);
                    FileObject libs [] = libFolder.getChildren ();
                    boolean anyChanged = false;
                    for (int i = 0; i < libs.length; i++) {
                        anyChanged = addLibrary (cpItems, libs [i]) || anyChanged;
                    }
                    if (anyChanged) {
                        wpp.put (WebProjectProperties.JAVAC_CLASSPATH, cpItems);
                        wpp.store ();
                        ProjectManager.getDefault ().saveProject (WebProject.this);
                    }
                    libFolder.addFileChangeListener (WebProject.this);
                }
                // Register copy on save support
                css.initialize();
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    WebProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName (),
                    WebProject.class.getResource("resources/build.xsl"),
                    true);
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
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (WebPhysicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
            webPagesFileWatch.init();
            javaSourceFileWatch.init();
        }

        protected void projectClosed() {

            webPagesFileWatch.reset();
            javaSourceFileWatch.reset();

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
            };
        }

    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
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
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            
            "Templates/JSP_Servlet/JSP.jsp",
            "Templates/JSP_Servlet/Html.html",
            "Templates/JSP_Servlet/Servlet.java",
            "Templates/Classes/Class.java",
            "Templates/Other/Folder",
            "Templates/JSP_Servlet/WebService",
            "Templates/JSP_Servlet/WebServiceClient"
        };
        
        public String[] getRecommendedTypes() {
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
    
}
