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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;

/**
 * according to <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#javadocoptions">Javadoc Options</a>
 */
enum Access {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    /**
     * accept [public|protected|package|private], default(null or other) is protected
     */
    public static Access resolve(String s) {
        if (s != null) {
            s = s.trim().toLowerCase();
            if ("public".equals(s)) { // NOI18N
                return Access.PUBLIC;
            } else if ("protected".equals(s)) { // NOI18N
                return Access.PROTECTED;
            } else if ("private".equals(s)) { // NOI18N
                return Access.PRIVATE;
            } else if ("package".equals(s)) { // NOI18N
                return Access.PACKAGE;
            }
        }
        return Access.PROTECTED;
    }

    public boolean isAccessible(Set<Modifier> flags) {
        switch(this) {
        case PRIVATE:
            return true;
        case PACKAGE:
            return !flags.contains(Modifier.PRIVATE);
        case PROTECTED:
            return flags.contains(Modifier.PUBLIC) || flags.contains(Modifier.PROTECTED);
        case PUBLIC:
            return flags.contains(Modifier.PUBLIC);
        default:
            throw new IllegalStateException();
        }
    }

    /**
     * @param path path to check
     * @param alwaysAccessible true means to check if the path contains only class members;
     *                         false means to check besides class members also their modifiers
     * @return is accessible
     * @see #isAccessible(Set)
     */
    public boolean isAccessible(CompilationInfo javac, TreePath path, boolean alwaysAccessible) {
        TreePath parent = path.getParentPath();
        Tree leaf = path.getLeaf();
        if (parent != null) {
            Tree.Kind parentKind = parent.getLeaf().getKind();
            if (parentKind != Tree.Kind.CLASS && parentKind != Tree.Kind.COMPILATION_UNIT) {
                // not class member
                return false;
            }

            if (!isAccessible(javac, parent, alwaysAccessible)) {
                return false;
            }
        }

        Set<Modifier> flags;
        switch (leaf.getKind()) {
        case COMPILATION_UNIT: return true;
        case CLASS: flags = ((ClassTree) leaf).getModifiers().getFlags(); break;
        case METHOD: flags = ((MethodTree) leaf).getModifiers().getFlags(); break;
        case VARIABLE: flags = ((VariableTree) leaf).getModifiers().getFlags(); break;
        default: return false;
        }

        // all members of interface and annotation type are public by definition (JLS 9.1.5)
        return alwaysAccessible || isInterfaceMember(javac, path) || isAccessible(flags);
    }

    /**
     * @return is member of interface or annotatin type
     */
    private boolean isInterfaceMember(CompilationInfo javac, TreePath path) {
        TreePath parentPath = path.getParentPath();
        if (parentPath == null) {
            return false;
        }
        Tree parent = parentPath.getLeaf();
        TreeUtilities utils = javac.getTreeUtilities();
        return Tree.Kind.CLASS == parent.getKind()
                && (utils.isInterface((ClassTree) parent) || utils.isAnnotation((ClassTree) parent));
    }
}
