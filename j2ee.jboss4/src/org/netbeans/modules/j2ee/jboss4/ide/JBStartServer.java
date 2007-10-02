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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.jboss4.ide;

import java.util.Collections;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import java.io.IOException;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.util.RequestProcessor;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;

/**
 *
 * @author Kirill Sorokin
 */
public class JBStartServer extends StartServer implements ProgressObject{
    
    static enum MODE { RUN, DEBUG, PROFILE };
    
    static enum ACTION_STATUS { SUCCESS, FAILURE, UNKNOWN };
    
    private static final int AVERAGE_SERVER_INSTANCES = 2;
    
    private MODE mode;
    
    private final JBDeploymentManager dm;

    private static Set<String> isDebugModeUri = Collections.synchronizedSet(
            new HashSet<String>(AVERAGE_SERVER_INSTANCES));
    
    public JBStartServer(DeploymentManager dm) {
        if (!(dm instanceof JBDeploymentManager)) {
            throw new IllegalArgumentException("Not an instance of JBDeploymentManager"); // NOI18N
        }
        this.dm = (JBDeploymentManager) dm;
    }
    
    private void addDebugModeUri() {
        isDebugModeUri.add(dm.getUrl());
    }
    
    private void removeDebugModeUri() {
        isDebugModeUri.remove(dm.getUrl());
    }
    
    private boolean existsDebugModeUri() {
        return isDebugModeUri.contains(dm.getUrl());
    }
    
    public ProgressObject startDebugging(Target target) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.DEBUG;
        RequestProcessor.getDefault().post(new JBStartRunnable(null, dm, this), 0, Thread.NORM_PRIORITY);
        addDebugModeUri();
        return this;
    }
    
    public boolean isDebuggable(Target target) {
        if (!existsDebugModeUri()) {
            return false;
        }
        if (!isRunning()) {
            return false;
        }
        return true;
    }
    
    public boolean supportsStartDebugging(Target target) {
        return true;
    }
    
    public boolean supportsStartProfiling(Target target) {
        return true;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo("localhost", dm.getDebuggingPort());
    }
    
    /**
     * Starts the server in profiling mode.
     */
    public ProgressObject startProfiling(Target target, ProfilerServerSettings settings) {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_PROFILED_SERVER_IN_PROGRESS", serverName))); //NOI18N
        mode = MODE.PROFILE;
        RequestProcessor.getDefault().post(new JBStartRunnable(settings, dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    
    /**
     * Indicates whether this server supports start/stop.
     *
     * @return true/false - supports/does not support
     */
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    /**
     * Stops the server.
     */
    public ProgressObject stopDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
        RequestProcessor.getDefault().post(new JBStopRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    /**
     * Starts the server
     */
    public ProgressObject startDeploymentManager() {
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, StateType.RUNNING, NbBundle.getMessage(JBStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)));//NOI18N
        mode = MODE.RUN;
        RequestProcessor.getDefault().post(new JBStartRunnable(null, dm, this), 0, Thread.NORM_PRIORITY);
        removeDebugModeUri();
        return this;
    }
    
    
    public boolean needsStartForTargetList() {
        return false;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public boolean needsStartForAdminConfig() {
        return false;
    }
    
    private boolean isReallyRunning(){
        final InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        // this should prevent the thread from getting stuck if the server is in suspended state
        SafeTrueTest test = new SafeTrueTest() {
            public void run() {
                String checkingConfigName = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
                String checkingServerDir = null;

                try {
                    String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);

                    if (serverDir == null) {
                        result = false;
                        return;
                    }
                    
                    checkingServerDir = new File(serverDir).getCanonicalPath();
                } catch (IllegalStateException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                    result = false;
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                    result = false;
                }

                Object serverName = Util.getMBeanParameter(dm, "ServerName", "jboss.system:type=ServerConfig");
                Object serverHome = Util.getMBeanParameter(dm, "ServerHomeDir", "jboss.system:type=ServerConfig");
                
                if(serverName == null || serverHome == null) {
                    result = false;
                    return;
                }
                
                serverHome = ((File)serverHome).getAbsolutePath();
                
                if (checkingConfigName.equals(serverName) && checkingServerDir.equals(serverHome))
                    result = true;
            }
        };
        
        return safeTrueTest(test, 10000);
    }
    
    /** Safe true/false test useful. */
    private abstract static class SafeTrueTest implements Runnable {
        protected boolean result = false;
        
        public abstract void run();
        
        public final boolean result() {
            return result;
        }
    };
    
    /** Return the result of the test or false if the given time-out ran out. */
    private boolean safeTrueTest(SafeTrueTest test, int timeout) {
        try {
            new RequestProcessor().post(test).waitFinished(timeout);
        } catch (InterruptedException ie) {
            // no op
        }
        return test.result();
    }
    
    public boolean isRunning() {
        
        InstanceProperties ip = dm.getInstanceProperties();
        if (ip == null) {
            return false; // finish, it looks like this server instance has been unregistered
        }
        
        if (!isReallyRunning()){
            dm.setRunningLastCheck(ip, Boolean.FALSE);
            return false;
        }
        
        dm.setRunningLastCheck(ip, Boolean.TRUE);
        return true;
    }
    
    // ----------  Implementation of ProgressObject interface
    private Vector listeners = new Vector();
    private DeploymentStatus deploymentStatus;
    
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }
    
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isStopSupported() {
        return false;
    }
    
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{};
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);
        
        this.deploymentStatus = deploymentStatus;
        
        java.util.Vector targets = null;
        synchronized (this) {
            if (listeners != null) {
                targets = (java.util.Vector) listeners.clone();
            }
        }
        
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                ProgressListener target = (ProgressListener)targets.elementAt(i);
                target.handleProgressEvent(evt);
            }
        }
    }

    MODE getMode() {
        return mode;
    }
    
    
}
