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

package org.netbeans.modules.identity.profile.ui.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper.ProjectType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class for manipulating sun dd files.
 *
 * Created on August 7, 2006, 12:11 PM
 *
 * @author ptliu
 */
public class SunDDHelper {
    private static final String SUN_WEB_APP_TAG = "sun-web-app"; //NOI18N
    
    private static final String HTTPSERVLET_SECURITY_PROVIDER = "httpservlet-security-provider";    //NOI18N
    
    private static final String AM_HTTP_PROVIDER = "AMHttpProvider";    //NOI18N
    
    private static final String SECURITY_ROLE_MAPPING_TAG = "security-role-mapping";    //NOI18N
    
    private static final String ROLE_NAME_TAG = "role-name";    //NOI18N
    
    private static final String PRINCIPAL_NAME_TAG = "principal-name";  //NOI18N
    
    private static final String SERVLET_TAG = "servlet";            //NOI18N
    
    private static final String SERVLET_NAME_TAG = "servlet-name";  //NOI18N
    
    private static final String EJB_TAG = "ejb";            //NOI18N
    
    private static final String EJB_NAME_TAG = "ejb-name";  //NOI18N
    
    private static final String WEBSERVICE_ENDPOINT_TAG = "webservice-endpoint"; //NOI18N
    
    private static final String PORT_COMPONENT_NAME_TAG = "port-component-name"; //NOI18N
    
    private static final String MESSAGE_SECURITY_BINDING_TAG = "message-security-binding";  //NOI18N
    
    private static final String MESSAGE_SECURITY_TAG = "message-security";      //NOI18N
    
    private static final String MESSAGE_TAG = "message";        //NOI18N
    
    private static final String REQUEST_PROTECTION_TAG = "request-protection";  //NOI18N
    
    private static final String RESPONSE_PROTECTION_TAG = "response-protection";    //NOI18N
    
    private static final String AUTH_LAYER_ATTR = "auth-layer";     //NOI18N
    
    private static final String PROVIDER_ID_ATTR = "provider-id";   //NOI18N
    
    private static final String AUTH_SOURCE_ATTR = "auth-source"; //NOI18N
    
    private static final String AM_SERVER_PROVIDER_PREFIX = "AMServerProvider-"; //NOI18N
    
    private static final String AM_CLIENT_PROVIDER = "AMClientProvider";        //NOI18N
    
    private static final String REQUEST_POLICY_AUTH_SOURCE = "content";          //NOI18N
    
    private static final String RESPONSE_POLICY_AUTH_SOURCE = "content";        //NOI18N
    
    private static final String AUTH_LAYER = "SOAP";                            //NOI18N
    
    private static final String AUTHENTICATED_USERS = "AUTHENTICATED_USERS";    //NOI18N
    
    private static final String CONTEXT_ROOT_TAG = "context-root";  //NOI18N
    
    private static final String CLASS_LOADER_TAG = "class-loader";  //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_4 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_4-1.dtd";   //NOI18N
    
    private static final String SUN_WEB_SYSTEM_ID_2_5 = "http://www.sun.com/software/appserver/dtds/sun-web-app_2_5-0.dtd";   //NOI18N
    
    private static final String SUN_WEB_DTD_2_4 = "resources/sun-web-app_2_4-1.dtd";    //NOI18N
    
    private static final String SUN_WEB_DTD_2_5 = "resources/sun-web-app_2_5-0.dtd";    //NOI18N
    
    private static int TIME_TO_WAIT = 300;
    
    private FileObject sunDD;
    private Document document;
    private ProjectType type;
    
    public SunDDHelper(FileObject sunDD, ProjectType type) {
        this.sunDD = sunDD;
        this.type = type;
    }
    
    public void addSecurityRoleMapping() {
        System.out.println("addSecurityRoleMapping");
        
        //final FileChangeListener fcl = new FileChangeAdapter() {
        //    public void fileChanged(FileEvent event) {
        boolean isModified = false;
        Element sunWebApp = getSunWebAppElement();
        String value = sunWebApp.getAttribute(HTTPSERVLET_SECURITY_PROVIDER);
        
        if (value == null || !value.equals(AM_HTTP_PROVIDER)) {
            sunWebApp.setAttribute(HTTPSERVLET_SECURITY_PROVIDER, AM_HTTP_PROVIDER);
            isModified = true;
        }
        
        Element mapping = getSecurityRoleMapping(AUTHENTICATED_USERS);
        
        if (mapping == null) {
            sunWebApp.appendChild(createSecurityRoleMapping(AUTHENTICATED_USERS));
            isModified = true;
        } else {
            Element principal = getPrincipalName(mapping, AUTHENTICATED_USERS);
            
            if (principal == null) {
                mapping.appendChild(createElement(PRINCIPAL_NAME_TAG, AUTHENTICATED_USERS));
                isModified = true;
            }
        }
        
        System.out.println("isModified = " + isModified);
        if (isModified) writeDocument();
        
        //sunDD.removeFileChangeListener(this);
        //}
        //};
        
        //sunDD.addFileChangeListener(fcl);
    }
    
