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

package org.netbeans.modules.j2ee.sun.ide.j2ee.incrdeploy;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.ViewLogAction;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.openide.util.Exceptions;

/**
 *
 * @author  vkraemer
 */
public class DirectoryDeploymentFacade  extends IncrementalDeployment {
    
    Object inner = null;
    private File[] resourceDirs = null;
    private DeploymentManager dm;
    private boolean issue2999Fixed = true;
    
    private static Map cache = new WeakHashMap();

    public static IncrementalDeployment get(DeploymentManager dm) {
        IncrementalDeployment id = (IncrementalDeployment) cache.get(dm);
        if (null == id) {
            id = new DirectoryDeploymentFacade(dm);
            cache.put(dm,id);
        }
        return id;
    }
    
    /** Creates a new instance of DirectoryDeploymentFacade */
    private DirectoryDeploymentFacade(DeploymentManager dm) {
        try {
            setDeploymentManager(dm);
            Class[] cls= new Class[1];
            cls[0]=DeploymentManager.class;
            SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)dm;
            ClassLoader loader = ServerLocationManager.getNetBeansAndServerClassLoader(sdm.getPlatformRoot());
            java.lang.reflect.Constructor ctr = 
                    loader.loadClass("org.netbeans.modules.j2ee.sun.bridge.DirectoryDeployment").getConstructor(cls);
            Object[] o= new Object[1];
            o[0]=dm;
            
            inner = ctr.newInstance(o);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * @param manager
     */
    public void setDeploymentManager(DeploymentManager manager) {
        if (null == manager) {
            throw new java.lang.IllegalArgumentException("invalid null argumment");
        }
        if (manager instanceof org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface)
            this.dm = manager;
        else
            throw new java.lang.IllegalArgumentException("setDeploymentManager: Invalid manager type, expecting SunDeploymentManager and got " +
                    manager.getClass().getName());
        checkIssue2999(manager);
    }
    
    private void checkIssue2999(DeploymentManager manager) {
        org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface sdmi =
                (org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface) manager;
        if (ServerLocationManager.getAppServerPlatformVersion(sdmi.getPlatformRoot()) <=
                ServerLocationManager.GF_V2) {
            Target[] targs = manager.getTargets();
            TargetModuleID tmids[] = null;
            try {
                tmids = manager.getRunningModules(ModuleType.EAR, targs);
                if (tmids == null || tmids.length < 1) {
                    tmids = manager.getNonRunningModules(ModuleType.EAR, targs);
                }
                boolean jwsSupportFound = false;
                for (TargetModuleID ear : tmids) {
                    // find out if we are deploying to a broken 9.0 or 9.1 build
                    if (ear.toString().startsWith("__JWSappclients")) {
                        jwsSupportFound = true;
                        TargetModuleID tmids2[] = ear.getChildTargetModuleID();
                        if (null == tmids2 || tmids2.length < 1) {
                            issue2999Fixed = false;
                        }
                    }
                }
                // 8.x doesn't support this
                if (!jwsSupportFound) {
                    issue2999Fixed = false;
                }
            } catch (Exception ex) {
                // better safe than sorry here
                issue2999Fixed = false;
            }
        }        
    }
    
    
    
    public java.io.File getDirectoryForModule(javax.enterprise.deploy.spi.TargetModuleID module) {
        java.io.File retVal = null;
        if (null != inner){
            retVal = ((IncrementalDeployment)inner).getDirectoryForModule(module);
        }
        return retVal;
    }
    
    public String getModuleUrl(javax.enterprise.deploy.spi.TargetModuleID module) {
        String retVal = null;
        if (null != inner){
            retVal = ((IncrementalDeployment)inner).getModuleUrl(module);
        }
        return retVal;
    }
    
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes) {
        //Register resources if any
        
        // XXX
        //No way to get resourceDirs from input parameters of this method.
        //Initializing resourceDirs in canFileDeploy() method.
        //Relying on the assumption that canFileDeploy() is & *will* always get
        //called with appropriate DeployableObject before this method.
        if((resourceDirs != null) && (dm != null)){
            Utils.registerResources(resourceDirs, (ServerInterface)((SunDeploymentManagerInterface)dm).getManagement());
        }
        if (null!=dm){
            ViewLogAction.viewLog((SunDeploymentManagerInterface)dm);
        }
        
        return ((IncrementalDeployment)inner).incrementalDeploy(module, changes);
    }
    
    
    
    public File getDirectoryForNewApplication(String deploymentName, Target target, DeploymentConfiguration configuration){
        SunONEDeploymentConfiguration s1dc =(SunONEDeploymentConfiguration) configuration;
        s1dc.setDeploymentModuleName(deploymentName);
        return null;
    }
    
    public ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        //Register resources if any
        File[] resourceDirs = Utils.getResourceDirs(app);
        if((resourceDirs != null) && (dm != null)) {
            Utils.registerResources(resourceDirs, (ServerInterface)((SunDeploymentManagerInterface)dm).getManagement());
        }
        if (null != dm){
            ViewLogAction.viewLog((SunDeploymentManagerInterface)dm);
        }
        return ((IncrementalDeployment)inner).initialDeploy(target,app,configuration, dir);
    }
    
    /**
     * Whether the J2eeModule could be file deployed to the specified target
     * @param target target in question
     * @param module the J2eeModule in question
     * @return true if it is possible to do file deployment
     */
    public boolean canFileDeploy(Target target, J2eeModule module) {
        boolean retVal = true;
        if (null == dm){
            retVal = false;
        }
        if (null == module){
            retVal = false;
        } else {
            // TODO find out why this is here..
            resourceDirs = Utils.getResourceDirs(module);
            
            //so far only WAR are supported for Directory based deployment
            if ((module.getModuleType() == ModuleType.CAR)){
                retVal = false;
            }
            if (retVal && !issue2999Fixed) {
                retVal = module.getModuleType() != ModuleType.EAR;
            }
            if (retVal) {
                retVal = ((SunDeploymentManagerInterface)dm).isLocal();
            }
        }
        DeploymentManagerProperties dmp = new DeploymentManagerProperties(dm);
        if (!dmp.isDirectoryDeploymentPossible()) {
            return false;
        }
        
        return retVal;
    }
    
    public File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewApplication(target,app,configuration);
    }
    
    public File getDirectoryForNewApplication(String deploymentName, Target target, ModuleConfiguration configuration) {
        return super.getDirectoryForNewApplication(deploymentName, target, configuration);
    }
    
    public File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        return ((IncrementalDeployment)inner).getDirectoryForNewModule(appDir,uri, module,configuration);
    }
    
    public void notifyDeployment(TargetModuleID module) {
        super.notifyDeployment(module);
    }
    
}
