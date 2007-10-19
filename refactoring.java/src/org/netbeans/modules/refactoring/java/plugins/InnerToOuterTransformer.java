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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.GeneratorUtilities;
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
    
    @Override
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
        if (refactoring.getReferenceName()!=null && currentElement!=null && workingCopy.getTypes().isSubtype(getCurrentElement().getEnclosingElement().asType(), inner.asType())) {
            String thisString;
            if (getCurrentClass()==inner) {
                thisString = refactoring.getReferenceName();
            } else if (workingCopy.getTypes().isSubtype(getCurrentClass().asType(),outer.asType())) {
                thisString = "this";
            } else {
                TypeElement thisOuter = getOuter(getCurrentClass());
                if (thisOuter!=null)
                    thisString = getOuter(getCurrentClass()).getQualifiedName().toString() + ".this";
                else 
                    thisString = "this";
            
            }
            if (thisString!=null) {
                rewrite(arg0,make.addNewClassArgument(arg0, make.Identifier(thisString)));
            }
        }
        return super.visitNewClass(arg0, arg1);
    }
    
    private TypeElement getOuter(TypeElement element) {
        while (element != null && !workingCopy.getTypes().isSubtype(element.asType(),outer.asType())) {
            element = SourceUtils.getEnclosingTypeElement(element);
        }
        return element;
    }

    @Override
    public Tree visitMethod(MethodTree constructor, Element element) {
        if (constructor.getReturnType()==null) {
            //constructor
            if (!inner.equals(getCurrentClass()) && workingCopy.getTypes().isSubtype(getCurrentElement().getEnclosingElement().asType(), inner.asType())) {
                MemberSelectTree arg = make.MemberSelect(make.Identifier(getCurrentClass().getEnclosingElement().getSimpleName()), "this");
                MethodInvocationTree superCall = (MethodInvocationTree) ((ExpressionStatementTree)constructor.getBody().getStatements().get(0)).getExpression();
                MethodInvocationTree newSuperCall = make.insertMethodInvocationArgument(superCall, 0, arg);
                rewrite(superCall, newSuperCall);
            }
            
        }
        return super.visitMethod(constructor, element);
    }

    @Override
    public Tree visitClass(ClassTree classTree, Element element) {
        Element currentElement = workingCopy.getTrees().getElement(getCurrentPath());
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy); // helper        
        if (currentElement!=null && currentElement == outer) {
            Element outerouter = outer.getEnclosingElement();
            
            TreePath tp = workingCopy.getTrees().getPath(inner);
            ClassTree innerClass = (ClassTree) tp.getLeaf();

            ClassTree newInnerClass = innerClass;
            newInnerClass = genUtils.importComments(newInnerClass, workingCopy.getCompilationUnit());
            newInnerClass = genUtils.importFQNs(newInnerClass);

            newInnerClass = make.setLabel(innerClass, refactoring.getClassName());
            
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
                ClassTree newOuterOuter = GeneratorUtilities.get(workingCopy).insertClassMember(outerouterTree, newInnerClass);
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
        } else if (isThisReferenceToOuter()&& !"class".equals(memberSelect.getIdentifier().toString()) && refactoring.getReferenceName()!=null) { //NOI18N
            MemberSelectTree m = make.MemberSelect(make.Identifier(refactoring.getReferenceName()), memberSelect.getIdentifier());
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
        VariableTree variable = null;
        if (referenceName != null) {
            variable = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), refactoring.getReferenceName(), make.Type(outer.asType()), null);
            newInnerClass = GeneratorUtilities.get(workingCopy).insertClassMember(newInnerClass, variable);
        }
        
        ModifiersTree modifiersTree = newInnerClass.getModifiers();
        ModifiersTree newModifiersTree = make.removeModifiersModifier(modifiersTree, Modifier.PRIVATE);
        newModifiersTree = make.removeModifiersModifier(newModifiersTree, Modifier.STATIC);
        newModifiersTree = make.removeModifiersModifier(newModifiersTree, Modifier.PROTECTED);
        rewrite(modifiersTree, newModifiersTree);

        if (referenceName != null) {
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
                        newInnerClass = GeneratorUtilities.get(workingCopy).insertClassMember(newInnerClass, newConstructor);
                    }
                }
            }
        }
        return newInnerClass;
    }
}
