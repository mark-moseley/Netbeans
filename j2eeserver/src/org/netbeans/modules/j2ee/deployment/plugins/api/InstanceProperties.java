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

/*
 * InstanceProperties.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.InstancePropertiesImpl;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.*;


/**
 *  A way to ask the IDE to store customized information about a server instance
 *  and make it available to a plugin.
 *
 *  Typical usage for create new instance would be like this:
 *      InstanceProperties props = InstanceProperties.getInstanceProperties(url);
 *      if (props == null)
 *          props = InstanceProperties.createInstanceProperties(url, user, password, 
 *                          displayName);
 *      props.setProperty(prop1, value1);
 *      . . .
 *
 * @author George FinKlang
 * @author nn136682
 * @version 0.1
 */
public abstract class InstanceProperties {

    /**
     * URL property, its value is used as a connection string to get the deployment 
     * manager (e.g. "tomcat:home=jakarta-tomcat-5.0.27:base=jakarta-tomcat-5.0.27_base"
     * for Tomcat).
     */
    public static final String URL_ATTR = "url"; //NOI18N

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
     * Remove forbidden property, if its value is set to <code>true</code>, it 
     * won't be allowed to remove the server instance from the server registry.
     */
    public static final String REMOVE_FORBIDDEN = "removeForbidden"; //NOI18N
    
    /**
     * HTTP port property, The port where the instance runs
     */
    public static final String HTTP_PORT_NUMBER = "httpportnumber";
    
      /**
     * Startup timeout property, The number of seconds to allow before assuming 
     *   that a request to start an instance has failed
     * 
     *  @since 1.22
     */
    public static final String STARTUP_TIMEOUT = "startupTimeout";

    /**
     * Shutdown timeout property, The number of seconds to allow before assuming 
     *   that a request to stop an instance has failed
     * 
     *  @since 1.22
     */
    public static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";

    /**
     * Deployment timeout property, The number of seconds to allow before 
     *   assuming that a request to deploy a project to an instance has failed
     * 
     *  @since 1.22
     */
    public static final String DEPLOYMENT_TIMEOUT = "deploymentTimeout";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Returns instance properties for the server instance.
     *
     * @param url the url connection string to get the instance deployment manager.
     * @return the InstanceProperties object, null if instance does not exists.
     */
    public static InstanceProperties getInstanceProperties(String url) {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(url);
        if (inst == null)
            return null;
        return inst.getInstanceProperties();
    }
    
    /**
     * Create new instance and returns instance properties for the server instance.
     * 
     * @param url the url connection string to get the instance deployment manager
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if 
     *         instance does not exists
     * @exception InstanceCreationException when instance with same url already 
     *            registered.
     *
     * @deprecated use the factory method with displayName parameter.
     */    
    public static InstanceProperties createInstanceProperties(
            String url, String username, String password) throws InstanceCreationException {
        return createInstanceProperties(url, username, password, null);
    }

    /**
     * Create new instance and returns instance properties for the server instance.
     * 
     * @param url the url connection string to get the instance deployment manager.
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @param displayName display name which is used by IDE to represent this 
     *        server instance.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if 
     *         instance does not exists.
     * @exception InstanceCreationException when instance with same url already 
     *            registered.
     */
    public static InstanceProperties createInstanceProperties(String url, String username, 
            String password, String displayName) throws InstanceCreationException {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(url, username, password, displayName);
        ServerInstance inst = registry.getServerInstance(url);
        InstanceProperties ip = inst.getInstanceProperties();
        return ip;
    }
    
    /**
     * Returns list of URL strings of all registered instances
     * @return array of URL strings
     */
    public static String[] getInstanceList() {
        return ServerRegistry.getInstance().getInstanceURLs();
    }

    /**
     * Return default instance properties.
     */
    public static InstanceProperties getDefaultInstance() {
        return new InstancePropertiesImpl(ServerRegistry.getInstance().getDefaultInstance().getServerInstance());
    }
    
    /**
     * Set instance properties.
     * @param props properties to set for this server instance.
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperties(java.util.Properties props) throws IllegalStateException;

    /**
     * Set instance property
     * @param propname name of property
     * @param value property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperty(String propname, String value) throws IllegalStateException;
    
    /**
     * Get instance property
     * @param propname name of property
     * @return property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract String getProperty(String propname) throws IllegalStateException;
    
    /**
     * Get instance property keys
     * @return property key enunmeration
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract java.util.Enumeration propertyNames() throws IllegalStateException;
    
    /**
     * Is the target server the default J2EE server for deployment?
     * @return true if the target server or admin server is the default.
     */
    public abstract boolean isDefaultInstance();
    
    /**
     * Return DeploymentManager associated with this instance.
     */
    public abstract DeploymentManager getDeploymentManager();
    
    /**
     * Return default Target object for the target server from this instance, if any.
     */
    public abstract Target getDefaultTarget();
    
    /**
     * Set the target server the default server.
     * @param targetName name of the target server; null if admin server is also single target.
     */
    public abstract void setAsDefaultServer(String targetName);
    
    /**
     * Ask the server instance to reset cached deployment manager, J2EE
     * management objects and refresh it UI elements.
     */
    public abstract void refreshServerInstance();
    
    /**
     * Add <code>PropertyChangeListener</code> which will be notified of 
     * <code>InstanceProperties</code> changes.
     * 
     * @param <code>PropertyChangeListener</code> which will be notified of 
     *        <code>InstanceProperties</code> changes.
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * This method should be called to notify interested listeners when 
     * InstanceProperties change.
     *
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */
    protected void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }
}
