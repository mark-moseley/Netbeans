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

import gui.Utilities;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Override and Implement Methods dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class OverrideMethods extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private String MENU, TITLE;
    
    /** Creates a new instance of OverrideMethods */
    public OverrideMethods(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of OverrideMethods */
    public OverrideMethods(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Source") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.java.tools.Bundle","LAB_OverrideTool");
        TITLE = Bundle.getStringTrimmed("org.netbeans.modules.java.tools.Bundle","LBL_OverridePanel2_Title");
        
        // open a java file in the editor
        editor = Utilities.openFile("jEdit","bsh","Parser.java", true);
        waitNoEvent(5000);  // annotations, folds, toolbars, ...
    }
    
    public void prepare() {
        editor.makeComponentVisible();
        editor.setCaretPositionToLine(31);
    }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }
    
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new OverrideMethods("measureTime"));
    }
}
