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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Lahoda
 */
public class SemanticHighlighter extends ScanningCancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of SemanticHighlighter */
    SemanticHighlighter(FileObject file) {
        this.file = file;
    }

    public Document getDocument() {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (IOException e) {
            Logger.getLogger(SemanticHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e);
            return null;
        }
    }
    
    public @Override void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();

        if (doc == null) {
            Logger.getLogger(SemanticHighlighter.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }

        Set<Highlight> highlights = process(info, doc);
        
        if (isCancelled())
            return;
        
        Highlighter.getDefault().setHighlights(file, "semantic", highlights);
        OccurrencesMarkProvider.get(doc).setSematic(highlights);
    }
    
    private void removeImport(Document doc, int start, int end) {
        try {
            int len = doc.getLength();
            
            while (start > 0 && "\t ".indexOf(doc.getText(start - 1, 1).charAt(0)) != (-1))
                start--;
            
            boolean wasNewLine = start == 0 || "\n".equals(doc.getText(start - 1, 1));
            
            while (end < len && "\t ".indexOf(doc.getText(end, 1).charAt(0)) != (-1))
                end++;
            
            if (wasNewLine && "\n".equals(doc.getText(end, 1)))
                end++;
            
            doc.remove(start, end - start);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private static class FixAllImportsFixList implements LazyFixList {
        private Fix removeImport;
        private Fix removeAllUnusedImports;
        private List<Fix> allRemoveImportFixes;
        
        public FixAllImportsFixList(Fix removeImport, Fix removeAllUnusedImports, List<Fix> allRemoveImportFixes) {
            this.removeImport = removeImport;
            this.removeAllUnusedImports = removeAllUnusedImports;
            this.allRemoveImportFixes = allRemoveImportFixes;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public boolean probablyContainsFixes() {
            return true;
        }
        
        private List<Fix> fixes;
        
        public synchronized List<Fix> getFixes() {
            if (fixes != null)
                return fixes;
            
            if (allRemoveImportFixes.size() > 1) {
                fixes = Arrays.asList(removeImport, removeAllUnusedImports);
            } else {
                fixes = Collections.singletonList(removeImport);
            }
            
            return fixes;
        }
        
        public boolean isComputed() {
            return true;
        }
    }
    
    Set<Highlight> process(CompilationInfo info, final Document doc) {
        DetectorVisitor v = new DetectorVisitor(info, doc);
        
        long start = System.currentTimeMillis();
        
        Set<Highlight> result = new HashSet();
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();

        CompilationUnitTree cu = info.getCompilationUnit();
        
        scan(v, cu, null);
        
        if (isCancelled())
            return Collections.emptySet();
        
        final List<Fix> allRemoveImportFixes = new ArrayList<Fix>();
        final Fix removeAllUnusedImports = new Fix() {
            public String getText() {
                return "Remove all unused imports.";
            }

            public ChangeInfo implement() {
                BaseDocument bdoc = doc instanceof BaseDocument ? (BaseDocument) doc : null;
                
                if (bdoc != null)
                    bdoc.atomicLock();
                
                try {
                    for (Fix f : allRemoveImportFixes) {
                        f.implement();
                    }
                } finally {
                    if (bdoc != null)
                        bdoc.atomicUnlock();
                }
                
                return null;
            }
        };
        
        for (Element el : v.type2Highlight.keySet()) {
            if (isCancelled())
                return Collections.emptySet();

            final TreePath tree = v.type2Highlight.get(el);
            
            if (el == null || el.getSimpleName() == null)
                continue;
            
            Highlight h = Utilities.createHighlight(cu, info.getTrees().getSourcePositions(), doc, tree, EnumSet.of(ColoringAttributes.UNUSED), Color.GRAY);
            
            if (h != null) {
                result.add(h);
            }
            
            final int startPos = (int)info.getTrees().getSourcePositions().getStartPosition(cu, tree.getLeaf());
            final int endPos   = (int)info.getTrees().getSourcePositions().getEndPosition(cu, tree.getLeaf());
            int line     = NbDocument.findLineNumber((StyledDocument) doc, startPos) + 1;
            
            //XXX: will provide incorrect results if the document is already changed:
            final Position[] removeRange = new Position[2];
            final boolean[]  ignore = new boolean[1];
            
            doc.render(new Runnable() {
                public void run() {
                    try {
                        removeRange[0] = doc.createPosition(startPos);
                        removeRange[1] = doc.createPosition(endPos);
                    } catch (BadLocationException e) {
                        ignore[0] = true;
                    }
                }
            });
            
            if (!ignore[0]) {
                final Fix removeImport = new Fix() {
                    public String getText() {
                        return "Remove unused import";
                    }
                    public ChangeInfo implement() {
                        NbDocument.runAtomic((StyledDocument) doc, new Runnable() {
                            public void run() {
                                removeImport(doc, removeRange[0].getOffset(), removeRange[1].getOffset());
                            }
                        });
                        return null;
                    }
                };
                
                allRemoveImportFixes.add(removeImport);
                errors.add(ErrorDescriptionFactory.createErrorDescription(Severity.VERIFIER, "Unused import", new FixAllImportsFixList(removeImport, removeAllUnusedImports, allRemoveImportFixes), doc, line));
            }
        }
        
        for (Element decl : v.type2Uses.keySet()) {
            if (isCancelled())
                return Collections.emptySet();
            
            List<Use> uses = v.type2Uses.get(decl);
            
            for (Use u : uses) {
                if (u.spec == null)
                    continue;
                
                if (u.type.contains(UseTypes.Element) && org.netbeans.modules.java.editor.semantic.Utilities.isPrivateElement(decl)) {
                    if (decl.getKind().isField() || isLocalVariableClosure(decl)) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.READ, UseTypes.WRITE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if (decl.getKind() == ElementKind.CONSTRUCTOR || decl.getKind() == ElementKind.METHOD) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.EXECUTE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if (decl.getKind().isClass() || decl.getKind().isInterface()) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.CLASS_USE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                }
                
                Collection<ColoringAttributes> c = EnumSet.copyOf(u.spec);
                Highlight h = Utilities.createHighlight(cu, info.getTrees().getSourcePositions(), doc, u.tree, c, null);
                
                if (h != null) {
                    result.add(h);
                }
            }
        }
            
        if (isCancelled())
            return Collections.emptySet();
        
        ERROR_DESCRIPTION_SETTER.setErrors(doc, errors);
        
        TimesCollector.getDefault().reportTime(((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), "semantic", "Semantic", (System.currentTimeMillis() - start));
        
        return result;
    }
    
    private boolean hasAllTypes(List<Use> uses, Collection<UseTypes> types) {
        EnumSet e = EnumSet.copyOf(types);
        
        for (Use u : uses) {
            if (types.isEmpty()) {
                return true;
            }
            
            types.removeAll(u.type);
        }
        
        return types.isEmpty();
    }
    
    private enum UseTypes {
        READ, WRITE, EXECUTE, Element, CLASS_USE;
    }
    
    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.EXCEPTION_PARAMETER;
    }
        
    private static class Use {
        private Collection<UseTypes> type;
        private TreePath     tree;
        private Collection<ColoringAttributes> spec;
        
        public Use(Collection<UseTypes> type, TreePath tree, Collection<ColoringAttributes> spec) {
            this.type = type;
            this.tree = tree;
            this.spec = spec;
        }
        
        public String toString() {
            return "Use: " + type;
        }
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, EnumSet<UseTypes>> {
        
        private org.netbeans.api.java.source.CompilationInfo info;
        private Document doc;
        private Map<Element, List<Use>> type2Uses;
        private Map<Element, TreePath/*ImportTree*/> type2Highlight;
//        private Set<Highlight> highlights;
        
//        private int pos;
        private SourcePositions sourcePositions;
        
        private DetectorVisitor(org.netbeans.api.java.source.CompilationInfo info, Document doc/*, int pos*/) {
            this.info = info;
            this.doc  = doc;
            type2Uses = new HashMap<Element, List<Use>>();
            this.type2Highlight = new HashMap<Element, TreePath/*ImportTree*/>();
//            this.pos = pos;
        }
        
        private Highlight createHighlight(CompilationUnitTree cu, SourcePositions sp, TreePath tree, Collection<ColoringAttributes> c, Color es) {
            return Utilities.createHighlight(cu, sp, doc, tree, c, es);
        }
        
        @Override
        public Void visitAssignment(AssignmentTree tree, EnumSet<UseTypes> d) {
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getVariable()), EnumSet.of(UseTypes.WRITE));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), null);
            
            return super.visitAssignment(tree, null);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree tree, EnumSet<UseTypes> d) {
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getVariable()), EnumSet.of(UseTypes.WRITE));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getExpression(), null);
            
            return super.visitCompoundAssignment(tree, null);
        }

        @Override
        public Void visitReturn(ReturnTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            
            super.visitReturn(tree, null);
            return null;
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, EnumSet<UseTypes> d) {
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), expr);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind().isField()) {
                handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.READ));
            }
