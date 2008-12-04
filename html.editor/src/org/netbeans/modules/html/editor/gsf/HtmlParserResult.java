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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.netbeans.editor.ext.html.parser.SyntaxTree;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class HtmlParserResult extends ParserResult {

    private static final String FALLBACK_DOCTYPE = "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    private static final String ID_ATTR_NAME = "id"; //NOI18N
    private List<SyntaxElement> elements;
    private AstNode parseTreeRoot = null;
    private DTD dtd = null;
    private List<Error> errors;

    HtmlParserResult(Parser parser, Snapshot snapshot, List<SyntaxElement> elements) {
        super(snapshot);
        this.elements = elements;

        //init the parse tree se we are able to provide the diagnostics
        //consider toPhase usage here.
        root();
    }

    /** @return a root node of the hierarchical parse tree of the document. 
     * basically the tree structure is done by postprocessing the flat parse tree
     * you can get by calling elementsList() method.
     * Use the flat parse tree results if you do not need the tree structure since 
     * the postprocessing takes some time and is done lazily.
     */
    public synchronized AstNode root() {
        if (parseTreeRoot == null) {
            parseTreeRoot = SyntaxTree.makeTree(elementsList());
            analyzeParseResult();
        }
        return parseTreeRoot;
    }

    /** @return a list of SyntaxElement-s representing parse elements of the html source. */
    public List<SyntaxElement> elementsList() {
        return this.elements;
    }

    /** @return an instance of DTD bound to the html document. */
    public synchronized DTD dtd() {
        if (dtd == null) {
            final DTD[] dtds = new DTD[]{org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null)};
            //find document type declaration
            AstNodeUtils.visitChildren(root(), new AstNodeVisitor() {

                public void visit(AstNode node) {
                    if (node.type() == AstNode.NodeType.DECLARATION) {
                        String publicID = (String) node.getAttribute("public_id"); //NOI18N
                        if (publicID != null) {
                            DTD dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD(publicID, null);
                            if (dtd != null) {
                                dtds[0] = dtd;
                            }
                        }
                    }
                }
            });
            dtd = dtds[0];
        }
        return dtd;
    }

    /** @return a set of html document element's ids. */
    public Set<TagAttribute> elementsIds() {
        HashSet ids = new HashSet(elementsList().size() / 10);
        for (SyntaxElement element : elementsList()) {
            if (element.type() == SyntaxElement.TYPE_TAG) {
                TagAttribute attr = ((SyntaxElement.Tag) element).getAttribute(ID_ATTR_NAME);
                if (attr != null) {
                    ids.add(attr);
                }
            }
        }
        return ids;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        //todo - we provide errors somewhere else
        return Collections.EMPTY_LIST;
    }

    @Override
    protected void invalidate() {
        //todo
    }

    private void analyzeParseResult() {
        final DTD dtd = dtd();
        final List<Error> _errors = new ArrayList<Error>();
        AstNodeUtils.visitChildren(root(),
                new AstNodeVisitor() {

                    public void visit(AstNode node) {
                        if (node.type() == AstNode.NodeType.UNMATCHED_TAG) {
                            AstNode unmatched = node.children().get(0);
                            if (dtd != null) {
                                //check the unmatched tag according to the DTD
                                Element element = dtd.getElement(node.name().toUpperCase());
                                if (element != null) {
                                    if (unmatched.type() == AstNode.NodeType.OPEN_TAG && element.hasOptionalEnd() || unmatched.type() == AstNode.NodeType.ENDTAG && element.hasOptionalStart()) {
                                        return;
                                    }
                                }
                            }

                            Error error =
                                    new DefaultError("unmatched_tag",
                                    NbBundle.getMessage(this.getClass(), "MSG_Unmatched_Tag"),
                                    null,
                                    getSnapshot().getSource().getFileObject(),
                                    node.startOffset(),
                                    node.endOffset(),
                                    Severity.WARNING); //NOI18N
                            _errors.add(error);
                        }
                    }
                });

        this.errors = _errors;

    }
}
