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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * This test contains examples described in HOWTO - modify the source code.
 *
 * (Test will always pass, as it does not check the results. It is used
 * just for explanation.)
 *
 * @author Pavel Flaska
 */

public class TutorialTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of TutorialTest */
    public TutorialTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(TutorialTest.class);
        return suite;
    }
    
    // Modifications demo:
    // adds "implements Externalizable" and its import to the source.
    public void testFirstModification() throws FileStateInvalidException,IOException {
        // First of all, we have to look for JavaSource we want to work with...
        // There are more ways to do it. For our demostration, we use 
        // straightforward solution, often used in tests. We omit details how
        // to obtain correct file object and java source and we expect 
        // successful behaviour of called methods.
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial1.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                // working copy is used for modify source. When all is
                // done, call commit() method on it to propagate changes
                // to original source.
                workingCopy.toPhase(Phase.RESOLVED);
                
                // CompilationUnitTree represents one java source file,
                // exactly as defined in  JLS, §7.3 Compilation Units.
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                // Get the tree maker. This is the core class used for
                // many modifications. It allows to add new members to class,
                // modify statements. You should be able to do anything
                // you need with your source.
                TreeMaker make = workingCopy.getTreeMaker();
                // Go through all the (§7.6) Top Level Type Declarations and
                // add the Externalizable interface to their declaration.
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        // Now, there are several way how to create interface
                        // identifier which we wants to add to the class declaration.
                        
                        // Simpliest, but not sufficient solution: Add the
                        // plain identifier. It generates source as you can
                        // see below, but when import is not available,
                        // identifier is not resolved and class will not
                        // compile.
                        // public class Tutorial1 {
                        // public class Tutorial1 implements Externalizable {
                        ExpressionTree implementsClause = make.Identifier("Externalizable");
                        

                        // We can solve described problem with specifying
                        // fully-qualified name. We can create again identifier
                        // tree. (Bear in mind, that you will never get such
                        // an identifier from the compiler staff - this identifier
                        // will be represented as chain of MemberSelectTree
                        // of "io" and "Externalizable" and IdentifierTree "java".
                        // Currently, it is compilable and correct, but one can
                        // consider to do it much more precisely. See below.
                        // public class Tutorial1 {
                        // public class Tutorial1 implements java.io.Externalizable {
                        implementsClause = make.Identifier("java.io.Externalizable");
                        
                        // Last one and probably the most often used solution.
                        // Use the resolved type, provide the fully-qualified name
                        // for this resolution. You should check, that element is
                        // available. Then, make QualIdent tree, which will be
                        // recognized during source code modification and engine
                        // will decide (in accordance with options) how to correctly
                        // generate. Often, import for your class will be added
                        // and simple name will be used in implments clause.
                        // public class Tutorial1 {
                        //
                        // import java.io.Externalizable;
                        // public class Tutorial1 implements Externalizable {
                        TypeElement element = workingCopy.getElements().getTypeElement("java.io.Externalizable");
                        implementsClause = make.QualIdent(element);
                        
                        // At this time, we want to add the created tree to correct
                        // place. We will use method addClassImplementsClause().
                        // Many of features uses these method, let's clarify
                        // names of the method:
                        // (add|insert|remove) prepend identified operation.
                        // (identifier)  identifies tree which will be modified,
                        // in our case it is ClassTree. The rest identifies the
                        // list which will be updated.
                        // See TreeMaker javadoc for details.
                        ClassTree modifiedClazz = make.addClassImplementsClause(clazz, implementsClause);
                        // As nodes in tree are immutable, all method return
                        // the same class type as provided in first paramter.
                        // If the method takes ClassTree parameter, it will
                        // return another class tree, which contains provided
                        // modification.
                        
                        // At the and, when you makes all the necessary changes,
                        // do not forget to replace original node with the new
                        // one.
                        
                        // TODO: changes can be chained, demonstrate!
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

            public void cancel() {
            }
        };

        // Now, we can start to process the changes. Because we want to modify
        // source, we have to use runModificationTask (see its javadoc).
        // At the end, we have to commit changes to propagate all the work
        // to the source file... This can fail, so ensure you correctly
        // handling exceptions. For testing reasons it is unimportant.
        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }

    // creates and adds method to the source-code
    public void testAddMethod() throws FileStateInvalidException, IOException {
        File tutorialFile = getFile(getSourceDir(), "/org/netbeans/test/codegen/Tutorial1.java");
        JavaSource tutorialSource = JavaSource.forFileObject(FileUtil.toFileObject(tutorialFile));
        
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        
                        // Create the method. This is done again through the
                        // TreeMaker as in the previous method. Here is the
                        // code how will method look like:
                        //
                        //    public void writeExternal(final ObjectOutput arg0) throws IOException {
                        //        throw new UnsupportedOperationException("Not supported yet.");
                        //    }
                        
                        // create method modifier: public and no annotation
                        ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList()
                        );
                        
                        // create parameter:
                        // final ObjectOutput arg0
                        VariableTree parameter = make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "arg0", // name
                                make.Identifier("Object"), // parameter type
                                null // initializer - does not make sense in parameters.
                        );
                        
                        // prepare simple name to throws clause:
                        // 'throws IOException' and its import will be added
                        TypeElement element = workingCopy.getElements().getTypeElement("java.io.IOException");
                        ExpressionTree throwsClause = make.QualIdent(element);
                        
                        // create method. There are two basic options:
                        // 1)
                        // make.Method() with 'BlockTree body' parameter -
                        // body has to be created, here in example code
                        // empty body block commented out
                        // 2)
                        // make.Method() with 'String body' parameter -
                        // body is added as a text. Used in our example.
                        MethodTree newMethod = make.Method(
                            methodModifiers, // public
                            "writeExternal", // writeExternal
                            make.PrimitiveType(TypeKind.VOID), // return type "void"
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            Collections.<VariableTree>singletonList(parameter), // final ObjectOutput arg0
                            Collections.<ExpressionTree>singletonList(throwsClause), // throws 
                            "{ throw new UnsupportedOperationException(\"Not supported yet.\") }", // body text
                            // make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
                            null // default value - not applicable here, used by annotations
                        );

                        // and in the same way as interface was added to implements clause,
                        // add feature to the class:
                        ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

            public void cancel() {
            }
        };

        tutorialSource.runModificationTask(task).commit();
        
        // print the result to the System.err to see the changes in console.
        BufferedReader in = new BufferedReader(new FileReader(tutorialFile));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }

    // not important for tutorial reasons.
    // please ignore.
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ConstructorTest/ConstructorTest/";
    }

}
