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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of About dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class About_2 extends ValidateAbout {
    About about;
    
    /** Creates a new instance of ValidateAbout_2 */
    public About_2(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidateAbout_2 */
    public About_2(String testName, String performanceDataName) {
        super(testName, performanceDataName);    
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare(){
        about = (About)super.open();
    }
    
    public ComponentOperator open(){
        about.selectPageDetail();
        return new org.netbeans.jellytools.NbDialogOperator("About");
    }
    
}
