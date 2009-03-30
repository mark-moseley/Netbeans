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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lexer.api.CssTokenId;

/**
 *
 * @author marek
 */
public class LexerUtils {

    public static TokenSequence<CssTokenId> getCssTokenSequence(Document doc, int offset) {
        TokenHierarchy hi = TokenHierarchy.get(doc);
        
        //if we are at the border of the tokensequence then,
        //try to look ahead
        TokenSequence<CssTokenId> ts = tokenSequenceList(hi, offset, false);
            
        //and back
        if(ts == null) {
            ts = tokenSequenceList(hi, offset, true);
        }
        
        if(ts == null) {
            //token sequence neither in forward nor in backward direction, give up
            return null;
        }
        
        //check boundaries of the token sequence - if the skip lenghts are used the token 
        //sequence is returned even for offsets outside of the tokenSequence content
        
        //test beginning
        ts.moveStart();
        if(ts.moveNext()) {
            if(ts.offset() > offset) {
                return null;
            }
        }
        
        //test end
        ts.moveEnd();
        if(ts.movePrevious()) {
            if(ts.offset() + ts.token().length() < offset) {
                return null;
            }
        }
        
        //seems to be ok
        return ts;
        

    }

    @SuppressWarnings("unchecked")
    private static TokenSequence<CssTokenId> tokenSequenceList(TokenHierarchy hi, int offset, boolean backwardBias) {
        List<TokenSequence> tsl = hi.embeddedTokenSequences(offset, backwardBias);
        if (tsl.size() > 0) {
            TokenSequence ts = tsl.get(tsl.size() - 1);
            if (ts.language() != CssTokenId.language()) {
                return null;
            } else {
                return (TokenSequence<CssTokenId>)ts;
            }
        }
        return null;
    }
}
