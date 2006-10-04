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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.inc;

import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CompactMap;
import org.netbeans.lib.lexer.BranchTokenList;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Token list used by token hierarchy snapshot.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class SnapshotTokenList implements TokenList {

    /** Due to debugging purposes - dumpInfo() use. */
    private TokenHierarchyOperation snapshot;

    private IncTokenList liveTokenList;

    private int liveTokenGapStart = -1;

    private int liveTokenGapEnd;

    private int liveTokenGapStartOffset;

    private int liveTokenOffsetDiff;

    /** Captured original tokens or branches. */
    private Object[] origTokensOrBranches;

    /** Original token's offsets. The array is occupied
     * and maintained in the same way like origTokensOrBranches.
     */
    private int[] origOffsets;

    /** Index where tokens start in tokens array. */
    private int origTokenStartIndex;

    /** Number of original tokens. */
    private int origTokenCount;

    /** Overrides of tokens' offset. */
    private CompactMap<AbstractToken, Token2OffsetEntry> token2offset;

    public int liveTokenGapStart() {
        return liveTokenGapStart;
    }

    public int liveTokenGapEnd() {
        return liveTokenGapEnd;
    }
    
    public SnapshotTokenList(TokenHierarchyOperation snapshot) {
        this.snapshot = snapshot;
        this.liveTokenList = (IncTokenList)snapshot.
                liveTokenHierarchyOperation().tokenList();
        token2offset = new CompactMap<AbstractToken,Token2OffsetEntry>();
    }

    public TokenHierarchyOperation snapshot() {
        return snapshot;
    }
    
    public LanguagePath languagePath() {
        return liveTokenList.languagePath();
    }
    
    public Object tokenOrBranch(int index) {
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            return liveTokenList.tokenOrBranch(index);
        }
        index -= liveTokenGapStart;
        if (index < origTokenCount) {
            return origTokensOrBranches[origTokenStartIndex + index];
        }
        return liveTokenList.tokenOrBranch(liveTokenGapEnd + index - origTokenCount);
    }

    public int lookahead(int index) {
        // Lookahead not supported for certain snapshot's tokens
        // so better don't return it for any of them.
        return -1;
    }

    public Object state(int index) {
        // Lookahead not supported for certain snapshot's tokens
        // so better don't return it for any of them.
        return null;
    }

    public int tokenOffset(int index) {
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            return liveTokenList.tokenOffset(index);
        }
        index -= liveTokenGapStart;
        if (index < origTokenCount) {
            return origOffsets[origTokenStartIndex + index];
        }
        index -= origTokenCount;

        AbstractToken token = LexerUtilsConstants.token(liveTokenList.
                tokenOrBranchUnsync(liveTokenGapEnd + index));
        int offset;
        if (token.isFlyweight()) {
            offset = token.length();
            while (--index >= 0) {
                token = LexerUtilsConstants.token(liveTokenList.
                        tokenOrBranchUnsync(liveTokenGapEnd + index));
                if (token.isFlyweight()) {
                    offset += token.length();
                } else { // non-flyweight element
                    offset += tokenOffset(token, liveTokenList, token.rawOffset());
                    break;
                }
            }
            if (index == -1) { // below the boundary of above-gap live tokens
                index += liveTokenGapStart + origTokenCount;
                if (index >= 0) {
                    offset += tokenOffset(index);
                }
            }
            
        } else { // non-flyweight
            offset = tokenOffset(token, liveTokenList, token.rawOffset());
        }
        return offset;
    }

    /**
     * @param token non-null token for whicht the offset is being computed.
     * @param tokenList non-null token list to which the token belongs.
     * @param rawOffset raw offset of the token.
     * @return offset for the particular token.
     */
    public int tokenOffset(AbstractToken token, TokenList tokenList, int rawOffset) {
        // The following situations can happen:
        // 1. Token instance is contained in token2offset map so the token's
        //    offset is overriden by the information in the map.
        // 2. Token instance is not contained in token2offset map
        //    and the token's tokenList is IncTokenList. In that case
        //    it needs to be checked whether the regularly computed offset
        //    is above liveTokenGapStartOffset and if so then
        //    liveTokenOffsetDiff must be added to it.
        // 3. Token instance is not contained in token2offset map
        //    and the token's tokenList is not IncTokenList.
        //    It happens for removed tokens that were removed but
        //    do not need the offset correction.
        //    In that case the computed offset is simply returned.
        // 4. Token from branch token list is passed.
        //    In this case the offset of the corresponding rootBranchToken
        //    needs to be corrected if necessary.
        if (tokenList.getClass() == BranchTokenList.class) {
            BranchTokenList branchTokenList = (BranchTokenList)tokenList;
            AbstractToken rootBranchToken = branchTokenList.rootBranchToken();
            Token2OffsetEntry entry = (Token2OffsetEntry)token2offset.get(rootBranchToken);
            if (entry != null) {
                return entry.offset() + branchTokenList.childTokenOffsetShift(rawOffset);
            } else { // no special entry => check whether the regular offset is below liveTokenGapStartOffset
                int offset = branchTokenList.childTokenOffset(rawOffset);
                TokenList rootTokenList = branchTokenList.root();
                if (rootTokenList != null && rootTokenList.getClass() == IncTokenList.class) {
                    if (offset >= liveTokenGapStartOffset) {
                        offset += liveTokenOffsetDiff;
                    }
                }
                return offset;
            }

        } else {
            Token2OffsetEntry entry = (Token2OffsetEntry)token2offset.get(token);
            if (entry != null) {
                return entry.offset();
            } else {
                if (tokenList.getClass() == IncTokenList.class) {
                    rawOffset = tokenList.childTokenOffset(rawOffset);
                    if (rawOffset >= liveTokenGapStartOffset) {
                        rawOffset += liveTokenOffsetDiff;
                    }
                    return rawOffset;
                }
                return tokenList.childTokenOffset(rawOffset);
            }
        }
    }
    
    public int tokenCount() {
        return (liveTokenGapStart == -1)
                ? liveTokenList.tokenCount()
                : liveTokenList.tokenCount() - (liveTokenGapEnd - liveTokenGapStart)
                    + origTokenCount;
    }

    public int tokenCountCurrent() {
        return (liveTokenGapStart == -1)
                ? liveTokenList.tokenCountCurrent()
                : liveTokenList.tokenCountCurrent() - (liveTokenGapEnd - liveTokenGapStart)
                    + origTokenCount;
    }

    public int modCount() {
        return -1;
    }
    
    public int childTokenOffset(int rawOffset) {
        // Offset of the standalone token is absolute
        return rawOffset;
    }
    
    public char childTokenCharAt(int rawOffset, int index) {
        // No tokens expected to be parented to this token list
        throw new IllegalStateException("Not expected to be called"); // NOI18N
    }

    public void wrapToken(int index, BranchTokenList wrapper) {
        // Allow branching
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            liveTokenList.wrapToken(index, wrapper);
        } else {
            index -= liveTokenGapStart;
            if (index < origTokenCount) {
                origTokensOrBranches[origTokenStartIndex + index] = wrapper;
            } else {
                liveTokenList.wrapToken(liveTokenGapEnd + index - origTokenCount, wrapper);
            }
        }
    }

    public <T extends TokenId> AbstractToken<T> createNonFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        AbstractToken<T> nonFlyToken;
        if (liveTokenGapStart == -1 || index < liveTokenGapStart) {
            nonFlyToken = liveTokenList.createNonFlyToken(index, flyToken, offset);
        } else {
            index -= liveTokenGapStart;
            if (index < origTokenCount) {
                nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
                origTokensOrBranches[origTokenStartIndex + index] = nonFlyToken;
            } else {
                nonFlyToken = liveTokenList.createNonFlyToken(
                        liveTokenGapEnd + index - origTokenCount,
                        flyToken, offset - liveTokenOffsetDiff);
            }
        }
        return nonFlyToken;
    }
    
    public TokenList root() {
        return this;
    }
    
    public InputAttributes inputAttributes() {
        return liveTokenList.inputAttributes();
    }

    public boolean isContinuous() {
        return true;
    }

    public Set<? extends TokenId> skipTokenIds() {
        return null;
    }

    public boolean canModifyToken(int index, AbstractToken token) {
        return liveTokenGapStart != -1
                && index >= liveTokenGapStart
                && index < liveTokenGapEnd
                && !token2offset.containsKey(token);
    }

    public void update(TokenListChange change) {
        TokenList removedTokenList = change.removedTokenList();
        int startRemovedIndex = change.tokenIndex();
        int endRemovedIndex = startRemovedIndex + removedTokenList.tokenCount();
        if (liveTokenGapStart == -1) { // no modifications yet
            liveTokenGapStart = startRemovedIndex;
            liveTokenGapEnd = startRemovedIndex;
            liveTokenGapStartOffset = change.modifiedTokensStartOffset();
            origTokensOrBranches = new Object[removedTokenList.tokenCount()];
            origOffsets = new int[origTokensOrBranches.length];
        }

        int liveTokenIndexDiff = change.addedTokenCount() - change.removedTokenCount();
        if (startRemovedIndex < liveTokenGapStart) { // will affect initial shared tokens
            int extraOrigTokenCount = liveTokenGapStart - startRemovedIndex;
            ensureOrigTokensStartCapacity(extraOrigTokenCount);
            origTokenStartIndex -= extraOrigTokenCount;
            origTokenCount += extraOrigTokenCount;

            int bound = Math.min(endRemovedIndex, liveTokenGapStart);
            int index;
            int offset = change.modifiedTokensStartOffset();
            liveTokenGapStartOffset = offset;
            for (index = startRemovedIndex; index < bound; index++) {
                Object tokenOrBranch = removedTokenList.tokenOrBranch(index - startRemovedIndex);
                Token t = LexerUtilsConstants.token(tokenOrBranch);
                if (!t.isFlyweight()) {
                    AbstractToken token = (AbstractToken)t;
                    TokenList tokenList = token.tokenList();
                    if (tokenList == null) {
                        tokenList = new StandaloneTokenList(change.languagePath(),
                                change.originalText().toCharArray(offset, offset + token.length()));
                        token.setTokenList(tokenList);
                    }
                }
                origOffsets[origTokenStartIndex] = offset;
                origTokensOrBranches[origTokenStartIndex++] = tokenOrBranch;
                offset += t.length();
            }

            while (index < liveTokenGapStart) {
                Object tokenOrBranch = liveTokenList.tokenOrBranchUnsync(index + liveTokenIndexDiff);
                AbstractToken t = LexerUtilsConstants.token(tokenOrBranch);
                if (!t.isFlyweight()) {
                    token2offset.putEntry(new Token2OffsetEntry(t, offset));
                }
                origOffsets[origTokenStartIndex] = offset;
                origTokensOrBranches[origTokenStartIndex++] = tokenOrBranch;
                offset += t.length();
                index++;
            }
            liveTokenGapStart = startRemovedIndex;
        }

        if (endRemovedIndex > liveTokenGapEnd) { // will affect ending shared tokens
            int extraOrigTokenCount = endRemovedIndex - liveTokenGapEnd;
            ensureOrigTokensEndCapacity(extraOrigTokenCount);
            origTokenCount += extraOrigTokenCount;
            int origTokenIndex = origTokenStartIndex + origTokenCount - 1;

            int bound = Math.max(startRemovedIndex, liveTokenGapEnd);
            int index = endRemovedIndex;
            int offset = change.removedTokensEndOffset();
            for (index = endRemovedIndex - 1; index >= bound; index--) {
                Object tokenOrBranch = removedTokenList.tokenOrBranch(index - startRemovedIndex);
                AbstractToken t = LexerUtilsConstants.token(tokenOrBranch);
                offset -= t.length();
                if (!t.isFlyweight()) {
                    AbstractToken token = (AbstractToken)t;
                    TokenList tokenList = token.tokenList();
                    if (tokenList == null) {
                        tokenList = new StandaloneTokenList(change.languagePath(),
                                change.originalText().toCharArray(offset, offset + token.length()));
                        token.setTokenList(tokenList);
                    }
                }
                origOffsets[origTokenIndex] = offset + liveTokenOffsetDiff;
                // If the token's offset had to be diff-ed already then a map entry is necessary
                if (liveTokenOffsetDiff != 0) {
                    token2offset.putEntry(new Token2OffsetEntry(t, origOffsets[origTokenIndex]));
                }
                origTokensOrBranches[origTokenIndex--] = tokenOrBranch;
            }

            while (index >= liveTokenGapEnd) {
                Object tokenOrBranch = liveTokenList.tokenOrBranchUnsync(index + liveTokenIndexDiff);
                AbstractToken t = LexerUtilsConstants.token(tokenOrBranch);
                offset -= t.length();
                if (!t.isFlyweight()) {
                    token2offset.putEntry(new Token2OffsetEntry(t, offset));
                }
                origOffsets[origTokenIndex] = offset + liveTokenOffsetDiff;
                token2offset.putEntry(new Token2OffsetEntry(t, origOffsets[origTokenIndex]));
                origTokensOrBranches[origTokenIndex--] = tokenOrBranch;
                index--;
            }
            liveTokenGapEnd = endRemovedIndex;
        }

        liveTokenOffsetDiff += change.removedLength() - change.insertedLength();
        liveTokenGapEnd += liveTokenIndexDiff;
    }

    private void ensureOrigTokensStartCapacity(int extraOrigTokenCount) {
        if (extraOrigTokenCount > origTokensOrBranches.length - origTokenCount) { // will need to reallocate
            // Could check for maximum possible token count (origTokenCount + below-and-above live token counts)
            // but would cause init of live tokens above gap which is undesirable
            Object[] newOrigTokensOrBranches = new Object[(origTokensOrBranches.length * 3 / 2) + extraOrigTokenCount];
            int[] newOrigOffsets = new int[newOrigTokensOrBranches.length];
            int newIndex = Math.max(extraOrigTokenCount, (newOrigTokensOrBranches.length
                    - (origTokenCount + extraOrigTokenCount)) / 2);
            System.arraycopy(origTokensOrBranches, origTokenStartIndex,
                    newOrigTokensOrBranches, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    newOrigOffsets, newIndex, origTokenCount);
            origTokensOrBranches = newOrigTokensOrBranches;
            origOffsets = newOrigOffsets;
            origTokenStartIndex = newIndex;

        } else if (extraOrigTokenCount > origTokenStartIndex) { // only move
            // Move to the end of the array
            int newIndex = origTokensOrBranches.length - origTokenCount;
            System.arraycopy(origTokensOrBranches, origTokenStartIndex,
                    origTokensOrBranches, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    origOffsets, newIndex, origTokenCount);
            origTokenStartIndex = origTokensOrBranches.length - origTokenCount;
        }
    }
    
    private void ensureOrigTokensEndCapacity(int extraOrigTokenCount) {
        if (extraOrigTokenCount > origTokensOrBranches.length - origTokenCount) { // will need to reallocate
            // Could check for maximum possible token count (origTokenCount + below-and-above live token counts)
            // but would cause init of live tokens above gap which is undesirable
            Object[] newOrigTokensOrBranches = new Object[(origTokensOrBranches.length * 3 / 2) + extraOrigTokenCount];
            int[] newOrigOffsets = new int[newOrigTokensOrBranches.length];
            int newIndex = (newOrigTokensOrBranches.length
                    - (origTokenCount + extraOrigTokenCount)) / 2;
            System.arraycopy(origTokensOrBranches, origTokenStartIndex,
                    newOrigTokensOrBranches, newIndex, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    newOrigOffsets, newIndex, origTokenCount);
            origTokensOrBranches = newOrigTokensOrBranches;
            origOffsets = newOrigOffsets;
            origTokenStartIndex = newIndex;

        } else if (extraOrigTokenCount > origTokensOrBranches.length - origTokenCount - origTokenStartIndex) { // only move
            // Move to the end of the array
            System.arraycopy(origTokensOrBranches, origTokenStartIndex,
                    origTokensOrBranches, 0, origTokenCount);
            System.arraycopy(origOffsets, origTokenStartIndex,
                    origOffsets, 0, origTokenCount);
            origTokenStartIndex = 0;
        }
    }

    public String toString() {
        return "liveTokenGapStart=" + liveTokenGapStart +
                ", liveTokenGapEnd=" + liveTokenGapEnd +
                ", liveTokenGapStartOffset=" + liveTokenGapStartOffset +
                ", liveTokenOffsetDiff=" + liveTokenOffsetDiff +
                ",\n origTokenStartIndex=" + origTokenStartIndex +
                ", origTokenCount=" + origTokenCount +
                ", token2offset: " + token2offset;

    }

    public int tokenShiftStartOffset() {
        return liveTokenGapStartOffset;
    }
    
    public int tokenShiftEndOffset() {
        return liveTokenGapStartOffset + liveTokenOffsetDiff;
    }

    private static final class Token2OffsetEntry extends CompactMap.AbstractMapEntry<AbstractToken, Token2OffsetEntry> {
        
        private final AbstractToken token; // 16 bytes
        
        private final int offset; // 20 bytes
        
        Token2OffsetEntry(AbstractToken token, int offset) {
            this.token = token;
            this.offset = offset;
        }
        
        public AbstractToken getKey() {
            return token;
        }

        public Token2OffsetEntry getValue() {
            return this;
        }
        
        public int offset() {
            return offset;
        }

        public Token2OffsetEntry setValue(Token2OffsetEntry value) {
            throw new IllegalStateException("Prohibited"); // NOI18N
        }

        public String toString() {
            // Debug the offset being held
            return String.valueOf(offset);
        }
        
    }
    
}
