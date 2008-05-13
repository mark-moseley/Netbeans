/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class CloneTest extends JellyTestCase {

    public File projectPath;
    public PrintStream stream;
    String os_name;
    static File f;

    public CloneTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CloneTest("testCloneProject"));
        return suite;
    }

    public void testCloneProject() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 15000);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }

        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 15000);
        } finally {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
        }

        try {
            Node nodeFile;
            NbDialogOperator ndo;
            JButtonOperator bo;
            JTextFieldOperator tfo;
            OutputTabOperator oto;
            String s = TestKit.getProjectAbsolutePath(TestKit.PROJECT_NAME);
            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performMenuActionNoBlock("Versioning|Clone -");
            ndo = new NbDialogOperator("Clone Repository");
            bo = new JButtonOperator(ndo, "Clone");
            bo.push();
            String outputTabName=s;
            System.out.println(outputTabName);
            oto = new OutputTabOperator(outputTabName);
//            oto.waitText("INFO: End of Clone");
            nodeFile = new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME);
            nodeFile.performMenuActionNoBlock("Versioning|Clone Other...");
            ndo = new NbDialogOperator("Clone External Repository");
            tfo = new JTextFieldOperator(ndo);
            String repoPath = "file://" + s.replace(File.separatorChar, "/".toCharArray()[0]);
            tfo.setText(repoPath);
            bo = new JButtonOperator(ndo, "Next");
            bo.push();
            bo.push();
            tfo = new JTextFieldOperator(ndo);
            tfo.setText(tfo.getText() + TestKit.CLONE_SUF_1);
            System.out.println(tfo.getText() + TestKit.CLONE_SUF_1);
            bo = new JButtonOperator(ndo, "Finish");
            bo.push();
            ndo = new NbDialogOperator("Clone Completed");
            bo = new JButtonOperator(ndo, "Open");
            System.out.println(bo.getText());
            bo.push();
            outputTabName=repoPath;
            System.out.println(outputTabName);
            oto = new OutputTabOperator(outputTabName);
//            oto.waitText("INFO: End of Clone");

        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            // do not remove it as following tests will work on the project
//            TestKit.closeProject(PROJECT_NAME);
        }
    }
}

