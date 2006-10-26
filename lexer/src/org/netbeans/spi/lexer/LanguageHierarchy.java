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

package org.netbeans.spi.lexer;

import java.util.Collection;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.CharPreprocessorOperation;
import org.netbeans.lib.lexer.CharProvider;
import org.netbeans.lib.lexer.LanguageOperation;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TokenIdImpl;

/**
 * Definition of a language, its lexer and its embedded languages.
 * <br/>
 * It's a mirror of {@link Language} on SPI level containing
 * additional information necessary for the lexer infrastructure operation.
 * <br/>
 * The language hierarchies should be implemented by SPI providers
 * and their languages should be given for public use
 * (language hierarchy classes do not need to be public though).
 * <br/>
 * A typical situation may look like this:<pre>
 *
 * public enum MyTokenId implements TokenId {
 *
 *     ERROR(null, "error"),
 *     IDENTIFIER(null, "identifier"),
 *     ABSTRACT("abstract", "keyword"),
 *     ...
 *     SEMICOLON(";", "separator"),
 *     ...
 *
 *
 *     private final String fixedText; // Used by lexer for production of flyweight tokens
 *
 *     private final String primaryCategory;
 *
 *     MyTokenId(String fixedText, String primaryCategory) {
 *         this.fixedText = fixedText;
 *         this.primaryCategory = primaryCategory;
 *     }
 *
 *     public String fixedText() {
 *         return fixedText;
 *     }
 *
 *     public String primaryCategory() {
 *         return primaryCategory;
 *     }
 *
 *
 *     private static final Language&lt;MyTokenId&gt; language = new LanguageHierarchy&lt;MyTokenId&gt;() {
 *         protected String mimeType() {
 *             return "text/x-my";
 *         }
 *
 *         protected Collection&lt;MyTokenId&gt; createTokenIds() {
 *             return EnumSet.allOf(MyTokenId.class);
 *         }
 *
 *         protected Lexer&lt;MyTokenId&gt; createLexer(LexerInput input, TokenFactory&lt;MyTokenId&gt; tokenFactory, Object state) {
 *             return new MyLexer(input, tokenFactory, state);
 *         }
 *
 *     }.language();
 *
 *     public static Language&lt;MyTokenId&gt; language() {
 *         return language;
 *     }
 *
 * }
 * </pre>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LanguageHierarchy<T extends TokenId> {
    
    static {
        LexerSpiPackageAccessor.register(new Accessor());
    }

    /**
     * Create a default token id instance in case the token ids
     * are generated (not created by enum class).
     */
    public static TokenId newId(String name, int ordinal) {
        return newId(name, ordinal, null);
    }

    /**
     * Create a default token id instance in case the token ids
     * are generated (not created by enum class).
     */
    public static TokenId newId(String name, int ordinal, String primaryCategory) {
        return new TokenIdImpl(name, ordinal, primaryCategory);
    }

    
    /** Operation containing impls of additional services for this language hierarchy. */
    LanguageOperation<T> operation = new LanguageOperation<T>(this);

    /**
     * Provide a collection of token ids that comprise the language.
     * <br>
     * If token ids are defined as enums then this method
     * should simply return <code>EnumSet.allOf(MyTokenId.class)</code>.
     *
     * <p>
     * This method is only called once by the infrastructure
     * (when constructing language) so it does
     * not need to cache its result.
     * <br>
     * This method is called in synchronized section.
     * If its implementation would use any synchronization
     * a care must be taken to prevent deadlocks.
     * </p>
     *
     * @return non-null collection of {@link TokenId} instances.
     */
    protected abstract Collection<T> createTokenIds();

    /**
     * Provide map of token category names to collection of its members.
     * <br/>
     * The results of this method will be merged with the primary-category
     * information found in token ids.
     * <br>
     * This method is only called once by the infrastructure
     * (when constructing language) so it does
     * not need to cache its result.
     * <br>
     * This method is called in synchronized section.
     * If its implementation would use any synchronization
     * a care must be taken to prevent deadlocks.
     *
     *  <p>
     *  There is a convention that the category names should only consist
     *  of lowercase letters, numbers and hyphens.
     *
     * @return mapping of category name to collection of its ids.
     *  It may return null to signal no mappings.
     */
    protected Map<String,Collection<T>> createTokenCategories() {
        return null; // no extra categories
    }

    /**
     * Create lexer prepared for returning tokens
     * from subsequent calls to {@link Lexer#nextToken()}.
     *
     * @param info non-null lexer restart info containing the information
     *  necessary for lexer restarting.
     */
    protected abstract Lexer<T> createLexer(LexerRestartInfo<T> info);
    
    /**
     * Gets the mime type of the language constructed from this language hierarchy.
     *
     * @return non-null language's mime type.
     * @see org.netbeans.api.lexer.LanguagePath#mimePath()
     */
    protected abstract String mimeType();
    
    /**
     * Get language embedding (if exists) for a particular token
     * of the language at this level of language hierarchy.
     * <br>
     * This method will only be called if the given token instance
     * will not be flyweight token or token with custom text:
     * <code>token.isFlyweight() == false && token.isCustomText() == false</code>
     * <br>
     * That restriction exists because the children token list is constructed
     * lazily and the infrastructure needs to access the token's parent token
     * list which would not be possible if the token would be flyweight.
     *
     * @param token non-null token for which the language embedding will be resolved.
     * @param tokenComplete true if the given token was completely lexed. False
     *  in case when just the initial token part is known and the embedded lexer
     *  will influence where the token will physically end.
     *
     * @param languagePath non-null language path at which the language embedding
     *  is being created. It may be used for obtaining appropriate information
     *  from inputAttributes.
     *
     * @param inputAttributes input attributes that could affect the embedding creation.
     *  It may be null if there are no extra attributes.
     *
     * @return language embedding instance or null if there is no language embedding
     *  for this token.
     */
    protected LanguageEmbedding embedding(Token<T> token, boolean tokenComplete,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        return null; // No extra hardcoded embedding by default
    }
    
    /**
     * Create character preprocessor that translates certain character sequences
     * into characters (for example Unicode escape sequences).
     *
     * @return valid preprocessor or null if there is no extra preprocessor.
     */
    protected CharPreprocessor createCharPreprocessor() {
        return null; // no preprocessor by default
    }
    
    /**
     * Create token validator for the given token id.
     *
     * @param tokenId token id for which the token validator should be returned.
     * @return valid token validator or null if there is no validator
     *  for the given token id.
     */
    protected TokenValidator<T> createTokenValidator(T tokenId) {
        return null;
    }

    /**
     * Determine whether the text of the token with the particular id should
     * be retained after the token has been removed from the token list
     * because of the underlying mutable input source modification.
     * <br/>
     * {@link org.netbeans.api.lexer.Token#text()} will continue
     * to return the value that it had right before the token's removal.
     * <br/>
     * This may be useful if the tokens are held directly in parse trees
     * and the parser queries the tokens for text.
     *
     * <p>
     * Retaining text in the tokens has performance and memory implications
     * and should only be done selectively for tokens where it's desired
     * (such as identifiers).
     * <br/>
     * The extra performance and memory penalty only happens during
     * token's removal from the token list for the given input.
     * Token creation performance and memory consumption during
     * token's lifetime stay unaffected.
     * </p>
     *
     * <p>
     * Retaining will only work if the input source is capable of providing
     * the removed text right after the modification has been performed.
     * </p>
     *
     * @return true if the text should be retained or false if not.
     */
    protected boolean isRetainTokenText(T tokenId) {
        return false;
    }


    /**
     * Get description of the language constructed for this language hierarchy
     * based on token ids and token categories provided.
     *
     * @return non-null description of the language.
     */
    public final Language<T> language() {
        return operation.language();
    }
    
    /** Enforce default implementation of <code>hashCode()</code>. */
    public final int hashCode() {
        return super.hashCode();
    }
    
    /** Enforce default implementation of <code>equals()</code>. */
    public final boolean equals(Object o) {
        return super.equals(o);
    }

    public String toString() {
        return getClass().getName(); // for debugging purposes only
    }
    
    /** Implementation of lexer spi package accessor. */
    private static final class Accessor extends LexerSpiPackageAccessor {
        
        public <T extends TokenId> Collection<T> createTokenIds(LanguageHierarchy<T> languageHierarchy) {
            return languageHierarchy.createTokenIds();
        }

        public <T extends TokenId> Map<String,Collection<T>> createTokenCategories(LanguageHierarchy<T> languageHierarchy) {
            return languageHierarchy.createTokenCategories();
        }

        public String mimeType(LanguageHierarchy languageHierarchy) {
            return languageHierarchy.mimeType();
        }

        public LanguageOperation operation(LanguageHierarchy languageHierarchy) {
            return languageHierarchy.operation;
        }
        
        @SuppressWarnings("unchecked")
        public LanguageEmbedding embedding(LanguageHierarchy languageHierarchy,
        Token token, boolean tokenComplete,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return languageHierarchy.embedding(token, tokenComplete, languagePath, inputAttributes);
        }

        @SuppressWarnings("unchecked")
        public Lexer createLexer(LanguageHierarchy languageHierarchy, LexerRestartInfo info) {
            return languageHierarchy.createLexer(info);
        }

        public <T extends TokenId> LexerRestartInfo<T> createLexerRestartInfo(
        LexerInput input, TokenFactory<T> tokenFactory, Object state,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return new LexerRestartInfo<T>(input, tokenFactory, state, languagePath, inputAttributes);
        }

        @SuppressWarnings("unchecked")
        public TokenValidator createTokenValidator(LanguageHierarchy languageHierarchy, TokenId id) {
            return languageHierarchy.createTokenValidator(id);
        }

        @SuppressWarnings("unchecked")
        public boolean isRetainTokenText(LanguageHierarchy languageHierarchy, TokenId id) {
            return languageHierarchy.isRetainTokenText(id);
        }

        public CharPreprocessor createCharPreprocessor(LanguageHierarchy languageHierarchy) {
            return languageHierarchy.createCharPreprocessor();
        }

        public LexerInput createLexerInput(CharProvider charProvider) {
            return new LexerInput(charProvider);
        }

        public void init(CharPreprocessor preprocessor, CharPreprocessorOperation operation) {
            preprocessor.init(operation);
        }

        public void preprocessChar(CharPreprocessor preprocessor) {
            preprocessor.preprocessChar();
        }

        public Language<? extends TokenId> language(MutableTextInput<?> mti) {
            return mti.language();
        }

        public CharSequence text(MutableTextInput mti) {
            return mti.text();
        }

        public InputAttributes inputAttributes(MutableTextInput mti) {
            return mti.inputAttributes();
        }

        public Object inputSource(MutableTextInput mti) {
            return mti.inputSource();
        }

        @SuppressWarnings("unchecked")
        public TokenFactory createTokenFactory(LexerInputOperation lexerInputOperation) {
            return new TokenFactory(lexerInputOperation);
        }

    }

}
