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

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.Target;

import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author  nn136682
 */
public class TestStartServer extends StartServer {

    private TestDeploymentManager dm;

    public TestStartServer(DeploymentManager dm) {
        this.dm = (TestDeploymentManager) dm;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public boolean isDebuggable(Target target) {
        return false; //target.getName().equals("Target 1");
    }
    
    public boolean isRunning() {
        return dm.getState() == TestDeploymentManager.RUNNING;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        this.dm = (TestDeploymentManager) manager;
    }
    
    public ProgressObject startDebugging(Target target) {
        return dm.createServerProgress();
    }
    
    public ProgressObject startDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(TestDeploymentManager.STARTING);
                sp.setStatusStartRunning("TestPluginDM: "+dm.getName()+" is starting.");
                try { Thread.sleep(2000); //super server starting time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == TestDeploymentManager.START_FAILED) {
                    dm.setState(TestDeploymentManager.FAILED);
                    sp.setStatusStartFailed("TestPluginDM: "+dm.getName()+" startup failed");
                } else {
                    dm.setState(TestDeploymentManager.RUNNING);
                    sp.setStatusStartCompleted("TestPluginDM "+dm.getName()+" startup finished");
                }
            }
        };
        
        (new Thread(r)).start();
        return sp;
    }
    
    public ProgressObject stopDeploymentManager() {
        final ServerProgress sp = dm.createServerProgress();
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(500); //latency
                } catch (Exception e) {}
                dm.setState(TestDeploymentManager.STOPPING);
                sp.setStatusStopRunning("TestPluginDM is preparing to stop "+dm.getName()+"...");
                try { Thread.sleep(2000); //super server stop time
                } catch (Exception e) {}
                if (dm.getTestBehavior() == TestDeploymentManager.STOP_FAILED) {
                    dm.setState(TestDeploymentManager.FAILED);
                    sp.setStatusStopFailed("TestPluginDM stop "+dm.getName()+" failed");
                } else {
                    dm.setState(TestDeploymentManager.STOPPED);
                    sp.setStatusStopCompleted("TestPluginDM startup "+dm.getName()+" finished");
                }
            }
        };

        (new Thread(r)).start();
        return sp;
    }
    
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    public boolean needsStartForTargetList() {
        return true;
    }
    
}
