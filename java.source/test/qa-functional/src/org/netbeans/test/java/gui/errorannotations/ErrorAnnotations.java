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

package org.netbeans.test.java.gui.errorannotations;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.java.Utilities;



/**
 * Tests Error annotations.
 * @author Roman Strobl
 */
public class ErrorAnnotations extends JellyTestCase {

    // default timeout for actions in miliseconds
    private static final int ACTION_TIMEOUT = 3000;

    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";

    // path to sample files
    private static final String TEST_PACKAGE_PATH = "org.netbeans.test.java.gui.errorannotations";

    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH + ".test";

    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";

    /**
     * error log
     */
    protected static PrintStream err;

    /**
     * standard log
     */
    protected static PrintStream log;

    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";

    // actual directory with project
    private static String projectDir;

    /**
     * Needs to be defined because of JUnit
     * @param name test name
     */
    public ErrorAnnotations(String name) {
        super(name);
    }

    /**
     * Adds tests into the test suite.
     * @return suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        // prepare testing project and package - not a core test but needed
        suite.addTest(new ErrorAnnotations("testAnnotationsSimple"));
        suite.addTest(new ErrorAnnotations("testUndo"));
        suite.addTest(new ErrorAnnotations("testAnnotationsSimple2"));
        suite.addTest(new ErrorAnnotations("testAnnotationsSimple3"));
        suite.addTest(new ErrorAnnotations("testChangeCloseDiscart"));

        return suite;
    }

    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Sets up logging facilities.
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) {
        }
    }

    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();

        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME + "|" + TEST_CLASS_NAME);

        n.select();
        new OpenAction().perform();

        // test a simple error - a space in public keyword
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator editor = ewo.getEditor(TEST_CLASS_NAME);
        editor.insert(" ", 11, 3);

        Utilities.takeANap(ACTION_TIMEOUT);

        log(editor.getText());
        Object[] annots = editor.getAnnotations();
        assertNotNull(annots);
        assertEquals(1, annots.length);
        assertEquals("org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[0]));
        assertEquals("class, interface, or enum expected", EditorOperator.getAnnotationShortDescription(annots[0]));
    }

    /**
     * Tests undo after simple annotations test.
     */
    public void testUndo() {
        // undo
        new ActionNoBlock("Edit|Undo", null).perform();

        Utilities.takeANap(ACTION_TIMEOUT);

        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator editor = ewo.getEditor(TEST_CLASS_NAME);
        log(editor.getText());
        Object[] annots = editor.getAnnotations();

        // there should be no annotations
        assertEquals(annots.length, 0);
    }

    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple2() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();

        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME + "|" + TEST_CLASS_NAME);

        n.select();
        new OpenAction().perform();

        // change class to klasa
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator editor = ewo.getEditor(TEST_CLASS_NAME);
        editor.replace("class", "klasa");

        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        // check error annotations
        Object[] annots = editor.getAnnotations();
        try {
            assertNotNull("There are not any annotations.", annots);
            assertEquals("There are not one annotation", 2, annots.length);
            assertEquals("Wrong annotation type ", "org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[0]));
            assertEquals("Wrong annotation short description", "class, interface, or enum expected", EditorOperator.getAnnotationShortDescription(annots[0]));
            assertEquals("Wrong annotation type ", "org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[1]));
            assertEquals("Wrong annotation short description", "class, interface, or enum expected", EditorOperator.getAnnotationShortDescription(annots[1]));
        } finally {
            new ActionNoBlock("Edit|Undo", null).perform();
            Utilities.takeANap(ACTION_TIMEOUT);
            ewo.closeDiscard();
        }
    }

    /**
     * Simple annotations tests - tries a simple error.
     */
    public void testAnnotationsSimple3() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();

        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME + "|" + TEST_CLASS_NAME);

        n.select();
        new OpenAction().perform();

        // add xxx string to ctor
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator editor = ewo.getEditor(TEST_CLASS_NAME);
        editor.replace(TEST_CLASS_NAME, TEST_CLASS_NAME + "xxx", 3);

        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        // check error annotations
        Object[] annots = editor.getAnnotations();
        assertNotNull("There are not any annotations.", annots);
        assertEquals("There are more than  one annotation: " + String.valueOf(annots.length), 1, annots.length);
        assertEquals("Wrong annotation type: " + EditorOperator.getAnnotationType(annots[0]), "org-netbeans-spi-editor-hints-parser_annotation_err", EditorOperator.getAnnotationType(annots[0]));
        assertEquals("Wrong annotation short description.","invalid method declaration; return type required", EditorOperator.getAnnotationShortDescription(annots[0]));        
        new ActionNoBlock("Edit|Undo", null).perform();
        Utilities.takeANap(ACTION_TIMEOUT);
        ewo.closeDiscard();
    }

    public void testChangeCloseDiscart() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();

        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + "|" + TEST_PACKAGE_NAME + "|" + TEST_CLASS_NAME);

        n.select();
        new OpenAction().perform();


        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator editor = ewo.getEditor(TEST_CLASS_NAME);
        String context = "" + "/*\n" + " * TestClass.java               \n" + " *\n" + " */\n" + "\n" + "package org.netbeans.test.java.gui.errorannotations.test;\n" + "\n" + "/**\n" + " *\n" + " */\n" + "public class TestClass {\n" + "   \n" + "    /** Creates a new instance of TestClass */\n" + "    public TestClass() {\n" + "    }\n" + "   \n" + "}\n";

        editor.delete(0, editor.getText().length());
        editor.insert(context); //setting right content of file to avoid dependency between tests
        new EventTool().waitNoEvent(1000);
        new SaveAction().perform();

        editor.insert(" ", 11, 3);
        Utilities.takeANap(ACTION_TIMEOUT);
        log(editor.getText());
        Object[] annots = editor.getAnnotations();
        assertNotNull("There are not any annotations.", annots);
        assertEquals("There are annotations: " + String.valueOf(annots.length), 1, annots.length);
        ewo.closeDiscard();
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(500);
        editor = ewo.getEditor(TEST_CLASS_NAME);
        annots = editor.getAnnotations();
        assertEquals(0, annots.length); //there should be no annotations
        ewo.closeDiscard();
    }
}
