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

package org.netbeans.modules.html.editor;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Super simple implementation of BracesMatcher. The performance is not good since
 * the logic break some rules defined in the SPI - findOrigin() method
 * is quite cost (uses two match searches) and the searches goes beyond the limited area.
 * Needs to be reimplemented later.
 *
 * @author Marek Fukala
 */
public class HTMLBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    private LanguagePath htmlLanguagePath;
    
    public HTMLBracesMatching() {
        this(null, null);
    }
    
    private HTMLBracesMatching(MatcherContext context, LanguagePath htmlLanguagePath) {
        this.context = context;
        this.htmlLanguagePath = htmlLanguagePath;
    }
    
    //use two searches to find the original area :-|
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        HTMLSyntaxSupport syntaxSupport = HTMLSyntaxSupport.get(context.getDocument());
        int searchOffset = context.getSearchOffset();
        int[] found = syntaxSupport.findMatchingBlock(searchOffset, false);
        if(found == null) {
            return null;
        }
        int[] opposite = syntaxSupport.findMatchingBlock(found[0], false);
        return opposite;
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        HTMLSyntaxSupport syntaxSupport = HTMLSyntaxSupport.get(context.getDocument());
        int searchOffset = context.getSearchOffset();
        return syntaxSupport.findMatchingBlock(searchOffset, false);
    }
    
    //BracesMatcherFactory implementation
    public BracesMatcher createMatcher(MatcherContext context) {
        TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());
        List<TokenSequence<? extends TokenId>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
        for(TokenSequence ts : ets) {
            Language language = ts.language();
            if(language == HTMLTokenId.language()) {
                return new HTMLBracesMatching(context, ts.languagePath());
            }
        }
        throw new IllegalStateException("No text/html language found on the MatcherContext's search offset! This should never happen!");
        //Vita's mobile: 739 771 463
    }
    
}
