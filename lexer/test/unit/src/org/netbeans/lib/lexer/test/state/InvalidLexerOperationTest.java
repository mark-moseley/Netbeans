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

package org.netbeans.lib.lexer.test.state;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test of invalid lexer's behavior.
 *
 * @author mmetelka
 */
public class InvalidLexerOperationTest extends TestCase {
    
    public InvalidLexerOperationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testEarlyNullToken() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);
        // Put the language now into the document so that lexing starts from scratch
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy hi = TokenHierarchy.get(doc);
        TokenSequence ts = hi.tokenSequence();

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
        assertEquals(LexerTestUtilities.lookahead(ts), 0);
        assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);

        attrs.setValue(StateTokenId.language(), "returnNullToken", Boolean.TRUE, true);
        try {
            // Lexer will return null token too early
            assertTrue(ts.moveNext());
            fail("IllegalStateException not thrown when null token returned before input end.");
        } catch (IllegalStateException e) {
            // Expected fail of lexer
        }
    }

}
