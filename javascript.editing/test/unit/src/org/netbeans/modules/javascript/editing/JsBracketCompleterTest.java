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

package org.netbeans.modules.javascript.editing;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;

/**
 * @todo Try typing in whole source files and other than tracking missing end and } closure
 *   statements the buffer should be identical - both in terms of quotes to the rhs not having
 *   accumulated as well as indentation being correct.
 * @todo
 *   // automatic reindentation of "end", "else" etc.
 *
 * 
 * 
 * @author Tor Norbye
 */
public class JsBracketCompleterTest extends JsTestBase {
    
    public JsBracketCompleterTest(String testName) {
        super(testName);
    }

    private void match(String original) throws BadLocationException {
        JsBracketCompleter bc = new JsBracketCompleter();
        
        int caretPos = original.indexOf('^');
        
        original = original.substring(0, caretPos) + original.substring(caretPos+1);
        int matchingCaretPos = original.indexOf('^');
        assert caretPos < matchingCaretPos;
        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        OffsetRange range = bc.findMatching(doc, caretPos);
        
        assertNotSame("Didn't find matching token for " + LexUtilities.getToken(doc, caretPos).text().toString(), 
                OffsetRange.NONE, range);
        assertEquals("forward match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                LexUtilities.getToken(doc, matchingCaretPos).text().toString(), 
                matchingCaretPos, range.getStart());
        
        // Perform reverse match
        range = bc.findMatching(doc, matchingCaretPos);
        
        assertNotSame(OffsetRange.NONE, range);
        assertEquals("reverse match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " + 
                LexUtilities.getToken(doc, caretPos).text().toString(), 
                caretPos, range.getStart());
    }
    
    private void insertBreak(String original, String expected) throws BadLocationException {
        JsBracketCompleter bc = new JsBracketCompleter();
        
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);
        int newOffset = bc.beforeBreak(doc, insertOffset, ta);
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.insertString(caret.getDot(), "\n", null);
            // Indent the new line
            JsFormatter formatter = new JsFormatter();
            //ParserResult result = parse(fo);

            int startPos = caret.getDot()+1;
            int endPos = startPos+1;

            //ParserResult result = parse(fo);
            formatter.reindent(doc, startPos, endPos);
            int indent = LexUtilities.getLineIndent(doc, insertOffset+1);

