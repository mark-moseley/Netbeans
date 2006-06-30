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
package org.netbeans.modules.j2ee.websphere6.j2ee;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.websphere6.WSURIManager;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import javax.enterprise.deploy.spi.DeploymentManager;

/**
 *
 * @author dlm198383
 */
public class DeploymentManagerProperties {

    /**
     * Username property, its value is used by the deployment manager.
     */
    public static final String USERNAME_ATTR = "username"; //NOI18N
    
    /**
     * Password property, its value is used by the deployment manager.
     */
    public static final String PASSWORD_ATTR = "password"; //NOI18N
    
    
    /**
     * Display name property, its value is used by IDE to represent server instance.
     */
    public static final String DISPLAY_NAME_ATTR = "displayName"; //NOI18N
    
    /**
     * Location of the app server instance property, its value is used by IDE to represent server instance.
     */
    public static final String LOCATION_ATTR = "LOCATION"; //NOI1
    
    /**
     * Port property, its value is used by the deployment manager.
     */
    public static final String PORT_ATTR = "port";
    
    
    private InstanceProperties instanceProperties;
    private WSDeploymentManager WSDM;
    /** Creates a new instance of DeploymentManagerProperties */
    public DeploymentManagerProperties(DeploymentManager dm) {
        
        WSDM = (WSDeploymentManager)dm;
        
        instanceProperties = WSURIManager.getInstanceProperties(WSDM.getHost(),WSDM.getPort());
        
        if (instanceProperties==null){
            try {
                
                instanceProperties = WSURIManager.createInstanceProperties(
                        WSDM.getHost(),
                        WSDM.getPort(),
                        WSDM.getUsername(),
                        WSDM.getPassword(),
                        WSDM.getHost()+":"+WSDM.getPort());
            } catch (InstanceCreationException e){
                
            }
        }
    }
    
    /**
     * Getter for property location. can be null if the dm is remote
     * @return Value of property location.
     */
    
    
    /**
     * Getter for property password. can be null if the DM is a disconnected DM
     * @return Value of property password.
     */
    public java.lang.String getPassword() {
        if (instanceProperties==null)
            return null;
        return instanceProperties.getProperty(InstanceProperties.PASSWORD_ATTR) ;
    }
    
    /**
     * Setter for property password.
     * @param password New value of property password.
     */
    public void setPassword(java.lang.String password) {
        instanceProperties.setProperty(InstanceProperties.PASSWORD_ATTR, password);
        
    }
    
    
    /**
     * Getter for property UserName. can be null for a disconnected DM.
     * @return Value of property UserName.
     */
    public java.lang.String getUserName() {
        if (instanceProperties==null)
            return null;
        return instanceProperties.getProperty(InstanceProperties.USERNAME_ATTR) ;
    }
    
    /**
     * Setter for property UserName.
     * @param UserName New value of property UserName.
     */
    public void setUserName(java.lang.String UserName) {
        instanceProperties.setProperty(InstanceProperties.USERNAME_ATTR, UserName);
        
    }
    /**
     * Ask the server instance to reset cached deployment manager, J2EE
     * management objects and refresh it UI elements.
     */
    public  void refreshServerInstance(){
        instanceProperties.refreshServerInstance();
    }
    
    public String getPort() {
        if (instanceProperties==null)
            return "8880";
        return instanceProperties.getProperty(PORT_ATTR) ;
    }
    
    public void setPort(String port) {
        if(port.trim().matches("[0-9]+"))
        {
        instanceProperties.setProperty(PORT_ATTR, port);
        WSDM.setPort(port);
        }
    }
    
    public String getDisplayName() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    public String getUrl() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.URL_ATTR);
    }
    
    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }
    
    public String getServerRoot() {
        return WSDM.getServerRoot();
    }
    
    public void setServerRoot(String serverRoot) {
        WSDM.setServerRoot(serverRoot);
    }
    public String getDomainRoot() {
        return WSDM.getDomainRoot();
    }
    public void setDomainRoot(String domainRoot) {
        WSDM.setDomainRoot(domainRoot);
    }
    public void setHost(String host) {
        WSDM.setHost(host);
    }
    public void setServerName(String name) {
        WSDM.setServerName(name);
    }
    public void setConfigXmlPath(String path) {
        WSDM.setConfigXmlPath(path);
    }
    public void setIsLocal(String isLocal) {
        WSDM.setIsLocal(isLocal);
    }
    public String getIsLocal() {
        return WSDM.getIsLocal();
    }
    
    
}
