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

package org.netbeans.lib.lexer;

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Various utility methods and constants in lexer module.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LexerUtilsConstants {
    
    /**
     * Maximum allowed number of consecutive flyweight tokens.
     * <br>
     * High number of consecutive flyweight tokens
     * would degrade performance of offset
     * finding.
     */
    public static final int MAX_FLY_SEQUENCE_LENGTH = 5;
    
    /**
     * Token list's modCount for the case when the source input is unmodifiable.
     */
    public static final int MOD_COUNT_IMMUTABLE_INPUT = -1;
    
    /**
     * ModCount when the particular token list was removed from the token hierarchy.
     */
    public static final int MOD_COUNT_REMOVED = -2;
    
    /**
     * Maximum token length that has the TokenLength objects cached by TokenLength.CACHE.
     */
    public static final int MAX_CACHED_TOKEN_LENGTH = 200;
    
    /**
     * Threshold (used by TokenLength) above which the DefaultToken implementations will
     * start to cache the Token.text().toString() result in itself.
     */
    public static final short CACHE_TOKEN_TO_STRING_THRESHOLD = 900;
    
    /**
     * Threshold similar to TOKEN_TEXT_STRING_THRESHOLD but for a case when a root token list's text
     * is a String instance. In that case a String.substring(start, end) will be used
     * which is considerably cheaper than a regular case because the character data
     * will be shared with the root text and there will be no character copying.
     */
    public static final short INPUT_TEXT_STRING_THRESHOLD = 300;
    
    /**
     * Used by TokenLength as a measure of a String instance production.
     */
    public static final short TOKEN_LENGTH_STRING_CREATION_FACTOR = 50;
    
    /**
     * Initial size of a buffer for copying a text of a Reader.
     */
    public static final int READER_TEXT_BUFFER_SIZE = 4096;
    
    static {
        // Require the following to only use THRESHOLD in certain checks
        assert (CACHE_TOKEN_TO_STRING_THRESHOLD >= INPUT_TEXT_STRING_THRESHOLD);
    }

    public static final AbstractToken<?> SKIP_TOKEN
        = new TextToken<TokenId>(
            new TokenIdImpl("skip-token-id; special id of TokenFactory.SKIP_TOKEN; " + // NOI18N
                    " It should never be part of token sequence", 0, null), // NOI18N
            "" // empty skip token text NOI18N
        );
    
    /**
     * Initial embedded token list's modCount prior it was synced
     * with the root token list's modCount.
     */
    public static final int MOD_COUNT_EMBEDDED_INITIAL = -3;

    public static void tokenLengthZeroOrNegative(int tokenLength) {
        if (tokenLength == 0) {
            throw new IllegalArgumentException(
                "Tokens with zero length are not supported by the framework." // NOI18N
              + " Fix the lexer." // NOI18N
            );
        } else { // tokenLength < 0
            throw new IllegalArgumentException(
                "Negative token length " + tokenLength // NOI18N
            );
        }
    }

    public static void throwFlyTokenProhibited() {
        throw new IllegalStateException("Flyweight token created but prohibited." // NOI18N
                + " Lexer needs to check lexerInput.isFlyTokenAllowed()."); // NOI18N
    }

    public static void throwBranchTokenFlyProhibited(AbstractToken token) {
        throw new IllegalStateException("Language embedding cannot be created" // NOI18N
                + " for flyweight token=" + token // NOI18N
                + "\nFix the lexer to not create flyweight token instance when"
                + " language embedding exists for the token."
        );
    }
    
    public static void checkValidBackup(int count, int maxCount) {
        if (count > maxCount) {
            throw new IndexOutOfBoundsException("Cannot backup " // NOI18N
                    + count + " characters. Maximum: " // NOI18N
                    + maxCount + '.');
        }
    }
    
    /**
     * Returns the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language to the generic type requested by the caller.
     */
    public static <T extends TokenId> Language<T> innerLanguage(LanguagePath languagePath) {
        @SuppressWarnings("unchecked")
        Language<T> l = (Language<T>)languagePath.innerLanguage();
        return l;
    }
    
    /**
     * Returns language hierarchy of the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language hierarchy to the generic type requested by the caller.
     */
    public static <T extends TokenId> LanguageHierarchy<T> innerLanguageHierarchy(LanguagePath languagePath) {
        Language<T> language = innerLanguage(languagePath);
        return LexerApiPackageAccessor.get().languageHierarchy(language);
    }
    
    /**
     * Returns language operation of the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language operation to the generic type requested by the caller.
     */
    public static <T extends TokenId> LanguageOperation<T> innerLanguageOperation(LanguagePath languagePath) {
        Language<T> language = innerLanguage(languagePath);
        return LexerApiPackageAccessor.get().languageOperation(language);
    }
    
    /**
     * Find the language embedding for the given parameters.
     * <br/>
     * First the <code>LanguageHierarchy.embedding()</code> method is queried
     * and if no embedding is found then the <code>LanguageProvider.findLanguageEmbedding()</code>.
     */
    public static <T extends TokenId> LanguageEmbedding<?>
    findEmbedding(LanguageHierarchy<T> languageHierarchy, AbstractToken<T> token,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        LanguageEmbedding<?> embedding =
                LexerSpiPackageAccessor.get().embedding(
                languageHierarchy, token, languagePath, inputAttributes);

        if (embedding == null) {
            // try language embeddings registered in Lookup
            embedding = LanguageManager.getInstance().findLanguageEmbedding(
                    token, languagePath, inputAttributes);
        }
        
        return embedding;
    }
    
    public static int maxLanguagePathSize(Set<LanguagePath> paths) {
        int maxPathSize = 0;
        for (LanguagePath lp : paths) {
            maxPathSize = Math.max(lp.size(), maxPathSize);
        }
        return maxPathSize;
    }
    
    /**
     * Get index of the token that "contains" the given offset.
     * If the offset is beyond the existing tokens the method asks
     * for next tokens by <code>tokenList.tokenOrEmbedding()</code>.
     * 
     * @param offset offset for which the token index should be found.
     * @return array of two items where the [0] is token's index and [1] is its offset.
     *  <br/>
     *  If offset &gt;= last-token-end-offset then [0] contains token-count and
     *  [1] conains last-token-end-offset.
     *  <br/>
     *  [0] may contain -1 to indicate that there are no tokens in the token list
     *  ([1] then contains zero).
     */
    public static int[] tokenIndexLazyTokenCreation(TokenList<?> tokenList, int offset) {
        // Token count in the list may change as possibly other threads
        // keep asking for tokens. Root token list impls create tokens lazily
        // when asked by clients.
        // The intent is to not force creation of all token (because of using a binary search)
        // so first a last token is checked whether it covers the requested offset.
        int tokenCount = tokenList.tokenCountCurrent(); // presently created token count
        if (tokenCount == 0) { // no tokens yet -> attempt to create at least one
            if (tokenList.tokenOrEmbedding(0) == null) { // really no tokens at all
                return new int[] { -1, 0 };
            }
            // Re-get the present token count (could be created a chunk of tokens at once)
            tokenCount = tokenList.tokenCountCurrent();
        }

        // tokenCount surely >0
        int prevTokenOffset = tokenList.tokenOffset(tokenCount - 1);
        if (offset > prevTokenOffset) { // may need to create further tokens if they do not exist
            // Force token list to create subsequent tokens
            // Cannot subtract offset by each token's length because
            // there may be gaps between tokens due to token id filter use.
            int tokenLength = tokenList.tokenOrEmbedding(tokenCount - 1).token().length();
            while (offset >= prevTokenOffset + tokenLength) { // above present token
                TokenOrEmbedding<?> tokenOrEmbedding = tokenList.tokenOrEmbedding(tokenCount);
                if (tokenOrEmbedding != null) {
                    AbstractToken<?> t = tokenOrEmbedding.token();
                    if (t.isFlyweight()) { // need to use previous tokenLength
                        prevTokenOffset += tokenLength;
                    } else { // non-flyweight token - retrieve offset
                        prevTokenOffset = tokenList.tokenOffset(tokenCount);
                    }
                    tokenLength = t.length();
                    tokenCount++;

                } else { // no more tokens => position behind last token
                    return new int[] { tokenCount, prevTokenOffset + tokenLength };
                }
            }
            return new int[] { tokenCount - 1, prevTokenOffset };
        }
        // Now do a regular binary search
        return tokenIndexBinSearch(tokenList, offset, tokenCount);
    }
    
    /**
     * Get index of the token that "contains" the given offset by using binary search
     * in existing tokens.
     * 
     * @param offset offset for which the token index should be found.
     * @return array of two items where the [0] is token's index and [1] is its offset.
     *  <br/>
     *  If offset &gt;= last-token-end-offset then [0] contains token-count and
     *  [1] conains last-token-end-offset.
     *  <br/>
     *  [0] may contain -1 to indicate that there are no tokens in the token list
     *  ([1] then contains zero).
     */
    public static int[] tokenIndexBinSearch(TokenList<?> tokenList, int offset, int tokenCount) {
        // The offset is within the currently recognized tokens
        // Use binary search
        int low = 0;
        int high = tokenCount - 1;
        int mid = -1;
        int midStartOffset = -1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            midStartOffset = tokenList.tokenOffset(mid);
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Token starting exactly at offset found
                return new int[] { mid, midStartOffset}; // right at the token begining
            }
        }
        
        // Not found exactly and high + 1 == low => high < low
        // BTW there may be gaps between tokens; if offset is in gap then position to lower token
        if (high >= 0) { // could be -1
            if (low == tokenCount) { // Could be beyond end of last token
                AbstractToken<?> t = tokenList.tokenOrEmbedding(high).token();
                // Use current midStartOffset
                if (offset >= midStartOffset + t.length()) { // beyond end of last token
                    // Offset in the gap above the "high" token
                    high++;
                    midStartOffset += t.length();
                } else if (mid != high) {
                    midStartOffset = tokenList.tokenOffset(high);
                }
            } else if (mid != high) {
                midStartOffset = tokenList.tokenOffset(high);
            }
        } else { // high == -1 => mid == 0
            if (tokenCount == 0) { // Need to return -1
                return new int[] { -1, 0 };
            }
            high = 0;
            // Use current midStartOffset
        }
        return new int[] { high, midStartOffset };
    }

    public static int updatedStartOffset(EmbeddedTokenList<?> etl, TokenHierarchyEventInfo eventInfo) {
        etl.embeddingContainer().updateStatusUnsync();
        int startOffset = etl.startOffset();
        return (etl.isRemoved() && startOffset > eventInfo.modOffset())
                ? Math.max(startOffset - eventInfo.removedLength(), eventInfo.modOffset())
                : startOffset;
    }

    public static <T extends TokenId> StringBuilder appendTokenList(StringBuilder sb, TokenList<T> tokenList) {
        return appendTokenList(sb, tokenList, -1, 0, Integer.MAX_VALUE, true, 0, true);
    }

    public static <T extends TokenId> StringBuilder appendTokenListIndented(
        StringBuilder sb, TokenList<T> tokenList, int indent
    ) {
        return appendTokenList(sb, tokenList, -1, 0, Integer.MAX_VALUE, false, indent, true);
    }

    public static <T extends TokenId> StringBuilder appendTokenList(StringBuilder sb,
            TokenList<T> tokenList, int currentIndex, int startIndex, int endIndex,
            boolean appendEmbedded, int indent, boolean dumpTokenText
    ) {
        if (sb == null) {
            sb = new StringBuilder(200);
        }
        TokenHierarchy<?> tokenHierarchy;
        if (tokenList instanceof SnapshotTokenList) {
            tokenHierarchy = ((SnapshotTokenList<T>)tokenList).snapshot().tokenHierarchy();
        } else {
            tokenHierarchy = null;
        }

        endIndex = Math.min(tokenList.tokenCountCurrent(), endIndex);
        int digitCount = ArrayUtilities.digitCount(endIndex - 1);
        for (int i = Math.max(startIndex, 0); i < endIndex; i++) {
            ArrayUtilities.appendSpaces(sb, indent);
            sb.append((i == currentIndex) ? '*' : 'T');
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            appendTokenInfo(sb, tokenList, i, tokenHierarchy,
                    appendEmbedded, indent, dumpTokenText);
            sb.append('\n');
        }
        return sb;
    }
    
    public static boolean statesEqual(Object state1, Object state2) {
        return (state1 == null && state2 == null)
            || (state1 != null && state1.equals(state2));
    }
    
    public static String idToString(TokenId id) {
        return id.name() + '[' + id.ordinal() + ']'; // NOI18N;
    }
    
    public static <T extends TokenId> void appendTokenInfo(StringBuilder sb,
            TokenList<T> tokenList, int index,
            TokenHierarchy tokenHierarchy, boolean appendEmbedded, int indent,
            boolean dumpTokenText
    ) {
        try {
            appendTokenInfo(sb, tokenList.tokenOrEmbedding(index),
                    tokenList.lookahead(index), tokenList.state(index),
                    tokenHierarchy, appendEmbedded, indent, dumpTokenText);
        } catch (IndexOutOfBoundsException e) {
            // Special handling due to fact that the JTL.lookahead() failed here and additional info was needed
           tokenList.lookahead(index); // Reattempt to possibly debug the exception throwing
            System.err.println("Index=" + index + ", tokenCount=" + tokenList.tokenCount() + ", cls=" + tokenList.getClass());
            throw e; // Rethrow the IOOBE
        }
    }

    public static <T extends TokenId> void appendTokenInfo(StringBuilder sb,
            TokenOrEmbedding<T> tokenOrEmbedding, int lookahead, Object state,
            TokenHierarchy<?> tokenHierarchy, boolean appendEmbedded, int indent,
            boolean dumpTokenText
    ) {
        if (tokenOrEmbedding == null) {
            sb.append("<NULL-TOKEN>");
        } else { // regular token
            EmbeddingContainer<T> ec = tokenOrEmbedding.embedding();
            AbstractToken<T> token = tokenOrEmbedding.token();
            token.dumpInfo(sb, tokenHierarchy, dumpTokenText, true, indent);
            appendLAState(sb, lookahead, state);
            sb.append(", ");
            appendIdentityHashCode(sb, token);

            // Check for embedding and if there is one dump it
            if (ec != null) {
                indent += 4;
                // Append EC's IHC
                sb.append("; EC-");
                appendIdentityHashCode(sb, ec);
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                int index = 0;
                while (etl != null) {
                    sb.append('\n');
                    ArrayUtilities.appendSpaces(sb, indent);
                    sb.append("Embedding[").append(index).append("]: \"").append(etl.languagePath().mimePath()).append("\"\n");
                    if (appendEmbedded) {
                        appendTokenList(sb, etl, -1, 0, Integer.MAX_VALUE, appendEmbedded, indent, true);
                    }
                    etl = etl.nextEmbeddedTokenList();
                    index++;
                }
            } 
        }
    }
    
    public static void appendIdentityHashCode(StringBuilder sb, Object o) {
        sb.append("IHC=");
        sb.append(System.identityHashCode(o));
    }
    
    public static void appendLAState(StringBuilder sb, TokenList<?> tokenList, int index) {
        appendLAState(sb, tokenList.lookahead(index), tokenList.state(index));
    }

    public static void appendLAState(StringBuilder sb, int lookahead, Object state) {
        if (lookahead > 0) {
            sb.append(", la=");
            sb.append(lookahead);
        }
        if (state != null) {
            sb.append(", st=");
            sb.append(state);
        }
    }
