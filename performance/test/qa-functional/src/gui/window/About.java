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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of About dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class About extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected String MENU, ABOUT, DETAIL;

    /** Creates a new instance of About */
    public About(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of About */
    public About(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
        ABOUT = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Title");
        DETAIL = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Detail");
        waitNoEvent(2000);
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        // invoke Help / About from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new org.netbeans.jellytools.NbDialogOperator(ABOUT);
    }
    
}