    public void removeSecurityRoleMapping() {
        //final FileChangeListener fcl = new FileChangeAdapter() {
        //    public void fileChanged(FileEvent event) {
        boolean isModified = false;
        Element sunWebApp = getSunWebAppElement();
        
        String value = sunWebApp.getAttribute(HTTPSERVLET_SECURITY_PROVIDER);
        
        if (value != null && value.equals(AM_HTTP_PROVIDER)) {
            sunWebApp.removeAttribute(HTTPSERVLET_SECURITY_PROVIDER);
            isModified = true;
        }
        
        Element mapping = getSecurityRoleMapping(AUTHENTICATED_USERS);
        
        if (mapping != null) {
            Element principal = getPrincipalName(mapping, AUTHENTICATED_USERS);
            
            if (principal != null) {
                mapping.removeChild(principal);
                isModified = true;
            }
            
            if (mapping.getElementsByTagName(PRINCIPAL_NAME_TAG).getLength() == 0) {
                sunWebApp.removeChild(mapping);
                isModified = true;
            }
        }
        
        if (isModified) writeDocument();
        
        //sunDD.removeFileChangeListener(this);
        //}
        //};
        
        //sunDD.addFileChangeListener(fcl);
    }
    
    public void setServiceMessageSecurityBinding(String svcDescName,
            String pcName, String providerId) {
        Element root = null;
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
        
        if (type == ProjectType.WEB) {
            root = getSunWebAppElement();
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            
        }
        
        component = getComponentElement(componentTag, componentNameTag,
                pcName);
        
        if (component == null) {
            component = createComponentElement(componentTag, componentNameTag,
                    pcName);
            
            if (type == ProjectType.WEB) {
                insertServletElement(root, component);
            } else {
                root.appendChild(component);
            }
        }
        
        Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
        
        if (endpointElement == null) {
            endpointElement = createWebServiceEndpointElement(pcName);
            component.appendChild(endpointElement);
        }
        
        Element binding = getElement(endpointElement, MESSAGE_SECURITY_BINDING_TAG);
        
        if (binding == null) {
            binding = createSecurityBindingElement();
            endpointElement.appendChild(binding);
        }
        
        binding.setAttribute(PROVIDER_ID_ATTR,
                AM_SERVER_PROVIDER_PREFIX + providerId);
        
        writeDocument();
    }
    