            //bc.afterBreak(doc, insertOffset, caret);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (newOffset != -1) {
                caret.setDot(newOffset);
            } else {
                caret.setDot(insertOffset+1+indent);
            }
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    private void insertChar(String original, char insertText, String expected) throws BadLocationException {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws BadLocationException {
        insertChar(original, insertText, expected, selection, false);
    }

    private void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws BadLocationException {
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        JsBracketCompleter bc = new JsBracketCompleter();

        BaseDocument doc = getDocument(original);
        
        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N        
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);            
        }

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        boolean handled = false;
        try {
            handled = bc.beforeCharInserted(doc, insertOffset, ta, insertText);
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
        if (!handled) {
            if (ta.getSelectedText() != null && ta.getSelectedText().length() > 0) {
                insertOffset = ta.getSelectionStart();
                doc.remove(ta.getSelectionStart(), ta.getSelectionEnd()-ta.getSelectionStart());
                caret.setDot(insertOffset);
            }
            doc.insertString(caret.getDot(), ""+insertText, null);
            caret.setDot(insertOffset+1);
            bc.afterCharInserted(doc, insertOffset, ta, insertText);
        }
        String formatted = doc.getText(0, doc.getLength());
        assertEquals(expected, formatted);
        if (finalCaretPos != -1) {
            assertEquals(finalCaretPos, caret.getDot());
        }
    }

    private void deleteChar(String original, String expected) throws BadLocationException {
        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        JsBracketCompleter bc = new JsBracketCompleter();

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(afterRemoveOffset);
        int dot = afterRemoveOffset;
        char ch = doc.getChars(dot-1, 1)[0];

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.remove(dot - 1, 1);
            caret.setDot(dot-1);
            boolean handled = bc.charBackspaced(doc, dot-1, ta, ch);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }
    
    private void deleteWord(String original, String expected) throws BadLocationException {
        // Try deleting the word not just using the testcase but also surrounded by strings
        // to make sure there's no problem with lexer token directions
        deleteWordImpl(original, expected);
        deleteWordImpl(original+"foo", expected+"foo");
        deleteWordImpl("foo"+original, "foo"+expected);
        deleteWordImpl(original+"::", expected+"::");
        deleteWordImpl(original+"::", expected+"::");
    }
    
    private void deleteWordImpl(String original, String expected) throws BadLocationException {
        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        JsBracketCompleter bc = new JsBracketCompleter();

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(afterRemoveOffset);
        int dot = afterRemoveOffset;
        //REMOVE char ch = doc.getChars(dot-1, 1)[0];

        int begin = bc.getNextWordOffset(doc, dot, true);
        if (begin == -1) {
            begin = Utilities.getPreviousWord(ta, dot);
        }
        
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.remove(begin, dot-begin);
            caret.setDot(begin);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }
    
    private void assertLogicalRange(String source, boolean up, String expected) throws Exception {
        String BEGIN = "%<%"; // NOI18N
        String END = "%>%"; // NOI18N
        int sourceStartPos = source.indexOf(BEGIN);
        if (sourceStartPos != -1) {
            source = source.substring(0, sourceStartPos) + source.substring(sourceStartPos+BEGIN.length());
        }
        
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);

        int sourceEndPos = source.indexOf(END);
        if (sourceEndPos != -1) {
            source = source.substring(0, sourceEndPos) + source.substring(sourceEndPos+END.length());
        }
        
        int expectedStartPos = expected.indexOf(BEGIN);
        if (expectedStartPos != -1) {
            expected = expected.substring(0, expectedStartPos) + expected.substring(expectedStartPos+BEGIN.length());
        }

        int expectedCaretPos = expected.indexOf('^');
        expected = expected.substring(0, expectedCaretPos) + expected.substring(expectedCaretPos+1);
        
        int expectedEndPos = expected.indexOf(END);
        if (expectedEndPos != -1) {
            expected = expected.substring(0, expectedEndPos) + expected.substring(expectedEndPos+END.length());
        }

        assertEquals("Only range markers should differ", source,expected);

        OffsetRange selected = null;
        
        BaseDocument doc = getDocument(source);
        FileObject fileObject = null;
        CompilationInfo info = new TestCompilationInfo(this, fileObject, doc, source);
        
        JsBracketCompleter completer = new JsBracketCompleter();
        List<OffsetRange> ranges = completer.findLogicalRanges(info, caretPos);
        OffsetRange expectedRange;
        if (expectedStartPos != -1) {
            expectedRange = new OffsetRange(expectedStartPos, expectedEndPos);
        } else {
            expectedRange = new OffsetRange(expectedCaretPos, expectedCaretPos);
        }

        if (sourceStartPos != -1) {
            assert sourceEndPos != -1;
            selected = new OffsetRange(sourceStartPos, sourceEndPos);            

            for (int i = 0; i < ranges.size(); i++) {
                if (ranges.get(i).equals(selected)) {
                    if (up) {
                        assertTrue(i < ranges.size()-1);
                        OffsetRange was = ranges.get(i+1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    } else {
                        if (i == 0) {
                            assertEquals(caretPos, expectedCaretPos);
                            return;
                        }
                        OffsetRange was = ranges.get(i-1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    }
                }
            }
            fail("Selection range " + selected + " is not in the range");
        } else {
            assertTrue(ranges.size() > 0);
            OffsetRange was = ranges.get(0);
            assertEquals("Wrong selection: expected \"" + 
                    expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                    source.substring(was.getStart(), was.getEnd()) + "\"",
                    expectedRange, was);
            return;
        }
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("// Hello^", '\'', "// Hello'^");
        insertChar("// Hello^", '"', "// Hello\"^");
        insertChar("// Hello^", '[', "// Hello[^");
        insertChar("// Hello^", '(', "// Hello(^");
        insertChar("/* Hello^*/", '\'', "/* Hello'^*/");
        insertChar("/* Hello^*/", '"', "/* Hello\"^*/");
        insertChar("/* Hello^*/", '[', "/* Hello[^*/");
        insertChar("/* Hello^*/", '(', "/* Hello(^*/");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("x = \"^\"", '\'', "x = \"'^\"");
        insertChar("x = \"^\"", '[', "x = \"[^\"");
        insertChar("x = \"^\"", '(', "x = \"(^\"");
        insertChar("x = \"^)\"", ')', "x = \")^)\"");
        insertChar("x = '^'", '"', "x = '\"^'");
    }
    
    public void testSingleQuotes1() throws Exception {
        insertChar("x = ^", '\'', "x = '^'");
    }

    public void testSingleQuotes2() throws Exception {
        insertChar("x = '^'", '\'', "x = ''^");
    }

    public void testSingleQuotes3() throws Exception {
        insertChar("x = '^'", 'a', "x = 'a^'");
    }

    public void testSingleQuotes4() throws Exception {
        insertChar("x = '\\^'", '\'', "x = '\\'^'");
    }

    public void testInsertBrokenQuote() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^)", '"',
                "System.out.prinlnt(\"pavel\"^)");
    }

    public void testInsertBrokenQuote2() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^\n", '"',
                "System.out.prinlnt(\"pavel\"^\n");
    }
    
    public void testInsertBrokenQuote3() throws Exception {
        insertChar("System.out.prinlnt(\"^\n", '"',
                "System.out.prinlnt(\"\"^\n");
    }
    
    public void testInsertBrokenQuote4() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^", '"',
                "System.out.prinlnt(\"pavel\"^");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("x = ^", '"', "x = \"^\"");
    }

    public void testDoubleQuotes2() throws Exception {
        insertChar("x = \"^\"", '"', "x = \"\"^");
    }

    public void testDoubleQuotes3() throws Exception {
        insertChar("x = \"^\"", 'a', "x = \"a^\"");
    }

    public void testDobuleQuotes4() throws Exception {
        insertChar("x = \"\\^\"", '"', "x = \"\\\"^\"");
    }

    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }
    
    public void testInsertBlockComment() throws Exception {
        insertBreak("/**^", "/**\n * ^\n */");
    }

    public void testInsertBlockComment2() throws Exception {
        insertBreak("    /**^", "    /**\n     * ^\n     */");
    }

    public void testInsertBlockComment3() throws Exception {
        insertBreak("/*^\n", "/*\n * ^\n */\n");
    }

    public void testInsertBlockComment4() throws Exception {
        insertBreak("/*^\nfunction foo() {}", "/*\n * ^\n */\nfunction foo() {}");
    }
    
    public void testInsertBlockComment5() throws Exception {
        insertBreak("^/*\n*/\n", "\n^/*\n*/\n");
    }

