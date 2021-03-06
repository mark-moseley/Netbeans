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
package org.netbeans.cnd.api.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Vladirmir Voskresensky
 */
public class CndTokenUtilities {

    private CndTokenUtilities() {
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static boolean isInPreprocessorDirective(Document doc, int offset) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, false, true);
        if (cppTokenSequence != null) {
            return cppTokenSequence.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE;
        }
        return false;
    }

    /**
     * method should be called under document read lock and token processor must be
     * very fast to prevent document blocking. If startOffset is less than lastOffset,
     * then process tokens in backward direction, otherwise in forward
     * @param tp
     * @param doc
     * @param startOffset
     * @param lastOffset
     */
    public static void processTokens(CndTokenProcessor<Token<CppTokenId>> tp, Document doc, int startOffset, int lastOffset) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, startOffset, false, lastOffset < startOffset);
        if (cppTokenSequence == null) {
            // check if it is C/C++ document at all
            TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            if (ts != null) {
                if (ts.language() == CppTokenId.languageC() ||
                        ts.language() == CppTokenId.languageCpp() ||
                        ts.language() == CppTokenId.languagePreproc()) {
                    tp.start(startOffset, startOffset);
                    // just emulate finish
                    tp.end(lastOffset, lastOffset);
                    return;
                }
            }            
            return;
        }
        int shift = cppTokenSequence.move(startOffset);
        tp.start(startOffset, startOffset - shift);
        if (processTokensImpl(tp, cppTokenSequence, startOffset, lastOffset, shift != 0)) {
            tp.end(lastOffset, cppTokenSequence.offset());
        } else {
            tp.end(lastOffset, lastOffset);
        }
    }

    public static <T extends CppTokenId> TokenItem<T> createTokenItem(TokenSequence<T> ts) {
        return TokenItemImpl.create(ts);
    }

    public static <T extends CppTokenId> TokenItem<T> createTokenItem(Token<T> token, int tokenOffset) {
        return TokenItemImpl.create(token, tokenOffset);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static TokenItem<CppTokenId> getFirstNonWhiteBwd(Document doc, int offset) {
        SkipTokenProcessor tp = new SkipTokenProcessor(Collections.<CppTokenId>emptySet(), skipWSCategories, true);
        processTokens(tp, doc, offset, 0);
        return tp.getTokenItem();
    }

    /**
     * method should be called under read lock
     * move token sequence to the position of preprocessor keyword
     * @param ts token sequence
     * @return true if successfuly moved, false if no preprocessor keyword in given sequence
     * of sequence is null, or not preprocessor directive sequence
     */
    public static boolean moveToPreprocKeyword(TokenSequence<CppTokenId> ts) {
        if (ts != null && ts.language() == CppTokenId.languagePreproc()) {
            ts.moveStart();
            if (!ts.moveNext()) {// skip start #
                return false;
            }
            if (shiftToNonWhite(ts, true)) {
                switch (ts.token().id()) {
                    case PREPROCESSOR_DEFINE:
                    case PREPROCESSOR_ELIF:
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ENDIF:
                    case PREPROCESSOR_ERROR:
                    case PREPROCESSOR_IDENT:
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                    case PREPROCESSOR_LINE:
                    case PREPROCESSOR_PRAGMA:
                    case PREPROCESSOR_UNDEF:
                    case PREPROCESSOR_WARNING:
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * method should be called under read lock
     * move sequence to the first not whitespace category token if needed
     * @param ts token sequence to move
     * @param backward if <code>true</code> move backward, otherwise move forward
     * @return true if successfuly moved, false otherwise
     */
    public static boolean shiftToNonWhite(TokenSequence<CppTokenId> ts, boolean backward) {
        do {
            switch (ts.token().id()) {
                case WHITESPACE:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case LINE_COMMENT:
                case ESCAPED_LINE:
                case ESCAPED_WHITESPACE:
                    break;
                default:
                    return true;
            }
        } while (backward ? ts.movePrevious() : ts.moveNext());
        return false;
    }
    
    /**
     * method should be called under document read lock
     * returns offsetable token on interested offset
     * @param cppTokenSequence token sequence
     * @param offset interested offset
     * @return returns ofssetable token, but if offset is in the beginning of whitespace
     * or comment token, then it returns previous token
     */
    public static TokenItem<CppTokenId> getTokenCheckPrev(Document doc, int offset) {
        return getTokenImpl(doc, offset, true, true);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @param tokenizePP
     * @return
     */
    public static TokenItem<CppTokenId> getToken(Document doc, int offset, boolean tokenizePP) {
        return getTokenImpl(doc, offset, tokenizePP, false);
    }

    private static TokenItem<CppTokenId> getTokenImpl(Document doc, int offset, boolean tokenizePP, boolean checkPrevious) {
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, tokenizePP, false);
        if (cppTokenSequence == null) {
            return null;
        }
        TokenItem<CppTokenId> offsetToken = getTokenImpl(cppTokenSequence, offset, checkPrevious);
        return offsetToken;
    }

    private static TokenItem<CppTokenId> getTokenImpl(TokenSequence<CppTokenId> cppTokenSequence, int offset, boolean checkPrevious) {
        if (cppTokenSequence == null) {
            return null;
        }
        int shift = cppTokenSequence.move(offset);
        TokenItem<CppTokenId> offsetToken = null;
        boolean checkPrev = false;
        if (cppTokenSequence.moveNext()) {
            offsetToken = TokenItemImpl.create(cppTokenSequence);
            if (checkPrevious && (shift == 0)) {
                String category = offsetToken.id().primaryCategory();
                if (CppTokenId.WHITESPACE_CATEGORY.equals(category) ||
                        CppTokenId.COMMENT_CATEGORY.equals(category) ||
                        CppTokenId.SEPARATOR_CATEGORY.equals(category) ||
                        CppTokenId.OPERATOR_CATEGORY.equals(category)) {
                    checkPrev = true;
                }
            }
        }
        if (checkPrev && cppTokenSequence.movePrevious()) {
            offsetToken = TokenItemImpl.create(cppTokenSequence);
        }
        return offsetToken;
    }

    private static boolean processTokensImpl(CndTokenProcessor<Token<CppTokenId>> tp, TokenSequence<CppTokenId> cppTokenSequence, int startOffset, int lastOffset, boolean adjust) {
        boolean processedToken = false;
        boolean bwd = (lastOffset < startOffset);
        boolean adjustOnFirstIteration = adjust;
        // in forward direction move to next, otherwise move to previous token
        while (!tp.isStopped()) {
            // for backward mode, we need token as well
            boolean moved;
            if (adjustOnFirstIteration || !bwd) {
                moved = cppTokenSequence.moveNext();
            } else {
                moved = cppTokenSequence.movePrevious();
            }
            if (!moved) {
                break;
            }
            adjustOnFirstIteration = false;
            Token<CppTokenId> token = cppTokenSequence.token();
            // check finish condition
            if (bwd) {
                if (cppTokenSequence.offset() + token.length() < lastOffset) {
                    break;
                }
            } else {
                if (cppTokenSequence.offset() >= lastOffset) {
                    break;
                }
            }
            if (tp.token(token, cppTokenSequence.offset())) {
                // process embedding
                @SuppressWarnings("unchecked")
                TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) cppTokenSequence.embedded();
                if (embedded != null) {
                    int shift = 0;
                    if (cppTokenSequence.offset() < startOffset) {
                        shift = embedded.move(startOffset);
                    }
                    processedToken |= processTokensImpl(tp, embedded, startOffset, lastOffset, shift != 0);
                }
            } else {
                processedToken = true;
            }
        }
        return processedToken;
    }

    private static class SkipTokenProcessor extends CndAbstractTokenProcessor<Token<CppTokenId>> {

        private boolean stopped = false;
        private TokenItem<CppTokenId> tokenItem = null;
        private Token<CppTokenId> lastToken = null;
        private final Set<CppTokenId> skipTokenIds;
        private final Set<String> skipTokenCategories;
        private final boolean processPP;

        public SkipTokenProcessor(Set<CppTokenId> skipTokenIds, Set<String> skipTokenCategories, boolean processPP) {
            this.skipTokenIds = skipTokenIds;
            this.skipTokenCategories = skipTokenCategories;
            this.processPP = processPP;
        }

        @Override
        public boolean token(Token<CppTokenId> token, int tokenOffset) {
            lastToken = token;
            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return processPP;
            }
            if (!skipTokenIds.contains(token.id()) && !skipTokenCategories.contains(token.id().primaryCategory())) {
                stopped = true;
            }
            return false;
        }

        @Override
        public boolean isStopped() {
            return stopped;
        }

        public TokenItem<CppTokenId> getTokenItem() {
            return tokenItem;
        }

        @Override
        public void end(int offset, int lastTokenOffset) {
            super.end(offset, lastTokenOffset);
            if (lastToken != null) {
                tokenItem = TokenItemImpl.create(lastToken, lastTokenOffset);
            }
        }
    }

    private static final class TokenItemImpl<T extends CppTokenId> extends TokenItem.AbstractItem<T> {

        public TokenItemImpl(T tokenID, PartType pt, int index, int offset, CharSequence text) {
            super(tokenID, pt, index, offset, text);
        }

        private static <T extends CppTokenId> TokenItem<T> create(TokenSequence<T> ts) {
            Token<T> token = ts.token();
            return new TokenItemImpl<T>(token.id(), token.partType(), ts.index(), ts.offset(), token.text());
        }

        private static <T extends CppTokenId> TokenItem<T> create(Token<T> token, int offset) {
            return new TokenItemImpl<T>(token.id(), token.partType(), -1, offset, token.text());
        }
    }
    private static final Set<String> skipWSCategories = new HashSet<String>(1);


    static {
        skipWSCategories.add(CppTokenId.WHITESPACE_CATEGORY);
    }
}
