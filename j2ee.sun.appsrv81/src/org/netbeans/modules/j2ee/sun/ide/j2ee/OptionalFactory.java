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
/*
 * OptionalFactory.java
 *
 * Created on January 12, 2004, 4:15 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.openide.WizardDescriptor;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDatasourceManager;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.netbeans.modules.j2ee.sun.ide.dm.SunMessageDestinationDeployment;
import org.netbeans.modules.j2ee.sun.ide.j2ee.jsps.FindJSPServletImpl;
import org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy.DirectoryDeploymentFacade;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.TargetServerData;
//import org.netbeans.modules.tomcat5.AntDeploymentProviderImpl;

/**
 *
 * @author  ludo
 */


public  class OptionalFactory extends OptionalDeploymentManagerFactory {
    
    /** Creates a new instance of OptionalFactory */
    public OptionalFactory () {
    }
    
    public FindJSPServlet getFindJSPServlet (DeploymentManager dm) {
        return new FindJSPServletImpl (dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment (DeploymentManager dm) {
        return new DirectoryDeploymentFacade (dm);
    }
    
    public StartServer getStartServer (DeploymentManager dm) {
        return StartSunServer.get(dm);
    }
    
    /** Create AutoUndeploySupport for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        WizardDescriptor.InstantiatingIterator retVal = 
                new AddDomainWizardIterator();
        return retVal;
    }
    
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return new AntDeploymentProviderImpl(dm);
    }

    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        if (!(dm instanceof SunDeploymentManager))
            throw new IllegalArgumentException("");

        SunDatasourceManager dsMgr = new SunDatasourceManager(dm);
        return dsMgr;
    }
    
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        if (!(dm instanceof SunDeploymentManager))
            throw new IllegalArgumentException("");
        SunMessageDestinationDeployment destMgr = new SunMessageDestinationDeployment(dm);
        return destMgr;
    }
    
}
