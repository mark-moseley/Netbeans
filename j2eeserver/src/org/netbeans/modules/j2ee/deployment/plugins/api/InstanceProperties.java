/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigBeanCustomizer.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.InstancePropertiesImpl;

/**
 *  A way for the IDE to store customized information about a server instance
 *  and make it available to a plugin.
 *
 * @author George FinKlang
 * @version 0.1
 */

public abstract class InstanceProperties {

    public static final String USERNAME_ATTR = "username"; //NOI18N
    public static final String PASSWORD_ATTR = "password"; //NOI18N

    /**
     * Returns instance properties for the server instance
     * @param url the url connection string to get the instance deployment manager
     * @return the InstanceProperties object, null if instance does not exists
     */
    public static InstanceProperties getInstanceProperties(String url) {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(url);
        if (inst == null)
            return null;
        return new InstancePropertiesImpl(inst);
    }

    /**
     * Set instance property
     * @propname name of property
     * @value property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperty(String propname, String value) throws IllegalStateException;
    
    /**
     * Get instance property
     * @propname name of property
     * @return property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract String getProperty(String propname) throws IllegalStateException;
    
    /**
     * Get instance property keys
     * @propname name of property
     * @return property key enunmeration
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract java.util.Enumeration propertyNames() throws IllegalStateException;

}
