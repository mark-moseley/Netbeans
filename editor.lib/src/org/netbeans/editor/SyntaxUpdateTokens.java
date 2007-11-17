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

package org.netbeans.editor;

import java.util.Collections;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.ErrorManager;

/**
 * Notification about the tokens being relexed as result of a document
 * modification.
 * <br>
 * The SPIs may use <code>Document.putProperty(SyntaxUpdateNotify.class, inst)</code>
 * to assign an instance of this class to the particular document.
 * <br>
 * The client <code>DocumentListener</code>s can then retrieve
 * the list of the relexed tokens of interest by using
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public abstract class SyntaxUpdateTokens {
    
    /**
     * Retrieve unmodifiable list of relexed tokens of interest for the particular
     * document event.
     *
     * @param evt document event for which the list of tokens is being retrieved.
     * @return list of {@link TokenInfo}s describing the tokens of interest
     *  that were relexed because of the document modification.
     */
    public static List getTokenInfoList(DocumentEvent evt) {
        if (!(evt instanceof BaseDocumentEvent)) {
            return Collections.EMPTY_LIST;
        }
        
        return ((BaseDocumentEvent)evt).getSyntaxUpdateTokenList();
    }

    /**
     * Create list of tokens of interest for the whole document.
     * <br>
     * Document is readlocked during this operation.
     *
     * @param doc document for which the list of tokens is being retrieved.
     * @return list of {@link TokenInfo}s describing the tokens of interest
     *  throughout the whole document.
     */
    public static List getTokenInfoList(Document doc) {
        SyntaxUpdateTokens suTokens = (SyntaxUpdateTokens)doc.getProperty(SyntaxUpdateTokens.class);
        
        if (suTokens == null || !(doc instanceof BaseDocument)) {
            return Collections.EMPTY_LIST;
        }
        
        List tokenList;
        BaseDocument bdoc = (BaseDocument)doc;
        bdoc.readLock();
        try {
            suTokens.syntaxUpdateStart();
            try {
                bdoc.getSyntaxSupport().tokenizeText(
                    new AllTokensProcessor(suTokens), 0, bdoc.getLength(), true);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                tokenList = suTokens.syntaxUpdateEnd();
            }
            
        } finally {
            bdoc.readUnlock();
        }
        return tokenList;
    }
    
    /**
     * Notification that updating of the lexical states is starting
     * for the last performed modification.
     * <br>
     * The list of the token infos should be cleared here.
     */
    public abstract void syntaxUpdateStart();
    
    /**
     * Notification that updating of lexical states has ended
     * so there will be no more tokens relexed for the modification.
     * <br>
     * If there are any tokens of interest they should be returned
     * from this method.
     *
     * @return list of tokens of interest. The returned list will
     *  be copied by the infrastructure so the originally returned
     *  instance can continue to be used.
     */
    public abstract List syntaxUpdateEnd();
    
    /**
     * Notification that a token was found during updating
     * of lexical states. If this class is interested in providing
     * info about this token to clients then it should create
     * 
     */
    public abstract void syntaxUpdateToken(TokenID id, TokenContextPath contextPath, int offset, int length);
    
    
    public class TokenInfo {
        
        private final TokenID id;
        
        private final TokenContextPath contextPath;
        
        private final int offset;
        
        private final int length;
        
        public TokenInfo(TokenID id, TokenContextPath contextPath, int offset, int length) {
            this.id = id;
            this.contextPath = contextPath;
            this.offset = offset;
            this.length = length;
        }
        
        public final TokenID getID() {
            return id;
        }
        
        public final TokenContextPath getContextPath() {
            return contextPath;
        }
        
        public final int getOffset() {
            return offset;
        }
        
        public int getLength() {
            return length;
        }
        
        public String toString() {
            return "id=" + id + ", ctx=" + contextPath + ", off=" + offset + ", len=" + length; // NOI18N
        }

    }
    
    static final class AllTokensProcessor implements TokenProcessor {
        
        private SyntaxUpdateTokens suTokens;
        
        private int bufferStartOffset;
        
        AllTokensProcessor(SyntaxUpdateTokens suTokens) {
            this.suTokens = suTokens;
        }
        
        public void nextBuffer(char[] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
            bufferStartOffset = startPos - offset;
        }
        
        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath, int tokenBufferOffset, int tokenLength) {
            suTokens.syntaxUpdateToken(tokenID, tokenContextPath, tokenBufferOffset, tokenLength);
            return true;
        }
        
        public int eot(int offset) {
            return 0;
        }
        
    }



}
