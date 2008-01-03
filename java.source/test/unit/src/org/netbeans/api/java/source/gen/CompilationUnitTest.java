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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 * Test for compilation unit creation.
 * 
 * @author tom a lahvovej
 */
public class CompilationUnitTest extends GeneratorTestMDRCompat {

    public CompilationUnitTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CompilationUnitTest.class);
//        suite.addTest(new CompilationUnitTest("testNewCompilationUnit"));
//        suite.addTest(new CompilationUnitTest("test77010"));
//        suite.addTest(new CompilationUnitTest("test117607_1"));
//        suite.addTest(new CompilationUnitTest("test117607_2"));
        return suite;
    }

    public void testNewCompilationUnit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject rootFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject emptyJava = FileUtil.createData(rootFS, "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);
        
        String golden = 
            "package zoo;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    void m() {\n" +
            "    }\n" +
            "}\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                ClassTree clazz = make.Class(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nju));
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        System.err.println(res);
        assertEquals(res, golden);
    }

     public void test77010() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "  int a;\n" +
            "  public class Krtek {\n" +
            "    public void foo() {\n" +
            "      int c=a;\n" +
            "    }\n" +
            "  }\n" +
            "}");
        
        FileObject rootFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject emptyJava = FileUtil.createData(rootFS, "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    public void foo() {\n" +
            "        int c = outer.a;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "  int a;\n" +
            "}";
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(var.getInitializer(), make.Identifier("outer.a"));
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test117607_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class A {\n" +
            "  public class Krtek {\n" +
            "    public void foo() {\n" +
            "        List l = null;\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        );
        
        FileObject rootFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject emptyJava = FileUtil.createData(rootFS, "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    public void foo() {\n" +
            "        List l = null;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class A {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, GeneratorUtilities.get(workingCopy).importFQNs(clazz));
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test117607_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class A {\n" +
            "  public class Krtek {\n" +
            "    File m() {\n" +
            "      return null;\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        );
        
        FileObject rootFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject emptyJava = FileUtil.createData(rootFS, "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    File m() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class A {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, GeneratorUtilities.get(workingCopy).importFQNs(clazz));
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test121729() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "}\n"
        );
        
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        
        String golden = 
            "package zoo;\n" +
            "\n" +
            "public class B {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.forFileObject(testSourceFO);
        
        //does not modify the source:
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        assertNotNull(parameter.getTrees().getElement(getCurrentPath()));
                        return super.visitClass(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
            }
        }).commit();
        
        //does the modification:
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        assertNotNull(parameter.getTrees().getElement(getCurrentPath()));
                        parameter.rewrite(node, parameter.getTreeMaker().setLabel(node, "B"));
                        return super.visitClass(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(res, golden);
    }
     
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}