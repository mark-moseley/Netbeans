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
package org.netbeans.modules.j2ee.sun.dd.impl.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;
import org.netbeans.modules.j2ee.sun.dd.impl.DDTreeWalker;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.common.DDProviderDataObject;
import org.netbeans.modules.schema2beans.Schema2BeansUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;


/**
 *
 * @author Nitya Doraisamy
 * @author Peter Williams
 */
public class SunApplicationClientProxy implements SunApplicationClient, RootInterfaceImpl {
    
    private SunApplicationClient appClientRoot;
    private String version;
    private int ddStatus;
    private SAXParseException error;    
    private List<PropertyChangeListener> listeners; 
    private Schema2BeansUtil.ReindentationListener reindentationListener = new Schema2BeansUtil.ReindentationListener();
    

    public SunApplicationClientProxy(SunApplicationClient appClientRoot, String version) {
        this.appClientRoot = appClientRoot;
        this.version = version;
        this.listeners = new ArrayList<PropertyChangeListener>();
        addPropertyChangeListener(reindentationListener);
    }

    public void setEjbRef(int index, EjbRef value) {
        if (appClientRoot != null) appClientRoot.setEjbRef(index, value);
    }

    public EjbRef getEjbRef(int index) {
        return appClientRoot == null?null:appClientRoot.getEjbRef(index);
    }

    public int sizeEjbRef() {
        return appClientRoot == null?-1:appClientRoot.sizeEjbRef();
    }

    public void setEjbRef(EjbRef[] value) {
        if (appClientRoot != null) appClientRoot.setEjbRef(value);
    }

    public EjbRef[] getEjbRef() {
        return appClientRoot == null?null:appClientRoot.getEjbRef();
    }

    public int addEjbRef(EjbRef value) {
        return appClientRoot == null?-1:appClientRoot.addEjbRef(value);
    }

    public int removeEjbRef(EjbRef value) {
        return appClientRoot == null?-1:appClientRoot.removeEjbRef(value);
    }

    public EjbRef newEjbRef() {
        return appClientRoot == null?null:appClientRoot.newEjbRef();
    }

    public void setResourceRef(int index, ResourceRef value) {
        if (appClientRoot != null) appClientRoot.setResourceRef(index, value);
    }

    public ResourceRef getResourceRef(int index) {
        return appClientRoot == null?null:appClientRoot.getResourceRef(index);
    }

    public int sizeResourceRef() {
        return appClientRoot == null?-1:appClientRoot.sizeResourceRef();
    }

    public void setResourceRef(ResourceRef[] value) {
        if (appClientRoot != null) appClientRoot.setResourceRef(value);
    }

    public ResourceRef[] getResourceRef() {
        return appClientRoot == null?null:appClientRoot.getResourceRef();
    }

    public int addResourceRef(ResourceRef value) {
        return appClientRoot == null?-1:appClientRoot.addResourceRef(value);
    }

    public int removeResourceRef(ResourceRef value) {
        return appClientRoot == null?-1:appClientRoot.removeResourceRef(value);
    }

    public ResourceRef newResourceRef() {
        return appClientRoot == null?null:appClientRoot.newResourceRef();
    }

    public void setResourceEnvRef(int index, ResourceEnvRef value) {
        if (appClientRoot != null) appClientRoot.setResourceEnvRef(index, value);
    }

    public ResourceEnvRef getResourceEnvRef(int index) {
        return appClientRoot == null?null:appClientRoot.getResourceEnvRef(index);
    }

    public int sizeResourceEnvRef() {
        return appClientRoot == null?-1:appClientRoot.sizeResourceEnvRef();
    }

    public void setResourceEnvRef(ResourceEnvRef[] value) {
        if (appClientRoot != null) appClientRoot.setResourceEnvRef(value);
    }

    public ResourceEnvRef[] getResourceEnvRef() {
        return appClientRoot == null?null:appClientRoot.getResourceEnvRef();
    }

    public int addResourceEnvRef(ResourceEnvRef value) {
        return appClientRoot == null?-1:appClientRoot.addResourceEnvRef(value);
    }

    public int removeResourceEnvRef(ResourceEnvRef value) {
        return appClientRoot == null?-1:appClientRoot.removeResourceEnvRef(value);
    }

