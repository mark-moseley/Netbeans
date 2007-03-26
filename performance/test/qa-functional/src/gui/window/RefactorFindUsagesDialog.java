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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Find Usages Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class RefactorFindUsagesDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private static Node testNode;
    private String TITLE, ACTION;

    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsagesDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsagesDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.ui.Bundle";
        TITLE = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsed");  // "Find Usages"
        ACTION = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsedAction"); // "Find Usages..."
        testNode = new Node(new SourcePackagesNode("jEdit"),"org.gjt.sp.jedit|jEdit.java");
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Find Usages from the popup menu
        testNode.performPopupAction(ACTION);
        return new NbDialogOperator(TITLE);
    }
    
}
