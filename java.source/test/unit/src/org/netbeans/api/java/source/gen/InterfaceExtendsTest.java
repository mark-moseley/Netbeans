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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Extends in interfaces can contain more identifiers (like implements
 * in classes). Consider and test interface with more extends identifiers.
 * (javac contains these part in ClassTree in implements part.)
 *
 * @author Pavel Flaska
 */
public class InterfaceExtendsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of InterfaceExtendsTest */
    public InterfaceExtendsTest(String testName) {
        super(testName);
    }
    
    public void testAddExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable {\n" +
            "    public void taragui();\n" +
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
                        List<ExpressionTree> implementz = 
                            Collections.<ExpressionTree>singletonList(
                                make.Identifier("Serializable")
                            );
                        ClassTree copy = make.Class(
                                classTree.getModifiers(),
                                classTree.getSimpleName(),
                                classTree.getTypeParameters(),
                                classTree.getExtendsClause(),
                                implementz,
                                classTree.getMembers()
                            );
                        workingCopy.rewrite(classTree, copy);
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
    
    public void testAddTwoExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable, Externalizable {\n" +
            "    public void taragui();\n" +
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
                        ClassTree copy = make.addClassImplementsClause(
                                classTree, make.Identifier("Serializable")
                            );
                        copy = make.addClassImplementsClause(
                                copy, make.Identifier("Externalizable")
                            );
                        workingCopy.rewrite(classTree, copy);
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
    
    public void testRemoveTwoExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable, Externalizable {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
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
                        ClassTree copy = make.removeClassImplementsClause(classTree, 0);
                        copy = make.removeClassImplementsClause(copy, 0);
                        workingCopy.rewrite(classTree, copy);
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
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
}
