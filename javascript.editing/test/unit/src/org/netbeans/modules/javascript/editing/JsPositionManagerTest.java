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

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 */
public class JsPositionManagerTest extends JsTestBase {

    public JsPositionManagerTest(String name) {
        super(name);
    }

    private void addAll(Node node, List<Node> list) {
        list.add(node);
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addAll(child, list);
            }
        }
    }

    public void testGetPosition1() throws Exception {
        FileObject f = getTestFile("testfiles/prototype-new.js");
        Source source = Source.create(f);

        final ElementHandle [] handle = new ElementHandle[] { null };
        final OffsetRange [] range = new OffsetRange[] { null };
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);

                int snapshotLength = jspr.getSnapshot().getText().length();
                Node root = jspr.getRootNode();
                assertNotNull(root);

                List<Node> nodes = new ArrayList<Node>();
                addAll(root, nodes);
                for (Node node : nodes) {
                    AstElement element = AstElement.getElement(jspr, node);
                    if (element != null) {
                        int startOffset = element.getNode().getSourceStart();
                        assertTrue("Invalid start offset: " + startOffset + "; element: " + element,
                                startOffset >= 0 && startOffset < snapshotLength);

                        int endOffset = element.getNode().getSourceEnd();
                        assertTrue("Invalid end offset: " + endOffset + "; element: " + element,
                                endOffset >= startOffset && endOffset < snapshotLength);
                    }
                }

                // Look for one specific known case
                nodes.clear();
                AstUtilities.addNodesByType(root, new int[] { Token.FUNCNAME }, nodes);
                boolean found = false;
                for (Node node : nodes) {
                    if (node.getString().equals("toQueryPair")) {
                        found = true;
                        Node func = node.getParentNode();
                        assertNotNull(func);
                        assertTrue(func instanceof FunctionNode);
                        AstElement element = AstElement.getElement(jspr, func);
                        if (element != null) {
                            String t = jspr.getSnapshot().getText().toString();
                            int expected = t.indexOf("function toQueryPair");
                            assertTrue(expected != -1);
                            assertEquals(expected, element.getNode().getSourceStart());

                            handle[0] = element;
                            range[0] = element.getOffsetRange(jspr);
                        }
                        break;
                    }
                }
                assertTrue(found);

            }
        });


        assertNotNull(handle[0]);
        assertNotNull(range[0]);

        // Totally mismatched info (so resolving handles won't work)
        // ...obtain a reasonable position anyway (from old info)
        Source source2 = getTestSource(getTestFile("testfiles/rename.js"));
        ParserManager.parse(Collections.singleton(source2), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);

                OffsetRange newRange = handle[0].getOffsetRange(jspr);
                assertEquals(range[0], newRange);
            }
        });

    }

}