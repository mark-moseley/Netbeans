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


package gui.memory_usage;

import org.netbeans.junit.NbTestSuite;
import gui.menu.*;
import gui.window.*;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureMemoryUsage_action_4 {
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new EmptyTestCase("measureMemoryUsage", "Empty test case"));
        
        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
        suite.addTest(new CloseEditor("testClosing20kBFormFile", "Close Form file (20kB)"));
        
//TODO 5.0 still causes a lot of failures in the following tests        suite.addTest(new CloseAllEditors("testClosingAllJavaFiles", "Close All Documents if 10 Java files opened"));
        
        suite.addTest(new CloseEditorTab("testClosingTab", "Close on tab from Editor window"));
        
        suite.addTest(new CloseEditorModified("testClosingModifiedJavaFile", "Close modified Java file"));
        
        suite.addTest(new SaveModifiedFile("testSaveModifiedJavaFile", "Save modified Java file"));

        return suite;
    }
    
}
