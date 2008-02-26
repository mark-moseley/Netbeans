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

package org.netbeans.modules.javascript.editing.lexer;

import java.util.HashSet;
import static org.netbeans.modules.javascript.editing.lexer.LexUtilities.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.javascript.editing.JsTestBase;

/**
 *
 * @author martin
 */
public class LexUtilitiesTest extends JsTestBase {

    public LexUtilitiesTest(String testName) {
        super(testName);
    }

    public void testSkipParenthesis() {
        assertFalse(skipParenthesis(getSequence("if (true) foo();", 0)));
        assertTrue(skipParenthesis(getSequence("if (true) foo();", 2)));
        assertTrue(skipParenthesis(getSequence("if (true) foo();", 3)));
        assertFalse(skipParenthesis(getSequence("if (true) foo();", 4)));

        assertFalse(skipParenthesis(getSequence("if\n (true) foo();", 0)));
        assertTrue(skipParenthesis(getSequence("if \n(true) foo();", 2)));
        assertTrue(skipParenthesis(getSequence("if \n\n(true) foo();", 3)));

        assertFalse(skipParenthesis(getSequence("if ((true)) foo();", 0)));
        assertTrue(skipParenthesis(getSequence("if ((true)) foo();", 2)));
        assertTrue(skipParenthesis(getSequence("if ((true)) foo();", 3)));
        assertTrue(skipParenthesis(getSequence("if ((true)) foo();", 4)));
        assertFalse(skipParenthesis(getSequence("if ((true)) foo();", 5)));
        
        assertFalse(skipParenthesis(getSequence("if (true && (true)) foo();", 0)));
        assertTrue(skipParenthesis(getSequence("if (true && (true)) foo();", 2)));
        assertTrue(skipParenthesis(getSequence("if (true && (true)) foo();", 3)));
        assertFalse(skipParenthesis(getSequence("if (true && (true)) foo();", 4)));
        assertFalse(skipParenthesis(getSequence("if (true && (true)) foo();", 5)));
        
        assertFalse(skipParenthesis(getSequence("if true) foo();", 0)));
        assertFalse(skipParenthesis(getSequence("if true) foo();", 2)));
        
        assertFalse(skipParenthesis(getSequence("if (true foo();", 0)));
        assertFalse(skipParenthesis(getSequence("if (true foo();", 2)));
        
        assertFalse(skipParenthesis(getSequence("if (true)) foo();", 0)));
        assertTrue(skipParenthesis(getSequence("if (true)) foo();", 2)));
    }
    
    public void testIsBracelessMultiline() {
        
        // TODO martin : result depends on ranges, so play more with them
        
        assertFalse(isBracelessMultilineLastLine(getDocument("if (true) foo();"), 0, new HashSet<OffsetRange>()));
        assertTrue(isBracelessMultilineLastLine(getDocument("if (true)\n foo();"), 0, new HashSet<OffsetRange>()));
        assertTrue(isBracelessMultilineLastLine(getDocument("if (true) \nfoo();\n"), 0, new HashSet<OffsetRange>()));

        assertFalse(isBracelessMultilineLastLine(getDocument("if\n (true) foo();"), 0, new HashSet<OffsetRange>()));
        assertTrue(isBracelessMultilineLastLine(getDocument("if (true)\n foo();"), 1, new HashSet<OffsetRange>()));
        assertFalse(isBracelessMultilineLastLine(getDocument("if \n\n(true) \nfoo();\n"), 0, new HashSet<OffsetRange>()));
        assertFalse(isBracelessMultilineLastLine(getDocument("if \n\n(true) \nfoo();\n"), 6, new HashSet<OffsetRange>()));

        assertFalse(isBracelessMultilineLastLine(getDocument("for (var name in style) foo();"), 0, new HashSet<OffsetRange>()));
        assertTrue(isBracelessMultilineLastLine(getDocument("for (var name in style)\n foo();"), 0, new HashSet<OffsetRange>()));
        assertTrue(isBracelessMultilineLastLine(getDocument("for (var name in style) \nfoo();\n"), 0, new HashSet<OffsetRange>()));
    }
    
    private TokenSequence<? extends JsTokenId> getSequence(String text, int offset) {
        BaseDocument doc = getDocument(text);
        return getPositionedSequence(doc, offset);
    }
    
}
