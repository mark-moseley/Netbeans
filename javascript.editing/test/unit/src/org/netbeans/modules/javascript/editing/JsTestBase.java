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

package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import java.util.Map;
import java.util.prefs.Preferences;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * @author Tor Norbye
 */
public abstract class JsTestBase extends CslTestBase {

    public JsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JsLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }
    
    @Override
    protected Parser getParser() {
        JsParser.runtimeException = null;
        return super.getParser();
    }

    @Override
    protected void validateParserResult(ParserResult result) {
        if (JsParser.runtimeException != null) {
            JsParser.runtimeException.printStackTrace();
        }
        JsTestBase.assertNull(JsParser.runtimeException != null ? JsParser.runtimeException.toString() : "", JsParser.runtimeException);
    }
    
    @Override
    protected void setUp() throws Exception {
        JsIndexer.setClusterUrl("file:/bogus"); // No translation
        super.setUp();
    }
    
    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(4,4);
        }

        Preferences prefs = MimeLookup.getLookup(MimePath.get(JsTokenId.JAVASCRIPT_MIME_TYPE)).lookup(Preferences.class);
        prefs.putInt(SimpleValueNames.SPACES_PER_TAB, preferences.getIndentation());
        
        JsFormatter formatter = new JsFormatter();
        
        return formatter;
    }

