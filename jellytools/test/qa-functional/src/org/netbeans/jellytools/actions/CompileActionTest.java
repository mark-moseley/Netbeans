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

package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.CleanProjectAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class CompileActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CompileActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CompileActionTest("testPerformPopup"));
        suite.addTest(new CompileActionTest("testPerformMenu"));
        suite.addTest(new CompileActionTest("testPerformShortcut"));
        return suite;
         */
        return createModuleTest(CompileActionTest.class, "testPerformPopup", "testPerformMenu", "testPerformShortcut");
    }

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static Node node;
    private static MainWindowOperator.StatusTextTracer statusTextTracer;
    
    public void setUp() throws IOException {
        openDataProjects("SampleProject");
        if(node ==null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        }
        if(statusTextTracer == null) {
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    public void tearDown() {
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test performPopup method. */
    public void testPerformPopup() {
        new CompileAction().performPopup(node);
    }
    
    /** Test performMenu method. */
    public void testPerformMenu() {
        new CompileAction().performMenu(node);
    }
    
    /** Test performShortcut method. */
    public void testPerformShortcut() {
        new CompileAction().performShortcut(node);
    }
    
}
