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

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestCharTokenId;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.*;
import org.netbeans.lib.lexer.lang.TestChangingTokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author vita
 */
public class SimpleLanguageProvider extends LanguageProvider {
    
    private static SimpleLanguageProvider instance = null;
    
    public static void fireLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_LANGUAGE);
    }
    
    public static void fireTokenLanguageChange() {
        assert instance != null : "There is no SimpleLanguageProvider instance.";
        instance.firePropertyChange(PROP_EMBEDDED_LANGUAGE);
    }
    
    /** Creates a new instance of SimpleLanguageProvider */
    public SimpleLanguageProvider() {
        assert instance == null : "Multiple instances of DummyLanguageProvider detected";
        instance = this;
    }

    public Language<? extends TokenId> findLanguage(String mimePath) {
        if (LanguageManagerTest.MIME_TYPE_KNOWN.equals(mimePath)) {
            return new LH().language();
        } else if (TestChangingTokenId.MIME_TYPE.equals(mimePath)) {
            return TestChangingTokenId.language();
        }
        return null;
    }

    public LanguageEmbedding<? extends TokenId> findLanguageEmbedding(
    Token<? extends TokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
        if ("text/x-simple-plain".equals(languagePath.mimePath()) && token.id().name().equals("WORD")) {
            return LanguageEmbedding.create(TestCharTokenId.language(), 0, 0);
        } else {
            return null;
        }
    }
    
    private static final class LH extends LanguageHierarchy<TokenId> {
        protected Collection<TokenId> createTokenIds() {
            return Collections.emptyList();
        }

        protected Lexer<TokenId> createLexer(LexerRestartInfo<TokenId> info) {
            return null;
        }

        protected String mimeType() {
            return LanguageManagerTest.MIME_TYPE_KNOWN;
        }

    } // End of LD class
}
