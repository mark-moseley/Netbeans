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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.config.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.DefaultSourceMap;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport;
import org.openide.filesystems.FileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/** This object must be implemented by J2EE module support and an instance 
 * added into project lookup.
 * 
 * @author  Pavel Buzek
 */
public abstract class J2eeModuleProvider {
    
    private ConfigSupportImpl configSupportImpl;
    final List listeners = new ArrayList();
    private ConfigFilesListener configFilesListener = null;
    
    public J2eeModuleProvider () {
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();
    
    public final ConfigSupport getConfigSupport () {
        ConfigSupportImpl confSupp;
        synchronized (this) {
            confSupp = configSupportImpl;
        }
        if (confSupp == null) {
            confSupp = new ConfigSupportImpl(this);
            synchronized (this) {
                configSupportImpl = confSupp;
            }
        }
	return confSupp;
    }
    
    // Do not remove this method! It is a helper for the Maven support project to 
    // workaround the issue #109507. Please keep in mind that this is a hack, so
    // keep it private! No one else should use it.
    private synchronized void resetConfigSupport() {
        configSupportImpl = null;
    }
    
    /**
     * Return server debug info.
     * Note: if server is not running and needs to be up for retrieving debug info, 
     * this call will return null.  This call is also used by UI so it should not 
     * try to ping or start the server.
     */
    public final ServerDebugInfo getServerDebugInfo () {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        StartServer ss = si.getStartServer();
        if (ss == null) {
            return null;
        }
        // AS8.1 needs to have server running to get accurate debug info, and also need a non-null target 
        // But getting targets from AS8.1 require start server which would hang UI, so avoid start server
        // Note: for debug info after deploy, server should already start.
        if (! si.isRunningLastCheck() && ss.needsStartForTargetList()) {
            if (ss.isAlsoTargetServer(null)) {
                return ss.getDebugInfo(null);
            } else {
                return null;
            }
        }
        
        Target target = null;
        if (si != null) {
            ServerTarget[] sts = si.getTargets();
            for (int i=0; i<sts.length; i++) {
                if (si.getStartServer().isAlsoTargetServer(sts[i].getTarget())) {
                    target = sts[i].getTarget();
                }
            }
            if (target == null && sts.length > 0) {
                target = sts[0].getTarget();
            }
            return si.getStartServer().getDebugInfo(target);
        }
        return null;
    }
    
    /**
     * Gets the data sources deployed on the target server instance.
     *
     * @return set of data sources
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     * 
     * @since 1.15 
     */
    public Set<Datasource> getServerDatasources() throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        Set<Datasource> deployedDS = Collections.<Datasource>emptySet();
        if (si != null) {
            deployedDS = si.getDatasources();
        }
        else {
            Logger.getLogger("global").log(Level.WARNING, "The server data sources cannot be retrieved because the server instance cannot be found.");
        }
        return deployedDS;
    }
    
    /**
     * Gets the data sources saved in the module.
     *
     * @return set of data sources
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     * @since 1.15 
     */
    public Set<Datasource> getModuleDatasources() throws ConfigurationException {
        Set<Datasource> projectDS = getConfigSupport().getDatasources();
        return projectDS;
    }