    public void removeServiceMessageSecurityBinding(String svcDescName,
            String pcName) {
        Element root = null;
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
        
        if (type == ProjectType.WEB) {
            root = getSunWebAppElement();
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            
        }
        
        component = getComponentElement(componentTag, componentNameTag,
                pcName);
        
        if (component != null) {
            Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
            
            if (endpointElement != null) {
                component.removeChild(endpointElement);
            }
        }
        
        writeDocument();
    }
    
    
    public boolean isSecurityEnabled(String svcDescName, String pcName) {
        Element root = null;
        Element component = null;
        String componentTag = null;
        String componentNameTag = null;
        
        if (type == ProjectType.WEB) {
            root = getSunWebAppElement();
            componentTag = SERVLET_TAG;
            componentNameTag = SERVLET_NAME_TAG;
        } else if (type == ProjectType.EJB) {
            
        } else {
            return false;
        }
        
        component = getComponentElement(componentTag, componentNameTag,
                pcName);
        
        if (component != null) {
            Element endpointElement = getElement(component, WEBSERVICE_ENDPOINT_TAG);
            
            if (endpointElement != null) {
                Element binding = getElement(endpointElement, MESSAGE_SECURITY_BINDING_TAG);
                
                if (binding != null) {
                    if (binding.getAttribute(PROVIDER_ID_ATTR).startsWith(AM_SERVER_PROVIDER_PREFIX)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private Element getSunWebAppElement() {
        Document document = getDocument();
        NodeList nodes = document.getElementsByTagName(SUN_WEB_APP_TAG);
        
        return (Element) nodes.item(0);
    }
    
    private Element getComponentElement(String componentTag,
            String componentNameTag, String componentName) {
        Document document = getDocument();
        NodeList components = document.getElementsByTagName(componentTag);
        int length = components.getLength();
        
        for (int i = 0; i < length; i++) {
            Element component = (Element) components.item(0);
            NodeList names = component.getElementsByTagName(componentNameTag);
            
            if (names.getLength() > 0) {
                Element name = (Element) names.item(0);
                
                if (containsValue(name, componentName)) {
                    return component;
                }
            }
        }
        
        return null;
    }
    
    private Element createComponentElement(String componentTag,
            String componentNameTag, String componentName) {
        Document document = getDocument();
        Element componentElement = document.createElement(componentTag);
        componentElement.appendChild(createElement(componentNameTag, componentName));
        
        return componentElement;
    }
    
    private Element getElement(Element component, String tagName) {
        NodeList elements = component.getElementsByTagName(tagName);
        
        if (elements.getLength() > 0) {
            return (Element) elements.item(0);
        }
        
        return null;
    }
    
    private Element createWebServiceEndpointElement(String endpointName) {
        Document document = getDocument();
        Element endpoint = document.createElement(WEBSERVICE_ENDPOINT_TAG);
        endpoint.appendChild(createElement(PORT_COMPONENT_NAME_TAG, endpointName));
        
        return endpoint;
    }
    
    private Element createSecurityBindingElement() {
        Document document = getDocument();
        Element binding = document.createElement(MESSAGE_SECURITY_BINDING_TAG);
        binding.setAttribute(AUTH_LAYER_ATTR, AUTH_LAYER);
        Element security = document.createElement(MESSAGE_SECURITY_TAG);
        security.appendChild(document.createElement(MESSAGE_TAG));
        Element request = document.createElement(REQUEST_PROTECTION_TAG);
        request.setAttribute(AUTH_SOURCE_ATTR, REQUEST_POLICY_AUTH_SOURCE);
        security.appendChild(request);
        Element response = document.createElement(RESPONSE_PROTECTION_TAG);
        response.setAttribute(AUTH_SOURCE_ATTR, RESPONSE_POLICY_AUTH_SOURCE);
        security.appendChild(response);
        binding.appendChild(security);
        
        return binding;
    }
    
    private void insertServletElement(Element root, Element servletElement) {
        Element classLoader = getElement(root, CLASS_LOADER_TAG);
        root.insertBefore(servletElement, classLoader);
    }
    
    private Element getSecurityRoleMapping(String value) {
        Document document = getDocument();
        NodeList mappings = document.getElementsByTagName(SECURITY_ROLE_MAPPING_TAG);
        int length = mappings.getLength();
        
        for (int i = 0; i < length; i++) {
            Element mapping = (Element) mappings.item(i);
            NodeList roleNames = mapping.getElementsByTagName(ROLE_NAME_TAG);
            
            if (roleNames.getLength() > 0) {
                Element roleName = (Element) roleNames.item(0);
                
                if (containsValue(roleName, value)) {
                    return mapping;
                }
            }
        }
        
        return null;
    }
    
    private Element createSecurityRoleMapping(String value) {
        Document document = getDocument();
        Element mapping = document.createElement(SECURITY_ROLE_MAPPING_TAG);
        Element roleName = createElement(ROLE_NAME_TAG, value);
        Element principal = createElement(PRINCIPAL_NAME_TAG, value);
        mapping.appendChild(roleName);
        mapping.appendChild(principal);
        
        return mapping;
    }
    
    private Element createElement(String tag, String value) {
        Document document = getDocument();
        Element element = document.createElement(tag);
        Text text = document.createTextNode(value);
        element.appendChild(text);
        
        return element;
    }
    
    private Element getPrincipalName(Element mapping, String value) {
        NodeList nodes = mapping.getElementsByTagName(PRINCIPAL_NAME_TAG);
        int length = nodes.getLength();
        
        for (int i = 0; i < length; i++) {
            Element principal = (Element) nodes.item(i);
            
            if (containsValue(principal, value)) {
                return principal;
            }
        }
        
        return null;
    }
    
    private boolean containsValue(Element element, String value) {
        Node child = element.getFirstChild();
        
        if (child instanceof Text) {
            return (((Text) child).getWholeText().equals(value));
        }
        
        return false;
    }
    
    private void writeDocument() {
        //RequestProcessor.getDefault().post(new Runnable() {
        //   public void run() {
        FileLock lock = null;
        OutputStream os = null;
        
        try {
            Document document = getDocument();
            DocumentType docType = document.getDoctype();
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            DOMSource source = new DOMSource(document);
            
            lock = sunDD.lock();
            os = sunDD.getOutputStream(lock);
            StreamResult result = new StreamResult(os);
            
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N
            transformer.transform(source, result);
            
            //transformer.transform(source, new StreamResult(System.out));
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            if (lock != null) {
                lock.releaseLock();
            }
        }
        //}
        //}, TIME_TO_WAIT);
    }
    
    private Document getDocument() {
        if (document == null) {
            DocumentBuilder builder = getDocumentBuilder();
            if (builder == null)
                return null;
            
            FileLock lock = null;
            InputStream is = null;
            
            try {
                lock = sunDD.lock();
                is = sunDD.getInputStream();
                document = builder.parse(is);
            } catch (SAXException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
                
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        
        return document;
    }
    
    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);
        
        try {
            builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new SunWebDTDResolver());
        } catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return builder;
    }
    
    /**
     *
     *
     */
    private static class SunWebDTDResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            String dtd = null;
            
            if (systemId.equals(SUN_WEB_SYSTEM_ID_2_4)) {
                dtd = SUN_WEB_DTD_2_4;
            } else if (systemId.equals(SUN_WEB_SYSTEM_ID_2_5)) {
                dtd = SUN_WEB_DTD_2_5;
            }
            
            if (dtd != null) {
                InputStream is = this.getClass().getResourceAsStream(dtd);
                InputStreamReader isr = new InputStreamReader(is);
                return new InputSource(isr);
            } else {
                return null;
            }
        }
    }
}
