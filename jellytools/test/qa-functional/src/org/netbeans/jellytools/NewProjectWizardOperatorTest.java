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
package org.netbeans.jellytools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.NewProjectWizardOperator.
 * @author tb115823
 */
public class NewProjectWizardOperatorTest extends JellyTestCase {

    public static NewProjectWizardOperator op;
    // "Java Application"
    private static final String javaApplicationLabel =
            Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                             "Templates/Project/Standard/emptyJ2SE.xml");
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewProjectWizardOperatorTest("testInvokeTitle"));
        suite.addTest(new NewProjectWizardOperatorTest("testInvoke"));
        suite.addTest(new NewProjectWizardOperatorTest("testSelectCategoryAndProject"));
        suite.addTest(new NewProjectWizardOperatorTest("testVerify"));
        suite.addTest(new NewProjectWizardOperatorTest("testGetDescription"));
        return suite;
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewProjectWizardOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method with title parameter. Opens new wizard, waits for the dialog and closes it. */
    public void testInvokeTitle() {
        // "New Project"
        String title = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewProjectWizard_Title");
        op = NewProjectWizardOperator.invoke(title);
        op.close();
    }

    /** Test of invoke method. Opens new wizard and waits for the dialog. */
    public void testInvoke() {
        op = NewProjectWizardOperator.invoke();
    }

    /** Test of methods selectCategory and selectProject. */
    public void testSelectCategoryAndProject() {
        // Standard
        String standardLabel = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        op.selectCategory(standardLabel);
        op.selectProject(javaApplicationLabel);
    }
    
    /** Test of verify method. */
    public void testVerify() {
        op.verify();
    }
    
    /** Test of getDescription method. */
    public void testGetDescription() {
        assertTrue("Wrong description.", op.getDescription().indexOf("Java SE application")>0);
        op.cancel();
    }
}
