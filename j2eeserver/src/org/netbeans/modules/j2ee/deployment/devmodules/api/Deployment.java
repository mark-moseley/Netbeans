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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek
 */
public final class Deployment {
    
    private static final int MAX_DEPLOY_PROGRESS = 5;

    private static boolean alsoStartTargets = true;    //TODO - make it a property? is it really needed?
    
    private static Deployment instance = null;

    private Map progressMonitors = new WeakHashMap();

    public static synchronized Deployment getDefault () {
        if (instance == null) {
            instance = new Deployment ();
        }
        return instance;
    }
    
    private Deployment () {
    }
    
    /** Deploys a web J2EE module to server.
     * @param clientModuleUrl URL of module within a J2EE Application that 
     * should be used as a client (can be null for standalone modules)
     * <div class="nonnormative">
     * <p>Note: if null for J2EE application the first web or client module will be used.</p>
     * </div>
     * @return complete URL to be displayed in browser (server part plus the client module and/or client part provided as a parameter)
     */
    public String deploy (J2eeModuleProvider jmp, boolean debugmode, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy) throws DeploymentException {
        return deploy(jmp, debugmode, clientModuleUrl, clientUrlPart, forceRedeploy, null);
    }
    
    public String deploy (J2eeModuleProvider jmp, boolean debugmode, String clientModuleUrl, String clientUrlPart, boolean forceRedeploy, Logger logger) throws DeploymentException {
        DeploymentTargetImpl target = new DeploymentTargetImpl(jmp, clientModuleUrl);

        String err;
        TargetModule[] modules = null;
        final J2eeModule module = target.getModule();
        DeployProgressMonitor progress = null;
        
        synchronized (progressMonitors) {
            progress = (DeployProgressMonitor) progressMonitors.get(module);    

            if (progress != null) {
                progress.setLogger(logger);
                if (progress.getUI() != null) {
                    progress.getUI().toFront();
                }
            } else {
                progress = new DeployProgressMonitor(false, true, logger);  // modeless with stop/cancel buttons
                progressMonitors.put(module, progress);
                progress.startProgressUI(MAX_DEPLOY_PROGRESS);
                progress.getUI().addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent evt) {
                        progressMonitors.remove(module);
                    }
                });
            }
        }
        
        try {
            ServerString server = target.getServer(); //will throw exception if bad server id
        
            if (module == null) {
                err = NbBundle.getMessage (Deployment.class, "MSG_NoJ2eeModule"); //NOI18N
                progress.addError(err);
                throw new DeploymentException (err);
            }
            if (server == null || server.getServerInstance() == null) {
                err = NbBundle.getMessage (Deployment.class, "MSG_NoTargetServer"); //NOI18N
                progress.addError(err);
                throw new DeploymentException (err);
            }
            
            progress.recordWork(1);
            
            boolean serverReady = false;
            TargetServer targetserver = new TargetServer(target);

            if (alsoStartTargets || debugmode) {
                serverReady = targetserver.startTargets(debugmode, progress);
            } else { //PENDING: how do we know whether target does not need to start when deploy only
                serverReady = server.getServerInstance().start(progress);
            }
            if (! serverReady) {
                err = NbBundle.getMessage (Deployment.class, "MSG_StartServerFailed", target.getServer ().getServerInstance ().getDisplayName ()); //NOI18N
                progress.addError(err);
                throw new DeploymentException (err);
            }
            
            progress.recordWork(2);
            modules = targetserver.deploy(progress, forceRedeploy);
            // inform the plugin about the deploy action, even if there was
            // really nothing needed to be deployed
            targetserver.notifyIncrementalDeployment(modules);
            progress.recordWork(MAX_DEPLOY_PROGRESS-1);
            
        } catch (Exception ex) {            
            err = NbBundle.getMessage (Deployment.class, "MSG_DeployFailed", ex.getLocalizedMessage ());
            progress.addError(err);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            throw new DeploymentException (err, ex);
        } finally {
            if (progress != null)
                progress.setLogger(null);
        }
        
        try {
            if (modules != null && modules.length > 0) {
                target.setTargetModules(modules);
                progress.recordWork(MAX_DEPLOY_PROGRESS);
            } else {
                err = NbBundle.getMessage (Deployment.class, "MSG_AnotherError");
                throw new DeploymentException (err);
            }
            return target.getClientUrl(clientUrlPart);
        } finally {
            if (progress != null)
                progress.setLogger(null);
        }
    }
    
    public static final class DeploymentException extends Exception {
        private DeploymentException (String msg) {
            super (msg);
        }
        private DeploymentException (Throwable t) {
            super (t);
        }
        private DeploymentException (String s, Throwable t) {
            super (s, t);
        }
    }
    
    public String [] getServerInstanceIDs () {
        return InstanceProperties.getInstanceList ();
    }
    
    public String getServerInstanceDisplayName (String id) {
        return ServerRegistry.getInstance ().getServerInstance (id).getDisplayName ();
    }
    
    public String getServerID (String instanceId) {
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(instanceId);
        if (si != null) {
            return si.getServer().getShortName();
        }
        return null;
    }
    
    public String getDefaultServerInstanceID () {
        ServerString defInst = ServerRegistry.getInstance ().getDefaultInstance ();
        if (defInst != null) {
            ServerInstance si = defInst.getServerInstance();
            if (si != null) {
                return si.getUrl ();
            }
        }
        return null;
    }
    
    public String [] getInstancesOfServer (String id) {
        if (id == null  || ServerRegistry.getInstance ().getServer (id) == null) 
            return getServerInstanceIDs();
        
        ServerInstance sis [] = ServerRegistry.getInstance ().getServer (id).getInstances ();
        String ids [] = new String [sis.length];
        for (int i = 0; i < sis.length; i++) {
            ids [i] = sis [i].getUrl ();
        }
        return ids;
    }
    
    public String [] getServerIDs () {
        Collection c = ServerRegistry.getInstance ().getServers ();
        String ids [] = new String [c.size ()];
        Iterator iter = c.iterator ();
        for (int i = 0; i < c.size (); i++) {
            Server s = (Server) iter.next ();
            ids [i] = s.getShortName ();
        }
        return ids;
    }
    
    /**
     * Return server instance's <code>J2eePlatform</code>.
     *
     * @param  serverInstanceID server instance ID.
     * @return <code>J2eePlatform</code> for the given server instance, <code>null</code> if
     *         server instance of the specified ID does not exist.
     * @since 1.5
     */
    public J2eePlatform getJ2eePlatform(String serverInstanceID) {
        ServerInstance serInst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        if (serInst == null) return null;
        J2eePlatform result = serInst.getJ2eePlatform();
        if (result == null) {
            J2eePlatformImpl platformImpl = serInst.getJ2eePlatformImpl();
            if (platformImpl != null) {
                result = new J2eePlatform(platformImpl);
                serInst.setJ2eePlatform(result);
            }
        }
        return result;
    }
    
    public String getServerDisplayName (String id) {
        return ServerRegistry.getInstance ().getServer (id).getShortName ();
    }
    
    public static interface Logger {
        public void log(String message);
    }
}
