/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.propertyeditors;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of Element Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_ElementFormat extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    
    /** Creates a new instance of PropertyType_ElementFormat */
    public PropertyType_ElementFormat(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Element Format";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_ElementFormat("testByInPlace"));
        return suite;
    }
    
    public void testByInPlace(){
        propertyValue_L = "element";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    
    public void setCustomizerValue() {
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_String.class));
        junit.textui.TestRunner.run(suite());
    }
    
    public void verifyCustomizerLayout() {
    }    
    
}
