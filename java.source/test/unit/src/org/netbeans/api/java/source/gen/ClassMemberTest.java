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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class ClassMemberTest extends GeneratorTest {
    
    /** Creates a new instance of ClassMemberTest */
    public ClassMemberTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(ClassMemberTest.class);
        suite.addTest(new ClassMemberTest("testAddAtIndex0"));
        suite.addTest(new ClassMemberTest("testAddAtIndex2"));
        suite.addTest(new ClassMemberTest("testAddToEmpty"));
        suite.addTest(new ClassMemberTest("testAddConstructor"));
        return suite;
    }
    
    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void newlyCreatedMethod(int a,float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 0, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddAtIndex2() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.insertClassMember(classTree, 2, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddToEmpty() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "}\n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassMember(classTree, m(make));
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddConstructor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    String prefix;\n" +
            "    \n" +
            "    public Test(boolean prefix) {\n" +
            "    }\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
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
                        ClassTree classTree = (ClassTree) typeDecl;
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        List<VariableTree> arguments = new ArrayList<VariableTree>();
                        arguments.add(make.Variable(
                            make.Modifiers(EnumSet.noneOf(Modifier.class)),
                            "prefix",
                            make.PrimitiveType(TypeKind.BOOLEAN), null)
                        );
                        MethodTree constructor = make.Method(
                            mods,
                            "<init>",
                            null,
                            Collections.<TypeParameterTree> emptyList(),
                            arguments,
                            Collections.<ExpressionTree>emptyList(),
                            make.Block(Collections.<StatementTree>emptyList(), false),
                            null
                        );
                        ClassTree copy = make.insertClassMember(classTree, 2, constructor);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    private MethodTree m(TreeMaker make) {
        // create method modifiers
        ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
        // create parameters
        VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
        VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.FLOAT), null);
        List<VariableTree> parList = new ArrayList<VariableTree>(2);
        parList.add(par1);
        parList.add(par2);
        // create method
        MethodTree newMethod = make.Method(
            make.Modifiers( 
                Collections.singleton(Modifier.PUBLIC), // modifiers
                Collections.EMPTY_LIST // annotations
            ), // modifiers and annotations
            "newlyCreatedMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.EMPTY_LIST, // type parameters for parameters
            parList, // parameters
            Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
            make.Block(Collections.EMPTY_LIST, false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
