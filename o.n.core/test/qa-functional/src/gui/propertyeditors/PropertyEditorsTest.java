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
    
    //protected static NbDialogOperator beanCustomizer = null;
    protected static FrameOperator propertiesWindow = null;
    
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
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new TextFieldProperty(propertiesTab, propertyName).setValue(propertyValue);
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
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new ComboBoxProperty(propertiesTab, propertyName).setValue(propertyValue);
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
            
            //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
            //H1 new ComboBoxProperty(propertiesTab, propertyName).setValue(propertyValueIndex);
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
        // open Property Editor
        //err.println(CAPTION + " Trying to open Property Customizer{"+JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout")+"}.");

        //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        //H1 new Property(propertiesTab, propertyName).openEditor();
        findProperty(propertyName, "").openEditor();
        
        //err.println(CAPTION + " Trying to open Property Customizer - finished.");
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
        String title = "Information";
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
        String returnValue;
        
        //H1 PropertySheetTabOperator propertiesTab = new PropertySheetTabOperator(new PropertySheetOperator(propertiesWindow));
        //H1 returnValue = new Property(propertiesTab, propertyName).getValue();
        returnValue = findProperty(propertyName, "").getValue();
        
        err.println("GET VALUE = [" + returnValue + "].");
        
        return returnValue;
    }
    
    /** Find Property Cusotmizer by name of property <b>propertyName</b>
     * @param propertyName name of property 
     * @return founded Property Customizer
     */    
    private static NbDialogOperator findPropertyCustomizer(String propertyName){
        //err.println(CAPTION + " Trying to find Property Customizer.");
        NbDialogOperator propertyCustomizer = new NbDialogOperator(propertyName);
        //err.println(CAPTION + " Trying to find Property Customizer - finished.");
        return propertyCustomizer;
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
    
    
    public static FrameOperator reInitializeWorkplace() {
        propertiesWindow = null;
        return openPropertySheet();
    }
    
    /* Initialize Workplace.
     */
    public static FrameOperator initializeWorkplace() {
        return openPropertySheet();
    }
    
    /* Open property sheet (bean customizer).
     */
    private static FrameOperator openPropertySheet() {
        String waitFrameTimeout = "FrameWaiter.WaitFrameTimeout";
        long findTimeout = JemmyProperties.getCurrentTimeout(waitFrameTimeout);
        JemmyProperties.setCurrentTimeout(waitFrameTimeout, 3000);

        try{
            propertiesWindow = new FrameOperator("Properties of TestNode");
        }catch(org.netbeans.jemmy.TimeoutExpiredException exception){
           new PropertiesTest();
           propertiesWindow = new FrameOperator("Properties of TestNode");
        }
        
        JemmyProperties.setCurrentTimeout(waitFrameTimeout, findTimeout);
        
/*        
        if(propertiesWindow==null){
            new PropertiesTest();
            // beanCustomizer = new TopComponentOperator(Bundle.getString("org.openide.nodes.Bundle", "Properties"));
            //beanCustomizer = new TopComponentOperator(Bundle.getString("org.netbeans.core.Bundle","CTL_FMT_LocalProperties",new Object[] {new Integer(1), "TestN"}));
            propertiesWindow = new FrameOperator("Properties of TestNode");
            
            // Next code doesn't work because seDefaultStringComparator sets comparator for all Operators
            // PropertySheetOperator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        }
        //err.println(CAPTION + " Trying to run PropertiesTest - finished.");
*/        
        return propertiesWindow;
    }
    
    
    /* H1
     * Find Property in Property Sheet and return them. 
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
    
/*
    public String hackForJamPropertyButtonGetValue(String value) {
        // line 246 in JamPropertyButton , call getValue return not right value but some truncated value
        // this is the same code as for getValue, means excpectation value must be the same as this one
        return value.substring(value.lastIndexOf(':') + 2); // extra ++ for space
    }
 */
    
    public void tearDown() {
        closeAllModal();
        
        /*
        FileSystem fs = new FileSystems().getFileSystem(PropertyEditorsSupport.getFS("data", "ClearJFrameWithPropertyEditorTestBean", "java"));
        if(useForm)
            ExplorerNode.find(fs, "data, ClearJFrameWithPropertyEditorTestBean").getActions().save();
         */
        
        /*
        if(lastTest) {
            Editor e = Editor.find();
            e.switchToTab(PropertyEditorsSupport.beanName);
            e.pushPopupMenu("Save");
        }
         */
        
        /*
        if(useForm && lastTest) {
            org.netbeans.test.oo.gui.jelly.FileSystem fs = new org.netbeans.test.oo.gui.jelly.FileSystems().getFileSystem(PropertyEditorsSupport.getFS(PropertyEditorsSupport.Resources, "ClearJFrameWithPropertyEditorTestBean", "java"));
            org.netbeans.test.oo.gui.jelly.ExplorerNode.find(fs, PropertyEditorsSupport.Resources+", "+"ClearJFrameWithPropertyEditorTestBean").select();
            org.netbeans.test.oo.gui.jelly.MainFrame.getMainFrame().pushFileMenu("Save");
        }
         */
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
