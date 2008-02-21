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

package org.netbeans.modules.html.editor.gsf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.netbeans.editor.ext.html.parser.SyntaxTree;
import org.netbeans.fpi.gsf.ElementHandle;
import org.netbeans.fpi.gsf.Parser;
import org.netbeans.fpi.gsf.ParserFile;
import org.netbeans.fpi.gsf.ParserResult;
import org.netbeans.modules.editor.html.HTMLKit;

/**
 *
 * @author marek
 */
public class HtmlParserResult extends ParserResult {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    
    private List<SyntaxElement> elements;
    
    private AstNode parseTreeRoot = null;
    
    HtmlParserResult(Parser parser, ParserFile parserFile, List<SyntaxElement> elements) {
        super(parser, parserFile, HTMLKit.HTML_MIME_TYPE);
        this.elements = elements;
    }
    
    /** @return a root node of the hierarchical parse tree of the document. 
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since 
     * the postprocessing takes some time and is done lazily.
     */
    public synchronized AstNode root() {
        if(parseTreeRoot == null) {
            parseTreeRoot = SyntaxTree.makeTree(elementsList());
        }
        return parseTreeRoot;
    }
    
    /** @return a list of SyntaxElement-s representing parse elements of the html source. */
    public List<SyntaxElement> elementsList() {
        return this.elements;
    }
    
    /** @return a set of html document element's ids. */
    public Set<TagAttribute> elementsIds() {
        HashSet ids = new HashSet(elementsList().size() / 10);
        for(SyntaxElement element : elementsList()) {
            if(element.type() == SyntaxElement.TYPE_TAG) {
                TagAttribute attr = ((SyntaxElement.Tag)element).getAttribute(ID_ATTR_NAME);
                if(attr != null) {
                    ids.add(attr);
                }
            }
        }
        return ids;
    }
            
    @Override
    public AstTreeNode getAst() {
        return  null;
    }

    @Override
    public ElementHandle getRoot() {
        return null;
    }

}
