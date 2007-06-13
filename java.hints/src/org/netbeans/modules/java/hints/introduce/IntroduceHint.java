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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.SelectionAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceHint implements CancellableTask<CompilationInfo> {

    public IntroduceHint() {
    }

    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID);
    private static final Set<JavaTokenId> WHITESPACES = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    
    static int[] ignoreWhitespaces(CompilationInfo ci, int start, int end) {
        TokenSequence<JavaTokenId> ts = ci.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        if (ts == null) {
            return new int[] {start, end};
        }
        
        ts.move(start);
        
        if (ts.moveNext()) {
            while (WHITESPACES.contains(ts.token().id()))
                ts.moveNext();
            
            if (ts.offset() > start)
                start = ts.offset();
        }
        
        ts.move(end);
        
        while (ts.movePrevious() && WHITESPACES.contains(ts.token().id()) && ts.offset() < end)
            end = ts.offset();
        
        return new int[] {start, end};
    }
    
    static TreePathHandle validateSelection(CompilationInfo ci, int start, int end) {
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);
        
        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();
            
            if (StatementTree.class.isAssignableFrom(leaf.getKind().asInterface()))
                return null;
            
            if (!ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface()))
                continue;
            
            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);
            
            if (treeStart != start || treeEnd != end) {
                continue;
            }
            
            TypeMirror type = ci.getTrees().getTypeMirror(tp);
            
            if (type == null || NOT_ACCEPTED_TYPES.contains(type.getKind()))
                continue;
            
            if (tp.getParentPath().getLeaf().getKind() == Kind.EXPRESSION_STATEMENT)
                continue;
            
            return TreePathHandle.create(tp, ci);
        }
        
        return null;
    }
    
    static TreePathHandle validateSelectionForIntroduceMethod(CompilationInfo ci, int start, int end, int[] statementsSpan) {
        int[] span = ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));
        
        start = span[0];
        end   = span[1];
        
        if (start >= end)
            return null;
        
        TreePath tpStart = ci.getTreeUtilities().pathFor(start);
        TreePath tpEnd = ci.getTreeUtilities().pathFor(end);
        
        if (tpStart.getLeaf() != tpEnd.getLeaf() || tpStart.getLeaf().getKind() != Kind.BLOCK) {
            //??? not in the same block:
            return null;
        }
        
        int from = -1;
        int to   = -1;
        
        BlockTree block = (BlockTree) tpStart.getLeaf();
        int index = 0;
        
        for (StatementTree s : block.getStatements()) {
            long sStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), s);
            
            if (sStart == start) {
                from = index;
            }
            
            if (end < sStart && to == (-1)) {
                to = index - 1;
            }
            
            index++;
        }
        
        if (from == (-1)) {
            return null;
        }
        
        if (to == (-1))
            to = block.getStatements().size() - 1;
     
        if (to < from) {
            return null;
        }
        
        statementsSpan[0] = from;
        statementsSpan[1] = to;
        
        return TreePathHandle.create(tpStart, ci);
    }
    
    public void run(CompilationInfo info) {
        FileObject file = info.getFileObject();
        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(file);
        
        if (selection == null) {
            //nothing to do....
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), Collections.<ErrorDescription>emptyList());
        } else {
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), computeError(info, selection[0], selection[1], null, new EnumMap<IntroduceKind, String>(IntroduceKind.class)));
        }
    }
    
    public void cancel() {
        //XXX
    }
    
    private static boolean isConstructor(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);
        
        return e != null && e.getKind() == ElementKind.CONSTRUCTOR;
    }
    
    private static List<TreePath> findConstructors(CompilationInfo info, TreePath method) {
        List<TreePath> result = new LinkedList<TreePath>();
        TreePath parent = method.getParentPath();
        
        if (parent.getLeaf().getKind() == Kind.CLASS) {
            for (Tree t : ((ClassTree) parent.getLeaf()).getMembers()) {
                TreePath tp = new TreePath(parent, t);
                
                if (isConstructor(info, tp)) {
                    result.add(tp);
                }
            }
        }
        
        return result;
    }
    
    static List<ErrorDescription> computeError(CompilationInfo info, int start, int end, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage) {
        List<ErrorDescription> hints = new LinkedList<ErrorDescription>();
        List<Fix> fixes = new LinkedList<Fix>();
        TreePathHandle h = validateSelection(info, start, end);
        
        if (h != null) {
            TreePath resolved = h.resolve(info);
            TreePath method   = findMethod(resolved);
            boolean isConstant = checkConstantExpression(info, resolved);
            boolean isVariable = findStatement(resolved) != null && method != null;
            List<TreePath> duplicatesForVariable = isVariable ? CopyFinder.computeDuplicates(info, resolved, method) : null;
            List<TreePath> duplicatesForConstant = /*isConstant ? */CopyFinder.computeDuplicates(info, resolved, new TreePath(info.getCompilationUnit()));// : null;
            Scope scope = info.getTrees().getScope(resolved);
            boolean statik = scope != null ? info.getTreeUtilities().isStaticContext(scope) : false;
            String guessedName = Utilities.guessName(info, resolved);
            Fix variable = isVariable ? new IntroduceFix(h, info.getJavaSource(), guessedName, duplicatesForVariable.size() + 1, IntroduceKind.CREATE_VARIABLE) : null;
            Fix constant = isConstant ? new IntroduceFix(h, info.getJavaSource(), guessedName, duplicatesForConstant.size() + 1, IntroduceKind.CREATE_CONSTANT) : null;
            Fix field = null;
            
            if (method != null) {
                int[] initilizeIn = computeInitializeIn(info, resolved, duplicatesForConstant);
                
                if (statik) {
                    initilizeIn[0] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
                    initilizeIn[1] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
                }
                
                boolean allowFinalInCurrentMethod = false;
                
                if (isConstructor(info, method)) {
                    //how many constructors do we have in the target class?:
                    allowFinalInCurrentMethod = findConstructors(info, method).size() == 1;
                }
                
                field = new IntroduceFieldFix(h, info.getJavaSource(), guessedName, duplicatesForConstant.size() + 1, initilizeIn, statik, allowFinalInCurrentMethod);
            }
            
            if (fixesMap != null) {
                fixesMap.put(IntroduceKind.CREATE_VARIABLE, variable);
                fixesMap.put(IntroduceKind.CREATE_CONSTANT, constant);
                fixesMap.put(IntroduceKind.CREATE_FIELD, field);
            }
            
            
            if (variable != null) {
                fixes.add(variable);
            }
            
            if (constant != null) {
                fixes.add(constant);
            }
            
            if (field != null) {
                fixes.add(field);
            }
        }
        
        Fix introduceMethod = computeIntroduceMethod(info, start, end, fixesMap, errorMessage);
        
        if (introduceMethod != null) {
            fixes.add(introduceMethod);
            if (fixesMap != null) {
                fixesMap.put(IntroduceKind.CREATE_METHOD, introduceMethod);
            }
        }
        
        if (!fixes.isEmpty()) {
            int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            String displayName = NbBundle.getMessage(IntroduceHint.class, "HINT_Introduce");
            
            hints.add(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, fixes, info.getFileObject(), pos, pos));
        }
        
        return hints;
    }
    
    static Fix computeIntroduceMethod(CompilationInfo info, int start, int end, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage) {
        int[] statements = new int[2];
        
        TreePathHandle h = validateSelectionForIntroduceMethod(info, start, end, statements);
        
        if (h == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection"); // NOI18N
            return null;
        }
        
        TreePath block = h.resolve(info);
        TreePath method = findMethod(block);
        Element methodEl = info.getTrees().getElement(method);
        BlockTree bt = (BlockTree) block.getLeaf();
        List<? extends StatementTree> statementsToWrap = bt.getStatements().subList(statements[0], statements[1] + 1);
        ScanStatement scanner = new ScanStatement(info, statementsToWrap.get(0), statementsToWrap.get(statementsToWrap.size() - 1));
        Set<TypeMirror> exceptions = new HashSet<TypeMirror>();
        int index = 0;
        TypeMirror methodReturnType = info.getTypes().getNoType(TypeKind.VOID);
        
        if (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR) {
            ExecutableElement ee = (ExecutableElement) methodEl;
            
            scanner.localVariables.addAll(ee.getParameters());
            methodReturnType = ee.getReturnType();
        }
        
        scanner.scan(method, null);
        
        for (StatementTree s : bt.getStatements()) {
            TreePath path = new TreePath(block, s);
            
            if (index >= statements[0] && index <= statements[1]) {
                exceptions.addAll(info.getTreeUtilities().getUncaughtExceptions(path));
            }
            
            index++;
        }
        
        ExitsFromAllBranches efab = new ExitsFromAllBranches(info);
        
        boolean exitsFromAllBranches = efab.scan(new TreePath(block, statementsToWrap.get(statementsToWrap.size() - 1)), null) == Boolean.TRUE;
        
        String exitsError = scanner.verifyExits(exitsFromAllBranches);
        
        if (exitsError != null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, exitsError);
            return null;
        }
        
        List<TypeMirrorHandle> paramTypes = new LinkedList<TypeMirrorHandle>();
        List<String> paramNames = new LinkedList<String>();
        
        for (VariableElement ve : scanner.usedLocalVariables) {
            paramTypes.add(TypeMirrorHandle.create(ve.asType()));
            paramNames.add(ve.getSimpleName().toString());
        }
        
        List<TreePathHandle> exits = new LinkedList<TreePathHandle>();
        
        for (TreePath tp : scanner.selectionExits) {
            exits.add(TreePathHandle.create(tp, info));
        }
        
        TypeMirror returnType;
        String returnName;
        boolean declareVariableForReturnValue;
        
        if (!scanner.usedSelectionLocalVariables.isEmpty()) {
            VariableElement result = scanner.usedSelectionLocalVariables.iterator().next();
            
            returnType = result.asType();
            returnName = result.getSimpleName().toString();
            declareVariableForReturnValue = scanner.selectionLocalVariables.contains(result);
        } else {
            if (!exits.isEmpty() && !exitsFromAllBranches) {
                returnType = info.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                returnName = null;
                declareVariableForReturnValue = false;
            } else {
                if (exitsFromAllBranches && scanner.hasReturns) {
                    returnType = methodReturnType;
                    returnName = null;
                    declareVariableForReturnValue = false;
                } else {
                    returnType = info.getTypes().getNoType(TypeKind.VOID);
                    returnName = null;
                    declareVariableForReturnValue = false;
                }
            }
        }
        
        Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();
        
        for (TypeMirror tm : exceptions) {
            exceptionHandles.add(TypeMirrorHandle.create(tm));
        }
        
        return new IntroduceMethodFix(info.getJavaSource(), h, paramTypes, paramNames, TypeMirrorHandle.create(returnType), returnName, declareVariableForReturnValue, exceptionHandles, exits, exitsFromAllBranches, statements[0], statements[1]);
    }
    
    static boolean checkConstantExpression(CompilationInfo info, TreePath path) {
        Tree expr = path.getLeaf();
        
        if (expr.getKind().asInterface() == BinaryTree.class) {
            BinaryTree bt = (BinaryTree) expr;
            
            return    checkConstantExpression(info, new TreePath(path, bt.getLeftOperand()))
                   && checkConstantExpression(info, new TreePath(path, bt.getRightOperand()));
        }
        
        if (expr.getKind() == Kind.IDENTIFIER || expr.getKind() == Kind.MEMBER_SELECT) {
            Element e = info.getTrees().getElement(path);
            
            if (e == null)
                return false;
            
            if (e.getKind() != ElementKind.FIELD)
                return false;
            
            if (!e.getModifiers().contains(Modifier.STATIC))
                return false;
            
            if (!e.getModifiers().contains(Modifier.FINAL))
                return false;
            
            TypeMirror type = e.asType();
            
            if (type.getKind().isPrimitive())
                return true;
            
            if (type.getKind() == TypeKind.DECLARED) {
                TypeElement te = (TypeElement) ((DeclaredType) type).asElement();
                
                return "java.lang.String".equals(te.getQualifiedName().toString()); // NOI18N
            }
            
            return false;
        }
    
        return LITERALS.contains(expr.getKind());
    }
    
    private static final Set<Kind> LITERALS = EnumSet.of(Kind.STRING_LITERAL, Kind.CHAR_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.FLOAT_LITERAL, Kind.DOUBLE_LITERAL);
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
    private static TreePath findStatement(TreePath statementPath) {
        while (    statementPath != null
                && (   !StatementTree.class.isAssignableFrom(statementPath.getLeaf().getKind().asInterface())
                || (   statementPath.getParentPath() != null
                && statementPath.getParentPath().getLeaf().getKind() != Kind.BLOCK))) {
            if (statementPath.getLeaf().getKind() == Kind.CLASS)
                return null;
            
            statementPath = statementPath.getParentPath();
        }
        
        return statementPath;
    }
    
    private static TreePath findMethod(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.METHOD) {
                return path;
            }
            
            if (   path.getLeaf().getKind() == Kind.BLOCK
                && path.getParentPath() != null
                && path.getParentPath().getLeaf().getKind() == Kind.CLASS) {
                //initializer:
                return path;
            }
            
            path = path.getParentPath();
        }
        
        return null;
    }
    
    private static TreePath findClass(TreePath path) {
        while (path != null) {
            if (path.getLeaf().getKind() == Kind.CLASS) {
                return path;
            }
            
            path = path.getParentPath();
        }
        
        return null;
    }
    
    private static boolean isParentOf(TreePath parent, TreePath path) {
        Tree parentLeaf = parent.getLeaf();
        
        while (path != null && path.getLeaf() != parentLeaf) {
            path = path.getParentPath();
        }
        
        return path != null;
    }
    
    private static boolean isParentOf(TreePath parent, List<? extends TreePath> candidates) {
        for (TreePath tp : candidates) {
            if (!isParentOf(parent, tp))
                return false;
        }
        
        return true;
    }
    
    private static BlockTree findAddPosition(CompilationInfo info, TreePath original, List<? extends TreePath> candidates, int[] outPosition) {
        //find least common block holding all the candidates:
        TreePath statement = original;
        
        for (TreePath p : candidates) {
            Tree leaf = p.getLeaf();
            int  leafStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
            int  stPathStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement.getLeaf());
            
            if (leafStart < stPathStart) {
                statement = p;
            }
        }
        
        List<TreePath> allCandidates = new LinkedList<TreePath>();
        
        allCandidates.add(original);
        allCandidates.addAll(candidates);
        
        statement = findStatement(statement);
        
        if (statement == null) {
            //XXX: well....
            return null;
        }
        
        while (statement.getParentPath() != null && !isParentOf(statement.getParentPath(), allCandidates)) {
            statement = statement.getParentPath();
        }
        
        if (statement.getParentPath() == null)
            return null;//XXX: log
        
        BlockTree statements = (BlockTree) statement.getParentPath().getLeaf();
        StatementTree statementTree = (StatementTree) statement.getLeaf();
        
        int index = statements.getStatements().indexOf(statementTree);
        
        if (index == (-1)) {
            //really strange...
            return null;
        }
        
        outPosition[0] = index;
        
        return statements;
    }
    
    private static int[] computeInitializeIn(final CompilationInfo info, TreePath firstOccurrence, List<TreePath> occurrences) {
        int[] result = new int[] {7, 7};
        boolean inOneMethod = true;
        Tree currentMethod = findMethod(firstOccurrence).getLeaf();
        
        for (TreePath occurrence : occurrences) {
            TreePath method = findMethod(occurrence);
            
            if (method == null || currentMethod != method.getLeaf()) {
                inOneMethod = false;
                break;
            }
        }
        
        class Result extends RuntimeException {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return null;
            }
            
        }
        class ReferencesLocalVariable extends TreePathScanner<Void, Void> {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element e = info.getTrees().getElement(getCurrentPath());
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    throw new Result();
                }
                
                return null;
            }
        }
        
        boolean referencesLocalvariables = false;
        
        try {
            new ReferencesLocalVariable().scan(firstOccurrence, null);
        } catch (Result r) {
            referencesLocalvariables = true;
        }
        
        if (!inOneMethod) {
            result[1] = IntroduceFieldPanel.INIT_FIELD | IntroduceFieldPanel.INIT_CONSTRUCTORS;
        }
        
        if (referencesLocalvariables) {
            result[0] = IntroduceFieldPanel.INIT_METHOD;
            result[1] = IntroduceFieldPanel.INIT_METHOD;
        }
        
        return result;
    }
    
    private static final class ScanStatement extends TreePathScanner<Void, Void> {
        private static final int PHASE_BEFORE_SELECTION = 1;
        private static final int PHASE_INSIDE_SELECTION = 2;
        private static final int PHASE_AFTER_SELECTION = 3;
        
        private CompilationInfo info;
        private int phase = PHASE_BEFORE_SELECTION;
        private Tree firstInSelection;
        private Tree lastInSelection;
        private Set<VariableElement> localVariables = new HashSet<VariableElement>();
        private Set<VariableElement> usedLocalVariables = new HashSet<VariableElement>();
        private Set<VariableElement> selectionLocalVariables = new HashSet<VariableElement>();
        private Set<VariableElement> selectionWrittenLocalVariables = new HashSet<VariableElement>();
        private Set<VariableElement> usedSelectionLocalVariables = new HashSet<VariableElement>();
        private Set<TreePath> selectionExits = new HashSet<TreePath>();
        private Set<Tree> treesSeensInSelection = new HashSet<Tree>();
        private boolean hasReturns = false;
        private boolean hasBreaks = false;
        private boolean hasContinues = false;

        public ScanStatement(CompilationInfo info, Tree firstInSelection, Tree lastInSelection) {
            this.info = info;
            this.firstInSelection = firstInSelection;
            this.lastInSelection = lastInSelection;
        }

        @Override
        public Void scan(Tree tree, Void p) {
            if (tree == firstInSelection) {
                phase = PHASE_INSIDE_SELECTION;
            }
            
            if (phase == PHASE_INSIDE_SELECTION) {
                treesSeensInSelection.add(tree);
            }
            
            Void result = super.scan(tree, p);
            
            if (tree == lastInSelection) {
                phase = PHASE_AFTER_SELECTION;
            }
            
            return result;
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            
            assert LOCAL_VARIABLES.contains(e.getKind());
            
            switch (phase) {
                case PHASE_BEFORE_SELECTION:
                    localVariables.add((VariableElement) e);
                    break;
                case PHASE_INSIDE_SELECTION:
                    selectionLocalVariables.add((VariableElement) e);
                    break;
            }
            
            return super.visitVariable(node, p);
        }

        @Override
        public Void visitAssignment(AssignmentTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION) {
                Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    selectionWrittenLocalVariables.add((VariableElement) e);
                }
            }
            
            return super.visitAssignment(node, p);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION) {
                Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    selectionWrittenLocalVariables.add((VariableElement) e);
                }
            }
            
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (LOCAL_VARIABLES.contains(e.getKind())) {
                switch (phase) {
                    case PHASE_INSIDE_SELECTION:
                        if (localVariables.contains(e)) {
                            usedLocalVariables.add((VariableElement) e);
                        }
                        break;
                    case PHASE_AFTER_SELECTION:
                        if (selectionLocalVariables.contains(e) || selectionWrittenLocalVariables.contains(e)) {
                            usedSelectionLocalVariables.add((VariableElement) e);
                        }
                        break;
                }
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitReturn(ReturnTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION) {
                selectionExits.add(getCurrentPath());
                hasReturns = true;
            }
            return super.visitReturn(node, p);
        }

        @Override
        public Void visitBreak(BreakTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(info, getCurrentPath()))) {
                selectionExits.add(getCurrentPath());
                hasBreaks = true;
            }
            return super.visitBreak(node, p);
        }

        @Override
        public Void visitContinue(ContinueTree node, Void p) {
            if (phase == PHASE_INSIDE_SELECTION && !treesSeensInSelection.contains(info.getTreeUtilities().getBreakContinueTarget(info, getCurrentPath()))) {
                selectionExits.add(getCurrentPath());
                hasContinues = true;
            }
            return super.visitContinue(node, p);
        }
        
        private String verifyExits(boolean exitsFromAllBranches) {
            int i = 0;
            
            i += hasReturns ? 1 : 0;
            i += hasBreaks ? 1 : 0;
            i += hasContinues ? 1 : 0;
            
            if (i > 1) {
                return "ERR_Too_Many_Different_Exits"; // NOI18N
            }
            
            if ((exitsFromAllBranches ? 0 : i) + usedSelectionLocalVariables.size() > 1) {
                return "ERR_Too_Many_Return_Values"; // NOI18N
            }
            
            StatementTree breakOrContinueTarget = null;
            boolean returnValueComputed = false;
            TreePath returnValue = null;
            
            for (TreePath tp : selectionExits) {
                if (tp.getLeaf().getKind() == Kind.RETURN) {
                    if (!exitsFromAllBranches) {
                        ReturnTree rt = (ReturnTree) tp.getLeaf();
                        TreePath currentReturnValue = rt.getExpression() != null ? new TreePath(tp, rt.getExpression()) : null;
                        
                        if (!returnValueComputed) {
                            returnValue = currentReturnValue;
                            returnValueComputed = true;
                        } else {
                            if (returnValue != null && currentReturnValue != null) {
                                List<TreePath> candidates = CopyFinder.computeDuplicates(info, returnValue, currentReturnValue);
                                
                                if (candidates.size() != 1 || candidates.get(0).getLeaf() != rt.getExpression()) {
                                    return "ERR_Different_Return_Values"; // NOI18N
                                }
                            } else {
                                if (returnValue != currentReturnValue) {
                                    return "ERR_Different_Return_Values"; // NOI18N
                                }
                            }
                        }
                    }
                } else {
                    StatementTree target = info.getTreeUtilities().getBreakContinueTarget(info, tp);
                    
                    if (breakOrContinueTarget == null) {
                        breakOrContinueTarget = target;
                    }
                    
                    if (breakOrContinueTarget != target)
                        return "ERR_Break_Mismatch"; // NOI18N
                }
            }
            
            return null;
        }
    }
    
    private static final class ExitsFromAllBranches extends TreePathScanner<Boolean, Void> {
        
        private CompilationInfo info;
        private Set<Tree> seenTrees = new HashSet<Tree>();

        public ExitsFromAllBranches(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            seenTrees.add(tree);
            return super.scan(tree, p);
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            return scan(node.getThenStatement(), null) == Boolean.TRUE && scan(node.getElseStatement(), null) == Boolean.TRUE;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(info, getCurrentPath()));
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(info, getCurrentPath()));
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            return false;
        }
        
    }
    
    private static final class IntroduceFix implements Fix {
        
        private String guessedName;
        private TreePathHandle handle;
        private JavaSource js;
        private int numDuplicates;
        private IntroduceKind kind;

        public IntroduceFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, IntroduceKind kind) {
            this.handle = handle;
            this.js = js;
            this.guessedName = guessedName;
            this.numDuplicates = numDuplicates;
            this.kind = kind;
        }

        @Override
        public String toString() {
            return "[IntroduceFix:" + guessedName + ":" + numDuplicates + ":" + kind + "]"; // NOI18N
        }
        
        public String getKeyExt() {
            switch (kind) {
                case CREATE_CONSTANT:
                    return "IntroduceConstant"; //NOI18N
                case CREATE_VARIABLE:
                    return "IntroduceVariable"; //NOI18N
                default:
                    throw new IllegalStateException();
            }
        }
        
        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_" + getKeyExt()); //NOI18N
        }
        
        public ChangeInfo implement() throws IOException, BadLocationException {
            IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, guessedName, kind == IntroduceKind.CREATE_CONSTANT);
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                return null;//cancel
            }
            final String name = panel.getVariableName();
            final boolean replaceAll = panel.isReplaceAll();
            final boolean declareFinal = panel.isDeclareFinal();
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {}
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    
                    TreePath resolved = handle.resolve(parameter);
                    TypeMirror tm = parameter.getTrees().getTypeMirror(resolved);
                    
                    if (resolved == null || tm == null) {
                        return ; //TODO...
                    }
                    
                    //hack: creating a copy of the expression:
                    Document doc = parameter.getDocument();
                    int start = (int) parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), resolved.getLeaf());
                    int end   = (int) parameter.getTrees().getSourcePositions().getEndPosition(parameter.getCompilationUnit(), resolved.getLeaf());
                    String text = doc.getText(start, end - start);
                    ExpressionTree expressionCopy = parameter.getTreeUtilities().parseExpression(text, new SourcePositions[1]);
                    ModifiersTree mods;
                    final TreeMaker make = parameter.getTreeMaker();
                    
                    switch (kind) {
                        case CREATE_CONSTANT:
                            //find first class:
                            TreePath pathToClass = resolved;
                            
                            while (pathToClass != null && pathToClass.getLeaf().getKind() != Kind.CLASS) {
                                pathToClass = pathToClass.getParentPath();
                            }
                            
                            if (pathToClass == null) {
                                return ; //TODO...
                            }
                            
                            mods = make.Modifiers(EnumSet.of(Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE));
                            
                            VariableTree constant = make.Variable(mods, name, make.Type(tm), expressionCopy);
                            ClassTree nueClass = GeneratorUtils.insertClassMember(parameter, pathToClass, constant);
                            
                            parameter.rewrite(pathToClass.getLeaf(), nueClass);
                            
                            if (replaceAll) {
                                for (TreePath p : CopyFinder.computeDuplicates(parameter, resolved, new TreePath(parameter.getCompilationUnit()))) {
                                    parameter.rewrite(p.getLeaf(), make.Identifier(name));
                                }
                            }
                            break;
                        case CREATE_VARIABLE:
                            TreePath method        = findMethod(resolved);
                            
                            if (method == null) {
                                return ; //TODO...
                            }
                            
                            BlockTree statements;
                            int       index;
                            
                            if (replaceAll) {
                                List<TreePath> candidates = CopyFinder.computeDuplicates(parameter, resolved, method);
                                for (TreePath p : candidates) {
                                    Tree leaf = p.getLeaf();
                                    
                                    parameter.rewrite(leaf, make.Identifier(name));
                                }
                                
                                int[] out = new int[1];
                                statements = findAddPosition(parameter, resolved, candidates, out);
                                
                                if (statements == null) {
                                    return;
                                }
                                
                                index = out[0];
                            } else {
                                int[] out = new int[1];
                                statements = findAddPosition(parameter, resolved, Collections.<TreePath>emptyList(), out);
                                
                                if (statements == null) {
                                    return;
                                }
                                
                                index = out[0];
                            }
                            
                            List<StatementTree> nueStatements = new LinkedList<StatementTree>(statements.getStatements());
                            mods = make.Modifiers(declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class));
                            
                            nueStatements.add(index, make.Variable(mods, name, make.Type(tm), expressionCopy/*(ExpressionTree) resolved.getLeaf()*//*(ExpressionTree) resolved.getLeaf()*/));
                            
                            BlockTree nueBlock = make.Block(nueStatements, false);
                            
                            parameter.rewrite(statements, nueBlock);
                            break;
                    }
                    
                    parameter.rewrite(resolved.getLeaf(), make.Identifier(name));
                }
            }).commit();
            return null;
        }
    }
            
    private static final class IntroduceFieldFix implements Fix {
        
        private String guessedName;
        private TreePathHandle handle;
        private JavaSource js;
        private int numDuplicates;
        private int[] initilizeIn;
        private boolean statik;
        private boolean allowFinalInCurrentMethod;

        public IntroduceFieldFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod) {
            this.handle = handle;
            this.js = js;
            this.guessedName = guessedName;
            this.numDuplicates = numDuplicates;
            this.initilizeIn = initilizeIn;
            this.statik = statik;
            this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
        }
        
        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceField");
        }
        
        @Override
        public String toString() {
            return "[IntroduceField:" + guessedName + ":" + numDuplicates + ":" + statik + ":" + allowFinalInCurrentMethod + ":" + Arrays.toString(initilizeIn) + "]"; // NOI18N
        }
        
        public ChangeInfo implement() throws IOException, BadLocationException {
            IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, allowFinalInCurrentMethod);
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceField");
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                return null;//cancel
            }
            final String name = panel.getFieldName();
            final boolean replaceAll = panel.isReplaceAll();
            final boolean declareFinal = panel.isDeclareFinal();
            final Set<Modifier> access = panel.getAccess();
            final int initializeIn = panel.getInitializeIn();
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {}
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    
                    TreePath resolved = handle.resolve(parameter);
                    TypeMirror tm = parameter.getTrees().getTypeMirror(resolved);
                    
                    if (resolved == null || tm == null) {
                        return ; //TODO...
                    }
                    
                    TreePath pathToClass = resolved;
                    
                    while (pathToClass != null && pathToClass.getLeaf().getKind() != Kind.CLASS) {
                        pathToClass = pathToClass.getParentPath();
                    }
                    
                    if (pathToClass == null) {
                        return ; //TODO...
                    }
                    
                    //hack: creating a copy of the expression:
                    Document doc = parameter.getDocument();
                    int start = (int) parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), resolved.getLeaf());
                    int end   = (int) parameter.getTrees().getSourcePositions().getEndPosition(parameter.getCompilationUnit(), resolved.getLeaf());
                    String text = doc.getText(start, end - start);
                    ExpressionTree expressionCopy = parameter.getTreeUtilities().parseExpression(text, new SourcePositions[1]);
                    
                    Set<Modifier> mods = declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class);
                    
                    if (statik) {
                        mods.add(Modifier.STATIC);
                    }
                    
                    mods.addAll(access);
                    final TreeMaker make = parameter.getTreeMaker();
                    
                    ModifiersTree modsTree = make.Modifiers(mods);
                    
                    if (replaceAll) {
                        for (TreePath p : CopyFinder.computeDuplicates(parameter, resolved, new TreePath(parameter.getCompilationUnit()))) {
                            parameter.rewrite(p.getLeaf(), make.Identifier(name));
                        }
                    }
                    
                    VariableTree field = make.Variable(modsTree, name, make.Type(tm), initializeIn == IntroduceFieldPanel.INIT_FIELD ? expressionCopy : null);
                    ClassTree nueClass = GeneratorUtils.insertClassMember(parameter, pathToClass, field);
                    
                    parameter.rewrite(resolved.getLeaf(), make.Identifier(name));

                    TreePath method        = findMethod(resolved);
                        
                    if (method == null) {
                        return ; //TODO...
                    }
                    
                    if (initializeIn == IntroduceFieldPanel.INIT_METHOD) {
                        TreePath statementPath = resolved;
                        
                        statementPath = findStatement(statementPath);
                        
                        if (statementPath == null) {
                            //XXX: well....
                            return ;
                        }
                        
                        BlockTree statements = (BlockTree) statementPath.getParentPath().getLeaf();
                        StatementTree statement = (StatementTree) statementPath.getLeaf();
                        
                        int index = statements.getStatements().indexOf(statement);
                        
                        if (index == (-1)) {
                            //really strange...
                            return ;
                        }
                        
                        List<StatementTree> nueStatements = new LinkedList<StatementTree>(statements.getStatements());
                        
                        nueStatements.add(index, make.ExpressionStatement(make.Assignment(make.Identifier(name), expressionCopy)));
                        
                        BlockTree nueBlock = make.Block(nueStatements, false);
                        
                        parameter.rewrite(statements, nueBlock);
                    }
                    
                    if (initializeIn == IntroduceFieldPanel.INIT_CONSTRUCTORS) {
                        for (TreePath constructor : findConstructors(parameter, method)) {
                            //check for syntetic constructor:
                            if (parameter.getTreeUtilities().isSynthetic(constructor)) {
                                List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                                ExpressionTree reference = make.Identifier(name);
                                ModifiersTree constrMods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                                
                                nueStatements.add(make.ExpressionStatement(make.Assignment(reference, expressionCopy)));
                                
                                BlockTree nueBlock = make.Block(nueStatements, false);
                                MethodTree nueConstr = make.Method(constrMods, "<init>", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), nueBlock, null); //NOI18N
                                
                                nueClass = GeneratorUtils.insertClassMember(parameter, new TreePath(new TreePath(parameter.getCompilationUnit()), nueClass), nueConstr);
                                
                                nueClass = make.removeClassMember(nueClass, constructor.getLeaf());
                                break;
                            }
                            
                            boolean hasParameterOfTheSameName = false;
                            MethodTree constr = ((MethodTree) constructor.getLeaf());
                            
                            for (VariableTree p : constr.getParameters()) {
                                if (name.equals(p.getName().toString())) {
                                    hasParameterOfTheSameName = true;
                                    break;
                                }
                            }
                            
                            BlockTree origBody = constr.getBody();
                            List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                            ExpressionTree reference = hasParameterOfTheSameName ? make.MemberSelect(make.Identifier("this"), name) : make.Identifier(name); // NOI18N
                            
                            nueStatements.add(make.ExpressionStatement(make.Assignment(reference, expressionCopy)));
                            nueStatements.addAll(origBody.getStatements().subList(1, origBody.getStatements().size()));
                            
                            BlockTree nueBlock = make.Block(nueStatements, false);
                            
                            parameter.rewrite(origBody, nueBlock);
                        }
                    }
                    
                    parameter.rewrite(pathToClass.getLeaf(), nueClass);
                }
            }).commit();
            return null;
        }
    }
    
    private static final class IntroduceMethodFix implements Fix {

        private JavaSource js;
        
        private TreePathHandle parentBlock;
        private List<TypeMirrorHandle> parameterTypes;
        private List<String> parameterNames;
        private TypeMirrorHandle returnType;
        private String returnName;
        private boolean declareVariableForReturnValue;
        private Set<TypeMirrorHandle> thrownTypes;
        private List<TreePathHandle> exists;
        private boolean exitsFromAllBranches;
        private int from;
        private int to;

        public IntroduceMethodFix(JavaSource js, TreePathHandle parentBlock, List<TypeMirrorHandle> parameterTypes, List<String> parameterNames, TypeMirrorHandle returnType, String returnName, boolean declareVariableForReturnValue, Set<TypeMirrorHandle> thrownTypes, List<TreePathHandle> exists, boolean exitsFromAllBranches, int from, int to) {
            this.js = js;
            this.parentBlock = parentBlock;
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.returnType = returnType;
            this.returnName = returnName;
            this.declareVariableForReturnValue = declareVariableForReturnValue;
            this.thrownTypes = thrownTypes;
            this.exists = exists;
            this.exitsFromAllBranches = exitsFromAllBranches;
            this.from = from;
            this.to = to;
        }

        public String getText() {
            return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
        }

        public String toDebugString(CompilationInfo info) {
            return "[IntroduceMethod:" + from + ":" + to + "]"; // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            IntroduceMethodPanel panel = new IntroduceMethodPanel(""); //NOI18N
            String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceField");
            DialogDescriptor dd = new DialogDescriptor(panel, caption, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                return null;//cancel
            }
            final String name = panel.getMethodName();
            final Set<Modifier> access = panel.getAccess();
            
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {}
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(Phase.RESOLVED);
                    
                    TreePath block = parentBlock.resolve(copy);
                    TypeMirror returnType = IntroduceMethodFix.this.returnType.resolve(copy);
                    
                    if (block == null || returnType == null) {
                        return ; //TODO...
                    }
                    
                    BlockTree statements = (BlockTree) block.getLeaf();
                    List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                    
                    nueStatements.addAll(statements.getStatements().subList(0, from));
                    
                    List<ExpressionTree> realArguments = new LinkedList<ExpressionTree>();
                    final TreeMaker make = copy.getTreeMaker();
                    
                    for (String name : parameterNames) {
                        realArguments.add(make.Identifier(name));
                    }
                    
                    List<StatementTree> methodStatements = new LinkedList<StatementTree>(statements.getStatements().subList(from, to + 1));
                    Tree returnTypeTree = make.Type(returnType);
                    ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);
                    
                    ReturnTree ret = null;
                    
                    if (returnName != null) {
                        ret = make.Return(make.Identifier(returnName));
                        if (declareVariableForReturnValue) {
                            nueStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), returnName, returnTypeTree, invocation));
                            invocation = null;
                        } else {
                            invocation = make.Assignment(make.Identifier(returnName), invocation);
                        }
                    }
                    
                    if (!exists.isEmpty()) {
                        TreePath handle = null;
                        
                        handle = exists.iterator().next().resolve(copy);
                        
                        if (handle == null) {
                            return ; //TODO...
                        }
                        
                        assert handle != null;
                        
                        if (exitsFromAllBranches && handle.getLeaf().getKind() == Kind.RETURN) {
                            nueStatements.add(make.Return(invocation));
                        } else {
                            if (ret == null) {
                                if (exitsFromAllBranches) {
                                    ret = make.Return(null);
                                } else {
                                    ret = make.Return(make.Literal(true));
                                }
                            }
                            
                            for (TreePathHandle h : exists) {
                                TreePath resolved = h.resolve(copy);
                                
                                if (resolved == null) {
                                    return ; //TODO...
                                }
                                
                                copy.rewrite(resolved.getLeaf(), ret);
                            }
                            
                            StatementTree branch = null;
                            
                            switch (handle.getLeaf().getKind()) {
                                case BREAK:
                                    branch = make.Break(((BreakTree) handle.getLeaf()).getLabel());
                                    break;
                                case CONTINUE:
                                    branch = make.Continue(((ContinueTree) handle.getLeaf()).getLabel());
                                    break;
                                case RETURN:
                                    branch = make.Return(((ReturnTree) handle.getLeaf()).getExpression());
                                    break;
                            }
                            
                            if (returnName != null || exitsFromAllBranches) {
                                nueStatements.add(make.ExpressionStatement(invocation));
                                nueStatements.add(branch);
                            } else {
                                nueStatements.add(make.If(make.Parenthesized(invocation), branch, null));
                                methodStatements.add(make.Return(make.Literal(false)));
                            }
                        }
                        
                        invocation = null;
                    } else {
                        if (ret != null) {
                            methodStatements.add(ret);
                        }
                    }
                    
                    if (invocation != null)
                        nueStatements.add(make.ExpressionStatement(invocation));
                    
                    nueStatements.addAll(statements.getStatements().subList(to + 1, statements.getStatements().size()));
                    
                    ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                    List<VariableTree> formalArguments = new LinkedList<VariableTree>();
                    Iterator<TypeMirrorHandle> argType = parameterTypes.iterator();
                    Iterator<String> argName = parameterNames.iterator();
                    
                    while (argType.hasNext() && argName.hasNext()) {
                        TypeMirror tm = argType.next().resolve(copy);
                        
                        if (tm == null) {
                            //XXX:
                            return ;
                        }
                        
                        Tree type = make.Type(tm);
                        
                        formalArguments.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName.next(), type, null));
                    }
                    
                    List<ExpressionTree> thrown = new LinkedList<ExpressionTree>();
                    
                    for (TypeMirrorHandle h : thrownTypes) {
                        TypeMirror t = h.resolve(copy);
                        
                        if (t == null) {
                            return ;
                        }
                        
                        thrown.add((ExpressionTree) make.Type(t));
                    }
                    
                    MethodTree method = make.Method(mods, name, returnTypeTree, Collections.<TypeParameterTree>emptyList(), formalArguments, thrown, make.Block(methodStatements, false), null);
                    
                    TreePath pathToClass = findClass(block);
                    
                    assert pathToClass != null;
                    
                    ClassTree nueClass = GeneratorUtils.insertClassMember(copy, pathToClass, method);
                    
                    copy.rewrite(pathToClass.getLeaf(), nueClass);
                    copy.rewrite(statements, make.Block(nueStatements, statements.isStatic()));
                }
            }).commit();
            
            return null;
        }
        
    }
}