// These tests no longer apply -- I'm doing the string-literal insertion differently now    
//    public void testSplitStrings() throws Exception {
//        insertBreak("  x = 'te^st'", "  x = 'te' +\n  '^st'");
//    }
//
//    public void testSplitStrings2() throws Exception {
//        insertBreak("  x = 'test^'", "  x = 'test' +\n  '^'");
//    }
//    
//    public void testSplitStrings3() throws Exception {
//        insertBreak("  x = \"te^st\"", "  x = \"te\" +\n  \"^st\"");
//    }
//    
//    public void testSplitRegexps1() throws Exception {
//        insertBreak("  x = /te^st/", "  x = /te/ +\n  /^st/");
//    }
//
//    public void testSplitRegexps2() throws Exception {
//        insertBreak("  x = /test^/", "  x = /test/ +\n  /^/");
//    }
//

    public void testSplitStrings1() throws Exception {
        insertBreak("  x = 'te^st'", "  x = 'te\\n\\\n^st'");
    }

    public void testSplitStrings1b() throws Exception {
        insertBreak("  x = '^test'", "  x = '\\\n^test'");
    }

    public void testSplitStrings2() throws Exception {
        insertBreak("  x = 'test^'", "  x = 'test\\n\\\n^'");
    }
    
    public void testSplitStrings3() throws Exception {
        insertBreak("  x = \"te^st\"", "  x = \"te\\n\\\n^st\"");
    }
    
    public void testSplitRegexps1() throws Exception {
        insertBreak("  x = /te^st/", "  x = /te\\n\\\n^st/");
    }

    
    public void testSplitRegexps1b() throws Exception {
        insertBreak("  x = /^test/", "  x = /\\\n^test/");
    }

    public void testSplitRegexps2() throws Exception {
        insertBreak("  x = /test^/", "  x = /test\\n\\\n^/");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }
    
    public void testInsertEnd3() throws Exception {
        insertBreak("function foo() {^\n}", "function foo() {\n    ^\n}");
    }

    // THIS IS BROKEN!!!
    //public void testInsertEnd5() throws Exception {
    //    insertBreak("if (a_condition) ^thing() {", "if (a_condition) \n  ^thing()\n}");
    //}
    
    public void testInsertIf1() throws Exception {
        insertBreak("    if (true) {^", "    if (true) {\n        ^\n    }");
    }

    public void testBrackets1() throws Exception {
        insertChar("x = ^", '[', "x = [^]");
    }

    public void testBrackets2() throws Exception {
        insertChar("x = [^]", ']', "x = []^");
    }

    public void testBrackets3() throws Exception {
        insertChar("x = [^]", 'a', "x = [a^]");
    }

    public void testBrackets4() throws Exception {
        insertChar("x = [^]", '[', "x = [[^]]");
    }

    public void testBrackets5() throws Exception {
        insertChar("x = [[^]]", ']', "x = [[]^]");
    }

    public void testBrackets6() throws Exception {
        insertChar("x = [[]^]", ']', "x = [[]]^");
    }

    public void testParens1() throws Exception {
        insertChar("x = ^", '(', "x = (^)");
    }

    public void testParens2() throws Exception {
        insertChar("x = (^)", ')', "x = ()^");
    }

    public void testParens3() throws Exception {
        insertChar("x = (^)", 'a', "x = (a^)");
    }

    public void testParens4() throws Exception {
        insertChar("x = (^)", '(', "x = ((^))");
    }

    public void testParens5() throws Exception {
        insertChar("x = ((^))", ')', "x = (()^)");
    }

    public void testParens6() throws Exception {
        insertChar("x = (()^)", ')', "x = (())^");
    }

    public void testRegexp1() throws Exception {
        insertChar("x = ^", '/', "x = /^/");
    }

    public void testRegexp2() throws Exception {
        insertChar("x = /^/", '/', "x = //^");
    }

    public void testRegexp3() throws Exception {
        insertChar("x = /^/", 'a', "x = /a^/");
    }
    
    public void testRegexp4() throws Exception {
        insertChar("x = /\\^/", '/', "x = /\\/^/");
    }

    public void testRegexp5() throws Exception {
        insertChar("    regexp = /fofo^\n      // Subsequently, you can make calls to it by name with <tt>yield</tt> in", '/',
                "    regexp = /fofo/^\n      // Subsequently, you can make calls to it by name with <tt>yield</tt> in");
    }

    public void testRegexp6() throws Exception {
        insertChar("    regexp = /fofo^\n", '/',
                "    regexp = /fofo/^\n");
    }

    public void testRegexp7() throws Exception {
        insertChar("x = ^\n", '/', "x = /^/\n");
    }

    public void testRegexp8() throws Exception {
        insertChar("x = /^/\n", '/', "x = //^\n");
    }

    public void testRegexp9() throws Exception {
        insertChar("x = /^/\n", 'a', "x = /a^/\n");
    }
    
    public void testRegexp10() throws Exception {
        insertChar("x = /\\^/\n", '/', "x = /\\/^/\n");
    }
    
    public void testRegexp11() throws Exception {
        insertChar("/foo^", '/',
                "/foo/^");
    }

    public void testNotRegexp1() throws Exception {
        insertChar("x = 10 ^", '/', "x = 10 /^");
    }

    public void testNotRegexp2() throws Exception {
        insertChar("x = 3.14 ^", '/', "x = 3.14 /^");
    }

    // This test doesn't work; the lexer identifies x = y / as the
    // beginning of a regular expression. Without the space it DOES
    // work (see regexp4)
    //public void testNotRegexp3() throws Exception {
    //    insertChar("x = y ^", '/', "x = y /^");
    //}

    public void testNotRegexp4() throws Exception {
        insertChar("x = y^", '/', "x = y/^");
    }

    public void testNotRegexp5() throws Exception {
        insertChar("/^", '/', "//^");
    }

    public void testFindMatching1() throws Exception {
        match("if (true) ^{\n^}");
    }
    
    public void testFindMatching2() throws Exception {
        match("x=^(true^)\ny=5");
    }
    
    public void testFindMatching3() throws Exception {
        match("x=^(true || (false)^)\ny=5");
    }

    public void testFindMatching4() throws Exception {
        match("function foo() ^{\nif (true) {\n}\n^}\n}");
    }

    public void testBackspace1() throws Exception {
        deleteChar("x^", "^");
    }

    public void testBackspace2() throws Exception {
        deleteChar("x^y", "^y");
    }
    
    public void testBackspace3() throws Exception {
        deleteChar("xy^z", "x^z");
    }
    
    public void testBackspace4() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace5() throws Exception {
        deleteChar("x=\"^\"", "x=^");
    }
    
    public void testBackspace6() throws Exception {
        deleteChar("x='^'", "x=^");
    }
    
    public void testBackspace7() throws Exception {
        deleteChar("x=(^)", "x=^");
    }

    public void testBackspace7b() throws Exception {
        deleteChar("x=[^]", "x=^");
    }

    public void testBackspace8() throws Exception {
        // See bug 111534
        deleteChar("x={^}", "x=^");
    }
    
    public void testBackspace9() throws Exception {
        deleteChar("x=/^/", "x=^");
    }
    

    public void testContComment() throws Exception {
        if (JsBracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }
    
    public void testContComment2() throws Exception {
        // No auto-# on new lines
        if (JsBracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("   //  ^", "   //  \n   //  ^");
        } else {
            insertBreak("   //  ^", "   //  \n   ^");
        }
    }
    
    public void testContComment3() throws Exception {
        // No auto-# on new lines
        if (JsBracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("   //\t^", "   //\t\n   //\t^");
        } else {
            insertBreak("   //\t^", "   //\t\n   ^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment5() throws Exception {
        // No auto-# on new lines
        if (JsBracketCompleter.CONTINUE_COMMENTS) {
            insertBreak("      // ^", "      // \n      // ^");
        } else {
            insertBreak("      // ^", "      // \n      ^");
        }
    }
    
    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }
    
    public void testContComment7() throws Exception {
        insertBreak("   // foo^\n   // bar", "   // foo\n   // ^\n   // bar");
    }
    
    public void testContComment8() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    
    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment10() throws Exception {
        insertBreak("//foo\n^// foobar", "//foo\n// ^\n// foobar");
    }

    public void testContComment11() throws Exception {
        // This behavior is debatable -- to be consistent with testContComment10 I
        // should arguably continue comments here as well
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment12() throws Exception {
        insertBreak("  code\n^// foobar", "  code\n\n  ^// foobar");
    }
    
    public void testContComment14() throws Exception {
        insertBreak("function foo() {\n    code\n^// foobar\n}\n", "function foo() {\n    code\n\n    ^// foobar\n}\n");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testContComment17() throws Exception {
        insertBreak("function foo() {\n  // cmnt1\n^  // cmnt2\n}\n", "function foo() {\n  // cmnt1\n  // ^\n  // cmnt2\n}\n");
    }
    
    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }

    public void testDeleteContComment() throws Exception {
        deleteChar("// ^", "^");
        deleteChar("\n// ^", "\n^");
    }
    
    public void testDeleteContComment2() throws Exception {
        deleteChar("// ^  ", "^  ");
        deleteChar("\n// ^  ", "\n^  ");
    }
    
    public void testNoDeleteContComment() throws Exception {
        deleteChar("//  ^", "// ^");
        deleteChar("//^", "^");
        deleteChar("puts ('// ^')", "puts ('//^')");
    }

    public void testNoInsertPercentElsewhere() throws Exception {
        insertChar("x = ^", '#', "x = #^");
    }
    
    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
    }

    public void testReplaceSelection5() throws Exception {
        insertChar("'(^position:absolute;'", '{', "'{^position:absolute;'", "(");
    }

    public void testReplaceSelection6() throws Exception {
        insertChar("'position^:absolute;'", '{', "'pos{^:absolute;'", "ition");
    }

    public void testReplaceSelectionChangeType1() throws Exception {
        insertChar("x = \"foo\"^", '\'', "x = 'foo'^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType2() throws Exception {
        insertChar("x = \"foo\"^", '{', "x = {foo}^", "\"foo\"");
    }

    public void testReplaceSelectionNotInTemplateMode1() throws Exception {
        insertChar("x = foo^", '"', "x = \"^\"", "foo", true);
    }
    
    public void testdeleteWord() throws Exception {
        deleteWord("foo_bar_baz^", "foo_bar_^");
    }

    public void testdeleteWord111303() throws Exception {
        deleteWord("foo::bar^", "foo::^");
        deleteWord("Foo::Bar^", "Foo::^");
        deleteWord("Foo::Bar_Baz^", "Foo::Bar_^");
    }
    public void testdeleteWordx111305() throws Exception {
        deleteWord("foo_bar^", "foo_^");
        deleteWord("x.foo_bar^.y", "x.foo_^.y");
    }

    public void testdeleteWord2() throws Exception {
        deleteWord("foo_bar_baz ^", "foo_bar_baz^");
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testdeleteWord3() throws Exception {
        deleteWord("FooBarBaz^", "FooBar^");
    }
    
    public void testDeleteWord4_110998() throws Exception {
        deleteWord("Blah::Set^Foo", "Blah::^Foo");
    }

    public void testdeleteWord5() throws Exception {
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testdeleteWords() throws Exception {
        deleteWord("foo bar^", "foo ^");
    }


    public void testDeleteWord4_110998c() throws Exception {
        String before = "  snark^\n";
        String after = "  ^\n";
        deleteWord(before, after);
    }
    
    public void testBackwardsDeletion() throws Exception {
        String s = "Foo::Bar = whatever('hello')  \n  nextline";
        JsBracketCompleter bc = new JsBracketCompleter();
        for (int i = s.length(); i >= 1; i--) {
            String shortened = s.substring(0, i);
            BaseDocument doc = getDocument(shortened);

            JTextArea ta = new JTextArea(doc);
            Caret caret = ta.getCaret();
            int dot = i;
            caret.setDot(dot);
            int begin = bc.getNextWordOffset(doc, dot, true);
            if (begin == -1) {
                begin = Utilities.getPreviousWord(ta, dot);
            }
            
            assert begin != -1 && begin < i;
        }
    }
    
    public void testLogicalRange1() throws Exception {
        String code = "if (true) {\n  fo^o\n}";
        String next = "if (true) {\n  %<%fo^o%>%\n}";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange2() throws Exception {
        String code = "if (true) {\n  %<%fo^o%>%\n}";
        String next = "%<%if (true) {\n  fo^o\n}%>%";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRange3() throws Exception {
        String code = "function foo() {\nif (true) {\n  %<%fo^o%>%\n}\n}";
        String next = "function foo() {\n%<%if (true) {\n  fo^o\n}%>%\n}";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeComment1() throws Exception {
        String code = "foo\n  // Foo Bar\n  // Foo^y Baary\n  // Bye\nfunction foo() {\n}\n";
        String next = "foo\n  // Foo Bar\n  %<%// Foo^y Baary%>%\n  // Bye\nfunction foo() {\n}\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeComment2() throws Exception {
        String code = "foo\n  // Foo Bar\n  %<%// Foo^y Baary%>%\n  // Bye\nfunction foo() {\n}\n";
        String next = "foo\n  %<%// Foo Bar\n  // Foo^y Baary\n  // Bye%>%\nfunction foo() {\n}\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeCommentBlocks() throws Exception {
        String code = "foo\n  /* Foo Bar\n  * Foo^y Baary\n  * Bye */\nfunction foo() {\n}\n";
        String next = "foo\n  %<%/* Foo Bar\n  * Foo^y Baary\n  * Bye */%>%\nfunction foo() {\n}\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }
    
    public void testLogicalRangeComment3() throws Exception {
        String code = "foo\n  // Foo Bar\n\n  %<%// Foo^y Baary%>%\n  // Bye\nfunction foo() {\n}\n";
        String next = "foo\n  // Foo Bar\n\n  %<%// Foo^y Baary\n  // Bye%>%\nfunction foo() {\n}\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }
    public void testLogicalRangeComment5() throws Exception {
        String code = "foo\n  foo // Foo Bar\n  %<%// Foo^y Baary%>%\n  // Bye\nfunction foo() {\n}\n";
        String next = "foo\n  foo // Foo Bar\n  %<%// Foo^y Baary\n  // Bye%>%\nfunction foo() {\n}\n";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }
    
    public void testLogicalRangeStrings1() throws Exception {
        String code = "x = 'foo b^ar baz', y = \"whatever\"";
        String next = "x = %<%'foo b^ar baz'%>%, y = \"whatever\"";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeStrings3() throws Exception {
        String code = "function foo() {\nif (true) {\nx = %<%'foo b^ar baz'%>%\n}\n}";
        String next = "function foo() {\nif (true) {\n%<%x = 'foo b^ar baz'%>%\n}\n}";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeStrings4() throws Exception {
        String code = "function foo() {\nif (true) {\n%<%x = 'foo b^ar baz'%>%\n}\n}";
        String next = "function foo() {\n%<%if (true) {\nx = 'foo b^ar baz'\n}%>%\n}";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    public void testLogicalRangeStrings5() throws Exception {
        String code = "function test() {\n 'return^ me'\n}";
        String next = "function test() {\n %<%'return^ me'%>%\n}";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }

    // THIS IS BROKEN - THERE IS AN EXTRA BLOCK IN THERE WITH BAD OFFSETS!
    //public void testLogicalRangeStrings6() throws Exception {
    //    String code = "function test() {\n %<%'return^ me'%>%\n}";
    //    String next = "%<%function test() {\n 'return^ me'\n}%>%";
    //    assertLogicalRange(code, true, next);
    //    assertLogicalRange(next, false, code);
    //}

    public void testLogicalRangeRegexps1() throws Exception {
        String code = "x = /foo b^ar baz/, y = \"whatever\"";
        String next = "x = %<%/foo b^ar baz/%>%, y = \"whatever\"";
        assertLogicalRange(code, true, next);
        assertLogicalRange(next, false, code);
    }
}
