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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.CustomTextToken;
import org.netbeans.lib.lexer.token.DefaultToken;
import org.netbeans.lib.lexer.token.PropertyToken;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Implementation of the functionality related to lexer input.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerInputOperation<T extends TokenId> {

    // -J-Dorg.netbeans.lib.lexer.LexerInputOperation.level=FINE
    static final Logger LOG = Logger.getLogger(LexerInputOperation.class.getName());
    
    protected final TokenList<T> tokenList;
    
    /**
     * Current reading index which usually corresponds to real offset.
     * <br/>
     * It should be set to its initial value in the constructor by descendants.
     */
    protected int readOffset;
    
    /**
     * A value that designates a start of a token being currently recognized.
     */
    protected int tokenStartOffset;

    /**
     * Maximum index from which the char was fetched for current
     * (or previous) tokens recognition.
     * <br>
     * The index is updated lazily - only when EOF is reached
     * and when backup() is called.
     */
    private int lookaheadOffset;
    
    /**
     * Token length computed by assignTokenLength().
     */
    protected int tokenLength;
    
    protected Lexer<T> lexer;
    
    protected final LanguageOperation<T> innerLanguageOperation;

    
    /**
     * How many flyweight tokens were created in a row.
     */
    private int flyTokenSequenceLength;
    
    public LexerInputOperation(TokenList<T> tokenList, int tokenIndex, Object lexerRestartState) {
        this.tokenList = tokenList;
        LanguagePath languagePath = tokenList.languagePath();
        this.innerLanguageOperation = LexerUtilsConstants.innerLanguageOperation(languagePath);
        
        // Determine flyTokenSequenceLength setting
        while (--tokenIndex >= 0 && tokenList.tokenOrEmbedding(tokenIndex).token().isFlyweight()) {
            flyTokenSequenceLength++;
        }

        LanguageHierarchy<T> languageHierarchy = LexerApiPackageAccessor.get().languageHierarchy(
                LexerUtilsConstants.<T>innerLanguage(languagePath));
        TokenFactory<T> tokenFactory = LexerSpiPackageAccessor.get().createTokenFactory(this);
        LexerInput lexerInput = LexerSpiPackageAccessor.get().createLexerInput(this);

        LexerRestartInfo<T> info = LexerSpiPackageAccessor.get().createLexerRestartInfo(
                lexerInput, tokenFactory, lexerRestartState,
                languagePath, tokenList.inputAttributes());
        lexer = LexerSpiPackageAccessor.get().createLexer(languageHierarchy, info);
    }

    public abstract int read(int offset);

    public abstract char readExisting(int offset);

    /**
     * Fill appropriate data like token list and offset into a non-flyweight token.
     * <br/>
     * This method should also move over the token's characters by increasing
     * starting offset of the token and possibly other related variables.
     * 
     * @param token non-null non-flyweight token.
     */
    protected abstract void fillTokenData(AbstractToken<T> token);
    
    public final int read() {
        int c = read(readOffset++);
        if (c == LexerInput.EOF) {
            lookaheadOffset = readOffset; // count EOF char into lookahead
            readOffset--; // readIndex must not include EOF
        }
        return c;
    }
    
    public final int readLength() {
        return readOffset - tokenStartOffset;
    }
    
    public final char readExistingAtIndex(int index) {
        return readExisting(tokenStartOffset + index);
    }
    
    public final void backup(int count) {
        if (lookaheadOffset < readOffset) {
            lookaheadOffset = readOffset;
        }
        readOffset -= count;
    }
    
    /**
     * Get last recognized token's lookahead.
     * The method should only be used after fetching of the token.
     * 
     * @return extra characters need for token's recognition >= 0.
     */
    public final int lookahead() {
        return Math.max(lookaheadOffset, readOffset) - tokenStartOffset;
    }

    public AbstractToken<T> nextToken() {
        while (true) {
            AbstractToken<T> token = (AbstractToken<T>)lexer.nextToken();
            if (token == null) {
                checkLexerInputFinished();
                return null;
            }
            // Check if token id of the new token belongs to the language
            Language<T> language = innerLanguageOperation.language();
            // Check that the id belongs to the language
            if (!isSkipToken(token) && !language.tokenIds().contains(token.id())) {
                String msgPrefix = "Invalid TokenId=" + token.id()
                        + " returned from lexer="
                        + lexer + " for language=" + language + ":\n";
                if (token.id().ordinal() > language.maxOrdinal()) {
                    throw new IllegalStateException(msgPrefix +
                            "Language.maxOrdinal()=" + language.maxOrdinal() + " < " + token.id().ordinal());
                } else { // Ordinal ok but different id with that ordinal contained in language
                    throw new IllegalStateException(msgPrefix +
                            "Language contains no or different tokenId with ordinal="
                            + token.id().ordinal() + ": " + language.tokenId(token.id().ordinal()));
                }
            }
            // Skip token's chars
            tokenStartOffset += tokenLength;
            if (!isSkipToken(token))
                return token;
        } // Continue to fetch non-skip token
    }

    /**
     * Used by token list updater after nextToken() to determine start offset of a token 
     * to be recognized next. Overriden for join token lists since join tokens
     * may span multiple ETLs.
     * 
     * @return start offset of a next token that would be recognized.
     */
    public int lastTokenEndOffset() {
        return tokenStartOffset;
    }

    public AbstractToken<T> getFlyweightToken(T id, String text) {
        assert (text.length() <= readLength());
        // Compare each recognized char with the corresponding char in text
        if (LOG.isLoggable(Level.FINE)) {
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != readExisting(i)) {
                    throw new IllegalArgumentException("Flyweight text in " + // NOI18N
                            "TokenFactory.getFlyweightToken(" + id + ", \"" + // NOI18N
                            CharSequenceUtilities.debugText(text) + "\") " + // NOI18N
                            "differs from recognized text: '" + // NOI18N
                            CharSequenceUtilities.debugChar(readExisting(i)) +
                            "' != '" + CharSequenceUtilities.debugChar(text.charAt(i)) + // NOI18N
                            "' at index=" + i // NOI18N
                    );
                }
            }
        }

        assignTokenLength(text.length());
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            if (isFlyTokenAllowed()) {
                token = innerLanguageOperation.getFlyweightToken(id, text);
                flyTokenSequenceLength++;
            } else { // Create regular token
                token = createDefaultTokenInstance(id);
                fillTokenData(token);
                flyTokenSequenceLength = 0;
            }
        }
        return token;
    }
    
    private AbstractToken<T> checkSkipToken(T id) {
        if (isSkipTokenId(id)) {
            // Prevent fly token occurrence after skip token to have a valid offset
            flyTokenSequenceLength = LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH;
            return skipToken();
        }
        return null;
    }
    
    public AbstractToken<T> createToken(T id, int length) {
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createDefaultTokenInstance(id);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }

    protected AbstractToken<T> createDefaultTokenInstance(T id) {
        return new DefaultToken<T>(id, tokenLength);
    }

    public AbstractToken<T> createToken(T id, int length, PartType partType) {
        if (partType == null)
            throw new IllegalArgumentException("partType must be non-null");
        if (partType == PartType.COMPLETE)
            return createToken(id, length);

        return createPropertyToken(id, length, null, partType);
    }

    public AbstractToken<T> createPropertyToken(T id, int length,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        if (partType == null)
            partType = PartType.COMPLETE;
        
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createPropertyTokenInstance(id, propertyProvider, partType);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }

    protected AbstractToken<T> createPropertyTokenInstance(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        return new PropertyToken<T>(id, tokenLength, propertyProvider, partType);
    }

    public AbstractToken<T> createCustomTextToken(T id, int length, CharSequence customText) {
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createCustomTextTokenInstance(id, customText);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }
    
    protected AbstractToken<T> createCustomTextTokenInstance(T id, CharSequence customText) {
        return new CustomTextToken<T>(id, customText, tokenLength);
    }

    public boolean isSkipTokenId(T id) {
        Set<T> skipTokenIds = tokenList.skipTokenIds();
        return (skipTokenIds != null && skipTokenIds.contains(id));
    }

    protected final int tokenLength() {
        return tokenLength;
    }

    public void assignTokenLength(int tokenLength) {
        if (tokenLength > readLength()) {
            throw new IndexOutOfBoundsException("tokenLength=" + tokenLength // NOI18N
                    + " >" + readLength());
        }
        this.tokenLength = tokenLength;
    }
    
    public final Object lexerState() {
        return lexer.state();
    }

    protected boolean isFlyTokenAllowed() {
        return (flyTokenSequenceLength < LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH);
    }
    
    public final boolean isSkipToken(AbstractToken<T> token) {
        return (token == LexerUtilsConstants.SKIP_TOKEN);
    }
    
    @SuppressWarnings("unchecked")
    public final AbstractToken<T> skipToken() {
        return (AbstractToken<T>)LexerUtilsConstants.SKIP_TOKEN;
    }

    /**
     * Release the underlying lexer. This method can be called multiple times.
     */
    public final void release() {
        if (lexer != null) {
            lexer.release();
            lexer = null;
        }
    }
    
    /**
     * Check that there are no more characters to be read from the given
     * lexer input operation.
     */
    private void checkLexerInputFinished() {
        if (read() != LexerInput.EOF) {
            throw new IllegalStateException(
                "Lexer " + lexer + // NOI18N
                " returned null token" + // NOI18N
                " but EOF was not read from lexer input yet." + // NOI18N
                " Fix the lexer."// NOI18N
            );
        }
        if (readLength() > 0) {
            throw new IllegalStateException(
                "Lexer " + lexer + // NOI18N
                " returned null token but lexerInput.readLength()=" + // NOI18N
                readLength() +
                " - these characters need to be tokenized." + // NOI18N
                " Fix the lexer." // NOI18N
            );
        }
    }

    @Override
    public String toString() {
        return "tokenStartOffset=" + tokenStartOffset + ", readOffset=" + readOffset + // NOI18N
                ", lookaheadOffset=" + lookaheadOffset;
    }

}
