/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.nbbridge.test.simple;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum SimpleCharTokenId implements TokenId {

    CHARACTER,
    DIGIT;

    SimpleCharTokenId() {
    }

    public String primaryCategory() {
        return "chars";
    }

    public static final String MIME_TYPE = "text/x-simple-char";
    
    private static final LanguageDescription<SimpleCharTokenId> language
    = new LanguageHierarchy<SimpleCharTokenId>() {

        protected Collection<SimpleCharTokenId> createTokenIds() {
            return EnumSet.allOf(SimpleCharTokenId.class);
        }
        
        public Lexer<SimpleCharTokenId> createLexer(
        LexerInput input, TokenFactory<SimpleCharTokenId> tokenFactory, Object state,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return new SimpleCharLexer(input, tokenFactory, state, languagePath, inputAttributes);
        }

        public LanguageEmbedding embedding(
        Token<SimpleCharTokenId> token, boolean tokenComplete,
        LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        public String mimeType() {
            return MIME_TYPE;
        }
        
    }.language();

    public static LanguageDescription<SimpleCharTokenId> description() {
        return language;
    }

}
