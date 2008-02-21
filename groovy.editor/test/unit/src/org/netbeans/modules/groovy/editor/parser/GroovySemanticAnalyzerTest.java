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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.editor.parser;

import java.io.IOException;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.fpi.gsf.CompilationInfo;

/**
 *
 * @author Matthias Schmidt
 */
public class GroovySemanticAnalyzerTest extends GroovyTestBase {

    public GroovySemanticAnalyzerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        // Logger.getLogger(GroovySemanticAnalyzer.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(org.netbeans.modules.groovy.editor.AstUtilities.class.getName()).setLevel(Level.FINEST);
    }
    
        // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    
    private void parseFile(FileObject file) throws IOException {
        CompilationInfo info = getInfo(file);
        GroovySemanticAnalyzer analyzer = new GroovySemanticAnalyzer();
        analyzer.run(info);
    }

    public void testCombinedTest() throws IOException {
        
        String str =    "class DemoClass {\n" +
                        "\tint field1 = 1\n" +
                        "\n" +
                        "DemoClass(int inputval){\n" +
                        "\tfield1 = inputval\n" +
                        "}\n" +
                        "static void main(args) {\n" +
                        "\tString s = 'aaa'\n" +
                        "\tprintln 'Hello, world'\n" +
                        "}\n" +
                        "void dynamicmethod() {\n" +
                        "\tfield1 = 2\n" +
                        "\tthis.field1 = 77\n" +
                        "}\n" +
                        "}";
        
        // un-comment the line below to have cut'n'pastable 
        // testcases for the groovy-editor
        // System.out.println(str);
        copyStringToFileObject(testFO, str);
        parseFile(testFO);
    }
    
    public void testPropertyExpression() throws IOException {
        
        String str =    "class TestClass {\n" +
                        "\tpublic int pField = 1\n" +
                        "}\n" +
                        "TestClass tc = new TestClass()\n" +
                        "tc.pField = 9\n" +
                        "\n";
        
        // un-comment the line below to have cut'n'pastable 
        // testcases for the groovy-editor
        // System.out.println(str);
        copyStringToFileObject(testFO, str);
        parseFile(testFO);
    }
    
        public void testConstructorAnnotation() throws IOException {
        
        String str =    "class TestClass {\n" +
                        "\tint field1 = 1;\n" +
                        "\n" +
                        "\tTestClass (int f) {\n" +
                        "\tfield1 = f;\n" +
                        "\t}\n" +
                        "}\n" +
                        "println \"End.\"\n";
        
        // un-comment the line below to have cut'n'pastable 
        // testcases for the groovy-editor
        // System.out.println(str);
        copyStringToFileObject(testFO, str);
        parseFile(testFO);
        
        // mark this below "false" to deliberately fail the test and 
        // thereby enabling the logging from the SemanticAnalyzer
        assertTrue(true);
    }

        public void testClassNode() throws IOException {
        
        String str =    "class TestClass {\n" +
                        "\tint field1 = 1;\n" +
                        "\n" +
                        "\tTestClass (int f) {\n" +
                        "\t}\n" +
                        "}\n" +
                        "  class  SecondTestClass {\n" +
                        "\n" +
                        "\nSecondTestClass (int f) {\n" +
                        "\t}\n" +
                        "\nSecondTestClass (String str) {\n" +
                        "\t}\n" +
                        "}\n" +
                        "   class   ThirdTestClass {\n" +
                        "\n" +
                        "\nThirdTestClass (int f) {\n" +
                        "\t}\n" +
                        "\nThirdTestClass (String str) {\n" +
                        "\t}\n" +
                        "}\n" +
                        "println \"End.\"\n";
        
        // un-comment the line below to have cut'n'pastable 
        // testcases for the groovy-editor
        // System.out.println(str);
        copyStringToFileObject(testFO, str);
        parseFile(testFO);
        
        // mark this below "false" to deliberately fail the test and 
        // thereby enabling the logging from the SemanticAnalyzer
        assertTrue(true);
    }        
    
}
