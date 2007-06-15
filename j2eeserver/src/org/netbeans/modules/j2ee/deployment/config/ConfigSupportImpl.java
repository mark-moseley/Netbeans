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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.common.ComponentInterface;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MappingConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.openide.util.Parameters;

/**
 * Each J2eeModuleProvider hold a reference to an instance of this config support.
 * An instance of ConfigDataObject representing the current target configuration
 * and it is cached for to avoid performance penalty of creating new one for every
 * access to configuration.
 *
 * Whenenver target server of the module changes, a new config support is associate
 * with the module providing access to the right configuration data object.
 *
 * @author  nn136682
 */
//PENDING: cleanup the usage of fakeserver, refresh. Instead, provide UI feedback for
// case when provider does not associate with any server.

public final class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport, 
        ModuleConfigurationProvider {
    
    private static final File[] EMPTY_FILE_LIST = new File[0];
    private static final String GENERIC_EXTENSION = ".dpf"; // NOI18N
    
    private String configurationPrimaryFileName = null;
    private Map relativePaths = null;
    private Map allRelativePaths = null;
    
    private final J2eeModuleProvider provider;
    private final J2eeModule j2eeModule;
    
    private Server server;
    private ServerInstance instance;
    private ModuleConfiguration moduleConfiguration;
    
    /** Creates a new instance of ConfigSupportImpl */
    public ConfigSupportImpl (J2eeModuleProvider provider) {
        this.provider = provider;
        j2eeModule = provider.getJ2eeModule();
        J2eeModuleAccessor.DEFAULT.setJ2eeModuleProvider(j2eeModule, provider);
        String serverInstanceId = provider.getServerInstanceID();
        if (serverInstanceId != null) {
            instance = ServerRegistry.getInstance().getServerInstance(serverInstanceId);
            if (instance != null) {
                // project server instance exists
                server = instance.getServer();
            }
        }
        if (server == null) {
            // project server instance is not set or does not exist
            String serverID = provider.getServerID();
            if (serverID != null) {
                // project server exists
                server = ServerRegistry.getInstance().getServer(serverID);
            }
        }
    }
    
    /**
     * This method save configurations in deployment plan in content directory
     * and return the fileobject for the plan.  Primary use is for remote deployment
     * or standard jsr88 deployement.
     */
    public File getConfigurationFile() {
        try {
            return getDeploymentPlanFileForDistribution();
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
        return null;
    }
    
    /**
     * Return list of server specific configuration files.
     */
    public static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server) {
        return getDeploymentConfigurationFiles(provider, server, false);
    }
    
    public static FileObject[] getConfigurationFiles(J2eeModuleProvider jmp) {
        Collection servers = ServerRegistry.getInstance().getServers();
        ArrayList files = new ArrayList();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server s  = (Server) i.next();
            File[] configs = getDeploymentConfigurationFiles(jmp, s, true);
            for (int j=0; j<configs.length; j++) {
                files.add(FileUtil.toFileObject(configs[j]));
            }
        }
        return (FileObject[]) files.toArray(new FileObject[files.size()]);
    }
    
    public String getDeploymentName() {
        try {
            FileObject fo = getProvider().getJ2eeModule().getContentDirectory();
            if (fo == null) {
                String configFileName = getPrimaryConfigurationFileName();
                File file = j2eeModule.getDeploymentConfigurationFile(configFileName);
                if (file != null) {
                    fo = FileUtil.toFileObject(file);
                }
            }
            if (fo == null)
                return null;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (owner != null)
                return owner.getProjectDirectory().getName();
            
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        return null;
    }
    
    /** dispose all created deployment configurations */
    public void dispose() {
        if (server != null) {
            ModuleConfiguration moduleConfig = null;
            synchronized (this) {
                moduleConfig = moduleConfiguration;
            }
            if (moduleConfig != null) {
                moduleConfig.dispose();
            }
        }
    }
    
    // J2eeModuleProvider.ConfigSupport ---------------------------------------
    
    public boolean createInitialConfiguration() {
        return getModuleConfiguration() != null;
    }
    
    public boolean ensureConfigurationReady() {
        return getModuleConfiguration() != null;
    }
     
    /**
     * Get context root (context path)
     *
     * @return string value, null if not set or not a WAR module
     */
    public String getWebContextRoot() {
        if (!getProvider().getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
            Logger.getLogger("global").log(Level.INFO, "getWebContextRoot called on other module type then WAR"); //NOI18N
            return null;
        }
        if (server == null) {
            return null;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config == null) {
            return null;
        }
        
        try {
            ContextRootConfiguration contextRootConfiguration = config.getLookup().lookup(ContextRootConfiguration.class);
            if (contextRootConfiguration != null) {
                return contextRootConfiguration.getContextRoot();
            }
        } catch (ConfigurationException ce) {
            Logger.getLogger("global").log(Level.INFO, null, ce);
        }
        return null;
    }
    
    /**
     * Set context root (context path)
     */
    public void setWebContextRoot(String contextRoot) {
        if (!getProvider().getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
            Logger.getLogger("global").log(Level.INFO, "setWebContextRoot called on other module type then WAR"); //NOI18N
            return;
        }
        if (server == null) {
            return;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config == null) {
            return;
        }
        try {
            ContextRootConfiguration contextRootConfiguration = config.getLookup().lookup(ContextRootConfiguration.class);
            if (contextRootConfiguration != null) {
                contextRootConfiguration.setContextRoot(contextRoot);
            }
        } catch (ConfigurationException ce) {
            Logger.getLogger("global").log(Level.INFO, null, ce);
        }
    }
        
    public String[] getDeploymentConfigurationFileNames() {
        if (server == null) {
            return new String[]{};
        }
        if (hasCustomSupport()) {
            return (String[]) getRelativePaths().keySet().toArray(new String[relativePaths.size()]);
        }
        return new String[] { getStandardDeploymentPlanName() };
    }
    
    public String getContentRelativePath(String configName) {
        if (! hasCustomSupport()) {
            return configName; //just return the name so that the .dpf file is writen at the root of dist directory.
        }
        return (String) getAllRelativePaths().get(configName);
    }
    
    public void setCMPMappingInfo(final OriginalCMPMapping[] mappings) throws ConfigurationException {
        if (server == null) {
            // the module has no target server
            return;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config == null) {
            return;
        }
        MappingConfiguration mappingConfiguration = config.getLookup().lookup(MappingConfiguration.class);
        if (mappingConfiguration != null) {
            mappingConfiguration.setMappingInfo(mappings);
        }
    }
    
    public void setCMPResource(String ejbName, String jndiName) throws ConfigurationException {
        Parameters.notNull("ejbName", ejbName);     // NOI18N
        Parameters.notNull("jndiName", jndiName);   // NOI18N
        if (server == null) {
            // the module has no target server
            return;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config == null) {
            return;
        }
        MappingConfiguration mappingConfiguration = config.getLookup().lookup(MappingConfiguration.class);
        if (mappingConfiguration != null) {
            mappingConfiguration.setCMPResource(ejbName, jndiName);
        }
    }
    
    public Set<Datasource> getDatasources() throws ConfigurationException {
        
        Set<Datasource> projectDS = Collections.<Datasource>emptySet();
        
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    projectDS = datasourceConfiguration.getDatasources();
                }
            }
        }

        return projectDS;
    }

    public boolean isDatasourceCreationSupported() {
        if (server == null) {
            // the module has no target server
            return false;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config != null) {
            DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
            if (datasourceConfiguration != null) {
                return datasourceConfiguration.supportsCreateDatasource();
            }
        }
        return false;
    }
    
    public Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws UnsupportedOperationException, DatasourceAlreadyExistsException {
        Datasource ds = null;
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    try {
                        ds = datasourceConfiguration.createDatasource(jndiName, url, username, password, driver);
                    } catch (ConfigurationException ce) {
                        Logger.getLogger("global").log(Level.INFO, null, ce);
                    }
                }
            }
        }
        return ds;
    }    
    
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {

        Parameters.notNull("referenceName", referenceName);     // NOI18N
        Parameters.notNull("jndiName", jndiName);               // NOI18N
        
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    datasourceConfiguration.bindDatasourceReference(referenceName, jndiName);
                }
            }
        }
   }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
            String referenceName, String jndiName) throws ConfigurationException {
        
        Parameters.notNull("ejbName", ejbName);             // NOI18N
        Parameters.notNull("ejbType", ejbType);             // NOI18N
        Parameters.notNull("referenceName", referenceName); // NOI18N
        Parameters.notNull("jndiName", jndiName);           // NOI18N
        
        if (!EnterpriseBeans.SESSION.equals(ejbType) &&
            !EnterpriseBeans.ENTITY.equals(ejbType) &&
            !EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            throw new IllegalArgumentException("ejbType parameter doesn't have an allowed value.");
        }
        
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    datasourceConfiguration.bindDatasourceReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
                }
            }
        }
    }

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        
        Parameters.notNull("referenceName", referenceName); // NOI18N
        
        String jndiName = null;
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    jndiName = datasourceConfiguration.findDatasourceJndiName(referenceName);
                }
            }
        }
        
        return jndiName;
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {

        Parameters.notNull("ejbName", ejbName);             // NOI18N
        Parameters.notNull("referenceName", referenceName); // NOI18N

        String jndiName = null;
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                DatasourceConfiguration datasourceConfiguration = config.getLookup().lookup(DatasourceConfiguration.class);
                if (datasourceConfiguration != null) {
                    jndiName = datasourceConfiguration.findDatasourceJndiNameForEjb(ejbName, referenceName);
                }
            }
        }
        
        return jndiName;
    }

    public Datasource findDatasource(String jndiName) throws ConfigurationException {
        
        Parameters.notNull("jndiName", jndiName);           // NOI18N

        Set<Datasource> datasources = getDatasources();
        for (Datasource ds : datasources) {
            if (jndiName.equals(ds.getJndiName())) {
                return ds;
            }
        }
        datasources = provider.getServerDatasources();
        for (Datasource ds : datasources) {
            if (jndiName.equals(ds.getJndiName())) {
                return ds;
            }
        }
        
        return null;
    }

    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        
        Set<MessageDestination> destinations = Collections.<MessageDestination>emptySet();
        
        if (server != null) {
            ModuleConfiguration config = getModuleConfiguration();
            if (config != null) {
                MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
                if (msgConfig != null) {
                    destinations = msgConfig.getMessageDestinations();
                }
            }
        }

        return destinations;
    }

    public Set<MessageDestination> getServerMessageDestinations() throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(provider.getServerInstanceID());
        if (si == null) {
            Logger.getLogger("global").log(Level.WARNING,
                                           "The server data sources cannot be retrieved because the server instance cannot be found.");
            return Collections.<MessageDestination>emptySet();
        }
        
        return si.getMessageDestinations();
   }
    
    public boolean supportsCreateMessageDestination() {
        if (server == null) {
            // the module has no target server
            return false;
        }
        ModuleConfiguration config = getModuleConfiguration();
        if (config != null) {
            MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
            if (msgConfig != null) {
                return msgConfig.supportsCreateMessageDestination();
            }
        }
        return false;
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException {
        
        Parameters.notNull("name", name);           // NOI18N
        Parameters.notNull("type", type);           // NOI18N
        
        if (server == null) {
            return null;
        }
         
        ModuleConfiguration config = getModuleConfiguration();
        if (config == null) {
            return null;
        }
        
        MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
        if (msgConfig != null) {
            return msgConfig.createMessageDestination(name, type);
        }

        return null;
    }
    
    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException {
        
        Parameters.notNull("mdbName", mdbName);     // NOI18N
        Parameters.notNull("name", name);           // NOI18N
        Parameters.notNull("type", type);           // NOI18N

        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return;
        }
        
        MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
        if (msgConfig != null) {
            msgConfig.bindMdbToMessageDestination(mdbName, name, type);
        }
    }

    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        
        Parameters.notNull("mdbName", mdbName);     // NOI18N
        
        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return null;
        }
        
        MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
        if (msgConfig != null) {
            return msgConfig.findMessageDestinationName(mdbName);
        }
        
        return null;
    }
    
    public MessageDestination findMessageDestination(String name) throws ConfigurationException {
        
        Parameters.notNull("name", name);     // NOI18N

        Set<MessageDestination> destinations = getMessageDestinations();
        for (MessageDestination dest : destinations) {
            if (name.equals(dest.getName())) {
                return dest;
            }
        }
        destinations = provider.getConfigSupport().getServerMessageDestinations();
        for (MessageDestination dest : destinations) {
            if (name.equals(dest.getName())) {
                return dest;
            }
        }
        
        return null;
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException {
        
        Parameters.notNull("referenceName", referenceName);                 // NOI18N
        Parameters.notNull("connectionFactoryName", connectionFactoryName); // NOI18N
        Parameters.notNull("destName", destName);                           // NOI18N
        Parameters.notNull("type", type);                                   // NOI18N
        
        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return;
        }
        
        MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
        if (msgConfig != null) {
            msgConfig.bindMessageDestinationReference(referenceName, connectionFactoryName, destName, type);
        }
    }

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
        
        Parameters.notNull("ejbName", ejbName);             // NOI18N
        Parameters.notNull("ejbType", ejbType);             // NOI18N
        Parameters.notNull("referenceName", referenceName);                 // NOI18N
        Parameters.notNull("connectionFactoryName", connectionFactoryName); // NOI18N
        Parameters.notNull("destName", destName);                           // NOI18N
        Parameters.notNull("type", type);                                   // NOI18N
        
        if (!EnterpriseBeans.SESSION.equals(ejbType) &&
            !EnterpriseBeans.ENTITY.equals(ejbType) &&
            !EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            throw new IllegalArgumentException("ejbType parameter doesn't have an allowed value.");
        }
        
        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return;
        }
        
        MessageDestinationConfiguration msgConfig = config.getLookup().lookup(MessageDestinationConfiguration.class);
        if (msgConfig != null) {
            msgConfig.bindMessageDestinationReferenceForEjb(ejbName, ejbType, referenceName, connectionFactoryName, destName, type);
        }
        
    }
    
    public void bindEjbReference(String referenceName, String referencedEjbName) throws ConfigurationException {
        
        Parameters.notNull("referenceName", referenceName);     // NOI18N
        Parameters.notNull("referencedEjbName", referencedEjbName);                 // NOI18N
        
        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return;
        }
        
        EjbResourceConfiguration ejbConfig = config.getLookup().lookup(EjbResourceConfiguration.class);
        if (ejbConfig != null) {
            ejbConfig.bindEjbReference(referenceName, referencedEjbName);
        }
        
    }

    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String referencedEjbName) throws ConfigurationException {
        
        Parameters.notNull("ejbName", ejbName);                 // NOI18N
        Parameters.notNull("ejbType", ejbType);                 // NOI18N
        Parameters.notNull("referenceName", referenceName);     // NOI18N
        Parameters.notNull("referencedEjbName", referencedEjbName);                 // NOI18N
        
        if (!EnterpriseBeans.SESSION.equals(ejbType) &&
            !EnterpriseBeans.ENTITY.equals(ejbType) &&
            !EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)) {
            throw new IllegalArgumentException("ejbType parameter doesn't have an allowed value.");
        }
        
        ModuleConfiguration config = getModuleConfiguration();
        if (server == null || config == null) {
            return;
        }
        
        EjbResourceConfiguration ejbConfig = config.getLookup().lookup(EjbResourceConfiguration.class);
        if (ejbConfig != null) {
            ejbConfig.bindEjbReferenceForEjb(ejbName, ejbType, referenceName, referencedEjbName);
        }
        
    }

    // DeploymentConfigurationProvider implementation -------------------------
    
    /**
     * Create and cache deployment configuration for the current server.
     */
    public synchronized ModuleConfiguration getModuleConfiguration() {
        if (moduleConfiguration == null) {
            try {
                if (server == null) {
                    return null;
                }
                ModuleConfigurationFactory moduleConfigurationFactory = server.getModuleConfigurationFactory();
                moduleConfiguration = moduleConfigurationFactory.create(j2eeModule);
            } catch (ConfigurationException ce) {
                Logger.getLogger("global").log(Level.INFO, null, ce);
                return null;
            }
        }
        return moduleConfiguration;
    }
        
    public J2eeModule getJ2eeModule(String moduleUri) {
        if (j2eeModule instanceof J2eeApplication) {
            for (J2eeModule childModule : ((J2eeApplication) j2eeModule).getModules()) {
                if (childModule.getUrl().equals(moduleUri)) {
                    return childModule;
                }
            }
            // If the moduleUri is null, the j2eeModule needs to be sent back,
            //     to enable directory deployment of EAR projects.
            return moduleUri == null ? j2eeModule : null;
        }
        return j2eeModule;
    }
    
    // private helpers --------------------------------------------------------
    
    /**
     * Return list of server specific configuration files.
     */
    private static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server, boolean existingOnly) {
        if (provider == null || server == null)
            return new File[0];
        
        ModuleType type = (ModuleType) provider.getJ2eeModule().getModuleType();
        String[] fnames;
        if (hasCustomSupport(server, type)) {
            fnames = server.getDeploymentPlanFiles(type);
        } else if (server.supportsModuleType(type)) {
            fnames = new String[] { getStandardDeploymentPlanName(server) };
        } else {
            return EMPTY_FILE_LIST;
        }
        
        ArrayList files = new ArrayList();
        for (int i = 0; i < fnames.length; i++) {
            File path = new File(fnames[i]);
            String fname = path.getName();
            File file = provider.getJ2eeModule().getDeploymentConfigurationFile(fname);
            if (file != null && (!existingOnly || file.exists())) {
                files.add(file);
            }
        }
        return (File[])files.toArray(new File[files.size()]);
    }
    
    /**
     * Creates and returns the JSR-88 deployment plan file for the current 
     * deployment configuration.
     *
     * @return deployment plan file.
     */
    private File getDeploymentPlanFileForDistribution() throws IOException, ConfigurationException {
        if (server == null) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_NoTargetSelected");
            throw new ConfigurationException(msg);
        }
        
        FileLock lock = null;
        OutputStream out = null;
        try {
            FileObject dist = getProvider().getJ2eeModule().getContentDirectory();
            String planName = getStandardDeploymentPlanName();
            FileObject plan = null;
            if (dist != null) {
                plan = dist.getFileObject(planName);
                if (plan == null) {
                    plan = dist.createData(planName);
                }
            } else {
                return null;
            }
            lock = plan.lock();
            out = plan.getOutputStream(lock);
            ModuleConfiguration conf = getModuleConfiguration();
            if (conf != null) {
                DeploymentPlanConfiguration deploymentPlanConfiguration = conf.getLookup().lookup(DeploymentPlanConfiguration.class);
                if (deploymentPlanConfiguration != null) {
                    deploymentPlanConfiguration.save(out);
                    return FileUtil.toFile(plan);
                }
            }
            return null;
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (out != null) out.close();
            } catch(IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.toString());
            }
        }
    }
    
    private String getPrimaryConfigurationFileName() {
        getRelativePaths();
        
        if (configurationPrimaryFileName == null)
            return getStandardDeploymentPlanName();
        else
            return configurationPrimaryFileName;
    }

    private String getStandardDeploymentPlanName() {
        return getStandardDeploymentPlanName(server);

    }
    
    private static String getStandardDeploymentPlanName(Server server) {
        return server.getShortName() + GENERIC_EXTENSION;
    }

    private FileObject findPrimaryConfigurationFO() throws IOException {
        String configFileName = getPrimaryConfigurationFileName();
        File file = j2eeModule.getDeploymentConfigurationFile(configFileName);
        return FileUtil.toFileObject(file);
    }   

    private ModuleType getModuleType() {
        return (ModuleType) getProvider().getJ2eeModule().getModuleType();
    }
    
    private boolean hasCustomSupport() {
        return hasCustomSupport(server, getModuleType());
    }
    
    private static boolean hasCustomSupport(Server server, ModuleType type) {
        if (server == null || server.getModuleConfigurationFactory() == null) {
            return false;
        }
        return server.getDeploymentPlanFiles(type) != null;
    }

    private J2eeModuleProvider getProvider () {
        return provider;
    }
    
    private Map getRelativePaths() {
        if (relativePaths != null) 
            return relativePaths;
        
        relativePaths = new HashMap();
        if (hasCustomSupport()) {
            String [] paths = server.getDeploymentPlanFiles(getModuleType());
            configurationPrimaryFileName = paths[0].substring(paths[0].lastIndexOf("/")+1);
        
            collectData(server, relativePaths);
        }
        
        return relativePaths;
    }
    
    private void collectData(Server server, Map map) {
        if (! this.hasCustomSupport(server, getModuleType()))
            return;
        
        String [] paths = server.getDeploymentPlanFiles(getModuleType());
        paths = (paths == null) ? new String[0] : paths;
        for (int i=0; i<paths.length; i++) {
            String name = paths[i].substring(paths[i].lastIndexOf("/")+1);
            map.put(name, paths[i]);
        }        
    }
    
    private Map getAllRelativePaths() {
        if (allRelativePaths != null)
            return allRelativePaths;
        
        allRelativePaths = new HashMap();
        Collection servers = ServerRegistry.getInstance().getServers();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server server = (Server) i.next();
            collectData(server, allRelativePaths);
        }
        return allRelativePaths;
    }
}