//            System.err.println("XXXX=" + tree.toString());
//            System.err.println("YYYY=" + info.getElement(tree));
            
            super.visitMemberSelect(tree, null);
            return null;
        }
        
        private void addModifiers(Element decl, Collection<ColoringAttributes> c) {
            if (decl.getModifiers().contains(Modifier.STATIC)) {
                c.add(ColoringAttributes.STATIC);
            }
            
            if (decl.getModifiers().contains(Modifier.ABSTRACT)) {
                c.add(ColoringAttributes.ABSTRACT);
            }
            
            boolean accessModifier = false;
            
            if (decl.getModifiers().contains(Modifier.PUBLIC)) {
                c.add(ColoringAttributes.PUBLIC);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PROTECTED)) {
                c.add(ColoringAttributes.PROTECTED);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PRIVATE)) {
                c.add(ColoringAttributes.PRIVATE);
                accessModifier = true;
            }
            
            if (!accessModifier && !isLocalVariableClosure(decl)) {
                c.add(ColoringAttributes.PACKAGE_PRIVATE);
            }
            
            if (info.getElements().isDeprecated(decl)) {
                c.add(ColoringAttributes.DEPRECATED);
            }
        }
        
        private Collection<ColoringAttributes> getMethodColoring(ExecutableElement mdecl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(mdecl, c);
            
            if (mdecl.getKind() == ElementKind.CONSTRUCTOR)
                c.add(ColoringAttributes.CONSTRUCTOR);
            else
                c.add(ColoringAttributes.METHOD);
            
            return c;
        }
        
        private Collection<ColoringAttributes> getVariableColoring(Element decl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(decl, c);
            
            if (decl.getKind().isField()) {
                c.add(ColoringAttributes.FIELD);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.LOCAL_VARIABLE || decl.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                c.add(ColoringAttributes.LOCAL_VARIABLE);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.PARAMETER) {
                c.add(ColoringAttributes.PARAMETER);
                
                return c;
            }
            
            assert false;
            
            return null;
        }

        private static final Set<Kind> LITERALS = EnumSet.of(Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.DOUBLE_LITERAL, Kind.FLOAT_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.STRING_LITERAL);

        private void handlePossibleIdentifier(TreePath expr, Collection<UseTypes> type) {
            handlePossibleIdentifier(expr, type, null, false);
        }
        
        private void handlePossibleIdentifier(TreePath expr, Collection<UseTypes> type, Element decl, boolean providesDecl) {
            
            if (Utilities.isKeyword(expr.getLeaf())) {
                //ignore keywords:
                return ;
            }

            if (expr.getLeaf().getKind() == Kind.PRIMITIVE_TYPE) {
                //ignore primitive types:
                return ;
            }

            if (LITERALS.contains(expr.getLeaf().getKind())) {
                //ignore literals:
                return ;
            }

            decl = !providesDecl ? info.getTrees().getElement(expr) : decl;
            
            Collection<ColoringAttributes> c = null;
            
            //causes NPE later, as decl is put into list of declarations to handle:
//            if (decl == null) {
//                c = Collections.singletonList(ColoringAttributes.UNDEFINED);
//            }
            
            if (decl != null && (decl.getKind().isField() || isLocalVariableClosure(decl))) {
                c = getVariableColoring(decl);
            }
            
            if (decl != null && decl instanceof ExecutableElement) {
                c = getMethodColoring((ExecutableElement) decl);
            }
            
            if (decl != null && (decl.getKind().isClass() || decl.getKind().isInterface())) {
                //class use make look like read variable access:
                if (type.contains(UseTypes.READ)) {
                    type.remove(UseTypes.READ);
                    type.add(UseTypes.CLASS_USE);
                }
                
                c = new ArrayList<ColoringAttributes>();
                
                addModifiers(decl, c);
            }
            
            if (c != null) {
                addUse(decl, type, expr, c);
            }
        }
        
        private void addUse(Element decl, Collection<UseTypes> useTypes, TreePath t, Collection<ColoringAttributes> c) {
            List<Use> uses = type2Uses.get(decl);
            
            if (uses == null) {
                type2Uses.put(decl, uses = new ArrayList<Use>());
            }
            
            Use u = new Use(useTypes, t, c);
            
            uses.add(u);
        }

        @Override
        public Void visitTypeCast(TypeCastTree tree, EnumSet<UseTypes> d) {
            resolveType(new TreePath(getCurrentPath(), tree.getType()));
            
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            super.visitTypeCast(tree, d);
            return null;
        }

        @Override
        public Void visitInstanceOf(InstanceOfTree tree, EnumSet<UseTypes> d) {
            Tree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            TreePath tp = new TreePath(getCurrentPath(), tree.getType());
            resolveType(tp);
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            
            super.visitInstanceOf(tree, null);
            
            //TODO: should be considered
            return null;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, EnumSet<UseTypes> d) {
	    scan(tree.getPackageAnnotations(), d);
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
	    scan(tree.getImports(), d);
	    scan(tree.getTypeDecls(), d);
	    return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, EnumSet<UseTypes> d) {
            Tree possibleIdent = tree.getMethodSelect();
            boolean handled = false;
            
            if (possibleIdent.getKind() == Kind.IDENTIFIER) {
                //handle "this" and "super" constructors:
                String ident = ((IdentifierTree) possibleIdent).getName().toString();
                
                if ("super".equals(ident) || "this".equals(ident)) { //NOI18N
                    Element resolved = info.getTrees().getElement(getCurrentPath());
                    
                    addUse(resolved, EnumSet.of(UseTypes.EXECUTE), null, null);
                    handled = true;
                }
            }
            
            if (!handled) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), possibleIdent), EnumSet.of(UseTypes.EXECUTE));
            }
            
            for (Tree expr : tree.getArguments()) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                }
            }
            for (Tree expr : tree.getTypeArguments()) {
                if (expr instanceof IdentifierTree) {
                    resolveType(new TreePath(getCurrentPath(), expr));
                }
            }
            
            super.visitMethodInvocation(tree, null);
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, EnumSet<UseTypes> d) {
//            if ("l".equals(tree.toString())) {
//                Thread.dumpStack();
//            }
//            handlePossibleIdentifier(tree);
//            //also possible type: (like in Collections.EMPTY_LIST):
//            resolveType(tree);
//            Thread.dumpStack();
            
            if (d != null) {
                handlePossibleIdentifier(getCurrentPath(), d);
            }
            super.visitIdentifier(tree, null);
            return null;
        }
