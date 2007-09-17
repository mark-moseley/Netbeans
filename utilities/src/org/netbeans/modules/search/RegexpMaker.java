/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.util.regex.Pattern;

/**
 * Parser of simple regular expressions with only three supported special
 * characters {@code '*'} (zero or more), {@code '?'} (zero or one)
 * and {@code '\\'} (quotes the following character).
 *
 * @author  Marian Petras
 */
final class RegexpMaker {

    /** regular expression representing a set of word characters */
    private static final String wordCharsExpr
                                = "[\\p{javaLetterOrDigit}_]";          //NOI18N
    /**
     * regular expression representing negative lookbehind
     * for a {@linkplain #wordCharsExpr word character}
     */
    private static final String checkNotAfterWordChar
                                = "(?<!" + wordCharsExpr + ")";         //NOI18N
    /**
     * regular expression representing negative lookahead
     * for a {@linkplain #wordCharsExpr word character}
     */
    private static final String checkNotBeforeWordChar
                                = "(?!" + wordCharsExpr + ")";          //NOI18N
    
    private RegexpMaker() {
    }

    /**
     * Translates the given simple pattern to a regular expression.
     * 
     * @param  simplePattern  pattern to be translated
     * @return  regular expression corresponding to the simple pattern
     */
    static String makeRegexp(String simplePattern) {

        /* This method is currently used only in tests. */

        return makeRegexp(simplePattern, false);
    }

