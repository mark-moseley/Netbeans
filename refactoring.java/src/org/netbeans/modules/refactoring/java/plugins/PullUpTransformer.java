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
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;

/**
 *
 * @author Jan Becicka
 */
public class PullUpTransformer extends RefactoringVisitor {

    private MemberInfo<ElementHandle<? extends Element>>[] members;
    private Element targetType;
    private PullUpRefactoring refactoring;
    public PullUpTransformer(PullUpRefactoring refactoring) {
        this.refactoring = refactoring;
        this.members = refactoring.getMembers();
    }
    
    public void setWorkingCopy(WorkingCopy copy) throws ToPhaseException {
        super.setWorkingCopy(copy);
        this.targetType = refactoring.getTargetType().resolve(copy);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy); // helper
        boolean classIsAbstract = el.getKind().isInterface();
        ClassTree njuClass = tree;
        if (el.equals(targetType)) {
            //target type
            //add members
            for (int i = 0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
                    njuClass = make.addClassImplementsClause(njuClass, make.Identifier(members[i].getElementHandle().resolve(workingCopy)));
                    rewrite(tree, njuClass);
                } else {
                    if (members[i].isMakeAbstract()) {
                        
                        if (!classIsAbstract) {
                            classIsAbstract = true;
                            Set<Modifier> mod = new HashSet<Modifier>(njuClass.getModifiers().getFlags());
                            mod.add(Modifier.ABSTRACT);
                            mod.remove(Modifier.FINAL);
                            ModifiersTree modifiers = make.Modifiers(mod);
                            rewrite(njuClass.getModifiers(), modifiers);
                        }
                        
                        Element methodElm = members[i].getElementHandle().resolve(workingCopy);
                        MethodTree method = (MethodTree) workingCopy.getTrees().getTree(methodElm);
                        Set<Modifier> mod = new HashSet<Modifier>(method.getModifiers().getFlags());
                        mod.add(Modifier.ABSTRACT);
                        mod.remove(Modifier.FINAL);
                        if (el.getKind().isInterface()) {
                            mod.remove(Modifier.PUBLIC);
                            mod.remove(Modifier.PROTECTED);
                            mod.remove(Modifier.PRIVATE);
                            mod.remove(Modifier.ABSTRACT);
                        }
                        MethodTree nju = make.Method(
                                make.Modifiers(mod),
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                method.getParameters(),
                                method.getThrows(),
                                (BlockTree) null,
                                (ExpressionTree)method.getDefaultValue());
                        nju = genUtils.importFQNs(nju);
                        RetoucheUtils.copyJavadoc(methodElm, nju, workingCopy);
                        njuClass = genUtils.insertClassMember(njuClass, nju);
                        rewrite(tree, njuClass);
                    } else {                        
                        Tree newMethodTree = genUtils.importFQNs(workingCopy.getTrees().getTree(members[i].getElementHandle().resolve(workingCopy)));
                        njuClass = genUtils.insertClassMember(njuClass, newMethodTree);
                        rewrite(tree, njuClass);
                    }
                }
            }
        } else {
            for (int i=0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
                    for (Tree t:njuClass.getImplementsClause()) {
                        Element currentInterface = workingCopy.getTrees().getElement(TreePath.getPath(getCurrentPath(), t));
                        if (currentInterface.equals(members[i].getElementHandle().resolve(workingCopy))) {
                            njuClass = make.removeClassImplementsClause(njuClass, t);
                            rewrite(tree, njuClass);
                        }
                    }
                } else {
                    Element current = workingCopy.getTrees().getElement(getCurrentPath());
                    Element currentMember = members[i].getElementHandle().resolve(workingCopy);
                    if (currentMember.getEnclosingElement().equals(current)) {
                        if (!members[i].isMakeAbstract()) {
                            njuClass = make.removeClassMember(njuClass, workingCopy.getTrees().getTree(currentMember));
                            rewrite(tree, njuClass);
                        }
                    }
                }
            }
        }
        return super.visitClass(tree, p);
    }
    
}
