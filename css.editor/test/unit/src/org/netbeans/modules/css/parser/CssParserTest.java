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
package org.netbeans.modules.css.parser;

import java.io.StringReader;
import org.junit.Assert;
import org.netbeans.modules.css.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class CssParserTest extends TestBase {

    public CssParserTest() {
        super(CssParserTest.class.getName());
    }

    private SimpleNode parse(String source) throws ParseException {
        CSSParser parser = new CSSParser();
        CSSParserTokenManager tokenManager = new CSSParserTokenManager(new ASCII_CharStream(new StringReader(source)));
        parser.ReInit(tokenManager);

        return parser.styleSheet();
    }

    private static boolean isErrorNode(SimpleNode node) {
        return node.kind() == CSSParserTreeConstants.JJTERROR_SKIPBLOCK ||
                node.kind() == CSSParserTreeConstants.JJTERROR_SKIPDECL ||
                node.kind() == CSSParserTreeConstants.JJTERROR_SKIP_TO_WHITESPACE;
    }

    /** returns number of error nodes underneath the node. */
    private static int getNumErrors(SimpleNode node) {
        final int[] errors = new int[1];
        SimpleNodeUtil.visitChildren(node, new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (isErrorNode(node)) {
                    errors[0]++;
                }
            }
        });
        return errors[0];
    }

    public void testParserBasis() throws ParseException {
        SimpleNode node = parse("h1 { color: red; }");
        Assert.assertNotNull(node);
        Assert.assertEquals(0, getNumErrors(node));
    }

    private void check(String source) throws ParseException {
        SimpleNode node = parse(source);
        Assert.assertNotNull(node);
        Assert.assertEquals(0, getNumErrors(node));
    }

    // @@@ represents a gap from the css perspective in reality filled with 
    // a templating language code.
    public void testParserOnTemplating() throws ParseException {
        //generated properties
        check("h1 { @@@: red; }");
        check("h1 { color: @@@; }");
        check("h1 { @@@: @@@; }");
        check("h1 { color: @@@ red @@@; }");
        check("h1 { co@@@lor: red; }");
        check("h1 { @@@@@@: green; }");

        check("h1 { background-image: url(@@@); }");
        check("h1 { background-image: url(\"@@@\"); }");
        check("h1 { color: rgb(@@@,@@@,@@@); }");
        check("h1 { color: rgb(0,0,@@@); }");

//        check("h1 { @@@ }"); //fails

        //selectors are generated
        check("@@@ { }");
        check("h1 @@@ h2 { }");
        check("t@@@ble { }");

    }
}