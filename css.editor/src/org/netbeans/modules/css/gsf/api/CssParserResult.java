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

package org.netbeans.modules.css.gsf.api;

import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author marek.fukala@sun.com
 */
public class CssParserResult extends ParserResult {

    private SimpleNode root;
    private List<Error> errors;
    
    public CssParserResult(Parser parser, Snapshot snapshot, SimpleNode root, List<Error> errors) {
        super(snapshot);

        if(root == null) {
            throw new NullPointerException("root cannot be null!");
        }

        this.root = root;
        this.errors = errors;
    }
    
    public SimpleNode root() {
        if(root == null) {
            throw new IllegalStateException("The CssParserResult already invalidated!");
        }
        return root;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        if(root == null) {
            throw new IllegalStateException("The CssParserResult already invalidated!");
        }
        return errors;
    }

    @Override
    protected void invalidate() {
//        root = null;
//        errors = null;
    }

//        @Override
//    public synchronized AstTreeNode getAst() {
//        if(astNodesRoot == null) {
//            astNodesRoot = new CssAstTreeNode(root);
//        }
//        return astNodesRoot;
//    }
//    private static final class CssAstTreeNode implements AstTreeNode {
//
//        private final SimpleNode node;
//        private ArrayList<CssAstTreeNode> children = null;
//
//        public CssAstTreeNode(SimpleNode node) {
//            this.node = node;
//        }
//
//        public Object getAstNode() {
//            return node;
//        }
//
//        public int getStartOffset() {
//            return node.startOffset();
//        }
//
//        public int getEndOffset() {
//            return node.endOffset();
//        }
//
//        public TreeNode getChildAt(int childIndex) {
//            if(children == null) {
//                initChildren();
//            }
//            return children.get(childIndex);
//        }
//
//        public int getChildCount() {
//            return node.jjtGetNumChildren();
//        }
//
//        public TreeNode getParent() {
//            return null;
//        }
//
//        public int getIndex(TreeNode tnode) {
//            if(children == null) {
//                initChildren();
//            }
//            return children.indexOf(tnode);
//        }
//
//        public boolean getAllowsChildren() {
//            return true;
//        }
//
//        public boolean isLeaf() {
//            return getChildCount() == 0;
//        }
//
//        public Enumeration children() {
//            return Collections.enumeration(children);
//        }
//
//        public String toString() {
//            return CSSParserTreeConstants.jjtNodeName[node.kind()] + " (" + node.startOffset() + "-" + node.endOffset() + ") '" + node.image() + "'";
//        }
//
//        private synchronized void initChildren() {
//            children = new ArrayList<CssAstTreeNode>(node.jjtGetNumChildren());
//            for(int i = 0; i < node.jjtGetNumChildren(); i++ ) {
//                children.add(new CssAstTreeNode((SimpleNode)node.jjtGetChild(i)));
//            }
//        }
//
//    }
    
}
