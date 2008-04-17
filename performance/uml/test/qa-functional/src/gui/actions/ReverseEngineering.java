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
package gui.actions;

import java.io.File;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;

import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 * Test Reverse Engineering 
 *
 * @author  rashid@netbeans.org
 */
public class ReverseEngineering extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private static String testProjectName = "jEdit";
    private static long suffix;

    /**
     * Creates a new instance of ReverseEngineering
     * @param testName the name of the test
     */
    public ReverseEngineering(String testName) {
        super(testName);
        expectedTime = 300000;
        WAIT_AFTER_OPEN = 4000;
    }

    /**
     * Creates a new instance of ReverseEngineering
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ReverseEngineering(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 300000;
        WAIT_AFTER_OPEN = 4000;
    }

    @Override
    public void initialize() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir") + File.separator + testProjectName);
        System.gc();
        new EventTool().waitNoEvent(3000);
    }

    public void prepare() {
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();
        pNode.performPopupAction("Reverse Engineer...");

        new EventTool().waitNoEvent(1000);
        NbDialogOperator reng = new NbDialogOperator("Reverse Engineer");
        JRadioButtonOperator newUMLradio = new JRadioButtonOperator(reng, "Create New UML Project");
        newUMLradio.push();
        new EventTool().waitNoEvent(1000);
        suffix = System.currentTimeMillis();
        JTextFieldOperator textProject = new JTextFieldOperator(reng, 2);
        textProject.clearText();
        textProject.typeText("jEdit-Model_" + suffix);
        reng.ok();
    }

    public ComponentOperator open() {
        OutputTabOperator asot = new OutputOperator().getOutputTab("Reverse Engineering");
        asot.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 500000);
        asot.waitText("Task Successful");
        return null;
    }

    @Override
    public void close() {
        ProjectSupport.closeProject("jEdit-Model_" + suffix);
        System.gc();
        new EventTool().waitNoEvent(3000);

    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new ReverseEngineering("measureTime"));
    }
}
