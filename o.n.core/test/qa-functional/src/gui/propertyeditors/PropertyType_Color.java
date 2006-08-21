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

package gui.propertyeditors;

import org.netbeans.jellytools.properties.ColorProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jellytools.properties.editors.ColorCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Color Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Color extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Boolean */
    public PropertyType_Color(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Color";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Color("verifyCustomizer"));
        suite.addTest(new PropertyType_Color("testCustomizerCancel"));
        suite.addTest(new PropertyType_Color("testCustomizerOk"));
        suite.addTest(new PropertyType_Color("testByInPlace"));
        suite.addTest(new PropertyType_Color("testByInPlaceInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "50,50,50";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "100,100,100";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "20,10,100";
        propertyValueExpectation_L = "["+propertyValue_L+"]";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void testByInPlaceInvalid(){
        propertyValue_L = "xx color";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = true;
        setByInPlace(propertyName_L, propertyValue_L, false);
    }
    
    public void verifyCustomizer(){
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ColorCustomEditorOperator customizer = new ColorCustomEditorOperator(propertyCustomizer);
        
        int first_comma = propertyValue_L.indexOf(',');
        int second_comma = propertyValue_L.indexOf(',',first_comma+1);
        
        int r = new Integer(propertyValue_L.substring(0,first_comma)).intValue();
        int g = new Integer(propertyValue_L.substring(first_comma+1, second_comma)).intValue();
        int b = new Integer(propertyValue_L.substring(second_comma+1)).intValue();
        
        customizer.setRGBValue(r,g,b);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public String getValue(String propertyName) {
        String returnValue;
        PropertySheetOperator propertiesTab = new PropertySheetOperator(propertiesWindow);
        
        returnValue = new ColorProperty(propertiesTab, propertyName_L).getValue();
        err.println("GET VALUE = [" + returnValue + "].");
        
        // hack for color poperty, this action expects, that right value is displayed as tooltip
        //        returnValue = new Property(propertiesTab, propertyName_L).valueButtonOperator().getToolTipText();
        //        err.println("GET VALUE TOOLTIP = [" + returnValue + "].");
        
        return returnValue;
    }
    
    public void verifyCustomizerLayout() {
        ColorCustomEditorOperator customizer = new ColorCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Color.class));
        junit.textui.TestRunner.run(suite());
    }
}
