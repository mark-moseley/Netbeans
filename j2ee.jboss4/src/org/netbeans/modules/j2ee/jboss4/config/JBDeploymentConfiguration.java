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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.j2ee.dd.api.common.ComponentInterface;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.ds.DatasourceSupport;
import org.netbeans.modules.j2ee.jboss4.config.mdb.MessageDestinationSupport;
import org.openide.loaders.DataObject;

/** 
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author  Pavel Buzek, Libor Kotouc
 */
public abstract class JBDeploymentConfiguration 
        implements DatasourceConfiguration, MessageDestinationConfiguration, EjbResourceConfiguration {

    // TODO move to a more appropriate class as soon as E-mail resource API is introduced
    protected static final String MAIL_SERVICE_JNDI_NAME_JB4 = "java:Mail"; // NOI18N

    //JSR-88 deployable object - initialized when instance is constructed
    protected J2eeModule j2eeModule;
    
    //cached data object for the server-specific configuration file (initialized by the subclasses)
    protected DataObject deploymentDescriptorDO;
    
    //the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

     //support for data sources
    private DatasourceSupport dsSupport;

    //support for message destination resources
    private MessageDestinationSupport destSupport;
    
    /** Creates a new instance of JBDeploymentConfiguration */
    public JBDeploymentConfiguration (J2eeModule j2eeModule) {
        this.j2eeModule = j2eeModule;
        this.resourceDir = j2eeModule.getResourceDirectory();
    }
            
// -------------------------------------- ModuleConfiguration  -----------------------------------------
    
    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }
    
// -------------------------------------- DatasourceConfiguration  -----------------------------------------

    private DatasourceSupport getDatasourceSupport() {
        if (dsSupport == null) {
            dsSupport = new DatasourceSupport(resourceDir);
        }
        return dsSupport;
    }
   
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return getDatasourceSupport().getDatasources();
    }

    public Datasource createDatasource(String jndiName, String url,
            String username, String password, String driver) 
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        
        return getDatasourceSupport().createDatasource(jndiName, url, username, password, driver);
    }

    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {}
    
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
            String referenceName, String jndiName) throws ConfigurationException {}

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return null;
    }
    
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return null;
    }
    
// -------------------------------------- MessageDestinationConfiguration  -----------------------------------------

    private MessageDestinationSupport getMessageDestinationsSupport() {
        if (destSupport == null) {
            destSupport = new MessageDestinationSupport(resourceDir);
        }
        return destSupport;
    }
   
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return getMessageDestinationsSupport().getMessageDestinations();
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException {
        return getMessageDestinationsSupport().createMessageDestination(name, type);
    }
    
    public void bindMdbToMessageDestination(String mdbName, String name, 
            MessageDestination.Type type) throws ConfigurationException {}

    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return null;
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException {}

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {}
    
// -------------------------------------- EjbResourceConfiguration  -----------------------------------------
    
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return null;
    }
    
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {}

    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {}
    
}
