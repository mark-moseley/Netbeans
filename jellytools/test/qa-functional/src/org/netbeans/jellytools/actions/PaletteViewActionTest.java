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
package org.netbeans.jellytools.actions;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.actions.PaletteViewAction;
        
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.PaletteViewAction
 *
 * @author mmirilovic@netbeans.org
 */
public class PaletteViewActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PaletteViewActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PaletteViewActionTest("testPerformMenu"));
        return suite;
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        PaletteOperator.invoke().close();
        new PaletteViewAction().performMenu();
        new PaletteOperator().close();
    }

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
}
