/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;

import javax.swing.JMenuItem;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test of application main menu.</p>
 * <p>Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 * @author mmirilovic@netbeans.org
 * @author Radim Kubacki
 */
public class MainSubMenus extends testUtilities.PerformanceTestCase {
    
    protected static String mainMenuPath;
    protected static JMenuOperator testedMainMenu;
    protected static String subMenuPath;
    
    /** Creates a new instance of MainSubMenus */
    public MainSubMenus(String testName) {
        super(testName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
    }
    
    
    /** Creates a new instance of MainSubMenus */
    public MainSubMenus(String testName, String performanceDataName) {
        this(testName);
        repeat = 1; // only first use is interesting
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
        setTestCaseName(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MainSubMenus("testFileOpenRecentProjectMenu", "File | Open Recent Project main menu"));
        suite.addTest(new MainSubMenus("testFileSetMainProjectMenu", "File | Set Main Project main menu"));
        suite.addTest(new MainSubMenus("testViewDocumentationIndicesMenu", "View | Documentation Indices main menu"));
        suite.addTest(new MainSubMenus("testViewToolbarsMenu", "View | Toolbars main menu"));
        suite.addTest(new MainSubMenus("testViewCodeFoldsMenu", "View | Code Folds main menu"));
        suite.addTest(new MainSubMenus("testRunRunOtherMenu", "Run | Run Other main menu"));
        suite.addTest(new MainSubMenus("testRunStackMenu", "Run | Stack main menu"));
        suite.addTest(new MainSubMenus("testVersioningCVSMenu", "Versioning | CVS main menu"));
        suite.addTest(new MainSubMenus("testVersioningPVCSMenu", "Versioning | PVCS main menu"));
        suite.addTest(new MainSubMenus("testToolsI18nMenu", "Tools | Internationalization main menu"));
        suite.addTest(new MainSubMenus("testWinGuiMenu", "Window | GUI Editing main menu"));
        suite.addTest(new MainSubMenus("testWinDebuggingMenu", "Window | Debugging main menu"));
        suite.addTest(new MainSubMenus("testWinVersioningMenu", "Window | Versioning main menu"));
        suite.addTest(new MainSubMenus("testWinSelectDocumentNodeInMenu", "Window | Select Document in main menu"));
        return suite;
    }
    
    //TODO open more than one project (nice to have open 10 projects) and close 5 projects
    public void testFileOpenRecentProjectMenu(){
        testSubMenu("org.netbeans.core.Bundle","File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_RecentProjectsAction_Name");
    }
    
    //TODO open more than one project (nice to have open 10 projects)
    public void testFileSetMainProjectMenu(){
        testSubMenu("org.netbeans.core.Bundle","File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetMainProjectAction_Name");
    }

    //TODO open form file
    public void testViewEditorsMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.core.multiview.Bundle", "CTL_EditorsAction");
    }
    
    //TODO open java file
    public void testViewCodeFoldsMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
    }
    
    public void testViewDocumentationIndicesMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.modules.javadoc.search.Bundle", "CTL_INDICES_MenuItem");
    }
    
    public void testViewToolbarsMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction");
    }
    
    public void testRunStackMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "RunProject", "Stack"); // this can't be localized
    }
    
    public void testRunRunOtherMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "RunProject", "org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    }
    
    public void testVersioningCVSMenu(){
        testSubMenu("org.netbeans.modules.vcscore.actions.Bundle","Versioning", "org.netbeans.modules.vcs.profiles.cvsprofiles.config.Bundle", "CVS");
    }
    
    public void testVersioningPVCSMenu(){
        testSubMenu("org.netbeans.modules.vcscore.actions.Bundle","Versioning", "org.netbeans.modules.vcs.profiles.pvcs.config.Bundle", "PVCS");
    }
    
    public void testToolsI18nMenu(){
        testSubMenu("org.netbeans.core.Bundle","Tools", "org.netbeans.modules.i18n.Bundle", "LBL_I18nGroupActionName");
    }
    
    public void testWinGuiMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.modules.form.resources.Bundle", "Menu/Window/Form");
    }
    
    public void testWinDebuggingMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    }
    
    public void testWinVersioningMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.modules.vcscore.Bundle", "Menu/Window/Versioning");
    }
    
    //TODO open java file
    public void testWinSelectDocumentNodeInMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.core.actions.Bundle", "Menu/Window/SelectDocumentNode");
    }
    
    private void testSubMenu(String mainMenu, String subMenu){
        mainMenuPath = mainMenu;
        subMenuPath = subMenu;
        doMeasurement();
    }
    
    private void testSubMenu(String bundle, String mainMenu, String subMenu) {
        mainMenuPath = getFromBundle(bundle,"Menu/"+mainMenu);
        subMenuPath = subMenu;
        doMeasurement();
    }
    
    private void testSubMenu(String bundle, String mainMenu, String bundle_2, String subMenu) {
        mainMenuPath = getFromBundle(bundle,"Menu/"+mainMenu);
        subMenuPath = getFromBundle(bundle_2,subMenu);
        doMeasurement();
    }
    
    private String getFromBundle(String bundle, String key){
        return org.netbeans.jellytools.Bundle.getStringTrimmed(bundle,key);
    }
    
    public void prepare(){
        MainWindowOperator.getDefault().menuBar().pushMenu(mainMenuPath,"|");
        testedMainMenu = new JMenuOperator(MainWindowOperator.getDefault());
    }
    
    public ComponentOperator open(){
        JMenuItem submenu = testedMainMenu.findJMenuItem(testedMainMenu.getContainers()[0], subMenuPath, false, false);
        assertNotNull("Can not find "+subMenuPath+" menu item", submenu);
        JMenuItemOperator mio = new JMenuItemOperator (submenu);
        
        MouseDriver mdriver = org.netbeans.jemmy.drivers.DriverManager.getMouseDriver(mio);
        mdriver.moveMouse(mio, mio.getCenterXForClick(), mio.getCenterYForClick());
//        mio.pushKey(java.awt.event.KeyEvent.VK_RIGHT);
        return mio;
    }
    
    public void close() {
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
}
