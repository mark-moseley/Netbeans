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

package org.netbeans.modules.refactoring.java.plugins;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.DiffElement;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.java.plugins.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/*
 * UseSuperTypeRefactoringPlugin.java
 *
 * Created on June 22, 2005
 *
 * @author Bharath Ravi Kumar
 */
/**
 * The plugin that performs the actual work on
 * behalf of the use super type refactoring
 */
public class UseSuperTypeRefactoringPlugin extends JavaRefactoringPlugin {
    
    private final UseSuperTypeRefactoring refactoring;
    
    /**
     * Creates a new instance of UseSuperTypeRefactoringPlugin
     * @param refactoring The refactoring to be used by this plugin
     */
    public UseSuperTypeRefactoringPlugin(UseSuperTypeRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    /**
     * Prepares the underlying where used query & checks
     * for the visibility of the target type.
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle subClassHandle = refactoring.getTypeElement();
        replaceSubtypeUsages(subClassHandle, refactoringElements);
        return null;
    }
    
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            return JavaSource.forFileObject(refactoring.getTypeElement().getFileObject());
        }
    }
    /**
     *Checks whether the candidate element is a valid Type.
     *@return Problem The problem instance indicating that an invalid element was selected.
     */
    protected org.netbeans.modules.refactoring.api.Problem preCheck(CompilationController info) {
        //        Element subType = refactoring.getTypeElement();
        //        if(!(subType instanceof JavaClass)){
        //            String errMsg = NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class,
        //                    "ERR_UseSuperType_InvalidElement"); // NOI18N
        //            return new Problem(true, errMsg);
        //        }
        return null;
    }
    
    /**
     * @return A problem indicating that no super type was selected.
     */
    public org.netbeans.modules.refactoring.api.Problem fastCheckParameters(CompilationController info) {
        if (refactoring.getTargetSuperType() == null) {
            return new Problem(true, NbBundle.getMessage(UseSuperTypeRefactoringPlugin.class, "ERR_UseSuperTypeNoSuperType"));
        }
        return null;
    }
    
    /**
     * A no op. Returns null
     */
    public org.netbeans.modules.refactoring.api.Problem checkParameters(CompilationController info) {
        return null;
    }
    
    //---------private  methods follow--------
    
    private void replaceSubtypeUsages(final TreePathHandle subClassHandle,
            final RefactoringElementsBag elemsBag){
        JavaSource javaSrc = JavaSource.forFileObject(subClassHandle.getFileObject());
        
        
        try{
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController complController) throws IOException {
                    complController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    FileObject fo = subClassHandle.getFileObject();
                    ClasspathInfo classpathInfo = getClasspathInfo(refactoring);
                    
                    ClassIndex clsIndx = classpathInfo.getClassIndex();
                    TypeElement javaClassElement = (TypeElement) subClassHandle.
                            resolveElement(complController);
                    EnumSet<ClassIndex.SearchKind> typeRefSearch = EnumSet.of(ClassIndex.SearchKind.
                            TYPE_REFERENCES);
                    Set<FileObject> refFileObjSet = clsIndx.getResources(ElementHandle.
                            create(javaClassElement), typeRefSearch,
                            EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    
                    
                    if(! refFileObjSet.isEmpty()){
                        fireProgressListenerStart(AbstractRefactoring.PREPARE,
                                refFileObjSet.size());
                        
                        Collection<ModificationResult> results =
                                processFiles(refFileObjSet,
                                new FindRefTask(subClassHandle,
                                refactoring.getTargetSuperType()));
                        elemsBag.registerTransaction(new RetoucheCommit(results));
                        for (ModificationResult result : results) {
                            for (FileObject fileObj : result.getModifiedFileObjects()) {
                                for (Difference diff : result.getDifferences(fileObj)) {
                                    String old = diff.getOldText();
                                    if (old != null) {
                                        elemsBag.add(refactoring, DiffElement.create(diff, fileObj, result));
                                    }
                                }
                            }
                        }
                    }
                }
            }, false);
        }catch(IOException ioex){
            ioex.printStackTrace();
        }finally{
            fireProgressListenerStop();
        }
        return;
    }
    
    private final class FindRefTask implements CancellableTask<WorkingCopy>{
        
        private final TreePathHandle subClassHandle;
        private final ElementHandle superClassHandle;
        private FindRefTask(TreePathHandle subClassHandle, ElementHandle
                superClassHandle){
            this.subClassHandle = subClassHandle;
            this.superClassHandle = superClassHandle;
        }
        
        public void cancel() {
        }
        
        public void run(WorkingCopy compiler) throws Exception {
            try{
                compiler.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cu = compiler.getCompilationUnit();
                if (cu == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                    return;
                }
                Element subClassElement = subClassHandle.resolveElement(compiler);
                Element superClassElement = superClassHandle.resolve(compiler);
                assert subClassElement != null;
                ReferencesVisitor findRefVisitor = new ReferencesVisitor(compiler,
                        subClassElement, superClassElement);
                findRefVisitor.scan(compiler.getCompilationUnit(), subClassElement);
            }finally{
                fireProgressListenerStep();
            }
        }
    }
    
    private static class ReferencesVisitor extends SearchVisitor{
        private final Element superTypeElement;
        private final Element subTypeElement;
        private ReferencesVisitor(WorkingCopy workingCopy, Element subClassElement,
                Element superClassElement){
            setWorkingCopy(workingCopy);
            this.superTypeElement = superClassElement;
            this.subTypeElement = subClassElement;
        }
        
        @Override
        public Tree visitVariable(VariableTree varTree, Element elementToMatch) {
            Element typeElement = workingCopy.getTrees().getElement(getCurrentPath());
            TreePath treePath = getCurrentPath();
            VariableElement varElement = (VariableElement) workingCopy.
                    getTrees().getElement(treePath);
            TypeMirror varType = varElement.asType();
            if(varType.equals(elementToMatch.asType())){
                if(isReplaceCandidate(varElement)){
                    replaceWithSuperType(varTree, superTypeElement);
                }
            }
            return super.visitVariable(varTree, elementToMatch);
        }
        
        private boolean isReplaceCandidate(VariableElement varElement){
            VarUsageVisitor varUsagesVisitor = new VarUsageVisitor(workingCopy,
                    (TypeElement) superTypeElement);
            varUsagesVisitor.scan(workingCopy.getCompilationUnit(),
                    varElement);
            return varUsagesVisitor.isReplaceCandidate();
        }
        
        private void replaceWithSuperType(VariableTree oldVarTree, Element superClassElement){
            Tree superTypeTree = make.Type(superClassElement.asType());
            ExpressionTree oldInitTree = oldVarTree.getInitializer();
            ModifiersTree oldModifiers = oldVarTree.getModifiers();
            Tree newTree = make.Variable(oldModifiers, oldVarTree.getName(),
                    superTypeTree, oldInitTree);
            workingCopy.rewrite(oldVarTree, newTree);
        }
        
    }
}
