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

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressMonitor;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.*;
import org.apache.tools.ant.Project;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;

/**
 * Ant task that starts the server if needed and deploys module to the server
 * @author Martin Grebac
 */
public class Deploy extends Task {
    
    static final int MAX_DEPLOY_PROGRESS = 5;

    /**
     * Holds value of property debugmode.
     */
    private boolean debugmode = false;
    
    /**
     * Holds value of property clientUrl.
     */
    private String clientUrlPart;

    private boolean alsoStartTargets = true;    //TODO - make it a property? is it really needed?
    
    public void execute() throws BuildException { 

        J2eeDeploymentLookup jdl = null;
        try {
            FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
            fob.refresh(); // without this the "build" directory is not found in filesystems
            jdl = (J2eeDeploymentLookup) FileOwnerQuery.getOwner(fob).getLookup().lookup(J2eeDeploymentLookup.class);
        } catch (Exception e) {
            throw new BuildException(e);
        }

        J2eeProfileSettings settings = jdl.getJ2eeProfileSettings();
        DeploymentTargetImpl target = new DeploymentTargetImpl(settings, jdl);

        ServerString server = target.getServer();
        J2eeModule module = target.getModule();
        TargetModule[] modules = null;
        DeployProgressUI progress = new DeployProgressMonitor(false, true);  // modeless with stop/cancel buttons
        progress.startProgressUI(MAX_DEPLOY_PROGRESS);
        
        try {
            if (module == null) {
                progress.addError(getBundle("MSG_NoJ2eeModule"));
                throw new BuildException(getBundle("MSG_NoJ2eeModule"));
            }
            if (server == null || server.getServerInstance() == null) {
                progress.addError(getBundle("MSG_NoTargetServer"));
                throw new BuildException(getBundle("MSG_NoTargetServer"));
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
                progress.addError(getBundle("MSG_StartServerFailed"));
                throw new BuildException(getBundle("MSG_StartServerFailed"));
            }
            
            progress.recordWork(2);
            modules = targetserver.deploy(progress);
            progress.recordWork(MAX_DEPLOY_PROGRESS-1);
            
        } catch (Exception ex) {
            throw new BuildException(getBundle("MSG_DeployFailed"));
        }
        
        if (modules != null && modules.length > 0) {
            target.setTargetModules(modules);
            progress.recordWork(MAX_DEPLOY_PROGRESS);
        } else {
            throw new BuildException("Some other error.");
        }

        String clientUrl = target.getClientUrl(getClientUrlPart());
        getProject().setProperty("client.url", clientUrl);
        
        if (debugmode) {
            Target t = null;
            Target[] targs = server.toTargets();
            if (targs != null && targs.length > 0) {
                t = targs[0];
            }
            
            ServerDebugInfo sdi = server.getServerInstance().getStartServer().getDebugInfo(t);
            if (sdi == null) {
                throw new BuildException("Error retrieving debug info from server");
            }
            String h = sdi.getHost();
            String transport = sdi.getTransport();
            String address = "";                                                //NOI18N
            
            if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                address = sdi.getShmemName();
            } else {
                address = Integer.toString(sdi.getPort());
            }
            
            getProject().setProperty("jpda.transport", transport);
            getProject().setProperty("jpda.host", h);
            getProject().setProperty("jpda.address", address);
        }
        
    }

    private String getBundle(String key) {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ant/Bundle").getString(key); // NOI18N
    }
    
    /**
     * Getter for property debugmode.
     * @return Value of property debugmode.
     */
    public boolean getDebugmode() {
        return this.debugmode;
    }
    
    /**
     * Setter for property debugmode.
     * @param debugmode New value of property debugmode.
     */
    public void setDebugmode(boolean debugmode) {
        this.debugmode = debugmode;
    }
        
    /**
     * Getter for property clientUrl.
     * @return Value of property clientUrl.
     */
    public String getClientUrlPart() {
        return this.clientUrlPart;
    }
    
    /**
     * Setter for property clientUrl.
     * @param clientUrl New value of property clientUrl.
     */
    public void setClientUrlPart(String clientUrlPart) {
        this.clientUrlPart = clientUrlPart;
    }
    
}
