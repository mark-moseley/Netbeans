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
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.MemberInfo;

/**
 *
 * @author Jan Becicka
 */
public class PushDownTransformer extends SearchVisitor {

    private MemberInfo[] members;
    public PushDownTransformer(WorkingCopy workingCopy, MemberInfo members[]) {
        super(workingCopy);
        this.members = members;
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        ClassTree njuClass = tree;
        if (el.equals(p)) {
            //source type
            boolean classIsAbstract = el.getKind().isInterface();

            for (Tree t:njuClass.getImplementsClause()) {
                Element currentInterface = workingCopy.getTrees().getElement(TreePath.getPath(getCurrentPath(), t));
                for (int i=0; i<members.length; i++) {
                    if (members[i].getType()==1 && currentInterface.equals(members[i].getElementHandle().resolve(workingCopy))) {
                        njuClass = make.removeClassImplementsClause(njuClass, t);
                        workingCopy.rewrite(tree, njuClass);
                    }
                }
            }
            
            for (Tree t: njuClass.getMembers()) {
                for (int i=0; i<members.length; i++) {
                    Element current = workingCopy.getTrees().getElement(TreePath.getPath(getCurrentPath(), t));
                    if (members[i].getType()==0 && current.equals(members[i].getElementHandle().resolve(workingCopy))) {
                        Boolean b = members[i].getUserData().lookup(Boolean.class);
                        if (b==null?Boolean.FALSE:b) {
                            
                            if (!classIsAbstract) {
                                classIsAbstract = true;
                                Set<Modifier> mod = new HashSet<Modifier>(njuClass.getModifiers().getFlags());
                                mod.add(Modifier.ABSTRACT);
                                ModifiersTree modifiers = make.Modifiers(mod);
                                workingCopy.rewrite(njuClass.getModifiers(), modifiers);
                            }
                            
                            
                            MethodTree method = (MethodTree) t;
                            Set<Modifier> mod = new HashSet<Modifier>(method.getModifiers().getFlags());
                            mod.add(Modifier.ABSTRACT);
                            MethodTree nju = make.Method(
                                    make.Modifiers(mod),
                                    method.getName(),
                                    method.getReturnType(),
                                    method.getTypeParameters(),
                                    method.getParameters(),
                                    method.getThrows(),
                                    (BlockTree) null,
                                    (ExpressionTree)method.getDefaultValue());
                            workingCopy.rewrite(method, nju);
                        } else {
                            njuClass = make.removeClassMember(njuClass, t);
                            workingCopy.rewrite(tree, njuClass);
                        }
                    }
                }
                
            }
        } else {
            //target type
            TypeMirror tm = el.asType();
            if (workingCopy.getTypes().isSubtype(tm, p.asType())) {
                for (int i = 0; i<members.length; i++) {
                    if (members[i].getType()==1) {
                        njuClass = make.addClassImplementsClause(njuClass, make.Identifier(members[i].getElementHandle().resolve(workingCopy)));
                    } else {
                        njuClass = make.addClassMember(njuClass, workingCopy.getTrees().getTree(members[i].getElementHandle().resolve(workingCopy)));
                    }
                }
                workingCopy.rewrite(tree, njuClass);
            }
        }
        return super.visitClass(tree, p);
    }
    
}