    public ResourceEnvRef newResourceEnvRef() {
        return appClientRoot == null?null:appClientRoot.newResourceEnvRef();
    }

    public void setServiceRef(int index, ServiceRef value) {
        if (appClientRoot != null) appClientRoot.setServiceRef(index, value);
    }

    public ServiceRef getServiceRef(int index) {
        return appClientRoot == null?null:appClientRoot.getServiceRef(index);
    }

    public int sizeServiceRef() {
        return appClientRoot == null?-1:appClientRoot.sizeServiceRef();
    }

    public void setServiceRef(ServiceRef[] value) {
        if (appClientRoot != null) appClientRoot.setServiceRef(value);
    }

    public ServiceRef[] getServiceRef() {
        return appClientRoot == null?null:appClientRoot.getServiceRef();
    }

    public int addServiceRef(ServiceRef value) {
        return appClientRoot == null?-1:appClientRoot.addServiceRef(value);
    }

    public int removeServiceRef(ServiceRef value) {
        return appClientRoot == null?-1:appClientRoot.removeServiceRef(value);
    }

    public ServiceRef newServiceRef() {
        return appClientRoot == null?null:appClientRoot.newServiceRef();
    }

    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException {
        if (appClientRoot != null) appClientRoot.setMessageDestinationRef(index, value);
    }

    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException {
        return appClientRoot == null?null:appClientRoot.getMessageDestinationRef(index);
    }

