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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.io.File;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleCookieImpl implements UndeployModuleCookie {
    
    private String fileName;
    private Lookup lookup;
    private ModuleType type;
    private final boolean isEJB3;
    private boolean isRunning;
    
    public UndeployModuleCookieImpl(String fileName, ModuleType type, Lookup lookup) {
        this(fileName, type, false, lookup);
    }

    public UndeployModuleCookieImpl(String fileName, Lookup lookup) {
        this(fileName, ModuleType.EJB, true, lookup);
    }

    private UndeployModuleCookieImpl(String fileName, ModuleType type, boolean isEJB3, Lookup lookup) {
        this.lookup = lookup;
        this.fileName = fileName;
        this.type = type;
        this.isEJB3 = isEJB3;
        this.isRunning = false;
    }    
    
    public Task undeploy() {
        final JBDeploymentManager dm = (JBDeploymentManager) lookup.lookup(JBDeploymentManager.class);
        final String nameWoExt = fileName.substring(0, fileName.lastIndexOf('.'));
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(UndeployModuleCookieImpl.class,
                "LBL_UndeployProgress", nameWoExt));
        
        Runnable r = new Runnable() {
            public void run() {
                isRunning = true;
                String deployDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
                File file = new File(deployDir, fileName);
                
                if(file.exists() && file.canWrite()) {
                    file.delete();
                    
                    try {
                        ObjectName searchPattern = null;
                        if (Util.isRemoteManagementSupported(lookup) && !isEJB3) {
                            searchPattern = new ObjectName("jboss.management.local:"+(!type.equals(ModuleType.EAR) ?
                                "J2EEApplication=null," : "")+"j2eeType="+Util.getModuleTypeString(type)+",name=" + fileName + ",*");
                        }
                        else {
                            if (type.equals(ModuleType.EAR)) {
                                searchPattern = new ObjectName("jboss.j2ee:service=EARDeployment,url='" + fileName + "'"); // NOI18N
                            }
                            else 
                            if (type.equals(ModuleType.WAR)) {
                                searchPattern = new ObjectName("jboss.web:j2eeType=WebModule,J2EEApplication=none,name=//localhost/" + nameWoExt + ",*"); // NOI18N
                            }
                            else
                            if (type.equals(ModuleType.EJB)) {
                                searchPattern = new ObjectName("jboss.j2ee:service=" + (isEJB3 ? "EJB3" : "EjbModule") + ",module=" + fileName); // NOI18N
                            }
                        }
                        
                        int time = 0;
                        while(dm.refreshRMIServer() != null && Util.isObjectDeployed(dm.getRMIServer(), searchPattern) && time < 30000) {
                            try {
                                Thread.sleep(2000);
                                time += 2000;
                            } catch (InterruptedException ex) {
                                // Nothing to do
                            }
                        }
                    } catch (MalformedObjectNameException ex) {
                    } catch (NullPointerException ex) {
                        // Nothing to do
                    }
                }
                
                handle.finish();
                isRunning = false;
            }
        };
        
        handle.start();
        return RequestProcessor.getDefault().post(r);
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
}