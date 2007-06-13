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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class WebJaxWsLookupProvider implements LookupProvider {
    
    private String JAX_WS_XML_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"; //NOI18N
    private String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-web.xsl"; //NOI18N
    private String JAXWS_EXTENSION = "jaxws"; //NOI18N
    
    /** Creates a new instance of JaxWSLookupProvider */
    public WebJaxWsLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        if (prj==null) return null;
        final JaxWsModel jaxWsModel = getJaxWsModel(prj);
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            private FileChangeListener jaxWsListener;
            private FileChangeListener jaxWsCreationListener;
            private JaxWsModel.ServiceListener serviceListener;
            protected void projectOpened() {
                if (jaxWsModel!=null) {
                    serviceListener = new JaxWsModel.ServiceListener() {
                        public void serviceAdded(String name, String implementationClass) {
                            WebServiceNotifier servicesNotifier = prj.getLookup().lookup(WebServiceNotifier.class);
                            if (servicesNotifier!=null) {
                                servicesNotifier.serviceAdded(name, implementationClass);
                            }
                        }

                        public void serviceRemoved(String name) {
                            WebServiceNotifier servicesNotifier = prj.getLookup().lookup(WebServiceNotifier.class);
                            if (servicesNotifier!=null) {
                                servicesNotifier.serviceRemoved(name);
                            }
                        }
                    };
                    jaxWsModel.addServiceListener(serviceListener);
                    AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                    if (ext != null) {
                        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                        int clientsLength = jaxWsModel.getClients().length;
                        int servicesLength = jaxWsModel.getServices().length;
                        int fromWsdlServicesLength=0;
                        for (Service service: jaxWsModel.getServices()) {
                            if (service.getWsdlUrl()!=null) fromWsdlServicesLength++;
                        }
                        Boolean jsr109 = jaxWsModel.getJsr109();
                        boolean isJsr109 = (jsr109==null?true:jsr109.booleanValue());
                        try {
                            
                            if (jaxws_build==null) {
                                // generate nbproject/jaxws-build.xml
                                // add jaxws extension
                                if (servicesLength+clientsLength > 0) {
                                    addJaxWsExtension(prj, JAX_WS_STYLESHEET_RESOURCE, ext, servicesLength, fromWsdlServicesLength, clientsLength, isJsr109);
                                    ProjectManager.getDefault().saveProject(prj);                                    
                                }
                            } else if (servicesLength+clientsLength == 0) {
                                // remove nbproject/jaxws-build.xml
                                // remove the jaxws extension
                                removeJaxWsExtension(jaxws_build, ext);
                                ProjectManager.getDefault().saveProject(prj);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        FileObject jaxws_fo = getJaxWsFileObject(prj);
                        if (jaxws_fo!=null) {                     
                            jaxWsListener = new FileChangeAdapter() {
                                public void fileChanged(FileEvent fe) {
                                    handleJaxsClientBuildScript();
                                }
                            };  
                            jaxws_fo.addFileChangeListener(jaxWsListener);
                        } else {
                            FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                            if (nbprojectDir!=null) {
                                jaxWsCreationListener = new FileChangeAdapter() {
                                    public void fileDataCreated(FileEvent fe) {
                                        if ("jax-ws.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                                            FileObject jaxws_fo = getJaxWsFileObject(prj);
                                            if (jaxws_fo!=null) {
                                                jaxWsListener = new FileChangeAdapter() {
                                                    public void fileChanged(FileEvent fe) {
                                                        handleJaxsClientBuildScript();
                                                    }
                                                };  
                                                jaxws_fo.addFileChangeListener(jaxWsListener);                                           
                                            }
                                        }
                                    }
                                };
                                nbprojectDir.addFileChangeListener(jaxWsCreationListener);
                            }
                        }
                    }
                }
            }
            protected void projectClosed() {
                FileObject nbprojectDir = prj.getProjectDirectory().getFileObject("nbproject"); //NOI18N
                if (nbprojectDir!=null) {
                    nbprojectDir.removeFileChangeListener(jaxWsCreationListener);
                    FileObject jaxws_fo = getJaxWsFileObject(prj);
                    if (jaxws_fo!=null)
                        jaxws_fo.removeFileChangeListener(jaxWsListener);
                }
                if (jaxWsModel!=null) jaxWsModel.removeServiceListener(serviceListener);
                
            }
            
            private void handleJaxsClientBuildScript() {
                AntBuildExtender ext = prj.getLookup().lookup(AntBuildExtender.class);
                if (ext != null) {
                    FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
                    int clientsLength = jaxWsModel.getClients().length;
                    int servicesLength = jaxWsModel.getServices().length;
                    int fromWsdlServicesLength=0;
                    for (Service service: jaxWsModel.getServices()) {
                        if (service.getWsdlUrl()!=null) fromWsdlServicesLength++;
                    }
                    Boolean jsr109 = jaxWsModel.getJsr109();
                    boolean isJsr109 = (jsr109==null?true:jsr109.booleanValue());
                    try {
                        if (clientsLength+servicesLength == 0) {
                            // remove nbproject/jaxws-build.xml
                            // remove the jaxws extension
                            removeJaxWsExtension(jaxws_build, ext);
                            ProjectManager.getDefault().saveProject(prj);
                        } else {
                            // re-generate nbproject/jaxws-build.xml
                            // add jaxws extension, if needed
                            boolean needToSave = changeJaxWsExtension(prj, JAX_WS_STYLESHEET_RESOURCE, ext, servicesLength, fromWsdlServicesLength, clientsLength, isJsr109);
                            if (needToSave) ProjectManager.getDefault().saveProject(prj);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        return Lookups.fixed(new Object[] {
            openhook,
            jaxWsModel 
        });
    }
    
    private JaxWsModel getJaxWsModel(Project prj) {
        try {
            FileObject fo = getJaxWsFileObject(prj);
            if (fo==null)
                return JaxWsModelProvider.getDefault().getJaxWsModel(
                        WSUtils.class.getResourceAsStream(JAX_WS_XML_RESOURCE));
            else {
                JaxWsModel jaxWsModel = JaxWsModelProvider.getDefault().getJaxWsModel(fo);
                return jaxWsModel;
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    private FileObject getJaxWsFileObject(Project prj) {
        FileObject jaxWsFo = findJaxWsFileObject(prj);
        return jaxWsFo;
    }
    
    public FileObject findJaxWsFileObject(Project prj) {
        return prj.getProjectDirectory().getFileObject(TransformerUtils.JAX_WS_XML_PATH);
    }
    
    private void addJaxWsExtension(
                        final Project prj, 
                        final String styleSheetResource,
                        AntBuildExtender ext,
                        int servicesLength,
                        int fromWsdlServicesLength,
                        int clientsLength,
                        boolean isJsr109) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    TransformerUtils.transformClients(prj.getProjectDirectory(), styleSheetResource, true);
                    return Boolean.TRUE;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build!=null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension==null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            //adding dependencies
            if (clientsLength>0) {
                extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
                extension.addDependency("-do-ws-compile", "wsimport-client-compile"); //NOI18N
                extension.addDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
            }
            if (fromWsdlServicesLength>0) {
                extension.addDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
                extension.addDependency("-do-compile", "wsimport-service-compile"); //NOI18N
                extension.addDependency("-do-compile-single", "wsimport-service-compile"); //NOI18N                
            }
            if (!isJsr109 && servicesLength > fromWsdlServicesLength) {
                extension.addDependency("-post-compile", "wsgen-service-compile"); //NOI18N
            }
        }
    }
    private boolean changeJaxWsExtension(
                        final Project prj, 
                        final String styleSheetResource,
                        AntBuildExtender ext,
                        int servicesLength,
                        int fromWsdlServicesLength,
                        int clientsLength,
                        boolean isJsr109) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    TransformerUtils.transformClients(prj.getProjectDirectory(), styleSheetResource, true);
                    return Boolean.TRUE;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
        FileObject jaxws_build = prj.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build!=null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        boolean extensionCreated = false;
        if (extension==null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            extensionCreated = true;
        }
        
        // adding/removing dependencies
        boolean needToSave = false;
        if (clientsLength > 0) {
            extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
            extension.addDependency("-do-ws-compile", "wsimport-client-compile"); //NOI18N
            extension.addDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
            needToSave = true;
        } else if (!extensionCreated && clientsLength == 0) {
            extension.removeDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
            extension.removeDependency("-do-ws-compile", "wsimport-client-compile"); //NOI18N
            extension.removeDependency("-do-compile-single", "wsimport-client-compile"); //NOI18N
            needToSave = true;
        }
        if (fromWsdlServicesLength > 0) {
            extension.addDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
            extension.addDependency("-do-compile", "wsimport-service-compile"); //NOI18N
            extension.addDependency("-do-compile-single", "wsimport-service-compile"); //NOI18N  
            needToSave = true;
        } else if (!extensionCreated && fromWsdlServicesLength == 0) {
            extension.removeDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
            extension.removeDependency("-do-compile", "wsimport-service-compile"); //NOI18N
            extension.removeDependency("-do-compile-single", "wsimport-service-compile"); //NOI18N
            needToSave = true;
        }
        if (!isJsr109 && servicesLength > fromWsdlServicesLength) {
            extension.addDependency("-post-compile", "wsgen-service-compile"); //NOI18N
            needToSave = true;
        } else {
            extension.removeDependency("-post-compile", "wsgen-service-compile"); //NOI18N
            needToSave = true;
        }
        return needToSave;
    }
    
    private void removeJaxWsExtension(
                        FileObject jaxws_build, 
                        AntBuildExtender ext) throws IOException {
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension!=null) {
            ext.removeExtension(JAXWS_EXTENSION);
        }
        if (jaxws_build!=null) {
            FileLock fileLock = jaxws_build.lock();
            if (fileLock!=null) {
                try {
                    jaxws_build.delete(fileLock);
                } finally {
                    fileLock.releaseLock();
                }
            }
        }
    }
    
}
