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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.CancellableTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests the method generator.
 *
 * @author  Jan Becicka
 */
public class MethodTest2 extends GeneratorTest {
    
    /** Need to be defined because of JUnit */
    public MethodTest2(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MethodTest2("testMethodAdd"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest2.java");
    }

    /**
     * Changes the modifiers on method. Removes public modifier, sets static
     * and private modifier.
     */
    public void testMethodAdd() throws IOException {
        FileObject fo = FileUtil.toFileObject(testFile);
        JavaSource js = JavaSource.forFileObject(fo);
        js.runModificationTask(new CancellableTask<WorkingCopy>() {
            public void cancel() {
            }
            public void run(WorkingCopy wc) {
                
                CreateMethod create = new CreateMethod();
                SourceUtilsTestUtil2.run(wc, create);
                MethodTree mt = create.makeMethod();
                
                MethodImplGenerator add = new MethodImplGenerator(mt);
                SourceUtilsTestUtil2.run(wc, add);
                
                RenameImplGenerator rename = new RenameImplGenerator(add.method, "foo");
                SourceUtilsTestUtil2.run(wc, rename);
                
                SetTypeGenerator setType = new SetTypeGenerator(rename.method, wc.getElements().getTypeElement("java.lang.String"));
                SourceUtilsTestUtil2.run(wc, setType);
                
                SourcePositions pos[] = new SourcePositions[1];
                BlockTree btree = (BlockTree) wc.getTreeUtilities().parseStatement("{System.out.println();}", pos);
                
                SetBodyGenerator setBody = new SetBodyGenerator(setType.method, btree);
                SourceUtilsTestUtil2.run(wc, setBody);
            }
        }).commit();
        
        assertFiles("testMethodAdd.pass");
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest2/MethodTest2/";
    }
    
    private class SetTypeGenerator extends Transformer<Void, Object> {
        
        public MethodTree method;
        Element newType;
        public SetTypeGenerator(MethodTree m, Element el) {
            method = m;
            this.newType = el;
        }
        
        public Void visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            if (method.equals(node)) {
                MethodTree njuMethod = make.Method(
                        node.getModifiers(),
                        node.getName().toString(),
                        make.Identifier(newType),
                        node.getTypeParameters(),
                        node.getParameters(),
                        node.getThrows(),
                        node.getBody(),
                        (ExpressionTree) node.getDefaultValue()
                        );
                changes.rewrite(node, njuMethod);
                method = njuMethod;
            }
            return null;
        }
    }    
    
    private class SetBodyGenerator extends Transformer<Void, Object> {
        
        public MethodTree method;
        BlockTree newType;
        public SetBodyGenerator(MethodTree m, BlockTree tree) {
            method = m;
            this.newType = newType;
            newType = tree;
        }
        
        public Void visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            if (method.equals(node)) {
                MethodTree njuMethod = make.Method(
                        node.getModifiers(),
                        node.getName().toString(),
                        (ExpressionTree) node.getReturnType(),
                        node.getTypeParameters(),
                        node.getParameters(),
                        node.getThrows(),
                        newType,
                        (ExpressionTree) node.getDefaultValue()
                        );
                method = njuMethod;
                changes.rewrite(node, njuMethod);
            }
            return null;
        }
    }        
    private class RenameImplGenerator extends Transformer<Void, Object> {
        
        public MethodTree method;
        String newName;
        public RenameImplGenerator(MethodTree m, String newName) {
            method = m;
            this.newName = newName;
        }
        
        public Void visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            if (method.equals(node)) {
                MethodTree njuMethod = make.Method(
                        node.getModifiers(),
                        newName,
                        (ExpressionTree) node.getReturnType(),
                        node.getTypeParameters(),
                        node.getParameters(),
                        node.getThrows(),
                        node.getBody(),
                        (ExpressionTree) node.getDefaultValue()
                        );
                changes.rewrite(node, njuMethod);
                method = njuMethod;
            }
            return null;
        }
    }    
    private class MethodImplGenerator extends Transformer<Void, Object> {
        
        public MethodTree method;
        public MethodImplGenerator(MethodTree m) {
            method = m;
            
        }
        
        public Void visitClass(ClassTree node, Object p) {
            super.visitClass(node, p);
            TypeElement te = (TypeElement)model.getElement(node);
            if (te != null) {
                List<Tree> members = new ArrayList<Tree>();
                for(Tree m : node.getMembers())
                    members.add(m);
                members.add(method);
                ClassTree decl = make.Class(node.getModifiers(), node.getSimpleName(), node.getTypeParameters(), node.getExtendsClause(), (List<ExpressionTree>)node.getImplementsClause(), members);
                model.setElement(decl, te);
                model.setType(decl, model.getType(node));
                changes.rewrite(node, decl);
            }
            return null;
        }
    }

    private class CreateMethod extends Transformer<Void, Object> {
        public MethodTree makeMethod() {
            Set<Modifier> emptyModifs = Collections.emptySet();
            List<TypeParameterTree> emptyTpt= Collections.emptyList();
            List<VariableTree> emptyVt = Collections.emptyList();
            List<ExpressionTree> emptyEt = Collections.emptyList();
            return make.Method(
                      make.Modifiers(emptyModifs),
                      (CharSequence)"",
                      (ExpressionTree) null,
                      emptyTpt,
                      emptyVt,
                      emptyEt,
                      (BlockTree) null,
                      (ExpressionTree)null);                    
        }
    }
    
}
