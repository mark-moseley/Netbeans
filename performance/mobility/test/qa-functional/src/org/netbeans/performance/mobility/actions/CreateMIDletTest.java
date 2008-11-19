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
package org.netbeans.performance.mobility.actions;

import junit.framework.Test;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;


import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 * Test Create MIDlet
 *
 * @author  rashid@netbeans.org
 */
public class CreateMIDletTest extends PerformanceTestCase {

    private NewFileNameLocationStepOperator location;
    private static String testProjectName = "MobileApplicationVisualMIDlet";

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     */
    public CreateMIDletTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    /**
     * Creates a new instance of CreateMIDlet
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateMIDletTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    @Override
    public void initialize() {
        //ProjectSupport.openProject(CommonUtilities.getProjectsDir() + testProjectName);
        new CloseAllDocumentsAction().performAPI();
        
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_EXPLORER_TREE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);
    }

    public void prepare() {

        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();

        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("MIDP"); //NOI18N
        wizard.selectFileType("MIDlet"); //NOI18N
        wizard.next();

        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText("MIDlet_" + System.currentTimeMillis());
    }

    public ComponentOperator open() {
        location.finish();
        return null;
    }

    @Override
    protected void shutdown() {
        //ProjectSupport.closeProject(testProjectName);
    }

    public static Test suite() {
        prepareForMeasurements();

        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CreateMIDletTest.class)
            .addTest("measureTime")
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );
    }
}
