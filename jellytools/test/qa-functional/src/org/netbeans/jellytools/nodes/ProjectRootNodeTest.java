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
package org.netbeans.jellytools.nodes;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.ProjectRootNodeTest
 *
 */
public class ProjectRootNodeTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ProjectRootNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ProjectRootNodeTest("testVerifyPopup"));
        suite.addTest(new ProjectRootNodeTest("testFind"));
        suite.addTest(new ProjectRootNodeTest("testBuildProject"));
        suite.addTest(new ProjectRootNodeTest("testCleanProject"));
        suite.addTest(new ProjectRootNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static ProjectRootNode projectRootNode;
    
    /** Find node. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(projectRootNode == null) {
            projectRootNode = ProjectsTabOperator.invoke().getProjectRootNode("SampleProject"); // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        projectRootNode.verifyPopup();
    }
    
    /** Test find */
    public void testFind() {
        projectRootNode.find();
        new FindInFilesOperator().close();
    }
    
    /** Test buildProject */
    public void testBuildProject() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        projectRootNode.buildProject();
        // wait status text "Building SampleProject (jar)"
        statusTextTracer.waitText("jar", true); // NOI18N
        // wait status text "Finished building SampleProject (jar).
        statusTextTracer.waitText("jar", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test cleanProject*/
    public void testCleanProject() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        projectRootNode.cleanProject();
        // wait status text "Building SampleProject (clean)"
        statusTextTracer.waitText("clean", true); // NOI18N
        // wait status text "Finished building SampleProject (clean).
        statusTextTracer.waitText("clean", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test properties */
    public void testProperties() {
        projectRootNode.properties();
        new NbDialogOperator("SampleProject").close(); //NOI18N
    }
    
}
