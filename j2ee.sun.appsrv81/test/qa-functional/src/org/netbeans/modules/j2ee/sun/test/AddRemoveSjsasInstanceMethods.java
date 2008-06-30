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

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
//import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Michal Mocnak
 */
public class AddRemoveSjsasInstanceMethods extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public AddRemoveSjsasInstanceMethods(String testName) {
        super(testName);
    }
    
    public void addSjsasInstance() {
        try {
            AddDomainWizardIterator inst = new AddDomainWizardIterator(new PlatformValidator());
            WizardDescriptor wizard = new WizardDescriptor(new Panel[] {});
            wizard.putProperty(Util.PLATFORM_LOCATION, new File(Util._PLATFORM_LOCATION));
            wizard.putProperty(Util.INSTALL_LOCATION, Util._INSTALL_LOCATION);
            wizard.putProperty(Util.PROP_DISPLAY_NAME, Util._DISPLAY_NAME);
            wizard.putProperty(Util.HOST, Util._HOST);
            wizard.putProperty(Util.PORT, Util._PORT);
            wizard.putProperty(Util.DOMAIN, Util._DOMAIN);
            wizard.putProperty(Util.USER_NAME, Util._USER_NAME);
            wizard.putProperty(Util.PASSWORD, Util._PASSWORD);
            
            inst.initialize(wizard);
            inst.instantiate();
            
            ServerRegistry.getInstance().checkInstanceExists(Util._URL);
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void removeSjsasInstance() {
        try {
            Util.sleep(SLEEP);
            
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            boolean wasRunning = inst.isRunning();
            
            inst.remove();
            
            if (wasRunning) {
                Util.sleep(SLEEP);
            }

            try {
                ServerRegistry.getInstance().checkInstanceExists(Util._URL);
            } catch(Exception e) {
                if (wasRunning && inst.isRunning())
                    fail("remove did not stop the instance");
                String instances[] = ServerRegistry.getInstance().getInstanceURLs();
                if (null != instances) 
                    if (instances.length > 1)
                        fail("too many instances");
                return;
            }
            
            fail("Sjsas instance still exists !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddRemoveSjsasInstanceMethods.class).
                addTest("addSjsasInstance","removeSjsasInstance").enableModules(".*").clusters(".*"));
//        NbTestSuite suite = new NbTestSuite("AddRemoveSjsasInstanceMethods");
//        suite.addTest(new AddRemoveSjsasInstanceMethods("addSjsasInstance"));        
//        suite.addTest(new AddRemoveSjsasInstanceMethods("removeSjsasInstance"));        
//        return suite;
    }
}