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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.netbeans.modules.j2ee.sun.share.serverresources.SunDatasource;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunDatasourceManager implements DatasourceManager {
    
    private DeploymentManager dm;
    private SunDeploymentManager sunDm;
    
    /**
     * Creates a new instance of SunDatasourceManager
     */
    public SunDatasourceManager(DeploymentManager dm) {
        this.dm = dm;
        this.sunDm = (SunDeploymentManager)this.dm;
    }
    
    public Set getDatasources() {
        return this.sunDm.getResourceConfigurator().getServerDataSources();
    }
    
    public void deployDatasources(Set datasources) throws ConfigurationException,
            DatasourceAlreadyExistsException {
        Object[] dsources = (Object[])datasources.toArray();
        List dirs = new ArrayList();
        for(int i=0; i<dsources.length; i++){
            SunDatasource ds = (SunDatasource)dsources[i];
            dirs.add(ds.getResourceDir());
        }
        
        if(! dirs.isEmpty()){
            File[] resourceDirs = (File[])dirs.toArray(new File[dirs.size()]);
            if(resourceDirs != null){
                Utils.registerResources(resourceDirs, this.sunDm.getManagement());
            }
        }
    }
    
}
