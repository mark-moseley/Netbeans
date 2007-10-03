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
package org.netbeans.modules.j2ee.websphere6;

import java.io.*;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.spi.exceptions.*;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.websphere6.util.WSDebug;

/**
 * The main entry point to the plugin. Keeps the required static data for the
 * plugin and returns the DeploymentManagers required for deployment and
 * configuration. Does not directly perform any interaction with the server.
 *
 * @author Kirill Sorokin
 */
public class WSDeploymentFactory implements DeploymentFactory {
    
    // additional properties that are stored in the InstancePropeties object
    public static final String SERVER_ROOT_ATTR = "serverRoot";        // NOI18N
    public static final String DOMAIN_ROOT_ATTR = "domainRoot";        // NOI18N
    public static final String IS_LOCAL_ATTR = "isLocal";              // NOI18N
    public static final String HOST_ATTR = "host";                     // NOI18N
    public static final String PORT_ATTR = "port";                     // NOI18N
    public static final String DEBUGGER_PORT_ATTR = "debuggerPort";    // NOI18N
    public static final String SERVER_NAME_ATTR = "serverName";        // NOI18N
    public static final String CONFIG_XML_PATH = "configXmlPath";      // NOI18N
    public static final String ADMIN_PORT_ATTR = "adminPort";          // NOI18N
    
    public static final String USERNAME_ATTR = "username";
    public static final String PASSWORD_ATTR = "password";
    public static final String DEFAULT_HOST_PORT_ATTR="defaultHostPort";
    
    /**
     * The singleton instance of the factory
     */
    private static WSDeploymentFactory instance;
    
    /**
     * The singleton factory method
     * 
     * @return the singleton instance of the factory
     */
    public static synchronized DeploymentFactory create() {
        // if the instance is not initialized yet - create it
        if (instance == null) {
            instance = new WSDeploymentFactory();
        }
        
        // return the instance
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // DeploymentFactory implementation
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a wrapper for the connected deployment manager
     * 
     * @return a connected DeploymentManager implementation
     */
    public DeploymentManager getDeploymentManager(String uri, String username, 
            String password) throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDeploymentManager(" + uri + ", " +      // NOI18N
                    username + ", " + password + ")");                 // NOI18N
        
        
        // return a new deployment manager
        return new WSDeploymentManager(uri, username, password);
    }
    
    /**
     * Returns a wrapper for the disconnecter deployment manager
     * 
     * @return a disconnected DeploymentManager implementation
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
            throws DeploymentManagerCreationException {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisconnectedDeploymentManager(" + uri + // NOI18N
                    ")");                                              // NOI18N
      
        // return a new deployment manager
        return new WSDeploymentManager(uri);
    }
    
    /**
     * Tells whether this deployment factory is capable to handle the server
     * identified by the given URI
     * 
     * @param uri the server URI
     * @return can or cannot handle the URI
     */
    public boolean handlesURI(String uri) {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("handlesURI(" + uri + ")");                 // NOI18N
        
        //return uri == null ? false : uri.startsWith(
        return uri == null ? false : (uri.indexOf(WSURIManager.WSURI)>-1);                                // NOI18N
    }
    
    /**
     * Returns the product version of the deployment factory
     * 
     * @return the product version
     */
    public String getProductVersion() {
        return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_productVersion");                                 // NOI18N
    }
    
    /**
     * Returns the deployment factory dysplay name
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (WSDebug.isEnabled()) // debug output
            WSDebug.notify("getDisplayName()");                        // NOI18N
        
        return NbBundle.getMessage(WSDeploymentFactory.class, 
                "TXT_displayName");                                    // NOI18N
    }
}