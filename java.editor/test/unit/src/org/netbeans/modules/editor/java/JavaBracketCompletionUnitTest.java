/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.java.BracketCompletion;


/**
 * Test java bracket completion.
 *
 * @autor Miloslav Metelka
 */
public class JavaBracketCompletionUnitTest extends JavaBaseDocumentUnitTestCase {
    
    public JavaBracketCompletionUnitTest(String testMethodName) {
        super(testMethodName);
    }
    
    // ------- Tests for completion of right parenthesis ')' -------------
    
    public void testRightParenSimpleMethodCall() {
        setLoadDocumentText("m()|)");
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenSwingInvokeLaterRunnable() {
        setLoadDocumentText("SwingUtilities.invokeLater(new Runnable()|))");
        assertTrue(isSkipRightParen());
    }

    public void testRightParenSwingInvokeLaterRunnableRun() {
        setLoadDocumentText(
            "SwingUtilities.invokeLater(new Runnable() {\n"
          + "    public void run()|)\n"
          + "})"  
            
        );
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenIfMethodCall() {
        setLoadDocumentText(
            " if (a()|) + 5 > 6) {\n"
          + " }"
        );
        assertTrue(isSkipRightParen());
    }

    public void testRightParenNoSkipNonBracketChar() {
        setLoadDocumentText("m()| ");
        assertFalse(isSkipRightParen());
    }

    public void testRightParenNoSkipDocEnd() {
        setLoadDocumentText("m()|");
        assertFalse(isSkipRightParen());
    }

    
    // ------- Tests for completion of right brace '}' -------------
    
    public void testAddRightBraceIfLeftBrace() {
        setLoadDocumentText("if (true) {|");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceWhiteSpace() {
        setLoadDocumentText("if (true) { \t|\n");
        assertTrue(isAddRightBrace());
    }
    
    public void testAddRightBraceIfLeftBraceLineComment() {
        setLoadDocumentText("if (true) { // line-comment|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceBlockComment() {
        setLoadDocumentText("if (true) { /* block-comment */|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceAlreadyPresent() {
        setLoadDocumentText(
            "if (true) {|\n"
          + "}"
        );
        assertFalse(isAddRightBrace());
    }

    public void testAddRightBraceCaretInComment() {
        setLoadDocumentText(
            "if (true) { /* in-block-comment |\n"
        );
        assertFalse(isAddRightBrace());
    }
    
    public void testSimpleAdditionOfOpeningParenthesisAfterWhile () throws Exception {
        setLoadDocumentText (
            "while |"
        );
        typeChar('(');
        assertDocumentTextAndCaret ("Even a closing ')' should be added", 
            "while (|)"
        );
    }

    
    // ------- Tests for completion of quote (") -------------    
    public void testSimpleQuoteInEmptyDoc () throws Exception {
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote In Empty Doc", 
            "\"|\""
        );
    }

    public void testSimpleQuoteAtBeginingOfDoc () throws Exception {
        setLoadDocumentText (
            "|  "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote At Begining Of Doc", 
            "\"|\"  "
        );
    }

    public void testSimpleQuoteAtEndOfDoc () throws Exception {
        setLoadDocumentText (
            "  |"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote At End Of Doc", 
            "  \"|\""
        );
    }
    
    public void testSimpleQuoteInWhiteSpaceArea () throws Exception {
        setLoadDocumentText (
            "  |  "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Simple Quote In White Space Area", 
            "  \"|\"  "
        );
    }
    
    public void testQuoteAtEOL () throws Exception {
        setLoadDocumentText (
            "  |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote At EOL", 
            "  \"|\"\n"
        );
    }
    
    public void testQuoteWithUnterminatedStringLiteral () throws Exception {
        setLoadDocumentText (
            "  \"unterminated string| \n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote With Unterminated String Literal", 
            "  \"unterminated string\"| \n"
        );
    }
    
    public void testQuoteAtEOLWithUnterminatedStringLiteral () throws Exception {
        setLoadDocumentText (
            "  \"unterminated string |\n"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote At EOL With Unterminated String Literal", 
            "  \"unterminated string \"|\n"
        );
    }

    public void testQuoteInsideStringLiteral () throws Exception {
        setLoadDocumentText (
            "  \"stri|ng literal\" "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside String Literal", 
            "  \"stri\"|ng literal\" "
        );
    }

    public void testQuoteInsideEmptyParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(|) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Empty Parentheses", 
            " System.out.println(\"|\") "
        );
    }

    public void testQuoteInsideNonEmptyParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(|some text) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses", 
            " System.out.println(\"|some text) "
        );
    }
    
    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(i+|) "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses Before Closing Parentheses", 
            " System.out.println(i+\"|\") "
        );
    }
    
    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral () throws Exception {
        setLoadDocumentText (
            " System.out.println(\"unterminated string literal |); "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Non Empty Parentheses Before Closing Parentheses And Unterminated String Literal", 
            " System.out.println(\"unterminated string literal \"|); "
        );
    }

    public void testQuoteBeforePlus () throws Exception {
        setLoadDocumentText (
            " System.out.println(|+\"string literal\"); "
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Plus", 
            " System.out.println(\"|\"+\"string literal\"); "
        );
    }

    public void testQuoteBeforeComma () throws Exception {
        setLoadDocumentText (
            "String s[] = new String[]{|,\"two\"};"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Comma", 
            "String s[] = new String[]{\"|\",\"two\"};"
        );
    }

    public void testQuoteBeforeBrace () throws Exception {
        setLoadDocumentText (
            "String s[] = new String[]{\"one\",|};"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Brace", 
            "String s[] = new String[]{\"one\",\"|\"};"
        );
    }

    public void testQuoteBeforeSemicolon() throws Exception {
        setLoadDocumentText (
            "String s = \"\" + |;"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon", 
            "String s = \"\" + \"|\";"
        );
    }

    public void testQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setLoadDocumentText (
            "String s = \"\" +| ;"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon With Whitespace", 
            "String s = \"\" +\"|\" ;"
        );
    }

    public void testQuoteAfterEscapeSequence() throws Exception {
        setLoadDocumentText (
            "\\|"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Before Semicolon With Whitespace", 
            "\\\"|"
        );
    }
    
    /** issue #69524 */
    public void testQuoteEaten() throws Exception {
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('"');
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Eaten", 
            "\"\"|"
        );
    }    

    /** issue #69935 */
    public void testQuoteInsideComments() throws Exception {
        setLoadDocumentText (
            "/** |\n */"
        );
        typeQuoteChar('"');
        assertDocumentTextAndCaret ("Quote Inside Comments", 
            "/** \"|\n */"
        );
    }    
    
    // ------- Tests for completion of single quote (') -------------        
    
    public void testSingleQuoteInEmptyDoc () throws Exception {
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote In Empty Doc", 
            "'|'"
        );
    }

    public void testSingleQuoteAtBeginingOfDoc () throws Exception {
        setLoadDocumentText (
            "|  "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At Begining Of Doc", 
            "'|'  "
        );
    }

    public void testSingleQuoteAtEndOfDoc () throws Exception {
        setLoadDocumentText (
            "  |"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At End Of Doc", 
            "  '|'"
        );
    }
    
    public void testSingleQuoteInWhiteSpaceArea () throws Exception {
        setLoadDocumentText (
            "  |  "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote In White Space Area", 
            "  '|'  "
        );
    }
    
    public void testSingleQuoteAtEOL () throws Exception {
        setLoadDocumentText (
            "  |\n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At EOL", 
            "  '|'\n"
        );
    }
    
    public void testSingleQuoteWithUnterminatedCharLiteral () throws Exception {
        setLoadDocumentText (
            "  '| \n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote With Unterminated Char Literal", 
            "  ''| \n"
        );
    }
    
    public void testSingleQuoteAtEOLWithUnterminatedCharLiteral () throws Exception {
        setLoadDocumentText (
            "  ' |\n"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote At EOL With Unterminated Char Literal", 
            "  ' '|\n"
        );
    }

    public void testSingleQuoteInsideCharLiteral () throws Exception {
        setLoadDocumentText (
            "  '| ' "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Char Literal", 
            "  ''| ' "
        );
    }

    public void testSingleQuoteInsideEmptyParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(|) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Empty Parentheses", 
            " System.out.println('|') "
        );
    }

    public void testSingleQuoteInsideNonEmptyParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(|some text) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses", 
            " System.out.println('|some text) "
        );
    }
    
    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParentheses () throws Exception {
        setLoadDocumentText (
            " System.out.println(i+|) "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses Before Closing Parentheses", 
            " System.out.println(i+'|') "
        );
    }
    
    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedCharLiteral () throws Exception {
        setLoadDocumentText (
            " System.out.println(' |); "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Non Empty Parentheses Before Closing Parentheses And Unterminated Char Literal", 
            " System.out.println(' '|); "
        );
    }

    public void testSingleQuoteBeforePlus () throws Exception {
        setLoadDocumentText (
            " System.out.println(|+\"string literal\"); "
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Plus", 
            " System.out.println('|'+\"string literal\"); "
        );
    }

    public void testSingleQuoteBeforeComma () throws Exception {
        setLoadDocumentText (
            "String s[] = new String[]{|,\"two\"};"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Comma", 
            "String s[] = new String[]{'|',\"two\"};"
        );
    }

    public void testSingleQuoteBeforeBrace () throws Exception {
        setLoadDocumentText (
            "String s[] = new String[]{\"one\",|};"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Brace", 
            "String s[] = new String[]{\"one\",'|'};"
        );
    }

    public void testSingleQuoteBeforeSemicolon() throws Exception {
        setLoadDocumentText (
            "String s = \"\" + |;"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon", 
            "String s = \"\" + '|';"
        );
    }

    public void testsingleQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setLoadDocumentText (
            "String s = \"\" +| ;"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon With Whitespace", 
            "String s = \"\" +'|' ;"
        );
    }

    public void testSingleQuoteAfterEscapeSequence() throws Exception {
        setLoadDocumentText (
            "\\|"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Before Semicolon With Whitespace", 
            "\\'|"
        );
    }
    
    /** issue #69524 */
    public void testSingleQuoteEaten() throws Exception {
        setLoadDocumentText (
            "|"
        );
        typeQuoteChar('\'');
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Eaten", 
            "''|"
        );
    }    
    
    /** issue #69935 */
    public void testSingleQuoteInsideComments() throws Exception {
        setLoadDocumentText (
            "/* |\n */"
        );
        typeQuoteChar('\'');
        assertDocumentTextAndCaret ("Single Quote Inside Comments", 
            "/* \'|\n */"
        );
    }    
    
    
    // ------- Private methods -------------
    
    private void typeChar(char ch) throws Exception {
        int pos = getCaretOffset();
        getDocument ().insertString(pos, String.valueOf(ch), null);
        BracketCompletion.charInserted(getDocument(), pos, getCaret(), ch);
    }
    
    private void typeQuoteChar(char ch) throws Exception {
        int pos = getCaretOffset();
        Caret caret = getCaret();
        boolean inserted = BracketCompletion.completeQuote(getDocument(), pos, caret, ch);        
        if (inserted){
            caret.setDot(pos+1);
        }else{
            getDocument ().insertString(pos, String.valueOf(ch), null);
        }
    }
    
    private boolean isSkipRightParen() {
        return isSkipRightBracketOrParen(true);
    }
    
    private boolean isSkipRightBracket() {
        return isSkipRightBracketOrParen(false);
    }
    
    private boolean isSkipRightBracketOrParen(boolean parenthesis) {
        TokenID bracketTokenId = parenthesis
        ? JavaTokenContext.RPAREN
        : JavaTokenContext.RBRACKET;
        
        try {
            return BracketCompletion.isSkipClosingBracket(getDocument(),
            getCaretOffset(), bracketTokenId);
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }
    
    private boolean isAddRightBrace() {
        try {
            return BracketCompletion.isAddRightBrace(getDocument(),
            getCaretOffset());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }

}
