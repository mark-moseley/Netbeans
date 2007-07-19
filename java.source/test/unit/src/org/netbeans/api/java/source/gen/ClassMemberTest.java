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

import java.io.*;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class ClassMemberTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ClassMemberTest */
    public ClassMemberTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassMemberTest.class);
//        suite.addTest(new ClassMemberTest("testAddAtIndex0"));
//        suite.addTest(new ClassMemberTest("testAddAtIndex2"));
//        suite.addTest(new ClassMemberTest("testAddToEmpty"));
//        suite.addTest(new ClassMemberTest("testAddConstructor"));
//        suite.addTest(new ClassMemberTest("testInsertFieldToIndex0"));
//        suite.addTest(new ClassMemberTest("testModifyFieldName"));
//        suite.addTest(new ClassMemberTest("testModifyModifiers"));
//        suite.addTest(new ClassMemberTest("testAddToEmptyInterface"));
//        suite.addTest(new ClassMemberTest("testAddNewClassWithNewMembers"));
//        suite.addTest(new ClassMemberTest("testAddInnerInterface"));
//        suite.addTest(new ClassMemberTest("testAddInnerAnnotationType"));
//        suite.addTest(new ClassMemberTest("testAddInnerEnum"));
//        suite.addTest(new ClassMemberTest("testAddMethodAndModifyConstr"));
//        suite.addTest(new ClassMemberTest("testAddAfterEmptyInit1"));
//        suite.addTest(new ClassMemberTest("testAddAfterEmptyInit2"));
//        suite.addTest(new ClassMemberTest("testMemberIndent93735_1"));
//        suite.addTest(new ClassMemberTest("testMemberIndent93735_2"));
//        suite.addTest(new ClassMemberTest("testAddArrayMember"));
//        suite.addTest(new ClassMemberTest("testAddCharMember"));
//        suite.addTest(new ClassMemberTest("testRenameReturnTypeInAbstract"));
//        suite.addTest(new ClassMemberTest("testAddInitToVar"));
//        suite.addTest(new ClassMemberTest("testAddMethodWithDoc"));
//        suite.addTest(new ClassMemberTest("testAddFieldWithDoc1"));
//        suite.addTest(new ClassMemberTest("testAddFieldWithDoc2"));
        return suite;
    }

    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    \n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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
            "\n" +
            "    public Test(boolean prefix) {\n" +
            "    }\n" +
            "    \n" +
            "    public void method() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testInsertFieldToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    String prefix;\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        VariableTree member = make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "prefix",
                                make.Identifier("String"),
                                null
                            );
                        ClassTree modifiedClazz = make.insertClassMember(clazz, 0, member);
                        workingCopy.rewrite(clazz,modifiedClazz);
                    }
                }
            }
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyFieldName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    int newFieldName = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        VariableTree copy = make.setLabel(variable, "newFieldName");
                        workingCopy.rewrite(variable, copy);
                    }
                }
            }
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyModifiers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    private int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    \n" +
            "    public int i = 0;\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        VariableTree variable = (VariableTree) ((ClassTree) typeDecl).getMembers().get(0);
                        ModifiersTree mods = variable.getModifiers();
                        workingCopy.rewrite(mods, make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)));
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();    
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmptyInterface() throws Exception {
        //member position 2 is actually after the taragui method, as position 0 is the syntetic constructor:
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public interface Test {\n\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        MethodTree method = m(make);
                        MethodTree methodC = make.Method(method.getModifiers(),
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                method.getParameters(),
                                method.getThrows(),
                                (BlockTree) null,
                                null
                        );
                        ClassTree copy = make.addClassMember(classTree, methodC);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddNewClassWithNewMembers() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n\n" +
                "    public class X {\n\n" +
                "        private int i;\n\n" +
                "        public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = m(make);
                MethodTree methodC = make.Method(method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        "{}",
                        null
                        );
                VariableTree var = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), "i", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                ClassTree nueClass = make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "X", Collections.<TypeParameterTree>emptyList(), null, Collections.<ExpressionTree>emptyList(), Arrays.asList(var, methodC));
                ClassTree copy = make.addClassMember(ct, nueClass);
                workingCopy.rewrite(ct, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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
                Collections.<AnnotationTree>emptyList() // annotations
            ), // modifiers and annotations
            "newlyCreatedMethod", // name
            make.PrimitiveType(TypeKind.VOID), // return type
            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
            parList, // parameters
            Collections.singletonList(make.Identifier("java.io.IOException")), // throws 
            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
            null // default value - not applicable here, used by annotations
        );
        return newMethod;
    }
    
    /**
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initilizer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void testAddAfterEmptyInit1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    static enum Enumerace {\n" +
            "        A, B\n" +
            "    };\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #92726, #92127: When semicolon is in class declaration, it is represented
     * as an empty initilizer in the tree with position -1. This causes many
     * problems during generating. See issues for details.
     */
    public void testAddAfterEmptyInit2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    ;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    ;\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerInterface() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Interface(make.Modifiers(
                        Collections.<Modifier>emptySet()),
                        "Honza",
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerAnnotationType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    public @interface Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.AnnotationType(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Honza",
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #96070
     */
    public void testAddInnerEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n\n" +
            "    protected enum Honza {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree innerIntfc = make.Enum(make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PROTECTED)),
                        "Honza",
                        Collections.<ExpressionTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, innerIntfc));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * 
     */
    public void testAddMethodAndModifyConstr() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public Test() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public Test() {\n" +
            "        // TODO: Make it!\n" +
            "        int i = 0;\n" +
            "    }\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree constr = (MethodTree) classTree.getMembers().get(0);
//                ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
                // create parameters
//                VariableTree par1 = make.Variable(parMods, "a", make.PrimitiveType(TypeKind.INT), null);
//                VariableTree par2 = make.Variable(parMods, "b", make.PrimitiveType(TypeKind.INT), null);
//                MethodTree constrCopy = make.addMethodParameter(constr, par1);
//                constrCopy = make.addMethodParameter(constrCopy, par2);
                BlockTree newBody = make.createMethodBody(constr, "{\n // TODO: Make it!\nint i = 0; }");
                workingCopy.rewrite(constr.getBody(), newBody);
                ClassTree copy = make.insertClassMember(classTree, 1, m(make));
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMemberIndent93735_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testMemberIndent93735_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int i = 0;\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int i = 0;\n" +
            "\n" +
            "    public void newlyCreatedMethod(int a, float b) throws java.io.IOException {\n" + 
            "    }\n" +
            "    // what a strange thing is this?\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

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
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddArrayMember() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public java.util.List[] taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public List<E>[] newlyCreatedMethod() {\n" + 
            "    }\n" +
            "    \n" +
            "    public java.util.List[] taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                TypeMirror mirror = workingCopy.getElements().getTypeElement("java.util.List").asType();
                mirror = workingCopy.getTypes().getArrayType(mirror);
                MethodTree njuMethod = (MethodTree) classTree.getMembers().get(1);
                njuMethod = make.Method(
                        njuMethod.getModifiers(),
                        "newlyCreatedMethod",
                        make.Type(mirror),
                        njuMethod.getTypeParameters(), 
                        njuMethod.getParameters(),
                        njuMethod.getThrows(),
                        njuMethod.getBody(),
                        (ExpressionTree) njuMethod.getDefaultValue()
                );
                ClassTree copy = make.insertClassMember(classTree, 0, njuMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCharMember() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public char taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public char newlyCreatedMethod() {\n" +
            "        char returnValue = taragui();\n" +
            "        char expectedValue = 'c';\n" + 
            "    }\n" +
            "    \n" +
            "    public char taragui() {\n" +
            "    }\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement neterk = workingCopy.getElements().getTypeElement("hierbas.del.litoral.Test");
                List<? extends Element> members = workingCopy.getElements().getAllMembers(neterk);
                ExecutableElement konecny = null;
                for (Element e : members) {
                    if ("taragui".contentEquals(e.getSimpleName())) {
                        konecny = (ExecutableElement) e;
                        break;
                    }
                }
                List<ExpressionTree> empatik = Collections.<ExpressionTree>emptyList();
                MethodInvocationTree mit = make.MethodInvocation(empatik, make.Identifier("taragui"), empatik);
                VariableTree stmt = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "returnValue",
                        make.Type(konecny.getReturnType()),
                        mit);
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(stmt), false);
                stmt = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "expectedValue",
                        make.Type(konecny.getReturnType()),
                        make.Literal(('c')));
                block = make.addBlockStatement(block, stmt);
                MethodTree njuMethod = (MethodTree) classTree.getMembers().get(1);
                njuMethod = make.Method(
                        njuMethod.getModifiers(),
                        "newlyCreatedMethod",
                        make.Type(konecny.getReturnType()),
                        njuMethod.getTypeParameters(), 
                        njuMethod.getParameters(),
                        njuMethod.getThrows(),
                        block,
                        (ExpressionTree) njuMethod.getDefaultValue()
                );
                ClassTree copy = make.insertClassMember(classTree, 0, njuMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameReturnTypeInAbstract() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public abstract class Test {\n" +
            "    \n" +
            "    public Object taragui();\n" +
            "    \n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public abstract class Test {\n" +
            "    \n" +
            "    public String taragui();\n" +
            "    \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) classTree.getMembers().get(1);
                workingCopy.rewrite(method.getReturnType(), make.Identifier("String"));
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstFeatureField() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vt = make.Variable(
                    make.Modifiers(EnumSet.noneOf(Modifier.class)),
                    "prefix",
                    make.PrimitiveType(TypeKind.BOOLEAN), 
                    null
                );
                ClassTree copy = make.addClassMember(classTree, vt);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #109489
    public void testAddInitToVar() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix = true;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree vetecko = (VariableTree) classTree.getMembers().get(1);
                VariableTree copy = make.Variable(
                        vetecko.getModifiers(),
                        vetecko.getName(),
                        vetecko.getType(),
                        make.Literal(Boolean.TRUE)
                );
                workingCopy.rewrite(vetecko, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    // #110211
    public void testAddMethodWithDoc() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    boolean prefix;\n" +
            "\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    public void foo() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree m = make.Method(
                        make.Modifiers(java.lang.reflect.Modifier.PUBLIC, Collections.<AnnotationTree>emptyList()),
                        "foo",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null
                );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, m);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(m, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #110211
    public void testAddFieldWithDoc1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    String prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree member = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "prefix",
                        make.Identifier("String"),
                        null
                    );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, member);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(member, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #110211
    public void testAddFieldWithDoc2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /**\n" +
            "     * Test comment\n" +
            "     */\n" +
            "    public String prefix;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED); // is it neccessary?
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree member = make.Variable(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "prefix",
                        make.Identifier("String"),
                        null
                    );
                // Insert the class before constructor
                ClassTree modifiedClazz = make.addClassMember(clazz, member);
                Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, "Test comment");
                make.addComment(member, comment, true);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        src.runModificationTask(task).commit();
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
