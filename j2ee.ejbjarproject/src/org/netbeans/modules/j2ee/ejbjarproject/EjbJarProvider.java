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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
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
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;


/** A ejb module implementation on top of project.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarProvider extends J2eeModuleProvider implements EjbJarImplementation, J2eeModule, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener {
    
    public static final String FILE_DD        = "ejb-jar.xml";//NOI18N
    
    private EjbJarProject project;
    private AntProjectHelper helper;
    private Set versionListeners = null;
    
    private long notificationTimeout = 0; // used to suppress repeating the same messages
    
    EjbJarProvider(EjbJarProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        project.evaluator().addPropertyChangeListener (this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo == null) {
            return null;
        }
        return metaInfFo.getFileObject(FILE_DD);
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
            String relativePath = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.META_INF);
            String path = (relativePath != null ? helper.resolvePath(relativePath) : ""); // NOI18N
            showErrorMessage(NbBundle.getMessage(EjbJarProject.class,"MSG_MetaInfCorrupted", project.getName(), path));
            return null;
        }
        return metaInf;
    }

    public File getEnterpriseResourceDirectory() {
        return getFile(EjbJarProjectProperties.RESOURCE_DIR);
    }

    public FileObject findDeploymentConfigurationFile(String name) {
        FileObject metaInf = getMetaInf();
        if (metaInf == null) {
            return null;
        }
        return metaInf.getFileObject(name);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        FileObject moduleFolder = getMetaInf();
        if (moduleFolder == null) {
            return null;
        }
        File configFolder = FileUtil.toFile(moduleFolder);
        return new File(configFolder, name);
    }
    
    public ClassPathProvider getClassPathProvider() {
        return (ClassPathProvider) project.getLookup().lookup(ClassPathProvider.class);
    }
    
    public FileObject getArchive() {
        return getFileObject(EjbJarProjectProperties.DIST_JAR); //NOI18N
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
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule() {
        return this;
    }
    
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }
    
    public boolean useDefaultServer() {
        return true;
    }
    
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public void setServerInstanceID(String severInstanceID) {
        EjbJarProjectProperties.setServerInstance(project, helper, severInstanceID);
    }
    
    public Iterator getArchiveContents() throws java.io.IOException {
        return new IT(getContentDirectory());
    }
    
    public FileObject getContentDirectory() {
        return getFileObject(EjbJarProjectProperties.BUILD_CLASSES_DIR); //NOI18N
    }
    
    public FileObject getBuildDirectory() {
        return getFileObject(EjbJarProjectProperties.BUILD_DIR); //NOI18N
    }
    
    public File getContentDirectoryAsFile() {
        return getFile(EjbJarProjectProperties.BUILD_CLASSES_DIR); //NOI18N
    }
    
    public org.netbeans.modules.schema2beans.BaseBean getDeploymentDescriptor(String location) {
        if (J2eeModule.EJBJAR_XML.equals(location)){
            EjbJar webApp = getEjbJar();
            if (webApp != null) {
                //PENDING find a better way to get the BB from WApp and remove the HACK from DDProvider!!
                return DDProvider.getDefault().getBaseBean(webApp);
            }
        }
        else if(J2eeModule.EJBSERVICES_XML.equals(location)){
            Webservices webServices = getWebservices();
            if(webServices != null){
                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getBaseBean(webServices);
            }
        }
        return null;
    }
    
    private EjbJar getEjbJar() {
        try {
            FileObject ejbJarXml = getDeploymentDescriptor();
            if (ejbJarXml == null) {
                return null;
            }
            return DDProvider.getDefault().getDDRoot(ejbJarXml);
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
        }
        return null;
    }
    
    private Webservices getWebservices() {
        FileObject wsdd = getDD();
        if(wsdd != null) {
            try {
                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault()
                .getDDRoot(getDD());
            } catch (java.io.IOException e) {
                org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
            }
        }
        
        return null;
    }
    
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
        EjbJar ejbJar = getEjbJar();
        return ejbJar.getVersion().toString();
    }
    
    private Set versionListeners() {
        if (versionListeners == null) {
            versionListeners = new HashSet();
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = getEjbJar();
            if (ejbJar != null) {
                PropertyChangeListener l = (PropertyChangeListener) org.openide.util.WeakListeners.create(PropertyChangeListener.class, this, ejbJar);
                ejbJar.addPropertyChangeListener(l);
            }
        }
        return versionListeners;
    }
    
    public void addVersionListener(J2eeModule.VersionListener vl) {
        versionListeners().add(vl);
    }
    
    public void removeVersionListener(J2eeModule.VersionListener vl) {
        if (versionListeners != null)
            versionListeners.remove(vl);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.PROPERTY_VERSION)) {
            for (Iterator i=versionListeners.iterator(); i.hasNext();) {
                J2eeModule.VersionListener vl = (J2eeModule.VersionListener) i.next();
                String oldVersion = (String) evt.getOldValue();
                String newVersion = (String) evt.getNewValue();
                vl.versionChanged(oldVersion, newVersion);
            }
        } else if (evt.getPropertyName ().equals (EjbJarProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault ();
            String oldServerID = evt.getOldValue () == null ? null : d.getServerID ((String) evt.getOldValue ());
            String newServerID = evt.getNewValue () == null ? null : d.getServerID ((String) evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        }  else if (EjbJarProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String)evt.getOldValue();
            String newValue = (String)evt.getNewValue();
            firePropertyChange(
                    PROP_ENTERPRISE_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
    
    public String getUrl() {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(EjbJarProjectProperties.JAR_NAME);
        return name == null ? "" : ("/"+name); //NOI18N
    }
    
    public boolean isManifestChanged(long timestamp) {
        return false;
    }
    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Cannot customize URL of web module");
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

    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List roots = new LinkedList();
        FileObject metaInf = getMetaInf();
        if (metaInf != null)
            roots.add(metaInf);
        
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
                if (projects[i].equals(project)) 
                    return true;
            }
            return false;
        } else {
            // be conservative -- don't know anything about the project
            // so consider it open
            return true;
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
