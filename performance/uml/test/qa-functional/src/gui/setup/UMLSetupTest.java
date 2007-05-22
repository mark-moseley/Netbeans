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

package gui.setup;

import gui.UMLUtilities;

import java.io.File;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  mmirilovic@netbeans.org
 */
public class UMLSetupTest extends IDESetupTest {
    
    public UMLSetupTest(java.lang.String testName) {
        super(testName);
    }
    
    public void openReservationPartnerServicesProject() {
        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + "TravelReservationService" + File.separator + "ReservationPartnerServices");
    }
    
    public void openTravelReservationServiceProject() {
        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + "TravelReservationService" + File.separator + "TravelReservationService");
    }
    
    public void openTravelReservationServiceApplicationProject() {
        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + "TravelReservationService" + File.separator + "TravelReservationServiceApplication");
    }
    
    public void openSoaTestProject() {
        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + "SOATestProject");
    }
    
    public void openBPELTestProject() {
        UMLUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir") + File.separator + "BPELTestProject");
    }
}
