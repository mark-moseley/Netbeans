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

package org.netbeans.lib.lexer.inc;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.test.simple.SimplePlainTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenHierarchyEventTest extends NbTestCase {
    
    public TokenHierarchyEventTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testCreateEmbedding() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "abc def ghi";
        doc.insertString(0, text, null);
        // Assign a language to the document
        doc.putProperty(Language.class, SimpleTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        THListener listener = new THListener();
        hi.addTokenHierarchyListener(listener);
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();

        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "def", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.IDENTIFIER, "ghi", 8);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, SimpleTokenId.WHITESPACE, " ", 11);
        assertTrue(ts.moveNext());
        
        // Do insert
        doc.insertString(5, "x", null);
        
        // Check the fired event
        TokenHierarchyEvent evt = listener.fetchLastEvent();
        assertNotNull(evt);
        TokenChange<? extends TokenId> tc = evt.tokenChange();
        assertNotNull(tc);
        assertEquals(2, tc.index());
        assertEquals(15, tc.offset());
        assertEquals(0, tc.addedTokenCount());
        assertEquals(0, tc.removedTokenCount());
        assertEquals(SimpleTokenId.language(), tc.language());
        assertEquals(1, tc.embeddedChangeCount());
        TokenChange<? extends TokenId> etc = tc.embeddedChange(0);
        assertEquals(0, etc.index());
        assertEquals(18, etc.offset());
        assertEquals(0, etc.addedTokenCount()); // 0 to allow for lazy lexing where this would be unknowns
        assertEquals(0, etc.removedTokenCount());
        assertEquals(SimpleTokenId.language(), etc.language());
        assertEquals(0, etc.embeddedChangeCount());
        
        // Test the contents of the embedded sequence
        TokenSequence<? extends TokenId> ets = ts.embedded();
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "line", 18);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 22);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "comment", 23);
        assertFalse(ets.moveNext());
        
        // Move main TS back and try extra embedding on comment
        assertTrue(ts.movePrevious());
        assertTrue(ts.createEmbedding(SimpleTokenId.language(), 2, 2));
        ets = ts.embedded(); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.IDENTIFIER, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimpleTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
        
        // Get the default embedding - should be available as well
        ets = ts.embedded(SimplePlainTokenId.language()); // Should be the explicit one
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WORD, "def", 5);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WHITESPACE, " ", 8);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WORD, "ghi", 9);
        assertTrue(ets.moveNext());
        LexerTestUtilities.assertTokenEquals(ets, SimplePlainTokenId.WHITESPACE, " ", 12);
        assertFalse(ets.moveNext());
    }
    
    public void testEmbeddingCaching() throws Exception {
        LanguageEmbedding<? extends TokenId> e = LanguageEmbedding.create(SimpleTokenId.language(), 2, 1);
        assertSame(SimpleTokenId.language(), e.language());
        assertSame(2, e.startSkipLength());
        assertSame(1, e.endSkipLength());
        LanguageEmbedding<? extends TokenId> e2 = LanguageEmbedding.create(SimpleTokenId.language(), 2, 1);
        assertSame(e, e2);
    }
    
    private static final class THListener implements TokenHierarchyListener {
        
        private TokenHierarchyEvent lastEvent;
    
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            this.lastEvent = evt;
        }
        
        public TokenHierarchyEvent fetchLastEvent() {
            TokenHierarchyEvent evt = lastEvent;
            lastEvent = null;
            return evt;
        }

    }

}