//
        @Override
        public Void visitMethod(MethodTree tree, EnumSet<UseTypes> d) {
//            Element decl = pi.getAttribution().getElement(tree);
//            
//            if (decl != null) {
//                assert decl instanceof ExecutableElement;
//                
//                Coloring c = getMethodColoring((ExecutableElement) decl);
//                HighlightImpl h = createHighlight(decl.getSimpleName(), tree, c, null);
//                
//                if (h != null) {
//                    highlights.add(h);
//                }
//            }
            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.Element));
            
            for (Tree t : tree.getThrows()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            if (tree.getReturnType() != null)
                resolveType(new TreePath(getCurrentPath(), tree.getReturnType()));
           
            EnumSet<UseTypes> paramsUseTypes;
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getModifiers().contains(Modifier.ABSTRACT)) {
                paramsUseTypes = EnumSet.of(UseTypes.WRITE, UseTypes.READ);
            } else {
                paramsUseTypes = EnumSet.of(UseTypes.WRITE);
            }
        
            scan(tree.getModifiers(), null);
            scan(tree.getReturnType(), EnumSet.of(UseTypes.CLASS_USE));
            scan(tree.getTypeParameters(), null);
            scan(tree.getParameters(), paramsUseTypes);
            scan(tree.getThrows(), null);
            scan(tree.getBody(), null);
        
            return null;
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree tree, EnumSet<UseTypes> d) {
//            if (tree instanceof IdentifierTree) {
//                handlePossibleIdentifier(tree, EnumSet.of(UseTypes.READ));
//            }
            
            super.visitExpressionStatement(tree, null);
            return null;
        }

        @Override
        public Void visitParenthesized(ParenthesizedTree tree, EnumSet<UseTypes> d) {
            ExpressionTree expr = tree.getExpression();
            
            if (expr instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
            }
            
            super.visitParenthesized(tree, null);
            return null;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree tree, EnumSet<UseTypes> d) {
            scan(tree.getVariable(), EnumSet.of(UseTypes.WRITE));
            
            if (tree.getExpression().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            
            scan(tree.getExpression(), null);
            scan(tree.getStatement(), null);
            
            return null;
        }
        
        @Override
        public Void visitImport(ImportTree tree, EnumSet<UseTypes> d) {
            if (!tree.isStatic()) {
                Element decl = info.getTrees().getElement(new TreePath(getCurrentPath(), tree.getQualifiedIdentifier()));
                
                if (decl != null && decl.asType().getKind() != TypeKind.ERROR) { //unresolvable imports should not be marked as unused
                    type2Highlight.put(decl, getCurrentPath());
                }
//                } else {
//                    //cannot handle package import for now.
//                    //cannot handle static imports for now.
//                }
            }
            super.visitImport(tree, null);
            return null;
        }
        
        private String getSimple(String fqn) {
            int lastDot = fqn.lastIndexOf('.');
            
            if (lastDot != (-1)) {
                return fqn.substring(lastDot + 1);
            } else {
                return fqn;
            }
        }
        
        @Override
        public Void visitVariable(VariableTree tree, EnumSet<UseTypes> d) {
            TreePath type = new TreePath(getCurrentPath(), tree.getType());
            
            if (type.getLeaf() instanceof ArrayTypeTree) {
                type = new TreePath(type, ((ArrayTypeTree) type.getLeaf()).getType());
            }
            
            resolveType(type);
            
            if (type.getLeaf().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(type, EnumSet.of(UseTypes.CLASS_USE));
            
            Collection<UseTypes> uses = null;
            boolean isParameter = false;
            
            if (tree.getInitializer() != null) {
                uses = EnumSet.of(UseTypes.Element, UseTypes.WRITE);
                if (tree.getInitializer().getKind() == Kind.IDENTIFIER)
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getInitializer()), EnumSet.of(UseTypes.READ));
            } else {
                Element e = info.getTrees().getElement(getCurrentPath());
                
                if (e != null && e.getKind() == ElementKind.FIELD) {
                    uses = EnumSet.of(UseTypes.Element, UseTypes.WRITE);
                } else {
                    uses = EnumSet.of(UseTypes.Element);
                }
            }
            
            if (d != null) {
                Set<UseTypes> ut = new HashSet();
                
                ut.addAll(uses);
                ut.addAll(d);
                
                uses = EnumSet.copyOf(ut);
            }
            
            handlePossibleIdentifier(getCurrentPath(), uses);
            
            super.visitVariable(tree, null);
            return null;
        }
        
        private boolean isParameter(VariableTree var, MethodTree decl) {
            for (VariableTree declVar : decl.getParameters()) {
                if (var == declVar)
                    return true;
            }
            
            return false;
        }
        
        @Override
        public Void visitAnnotation(AnnotationTree tree, EnumSet<UseTypes> d) {
//            System.err.println("tree.getType()= " + tree.toString());
//            System.err.println("tree.getType()= " + tree.getClass());
//        
            TreePath tp = new TreePath(getCurrentPath(), tree.getAnnotationType());
            resolveType(tp);
            handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            super.visitAnnotation(tree, EnumSet.noneOf(UseTypes.class));
            //TODO: maybe should be considered
            return null;
        }

        @Override
        public Void visitNewClass(NewClassTree tree, EnumSet<UseTypes> d) {
            TreePath tp;
            Tree ident = tree.getIdentifier();
            
            if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                tp = new TreePath(new TreePath(getCurrentPath(), ident), ((ParameterizedTypeTree) ident).getType());
            } else {
                tp = new TreePath(getCurrentPath(), ident);
            }
            
            resolveType(tp);
	    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.EXECUTE), info.getTrees().getElement(getCurrentPath()), true);
            
            Element clazz = info.getTrees().getElement(tp);
            
            if (clazz != null) {
                addUse(clazz, EnumSet.of(UseTypes.CLASS_USE), null, null);
            }
	    
            for (Tree expr : tree.getArguments()) {
                if (expr instanceof IdentifierTree) {
                    handlePossibleIdentifier(new TreePath(getCurrentPath(), expr), EnumSet.of(UseTypes.READ));
                }
            }
            
            super.visitNewClass(tree, null);
            
            return null;
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree tree, EnumSet<UseTypes> d) {
            if (getCurrentPath().getParentPath().getLeaf().getKind() != Kind.NEW_CLASS) {
                //NewClass has already been handled as part of visitNewClass:
                TreePath tp = new TreePath(getCurrentPath(), tree.getType());
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (Tree t : tree.getTypeArguments()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                
//                HighlightImpl h = createHighlight("", t, TYPE_PARAMETER);
//                
//                if (h != null)
//                    highlights.add(h);
            }
            
            super.visitParameterizedType(tree, null);
            return null;
        }

        @Override
        public Void visitBinary(BinaryTree tree, EnumSet<UseTypes> d) {
            Tree left = tree.getLeftOperand();
            Tree right = tree.getRightOperand();
            
            if (left instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), left);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            if (right instanceof IdentifierTree) {
                TreePath tp = new TreePath(getCurrentPath(), right);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.READ));
            }
            
            super.visitBinary(tree, null);
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, EnumSet<UseTypes> d) {
            Tree extnds = tree.getExtendsClause();
            
            if (extnds != null) {
                TreePath tp = new TreePath(getCurrentPath(), extnds);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (Tree t : tree.getImplementsClause()) {
                TreePath tp = new TreePath(getCurrentPath(), t);
                resolveType(tp);
                handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
            }
            
            for (TypeParameterTree t : tree.getTypeParameters()) {
                for (Tree bound : t.getBounds()) {
                    TreePath tp = new TreePath(new TreePath(getCurrentPath(), t), bound);
                    resolveType(tp);
                    handlePossibleIdentifier(tp, EnumSet.of(UseTypes.CLASS_USE));
                }
            }
            
            handlePossibleIdentifier(getCurrentPath(), EnumSet.of(UseTypes.Element));
            
            super.visitClass(tree, null);
            //TODO: maybe should be considered
            return null;
        }
        
        @Override
        public Void visitUnary(UnaryTree tree, EnumSet<UseTypes> d) {
            if (tree.getExpression() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            }
            super.visitUnary(tree, d);
            return null;
        }

        @Override
        public Void visitArrayAccess(ArrayAccessTree tree, EnumSet<UseTypes> d) {
            handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getExpression()), EnumSet.of(UseTypes.READ));
            
            if (tree.getIndex() instanceof IdentifierTree) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getIndex()), EnumSet.of(UseTypes.READ));
            }
            
            super.visitArrayAccess(tree, null);
            return null;
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, EnumSet<UseTypes> d) {
            if (tree.getType() != null)
                resolveType(new TreePath(getCurrentPath(), tree.getType()));
            
            scan(tree.getType(), null);
            scan(tree.getDimensions(), EnumSet.of(UseTypes.READ));
            scan(tree.getInitializers(), EnumSet.of(UseTypes.READ));
            
            return null;
        }
        
        @Override
        public Void visitCatch(CatchTree tree, EnumSet<UseTypes> d) {
            scan(tree.getParameter(), EnumSet.of(UseTypes.WRITE));
            scan(tree.getBlock(), null);
            return null;
        }

        @Override
        public Void visitConditionalExpression(ConditionalExpressionTree node, EnumSet<UseTypes> p) {
            return super.visitConditionalExpression(node, EnumSet.of(UseTypes.READ));
        }
        
        @Override
        public Void visitAssert(AssertTree tree, EnumSet<UseTypes> p) {
            if (tree.getCondition().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getCondition()), EnumSet.of(UseTypes.READ));
            if (tree.getDetail() != null && tree.getDetail().getKind() == Kind.IDENTIFIER)
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getDetail()), EnumSet.of(UseTypes.READ));
            
            return super.visitAssert(tree, null);
        }
        
        private void resolveType(TreePath type) {
            FirstIdentTypeVisitor v = new FirstIdentTypeVisitor();
            
            v.scan(type, null);
            
            if (v.first == null)
                return ;
            
            Element decl = info.getTrees().getElement(v.first);
            
            if (decl == null) {
//                System.err.println("Warning: type=" + type);
//                System.err.println("decl=" + decl);
                
                return ;
            }
            
            type2Highlight.remove(decl);
        }
        
        private static class FirstIdentTypeVisitor extends TreePathScanner<Void, Void> {
            private TreePath first = null;
            
            public Void visitIdentifier(IdentifierTree tree, Void d) {
                if (first == null) {
                    first = getCurrentPath();
                }
                
                return super.visitIdentifier(tree, null);
            }
            
        }
    }
    
    public static interface ErrorDescriptionSetter {
        
        public void setErrors(Document doc, List<ErrorDescription> errors);
        
    }
    
    static ErrorDescriptionSetter ERROR_DESCRIPTION_SETTER = new ErrorDescriptionSetter() {
        
        public void setErrors(Document doc, List<ErrorDescription> errors) {
            HintsController.setErrors(doc, "semantic-highlighter", errors);
        }
        
    };

}
