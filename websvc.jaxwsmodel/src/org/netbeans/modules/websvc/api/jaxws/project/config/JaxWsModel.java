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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
//retouche:
//import org.netbeans.modules.j2ee.metadata.JAXWSMetadataProvider;
//import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.websvc.jaxwsmodel.project.metadata.JAXWSNNMergerProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;

/** Provides information about web services and clients in a project
 * Provides information used for build-impl generation
 * Working over nbproject/jax-ws.xml file
 */
public final class JaxWsModel {
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws;
    private FileObject fo;
    private boolean nninitialized = false;
    private Object initLock = new Object();
    private List<ServiceListener> serviceListeners;
    
    JaxWsModel(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws) {
        this(jaxws,null);
    }
    
    JaxWsModel(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws, FileObject fo) {
        this.jaxws=jaxws;
        this.fo=fo;
        serviceListeners = new ArrayList<ServiceListener>();
    }
    
    public Service[] getServices() {
        //init annotations provider
        initNNProvider();
        
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return new Service[]{};
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return new Service[]{};
            Service[] newServices = new Service[org.length];
            for (int i=0;i<org.length;i++) newServices[i] = new Service(org[i]);
            return newServices;
        }
    }
    
    public void setJsr109(Boolean jsr109){
        jaxws.setJsr109(jsr109);
    }
    
    public Boolean getJsr109(){
        return jaxws.getJsr109();
    }
    
    private void initNNProvider() {
        synchronized (initLock) {
            if(!nninitialized) {
                if(fo != null) {
                    //retouche:
//                    try {
//                        Project project = FileOwnerQuery.getOwner(fo);
//                        MetadataUnit mu = findJAXWSMetadata(project);
//                        if(mu != null) {
//                            //init the NN listener if there is JAXWSMetadataProvider instance in the project lookup
//                            JAXWSNNMergerProvider.getDefault().initNNMerger(this, mu);
//                        }
                        //set nninitialized to true even there is no JAXWSMetadataProvider in the project lookup
                        //so next time initNNProvider is called it will not try to find it - which cannot happen
                        nninitialized = true;
                        //retouche:
//                    }catch(IOException e) {
//                        ErrorManager.getDefault().notify(e);
//                    }
                }
            }
        }
    }
    
    public Service findServiceByName(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = findService(name);
        return (service==null?null:new Service(service));
    }
    
    public Service findServiceByImplementationClass(String wsClassName) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = _findServiceByClass(wsClassName);
        return (service==null?null:new Service(service));
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service findService(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (name.equals(org[i].getName())) return org[i];
            }
            return null;
        }
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service _findServiceByClass(String wsClassName) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (wsClassName.equals(org[i].getImplementationClass())) return org[i];
            }
            return null;
        }
    }
    
    public boolean removeService(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = findService(name);
        if (name==null) return false;
        else {
            jaxws.getServices().removeService(service);
            fireServiceRemoved(name);
            return true;
        }
    }
    
    public boolean removeServiceByClassName(String webserviceClassName) {
        if (webserviceClassName != null) {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = _findServiceByClass(webserviceClassName);
            if (service != null) {
                fireServiceRemoved(service.getName());
                jaxws.getServices().removeService(service);            
                return true;
            }
        }
        return false;
    }
    
    public Service addService(String name, String implementationClass)
    throws ServiceAlreadyExistsExeption {
        
        if (findService(name)!=null) throw new ServiceAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = jaxws.getServices().newService();
        service.setName(name);
        service.setImplementationClass(implementationClass);
        jaxws.getServices().addService(service);
        fireServiceAdded(name, implementationClass);
        return new Service(service);
    }
    
    public Service addService(String name, String implementationClass, String wsdlUrl, String serviceName, String portName, String packageName)
    throws ServiceAlreadyExistsExeption {
        
        if (findService(name)!=null) throw new ServiceAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = jaxws.getServices().newService();
        service.setName(name);
        service.setImplementationClass(implementationClass);
        service.setWsdlUrl(wsdlUrl);
        service.setServiceName(serviceName);
        service.setPortName(portName);
        service.setPackageName(packageName);
        jaxws.getServices().addService(service);
        return new Service(service);
    }
    
    public Client[] getClients() {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return new Client[]{};
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return new Client[]{};
            Client[] newClients = new Client[org.length];
            for (int i=0;i<org.length;i++) newClients[i] = new Client(org[i]);
            return newClients;
        }
    }
    
    public Client findClientByName(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = findClient(name);
        return (client==null?null:new Client(client));
    }
    
    public Client findClientByWsdlUrl(String wsdlUrl) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (wsdlUrl.equals(org[i].getWsdlUrl())) return new Client(org[i]);
            }
            return null;
        }
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client findClient(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (name.equals(org[i].getName())) return org[i];
            }
            return null;
        }
    }
    
    public boolean removeClient(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = findClient(name);
        if (client==null) return false;
        else {
            jaxws.getClients().removeClient(client);
            return true;
        }
    }
    
    public Client addClient(String name, String wsdlUrl, String packageName)
    throws ClientAlreadyExistsExeption {
        
        if (findClient(name)!=null) throw new ClientAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = jaxws.getClients().newClient();
        client.setName(name);
        client.setWsdlUrl(wsdlUrl);
        if (packageName!=null) {
            client.setPackageName(packageName);
            client.setPackageNameForceReplace("true");
        }
        jaxws.getClients().addClient(client);
        return new Client(client);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        jaxws.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        jaxws.removePropertyChangeListener(l);
    }
    
    public void merge(JaxWsModel newJaxWs) {
        if (newJaxWs.jaxws!=null)
            jaxws.merge(newJaxWs.jaxws,BaseBean.MERGE_UPDATE);
    }
    
    public void write(OutputStream os) throws IOException {
        jaxws.write(os);
    }
    
    public FileObject getJaxWsFile() {
        return fo;
    }
    
    public void setJaxWsFile(FileObject fo) {
        this.fo=fo;
    }
    
    public void write() throws IOException {
        if (fo!=null) {
            fo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() {
                FileLock lock=null;
                try {
                    lock = fo.lock();
                    OutputStream os = fo.getOutputStream(lock);
                    write(os);
                    os.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    if (lock!=null) lock.releaseLock();
                }
            }
        });
        } else throw new IOException("No FileObject for writing specified"); //NOI18N
    }
    
    //retouche:
//    private static MetadataUnit findJAXWSMetadata(Project project) {
//        JAXWSMetadataProvider metadataProvider = (JAXWSMetadataProvider)project.getLookup().lookup(JAXWSMetadataProvider.class);
//        if(metadataProvider != null) {
//            return metadataProvider.getJAXWSMetadata();
//        } else {
//            return null;
//        }
//    }
    
    public synchronized void addServiceListener(ServiceListener listener) {
        if (listener!=null)
            serviceListeners.add(listener);
    }
    
    public synchronized void removeServiceListener(ServiceListener listener) {
        serviceListeners.remove(listener);
    }
    
    void fireServiceAdded(String name, String implementationClass) {
        Iterator<ServiceListener> it = serviceListeners.iterator();
        while (it.hasNext()) it.next().serviceAdded(name, implementationClass);
    }
    
    void fireServiceRemoved(String name) {
        Iterator<ServiceListener> it = serviceListeners.iterator();
        while (it.hasNext()) it.next().serviceRemoved(name);
    }
    
    public static interface ServiceListener {
        
        public void serviceAdded(String name, String implementationClass);
        
        public void serviceRemoved(String name);
        
    }
    
}