    /**
     * Translates the given simple pattern to a regular expression.
     * 
     * @param  simplePattern  pattern to be translated
     * @param  wholeWords  whether the <i>Whole Words</i> option is selected
     * @return  regular expression corresponding to the simple pattern
     */
    static String makeRegexp(String simplePattern, boolean wholeWords) {
        if (simplePattern.length() == 0) {              //trivial case
            return simplePattern;
        }
        
        if (!wholeWords
                && Pattern.matches("[a-zA-Z0-9 ]*", simplePattern)) {   //NOI18N
            return simplePattern;                       //trivial case
        }
        
        StringBuilder buf = new StringBuilder(simplePattern.length() + 16);
        boolean quoted = false;
        boolean starPresent = false;
        int minCount = 0;

        boolean bufIsEmpty = true;
        char lastInputChar = '*';       //might be any other non-word character
        for (char c : simplePattern.toCharArray()) {
            if (quoted) {
                assert !starPresent && (minCount == 0);
                if (wholeWords && bufIsEmpty && isWordChar(c)) {
                    buf.append(checkNotAfterWordChar);
                }
                if (!isSimpleCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
                lastInputChar = c;
                bufIsEmpty = false;
                quoted = false;
            } else if (c == '?') {
                minCount++;
            } else if (c == '*') {
                starPresent = true;
            } else {
                if (starPresent || (minCount != 0)) {
                    if (wholeWords && bufIsEmpty && !starPresent) {
                        buf.append(checkNotAfterWordChar);
                    }
                    bufIsEmpty &= !addMetachars(buf, starPresent, minCount, wholeWords, !bufIsEmpty);
                    starPresent = false;
                    minCount = 0;
                }

                if (c == '\\') {
                    quoted = true;
                } else {
                    if (wholeWords && bufIsEmpty && isWordChar(c)) {
                        buf.append(checkNotAfterWordChar);
                    }
                    if (!isSimpleCharacter(c)) {
                        buf.append('\\');
                    }
                    buf.append(c);
                    lastInputChar = c;
                    bufIsEmpty = false;
                }
            }
        }
        if (quoted) {
            assert !starPresent && (minCount == 0);
            buf.append('\\').append('\\');
            lastInputChar = '\\';
            bufIsEmpty = false;
            quoted = false;
        } else if (starPresent || (minCount != 0)) {
            if (wholeWords && !starPresent && bufIsEmpty) {
                buf.append(checkNotAfterWordChar);
            }
            bufIsEmpty &= !addMetachars(buf, starPresent, minCount, wholeWords, false);
            if (wholeWords && !starPresent) {
                buf.append(checkNotBeforeWordChar);
            }
            lastInputChar = '*';    //might be any other non-word character
            starPresent = false;
            minCount = 0;
        }
        if (wholeWords && isWordChar(lastInputChar)) {
            buf.append(checkNotBeforeWordChar);
        }
        return buf.toString();
    }

    /**
     * Checks whether the given character is a word character.
     * @param  c  character to be checked
     * @return  {@code true} if the character is a word character,
     *          {@code false} otherwise
     * @see  #wordCharsExpr
     */
    private static boolean isWordChar(char c) {
        /* not necessary - just for performance */
        if ((c == '*') || (c == '\\')) {
            return false;
        }

        return Pattern.matches(wordCharsExpr, new String(new char[] {c}));
    }

    /**
     * Generates the part of a regular expression, that represents a sequence
     * of simple expression's metacharacters {@code '*'} and {@code '?'},
     * and adds it to the given string buffer.
     * 
     * @param  buf  string buffer to which the new part is to be added
     * @param  starPresent  whether the sequence contained at least one
     *                      {@code '*'} character
     * @param  minCount  number of {@code '?'} characters in the sequence
     * @param  wholeWords  whether the <i>Whole Words</i> option is selected
     * @param  middle  whether the metachars are to be placed in the middle
     *                 (i.e. not in the beginning or at the end) of the search
     *                 expression
     * @return  {@code true} if something was added to the string buffer,
     *          {@code false} if the buffer was not modified
     */
    private static boolean addMetachars(final StringBuilder buf,
                                     boolean starPresent,
                                     final int minCount,
                                     final boolean wholeWords,
                                     final boolean middle) {
        assert starPresent || (minCount != 0);

        /*
         * If 'Whole Words' is not activated, ignore stars in the beginning
         * and at the end of the expression:
         */
        if (starPresent && !wholeWords && !middle) {
            starPresent = false;
        }

        if ((minCount == 0) && !starPresent) {
            return false;
        }

        if (wholeWords) {
            buf.append(wordCharsExpr);
        } else {
            buf.append('.');
        }
        switch (minCount) {
        case 0:
            assert starPresent;
            buf.append('*');
            break;
        case 1:
            if (starPresent) {
                buf.append('+');
            }
            break;
        default:
            if (wholeWords) {
                buf.append('{').append(minCount);
                if (starPresent) {
                    buf.append(',');
                }
                buf.append('}');
            } else {
                for (int i = 1; i < minCount; i++) {
                    buf.append('.');
                }
                if (starPresent) {
                    buf.append('+');
                }
            }
        }
        if (starPresent && middle) {
            buf.append('?');    //use reluctant variant of the quantifier
        }
        return true;
    }
    
    /**
     * Translates the given simple pattern (or several patterns) to a single
     * regular expression.
     * 
     * @param  simplePatternList  pattern list to be translated
     * @return  regular expression corresponding to the simple pattern
     *          (or to the list of simple patterns)
     */
    static String makeMultiRegexp(String simplePatternList) {
        if (simplePatternList.length() == 0) {              //trivial case
            return simplePatternList;
        }
        
        if (Pattern.matches("[a-zA-Z0-9]*", simplePatternList)) {       //NOI18N
            return simplePatternList;                       //trivial case
        }
        
        StringBuilder buf = new StringBuilder(simplePatternList.length() + 16);
        boolean lastWasSeparator = false;
        boolean quoted = false;
        boolean starPresent = false;
        for (char c : simplePatternList.toCharArray()) {
            if (quoted) {
                if (!isSimpleCharacter(c)) {
                    buf.append('\\');
                }
                buf.append(c);
                quoted = false;
            } else if ((c == ',') || (c == ' ')) {
                if (starPresent) {
                    buf.append('.').append('*');
                    starPresent = false;
                }
                lastWasSeparator = true;
            } else {
                if (lastWasSeparator && (buf.length() != 0)) {
                    buf.append('|');
                }
                if (c == '?') {
                    buf.append('.');
                } else if (c == '*') {
                    starPresent = true;
                } else {
                    if (starPresent) {
                        buf.append('.').append('*');
                        starPresent = false;
                    }
                    if (c == '\\') {
                        quoted = true;
                    } else {
                        if (!isSimpleCharacter(c)) {
                            buf.append('\\');
                        }
                        buf.append(c);
                    }
                }
                lastWasSeparator = false;
            }
        }
        if (quoted) {
            buf.append('\\').append('\\');
            quoted = false;
        } else if (starPresent) {
            buf.append('.').append('*');
            starPresent = false;
        }
        return buf.toString();
    }
    
    private static boolean isSimpleCharacter(char c) {
        int cint = (int) c;
        return (cint == 0x20)                               //space
                || (cint > 0x7f)                            //non-ASCII
                || (cint >= 0x30) && (cint <= 0x39)          //'0' .. '9'
                || (cint & ~0x7f) == 0
                    && ((cint &= ~0x20) >= 0x41) && (cint <= 0x5a); //a..z,A..Z
    }

}