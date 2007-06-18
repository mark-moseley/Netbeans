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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * This hint changes the type of a variable to the type of
 * initializer expression. In effect it works opposite of Add Cast hist.
 *
 * @author Sandip Chitale
 */
final class ChangeTypeFix implements Fix {
    
    private JavaSource js;
    private String treeName;
    private String type;
    private int position;
    
    public ChangeTypeFix(JavaSource js, String treeName, String type, int position) {
        this.js = js;
        this.treeName = treeName;
        this.type = type;
        this.position = position;
    }
    
    public ChangeInfo implement() {
        try {
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(final WorkingCopy working) throws IOException {
                    working.toPhase(Phase.RESOLVED);
                    TypeMirror[] tm = new TypeMirror[1];
                    ExpressionTree[] expression = new ExpressionTree[1];
                    TypeMirror[] expressionType = new TypeMirror[1];
                    Tree[] leaf = new Tree[1];
                    
                    ChangeType.computeType(working, position, tm, expression, expressionType, leaf);
                    
                    if (leaf[0] instanceof VariableTree) {
                        VariableTree oldVariableTree = ((VariableTree)leaf[0]);
                        TreeMaker make = working.getTreeMaker();
    
                        VariableTree newVariableTree = make.Variable(
                                oldVariableTree.getModifiers(), 
                                oldVariableTree.getName(),
                                make.Type(expressionType[0]),
                                oldVariableTree.getInitializer()); 
                        
                        working.rewrite(leaf[0], newVariableTree);
                    }
                }
            }).commit();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        return null;
    }
    
    public String getText() {
        return NbBundle.getMessage(ChangeTypeFix.class, "MSG_ChangeVariablesType", treeName, type); // NOI18N
    }

}
