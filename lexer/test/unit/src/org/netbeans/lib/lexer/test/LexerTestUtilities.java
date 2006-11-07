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

package org.netbeans.lib.lexer.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.test.dump.TokenDumpCheck;


/**
 * Various utilities related to lexer's and token testing.
 *
 * @author mmetelka
 */
public final class LexerTestUtilities {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    private static final String LAST_TOKEN_HIERARCHY = "last-token-hierarchy";

    private static Field tokenListField;
    
    private LexerTestUtilities() {
        // no instances
    }
    
    /**
     * @see #assertTokenEquals(String, TokenSequence, TokenId, String, int)
     */
    public static void assertTokenEquals(TokenSequence ts, TokenId id, String text, int offset) {
        assertTokenEquals(null, ts, id, text, offset);
    }

    /**
     * Compare <code>TokenSequence.token()</code> to the given
     * token id, text and offset.
     *
     * @param offset expected offset. It may be -1 to prevent offset testing.
     */
    public static void assertTokenEquals(String message, TokenSequence ts, TokenId id, String text, int offset) {
        message = messagePrefix(message);
        Token t = ts.token();
        TokenId tId = t.id();
        TestCase.assertEquals(message + "Invalid token.id()", id, tId);
        CharSequence tText = t.text();
        assertTextEquals(message + "Invalid token.text()", text, tText);
        // The token's length must correspond to text.length()
        TestCase.assertEquals(message + "Invalid token.length()", text.length(), t.length());

        if (offset != -1) {
            int tsOffset = ts.offset();
            TestCase.assertEquals(message + "Invalid tokenSequence.offset()", offset, tsOffset);

            // It should also be true that if the token is non-flyweight then
            // ts.offset() == t.offset()
            // and if it's flyweight then t.offset() == -1
            int tOffset = t.offset(null);
            assertTokenOffsetMinusOneForFlyweight(t.isFlyweight(), tOffset);
            if (!t.isFlyweight()) {
                assertTokenOffsetsEqual(message, tOffset, offset);
            }
        }
    }
    
    public static void assertTokenEquals(TokenSequence ts, TokenId id, String text, int offset,
    int lookahead, Object state) {
        assertTokenEquals(null, ts, id, text, offset, lookahead, state);
    }

    public static void assertTokenEquals(String message, TokenSequence ts, TokenId id, String text, int offset,
    int lookahead, Object state) {
        assertTokenEquals(message, ts, id, text, offset);

        Token t = ts.token();
        message = messagePrefix(message);
        TestCase.assertEquals(message + "Invalid token.lookahead()", lookahead, lookahead(ts));
        TestCase.assertEquals(message + "Invalid token.state()", state, state(ts));
    }
    
    public static void assertTokenOffsetsEqual(String message, int offset1, int offset2) {
        if (offset1 != -1 && offset2 != -1) { // both non-flyweight
            TestCase.assertEquals(messagePrefix(message)
                    + "Offsets equal", offset1, offset2);
        }
    }
    
    public static void assertTokenFlyweight(Token token) {
        TestCase.assertEquals("Token flyweight", true, token.isFlyweight());
    }
    
    public static void assertTokenNotFlyweight(Token token) {
        TestCase.assertEquals("Token not flyweight", true, !token.isFlyweight());
    }
    
    private static void assertTokenOffsetMinusOneForFlyweight(boolean tokenFlyweight, int offset) {
        if (tokenFlyweight) {
            TestCase.assertEquals("Flyweight token => token.offset()=-1", -1, offset);
        } else { // non-flyweight
            TestCase.assertTrue("Non-flyweight token => token.offset()!=-1 but " + offset, (offset != -1));
        }
    }

    /**
     * Assert that the next token in the token sequence
     */
    public static void assertNextTokenEquals(TokenSequence ts, TokenId id, String text) {
        assertNextTokenEquals(null, ts, id, text);
    }

