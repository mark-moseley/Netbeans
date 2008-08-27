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
package org.netbeans.test.bookmarks;

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Element;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;

/**
 * Test of typing at begining/end and other typing tests.
 *
 * @author Miloslav Metelka
 */
public class BookmarksPersistenceTest extends EditorBookmarksTestCase {

    public BookmarksPersistenceTest(String testMethodName) {
        super(testMethodName);
    }

    public void testPersistence() {
        int[] bookmarkLines = new int[]{1, 7, 9};

        openDefaultProject();

        openDefaultSampleFile();
        try {

            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();

            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                new EventTool().waitNoEvent(1000);
            }

        } finally {
            closeFileWithDiscard();
        }

        openDefaultSampleFile();
        try {

            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, bookmarkLines);

        } finally {
            closeFileWithDiscard();
        }
    }

    public void testBookmarkMove() {
        int bookmarkLine = 14;
        int lineToDelete = 12;

        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            editorOper.setCaretPosition(getLineOffset(doc, bookmarkLine));
            txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            editorOper.setCaretPosition(getLineOffset(doc, lineToDelete));
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK);
            doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, new int[]{bookmarkLine - 1});
        } finally {
            closeFileWithDiscard();
        }
    }

//    public void testBookmarkMerge() {
//        int[] bookmarkLines = new int[]{9, 10, 11};
//
//        openDefaultProject();
//
//        openDefaultSampleFile();
//        try {
//            EditorOperator editorOper = getDefaultSampleEditorOperator();
//            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
//            Document doc = txtOper.getDocument();
//            for (int i = 0; i < bookmarkLines.length; i++) {
//                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
//                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            }
//            editorOper.setCaretPosition(bookmarkLines[0] + 1, 1);
//            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
//            BookmarkList bml = BookmarkList.get(doc);
//            checkBookmarksAtLines(bml, new int[]{9, 9, 9});
//        } finally {
//            closeFileWithDiscard();
//        }
//    }

    public void testNextBookmark() {
        int[] bookmarkLines = new int[]{9, 10, 11};
        int[] expectedLines = new int[]{9, 10, 11, 9};

        openDefaultProject();

        openDefaultSampleFile();
        try {

            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc, 2));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_PERIOD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                int j = expectedLines[i];
                assertEquals("Caret is at bad location", j, actLine);
            }
        } finally {
            closeFileWithDiscard();
        }
    }

    public void testPreviousBookmark() {
        int[] bookmarkLines = new int[]{9, 10, 11};
        int[] expectedLines = new int[]{11, 10, 9, 11};

        openDefaultProject();

        openDefaultSampleFile();
        try {

            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(bookmarkLines[i] + 1, 1);
                txtOper.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc, 14));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int j = expectedLines[i];
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                assertEquals("Caret is at bad location", j, actLine);
            }
        } finally {
            closeFileWithDiscard();
        }
    }

    private void checkBookmarksAtLines(BookmarkList bookmarkList, int[] expectedLineIndexes) {
        List<Bookmark> bookmarks = bookmarkList.getBookmarks ();
        assertEquals("Invalid bookmark count", expectedLineIndexes.length, bookmarks.size ());
        for (int i = 0; i < expectedLineIndexes.length; i++) {
            int expectedLineIndex = expectedLineIndexes[i];
            int lineIndex = bookmarks.get (i).getLineNumber();
            assertEquals("Bookmark line index " + lineIndex + " differs from expected " + expectedLineIndex+ "index "+i,
                    lineIndex,
                    expectedLineIndex);
        }
    }

    private int getLineOffset(Document doc, int lineIndex) {
        Element root = doc.getDefaultRootElement();
        return root.getElement(lineIndex).getStartOffset();
    }

    private int getLineIndex(Document doc, int offset) {
        Element root = doc.getDefaultRootElement();
        return root.getElementIndex(offset);
    }

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("#### " + getName() + " starts ####");
    }

    protected void tearDown() throws Exception {
        System.out.println("#### " + getName() + " ends ####");
        super.tearDown();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BookmarksPersistenceTest.class).enableModules(".*").clusters(".*")
    

);
    }
}
