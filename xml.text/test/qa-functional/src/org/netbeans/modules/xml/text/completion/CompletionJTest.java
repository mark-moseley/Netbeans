/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.completion;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.textui.TestRunner;
import org.netbeans.editor.ext.ListCompletionView;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.xml.text.syntax.XMLOptions;
import org.netbeans.tests.xml.JXTest;
import org.openide.loaders.DataObject;
import org.openide.options.SystemOption;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: NewFromTemplate
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test tests New From Template action on all XML's templates.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create new documents from template<BR>
 * 2) write the created documents to output<BR>
 * 3) close source editor<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Set XML documents.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on Januar 09, 2001, 12:33 PM
 * <P>
 */

public class CompletionJTest extends JXTest {
    private static int NO_WAIT = 0;
    private static int EMPTY = 1;
    private static int NO_EMPTY = 2;
    
    
    int counter;
    int col;
    EditorOperator editor;
    JTextComponentOperator text;
    
    /** Creates new CoreTemplatesTest */
    public CompletionJTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        String folder = getFilesystemName() + DELIM + getDataPackageName(DELIM);
        // catalog is only real template in the module :-)
        String name = "OASIS XML Catalog";
        String ext = "xml";
        
        XMLOptions options = (XMLOptions) SystemOption.findObject(XMLOptions.class, true);
        options.setCompletionAutoPopup(false);
        
        DataObject dao = TestUtil.THIS.findData(name + "." + ext);
        if (dao != null) dao.delete();
        NewWizardOperator.create("XML" + DELIM + name, folder, name);
        editor = new EditorOperator(name);
        text = new JTextComponentOperator(editor);

        clearText();
        insert(""
        + "<?xml version='1.0' encoding='UTF-8'?>\n"
        + "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'\n"
        + "'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n"
        + "<h");
        save();
        //tml>
        showCompl();
        enter();
        insert(">\n");
        //<head>
        insertTag("<", ">\n", 0);
        //<title>Test page</title>
        insertTag("<t", "Test page", -1);
        end();
        insert("\n");
        //</head>
        insertTag("</", "\n", -1);
        //<body>
        insertTag("<", ">\n", 0);
        //<h1 title="test">Test</h1>
        insertTag("<h", " ", 0);
        insertTag("t", "test\">Test", -1);
        insertTag("</", "\n", -1);
        //<table border="1">
        insertTag("<t", " ", 0);
        insertTag("b", "1\">\n", 1);
        //<tr align="center">
        insertTag("<t", " ", 4);
        insertTag("a", "center\">\n", -1);
        //<td>1</td><td>2</td>
        insertTag("<td", "1", -1);
        end();
        insertTag("<td", "2", -1);
        end();
        insert("\n");
        //</tr>
        insertTag("</", "\n", -1);
        //</table>
        insertTag("</", "\n", -1);
        //</body>
        insertTag("</", "\n", -1);
        //</html>
        insertTag("</", "\n", -1);
    }
    
    void insertTag(String pref, String suf, int index) {
        insert(pref);
        if (index < 0) {
            showCompl(NO_WAIT);
        } else {
            showCompl(NO_EMPTY);
        }
        for (int i = 0; i < index; i++) {
            down();
        }
        if (index > -1) {
            enter();
        }
        //!!! completion needn't finish on time
        sleepTest(500); 
        insert(suf);
    }
    
    private void insert(String txt) {
        editor.txtEditorPane().typeText(txt);
    }
    
    private void move(int x, int y) {
        col += y;
        editor.setCaretPosition(editor.getLineNumber() + x, col);
    }
    
    private void hMove(int len) {
        editor.setCaretPosition(editor.getLineNumber() + len, col);
    }
    
    private void vMove(int len) {
        col += len;
        editor.setCaretPosition(editor.getLineNumber(), col);
    }
    
    private void goTo(int x, int y) {
        editor.setCaretPosition(x, y);
    }
    
    private void save() {
        editor.save();
    }
    
    private void clearText() {
        col = 0;
        text.clearText();
    }
    
    private void showCompl() {
        showCompl(NO_EMPTY);
    }
    
    private void showCompl(int mode) {
        editor.pressKey(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
        if (mode == NO_EMPTY) {
            waitCompl(1);
        } else if (mode == EMPTY) {
            waitCompl(0);
        }
    }
        
    private void waitCompl(int minSize) {
        CompletionChooser completionChoser = new CompletionChooser(minSize);
        ListCompletionView completionView = (ListCompletionView) ComponentOperator
        .waitComponent((Container) editor.getWindowContainerOperator().getSource()
        , completionChoser);
        int size = completionView.getModel().getSize();
        System.out.println(counter + "- Model size: " + size);
    }
    
    private void checkCompletion(int minSize) {
        showCompl(NO_WAIT);
        waitCompl(minSize);
        esc();
    }
    
    private class CompletionChooser implements ComponentChooser {
        int minSize;
        
        public CompletionChooser() {
            this(0);
        }
        
        public CompletionChooser(int minSize) {
            this.minSize = minSize;
        }
        
        public boolean checkComponent(Component component) {
            //System.out.println("> " + component);
            
            if (component instanceof ListCompletionView) {
                ListCompletionView cmpl = (ListCompletionView) component;
                if (cmpl.getModel().getSize() >= minSize) return true;
            }
            return false;
        }
        
        public String getDescription() {
            return("Instace of ScrollCompletionPane");
        }
    }
    
    // KEYS
    
    private void delete(int len) {
        editor.delete(len);
    }
    
    private void backSp(int len) {
        for (int i = 0; i < len; i++) {
            editor.pushKey(KeyEvent.VK_BACK_SPACE);
        }
    }
    
    private void esc() {
        editor.pressKey(KeyEvent.VK_ESCAPE);
    }
    
    private void enter() {
        editor.pressKey(KeyEvent.VK_ENTER);
    }
    
    private void down() {
        editor.pressKey(KeyEvent.VK_DOWN);
    }
    
    private void up() {
        editor.pressKey(KeyEvent.VK_UP);
    }
    
    private void left() {
        editor.pressKey(KeyEvent.VK_LEFT);
    }
    
    private void right() {
        editor.pressKey(KeyEvent.VK_RIGHT);
    }
    
    private void end() {
        editor.pressKey(KeyEvent.VK_END);
    }
    
    public static void main(String[] args) {
        //JamController.setFast(true);
        DEBUG = true;
        TestRunner.run(CompletionJTest.class);
    }
}
