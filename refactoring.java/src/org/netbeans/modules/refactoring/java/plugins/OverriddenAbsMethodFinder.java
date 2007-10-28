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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.util.TreePath;
import java.util.Collection;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGrip;

/**
 *
 * @author Bharath Ravi Kumar
 */
final class OverriddenAbsMethodFinder implements CancellableTask<CompilationController>{

    private final TreePathHandle methodHandle;
    Collection<ElementGrip> refactoringElements;
    OverriddenAbsMethodFinder(TreePathHandle methodPathHandle, Collection<ElementGrip> refacElemsColl) {
        refactoringElements = refacElemsColl;
        methodHandle = methodPathHandle;
    }

    public void cancel() {
        
    }

    public void run(CompilationController compilationController) throws Exception {
        ExecutableElement implementingMethod = (ExecutableElement) 
                methodHandle.resolveElement(compilationController);
        Collection<ExecutableElement> overriddenMethods = RetoucheUtils.getOverridenMethods(implementingMethod, 
                compilationController);
        for (ExecutableElement overriddenMethod : overriddenMethods) {
            if(overriddenMethod.getModifiers().contains(Modifier.ABSTRACT)){
                TreePath overriddenMethTreePath = getTreePath(overriddenMethod, compilationController);
                refactoringElements.add(new ElementGrip(overriddenMethTreePath, compilationController));
            }
        }

    }

    private static TreePath getTreePath(ExecutableElement overriddenMethod, CompilationController compilationController) {
        return compilationController.getTrees().getPath(overriddenMethod);
    }

}
