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

import org.netbeans.test.oo.gui.jelly.propertyEditors.customizers.StringArrayCustomizer;

import org.netbeans.junit.NbTestSuite;



/**
 * Tests of StringArray Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_StringArray extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private final String ADD = "Add:";
    private final String REMOVE = "Remove:";
    private final String EDIT = "Edit:";
    private final String UP = "Up:";
    private final String DOWN = "Down:";
    
    private final String EE = "; ";
    
    /** Creates a new instance of PropertyType_StringArray */
    public PropertyType_StringArray(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "p_stringArray";
        useForm = true;
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_StringArray("testByInPlace"));
        suite.addTest(new PropertyType_StringArray("testCustomizerCancel"));
        suite.addTest(new PropertyType_StringArray("testCustomizerAdd"));
        suite.addTest(new PropertyType_StringArray("testCustomizerRemove"));
        suite.addTest(new PropertyType_StringArray("testCustomizerEdit"));
        suite.addTest(new PropertyType_StringArray("testCustomizerUp"));
//        suite.addTest(new PropertyType_StringArray("testCustomizerDown"));
        return suite;
    }
    
    
    public void testCustomizerAdd() {
        propertyValue_L = ADD + "add";
        propertyValueExpectation_L = "remove, down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerEdit() {
        propertyValue_L = EDIT + "edit" + EE + "newEdit";
        propertyValueExpectation_L = "down, up, newEdit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + "remove";
        propertyValueExpectation_L = "down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerUp() {
        propertyValue_L = UP + "up";
        propertyValueExpectation_L = "up, down, newEdit, add";
        waitDialog = false;
        lastTest = true;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerDown() {
        propertyValue_L = DOWN + "down";
        propertyValueExpectation_L = "up, newEdit, down, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = ADD + "cancel";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "remove, down, up, edit";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void setCustomizerValue() {
        StringArrayCustomizer customizer = new StringArrayCustomizer(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADD)){
            customizer.addItem(getItem(propertyValue_L,ADD));
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            customizer.removeItem(getItem(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(EDIT)){
            customizer.editItem(getItem(propertyValue_L,EDIT), getItem(propertyValue_L,EE));
        }
        
        if(propertyValue_L.startsWith(UP)){
            customizer.moveUpItem(getItem(propertyValue_L,UP));
        }
        
        if(propertyValue_L.startsWith(DOWN)){
            customizer.moveDownItem(getItem(propertyValue_L,DOWN));
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    private String getItem(String str, String delim) {
        int first = str.indexOf(delim);
        int end = str.indexOf(EE);

        if(end > 0 && !delim.equals(EE)){
            return str.substring(delim.length(), end);
        } else {
            return str.substring(first + delim.length());
        }
    }
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_StringArray.class));
        junit.textui.TestRunner.run(suite());
    }
    
    
}
