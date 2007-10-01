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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ConcurrentModificationException;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import junit.textui.TestRunner;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class SyntaxHighlightingTest extends NbTestCase {
    
    public static void main(String... args) {
        TestRunner.run(SyntaxHighlightingTest.class);
    }
    
    /** Creates a new instance of SyntaxHighlightingTest */
    public SyntaxHighlightingTest(String name) {
        super(name);
    }
    
    public void testSimple() {
        checkText("+ - / * public", TestTokenId.language());
    }
    
    public void testEmbedded() {
        checkText("/**//* this is a comment */", TestTokenId.language());
    }
    
    public void testComplex() {
        checkText(
            "public       /**/ +/-  private /** hello */ something /* this is a comment */ \"hi hi hi\" xyz    ", 
            TestTokenId.language());
    }

    public void testNoPrologEpilogEmbedding() {
        checkText(
            "hello world 0-1-2-3-4-5-6-7-8-9-A-B-C-D-E-F      Ooops", 
            TestPlainTokenId.language());
    }
    
    public void testConcurrentModifications() throws BadLocationException {
        Document doc = createDocument(TestTokenId.language(), "NetBeans NetBeans NetBeans");
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);
        
        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            assertFalse("There should be no highlights after co-modification", hs.moveNext());
        }        
    }

    public void testEvents() throws BadLocationException {
        final String text = "Hello !";
        Document doc = createDocument(TestTokenId.language(), text);
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);
        L listener = new L();
        layer.addHighlightsChangeListener(listener);
        
        assertHighlights(
            TokenHierarchy.create(text, TestTokenId.language()).tokenSequence(), 
            layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE), 
            true, 
            ""
        );
        
        assertEquals("There should be no events", 0, listener.eventsCnt);
        
        final String addedText = "World";
        doc.insertString(6, addedText, SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertTrue("Wrong change start offset", 6 >= listener.lastStartOffset);
        assertTrue("Wrong change end offset", 6 + addedText.length() <= listener.lastEndOffset);
    }
    
    private void checkText(String text, Language<? extends TokenId> lang) {
        System.out.println("Checking text: '" + text + "'\n");
        Document doc = createDocument(lang, text);
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        TokenHierarchy<Void> tokens = TokenHierarchy.create(text, lang);
        assertHighlights(tokens.tokenSequence(), hs, true, "");
        assertFalse("Unexpected highlights at the end of the sequence", hs.moveNext());
        System.out.println("------------------------\n");
    }
    
    private Document createDocument(Language lang, String text) {
        try {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.putProperty(Language.class, lang);
            doc.insertString(0, text, SimpleAttributeSet.EMPTY);
            return doc;
        } catch (BadLocationException e) {
            fail(e.getMessage());
            return null;
        }
    }
    
    private void assertHighlights(TokenSequence<? extends TokenId> ts, HighlightsSequence hs, boolean moveHs, String indent) {
        while (ts.moveNext()) {
            boolean hasHighlight;
            if (moveHs) {
                hasHighlight = hs.moveNext();
            } else {
                hasHighlight = moveHs = true;
            }
            assertTrue("Wrong number of highlights", hasHighlight);
            
            System.out.println(indent + "Token    : <" + 
                ts.offset() + ", " + 
                (ts.offset() + ts.token().length()) + ", '" + 
                ts.token().text() + "', " + 
                ts.token().id().name() + ">");
            
            TokenSequence<? extends TokenId> embeddedSeq = ts.embedded();
            if (embeddedSeq == null) {
                System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                // XXX: compare attributes as well
            } else {
                int prologueLength = embeddedPrologLength(ts, embeddedSeq);
                int epilogLength = embeddedEpilogLength(ts, embeddedSeq);
                
                if (prologueLength != -1 && epilogLength != -1) {
                    if (prologueLength > 0) {
                        System.out.println(indent + "Prolog   : <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                        assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                        assertEquals("Wrong ending offset", ts.offset() + prologueLength, hs.getEndOffset());
                        // XXX: compare attributes as well
                    }
                    
                    assertHighlights(ts.embedded(), hs, prologueLength > 0, indent + "  ");
                    
                    if (epilogLength > 0) {
                        assertTrue("Wrong number of highlights", hs.moveNext());
                        System.out.println(indent + "Epilog   : <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                        
                        assertEquals("Wrong starting offset", ts.offset() + ts.token().length() - epilogLength, hs.getStartOffset());
                        assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                        // XXX: compare attributes as well
                    }
                } else {
                    System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                    assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                    assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                    // XXX: compare attributes as well
                }
            }
        }
    }
    
    private int embeddedPrologLength(
        TokenSequence<? extends TokenId> embeddingSeq, 
        TokenSequence<? extends TokenId> embeddedSeq) 
    {
        embeddedSeq.moveStart();
        if (embeddedSeq.moveNext()) {
            return embeddedSeq.offset() - embeddingSeq.offset();
        } else {
            return -1;
        }
    }
    
    private int embeddedEpilogLength(
        TokenSequence<? extends TokenId> embeddingSeq, 
        TokenSequence<? extends TokenId> embeddedSeq) 
    {
        embeddedSeq.moveEnd();
        if (embeddedSeq.movePrevious()) {
            return (embeddingSeq.offset() + embeddingSeq.token().length()) - (embeddedSeq.offset() + embeddedSeq.token().length());
        } else {
            return -1;
        }
    }

    private void dumpSequence(HighlightsSequence hs) {
        System.out.println("Dumping sequence: " + hs + " {");
        while(hs.moveNext()) {
            System.out.println("<" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
        }
        System.out.println("} End of sequence: " + hs + " dump ------------");
    }

    private static final class L implements HighlightsChangeListener {
        public int eventsCnt = 0;
        public int lastStartOffset;
        public int lastEndOffset;
        
        public void highlightChanged(HighlightsChangeEvent event) {
            eventsCnt++;
            lastStartOffset = event.getStartOffset();
            lastEndOffset = event.getEndOffset();
        }
    } // End of L class
}
