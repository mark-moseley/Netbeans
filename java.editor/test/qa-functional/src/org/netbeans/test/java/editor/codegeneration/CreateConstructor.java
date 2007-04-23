/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.test.java.editor.codegeneration;

import java.util.Enumeration;
import junit.textui.TestRunner;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.java.editor.jelly.GenerateCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateConstructorOperator;
import org.openide.actions.UndoAction;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class CreateConstructor extends EditorTestCase {
    
    private JEditorPaneOperator txtOper;
    private EditorOperator editor;
    
    /** Creates a new instance of CreateConstructor */
    public CreateConstructor(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");
        //openDefaultProject();
        //openDefaultSampleFile();
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private boolean contains(String text,String pattern) {
        return text.contains(pattern);
    }
    
    private class EditorValueResolver implements ValueResolver {
        
        String text;
        
        public EditorValueResolver(String text) {
            this.text = text;
        }
        
        public Object getValue() {
            return editor.getText().contains(text);
        }
        
    }
    
    private boolean isWin() {
        return System.getProperty("os.name").contains("Windows");
    }
    
    public void testSuperConstructor() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(2);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(ThreadGroup group, Runnable target) {\n"+
                    "        super(group, target);\n"+
                    "    }\n";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testInitFields() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(1);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(int b) {\n"+
                    "        this.b = b;\n"+
                    "    }\n";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testInitFieldAndSuper() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(7);
            jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(2);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(ThreadGroup group, Runnable target, String name, long stackSize, double c) {\n"+
                    "        super(group, target, name, stackSize);\n"+
                    "        this.c = c;\n"+
                    "    }\n";
            if(isWin()) expected = expected.replace("\n", "\r\n");           
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testMultipleSuperSelection() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(2);
            jto.selectRow(3);
            jto.selectRow(4);
            jto.selectRow(7);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(ThreadGroup group, Runnable target, String name, long stackSize) {\n"+
                    "        super(group, target, name, stackSize);\n"+
                    "    }\n";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testMultipleFiledSelection() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(0);
            jto.selectRow(1);
            jto.selectRow(2);
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(int b, double c) {\n"+
                    "        this.b = b;\n"+
                    "        this.c = c;\n"+
                    "    }";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testCancel() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(1);
            gco.btCancel().push();
            String expected = "" +
                    "    public testSimpleCase(String a) {\n"+
                    "        this.a = a;\n"+
                    "    }";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertFalse("Constuctor inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testUndoRedo() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(35, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(String a) {\n"+
                    "        this.a = a;\n"+
                    "    }";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
            new Action("Edit|Undo",null).perform();
            assertFalse("Constuctor not removed",contains(editor.getText(),expected));           
            MainWindowOperator.getDefault().menuBar().pushMenu("Edit");
            MainWindowOperator.getDefault().menuBar().closeSubmenus();
            new Action("Edit|Redo",null).perform();
            assertTrue("Constuctor not re-inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public void testInnerClass() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateConstructor", "TestInnerClass");
        editor = new EditorOperator("TestInnerClass");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(20, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR,editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "        public Inner(String afield) {\n"+
                    "            this.afield = afield;\n"+
                    "        }\n";
            if(isWin()) expected = expected.replace("\n", "\r\n");
            waitMaxMilisForValue(1500,new EditorValueResolver(expected), Boolean.TRUE);
            assertTrue("Constuctor not inserted",contains(editor.getText(),expected));
        } finally {
            editor.close(false);
        }
    }
    
    public static void main(String[] args) {
        //new TestRunner().run(CreateConstructor.class);
        new TestRunner().run(new CreateConstructor("testInitFieldAndSuper"));
        new TestRunner().run(new CreateConstructor("testMultipleSuperSelection"));
    }
    
    
}
