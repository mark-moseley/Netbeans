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

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;

import java.io.*;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;

/**
 *
 * @author  nn136682
 */
public class IncrementalDeploySupport extends IncrementalDeployment {
    DepManager dm;
    File applicationsDir;
    HashMap moduleDirectories = new HashMap();
    
    /** Creates a new instance of IncrementalDeploySupport */
    public IncrementalDeploySupport(DeploymentManager manager) {
        this.dm = (DepManager) dm;
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        if (manager instanceof DepManager)
            dm = (DepManager) manager;
        else
            throw new IllegalArgumentException("setDeploymentManager: Invalid manager type");
    }
    
    File getApplicationsDir() {
        if (applicationsDir != null)
            return applicationsDir;
        
        File userdir = new File(System.getProperty("netbeans.user"));
        applicationsDir = new File(userdir, "testplugin/applications");
        if (! applicationsDir.exists())
            applicationsDir.mkdirs();
        return applicationsDir;
    }
        
    static Map planFileNames = new HashMap();
    static {
        planFileNames.put(ModuleType.WAR, new String[] {"tpi-web.xml"});
        planFileNames.put(ModuleType.EJB, new String[] {"tpi-ejb-jar.xml"});
        planFileNames.put(ModuleType.EAR, new String[] {"tpi-application.xml"});
    }
    
    public File getDirectoryForModule (TargetModuleID module) {
        File appDir = new File(getApplicationsDir(), getModuleRelativePath((TestTargetMoid)module));
        if (! appDir.exists())
            appDir.mkdirs();
        System.out.println("getDirectoryForModule("+module+") returned: "+appDir);
        return appDir;
    }
    
    String getModuleRelativePath(TestTargetMoid module) {
        File path;
        if (module.getParent() != null)
            path = new File(module.getParent().getModuleID(), module.getModuleID());
        else
            path = new File(module.getModuleID());
        return path.getPath();
    }
    
    public String getModuleUrl(TargetModuleID module) {
        return ((TestTargetMoid)module).getModuleUrl();
    }
    
    
    public ProgressObject incrementalDeploy (TargetModuleID module, AppChangeDescriptor changes) {
        return null;//dm.incrementalDeploy(module, changes);
    }
    
    public boolean canFileDeploy (Target target, J2eeModule deployable) {
        return true;
    }    
    
    public File getDirectoryForNewApplication (Target target, J2eeModule app, ModuleConfiguration configuration) {
        return null;
    }
    
    public File getDirectoryForNewModule (File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        return null;
    }
    
    public ProgressObject initialDeploy (Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        return null;
    }
    
}
