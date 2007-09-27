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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import antlr.TokenStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * base class for #if, #elif directives
 * @author Vladimir Voskresensky
 */
public abstract class APTIfConditionBaseNode extends APTTokenAndChildBasedNode
                                            implements Serializable {
    private static final long serialVersionUID = 1068728941146083839L;
    private List<Token> condition;
    private int endOffset;
    
    /** Copy constructor */
    /**package*/APTIfConditionBaseNode(APTIfConditionBaseNode orig) {
        super(orig);
        this.condition = orig.condition;
    }
    
    /** Constructor for serialization */
    protected APTIfConditionBaseNode() {
    }
    
    /**
     * Creates a new instance of APTIfConditionBaseNode
     */
    protected APTIfConditionBaseNode(Token token) {
        super(token);
    }
    
    public String getText() {
        String text = super.getText();
        String condStr;
        if (condition != null) {
            condStr = APTUtils.toString(getCondition());
        } else {
            assert(true):"is it ok to have #if/#elif without condition?"; // NOI18N
            condStr = "<no condition>"; // NOI18N
        }
        return text + " CONDITION{" + condStr + "}"; // NOI18N
    }
    
    /** provides APTIf and APTElif interfaces support */
    public TokenStream getCondition() {
        return condition != null ? new ListBasedTokenStream(condition) : APTUtils.EMPTY_STREAM;
    }
    
    public boolean accept(Token token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = ((APTToken)token).getOffset();
            if (condition == null) {
                if (DebugUtils.STANDALONE) {
                    System.err.printf("line %d: %s with no expression\n", // NOI18N
                        getToken().getLine(), getToken().getText().trim());
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "line {0}: {1} with no expression", // NOI18N
                            new Object[] {getToken().getLine(), getToken().getText().trim()} );                
                }
            }
            return false;
        } else if (!APTUtils.isCommentToken(ttype)) {
            if (condition == null) {
                condition = new ArrayList<Token>();
            }
            condition.add(token);
        }
        return true;
    }
    
    public int getEndOffset() {
        return endOffset;
    }
}
