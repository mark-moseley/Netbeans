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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Jan Becicka
 */
public class FindOverridingVisitor extends SearchVisitor {

    public FindOverridingVisitor(WorkingCopy workingCopy) {
        super(workingCopy);
    }

    @Override
    public Tree visitMethod(MethodTree node, Element elementToFind) {
        ExecutableElement el = (ExecutableElement) workingCopy.getTrees().getElement(getCurrentPath());

        if (workingCopy.getElements().overrides(el, (ExecutableElement) elementToFind, (TypeElement) el.getEnclosingElement())) {
            addUsage(node);
        }
        return super.visitMethod(node, elementToFind);
    }
    
}
