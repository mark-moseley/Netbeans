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

package org.netbeans.modules.websvc.jaxws.spi;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Milan Kuchtiak
 */
public interface JAXWSSupportImpl {
    
    public static final String XML_RESOURCES_FOLDER="xml-resources"; //NOI18N
    public static final String SERVICES_LOCAL_FOLDER="web-services"; //NOI18N
    public static final String CATALOG_FILE="catalog.xml"; //NOI18N
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from java
     * @param serviceName service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     */
    public void addService(String serviceName, String serviceImpl, boolean isJsr109);
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @param name service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param wsdlUrl url of the local wsdl file, e.g. file:/home/userXY/documents/wsdl/SearchService.wsdl"
     * @param serviceName service name (from service wsdl element), e.g. SearchService
     * @param portName port name (from service:port element), e.g. SearchServicePort
     * @param packageName package name where java artifacts will be generated
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     * @return returns the unique IDE service name
     */
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, 
            String portName, String packageName, boolean isJsr109);
    
    /**
     * Returns the list of web services in the project
     */
    public List/*Service*/ getServices();    
    
    /**
     * Remove the web service entries from the project properties
     * project.xml files
     */
    public void removeService(String serviceName);
    
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed 
     * or when impl.class is removed (manually from project) 
     */
    public void serviceFromJavaRemoved(String serviceName);
    
    /**
     * Returns the name of the implementation class
     * given the service (ide) name
     */
    public String getServiceImpl(String serviceName);
    
    /**
     * Determine if the web service was created from WSDL
     */
    public boolean isFromWSDL(String serviceName);
    
    /**
     *  return wsdl folder for local copy of WSDL
     */
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException;
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder);
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder);
    
    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();
    
    /** Get EntityCatalog for local copies of wsdl and schema files
     */
    public URL getCatalog();
    
    /** Get wsdlLocation information 
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public String getWsdlLocation(String serviceName);
   
    /**
     * Remove all entries associated with a non-JSR 109 entries
     * This may include entries in the module's deployment descriptor,
     * and entries in the implementation-specific descriptor file, sun-jaxws.xml.
     * This is provided as a service so that the node can also use it for cleanup.
     */
    public void removeNonJsr109Entries(String serviceName) throws IOException;
    
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */
    public FileObject getDeploymentDescriptorFolder();
    
}
