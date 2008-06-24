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

package org.netbeans.lib.lexer.test.simple;

import java.io.PrintStream;
import java.util.logging.Level;
import org.netbeans.lib.lexer.lang.TestTokenId;
import java.util.ConcurrentModificationException;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerIncTest extends NbTestCase {
    
    public SimpleLexerIncTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    public PrintStream getLog() {
        return System.out;
//        return super.getLog();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();;
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);

        // Check insertion of text that produces token with LA=0
        doc.insertString(0, "+", null);
        LexerTestUtilities.incCheck(doc, false);
        doc.remove(0, doc.getLength());
        LexerTestUtilities.incCheck(doc, false);
        
        TokenSequence<?> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String commentText = "/* test comment  */";
        //             0123456789
        String text = "abc+uv-xy +-+" + commentText + "def";
        int commentTextStartOffset = 13;
        doc.insertString(0, text, null);

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertEquals(1, LexerTestUtilities.lookahead(ts));
        assertEquals(null, LexerTestUtilities.state(ts));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertEquals(1, LexerTestUtilities.lookahead(ts)); // la=1 because may be "+-+"
        assertEquals(null, LexerTestUtilities.state(ts));
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "xy", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 9);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS_MINUS_PLUS, "+-+", 10);
        assertEquals(0, LexerTestUtilities.lookahead(ts));
        assertEquals(null, LexerTestUtilities.state(ts));
        assertTrue(ts.moveNext());
        int offset = commentTextStartOffset;
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.BLOCK_COMMENT, commentText, offset);
        offset += commentText.length();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", offset);
        assertFalse(ts.moveNext());
        LexerTestUtilities.incCheck(doc, false);
        
        // Check TokenSequence.move()
        int relOffset = ts.move(50); // past the end of all tokens
        assertEquals(relOffset, 50 - (offset + 3));
        assertTrue(ts.movePrevious());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", offset);

        relOffset = ts.move(6); // right at begining of "-"
        assertEquals(relOffset, 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);

        relOffset = ts.move(-5); // to first token "abc"
        assertEquals(relOffset, -5);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);

        relOffset = ts.move(5); // to "uv"
        assertEquals(relOffset, 1);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);


        doc.insertString(2, "d", null); // should be "abdc"

        // Last token sequence should throw exception - new must be obtained
        try {
            ts.moveNext();
            fail("TokenSequence.moveNext() did not throw exception as expected.");
        } catch (ConcurrentModificationException e) {
            // Expected exception
        }
        LexerTestUtilities.incCheck(doc, false);
        
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abdc", 0);
        LexerTestUtilities.incCheck(doc, false);
        
        // Remove added 'd' to become "abc" again
        doc.remove(2, 1); // should be "abc" again
        LexerTestUtilities.incCheck(doc, false);
        

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        LexerTestUtilities.incCheck(doc, false);

        
        // Now insert right at the end of first token - identifier with lookahead 1
        doc.insertString(3, "x", null); // should become "abcx"
        LexerTestUtilities.incCheck(doc, false);
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abcx", 0);

        doc.remove(3, 1); // return back to "abc"
        LexerTestUtilities.incCheck(doc, false);

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);

        
        // Now insert right at the end of "+" token - operator with lookahead 1 (because of "+-+" operator)
        doc.insertString(4, "z", null); // should become "abc" "+" "zuv"
        LexerTestUtilities.incCheck(doc, false);
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "zuv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 7);
        
        doc.remove(4, 1); // return back to "abc" "+" "uv"
        LexerTestUtilities.incCheck(doc, false);

        // Now insert right after "-" - operator with lookahead 0
        doc.insertString(7, "z", null);
        LexerTestUtilities.incCheck(doc, false);
        
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "uv", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "zxy", 7);

        doc.remove(7, 1); // return back to "abc" "+" "uv"
        LexerTestUtilities.incCheck(doc, false);

        // Now insert between "+-" and "+" in "+-+" - operator with lookahead 0
        doc.insertString(12, "z", null);
        LexerTestUtilities.incCheck(doc, false);
        doc.remove(12, 1);
        LexerTestUtilities.incCheck(doc, false);

        // Now insert "-" at the end of the document
        doc.insertString(doc.getLength(), "-", null);
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "-" at the end of the document (now lookahead of preceding is zero)
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        doc.insertString(doc.getLength(), "-", null);
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.WARNING); // End of extra logging
        LexerTestUtilities.incCheck(doc, false);
        // Insert again "+-+" at the end of the document (now lookahead of preceding is zero)
        doc.insertString(doc.getLength(), "+-+", null);
        LexerTestUtilities.incCheck(doc, false);
        // Remove the ending "+" so that "+-+" becomes "+" "-"
        doc.remove(doc.getLength() - 1, 1);
        LexerTestUtilities.incCheck(doc, false);
        
        doc.insertString(5, "a+b-c", null); // multiple tokens
        LexerTestUtilities.incCheck(doc, false);
        doc.insertString(7, "-++", null); // complete PLUS_MINUS_PLUS
        LexerTestUtilities.incCheck(doc, false);
    }

}
