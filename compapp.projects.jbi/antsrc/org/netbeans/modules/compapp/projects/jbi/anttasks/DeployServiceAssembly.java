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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.AdministrationServiceHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;

/**
 * Ant task to deploy/undeploy Service Assembly to/from the target JBI server.
 *
 */
public class DeployServiceAssembly extends Task {
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyID;
    
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyLocation;
    
    private String undeployServiceAssembly = "false";
    
    private String hostName;
    
    private String port;
    
    private String userName;
    
    private String password;
    
    // REMOVE ME
    private String serverInstanceLocation;
    
    private String netBeansUserDir;
    
    // If not defined, then the first server instance in the setting file
    // will be used.
    // REMOVE ME
    private String j2eeServerInstance;
    
    private boolean FORCE = true; //??
    
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyID() {
        return this.serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyID
     *            The ServiceAssembly ID to set.
     */
    public void setServiceAssemblyID(String serviceAssemblyID) {
        this.serviceAssemblyID = serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyLocation() {
        return this.serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyLocation
     *            The ServiceAssembly location to set.
     */
    public void setServiceAssemblyLocation(String serviceAssemblyLocation) {
        this.serviceAssemblyLocation = serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the undeployServiceAssembly.
     */
    public String getUndeployServiceAssembly() {
        return this.undeployServiceAssembly;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param undeployServiceAssembly
     *            The undeployServiceAssembly command.
     */
    public void setUndeployServiceAssembly(String undeployServiceAssembly) {
        this.undeployServiceAssembly = undeployServiceAssembly;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setServerInstanceLocation(String serverInstanceLocation) {
        this.serverInstanceLocation = serverInstanceLocation;
    }
    
    public String getServerInstanceLocation() {
        return serverInstanceLocation;
    }
    
    public void setNetBeansUserDir(String netBeansUserDir) {
        this.netBeansUserDir = netBeansUserDir;
    }
    
    public String getNetBeansUserDir() {
        return netBeansUserDir;
    }
    
    public void setJ2eeServerInstance(String j2eeServerInstance) {
        this.j2eeServerInstance = j2eeServerInstance;
    }
    
    public String getJ2eeServerInstance() {
        return j2eeServerInstance;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException
     *             DOCUMENT ME!
     */
    public void execute() throws BuildException {
        if (serviceAssemblyID != null && 
                serviceAssemblyID.equals("${org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit}")) {
            String msg = "Unknown Service Assembly ID: " + serviceAssemblyID + 
                    System.getProperty("line.separator") +
                    "Please re-open your CompApp project using the latest NetBeans to refresh your CompApp project." +
                    System.getProperty("line.separator") +
                    "See http://www.netbeans.org/issues/show_bug.cgi?id=108702 for more info."; 
            throw new BuildException(msg);
        }
                
        String nbUserDir = getNetBeansUserDir();
        String serverInstanceID = getJ2eeServerInstance();
                
        startServer(serverInstanceID);
        
        ServerInstance serverInstance = AdministrationServiceHelper.getServerInstance(
                nbUserDir, serverInstanceID);
               
        try {
            RuntimeManagementServiceWrapper mgmtServiceWrapper = 
                    AdministrationServiceHelper.
                    getRuntimeManagementServiceWrapper(serverInstance);
            DeploymentService deploymentService = 
                    AdministrationServiceHelper.
                    getDeploymentService(serverInstance);
        
            hostName = serverInstance.getHostName();
            port = serverInstance.getAdminPort();
            userName = serverInstance.getUserName();
            password = serverInstance.getPassword();

            ServiceAssemblyInfo assembly = mgmtServiceWrapper.getServiceAssembly(
                    serviceAssemblyID, "server");        
            String status = assembly == null ? null : assembly.getState();
            // System.out.println("Current assembly status is " + status);

            if (JBIComponentStatus.UNKNOWN_STATE.equals(status)) {
                String msg = "Unknown status for Service Assembly "
                        + serviceAssemblyID;
                throw new BuildException(msg);
            }

            if (undeployServiceAssembly.equalsIgnoreCase("true")) { // NOI18N
                if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                    stopServiceAssembly(mgmtServiceWrapper);
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                    undeployServiceAssembly(deploymentService);
                }
            } else { // deploy action...
                if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                    stopServiceAssembly(mgmtServiceWrapper);
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                    shutdownServiceAssembly(mgmtServiceWrapper);
                    undeployServiceAssembly(deploymentService);
                } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                    undeployServiceAssembly(deploymentService);
                } 
                
                deployServiceAssembly(deploymentService);
                startServiceAssembly(mgmtServiceWrapper);                
            }
        } catch (ManagementRemoteException e) {
            Object[] processResult = JBIMBeanTaskResultHandler.getProcessResult(
                    GenericConstants.START_COMPONENT_OPERATION_NAME,
                    serviceAssemblyID, e.getMessage(), false);
            throw new BuildException((String) processResult[0]);
        }             
    }
      
    private void startServer(String serverInstanceID) {
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = null;
        
        try {
             inst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        } catch (Exception e) {
            // NPE from command line deployment.
            // Fine. Not supporting auto server start if deployed from command line.
            return;
        }
        
        if (inst == null) {
            log("Bad target server ID: " + serverInstanceID);
            return;
        }
        
        ServerString server = new ServerString(inst);
        
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance serverInstance = 
                server.getServerInstance();
        if (server == null || serverInstance == null) {
            log("Make sure a target server is set in project properties.");
        }
        
        // Currently it is not possible to select target to which modules will
        // be deployed. Lets use the first one.
        // (This will start the server if the server is not running.)
        ServerTarget targets[] = serverInstance.getTargets();
    }
    
//    /**
//     * Retrieves the status of the given Service Assembly deployed on the JBI
//     * Container on the Server.
//     *
//     * @param assemblyName
//     *            name of a Service Assembly
//     * @return JBI ServiceAssembly Status
//     */
//    private JBIServiceAssemblyStatus getJBIServiceAssemblyStatus(
//            AdministrationService adminService, String assemblyName) {
//        
//        if (adminService != null) {
//            List<JBIServiceAssemblyStatus> assemblyList = 
//                    adminService.getServiceAssemblyStatusList();
//            for (JBIServiceAssemblyStatus assembly : assemblyList) {
//                if (assembly.getServiceAssemblyName().equals(assemblyName)) {
//                    return assembly;
//                }
//            }
//        }
//        
//        return null;
//    }
    
    private void deployServiceAssembly(DeploymentService adminService) 
            throws BuildException {
        log("[deploy-service-assembly]");
        log("    Deploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        file=" + serviceAssemblyLocation);
        
        try {
            adminService.deployServiceAssembly(serviceAssemblyLocation, "server");
        } catch (ManagementRemoteException e) {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Deploy", serviceAssemblyLocation, e.getMessage(), false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void startServiceAssembly(
            RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[start-service-assembly]");
        log("    Starting a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
          
        try {
            adminService.startServiceAssembly(serviceAssemblyID, "server");
        } catch (ManagementRemoteException e) {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Start", serviceAssemblyID, e.getMessage(), false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void stopServiceAssembly(RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[stop-service-assembly]");
        log("    Stopping a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        try {
            adminService.stopServiceAssembly(serviceAssemblyID, "server");
        } catch (ManagementRemoteException e) {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Stop", serviceAssemblyID, e.getMessage(), false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void shutdownServiceAssembly(RuntimeManagementServiceWrapper adminService) 
            throws BuildException {
        log("[shutdown-service-assembly]");
        log("    Shutting down a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        try {
            adminService.shutdownServiceAssembly(serviceAssemblyID, FORCE, "server");
        } catch (ManagementRemoteException e) {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Shutdown", serviceAssemblyID, e.getMessage(), false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    private void undeployServiceAssembly(DeploymentService adminService) 
            throws BuildException {
        log("[undeploy-service-assembly]");
        log("    Undeploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
         try {
            adminService.undeployServiceAssembly(serviceAssemblyID, FORCE, "server");
        } catch (ManagementRemoteException e) {
            Object[] value = JBIMBeanTaskResultHandler.getProcessResult(
                "Undeploy", serviceAssemblyID, e.getMessage(), false);
            if (value[0] != null) {
                throw new BuildException((String)value[0]);
            }
        }
    }
    
    static Document loadXML(String xmlSource) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            // documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(
                    xmlSource)));
        } catch (Exception e) {
            System.out.println("Error parsing XML: " + e);
            return null;
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        System.out.println("here");
        DeployServiceAssembly deploy = new DeployServiceAssembly();
        deploy.setServiceAssemblyID("01000000-C40493EE0B0100-8199A774-01"); 
        deploy.setServiceAssemblyLocation("C:\\Documents and Settings\\jqian\\CompositeApp10\\dist\\CompositeApp10.zip"); 
        deploy.setUserName("admin");
        deploy.setPassword("adminadmin");
        deploy.setHostName("localhost");
        deploy.setPort("4848");
        deploy.setServerInstanceLocation("C:\\Alaska\\Sun\\AppServer");
        deploy.execute();
    }
    
}
