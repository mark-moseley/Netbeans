/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui;

import gui.setup.WebSetupTest;

import org.netbeans.junit.NbTestSuite;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests and installs Application server instance
 *
 * @author  rkubacki@netbeans.org, mmirilovic@netbeans.org, mkhramov@netbeans.org
 */
public class VWPMeasuringSetup extends NbTestSuite {

    public VWPMeasuringSetup (java.lang.String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Setup suite for Visual Web Pack");

        suite.addTest(new WebSetupTest("closeMemoryToolbar"));
        suite.addTest(new WebSetupTest("closeUIGesturesToolbar"));
        
        suite.addTest(new WebSetupTest("closeWelcome"));
        
        // server is registered from command line
        //suite.addTest(new WebSetupTest("setupAppServer"));
        
        suite.addTest(new WebSetupTest("openWebPackProject"));
        
        suite.addTest(new WebSetupTest("closeAllDocuments"));
        
        return suite;
    }
    
}