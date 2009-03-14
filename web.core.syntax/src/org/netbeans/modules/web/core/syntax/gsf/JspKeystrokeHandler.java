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

package org.netbeans.modules.web.core.syntax.gsf;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.editor.indent.api.Indent;

public class JspKeystrokeHandler implements KeystrokeHandler {

    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        // TODO: below whitespace skipping does not work because whitespace
        // tokens between JSP tokens are actually HTML tokens and not JSP tokens.
        // Proper way is to iterate over document characters and skip all whitespaces
        // till you get to a text and then get token for the text.
        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence((BaseDocument)doc, caretOffset, JspTokenId.language());
        if (ts == null) {
            return -1;
        }
        ts.move(caretOffset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == JspTokenId.SYMBOL &&
                ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == JspTokenId.ENDTAG) {
                closingTagName = ts.token().text().toString();
                end = ts.offset()+ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return  -1;
        }
        boolean foundOpening = false;
        if (ts.token().id() == JspTokenId.SYMBOL &&
                ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == JspTokenId.TAG) {
                    if (ts.token().text().toString().equals(closingTagName)) {
                        foundOpening = true;
                    }
                    break;
                }
            }
        }
        if (foundOpening) {
            final Indent indent = Indent.get(doc);
            doc.insertString(caretOffset, "\n", null); //NOI18N
            //move caret
            target.getCaret().setDot(caretOffset);
            //and indent the line
            indent.reindent(caretOffset + 1, end);
        }
        return -1;
    }

    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        return new ArrayList<OffsetRange>();
    }

    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
