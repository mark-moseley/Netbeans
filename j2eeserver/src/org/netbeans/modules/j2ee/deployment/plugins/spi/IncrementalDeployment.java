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

/*
 * IncrementalDeployment.java
 *
 * Created on November 14, 2003, 9:13 AM
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.File;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;

/**
 * This interface replaces DeploymentManager calls <code>deploy</code> and <code>redeploy</code> during
 * directory-based deployment.  The calling sequence is as following:
 * <P>
 * Initially, j2eeserver will call <code>getDirectoryForNewApplication()</code>
 * to request destination directory to deliver the being deployed application or 
 * stand-alone module. In case of J2EE applications, <code>getDirectoryForNewModule()</code>
 * will be called for each child module.
 * <P>
 * After done with copying of files over to the destination, <code>initialDeploy()</code> will
 * be called to signal the copying is done.  Processing of the returned <code>ProgressObject</code>
 * is the same as in <code>DeploymentManager.distribute()</code> call. 
 * <P> 
 * Subsequent deployments are incremental. For each root and child module the IDE will ask plugin
 * for destination directory by calling <code>getDirectoryForModule()</code>.  After delivering
 * the changed files for all modules, the IDE then call <code>incrementalDeploy</code> with
 * the description of what have changed since previous deployment.
 *<P>
 * For in-place file deployment, where the file copying step is skipped, method 
 * <code>getDirectoryForNewApplication</code> or <code>getDirectoryForNewModule</code> calls
 * return null.
 * <P>
 * J2eeserver optain an instance of IncrementalDeployment from server integration plugin by
 * calling {@link OptionalDeploymentManagerFactory} to optain an instance of IncrementalDeployment
 * for each {@link javax.enterprise.deploy.spi.DeploymentManager} instance.
 * <P>
 * @author  George Finklang
 */
public abstract class IncrementalDeployment {

    /**
     * First time deployment file distribution.  Before this method is called 
     * the module content files should be ready in the destination location.
     *
     * @param target target of deployment
     * @param app the app to deploy
     * @param configuration server specific data for deployment
     * @param dir the destination directory for the given deploy app
     * @return the object for feedback on progress of deployment
     */ 
    public abstract ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration, File dir);

    /**
     * Before this method is called, the on-disk representation of TargetModuleID
     * is updated.
     * @param module the TargetModuleID of the deployed application or stand-alone module.
     * @param changes AppChangeDescriptor describing what in the application changed. 
     * @return the ProgressObject providing feedback on deployment progress.
     **/
    public abstract ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes);
    
    /**
     * Whether the deployable object could be file deployed to the specified target
     * @param target target in question
     * @param deployable the deployable object in question
     * @return true if it is possible to do file deployment
     */
    public abstract boolean canFileDeploy(Target target, J2eeModule deployable);
    
    /**
     * Return absolute path which the IDE will write the specified app or
     * stand-alone module content to.
     * @param target target server of the deployment
     * @param app the app or stand-alone module to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or
     * null if server can accept the deployment from an arbitrary directory.
     */
    public abstract File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration);
    
    /**
     * Return absolute path the IDE will write the app or stand-alone module content to.
     * Note: to use deployment name, implementation nees to override this.
     *
     * @param deploymentName name to use in deployment
     * @param target target server of the deployment
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or null if server can accept the deployment from an arbitrary directory.
     */
    public File getDirectoryForNewApplication(String deploymentName, Target target, ModuleConfiguration configuration) {
        return getDirectoryForNewApplication(target, configuration.getJ2eeModule(), configuration);
  } 

  /**
     * Return absolute path to which the IDE will write the specified module content.
     * @param appDir the root directory of containing application
     * @param uri the URI of child module within the app
     * @param module the child module object to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified module.
     */
    public abstract File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration);
    
    /**
     * Return absolute path to which the IDE will write the content changes of specified module.
     * @param module id for the target module.
     * @return absolute path root directory for the specified module.
     */
    public abstract File getDirectoryForModule(TargetModuleID module);
    
    /**
     * Get the URI pointing to location of child module inside a application archive.
     *
     * @param module TargetModuleID of the child module
     * @return its relative path within application archive, returns null by 
     * default (for standalone module)
     */
    public String getModuleUrl(TargetModuleID module) {
        return null;
    }
    
    /**
     * Inform the plugin that the specified module is being deployed. Notification
     * is sent even if there is really nothing needed to be deployed.
     *
     * @param module module which is being deployed.
     */
    public void notifyDeployment(TargetModuleID module) {
        //do nothing, override if needed
    }
}
