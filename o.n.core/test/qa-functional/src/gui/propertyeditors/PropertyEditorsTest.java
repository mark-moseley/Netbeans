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

import gui.propertyeditors.data.PropertiesTest;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.DialogOperator;

import org.netbeans.jemmy.operators.FrameOperator;
import org.netbeans.jemmy.operators.Operator;


/**
 * JellyTestCase test case with implemented Property Editors Test support stuff
 *
 * @author  mmirilovic@netbeans.org
 */
public abstract class PropertyEditorsTest extends JellyTestCase {
    
    protected static PrintStream err;
    protected static PrintStream log;
    
    public String propertyInitialValue;
    public String propertyValue;
    
    protected static NbDialogOperator propertyCustomizer;
    
    protected static NbDialogOperator propertiesWindow = null;
    
    private static final String CAPTION = "\n===========================";
    
    /** Creates a new instance of PropertyEditorsTest */
    public PropertyEditorsTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() {
        //err = System.out;
        err = getLog();
        log = getRef();
        
        try {
            JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, true), null));
            initializeWorkplace();
        }catch(Exception exc) {
            failTest(exc, "SetUp failed. It seems like initializeWorkplace cause exception:"+exc.getMessage());
        }
    }
    
    /** Open Property Customizer for <b>propertyName</b>, set value by customizer and press Ok button, verify value with <b>expectance</b>.
     * @param propertyName name of property to be customized
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */    
    public void setByCustomizerOk(String propertyName, boolean expectance){
        try {
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.ok();
            
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizer("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Open Property Customizer for <b>propertyName</b>, set value by customizer and press Cancel button, verify value with <b>expectance</b>.
     * @param propertyName name of property to be customized
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */    
    public void setByCustomizerCancel(String propertyName, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();

            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizerCancel("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set value <b>propertyValue</b> of property <b>propertyName</b> by in-place, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValue new value of property 
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */    
    public void setByInPlace(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            ((TextFieldProperty) findProperty(propertyName, "TextFieldProperty")).setValue(propertyValue);
            
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByInPlace("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set value <b>propertyValue</b> of property <b>propertyName</b> by combobox, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValue new value of property 
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */    
    public void setByCombo(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            ((ComboBoxProperty) findProperty(propertyName,"ComboBoxProperty")).setValue(propertyValue);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set indexed value <b>propertyValueIndex</b> of property <b>propertyName</b> by combobox, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValueIndex index of new value in combobox
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */    
    public void setByCombo(String propertyName, int propertyValueIndex, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"} .");
            propertyInitialValue = getValue(propertyName);
            
            ((ComboBoxProperty) findProperty(propertyName, "ComboBoxProperty")).setValue(propertyValueIndex);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValueIndex+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Verify customizer layout for property <b>propertyName</b>.
     * @param propertyName name of property to be changed
     */    
    public void verifyCustomizer(String propertyName){
        try {
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"} .");
            openAndGetPropertyCustomizer(propertyName);
            verifyCustomizerLayout();

            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"}  - finished.");
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: Verification of Property Customizer Layout for property("+propertyName+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Open property customizer for property <b>propertyName</b>.
     * @param propertyName name of property to be changed
     * @return Property Customizer 
     */    
    public static NbDialogOperator openAndGetPropertyCustomizer(String propertyName) {
        findProperty(propertyName, "").openEditor();
        propertyCustomizer = findPropertyCustomizer(propertyName);
        return propertyCustomizer;
    }
    
    /** Return Property Customizer.
     * @return Property Customizer.
     */    
    public NbDialogOperator getPropertyCustomizer() {
        return propertyCustomizer;
    }
    
    /** Return Informational dialog
     * @return Informational dialog
     */    
    public NbDialogOperator getInformationDialog() {
        String title = org.netbeans.jellytools.Bundle.getString("org.openide.Bundle", "NTF_InformationTitle");
        
        err.println(CAPTION + " Waiting dialog {"+title+"} .");
        NbDialogOperator dialog = new NbDialogOperator(title);
        err.println(CAPTION + " Waiting dialog {"+title+"} - finished.");
        return dialog;
    }
    
    /** Get value of property <b>propertyName</b>
     * @param propertyName name of property asked for value
     * @return value of property 
     */    
    public String getValue(String propertyName) {
        String returnValue = findProperty(propertyName, "").getValue();
        err.println("GET VALUE = [" + returnValue + "].");
        return returnValue;
    }
    
    /** Find Property Cusotmizer by name of property <b>propertyName</b>
     * @param propertyName name of property 
     * @return founded Property Customizer
     */    
    private static NbDialogOperator findPropertyCustomizer(String propertyName){
        return new NbDialogOperator(propertyName);
    }
    
    /** Verify exceptation value.
     * @param propertyName name of property 
     * @param expectation true - expected value must be the same as new value, false - expected value should not be the same
     * @param propertyValueExpectation expected value
     * @param propertyValue new value
     * @param waitDialog true - after changing value Informational dialog about impissibility to set invalid value arise
     */    
    public void verifyExpectationValue(String propertyName, boolean expectation, String propertyValueExpectation, String propertyValue, boolean waitDialog){
        
        // Dialog isn't used for informing user about Invalid new value: Class,
        if(waitDialog) {
            getInformationDialog().ok();
            err.println(CAPTION + " Dialog closed by [Ok].");
            
            if(propertyCustomizer!=null && propertyCustomizer.isShowing()){
                err.println(CAPTION + " Property Customizer is still showing.");
                propertyCustomizer.cancel();
                err.println(CAPTION + " Property Customizer closed by [Cancel].");
            }
            
        }
        
        String newValue = getValue(propertyName);
        String log = "Actual value is {"+newValue+"} and initial is{"+propertyInitialValue+"} - set value is {"+propertyValue+"} / expectation value is {"+propertyValueExpectation+"}";
        
        err.println(CAPTION + " Trying to verify value ["+log+"].");
        
        if(expectation){
            if(newValue.equals(propertyValueExpectation) ) {
                log(log + " --> PASS");
            }else {
                fail(log + " --> FAIL");
            }
        }else {
            if(newValue.equals(propertyInitialValue)){
                log(log + " --> PASS");
            }else{
                fail(log + " --> FAIL");
            }
            
        }
    }
    
    
    /** Reinitialize Workplace. */
    public static NbDialogOperator reInitializeWorkplace() {
        propertiesWindow = null;
        return openPropertySheet();
    }
    
    /** Initialize Workplace. */
    public static NbDialogOperator initializeWorkplace() {
        return openPropertySheet();
    }
    
    /** Open property sheet (bean customizer). */
    private static NbDialogOperator openPropertySheet() {
        String waitFrameTimeout = "FrameWaiter.WaitFrameTimeout";
        long findTimeout = JemmyProperties.getCurrentTimeout(waitFrameTimeout);
        JemmyProperties.setCurrentTimeout(waitFrameTimeout, 3000);

        try{
            propertiesWindow = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }catch(org.netbeans.jemmy.TimeoutExpiredException exception){
            new PropertiesTest();
            propertiesWindow = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }
        
        JemmyProperties.setCurrentTimeout(waitFrameTimeout, findTimeout);
        
        return propertiesWindow;
    }
    
    
     /** Find Property in Property Sheet and return them. 
     * This is first hack for new Jelly2, because it isn't possible to set String Comparator only for one operator.
     * @param propertyName name of property
     * @param type  TextFieldProperty - textfield property, ComboBoxProperty - combobox property
     * @return property by <b>propertyName</b> and <b>type</b>.
     */    
    public static Property findProperty(String propertyName, String type) {
        Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        
        PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        Property property;
        
        if(type.indexOf("TextFieldProperty")!=-1)
            property = new TextFieldProperty(propertiesTab, propertyName);
        else if(type.indexOf("ComboBoxProperty")!=-1)
            property = new ComboBoxProperty(propertiesTab, propertyName);
        else
            property = new Property(propertiesTab, propertyName);
        
        Operator.setDefaultStringComparator(oldComparator);
        
        return property;
    }
    
    public void tearDown() {
        closeAllModal();
    }
    
    /** Print full stack trace to log files, get message and log to test results if test fails.
     * @param exc Exception logged to description
     * @param message written to test results
     */    
    protected void failTest(Exception exc, String message) {
        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }
    
    /** Make IDE screenshot of whole IDE 
     * @param testCase it is needed for locate destination directory of saving screenshot file 
     */    
    public static void makeIDEScreenshot(JellyTestCase testCase) {
        try{                                                                                                                                           
            testCase.getWorkDir();                                                                                                                     
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(testCase.getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");      
        }catch(Exception ioexc){                                                                                                                       
            testCase.log("Impossible make IDE screenshot!!! \n" + ioexc.toString());                                                                   
        }                                                                                                                                              
    }                                                                                                                                                  
    
    public abstract void setCustomizerValue();
    
    public abstract void verifyCustomizerLayout();
    
    public abstract void verifyPropertyValue(boolean expectation);
    
}
