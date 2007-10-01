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

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestTokenId;
import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class FlyTokensTest extends TestCase {

    public FlyTokensTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testMaxFlySequenceLength() {
        // Both "public" and " " are flyweight
        String text = "public public public public public public public ";
        int commentTextStartOffset = 5;
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<? extends TokenId> ts = hi.tokenSequence();
        int firstNonFlyIndex = -1;
        int secondNonFlyIndex = -1;
        int tokenIndex = 0;
        for (int i = 0; i < 7; i++) {
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;

            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", -1);
            if (!ts.token().isFlyweight()) {
                if (firstNonFlyIndex == -1) {
                    firstNonFlyIndex = tokenIndex;
                } else if (secondNonFlyIndex == -1) {
                    secondNonFlyIndex = tokenIndex;
                }
            }
            tokenIndex++;
        }
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH, firstNonFlyIndex);
        assertEquals(LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH * 2 + 1, secondNonFlyIndex);
    }    
}
