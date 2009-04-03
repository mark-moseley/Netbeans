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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.html.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/**Html completion test
 * This class extends TestBase class which provides access to the html editor module layer
 *
 * @author Marek Fukala
 */
public class HtmlCompletionQueryTest extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public static enum Match {

        EXACT, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY;
    }

    public HtmlCompletionQueryTest() throws IOException, BadLocationException {
        super("htmlsyntaxsupporttest");
        NbReaderProvider.setupReaders(); //initialize DTD providers
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockMimeLookup.class);
    }

    //test methods -----------
    public void testIndexHtml() throws IOException, BadLocationException {
        testCompletionResults("index.html");
    }

    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
        testCompletionResults("netbeans.org.html");
    }
    
    public void testTags() {
        assertItems("<|", arr("div"), Match.CONTAINS);
        assertItems("<|", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<d|", arr("div"), Match.CONTAINS);
        assertItems("<div|", arr("div"), Match.EXACT);

        assertItems("<div></|", arr("div"), Match.CONTAINS);
        assertItems("<div></d|", arr("div"), Match.CONTAINS);
        assertItems("<div></div|", arr("div"), Match.EXACT);
    }

    public void testCompleteTags() throws BadLocationException {
        assertCompletedText("<|", "div", "<div>|");
        assertCompletedText("<di|", "div", "<div>|");
        assertCompletedText("<div|", "div", "<div>|");

        assertCompletedText("<div></|", "div", "<div></div>|");
        assertCompletedText("<div></d|", "div", "<div></div>|");
        assertCompletedText("<div></div|", "div", "<div></div>|");
    }

    public void testTagAttributes() {
        assertItems("<div |", arr("align"), Match.CONTAINS);
        assertItems("<div |", arr("jindra"), Match.DOES_NOT_CONTAIN);
        assertItems("<div a|", arr("align"), Match.CONTAINS);
    }

    public void testCompleteTagAttributes() throws BadLocationException {
        assertCompletedText("<div |", "align", "<div align=\"|\"");
        assertCompletedText("<div a|", "align", "<div align=\"|\"");
    }

    public void testTagAttributeValues() {
        assertItems("<div align=\"|\"", arr("center"), Match.CONTAINS);
        assertItems("<div align=\"ce|\"", arr("center"), Match.CONTAINS);
        assertItems("<div align=\"center|\"", arr("center"), Match.EXACT);
    }

    public void testCompleteTagAttributeValues() throws BadLocationException {
        assertCompletedText("<div align=\"|\"", "center", "<div align=\"center|\"");
        assertCompletedText("<div align=\"ce|\"", "center", "<div align=\"center|\"");

        //regression test - issue #161852
        assertCompletedText("<div align=\"|\"", "left", "<div align=\"left|\"");

        //test single quote
        assertCompletedText("<div align='|'", "center", "<div align='center'|");

        //test values cc without quotation
        assertCompletedText("<div align=|", "left", "<div align=left|");
        assertCompletedText("<div align=ri|", "right", "<div align=right|");
    }

    public void testCharacterReferences() throws BadLocationException {
        assertItems("&|", arr("amp"), Match.CONTAINS);
        assertItems("&a|", arr("amp"), Match.CONTAINS);
        assertItems("&amp|", arr("amp"), Match.EXACT);

        assertCompletedText("&|", "amp", "&amp;|");
        assertCompletedText("&am|", "amp", "&amp;|");
    }

    public void testBooleanAttributes() throws BadLocationException {
        assertItems("<input d|", arr("disabled"), Match.CONTAINS);
        assertCompletedText("<input d|", "disabled", "<input disabled|");
    }

    //helper methods ------------
    private void assertItems(String documentText, final String[] expectedItemsNames, final Match type) {
        StringBuffer content = new StringBuffer(documentText);

        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";

        //remove the pipe
        content.deleteCharAt(pipeOffset);
        Document doc = getDocument(content.toString());

        HtmlCompletionQuery query = new HtmlCompletionQuery();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        List<CompletionItem> items = query.query(component, pipeOffset);

        assertCompletionItemNames(expectedItemsNames, items, type);

    }

    private void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException {
        StringBuffer content = new StringBuffer(documentText);
        final int pipeOffset = content.indexOf("|");
        assert pipeOffset >= 0 : "define caret position by pipe character in the document source!";
        //remove the pipe
        content.deleteCharAt(pipeOffset);

        StringBuffer expectedContent = new StringBuffer(expectedText);
        final int expectedPipeOffset = expectedContent.indexOf("|");
        assert expectedPipeOffset >= 0 : "define caret position by pipe character in the expected text!";
        //remove the pipe
        expectedContent.deleteCharAt(expectedPipeOffset);

        Document doc = getDocument(content.toString());

        HtmlCompletionQuery query = new HtmlCompletionQuery();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        component.getCaret().setDot(pipeOffset);
        List<CompletionItem> items = query.query(component, pipeOffset);

        assertNotNull(items);

        CompletionItem item = null;
        for (CompletionItem ci : items) {
            if (ci instanceof HtmlCompletionItem) {
                HtmlCompletionItem htmlci = (HtmlCompletionItem) ci;
                if(htmlci.getItemText().equals(itemToCompleteName)) {
                    item = ci; //found
                    break;
                }
            }
        }

        assertNotNull(item);
        assertTrue(item instanceof HtmlCompletionItem);

        item.defaultAction(component); //complete

        assertEquals(expectedContent.toString(), doc.getText(0, doc.getLength()));
//        assertEquals(expectedPipeOffset, component.getCaret().getDot());

    }

    private void assertCompletionItemNames(String[] expected, List<CompletionItem> ccresult, Match type) {
        Collection<String> real = new ArrayList<String>();
        for (CompletionItem ccp : ccresult) {
            //check only html items
            if (ccp instanceof HtmlCompletionItem) {
                HtmlCompletionItem htmlci = (HtmlCompletionItem) ccp;
                real.add(htmlci.getItemText());
            }
        }
        Collection<String> exp = new ArrayList<String>(Arrays.asList(expected));

        if (type == Match.EXACT) {
            assertEquals(exp, real);
        } else if (type == Match.CONTAINS) {
            exp.removeAll(real);
            assertEquals(Collections.EMPTY_LIST, exp);
        } else if (type == Match.EMPTY) {
            assertEquals(0, real.size());
        } else if (type == Match.NOT_EMPTY) {
            assertTrue(real.size() > 0);
        } else if (type == Match.DOES_NOT_CONTAIN) {
            int originalRealSize = real.size();
            real.removeAll(exp);
            assertEquals(originalRealSize, real.size());
        }

    }

    private String[] arr(String... args) {
        return args;
    }

    private void testCompletionResults(String testFile) throws IOException, BadLocationException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFile);
        BaseDocument doc = getDocument(source);
        HtmlCompletionQuery query = new HtmlCompletionQuery();
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);

        StringBuffer output = new StringBuffer();
        for (int i = 0; i < doc.getLength(); i++) {
            List<CompletionItem> result = query.query(component, i);
            if (result != null) {
                output.append(i + ":");
                output.append('[');
                Iterator<CompletionItem> itr = result.iterator();
                while (itr.hasNext()) {
                    CompletionItem ci = itr.next();
                    if (ci instanceof HtmlCompletionItem) {
                        //test only html completion items
                        HtmlCompletionItem htmlci = (HtmlCompletionItem) ci;
                        output.append(htmlci.getItemText());
                        if (itr.hasNext()) {
                            output.append(',');
                        }
                    }
                }
                output.append(']');
                output.append('\n');
            }
        }

        assertDescriptionMatches(source, output.toString(), false, ".pass", true);

    }
}
