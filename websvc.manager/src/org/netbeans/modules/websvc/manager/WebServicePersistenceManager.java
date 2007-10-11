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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.websvc.manager;

import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.*;
import java.io.BufferedOutputStream;
import java.util.*;

import java.beans.ExceptionListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * WebServicePersistenceManager.java
 * @author  Winston Prakash, quynguyen
 */
public class WebServicePersistenceManager implements ExceptionListener {
    private File websvcDir = new File(WebServiceManager.WEBSVC_HOME);
    private File websvcRefFile = new File(websvcDir, "websvc_ref.xml");
    private List<WebServiceDescriptor> descriptorsToWrite = null;
    
    public void load() {        
        if (websvcRefFile.exists()) {
            try{
                XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
                List<WebServiceData> wsDatas = new ArrayList<WebServiceData>();
                List<WebServiceGroup> wsGroups = new ArrayList<WebServiceGroup>();
                
                List<String> partnerServices = WebServiceListModel.getInstance().getPartnerServices();
                Object firstObject = decoder.readObject();
                int wsDataNums;
                
                if (firstObject instanceof List) {
                    List<String> loadedServices = (List<String>)firstObject;
                    for (String url : loadedServices) {
                        partnerServices.add(url);
                    }
                    wsDataNums = ((Integer)decoder.readObject()).intValue();
                }else {
                    wsDataNums = ((Integer)firstObject).intValue();
                }
                
                for(int i=0; i< wsDataNums; i++){
                    WebServiceData wsData = null;
                    try{
                        wsData = (WebServiceData) decoder.readObject();
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }
                    
                    wsDatas.add(wsData);
                }
                int wsGroupSize = ((Integer)decoder.readObject()).intValue();
                for(int i=0; i< wsGroupSize; i++){
                    try{
                        WebServiceGroup group = (WebServiceGroup) decoder.readObject();
                        wsGroups.add(group);
                    }catch(Exception exc){
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }
                }
                decoder.close();
                
                for (WebServiceData wsData : wsDatas) {
                    if (wsData.getJaxRpcDescriptorPath() != null)
                        wsData.setJaxRpcDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxRpcDescriptorPath()));
                    
                    if (wsData.getJaxWsDescriptorPath() != null)
                        wsData.setJaxWsDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxWsDescriptorPath()));
                    WebServiceListModel.getInstance().addWebService(wsData);
                }
                
                for (WebServiceGroup group : wsGroups) {
                    try {
                        WebServiceListModel.getInstance().addWebServiceGroup(group);
                    }catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
                
            }catch(Exception exc){
                exc.printStackTrace();
            }
        }
        
        loadPartnerServices();
    }
    
    public void save() {
        WebServiceListModel model = WebServiceListModel.getInstance();
        if (!model.isInitialized()) {
            return;
        }
        
        if (!websvcDir.exists())websvcDir.mkdirs();
        if (websvcRefFile.exists()) websvcRefFile.delete();
        try{
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(websvcRefFile)));
            encoder.setExceptionListener(this);
            
            DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);
            
            encoder.writeObject(model.getPartnerServices());
            
            List<WebServiceData> wsDataSet =  model.getWebServiceSet();
            encoder.writeObject(new Integer(wsDataSet.size()));
            
            synchronized (wsDataSet) {
                for (WebServiceData wsData : wsDataSet) {
                    encoder.writeObject(wsData);
                }
            }
            
            List<WebServiceGroup> wsGroupSet =  model.getWebServiceGroupSet();
            encoder.writeObject(new Integer(wsGroupSet.size()));
            
            synchronized (wsGroupSet) {
                for (WebServiceGroup group : wsGroupSet) {
                    encoder.writeObject(group);
                }
            }
            
            encoder.close();
            encoder.flush();
            delegate = new DefaultPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);
            
            if (descriptorsToWrite != null) {
                for (WebServiceDescriptor descriptor : descriptorsToWrite) {
                    saveWebServiceDescriptor(descriptor);
                }
            }
        } catch(Exception exc){
            ErrorManager.getDefault().notify(exc);
            return;
        }
    }
    
    private WebServiceDescriptor loadDescriptorFile(String descriptorPath) {
        if (descriptorPath == null || descriptorPath.length() == 0) {
            return null;
        }else {
            XMLDecoder decoder = null;
            try {
                decoder = new java.beans.XMLDecoder(new java.io.BufferedInputStream(new java.io.FileInputStream(descriptorPath)));
                return (WebServiceDescriptor)decoder.readObject();
            } catch (Exception ex) {
                exceptionThrown(ex);
                return null;
            } finally {
                if (decoder != null) {
                    decoder.close();
                }
            }
        }
        
    }
    
    /**
     * Loads (or reloads if the services already exist) a set of partner services
     * 
     * @param serviceFolder the folder location of the component definitions in the system filesystem
     * @param partnerName optional partner name for the web service group folder name, should be null 
     *        or identical to existing group name if overwriting an existing component's folder
     */
    public static void loadPartnerService(String serviceFolder, String partnerName) {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = sfs.findResource(serviceFolder);
        
        loadPartnerFromFolder(folder, partnerName, true);
    }
    
    public static void loadPartnerServices() {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        
        FileObject f = sfs.findResource("RestComponents"); // NOI18N
        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject nextFolder = en.nextElement();
                String groupName = nextFolder.getName();
                //TODO: (nam) if and how to internalize partner's name
                if (groupName.equals("StrikeIron")) { // NOI18N
                    groupName = NbBundle.getMessage(WebServicePersistenceManager.class, "STRIKE_IRON_GROUP");
                }
                
                loadPartnerFromFolder(nextFolder, groupName, false);
            }
        }
    }
    
    private static void loadPartnerFromFolder(FileObject folder, String groupName, boolean reloadIfExists) {
        if (folder == null || !folder.isFolder()) {
            return;
        }
        
        Map<String, String> currentUrls = new HashMap<String, String>(); //url->name
        List<String> partnerUrls = WebServiceListModel.getInstance().getPartnerServices();

        FileObject[] contents = folder.getChildren();
        for (int i = 0; i < contents.length; i++) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(contents[i].getInputStream());
                NodeList nodes = doc.getElementsByTagName("method"); // NOI18N

                for (int j = 0; j < nodes.getLength(); j++) {
                    NamedNodeMap attributes = nodes.item(j).getAttributes();
                    String type = attributes.getNamedItem("type").getNodeValue(); // NOI18N
                    String url = attributes.getNamedItem("url").getNodeValue(); // NOI18N

                    boolean addUrl = !currentUrls.containsKey(url) && (reloadIfExists ||
                            (!reloadIfExists && !partnerUrls.contains(url)));
                    
                    if ("http://schemas.xmlsoap.org/wsdl/".equals(type) && addUrl) { // NOI18N
                        String serviceName = attributes.getNamedItem("serviceName").getNodeValue(); //NOI18N
                        currentUrls.put(url, serviceName);
                    }
                }
            } catch (Exception ex) {
                String msg = NbBundle.getMessage(WebServicePersistenceManager.class, "MSG_BadContent", contents[i].getPath());
                Throwable t = ErrorManager.getDefault().annotate(ex, msg);
                ErrorManager.getDefault().notify(t);
            }
        }

        if (currentUrls.size() > 0) {
            if (groupName == null) {
                groupName = folder.getName();
                //TODO: (nam) if and how to internalize partner's name
                if (groupName.equals("StrikeIron")) { // NOI18N
                    groupName = NbBundle.getMessage(WebServicePersistenceManager.class, "STRIKE_IRON_GROUP");
                }
            }
            
            WebServiceGroup newGroup = null;
            List<WebServiceGroup> webServiceGroups = WebServiceListModel.getInstance().getWebServiceGroupSet();
            for (WebServiceGroup group : webServiceGroups) {
                if (!group.isUserDefined() && group.getName().equals(groupName)) {
                    newGroup = group;
                    break;
                }
            }
            
            if (newGroup == null) {
                newGroup = new WebServiceGroup(WebServiceListModel.getInstance().getUniqueWebServiceGroupId());
                newGroup.setName(groupName);
                newGroup.setUserDefined(false);
            }

            for (Map.Entry<String, String> entry : currentUrls.entrySet()) {
                String url = entry.getKey();
                
                // !reloadIfExists -> !partnerUrls.contains(url)
                if (!reloadIfExists || !partnerUrls.contains(url)) {
                    // Add a new web service
                    partnerUrls.add(url);
                    WebServiceData wsData = new WebServiceData(url, url, newGroup.getId());
                    wsData.setName(entry.getValue());
                    WebServiceListModel.getInstance().addWebService(wsData);

                    newGroup.add(wsData.getId(), true);
                }else {
                    // reset an existing service
                    WebServiceData existingData = null;
                    List<WebServiceData> wsDatas = WebServiceListModel.getInstance().getWebServiceSet();
                    for (WebServiceData wsData : wsDatas) {
                        if (wsData.getOriginalWsdl().equals(url)) {
                            existingData = wsData;
                            break;
                        }
                    }
                    
                    if (existingData != null) {
                        WebServiceManager.getInstance().resetWebService(existingData);
                        existingData.setName(entry.getValue());
                    }else {
                        WebServiceData wsData = new WebServiceData(url, url, newGroup.getId());
                        wsData.setName(entry.getValue());
                        WebServiceListModel.getInstance().addWebService(wsData);

                        newGroup.add(wsData.getId(), true);
                    }
                }
            }

            WebServiceListModel.getInstance().addWebServiceGroup(newGroup);
        }     
    }
    
    private void saveWebServiceDescriptor(WebServiceDescriptor descriptor) {
        try {
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(descriptor.getXmlDescriptor())));
            encoder.setExceptionListener(this);
            DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
            encoder.setPersistenceDelegate(WsdlService.class, delegate);
            encoder.writeObject(descriptor);
            
            encoder.close();
            encoder.flush();
        }catch (IOException ex) {
            exceptionThrown(ex);
        }
    }
    
    public void exceptionThrown(Exception exc) {
        ErrorManager.getDefault().notify(exc);
    }
    
    public class WebServiceDataPersistenceDelegate extends DefaultPersistenceDelegate {
        /**
         * Suppress the writing of 
         * org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService
         * It will be created from the WSDL file
         */  
        public void writeObject(Object oldInstance, Encoder out) {
            if(oldInstance instanceof WsdlService) {
                return;
            }else if (oldInstance instanceof WebServiceDescriptor) {
                if (descriptorsToWrite == null) {
                    descriptorsToWrite = new ArrayList<WebServiceDescriptor>();
                }
                descriptorsToWrite.add((WebServiceDescriptor)oldInstance);
            }else {
                super.writeObject(oldInstance,out);
            } 
        }  
    }
    
}
