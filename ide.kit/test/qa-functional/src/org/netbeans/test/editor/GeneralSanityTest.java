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

package org.netbeans.test.editor;

import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.ide.BlacklistedClassesHandler;
import org.netbeans.test.ide.BlacklistedClassesHandlerSingleton;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class GeneralSanityTest extends NbTestCase {

    public GeneralSanityTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTest(new GeneralSanityTest("testInitBlacklistedClassesHandler"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.createConfiguration(
                GeneralSanityTest.class
            ).gui(true).clusters(".*").enableModules(".*").
            honorAutoloadEager(true).
            addTest(
                "testBlacklistedClassesHandler",
                "testOrgOpenideOptionsIsDisabledAutoload"
            )
        ));
        return s;
    }

    public void testInitBlacklistedClassesHandler() {
        String configFN = new File(getDataDir(), "BlacklistedClassesHandlerConfig.xml").getPath();
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();

        System.out.println("BlacklistedClassesHandler will be initialized with " + configFN);
        if (bcHandler.initSingleton(configFN)) {
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
        } else {
            fail("Cannot initialize blacklisted class handler");
        }
    }


    public void testBlacklistedClassesHandler() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        if (bcHandler.isGeneratingWhitelist()) {
            bcHandler.saveWhiteList(getLog("whitelist.txt"));
        }
        try {
            if (bcHandler.hasWhitelistStorage()) {
                bcHandler.saveWhiteList();
                bcHandler.saveWhiteList(getLog("whitelist.txt"));
                bcHandler.reportDifference(getLog("diff.txt"));
                assertTrue(bcHandler.reportViolations(getLog("violations.xml"))
                        + bcHandler.reportDifference(), bcHandler.noViolations());
            } else {
                assertTrue(bcHandler.reportViolations(getLog("violations.xml")), bcHandler.noViolations());
            }
        } finally {
            bcHandler.unregister();
        }
    }

    public void testOrgOpenideOptionsIsDisabledAutoload() {
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.getCodeNameBase().equals("org.openide.options")) {
                assertFalse("org.openide.options shall not be enabled", m.isEnabled());
                return;
            }
        }
        fail("No org.openide.options module found, it should be present, but disabled");
    }
}