static int cnt;
    public static String checkConsistencyTokenList(TokenList<?> tokenList, boolean checkEmbedded) {
        int tokenCountCurrent = tokenList.tokenCountCurrent();
        boolean continuous = tokenList.isContinuous();
        // To obtain up-to-date startOffset() a EC.updateStatus() needs to be called.
        // Of course this may affect a testing in case a missing EC.updateStatus()
        //   is a reason of failure.
        if (tokenList instanceof EmbeddedTokenList) {
            ((EmbeddedTokenList<?>)tokenList).embeddingContainer().updateStatus();
        }
        int startOffset = tokenList.startOffset();
        int lastOffset = startOffset;
        for (int i = 0; i < tokenCountCurrent; i++) {
            if (tokenList.getClass() == JoinTokenList.class && i == 5) {
                cnt++;
            }
            TokenOrEmbedding<?> tokenOrEmbedding = tokenList.tokenOrEmbedding(i);
            if (tokenOrEmbedding == null) {
                tokenOrEmbedding = tokenList.tokenOrEmbedding(i); // Repeat operation (place bkpt here for debugging)
                return dumpContext("Null token", tokenList, i); // NOI18N
            }
            AbstractToken<?> token = tokenOrEmbedding.token();
            if (token.isRemoved()) {
                return dumpContext("Token is removed", tokenList, i); // NOI18N
            }
            // Check whether tokenList.startOffset() corresponds to the start of first token
            if (i == 0 && continuous && tokenCountCurrent > 0 && !token.isFlyweight()) {
                if (token.offset(null) != tokenList.startOffset()) {
                    return dumpContext("firstToken.offset()=" + token.offset(null) + // NOI18N
                            " != tokenList.startOffset()=" + tokenList.startOffset(), // NOI18N
                            tokenList, i);
                }
            }
            if (!token.isFlyweight() && token.tokenList() != tokenList && !(tokenList instanceof JoinTokenList)) {
                return dumpContext("Invalid token.tokenList()=" + token.tokenList(), // NOI18N
                        tokenList, i);
            }
            if (token.text() == null) {
                return dumpContext("Null token.text()", tokenList, i); // NOI18N
            }
            if (token.text().toString() == null) {
                return dumpContext("Null token.text().toString()", tokenList, i); // NOI18N
            }
            int offset = (token.isFlyweight()) ? lastOffset : token.offset(null);
            if (offset < 0) {
                return dumpContext("Token offset=" + offset + " < 0", tokenList, i); // NOI18N // NOI18N
            }
            if (offset < lastOffset) {
                return dumpContext("Token offset=" + offset + " < lastOffset=" + lastOffset, // NOI18N
                        tokenList, i);
            }
            if (offset > lastOffset && continuous) {
                return dumpContext("Gap between tokens; offset=" + offset + ", lastOffset=" + lastOffset, // NOI18N
                        tokenList, i);
            }
            lastOffset = offset + token.length();
            EmbeddingContainer<?> ec = tokenOrEmbedding.embedding();
            if (ec != null && checkEmbedded) {
                EmbeddedTokenList<?> etl = ec.firstEmbeddedTokenList();
                while (etl != null) {
                    String error = checkConsistencyTokenList(etl, checkEmbedded);
                    if (error != null)
                        return error;
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
        // Check that last offset ended at TL.endOffset() for continuous TLs
        if (tokenList instanceof MutableTokenList && ((MutableTokenList<?>)tokenList).isFullyLexed()) {
            int endOffset = tokenList.endOffset();
            if (startOffset != endOffset && tokenCountCurrent == 0) {
                return dumpContext("Non-empty token list does not contain any tokens", tokenList, 0); // NOI18N
            }
            if (continuous && lastOffset != endOffset) {
                return dumpContext("lastOffset=" + lastOffset + " != endOffset=" + endOffset, // NOI18N
                        tokenList, tokenCountCurrent);
            }
        }
        return null;
    }
    
    private static String dumpContext(String msg, TokenList<?> tokenList, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append(" at index="); // NOI18N
        sb.append(index);
        sb.append(" of tokens of language-path "); // NOI18N
        sb.append(tokenList.languagePath().mimePath());
        sb.append(", ").append(tokenList.getClass());
        sb.append('\n');
        LexerUtilsConstants.appendTokenList(sb, tokenList, index, index - 2, index + 3, false, 0, true);
        return sb.toString();
    }
    
    private LexerUtilsConstants() {
        // no instances
    }

}
