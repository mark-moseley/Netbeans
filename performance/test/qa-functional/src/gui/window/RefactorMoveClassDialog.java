/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import gui.Utilities;

import java.io.PrintStream;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;


import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Refactor | Move Class Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class RefactorMoveClassDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE, ACTION;
    
    /** Creates a new instance of RefactorRenameDialog */
    public RefactorMoveClassDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=60000;
    }
    
    /** Creates a new instance of RefactorRenameDialog */
    public RefactorMoveClassDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=60000;
    }
    
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.ui.Bundle";
        TITLE = Bundle.getStringTrimmed(BUNDLE,"LBL_MoveClass");  // "Move Class"
        ACTION = Bundle.getStringTrimmed(BUNDLE,"LBL_Action") + "|" + Bundle.getStringTrimmed(BUNDLE,"LBL_MoveClassAction"); // "Refactor|Move Class..."
        testNode = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),Utilities.SOURCE_PACKAGES + "|org.gjt.sp.jedit|jEdit.java");
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Refactor | Move Class from the popup menu
        testNode.performPopupAction(ACTION);
        return new NbDialogOperator(TITLE);
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new RefactorMoveClassDialog("measureTime"));
    }
    
}
