/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test.simple;

import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerBatchTest extends TestCase {
    
    public SimpleLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() {
        String commentText = "/* test comment  */";
        String text = "abc+ " + commentText + "def public publica publi static x";
        int commentTextStartOffset = 5;
        TokenHierarchy hi = TokenHierarchy.create(text, SimpleLanguage.description());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 4);
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        int commentIndex = ts.index();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", offset);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.PUBLIC, "public", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "publica", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "publi", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.STATIC, "static", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", -1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "x", -1);
        assertFalse(ts.moveNext());

        // Go back to block comment
        assertTrue(ts.moveIndex(commentIndex));
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.BLOCK_COMMENT, commentText, commentTextStartOffset);

        // Test embedded token sequence
        TokenSequence embedded = ts.embedded();
        assertNotNull("Null embedded sequence", embedded);
        assertTrue(embedded.moveNext());
        offset = commentTextStartOffset + 2; // skip "/*"
        LexerTestUtilities.assertTokenEquals(embedded, SimplePlainTokenId.WHITESPACE, " ", offset);
        offset += 1;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded, SimplePlainTokenId.WORD, "test", offset);
        offset += 4;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded, SimplePlainTokenId.WHITESPACE, " ", offset);
        offset += 1;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded, SimplePlainTokenId.WORD, "comment", offset);
        offset += 7;
        assertTrue(embedded.moveNext());
        LexerTestUtilities.assertTokenEquals(embedded, SimplePlainTokenId.WHITESPACE, "  ", offset);
        assertFalse(embedded.moveNext());

    }
    
    public void testPerf() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7000; i++) {
            sb.append("public static x + y /* test comment */ abc * def\n");
        }
        String text = sb.toString();

        long tm;
        tm = System.currentTimeMillis();
        TokenHierarchy hi = TokenHierarchy.create(text, SimpleLanguage.description());
        tm = System.currentTimeMillis() - tm;
        assertTrue("Timeout tm = " + tm + "msec", tm < 100); // Should be fast
        
        tm = System.currentTimeMillis();
        TokenSequence ts = hi.tokenSequence();
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
