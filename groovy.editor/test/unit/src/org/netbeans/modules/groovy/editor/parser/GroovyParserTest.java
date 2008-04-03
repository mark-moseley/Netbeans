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
import java.util.Scanner;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.groovy.editor.test.TestCompilationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class GroovyParserTest extends GroovyTestBase {

    public GroovyParserTest(String testName) {
        super(testName);
    }
    
    private void checkParseTree(FileObject file, String caretLine, String nodeName) throws IOException {
        CompilationInfo info = getInfo(file);
        
        String text = info.getText();

        int caretOffset = -1;
        int caretDelta = -1;
        if (caretLine != null) {
            caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ((TestCompilationInfo)info).setCaretOffset(caretOffset);
        }

        ASTNode root = AstUtilities.getRoot(info);
        assertNotNull("Parsing broken input failed for " + file, root);
        
        // Ensure that we find the node we're looking for
        if (nodeName != null) {
//            GroovyParserResult rpr = (GroovyParserResult)info.getParserResult();
            OffsetRange range = OffsetRange.NONE; //rpr.getSanitizedRange();
            if (range.containsInclusive(caretOffset)) {
                caretOffset = range.getStart();
            }
            
            Scanner scanner = new Scanner(text);
            int lineNumber = 1;
            int column = -1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int indexOfCaretLine = line.indexOf(caretLine);
                if (indexOfCaretLine != -1) {
                    column = indexOfCaretLine + caretDelta + 1;
                    break;
                }
                lineNumber++;
            }

            AstPath path = new AstPath(root, lineNumber, column);
            ASTNode closest = path.leaf();
            assertNotNull(closest);
            String leafName = closest.getClass().getName();
            leafName = leafName.substring(leafName.lastIndexOf('.')+1);
            assertEquals(nodeName, leafName);
        }
    }

    public void test1() throws IOException {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tstatic void main(args) {\n" +
                "\t\tString s = 'aaa'\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        checkParseTree(testFO, "void ^main", "MethodNode");
    }
    
    public void test2() throws IOException {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        checkParseTree(testFO, "void ^main", "MethodNode");
    }
    
    public void testAstUtilitiesGetRoot() throws IOException {
        copyStringToFileObject(testFO,
                "class Hello {\n" +
                "\tdef name = 'aaa'\n" +
                "\tprintln name\n" +
                "\tstatic void main(args) {\n" +
                "\t\tprintln 'Hello, world'\n" +
                "\t}\n" +
                "}");
        
        CompilationInfo info = getInfo(testFO);
        ASTNode root = AstUtilities.getRoot(info);
        AstPath path = new AstPath(root ,1, (BaseDocument)info.getDocument());
        assertNotNull("new AstPath() failed", path);
    }    
    
    
    public void testRootNPE() throws IOException {
        
        // we have an issue with compileUnit.getModules()
        // in GroovyParser.java returning no AST, see # 131317
        // This might be related to GROOVY-1443
        // The sanitize() recursion reads:
        // NONE
        // ERROR_DOT
        // ERROR_LINE
        // MISSING_END
      
        
        copyStringToFileObject(testFO,
                "def m() {\n" +
                "\tObject x = new Object()\n" +
                "\tx.\n" +
                "}\n");
        
        CompilationInfo info = getInfo(testFO);
        ASTNode root = AstUtilities.getRoot(info);
        // root is null, the AssertionError/NPE is just the follow-up:
        AstPath path = new AstPath(root ,1, (BaseDocument)info.getDocument());
        assertNotNull("new AstPath() failed", path);
    }

}
