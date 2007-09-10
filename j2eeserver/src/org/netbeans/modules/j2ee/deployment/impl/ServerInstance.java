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


package org.netbeans.modules.j2ee.deployment.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import javax.enterprise.deploy.spi.status.*;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.openide.filesystems.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;


public class ServerInstance implements Node.Cookie, Comparable {
    
    /** Server state is being checked or state changes is in progress */
    public static final int STATE_WAITING   = 1;
    /** Server is stopped */
    public static final int STATE_STOPPED   = 2;
    /** Server is running in normal mode */
    public static final int STATE_RUNNING   = 3;
    /** Server is running in debug mode */
    public static final int STATE_DEBUGGING = 4;
    /** Server is suspended on a break point (in debug mode and not responding) */
    public static final int STATE_SUSPENDED = 5;
    /** Server is running in profile mode */
    public static final int STATE_PROFILING = 6;
    /** Server is ready for the profiler to connect, server JVM is blocked. */
    public static final int STATE_PROFILER_BLOCKING = 7;
    /** Server is starting in profile mode. */
    public static final int STATE_PROFILER_STARTING = 8;
    
    /** For how long should plugins be allowed to block in the isRunning method */
    private static final int RUNNING_CHECK_TIMEOUT = 10000; // in millis
    /** For how long should plugins be allowed to block in the isDebuggable method */
    private static final int DEBUGGING_CHECK_TIMEOUT = 10000; // in millis
    
    /** Default maximum amount of time the server should finish starting/stopping/deploying in */
    private static final long DEFAULT_TIMEOUT = 1200000; // in millis
    
    private static final Logger LOGGER = Logger.getLogger(ServerInstance.class.getName());
    
    private final String url;
    private final Server server;
    private DeploymentManager manager;
    private DeploymentManager disconnectedManager;
    private IncrementalDeployment incrementalDeployment;
    private TargetModuleIDResolver tmidResolver;
    private J2eePlatform j2eePlatform;
    private J2eePlatformImpl j2eePlatformImpl;
    private StartServer startServer;
    private FindJSPServlet findJSPServlet;
    private DatasourceManager dsMgr;
    private DatasourceManager ddsMgr;
    private MessageDestinationDeployment msgDestDeploymentConnected;
    private MessageDestinationDeployment msgDestDeploymentDisconnected;
    private final Set targetsStartedByIde = new HashSet(); // valued by target name
    private Map targets; // keyed by target name, valued by ServerTarget
    private boolean managerStartedByIde = false;
    private ServerTarget coTarget = null;
    private final InstancePropertiesImpl instanceProperties;
    private final HashMap/*<Target, ServerDebugInfo>*/ debugInfo = new HashMap();
    
    // last known server state, the initial value is stopped
    private volatile int serverState = STATE_STOPPED;
    // server state listeners
    private List stateListeners = new ArrayList();
    
    // running check helpers
    private long lastCheck = 0;
    private boolean isRunning = false;
    
    
    private volatile static ServerInstance profiledServerInstance;
    private ProfilerServerSettings  profilerSettings;
    
    private final DebuggerStateListener debuggerStateListener;
    
    // PENDING how to manage connected/disconnected servers with the same manager?
    // maybe concept of 'default unconnected instance' is broken?
    public ServerInstance(Server server, String url) {
        this.server = server;
        this.url = url;
        instanceProperties = new InstancePropertiesImpl(url);
        // listen to debugger changes so that we can update server status accordingly
        debuggerStateListener = new DebuggerStateListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(debuggerStateListener);
    }
    
    /** Return this server instance InstanceProperties. */
    public InstancePropertiesImpl getInstanceProperties() {
        return instanceProperties;
    }
    
    /** Return display name of this server instance.*/
    public String getDisplayName() {
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    /**
     * Returns value of the specified timeout propety in milliseconds. If the
     * timeout property is not defined the specified default values is returned.
     * 
     * @param propName timeout property
     * @param defaultValue value which will be returned when the specified timeout
     *        property is not set.
     * 
     * @return value of the specified timeout propety in milliseconds.
     */
    private long getTimeout(String propName, long defaultValue) {
        long returnValue = defaultValue;
        String timeout = instanceProperties.getProperty(propName);
        if (timeout != null) {
            try {
                returnValue = Long.parseLong(timeout) * 1000;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.FINE, "could not parse timeout property", e); // NOI18N
            }
        }
        return returnValue;
    }
    
    /** Get the server startup timeout in milliseconds */
    private long getStartupTimeout() {
        return getTimeout(InstanceProperties.STARTUP_TIMEOUT, DEFAULT_TIMEOUT);
    }
    
    /** Get the server shutdown timeout in milliseconds */
    private long getShutdownTimeout() {
        return getTimeout(InstanceProperties.SHUTDOWN_TIMEOUT, DEFAULT_TIMEOUT);
    }
    
    /** Get the deployment timeout in milliseconds */
    long getDeploymentTimeout() {
        return getTimeout(InstanceProperties.DEPLOYMENT_TIMEOUT, DEFAULT_TIMEOUT);
    }
    
    public Server getServer() {
        return server;
    }
    
    public String getUrl() {
        return url;
    }
    
    public DeploymentManager getDeploymentManager() {
        DeploymentManager managerTmp = null;
        synchronized (this) {
            managerTmp = manager;
        }
        if (managerTmp != null) {
            return managerTmp;
        }
        try {
            FileObject fo = ServerRegistry.getInstanceFileObject(url);
            if (fo == null) {
                String msg = NbBundle.getMessage(ServerInstance.class, "MSG_NullInstanceFileObject", url);
                throw new IllegalStateException(msg);
            }
            String username = (String) fo.getAttribute(ServerRegistry.USERNAME_ATTR);
            String password = (String) fo.getAttribute(ServerRegistry.PASSWORD_ATTR);
            managerTmp = server.getDeploymentManager(url, username, password);
            synchronized (this) {
                manager = managerTmp;
            }
        } catch(javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException e) {
            throw new RuntimeException(e);
        }
        return managerTmp;
    }
    
