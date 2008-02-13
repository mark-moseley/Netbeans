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
package org.netbeans.modules.ws.qaf;

import java.io.IOException;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author lukas
 */
public class WebServiceSamplesTest extends WebServicesTestBase {

    private static boolean[] deployedApps = {false, false, false, false};

    public WebServiceSamplesTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return getName().substring(4);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.SAMPLE;
    }

    @Override
    protected String getSamplesCategoryName() {
        return Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro");
    }

    public void testCalculatorApp() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro/Calculator");
        createProject(sampleName, getProjectType(), null);
        deployProject("CalculatorApp"); //NOI18N
        deployedApps[0] = true;
        deployProject("CalculatorClientApp"); //NOI18N
        deployedApps[1] = true;
    }

    public void testSecureCalculatorApp() throws IOException {
        String sampleName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.metro.samples.Bundle", "Templates/Project/Samples/Metro/SecureCalculator");
        createProject(sampleName, getProjectType(), null);
        deployProject("SecureCalculatorApp"); //NOI18N
        deployedApps[2] = true;
        deployProject("SecureCalculatorClientApp"); //NOI18N
        deployedApps[3] = true;
    }

    public void testUndeployAll() throws IOException {
        if (deployedApps[0]) {
            undeployProject("CalculatorApp"); //NOI18N
        }
        if (deployedApps[1]) {
            undeployProject("CalculatorClientApp"); //NOI18N
        }
        if (deployedApps[2]) {
            undeployProject("SecureCalculatorApp"); //NOI18N
        }
        if (deployedApps[3]) {
            undeployProject("SecureCalculatorClientApp"); //NOI18N
        }
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new WebServiceSamplesTest("testCalculatorApp"));
        suite.addTest(new WebServiceSamplesTest("testSecureCalculatorApp"));
        suite.addTest(new WebServiceSamplesTest("testUndeployAll"));
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }
}
