/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package gui.action;

import gui.VWPUtilities;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebProjectDeployment extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private String targetProject;
    private Node proj;
    private JPopupMenuOperator projectMenu;
    
    /**
     * Creates a new instance of WebProjectDeployment
     */
    public WebProjectDeployment(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public WebProjectDeployment(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public void testDeploySmallProject() {
        targetProject = "VisualWebProject";
        doMeasurement();
    }
    
    public void testDeployLargeProject() {
        targetProject = "HugeApp";
        doMeasurement();
    }
    
    public void initialize() {
        log("::initialize");
        String asName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.sun.ide.dm.Bundle", "FACTORY_DISPLAYNAME");
        log("Applications server node name from bundle is: "+asName+" ??"); 
        long start = System.currentTimeMillis();
        VWPUtilities.startApplicationServer();
        long stop = System.currentTimeMillis();
        
        log("App server started in "+(stop-start)+" ms");
        
        new ProjectsTabOperator().invoke();
        VWPUtilities.buildproject(targetProject);
        VWPUtilities.waitForPendingBackgroundTasks();
    }
    
    public void prepare() {
        log(":: prepare");
        proj = null;
        try {
            proj = new ProjectsTabOperator().getProjectRootNode(targetProject);
            proj.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        projectMenu = proj.callPopup();
    }
    
    public ComponentOperator open() {
        log(":: open");
        projectMenu.pushMenuNoBlock(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RedeployAction_Name"));
        
        VWPUtilities.waitForPendingBackgroundTasks();
        String statusText = "Finished building " + targetProject + " (run-deploy)."; // NOI18N
        log("Waiting for: '"+statusText+"' text in status bar");  
        MainWindowOperator.getDefault().waitStatusText(statusText); 
        
        return null;
    }
    
    public void close() {
        //TODO Undeploy?
    }
    
    public void shutdown() {
        log(":: shutdown ");
        VWPUtilities.stopApplicationServer();
    }
}