// XXX: parsingapi
//    // Called via reflection from GsfUtilities. This is necessary because
//    // during tests, going from a FileObject to a BaseDocument only works
//    // if all the correct data loaders are installed and working - and that
//    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
//    // If anyone can figure this out, please let me know and simplify the
//    // test infrastructure.
//    public static BaseDocument getDocumentFor(FileObject fo) {
//        BaseDocument doc = GsfTestBase.createDocument(read(fo));
//        doc.putProperty(org.netbeans.api.lexer.Language.class, JsTokenId.language());
//        doc.putProperty("mimeType", JsTokenId.JAVASCRIPT_MIME_TYPE);
//
//        return doc;
//    }
    
    protected String[] JAVASCRIPT_TEST_FILES = new String[] {
        "testfiles/arraytype.js",
        "testfiles/bubble.js",
        "testfiles/class-inheritance-ext.js",
        "testfiles/class-via-function.js",
        "testfiles/classes.js",
        "testfiles/classprops.js",
        "testfiles/completion/lib/comments.js",
        "testfiles/completion/lib/expressions.js",
        "testfiles/completion/lib/expressions2.js",
        "testfiles/completion/lib/expressions3.js",
        "testfiles/completion/lib/expressions4.js",
        "testfiles/completion/lib/expressions5.js",
        "testfiles/completion/lib/test1.js",
        "testfiles/completion/lib/test129036.js",
        "testfiles/completion/lib/test2.js",
        "testfiles/completion/lib/yahoo.js",
        "testfiles/dnd.js",
        "testfiles/dragdrop.js",
        "testfiles/e4x.js",
        "testfiles/e4x2.js",
        "testfiles/e4xexample1.js",
        "testfiles/e4xexample2.js",
        "testfiles/embedding/convertscript.html.js",
        "testfiles/embedding/embed124916.erb.js",
        "testfiles/embedding/fileinclusion.html.js",
        "testfiles/embedding/mixed.erb.js",
        "testfiles/embedding/rails-index.html.js",
        "testfiles/embedding/sideeffects.html.js",
        "testfiles/embedding/yuisample.html.js",
        "testfiles/events.js",
        "testfiles/fileinclusion.html.js",
        "testfiles/indexable/dojo.js",
        "testfiles/indexable/dojo.uncompressed.js",
        "testfiles/indexable/ext-all-debug.js",
        "testfiles/indexable/ext-all.js",
        "testfiles/indexable/foo.js",
        "testfiles/indexable/foo.min.js",
        "testfiles/indexable/lib.js",
        "testfiles/indexable/yui-debug.js",
        "testfiles/indexable/yui-min.js",
        "testfiles/indexable/yui.js",
        "testfiles/jmaki-uncompressed.js",
        "testfiles/jsexample1.js",
        "testfiles/newstyle-prototype.js",
        "testfiles/occurrences.js",
        "testfiles/occurrences2.js",
        "testfiles/oldstyle-prototype.js",
        "testfiles/orig-dojo.js.uncompressed.js",
        "testfiles/prototype-new.js",
        "testfiles/prototype.js",
        "testfiles/rename.js",
        "testfiles/returntypes.js",
        "testfiles/semantic1.js",
        "testfiles/semantic2.js",
        "testfiles/semantic3.js",
        "testfiles/semantic4.js",
        "testfiles/semantic5.js",
        "testfiles/semantic6.js",
        "testfiles/semantic7.js",
        "testfiles/simple.js",
        "testfiles/SpryAccordion.js",
        "testfiles/SpryData.js",
        "testfiles/SpryEffects.js",
        "testfiles/SpryXML.js",
        "testfiles/stub_dom2_Node.js",
        "testfiles/stub_dom_Window.js",
        "testfiles/stub_Element.js",
        "testfiles/switches.js",
        "testfiles/tryblocks.js",
        "testfiles/two-names.js",
        "testfiles/types1.js",
        "testfiles/types2.js",
        "testfiles/woodstock-body.js",
        "testfiles/woodstock2.js",
        "testfiles/yui-anim.js",
        "testfiles/yui.js",
    };

    @Override
    protected void assertEquals(String message, BaseDocument doc, ParserResult expected, ParserResult actual) throws Exception {
        Node expectedRoot = ((JsParseResult)expected).getRootNode();
        Node actualRoot = ((JsParseResult)actual).getRootNode();
        assertEquals(doc, expectedRoot, actualRoot);
    }

    private boolean assertEquals(BaseDocument doc, Node expected, Node actual) throws Exception {
        assertEquals(expected.hasChildren(), actual.hasChildren());
        if (expected.getType() != actual.getType() ||
                expected.hasChildren() != actual.hasChildren() /* ||
                expected.getSourceStart() != actual.getSourceStart() ||
                expected.getSourceEnd() != actual.getSourceEnd()*/
                ) {
            String s = null;
            Node curr = expected;
            while (curr != null) {
                String desc = curr.toString();
                int start = curr.getSourceStart();
                int line = Utilities.getLineOffset(doc, start);
                desc = desc + " (line " + line + ")";
                if (curr.getType() == Token.FUNCTION) {
                    String name = null;
                    Node label = ((FunctionNode)curr).labelNode;
                    if (label != null) {
                        name = label.getString();
                    } else {
                        for (Node child = curr.getFirstChild(); child != null; child = child.getNext()) {
                            if (child.getType() == Token.FUNCNAME) {
                                desc = child.getString();
                                break;
                            }
                        }
                    }
                    if (name != null) {
                        desc = desc + " : " + name + "()";
                    }
                } else if (curr.getType() == Token.OBJECTLIT) {
                    String[] names = AstUtilities.getObjectLitFqn(curr);
                    if (names != null) {
                        desc = desc + " : " + names[0];
                    }
                }
                if (s == null) {
                    s = desc;
                } else {
                    s = desc + " - " + s;
                }
                curr = curr.getParentNode();
            }
            fail("node mismatch: Expected=" + expected + ", Actual=" + actual + "; path=" + s);
        }

        if (expected.hasChildren()) {
            for (Node expectedChild = expected.getFirstChild(),
                    actualChild = actual.getFirstChild();
                    expectedChild != null; expectedChild = expectedChild.getNext(), actualChild = actualChild.getNext()) {
                assertEquals(expectedChild.getNext() != null, actualChild.getNext() != null);
                assertEquals(doc, expectedChild, actualChild);
            }
        }

        return true;
    }
// XXX: parsingapi
//    @Override
//    protected void verifyIncremental(ParserResult result, EditHistory history, ParserResult oldResult) {
//        JsParseResult pr = (JsParseResult)result;
//        assertNotNull(pr.getIncrementalParse());
//        assertNotNull(pr.getIncrementalParse().newFunction);
//    }
}
