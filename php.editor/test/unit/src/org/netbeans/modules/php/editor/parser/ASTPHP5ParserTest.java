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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java_cup.runtime.Symbol;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Petr Pisl
 */
public class ASTPHP5ParserTest extends ParserTestBase {
    
    public ASTPHP5ParserTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGotoStatment() throws Exception {
        performTest("php53/gotostatement");
    }
    public void testLambdaFunction() throws Exception {
        performTest("php53/lambdaFunction");
    }
    public void testLambdaFunctionWithParams() throws Exception {
        performTest("php53/lambdaFunctionWithParams");
    }
    public void testLambdaFunctionWithParamsWithVars() throws Exception {
        performTest("php53/lambdaFunctionWithParamsWithVars");
    }
    public void testLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("php53/lambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testMultipleBracketedNamespaces() throws Exception {
        performTest("php53/multipleBracketedNamespaces");
    }
    public void testMultipleUnBracketedNamespaces1() throws Exception {
        performTest("php53/multipleUnBracketedNamespaces1");
    }
    public void testMultipleUnBracketedNamespaces2() throws Exception {
        performTest("php53/multipleUnBracketedNamespaces2");
    }
    public void testNamespaceDeclaration() throws Exception {
        performTest("php53/namespaceDeclaration");
    }
    public void testSubNamespaceDeclaration() throws Exception {
        performTest("php53/subNamespaceDeclaration");
    }
    public void testNamespaceElementDeclarations() throws Exception {
        performTest("php53/namespaceElementDeclarations");
    }
    public void testNowDoc() throws Exception {
        performTest("php53/nowDoc");
    }
    public void testRefLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("php53/refLambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testTernaryOperator() throws Exception {
        performTest("php53/ternaryOperator");
    }
    public void testUseGlobal() throws Exception {
        performTest("php53/useGlobal");
    }
    public void testUseGlobalSubNamespace() throws Exception {
        performTest("php53/useGlobalSubNamespace");
    }
    public void testUseNamespaceAs() throws Exception {
        performTest("php53/useNamespaceAs");
    }
    public void testUseSimple() throws Exception {
        performTest("php53/useSimple");
    }
    public void testUseSubNamespace() throws Exception {
        performTest("php53/useSubNamespace");
    }
    public void testTextSearchQuery () throws Exception {
        // testing real file from phpwiki
        performTest("TextSearchQuery");
    }
    
    public void testPHPDoc () throws Exception {
        //unfinished phpdoc
        performTest("test01");
    }

    public void testNowdoc () throws Exception {
        performTest("nowdoc01");
        performTest("nowdoc02");
        performTest("nowdoc_000");
        performTest("nowdoc_001");
        performTest("nowdoc_002");
        performTest("nowdoc_003");
        performTest("nowdoc_004");
        performTest("nowdoc_005");
        performTest("nowdoc_006");
        performTest("nowdoc_007");
        performTest("nowdoc_008");
        performTest("nowdoc_009");
        performTest("nowdoc_010");
        performTest("nowdoc_011");
        performTest("nowdoc_012");
        performTest("nowdoc_013");
        performTest("nowdoc_014");
        performTest("nowdoc_015");
    }

    public void testHereDoc() throws Exception {
        performTest("heredoc00");
        //unfinished hredoc
        performTest("heredoc01");
        performTest("heredoc_001");
        performTest("heredoc_002");
        performTest("heredoc_003");
        performTest("heredoc_004");
        performTest("heredoc_005");
        performTest("heredoc_006");
        performTest("heredoc_007");
        performTest("heredoc_008");
        performTest("heredoc_009");
        performTest("heredoc_010");
        performTest("heredoc_011");
        performTest("heredoc_012");
        performTest("heredoc_013");
        performTest("heredoc_014");
    }

    public void testVarCommentSimple01() throws Exception {
        performTest("varcomment/simple01");
    }

    public void testVarCommentSimple02() throws Exception {
        performTest("varcomment/simple02");
    }

    public void testVarCommentMixedType01() throws Exception {
        performTest("varcomment/mixed01");
    }

    public void testVarCommentMixedType02() throws Exception {
        performTest("varcomment/mixed02");
    }
    
    @Override
    protected String getTestResult(String filename) throws Exception {
        File testFile = new File(getDataDir(), "testfiles/" + filename + ".php");
        StringBuffer result = new StringBuffer();
        String content = PHPLexerUtils.getFileContent(testFile);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(content));

        Symbol symbol;
        result.append("<testresult testFile='").append(testFile.getName()).append("'>\n");
        result.append("    <scanner>\n");
        do {
            symbol = scanner.next_token();
            result.append("        <token id='").append(Utils.getASTScannerTokenName(symbol.sym)).append("' start='");
            result.append(symbol.left).append("' end='").append(symbol.right + "'>\n");
            result.append("            <text>");
            result.append(PHPLexerUtils.getXmlStringValue(content.substring(symbol.left, symbol.right)));
            result.append("</text>\n");
            result.append("        </token>\n");
        } while (symbol.sym != ASTPHP5Symbols.EOF);
        result.append("    </scanner>\n");

        scanner.reset(new FileReader(testFile));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        if (root != null){
            Program rootnode = (Program)root.value;

            result.append((new PrintASTVisitor()).printTree(rootnode, 1));
        }
        result.append("</testresult>\n");
        return result.toString();
    }
}
