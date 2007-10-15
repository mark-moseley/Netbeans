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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.rename.InstantRenamePerformer;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInner extends AbstractHint {
    
    public ConvertAnonymousToInner() {
        super(true, true, HintSeverity.CURRENT_LINE_WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.NEW_CLASS);
    }

    static Fix computeFix(CompilationInfo info, TreePath path, int pos) {
        if (path.getLeaf().getKind() != Kind.NEW_CLASS)
            return null;
        
        NewClassTree nct = (NewClassTree) path.getLeaf();
        
        if (nct.getClassBody() == null) {
            return null;
        }
        
        if (pos != (-1)) {
            long start = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), nct.getClassBody());
            
            if (pos > start) {
                return null;
            }
        }
        
        return new FixImpl(TreePathHandle.create(path, info), info.getJavaSource(), info.getFileObject());
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(compilationInfo.getFileObject());
        Fix f = computeFix(compilationInfo, treePath, pos);
        
        if (f == null)
            return null;
        
        List<Fix> fixes = Collections.<Fix>singletonList(f);
        String hintDescription = NbBundle.getMessage(ConvertAnonymousToInner.class, "HINT_ConvertAnonymousToInner");
        
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, hintDescription, fixes, compilationInfo.getFileObject(), pos, pos));
    }
    
    public String getId() {
        return ConvertAnonymousToInner.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertAnonymousToInner.class, "DN_ConvertAnonymousToInner");
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertAnonymousToInner.class, "DESC_ConvertAnonymousToInner");
    }
    
    private static class FixImpl implements Fix, Task<WorkingCopy> {
        
        private TreePathHandle tph;
        private JavaSource js;
        private FileObject file;
        private Position instantRenamePosition;
        
        public FixImpl(TreePathHandle tph, JavaSource js, FileObject file) {
            this.tph = tph;
            this.js = js;
            this.file = file;
        }

        public String getText() {
            return NbBundle.getMessage(ConvertAnonymousToInner.class, "FIX_ConvertAnonymousToInner");
        }

        public ChangeInfo implement() throws IOException {
            js.runModificationTask(this).commit();
            
            if (instantRenamePosition != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            EditorCookie cook = DataObject.find(file).getLookup().lookup(EditorCookie.class);
                            JEditorPane[] arr = cook.getOpenedPanes();
                            if (arr == null) {
                                return;
                            }
                            arr[0].setCaretPosition(instantRenamePosition.getOffset());
                            InstantRenamePerformer.invokeInstantRename(arr[0]);
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            
            return null;
        }

        public void run(WorkingCopy parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);
            TreePath tp = tph.resolve(parameter);
            
            convertAnonymousToInner(parameter, tp);
            
            final int instantRenamePositionOffset = (int) parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), ((NewClassTree) tp.getLeaf()).getIdentifier());
            
            final Document doc = parameter.getDocument();
            
            if (doc != null) {
                doc.render(new Runnable() {
                    public void run() {
                        try {
                            instantRenamePosition = doc.createPosition(instantRenamePositionOffset);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(ConvertAnonymousToInner.class.getName()).log(Level.INFO, null, ex);
                        }
                    }
                });
            }
        }
    }

    private static final class DetectUsedVars extends TreePathScanner<Void, Set<VariableElement>> {
        private CompilationInfo info;
        private TreePath newClassToConvert;
        
        private DetectUsedVars(CompilationInfo info, TreePath newClassToConvert) {
            this.info = info;
            this.newClassToConvert = newClassToConvert;
        }
        
        private static final Set<ElementKind> VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.PARAMETER, ElementKind.LOCAL_VARIABLE);
        
        @Override
        public Void visitIdentifier(IdentifierTree node, Set<VariableElement> p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            TreePath elPath = el != null ? info.getTrees().getPath(el) : null;
            
            if (el != null && elPath != null && VARIABLES.contains(el.getKind()) && !isParent(newClassToConvert, elPath)) {
                p.add((VariableElement) el);
            }
            
            return super.visitIdentifier(node, p);
        }

    }
    
    private static final class DetectUseOfNonStaticMembers extends TreePathScanner<Boolean, Void> {
        private CompilationInfo info;
        private TreePath newClassToConvert;
        
        private DetectUseOfNonStaticMembers(CompilationInfo info, TreePath newClassToConvert) {
            this.info = info;
            this.newClassToConvert = newClassToConvert;
        }
        
        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && (el.getKind().isField() || el.getKind() == ElementKind.METHOD) && !el.getModifiers().contains(Modifier.STATIC)) {
                return true;
            }
            
            return super.visitIdentifier(node, p);
        }

        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
        }

    }
    
    private static boolean isParent(TreePath tp1, TreePath tp2) {
        while (tp2 != null && tp1.getLeaf() != tp2.getLeaf()) {
            tp2 = tp2.getParentPath();
        }

        if (tp2 == null) {
            return false;
        }

        return tp1.getLeaf() == tp2.getLeaf();
    }
    
    private static String generateName(CompilationInfo info, TreePath newClassToConvert, String prototype) {
        Scope s = info.getTrees().getScope(newClassToConvert);
        Integer extension = null;
        
        if (s == null) return prototype + "Impl"; //NOI18N
        
        while (true) {
            String currentProposal = prototype + "Impl" + (extension == null ? "" : extension.toString()); //NOI18N
            Scope currentScope = s;
            boolean found = false;
            
            OUTER : while (currentScope != null) {
                for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new ElementAcceptor() {
                    public boolean accept(Element e, TypeMirror type) {
                        return true;
                    }
                })) {
                    if (e.getKind().isClass() || e.getKind().isInterface()) {
                        String sn = e.getSimpleName().toString();
                        
                        if (currentProposal.equals(sn)) {
                            found = true;
                            break OUTER;
                        }
                    }
                }
                
                currentScope = currentScope.getEnclosingScope();
            }
            
            if (!found) {
                return currentProposal;
            }
            
            extension = extension != null ? extension + 1 : 1;
        }
    }
    
    static void convertAnonymousToInner(WorkingCopy copy, TreePath newClassToConvert) {
        TreeMaker make = copy.getTreeMaker();
        NewClassTree nct = (NewClassTree) newClassToConvert.getLeaf();
        
        Set<VariableElement> usedElementVariables = new LinkedHashSet<VariableElement>();
        
        new DetectUsedVars(copy, newClassToConvert).scan(new TreePath(newClassToConvert, nct.getClassBody()), usedElementVariables);
        boolean usesNonStaticMembers = new DetectUseOfNonStaticMembers(copy, newClassToConvert).scan(new TreePath(newClassToConvert, nct.getClassBody()), null) == Boolean.TRUE;
                
        TreePath tp = newClassToConvert;
        
        while (tp != null && tp.getLeaf().getKind() != Kind.CLASS) {
            tp = tp.getParentPath();
        }
        
        ClassTree target = (ClassTree) tp.getLeaf();
        
        TypeMirror superType = copy.getTrees().getTypeMirror(new TreePath(newClassToConvert, nct.getIdentifier()));
        Element superTypeElement = copy.getTrees().getElement(new TreePath(newClassToConvert, nct.getIdentifier()));
        
        boolean isStaticContext = true;
        FileObject source = SourceUtils.getFile(superTypeElement, copy.getClasspathInfo());
        
        if (source == copy.getFileObject() /*&& copy.getElementUtilities().outermostTypeElement(superTypeElement) != superTypeElement*/) {
            Element currentElement = superTypeElement;
            
            while (currentElement.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                if (!currentElement.getModifiers().contains(Modifier.STATIC)) {
                    isStaticContext = false;
                    break;
                }
                
                currentElement = currentElement.getEnclosingElement();
            }
        }
        
        Tree superTypeTree = make.Type(superType);
        
        Logger.getLogger(ConvertAnonymousToInner.class.getName()).log(Level.FINE, "usesNonStaticMembers = {0}", usesNonStaticMembers ); //NOI18N
        
        TreePath superConstructorCall = findSuperConstructorCall(newClassToConvert);
        
        ModifiersTree classModifiers = make.Modifiers((isStaticContext && !usesNonStaticMembers) ? EnumSet.of(Modifier.PRIVATE, Modifier.STATIC) : EnumSet.of(Modifier.PRIVATE));
        
        List<Tree> members = new ArrayList<Tree>();
        List<VariableTree> constrArguments = new ArrayList<VariableTree>();
        List<StatementTree> constrBodyStatements = new ArrayList<StatementTree>();
        List<ExpressionTree> constrRealArguments = new ArrayList<ExpressionTree>();
        
        if (superConstructorCall != null) {
            Element superConstructor = copy.getTrees().getElement(superConstructorCall);
            
            if (superConstructor != null && superConstructor.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) superConstructor;
                TypeMirror nctTypes = copy.getTrees().getTypeMirror(newClassToConvert);
                
                assert nctTypes.getKind() == TypeKind.DECLARED;
                
                ExecutableType et = (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) nctTypes, ee);
                
                if (!ee.getParameters().isEmpty()) {
                    List<ExpressionTree> nueSuperConstructorCallRealArguments = new LinkedList<ExpressionTree>();
                    Iterator<? extends VariableElement> names = ee.getParameters().iterator();
                    Iterator<? extends TypeMirror> types = et.getParameterTypes().iterator();

                    while (names.hasNext() && types.hasNext()) {
                        ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        CharSequence name = names.next().getSimpleName();

                        constrArguments.add(make.Variable(mt, name, make.Type(types.next()), null));
                        nueSuperConstructorCallRealArguments.add(make.Identifier(name));
                    }

                    constrBodyStatements.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), nueSuperConstructorCallRealArguments)));
                }
            }
        }
        
        constrRealArguments.addAll(nct.getArguments());
        
        ModifiersTree privateFinalMods = make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
        ModifiersTree emptyArgs = make.Modifiers(EnumSet.noneOf(Modifier.class));
        
        for (VariableElement ve : usedElementVariables) {
            members.add(make.Variable(privateFinalMods, ve.getSimpleName(), make.Type(ve.asType()), null));
            constrArguments.add(make.Variable(emptyArgs, ve.getSimpleName(), make.Type(ve.asType()), null));
            constrBodyStatements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); // NOI18N
            constrRealArguments.add(make.Identifier(ve.getSimpleName()));
        }
        
        List<Tree> oldMembers = new ArrayList<Tree>(nct.getClassBody().getMembers());
        
        //remove def. constructor:
        oldMembers.remove(0);
        
        ModifiersTree constructorModifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
        
        MethodTree constr = make.Method(constructorModifiers, "<init>", null, Collections.<TypeParameterTree>emptyList(), constrArguments, Collections.<ExpressionTree>emptyList(), make.Block(constrBodyStatements, false), null); // NOI18N
        
        members.add(constr);
        members.addAll(oldMembers);
        
        String newClassName = generateName(copy, newClassToConvert, superTypeElement.getSimpleName().toString());
        
        ClassTree clazz = make.Class(classModifiers, newClassName, Collections.<TypeParameterTree>emptyList(), superTypeElement.getKind().isClass() ? superTypeTree : null, superTypeElement.getKind().isClass() ? Collections.<Tree>emptyList() : Collections.<Tree>singletonList(superTypeTree), members);
        
        copy.rewrite(target, make.addClassMember(target, clazz));
        
        NewClassTree nueNCT = make.NewClass(/*!!!*/null, Collections.<ExpressionTree>emptyList(), make.Identifier(newClassName), constrRealArguments, null);
        
        copy.rewrite(nct, nueNCT);
    }

    public void cancel() {
    }

    private static TreePath findSuperConstructorCall(TreePath nct) {
        class FindSuperConstructorCall extends TreePathScanner<TreePath, Void> {

            private boolean stop;

            @Override
            public TreePath scan(Tree tree, Void p) {
                if (stop) return null;
                return super.scan(tree, p);
            }
            
            @Override
            public TreePath visitMethodInvocation(MethodInvocationTree tree, Void v) {
                if (tree.getMethodSelect().getKind() == Kind.IDENTIFIER && "super".equals(((IdentifierTree) tree.getMethodSelect()).getName().toString())) {
                    stop = true;
                    return getCurrentPath();
                }

                return null;
            }

            @Override
            public TreePath reduce(TreePath first, TreePath second) {
                if (first == null) {
                    return second;
                } else {
                    return first;
                }
            }

        }
        
        return new FindSuperConstructorCall().scan(nct, null);
    }
}
