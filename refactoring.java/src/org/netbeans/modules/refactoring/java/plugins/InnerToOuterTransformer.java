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

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class InnerToOuterTransformer extends RefactoringVisitor {

    private Element inner;
    private Element outer;
    private InnerToOuterRefactoring refactoring;
    
    private Element getCurrentElement() {
        return workingCopy.getTrees().getElement(getCurrentPath());
    }
    
    public InnerToOuterTransformer(InnerToOuterRefactoring re) {
        this.refactoring = re;
    }
    
    public void setWorkingCopy(WorkingCopy wc) throws ToPhaseException {
        super.setWorkingCopy(wc);
        this.inner = refactoring.getSourceType().resolveElement(wc);
        outer = SourceUtils.getEnclosingTypeElement(inner);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (inner.equals(getCurrentElement())) {
            Tree newTree = make.setLabel(node, refactoring.getClassName());        
            rewrite(node, newTree);
        } else if (isThisReferenceToOuter()) {
            IdentifierTree m = make.Identifier(refactoring.getReferenceName() + "." + node.getName().toString());
            rewrite(node, m);
        }
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitNewClass(NewClassTree arg0, Element arg1) {
        Element currentElement = workingCopy.getTrees().getElement(getCurrentPath());
        if (currentElement!=null && workingCopy.getTypes().isSubtype(getCurrentElement().getEnclosingElement().asType(), inner.asType())) {
            rewrite(arg0,make.addNewClassArgument(arg0, null, make.Identifier(getCurrentClass()==inner?refactoring.getReferenceName():"this")));
        }
        return super.visitNewClass(arg0, arg1);
    }

    @Override
    public Tree visitMethod(MethodTree constructor, Element element) {
        if (constructor.getReturnType()==null) {
            //constructor
            if (!inner.equals(getCurrentClass()) && workingCopy.getTypes().isSubtype(getCurrentElement().getEnclosingElement().asType(), inner.asType())) {
                MemberSelectTree arg = make.MemberSelect(make.Identifier(getCurrentClass().getEnclosingElement().getSimpleName()), "this");
                MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree)constructor.getBody().getStatements().get(0)).getExpression();
                MethodInvocationTree newSuperCall = make.insertMethodInvocationArgument(superCall, 0, arg, null);
                rewrite(superCall, newSuperCall);
            }
            
        }
        return super.visitMethod(constructor, element);
    }

    @Override
    public Tree visitClass(ClassTree classTree, Element element) {
        Element currentElement = workingCopy.getTrees().getElement(getCurrentPath());
        if (currentElement!=null && currentElement == outer) {
            Element outerouter = outer.getEnclosingElement();
            
            TreePath tp = workingCopy.getTrees().getPath(inner);
            ClassTree innerClass = (ClassTree) tp.getLeaf();
            ClassTree newInnerClass = make.setLabel(innerClass, refactoring.getClassName());
            
            newInnerClass = refactorInnerClass(newInnerClass);
            
            if (outerouter.getKind() == ElementKind.PACKAGE) {
                FileObject sourceRoot=ClassPath.getClassPath(workingCopy.getFileObject(), ClassPath.SOURCE).findOwnerRoot(workingCopy.getFileObject());
                ClassTree outerTree = (ClassTree) workingCopy.getTrees().getTree(outer);
                ClassTree newOuter = make.removeClassMember(outerTree, innerClass);
                workingCopy.rewrite(outerTree, newOuter);
                JavaRefactoringUtils.cacheTreePathInfo(workingCopy.getTrees().getPath(outer), workingCopy);
                CompilationUnitTree compilationUnit = tp.getCompilationUnit();
                String relativePath = compilationUnit.getPackageName().toString().replace('.', '/') + '/' + refactoring.getClassName() + ".java";
                CompilationUnitTree newCompilation = make.CompilationUnit(sourceRoot, relativePath, null, Collections.singletonList(newInnerClass));
                workingCopy.rewrite(null, newCompilation);        
            } else {
                ClassTree outerTree = (ClassTree) workingCopy.getTrees().getTree(outer);
                ClassTree outerouterTree = (ClassTree) workingCopy.getTrees().getTree(outerouter);
                ClassTree newOuter = make.removeClassMember(outerTree, innerClass);
                ClassTree newOuterOuter = make.addClassMember(outerouterTree, newInnerClass);
                workingCopy.rewrite(outerTree, newOuter);
                JavaRefactoringUtils.cacheTreePathInfo(workingCopy.getTrees().getPath(outer), workingCopy);
                workingCopy.rewrite(outerouterTree, newOuterOuter);
            }
            
            for (Element superType:RetoucheUtils.getSuperTypes((TypeElement)inner, workingCopy, true)) {
                ClassTree tree = (ClassTree) workingCopy.getTrees().getTree(superType);
            }
        } else if (currentElement!=null && workingCopy.getTypes().isSubtype(currentElement.asType(), inner.asType()) && currentElement!=inner) {
                VariableTree variable = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), refactoring.getReferenceName(), make.Type(outer.asType()), null);
                for (Tree member:classTree.getMembers()) {
                    if (member.getKind() == Tree.Kind.METHOD) {
                        MethodTree m = (MethodTree) member;
                        if (m.getReturnType()==null) {
                            MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree) m.getBody().getStatements().get(0)).getExpression();
                            List<ExpressionTree> newArgs = new ArrayList(superCall.getArguments());
                            newArgs.add((ExpressionTree)make.Identifier(variable.getName().toString()));
                            MethodInvocationTree method = make.MethodInvocation(
                                    Collections.<ExpressionTree>emptyList(), 
                                    make.Identifier("super"),
                                    newArgs);

                            BlockTree block = make.insertBlockStatement(m.getBody(), 0, make.ExpressionStatement(method));
                            block = make.removeBlockStatement(block, 1);
                            
                            MethodTree newConstructor = make.addMethodParameter(m, variable);
                            newConstructor = make.Constructor(
                                    make.Modifiers(newConstructor.getModifiers().getFlags(), newConstructor.getModifiers().getAnnotations()),
                                    newConstructor.getTypeParameters(), 
                                    newConstructor.getParameters(),
                                    newConstructor.getThrows(),
                                    block);

                            rewrite(m, newConstructor);
                        }
                    }
                }                
            }
        return super.visitClass(classTree, element);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree memberSelect, Element element) {
        Element current = getCurrentElement();
        if (inner.equals(current)) {
            ExpressionTree ex = memberSelect.getExpression();
            Tree newTree;
            if (ex.getKind() == Tree.Kind.IDENTIFIER) {
                newTree = make.Identifier(refactoring.getClassName());
                rewrite(memberSelect, newTree);
            } else if (ex.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree m = make.MemberSelect(((MemberSelectTree) ex).getExpression(),refactoring.getClassName());
                rewrite(memberSelect,m);
            }
        } else if (isThisReferenceToOuter()) {
            MemberSelectTree m = make.MemberSelect(memberSelect, refactoring.getReferenceName());
            rewrite(memberSelect, m);
        }
        
        return super.visitMemberSelect(memberSelect, element);
    }
    
    private boolean isThisReferenceToOuter() {
        Element cur = getCurrentElement();
        if (cur==null || cur.getKind() == ElementKind.PACKAGE)
                return false;
        TypeElement encl = SourceUtils.getEnclosingTypeElement(cur);
        if (outer.equals(encl) && workingCopy.getTypes().isSubtype(getCurrentClass().asType(), inner.asType())) {
            return true;
        }
        return false;
    }
    
    private TypeElement getCurrentClass() {
        TreePath tp = getCurrentPath().getParentPath();
        while (tp!=null) {
            if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                return (TypeElement) workingCopy.getTrees().getElement(tp);
            }
            tp = tp.getParentPath();
        }
        throw new IllegalStateException();
    }

    
    private boolean isIn(Element el) {
        if (el==null)
            return false;
        Element current = el;
        while (current.getKind() != ElementKind.PACKAGE) {
            if (current.equals(inner)) {
                return true;
            }
            current = current.getEnclosingElement();
        }
        return false;
    }
    
    private ClassTree refactorInnerClass(ClassTree newInnerClass) {
        String referenceName = refactoring.getReferenceName();
        if (referenceName != null) {
            VariableTree variable = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), refactoring.getReferenceName(), make.Type(outer.asType()), null);
            newInnerClass = make.insertClassMember(newInnerClass, 0, variable);
            Set <Modifier> mods = new HashSet(newInnerClass.getModifiers().getFlags());
            mods.remove(Modifier.STATIC);
            mods.remove(Modifier.PRIVATE);
            newInnerClass = make.Class(
                    make.Modifiers(mods, newInnerClass.getModifiers().getAnnotations()),
                    newInnerClass.getSimpleName(),
                    newInnerClass.getTypeParameters(),
                    newInnerClass.getExtendsClause(),
                    newInnerClass.getImplementsClause(),
                    newInnerClass.getMembers());

            for (Tree member:newInnerClass.getMembers()) {
                if (member.getKind() == Tree.Kind.METHOD) {
                    MethodTree m = (MethodTree) member;
                    if (m.getReturnType()==null) {
                        MethodTree newConstructor = make.addMethodParameter(m, variable);
                        AssignmentTree assign = make.Assignment(make.Identifier("this."+referenceName), make.Identifier(referenceName));
                        BlockTree block = make.insertBlockStatement(newConstructor.getBody(), 1, make.ExpressionStatement(assign));
                        newConstructor = make.Constructor(
                                make.Modifiers(newConstructor.getModifiers().getFlags(), newConstructor.getModifiers().getAnnotations()),
                                newConstructor.getTypeParameters(), 
                                newConstructor.getParameters(),
                                newConstructor.getThrows(),
                                block);

                        newInnerClass = make.removeClassMember(newInnerClass, m);
                        newInnerClass = make.addClassMember(newInnerClass, newConstructor);
                    }
                }
            }
        }
        return newInnerClass;
    }

//    @Override
//    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
//        updateUsageIfMatch(getCurrentPath(), node,p);
//        return super.visitMemberSelect(node, p);
//    }
}
