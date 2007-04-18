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

package gui;

import org.netbeans.junit.NbTestSuite;
import gui.setup.MobilitySetupTest;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  rkubacki@netbeans.org, mmirilovic@netbeans.org
 */
public class MPMeasuringSetup extends NbTestSuite {

    public MPMeasuringSetup (java.lang.String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Setup suite for Mobility Pack");

        suite.addTest(new MobilitySetupTest("closeMemoryToolbar"));
        suite.addTest(new MobilitySetupTest("closeUIGesturesToolbar"));
        
        suite.addTest(new MobilitySetupTest("closeWelcome"));
        
        // TODO open some project
        
        suite.addTest(new MobilitySetupTest("closeAllDocuments"));
        
        return suite;
    }
    
}
