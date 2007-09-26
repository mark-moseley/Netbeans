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
