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

package org.netbeans.modules.j2ee.earproject;

//import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
//import java.lang.ref.SoftReference;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
//import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
//import org.netbeans.api.java.platform.JavaPlatform;
//import org.netbeans.api.java.platform.JavaPlatformManager;
//import org.netbeans.api.java.project.JavaProjectConstants;
//import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.api.project.ant.AntArtifact;
//import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.common.classpath.ClassPathProviderImpl;
//import org.netbeans.modules.j2ee.ejbjarproject.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.j2ee.earproject.ui.EarCustomizerProvider;
import org.netbeans.modules.j2ee.earproject.ui.LogicalViewProvider;
import org.netbeans.modules.j2ee.common.ui.J2eeArchiveLogicalViewProvider;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.common.J2eeArchiveActionProvider;
import org.netbeans.modules.j2ee.common.J2eeProject;

import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
//import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
//import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.SubprojectProvider;
//import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
//import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
//import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
//import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
//import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
//import org.netbeans.modules.j2ee.ejbjarproject.ui.BrokenReferencesAlertPanel;
//import org.netbeans.modules.j2ee.ejbjarproject.ui.FoldersListSettings;
//import org.netbeans.modules.j2ee.ejbjarproject.queries.SourceLevelQueryImpl;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
//import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.netbeans.modules.j2ee.common.ui.IconBaseProvider;

/**
 * Represents an Enterprise Application project.
 *
 * This is the project api centric view of the enterprise application.
 * 
 * @author vince kraemer
 * @see WebProject
 */
public final class EarProject implements J2eeProject, Project, AntProjectListener, FileChangeListener {
    
    private static final Icon EAR_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectEar appModule;
//    private FileObject libFolder = null;
    private AntBasedProjectType abpt;
    
