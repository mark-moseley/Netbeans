/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package gui.propertyeditors;

import gui.propertyeditors.utilities.CoreSupport;

import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;
import org.netbeans.jellytools.properties.editors.FilesystemCustomEditorOperator;

import org.netbeans.jemmy.EventTool;

import org.netbeans.junit.NbTestSuite;


/**
 * Tests of Identifier Array Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Filesystem extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    private final String ADDDIRECTORY = "Add Directory:";
    private final String ADDJAR = "Add JAR:";
    
    private static String FS_Data_path;
    private static String FS_Data_path_data_jar;
    
    /** Creates a new instance of PropertyType_Filesystem */
    public PropertyType_Filesystem(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Filesystem";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        
        //TODO write new way for promoD
        //String path = CoreSupport.getSystemPath("gui/data", CoreSupport.beanName, "java");
        String path = ""; 
        
        FS_Data_path = path.substring(0,path.lastIndexOf(System.getProperty("file.separator")));
        FS_Data_path_data_jar = FS_Data_path + System.getProperty("file.separator") + "data.jar";
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Filesystem("verifyCustomizer"));
        suite.addTest(new PropertyType_Filesystem("testCustomizerCancel"));
        suite.addTest(new PropertyType_Filesystem("testCustomizerAddDirectory"));
        suite.addTest(new PropertyType_Filesystem("testCustomizerAddJar"));
        return suite;
    }
    
    
    public void testCustomizerAddDirectory() {
        propertyValue_L = ADDDIRECTORY + FS_Data_path;
        propertyValueExpectation_L = getOSDependentFilesystem(FS_Data_path);
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerAddJar() {
        propertyValue_L = ADDJAR + FS_Data_path_data_jar;
        propertyValueExpectation_L = FS_Data_path_data_jar;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = ADDJAR + FS_Data_path_data_jar;
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        FilesystemCustomEditorOperator customizer = new FilesystemCustomEditorOperator(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADDDIRECTORY)){
            err.println("== ADDING DIRECTORY ============");
            customizer.addLocalDirectory();
            customizer.btBrowse().pushNoBlock();
            FileCustomEditorOperator dialog = new FileCustomEditorOperator("Add Local Directory");
            dialog.setFileValue(getPath(propertyValue_L, ADDDIRECTORY));
            new EventTool().waitNoEvent(500);
            dialog.ok();
            //customizer.setDirectory(getPath(propertyValue_L, ADDDIRECTORY));
        }
        
        if(propertyValue_L.startsWith(ADDJAR)){
            err.println("== ADDING JAR ============");
            customizer.addJARFile();
            customizer.btBrowse2().pushNoBlock();
            FileCustomEditorOperator dialog = new FileCustomEditorOperator("Add JAR File");
            dialog.fileChooser().chooseFile(getPath(propertyValue_L, ADDJAR));
            //new EventTool().waitNoEvent(500);
            //dialog.ok();
            //customizer.setJARFile(getPath(propertyValue_L, ADDJAR));
        }
        
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        //verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
        
        if(expectation){
            String newValue = getValue(propertyName_L);
            String log = "Actual value is {"+newValue+"} - set value is {"+propertyValue_L+"} / expectation value is {"+propertyValueExpectation_L+"}";
            
            err.println("=========================== Trying to verify value ["+log+"].");
            
            if(newValue.indexOf(propertyValueExpectation_L)!=-1) {
                log(log + " --> PASS");
            }else {
                fail(log + " --> FAIL");
            }
        }else {
            verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
        }
        
    }
    
    
    private String getPath(String str, String delim) {
        int index = str.indexOf(delim);
        
        err.println("============================= Try to set path="+str);
        
        if(index > -1)
            return str.substring(index + delim.length());
        
        return str;
    }
    
    private String getOSDependentFilesystem(String path) {
        String os = System.getProperty("os.name");
        err.println("Os name = {"+os+"}");
        
        if(os.indexOf("Win")!=-1)
            return path.replace('\\','/');
        
        return path;
    }
    
    public void verifyCustomizerLayout() {
        FilesystemCustomEditorOperator customizer = new FilesystemCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Filesystem.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
