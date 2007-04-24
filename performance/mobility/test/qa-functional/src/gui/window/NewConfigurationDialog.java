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

package gui.window;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author Administrator
 */
public class NewConfigurationDialog  extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private Node testNode;    
    private String targetProject, TITLE;
    
    public NewConfigurationDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        targetProject = "MobileApplicationVisualMIDlet";              
        
    }
    public NewConfigurationDialog(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        targetProject = "MobileApplicationVisualMIDlet";              
    }
    public void initialize() {
        log(":: initialize");
        String projectConfNodeName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.mobility.project.ui.Bundle", "LBL_ProjectConfigurations");
        testNode = new Node(new ProjectsTabOperator().getProjectRootNode(targetProject),projectConfNodeName);
        testNode.select();
        
    }
    public void prepare() {
        log(":: prepare");
    }

    public ComponentOperator open() {
        log(":: open");
        String cmdName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.mobility.project.ui.customizer.Bundle", "LBL_VCS_AddConfiguration");
        new ActionNoBlock(null,cmdName).performPopup(testNode);
        return new NbDialogOperator(cmdName);
    }
    
    public void close() {
        log(":: close");
        ((NbDialogOperator)testedComponentOperator).close();        
    }

}
