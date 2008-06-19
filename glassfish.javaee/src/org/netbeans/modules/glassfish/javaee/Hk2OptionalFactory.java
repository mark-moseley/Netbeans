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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.javaee.ide.FastDeploy;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.Lookup;


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
    public boolean isCommonUIRequired() {
        return false;
    }
    
    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new J2eeInstantiatingIterator(ServerUtilities.getAddInstanceIterator());
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
    
    private static class J2eeInstantiatingIterator implements InstantiatingIterator {
        
        private final InstantiatingIterator delegate;

        public J2eeInstantiatingIterator(InstantiatingIterator delegate) {
            this.delegate = delegate;
        }

        public void removeChangeListener(ChangeListener l) {
            delegate.removeChangeListener(l);
        }

        public void previousPanel() {
            delegate.previousPanel();
        }

        public void nextPanel() {
            delegate.nextPanel();
        }

        public String name() {
            return delegate.name();
        }

        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Panel current() {
            return delegate.current();
        }

        public void addChangeListener(ChangeListener l) {
            delegate.addChangeListener(l);
        }

        public void uninitialize(WizardDescriptor wizard) {
            delegate.uninitialize(wizard);
        }

        public Set instantiate() throws IOException {
            Set set = delegate.instantiate();
            if (!set.isEmpty()) {
                Object inst = set.iterator().next();
                if (inst instanceof ServerInstance) {
                    Lookup lookup = ServerUtilities.getLookupFor((ServerInstance) inst);
                    if (lookup != null) {
                        JavaEEServerModule module = lookup.lookup(JavaEEServerModule.class);
                        return Collections.singleton(module.getInstanceProperties());
                    }
                }
            }
            return Collections.EMPTY_SET;
        }

        public void initialize(WizardDescriptor wizard) {
            delegate.initialize(wizard);
        }
        
    }
}
