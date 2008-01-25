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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.ChangeSupport;

/** Provides information about web services and clients in a project
 * Provides information used for build-impl generation
 * Working over nbproject/jax-ws.xml file
 */
public final class JaxWsModel {
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws;
    private FileObject fo;
    private Object initLock = new Object();
    private List<ServiceListener> serviceListeners;
    private List<PropertyChangeListener> propertyChangeListeners;
    private ChangeSupport changeSupport;
    
    JaxWsModel(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws) {
        this(jaxws,null);
    }
    
    JaxWsModel(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws, FileObject fo) {
        this.jaxws=jaxws;
        this.fo=fo;
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        serviceListeners = new ArrayList<ServiceListener>();
        changeSupport = new ChangeSupport(this);
    }
    
    public Service[] getServices() {
        
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
            fireServiceRemoved(name);
            jaxws.getServices().removeService(service);
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
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        JaxWsPCL jaxWsPcl = new JaxWsPCL(l);
        propertyChangeListeners.add(jaxWsPcl);
        jaxws.addPropertyChangeListener(jaxWsPcl);
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        for (PropertyChangeListener pcl:propertyChangeListeners) {
            if (l == ((JaxWsPCL)pcl).getOriginalListener()) {
                jaxws.removePropertyChangeListener(pcl);
                propertyChangeListeners.remove(pcl);
                break;
            }
        }
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
        changeSupport.fireChange();
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
    
    private class JaxWsPCL implements PropertyChangeListener {

        PropertyChangeListener originalListener;
        JaxWsPCL(PropertyChangeListener originalListener) {
            this.originalListener = originalListener;
            
        }
        public void propertyChange(PropertyChangeEvent evt) {
           Object oldValue = evt.getOldValue();
           if (oldValue != null) {
               if (oldValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client) {
                   oldValue = new Client((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client)oldValue);
               }
               if (oldValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service) {
                   oldValue = new Service((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service)oldValue);
               }
           }
           Object newValue = evt.getNewValue();
           if (newValue != null) {
               if (newValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client) {
                   newValue = new Client((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client)newValue);
               }
               if (newValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service) {
                   newValue = new Service((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service)newValue);
               }
           }
           originalListener.propertyChange(new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), oldValue, newValue));
        }
        
        PropertyChangeListener getOriginalListener() {
            return originalListener;
        }
        
    }
    
    /** Registers ChangeListener for JaxWsModel object.
     *  The listener fires the ChangeEvent when FileObject is set for JaxWsModel
     *  (For projects, this occurs when jax-ws.xml is physicaly created in nbproject directory)   
     * 
     * @param listener ChangeListener instance
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    /** Unregisters ChangeListener from JaxWsModel object.
     * 
     * @param listener ChangeListener instance
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
}
