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

import gui.propertyeditors.utilities.CoreSupport;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.jellytools.properties.editors.ClasspathCustomEditorOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;

/**
 * Tests of NbClassPath Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_NbClassPath extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private final String ADDDIRECTORY = "Add Directory:";
    private final String ADDJAR = "Add JAR:";
    private final String REMOVE = "Remove:";
    private final String UP = "Up:";
    private final String DOWN = "Down:";
    
    private static String directoryPath;
    private static String dataJarPath;
    
    private static String delim;
    
    /** Creates a new instance of PropertyType_NbClassPath */
    public PropertyType_NbClassPath(String testName) {
        super(testName);
        directoryPath = CoreSupport.getSampleProjectPath(this);
        dataJarPath = directoryPath + System.getProperty("file.separator") + "data.jar";
        
        log("======= Directory Path={"+directoryPath+"}");
        log("======= Data.jar Path={"+dataJarPath+"}");
        
        String os = System.getProperty("os.name");
        System.err.println("Os name = {"+os+"}");
        
        if(os.indexOf("Win")!=-1)
            delim = ";";
        else
            delim = ":";
           
        System.err.println("delim={"+delim+"}");

        propertyName_L = "NbClassPath";
    }
    
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_NbClassPath("testByInPlace"));
        suite.addTest(new PropertyType_NbClassPath("verifyCustomizer")); 
        suite.addTest(new PropertyType_NbClassPath("testCustomizerCancel")); 
        suite.addTest(new PropertyType_NbClassPath("testCustomizerAddDirectory"));
        suite.addTest(new PropertyType_NbClassPath("testCustomizerRemove"));
        suite.addTest(new PropertyType_NbClassPath("testCustomizerUp"));
        suite.addTest(new PropertyType_NbClassPath("testCustomizerAddJar"));
        
        // must rewrite test specs to add this test 
        //suite.addTest(new PropertyType_NbClassPath("testCustomizerDown"));
        return suite;
    }
    
    
    public void testCustomizerAddDirectory() {
        propertyValue_L = ADDDIRECTORY + directoryPath;
        propertyValueExpectation_L =  "one.jar"+delim+"two.zip" + delim + directoryPath;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerAddJar() {
        propertyValue_L = ADDJAR + dataJarPath;
        propertyValueExpectation_L = "two.zip"+delim+"one.jar"+ delim + dataJarPath;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + directoryPath;
        //propertyValueExpectation_L = "one.jar:two.zip:" + FS_Data_path_data_jar;
        propertyValueExpectation_L = "one.jar"+delim+"two.zip";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerUp() {
        propertyValue_L = UP + "two.zip";
        //propertyValueExpectation_L = "two.zip:one.jar:" + FS_Data_path_data_jar;
        propertyValueExpectation_L = "two.zip"+delim+"one.jar";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerDown() {
        propertyValue_L = DOWN + "one.jar";
        propertyValueExpectation_L = "two.zip" + delim + dataJarPath + delim + "one.jar" ;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = REMOVE + "one.jar";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "one.jar"+delim+"two.zip";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ClasspathCustomEditorOperator customizer = new ClasspathCustomEditorOperator(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADDDIRECTORY)){
            err.println("========== ADDING DIRECTORY =======");
            customizer.addDirectory(getPath(propertyValue_L, ADDDIRECTORY));
        }
        
        if(propertyValue_L.startsWith(ADDJAR)){
            err.println("========== ADDING JAR =======");
//            customizer.addJARZIP(getPath(propertyValue_L, ADDJAR));
            // hack because previously code fails
            FileCustomEditorOperator editor=customizer.addJARZIP();
            editor.fileChooser().chooseFile(getPath(propertyValue_L, ADDJAR));
            customizer.ok();
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            err.println("========== REMOVE =======");
            customizer.remove(getPath(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(UP)){
            err.println("========== UP =======");
            customizer.lstClasspath().selectItem(getPath(propertyValue_L,UP));
            customizer.moveUp();
        }
        
        if(propertyValue_L.startsWith(DOWN)){
            err.println("========== DOWN =======");            
            customizer.lstClasspath().selectItem(getPath(propertyValue_L,DOWN));
            customizer.moveDown();
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    private String getPath(String str, String delim) {
        int index = str.indexOf(delim);

        if(index > -1) 
            return str.substring(index + delim.length());
        
        return str;
    }
    
    public void verifyCustomizerLayout() {
        ClasspathCustomEditorOperator customizer = new ClasspathCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_NbClassPath.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
