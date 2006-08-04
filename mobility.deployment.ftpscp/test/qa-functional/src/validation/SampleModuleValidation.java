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

package validation;
import java.awt.Point;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;

/**
 */
public class SampleModuleValidation extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public SampleModuleValidation(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SampleModuleValidation("testT1"));
        suite.addTest(new SampleModuleValidation("testT2"));
        suite.addTest(new SampleModuleValidation("testT3"));
        suite.addTest(new SampleModuleValidation("testT4"));
        suite.addTest(new SampleModuleValidation("testT5"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        //junit.textui.TestRunner.run(suite());
        // run only selected test case
        junit.textui.TestRunner.run(new SampleModuleValidation("testT3"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void tearDown() {
    }
    
    public void testT1() {
        JFrameOperator frame = new JFrameOperator();
        Point p = frame.getLocation();
        frame.setLocation(p.x+35, p.y+35);
    }
    
    public void testT2() {
        JFrameOperator frame = new JFrameOperator();
        Point p = frame.getLocation();
        frame.setLocation(p.x-35, p.y-35);
    }
    
    public void testT3() throws Exception {
        new ActionNoBlock("Help|About", null).perform();
        Thread.sleep(1000);
        new NbDialogOperator("About").close();
    }
    
    
    public void testT4() {
        TopComponentOperator tco = new TopComponentOperator("Projects");
    }
    
    public void testT5() {
        throw new JemmyException("Failed because of ...");
    }
    
}