    public int sizeMessageDestinationRef() throws VersionNotSupportedException {
        return appClientRoot == null?-1:appClientRoot.sizeMessageDestinationRef();
    }

    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException {
        if (appClientRoot != null) appClientRoot.setMessageDestinationRef(value);
    }

    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException {
        return appClientRoot == null?null:appClientRoot.getMessageDestinationRef();
    }

    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        return appClientRoot == null?-1:appClientRoot.addMessageDestinationRef(value);
    }

    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException {
        return appClientRoot == null?-1:appClientRoot.removeMessageDestinationRef(value);
    }

    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException {
        return appClientRoot == null?null:appClientRoot.newMessageDestinationRef();
    }

    public void setMessageDestination(int index, MessageDestination value) {
        if (appClientRoot != null) appClientRoot.setMessageDestination(index, value);
    }

    public MessageDestination getMessageDestination(int index) {
        return appClientRoot == null?null:appClientRoot.getMessageDestination(index);
    }

    public int sizeMessageDestination() {
        return appClientRoot == null?-1:appClientRoot.sizeMessageDestination();
    }

    public void setMessageDestination(MessageDestination[] value) {
        if (appClientRoot != null) appClientRoot.setMessageDestination(value);
    }

    public MessageDestination[] getMessageDestination() {
        return appClientRoot == null?null:appClientRoot.getMessageDestination();
    }

    public int addMessageDestination(MessageDestination value) {
        return appClientRoot == null?-1:appClientRoot.addMessageDestination(value);
    }

    public int removeMessageDestination(MessageDestination value) {
        return appClientRoot == null?-1:appClientRoot.removeMessageDestination(value);
    }

    public MessageDestination newMessageDestination() {
        return appClientRoot == null?null:appClientRoot.newMessageDestination();
    }

    public void setJavaWebStartAccess(JavaWebStartAccess value) throws VersionNotSupportedException {
        if (appClientRoot != null) appClientRoot.setJavaWebStartAccess(value);
    }

    public JavaWebStartAccess getJavaWebStartAccess() throws VersionNotSupportedException {
        return appClientRoot == null?null:appClientRoot.getJavaWebStartAccess();
    }

    public JavaWebStartAccess newJavaWebStartAccess() throws VersionNotSupportedException {
        return appClientRoot == null?null:appClientRoot.newJavaWebStartAccess();
    }

    public void setVersion(BigDecimal version) {
        String newVersion = version.toString();
        String currentVersion = null;
        if (this.version.equals(newVersion))
            return;
        if (appClientRoot != null) {
            Document document = null;
            if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
                currentVersion = SunApplicationClient.VERSION_1_3_0;
            }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
                currentVersion = SunApplicationClient.VERSION_1_4_0;
            }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
                currentVersion = SunApplicationClient.VERSION_1_4_1;
            }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient) {
                document =
                        ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
                currentVersion = SunApplicationClient.VERSION_5_0_0;
            }
            
            //remove the doctype
            document = removeDocType(document);
            
            if(newVersion.equals(SunApplicationClient.VERSION_5_0_0)){
                //This will always be an upgrade
                generate5_00Graph(document);
            }
            if(newVersion.equals(SunApplicationClient.VERSION_1_4_1)){
                if(currentVersion.equals(SunApplicationClient.VERSION_1_4_0) || currentVersion.equals(SunApplicationClient.VERSION_1_3_0))
                    generate1_41Graph(document);
                else
                    downgradeClientJarGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunApplicationClient.VERSION_1_4_0)){
                if(currentVersion.equals(SunApplicationClient.VERSION_1_3_0))
                    generate1_40Graph(document);
                else
                    downgradeClientJarGraph(document, newVersion, currentVersion);
            }
            if(newVersion.equals(SunApplicationClient.VERSION_1_3_0)){
                //This will always be a downgrade             
                downgradeClientJarGraph(document, newVersion, currentVersion);
            }
        }
    }

    private void downgradeClientJarGraph(Document document, String downgradeVersion, String currentVersion){
            DDTreeWalker downgradeScanner = new DDTreeWalker(document, downgradeVersion, currentVersion);
            downgradeScanner.downgradeSunClientDocument();
            if(downgradeVersion.equals(SunApplicationClient.VERSION_1_4_1)){
                generate1_41Graph(document);
            }else if(downgradeVersion.equals(SunApplicationClient.VERSION_1_4_0)){
                generate1_40Graph(document);
            }else if(downgradeVersion.equals(SunApplicationClient.VERSION_1_3_0)){
                generate1_30Graph(document);
            }
    }
    
    private void generate5_00Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient appClientGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient.createGraph(document);
        appClientGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_50_DTD_SYSTEM_ID);
        this.appClientRoot = appClientGraph;
    }
    
    private void generate1_41Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient appClientGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient.createGraph(document);
        appClientGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_141_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_141_DTD_SYSTEM_ID);
        this.appClientRoot = appClientGraph;
    }
    
    private void generate1_40Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient appClientGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient.createGraph(document);
        appClientGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_140_DTD_SYSTEM_ID);
        this.appClientRoot = appClientGraph;
    }
    
    private void generate1_30Graph(Document document){
        org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient appClientGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient.createGraph(document);
        appClientGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_130_DTD_SYSTEM_ID);
        this.appClientRoot = appClientGraph;
    }
    
    public BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }

    private Document getDocument(){
        Document document = null;
        if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
        }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
        }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
        }else if (appClientRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient)appClientRoot).graphManager().getXmlDocument();
        }
        return document;
    }
    
    private Document removeDocType(Document document){
        if (document != null) {
            org.w3c.dom.Element docElement = document.getDocumentElement();
            if (docElement != null) {
                org.w3c.dom.DocumentType docType = document.getDoctype();
                if (docType != null) {
                    document.removeChild(docType); //NOI18N
                }
            }
        }
        return document;
    }
    public void merge(CommonDDBean root, int mode) {
        if (root instanceof SunApplicationClientProxy) {
            root = ((SunApplicationClientProxy) root).getOriginal();
        }
        if (appClientRoot != root && root instanceof SunApplicationClient) {
            SunApplicationClient newAppClientRoot = (SunApplicationClient) root;
            if (appClientRoot != null && appClientRoot.getVersion().equals(newAppClientRoot.getVersion())) {
                removePropertyChangeListener(reindentationListener);
                appClientRoot.merge(newAppClientRoot, mode);
                addPropertyChangeListener(reindentationListener);
            } else {
                setOriginal((SunApplicationClient) newAppClientRoot.clone());
            }
        }
    }

    public CommonDDBean cloneVersion(String version) {
        return appClientRoot == null ? null : appClientRoot.cloneVersion(version);
    }
   
    public void setOriginal(SunApplicationClient appClientRoot) {
        if (this.appClientRoot != appClientRoot) {
            for (int i=0;i<listeners.size();i++) {
                PropertyChangeListener pcl = listeners.get(i);
                if (this.appClientRoot != null) {
                    this.appClientRoot.removePropertyChangeListener(pcl);
                }
                if (appClientRoot != null) {
                    appClientRoot.addPropertyChangeListener(pcl);
                }
            }
            this.appClientRoot = appClientRoot;
            if (appClientRoot != null) {
                setProxyVersion(appClientRoot.getVersion().toString());
            }
        }
    }
    
    public SunApplicationClient getOriginal() {
        return appClientRoot;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (appClientRoot != null) 
            appClientRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (appClientRoot != null) 
            appClientRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public Object getValue(String propertyName) {
        return appClientRoot == null?null:appClientRoot.getValue(propertyName);
    }

    public Object[] getValues(String name) {
        return appClientRoot == null?null:appClientRoot.getValues(name);
    }

    public Object getValue(String name, int index) {
        return appClientRoot == null?null:appClientRoot.getValue(name, index);
    }

    public void setValue(String name, Object value) {
        if (appClientRoot != null) appClientRoot.setValue(name, value);
    }

    public void setValue(String name, Object[] value) {
        if (appClientRoot != null) appClientRoot.setValue(name, value);
    }

    public void setValue(String name, int index, Object value) {
        if (appClientRoot != null) appClientRoot.setValue(name, index, value);
    }

    public String getAttributeValue(String name) {
        return appClientRoot == null?null:appClientRoot.getAttributeValue(name);
    }

    public String getAttributeValue(String propName, String name) {
        return appClientRoot == null?null:appClientRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return appClientRoot == null?null:appClientRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (appClientRoot != null) appClientRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (appClientRoot != null) appClientRoot.setAttributeValue(propName, index, name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (appClientRoot != null) appClientRoot.setAttributeValue(propName, name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return appClientRoot == null?null:appClientRoot.findPropertyValue(propName, value);
    }

    public int addValue(String name, Object value) {
        return appClientRoot == null?-1:appClientRoot.addValue(name, value);
    }

    public int removeValue(String name, Object value) {
        return appClientRoot == null?-1:appClientRoot.removeValue(name, value);
    }

    public void removeValue(String name, int index) {
        if (appClientRoot != null) appClientRoot.removeValue(name, index);
    }

    public void write(OutputStream os) throws IOException {
        if (appClientRoot != null) {
            appClientRoot.write(os);
        }
    }

    public void write(Writer w) throws IOException, DDException {
        if (appClientRoot != null) appClientRoot.write(w);
    }
    
    public void write(FileObject fo) throws IOException {
        if(appClientRoot != null) {
            DataObject dataObject = DataObject.find(fo);
            if(dataObject instanceof DDProviderDataObject) {
                ((DDProviderDataObject) dataObject).writeModel(appClientRoot);
            } else {
                FileLock lock = fo.lock();
                try {
                    OutputStream os = fo.getOutputStream(lock);
                    try {
                        write(os);
                    } finally {
                        os.close(); 
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        }
    }
    
    public String dumpBeanNode() {
        if (appClientRoot != null) 
            return appClientRoot.dumpBeanNode();
        else
            return null;
    }

    public CommonDDBean getPropertyParent(String name) {
        return appClientRoot.getPropertyParent(name);
    }
    
    public Object clone() {
        SunApplicationClientProxy proxy = null;
        if (appClientRoot == null)
            proxy = new SunApplicationClientProxy(null, version);
        else {
            SunApplicationClient clonedSunAppClient=(SunApplicationClient)appClientRoot.clone();
            proxy = new SunApplicationClientProxy(clonedSunAppClient, version);
        }
        proxy.setError(error);
        return proxy;
    }
    
    public SAXParseException getError() {
        return error;
    }
    
    public void setError(SAXParseException error) {
        this.error=error;
    }

    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || !version.equals(value)) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_VERSION, version, value); 
            version=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public int size(String name) {
        return appClientRoot == null?-1:appClientRoot.size(name);
    }
    
    public int getStatus() {
        return ddStatus;
    }
    
    public void setStatus(int value) {
        if (ddStatus!=value) {
            PropertyChangeEvent evt = new PropertyChangeEvent(
                    this, PROPERTY_STATUS, new Integer(ddStatus), new Integer(value));
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                listeners.get(i).propertyChange(evt);
            }
        }
    }
    
    public RootInterface getRootInterface() {
        return this;
    }
    
    public boolean hasOriginal() {
        return getOriginal() != null;
    }
}
