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
        public LanguageEmbedding<?> embedding(
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
