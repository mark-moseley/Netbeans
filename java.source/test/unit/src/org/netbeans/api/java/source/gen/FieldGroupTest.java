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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.java.source.gen;

import java.io.File;
import com.sun.source.tree.*;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class FieldGroupTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of FieldGroupTest */
    public FieldGroupTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FieldGroupTest.class);
//        suite.addTest(new FieldGroupTest("testFieldGroup1"));
//        suite.addTest(new FieldGroupTest("testFieldGroup2"));
//        suite.addTest(new FieldGroupTest("testFieldGroup3"));
//        suite.addTest(new FieldGroupTest("testFieldGroup4"));
//        suite.addTest(new FieldGroupTest("testFieldGroup5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody1"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody2"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody3"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody4"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody5"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody6"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBody7"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBodyCast1"));
//        suite.addTest(new FieldGroupTest("testFieldGroupInBodyCast2"));
//        suite.addTest(new FieldGroupTest("test114571"));
//        suite.addTest(new FieldGroupTest("testFieldGroupModifiers"));
//        suite.addTest(new FieldGroupTest("testNoFieldGroup"));
//        suite.addTest(new FieldGroupTest("testRemoveFromFieldGroup"));
        return suite;
    }
    
    /**
     */
    public void testFieldGroup1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int e;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a, b, c, d;\n" +
            "    int ecko;\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(5);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testFieldGroup2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int e;\n" +
            "    int a, b, c, d;\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int ecko;\n" +
            "    int a, b, c, d;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "ecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
       
    public void testFieldGroup3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, what = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "what"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int acko, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroup5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, c = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    int a, becko = 10, cecko = 25;\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = (VariableTree) clazz.getMembers().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, cecko = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(3);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, b = 10, c = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int a, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        int acko, becko = 10, c = 25;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    
    public void testFieldGroupInBody4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, cecko = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                workingCopy.rewrite(vt, make.setLabel(vt, "cecko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody5() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, b = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(1);
                workingCopy.rewrite(vt, make.setLabel(vt, "b"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBody6() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int a, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        int acko, becko = 10, c = 25;\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                workingCopy.rewrite(vt, make.setLabel(vt, "acko"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBodyCast1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=o21;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=(Widget) o11,w2=o21;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(2);
                ExpressionTree init = vt.getInitializer();
                workingCopy.rewrite(init, make.TypeCast(make.Identifier("Widget"), init));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFieldGroupInBodyCast2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=o21;\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class UserTask {\n" +
            "\n" +
            "    // aaa\n" +
            "    @Override\n" +
            "    public void method() {\n" +
            "        Object o11=null; Object o21=null;\n" +
            "        Widget w1=o11,w2=(Widget) o21;\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                BlockTree block = method.getBody();
                VariableTree vt = (VariableTree) block.getStatements().get(3);
                ExpressionTree init = vt.getInitializer();
                workingCopy.rewrite(init, make.TypeCast(make.Identifier("Widget"), init));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // test 114571
    public void test114571() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public MyOuterClass c = new MyOuterClass(), b= new MyOuterClass();\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MOC {\n" +
            "    public MOC c = new MOC(), b= new MOC();\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                workingCopy.rewrite(clazz, make.setLabel(clazz, "MOC"));
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                NewClassTree nct = (NewClassTree) var.getInitializer();
                ExpressionTree ident = nct.getIdentifier();
                workingCopy.rewrite(ident, make.setLabel(ident, "MOC"));
                workingCopy.rewrite(var.getType(), make.Identifier("MOC"));
                
                var = (VariableTree) clazz.getMembers().get(2);
                nct = (NewClassTree) var.getInitializer();
                ident = nct.getIdentifier();
                workingCopy.rewrite(ident, make.setLabel(ident, "MOC"));
                workingCopy.rewrite(var.getType(), make.Identifier("MOC"));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void XtestFieldGroupModifiers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean a, b, c;\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    private boolean a;\n" + 
            "    public boolean b, c;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var.getModifiers(), make.Modifiers(EnumSet.of(Modifier.PRIVATE)));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testNoFieldGroup() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    private Exception a;\n" + 
            "    private String b;\n" + 
            "    private Object c;\n" + 
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                Elements e = workingCopy.getElements();
                
                ClassTree nue = make.insertClassMember(clazz, 0, make.Variable(mods, "c", make.QualIdent(e.getTypeElement("java.lang.Object")), null));
                nue = make.insertClassMember(nue, 0, make.Variable(mods, "b", make.QualIdent(e.getTypeElement("java.lang.String")), null));
                nue = make.insertClassMember(nue, 0, make.Variable(mods, "a", make.QualIdent(e.getTypeElement("java.lang.Exception")), null));
                workingCopy.rewrite(clazz, nue);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFromFieldGroup() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean a, b, c;\n" +
            "}\n"
            );
        String golden = 
            "package javaapplication1;\n" +
            "\n" +
            "class MyOuterClass {\n" +
            "    public boolean b, c;\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(clazz, make.removeClassMember(clazz, var));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
