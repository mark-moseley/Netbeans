/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.api.*;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 * @author Jan Becicka, Bharath Ravi Kumar
 */
class VarUsageVisitor extends RefactoringVisitor {

    private final TypeElement superTypeElement;
    private final TypeElement subTypeElement;
    private boolean isReplCandidate = true;

    VarUsageVisitor(TypeElement subTypeElement, WorkingCopy workingCopy, 
            TypeElement superTypeElem) {
        try {
            setWorkingCopy(workingCopy);
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.superTypeElement = superTypeElem;
        this.subTypeElement = subTypeElement;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree memSelectTree, Element refVarElem) {

        Element methodElement = this.asElement(memSelectTree);
        Element varElement = asElement(memSelectTree.getExpression());
        if (!refVarElem.equals(varElement)) {
            return super.visitMemberSelect(memSelectTree, refVarElem);
        }

        boolean isAssgCmptble = isMemberAvailable(subTypeElement, methodElement, 
                superTypeElement);
        if (!isAssgCmptble) {
            isReplCandidate = false;
        }
        return super.visitMemberSelect(memSelectTree, refVarElem);
    }

    @Override
    public Tree visitAssignment(AssignmentTree assgnTree, Element refVarElem) {

        ExpressionTree exprnTree = assgnTree.getExpression();
        Element exprElement = asElement(exprnTree);
        if (!refVarElem.equals(exprElement)) {
            return super.visitAssignment(assgnTree, refVarElem);
        }
        ExpressionTree varExprTree = assgnTree.getVariable();

        VariableElement varElement = (VariableElement) asElement(varExprTree);
        isReplCandidate = isReplacableAssgnmt(varElement) && isReplCandidate;
        return super.visitAssignment(assgnTree, refVarElem);
    }

    @Override
    public Tree visitVariable(VariableTree varTree, Element refVarElem) {
        ExpressionTree initTree = varTree.getInitializer();
        if (null == initTree) {
            return super.visitVariable(varTree, refVarElem);
        }
        Element exprElement = asElement(initTree);
        if (!refVarElem.equals(exprElement)) {
            return super.visitVariable(varTree, refVarElem);
        }
        VariableElement varElement = (VariableElement) asElement(varTree);
        isReplCandidate = isReplacableAssgnmt(varElement) && isReplCandidate;
        return super.visitVariable(varTree, refVarElem);
    }

    private boolean isMemberAvailable(TypeElement subTypeElement, Element methodElement, 
            TypeElement superTypeElement) {
        ElementKind memberKind = methodElement.getKind();
        if(ElementKind.METHOD.equals(memberKind)){
            return isMethodAvailable(subTypeElement, (ExecutableElement)methodElement, 
                    superTypeElement);
        }else{
            return isHidingMember(subTypeElement, methodElement, superTypeElement);
        }
    }

    private boolean isMethodAvailable(TypeElement subTypeElement, 
            ExecutableElement execElem, TypeElement superTypeElement) {
        Elements elements = workingCopy.getElements();
        List<? extends Element> memberElements = elements.getAllMembers(superTypeElement);
        for (Element elem : memberElements) {
            if(ElementKind.METHOD.equals(elem.getKind())){
                if(isStatic(execElem) && elements.hides(execElem, elem)){
                    return true;
                }else{
                    if(execElem.equals(elem) || elements.overrides(execElem, (ExecutableElement)elem, 
                            subTypeElement)){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isHidingMember(TypeElement subTypeElement, Element variableElement, 
            TypeElement superTypeElement) {
        //TODO: We do not handle nested types yet (includes enums)
        Elements elements = workingCopy.getElements();
        List<? extends Element> memberElements = elements.getAllMembers(superTypeElement);
        for (Element elem : memberElements) {
            if(variableElement.equals(elem) || elements.hides(variableElement, elem)){
                return true;
            }
        }
        return false;
    }

    private boolean isReplacableAssgnmt(VariableElement varElement) {
        if (isDeclaredType(varElement.asType())) {
            DeclaredType declType = (DeclaredType) varElement.asType();
            TypeElement varType = (TypeElement) declType.asElement();
            if (isAssignable(superTypeElement, varType)) {
                return true;
            }
        }
        return false;
    }

    boolean isReplaceCandidate() {
        return isReplCandidate;
    }

    private boolean isAssignable(TypeElement typeFrom, TypeElement typeTo) {
        Types types = workingCopy.getTypes();
        return types.isAssignable(typeFrom.asType(), typeTo.asType());
    }

    private Element asElement(Tree tree) {
        Trees treeUtil = workingCopy.getTrees();
        TreePath treePath = treeUtil.getPath(workingCopy.getCompilationUnit(), tree);
        Element element = treeUtil.getElement(treePath);
        return element;
    }

    private boolean isDeclaredType(TypeMirror type) {
        return TypeKind.DECLARED.equals(type.getKind());
    }
    //TODO: This method can be shared. Copied from UseSuperTypeRefactoringPlugin.
    private boolean isStatic(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        return modifiers.contains(Modifier.STATIC);
    }

}