    /**
     * Tests whether data source creation is supported.
     *
     * @return true if data source creation is supported, false otherwise.
     *
     * @since 1.15 
     */
    public boolean isDatasourceCreationSupported() {
        return getConfigSupport().isDatasourceCreationSupported();
    }
    
    
    /**
     * Creates and saves data source in the module if it does not exist yet on the target server or in the module.
     * Data source is considered to be existing when JNDI name of the found data source and the one
     * just created equal.
     *
     * @param jndiName name of data source
     * @param url database URL
     * @param username database user
     * @param password user's password
     * @param driver fully qualified name of database driver class
     * @return created data source
     * @exception DatasourceAlreadyExistsException if conflicting data source is found
     *
     * @since 1.15 
     */
    public final Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws DatasourceAlreadyExistsException, ConfigurationException {

        //check whether the ds is not already on the server
        Set<Datasource> deployedDS = getServerDatasources();
        if (deployedDS != null) {
            for (Iterator it = deployedDS.iterator(); it.hasNext();) {
                Datasource ds = (Datasource) it.next();
                if (jndiName.equals(ds.getJndiName())) // ds with the same JNDI name already exists on the server, do not create new one
                    throw new DatasourceAlreadyExistsException(ds);
            }
        }
        
        Datasource ds = null;
        try {
            //btw, ds existence in a project is verified directly in the deployment configuration
            ds = getConfigSupport().createDatasource(jndiName, url, username, password, driver);
        } catch (UnsupportedOperationException oue) {
            Logger.getLogger("global").log(Level.INFO, null, oue);
        }
        
        return ds;
    }
    
