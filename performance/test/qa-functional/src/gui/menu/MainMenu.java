/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;


import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test of application main menu.</p>
 * <p>Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 * @author mmirilovic@netbeans.org
 */
public class MainMenu extends testUtilities.PerformanceTestCase {
    
    protected static String menuPath;
    
    private JMenuBarOperator menuBar;
    
    private JMenuOperator testedMenu;
    
    /** Creates a new instance of MainMenu */
    public MainMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
    }
    
    
    /** Creates a new instance of MainMenu */
    public MainMenu(String testName, String performanceDataName) {
        this(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        setTestCaseName(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MainMenu("testFileMenu", "File main menu"));
        suite.addTest(new MainMenu("testEditMenu", "Edit main menu"));
        suite.addTest(new MainMenu("testViewMenu", "View main menu"));
        suite.addTest(new MainMenu("testBuildMenu", "Build main menu"));
        suite.addTest(new MainMenu("testRunMenu", "Debug main menu"));
        suite.addTest(new MainMenu("testRefactoringMenu", "Refactoring main menu"));
        suite.addTest(new MainMenu("testVersioningMenu", "Versioning main menu"));
        suite.addTest(new MainMenu("testWindowMenu", "Window main menu"));
        suite.addTest(new MainMenu("testHelpMenu", "Help main menu"));
        return suite;
    }
    
    public void testFileMenu(){
        WAIT_AFTER_PREPARE = 1000;
        testMenu("org.netbeans.core.Bundle","Menu/File");
    }
    
    public void testEditMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/Edit");
    }
    
    public void testViewMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/View");
    }
    
    public void testBuildMenu(){
        testMenu("org.netbeans.modules.project.ui.Bundle","Menu/BuildProject");
    }
    
    public void testRunMenu(){
        testMenu("org.netbeans.modules.project.ui.Bundle","Menu/RunProject");
    }
    
    public void testRefactoringMenu(){
        testMenu("org.netbeans.modules.refactoring.ui.Bundle","LBL_Action");
    }
    
    public void testVersioningMenu(){
        testMenu("org.netbeans.modules.vcscore.actions.Bundle","Menu/Versioning");
    }
    
    public void testWindowMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/Window");
    }
    
    public void testHelpMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/Help");
    }
    
    
    protected void testMenu(String menu){
        menuPath = menu;
        doMeasurement();
    }
    
    
    protected void testMenu(String bundle, String menu) {
        menuPath = org.netbeans.jellytools.Bundle.getStringTrimmed(bundle,menu);
        doMeasurement();
    }
    
    public void prepare(){
    }
    
    public ComponentOperator open(){
        menuBar.pushMenu(menuPath,"|");
        return testedMenu;
    }

    /**
     * Prepare method have to contain everything that needs to be done prior to
     * repeated invocation of test.
     * Default implementation is empty.
     */
    protected void initialize() {
        menuBar = MainWindowOperator.getDefault().menuBar();
        testedMenu = new JMenuOperator(MainWindowOperator.getDefault());
    }
}
