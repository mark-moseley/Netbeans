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
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestSaveTokensInLATokenId implements TokenId {
    
    A, // matches "a"; also checks for "b" and "c"
    B, // matches "b"; also checks for "c"
    C, // matches "c"
    TEXT; // other text

    TestSaveTokensInLATokenId() {
    }

    public String primaryCategory() {
        return null;
    }

    private  static final Language<TestSaveTokensInLATokenId> language
    = new LanguageHierarchy<TestSaveTokensInLATokenId>() {

        @Override
        protected Collection<TestSaveTokensInLATokenId> createTokenIds() {
            return EnumSet.allOf(TestSaveTokensInLATokenId.class);
        }
        
        @Override
        public Lexer<TestSaveTokensInLATokenId> createLexer(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            return new LexerImpl(info);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(
        Token<TestSaveTokensInLATokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-simple-plain";
        }
        
    }.language();

    public static Language<TestSaveTokensInLATokenId> language() {
        return language;
    }

    private static final class LexerImpl implements Lexer<TestSaveTokensInLATokenId> {

        private static final int EOF = LexerInput.EOF;

        private LexerInput input;

        private TokenFactory<TestSaveTokensInLATokenId> tokenFactory;

        public LexerImpl(LexerRestartInfo<TestSaveTokensInLATokenId> info) {
            this.input = info.input();
            this.tokenFactory = info.tokenFactory();
            assert (info.state() == null); // passed argument always null
        }

        public Object state() {
            return null;
        }

        public Token<TestSaveTokensInLATokenId> nextToken() {
            int ch = input.read();
            switch (ch) {
                case 'a': // check for 'b' and 'c'
                    if (input.read() == 'b') {
                        if (input.read() == 'c') { // just check for "c"
                        }
                        input.backup(1);
                    }
                    input.backup(1);
                    return tokenFactory.createToken(A);

                case 'b':
                    if (input.read() == 'c') { // just check for "c"
                    }
                    input.backup(1);
                    return tokenFactory.createToken(B);
                    
                case 'c':
                    return tokenFactory.createToken(C);

                case EOF:
                    return null;

                default:
                    return tokenFactory.createToken(TEXT);
            }
        }

        private Token<TestSaveTokensInLATokenId> token(TestSaveTokensInLATokenId id) {
            return tokenFactory.createToken(id);
        }

        public void release() {
        }

    }

}
