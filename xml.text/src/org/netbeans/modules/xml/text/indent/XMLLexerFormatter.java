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
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence;
import org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter;
import org.netbeans.modules.xml.text.folding.TokenElement;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;
import org.netbeans.modules.xml.text.syntax.XMLKit;

/**
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLLexerFormatter extends TagBasedLexerFormatter {

    private static final String TAG_OPENING_PREFIX = "<"; //NOI18N
    private static final String TAG_CLOSING_PREFIX = "</"; //NOI18N
    
    private final LanguagePath languagePath;

    public XMLLexerFormatter(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }

    @Override
    protected boolean isOpeningTag(JoinedTokenSequence jts, int tagTokenOffset) {        
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        return token != null
                && token.id() == XMLTokenId.TAG
                && token.text().toString().startsWith(TAG_OPENING_PREFIX)
                && !token.text().toString().startsWith(TAG_CLOSING_PREFIX);
    }

    @Override
    protected boolean isClosingTag(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        return token != null
                && token.id() == XMLTokenId.TAG
                && token.text().toString().startsWith(TAG_CLOSING_PREFIX);
    }

    @Override
    protected boolean areTagNamesEqual(String tagName1, String tagName2) {
        return tagName1.equalsIgnoreCase(tagName2);
    }

    @Override
    protected boolean isClosingTagRequired(BaseDocument doc, String tagName) {
        return true;
    }

    @Override
    protected boolean isUnformattableToken(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        if (token.id() == XMLTokenId.BLOCK_COMMENT ||
            token.id() == XMLTokenId.CDATA_SECTION) {
            return true;
        }
        
        return false;
    }

    @Override
    protected boolean isUnformattableTag(String tag) {
        return false;
    }

    @Override
    protected boolean isTopLevelLanguage(BaseDocument doc) {
        return true;
    }

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }
    
    @Override
    protected String extractTagName(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);        
        String tagImage = token.text().toString();
        int startIndex = -1;
        
        if (isOpeningTag(jts, tagTokenOffset)) {
            startIndex = TAG_OPENING_PREFIX.length();
        } else if (isClosingTag(jts, tagTokenOffset)) {
            startIndex = TAG_CLOSING_PREFIX.length();
        }
        
        if (startIndex >= 0) {
            String tagName = tagImage.substring(startIndex);
            return tagName;
        }
        
        return null;        
    }
    
    @Override
    protected int getTagEndingAtPosition(JoinedTokenSequence jts,
            int position) throws BadLocationException {
        if (position >= 0) {
            int originalOffset = jts.offset();
            jts.move(position);
            jts.moveNext();
            Token token = jts.token();

            if (token.id() == XMLTokenId.TAG &&
                    !token.text().toString().endsWith("/>")) { //NOI18N

                while (jts.movePrevious()) {
                    int tokenOffset = jts.offset();

                    if (isOpeningTag(jts, tokenOffset) || isClosingTag(jts, tokenOffset)) {
                        int r = jts.offset();
                        jts.move(originalOffset);
                        jts.moveNext();
                        return r;
                    }
                }
            }

            jts.move(originalOffset);
            jts.moveNext();
        }
        return -1;
    }

    @Override
    protected int getTagEndOffset(JoinedTokenSequence jts, int tagStartOffset) {
        int originalOffset = jts.offset();
        jts.move(tagStartOffset);
        jts.moveNext();
        boolean thereAreMoreTokens = true;

        while (thereAreMoreTokens && jts.token().id() != XMLTokenId.TAG) {
            thereAreMoreTokens &= jts.moveNext();
        }

        int r = jts.offset() + jts.token().length();
        jts.move(originalOffset);
        jts.moveNext();
        return thereAreMoreTokens ? r : -1;
    }

//    @Override
//    protected boolean isJustBeforeClosingTag(JoinedTokenSequence jts, int tagTokenOffset) throws BadLocationException {
//        // a workaround for the difference with XML syntax support
//        return super.isJustBeforeClosingTag(jts, tagTokenOffset + "</".length()); //NOI18N
//    }

    @Override
    protected int getOpeningSymbolOffset(JoinedTokenSequence jts, int tagTokenOffset) {
        int originalOffset = jts.offset();
        jts.move(tagTokenOffset);
        boolean thereAreMoreTokens = true;

        do {
            thereAreMoreTokens = jts.movePrevious();
        } while (thereAreMoreTokens && jts.token().id() != XMLTokenId.TAG);

        if (thereAreMoreTokens) {
            int r = jts.offset();
            jts.move(originalOffset);
            jts.moveNext();
            return r;
        }

        jts.move(originalOffset);
        jts.moveNext();
        return -1;
    }

//    public void format(FormatWriter fw) {
//        XMLFormatSupport xfs = new XMLFormatSupport(fw);
//
//        FormatTokenPosition pos = xfs.getFormatStartPosition();
//
//        if ((xfs.isLineStart(pos) == false) ||
//                xfs.isIndentOnly()) { // don't do anything
//        } else { // remove end-line whitespace
//            while (pos.getToken() != null) {
//                pos = xfs.removeLineEndWhitespace(pos);
//                if (pos.getToken() != null) {
//                    pos = xfs.getNextPosition(pos);
//                }
//            }
//        }
//    }

    @Override
    public void reformat(Context context, int startOffset, int endOffset) throws BadLocationException {
//        if(!isFirstTime)//This is to prevent invoking of this method by the ActionFactory multiple times, we want only one time invocation
//            return null;
//       
        BaseDocument doc = (BaseDocument) context.document();
        doc.atomicLock();
        try {
            List<TokenElement> tags = getTags(doc);
            for (int i = tags.size() - 1; i >= 0; i--) {
                TokenElement tag = tags.get(i);
                int so = tag.getStartOffset();
                int lineOffset = Utilities.getLineOffset(doc, so);
                String tagName = tag.getName();
                if (tagName.startsWith("</")) {
                    /* For Eg:-
                     *
                     * <abc><xyz>123</xyz> //skip format for 'xyz'
                     *
                     * <xyz><abc>123</abc></xyz> //do format for 'xyz' and skip for 'abc'
                     *
                     */
                    Element docElem = doc.getDefaultRootElement().getElement(lineOffset);
                    String lineStr = doc.getText(docElem.getStartOffset(),
                            docElem.getEndOffset() - docElem.getStartOffset());
                    int ndx = lineStr.lastIndexOf(tagName);
                    if (ndx != -1) {
                        lineStr = lineStr.substring(0, ndx);
                        int ndx2 = lineStr.lastIndexOf("<" + tagName.substring(2) + ">");
                        if (ndx2 == -1) {//no start found in this line, so indent this tag
                            changePrettyText(doc, tag, so);
                        } else {
                            lineStr = lineStr.substring(ndx2 + 1);
                            ndx2 = lineStr.indexOf("<");
                            if (ndx2 != -1) {//indent this tag if it contains another tag
                                changePrettyText(doc, tag, so);
                            }
                        }
                    }
                } else {
                    changePrettyText(doc, tag, so);
                }
            }
        } catch (BadLocationException ble) {
        } catch (IOException iox) {
        } finally {
            doc.atomicUnlock();
//            //isFirstTime = false;
//            SYNCHRONIZER = RequestProcessor.getDefault().post(
//                new Runnable() {
//                    public void run() {
//                        isFirstTime = true;
//                    }
//                }, 2000
//            );
        }
    //return null;
    }

    private void changePrettyText(BaseDocument doc, TokenElement tag, int so) throws BadLocationException {
        Formatter formatter = Formatter.getFormatter(XMLKit.class);
        formatter.setExpandTabs(false);
        String newIndentText = formatter.getIndentString(doc, tag.getIndentLevel());
        doc.insertString(so, "\n" + newIndentText, null);
        int previousEndOffset = Utilities.getFirstNonWhiteBwd(doc, so) + 1;
        if (previousEndOffset < so) {
            doc.remove(previousEndOffset, so - previousEndOffset);
        }
    }

    /**
     * This is the core of the fold creation algorithm.
     * This method parses the document using lexer and creates folds and adds
     * them to the fold hierarchy.
     */
    private List<TokenElement> getTags(BaseDocument basedoc)
            throws BadLocationException, IOException {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
        TokenSequence<XMLTokenId> tokenSequence = tokenHierarchy.tokenSequence();
        org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
        // Add the text token, if any, before xml decalration to document node
        if (token != null && token.id() == XMLTokenId.TEXT) {
            if (tokenSequence.moveNext()) {
                token = tokenSequence.token();
            }
        }
        int currentTokensSize = 0;
        List<TokenElement> tags = new ArrayList<TokenElement>();
        Stack<TokenElement> stack = new Stack<TokenElement>();
        String currentNode = null;
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            XMLTokenId tokenId = token.id();
            String image = token.text().toString();
            TokenType tokenType = TokenType.TOKEN_WHITESPACE;
            switch (tokenId) {
                case TAG: {
                    int len = image.length();
                    if (image.charAt(len - 1) == '>') {// '/>'
                        if (len == 2) {
                            if (!stack.empty()) {
                                stack.pop();
                            }
                        }
                    } else {
                        tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                        if (image.startsWith("</")) {
                            String tagName = image.substring(2);
                            currentNode = tagName;
                            int beginOffset = currentTokensSize;
                            int endOffset = beginOffset + image.length();
                            int indentLevel = 0;
                            if (!stack.empty()) {
                                stack.pop();
                                indentLevel = stack.size();
                            }
                            TokenElement tag = new TokenElement(tokenType, image, beginOffset, endOffset, indentLevel);
                            tags.add(tag);
                        } else {
                            String tagName = image.substring(1);
                            int beginOffset = currentTokensSize;
                            int endOffset = beginOffset + image.length();
                            int indentLevel = stack.size();
                            TokenElement tag = new TokenElement(tokenType, tagName, beginOffset, endOffset, indentLevel);
                            tags.add(tag);
                            stack.push(tag);
                        }
                    }
                    break;
                }
                case BLOCK_COMMENT:
                case CDATA_SECTION:
                case PI_START:
                case PI_TARGET:
                case PI_CONTENT:
                case PI_END:
                case ARGUMENT: //attribute of an element
                case VALUE:
                case TEXT:
                case CHARACTER:
                case WS:
                case OPERATOR:
                case DECLARATION:
                    break; //Do nothing for above case's

                case ERROR:
                case EOL:
                default:
                    throw new IOException("Invalid token found in document: " +
                            "Please use the text editor to resolve the issues...");
            }
            currentTokensSize += image.length();
        }
        return tags;
    }

    public boolean isOneLiner(int start, int end, BaseDocument doc) {
        try {
            return Utilities.getLineOffset(doc, start) ==
                    Utilities.getLineOffset(doc, end);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            return false;
        }
    }
}
