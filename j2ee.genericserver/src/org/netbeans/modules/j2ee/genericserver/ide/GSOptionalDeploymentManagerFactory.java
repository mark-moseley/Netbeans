/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.genericserver.ide;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.genericserver.ide.GSInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Martin Adamek
 */
public class GSOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    // TODO: this is just temporary, to not show this instance in Registry
    // needs maybe option similar to is_it_bundled_tomcat to define visibility
    // current solution is only for EJB Freeform
    public StartServer getStartServer(DeploymentManager dm) {
        return null;//new GSStartServer();
    }

    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return null;
    }

    // TODO: if returned value is null then this server in not displayed 
    // in Add server instance dialog. InstantiatingIterator should be returned
    // when whole functionality will be implemented. Current solution is only for 
    // EJB freeform project
    public InstantiatingIterator getAddInstanceIterator() {
        return null;//new GSInstantiatingIterator();
    }
    
    
}
