/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/** Factory capable to create DeploymentManager that can deploy to
 * Tomcat 5.
 *
 * Tomcat URI has following format:
 * <PRE><CODE>tomcat:[home=&lt;home_path&gt;:[base=&lt;base_path&gt;:]]&lt;manager_app_url&gt;</CODE></PRE>
 * for example
 * <PRE><CODE>tomcat:http://localhost:8080/manager/</CODE></PRE>
 * where paths values will be used as CATALINA_HOME and CATALINA_BASE properties and manager_app_url
 * denotes URL of manager application configured on this server and has to start with <CODE>http:</CODE>.
 * @author Radim Kubacki
 */
public class TomcatFactory implements DeploymentFactory {
    
    private static final String tomcatUriPrefix = "tomcat:"; // NOI18N
    
    private static TomcatFactory instance;
    
    private static ErrorManager err = ErrorManager.getDefault ().getInstance ("org.netbeans.modules.tomcat5");  // NOI18N
    
    /** Factory method to create DeploymentFactory for Tomcat.
     */
    public static synchronized TomcatFactory create() {
        if (instance == null) {
            if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("Creating TomcatFactory"); // NOI18N
            instance = new TomcatFactory ();
            javax.enterprise.deploy.shared.factories.DeploymentFactoryManager
                .getInstance ().registerDeploymentFactory (instance);
        }
        return instance;
    }
    
    /** Get the {@link org.openide.ErrorManager} that logs module events.
     * @return Module specific ErrorManager.
     */
    public static ErrorManager getEM () {
        return err;
    }
    
    /** Creates a new instance of TomcatFactory */
    public TomcatFactory() {
    }
    
    /** Factory method to create DeploymentManager.
     * @param uri URL of configured manager application.
     * @param uname user with granted manager role
     * @param passwd user's password
     * @throws DeploymentManagerCreationException
     * @return {@link TomcatManager}
     */    
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) 
    throws DeploymentManagerCreationException {
        if (!handlesURI (uri)) {
            throw new DeploymentManagerCreationException ("Invalid URI");
        }
        return new TomcatManager (true, uri.substring (tomcatUriPrefix.length ()), uname, passwd);
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
    throws DeploymentManagerCreationException {
        if (!handlesURI (uri)) {
            throw new DeploymentManagerCreationException ("Invalid URI");
        }
        // PENDING parse to get home and base dirs
        
        return new TomcatManager (false, uri.substring (tomcatUriPrefix.length ()), null, null);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage (TomcatFactory.class, "LBL_TomcatFactory");
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage (TomcatFactory.class, "LBL_TomcatFactoryVersion");
    }
    
    /**
     * @param str
     * @return <CODE>true</CODE> for URIs beggining with <CODE>tomcat:</CODE> prefix
     */    
    public boolean handlesURI(String str) {
        return str != null && str.startsWith (tomcatUriPrefix) && str.indexOf ("http:", tomcatUriPrefix.length ()) >= 0;
    }
    
}
