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
package org.netbeans.test.syntax;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Jindrich Sedek
 */
public class CommentActionTest extends J2eeTestCase {

    private static final Boolean GENERATE_GOLDEN_FILES = true;
    private static final String PROJECT_DIR_NAME = "CommentTestProject";
    private static boolean firstRun = true;

    public CommentActionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (firstRun) {
            File projectDir = new File(getDataDir(), PROJECT_DIR_NAME);
            openProjects(projectDir.getAbsolutePath());
            firstRun = false;
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (GENERATE_GOLDEN_FILES) {
            CompletionTest.generateGoldenFiles(this);
        } else {
            compareReferenceFiles();
        }
    }

    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, CommentActionTest.class);
    }

    public void testHTMLSection() throws Exception {
        doSectionTest("testHTML.html", 4, 10);
    }

    public void testHTMLOneLine() throws Exception {
        doLineTest("testHTML.html", 8, 1);
    }

    public void testHTMLInsideOneLine() throws Exception {
        doLineTest("testHTML.html", 10, 10);
    }

    public void testJSPSection() throws Exception {
        doSectionTest("testJSP.jsp", 4, 10);
    }

    public void testJSPOneLine() throws Exception {
        doLineTest("testJSP.jsp", 8, 1);
    }

    public void testJSPInsideOneLine() throws Exception {
        doLineTest("testJSP.jsp", 10, 10);
    }

    public void testCSSSection() throws Exception {
        doSectionTest("testCSS.css", 4, 10);
    }

    public void testCSSOneLine() throws Exception {
        doLineTest("testCSS.css", 8, 1);
    }

    public void testCSSInsideOneLine() throws Exception {
        doLineTest("testCSS.css", 10, 10);
    }

    private void doSectionTest(String fileName, int beginLineNubmer, int endLineNumber) throws DataObjectNotFoundException {
        EditorOperator op = openFile(fileName);
        op.select(beginLineNubmer, endLineNumber);
        op.pushKey(KeyEvent.VK_SLASH, InputEvent.CTRL_MASK);
        op.waitModified(true);
        ref(op.getText());
    }

    private void doLineTest(String fileName, int lineNumber, int column) throws DataObjectNotFoundException {
        EditorOperator op = openFile(fileName);
        op.setCaretPosition(lineNumber, column);
        op.pushKey(KeyEvent.VK_SLASH, InputEvent.CTRL_MASK);
        op.waitModified(true);
        ref(op.getText());
    }

    private EditorOperator openFile(String fileName) throws DataObjectNotFoundException {
        FileObject project = FileUtil.toFileObject(new File(getDataDir(), PROJECT_DIR_NAME));
        FileObject file = project.getFileObject("web").getFileObject(fileName);
        DataObject dataObj = DataObject.find(file);
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(fileName);
    }
}


