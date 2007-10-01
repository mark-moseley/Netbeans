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
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerRandomTest extends TestCase {

    public SimpleLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    public void testWithSeed_1140557399761L() throws Exception {
        test(1140557399761L);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(TestTokenId.language());
        
        //randomModify.setDebugOperation(true);
        //randomModify.setDebugDocumentText(true);
        //randomModify.setDebugHierarchy(true);

        // Check for incorrect lookahead counting problem
        // after one of the larger updates of the LexerInputOperation's code
        randomModify.insertText(0, "+--+"); // "+"[2]; "-"[0]; "-"[0]; "+"[1];
        randomModify.removeText(2, 1); // "+-+": "+-+"[0];
        
        randomModify.clearDocument();

        // Check that token list updater respects that the lookaheads
        // of subsequent tokens (already present in the token list)
        // correspond to the lookahead of the relexed token.
        // In the following example a "+-+" token must be created.
        randomModify.insertText(0, "---+"); // "-"[0]; "-"[0]; "-"[0]; "+"[1];
        randomModify.insertText(1, "+"); 
        randomModify.removeText(3, 1);
        
        randomModify.clearDocument();

        // Check that the token list updater checks respects the rule
        // that the lookahead of the incrementally created tokens
        // is the same like in a regular batch lexing.
        randomModify.insertText(0, "+--+--");
        randomModify.removeText(2, 1);
        
        randomModify.clearDocument();
   
        // Check for the previous case but this time the relexing would normally
        // be skipped but this would lead to lookahead 1 for the "-" token
        // after the removed "+" (while the batch lexing would produce lookahead 0)
        randomModify.insertText(0, "-+--"); // "-"[0]; "+"[2]; "-"[1]; "-"[0];
        // Without extra care it would become "-"[0]; "-"[1]; "-"[0];
        randomModify.removeText(1, 1); // "---": "-"[0]; "-"[0]; "-"[0]; 
        
        randomModify.clearDocument();
        
        // Similar case to the previous one but with more tokens
        randomModify.insertText(0, "-+-++--");
        randomModify.removeText(1, 4);
        
        randomModify.clearDocument();

        // Check for the case when token validation cannot be performed
        // because although the lenghth of the removal is less than
        // the "+-+" token's length the removal spans token boundaries
        randomModify.insertText(0, "-+-+ --");
        randomModify.removeText(3, 2);
        
        randomModify.clearDocument();


        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("-+--+-+", 0.2),
            FixedTextDescriptor.create("+-", 0.2),
            FixedTextDescriptor.create("-+", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '+', '-', '*', '/'}, 0.3),
        };

        RandomCharDescriptor[] plusMinusChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.chars(new char[] { '+', '-' }, 0.3),
//            RandomCharDescriptor.chars(new char[] { '*', '/' }, 0.1),
            RandomCharDescriptor.space(0.1),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
        RandomTextProvider plusMinusTextProvider = new RandomTextProvider(plusMinusChars, fixedTexts);
        
        RandomTextProvider textProvider = plusMinusTextProvider;

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, plusMinusTextProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0), // snapshots create/destroy
            }
        );

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.4, 0.2, 0.2,
                        0.1, 0.1,
                        0.0, 0.0), // snapshots create/destroy
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.4, 0.3,
                        0.0, 0.0), // snapshots create/destroy
            }
        );
    }
    
}
