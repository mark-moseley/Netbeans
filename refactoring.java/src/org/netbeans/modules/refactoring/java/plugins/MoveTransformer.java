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

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class MoveTransformer extends RefactoringVisitor {

    private FileObject originalFolder;
    private MoveRefactoringPlugin move;
    private Set<Element> elementsToImport = new HashSet();
    private boolean isThisFileMoving;
    private boolean isThisFileReferencingOldPackage = false;
    private Set<Element> elementsAlreadyImported = new HashSet();
    private Problem problem;
    private boolean moveToDefaulPackageProblem = false;

    public Problem getProblem() {
        return problem;
    }

    public MoveTransformer(MoveRefactoringPlugin move) {
        this.move = move;
    }
    
    @Override
    public void setWorkingCopy(WorkingCopy copy) throws ToPhaseException {
        super.setWorkingCopy(copy);
        originalFolder = workingCopy.getFileObject().getParent();
        isThisFileMoving = move.filesToMove.contains(workingCopy.getFileObject());
        elementsToImport = new HashSet();
        isThisFileReferencingOldPackage = false;
        elementsAlreadyImported = new HashSet();
    }
    
    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                FileObject fo = SourceUtils.getFile(el, workingCopy.getClasspathInfo());
                if (isElementMoving(el)) {
                    elementsAlreadyImported.add(el);
                    String newPackageName = move.getTargetPackageName(SourceUtils.getFile(el, workingCopy.getClasspathInfo()));
                    String targetPackageName = workingCopy.getCompilationUnit().getPackageName().toString();
                    
                    if (!"".equals(newPackageName)) {
                        if (targetPackageName.equals(newPackageName)) { //remove newly created import from same package
                            List<? extends ImportTree> imports = new ArrayList<ImportTree>(workingCopy.getCompilationUnit().getImports());
                            ImportTree toRemove = null;
                            for (ImportTree importTree : imports) {
                                if (importTree.getQualifiedIdentifier().equals(node)) {
                                    toRemove = importTree;
                                }
                            }
                            imports.remove(toRemove);
                            Tree nju = make.CompilationUnit(workingCopy.getCompilationUnit().getPackageName(), imports, workingCopy.getCompilationUnit().getTypeDecls(), workingCopy.getCompilationUnit().getSourceFile());
                            rewrite(workingCopy.getCompilationUnit(), nju);
                        } else {
                            Tree nju = make.MemberSelect(make.Identifier(newPackageName), el);
                            rewrite(node, nju);
                        }
                    } else {
                        if (!moveToDefaulPackageProblem) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage"));
                            moveToDefaulPackageProblem = true;
                        }
                    }
                }
                if (isThisFileMoving && !isElementMoving(el)) {
                    if (el.getKind()!=ElementKind.PACKAGE && 
                        !move.filesToMove.contains(fo) &&
                        getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent())) && !(el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.PROTECTED))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2",workingCopy.getFileObject().getName(),el, getTypeElement(el).getSimpleName()));
                        }
                }
                if (!isThisFileMoving && !isElementMoving(el)) {
                    if (el.getKind()!=ElementKind.PACKAGE && 
                        move.filesToMove.contains(fo) &&
                        getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent())) && !(el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.PROTECTED))) {
                            problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature",workingCopy.getFileObject().getName(),el, getTypeElement(el).getSimpleName()));
                        }
                }
            } else if (isPackageRename() && "*".equals(node.getIdentifier().toString())) { // NOI18N
                ExpressionTree exprTree = node.getExpression();
                TreePath exprPath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), exprTree);
                Element elem = workingCopy.getTrees().getElement(exprPath);
                if (elem != null && elem.getKind() == ElementKind.PACKAGE) {
                    String newPackageName = move.getTargetPackageName(SourceUtils.getFile(elem, workingCopy.getClasspathInfo()));
                    if (!newPackageName.equals(elem.toString())) {
                        Tree nju = make.MemberSelect(make.Identifier(newPackageName), "*"); // NOI18N
                        rewrite(node, nju);
                    }
                }
            }
        }
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null) {
                FileObject fo = SourceUtils.getFile(el, workingCopy.getClasspathInfo());
                if (!isThisFileMoving) {
                    if (isElementMoving(el)) {
                        if (!elementsAlreadyImported.contains(el)) {
                            if (!RetoucheUtils.getPackageName(workingCopy.getCompilationUnit()).equals(move.getTargetPackageName(fo)))
                                elementsToImport.add(el);
                        }
                    } else if (el.getKind()!=ElementKind.PACKAGE && 
                            move.filesToMove.contains(fo) &&
                            getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent())) && !(el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.PROTECTED))) {
                                problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature",workingCopy.getFileObject().getName(), el, getTypeElement(el).getSimpleName()));
                            }
                } else {
                    if (!isThisFileReferencingOldPackage && (!isElementMoving(el) && isTopLevelClass(el)) && getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent()))) {
                        isThisFileReferencingOldPackage = true;
                    }
                    if (el.getKind()!=ElementKind.PACKAGE &&
                            (!isElementMoving(el) && 
                            !move.filesToMove.contains(fo) &&
                            getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent()))) && 
                            !(el.getModifiers().contains(Modifier.PUBLIC) || el.getModifiers().contains(Modifier.PROTECTED))) {
                                problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_AccessesPackagePrivateFeature2",workingCopy.getFileObject().getName(),el, getTypeElement(el).getSimpleName()));
                    }
                }
            }
        }
        
        return super.visitIdentifier(node, p);
    }
    
    private TypeElement getTypeElement(Element e) {
        TypeElement t = workingCopy.getElementUtilities().enclosingTypeElement(e);
        if (t==null && e instanceof TypeElement) {
            return (TypeElement) e;
        }
        return t;
    }
    static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        }
        problem.setNext(result);
        return problem;
    }
    
    
    private PackageElement getPackageOf(Element el) {
        //return workingCopy.getElements().getPackageOf(el);
        while (el.getKind() != ElementKind.PACKAGE) 
            el = el.getEnclosingElement();
        return (PackageElement) el;
    }

    private boolean isPackageRename() {
        return move.refactoring instanceof RenameRefactoring;
    }
    
    private boolean isThisFileReferencedbyOldPackage() {
        Set<FileObject> references = new HashSet(move.whoReferences.get(workingCopy.getFileObject()));
        references.removeAll(move.filesToMove);
        for (FileObject file:references) {
            if (file.getParent().equals(originalFolder))
                return true;
        }
        return false;
    }
    
