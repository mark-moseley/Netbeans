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

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.RuntimeViewAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test opening Runtime Tab .
 *
 * @author  anebuzelsky@netbeans.org
 */
public class RuntimeWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of RuntimeWindow */
    public RuntimeWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of RuntimeWindow */
    public RuntimeWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Runtime from the main menu
        new RuntimeViewAction().performMenu();
        return new RuntimeTabOperator();
    }
    
    public void close() {
        // close the tab
        ((RuntimeTabOperator)testedComponentOperator).close();
    }
    
}
