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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.LexerUtilsConstants;

/**
 * Default token which by default obtains text from its background storage.
 * <br/>
 * It is non-flyweight and it does not contain custom text.
 *
 * <p>
 * Once the token gets removed from a token list
 * (because of a text modification) the token
 * returns <code>null</code> from {@link #text()} because the text
 * that it would represent could no longer exist in the document.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DefaultToken<T extends TokenId> extends AbstractToken<T> implements CharSequence {
    
    /**
     * Used in Token.text() to decide whether "this" should be returned and
     * a text.charAt() will be slower or, for larger tokens,
     * a subsequence of input source text should be created and returned instead..
     */
    private static final int INPUT_SOURCE_SUBSEQUENCE_THRESHOLD = 30;
    
    /**
     * Field that is of type CharSequence and is either length of the token
     * or cached text of the token as String.
     */
    CharSequence tokenLengthOrCachedText; // 24 bytes (20-super + 4)
    
    /**
     * Construct new default token.
     */
    public DefaultToken(T id, int length) {
        super(id);
        assert (length > 0) : "Token length=" + length + " <= 0"; // NOI18N
        this.tokenLengthOrCachedText = TokenLength.get(length);
    }
    
    /**
     * Construct a special zero-length token.
     */
    public DefaultToken(T id) {
        super(id);
        this.tokenLengthOrCachedText = TokenLength.get(0);
    }

    @Override
    public int length() {
        return tokenLengthOrCachedText.length();
    }

    @Override
    protected String dumpInfoTokenType() {
        return "DefT"; // NOI18N "TextToken" or "FlyToken"
    }
    
    /**
     * Get text represented by this token.
     */
    @Override
    public synchronized CharSequence text() { // synchronized due to tokenLengthOrCachedText mutability
        CharSequence text;
        if (tokenLengthOrCachedText.getClass() == TokenLength.class) {
            if (!isRemoved()) { // Updates status for EmbeddedTokenList; tokenList != null
                int len = tokenLengthOrCachedText.length();
                if (len >= INPUT_SOURCE_SUBSEQUENCE_THRESHOLD) {
                    // Create subsequence of input source text
                    CharSequence inputSourceText = tokenList.inputSourceText();
                    int tokenOffset = tokenList.tokenOffset(this);
                    text = new InputSourceSubsequence(this, inputSourceText,
                            tokenOffset, tokenOffset + len, tokenOffset, tokenOffset + len);
                } else { // Small token
                    text = this;
                }
            } else { // Token is removed
                text = null;
            }
        } else { // tokenLength contains cached text
            text = tokenLengthOrCachedText;
        }
        return text;
    }

    /**
     * Implementation of <code>CharSequence.charAt()</code>
     * for case when this token is used as token's text char sequence.
     */
    public final char charAt(int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException(
                "index=" + index + ", length=" + length() // NOI18N
            );
        }
        if (tokenList == null) { // Should normally not happen
            // A bit strange to throw IOOBE but it's more practical since
            // TokenHierarchy's dump can overcome IOOBE and deliver a useful debug but not NPEs etc.
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + length() +
                    " but tokenList==null for token " + dumpInfo(null, null, false, true, 0));
        }
        int tokenOffset = tokenList.tokenOffset(this);
        return tokenList.inputSourceText().charAt(tokenOffset + index);
    }

    /**
     * Implementation of <code>CharSequence.subSequence()</code>
     * for case when this token is used as token's text char sequence.
     */
    public final synchronized CharSequence subSequence(int start, int end) { // synchronized due to tokenLengthOrCachedText mutability
        // Create subsequence of token's text
        CharSequence text;
        int textLength = tokenLengthOrCachedText.length();
        CharSequenceUtilities.checkIndexesValid(start, end, textLength);

        if (tokenLengthOrCachedText.getClass() == TokenLength.class) {
            // If calling this.subSequence() then this.text() was already called
            // so the status should be updated already and also the token is not removed.
            // For simplicity always make a subsequence of the input source text.
            CharSequence inputSourceText = tokenList.inputSourceText();
            int tokenOffset = tokenList.tokenOffset(this);
            text = new InputSourceSubsequence(this, inputSourceText,
                    tokenOffset + start, tokenOffset + end, tokenOffset, tokenOffset + textLength);

        } else { // tokenLength contains cached text
            text = tokenLengthOrCachedText.subSequence(start, end);
        }
        return text;
    }
    
    /**
     * Implementation of <code>CharSequence.toString()</code>
     * for case when this token is used as token's text char sequence.
     */
    @Override
    public synchronized String toString() { // synchronized due to tokenLengthOrCachedText mutability
        // In reality this method can either be called as result of calling Token.text().toString()
        // or just calling Token.toString() for debugging purposes
        String textStr;
        if (tokenLengthOrCachedText.getClass() == TokenLength.class) {
            if (!isRemoved()) { // Updates status for EmbeddedTokenList; tokenList != null
                TokenLength tokenLength = (TokenLength) tokenLengthOrCachedText;
                CharSequence inputSourceText = tokenList.inputSourceText();
                int nextCacheFactor = tokenLength.nextCacheFactor();
                int threshold = (inputSourceText.getClass() == String.class)
                            ? LexerUtilsConstants.INPUT_TEXT_STRING_THRESHOLD
                            : LexerUtilsConstants.CACHE_TOKEN_TO_STRING_THRESHOLD;
                int tokenOffset = tokenList.tokenOffset(this);
                textStr = inputSourceText.subSequence(tokenOffset,
                        tokenOffset + tokenLength.length()).toString();
                if (nextCacheFactor < threshold) {
                    tokenLengthOrCachedText = tokenLength.next(nextCacheFactor);
                } else { // Should become cached
                    tokenLengthOrCachedText = textStr;
                }
            } else { // Token already removed
                textStr = "<null>";
            }

        } else { // tokenLength contains cached text
            textStr = tokenLengthOrCachedText.toString();
        }
        return textStr;
    }

    private static final class InputSourceSubsequence implements CharSequence {
        
        private final DefaultToken token; // (8-super + 4) = 12 bytes
        
        private final CharSequence inputSourceText; // 16 bytes
        
        private final int start; // 20 bytes
        
        private final int end; // 24 bytes
        
        private final int tokenStart; // 28 bytes
        
        private final int tokenEnd; // 32 bytes
        
        public InputSourceSubsequence(DefaultToken token, CharSequence text,
                int start, int end, int tokenStart, int tokenEnd
        ) {
            this.token = token;
            this.inputSourceText = text;
            this.start = start;
            this.end = end;
            this.tokenStart = tokenStart;
            this.tokenEnd = tokenEnd;
        }
        
        public int length() {
            return end - start;
        }
        
        public char charAt(int index) {
            CharSequenceUtilities.checkIndexValid(index, length());
            return inputSourceText.charAt(start + index);
        }

        public CharSequence subSequence(int start, int end) {
            CharSequenceUtilities.checkIndexesValid(this, start, end);
            return new InputSourceSubsequence(token, inputSourceText,
                    this.start + start, this.start + end, tokenStart, tokenEnd);
        }

        @Override
        public String toString() {
            String textStr;
            // Increase usage
            synchronized (token) { // synchronized due to tokenLengthOrCachedText mutability
                if (token.tokenLengthOrCachedText.getClass() == TokenLength.class) {
                    TokenLength tokenLength = (TokenLength) token.tokenLengthOrCachedText;
                    int nextCacheFactor = tokenLength.nextCacheFactor();
                    int threshold = (inputSourceText.getClass() == String.class)
                                ? LexerUtilsConstants.INPUT_TEXT_STRING_THRESHOLD
                                : LexerUtilsConstants.CACHE_TOKEN_TO_STRING_THRESHOLD;
                    if (nextCacheFactor < threshold) {
                        textStr = inputSourceText.subSequence(start, end).toString();
                        token.tokenLengthOrCachedText = tokenLength.next(nextCacheFactor);
                    } else { // Should become cached
                        // Create cached text
                        String tokenTextString = inputSourceText.subSequence(tokenStart, tokenEnd).toString();
                        token.tokenLengthOrCachedText = tokenTextString;
                        // Substring returns this for start == 0 && end == length()
                        textStr = tokenTextString.substring(start - tokenStart, end - tokenStart);
                    }

                } else { // Already cached text
                    textStr = token.tokenLengthOrCachedText.subSequence(start - tokenStart, end - tokenStart).toString();
                }
                return textStr;
            }
        }

    }

}
