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

package org.netbeans.modules.tomcat5.ide;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.tomcat5.AntDeploymentProviderImpl;
import org.netbeans.modules.tomcat5.TomcatJDBCDriverDeployer;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.TomcatDatasourceManager;
import org.netbeans.modules.tomcat5.jsps.FindJSPServletImpl;
import org.openide.WizardDescriptor;
import org.netbeans.modules.tomcat5.wizard.AddInstanceIterator;

/**
 * OptionalFactory implementation
 *
 * @author  Pavel Buzek
 */
public class OptionalFactory extends OptionalDeploymentManagerFactory {
    
    private final TomcatVersion version;
    
    /** Creates a new instance of OptionalFactory */
    private OptionalFactory(TomcatVersion version) {
        this.version = version;
    }
    
    public static OptionalFactory create50() {
        return new OptionalFactory(TomcatVersion.TOMCAT_50);
    }
    
    public static OptionalFactory create55() {
        return new OptionalFactory(TomcatVersion.TOMCAT_55);
    }
    
    public static OptionalFactory create60() {
        return new OptionalFactory(TomcatVersion.TOMCAT_60);
    }
    
    public FindJSPServlet getFindJSPServlet (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new FindJSPServletImpl (dm);
    }
    
    public IncrementalDeployment getIncrementalDeployment (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TomcatIncrementalDeployment (dm);
    }
    
    public StartServer getStartServer (javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new StartTomcat (dm);
    }
    
    public TargetModuleIDResolver getTargetModuleIDResolver(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TMIDResolver (dm);
    }

    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return new AddInstanceIterator(version);
    }
    
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return new TomcatDatasourceManager(dm);
    }
    
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return new AntDeploymentProviderImpl(dm);
    }
    
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return new TomcatJDBCDriverDeployer((TomcatManager) dm);
    }
}
