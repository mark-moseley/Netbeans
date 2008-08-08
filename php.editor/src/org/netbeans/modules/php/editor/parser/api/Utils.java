/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.parser.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 * This is AST Utils class. 
 * @author Petr Pisl
 */
public class Utils {

    /**
     * 
     * @param root a Program node, where to look for the comment
     * @param node  a Node for which a commen you want to find. 
     * @return appropriate comment or null, if the comment doesn't exists.
     */
    public static Comment getCommentForNode(Program root, ASTNode node) {
        List<Comment> comments = root.getComments();
        Comment possible = null;

        if (node.getEndOffset() <= root.getEndOffset()) {
            for (Comment comm : comments) {
                if (comm.getEndOffset() < node.getStartOffset()) {
                    possible = comm;
                } else {
                    break;
                }
            }
            if (possible != null && (possible.getEndOffset() + 1 < node.getStartOffset())) {
                List<ASTNode> nodes = (new NodeRangeLocator()).locate(root, new OffsetRange(possible.getEndOffset() + 1, node.getStartOffset() - 1));
                if (nodes.size() != 0) {
                    possible = null;
                }
            }
        }

        return possible;
    }

    public static Program getRoot(CompilationInfo info) {
        ParserResult result = info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, 0);

        if (result == null) {
            return null;
        }

        if (result instanceof PHPParseResult) {
            return ((PHPParseResult) result).getProgram();
        } else {
            return null;
        }
    }

    public static ASTNode getNodeAtOffset(CompilationInfo info, int astOffset) {
        Program program = getRoot(info);
        return getNodeAtOffset(program, astOffset);
    }

    /**
     * Return an ASTNode at the given offset. It doesn't count comments. 
     * @param node
     * @param astOffset
     * @return null if there is not a node on this possition or an ASTNode except comments
     */
    public static ASTNode getNodeAtOffset(ASTNode node, int offset) {
        if (node.getStartOffset() > offset || node.getEndOffset() < offset) {
            return null;
        }
        return (new NodeLocator()).locate(node, offset);

    }
    
    /**
     * Return an ASTNode of given type at the given offset. It doesn't count comments. 
     * 
     * @param node
     * @param astOffset
     * @param terminus 
     * @return null if there is not a node on this possition or an ASTNode except comments
     */
    public static ASTNode getNodeAtOffset(ASTNode node, int offset, Class<? extends ASTNode> terminus) {
        if (node.getStartOffset() > offset || node.getEndOffset() < offset) {
            return null;
        }
        
        return (new SpecificClassNodeLocator(terminus)).locate(node, offset);
    }

    private static class NodeLocator extends DefaultVisitor {

        protected int offset = 0;
        protected ASTNode node = null;

        public ASTNode locate(ASTNode beginNode, int astOffset) {
            offset = astOffset;
            scan(beginNode);
            return this.node;
        }

        public void scan(ASTNode node) {
            if (node != null) {
                if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                    this.node = node;
                    node.accept(this);
                }
            }
        }
    }
    
    private static class SpecificClassNodeLocator extends NodeLocator{
        private Class<? extends ASTNode> terminus;

        public SpecificClassNodeLocator(Class<? extends ASTNode> terminus) {
            this.terminus = terminus;
        }

        @Override
        public void scan(ASTNode node) {
            if (terminus.isInstance(node)){
                if (node.getStartOffset() <= offset && offset <= node.getEndOffset()) {
                    this.node = node;
                }
            } else {
                super.scan(node);
            }
        }
    }

    private static class NodeRangeLocator extends DefaultVisitor {

        private OffsetRange range;
        private List<ASTNode> nodes = new ArrayList<ASTNode>();

        public List<ASTNode> locate(ASTNode beginNode, OffsetRange range) {
            this.range = range;
            scan(beginNode);
            return nodes;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null) {
                if (range.getStart() <= node.getStartOffset() && node.getEndOffset() <= range.getEnd()) {
                    // node is in the range
                    nodes.add(node);
                } else {
                    if ((node.getStartOffset() < range.getStart() && range.getStart() < node.getEndOffset()) || (node.getStartOffset() < range.getEnd() && range.getEnd() < node.getEndOffset())) {
                        // node is partialy in the range.
                        node.accept(this);
                    }
                }

            }
        }
    }
    
    public static String resolveVariableName(Variable variable) {
        String name = null;
        if (variable.getName() instanceof Identifier) {
            name = ((Identifier) variable.getName()).getName();
        }
        return name;
    }
}
