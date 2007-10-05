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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenList;

/**
 * Token list that allows mutating by token list mutator.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface MutableTokenList<T extends TokenId> extends TokenList<T> {

    /**
     * Return token or branch token list at the requested index
     * but do not synchronize the access - there should only be one thread
     * accessing the token list at this time.
     * Also do not perform any checks regarding index validity
     * - only items below {@link #tokenCountCurrent()} will be requested.
     */
    Object tokenOrEmbeddingContainerUnsync(int index);
    
    /**
     * Create lexer input operation used for relexing of the input.
     */
    LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState);
    
    /**
     * Check whether the whole input was tokenized or not.
     * <br/>
     * Incremental algorithm uses this information to determine
     * whether it should relex the input till the end or not.
     */
    boolean isFullyLexed();
    
    /**
     * Update the token list by replacing tokens according to the given change.
     */
    void replaceTokens(TokenListChange<T> change, int removeTokenCount, int diffLength);

    /**
     * Token list updater increments modification count of this token list
     * by using this method.
     */
//    void incrementModCount();

}