    public synchronized boolean isConnected () {
        return manager != null;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager() throws DeploymentManagerCreationException {
        DeploymentManager disconnectedManagerTmp = null;
        synchronized (this) {
            disconnectedManagerTmp = disconnectedManager;
        }
        if (disconnectedManagerTmp != null) {
            return disconnectedManagerTmp;
        }
        FileObject fo = ServerRegistry.getInstanceFileObject(url);
        if (fo == null) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_NullInstanceFileObject", url);
            throw new DeploymentManagerCreationException(msg);
        }
        disconnectedManagerTmp = server.getDisconnectedDeploymentManager(url);
        synchronized (this) {
            disconnectedManager = disconnectedManagerTmp;
        }
        return disconnectedManagerTmp;
    }
    
    public J2eePlatform getJ2eePlatform() {
        return j2eePlatform;
    }
    
    public void setJ2eePlatform(J2eePlatform aJ2eePlatform ) {
        j2eePlatform = aJ2eePlatform;
    }
    
    public J2eePlatformImpl getJ2eePlatformImpl() {
        if (j2eePlatformImpl == null) {
            J2eePlatformFactory fact = server.getJ2eePlatformFactory();
            // TODO this will be removed, implementation of J2EEPlatformFactory will be mandatory
            if (fact != null) {
                try {
                    j2eePlatformImpl = fact.getJ2eePlatformImpl(isConnected() ? getDeploymentManager() : getDisconnectedDeploymentManager());
                }  catch (DeploymentManagerCreationException dmce) {
                    Exceptions.printStackTrace(dmce);
                }
            }
        }
        return j2eePlatformImpl;
    }
    
    public ServerDebugInfo getServerDebugInfo(Target target) {
        assert debugInfo != null;
        ServerDebugInfo sdi = null;
        if (target == null) { //performance: treat as special simple case
            sdi = (ServerDebugInfo) debugInfo.get(null);
        } else {
            for (Iterator it = debugInfo.keySet().iterator(); sdi == null && it.hasNext(); ) {
                Target t = (Target) it.next();
                if (t == target || (t != null && t.getName().equals(target.getName()))) {
                    sdi = (ServerDebugInfo) debugInfo.get(t);
                }
            }
        }
        
        return sdi;
    }
    
