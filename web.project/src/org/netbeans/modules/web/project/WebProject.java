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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.queries.CompiledSourceForBinaryQuery;
import org.netbeans.modules.web.project.ui.WebCustomizerProvider;
import org.netbeans.modules.web.project.ui.WebPhysicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 */
final class WebProject implements Project, AntProjectListener, FileChangeListener {
    
    private static final Icon WEB_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectWebModule webModule;
    private FileObject libFolder = null;
    
    WebProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux);
        genFilesHelper = new GeneratedFilesHelper(helper);
        webModule = new ProjectWebModule (this, helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(new String[] {
            "${src.dir}/*.java" // NOI18N
        }, new String[] {
            "${build.classes.dir}/*.class" // NOI18N
        });
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            webModule,
            J2eeModuleProvider.createJ2eeProjectMarker (webModule),
            new WebActionProvider( this, helper ),
            new WebPhysicalViewProvider(this, helper, spp),
            new WebCustomizerProvider( this, helper, refHelper ),
            new ClassPathProviderImpl(helper),
            new CompiledSourceForBinaryQuery(helper),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
            fileBuilt,
            new WebSources(helper)
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
    
    // Package private methods -------------------------------------------------
    
    ProjectWebModule getWebModule () {
        return webModule;
    }
    
    FileObject getSourceDirectory() {
        String srcDir = helper.evaluate("src.dir"); // NOI18N
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
    
    private void checkLibraryFolder (FileObject fo) {
        if (fo.getParent ().equals (libFolder)) {
            WebProjectProperties wpp = new WebProjectProperties (this, helper, refHelper);
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
    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return helper.getName();
        }
        
        public String getDisplayName() {
            return helper.getDisplayName();
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
                GeneratedFilesHelper.BUILD_XML_PATH,
                WebProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
    
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
            String file = "${"+WebProjectProperties.LIBRARIES_DIR+"}/"+lib.getNameExt ();
            String eval = helper.getStandardPropertyEvaluator ().evaluate (file);
            VisualClassPathItem cpItem = new VisualClassPathItem( file, VisualClassPathItem.TYPE_JAR, file, eval, VisualClassPathItem.PATH_IN_WAR_LIB);
            cpItems.add (cpItem);
        }
        return needsAdding;
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            try {
                //Check if external source root needs to be registered
                String externalRoot = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.SOURCE_ROOT);
                if (externalRoot != null && !(externalRoot.equals ("") || externalRoot.equals ("."))) { //NOI18N
                    FileObject root [] = FileUtil.fromFile (FileUtil.normalizeFile (new java.io.File (externalRoot)));
                    if (root != null && root.length == 1) {
                        FileOwnerQuery.markExternalOwner (root [0], WebProject.this, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    }
                }
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.LIBRARIES_DIR);
                WebProjectProperties wpp = new WebProjectProperties (WebProject.this, helper, refHelper);
                if (libFolderName != null && new File (libFolderName).isDirectory ()) {
                    List cpItems = (List) wpp.get (WebProjectProperties.JAVAC_CLASSPATH);
                    FileObject libFolder = FileUtil.toFileObject (new File (libFolderName));
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
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    WebProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    WebProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null || ctxRoot.equals ("")) {
                String sysName = "/" + getProjectDirectory ().getName (); //NOI18N
                sysName = sysName.replace (' ', '_'); //NOI18N
                webModule.setContextPath (sysName);
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                // XXX provide WAR as an artifact (for j2ee application?):
//                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.war", "war", "clean"), // NOI18N
            };
        }

    }
    
}
