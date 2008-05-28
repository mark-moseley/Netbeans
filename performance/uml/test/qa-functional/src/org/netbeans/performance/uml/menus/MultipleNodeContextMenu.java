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


package org.netbeans.performance.uml.menus;

import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import javax.swing.tree.TreePath;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.performance.uml.UMLUtilities;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class MultipleNodeContextMenu extends PerformanceTestCase {

    private static String testProjectName = "jEdit-Model";
    private static String testDiagramName = "ClassDiagram";
    private Node diag;

    /** Creates a new instance of MultipleNodeContextMenu*/
    public MultipleNodeContextMenu(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 2000;
        WAIT_AFTER_OPEN = 4000;
    }

    public MultipleNodeContextMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 2000;
        WAIT_AFTER_OPEN = 4000;
    }

    @Override
    public void initialize() {
        log(":: initialize");

        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }

    public void prepare() {
        log(":: prepare");
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        diag = new Node(pNode, "Model" + "|" + testDiagramName);
        diag.select();
        new EventTool().waitNoEvent(1000);
    }

    public ComponentOperator open() {
        log("::open");

        JTreeOperator projectTree = new ProjectsTabOperator().tree();
        new EventTool().waitNoEvent(1000);

        TreePath[] arrTreePath;
        arrTreePath = new TreePath[110];
        for (int i = 0; i < arrTreePath.length; i++) {
            arrTreePath[i] = projectTree.getPathForRow(3 + i);
        }
        log("::after array");
        projectTree.callPopupOnPaths(arrTreePath);

        return null;
    }

    @Override
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }

    @Override
    public void close() {
        log("::close");
        new CloseAllDocumentsAction().performAPI();
    }

//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new MultipleNodeContextMenu("measureTime"));
//    }
}
