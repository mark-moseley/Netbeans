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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.lang;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Embedded language for join sections testing.
 *
 * @author mmetelka
 */
public enum TestJoinSectionsTextTokenId implements TokenId {
    
    TEXT(), // Text except of text within braces
    BRACES(); // "{ ... }" i.e. text within braces

    private TestJoinSectionsTextTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    private static final Language<TestJoinSectionsTextTokenId> language
    = new LanguageHierarchy<TestJoinSectionsTextTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-join-sections-text";
        }

        @Override
        protected Collection<TestJoinSectionsTextTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinSectionsTextTokenId.class);
        }

        @Override
        protected Lexer<TestJoinSectionsTextTokenId> createLexer(LexerRestartInfo<TestJoinSectionsTextTokenId> info) {
            return new TestJoinSectionsTextLexer(info);
        }
        
        @Override
        public LanguageEmbedding<? extends TokenId> embedding(
        Token<TestJoinSectionsTextTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
//                case TEXT:
//                    return LanguageEmbedding.create(TestStringTokenId.language(), 1, 1);
            }
            return null; // No embedding
        }

    }.language();

    public static Language<TestJoinSectionsTextTokenId> language() {
        return language;
    }

}
