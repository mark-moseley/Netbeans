/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.junit.Test;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This test verifies issues from netbeans issueszila about rewriting trees.
 *
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 */
public class RewriteOccasionalStatements extends GeneratorTest {
    private static final String TEST_CONTENT = "\n" +
            "public class NewArrayTest {\n" +
            "\n" +
            "int[] test = new int[3];" +
            "}\n";

    public RewriteOccasionalStatements(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RewriteOccasionalStatements.class);
        return suite;
    }

    @Test
    public void test158337regresion1() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, TEST_CONTENT);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[5];" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(5));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        init, new ArrayList<ExpressionTree>());
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }


    @Test
    public void test158337regresion2() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class NewArrayTest {\n" +
                        "\n" +
                        "int[] test = new int[]{1,2,3};" +
                        "}\n");
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[]{4,5,6};" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(4));
                init.add(make.Literal(5));
                init.add(make.Literal(6));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        new ArrayList<ExpressionTree>(),
                        init);
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }


    @Test
    public void test158337() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, TEST_CONTENT);
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "\n" +
                "int[] test = new int[]{1, 2, 3};" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                Tree node = extractOriginalNode(cut);
                TreeMaker make = workingCopy.getTreeMaker();
                List<ExpressionTree> init = new ArrayList<ExpressionTree>();
                init.add(make.Literal(1));
                init.add(make.Literal(2));
                init.add(make.Literal(3));
                ExpressionTree modified = make.NewArray(
                        make.PrimitiveType(TypeKind.INT),
                        new ArrayList<ExpressionTree>(),
                        init);
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                workingCopy.rewrite(node, modified);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }

    private Tree extractOriginalNode(CompilationUnitTree cut) {
        List<? extends Tree> classes = cut.getTypeDecls();
        if (!classes.isEmpty()) {
            ClassTree clazz = (ClassTree) classes.get(0);
            List<? extends Tree> trees = clazz.getMembers();
//                    System.out.println("Trees:" + trees);
            if (trees.size() == 2) {
                VariableTree tree = (VariableTree) trees.get(1);
                return tree.getInitializer();
            }
        }

        throw new IllegalStateException("There is no array declaration in expected place.");

    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    public void testExtractInterface117986() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1, MyInterface2, MyInterface3 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }

    public void testExtractInterfaceRegresion1() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }

    public void testExtractInterfaceRegresion2() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String source = "public class ExtractSuperInterface implements MyInterface1, MyInterface2 {\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, source);
        String golden = "public class ExtractSuperInterface implements SuperInterface {\n" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                Elements elements = wc.getElements();
                TypeElement typeElement = elements.getTypeElement("ExtractSuperInterface");
                ElementHandle<TypeElement> sourceType = ElementHandle.<TypeElement>create(typeElement);
                TypeElement clazz = sourceType.resolve(wc);
                assert clazz != null;
                ClassTree classTree = wc.getTrees().getTree(clazz);
                TreeMaker maker = wc.getTreeMaker();
                // fake interface since interface file does not exist yet
                Tree interfaceTree = maker.Identifier("SuperInterface");

                // filter out obsolete members
                List<Tree> members2Add = new ArrayList<Tree>();
                // filter out obsolete implements trees
                List<Tree> impls2Add = Collections.singletonList(interfaceTree);

                ClassTree nc;
                if (clazz.getKind() == ElementKind.CLASS) {
                    nc = maker.Class(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            classTree.getExtendsClause(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.INTERFACE) {
                    nc = maker.Interface(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            classTree.getTypeParameters(),
                            impls2Add,
                            members2Add);
                } else if (clazz.getKind() == ElementKind.ENUM) {
                    nc = maker.Enum(
                            classTree.getModifiers(),
                            classTree.getSimpleName(),
                            impls2Add,
                            members2Add);
                } else {
                    throw new IllegalStateException(classTree.toString());
                }

                wc.rewrite(classTree, nc);
            }
        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals("Golden and result does not match", golden, res);
    }


/*  This issue has been waived for yet.
    @Test
    public void test159941() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "\n" +
                        "public class NewArrayTest {\n" +
                        "   void m4(int[] p) {\n" +
                        "        if (p[0] > 0) {\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"x\");\n" +
                        "            }\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"y\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "        if (p[0] > 0) {\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"z\");\n" +
                        "            }\n" +
                        "            if (p[1] > 0) {\n" +
                        "                System.out.println(\"w\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }" +
                        "}\n");
        String golden = "\n" +
                "public class NewArrayTest {\n" +
                "   void m4(int[] p) {\n" +
                "        if (p[0] > 0) {\n" +
                "            if (p[1] > 0) {\n" +
                "                System.out.println(\"x\");\n" +
                "            }\n" +
                "        }\n" +
                "    }" +
                "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(JavaSource.Phase.RESOLVED);                
                SimpleScanner ss = new SimpleScanner(wc);
                ss.scan(wc.getCompilationUnit().getTypeDecls().get(0), null);
            }

        };

        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.out.println(res);
        assertEquals(golden, res);
    }
*/


    class SimpleScanner extends TreeScanner<Void, Void> {
        private final WorkingCopy wc;
        protected GeneratorUtilities gu;

        SimpleScanner(WorkingCopy wc) {
            this.wc = wc;
            gu = GeneratorUtilities.get(this.wc);
        }

        @Override
        public Void visitBlock(BlockTree node, Void p) {
            List<? extends StatementTree> st = node.getStatements();
            if (st.size() == 2) {
                List<StatementTree> st2 = new ArrayList<StatementTree>();
                st2.add(st.get(0));
                TreeMaker make = wc.getTreeMaker();
                BlockTree modified = make.Block(st2, node.isStatic());
                modified = gu.importFQNs(modified);                         
                System.out.println("original: " + node);
                System.out.println("modified: " + modified);
                wc.rewrite(node, modified);
            }
            return super.visitBlock(node, p);
        }

    }
}
