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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.syntax;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JSpinner;
import junit.framework.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class IndentCasesTest extends J2eeTestCase {

    private static File projectDir;
    private boolean debug = false;
    private BaseDocument doc;
    private static boolean projectsOpened = false;

    public IndentCasesTest() {
        super("IndentationTesting");
    }
   
    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, IndentCasesTest.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!projectsOpened){
            File dataDir = getDataDir();
            projectDir = new File(dataDir, "IndentationTestProjects/IndentationTest");
            projectDir = projectDir.getAbsoluteFile();
            openProjects(projectDir.getAbsolutePath());
            resolveServer(projectDir.getName());
            setIndent(5);
            setIndent(4);
            projectsOpened = true;
            openFile("indentationTest.jsp");
            openFile("indentationTest.html");
            Thread.sleep(10000);
        }
    }

    private void setIndent(int number){
        OptionsOperator options = OptionsOperator.invoke();
        options.selectEditor();
        new JTabbedPaneOperator(options).selectPage("Indentation");
        JLabelOperator label = new JLabelOperator(options, "Number");
        JSpinner spinner = (JSpinner) label.getLabelFor();
        JSpinnerOperator spinnerOp = new JSpinnerOperator(spinner);
        spinnerOp.getNumberSpinner().scrollToValue(number);
        options.ok();
    }

    public void testJSPFirstLineIndent() throws Exception {
        testJSP(5, 1, 6, 1);
    }
    
    public void testJSPTagEndLine() throws Exception {
        testJSP(5, 7, 6, 5);
    }

//issue 120136
//    public void testJSPAttribute() throws Exception {
//        testJSP(8, 15, 9, 13);
//    }
//
//    public void testJSPAttribute2() throws Exception {
//        testJSP(8, 41, 9, 15);
//    }

    public void testJSPSmartEnter() throws Exception {
        testJSP(22, 21, 23, 21);
    }

    public void testJSPOpenTagIndent() throws Exception {
        testJSP(23, 21, 24, 21);
    }

    public void testJSPEmbeddedCSS1() throws Exception {
        testJSP(10, 16, 11, 17);
    }

    public void testJSPEmbeddedCSS2() throws Exception {
        testJSP(11, 30, 12, 17);
    }

    public void testJSPScriptletStart() throws Exception {
        testJSP(29, 11, 30, 9);
    }

    public void testJSPScriptletIfBlock() throws Exception {
        testJSP(30, 19, 31, 13);
    }

    public void testJSPScriptletForBlock() throws Exception {
        testJSP(31, 44, 32, 17);
    }
    
    public void testJSPScriptletClosingBracket() throws Exception {
        testJSP(33, 14, 34, 13);
    }

    public void testHTMLFirstLineIndent() throws Exception {
        testHTML(1, 1, 2, 1);
    }
    
    public void testHTMLTagEndLine() throws Exception {
        testHTML(1, 7, 2, 5);
    }
    
//issue 120136
//    public void testHTMLAttribute() throws Exception {
//        testHTML(4, 15, 5, 13);
//    }
//
//    public void testHTMLAttribute2() throws Exception {
//        testHTML(4, 41, 5, 13);
//    }

    public void testHTMLSmartEnter() throws Exception {
        testHTML(14, 21, 15, 21);
    }
    
    public void testHTMLOpenTagIndent() throws Exception {
        testHTML(19, 21, 20, 21);
    }
    
    public void testHTMLEmbeddedCSS1() throws Exception {
        testHTML(6, 16, 7, 17);
    }

    public void testHTMLEmbeddedCSS2() throws Exception {
        testHTML(7, 30, 8, 17);
    }

    private void testJSP(int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        test("indentationTest.jsp", lineNum, offset, endLineNum, endOffset);
    }

    private void testHTML(int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        test("indentationTest.html", lineNum, offset, endLineNum, endOffset);
    }

    private void test(String fileName, int lineNum, int offset, int endLineNum, int endOffset) throws Exception {
        EditorOperator.closeDiscardAll();
        EditorOperator op = openFile(fileName);
        op.setCaretPositionToLine(lineNum);
        op.setCaretPositionRelative(offset - 1);
        if (debug) {
            Thread.sleep(3000); // to be visible ;-)
        }
        op.pressKey(KeyEvent.VK_ENTER);
        op.waitModified(true);
        int newPossition = op.txtEditorPane().getCaretPosition();
        int newLine = Utilities.getLineOffset(doc, newPossition) + 1;
        int newOffset = newPossition - Utilities.getRowStart(doc, newPossition);
        if (debug) {
            Thread.sleep(3000); // to be visible ;-)
        }
        assertEquals("FINAL POSSITION", endLineNum, newLine);
        assertEquals("FINAL POSSITION", endOffset - 1, newOffset);
    }
    
    private EditorOperator openFile(String fileName) throws DataObjectNotFoundException, IOException {
        File file = new File(new File(projectDir, "web"), fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        doc = (BaseDocument) ed.openDocument();
        ed.open();
        EditorOperator operator = new EditorOperator(fileName);
        return operator;
    }

}
