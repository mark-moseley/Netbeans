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
import java.util.logging.Logger;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class CopyTransformer extends RefactoringVisitor {
    
    private String newName;
    private boolean insertImport;
    private String oldPackage;
    private String oldName;
    private String newPackage;

    public CopyTransformer(WorkingCopy workingCopy, String oldName, String newName, boolean insertImport, String oldPackage) {
        try {
            setWorkingCopy(workingCopy);
            this.newName = newName;
            this.insertImport = insertImport;
            this.oldPackage = oldPackage;
            this.oldName = oldName;
            this.newPackage = RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent());
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            if (insertImport) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                Tree tree2 = make.insertCompUnitImport(tree, 0, make.Import(make.Identifier(oldPackage + ".*"), false));
                rewrite(tree, tree2);
            }
        }
        return super.visitCompilationUnit(tree, p);
    }         

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            TypeElement currentClass = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            if (currentClass == null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").severe("Cannot resolve tree " + tree + "\n file: " + workingCopy.getFileObject().getPath());
            } else {
                if (!currentClass.getNestingKind().isNested() && ((workingCopy.getCompilationUnit().getTypeDecls().size() == 1) || tree.getSimpleName().toString().equals(oldName))) {
                    Tree nju = make.setLabel(tree, newName);
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitClass(tree, p);
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
        
        if ((el instanceof TypeElement) && ((TypeElement) el).getQualifiedName().toString().equals(newPackage+"."+oldName)) {
            Tree nju = make.setLabel(tree, newName);
            rewrite(tree, nju);
        }
    }
    
}
