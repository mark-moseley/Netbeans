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

package gui.window;

import gui.window.windowOperators.ProxyConfiguration;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.WizardOperator;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Proxy Configuration.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ProxyConfiguration extends testUtilities.PerformanceTestCase {
    
    JButtonOperator openProxyButton;
    WizardOperator wizard;
    
    /** Creates a new instance of ValidateProxyConfiguration */
    public ProxyConfiguration(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidateProxyConfiguration */
    public ProxyConfiguration(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    protected void initialize() {
        // open the Update Center wizard
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Tools|Update Center","|");
        wizard = new WizardOperator("Update Center Wizard");
        openProxyButton = new JButtonOperator(wizard, "Proxy Configuration");
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        // invoke the action
        openProxyButton.pushNoBlock();
        return new org.netbeans.jellytools.NbDialogOperator("Proxy Configuration");
    }

    protected void shutdown() {
        // close the wizard
        wizard.close();
    }
}
