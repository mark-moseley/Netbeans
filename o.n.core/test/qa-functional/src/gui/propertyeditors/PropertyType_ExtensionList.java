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

import org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Extension List Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_ExtensionList extends PropertyEditorsTest {

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
    
    /** Creates a new instance of PropertyType_ExtensionList */
    public PropertyType_ExtensionList(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Extension List";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_ExtensionList("testByInPlace"));
        suite.addTest(new PropertyType_ExtensionList("verifyCustomizer"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerCancel"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerAdd"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerRemove"));
        suite.addTest(new PropertyType_ExtensionList("testCustomizerEdit"));
        return suite;
    }
    
    
    public void testCustomizerAdd() {
        propertyValue_L = ADD + "java";
        propertyValueExpectation_L = "gif, html, java, jpg, png";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerEdit() {
        propertyValue_L = EDIT + "html" + EE + "newHtml";
        propertyValueExpectation_L = "java, jpg, newHtml, png";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + "gif";
        propertyValueExpectation_L = "html, java, jpg, png";
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
        propertyValue_L = "gif, jpg, png, html";
        propertyValueExpectation_L = "gif, html, jpg, png";
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADD)){
            customizer.add(getItem(propertyValue_L,ADD));
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            customizer.remove(getItem(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(EDIT)){
            customizer.edit(getItem(propertyValue_L,EDIT), getItem(propertyValue_L,EE));
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
    
    public void verifyCustomizerLayout() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        customizer.btAdd();
        customizer.btRemove();
        customizer.btEdit();
        customizer.lstItemList();
        customizer.txtItemText();
        customizer.btOK();
        customizer.btCancel();
    }
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_ExtensionList.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
