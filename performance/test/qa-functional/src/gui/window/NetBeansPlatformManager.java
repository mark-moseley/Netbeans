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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of NetBeans Platform Manager invoked from main menu.
 *
 * @author  mmirilovic@netbeans.org
 */
public class NetBeansPlatformManager extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of SetupWizard */
    public NetBeansPlatformManager(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of SetupWizard */
    public NetBeansPlatformManager(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare(){
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open(){
        String menu = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.platform.Bundle","CTL_NbPlatformManager_Menu");

        MainWindowOperator.getDefault().menuBar().pushMenu(menu);
        
        return new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.platform.Bundle","CTL_NbPlatformManager_Title"));
    }
    
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NetBeansPlatformManager("measureTime"));
    }
}
