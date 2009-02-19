/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 * Macro expanded token processor.
 *
 * @author Nick Krasilnikov
 */
public final class CsmExpandedTokenProcessor implements CndTokenProcessor<Token<CppTokenId>> {

    private final CndTokenProcessor<Token<CppTokenId>> tp;
    private final Document doc;
    private final int offset;
    private boolean inMacro;
    private final CsmFile file;
    private List<CsmReference> macros;

    private CsmExpandedTokenProcessor(Document doc, CsmFile file, CndTokenProcessor<Token<CppTokenId>> tp, int offset, List<CsmReference> macros) {
        this.tp = tp;
        this.doc = doc;
        this.offset = offset;
        this.file = file;
        this.macros = macros;
    }

    public static CndTokenProcessor<Token<CppTokenId>> create(Document doc, CndTokenProcessor<Token<CppTokenId>> tp, int offset) {
        if (doc != null) {
            CsmFile file = CsmUtilities.getCsmFile(doc, true);
            if (file != null) {
                List<CsmReference> macros = CsmFileInfoQuery.getDefault().getMacroUsages(file);
                if (macros != null) {
                    return create(doc, file, tp, offset, macros);
                }
            }
        }
        return tp;
    }

    public static CndTokenProcessor<Token<CppTokenId>> create(Document doc, CsmFile file, CndTokenProcessor<Token<CppTokenId>> tp, int offset, List<CsmReference> macros) {
        CsmMacroExpansion.expand(doc, file, 0, 0);
        return new CsmExpandedTokenProcessor(doc, file, tp, offset, macros);
    }

    public void start(int startOffset, int firstTokenOffset) {
        tp.start(startOffset, firstTokenOffset);
    }

    public void end(int offset, int lastTokenOffset) {
        tp.end(offset, lastTokenOffset);
    }

    public boolean isStopped() {
        return tp.isStopped();
    }

    public boolean isInMacro() {
        return inMacro;
    }

    public boolean isMacro(Token token, int tokenOffset) {
        return Character.isJavaIdentifierStart(token.text().charAt(0)) && ReferencesSupport.findMacro(macros, tokenOffset) != null;
    }

    public boolean token(Token<CppTokenId> token, int tokenOffset) {
        // Additional logic only for macros
        if (isMacro(token, tokenOffset) || inMacro) {
            TokenSequence<CppTokenId> expTS = null;
            String expansion = CsmMacroExpansion.expand(doc, file, tokenOffset, tokenOffset + token.length());
            if (expansion != null) {
                if (expansion.equals("")) { // NOI18N
                    if (offset == -1 || tokenOffset + token.length() < offset) {
                        return false;
                    }
                } else if (inMacro) {
                    inMacro = false;
                } else {
                    inMacro = true;
                    TokenHierarchy<String> hi = TokenHierarchy.create(expansion, CndLexerUtilities.getLanguage(doc));
                    List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(tokenOffset + token.length(), true);
                    // Go from inner to outer TSes
                    for (int i = tsList.size() - 1; i >= 0; i--) {
                        TokenSequence<?> ts = tsList.get(i);
                        final Language<?> lang = ts.languagePath().innerLanguage();
                        if (CndLexerUtilities.isCppLanguage(lang, false)) {
                            @SuppressWarnings("unchecked") // NOI18N
                            TokenSequence<CppTokenId> uts = (TokenSequence<CppTokenId>) ts;
                            expTS = uts;
                        }
                    }
                    if (expTS != null) {
                        expTS.moveStart();
                        if (expTS.moveNext()) {
                            boolean res;
                            Token<CppTokenId> expToken = expTS.token();
                            if (!expTS.moveNext()) {
                                if (expToken.text().toString().equals(token.text().toString()) &&
                                        expToken.id().equals(token.id())) {
                                    res = processMacroToken(token, tokenOffset);
                                } else {
                                    res = processMacroToken(expToken, tokenOffset);
                                }
                            } else {
                                res = processMacroToken(expToken, tokenOffset);
                                res = processMacroToken(expTS.token(), tokenOffset);
                                while (expTS.moveNext()) {
                                    res = processMacroToken(expTS.token(), tokenOffset);
                                }
                            }
                            return res;
                        }
                    }
                }
            }
        }
        if (!isWhitespace(token)) {
            inMacro = false;
        }
        return tp.token(token, tokenOffset);
    }

    private boolean processMacroToken(Token<CppTokenId> token, int tokenOffset) {
        int macroOffset = tokenOffset;
        if (offset != -1 && macroOffset + token.length() >= offset) {
            macroOffset = offset - token.length() - 1;
        }
        return tp.token(token, macroOffset);
    }

    private boolean isWhitespace(Token<CppTokenId> docToken) {
        switch (docToken.id()) {
            case NEW_LINE:
            case WHITESPACE:
            case ESCAPED_WHITESPACE:
            case ESCAPED_LINE:
                return true;
            default:
                return false;
        }
    }
}
