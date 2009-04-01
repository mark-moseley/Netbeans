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
import java.util.ArrayList;
import java.util.List;
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
        CssParser parser = new CssParser();
        CssParserTokenManager tokenManager = new CssParserTokenManager(new ASCII_CharStream(new StringReader(source)));
        parser.ReInit(tokenManager);

        return parser.styleSheet();
    }

    private static boolean isErrorNode(SimpleNode node) {
        return node.kind() == CssParserTreeConstants.JJTERROR_SKIPBLOCK ||
                node.kind() == CssParserTreeConstants.JJTERROR_SKIPDECL ||
                node.kind() == CssParserTreeConstants.JJTERROR_SKIP_TO_WHITESPACE;
    }

    /** returns number of error nodes underneath the node. */
    private static List<SimpleNode> getErrors(SimpleNode node) {
        final List<SimpleNode> errors = new ArrayList<SimpleNode>();
        SimpleNodeUtil.visitChildren(node, new NodeVisitor() {

            public void visit(SimpleNode node) {
                if (isErrorNode(node)) {
                    errors.add(node);
                }
            }
        });
        return errors;
    }

    private void assertNoErrors(SimpleNode node) {
        List<SimpleNode> errors = getErrors(node);
        if(errors.size() > 0) {
            StringBuffer buf = new StringBuffer();
            for(SimpleNode e : errors) {
                buf.append(e.toString());
                buf.append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
            assertEquals("Unexpected parse errors found: " + buf.toString(), 0, errors.size());
        }
    }

    public void testParserBasis() throws ParseException {
        SimpleNode node = parse("h1 { color: red; }");
        Assert.assertNotNull(node);
        assertNoErrors(node);
        
    }

    private void check(String source) throws ParseException {
        SimpleNode node = parse(source);
        Assert.assertNotNull(node);
        assertNoErrors(node);
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

        check("table > @@@ { }");
        check("t[@@@] { }");
        check("t[x=@@@] { }");
        check("* > t[x=@@@] { }");
        check("E:lang(@@@){ }");
        check("E:@@@{ }");
        check("t[x|=@@@] { }");

//        check("#@@@ { }");//fails
//        check("E#@@@ { }");//fails

        check("@@@ + t[x=@@@] { }");

        check("h1 { @@@: rgb(@@@); }");
        check("span[hello=@@@][@@@]{}");

//        check("EXAMPLE { /* @@@ */ }");//fails
        check("p.@@@:first-letter {color: @@@}");

//        check("h1 {color: #@@@}");//fails
        check("media @@@{}");

//        check("media TV{ @@@ { } }");//fails
        check("@page:left{margin-left:@@@;}");
    }
}