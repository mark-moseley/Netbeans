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

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;

/**
 *
 * @author Jan Becicka
 */
public class ChangeParamsTransformer extends SearchVisitor {

    private Set<ElementHandle<ExecutableElement>> allMethods;
    Elements elements = workingCopy.getElements();
    

    public ChangeParamsTransformer(ChangeParametersRefactoring refactoring, WorkingCopy workingCopy, Set<ElementHandle<ExecutableElement>> am) {
        super(workingCopy);
        this.refactoring = refactoring;
        this.allMethods = am;
    }
    
    @Override
    public Tree visitNewClass(NewClassTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    NewClassTree nju = make.NewClass(tree.getEnclosingExpression(),
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getIdentifier(),
                            arguments,
                            tree.getClassBody());
                    workingCopy.rewrite(tree, nju);
                }
            }
        }
        return super.visitNewClass(tree, p);
    }
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = refactoring.getParameterInfo();
        for (int i=0; i<pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex <0) {
                String value = pi[i].getDefaultValue();
                SourcePositions pos[] = new SourcePositions[1];
                vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            } else {
                vt = currentArguments.get(pi[i].getOriginalIndex());
            }
            arguments.add(vt);
        }
        return arguments;
    }
    

    public Tree visitMethodInvocation(MethodInvocationTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    
                    MethodInvocationTree nju = make.MethodInvocation(
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getMethodSelect(),
                            arguments);
                    workingCopy.rewrite(tree, nju);
                }
            }
        }
        return super.visitMethodInvocation(tree, p);
    }
    
    
    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    ChangeParametersRefactoring refactoring;
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        MethodTree current = (MethodTree) tree;
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList();
            
            ParameterInfo[] p = refactoring.getParameterInfo();
            for (int i=0; i<p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex <0) {
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), p[i].getName(),make.Identifier(p[i].getType()), null);
                } else {
                    vt = currentParameters.get(p[i].getOriginalIndex());
                }
                newParameters.add(vt);
            }
            
            MethodTree nju = make.Method(
                    make.Modifiers(refactoring.getModifiers()),
                    current.getName(),
                    current.getReturnType(),
                    current.getTypeParameters(),
                    newParameters,
                    current.getThrows(),
                    current.getBody(),
                    (ExpressionTree) current.getDefaultValue());
            workingCopy.rewrite(tree, nju);
            return;
        }
    }
    
    private boolean isMethodMatch(Element method) {
        if ((method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod.equals(method) || elements.overrides((ExecutableElement)method, baseMethod, SourceUtils.getEnclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }
}
