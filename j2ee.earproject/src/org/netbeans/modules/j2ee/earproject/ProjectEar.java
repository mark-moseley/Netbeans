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

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.DeployOnSaveSupport;
import org.netbeans.modules.j2ee.earproject.model.ApplicationMetadataModelImpl;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * An enterprise application project's j2eeserver implementation
 *
 * @see ProjectEar
 * @author  vince kraemer
 */
public final class ProjectEar extends J2eeApplicationProvider
        implements
        ModuleChangeReporter,
        EjbChangeDescriptor,
        PropertyChangeListener,
        EarImplementation,
        J2eeApplicationImplementation {
    
    public static final String FILE_DD        = "application.xml";//NOI18N

    private static final Logger LOGGER = Logger.getLogger(ProjectEar.class.getName());

    private final EarProject project;
    
    private PropertyChangeSupport propertyChangeSupport;
    private J2eeApplication j2eeApplication;
    // XXX remove this property after metadata model is implemented
    /* application reference for JAVA EE 5 only (if application.xml doesn't exist)  */
    private Application application;
    private MetadataModel<ApplicationMetadata> metadataModel;
    
    private final DeployOnSaveSupport deployOnSaveSupport = new DeployOnSaveSupportProxy();
    
    ProjectEar (EarProject project) { // ], AntProjectHelper helper) {
        this.project = project;
        AntProjectHelper helper = project.getAntProjectHelper();
        helper.getStandardPropertyEvaluator().addPropertyChangeListener(this);
    }
    
    /**
     * Get the file object for deployment descriptor of EAR project.
     * <b>Important note:</b> This method can return <code>null</code> (which is also default for JAVA EE 5).
     * <p>
     * This method should be used <b>only</b> when model is needed for writing.
     * @return deployment descriptor or <code>null</null> when <i>application.xml</i>
     *         doesn't exists.
     */
    public FileObject getDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject(FILE_DD);
        }
        return null;
    }
    
    public FileObject getMetaInf() {
        return project.getOrCreateMetaInfDir();
    }
    
    public File getResourceDirectory() {
        return project.getFile(EarProjectProperties.RESOURCE_DIR);
    }

    public ClassPathProvider getClassPathProvider () {
        return project.getLookup().lookup(ClassPathProvider.class);
    }
    
    public FileObject getArchive () {
        return project.getFileObject (EarProjectProperties.DIST_JAR); //NOI18N
    }
    
    public synchronized J2eeModule getJ2eeModule () {
        if (j2eeApplication == null) {
            j2eeApplication = J2eeModuleFactory.createJ2eeApplication(this);
        }
        return j2eeApplication;
    }
    
    public ModuleChangeReporter getModuleChangeReporter () {
        return this;
    }
    
    @Override
    public String getServerID () {
        return project.getServerID(); //helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public void setServerInstanceID(String severInstanceID) {
        // TODO: implement when needed
    }

    @Override
    public String getServerInstanceID () {
        return project.getServerInstanceID(); //helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    /** Returns the contents of the archive, in copyable form. 
     * Used for incremental deployment. Currently uses its own J2eeModule.RootedEntry interface. 
     * If the J2eeModule instance describes a j2ee application, the result should not 
     * contain module archives.
     */
    public Iterator getArchiveContents () throws IOException {
        FileObject content = getContentDirectory();
        content.refresh();
        return new IT(this, content);
    }

    public FileObject getContentDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return project.getFileObject (EarProjectProperties.BUILD_DIR); //NOI18N
    }
    
    /**
     * Get metadata model of enterprise application.
     */
    public synchronized MetadataModel<ApplicationMetadata> getMetadataModel() {
        if (metadataModel == null) {
            metadataModel = MetadataModelFactory.createMetadataModel(new ApplicationMetadataModelImpl(project));
        }
        return metadataModel;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == ApplicationMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        return null;
    }

    /**
     * Get the metadata model of EAR project. The model is taken from deployment descriptor
     * (<i>application.xml</i>) if it exists or is created on demand.
     * <p>
     * This method should be used whenever model is needed for reading or listening to changes
     * but <b>not for writing</b>.
     * @return metadata model.
     */
    public Application getApplication() {
        try {
            // does application.xml exist?
            if (EarProjectUtil.isDDCompulsory(project)
                    || EarProjectUtil.isDDWritable(project)) {
                // for JAVA EE 5 application.xml doesn't have to exist but can be generated on demand
                //   - so free resources if this is the case
                if (!EarProjectUtil.isDDCompulsory(project)) {
                    synchronized (this) {
                        if (application != null) {
                            application = null;
                        }
                    }
                }
                return getDDFromFile();
            }

            // application.xml doesn't exist
            return setupDDFromVirtual();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        // maybe exception?
        return null;
    }
    
    private Application getDDFromFile() throws IOException {
        FileObject dd = getDeploymentDescriptor();
        if (dd == null) {
            dd = EarProjectGenerator.setupDD(project.getJ2eePlatformVersion(), getMetaInf(), project);
        }
        return DDProvider.getDefault().getDDRoot(dd);
    }
    
    // FIXME remove this after andrei's metadata model will be finished
    private synchronized Application setupDDFromVirtual() throws IOException {
        // application.xml exists?
        if (EarProjectUtil.isDDWritable(project)) {
            return getApplication();
        }
        // model already created
        if (application != null) {
            return application;
        }
        
        // create model
        FileObject template = Repository.getDefault().getDefaultFileSystem().findResource(
                "org-netbeans-modules-j2ee-earproject/ear-5.xml"); // NOI18N
        assert template != null;
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject dd = FileUtil.copyFile(template, root, "application"); // NOI18N

        application = DDProvider.getDefault().getDDRoot(dd);
        application.setDisplayName(ProjectUtils.getInformation(project).getDisplayName());
        for (ClassPathSupport.Item item : EarProjectProperties.getJarContentAdditional(project)) {
            EarProjectProperties.addItemToAppDD(project, application, item);
        }
        
        return application;
    }
    
    public EjbChangeDescriptor getEjbChanges (long timestamp) {
        return this;
    }

    public Object getModuleType () {
        return J2eeModule.EAR;
    }

    public String getModuleVersion () {
        Application app = getApplication();
        return (app == null) ? Application.VERSION_5 /* fallback */ : app.getVersion().toString();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Application.PROPERTY_VERSION)) {
            String oldVersion = (String) evt.getOldValue();
            String newVersion = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(J2eeModule.PROP_MODULE_VERSION, oldVersion, newVersion);
        } else if (EarProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue() == null ? null : d.getServerID((String)evt.getOldValue ());
            String newServerID = evt.getNewValue() == null ? null : d.getServerID((String)evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        } else if (EarProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String)evt.getOldValue();
            String newValue = (String)evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(
                    J2eeModule.PROP_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
        
    public String getUrl () {
        return "";
    }

    public boolean isManifestChanged (long timestamp) {
        return false;
    }

    public void setUrl (String url) {
        throw new UnsupportedOperationException ("Cannot customize URL of web module"); // NOI18N
    }

    public boolean ejbsChanged () {
        return false;
    }

    public String[] getChangedEjbs () {
        return new String[] {};
    }

    public String getJ2eePlatformVersion () {
        return project.getJ2eePlatformVersion(); // helper.getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_PLATFORM);
    }
    
    @Override
    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] roots = new FileObject[groups.length+1];
        roots[0] = getMetaInf();
        for (int i=0; i < groups.length; i++) {
            roots[i+1] = groups[i].getRootFolder();
        }
        
        return roots; 
    }
 
    private static class IT implements Iterator {
        Iterator<FileObject> it;
        FileObject root;
        
        private IT(ProjectEar pe, FileObject f) {
            J2eeModule mods[] = pe.getModules();
            // build filter
            Set<String> filter = new HashSet<String>(mods.length);
            for (int i = 0; i < mods.length; i++) {
                FileObject modArchive = null;
                try {
                    modArchive = mods[i].getArchive();
                } catch (java.io.IOException ioe) {
                    Logger.getLogger(ProjectEar.class.getName()).log(Level.FINER,
                            null,ioe);
                    continue;
                }
                String modName = modArchive.getNameExt();
                long modSize = modArchive.getSize();
                filter.add(modName+"."+modSize);        // NOI18N
            }
            
            ArrayList<FileObject> filteredContent = new ArrayList<FileObject>(5);
            Enumeration ch = f.getChildren(true);
            while (ch.hasMoreElements()) {
                FileObject fo = (FileObject) ch.nextElement();
                String fileName = fo.getNameExt();
                long fileSize = fo.getSize();
                if (filter.contains(fileName+"."+fileSize)) {   // NOI18N
                    continue;
                }
                filteredContent.add(fo);
            }
            this.root = f;
            it = filteredContent.iterator();
        }
        
        public boolean hasNext() {
            return it.hasNext();
        }
        
        public Object next() {
            return new FSRootRE(root, it.next());
        }
        
        public void remove () {
            throw new UnsupportedOperationException ();
        }
        
    }

    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;
        
        FSRootRE (FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }
        
        public FileObject getFileObject () {
            return f;
        }
        
        public String getRelativePath () {
            return FileUtil.getRelativePath (root, f);
        }
    }
    
    private Map<String, J2eeModuleProvider> mods = new HashMap<String, J2eeModuleProvider>();
    
    void setModules(Map<String, J2eeModuleProvider> mods) {
        if (null == mods) {
            throw new IllegalArgumentException("mods"); // NOI18N
        }
        this.mods = mods;
    }
    
    public J2eeModule[] getModules() {
        J2eeModule[] retVal = new J2eeModule[mods.size()];
        int i = 0;
        for (J2eeModuleProvider provider : mods.values()) {
            retVal[i++] = provider.getJ2eeModule();
        }
        return retVal;
    }
    
    public void addModuleProvider(J2eeModuleProvider jmp, String uri) {
        mods.put(uri, jmp);
        J2eeModule jm = jmp.getJ2eeModule();
        fireAddModule(jm);
    }
    
    public void removeModuleProvider(J2eeModuleProvider jmp, String uri) {
        // J2eeModuleProvider tmp = (J2eeModuleProvider) mods.get(uri);
        // if (!tmp.equals(jmp)) {
            // something fishy may be happening here
            // XXX log it
        // }
        J2eeModule jm = jmp.getJ2eeModule();
        fireRemoveModule(jm);
        mods.remove(uri);
    }
    
    private final List<ModuleListener> modListeners = new ArrayList<ModuleListener>();
    
    private void fireAddModule(J2eeModule jm) {
        for (ModuleListener ml : modListeners) {
            try {
                ml.addModule(jm);
            } catch (RuntimeException rex) {
                Logger.getLogger("global").log(Level.INFO, rex.getLocalizedMessage());
            }
        }
    }

    private void fireRemoveModule(J2eeModule jm) {
        for (ModuleListener ml : modListeners) {
            try {
                ml.removeModule(jm);
            } catch (RuntimeException rex) {
                Logger.getLogger("global").log(Level.FINE, rex.getLocalizedMessage());
            }
        }
    }
    
    public void addModuleListener(ModuleListener ml) {
        modListeners.add(ml);
    }
    
    public void removeModuleListener(ModuleListener ml){
        modListeners.remove(ml);
    }
    
    /**
     * Returns the provider for the child module specified by given URI.
     * @param uri the child module URI within the J2EE application.
     * @return J2eeModuleProvider object
     */
    public J2eeModuleProvider getChildModuleProvider(String uri) {
        return mods.get(uri);
    }
    
    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * @return array of J2eeModuleProvider objects.
     */
    public  J2eeModuleProvider[] getChildModuleProviders() {
        return mods.values().toArray(new J2eeModuleProvider[mods.size()]);
    }
   
    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return deployOnSaveSupport;
    }
    
    public File getDeploymentConfigurationFile(String name) {
        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
        if (path.startsWith("META-INF/")) { // NOI18N
            path = path.substring(8); // removing "META-INF/"
        }
        FileObject moduleFolder = getMetaInf();
        File configFolder = FileUtil.toFile(moduleFolder);
        return new File(configFolder, path);
    }
    
    public void addEjbJarModule(EjbJar module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getMetaInf();
        }
        addModule(childFO);
    }
    
    public void addWebModule(WebModule module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getWebInf();
        }
        addModule(childFO);
    }
    
    public void addCarModule(Car module) {
        FileObject childFO = module.getDeploymentDescriptor();
        if (childFO == null) {
            childFO = module.getMetaInf();
        }
        addModule(childFO);
    }

    private void addModule(final FileObject childFO) {
        Project owner = null;
        if (childFO != null) {
            owner = FileOwnerQuery.getOwner(childFO);
        }
        if (owner == null) {
            LOGGER.log(Level.INFO, "Unable to add module to the Enterpise Application. Owner project not found."); // NOI18N
        } else {
            EarProjectProperties.addJ2eeSubprojects(project, new Project [] {owner});
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized (this) {
            if (propertyChangeSupport == null) {
                return;
            }
        }
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    private PropertyChangeSupport getPropertyChangeSupport() {
        Application app = getApplication();
        synchronized (this) {
            if (propertyChangeSupport == null) {
                propertyChangeSupport = new PropertyChangeSupport(this);
                if (app != null) {
                    PropertyChangeListener l = WeakListeners.create(PropertyChangeListener.class, this, app);
                    app.addPropertyChangeListener(l);
                }
            }
            return propertyChangeSupport;
        }
    }

    /**
     * This class is proxying events from child listeners and perform
     * <i>library-inclusion-in-manifest</i> (out of EJB and WEB) logic. Soo ugly but
     * inevitable :(
     */
    private class DeployOnSaveSupportProxy implements ArtifactListener, DeployOnSaveSupport {

        private final List<ArtifactListener> listeners = new ArrayList<ArtifactListener>();

        public DeployOnSaveSupportProxy() {
            super();
        }

        public synchronized void addArtifactListener(ArtifactListener listner) {
            boolean register = listeners.isEmpty();
            if (listner != null) {
                listeners.add(listner);
            }

            if (register) {
                for (J2eeModuleProvider provider : getChildModuleProviders()) {
                    DeployOnSaveSupport support = provider.getDeployOnSaveSupport();
                    if (support != null) {
                        support.addArtifactListener(this);
                    }
                }
            }
        }

        public synchronized void removeArtifactListener(ArtifactListener listener) {
            if (listener != null) {
                listeners.remove(listener);
            }

            if (listeners.isEmpty()) {
                for (J2eeModuleProvider provider : getChildModuleProviders()) {
                    DeployOnSaveSupport support = provider.getDeployOnSaveSupport();
                    if (support != null) {
                        support.removeArtifactListener(this);
                    }
                }
            }
        }

        public void artifactsUpdated(Iterable<Artifact> artifacts) {
            List<Artifact> recomputed = new ArrayList<Artifact>();
            for (Artifact artifact : artifacts) {
                if (artifact.isReferencedLibrary()) {
                    // FIXME manifest ant TLD magic
                    File buildDir = project.getAntProjectHelper().resolveFile(
                            project.evaluator().getProperty(EarProjectProperties.BUILD_DIR));
                    File destFile = new File(buildDir, artifact.getFile().getName());
                    try {
                        FileUtil.createData(destFile);
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "Could not prepare data file", ex);
                        continue;
                    }
                    recomputed.add(artifact.distributionPath(destFile));
                } else {
                    recomputed.add(artifact);
                }
            }
            List<ArtifactListener> toFire = null;
            synchronized (this) {
                toFire = new ArrayList<ArtifactListener>(listeners);
            }
            for (ArtifactListener listener : toFire) {
                listener.artifactsUpdated(recomputed);
            }
        }

    }
}
