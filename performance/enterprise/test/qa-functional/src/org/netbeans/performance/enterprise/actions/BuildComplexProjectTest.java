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

package org.netbeans.performance.enterprise.actions;

import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildProjectAction;
import org.netbeans.jellytools.actions.CleanProjectAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Build Complex Project
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class BuildComplexProjectTest extends PerformanceTestCase {
    private String project_name = "TravelReservationServiceApplication";
    private Node projectNode;
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     */
    public BuildComplexProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
        MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
    }
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public BuildComplexProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
        MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(BuildComplexProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void close() {
        super.close();
        if (projectNode != null) {
            new CleanProjectAction().performPopup(projectNode);
            MainWindowOperator.getDefault().waitStatusText("Finished building"); // NOI18N
        }
        projectNode = null;
    }
    
    public void prepare(){
//        new CloseAllDocumentsAction().performAPI();
    }
    
    public ComponentOperator open(){
        projectNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        new BuildProjectAction().performPopup(projectNode);
        Timeouts temp = JemmyProperties.getProperties().getTimeouts().cloneThis();
        JemmyProperties.getProperties().getTimeouts().setTimeout("Waiter.WaitingTime", expectedTime * 5);
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (jbi-build)"); // NOI18N
        JemmyProperties.setCurrentTimeouts(temp);
        return null;
    }
    
 
}