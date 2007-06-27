/*
 * and Distribution License (the License). You may not use this file except in
 * The contents of this file are subject to the terms of the Common Development
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;


/** A ejb module implementation on top of project.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarProvider extends J2eeModuleProvider
        implements EjbJarImplementation, J2eeModuleImplementation, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener {
    
    public static final String FILE_DD = "ejb-jar.xml";//NOI18N
    
    private final EjbJarProject project;
    private final AntProjectHelper helper;
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    private PropertyChangeSupport propertyChangeSupport;
    private J2eeModule j2eeModule;
    private ClassPathProviderImpl cpProvider;
    
    private long notificationTimeout = 0; // used to suppress repeating the same messages
    
    EjbJarProvider(EjbJarProject project, AntProjectHelper helper, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.helper = helper;
        this.cpProvider = cpProvider;
        project.evaluator().addPropertyChangeListener (this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject ddFO = null;
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo != null) {
            ddFO = metaInfFo.getFileObject(FILE_DD);
        }
        if (ddFO == null && !EjbJarProjectProperties.JAVA_EE_5.equals(getJ2eePlatformVersion())) {
            // ...generate the DD from template...
        }
        return ddFO;
    }
    
    /** @deprecated use getJavaSources */
    public ClassPath getClassPath() {
        ClassPathProvider cpp = (ClassPathProvider) project.getLookup().lookup(ClassPathProvider.class);
        if (cpp != null) {
            return cpp.findClassPath(getFileObject(EjbJarProjectProperties.SRC_DIR), ClassPath.SOURCE);
        }
        return null;
    }

    public FileObject[] getJavaSources() {
        return project.getSourceRoots().getRoots();
    }
    
    public FileObject getMetaInf() {
        FileObject metaInf = getFileObject(EjbJarProjectProperties.META_INF);
        if (metaInf == null) {
            String version = project.getAPIEjbJar().getJ2eePlatformVersion();
            if (needConfigurationFolder(version)) {
                String relativePath = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.META_INF);
                String path = (relativePath != null ? helper.resolvePath(relativePath) : "");
                showErrorMessage(NbBundle.getMessage(EjbJarProvider.class,"MSG_MetaInfCorrupted", project.getName(), path));
            }
        }
        return metaInf;
    }
    
    /** Package-private for unit test only. */
    static boolean needConfigurationFolder(final String version) {
        return EjbJarProjectProperties.J2EE_1_3.equals(version) ||
                EjbJarProjectProperties.J2EE_1_4.equals(version);
    }
    
    public File getMetaInfAsFile() {
        return getFile(EjbJarProjectProperties.META_INF);
    }

    public File getResourceDirectory() {
        return getFile(EjbJarProjectProperties.RESOURCE_DIR);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        return new File(getMetaInfAsFile(), name);
    }
    
    public ClassPathProvider getClassPathProvider() {
        return (ClassPathProvider) project.getLookup().lookup(ClassPathProvider.class);
    }
    
    public FileObject getArchive() {
        return getFileObject(EjbJarProjectProperties.DIST_JAR);
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        
        return null;
    }
    
    private File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        }
        return null;
    }
    
    public synchronized J2eeModule getJ2eeModule () {
        if (j2eeModule == null) {
            j2eeModule = J2eeModuleFactory.createJ2eeModule(this);
        }
        return j2eeModule;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }
    
    public boolean useDefaultServer() {
        return false;
    }
    
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public void setServerInstanceID(String serverInstanceID) {
        assert serverInstanceID != null : "passed serverInstanceID cannot be null";
        EjbJarProjectProperties.setServerInstance(project, helper, serverInstanceID);
    }
    
    public Iterator getArchiveContents() throws java.io.IOException {
        return new IT(getContentDirectory());
    }
    
    public FileObject getContentDirectory() {
        return getFileObject(EjbJarProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public FileObject getBuildDirectory() {
        return getFileObject(EjbJarProjectProperties.BUILD_DIR);
    }
    
    public File getContentDirectoryAsFile() {
        return getFile(EjbJarProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
    }
    
// TODO MetadataModel: rewrite when MetadataModel<WebservicesMode> is ready
//    
//    private Webservices getWebservices() {
//        if (Util.isJavaEE5orHigher(project)) {
//            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
//            try {
//                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getMergedDDRoot(wss);
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        } else {
//            FileObject wsdd = getDD();
//            if(wsdd != null) {
//                try {
//                    return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getDDRoot(wsdd);
//                } catch (java.io.IOException e) {
//                    org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
//                }
//            }
//        }
//        return null;
//    }
    
    public FileObject getDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(WebServicesConstants.WEBSERVICES_DD, "xml"); // NOI18N
    }
    
    public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long timestamp) {
        return this;
    }
    
    public Object getModuleType() {
        return J2eeModule.EJB;
    }
    
    public String getModuleVersion() {
        // we don't want to use MetadataModel here as it can block
        String version = null;
        try {
            FileObject ddFO = getDeploymentDescriptor();
            if (ddFO != null) {
                org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFO);
                version = ejbJar.getVersion().toString();
            }
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.WARNING, null, e); // NOI18N
        }
        if (version == null) {
            // XXX should return a version based on the Java EE version
            version = EjbJar.VERSION_3_0;
        }
        return version;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.PROPERTY_VERSION)) {
            String oldVersion = (String) evt.getOldValue();
            String newVersion = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(J2eeModule.PROP_MODULE_VERSION, oldVersion, newVersion);
        } else if (evt.getPropertyName ().equals (EjbJarProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue () == null ? null : d.getServerID ((String) evt.getOldValue ());
            String newServerID = evt.getNewValue () == null ? null : d.getServerID ((String) evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        }  else if (EjbJarProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String) evt.getOldValue();
            String newValue = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(
                    J2eeModule.PROP_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
    
    public String getUrl() {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(EjbJarProjectProperties.JAR_NAME);
        return name == null ? "" : ('/' + name);
    }
    
    public boolean isManifestChanged(long timestamp) {
        return false;
    }
    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Cannot customize URL of EJB module"); // NOI18N
    }
    
    public boolean ejbsChanged() {
        return false;
    }
    
    public String[] getChangedEjbs() {
        return new String[] {};
    }
    
    public String getJ2eePlatformVersion() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_PLATFORM);
    }

    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }
    
    public synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            FileObject ddFO = getDD();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webservicesMetadataModel = WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return webservicesMetadataModel;
    }

    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List roots = new LinkedList();
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            roots.add(metaInf);
        }
        
        for (int i = 0; i < groups.length; i++) {
            roots.add(groups[i].getRootFolder());
        }
        FileObject[] rootArray = new FileObject[roots.size()];
        return (FileObject[])roots.toArray(rootArray);        
    }
    
    private void showErrorMessage(final String message) {
        // only display the messages if the project is open
        if(new Date().getTime() > notificationTimeout && isProjectOpen()) {
            // DialogDisplayer waits for the AWT thread, blocking the calling
            // thread -- deadlock-prone, see issue #64888. therefore invoking
            // only in the AWT thread
            Runnable r = new Runnable() {
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            };
            r.run();
            
            // set timeout to suppress the same messages during next 20 seconds (feel free to adjust the timeout
            // using more suitable value)
            notificationTimeout = new Date().getTime() + 20000;
        }
    }
    
    private boolean isProjectOpen() {
        // OpenProjects.getDefault() is null when this method is called upon
        // IDE startup from the project's impl of ProjectOpenHook
        if (OpenProjects.getDefault() != null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].equals(project)) {
                    return true;
                }
            }
            return false;
        } else {
            // be conservative -- don't know anything about the project
            // so consider it open
            return true;
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
        synchronized (this) {
            if (propertyChangeSupport == null) {
                propertyChangeSupport = new PropertyChangeSupport(this);
                // XXX need to listen on the module version
                // try {
                //     project.getAPIEjbJar().getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                //         public Void run(EjbJarMetadata metadata) throws MetadataModelException, IOException {
                //             EjbJar ejbJar = metadata.getRoot();
                //             PropertyChangeListener l = (PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, EjbJarProvider.this, ejbJar);
                //             ejbJar.addPropertyChangeListener(l);
                //             return null;
                //         }
                //     });
                // } catch (MetadataModelException e) {
                //     // TODO MetadataModel: handle the exception
                // } catch (IOException e) {
                //     // TODO MetadataModel: handle the exception
                // }
            }
            return propertyChangeSupport;
        }
    }

    private static class IT implements Iterator {
        java.util.Enumeration ch;
        FileObject root;
        
        private IT(FileObject f) {
            this.ch = f.getChildren(true);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ch.hasMoreElements();
        }
        
        public Object next() {
            FileObject f = (FileObject) ch.nextElement();
            return new FSRootRE(root, f);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;
        
        FSRootRE(FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }
        
        public FileObject getFileObject() {
            return f;
        }
        
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }

}
