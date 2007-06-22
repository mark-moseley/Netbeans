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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class AssignmentToItself extends AbstractHint {

    
    private Set<Kind> KINDS = Collections.<Tree.Kind>singleton(Tree.Kind.ASSIGNMENT);
    
    public AssignmentToItself() {
        super( false, true, HintSeverity.WARNING );
    }

    public Set<Kind> getTreeKinds() {
        return KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        
        Tree node = treePath.getLeaf();

        if ( node.getKind() != Tree.Kind.ASSIGNMENT ) {
            return null;
        }
        
        AssignmentTree tree = (AssignmentTree)node;
        
        TreePath tpVar = new TreePath( treePath, tree.getVariable() );
        TreePath tpExp = new TreePath( treePath, tree.getExpression() );
        
        Element eVar = info.getTrees().getElement(tpVar);
        Element eExp = info.getTrees().getElement(tpExp);
        
        if ( eVar != null && eExp != null && eVar.equals( eExp ) ) {
            
            List<Fix> fixes = new ArrayList<Fix>();
            
            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(), 
                        getDisplayName(), 
                        fixes, 
                        info.getFileObject(),
                        (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree ),
                        (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree ) );
                    
            return Collections.<ErrorDescription>singletonList(ed);            
        }
        
        return null;
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "AssignmentToItself"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(AssignmentToItself.class, "DSC_ATI"); // NOI18N
    }

    private static class ATIFix implements Fix, Task<WorkingCopy> {

        private static final int REMOVE = 0;
        private static final int QUALIFY = 1;
        private static final int NEW_PARAMETER = 2;
        private static final int NEW_FIELD = 3;
        
        private int kind;
        private TreePath treePath;
        private FileObject file;

        public ATIFix(int kind, TreePath treePath, FileObject file) {
            this.kind = kind;
            this.treePath = treePath;
            this.file = file;
        }
        
        public String getText() {
            
            switch( kind ) {
                case REMOVE:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Remove_FIX"); // NOI18N
                case QUALIFY:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Qualify_FIX"); // NOI18N
                case NEW_PARAMETER:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewParameter_FIX"); // NOI18N
                case NEW_FIELD:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewField_FIX"); // NOI18N
            } 
            
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(file);
            try {
                js.runModificationTask(this).commit();
            }
            catch( IOException e ) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            /*
            workingCopy.toPhase(Phase.RESOLVED);
            TreeMaker treeMaker = workingCopy.getTreeMaker();

            TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            AssignmentTree assignmentTree = (AssignmentTree)getEnclosingTreeOfKind(treePath, Tree.Kind.ASSIGNMENT);

            TreePath tpVar = new TreePath( treePath, assignmentTree.getVariable() );
            Element eVar = workingCopy.getTrees().getElement(tpVar);
            VariableTree vt = (VariableTree) workingCopy.getTrees().getTree(eVar); // XXX test iof


            VariableElement var = (VariableElement)eVar; // XXX test iof 

            MethodTree methodTree = (MethodTree)getEnclosingTreeOfKind(treePath, Tree.Kind.METHOD);

            MethodTree newMethod = treeMaker.addMethodParameter(methodTree, treeMaker.Variable(
                                    treeMaker.Modifiers(
                                        Collections.<Modifier>emptySet(),
                                        Collections.<AnnotationTree>emptyList()
                                    ),
                                    eVar.getSimpleName().toString(),
                                    vt.getType(),
                                    null
                                ));

            workingCopy.rewrite(methodTree, newMethod);
        
        */
        }
    }
}
