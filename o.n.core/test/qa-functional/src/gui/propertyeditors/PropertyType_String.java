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

import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of String Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_String extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    
    /** Creates a new instance of PropertyType_Boolean */
    public PropertyType_String(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "String";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_String("verifyCustomizer"));
        suite.addTest(new PropertyType_String("testCustomizerCancel"));
        suite.addTest(new PropertyType_String("testCustomizerOk"));
        suite.addTest(new PropertyType_String("testByInPlace"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "My String - ok";
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "My String - cancel";
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "My String - in-place";
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        customizer.setStringValue(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        String newValue = getValue(propertyName_L);
        
        if( ( !newValue.equals(propertyInitialValue) && propertyValue_L.equals(newValue)  ) && expectation) {
            log("New value is {"+newValue+"} and old value is{"+propertyInitialValue+"} - expactation is {"+expectation+"} --> PASS");
        }else if ( newValue.equals(propertyInitialValue) && !propertyValue_L.equals(newValue) && !expectation ) {
            log("New value is {"+newValue+"} and old value is{"+propertyInitialValue+"} - expactation is {"+expectation+"} --> PASS");
        }else {
            fail("New value is {"+newValue+"} and old value is{"+propertyInitialValue+"} - expactation is {"+expectation+"} --> FAIL");
        }
    }
    
    public void verifyCustomizerLayout() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_String.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
