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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.ServicesCustomizer;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of Service Type Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_ServiceType extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_ServiceType */
    public PropertyType_ServiceType(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_serviceType";
        useForm = false;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_ServiceType("testByCombo"));
        suite.addTest(new PropertyType_ServiceType("testCustomizerOk"));
        suite.addTest(new PropertyType_ServiceType("testCustomizerCancel"));
        return suite;
    }
    
    public void testByCombo(){
        propertyValue_L = "NetBeans Update Center Alpha";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCombo(propertyName_L, propertyValue_L, true);
    }
    
    public void testCustomizerOk(){
        propertyValue_L = "Date";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "Type";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        lastTest = true;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void setCustomizerValue() {
        ServicesCustomizer customizer = new ServicesCustomizer(propertyCustomizer);
        customizer.selectItem(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, false);
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_ServiceType.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
