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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.Utilities;

/**
 *
 * @author Vladimir Voskresensky
 * based on idea of org.netbeans.modules.editor.java.CamelCaseOperations
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(JTextComponent textComponent, boolean skipEOL) throws BadLocationException {

        // get current caret position
        int offset = textComponent.getCaretPosition();

        // get token chain at the offset
        TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(textComponent, offset, true, false);
        if (ts != null) {
            Token<CppTokenId> token = ts.token();
            // is this an identifier or include strings,
            // btw we are sure this is not the end of token
            // due to false as last param of getCppTokenSequence call
            String category = token.id().primaryCategory();
            if (CppTokenId.IDENTIFIER_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_USER_INCLUDE_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_SYS_INCLUDE_CATEGORY.equals(category)) {
                CharSequence image = token.text();
                if (image != null && image.length() > 0) {
                    int length = image.length();
                    int offsetInImage = offset - ts.offset();
                    int start = offsetInImage + 1;
                    if (Character.isUpperCase(image.charAt(offsetInImage))) {
                        // if starting from a Uppercase char, first skip over follwing upper case chars
                        for (int i = start; i < length; i++) {
                            char charAtI = image.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                break;
                            }
                            start++;
                        }
                    }
                    for (int i = start; i < length; i++) {
                        char charAtI = image.charAt(i);
                        if (Character.isUpperCase(charAtI)) {
                            // return offset of next uppercase char in the identifier
                            return ts.offset() + i;
                        }
                    }
                    return ts.offset() + length;
                }
            } else if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) { // NOI18N
                int endWS;
                // skip all whitespaces
                do {
                    endWS = ts.offset() + ts.token().length();
                    if (!skipEOL && ts.token().id() == CppTokenId.NEW_LINE) {
                        break;
                    }
                } while (ts.moveNext() && CppTokenId.WHITESPACE_CATEGORY.equals(ts.token().id().primaryCategory()));
                return endWS;
            }
        }

        // not recognized situation - simply return next word offset
        return Utilities.getNextWord(textComponent, offset);
    }

    static int previousCamelCasePosition(JTextComponent textComponent) throws BadLocationException {
        // get current caret position
        int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document
        if (offset == 0) {
            return 0;
        }
        TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(textComponent, offset, true, true);
        if (ts != null) {
            Token<CppTokenId> token = ts.token();
            // is this an identifier of include strings
            String category = token.id().primaryCategory();
            if (CppTokenId.IDENTIFIER_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_USER_INCLUDE_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_SYS_INCLUDE_CATEGORY.equals(category)) {
                CharSequence image = token.text();
                if (image != null && image.length() > 0) {
                    int offsetInImage = offset - 1 - ts.offset();
                    // go back to the first upper case character
                    int upperCaseOffset = offsetInImage - 1;
                    for (; upperCaseOffset > 0; upperCaseOffset--) {
                        if (Character.isUpperCase(image.charAt(upperCaseOffset))) {
                            break;
                        }
                    }
                    if (upperCaseOffset > 0 && Character.isUpperCase(image.charAt(upperCaseOffset))) {
                        // skip over previous uppercase chars in the identifier
                        for (int i = upperCaseOffset - 1; i >= 0; i--) {
                            char charAtI = image.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                // return offset of previous uppercase char in the identifier
                                return ts.offset() + i + 1;
                            }
                        }
                    }
                    return ts.offset();
                }
            } else if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) { // NOI18N
                int wsOffset = ts.offset();
                while (ts.movePrevious() && CppTokenId.WHITESPACE_CATEGORY.equals(ts.token().id().primaryCategory())) {
                    wsOffset = ts.offset();
                    if (wsOffset == 0) {
                        //at the very beginning of a file
                        return 0;
                    }
                }
                return wsOffset;
            }
        }

        // not recognized situation - simply return previous word offset
        return Utilities.getPreviousWord(textComponent, offset);
    }
}
