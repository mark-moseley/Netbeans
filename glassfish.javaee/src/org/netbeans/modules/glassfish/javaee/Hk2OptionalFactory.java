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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.javaee.ide.FastDeploy;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2OptionalFactory extends OptionalDeploymentManagerFactory {
    
    public StartServer getStartServer(DeploymentManager dm) {
        return new Hk2StartServer(dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new FastDeploy((Hk2DeploymentManager) dm) : null;
    }
    
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        Logger.getLogger("glassfish-javaee").log(Level.INFO, 
                "JavaEE_V3_OptionalFactory.getFindJSPServlet");
        return null;
    }
    
    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return null;
    }
    
    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return dm instanceof Hk2DeploymentManager ?
                new Hk2DatasourceManager((Hk2DeploymentManager) dm) : null;
    }
    
    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return null;
    }
    
}
