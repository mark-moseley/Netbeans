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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.editor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CSSParserConstants;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * A domain object model representing CSS file backed up by 
 * instance of {@link org.netbeans.modules.parsing.api.Snapshot;}.
 *
 * @author Marek Fukala
 */
public final class CssModel {

    private static final Logger LOGGER = Logger.getLogger(CssModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private final List<CssRule> rules = new ArrayList<CssRule>(10);
    private Snapshot snapshot;

    
    public static CssModel create(CssParserResult result) {
         return new CssModel(result.getSnapshot(), result.root());
    }


    private CssModel(Snapshot snapshot, SimpleNode root) {
        this.snapshot = snapshot;
        updateModel(snapshot, root);
    }

    //--- API methods ---

    /** @return an instance of the source Snapshot of the parser result used to construct this model. */
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /** @return List of {@link CssRule}s or null if the document hasn't been parsed yet. */
    public List<CssRule> rules() {
        return rules;
    }

    /** Finds a rule on the given offset.
     * 
     * @return Instance of {@link CssRule} for the given offset or null if the 
     * offset falls into a space where there is no css rule.
     * @param offset within the model's document
     */
    public CssRule ruleForOffset(int offset) {
        synchronized (rules) {
            if (rules != null) {
                for (CssRule rule : rules()) {
                    if (rule.getRuleNameOffset() <= offset && rule.getRuleCloseBracketOffset() >= offset) {
                        return rule;
                    }
                }
            }
            return null;
        }
    }

    //--- private methods ---

    private synchronized void updateModel(final Snapshot snapshot, SimpleNode root) {
        synchronized (rules) {
            NodeVisitor styleRuleVisitor = new NodeVisitor() {

                public void visit(SimpleNode node) {
                        if (node.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                            //find curly brackets
                            Token t = node.jjtGetFirstToken();
                            Token last = node.jjtGetLastToken();

                            int openCurlyBracketOffset = -1;
                            int closeCurlyBracketOffset = -1;
                            ArrayList<Integer> semicolons = new ArrayList<Integer>();
                            ArrayList<Integer> colons = new ArrayList<Integer>();
                            while (t != null && t.offset <= last.offset) { //also include the last token
                                if (t.kind == CSSParserConstants.LBRACE) {
                                    openCurlyBracketOffset = t.offset;
                                } else if (t.kind == CSSParserConstants.RBRACE) {
                                    closeCurlyBracketOffset = t.offset;
                                } else if (t.kind == CSSParserConstants.SEMICOLON) {
                                    semicolons.add(Integer.valueOf(t.offset));
                                } else if (t.kind == CSSParserConstants.COLON) {
                                    colons.add(Integer.valueOf(t.offset));
                                }
                                t = t.next;
                            }

                            //parse style rule
                            SimpleNode selectortList = SimpleNodeUtil.getChildByType(node, CSSParserTreeConstants.JJTSELECTORLIST);
                            SimpleNode[] declarations = SimpleNodeUtil.getChildrenByType(node, CSSParserTreeConstants.JJTDECLARATION);
                            List<CssRuleItem> ruleItems = new ArrayList<CssRuleItem>(declarations.length);
                            for (int i = 0; i < declarations.length; i++) {
                                SimpleNode declaration = declarations[i];
                                SimpleNode property = SimpleNodeUtil.getChildByType(declaration, CSSParserTreeConstants.JJTPROPERTY);
                                SimpleNode value = SimpleNodeUtil.getChildByType(declaration, CSSParserTreeConstants.JJTEXPR);

                                int semicolonOffset = i < semicolons.size() ? semicolons.get(i) : -1; //there may not be the semicolon after last declaration
                                int colonOffset = colons.get(i);

                                CssRuleItem ruleItem = new CssRuleItem(property.image().trim(), property.startOffset(), value.image().trim(), value.startOffset(), colonOffset, semicolonOffset);

                                ruleItems.add(ruleItem);
                            }

                            String ruleName = selectortList.image().trim();
                            CssRule rule = new CssRule(snapshot, ruleName, selectortList.startOffset(),
                                    openCurlyBracketOffset, closeCurlyBracketOffset, ruleItems);
                            rules.add(rule);

                        }
                }
            };

            SimpleNodeUtil.visitChildren(root, styleRuleVisitor);

            if (LOG) {
                LOGGER.fine("CssModel parse tree:"); //NOI18N
                root.dump("");
                LOGGER.fine("CssModel structure:"); //NOI18N
                for(CssRule rule : rules) {
                    LOGGER.fine(rule.toString());
                }
            }
            
        }
    }
}