    public static void assertNextTokenEquals(String message, TokenSequence ts, TokenId id, String text) {
        String messagePrefix = messagePrefix(message);
        TestCase.assertTrue(messagePrefix + "No next token available", ts.moveNext());
        assertTokenEquals(message, ts, id, text, -1);
    }
    
    /**
     * @see #assertTokenSequencesEqual(String,TokenSequence,TokenHierarchy,TokenSequence,TokenHierarchy,boolean)
     */
    public static void assertTokenSequencesEqual(
    TokenSequence expected, TokenHierarchy expectedHi,
    TokenSequence actual, TokenHierarchy actualHi,
    boolean testLookaheadAndState) {
        assertTokenSequencesEqual(null, expected, expectedHi, actual, actualHi, testLookaheadAndState);
    }

    /**
     * Compare contents of the given token sequences by moving through all their
     * tokens.
     * <br/>
     * Token hierarchies are given to check implementations
     * of the Token.offset(TokenHierarchy) - useful for checking of token snapshots.
     *
     * @param message message to display (may be null).
     * @param expected non-null token sequence to be compared to the other token sequence.
     * @param expectedHi token hierarchy to which expected relates.
     * @param actual non-null token sequence to be compared to the other token sequence.
     * @param actualHi token hierarchy to which actual relates.
     * @param testLookaheadAndState whether lookahead and states should be checked
     *  or not. Generally it should be true but for snapshots checking it must
     *  be false because snapshots do not hold lookaheads and states.
     */
    public static void assertTokenSequencesEqual(String message,
    TokenSequence expected, TokenHierarchy expectedHi,
    TokenSequence actual, TokenHierarchy actualHi,
    boolean testLookaheadAndState) {
        boolean success = false;
        try {
            String prefix = messagePrefix(message);
            TestCase.assertEquals(prefix + "Move previous: ", expected.movePrevious(), actual.movePrevious());
            while (expected.moveNext()) {
                TestCase.assertTrue(prefix + "Move next: ", actual.moveNext());
                assertTokensEqual(message, expected, expectedHi, actual, actualHi, testLookaheadAndState);
            }
            TestCase.assertFalse(prefix + "Move next not disabled", actual.moveNext());
            success = true;
        } finally {
            if (!success) {
                System.err.println("Expected token sequence dump:\n" + expected);
                System.err.println("Test token sequence dump:\n" + actual);
            }
        }
    }

    private static void assertTokensEqual(String message,
    TokenSequence ts, TokenHierarchy tokenHierarchy,
    TokenSequence ts2, TokenHierarchy tokenHierarchy2, boolean testLookaheadAndState) {
        Token t = ts.token();
        Token t2 = ts2.token();

        message = messagePrefix(message);
        TestCase.assertEquals(message + "Invalid token id", t.id(), t2.id());
        assertTextEquals(message + "Invalid token text", t.text(), t2.text());
        
        assertTokenOffsetsEqual(message, t.offset(tokenHierarchy), t2.offset(tokenHierarchy2));
        TestCase.assertEquals(message + "Invalid tokenSequence offset", ts.offset(), ts2.offset());

        // Checking LOOKAHEAD and STATE matching in case they are filled in (during tests)
        if (testing && testLookaheadAndState) {
            TestCase.assertEquals(message + "Invalid token.lookahead()", lookahead(ts), lookahead(ts2));
            TestCase.assertEquals(message + "Invalid token.state()", state(ts), state(ts2));
        }
        TestCase.assertEquals(message + "Invalid token length", t.length(), t2.length());
    }
    
