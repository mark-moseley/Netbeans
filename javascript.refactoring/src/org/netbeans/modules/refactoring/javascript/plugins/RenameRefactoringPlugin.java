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
package org.netbeans.modules.refactoring.javascript.plugins;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mozilla.javascript.Token;
import org.netbeans.fpi.gsf.CancellableTask;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.Error;
import org.netbeans.fpi.gsf.Severity;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.ModificationResult.Difference;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.javascript.RetoucheUtils;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.javascript.JsElementCtx;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.javascript.editing.AstPath;
import org.netbeans.modules.javascript.editing.AstPath;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.mozilla.javascript.Node;
import org.netbeans.fpi.gsf.CancellableTask;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.ModificationResult;
import org.netbeans.napi.gsfret.source.ModificationResult.Difference;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.WorkingCopy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javascript.editing.Element;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;
import org.netbeans.modules.javascript.editing.AstElement;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.netbeans.modules.javascript.editing.ParseTreeWalker;
import org.netbeans.modules.javascript.editing.VariableVisitor;
import org.netbeans.modules.refactoring.javascript.DiffElement;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.javascript.RetoucheUtils;
import org.netbeans.modules.refactoring.javascript.JsElementCtx;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

/**
 * The actual Renaming refactoring work for Js. The skeleton (name checks etc.) based
 * on the Java refactoring module by Jan Becicka, Martin Matula, Pavel Flaska and Daniel Prusa.
 * 
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 * @author Tor Norbye
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * @todo Check that the new name doesn't conflict with an existing name
 * @todo Check unknown files!
 * @todo More prechecks
 * @todo When invoking refactoring on a file object, I also rename the file. I should (a) list the
 *   name it's going to change the file to, and (b) definitely "filenamize" it - e.g. for class FooBar the
 *   filename should be foo_bar.
 * @todo If you rename a Model, I should add a corresponding rename_table entry in the migrations...
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
public class RenameRefactoringPlugin extends JsRefactoringPlugin {
    
    private JsElementCtx treePathHandle = null;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    
    private RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public RenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        JsElementCtx tph = rename.getRefactoringSource().lookup(JsElementCtx.class);
        if (tph!=null) {
            treePathHandle = tph;
        } else {
            Source source = RetoucheUtils.getSource(rename.getRefactoringSource().lookup(FileObject.class));
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
                        org.mozilla.javascript.Node root = AstUtilities.getRoot(co);
                        if (root != null) {
                            JsParseResult rpr = AstUtilities.getParseResult(co);
                            if (rpr != null) {
                                AnalysisResult ar = rpr.getStructure();
                                List<? extends AstElement> els = ar.getElements();
                                if (els.size() > 0) {
                                    // TODO - try to find the outermost or most "relevant" module/class in the file?
                                    // In Java, we look for a class with the name corresponding to the file.
                                    // It's not as simple in Js.
                                    AstElement element = els.get(0);
                                    org.mozilla.javascript.Node node = element.getNode();
                                    treePathHandle = new JsElementCtx(root, node, element, co.getFileObject(), co);
                                    refactoring.getContext().add(co);
                                }
                            }
                        }
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected Source getJsSource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case CHECKPARAMETERS:    
                if (treePathHandle==null) {
                    return null;
                }
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return RetoucheUtils.createSource(cpInfo, treePathHandle.getFileObject());
            case FASTCHECKPARAMETERS:
                return RetoucheUtils.getSource(treePathHandle.getFileObject());

        }
        throw new IllegalStateException();
    }
    
    protected Problem preCheck(CompilationController info) throws IOException {
        Problem preCheckProblem = null;
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
//        Element el = treePathHandle.resolveElement(info);
//        preCheckProblem = isElementAvail(treePathHandle, info);
//        if (preCheckProblem != null) {
//            return preCheckProblem;
//        }
//        FileObject file = SourceUtils.getFile(el, info.getClasspathInfo());
//        if (FileUtil.getArchiveFile(file)!= null) { //NOI18N
//            preCheckProblem = createProblem(preCheckProblem, true, getCannotRename(file));
//            return preCheckProblem;
//        }
//        
//        if (!RetoucheUtils.isElementInOpenProject(file)) {
//            preCheckProblem = new Problem(true, NbBundle.getMessage(JsRefactoringPlugin.class, "ERR_ProjectNotOpened"));
//            return preCheckProblem;
//        }
//        
//        switch(treePathHandle.getKind()) {
//        case METHOD:
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            overriddenByMethods = RetoucheUtils.getOverridingMethods(el, info);
//            fireProgressListenerStep();
//            if (!overriddenByMethods.isEmpty()) {
//                String msg = new MessageFormat(getString("ERR_IsOverridden")).format(
//                        new Object[] {SourceUtils.getEnclosingTypeElement(el).getSimpleName().toString()});
//                preCheckProblem = createProblem(preCheckProblem, false, msg);
//            }
//            overridesMethods = RetoucheUtils.getOverridenMethods((ExecutableElement)el, info);
//            fireProgressListenerStep();
//            if (!overridesMethods.isEmpty()) {
//                boolean fatal = false;
//                for (Iterator iter = overridesMethods.iterator();iter.hasNext();) {
//                    ExecutableElement method = (ExecutableElement) iter.next();
//                    if (RetoucheUtils.isFromLibrary(method, info.getClasspathInfo())) {
//                        fatal = true;
//                        break;
//                    }
//                }
//                String msg = fatal?getString("ERR_Overrides_Fatal"):getString("ERR_Overrides");
//                preCheckProblem = createProblem(preCheckProblem, fatal, msg);
//            }
//            break;
//        case FIELD:
//        case ENUM_CONSTANT:
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            Element hiddenField = hides(el, el.getSimpleName().toString(), info);
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            if (hiddenField != null) {
//                String msg = new MessageFormat(getString("ERR_Hides")).format(
//                        new Object[] {SourceUtils.getEnclosingTypeElement(hiddenField)}
//                );
//                preCheckProblem = createProblem(preCheckProblem, false, msg);
//            }
//            break;
//        case PACKAGE:
//            //TODO: any prechecks?
//            break;
//        case LOCAL_VARIABLE:
//            //TODO: any prechecks for formal parametr or local variable?
//            break;
//        case CLASS:
//        case INTERFACE:
//        case ANNOTATION_TYPE:
//        case ENUM:
//            //TODO: any prechecks for JavaClass?
//            break;
//        default:
//            //                if (!((jmiObject instanceof Resource) && ((Resource)jmiObject).getClassifiers().isEmpty()))
//            //                    result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_RenameWrongType"));
//        }
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    private static final String getCannotRename(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, "ERR_CannotRenameFile")).format(new Object[] {r.getNameExt()});
    }
    
    protected Problem fastCheckParameters(CompilationController info) throws IOException {
        Problem fastCheckProblem = null;
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
        ElementKind kind = treePathHandle.getKind();
//        
        String newName = refactoring.getNewName();
//        String oldName = element.getSimpleName().toString();
        String oldName = treePathHandle.getSimpleName();
        
        if (oldName.equals(newName)) {
            boolean nameNotChanged = true;
            //if (kind == ElementKind.CLASS || kind == ElementKind.MODULE) {
            //    if (!((TypeElement) element).getNestingKind().isNested()) {
            //        nameNotChanged = info.getFileObject().getName().equals(element);
            //    }
            //}
            if (nameNotChanged) {
                fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                return fastCheckProblem;
            }
            
        }
        
        // TODO - get a better Js name picker - and check for invalid Js symbol names etc.
        // TODO - call JsUtils.isValidLocalVariableName if we're renaming a local symbol!
        /*if (kind == ElementKind.CLASS && !JsUtils.isValidJsClassName(newName)) {
            String s = getString("ERR_InvalidClassName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else*/ if (kind == ElementKind.METHOD && !JsUtils.isValidJsMethodName(newName)) {
            String s = getString("ERR_InvalidMethodName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else if (!JsUtils.isValidJsIdentifier(newName)) {
            String s = getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        
        
        String msg = JsUtils.getIdentifierWarning(newName, 0);
        if (msg != null) {
            fastCheckProblem = createProblem(fastCheckProblem, false, msg);
        }
        
        // TODO
//        System.out.println("TODO - look for variable clashes etc");
        
        
//        if (kind.isClass() && !((TypeElement) element).getNestingKind().isNested()) {
//            if (doCheckName) {
//                String oldfqn = RetoucheUtils.getQualifiedName(treePathHandle);
//                String newFqn = oldfqn.substring(0, oldfqn.lastIndexOf(oldName));
//                
//                String pkgname = oldfqn;
//                int i = pkgname.indexOf('.');
//                if (i>=0)
//                    pkgname = pkgname.substring(0,i);
//                else
//                    pkgname = "";
//                
//                String fqn = "".equals(pkgname) ? newName : pkgname + '.' + newName;
//                FileObject fo = treePathHandle.getFileObject();
//                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//                if (RetoucheUtils.typeExist(treePathHandle, newFqn)) {
//                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
//                            new Object[] {newName, pkgname}
//                    );
//                    fastCheckProblem = createProblem(fastCheckProblem, true, msg);
//                    return fastCheckProblem;
//                }
//            }
//            FileObject primFile = treePathHandle.getFileObject();
//            FileObject folder = primFile.getParent();
//            FileObject[] children = folder.getChildren();
//            for (int x = 0; x < children.length; x++) {
//                if (children[x] != primFile && !children[x].isVirtual() && children[x].getName().equals(newName) && "java".equals(children[x].getExt())) { //NOI18N
//                    String msg = new MessageFormat(getString("ERR_ClassClash")).format(
//                            new Object[] {newName, folder.getPath()}
//                    );
//                    fastCheckProblem = createProblem(fastCheckProblem, true, msg);
//                    break;
//                }
//            } // for
//        } else if (kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER) {
//            String msg = variableClashes(newName,treePath, info);
//            if (msg != null) {
//                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
//                return fastCheckProblem;
//            }
//        } else {
//            String msg = clashes(element, newName, info);
//            if (msg != null) {
//                fastCheckProblem = createProblem(fastCheckProblem, true, msg);
//                return fastCheckProblem;
//            }
//        }
        return fastCheckProblem;
    }
    
    protected Problem checkParameters(CompilationController info) throws IOException {
        
        Problem checkProblem = null;
        int steps = 0;
        if (overriddenByMethods != null) {
            steps += overriddenByMethods.size();
        }
        if (overridesMethods != null) {
            steps += overridesMethods.size();
        }
        
        fireProgressListenerStart(refactoring.PARAMETERS_CHECK, 8 + 3*steps);
        
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
//        Element element = treePathHandle.resolveElement(info);
        
        fireProgressListenerStep();
        fireProgressListenerStep();
        String msg;
        
        // TODO - check more parameters
        //System.out.println("TODO - need to check parameters for hiding etc.");
        
        
//        if (treePathHandle.getKind() == ElementKind.METHOD) {
//            checkProblem = checkMethodForOverriding((ExecutableElement)element, refactoring.getNewName(), checkProblem, info);
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//        } else if (element.getKind().isField()) {
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            Element hiddenField = hides(element, refactoring.getNewName(), info);
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            fireProgressListenerStep();
//            if (hiddenField != null) {
//                msg = new MessageFormat(getString("ERR_WillHide")).format(
//                        new Object[] {SourceUtils.getEnclosingTypeElement(hiddenField).toString()}
//                );
//                checkProblem = createProblem(checkProblem, false, msg);
//            }
//        }
        fireProgressListenerStop();
        return checkProblem;
    }
    
//        private Problem checkMethodForOverriding(ExecutableElement m, String newName, Problem problem, CompilationInfo info) {
//            ElementUtilities ut = info.getElementUtilities();
//            //problem = willBeOverridden(m, newName, argTypes, problem);
//            fireProgressListenerStep();
//            problem = willOverride(m, newName, problem, info);
//            fireProgressListenerStep();
//            return problem;
//        }
//    
//    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    @Override
    public Problem preCheck() {
        if (!treePathHandle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(RenameRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        }
        
        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        Source source = RetoucheUtils.createSource(cpInfo, treePathHandle.getFileObject());

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
                public void run(CompilationController info) throws Exception {
                    // TODO if getSearchInComments I -should- search all files
//                    System.out.println("TODO - compute a full set of files to be checked... for now just lamely using the project files");
                    if (treePathHandle.getKind() == ElementKind.VARIABLE || treePathHandle.getKind() == ElementKind.PARAMETER) {
                        // For local variables, only look in the current file!
                        set.add(info.getFileObject());
                    }  else {
                        set.addAll(RetoucheUtils.getJsFilesInProject(info.getFileObject()));
                    }
//                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
//                    info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
//                    Element el = treePathHandle.resolveElement(info);
//                    ElementKind kind = el.getKind();
//                    ElementHandle<TypeElement> enclosingType;
//                    if (el instanceof TypeElement) {
//                         enclosingType = ElementHandle.create((TypeElement)el);
//                    } else {
//                         enclosingType = ElementHandle.create(SourceUtils.getEnclosingTypeElement(el));
//                    }
//                    set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
//                    if (kind.isField()) {
//                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                    } else if (el instanceof TypeElement) {
//                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//                    } else if (kind == ElementKind.METHOD) {
//                        //add all references of overriding methods
//                        allMethods = new HashSet();
//                        allMethods.add(ElementHandle.create((ExecutableElement)el));
//                        for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)el, info)) {
//                            addMethods(e, set, info, idx);
//                        }
//                        //add all references of overriden methods
//                        for (ExecutableElement ov: RetoucheUtils.getOverridenMethods((ExecutableElement)el, info)) {
//                            addMethods(ov, set, info, idx);
//                            for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)ov, info)) {
//                                addMethods(e, set, info, idx);
//                            }
//                        }
//                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
//                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
//    private void addMethods(ExecutableElement e, Set set, CompilationInfo info, ClassIndex idx) {
//        set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
//        ElementHandle<TypeElement> encl = ElementHandle.create(SourceUtils.getEnclosingTypeElement(e));
//        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
//        allMethods.add(ElementHandle.create(e));
//    }
    
    private Set<JsElementCtx> allMethods;
    
    public Problem prepare(RefactoringElementsBag elements) {
        if (treePathHandle == null) {
            return null;
        }
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            TransformTask transform = new TransformTask(new RenameTransformer(refactoring.getNewName(), allMethods), treePathHandle);
            final Collection<ModificationResult> results = processFiles(a, transform);
            elements.registerTransaction(new RetoucheCommit(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference diff: result.getDifferences(jfo)) {
                        String old = diff.getOldText();
                        if (old!=null) {
                            //TODO: workaround
                            //generator issue?
                            elements.add(refactoring,DiffElement.create(diff, jfo, result));
                        }
                    }
                }
            }
        }
        fireProgressListenerStop();
        return null;
    }

//    private static int getAccessLevel(Element e) {
//        Set<Modifier> access = e.getModifiers();
//        if (access.contains(Modifier.PUBLIC)) {
//            return 3;
//        } else if (access.contains(Modifier.PROTECTED)) {
//            return 2;
//        } else if (!access.contains(Modifier.PRIVATE)) {
//            return 1;
//        } else {
//            return 0;
//        }
//    }
//    
//    private Problem willOverride(ExecutableElement method, String name, Problem problem, CompilationInfo info) {
//        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
//        TypeElement jc = (TypeElement) method.getEnclosingElement();
//        LinkedList supertypes = new LinkedList();
//        
//        ElementUtilities ut = info.getElementUtilities();
//        //TODO:
//        //ExecutableElement m = ut.getOverriddenMethod(method, name);
//        ExecutableElement m = null;
//        if (m!=null) {
//            if (m.getModifiers().contains(Modifier.FINAL)) {
//                String msg = new MessageFormat(getString("ERR_WillOverride_final")).format(
//                        new Object[] {
//                    method.getSimpleName(),
//                    method.getEnclosingElement().getSimpleName(),
//                    m.getSimpleName(),
//                    m.getEnclosingElement().getSimpleName()
//                }
//                );
//                return createProblem(problem, true, msg);
//            } else if (getAccessLevel(m) > getAccessLevel(method)) {
//                String msg = new MessageFormat(getString("ERR_WillOverride_access")).format(
//                        new Object[] {
//                    method.getSimpleName(),
//                    method.getEnclosingElement().getSimpleName(),
//                    m.getSimpleName(),
//                    m.getEnclosingElement().getSimpleName()
//                }
//                );
//                return createProblem(problem, true, msg);
//            } else if (m.getModifiers().contains(Modifier.STATIC)!= method.getModifiers().contains(Modifier.STATIC)) {
//                String msg = new MessageFormat(getString("ERR_WillOverride_static")).format(
//                        new Object[] {
//                    isStatic ? getString("LBL_static") : getString("LBL_instance"),
//                    method.getSimpleName(),
//                    method.getEnclosingElement().getSimpleName(),
//                    m.getModifiers().contains(Modifier.STATIC) ? getString("LBL_static") : getString("LBL_instance"),
//                    m.getSimpleName(),
//                    m.getEnclosingElement().getSimpleName()
//                }
//                );
//                return createProblem(problem, true, msg);
//            } else {
//                String msg = new MessageFormat(getString("ERR_WillOverride")).format(
//                        new Object[] {
//                    method.getSimpleName(),
//                    method.getEnclosingElement().getSimpleName(),
//                    m.getSimpleName(),
//                    m.getEnclosingElement().getSimpleName()
//                }
//                );
//                return createProblem(problem, false, msg);
//            }
//        } else {
//            return problem;
//        }
//    }
//    private Element hides(Element field, String name, CompilationInfo info) {
//        TypeElement jc = SourceUtils.getEnclosingTypeElement(field);
//        Elements elements = info.getElements();
//        ElementUtilities utils = info.getElementUtilities();
//        for (Element el:elements.getAllMembers(jc)) {
////TODO:
////            if (utils.willHide(el, field, name)) {
////                return el;
////            }
//            if (el.getKind().isField()) {
//                if (el.getSimpleName().toString().equals(name)) {
//                    if (!el.getEnclosingElement().equals(field.getEnclosingElement())) {
//                        return el;
//                    }
//                }
//            }
//        };
//        return null;
//    }
//    
//    private String variableClashes(String newName, TreePath tp, CompilationInfo info) {
//        LocalVarScanner lookup = new LocalVarScanner(info, newName);
//        TreePath scopeBlok = tp;
//        EnumSet set = EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.FOR_LOOP, Tree.Kind.METHOD);
//        while (!set.contains(scopeBlok.getLeaf().getKind())) {
//            scopeBlok = scopeBlok.getParentPath();
//        }
//        Element var = info.getTrees().getElement(tp);
//        lookup.scan(scopeBlok, var);
//
//        if (lookup.result)
//            return new MessageFormat(getString("ERR_LocVariableClash")).format(
//                new Object[] {newName}
//            );
//        
//        TreePath temp = tp;
//        while (temp.getLeaf().getKind() != Tree.Kind.METHOD) {
//            Scope scope = info.getTrees().getScope(temp);
//            for (Element el:scope.getLocalElements()) {
//                if (el.getSimpleName().toString().equals(newName)) {
//                    return new MessageFormat(getString("ERR_LocVariableClash")).format(
//                            new Object[] {newName}
//                    );
//                }
//            }
//            temp = temp.getParentPath();
//        }
//        return null;
//    }
//    
//    private String clashes(Element feature, String newName, CompilationInfo info) {
//        ElementUtilities utils = info.getElementUtilities();
//        Element dc = feature.getEnclosingElement();
//        ElementKind kind = feature.getKind();
//        if (kind.isClass() || kind.isInterface()) {
//            for (Element current:ElementFilter.typesIn(dc.getEnclosedElements())) {
//                if (current.getSimpleName().toString().equals(newName)) {
//                    return new MessageFormat(getString("ERR_InnerClassClash")).format(
//                            new Object[] {newName, dc.getSimpleName()}
//                    );
//                }
//            }
//        } else if (kind==ElementKind.METHOD) {
//            if (utils.alreadyDefinedIn((CharSequence) newName, (ExecutableType) feature.asType(), (TypeElement) dc)) {
//                return new MessageFormat(getString("ERR_MethodClash")).format(
//                        new Object[] {newName, dc.getSimpleName()}
//                );
//            }
//        } else if (kind.isField()) {
//            for (Element current:ElementFilter.fieldsIn(dc.getEnclosedElements())) {
//                if (current.getSimpleName().toString().equals(newName)) {
//                    return new MessageFormat(getString("ERR_FieldClash")).format(
//                            new Object[] {newName, dc.getSimpleName()}
//                    );
//                }
//            }
//        }
//        return null;
//    }
    
    
    private static final String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringPlugin.class, key);
    }
    
    /**
     *
     * @author Jan Becicka
     */
    public class RenameTransformer extends SearchVisitor {

        private Set<JsElementCtx> allMethods;
        private String newName;
        private String oldName;
        private CloneableEditorSupport ces;
        private List<Difference> diffs;

        @Override
        public void setWorkingCopy(WorkingCopy workingCopy) {
            // Cached per working copy
            this.ces = null;
            assert diffs == null; // Should have been committed already
            super.setWorkingCopy(workingCopy);
        }
        
        public RenameTransformer(String newName, Set<JsElementCtx> am) {
            this.newName = newName;
            this.oldName = treePathHandle.getSimpleName();
            this.allMethods = am;
        }
        
        @Override
        public void scan() {
            // TODO - do I need to force state to resolved?
            //compiler.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

            diffs = new ArrayList<Difference>();
            JsElementCtx searchCtx = treePathHandle;
            Error error = null;
            Node root = AstUtilities.getRoot(workingCopy);
            if (root != null) {
                
                Element element = AstElement.getElement(workingCopy, root);
                Node node = searchCtx.getNode();
                
                JsElementCtx fileCtx = new JsElementCtx(root, node, element, workingCopy.getFileObject(), workingCopy);
                
                Node scopeNode = null;
                if (node.getType() == org.mozilla.javascript.Token.NAME ||
                    node.getType() == org.mozilla.javascript.Token.BINDNAME ||
                    node.getType() == org.mozilla.javascript.Token.PARAMETER) {

                    
                    // TODO - map this node to our new tree.
                    // In the mean time, just search in the old seach tree.
                    Node searchRoot = node;
                    while (searchRoot.getParentNode() != null) {
                        searchRoot = searchRoot.getParentNode();
                    }

                    VariableVisitor v = new VariableVisitor();
                    new ParseTreeWalker(v).walk(searchRoot);
                    scopeNode = v.getDefiningScope(node);
                }
                

                if (scopeNode != null) {
                    findLocal(searchCtx, fileCtx, scopeNode, oldName);
                } else {
                    // Full AST search
                    AstPath path = new AstPath();
                    path.descend(root);
                    find(path, searchCtx, fileCtx, root, oldName);
                    path.ascend();
                }
            } else {
                //System.out.println("Skipping file " + workingCopy.getFileObject());
                // See if the document contains references to this symbol and if so, put a warning in
                if (workingCopy.getText().indexOf(oldName) != -1) {
                    // TODO - icon??
                    if (ces == null) {
                        ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
                    }
                    int start = 0;
                    int end = 0;
                    String desc = NbBundle.getMessage(RenameRefactoringPlugin.class, "ParseErrorFile", oldName);
                    List<Error> errors = workingCopy.getErrors();
                    if (errors.size() > 0) {
                        for (Error e : errors) {
                            if (e.getSeverity() == Severity.ERROR) {
                                error = e;
                                break;
                            }
                        }
                        if (error == null) {
                            error = errors.get(0);
                        }
                        
                        String errorMsg = error.getDisplayName();
                        
                        if (errorMsg.length() > 80) {
                            errorMsg = errorMsg.substring(0, 77) + "..."; // NOI18N
                        }

                        desc = desc + "; " + errorMsg;
                        start = error.getStartPosition();
                        start = LexUtilities.getLexerOffset(workingCopy, start);
                        if (start == -1) {
                            start = 0;
                        }
                        end = start;
                    }
                    PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                    PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                    Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, "", "", desc); // NOI18N
                    diffs.add(diff);
                }
            }

            if (error == null && refactoring.isSearchInComments()) {
                Document doc = RetoucheUtils.getDocument(workingCopy, workingCopy.getFileObject());
                if (doc != null) {
                    //force open
                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = th.tokenSequence();

                    ts.move(0);

                    searchTokenSequence(ts);
                }
            }

            // Sort the diffs, if applicable
            if (diffs.size() > 0) {
                Collections.sort(diffs, new Comparator<Difference>() {
                    public int compare(Difference d1, Difference d2) {
                        return d1.getStartPosition().getOffset() - d2.getStartPosition().getOffset();
                    }
                });
                for (Difference diff : diffs) {
                    workingCopy.addDiff(diff);
                }
            }
            diffs = null;
            ces = null;
            
        }
        
        private void searchTokenSequence(TokenSequence<?> ts) {
            if (ts.moveNext()) {
                do {
                    org.netbeans.api.lexer.Token<?> token = ts.token();
                    TokenId id = token.id();

                    String primaryCategory = id.primaryCategory();
                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
                        // search this comment
                        String text = token.text().toString();
                        int index = text.indexOf(oldName);
                        if (index != -1) {
                            // TODO make sure it's its own word. Technically I could
                            // look at identifier chars like "_" here but since they are
                            // used for other purposes in comments, consider letters
                            // and numbers as enough
                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index-1))) &&
                                    (index+oldName.length() >= text.length() || 
                                    !Character.isLetterOrDigit(text.charAt(index+oldName.length())))) {
                                int start = ts.offset() + index;
                                int end = start + oldName.length();
                                if (ces == null) {
                                    ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
                                }
                                PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                                PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                                String desc = getString("ChangeComment");
                                Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldName, newName, desc);
                                diffs.add(diff);
                            }
                        }
                    } else {
                        TokenSequence<?> embedded = ts.embedded();
                        if (embedded != null) {
                            searchTokenSequence(embedded);
                        }                                    
                    }
                } while (ts.moveNext());
            }
        }

        @SuppressWarnings("fallthrough")
        private void rename(Node node, String oldCode, String newCode, String desc) {
            OffsetRange range = AstUtilities.getNameRange(node);
            assert range != OffsetRange.NONE;
            int pos = range.getStart();

            if (desc == null) {
                // TODO - insert "method call", "method definition", "class definition", "symbol", "attribute" etc. and from and too?
                switch (node.getType()) {
                case Token.OBJLITNAME: {
                    if (!AstUtilities.isLabelledFunction(node)) {
                        desc = NbBundle.getMessage(RenameRefactoringPlugin.class, "UpdateRef", oldCode);
                        break;
                    } else {
                        // Fall through
                    }
                }
                case Token.FUNCNAME:
                case Token.FUNCTION:
                    desc = getString("UpdateMethodDef");
                    break;
                case Token.CALL:
                    desc = getString("UpdateCall");
                    break;
                case Token.NAME:
                    if (node.getParentNode() != null && node.getParentNode().getType() == Token.CALL) {
                        // Ignore 
                        desc = getString("UpdateCall");
                        break;
                    }
                    // Fallthrough
                case Token.BINDNAME:
                    desc = getString("UpdateLocalvar");
                    break;
                case Token.PARAMETER:
                    desc = getString("UpdateParameter");
                    break;
//                case Token.GLOBAL:
//                    desc = getString("UpdateGlobal");
//                case Token.PROPERTY:
//                    desc = getString("UpdateProperty");
                default:
                    desc = NbBundle.getMessage(RenameRefactoringPlugin.class, "UpdateRef", oldCode);
                    break;
                }
            }

            if (ces == null) {
                ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
            }
            
            // Convert from AST to lexer offsets if necessary
            pos = LexUtilities.getLexerOffset(workingCopy, pos);
            if (pos == -1) {
                // Translation failed
                return;
            }
            
            int start = pos;
            int end = pos+oldCode.length();
            // TODO if a SymbolNode, +=1 since the symbolnode includes the ":"
            try {
                BaseDocument doc = (BaseDocument)ces.openDocument();

                if (start > doc.getLength()) {
                    start = end = doc.getLength();
                }

                if (end > doc.getLength()) {
                    end = doc.getLength();
                }

                // Look in the document and search around a bit to detect the exact method reference
                // (and adjust position accordingly). Thus, if I have off by one errors in the AST (which
                // occasionally happens) the user's source won't get munged
                if (!oldCode.equals(doc.getText(start, end-start))) {
                    // Look back and forwards by 1 at first
                    int lineStart = Utilities.getRowFirstNonWhite(doc, start);
                    int lineEnd = Utilities.getRowLastNonWhite(doc, start)+1; // +1: after last char
                    if (lineStart == -1 || lineEnd == -1) { // We're really on the wrong line!
                        System.out.println("Empty line entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + start + " referenced by node " + 
                                node + " of type " + node.getClass().getName());
                        return;
                    }

                    if (lineStart < 0 || lineEnd-lineStart < 0) {
                        return; // Can't process this one
                    }

                    String line = doc.getText(lineStart, lineEnd-lineStart);
                    if (line.indexOf(oldCode) == -1) {
                        System.out.println("Skipping entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + line + " referenced by node " + 
                                node + " of type " + node.getClass().getName());
                    } else {
                        int lineOffset = start-lineStart;
                        int newOffset = -1;
                        // Search up and down by one
                        for (int distance = 1; distance < line.length(); distance++) {
                            // Ahead first
                            if (lineOffset+distance+oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset+distance, lineOffset+distance+oldCode.length()))) {
                                newOffset = lineOffset+distance;
                                break;
                            }
                            if (lineOffset-distance >= 0 && lineOffset-distance+oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset-distance, lineOffset-distance+oldCode.length()))) {
                                newOffset = lineOffset-distance;
                                break;
                            }
                        }

                        if (newOffset != -1) {
                            start = newOffset+lineStart;
                            end = start+oldCode.length();
                        }
                    }
                }
            } catch (IOException ie) {
                Exceptions.printStackTrace(ie);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
            
            if (newCode == null) {
                // Usually it's the new name so allow client code to refer to it as just null
                newCode = refactoring.getNewName(); // XXX isn't this == our field "newName"?
            }

            PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
            PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
            Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldCode, newCode, desc);
            diffs.add(diff);
        }
    
        /** Search for local variables in local scope */
        @SuppressWarnings("fallthrough")
        private void findLocal(JsElementCtx searchCtx, JsElementCtx fileCtx, Node node, String name) {
            switch (node.getType()) {
            case Token.PARAMETER:
                if (node.getString().equals(name)) {
                    rename(node, name, null, getString("RenameParam"));
                }
                break;
            case Token.NAME:
                if (node.getParentNode() != null && node.getParentNode().getType() == Token.CALL) {
                    // Ignore calls
                    break;
                }
                // Fallthrough
            case Token.BINDNAME:
                if (node.getString().equals(name)) {
                    rename(node, name, null, getString("UpdateLocalvar"));
                }
            }

            if (node.hasChildren()) {
                Node child = node.getFirstChild();

                for (; child != null; child = child.getNext()) {
                    findLocal(searchCtx, fileCtx, child, name);
                }
            }
        }
        
        /**
         * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
         *   - I've gotta filter fields, methods etc. that are not in the current class
         *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
         * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
         */
        @SuppressWarnings("fallthrough")
        private void find(AstPath path, JsElementCtx searchCtx, JsElementCtx fileCtx, Node node, String name) {
            switch (node.getType()) {
            case org.mozilla.javascript.Token.OBJLITNAME: {
                if (node.getString().equals(name) && AstUtilities.isLabelledFunction(node)) {
                    // TODO - implement skip semantics here, as is done for functions!
                    // AstUtilities.getLabelledFunction(node);
                    rename(node, name, null, getString("UpdateMethodDef"));
                }
                
                // No children to consider
                return;
            }
            
            case org.mozilla.javascript.Token.FUNCNAME: {
                if (node.getString().equals(name)) {
                    boolean skip = false;
//
//                        // Check that we're in a class or module we're interested in
//                        String fqn = AstUtilities.getFqnName(path);
//                        if (fqn == null || fqn.length() == 0) {
//                            fqn = JsIndex.OBJECT;
//                        }
//                        
//                        if (!fqn.equals(searchCtx.getDefClass())) {
//                            // XXX THE ABOVE IS NOT RIGHT - I shouldn't
//                            // use equals on the class names, I should use the
//                            // index and see if one derives fromor includes the other
//                            skip = true;
//                        }
//
//                        // Check arity
//                        if (!skip && AstUtilities.isCall(searchCtx.getNode())) {
//                            // The reference is a call and this is a definition; see if
//                            // this looks like a match
//                            // TODO - enforce that this method is also in the desired
//                            // target class!!!
//                            if (!AstUtilities.isCallFor(searchCtx.getNode(), searchCtx.getArity(), node)) {
//                                skip = true;
//                            }
//                        } else {
//                            // The search handle is a method def, as is this, with the same name.
//                            // Now I need to go and see if this is an override (e.g. compatible
//                            // arglist...)
//                            // XXX TODO
//                        }

                    if (!skip) {
                        // Found a method match
                        // TODO - check arity - see OccurrencesFinder
                        //node = AstUtilities.getDefNameNode((MethodDefNode)node);
                        rename(node, name, null, getString("UpdateMethodDef"));
                    }
                }
                break;
            }
            case org.mozilla.javascript.Token.CALL: {
                String s = AstUtilities.getCallName(node, false);
                if (s.equals(name)) {
                     // TODO - if it's a call without a lhs (e.g. Call.LOCAL),
                     // make sure that we're referring to the same method call
                    // Found a method call match
                    // TODO - make a node on the same line
                    // TODO - check arity - see OccurrencesFinder
                    rename(node, name, null, null);
                 }
                 break;
            }
            case org.mozilla.javascript.Token.NAME:
                if (node.getParentNode().getType() == org.mozilla.javascript.Token.CALL) {
                    // Skip - call name is already handled as part of parent
                    break;
                }
                // Fallthrough
            case org.mozilla.javascript.Token.STRING: {
                int parentType = node.getParentNode().getType();
                if (!(parentType == org.mozilla.javascript.Token.GETPROP ||
                        parentType == org.mozilla.javascript.Token.SETPROP)) {
                    break;
                }
                // Fallthrough
            }
            case org.mozilla.javascript.Token.BINDNAME: {
                // Global vars
                if (node.getString().equals(name)) {
                    rename(node, name, null, null);
                }
                break;
            }
            }
            
            if (node.hasChildren()) {
                Node child = node.getFirstChild();

                for (; child != null; child = child.getNext()) {
                    find(path, searchCtx, fileCtx, child, name);
                }
            }
        }
    }
}
