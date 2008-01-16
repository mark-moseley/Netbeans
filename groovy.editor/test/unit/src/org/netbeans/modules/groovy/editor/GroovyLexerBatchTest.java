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

package org.netbeans.modules.groovy.editor;

import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;

/**
 * Test groovy lexer
 * 
 * @todo ${...} inside strings
 * 
 * @author Martin Adamek
 */
public class GroovyLexerBatchTest extends TestCase {

    public GroovyLexerBatchTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testDiv() {
        String text = "def s = 4 / 2\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NUM_INT, "4", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.DIV, "/", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NUM_INT, "2", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
 
    public void testGstringLexing1(){
        
        TokenSequence<?> ts = seqForText("{{}}");
        
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.LBRACE, "{");
        next(ts, GroovyTokenId.RBRACE, "}");
        next(ts, GroovyTokenId.RBRACE, "}");
    }
    
    public void testGstringLexing2(){
        
        TokenSequence<?> ts = seqForText("x=\"z\";assert\"h{$x}\".size()==4");
        
        next(ts, GroovyTokenId.IDENTIFIER, "x");
        next(ts, GroovyTokenId.ASSIGN, "=");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"z\"");
        next(ts, GroovyTokenId.SEMI, ";");
        next(ts, GroovyTokenId.LITERAL_assert, "assert");
    }
    
    public void testGstringLexing3(){
        
        TokenSequence<?> ts = seqForText("println \"hallo\".size()");
        
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.DOT, ".");
        next(ts, GroovyTokenId.IDENTIFIER, "size");
        next(ts, GroovyTokenId.LPAREN, "(");
        next(ts, GroovyTokenId.RPAREN, ")");
    }
    
    public void testGstringLexing4(){
        
        TokenSequence<?> ts = seqForText("\"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
    }

    
    public void testGstringLexing5(){
        
        TokenSequence<?> ts = seqForText("println \"Hello $name!\"\r\n");
        
        // FIXME: This test is meant to be brocken, since I'm still
        // hunting the trailing doubleqoutes errer. BEWARE!
        
        next(ts, GroovyTokenId.IDENTIFIER, "println");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"Hello $");
        next(ts, GroovyTokenId.IDENTIFIER, "name");
        next(ts, GroovyTokenId.LNOT, "!");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"");
        
        assertTrue(ts.moveNext());
        Token t = ts.token();
        
        System.out.println("-------------------------------");  
        System.out.println("Token-Text   :" + t.toString() + ":CLOSED");  
        System.out.println("Token-Length :" + t.length());  
        System.out.println("Token-ID     :" + t.id());
        System.out.println("-------------------------------"); 
    }
    
    
    public void testMultipleStringConstants(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
    }
    
    public void testMultipleStringConstantsClosed(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        assertFalse(ts.moveNext());
    }
    
    public void testMultipleStringConstantsDosTerminated(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"\r\n");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.NLS, "\r\n");
        assertFalse(ts.moveNext());
    } 
    
    public void testMultipleStringConstantsUnixTerminated(){
        
        TokenSequence<?> ts = seqForText("\"hallo\" \"hallo\" \"hallo\" \"hallo\"\n");
        
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.WHITESPACE, " ");
        next(ts, GroovyTokenId.STRING_LITERAL, "\"hallo\"");
        next(ts, GroovyTokenId.NLS, "\n");
        assertFalse(ts.moveNext());
    }     
    
    
    TokenSequence<?> seqForText(String text){
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        return hi.tokenSequence();
    }
    
    
    void next(TokenSequence<?> ts, GroovyTokenId id, String fixedText){
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,id, fixedText, -1);
    }
    
    
    public void testLengthTest1(){
        String text = "true";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertTrue(ts.moveNext());
        Token t = ts.token();
        assertTrue(t.length() == 4);
        
    }
    
    public void testLengthTest2(){
        String text = "\"hallo\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        assertTrue(ts.moveNext());
        Token t = ts.token();
        assertTrue(t.length() == 7);
        
    }
    
    public void testLengthTest3(){
        String text = "assert true";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 6);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 4);
    }
    
    public void testLengthTest4(){
        String text = "\"hallo\".size()";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        System.out.println("4-Token:" + t.toString());
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 4);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
    }
    
    public void testLengthTest5(){
        String text = "println \"hallo\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        System.out.println("5-Token:" + t.toString());
        System.out.println("5-Token-ID:" + t.id());
        assertTrue(t.length() == 7);
    }    
    
     public void testLengthTest6(){
        String text = "println \"hallo\"\n";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        Token t;
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 7);
        
        assertTrue(ts.moveNext());
        t = ts.token();
        assertTrue(t.length() == 1);
     
        assertTrue(ts.moveNext());
        t = ts.token();
        System.out.println("6-Token:" + t.toString());
        assertTrue(t.length() == 7);
    }
     

    public void testGstringsWithoutNewLine() {
        String text = "def name = 'foo'; def s = \"H $name\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "'foo'", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $", -1); // this should be same in both tests
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
    }
    
    public void testGstringsBeforeNewLine(){
        String text = "def name = 'foo'; def s = \"H $name\"\n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "'foo'", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $", -1); // this should be same in both tests
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "name", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
    
    public void testQuotes() {
        String text = "def s = \"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "s", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.ASSIGN, "=", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"\"", -1);
    }
    
    public void test1() {
        String text = 
                "class Foo {\n" +
                "\n" +
                "private def aaa;\n" +
                "}";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_class, "class", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "Foo", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LBRACE, "{", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_private, "private", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "aaa", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.SEMI, ";", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
    }
   
    public void testCRLF() {
        String text = 
                "class LineTermination {\n" +
                "\n" +
                "\r\n" +
                "\r" +
                "}";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_class, "class", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "LineTermination", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LBRACE, "{", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\r\n", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.NLS, "\r", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.RBRACE, "}", -1);
    }
    public void test2() {
        String commentText = "/* test comment  */";
        String text = "abc+ " + commentText + "def public publica publi static x";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text,GroovyTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", 4);
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        int commentIndex = ts.index();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_def, "def", offset);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_public, "public", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "publica", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "publi", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.LITERAL_static, "static", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.IDENTIFIER, "x", -1);
        assertFalse(ts.moveNext());

        // Go back to block comment
        assertEquals(0, ts.moveIndex(commentIndex));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.BLOCK_COMMENT, commentText, commentTextStartOffset);

    }
    
    public void testPerf() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7000; i++) {
            sb.append("public static x + y /* test comment */ abc * def\n");
        }
        String text = sb.toString();

        long tm;
        Language<GroovyTokenId> language = GroovyTokenId.language();
        tm = System.currentTimeMillis();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language);
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        TokenSequence<?> ts = hi.tokenSequence();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        // Fetch 2 initial tokens - should be lexed lazily
        tm = System.currentTimeMillis();
        ts.moveNext();
        ts.token();
        ts.moveNext();
        ts.token();
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        ts.moveIndex(0);
        int cntr = 1; // On the first token
        while (ts.moveNext()) {
            Token t = ts.token();
            cntr++;
        }
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 3000); // Should be fast
        System.out.println("Lexed input " + text.length()
                + " chars long and created " + cntr + " tokens in " + tm + " ms.");
    }
    
}