    public void refresh() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    int oldState = getServerState();
                    setServerState(STATE_WAITING);
                    if (ServerInstance.this == profiledServerInstance) {
                        int profState = ProfilerSupport.getState();
                        if (profState == ProfilerSupport.STATE_STARTING) {
                            setServerState(ServerInstance.STATE_PROFILER_STARTING);
                            return;
                        } else if (profState == ProfilerSupport.STATE_BLOCKING) {
                            setServerState(ServerInstance.STATE_PROFILER_BLOCKING);
                            return;
                        } else if (profState == ProfilerSupport.STATE_PROFILING
                                   || profState == ProfilerSupport.STATE_RUNNING) {
                            initCoTarget();
                            setServerState(ServerInstance.STATE_PROFILING);
                            return;
                        } else {
                            //  profiler is inactive - has been shutdown
                            profiledServerInstance = null;
                        }
                    }
                    if (isSuspended()) {
                        setServerState(ServerInstance.STATE_SUSPENDED);
                    } else if (isDebuggable(null)) {
                        if (oldState != ServerInstance.STATE_SUSPENDED) {
                            // this will decrease the possibility of accessing server
                            // when it is in suspended mode when we might freeze
                            reset();
                        }
                        initCoTarget();
                        setServerState(ServerInstance.STATE_DEBUGGING);
                    } else if (isReallyRunning()) {
                        reset();
                        initCoTarget();
                        setServerState(ServerInstance.STATE_RUNNING);
                    } else {
                        reset();
                        setServerState(ServerInstance.STATE_STOPPED);
                    }
                } finally {
                    // safety catch - make sure that we are not still waiting
                    if (getServerState() == STATE_WAITING) {
                        setServerState(ServerInstance.STATE_STOPPED);
                    }
                }
            }
        });
    }
    
    public void reset() {
        DeploymentManager managerTmp = null;
        synchronized (this) {
            managerTmp = manager;
            manager = null;
        }
        if (managerTmp != null) {
            managerTmp.release();
        }
        synchronized (this) {
            disconnectedManager = null;
            incrementalDeployment = null;
            tmidResolver = null;
            startServer = null;
            findJSPServlet = null;
            coTarget = null;
            targets = null;
        }
    }
    
    /** Remove this server instance and stop it if it has been started from within the IDE */
    public void remove() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(debuggerStateListener);
        stopIfStartedByIde();        
        // close the server io window
        InputOutput io = UISupport.getServerIO(url);
        if (!io.isClosed()) {
            io.closeInputOutput();
        }
        ServerRegistry.getInstance().removeServerInstance(getUrl());
    }
    
    /** Stop the server if it has been started from within the IDE, do nothing otherwise */
    public void stopIfStartedByIde() {
        if (managerStartedByIde) {
            if (canStopDontWait()) {
                stopDontWait();
            } else {
                String title = NbBundle.getMessage(ServerInstance.class, "LBL_ShutDownServer", getDisplayName());
                final ProgressUI progressUI = new ProgressUI(title, true, null);
                progressUI.start();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            for (Iterator it = targetsStartedByIde.iterator(); it.hasNext();) {
                                ServerTarget serverTarget = getServerTarget((String)it.next());
                                if (serverTarget != null) {
                                    try {
                                        _stop(serverTarget.getTarget(), progressUI);
                                    } catch (ServerException ex) {
                                        Logger.getLogger("global").log(Level.INFO, null, ex);
                                    }
                                }
                            }
                            if (isReallyRunning() || isSuspended()) {
                                try {
                                    _stop(progressUI);
                                } catch (ServerException ex) {
                                    Logger.getLogger("global").log(Level.INFO, null, ex);
                                }
                            }
                        } finally {
                            progressUI.finish();
                        }
                    }
                });
                progressUI.showProgressDialog();
            }
        }
    }
    
    /** Set the server state and notify all listeners */
    public void setServerState(int newState) {
        int oldState = serverState;
        serverState = newState;
        fireStateChanged(oldState, newState);
    }
    
    /** Return the current server state */
    public int getServerState() {
        return serverState;
    }
    
    /** Is it forbidden to remove this server instance from the server registry? */
    public boolean isRemoveForbidden() {
        String removeForbid = instanceProperties.getProperty(InstanceProperties.REMOVE_FORBIDDEN);
        return Boolean.valueOf(removeForbid).booleanValue();
    }
    
    public ServerTarget[] getTargets() {
        Map targets = getTargetMap();
        synchronized (this) {
            return (ServerTarget[]) targets.values().toArray(new ServerTarget[targets.size()]);
        }
    }
    
    public Collection getTargetList() {
        Map targets = getTargetMap();
        synchronized (this) {
            return targets.values();
        }
    }
    
    // PENDING use targets final variable?
    private Map getTargetMap() {
        Map tmpTargets = null;
        synchronized (this) {
            tmpTargets = targets;
        }
        if (tmpTargets == null || tmpTargets.size() < 1) {
            Target[] targs = null;
            StartServer startServer = getStartServer();
            try {
                if (! isRunning() && startServer != null && startServer.needsStartForTargetList()) {
                    start();
                }
                
                targs = getDeploymentManager().getTargets();
            } catch(IllegalStateException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            if (targs == null) {
                targs = new Target[0];
            }
            
            tmpTargets = new HashMap();
            for (int i = 0; i < targs.length; i++) {
                tmpTargets.put(targs[i].getName(), new ServerTarget(this, targs[i]));
            }
            synchronized (this) {
                targets = tmpTargets;
            }
        }
        return tmpTargets;
    }
    
    public ServerTarget getServerTarget(String targetName) {
        return (ServerTarget) getTargetMap().get(targetName);
    }
    
    public Target getTarget(String targetName) {
        return getServerTarget(targetName).getTarget();
    }
    
    public StartServer getStartServer() {
        DeploymentManager dm = null;
        try {
            dm = getDisconnectedDeploymentManager();
        }  catch (DeploymentManagerCreationException dmce) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.INFO, null, dmce);
            return null;
        }
        synchronized (this) {
            if (startServer == null) {
                startServer = server.getOptionalFactory().getStartServer(dm);
            }
            return startServer;
        }
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        DeploymentManager dm = getDeploymentManager();
        synchronized (this) {
            if (incrementalDeployment == null) {
                incrementalDeployment = server.getOptionalFactory().getIncrementalDeployment(dm);
            }
            return incrementalDeployment;
        }
    }
    
    public AntDeploymentProvider getAntDeploymentProvider() {
        try {
            return server.getOptionalFactory().getAntDeploymentProvider(getDisconnectedDeploymentManager());
        } catch (DeploymentManagerCreationException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
            return null;
        }
    }
    
    public JDBCDriverDeployer getJDBCDriverDeployer() {
        try {
            return server.getOptionalFactory().getJDBCDriverDeployer(getDisconnectedDeploymentManager());
        } catch (DeploymentManagerCreationException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
            return null;
        }
    }
    
    public TargetModuleIDResolver getTargetModuleIDResolver() {
        DeploymentManager dm = getDeploymentManager();
        synchronized (this) {
            if (tmidResolver == null) {
                tmidResolver = server.getOptionalFactory().getTargetModuleIDResolver(dm);
            }
            return tmidResolver;
        }
    }
    
    public FindJSPServlet getFindJSPServlet() {
        DeploymentManager dm = null;
        try {
            dm = getDisconnectedDeploymentManager();
        }  catch (DeploymentManagerCreationException dmce) {
            throw new RuntimeException(dmce);
        }
        synchronized (this) {
            if (findJSPServlet == null) {
                findJSPServlet = server.getOptionalFactory().getFindJSPServlet(dm);
            }
            return findJSPServlet;
        }
    }
    
    private DatasourceManager getDatasourceManager() {
        DeploymentManager dm = getDeploymentManager();
        synchronized (this) {
            if (dsMgr == null) {
                dsMgr = server.getOptionalFactory().getDatasourceManager(dm);
            }
            return dsMgr;
        }
    }
    
    private DatasourceManager getDisconnectedDatasourceManager() {
        DeploymentManager dm = null;
        try {
            dm = getDisconnectedDeploymentManager();
        }  catch (DeploymentManagerCreationException dmce) {
            throw new RuntimeException(dmce);
        }
        synchronized (this) {
            if (ddsMgr == null) {
                ddsMgr = server.getOptionalFactory().getDatasourceManager(dm);
            }
            return ddsMgr;
        }
    }
    
    /**
     * Gets the data sources deployed on the this server instance.
     *
     * @return set of data sources 
     */
    public Set<Datasource> getDatasources() throws ConfigurationException {
        
        DatasourceManager ddsMgr = getDisconnectedDatasourceManager();
                
        Set deployedDS = Collections.<Datasource>emptySet();
        if (ddsMgr != null) 
            deployedDS = ddsMgr.getDatasources();
        
        return deployedDS;
    }
    
    /**
     * Deploys data sources saved in the module.
     *
     * @exception ConfigurationException if there is some problem with data source configuration
     * @exception DatasourceAlreadyExistsException if module data source(s) are conflicting
     * with data source(s) already deployed on the server
     */
    public void deployDatasources(Set<Datasource> datasources) throws ConfigurationException, DatasourceAlreadyExistsException {
        
        DatasourceManager dsMgr = getDatasourceManager();

        if (dsMgr != null) 
            dsMgr.deployDatasources(datasources);
    }
    
    private synchronized MessageDestinationDeployment getMessageDestinationDeploymentConnected() {
        if (msgDestDeploymentConnected == null) {
            msgDestDeploymentConnected = server.getOptionalFactory().
                    getMessageDestinationDeployment(getDeploymentManager());
        }

        return msgDestDeploymentConnected;
    }
    
    private MessageDestinationDeployment getMessageDestinationDeploymentDisconnected() {
        DeploymentManager dm = null;
        try {
            dm = getDisconnectedDeploymentManager();
        }  catch (DeploymentManagerCreationException dmce) {
            throw new RuntimeException(dmce);
        }
        synchronized (this) {
            if (msgDestDeploymentDisconnected == null) {
                msgDestDeploymentDisconnected = server.getOptionalFactory().getMessageDestinationDeployment(dm);
            }
            return msgDestDeploymentDisconnected;
        }
    }
    
    /**
     * Retrieves message destinations configured on the target server instance.
     *
     * @return set of message destinations
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        
        MessageDestinationDeployment destDepl = getMessageDestinationDeploymentDisconnected();
        if (destDepl != null) {
            return destDepl.getMessageDestinations();
        }
        
        return Collections.<MessageDestination>emptySet();
    }
    
    /**
     * Deploys message destinations saved in the module.
     *
     * @param destinations set of message destinations
     * 
     * @throws NullPointerException if destinations parameter is null
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
    
        Parameters.notNull("destinations", destinations);
        
        MessageDestinationDeployment destDepl = getMessageDestinationDeploymentConnected();
        if (destDepl != null) {
            destDepl.deployMessageDestinations(destinations);
        }
    }
    
    //---------- State API's:  running, debuggable, startedByIDE -----------
    
    public boolean isRunningLastCheck() {
        if (lastCheck > 0) {
            return isRunning;
        } else {
            return false;
        }
    }
    
    public boolean isReallyRunning() {
        return isRunningWithinMillis(0);
    }
    
    public boolean isRunning() {
        return isRunningWithinMillis(2000);
    }
    
    public boolean isRunningWithinMillis(long millisecs) {
        if (System.currentTimeMillis() - lastCheck < millisecs) {
            return isRunning;
        }
        final StartServer ss = getStartServer();
        if (ss != null) {
            isRunning = safeTrueTest(new SafeTrueTest() {
                                         public void run() {
                                             result = ss.isRunning();
                                         }
                                     }, 
                                     RUNNING_CHECK_TIMEOUT);
        } else {
            isRunning = false;
        }
        lastCheck = System.currentTimeMillis();
        return isRunning;
    }
    
    public boolean isDebuggable(final Target target) {
        final StartServer ss = getStartServer();
        if (ss != null) {
            return safeTrueTest(new SafeTrueTest() {
                                    public void run() {
                                        result = ss.isDebuggable(target);
                                    }
                                }, 
                                DEBUGGING_CHECK_TIMEOUT);
        } else {
            return false;
        }
    }
    
    /**
     * @return conflict data instance for server instance running in debug mode with the same socket number
     * or shared memory id. If no such server instance exists then null is returned.
     */
    public ConflictData anotherServerDebuggable(Target target) {
        
        ConflictData cd = null;
        //get debug info for this instance
        StartServer thisSS = getStartServer();
        if (thisSS == null) //this case should not occur =>
            return null; //attempt to start server (serverInstance remains null)
        ServerDebugInfo thisSDI = getServerDebugInfo(target);
        if (thisSDI == null) {
            Target t = _retrieveTarget(target);
            thisSDI = thisSS.getDebugInfo(t);
            if (thisSDI == null) {
                Logger.getLogger("global").log(Level.INFO, "DebuggerInfo cannot be found for: " + this.toString());
                return null;
            }
        }
        
        //get all server instances
        ServerInstance[] serverInstances = ServerRegistry.getInstance().getServerInstances();
        //check existence of a server instance running in debug mode with the same parameters
        for (int i = 0; cd == null && i < serverInstances.length; i++) {
            ServerInstance si = serverInstances[i];
            if (url.equalsIgnoreCase(si.getUrl())) continue;
            if (si.isDebuggable(null)) { //running in debug mode
                Target t = si._retrieveTarget(null);
                ServerDebugInfo sdi = si.getServerDebugInfo(t);
                if (sdi == null) continue; //should not occur -> workaround for issue #56714
                if (thisSDI.getTransport().equals(sdi.getTransport())) { //transport matches
                    if (thisSDI.getTransport() == ServerDebugInfo.TRANSPORT_SOCKET) {
                        if (thisSDI.getHost().equalsIgnoreCase(sdi.getHost())) //host matches
                            if (thisSDI.getPort() == sdi.getPort()) //port matches
                                cd = new ConflictData(si, thisSDI);
                    } else if (thisSDI.getShmemName().equalsIgnoreCase(sdi.getShmemName()))
                        cd = new ConflictData(si, thisSDI);
                }
            }
        }
        
        return cd;
    }
    
    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        Target target = _retrieveTarget(null);
        ServerDebugInfo sdi = getServerDebugInfo(target);
        if (sdi == null) {
            LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: " + this.toString());
            return false; // give user a chance to start server even if we don't know whether she will success
        }

        for (int i = 0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s == null) {
                continue;
            }
            Object o = s.lookupFirst(null, AttachingDICookie.class);
            if (o == null) {
                continue;
            }

            AttachingDICookie attCookie = (AttachingDICookie) o;
            if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                String shmem = attCookie.getSharedMemoryName();
                if (shmem == null) {
                    continue;
                }
                if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                    Object d = s.lookupFirst(null, JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger) d;
                        if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                            return true;
                        }
                    }
                }
            } else {
                String host = attCookie.getHostName();
                if (host != null && isSameHost(host, sdi.getHost())
                        && attCookie.getPortNumber() == sdi.getPort()) {
                    
                    Object d = s.lookupFirst(null, JPDADebugger.class);
                    if (d != null) {
                        JPDADebugger jpda = (JPDADebugger) d;
                        if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Can be this server started in the debug mode? Currently the only case when
     * the server cannot be started in the debugged is when the admin server is
     * not also the target server.
     *
     * @return <code>true</code> if the server can be started in the debug mode,
     *         <code>false/code> otherwise.
     */
    public boolean isDebugSupported() {
        StartServer ss = getStartServer();
        return ss.supportsStartDebugging(null);
    }
    
    /** 
     * Can be this server started in profile mode? Currently the only case when
     * the server cannot be started in the debugged is when the admin server is
     * not also the target server.
     */
    public boolean isProfileSupported() {
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler == null) {
            return false;
        }
        StartServer ss = getStartServer();
        return ss.supportsStartProfiling(null);
    }
    
    /**
     * Return set of ServerTarget's that have been started from inside IDE.
     * @return set of ServerTarget objects.
     */
    public Set getTargetsStartedByIde() {
        Set ret = new HashSet();
        for (Iterator i=targetsStartedByIde.iterator(); i.hasNext(); ) {
            String targetName = (String) i.next();
            ret.add(getServerTarget(targetName));
        }
        return ret;
    }
    
    //----------- State Transistion API's: ----------------------
    
    /**
     * Start the admin server. Show UI feedback.
     * Note: for debug mode, always use startDebugTarget() calls because
     * it is sure then the target need to be started.
     *
     * @throws ServerException if the server cannot be started.
     */
    public void start(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            startTarget(null, ui);
        } finally {
            refresh();
        }
    }
    
    /** Start the admin server in the debug mode. Show UI feedback. 
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startDebug(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            startTarget(null, ui, true);
            _retrieveDebugInfo(null);
        } finally {
            refresh();
        }
    }
    
    /** Start the admin server in the profile mode. Show UI feedback. 
     * @param settings settings that will be used to start the server
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startProfile(ProfilerServerSettings settings, boolean forceRestart, ProgressUI ui) 
    throws ServerException {
        // check whether another server not already running in profile mode
        // and ask whether it is ok to stop it
        ServerInstance tmpProfiledServerInstance = profiledServerInstance;
        if (tmpProfiledServerInstance != null && tmpProfiledServerInstance != this) {
            String msg = NbBundle.getMessage(
                                    ServerInstance.class,
                                    "MSG_AnotherServerProfiling",
                                    tmpProfiledServerInstance.getDisplayName());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.CANCEL_OPTION) {
                // start in profile mode has been cancelled
                String err = NbBundle.getMessage(ServerInstance.class, "MSG_ProfilingCancelled", getDisplayName());
                throw new ServerException(err);
            }
        }
        try {
            setServerState(STATE_WAITING);
            // target == null - admin server
            startProfileImpl(null, settings, forceRestart, ui);
        } finally {
            refresh();
        }
    }
    
    /** Restart the admin server in the mode the server was running in before. 
     * Show UI feedback. 
     *
     * @throws ServerException if the server cannot be restarted.
     */
    public void restart(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            boolean inDebug = isDebuggable(null);
            boolean inProfile = profiledServerInstance == this;
            boolean stopped = true;
            
            if (inProfile || isReallyRunning() || isSuspended()) {
                _stop(ui);
            }
            if (stopped) {
                // restart in the mode the server was running in before
                if (inProfile) {
                    startProfileImpl(null, profilerSettings, true, ui);
                } else if (inDebug) {
                    startDebugTarget(null, ui);
                } else {
                    startTarget(null, ui);
                }
            }
        } finally {
            refresh();
        }
    }
    
    /** Stop admin server. Show UI feedback. 
     *
     * @throws ServerException if the server cannot be stopped.
     */
    public void stop(ProgressUI ui) throws ServerException {
        try {
            setServerState(STATE_WAITING);
            if (profiledServerInstance == this || isReallyRunning() || isSuspended()) {
                _stop(ui);
            }
            debugInfo.clear();
        } finally {
            refresh();
        }
    }
    
    // Note: configuration needs
    /**
     * Return a connected DeploymentManager if needed by server platform for configuration
     * @return DeploymentManager object for configuration.
     */
    public DeploymentManager getDeploymentManagerForConfiguration() throws DeploymentManagerCreationException {
        StartServer ss = getStartServer();
        if (ss != null && ss.needsStartForConfigure()) {
            start();
            return getDeploymentManager();
        } else {
            return getDisconnectedDeploymentManager();
        }
    }
    
    // Note: execution only need these 3 state transition APIs
    
    /**
     * Start specified target server.  If it is also admin server only make sure
     * admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     *
     * @throws ServerException if the target cannot be started.
     */
    public void startTarget(Target target, ProgressUI ui) throws ServerException {
        startTarget(target, ui, false);
    }
    
    /**
     * Start specified target server in debug mode.  If target is also admin
     * server only make sure admin server is running.
     * @param target target server to be started
     * @param ui DeployProgressUI to display start progress
     *
     * @throws ServerException if the server cannot be started.
     */
    public void startDebugTarget(Target target, ProgressUI ui) throws ServerException {
        startTarget(target, ui, true);
        _retrieveDebugInfo(target);
    }
    
    /**
     * Start admin server, mainly for registry actions with no existing progress UI
     *
     * @throws ServerException if the server cannot be started.
     */
    private void start() {
        if (SwingUtilities.isEventDispatchThread()) {
            //PENDING maybe a modal dialog instead of async is needed here
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    start();
                }
            });
            return;
        }
        if (isRunning()) {
            return;
        }
        String title = NbBundle.getMessage(ServerInstance.class, "LBL_StartServerProgressMonitor", getDisplayName());
        ProgressUI ui = new ProgressUI(title, false);
        try {
            ui.start();
            start(ui);
        } catch (ServerException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } finally {
            ui.finish();
        }
    }
    
    /**
     * Start admin server for profiling, mainly for registry actions with no existing progress UI
     * @param settings settings that will be used to start the server
     *
     * @throws ServerException if the server cannot be started.
     */
    public boolean startProfile(final ProfilerServerSettings settings, boolean forceRestart, Deployment.Logger logger) {
        String title = NbBundle.getMessage(ServerInstance.class, "LBL_StartServerInProfileMode", getDisplayName());
        ProgressUI ui = new ProgressUI(title, false, logger);
        try {
            ui.start();
            startProfile(settings, forceRestart, ui);
            return true;
        } catch (ServerException ex) {
            return false;
        } finally {
            ui.finish();
        }
    }
    
    /** Stop the server and do not wait for response.
     * This will be used at IDE exit.
     */
    public void stopDontWait() {
        if (isReallyRunning()) {
            StartServer startServ = getStartServer();
            assert startServ.canStopDeploymentManagerSilently() : "server does not support silent stop of deployment manager";
            startServ.stopDeploymentManagerSilently();
        }
    }
    
    /** see stopDontWait */
    public boolean canStopDontWait() {
        StartServer startServ = getStartServer();
        return startServ.canStopDeploymentManagerSilently();
    }
    
    //------------------------------------------------------------
    /**
     * @throws ServerException if the conflict has not been resolved.
     */
    private void resolveServerConflict(Target target, ProgressUI ui, ConflictData cd) throws ServerException {
        
        ServerInstance si = cd.getServerInstance();
        //inform a user and allow him to stop the running instance
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(
                ServerInstance.class,
                "MSG_AnotherServerRunning",
                new Object[] {
            si.getDisplayName(),
                    cd.getServerDebugInfo().getHost(),
                    cd.getServerDebugInfo().getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                        "socket" : "shared memory",
                    cd.getServerDebugInfo().getTransport().equals(ServerDebugInfo.TRANSPORT_SOCKET) ?
                        new Integer(cd.getServerDebugInfo().getPort()).toString() : cd.getServerDebugInfo().getShmemName()
        }),
                NotifyDescriptor.QUESTION_MESSAGE
                );
        nd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton yes = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopYes"));
        JButton no = new JButton(NbBundle.getMessage(ServerInstance.class, "MSG_AnotherServerStopNo"));
        yes.setDefaultCapable(true);
        nd.setOptions(new Object[] { yes, no });
        Object option = DialogDisplayer.getDefault().notify(nd);
        if (option != yes) { //user pressed No
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ServerStartupCancelled", getDisplayName());
            throw new ServerException(msg);
        }
        //try to stop running server
        if (si.getStartServer().supportsStartDeploymentManager()) {
            si.stop(ui);
        } else {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
            throw new ServerException(msg);
        }
    }
    
    // multiplexor state-machine core
    /**
     * @throws ServerException if the server cannot be started.
     */
    private void startTarget(Target target, ProgressUI ui, boolean debugMode) throws ServerException {
        StartServer ss = getStartServer();
        
        // No StartServer, have to assume manually started
        if (ss == null) {
            ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_PluginHasNoStartServerClass", getServer()));
            return;
        }
        if (isSuspended()) {
            // cannot do anything with the server right now
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ServerSuspended", getServer());
            throw new ServerException(msg);
        }
        boolean canControlAdmin = ss.supportsStartDeploymentManager();
        boolean canDebug = ss.supportsStartDebugging(target);
        boolean needsRestart = ss.needsRestart(target);
        
        if (ss.isAlsoTargetServer(target)) {
            if (debugMode) {
                if (ss.isDebuggable(target)) { // already running in debug mode
                    if (! needsRestart) {
                        return;
                    }
                    if (!canControlAdmin || !canDebug) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_RestartingThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                } else if (isReallyRunning()) { // running but not debuggable
                    if (!canControlAdmin || !canDebug) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_DebugginThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                }                
                // the server is stopped now
                if (!canDebug) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_DebugginThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                // resolve conflicts with other servers
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) { // another server instance with the same parameters
                    resolveServerConflict(target, ui, cd);
                }
                _startDebug(target, ui);
            } else {
                if (isReallyRunning()) { // already running 
                    if (! needsRestart) {
                        return;
                    }
                    if (!canControlAdmin) {
                        String msg = NbBundle.getMessage(ServerInstance.class, "MSG_RestartingThisServerNotSupported", getDisplayName());
                        throw new ServerException(msg);
                    }
                    _stop(ui);
                }
                if (!canControlAdmin) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                _start(ui);
            }
        } else { // not also target server
            // this block ensure a running admin server to control other targets
            if (! isReallyRunning()) {
                if (!canControlAdmin) {
                    String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartingThisServerNotSupported", getDisplayName());
                    throw new ServerException(msg);
                }
                _start(ui);
            }
            if (debugMode) {
                if (ss.isDebuggable(target)) {
                    if ( ! needsRestart) {
                        return;
                    }
                    _stop(target, ui);
                } else if (ss.isRunning(target)) {
                    _stop(target, ui);
                }
                ConflictData cd = anotherServerDebuggable(target);
                if (cd != null) { //another server instance with the same parameters
                    resolveServerConflict(target, ui, cd);
                }
                _startDebug(target, ui);
            } else {
                if (ss.isRunning(target)) {
                    if (! needsRestart) {
                        return;
                    }
                    _stop(target, ui);
                }
                _start(target, ui);
            }
        }
    }
    
    //------------------------------------------------------------
    // state-transition atomic operations (always do-it w/o checking state)
    //------------------------------------------------------------
    // startDeploymentManager
    private void _start(ProgressUI ui) throws ServerException {
        ProgressObject po = getStartServer().startDeploymentManager();
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getStartupTimeout());
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", getDisplayName());
            throw new ServerException(msg);
        }
        synchronized (this) {
            managerStartedByIde = true;
            coTarget = null;
            targets = null;
            initCoTarget();
        }
    }
    
    // startDebugging
    private void _startDebug(Target target, ProgressUI ui) throws ServerException {
        ProgressObject po = getStartServer().startDebugging(target);
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getStartupTimeout());
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartDebugTimeout", getDisplayName());
            throw new ServerException(msg);
        }
        synchronized (this) {
            managerStartedByIde = true;
            coTarget = null;
            targets = null;
            initCoTarget();
        }
    }
    
    /** start server in the profile mode */
    private void startProfileImpl(
                                    Target target, 
                                    ProfilerServerSettings settings,
                                    boolean forceRestart,
                                    ProgressUI ui) throws ServerException {
        ProfilerServerSettings  tmpProfilerSettings;
        synchronized (this) {
            tmpProfilerSettings = profilerSettings;
        }
        ServerInstance tmpProfiledServerInstance = profiledServerInstance;
        if (tmpProfiledServerInstance == this && !forceRestart && settings.equals(tmpProfilerSettings)) {
            return; // server is already runnning in profile mode, no need to restart the server
        }
        if (tmpProfiledServerInstance != null && tmpProfiledServerInstance != this) {
            // another server currently running in profiler mode
            tmpProfiledServerInstance.stop(ui);
            profiledServerInstance = null;
        }
        if (profiledServerInstance == this || isReallyRunning() || isDebuggable(target)) {
            _stop(ui);
            debugInfo.clear();
            profiledServerInstance = null;
        }
        
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler == null) {
            // this should not occur, but better make sure
            throw new ServerException(NbBundle.getMessage(ServerInstance.class, "MSG_ProfilerNotRegistered"));
        }
        profiler.notifyStarting();
        ProgressObject po = getStartServer().startProfiling(target, settings);
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, DEFAULT_TIMEOUT);
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartProfileTimeout", getDisplayName());
            throw new ServerException(msg);
        }
        profiledServerInstance = this;
        synchronized (this) {
            profilerSettings = settings;
            managerStartedByIde = true;
        }
    }
    
    /** Tell the profiler to shutdown */
    private void shutdownProfiler(ProgressUI ui) throws ServerException {
        ui.progress(NbBundle.getMessage(ServerInstance.class, "MSG_StoppingProfiler"));
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler != null) {
            ProgressObject po = profiler.shutdown();
            try {
                boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getShutdownTimeout());
                if (!completedSuccessfully) {
                    throw new ServerException(po.getDeploymentStatus().getMessage());
                }
            } catch (TimedOutException e) {
                String msg = NbBundle.getMessage(ServerInstance.class, "MSG_ProfilerShutdownTimeout");
                throw new ServerException(msg);
            }
        }
    }
    
    // stopDeploymentManager
    private void _stop(ProgressUI ui) throws ServerException {
        // if the server is started in profile mode, deattach profiler first
        if (profiledServerInstance == this) {
            shutdownProfiler(ui);
            profiledServerInstance = null;
        }
        synchronized (this) {
            // if the server is suspended, the debug session has to be terminated first
            if (isSuspended()) {
                Target target = _retrieveTarget(null);
                ServerDebugInfo sdi = getServerDebugInfo(target);
                Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
                for (int i = 0; i < sessions.length; i++) {
                    Session s = sessions[i];
                    if (s != null) {
                        AttachingDICookie attCookie = (AttachingDICookie)s.lookupFirst(null, AttachingDICookie.class);
                        if (attCookie != null) {
                            if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                                String shmem = attCookie.getSharedMemoryName();
                                if (shmem != null && shmem.equalsIgnoreCase(sdi.getShmemName())) {
                                    s.kill();
                                }
                            } else {
                                String host = attCookie.getHostName();
                                if (host != null && isSameHost(host, sdi.getHost())
                                        && attCookie.getPortNumber() == sdi.getPort()) {
                                    s.kill();
                                }
                            }
                        }
                    }
                }
            }
        }
        ProgressObject po = getStartServer().stopDeploymentManager();
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getShutdownTimeout());
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", getDisplayName());
            throw new ServerException(msg);
        }
        synchronized (this) {
            managerStartedByIde = false;
        }
        reset();
    }
    
    private void _start(Target target, ProgressUI ui) throws ServerException {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning()) {
            return;
        }
        ProgressObject po = serverTarget.start();
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getStartupTimeout());
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StartServerTimeout", target.getName());
            throw new ServerException(msg);
        }
        targetsStartedByIde.add(serverTarget.getName());
    }
    
    private void _stop(Target target, ProgressUI ui) throws ServerException {
        ServerTarget serverTarget = getServerTarget(target.getName());
        if (serverTarget.isRunning()) {
            return;
        }
        ProgressObject po = serverTarget.stop();
        try {
            boolean completedSuccessfully = ProgressObjectUtil.trackProgressObject(ui, po, getShutdownTimeout());
            if (!completedSuccessfully) {
                throw new ServerException(po.getDeploymentStatus().getMessage());
            }
        } catch (TimedOutException e) {
            String msg = NbBundle.getMessage(ServerInstance.class, "MSG_StopServerTimeout", target.getName());
            throw new ServerException(msg);
        }
        targetsStartedByIde.remove(serverTarget.getName());
    }
    
    //-------------- End state-machine operations -------------------
    
    public boolean canStartServer() {
        return this.getStartServer() != null && getStartServer().supportsStartDeploymentManager();
    }
    
    public boolean isManagerOf(Target target) {
        return getTargetMap().keySet().contains(target.getName());
    }
    
    public synchronized ServerTarget getCoTarget() {
        return coTarget;
    }
    
    private void initCoTarget() {
        ServerTarget[] childs = getTargets();
        for (int i=0; i<childs.length; i++) {
            if (getStartServer().isAlsoTargetServer(childs[i].getTarget())) {
                synchronized (this) {
                    coTarget = childs[i];
                }
            }
        }
    }
    
    public boolean isDefault() {
        return url.equals(ServerRegistry.getInstance().getDefaultInstance().getUrl());
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public static boolean isProgressing(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.RUNNING || state == StateType.RELEASED);
    }
    public static boolean hasFailed(ProgressObject po) {
        StateType state = po.getDeploymentStatus().getState();
        return (state == StateType.FAILED);
    }
    
    // StateListener ----------------------------------------------------------
    
    /** Listener that allows to listen to server state changes */
    public static interface StateListener {
        void stateChanged(int oldState, int newState);
    }
    
    public void addStateListener(StateListener sl) {
        synchronized (stateListeners) {
            stateListeners.add(sl);
        }
    }
    
    public void removeStateListener(StateListener sl) {
        synchronized (stateListeners) {
            stateListeners.remove(sl);
        }
    }
    
    private void fireStateChanged(int oldState, int newState) {
        StateListener[] listeners;
        synchronized (stateListeners) {
            listeners = (StateListener[])stateListeners.toArray(new StateListener[stateListeners.size()]);
        }
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].stateChanged(oldState, newState);
        }
    }
    
    // private helper classes & methods ---------------------------------------
    
    private static class ConflictData {
        private ServerInstance si;
        private ServerDebugInfo sdi;
        
        public ConflictData(ServerInstance si, ServerDebugInfo sdi) {
            this.si = si;
            this.sdi = sdi;
        }
        
        public ServerInstance getServerInstance() { 
            return si; 
        }
        
        public ServerDebugInfo getServerDebugInfo() { 
            return sdi; 
        }
    };
    
    /** Safe true/false test useful. */
    private abstract static class SafeTrueTest implements Runnable {
        protected boolean result = false;
        
        public abstract void run();
        
        public final boolean result() {
            return result;
        }
    };
    
    /** Return the result of the test or false if the given time-out ran out. */
    private boolean safeTrueTest(SafeTrueTest test, long timeout) {
        try {
           new RequestProcessor().post(test).waitFinished(timeout);
        } catch (InterruptedException ie) {
            Exceptions.printStackTrace(ie);
        } finally {
            return test.result();
        }
    }
    
    private ServerDebugInfo _retrieveDebugInfo(Target target) {
        StartServer ss = getStartServer();
        if (ss == null) {
            return null;
        }
        
        Target t = _retrieveTarget(target);
        ServerDebugInfo sdi = ss.getDebugInfo(t);
        
        if (sdi != null || t != null) {
            debugInfo.remove(t);
            debugInfo.put(t, sdi);//cache debug info for given target
        }
        
        return sdi;
    }
    
    private Target _retrieveTarget(Target target) {
        StartServer ss = getStartServer();
        if (ss == null) {
            return null;
        }
        
        Target t = null;
        
        // Getting targets from AS8.1 requires start server which would hang UI, so avoid start server
        if (! isReallyRunning() && ss.needsStartForTargetList()) {
            if (t == null) {
                for (Iterator it = debugInfo.keySet().iterator(); t == null && it.hasNext(); ) {
                    Target cachedTarget = (Target) it.next();
                    if (ss.isAlsoTargetServer(cachedTarget)) {
                        t = cachedTarget;
                    }
                }
            } else {
                if (ss.isAlsoTargetServer(target))
                    t = target;
            }
        } else {
            ServerTarget[] targets = getTargets();
            for (int i = 0; t == null && i < targets.length; i++) {
                if (ss.isAlsoTargetServer(targets[i].getTarget())) {
                    t = targets[i].getTarget();
                }
            }
            
            if (t == null && targets.length > 0) {
                t = targets[0].getTarget();
            }
        }
        
        return t;
    }

    public int compareTo(Object other) {
        if (!(other instanceof ServerInstance)) {
            throw new IllegalArgumentException();
        }
        return getDisplayName().compareTo(((ServerInstance)other).getDisplayName());
    }

    /**
     * Tests whether two hostnames points to the same machine. If the method
     * returns <code>true</code> the hostnames are equivalent. If the method
     * returns <code>false</code> the equivalency could not be proven.
     *
     * @param firstName the first hostname to compare, <code>null</code> implies
     *             localhost
     * @param secondName the second hostname to compare, <code>null</code> implies
     *             localhost
     * @return <code>true</code> if given hostnames are equivalent,
     *             <code>false</code> if they are not or can't determine
     */
    // TODO make this some general purpose method (j2ee server common?)
    private boolean isSameHost(String firstName, String secondName) {
        if (firstName != null && firstName.equals(secondName)) {
            return true;
        }

        // check the well known localhost equality
        if (("127.0.0.1".equals(firstName) || "localhost".equals(firstName)) // NOI18N
                && ("127.0.0.1".equals(secondName) || "localhost".equals(secondName))) { // NOI18N
            return true;
        }

        // check for both of them are localhost adresses
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            for (InetAddress firstAddress : InetAddress.getAllByName(firstName)) {
                if (localhost.equals(firstAddress)
                        || "127.0.0.1".equals(firstName) || "localhost".equals(firstName)) { // NOI18N

                    for (InetAddress secondAddress : InetAddress.getAllByName(secondName)) {
                        if (localhost.equals(secondAddress)
                                || "127.0.0.1".equals(secondName) || "localhost".equals(secondName)) { // NOI18N
                            return true;
                        }
                    }
                    break;
                }
            }
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        // check that they are the same adresses or canonical names
        try {
            for (InetAddress firstAddress : InetAddress.getAllByName(firstName)) {
                for (InetAddress secondAddress : InetAddress.getAllByName(secondName)) {
                    if (firstAddress.equals(secondAddress)
                            || firstAddress.getCanonicalHostName().equals(secondAddress.getCanonicalHostName())) {
                        return true;
                    }
                }
            }
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        // check that they both have same local interface
        try {
            NetworkInterface firstInterface = NetworkInterface.getByInetAddress(
                    InetAddress.getByName(firstName));
            NetworkInterface secondInterface = NetworkInterface.getByInetAddress(
                    InetAddress.getByName(secondName));

            if (firstInterface != null && firstInterface.equals(secondInterface)) {
                return true;
            }
        } catch (SocketException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        return false;
    }

    /** DebugStatusListener listens to debugger state changes and calls refresh() 
     *  if needed. If the debugger stops at a breakpoint, the server status will
     *  thus change to suspended, etc. */
    private class DebuggerStateListener extends DebuggerManagerAdapter {
            
            private RequestProcessor.Task refreshTask;
            
            public void sessionAdded(Session session) {
                Target target = _retrieveTarget(null);
                ServerDebugInfo sdi = getServerDebugInfo(target);
                if (sdi == null) {
                    LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: " + ServerInstance.this);
                    return; // give it up
                }
                AttachingDICookie attCookie = (AttachingDICookie)session.lookupFirst(null, AttachingDICookie.class);
                if (attCookie == null) {
                    LOGGER.log(Level.INFO, "AttachingDICookie cannot be found for: " + ServerInstance.this);
                    return; // give it up
                }
                if (ServerDebugInfo.TRANSPORT_SHMEM.equals(sdi.getTransport())) {
                    String shmem = attCookie.getSharedMemoryName();
                    if (shmem != null && shmem.equalsIgnoreCase(sdi.getShmemName())) {
                        registerListener(session);
                    }
                } else {
                    String host = attCookie.getHostName();
                    if (host != null && isSameHost(host, sdi.getHost())
                            && attCookie.getPortNumber() == sdi.getPort()) {
                        registerListener(session);
                    }
                }
            }
            
            public synchronized void sessionRemoved(Session session) {
                refreshTask = null;
            }
            
            private void registerListener(Session session) {
                final JPDADebugger jpda = (JPDADebugger)session.lookupFirst(null, JPDADebugger.class);
                if (jpda != null) {
                    jpda.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            RequestProcessor.Task task; 
                            synchronized (DebuggerStateListener.this) {
                                if (refreshTask == null) {
                                    refreshTask = RequestProcessor.getDefault().create(new Runnable() {
                                        public void run() {
                                            if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                                setServerState(ServerInstance.STATE_SUSPENDED);
                                            } else {
                                                setServerState(ServerInstance.STATE_DEBUGGING);
                                            }
                                        }
                                    });
                                }
                                task = refreshTask;
                            }
                            // group fast arriving refresh calls
                            task.schedule(500);
                        }
                    });
                }
            }
        }
}
