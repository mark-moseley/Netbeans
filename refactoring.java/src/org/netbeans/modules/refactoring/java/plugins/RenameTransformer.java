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
import com.sun.source.util.TreePath;
import java.lang.annotation.ElementType;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class RenameTransformer extends SearchVisitor {

    private Set<ElementHandle<ExecutableElement>> allMethods;
    private String newName;
    Elements elements = workingCopy.getElements();
    

    public RenameTransformer(String newName, WorkingCopy workingCopy, Set<ElementHandle<ExecutableElement>> am) {
        super(workingCopy);
        this.newName = newName;
        this.allMethods = am;
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    private void renameUsageIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        Element el = workingCopy.getTrees().getElement(path);
        if (el==null)
            return;
        
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            Tree nju = make.setLabel(tree, newName);
            workingCopy.rewrite(tree, nju);
        }
    }

    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitClass(tree, p);
    }

    @Override
    public Tree visitVariable(VariableTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitVariable(tree, p);
    }

    @Override
    public Tree visitTypeParameter(TypeParameterTree arg0, Element arg1) {
        renameDeclIfMatch(getCurrentPath(), arg0, arg1);
        return super.visitTypeParameter(arg0, arg1);
    }
    
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        Element el = workingCopy.getTrees().getElement(path);
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            Tree nju = make.setLabel(tree, newName);
            workingCopy.rewrite(tree, nju);
            return;
        }
    }
    
    private boolean isMethodMatch(Element method) {
        if (method.getKind() == ElementKind.METHOD && allMethods !=null) {
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
