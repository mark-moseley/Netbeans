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

import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.lib.editor.util.FlyOffsetGapList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Embedded token list maintains a list of tokens
 * on a particular embedded language level .
 * <br>
 * The physical storage contains a gap to speed up list modifications
 * during typing in a document when tokens are typically added/removed
 * at the same index in the list.
 *
 * <p>
 * There is an intent to not degrade performance significantly
 * with each extra language embedding level so the token list maintains direct
 * link to the root level.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class EmbeddedTokenList<T extends TokenId>
extends FlyOffsetGapList<TokenOrEmbedding<T>> implements MutableTokenList<T> {
    
    /** Flag for additional correctness checks (may degrade performance). */
    private static final boolean testing = Boolean.getBoolean("netbeans.debug.lexer.test");
    
    /**
     * Marker value that represents that an attempt to create default embedding was
     * made but was unsuccessful.
     */
    public static final EmbeddedTokenList<TokenId> NO_DEFAULT_EMBEDDING
            = new EmbeddedTokenList<TokenId>(null, null, null);
    
    /**
     * Embedding container carries info about the token into which this
     * token list is embedded.
     */
    private EmbeddingContainer<?> embeddingContainer; // 36 bytes (32-super + 4)
    
    /**
     * Language embedding for this embedded token list.
     */
    private final LanguageEmbedding<T> embedding; // 40 bytes
    
    /**
     * Language path of this token list.
     */
    private final LanguagePath languagePath; // 44 bytes
    
    /**
     * Storage for lookaheads and states.
     * <br/>
     * It's non-null only initialized for mutable token lists
     * or when in testing environment.
     */
    private LAState laState; // 48 bytes
    
    /**
     * Next embedded token list forming a single-linked list.
     */
    private EmbeddedTokenList<?> nextEmbeddedTokenList; // 52 bytes
    
    /**
     * Additional information in case this ETL is contained in a JoinTokenList.
     * <br/>
     * Through this info a reference to the JoinTokenList is held. There is no other
     * indexed structure so the EmbeddedTokenList members of TokenListList
     * must be binary-searched.
     */
    public EmbeddedJoinInfo joinInfo; // 56 bytes

    
    public EmbeddedTokenList(EmbeddingContainer<?> embeddingContainer,
            LanguagePath languagePath, LanguageEmbedding<T> embedding
    ) {
        super(1); // Suitable for adding join-token parts
        this.embeddingContainer = embeddingContainer;
        this.languagePath = languagePath;
        this.embedding = embedding;

        if (embeddingContainer != null) { // ec may be null for NO_DEFAULT_EMBEDDING only
            initLAState();
        }
    }

    public void initAllTokens() {
        assert (!embedding.joinSections()); // Joined token creation must be used instead
//        initLAState();
        // Lex the whole input represented by token at once
        LexerInputOperation<T> lexerInputOperation = createLexerInputOperation(
                0, startOffset(), null);
        AbstractToken<T> token = lexerInputOperation.nextToken();
        while (token != null) {
            addToken(token, lexerInputOperation);
            token = lexerInputOperation.nextToken();
        }
        lexerInputOperation.release();
        lexerInputOperation = null;
        trimStorageToSize();
    }
    
    private void initLAState() {
        this.laState = (modCount() != LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT || testing)
                ? LAState.empty() // Will collect LAState
                : null;
    }

    /**
     * Return join token list with active token list positioned to this ETL
     * or return null if this.joinInfo == null.
     */
    public JoinTokenList<T> joinTokenList() {
        if (joinInfo != null) {
            TokenListList<T> tokenListList = rootTokenList().tokenHierarchyOperation().existingTokenListList(languagePath);
            int etlIndex = tokenListList.findIndex(startOffset());
            int tokenListStartIndex = etlIndex - joinInfo.tokenListIndex();
            JoinTokenList<T> jtl = new JoinTokenList<T>(tokenListList, joinInfo.base, tokenListStartIndex);
            // Position to this etl's join index
            jtl.setActiveTokenListIndex(etlIndex - tokenListStartIndex);
            return jtl;
        }
        return null;
    }

    /**
     * Add token without touching laState - suitable for JoinToken's handling.
     *
     * @param token non-null token
     */
    public void addToken(AbstractToken<T> token) {
        updateElementOffsetAdd(token); // must subtract startOffset()
        add(token);
    }

    public void addToken(AbstractToken<T> token, LexerInputOperation<T> lexerInputOperation) {
        addToken(token);
        if (laState != null) { // maintaining lookaheads and states
            // Only get LA and state when necessary (especially lexerState() may be costly)
            laState = laState.add(lexerInputOperation.lookahead(), lexerInputOperation.lexerState());
        }
    }

    /**
     * Used when dealing with PartToken instances.
     */
    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        addToken(token);
        if (laState != null) { // maintaining lookaheads and states
            laState = laState.add(lookahead, state);
        }
    }

    public void trimStorageToSize() {
        trimToSize(); // Compact storage
        if (laState != null)
            laState.trimToSize();
    }
    
    public EmbeddedTokenList<?> nextEmbeddedTokenList() {
        return nextEmbeddedTokenList;
    }
    
    void setNextEmbeddedTokenList(EmbeddedTokenList<?> nextEmbeddedTokenList) {
        this.nextEmbeddedTokenList = nextEmbeddedTokenList;
    }
    
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    public LanguageEmbedding embedding() {
        return embedding;
    }

    public int tokenCount() {
        return tokenCountCurrent();
    }
    
    public int tokenCountCurrent() {
        return size();
    }

    public int joinTokenCount() {
        int tokenCount = tokenCountCurrent();
        if (tokenCount > 0 && joinInfo.joinTokenLastPartShift() > 0)
            tokenCount--;
        return tokenCount;
    }

    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        synchronized (rootTokenList()) {
            return (index < size()) ? get(index) : null;
        }
    }
    
    public int lookahead(int index) {
        return (laState != null) ? laState.lookahead(index) : -1;
    }

    public Object state(int index) {
        return (laState != null) ? laState.state(index) : null;
    }

    /**
     * Returns absolute offset of the token at the given index
     * (startOffset gets added to the child token's real offset).
     * <br/>
     * For token hierarchy snapshots the returned value is corrected
     * in the TokenSequence explicitly by adding TokenSequence.tokenOffsetDiff.
     */
    public int tokenOffsetByIndex(int index) {
//        embeddingContainer().checkStatusUpdated();
        return elementOffset(index);
    }

    public int tokenOffset(AbstractToken<T> token) {
        if (token.getClass() == JoinToken.class) {
            return token.offset(null);
        }
        int rawOffset = token.rawOffset();
//        embeddingContainer().checkStatusUpdated();
        int relOffset = (rawOffset < offsetGapStart())
                ? rawOffset
                : rawOffset - offsetGapLength();
        return startOffset() + relOffset;
    }

    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexBinSearch(this, offset, tokenCountCurrent());
    }

    public int modCount() {
        // Mod count of EC must be returned to allow custom removed embeddings to work
        //  - they set LexerUtilsConstants.MOD_COUNT_REMOVED as cachedModCount.
        return embeddingContainer.cachedModCount();
    }
    
    @Override
    public int startOffset() { // used by FlyOffsetGapList
//        embeddingContainer.checkStatusUpdated();
        return embeddingContainer.branchTokenStartOffset() + embedding.startSkipLength();
    }
    
    public int endOffset() {
//        embeddingContainer.checkStatusUpdated();
        return embeddingContainer.branchTokenStartOffset() + embeddingContainer.token().length()
                - embedding.endSkipLength();
    }
    
    public int textLength() {
        return embeddingContainer.token().length() - embedding.startSkipLength() - embedding.endSkipLength();
    }
    
    public boolean isRemoved() {
        return embeddingContainer.isRemoved();
    }

    public TokenList<?> rootTokenList() {
        return embeddingContainer.rootTokenList();
    }

    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return rootTokenList().tokenHierarchyOperation();
    }
    
    protected int elementRawOffset(TokenOrEmbedding<T> elem) {
        return elem.token().rawOffset();
    }

    protected void setElementRawOffset(TokenOrEmbedding<T> elem, int rawOffset) {
        elem.token().setRawOffset(rawOffset);
    }
    
    protected boolean isElementFlyweight(TokenOrEmbedding<T> elem) {
        return elem.token().isFlyweight();
    }
    
    protected int elementLength(TokenOrEmbedding<T> elem) {
        return elem.token().length();
    }
    
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        synchronized (rootTokenList()) {
            TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset2Raw(offset));
            set(index, nonFlyToken);
            return nonFlyToken;
        }
    }

    public void wrapToken(int index, EmbeddingContainer<T> embeddingContainer) {
        synchronized (rootTokenList()) {
            set(index, embeddingContainer);
        }
    }

    public InputAttributes inputAttributes() {
        return rootTokenList().inputAttributes();
    }

    // MutableTokenList extra methods
    public TokenOrEmbedding<T> tokenOrEmbeddingUnsync(int index) {
        return get(index);
    }

    public LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState) {
//        embeddingContainer.checkStatusUpdated();
//        AbstractToken<?> branchToken = embeddingContainer.token();
        int endOffset = endOffset();
//        assert (!branchToken.isRemoved()) : "No lexing when token is removed";
//        assert (relexOffset >= startOffset()) : "Invalid relexOffset=" + relexOffset + " < startOffset()=" + startOffset();
        assert (relexOffset <= endOffset) : "Invalid relexOffset=" + relexOffset + " > endOffset()=" + endOffset;
        return new TextLexerInputOperation<T>(this, tokenIndex, relexState, relexOffset, endOffset);
    }

    public boolean isFullyLexed() {
        return true;
    }

    public void replaceTokens(TokenListChange<T> change, int diffLength) {
        int index = change.index();
        // Remove obsolete tokens (original offsets are retained)
        int removedTokenCount = change.removedTokenCount();
        int rootModCount = rootTokenList().modCount();
        AbstractToken<T> firstRemovedToken = null;
        if (removedTokenCount > 0) {
            @SuppressWarnings("unchecked")
            TokenOrEmbedding<T>[] removedTokensOrEmbeddings = new TokenOrEmbedding[removedTokenCount];
            copyElements(index, index + removedTokenCount, removedTokensOrEmbeddings, 0);
            firstRemovedToken = removedTokensOrEmbeddings[0].token();
            for (int i = 0; i < removedTokenCount; i++) {
                TokenOrEmbedding<T> tokenOrEmbedding = removedTokensOrEmbeddings[i];
                // It's necessary to update-status of all removed tokens' contained embeddings
                // since otherwise (if they would not be up-to-date) they could not be updated later
                // as they lose their parent token list which the update-status relies on.
                EmbeddingContainer<T> ec = tokenOrEmbedding.embedding();
                if (ec != null) {
                    ec.updateStatusUnsyncAndMarkRemoved();
                    assert (ec.cachedModCount() != rootModCount) : "ModCount already updated"; // NOI18N
                }
                AbstractToken<T> token = tokenOrEmbedding.token();
                if (!token.isFlyweight()) {
                    updateElementOffsetRemove(token);
                    token.setTokenList(null);
                }
            }
            remove(index, removedTokenCount); // Retain original offsets
            laState.remove(index, removedTokenCount); // Remove lookaheads and states
            change.setRemovedTokens(removedTokensOrEmbeddings);
        } else {
            change.setRemovedTokensEmpty();
        }

        if (diffLength != 0) { // JoinTokenList may pass 0 to not do any offset updates
            // Move and fix the gap according to the performed modification.
            // Instead of modOffset the gap is located at first relexed token's start
            // because then the already precomputed index corresponding to the given offset
            // can be reused. Otherwise there would have to be another binary search for index.
            int startOffset = startOffset(); // updateStatus() should already be called
            if (offsetGapStart() != change.offset() - startOffset) {
                // Minimum of the index of the first removed index and original computed index
                moveOffsetGap(change.offset() - startOffset, change.index());
            }
            updateOffsetGapLength(-diffLength);
        }

        // Add created tokens.
        // This should be called early when all the members are true tokens
        List<TokenOrEmbedding<T>> addedTokenOrEmbeddings = change.addedTokenOrEmbeddings();
        if (addedTokenOrEmbeddings != null) {
            for (TokenOrEmbedding<T> tokenOrEmbedding : addedTokenOrEmbeddings) {
                updateElementOffsetAdd(tokenOrEmbedding.token());
            }
            addAll(index, addedTokenOrEmbeddings);
            laState = laState.addAll(index, change.laState());
            change.syncAddedTokenCount();
            // Check for bounds change only
            if (removedTokenCount == 1 && addedTokenOrEmbeddings.size() == 1) {
                // Compare removed and added token ids and part types
                AbstractToken<T> addedToken = change.addedTokenOrEmbeddings().get(0).token();
                if (firstRemovedToken.id() == addedToken.id()
                    && firstRemovedToken.partType() == addedToken.partType()
                ) {
                    change.markBoundsChange();
                }
            }
        }
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<T> skipTokenIds() {
        return null;
    }
    
    public EmbeddingContainer<?> embeddingContainer() {
        return embeddingContainer;
    }
    
    public void setEmbeddingContainer(EmbeddingContainer<?> embeddingContainer) {
        this.embeddingContainer = embeddingContainer;
    }
    
    public StringBuilder dumpInfo(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder(50);
        }
        sb.append("ETL<").append(startOffset());
        sb.append(",").append(endOffset());
        sb.append("> TC=").append(tokenCountCurrent());
        if (joinInfo != null) {
            sb.append("(").append(joinTokenCount()).append(')');
            sb.append(" JI:");
            joinInfo.dumpInfo(sb);
        }
        sb.append(", IHC=").append(System.identityHashCode(this));
        return sb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        dumpInfo(sb);
        LexerUtilsConstants.appendTokenList(sb, this);
        return sb.toString();
    }

}
