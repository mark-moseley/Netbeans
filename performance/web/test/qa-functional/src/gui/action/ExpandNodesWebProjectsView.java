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

package gui.action;

import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.test.web.performance.WebPerformanceTestCase;

import org.netbeans.performance.test.utilities.PerformanceTestCase;
/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ExpandNodesWebProjectsView extends PerformanceTestCase {
    /** Name of the folder which test creates and expands */
    private static String project;
    /** Path to the folder which test creates and expands */
    private static String pathToFolderNode;
    /** Node represantation of the folder which test creates and expands */
    private static Node nodeToBeExpanded;
    /** Projects tab */
    private static ProjectsTabOperator projectTab;
    /** Project with data for these tests */
    private static String testDataProject = "PerformanceTestFolderWebApp";
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     */
    public ExpandNodesWebProjectsView(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
//        init();
    }
    
    /**
     * Creates a new instance of ExpandNodesInExplorer
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesWebProjectsView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
//        init();
    }
    
    public void testExpandProjectNode(){
        pathToFolderNode = "";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        doMeasurement();
    }

    public void testExpandSourcePackagesNode(){
        pathToFolderNode = "Source Packages";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        doMeasurement();
    }
    
    public void testExpandFolderWith50JspFiles(){
        pathToFolderNode = "Web Pages|jsp50";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        doMeasurement();
    }
    
    public void testExpandFolderWith100JspFiles(){
        pathToFolderNode = "Web Pages|jsp100";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        doMeasurement();
    }
    
    public void initialize(){
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode("TestWebProject").collapse();
        projectTab.getProjectRootNode(testDataProject).collapse();
        System.setProperty("perf.dont.resolve.java.badges", "true");
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
    }
        
    public void prepare() {
        if(pathToFolderNode.equals(""))
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        else
	    nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        //repaintManager().setOnlyExplorer(true);

//        nodeToBeExpanded.select();
    }
    
    public ComponentOperator open(){
//workaround for starting event:
//        nodeToBeExpanded.tree().clickMouse(1);

        nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
        nodeToBeExpanded.expand();
        return null;
    }
    
    public void close(){
        //repaintManager().setOnlyExplorer(true);
        nodeToBeExpanded.collapse();
    }
    
    public void shutdown() {
        super.shutdown();
        repaintManager().resetRegionFilters();
        System.setProperty("perf.dont.resolve.java.badges", "false");
        projectTab.getProjectRootNode(testDataProject).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }
}
