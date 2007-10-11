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

package org.netbeans.modules.web.core.syntax.formatting;

import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter;
import static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter.JoinedTokenSequence;
import org.netbeans.modules.web.core.syntax.deprecated.JspMultiSyntax;

/**
 * A lexer-based formatter for html files.
 * @author Tomasz.Slota@Sun.COM
 */

public class JSPLexerFormatter extends TagBasedLexerFormatter {

    /** Creates a new instance of HTMLFormater */
    public JSPLexerFormatter(Class kitClass) {
        super(kitClass);
    }

    @Override
    protected boolean acceptSyntax(Syntax syntax) {
        return syntax instanceof JspMultiSyntax;
    }

    @Override
    protected int getTagEndingAtPosition(JoinedTokenSequence tokenSequence, int position) throws BadLocationException {
        if (position >= 0) {
            int originalOffset = tokenSequence.offset();
            tokenSequence.move(position);
            tokenSequence.moveNext();
            Token token = tokenSequence.token();

            if (token.id() == JspTokenId.TAG && !token.text().toString().endsWith("/>")) {
                //NOI18N

                int r = tokenSequence.offset();
                tokenSequence.move(originalOffset);
                tokenSequence.moveNext();
                return r;
            }
            
            tokenSequence.move(originalOffset);
            tokenSequence.moveNext();
        }
        return -1;
    }
    
    @Override
    protected int getTagEndOffset(JoinedTokenSequence tokenSequence, int tagStartOffset) {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(tagStartOffset);
        tokenSequence.moveNext();
        
        String tokenTxt = tokenSequence.token().text().toString();
        
        int r = -1;
        boolean thereAreMoreTokens = true;
        
        if (tokenTxt.endsWith(">")) { //NOI18N
            r = tokenTxt.length() + tagStartOffset;
        } else {
            
            do {
                thereAreMoreTokens &= tokenSequence.moveNext();
            }
            while (thereAreMoreTokens && !isJSPTagCloseSymbol(tokenSequence.token()));

            r = tokenSequence.offset() + tokenSequence.token().length();
        }
        
        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return thereAreMoreTokens ? r : -1;
    }

    @Override
    protected boolean isJustBeforeClosingTag(JoinedTokenSequence tokenSequence, int tagTokenOffset) throws BadLocationException {
        // a workaround for the difference with XML syntax support
        return super.isJustBeforeClosingTag(tokenSequence, tagTokenOffset + "</".length()); //NOI18N
    }

    @Override
    protected boolean isClosingTag(JoinedTokenSequence tokenSequence, int tagOffset) {
        Token token = getTokenAtOffset(tokenSequence, tagOffset);
        return token != null && token.id() == JspTokenId.ENDTAG;
    }

    @Override
    protected boolean isOpeningTag(JoinedTokenSequence tokenSequence, int tagOffset) {
        Token token = getTokenAtOffset(tokenSequence, tagOffset);
        return token != null && token.id() == JspTokenId.TAG;
    }

    @Override
    protected String extractTagName(JoinedTokenSequence tokenSequence, int tagOffset) {
        Token token = getTokenAtOffset(tokenSequence, tagOffset);
        return token.text().toString();
    }

    @Override
    protected boolean areTagNamesEqual(String tagName1, String tagName2) {
        return tagName1.equalsIgnoreCase(tagName2);
    }

    @Override
    protected int getOpeningSymbolOffset(JoinedTokenSequence tokenSequence, int tagTokenOffset) {
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(tagTokenOffset);
        boolean thereAreMoreTokens = true;

        do {
            thereAreMoreTokens = tokenSequence.movePrevious();
        } while (thereAreMoreTokens && tokenSequence.token().id() != JspTokenId.TAG); // HTMLTokenId.TAG_OPEN_SYMBOL);
        if (thereAreMoreTokens) {
            tokenSequence.movePrevious(); //move to the tag opening symbol </ or <
            int r = tokenSequence.offset();
            tokenSequence.move(originalOffset);
            tokenSequence.moveNext();
            return r;
        }

        tokenSequence.move(originalOffset);
        tokenSequence.moveNext();
        return -1;
    }

    @Override
    protected boolean isClosingTagRequired(BaseDocument doc, String tagName) {
        return true;
    }

    @Override
    protected boolean isUnformattableToken(JoinedTokenSequence tokenSequence, int tagTokenOffset) {
        Token token = getTokenAtOffset(tokenSequence, tagTokenOffset);
        
        if (token == null){
            System.err.println("This should not happen!!");
        }

        if (token.id() == JspTokenId.COMMENT) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean isUnformattableTag(String tag) {
        return false;
    }
    
    protected LanguagePath supportedLanguagePath() {
        return LanguagePath.get(JspTokenId.language());
    }
    
    private boolean isJSPTagCloseSymbol(Token token){
        if (token.id() != JspTokenId.SYMBOL){
            return false;
        }
        
        String img = token.text().toString();
        return img.equals(">") || img.equals("/>") || img.equals("%>"); //NOI18N
    }
}