    /**
     * Compute number of flyweight tokens in the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return number of flyweight tokens in the token sequence.
     */
    public static int flyweightTokenCount(TokenSequence ts) {
        int flyTokenCount = 0;
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                flyTokenCount++;
            }
        }
        return flyTokenCount;
    }
    
    /**
     * Compute total number of characters represented by flyweight tokens
     * in the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return number of characters contained in the flyweight tokens
     *  in the token sequence.
     */
    public static int flyweightTextLength(TokenSequence ts) {
        int flyTokenTextLength = 0;
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                flyTokenTextLength += ts.token().text().length();
            }
        }
        return flyTokenTextLength;
    }
    
    /**
     * Compute distribution of flyweight token lengths accross the given token sequence.
     *
     * @param ts non-null token sequence.
     * @return non-null list containing number of the flyweight tokens that have the length
     *  equal to the index in the list.
     */
    public static List<Integer> flyweightDistribution(TokenSequence ts) {
        List<Integer> distribution = new ArrayList<Integer>();
        ts.moveIndex(0);
        while (ts.moveNext()) {
            if (ts.token().isFlyweight()) {
                int len = ts.token().text().length();
                while (distribution.size() <= len) {
                    distribution.add(0);
                }
                distribution.set(len, distribution.get(len) + 1);
            }
        }
        return distribution;
    }
    
    public static boolean collectionsEqual(Collection<?> c1, Collection<?> c2) {
        return c1.containsAll(c2) && c2.containsAll(c1);
    }
    
    public static void assertCollectionsEqual(Collection expected, Collection actual) {
        assertCollectionsEqual(null, expected, actual);
    }

    public static void assertCollectionsEqual(String message, Collection expected, Collection actual) {
        if (!collectionsEqual(expected, actual)) {
            message = messagePrefix(message);
            for (Iterator it = expected.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!actual.contains(o)) {
                    System.err.println(actual.toString());
                    TestCase.fail(message + " Object " + o + " not contained in tested collection");
                }
            }
            for (Iterator it = actual.iterator(); it.hasNext();) {
                Object o = it.next();
                if (!expected.contains(o)) {
                    System.err.println(actual.toString());
                    TestCase.fail(message + " Extra object " + o + " contained in tested collection");
                }
            }
            TestCase.fail("Collections not equal for unknown reason!");
        }
    }
    
    public static void incCheck(Document doc, boolean nested) {
        TokenHierarchy thInc = TokenHierarchy.get(doc);
        @SuppressWarnings("unchecked")
        Language<TokenId> language = (Language<TokenId>)
                doc.getProperty(Language.class);
        String docText = null;
        try {
            docText = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            TestCase.fail("BadLocationException occurred");
        }
        TokenHierarchy thBatch = TokenHierarchy.create(docText, language);
        boolean success = false;
        try {
            // Compare lookaheads and states as well
            assertTokenSequencesEqual(thBatch.tokenSequence(), thBatch,
                    thInc.tokenSequence(), thInc, true);
            success = true;
        } finally {
            if (!success) {
                System.err.println("BATCH token sequence dump:\n" + thBatch.tokenSequence());
                TokenHierarchy lastHi = (TokenHierarchy)doc.getProperty(LAST_TOKEN_HIERARCHY);
                if (lastHi != null) {
                    System.err.println("PREVIOUS token sequence dump:\n" + lastHi.tokenSequence());
                }
            }
        }
        
        // Check the change since last modification
        TokenHierarchy lastHi = (TokenHierarchy)doc.getProperty(LAST_TOKEN_HIERARCHY);
        if (lastHi != null) {
            // TODO comparison
        }
        doc.putProperty(LAST_TOKEN_HIERARCHY, thBatch); // new last batch token hierarchy
    }
    
    /**
     * Start to listen for changes in the token hierarchy.
     */
    public static void incInit(Document doc) {
        TestCase.assertNull(doc.getProperty(TokenHierarchyListener.class));
        doc.putProperty(TokenHierarchyListener.class, TestTokenChangeListener.INSTANCE);
    }

    /**
     * Get lookahead for the token to which the token sequence is positioned.
     * <br/>
     * The method uses reflection to get reference to tokenList field in token sequence.
     */
    public static int lookahead(TokenSequence ts) {
        return tokenList(ts).lookahead(ts.index());
    }

    /**
     * Get state for the token to which the token sequence is positioned.
     * <br/>
     * The method uses reflection to get reference to tokenList field in token sequence.
     */
    public static Object state(TokenSequence ts) {
        return tokenList(ts).state(ts.index());
    }

    /**
     * Compare whether the two character sequences represent the same text.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        return TokenUtilities.equals(text1, text2);
    }
    
    public static void assertTextEquals(CharSequence expected, CharSequence actual) {
        assertTextEquals(null, expected, actual);
    }
    
    public static void assertTextEquals(String message, CharSequence expected, CharSequence actual) {
        if (!textEquals(expected, actual)) {
            TestCase.fail(messagePrefix(message) +
                " expected:\"" + expected + "\" but was:\"" + actual + "\"");
        }
    }
    
    /**
     * Return the given text as String
     * translating the special characters (and '\') into escape sequences.
     *
     * @param text non-null text to be debugged.
     * @return non-null string containing the debug text.
     */
    public static String debugText(CharSequence text) {
        return TokenUtilities.debugText(text);
    }
    
    public static void initLastDocumentEventListening(Document doc) {
        doc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            public void removeUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            public void changedUpdate(DocumentEvent evt) {
                storeEvent(evt);
            }
            private void storeEvent(DocumentEvent evt) {
                evt.getDocument().putProperty(DocumentEvent.class, evt);
            }
        });
    }
    
    /**
     * Get token list from the given token sequence for testing purposes.
     */
    public static TokenList tokenList(TokenSequence ts) {
        try {
            if (tokenListField == null) {
                tokenListField = ts.getClass().getDeclaredField("tokenList");
                tokenListField.setAccessible(true);
            }
            return (TokenList)tokenListField.get(ts);
        } catch (Exception e) {
            TestCase.fail(e.getMessage());
            return null; // never reached
        }
    }
    
    private static String messagePrefix(String message) {
        if (message != null) {
            message = message + ": ";
        } else {
            message = "";
        }
        return message;
    }

    /**
     * Set whether the lexer should run in testing mode where there are some
     * additional correctness checks performed.
     */
    public static void setTesting(boolean testing) {
        System.setProperty("netbeans.debug.lexer.test", testing ? "true" : "false");
    }
    
    /**
     * Check whether token descriptions dump file (a file with added suffix ".tokens.txt")
     * for the given input file exists and whether it has the same content
     * like the one obtained by lexing the input file.
     * <br/>
     * It allows to test whether the tested lexer still produces the same tokens.
     * <br/>
     * The method will only pass successfully if both the input file and token descriptions
     * files exist and the token descriptions file contains the same information
     * as the generated files.
     * <br/>
     * If the token descriptions file does not exist the method will create it.
     * <br/>
     * As the lexer's behavior at the EOF is important and should be well tested
     * there is a support for splitting input file virtually into multiple inputs
     * by virtual EOF - see <code>TokenDumpTokenId</code> for details.
     * <br/>
     * Also there is possibility to specify special chars
     * - see <code>TokenDumpTokenId</code> for details.
     *
     * @param test non-null test (used for calling test.getDataDir()).
     * @param relFilePath non-null file path relative to datadir of the test.
     *  <br/>
     *  For example if "testfiles/testinput.mylang.txt" gets passed the test method will
     *  search for <code>new File(test.getDataDir() + "testfiles/testinput.mylang.txt")</code>,
     *  read its content, lex it and create token descriptions. Then it will search for 
     *  <code>new File(test.getDataDir() + "testfiles/testinput.mylang.txt.tokens.txt")</code>
     *  and it will compare the file content with the generated descriptions.
     *  
     */
    public static void checkTokenDump(NbTestCase test, String relFilePath,
    Language<? extends TokenId> language) throws Exception {
        TokenDumpCheck.checkTokenDump(test, relFilePath, language);
    }
    
    private static final class TestTokenChangeListener implements TokenHierarchyListener {
        
        static TestTokenChangeListener INSTANCE = new TestTokenChangeListener();
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            TokenHierarchy hi = evt.tokenHierarchy();
            Document d = (Document)hi.mutableInputSource();
            d.putProperty(TokenHierarchyEvent.class, evt);
        }
        
    }
}
