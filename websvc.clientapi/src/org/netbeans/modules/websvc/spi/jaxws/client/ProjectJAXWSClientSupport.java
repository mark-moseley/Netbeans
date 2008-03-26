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

package org.netbeans.modules.websvc.spi.jaxws.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkuchtiak
 */
public abstract class ProjectJAXWSClientSupport implements JAXWSClientSupportImpl {
    Project project;
    private FileObject clientArtifactsFolder;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public ProjectJAXWSClientSupport(Project project) {
        this.project=project;
    }
    
    public void removeServiceClient(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null && jaxWsModel.removeClient(serviceName)) {
            writeJaxWsModel(jaxWsModel);
        }
    }
    
    public String getWsdlUrl(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client client = jaxWsModel.findClientByName(serviceName);
            if (client!=null) return client.getWsdlUrl();
        }
        return null;
    }
    
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        final JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        String finalClientName=clientName;
        boolean clientAdded=false;
        if (jaxWsModel!=null) {
            
            // HACK to enable filesystems to fire events when new folder will be created
            // need to ask for children
            FileObject projectDir = project.getProjectDirectory();
            clientArtifactsFolder = projectDir.getFileObject("build/generated/wsimport/client"); //NOI18N
            if (clientArtifactsFolder!=null) {
                clientArtifactsFolder.getChildren(true);
            } else {
                try {
                    FileUtil.createFolder(projectDir, "build/generated/wsimport/client");
                } catch (IOException ex) {}
            }
            
            if(!isJsr109){
                try{
                    addJaxWs20Library();
                } catch(Exception e){  //TODO handle this
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            Client client=null;
            finalClientName = findProperClientName(clientName, jaxWsModel);
                      
            FileObject localWsdl=null;
            try {
                localWsdl = WSUtils.retrieveResource(
                        getLocalWsdlFolderForClient(finalClientName,true),
                        new URI(wsdlUrl));
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            if (localWsdl!=null) {
                Boolean value = jaxWsModel.getJsr109();
                if((value == null || Boolean.TRUE.equals(value)) && !isJsr109){
                    jaxWsModel.setJsr109(Boolean.FALSE);
                } else if (Boolean.FALSE.equals(value) && isJsr109) {
                    jaxWsModel.setJsr109(Boolean.TRUE);
                }             
                try {
                    client = jaxWsModel.addClient(finalClientName, wsdlUrl, packageName);
                } catch (ClientAlreadyExistsExeption ex) {
                    //this shouldn't happen
                }
                if (packageName == null) {
                    // compute package name from namespace
                    client.setPackageName(
                            WSUtils.getPackageNameForWsdl(FileUtil.toFile(localWsdl)));
                    System.out.println("packageName = "+packageName);
                }
                FileObject xmlResorcesFo = getLocalWsdlFolderForClient(finalClientName,false);
                String localWsdlUrl = FileUtil.getRelativePath(xmlResorcesFo, localWsdl);
                client.setLocalWsdlFile(localWsdlUrl);
                FileObject catalog = getCatalogFileObject();
                if (catalog!=null) client.setCatalogFile(CATALOG_FILE);
                writeJaxWsModel(jaxWsModel);
                clientAdded=true;
                // generate wsdl model immediately
                final String clientName2 = finalClientName;
                try {
                    final WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(localWsdl.getURL());
                    if (modeler!=null) {
                        modeler.setPackageName(packageName);
                        modeler.setCatalog(catalog.getURL());
                        modeler.generateWsdlModel(new WsdlModelListener() {
                            public void modelCreated(WsdlModel model) {
                                if (model==null) {
                                    RequestProcessor.getDefault().post(new Runnable() {
                                       public void run() {
                                           DialogDisplayer.getDefault().notify(new WsImportFailedMessage(modeler.getCreationException()));
                                       }
                                    });
                                    
                                } else {
                                    Client client = jaxWsModel.findClientByName(clientName2);
                                    String packName = client.getPackageName();                               
                                    // this shuldn't normally happen
                                    // this applies only for case when package name cannot be resolved for namespace
                                    if(packName == null) {
                                        if (model.getServices().size() > 0) {
                                            WsdlService service = model.getServices().get(0);
                                            String javaName = service.getJavaName();
                                            int index = javaName.lastIndexOf(".");
                                            if (index != -1){
                                                packName = javaName.substring(0,index );
                                            } else {
                                                packName = javaName;
                                            }                                 
                                            client.setPackageName(packName);
                                            writeJaxWsModel(jaxWsModel);
                                        }
                                    }
                                    
                                    runWsimport(clientName2);
                                }
                            }
                        });
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
            }
            return finalClientName;
        }
        return null;
    }
    
    private void runWsimport(String finalClientName){
        final FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        final String finalName = finalClientName;

        if (SwingUtilities.isEventDispatchThread()) {
            openOutputWindow();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    openOutputWindow();
                }
            });            
        }
                
        try {
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-client-"+finalName,"wsimport-client-compile" },null); //NOI18N
                    return Boolean.TRUE;
                }
            }).booleanValue();
        } catch (MutexException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void openOutputWindow() {
        TopComponent outputTc = WindowManager.getDefault().findTopComponent("output"); //NOI18N
        if (outputTc != null) {
            outputTc.open();
        }       
    }
    
    private String findProperClientName(String name, JaxWsModel jaxWsModel) {
        String firstName=name.length()==0?NbBundle.getMessage(ProjectJAXWSClientSupport.class,"LBL_defaultClientName"):name;
        if (jaxWsModel.findClientByName(firstName)==null) return firstName;
        for (int i = 1;; i++) {
            String finalName = firstName + "_" + i; // NOI18N
            if (jaxWsModel.findClientByName(finalName)==null)
                return finalName;
        }
    }
    
    private void writeJaxWsModel(JaxWsModel jaxWsModel) {
        try {
            jaxWsModel.write();
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "failed to save jax-ws.xml", ex); //NOI18N
        }
    }
    
    public List getServiceClients() {
        List<Client> jaxWsClients = new ArrayList<Client>();
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client[] clients = jaxWsModel.getClients();
            for (int i=0;i<clients.length;i++) jaxWsClients.add(clients[i]);
        }
        return jaxWsClients;
    }
    
    /**
     *  return root folder for wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, true);
    }
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, false);
    }
    
    private FileObject getArtifactsFolder(String clientName, boolean createFolder, boolean forWsdl) {
        String folderName = forWsdl?"wsdl":"bindings"; //NOI18N
        FileObject root = getXmlArtifactsRoot();
        assert root!=null;
        FileObject wsdlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER+"/"+CLIENTS_LOCAL_FOLDER+"/"+clientName+"/"+folderName); //NOI18N
        if (wsdlLocalFolder==null && createFolder) {
            try {
                FileObject xmlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER);
                if (xmlLocalFolder==null) xmlLocalFolder = root.createFolder(XML_RESOURCES_FOLDER);
                FileObject servicesLocalFolder = xmlLocalFolder.getFileObject(CLIENTS_LOCAL_FOLDER);
                if (servicesLocalFolder==null) servicesLocalFolder = xmlLocalFolder.createFolder(CLIENTS_LOCAL_FOLDER);
                FileObject serviceLocalFolder = servicesLocalFolder.getFileObject(clientName);
                if (serviceLocalFolder==null) serviceLocalFolder = servicesLocalFolder.createFolder(clientName);
                wsdlLocalFolder=serviceLocalFolder.getFileObject(folderName);
                if (wsdlLocalFolder==null) wsdlLocalFolder = serviceLocalFolder.createFolder(folderName);
            } catch (IOException ex) {
                return null;
            }
        }
        return wsdlLocalFolder;
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getProjectDirectory();
    }
    
    private FileObject getCatalogFileObject() {
        return project.getProjectDirectory().getFileObject(CATALOG_FILE);
    }
    
    public URL getCatalog() {
        try {
            FileObject catalog = getCatalogFileObject();
            return catalog==null?null:catalog.getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
        
    }
    
    protected abstract void addJaxWs20Library() throws Exception;
    
    public abstract FileObject getWsdlFolder(boolean create) throws IOException;
    
    public String getServiceRefName(Node clientNode) {
        WsdlService service = clientNode.getLookup().lookup(WsdlService.class);
        String serviceName = service.getName();
        return "service/" + serviceName;
    }
    
    private class WsImportFailedMessage extends NotifyDescriptor.Message {
        public WsImportFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(ProjectJAXWSClientSupport.class,"TXT_CannotGenerateClient",ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
        
    }
    
    /** folder where xml client artifacts should be saved, e.g. WEB-INF/wsdl/client/SampleClient
     */
    protected FileObject getWsdlFolderForClient(String name) throws IOException {
        FileObject globalWsdlFolder = getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject("client/"+name); //NOI18N
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        FileObject clientWsdlFolder = globalWsdlFolder.getFileObject("client"); //NOI18N
        if (clientWsdlFolder==null) clientWsdlFolder = globalWsdlFolder.createFolder("client"); //NOI18N
        return clientWsdlFolder.createFolder(name);
    }
    
}