    EarProject(final AntProjectHelper helper, AntBasedProjectType abpt) throws IOException {
        this.helper = helper;
        this.abpt = abpt;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator ());
        genFilesHelper = new GeneratedFilesHelper(helper);
        appModule = new ProjectEar (this); // , helper);
        lookup = createLookup(aux);
//        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "EarProject[" + getProjectDirectory() + "]"; // NOI18N
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
//        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(helper.getStandardPropertyEvaluator (), new String[] {
//            "${src.dir}/*.java" // NOI18N
//        }, new String[] {
//            "${build.classes.dir}/*.class" // NOI18N
//        });
//        final SourcesHelper sourcesHelper = new SourcesHelper(helper, evaluator());
//        String webModuleLabel = org.openide.util.NbBundle.getMessage(EjbJarCustomizerProvider.class, "LBL_Node_WebModule"); //NOI18N
//        String webPagesLabel = org.openide.util.NbBundle.getMessage(EjbJarCustomizerProvider.class, "LBL_Node_DocBase"); //NOI18N
//        String srcJavaLabel = org.openide.util.NbBundle.getMessage(EjbJarCustomizerProvider.class, "LBL_Node_Sources"); //NOI18N
//        
//        sourcesHelper.addPrincipalSourceRoot("${"+EjbJarProjectProperties.SOURCE_ROOT+"}", webModuleLabel, /*XXX*/null, null);
//        sourcesHelper.addPrincipalSourceRoot("${"+EjbJarProjectProperties.SRC_DIR+"}", srcJavaLabel, /*XXX*/null, null);
//        sourcesHelper.addPrincipalSourceRoot("${"+EjbJarProjectProperties.WEB_DOCBASE_DIR+"}", webPagesLabel, /*XXX*/null, null);
//        
//        sourcesHelper.addTypedSourceRoot("${"+EjbJarProjectProperties.SRC_DIR+"}", JavaProjectConstants.SOURCES_TYPE_JAVA, srcJavaLabel, /*XXX*/null, null);
//        //sourcesHelper.addTypedSourceRoot("${"+EjbJarProjectProperties.WEB_DOCBASE_DIR+"}", EjbProjectConstants.TYPE_DOC_ROOT, webPagesLabel, /*XXX*/null, null);
//        sourcesHelper.addTypedSourceRoot("${"+EjbJarProjectProperties.WEB_DOCBASE_DIR+"}/META-INF", EjbProjectConstants.TYPE_META_INF, /*XXX I18N*/ "META-INF", /*XXX*/null, null);
//        ProjectManager.mutex().postWriteRequest(new Runnable() {
//            public void run() {
//                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
//            }
//        });
        return Lookups.fixed(new Object[] {
            new Info(),
//            aux,
//            helper.createCacheDirectoryProvider(),
            spp,
// //           new ProjectEarProvider (),
            appModule, //implements J2eeModuleProvider
            new J2eeArchiveActionProvider( this, helper, refHelper, abpt),
            new LogicalViewProvider(this, helper, evaluator (), spp, refHelper, abpt),
            new MyIconBaseProvider(),
            new EarCustomizerProvider( this, helper, refHelper, abpt ),
            new ClassPathProviderImpl(helper, evaluator()),
//            new CompiledSourceForBinaryQuery(helper),
//            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
//            new SourceLevelQueryImpl(helper, evaluator()),
//            fileBuilt,
            new RecommendedTemplatesImpl(),
//            sourcesHelper.createSources()
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
//        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
//            // Could be various kinds of changes, but name & displayName might have changed.
//            Info info = (Info)getLookup().lookup(ProjectInformation.class);
//            info.firePropertyChange(ProjectInformation.PROP_NAME);
//            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
//        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored
        //TODO: should not be ignored!
    }
    
    public String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (ArchiveProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
//        return storedName == null ? "build.xml" : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    public ProjectEar getAppModule () {
        return appModule;
    }
    
    public FileObject getSourceDirectory() {
        String srcDir = helper.getStandardPropertyEvaluator ().getProperty ("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
    }    
    
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
//        FileObject fo = fe.getFile ();
//        checkLibraryFolder (fo);
    }
    
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
//        FileObject fo = fe.getFile ();
//        checkLibraryFolder (fo);
    }
    
//    private void checkLibraryFolder (FileObject fo) {
//        if (fo.getParent ().equals (libFolder)) {
//            EjbJarProjectProperties wpp = new EjbJarProjectProperties (this, helper, refHelper);
//            List cpItems = (List) wpp.get (EjbJarProjectProperties.JAVAC_CLASSPATH);
//            if (addLibrary (cpItems, fo)) {
//                wpp.put (EjbJarProjectProperties.JAVAC_CLASSPATH, cpItems);
//                wpp.store ();
//                try {
//                    ProjectManager.getDefault ().saveProject (this);
//                } catch (IOException e) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//                }
//            }
//        }
//    }
    
//    /** Last time in ms when the Broken References alert was shown. */
//    private static long brokenAlertLastTime = 0;
//    
//    /** Is Broken References alert shown now? */
//    private static boolean brokenAlertShown = false;
//
//    /** Timeout within which request to show alert will be ignored. */
//    private static int BROKEN_ALERT_TIMEOUT = 1000;
//    
//    private static synchronized void showBrokenReferencesAlert() {
//        // Do not show alert if it is already shown or if it was shown
//        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
//        if (brokenAlertShown || 
//            brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() ||
//            !FoldersListSettings.getDefault().isShowAgainBrokenRefAlert()) {
//                return;
//        }
//        brokenAlertShown = true;
//        SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    try {
//                        Object ok = NbBundle.getMessage(BrokenReferencesAlertPanel.class,"MSG_Broken_References_OK");
//                        DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(), 
//                            NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_Broken_References_Title"),
//                            true, new Object[] {ok}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                        Dialog dlg = null;
//                        try {
//                            dlg = DialogDisplayer.getDefault().createDialog(dd);
//                            dlg.setVisible(true);
//                        } finally {
//                            if (dlg != null)
//                                dlg.dispose();
//                        }
//                    } finally {
//                        synchronized (EarProject.class) {
//                            brokenAlertLastTime = System.currentTimeMillis();
//                            brokenAlertShown = false;
//                        }
//                    }
//                }
//            });
//    }
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "EAR????"; // NOI18N
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
            return EarProject.this.getName();
        }
        
        public String getDisplayName() {
            return EarProject.this.getName();
        }
        
        public Icon getIcon() {
            return EAR_PROJECT_ICON;
        }
        
        public Project getProject() {
            return EarProject.this;
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
                EarProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                EarProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
//    
    private boolean addLibrary (List cpItems, FileObject lib) {
        boolean needsAdding = true;
        for (Iterator vcpsIter = cpItems.iterator (); vcpsIter.hasNext ();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) vcpsIter.next ();

            if (vcpi.getType () != VisualClassPathItem.TYPE_JAR) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject (new File(helper.getStandardPropertyEvaluator ().evaluate (vcpi.getEvaluated ())));
            if (lib.equals (fo)) {
                needsAdding = false;
                break;
            }
        }
        if (needsAdding) {
            String file = "${"+ArchiveProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt ();
            String eval = helper.getStandardPropertyEvaluator ().evaluate (file);
            VisualClassPathItem cpItem = //new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, VisualClassPathItem.PATH_IN_WAR_LIB);
                VisualClassPathItem.create( file,  VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add (cpItem);
        }
        return needsAdding;
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.LIBRARIES_DIR);
                EarProjectProperties wpp = new EarProjectProperties (EarProject.this, helper, refHelper,abpt);
                getAppModule().setModules(wpp.getModuleMap());
                if (libFolderName != null && new File (libFolderName).isDirectory ()) {
                    List cpItems = (List) wpp.get (EarProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = FileUtil.toFileObject (new File (libFolderName));
                    FileObject libs [] = libFolder.getChildren ();
                    boolean anyChanged = false;
                    for (int i = 0; i < libs.length; i++) {
                        anyChanged = addLibrary (cpItems, libs [i]) || anyChanged;
                    }
                    if (anyChanged) {
                        wpp.put (EarProjectProperties.JAVAC_CLASSPATH, cpItems);
                        wpp.store ();
                        ProjectManager.getDefault ().saveProject (EarProject.this);
                    }
                    libFolder.addFileChangeListener (EarProject.this);
                }
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    EarProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName (),
                    EarProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            //check the config context path
//            String ctxRoot = webModule.getContextPath ();
//            if (ctxRoot == null || ctxRoot.equals ("")) {
//                String sysName = "/" + getProjectDirectory ().getName (); //NOI18N
//                sysName = sysName.replace (' ', '_'); //NOI18N
//                webModule.setContextPath (sysName);
//            }
            
            // register project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    try {
                        ProjectManager.getDefault().saveProject(EarProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            if (J2eeArchiveLogicalViewProvider.hasBrokenLinks(helper, refHelper)) {
                BrokenReferencesSupport.showAlert();
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
//    
//    /**
//     * Exports the main JAR as an official build product for use from other scripts.
//     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
//     */
//    private final class AntArtifactProviderImpl implements AntArtifactProvider {
//
//        public AntArtifact[] getBuildArtifacts() {
//            return new AntArtifact[] {
//                // XXX provide WAR as an artifact (for j2ee application?):
////                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.war", "war", "clean"), // NOI18N
//            };
//        }
//
//    }
//    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] { 
//            "java-classes",         // NOI18N
//            "ejb-types",            // NOI18N
//            "java-beans",           // NOI18N
//            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
//            "simple-files"          // NOI18N
        };
//        
        private static final String[] PRIVILEGED_NAMES = new String[] {
//            
//            "Templates/J2EE/Session",
            "Templates/XML/XMLWizard",
            "Templates/Other/Folder"
        };
//        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
//        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
//        
    }
        
        class MyIconBaseProvider implements IconBaseProvider {
            public String getIconBase() {
                return "org/netbeans/modules/j2ee/earproject/ui/resources/";
            }
        }
        
        FileObject getFileObject(String propname) {
            String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
            if (prop != null) {
                return helper.resolveFileObject(prop);
            } else {
                return null;
            }
        }

    public String getServerID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_TYPE);
    }

    public String getServerInstanceID () {
        return helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public String getJ2eePlatformVersion () {
        return  helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_PLATFORM);
    }
}
