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

package org.netbeans.modules.python.editor.lexer;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.python.editor.PythonTestBase;

/**
 *
 * @author Tor Norbye
 */
public class CallTest extends PythonTestBase {

    public CallTest(String testName) {
        super(testName);
    }

    private Call getCall(String source) {
        int caretPos = source.indexOf('^');

        source = source.substring(0, caretPos) + source.substring(caretPos + 1);

        BaseDocument doc = getDocument(source);

        TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
        Call call = Call.getCallType(doc, th, caretPos);

        return call;
    }

    public void testCall1() throws Exception {
        Call call = getCall("File.ex^");
        assertEquals("File", call.getLhs());
        assertEquals("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall1c() throws Exception {
        Call call = getCall("File.ex^ ");
        assertEquals("File", call.getLhs());
        assertEquals("File", call.getType());
        assertTrue(call.isSimpleIdentifier());
        assertTrue(call.isStatic());
    }

    public void testCall2() throws Exception {
        Call call = getCall("xy.ex^");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2b() throws Exception {
        Call call = getCall("xy.^");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2c() throws Exception {
        Call call = getCall("xy.ex^ ");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall2d() throws Exception {
        Call call = getCall("xy.^ ");
        assertEquals("xy", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCall3() throws Exception {
        Call call = getCall("\"foo\".gsu^");
        assertEquals("StringType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall3b() throws Exception {
        Call call = getCall("\"foo\".^");
        assertEquals("StringType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5() throws Exception {
        Call call = getCall("[1,2,3].each^");
        assertEquals("ListType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall5b() throws Exception {
        Call call = getCall("[1,2,3].^");
        assertEquals("ListType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6() throws Exception {
        Call call = getCall("{:x=>:y}.foo^");
        assertEquals("DictType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall6b() throws Exception {
        Call call = getCall("{:x=>:y}.^");
        assertEquals("DictType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2() throws Exception {
        Call call = getCall("True.foo^");
        assertEquals("BooleanType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll2b() throws Exception {
        Call call = getCall("True.^");
        assertEquals("BooleanType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3() throws Exception {
        Call call = getCall("False.foo^");
        assertEquals("BooleanType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCalll3b() throws Exception {
        Call call = getCall("False.^");
        assertEquals("BooleanType", call.getType());
        assertFalse(call.isSimpleIdentifier());
        assertFalse(call.isStatic());
    }

    public void testCall14() throws Exception {
        Call call = getCall("self.foo^");
        assertEquals("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCall14b() throws Exception {
        Call call = getCall("self.^");
        assertEquals("self", call.getType());
        assertEquals("self", call.getLhs());
    }

    public void testCalll5() throws Exception {
        Call call = getCall("super.foo^");
        assertEquals("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCalll5b() throws Exception {
        Call call = getCall("super.^");
        assertEquals("super", call.getType());
        assertEquals("super", call.getLhs());
    }

    public void testCall20() throws Exception {
        Call call = getCall("foo.bar.ex^");
        assertEquals("foo.bar", call.getLhs());
        assertEquals(null, call.getType());
    }

    public void testCallUnknown() throws Exception {
        Call call = getCall("getFoo().x^");
        assertSame(Call.UNKNOWN, call);
    }

    public void testCallLocal() throws Exception {
        Call call = getCall("foo^");
        assertSame(Call.LOCAL, call);
    }

    public void testCallOperator1() throws Exception {
        Call call = getCall("=foo^");
        assertSame(Call.LOCAL, call);
    }

    public void testCallOperator2() throws Exception {
        Call call = getCall("+foo^");
        assertSame(Call.LOCAL, call);
    }

    public void testCallOperator3() throws Exception {
        Call call = getCall("=SocketServer.^");
        assertEquals("SocketServer", call.getLhs());
        assertEquals("SocketServer", call.getType());
    }

    public void testParen() throws Exception {
        Call call = getCall("(SocketServer.^");
        assertEquals("SocketServer", call.getLhs());
        assertEquals("SocketServer", call.getType());
    }

    public void testParen2() throws Exception {
        Call call = getCall("[SocketServer.^");
        assertEquals("SocketServer", call.getLhs());
        assertEquals("SocketServer", call.getType());
    }

    // TODO - test complex numbers and tuples, lists and dicts,
    // etc. See http://www.python.org/doc/2.5.2/lib/module-types.html
}
