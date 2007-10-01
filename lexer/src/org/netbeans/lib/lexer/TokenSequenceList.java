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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * List of token lists that collects all token lists for a given language path.
 *
 * @author Miloslav Metelka
 */

public final class TokenSequenceList extends AbstractList<TokenSequence<? extends TokenId>> {
    
    private TokenHierarchyOperation<?,?> operation;

    private final TokenListList tokenListList;
    
    private final List<TokenSequence<?>> tokenSequences;

    private final int endOffset;
    
    private final int expectedModCount;

    /**
     * Index of the last item retrieved from tokenListList.
     * It may be equal to Integer.MAX_VALUE when searching thgroughout the token lists
     * was finished.
     */
    private int tokenListIndex;
    
    public TokenSequenceList(TokenHierarchyOperation<?,?> operation, LanguagePath languagePath,
    int startOffset, int endOffset) {
        this.operation = operation;
        this.endOffset = endOffset;
        this.expectedModCount = operation.modCount();

        if (languagePath.size() == 1) { // Is supported too
            this.tokenListList = null;
            tokenListIndex = Integer.MAX_VALUE; // Mark no mods to tokenSequences
            TokenList<?> rootTokenList = operation.validRootTokenList();
            if (rootTokenList.languagePath() == languagePath) {
                TokenSequence<?> rootTS = LexerApiPackageAccessor.get().createTokenSequence(
                            checkWrapTokenList(rootTokenList, startOffset, endOffset));
                tokenSequences = Collections.<TokenSequence<?>>singletonList(rootTS);
            } else {
                tokenSequences = Collections.emptyList();
            }

        } else { // languagePath.size() >= 2
            this.tokenListList = operation.tokenListList(languagePath);
            // Possibly skip initial token lists accroding to startOffset
            int size = tokenListList.size();
            int high = size - 1;
            // Find the token list which has the end offset above or equal to the requested startOffset
            EmbeddedTokenList<?> firstTokenList;
            if (startOffset > 0) {
                while (tokenListIndex <= high) {
                    int mid = (tokenListIndex + high) / 2;
                    EmbeddedTokenList<?> etl = tokenListList.get(mid);
                    // Update end offset before querying
                    etl.embeddingContainer().updateStatusImpl();
                    int tlEndOffset = etl.endOffset(); // updateStatusImpl() just called
                    if (tlEndOffset < startOffset) {
                        tokenListIndex = mid + 1;
                    } else if (tlEndOffset > startOffset) {
                        high = mid - 1;
                    } else { // tl ends exactly at start offset
                        tokenListIndex = mid + 1; // take the first above this
                        break;
                    }
                }
                // If not found exactly -> take the higher one (index variable)
                firstTokenList = tokenListList.getOrNull(tokenListIndex);
                if (tokenListIndex == size) { // Right above the ones that existed at begining of bin search
                    while (firstTokenList != null) {
                        firstTokenList.embeddingContainer().updateStatusImpl();
                        if (firstTokenList.endOffset() >= startOffset) { // updateStatusImpl() just called
                            break;
                        }
                        firstTokenList = tokenListList.getOrNull(++tokenListIndex);
                    }
                }

            } else { // startOffset == 0
                firstTokenList = tokenListList.getOrNull(0);
            }

            if (firstTokenList != null) {
                firstTokenList.embeddingContainer().updateStatusImpl();
                tokenSequences = new ArrayList<TokenSequence<?>>(4);
                tokenSequences.add(LexerApiPackageAccessor.get().createTokenSequence(
                        checkWrapTokenList(firstTokenList, startOffset, endOffset)));

            } else {// firstTokenList == null
                tokenSequences = Collections.emptyList();
                tokenListIndex = Integer.MAX_VALUE; // No token sequences at all
            }
        }
    }
    
    private TokenList<?> checkWrapTokenList(TokenList<?> tokenList, int startOffset, int endOffset) {
        // Expected that updateStatusImpl() was just called
        boolean wrapStart = ((startOffset > 0)
                && (tokenList.startOffset() < startOffset)
                && (startOffset < tokenList.endOffset()));
        boolean wrapEnd = ((endOffset != Integer.MAX_VALUE)
                && (tokenList.startOffset() < endOffset)
                && (endOffset < tokenList.endOffset()));
        if (wrapStart || wrapEnd) // Must create sub sequence
            tokenList = SubSequenceTokenList.create(
                    tokenList, startOffset, endOffset);
        if (wrapEnd) { // Also this will be the last one list
            tokenListIndex = Integer.MAX_VALUE;
        }
        return tokenList;
    }
    
    @Override
    public Iterator<TokenSequence<?>> iterator() {
        return new Itr();
    }
    
    public TokenSequence<?> get(int index) {
        findTokenSequenceWithIndex(index);
        return tokenSequences.get(index); // Will fail naturally if index too high
    }
    
    public TokenSequence<?> getOrNull(int index) {
        findTokenSequenceWithIndex(index);
        return (index < tokenSequences.size()) ? tokenSequences.get(index) : null;
    }

    public int size() {
        findTokenSequenceWithIndex(Integer.MAX_VALUE);
        return tokenSequences.size();
    }

    private void findTokenSequenceWithIndex(int index) {
        while (index >= tokenSequences.size() && tokenListIndex != Integer.MAX_VALUE) {
            EmbeddedTokenList<?> etl = tokenListList.getOrNull(++tokenListIndex);
            if (etl != null) {
                etl.embeddingContainer().updateStatusImpl();
                boolean wrapEnd = ((endOffset != Integer.MAX_VALUE)
                        && (etl.startOffset() < endOffset)
                        && (endOffset < etl.endOffset()));
                if (wrapEnd) {
                    tokenSequences.add(LexerApiPackageAccessor.get().createTokenSequence(
                            SubSequenceTokenList.create(etl, 0, endOffset)));
                    tokenListIndex = Integer.MAX_VALUE;
                } else {
                    tokenSequences.add(LexerApiPackageAccessor.get().createTokenSequence(etl));
                }
            } else { // Singnal no more token sequences
                tokenListIndex = Integer.MAX_VALUE;
            }
        }
    }
    
    void checkForComodification() {
        if (expectedModCount != operation.modCount())
            throw new ConcurrentModificationException(
                    "Caller uses obsolete TokenSequenceList: expectedModCount=" + expectedModCount + // NOI18N
                    " != modCount=" + operation.modCount()
            );
    }

    private class Itr implements Iterator<TokenSequence<?>> {
        
        private int cursor = 0;
        
        private TokenSequence<?> next;
        
        public boolean hasNext() {
            checkFetchNext();
            return (next != null);
        }
        
        public TokenSequence<?> next() {
            checkFetchNext();
            if (next == null)
                throw new NoSuchElementException();
            TokenSequence<?> ret = next;
            next = null;
            return ret;
        }
        
        private void checkFetchNext() {
            if (next == null) {
                checkForComodification();
                next = getOrNull(cursor++); // can increase cursor even if (next == null)
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException(); // underlying list is immutable
        }
        
    }
    
}
