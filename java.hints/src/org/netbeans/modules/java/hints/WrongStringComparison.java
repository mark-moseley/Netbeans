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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class WrongStringComparison extends AbstractHint {

    private static String STRING_TYPE = "java.lang.String";  // NOI18N
    
    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private static final Set<Tree.Kind> TREE_KINDS = 
            EnumSet.<Tree.Kind>of( Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO );
    
    public WrongStringComparison() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }


    public Set<Kind> getTreeKinds() {
        return TREE_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
                
        Tree t = treePath.getLeaf();
        
        if ( t.getKind() != Tree.Kind.EQUAL_TO && 
             t.getKind() != Tree.Kind.NOT_EQUAL_TO ) {
            return null;
        }
        
        BinaryTree bt = (BinaryTree) t;
        
        TreePath left = new TreePath(treePath, bt.getLeftOperand() );
        TreePath right = new TreePath(treePath, bt.getRightOperand() );
        
        Trees trees = info.getTrees(); 
        TypeMirror leftType = left == null ? null : trees.getTypeMirror(left);
        TypeMirror rightType = right == null ? null : trees.getTypeMirror(right);

        Kind kind = treePath.getLeaf().getKind(); //do not show for strings inside equals()
        boolean conditionalAnd = false;
        while (kind != Kind.CLASS && kind != Kind.COMPILATION_UNIT) {
            if(kind == Kind.CONDITIONAL_AND)
                conditionalAnd = true;
            if (kind == kind.METHOD) {
                if (conditionalAnd && ((MethodTree) treePath.getLeaf()).getName().toString().equals("equals")) {
                    return null;
                }
                break;
            }
            treePath = treePath.getParentPath();
            kind = treePath.getLeaf().getKind();
        }
        
        if ( leftType != null && rightType != null && 
             STRING_TYPE.equals(leftType.toString()) && 
             STRING_TYPE.equals(rightType.toString())) {
            return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(), 
                    getDisplayName(), 
                    // Collections.<Fix>singletonList(new EmptyStatementFix( info.getFileObject(), TreePathHandle.create(tp, info) ) ), 
                    NO_FIXES,    
                    info.getFileObject(),
                    (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                    (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)) );

        }
        
        return null;
    }

    public void cancel() {
        // Does nothing
    }
    
    public String getId() {
        return "Wrong_String_Comparison"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WrongStringComparison.class, "LBL_WrongStringComparison");
    }

    public String getDescription() {
        return NbBundle.getMessage(WrongStringComparison.class, "DSC_WrongStringComparison");
    }

}