//    private boolean isThisFileReferencingOldPackage() {
//        //TODO: correctly implement
//        return true;
//    }
    
    private boolean isElementMoving(Element el) {
        for (ElementHandle handle:move.classes.values()) {
            if (handle.signatureEquals(el)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTopLevelClass(Element el) {
        return (el.getKind().isClass() || 
                el.getKind().isInterface()) &&
                el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        Tree result = super.visitCompilationUnit(node, p);
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return result;
        }
        CompilationUnitTree cut = node;
        if (isThisFileMoving) {
            // change package statement if old and new package exist, i.e.
            // neither old nor new package is default
            String newPckg = move.getTargetPackageName(workingCopy.getFileObject());
            if (node.getPackageName() != null && !"".equals(newPckg)) {
                rewrite(node.getPackageName(), make.Identifier(move.getTargetPackageName(workingCopy.getFileObject())));
            } else {
                // in order to handle default package, we have to rewrite whole
                // compilation unit:
                cut = make.CompilationUnit(
                        "".equals(newPckg) ? null : make.Identifier(newPckg),
                        node.getImports(),
                        node.getTypeDecls(),
                        node.getSourceFile()
                );
            }
            if (isThisFileReferencingOldPackage) {
                //add import to old package
                ExpressionTree newPackageName = cut.getPackageName();
                if (newPackageName != null) {
                    cut = insertImport(cut, newPackageName.toString() + ".*", null); // NOI18N
                } else {
                    if (!moveToDefaulPackageProblem) {
                        problem = createProblem(problem, false, NbBundle.getMessage(MoveTransformer.class, "ERR_MovingClassToDefaultPackage"));
                        moveToDefaulPackageProblem = true;
                    }
                }
                      
            }
        }
        for (Element el:elementsToImport) {
            FileObject fo = SourceUtils.getFile(el, workingCopy.getClasspathInfo());
            String newPackageName = move.getTargetPackageName(fo);
            if (!"".equals(newPackageName)) {
                cut = insertImport(cut, newPackageName + "." +el.getSimpleName(), el); // NOI18N
            }
        }
        rewrite(node, cut);
        return result;
    }

    private CompilationUnitTree insertImport(CompilationUnitTree node, String imp, Element orig) {
        for (ImportTree tree: node.getImports()) {
            if (tree.getQualifiedIdentifier().toString().equals(imp)) 
                return node;
            if (orig!=null) {
                if (tree.getQualifiedIdentifier().toString().equals(getPackageOf(orig).getQualifiedName()+".*") && isPackageRename()) { // NOI18N
                    FileObject fo = SourceUtils.getFile(orig, workingCopy.getClasspathInfo());
                    rewrite(tree.getQualifiedIdentifier(), make.Identifier(move.getTargetPackageName(fo)+".*")); // NOI18N
                    return node;
                }
            }
        }
        CompilationUnitTree nju = make.insertCompUnitImport(node, 0, make.Import(make.Identifier(imp), false));
        return nju;
    }
    
}
