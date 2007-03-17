/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildProjectAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test Build Complex Project
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class BuildComplexProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private String project_name = "TravelReservationServiceApplication";
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     */
    public BuildComplexProject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public BuildComplexProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }
    
    public void prepare(){
        new CloseAllDocumentsAction().performAPI();
    }
    
    public ComponentOperator open(){
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        new BuildProjectAction().performPopup(projectNode);
        
        MainWindowOperator.getDefault().waitStatusText("Finished building"); // NOI18N
        return null;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new BuildComplexProject("measureTime"));
    }
}