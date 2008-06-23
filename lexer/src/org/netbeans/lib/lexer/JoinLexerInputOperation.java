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
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Lexer input operation over multiple joined sections (embedded token lists).
 * <br/>
 * It produces regular tokens (to be added directly into ETL represented by
 * {@link #activeTokenList()} and also special {@link #JoinToken} instances
 * in case a token spans boundaries of multiple ETLs.
 * <br/>
 * It can either work over JoinTokenList directly or, during a modification,
 * it simulates that certain token lists are already removed/added to underlying token list.
 * <br/>
 * 
 * {@link #recognizedTokenLastInTokenList()} gives information whether the lastly
 * produced token ends right at boundary of the activeTokenList.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JoinLexerInputOperation<T extends TokenId> extends LexerInputOperation<T> {
    
    CharSequence inputSourceText;

    private TokenListText readText; // For servicing read()
    
    private TokenListText readExistingText;
    
    /**
     * Token list in which the last recognized token started.
     */
    private EmbeddedTokenList<T> activeTokenList;
    
    /**
     * Index of activeTokenList in JTL.
     */
    private int activeTokenListIndex;
    
    /**
     * End offset of the active token list.
     */
    private int activeTokenListEndOffset;
    
    /**
     * Real token's start offset used to derive the token's offset in ETL.
     * Since tokenStartOffset is affected by TokenListList.readOffsetShift
     * it cannot be used for this purpose.
     */
    private int realTokenStartOffset;
    
    private boolean recognizedTokenJoined; // Whether recognized token will consist of parts
    

    public JoinLexerInputOperation(JoinTokenList<T> joinTokenList, int relexJoinIndex, Object lexerRestartState,
            int activeTokenListIndex, int relexOffset
    ) {
        super(joinTokenList, relexJoinIndex, lexerRestartState);
        this.inputSourceText = joinTokenList.inputSourceText();
        this.activeTokenListIndex = activeTokenListIndex;
        tokenStartOffset = relexOffset;
        readOffset = relexOffset;
    }

    public final void init() {
        // Following code uses tokenList() method overriden in MutableJoinLexerInputOperation
        // so the following code would fail when placed in constructor since the constructor of MJLIO would not yet run.
        fetchActiveTokenList();
        readText = new TokenListText();
        readText.tokenListIndex = activeTokenListIndex;
        readText.tokenListStartOffset = realTokenStartOffset; // contains start of active ETL
        readText.tokenListEndOffset = activeTokenListEndOffset;
        // Leave readText.readOffsetShift == 0 for first list

        // Assign realTokenStartOffset after fetchActiveTokenList() since it would overwrite it
        realTokenStartOffset = readOffset;
    }

    /**
     * Get active ETL into which the last produced token should be added.
     * For join tokens there is an ETL into which a last part of JT should be added.
     */
    public EmbeddedTokenList<T> activeTokenList() {
        return activeTokenList;
    }
    
    /**
     * Get index of active ETL into which the last produced token should be added.
     * For join tokens there is an index of the last ETL into which a last part of JT should be added.
     */
    public int activeTokenListIndex() {
        return activeTokenListIndex;
    }
    
    /**
     * True if the last returned token is last in {@link #activeTokenList()}.
     * For join tokens this applies to the last part of join token.
     */
    public boolean recognizedTokenLastInTokenList() {
        // realTokenStartOffset is set to the end of last recognized token
        return (realTokenStartOffset == activeTokenListEndOffset);
    }

    @Override
    public int lastTokenEndOffset() {
        return realTokenStartOffset;
    }

    public int read(int offset) { // index >= 0 is guaranteed by contract
        return readText.read(offset);
    }

    public char readExisting(int offset) {
        if (readText.isInBounds(offset)) {
            return readText.inBoundsChar(offset);
        }
        if (readExistingText == null) {
            readExistingText = new TokenListText();
            readExistingText.initFrom(readText);
        }
        return readExistingText.existingChar(offset);
    }

    @Override
    public void assignTokenLength(int tokenLength) {
        super.assignTokenLength(tokenLength);
        // Check whether activeTokenList needs to be changed due to various flags
        if (recognizedTokenLastInTokenList()) { // Advance to next token list
            // Since this is done when recognizing a next token it should be ok when recognizing
            // last token in the last ETL (it should not go beyond last ETL).
            do {
                activeTokenListIndex++;
                fetchActiveTokenList();
            } while (realTokenStartOffset == activeTokenListEndOffset); // Skip empty ETLs
        }
        // Advance to end of currently recognized token
        realTokenStartOffset += tokenLength;
        // Joined token past ETL's boundary
        recognizedTokenJoined = (realTokenStartOffset > activeTokenListEndOffset);
    }
    
    private void fetchActiveTokenList() {
        activeTokenList = tokenList(activeTokenListIndex);
        realTokenStartOffset = activeTokenList.startOffset();
        activeTokenListEndOffset = activeTokenList.endOffset();
    }
    
    public EmbeddedTokenList<T> tokenList(int tokenListIndex) { // Also used by JoinTokenListChange
        return ((JoinTokenList<T>) tokenList).tokenList(tokenListIndex);
    }

    protected int tokenListCount() {
        return ((JoinTokenList<T>) tokenList).tokenListCount();
    }

    protected void fillTokenData(AbstractToken<T> token) {
        if (!recognizedTokenJoined) {
            token.setTokenList(activeTokenList);
            // Subtract tokenLength since this is already advanced to end of token
            token.setRawOffset(realTokenStartOffset - tokenLength);
        }
    }
    
    @Override
    protected boolean isFlyTokenAllowed() {
        return super.isFlyTokenAllowed() && !recognizedTokenJoined;
    }
    
    @Override
    protected AbstractToken<T> createDefaultTokenInstance(T id) {
        if (recognizedTokenJoined) {
            return createJoinToken(id, null, PartType.COMPLETE);
        } else { // Regular case
            return super.createDefaultTokenInstance(id);
        }
    }

    @Override
    protected AbstractToken<T> createPropertyTokenInstance(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        if (recognizedTokenJoined) {
            return createJoinToken(id, null, partType);
        } else { // Regular case
            return super.createPropertyTokenInstance(id, propertyProvider, partType);
        }
    }
    
    private AbstractToken<T> createJoinToken(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        // Create join token
        // realTokenStartOffset is already advanced by tokenLength so first decrease it
        realTokenStartOffset -= tokenLength;
        JoinToken<T> joinToken = new JoinToken<T>(id, tokenLength, propertyProvider, partType);
        int joinPartCountEstimate = readText.tokenListIndex - activeTokenListIndex + 1;
        @SuppressWarnings("unchecked")
        PartToken<T>[] parts = new PartToken[joinPartCountEstimate];
        int partLength = activeTokenListEndOffset - realTokenStartOffset;
        PartToken<T> partToken = new PartToken<T>(id, partLength, propertyProvider, PartType.START, joinToken, 0, 0);
        partToken.setTokenList(activeTokenList);
        partToken.setRawOffset(realTokenStartOffset); // realTokenStartOffset already decreased by tokenLength
        parts[0] = partToken;
        int partIndex = 1;
        int partTextOffset = partLength; // Length of created parts so far
        int firstPartTokenListIndex = activeTokenListIndex;
        do {
            activeTokenListIndex++;
            fetchActiveTokenList();
            // realTokenStartOffset set to start activeTokenList
            PartType partPartType;
            // Attempt total ETL's length as partLength
            partLength = activeTokenListEndOffset - realTokenStartOffset;
            if (partLength == 0) {
                continue;
            }
            if (partTextOffset + partLength >= tokenLength) { // Last part
                partLength = tokenLength - partTextOffset;
                // If the partType of the join token is not complete then this will be PartType.MIDDLE
                partPartType = (partType == PartType.START) ? PartType.MIDDLE : PartType.END;
            } else { // Non-last part
                partPartType = PartType.MIDDLE;
            }

            partToken = new PartToken<T>(id, partLength, propertyProvider, partPartType, joinToken, partIndex, partTextOffset);
            // realTokenStartOffset still points to start of activeTokenList
            partToken.setRawOffset(realTokenStartOffset); // ETL.startOffset() will be subtracted upon addition to ETL
            partToken.setTokenList(activeTokenList);
            partTextOffset += partLength;
            parts[partIndex++] = partToken;
        } while (partTextOffset < tokenLength);
        // Update realTokenStartOffset which pointed to start of activeTokenList
        realTokenStartOffset += partLength;
        // Check that the array does not have any extra items
        if (partIndex < parts.length) {
            @SuppressWarnings("unchecked")
            PartToken<T>[] tmp = new PartToken[partIndex];
            System.arraycopy(parts, 0, tmp, 0, partIndex);
            parts = tmp;
        }
        List<PartToken<T>> partList = ArrayUtilities.unmodifiableList(parts);
        joinToken.setJoinedParts(partList, activeTokenListIndex - firstPartTokenListIndex);
        // joinToken.setTokenList() makes no sense - JoinTokenList instances are temporary
        // joinToken.setRawOffset() makes no sense - offset taken from initial part
        return joinToken;
    }
    
    /**
     * Class for reading of text of subsequent ETLs - it allows to see their text
     * as a consecutive character sequence (inputSourceText is used as a backing char sequence)
     * with an increasing readIndex (it's not decremented after token's recognition).
     */
    final class TokenListText {

        int tokenListIndex;

        int tokenListStartOffset;

        int tokenListEndOffset;

        /**
         * A constant added to readOffset to allow a smoothly increasing reading offset
         * when reading through multiple ETLs with gaps among them.
         */
        int readOffsetShift;

        void init() {
            EmbeddedTokenList<T> etl = tokenList(activeTokenListIndex);
            tokenListStartOffset = etl.startOffset();
            tokenListEndOffset = etl.endOffset();
            // No extra shift for first token
        }

        void initFrom(TokenListText text) {
            this.tokenListIndex = text.tokenListIndex;
            this.tokenListStartOffset = text.tokenListStartOffset;
            this.tokenListEndOffset = text.tokenListEndOffset;
            this.readOffsetShift = text.readOffsetShift;
        }

        /**
         * Read next char or return EOF.
         */
        int read(int offset) {
            offset += readOffsetShift;
            if (offset < tokenListEndOffset) {
                return inputSourceText.charAt(offset);
            } else {
                while (++tokenListIndex < tokenListCount()) {
                    EmbeddedTokenList etl = tokenList(tokenListIndex);
                    tokenListStartOffset = etl.startOffset();
                    // Increase offset shift by the size of gap between ETLs
                    readOffsetShift += tokenListStartOffset - tokenListEndOffset;
                    // Also shift given offset value
                    offset += tokenListStartOffset - tokenListEndOffset;
                    tokenListEndOffset = etl.endOffset();
                    if (offset < tokenListEndOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                tokenListIndex--; // Return to (tokenListCount() - 1)
                return LexerInput.EOF;
            }
        }

        /**
         * Check whether currently set text covers the given relative index.
         * 
         * @param index index in the same metrics as readIndex.
         * @return whether the given index is within current bounds.
         */
        boolean isInBounds(int offset) {
            offset += readOffsetShift;
            return offset >= tokenListStartOffset && offset < tokenListEndOffset;
        }
        
        /**
         * Get char that was previously verified to be within bounds.
         */
        char inBoundsChar(int offset) {
            offset += readOffsetShift;
            return inputSourceText.charAt(offset);
        }
        
        char existingChar(int offset) {
            offset += readOffsetShift;
            if (offset < tokenListStartOffset) {
                while (true) { // Char should exist
                    tokenListIndex--;
                    EmbeddedTokenList etl = tokenList(tokenListIndex);
                    tokenListEndOffset = etl.endOffset();
                    // Decrease offset shift by the size of gap between ETLs
                    readOffsetShift -= tokenListStartOffset - tokenListEndOffset;
                    // Also shift given offset value
                    offset -= tokenListStartOffset - tokenListEndOffset;
                    tokenListStartOffset = etl.startOffset();
                    if (offset >= tokenListStartOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                
            } else if (offset >= tokenListEndOffset) {
                while (true) { // Char should exist
                    tokenListIndex++;
                    EmbeddedTokenList etl = tokenList(tokenListIndex);
                    tokenListStartOffset = etl.startOffset();
                    // Increase offset shift by the size of gap between ETLs
                    readOffsetShift += tokenListStartOffset - tokenListEndOffset;
                    // Also shift given offset value
                    offset += tokenListStartOffset - tokenListEndOffset;
                    tokenListEndOffset = etl.endOffset();
                    if (offset < tokenListEndOffset) { // ETL might be empty
                        return inputSourceText.charAt(offset);
                    }
                }
                
            }
            // Index within current bounds
            return inputSourceText.charAt(offset);
        }
        
    }
    
    @Override
    public String toString() {
        return super.toString() + ", realTokenStartOffset=" + realTokenStartOffset + // NOI18N
                ", activeTokenListIndex=" + activeTokenListIndex + // NOI18N
                ", activeTokenListEndOffset=" + activeTokenListEndOffset; // NOI18N
    }

}
