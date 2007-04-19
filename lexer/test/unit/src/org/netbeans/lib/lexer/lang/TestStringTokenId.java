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
import java.util.Map;
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
 * Token ids for simple string embedding - copied from JavaStringTokenId
 * for java string language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestStringTokenId implements TokenId {

    TEXT("string"),
    BACKSPACE("string-escape"),
    FORM_FEED("string-escape"),
    NEWLINE("string-escape"),
    CR("string-escape"),
    TAB("string-escape"),
    SINGLE_QUOTE("string-escape"),
    DOUBLE_QUOTE("string-escape"),
    BACKSLASH("string-escape"),
    OCTAL_ESCAPE("string-escape"),
    OCTAL_ESCAPE_INVALID("string-escape-invalid"),
    ESCAPE_SEQUENCE_INVALID("string-escape-invalid");

    private final String primaryCategory;

    TestStringTokenId() {
        this(null);
    }

    TestStringTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestStringTokenId> language
    = new LanguageHierarchy<TestStringTokenId>() {
        @Override
        protected Collection<TestStringTokenId> createTokenIds() {
            return EnumSet.allOf(TestStringTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestStringTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        public Lexer<TestStringTokenId> createLexer(LexerRestartInfo<TestStringTokenId> info) {
            return new TestStringLexer(info);
        }

        @Override
        public LanguageEmbedding<? extends TokenId> embedding(
        Token<TestStringTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        protected String mimeType() {
            return "text/x-simple-string";
        }
    }.language();

    public static Language<TestStringTokenId> language() {
        return language;
    }

    public void release() {
    }

}