    /**
     * Deploys data sources saved in the module.
     *
     * @exception ConfigurationException if there is some problem with data source configuration
     * @exception DatasourceAlreadyExistsException if module data source(s) are conflicting
     * with data source(s) already deployed on the server
     *
     * @since 1.15 
     */
    public void deployDatasources() throws ConfigurationException, DatasourceAlreadyExistsException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDS = getModuleDatasources();
            si.deployDatasources(moduleDS);
        }
        else {
            Logger.getLogger("global").log(Level.WARNING,
                                           "The data sources cannot be deployed because the server instance cannot be found.");
        }
    }
    
    
    /**
     * Configuration support to allow development module code to access well-known 
     * configuration propeties, such as web context root, cmp mapping info...
     * The setters and getters work with server specific data on the server returned by
     * {@link getServerID} method.
     */
    public static interface ConfigSupport {
        /**
         * Create an initial fresh configuration for the current module.  Do nothing if configuration already exists.
         * @return true if there is no existing configuration, false if there is exsisting configuration.
         */
        public boolean createInitialConfiguration();
        /**
         * Ensure configuration is ready to respond to any editing to the module.
         * @return true if the configuration is ready, else false.
         */
        public boolean ensureConfigurationReady();

        /**
         * Set web module context root.
         * 
         * @param contextRoot web module context root. 
         * @throws ConfigurationException reports errors in setting the web context
         *         root.
         */
        public void setWebContextRoot(String contextRoot) throws ConfigurationException;
        
        /**
         * Get web module context root.
         * 
         * @return web module context root.
         * 
         * @throws ConfigurationException reports errors in setting the web context
         *         root.
         */
        public String getWebContextRoot() throws ConfigurationException;
        
        /**
         * Return a list of file names for current server specific deployment 
         * descriptor used in this module.
         */
        public String [] getDeploymentConfigurationFileNames();
        /**
         * Return relative path within the archive or distribution content for the
         * given server specific deployment descriptor file.
         * @param deploymentConfigurationFileName server specific descriptor file name
         * @return relative path inside distribution content.
         */
        public String getContentRelativePath(String deploymentConfigurationFileName);
        /**
         * Push the CMP and CMR mapping info to the server configuraion.
         * This call is typically used by CMP mapping wizard.
         * 
         * @throws ConfigurationException reports errors in setting the CMP mapping.
         */
        public void setCMPMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException;
        
        /**
         * Sets the resource for the specified CMP bean. Some containers may not 
         * support fine-grained per bean resource definition, in which case global 
         * EJB module CMP resource is set.
         *
         * @param ejbName   name of the CMP bean.
         * @param jndiName  the JNDI name of the resource.
         * 
         * @throws ConfigurationException reports errors in setting the CMP resource.
         * @throws NullPointerException if any of the parameters is <code>null</code>.
         * 
         * @since 1.30
         */
        void setCMPResource(String ejbName, String jndiName) throws ConfigurationException;
        
        /**
         * Tests whether data source creation is supported.
         *
         * @return true if data source creation is supported, false otherwise.
         *
         * @since 1.15 
         */
        public boolean isDatasourceCreationSupported();
                
        /**
         * Gets the data sources saved in the module.
         *
         * @return set of data sources
         *
         * @throws ConfigurationException reports errors in retrieving the data sources.
         * 
         * @since 1.15 
         * 
         */
        public Set<Datasource> getDatasources() throws ConfigurationException;
        
        /**
         * Creates and saves data source in the module if it does not exist yet in the module.
         * Data source is considered to be existing when JNDI name of the found data source and the one
         * just created equal.
         *
         * @param jndiName name of data source
         * @param url database URL
         * @param username database user
         * @param password user's password
         * @param driver fully qualified name of database driver class
         * 
         * @return created data source
         * 
         * @throws UnsupportedOperationException if operation is not supported
         * @throws DatasourceAlreadyExistsException if conflicting data source is found
         * @throws ConfigurationException reports errors in creating the data source.
         *
         * @since 1.15 
         */
        public Datasource createDatasource(String jndiName, String  url, String username, String password, String driver)
        throws UnsupportedOperationException, DatasourceAlreadyExistsException, ConfigurationException;
        
        /**
         * Binds the data source reference name with the corresponding data source which is
         * identified by the given JNDI name.
         * 
         * @param referenceName name used to identify the data source
         * @param jndiName JNDI name of the data source
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException;

        /**
         * Binds the data source reference name with the corresponding data source which is
         * identified by the given JNDI name. The reference is used within the scope of the EJB.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName name used to identify the data source
         * @param jndiName JNDI name of the data source

         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.25
         */
        public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
                String referenceName, String jndiName) throws ConfigurationException;
        
        /**
         * Finds JNDI name of data source which is mapped to the given reference name of a data source
         * 
         * @param referenceName reference name of data source
         * @return JNDI name which is mapped to the given JNDI name
         * 
         * @throws NullPointerException if reference name is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public String findDatasourceJndiName(String referenceName) throws ConfigurationException;
        
        /**
         * Finds JNDI name of data source which is mapped to the given reference name in the scope of the EJB.
         * 
         * @param ejbName EJB name
         * @param referenceName reference name of data source
         * @return data source if it exists, null otherwise
         *
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException;
        
        /**
         * Finds data source with the given JNDI name.
         * 
         * @param jndiName JNDI name of a data source
         * @param return data source if it exists, null otherwise
         *
         * @throws NullPointerException if JNDI name is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public Datasource findDatasource(String jndiName) throws ConfigurationException;

        /**
         * Retrieves message destinations stored in the module.
         * 
         * @return set of message destinations
         * 
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public Set<MessageDestination> getMessageDestinations() throws ConfigurationException;

        /**
         * Retrieves message destinations configured on the target server instance.
         *
         * @return set of message destinations
         * 
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25 
         */
        public Set<MessageDestination> getServerMessageDestinations() throws ConfigurationException;
        
        /**
         * Tests whether a message destination creation is supported.
         *
         * @return true if message destination creation is supported, false otherwise.
         *
         * @since 1.25
         */
        public boolean supportsCreateMessageDestination();

        /**
         * Creates and saves a message destination in the module if it does not exist in the module yet.
         * Message destinations are considered to be equal if their JNDI names are equal.
         *
         * @param name name of the message destination
         * @param type message destination type
         * @return created message destination
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws UnsupportedOperationException if this opearation is not supported
         * @throws ConfigurationException if there is some problem with message destination configuration
         *
         * @since 1.25 
         */
        public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
        throws UnsupportedOperationException, ConfigurationException;
        
        /**
         * Binds the message destination name with message-driven bean.
         * 
         * @param mdbName MDB name
         * @param name name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException;

        /**
         * Finds name of message destination which the given MDB listens to
         * 
         * @param mdbName MDB name
         * @return message destination name
         * 
         * @throws NullPointerException if MDB name is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public String findMessageDestinationName(String mdbName) throws ConfigurationException;

        /**
         * Finds message destination with the given name.
         * 
         * @param name message destination name
         * @param return message destination if it exists, null otherwise
         *
         * @throws NullPointerException if name is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public MessageDestination findMessageDestination(String name) throws ConfigurationException;

        /**
         * Binds the message destination reference name with the corresponding message destination which is
         * identified by the given name.
         * 
         * @param referenceName reference name used to identify the message destination
         * @param connectionFactoryName connection factory name
         * @param destName name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
                String destName, MessageDestination.Type type) throws ConfigurationException;

        /**
         * Binds the message destination reference name with the corresponding message destination which is
         * identified by the given name. The reference is used within the EJB scope.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName reference name used to identify the message destination
         * @param connectionFactoryName connection factory name
         * @param destName name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.25
         */
        public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
                String referenceName, String connectionFactoryName,
                String destName, MessageDestination.Type type) throws ConfigurationException;


        /**
         * Returns a JNDI name for the given EJB or <code>null</code> if the EJB has 
         * no JNDI name assigned.
         *
         * @param  ejbName EJB name
         * 
         * @return JNDI name bound to the EJB or <code>null</code> if the EJB has no 
         *         JNDI name assigned.
         * 
         * @throws ConfigurationException if there is some problem with EJB configuration.
         * 
         * @since 1.33
         */
         public String findJndiNameForEjb(String ejbName) throws ConfigurationException;

        /**
         * Binds EJB reference name with EJB name.
         * 
         * @param referenceName name used to identify the EJB
         * @param jndiName JNDI name of the referenced EJB
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with EJB configuration
         * 
         * @since 1.26
         */
        public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException;

        /**
         * Binds EJB reference name with EJB name within the EJB scope.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName name used to identify the referenced EJB
         * @param jndiName JNDI name of the referenced EJB
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with EJB configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.26
         */
        public void bindEjbReferenceForEjb(String ejbName, String ejbType,
                String referenceName, String jndiName) throws ConfigurationException;
    }

    /**
     *  Returns list of root directories for source files including configuration files.
     *  Examples: file objects for src/java, src/conf.  
     *  Note: 
     *  If there is a standard configuration root, it should be the first one in
     *  the returned list.
     */
    public FileObject[] getSourceRoots() {
        return new FileObject[0];
    }
    
    /**
     * Return destination path-to-source file mappings.
     * Default returns config file mapping with straight mapping from the configuration
     * directory to distribution directory.
     */
    public SourceFileMap getSourceFileMap() {
        return new DefaultSourceMap(this);
    }
    
    /** If the module wants to specify a target server instance for deployment 
     * it needs to override this method to return false. 
     */
    public boolean useDefaultServer () {
        return true;
    }
    
    /**
     * Set ID of the server instance that will be used for deployment.
     * 
     * @param severInstanceID server instance ID.
     * @since 1.6
     */
    public abstract void setServerInstanceID(String severInstanceID);
    
    /** Id of server isntance for deployment. The default implementation returns
     * the default server instance selected in Server Registry. 
     * The return value may not be null.
     * If modules override this method they also need to override {@link useDefaultServer}.
     */
    public String getServerInstanceID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getUrl ();
    }
    
    /**
     * Return InstanceProperties of the server instance
     **/
    public InstanceProperties getInstanceProperties(){
        return InstanceProperties.getInstanceProperties(getServerInstanceID());
    }

    /** This method is used to determin type of target server.
     * The return value must correspond to value returned from {@link getServerInstanceID}.
     */
    public String getServerID () {
        return ServerRegistry.getInstance ().getDefaultInstance ().getServer ().getShortName ();
    }
    
    /**
     * Return name to be used in deployment of the module.
     */
    public String getDeploymentName() {
        return getConfigSupportImpl().getDeploymentName();
    }

    /**
     * Returns true if the current target platform provide verifier support for this module.
     */
    public boolean hasVerifierSupport() {
        String serverId = getServerID();
        if (serverId != null) {
            Server server = ServerRegistry.getInstance().getServer(serverId);
            if (server != null) {
                return server.canVerify(getJ2eeModule().getModuleType());
            }
        }
        return false;
    }
    
    /**
     * Invoke verifier from current platform on the provided target file.
     * @param target File to run verifier against.
     * @param logger output stream to write verification resutl to.
     * @return true
     */
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        VerifierSupport verifier = ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport();
        if (verifier == null) {
            throw new ValidationException ("Verification not supported by the selected server");
        }
        Object type = getJ2eeModule().getModuleType();
        if (!verifier.supportsModuleType(type)) {
            throw new ValidationException ("Verification not supported for module type " + type);
        }
        ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport().verify(target, logger);
    }

    // TODO project should handle this
    protected final void fireServerChange (String oldServerID, String newServerID) {
        Server oldServer = ServerRegistry.getInstance().getServer(oldServerID);
        Server newServer = ServerRegistry.getInstance().getServer(newServerID);

        // corresponds to the "resolve missing server" or "new project"
        if (oldServer == null && newServer != null) {
            ConfigSupportImpl oldConSupp;
            synchronized (this) {
                oldConSupp = configSupportImpl;
                configSupportImpl = null;
            }

            if (oldConSupp != null) {
                /**
                 * Only if we are resolving the missing server we create the
                 * configuration. In fact this shouldn't hurt anything if we
                 * did it always, but some plugins print some annoying messages.
                 * However oldConSupp not null condition could be fragile.
                 */
                getConfigSupportImpl().ensureConfigurationReady();
                oldConSupp.dispose();
            }
            return;
        }

        // corresponds to switching from one server to another, both existing
        if (oldServer != null && newServer != null && !newServer.equals(oldServer)) {

            if (J2eeModule.WAR.equals(getJ2eeModule().getModuleType())) {
                String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                ConfigSupportImpl oldConSupp;
                synchronized (this) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();

                if (oldCtxPath == null || oldCtxPath.equals("")) { //NOI18N
                    oldCtxPath = getDeploymentName().replace(' ', '_'); //NOI18N
                    char c [] = oldCtxPath.toCharArray();
                    for (int i = 0; i < c.length; i++) {
                        if (!Character.UnicodeBlock.BASIC_LATIN.equals(Character.UnicodeBlock.of(c[i])) ||
                                !Character.isLetterOrDigit(c[i])) {
                            c[i] = '_';
                        }
                    }
                    oldCtxPath = "/" + new String (c); //NOI18N
                }
                getConfigSupportImpl().setWebContextRoot(oldCtxPath);

                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            } else {
                ConfigSupportImpl oldConSupp;
                synchronized (this) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();
                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            }
        }
    }

    /**
     * Returns all configuration files known to this J2EE Module.
     */
    public final FileObject[] getConfigurationFiles() {
        return getConfigurationFiles(false);
    }

    public final FileObject[] getConfigurationFiles(boolean refresh) {
        if (refresh) {
            configFilesListener.stopListening();
            configFilesListener = null;
        }
        addCFL();
        return ConfigSupportImpl.getConfigurationFiles(this);
    }
    
    public final void addConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.add(l);
    }
    public final void removeConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.remove(l);
    }
    
    /**
     * Register an instance listener that will listen to server instances changes.
     *
     * @l listener which should be added.
     *
     * @since 1.6
     */
    public final void addInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().addInstanceListener(l);
    }

    /**
     * Remove an instance listener which has been registered previously.
     *
     * @l listener which should be removed.
     *
     * @since 1.6
     */
    public final void removeInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().removeInstanceListener(l);
    }
    
    private void addCFL() {
        //already listen
        if (configFilesListener != null)
            return;
        configFilesListener = new ConfigFilesListener(this, listeners);
    }
        
    private ConfigSupportImpl getConfigSupportImpl() {
        return (ConfigSupportImpl) getConfigSupport();
    }

}
