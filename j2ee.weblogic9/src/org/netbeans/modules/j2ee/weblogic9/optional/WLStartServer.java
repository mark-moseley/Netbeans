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
package org.netbeans.modules.j2ee.weblogic9.optional;

import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import org.netbeans.modules.j2ee.weblogic9.util.WLTailer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.openide.*;
import org.openide.util.*;
import org.openide.windows.*;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentManager;

/**
 *
 * @author Kirill Sorokin
 */
public class WLStartServer extends StartServer {
    
    private WLDeploymentManager dm;
    
    public WLStartServer(DeploymentManager dm) {
        this.dm = (WLDeploymentManager) dm;
    }
    
    public ProgressObject startDebugging(Target target) {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "starting server DEBUGGGGG!");
        
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        serverProgress.notifyStart(StateType.RUNNING,  ""); // NOI18N
        
        RequestProcessor.getDefault().post(new WLStartDebugRunnable(serverProgress), 0, Thread.NORM_PRIORITY);
        
        isDebuggable = true;
        
        return serverProgress;
    }
    
    private boolean isDebuggable;
    
    public boolean isDebuggable(Target target) {
        return isDebuggable;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(dm.getHost(), new Integer(dm.getInstanceProperties().getProperty(WLDeploymentFactory.DEBUGGER_PORT_ATTR)).intValue());
    }
    
    public boolean supportsStartDeploymentManager() {
        if (dm.getInstanceProperties().getProperty(WLDeploymentFactory.IS_LOCAL_ATTR).equals("true")) { // NOI18N
            return true;
        } else {
            return false;
        }
    }
    
    public ProgressObject stopDeploymentManager() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "stopping server!");
        
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        serverProgress.notifyStop(StateType.RUNNING, ""); // NOI18N
        
        RequestProcessor.getDefault().post(new WLStopRunnable(serverProgress), 0, Thread.NORM_PRIORITY);
        
        isDebuggable = false;
        
        return serverProgress;
    }
    
    public ProgressObject startDeploymentManager() {
        if (WLDebug.isEnabled())
            WLDebug.notify(getClass(), "starting server!");
        
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        serverProgress.notifyStart(StateType.RUNNING,  ""); // NOI18N
        
        RequestProcessor.getDefault().post(new WLStartRunnable(serverProgress), 0, Thread.NORM_PRIORITY);
        
        isDebuggable = false;
        
        return serverProgress;
    }
    
    public boolean needsStartForTargetList() {
        return true;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    public boolean isRunning() {
        try {
            new Socket(dm.getHost(), new Integer(dm.getPort()).intValue());
            return true;
        } catch (UnknownHostException e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
        } catch (IOException e) {
            // do nothing this exception means that the server is 
            // not started
        }
        
        return false;
    }
    
    private static String[] properties2StringArray(Properties properties) {
        List list = new ArrayList();
        
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            list.add(key + "=" + properties.getProperty(key));
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }
    
    private class WLStartRunnable implements Runnable {
        
        private String domainHome;
        
        private WLServerProgress serverProgress;
        
        public WLStartRunnable(WLServerProgress serverProgress) {
            this.serverProgress = serverProgress;
            
            domainHome = dm.getInstanceProperties().getProperty(WLDeploymentFactory.DOMAIN_ROOT_ATTR);
        }
        
        public void run() {
            try {
                long start = System.currentTimeMillis();
                
                Process serverProcess = Runtime.getRuntime().exec(domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH)); // NOI18N
                
                new WLTailer(serverProcess.getInputStream(), NbBundle.getMessage(WLStartServer.class, "TXT_ioWindowTitle")).start(); // NOI18N
                
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (!isRunning()) {
                        serverProgress.notifyStart(StateType.RUNNING, ""); // NOI18N
                    } else {
                        serverProgress.notifyStart(StateType.COMPLETED, ""); // NOI18N
                        return;
                    }

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }

                serverProgress.notifyStart(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "")); // NOI18N
                serverProcess.destroy();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
        
        private static final int TIMEOUT = 120000;
        private static final int DELAY = 5000;
        
        private static final String STARTUP_SH = "startWebLogic.sh"; // NOI18N
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N
    }
    
    private class WLStartDebugRunnable implements Runnable {
        
        private String domainHome;
        private String debuggerPort;
        
        private WLServerProgress serverProgress;
        
        public WLStartDebugRunnable(WLServerProgress serverProgress) {
            this.serverProgress = serverProgress;
            
            domainHome = dm.getInstanceProperties().getProperty(WLDeploymentFactory.DOMAIN_ROOT_ATTR);
            debuggerPort = dm.getInstanceProperties().getProperty(WLDeploymentFactory.DEBUGGER_PORT_ATTR);
        }
        
        public void run() {
            try {
                long start = System.currentTimeMillis();
                
                Properties environment = System.getProperties();
                environment.put("JAVA_OPTIONS", "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=" + debuggerPort + ",server=y,suspend=n"); // NOI18N
                
                Process serverProcess = Runtime.getRuntime().exec(new String[]{domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH)}, properties2StringArray(environment));
                
//                ProcessBuilder processBuilder = new ProcessBuilder(new String[]{domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH)}); // NOI18N
//                processBuilder.environment().put("JAVA_OPTIONS", "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=" + debuggerPort + ",server=y,suspend=n"); // NOI18N
//                
//                Process serverProcess = processBuilder.start();
                
                new WLTailer(serverProcess.getInputStream(), NbBundle.getMessage(WLStartServer.class, "TXT_ioWindowTitle")).start();
                
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (!isRunning()) {
                        serverProgress.notifyStart(StateType.RUNNING, ""); // NOI18N
                    } else {
                        serverProgress.notifyStart(StateType.COMPLETED, ""); // NOI18N
                        return;
                    }

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }

                serverProgress.notifyStart(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "")); // NOI18N
                serverProcess.destroy();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
        
        private static final int TIMEOUT = 120000;
        private static final int DELAY = 5000;
        
        private static final String STARTUP_SH = "startWebLogic.sh"; // NOI18N
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N
    }
    
    private class WLStopRunnable implements Runnable {
        
        private String domainHome;
        
        private WLServerProgress serverProgress;
        
        public WLStopRunnable(WLServerProgress serverProgress) {
            this.serverProgress = serverProgress;
            
            domainHome = dm.getInstanceProperties().getProperty(WLDeploymentFactory.DOMAIN_ROOT_ATTR);
        }
        
        public void run() {
            try {
                long start = System.currentTimeMillis();
                
                Process serverProcess = Runtime.getRuntime().exec(domainHome + "/" + (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH)); // NOI18N
                
                new WLTailer(serverProcess.getInputStream(), NbBundle.getMessage(WLStartServer.class, "TXT_ioWindowTitle")).start(); // NOI18N
                
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (isRunning()) {
                        serverProgress.notifyStop(StateType.RUNNING, ""); // NOI18N
                    } else {
                        serverProgress.notifyStop(StateType.COMPLETED, ""); // NOI18N
                        return;
                    }

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }
                    
                serverProgress.notifyStop(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "")); // NOI18N
                serverProcess.destroy();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
        }
        
        private static final int TIMEOUT = 120000;
        private static final int DELAY = 5000;
        
        private static final String SHUTDOWN_SH = "stopWebLogic.sh"; // NOI18N
        private static final String SHUTDOWN_BAT = "stopWebLogic.cmd"; // NOI18N
    }
    
    private static class WLServerProgress implements ProgressObject {
        
        private Vector listeners = new Vector();
        
        private DeploymentStatus deploymentStatus;
        
        private Object source;
        
        public WLServerProgress(Object source) {
            this.source = source;
        }
        
        public void notifyStart(StateType state, String message) {
            notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.START, state, message));
        }
        
        public void notifyStop(StateType state, String message) {
            notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, state, message));
        }
        
        public void notify(DeploymentStatus deploymentStatus) {
            ProgressEvent evt = new ProgressEvent(source, null, deploymentStatus);
            
            this.deploymentStatus = deploymentStatus;
            
            java.util.Vector targets = null;
            synchronized (this) {
                if (listeners != null) {
                    targets = (java.util.Vector) listeners.clone();
                }
            }
            
            if (targets != null) {
                for (int i = 0; i < targets.size(); i++) {
                    ProgressListener target = (ProgressListener) targets.elementAt(i);
                    target.handleProgressEvent(evt);
                }
            }
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // ProgressObject implementation
        ////////////////////////////////////////////////////////////////////////////
        
        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return null;
        }
        
        public void removeProgressListener(ProgressListener progressListener) {
            listeners.remove(progressListener);
        }
        
        public void addProgressListener(ProgressListener progressListener) {
            listeners.add(progressListener);
        }
        
        
        public DeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }
        
        public TargetModuleID[] getResultTargetModuleIDs() {
            return new TargetModuleID[]{};
        }
        
        public boolean isStopSupported() { return false; }
        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("");
        }
        
        public boolean isCancelSupported() { return false; }
        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("");
        }
    }
    
    private static class WLDeploymentStatus implements DeploymentStatus {
        private ActionType action;
        private CommandType command;
        private StateType state;
        private String message;

        public WLDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
            this.action = action;
            this.command = command;
            this.state = state;
            this.message = message;
        }

        public ActionType getAction() { return action; }
        public CommandType getCommand() { return command; }
        public String getMessage() { return message; }
        public StateType getState() { return state; }
        public boolean isCompleted() { return StateType.COMPLETED.equals(state); }
        public boolean isFailed() { return StateType.FAILED.equals(state); }
        public boolean isRunning() { return StateType.RUNNING.equals(state); }
    };
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants section
    ////////////////////////////////////////////////////////////////////////////
    
    private static final int STATE_STOPPED  = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_STARTED  = 2;
    private static final int STATE_STOPPING = 3;
}
