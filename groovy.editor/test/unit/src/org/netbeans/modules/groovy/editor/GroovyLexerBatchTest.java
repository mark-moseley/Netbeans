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
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;

/**
 * Test groovy lexer
 * 
 * @todo testGstringsWithoutNewLine and testGstringsBeforeNewLine should take 
 * care about last token in same way
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
        String text = "def s = 4 / 2";
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
    }
    
    public void testGstringsWithoutNewLine() {
        String text = "def s = \"H $name\"";
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
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $name", -1); // this should be same in both tests
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
    }
    
    public void testGstringsBeforeNewLine(){
        String text = "def s = \"H $name\"\n\n";
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
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"H $name\"", -1);
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
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,GroovyTokenId.STRING_LITERAL, "\"", -1);
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
        Language<GsfTokenId> language = GroovyTokenId.language();
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
        assertTrue("Timeout tm = " + tm + "msec", tm < 1000); // Should be fast
        System.out.println("Lexed input " + text.length()
                + " chars long and created " + cntr + " tokens in " + tm + " ms.");
    }
    
}
