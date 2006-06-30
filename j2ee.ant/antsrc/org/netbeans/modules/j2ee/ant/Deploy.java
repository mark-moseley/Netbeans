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

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.openide.util.Lookup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.*;
import org.apache.tools.ant.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;

/**
 * Ant task that starts the server if needed and deploys module to the server
 * @author Martin Grebac
 */
public class Deploy extends Task implements Deployment.Logger {
    
    /**
     * Holds value of property debugmode.
     */
    private boolean debugmode = false;
    
    private boolean forceRedeploy = false;
    
    /**
     *  URI of the web client or rich client in J2EE application to execute after deployment.
     */
    private String clientModuleUri;
    
    /**
     * Part to build returned property client.url
     */
    private String clientUrlPart;

    
    public void execute() throws BuildException { 
        
        ClassLoader originalLoader = null;        
        
        try {
            // see issue #62448
            ClassLoader current = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            if (current == null) {
                current = ClassLoader.getSystemClassLoader();
            }
            if (current != null) {
                originalLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(current);
            }

            J2eeModuleProvider jmp = null;
            try {
                FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
                fob.refresh(); // without this the "build" directory is not found in filesystems
                jmp = (J2eeModuleProvider) FileOwnerQuery.getOwner(fob).getLookup().lookup(J2eeModuleProvider.class);
            } catch (Exception e) {
                throw new BuildException(e);
            }

            try {
                String clientUrl = Deployment.getDefault ().deploy (jmp, debugmode, clientModuleUri, clientUrlPart, forceRedeploy, this);
                if (clientUrl != null) {
                    getProject().setProperty("client.url", clientUrl);
                }

                ServerDebugInfo sdi = jmp.getServerDebugInfo();

                if (sdi != null) { //fix for bug 57854, this can be null
                    String h = sdi.getHost();
                    String transport = sdi.getTransport();
                    String address = "";   //NOI18N

                    if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        address = sdi.getShmemName();
                    } else {
                        address = Integer.toString(sdi.getPort());
                    }

                    getProject().setProperty("jpda.transport", transport);
                    getProject().setProperty("jpda.host", h);
                    getProject().setProperty("jpda.address", address);
                }
            } catch (Exception ex) {
                throw new BuildException(ex);
            }
        } finally {
            if (originalLoader != null) {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
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
        
    public boolean getForceRedeploy() {
        return this.forceRedeploy;
    }
    
    public void setForceRedeploy(boolean forceRedeploy) {
        this.forceRedeploy = forceRedeploy;
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
    
    /**
     * Get/setter for task parameter 'clientUri'
     */
    public String getClientModuleUri() {
        return this.clientModuleUri;
    }
    
    public void setClientModuleUri(String clientModuleUri) {
        this.clientModuleUri = clientModuleUri;
    }
}
