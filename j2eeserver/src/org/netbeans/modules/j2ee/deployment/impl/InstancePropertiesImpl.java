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
 * InstancePropertiesImpl.java
 *
 * Created on December 4, 2003, 6:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  nn136682
 */
public class InstancePropertiesImpl extends InstanceProperties implements ServerRegistry.InstanceListener {
    private final String url;
    private transient FileObject fo;
    
    /** Creates a new instance of InstancePropertiesImpl */
    public InstancePropertiesImpl(ServerInstance instance) {
        this.url = instance.getUrl();
        
    }
    
    private FileObject getFO() {
        if (fo == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(url);
            if (instance == null) 
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            fo = ServerRegistry.getInstance().getInstanceFileObject(url);
            if (fo == null)
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            
        }
        return fo;
    }
    
    // ServerRegistry.InstanceListener methods
    public void instanceRemoved(ServerString instance) {
        if (instance != null && url.equals(instance.getUrl()))
            fo = null;
    }
    public void instanceAdded(ServerString instance) {}
    public void changeDefaultInstance(ServerString instance){}
    
    public String getProperty(String propname) throws IllegalStateException {
        return getFO().getAttribute(propname).toString();
    }

    public java.util.Enumeration propertyNames() throws IllegalStateException {
        return getFO().getAttributes();
    }
    
    public void setProperty(String propname, String value) throws IllegalStateException {
        try {
            getFO().setAttribute(propname, value);
        } catch (java.io.IOException ioe) {
            throw (IllegalStateException) org.openide.ErrorManager.getDefault().annotate(
            new IllegalStateException(NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url)),ioe);
        }
    }
    
}
