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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import static org.netbeans.api.java.source.JavaSource.*;

/**
 * Tests method parameters.
 * 
 * @author Pavel Flaska
 */
public class MethodParametersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public MethodParametersTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(MethodParametersTest.class);
//        suite.addTest(new MethodParametersTest("testAddInsertReplaceParameters"));
        suite.addTest(new MethodParametersTest("testAddFirst"));
        suite.addTest(new MethodParametersTest("testAddToIndex0"));
        suite.addTest(new MethodParametersTest("testRemoveFirstTwo"));
        suite.addTest(new MethodParametersTest("testRemoveLast"));
        suite.addTest(new MethodParametersTest("testRemoveLastTwo"));
        suite.addTest(new MethodParametersTest("testRemoveMid"));
        suite.addTest(new MethodParametersTest("testSwap"));
        suite.addTest(new MethodParametersTest("testRenameInTypePar"));
        suite.addTest(new MethodParametersTest("testRenameInParameterizedType"));
        suite.addTest(new MethodParametersTest("testRenameInParameterInvocation"));
        return suite;
    }

    public void testAddInsertReplaceParameters() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int a, long c, String s) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada,File marcela, long c, String s,File cedron) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
                        );
                        copy = make.removeMethodParameter(copy, 1);
                        copy = make.addMethodParameter(
                            copy,
                            make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "cedron",
                                make.Identifier("File"),
                                null
                            )
                        );
                        copy = make.insertMethodParameter(
                            copy,
                            1,
                            make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "marcela",
                                make.Identifier("File"),
                                null
                            )
                        );
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
                        );
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File carqueja) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(final File elaborada,final File carqueja) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.insertMethodParameter(
                            node, 0,
                            make.Variable(
                                make.Modifiers(
                                    Collections.singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "elaborada",
                                make.Identifier("File"),
                                null
                            )
                        );
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.removeMethodParameter(
                            node, 0
                        );
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveMid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.removeMethodParameter(
                            node, 1
                        );
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLastTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.removeMethodParameter(node, 1);
                        copy = make.removeMethodParameter(copy, 1);
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int para, int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui( int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        MethodTree copy = make.removeMethodParameter(node, 0);
                        copy = make.removeMethodParameter(copy, 0);
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testSwap() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui( int sugerimos,int empezar) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        VariableTree vt = node.getParameters().get(0);
                        MethodTree copy = make.removeMethodParameter(node, 0);
                        copy = make.addMethodParameter(copy, vt);
                        workingCopy.rewrite(node, copy);
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #89746: Rename in type parameter/parameterized type
     */
    public void testRenameInTypePar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(List<Something> empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(List<Neco> empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        VariableTree vt = node.getParameters().get(0);
                        ParameterizedTypeTree ptt = (ParameterizedTypeTree) vt.getType();
                        IdentifierTree it = (IdentifierTree) ptt.getTypeArguments().get(0);
                        workingCopy.rewrite(it, make.Identifier("Neco"));
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #89746: Rename in type parameter/parameterized type
     */
    public void testRenameInParameterizedType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(List<Something> empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(Seznam<Something> empezar, int sugerimos) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        VariableTree vt = node.getParameters().get(0);
                        ParameterizedTypeTree ptt = (ParameterizedTypeTree) vt.getType();
                        workingCopy.rewrite(ptt.getType(), make.Identifier("Seznam"));
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #89746: Rename in type parameter/parameterized type
     */
    public void testRenameInParameterInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        String s = \"Nothing\";\n" +
            "        System.out.println(a, s);\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        String retez = \"Nothing\";\n" +
            "        System.out.println(a, retez);\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree node = (MethodTree) clazz.getMembers().get(1);
                        VariableTree vt = (VariableTree) node.getBody().getStatements().get(0);
                        workingCopy.rewrite(vt, make.setLabel(vt, "retez"));
                        ExpressionStatementTree est = (ExpressionStatementTree) node.getBody().getStatements().get(1);
                        MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                        IdentifierTree ident = (IdentifierTree) mit.getArguments().get(1);
                        workingCopy.rewrite(ident, make.Identifier("retez"));
                    }
                }
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
