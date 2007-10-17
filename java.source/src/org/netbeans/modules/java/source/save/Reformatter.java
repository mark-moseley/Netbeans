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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCModifiers;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Name;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.ReformatTask;

/**
 *
 * @author Dusan Balek
 */
public class Reformatter implements ReformatTask {
    
    private JavaSource javaSource;
    private Context context;
    private CompilationController controller;
    private Document doc;
    private int shift;

    public Reformatter(JavaSource javaSource, Context context) {
        this.javaSource = javaSource;
        this.context = context;
        this.doc = context.document();
    }

    public void reformat() throws BadLocationException {
        if (controller == null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.PARSED);
                        Reformatter.this.controller = controller;
                    }
                }, true);            
            } catch (IOException ioe) {
                JavaSourceAccessor.INSTANCE.unlockJavaCompiler();
            }
            if (controller == null)
                return;
        }
        for (Context.Region region : context.indentRegions())
            reformatImpl(region);
    }
    
    private void reformatImpl(Context.Region region) throws BadLocationException {
        int startOffset = region.getStartOffset() - shift;
        int endOffset = region.getEndOffset() - shift;
        int originalEndOffset = endOffset;
        PositionConverter converter = controller.getPositionConverter();
        if (converter != null) {
            startOffset = converter.getJavaSourcePosition(startOffset);
            endOffset = converter.getJavaSourcePosition(endOffset);
        }
        if (!"text/x-java".equals(context.mimePath())) { //NOI18N
            TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            if (ts != null) {
                ts.move(endOffset);
                if (ts.moveNext() && ts.token().id() == WHITESPACE) {
                    String t = ts.token().text().toString();
                    int i = t.lastIndexOf('\n'); //NOI18N
                    if (i >= 0)
                        endOffset -= (t.length() - i);
                }
            }
        }
        if (startOffset >= endOffset)
            return;
        TreePath path = getCommonPath(startOffset, endOffset);
        if (path == null)
            return;
        for (Diff diff : Pretty.reformat(controller, path, CodeStyle.getDefault(null))) {
            int start = diff.getStartOffset();
            int end = diff.getEndOffset();
            if (startOffset >= end || endOffset <= start)
                continue;
            if (startOffset > start)
                start = startOffset;
            if (endOffset < end)
                end = endOffset;
            if (converter != null) {
                start = converter.getOriginalPosition(start);
                end = converter.getOriginalPosition(end);
            }
            start += shift;
            end += shift;
            doc.remove(start, end - start);
            String text = diff.getText();
            if (text != null && text.length() > 0)
                doc.insertString(start, text, null);
        }
        shift = region.getEndOffset() - originalEndOffset;
        return;
    }
    
    public ExtraLock reformatLock() {
        return new Lock();
    }
    
    private TreePath getCommonPath(int startOffset, int endOffset) {
        TreeUtilities tu = controller.getTreeUtilities();
        TreePath startPath = tu.pathFor(startOffset);
        com.sun.tools.javac.util.List<Tree> reverseStartPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : startPath)
            reverseStartPath = reverseStartPath.prepend(t);
        TreePath endPath = tu.pathFor(endOffset);
        com.sun.tools.javac.util.List<Tree> reverseEndPath = com.sun.tools.javac.util.List.<Tree>nil();
        for (Tree t : endPath)
            reverseEndPath = reverseEndPath.prepend(t);
        TreePath path = null;
        while(reverseStartPath.head != null && reverseStartPath.head == reverseEndPath.head) {
            path = reverseStartPath.head instanceof CompilationUnitTree ? new TreePath((CompilationUnitTree)reverseStartPath.head) : new TreePath(path, reverseStartPath.head);
            reverseStartPath = reverseStartPath.tail;
            reverseEndPath = reverseEndPath.tail;
        }
        return path;
    }
    
    private class Lock implements ExtraLock {

        public void lock() {
            JavaSourceAccessor.INSTANCE.lockJavaCompiler();
        }

        public void unlock() {
            controller = null;
            JavaSourceAccessor.INSTANCE.unlockJavaCompiler();
        }        
    }
    
    public static class Factory implements ReformatTask.Factory {

        public ReformatTask createTask(Context context) {
            JavaSource js = JavaSource.forDocument(context.document());
            return js != null ? new Reformatter(js, context) : null;
        }        
    }

    private static class Pretty extends TreePathScanner<Boolean, Void> {

        private static final String OPERATOR = "operator"; //NOI18N
        private static final String EMPTY = ""; //NOI18N
        private static final String SPACE = " "; //NOI18N
        private static final String NEWLINE = "\n"; //NOI18N
        private static final String ERROR = "<error>"; //NOI18N
        private static final int ANY_COUNT = -1;

        private final CompilationInfo info;
        private final CodeStyle cs;

        private final int rightMargin;
        private final int tabSize;
        private final int indentSize;
        private final int continuationIndentSize;
        private final boolean expandTabToSpaces;

        private TokenSequence<JavaTokenId> tokens;    
        private int indent;
        private int col;
        private int endPos;
        private int wrapDepth;
        private int lastBlankLines;
        private int lastBlankLinesTokenIndex;
        private boolean afterAnnotation;
        private boolean fieldGroup;
        private LinkedList<Diff> diffs = new LinkedList<Diff>();

        private Pretty(CompilationInfo info, TreePath path, CodeStyle cs) {
            this.info = info;
            this.cs = cs;
            this.rightMargin = cs.getRightMargin();
            this.tabSize = cs.getTabSize();
            this.indentSize = cs.getIndentSize();
            this.continuationIndentSize = cs.getContinuationIndentSize();
            this.expandTabToSpaces =  cs.expandTabToSpaces();
            this.wrapDepth = 0;
            this.lastBlankLines = -1;
            this.lastBlankLinesTokenIndex = -1;
            this.afterAnnotation = false;
            this.fieldGroup = false;
            Tree tree = path.getLeaf();
            this.indent = getIndentLevel(path);
            this.col = this.indent;
            this.tokens = tree.getKind() == Tree.Kind.COMPILATION_UNIT
                    ? info.getTokenHierarchy().tokenSequence(JavaTokenId.language())
                    : info.getTreeUtilities().tokensFor(tree);
            tokens.moveEnd();
            tokens.movePrevious();
            this.endPos = tokens.offset();
            tokens.moveStart();
            tokens.moveNext();
        }

        public static LinkedList<Diff> reformat(CompilationInfo info, TreePath path, CodeStyle cs) {
            Pretty pretty = new Pretty(info, path, cs);
            if (pretty.indent >= 0)
                pretty.scan(path, null);
            return pretty.diffs;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            int lastEndPos = endPos;
            if (tree != null && tree.getKind() != Tree.Kind.COMPILATION_UNIT) {
                if (tree instanceof FakeBlock)
                    endPos = (int)info.getTrees().getSourcePositions().getEndPosition(getCurrentPath().getCompilationUnit(), ((FakeBlock)tree).stat);
                else
                    endPos = (int)info.getTrees().getSourcePositions().getEndPosition(getCurrentPath().getCompilationUnit(), tree);
            }
            try {
                return endPos < 0 ? false : tokens.offset() <= endPos ? super.scan(tree, p) : true;
            }
            finally {
                endPos = lastEndPos;
            }
        }

        @Override
        public Boolean visitCompilationUnit(CompilationUnitTree node, Void p) {
            ExpressionTree pkg = node.getPackageName();
            if (pkg != null) {
                blankLines(cs.getBlankLinesBeforePackage());
                accept(PACKAGE);
                int old = indent;
                indent += continuationIndentSize;
                space();
                scan(pkg, p);
                accept(SEMICOLON);
                indent = old;
                blankLines(cs.getBlankLinesAfterPackage());
            }
            List<? extends ImportTree> imports = node.getImports();
            if (imports != null && !imports.isEmpty()) {
                blankLines(cs.getBlankLinesBeforeImports());
                for (ImportTree imp : imports) {
                    blankLines();
                    scan(imp, p);
                }
                blankLines(cs.getBlankLinesAfterImports());
            }
            for (Tree typeDecl : node.getTypeDecls()) {
                blankLines(cs.getBlankLinesBeforeClass());
                scan(typeDecl, p);
                blankLines(cs.getBlankLinesAfterClass());
            }
            return true;
        }

        @Override
        public Boolean visitImport(ImportTree node, Void p) {
            accept(IMPORT);
            int old = indent;
            indent += continuationIndentSize;
            space();
            if (node.isStatic()) {
                accept(STATIC);
                space();
            }
            scan(node.getQualifiedIdentifier(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            if (getCurrentPath().getParentPath().getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
                int old = indent;
                ModifiersTree mods = node.getModifiers();
                if (mods != null) {
                    if (scan(mods, p)) {
                        indent += continuationIndentSize;
                        space();
                    } else if (afterAnnotation) {
                        blankLines();
                    }
                }
                JavaTokenId id = accept(CLASS, INTERFACE, ENUM, AT);
                if (indent == old)
                    indent += continuationIndentSize;
                if (id == AT)
                    accept(INTERFACE);
                space();
                if (!ERROR.contentEquals(node.getSimpleName()))
                    accept(IDENTIFIER);
                List<? extends TypeParameterTree> tparams = node.getTypeParameters();
                if (tparams != null && !tparams.isEmpty()) {
                    accept(LT);
                    for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                        TypeParameterTree tparam = it.next();
                        scan(tparam, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    accept(GT, GTGT, GTGTGT);
                }
                Tree ext = node.getExtendsClause();
                if (ext != null) {
                    wrapToken(cs.wrapExtendsImplementsKeyword(), 1, EXTENDS);
                    space();
                    scan(ext, p);
                }
                List<? extends Tree> impls = node.getImplementsClause();
                if (impls != null && !impls.isEmpty()) {
                    wrapToken(cs.wrapExtendsImplementsKeyword(), 1, id == INTERFACE ? EXTENDS : IMPLEMENTS);
                    spaces(1, true);
                    wrapList(cs.wrapExtendsImplementsList(), cs.alignMultilineImplements(), impls);
                }
                indent = old;
            }
            CodeStyle.BracePlacement bracePlacement = cs.getClassDeclBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeClassDeclLeftBrace();
            int old = indent;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    accept(LBRACE);
                    indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    indent += indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    indent = old + indentSize;
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    break;
            }
            boolean emptyClass = true;
            for (Tree member : node.getMembers()) {
                if (!info.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), member))) {
                    emptyClass = false;
                    break;
                }
            }
            if (emptyClass) {
                newline();
            } else {
                if (!cs.indentTopLevelClassMembers())
                    indent = old;
                blankLines(cs.getBlankLinesAfterClassHeader());
                JavaTokenId id = null;
                for (Tree member : node.getMembers()) {
                    if (!info.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), member))) {
                        switch(member.getKind()) {
                            case VARIABLE:
                                if (isEnumerator((VariableTree)member)) {
                                    wrapTree(cs.wrapEnumConstants(), id == COMMA ? 1 : 0, member);
                                    int index = tokens.index();
                                    int col = this.col;
                                    id = accept(COMMA, SEMICOLON);
                                    if (id == null) {
                                        rollback(index, col);
                                        blankLines(cs.getBlankLinesAfterFields());
                                    } else if (id == SEMICOLON) {
                                        blankLines(cs.getBlankLinesAfterFields());
                                    }
                                } else {
                                    if (!fieldGroup)
                                        blankLines(cs.getBlankLinesBeforeFields());
                                    scan(member, p);
                                    if(!fieldGroup)
                                        blankLines(cs.getBlankLinesAfterFields());
                                }
                                break;
                            case METHOD:
                                blankLines(cs.getBlankLinesBeforeMethods());
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterMethods());
                                break;
                            case CLASS:
                                blankLines(cs.getBlankLinesBeforeClass());
                                scan(member, p);
                                blankLines(cs.getBlankLinesAfterClass());
                                break;
                            case BLOCK:
                                scan(member, p);
                                newline();
                                break;
                        }
                    }
                }
            }
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    
                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(info.getText().substring(diff.start, diff.end)))
                    diffs.removeFirst();
            }
            accept(RBRACE);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitVariable(VariableTree node, Void p) {
            int old = indent;
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            boolean insideFor = parent.getKind() == Tree.Kind.FOR_LOOP;
            ModifiersTree mods = node.getModifiers();
            if (mods != null) {
                if (scan(mods, p)) {
                    if (!insideFor)
                        indent += continuationIndentSize;
                    space();
                } else if (afterAnnotation) {
                    if (parent.getKind() == Tree.Kind.CLASS || parent.getKind() == Tree.Kind.BLOCK) {
                        blankLines();
                    } else {
                        space();
                    }
                }
            }
            if (isEnumerator(node)) {
                accept(IDENTIFIER);
                ExpressionTree init = node.getInitializer();
                if (init != null && init.getKind() == Tree.Kind.NEW_CLASS) {
                    List<? extends ExpressionTree> args = ((NewClassTree)init).getArguments();
                    if (args != null && !args.isEmpty()) {
                        spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);            
                        accept(LPAREN);
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                        wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), args);
                        spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);            
                        accept(RPAREN);
                    }
                }
            } else {
                if (scan(node.getType(), p)) {
                    if (indent == old && !insideFor)
                        indent += continuationIndentSize;
                    space();
                    if (!ERROR.contentEquals(node.getName()))
                        accept(IDENTIFIER);
                } else if (indent == old && !insideFor) {
                    indent += continuationIndentSize;
                }
                ExpressionTree init = node.getInitializer();
                if (init != null) {
                    spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                    accept(EQ);
                    wrapTree(cs.wrapAssignOps(), cs.spaceAroundAssignOps() ? 1 : 0, init);
                }
                fieldGroup = accept(SEMICOLON, COMMA) == COMMA;
            }
            indent = old;
            return true;
        }

        @Override
        public Boolean visitMethod(MethodTree node, Void p) {
            int old = indent;
            ModifiersTree mods = node.getModifiers();
            if (mods != null) {
                if (scan(mods, p)) {
                    indent += continuationIndentSize;
                    space();
                } else {
                    blankLines();
                }
            }
            List<? extends TypeParameterTree> tparams = node.getTypeParameters();
            if (tparams != null && !tparams.isEmpty()) {
                accept(LT);
                if (indent == old)
                    indent += continuationIndentSize;
                for (Iterator<? extends TypeParameterTree> it = tparams.iterator(); it.hasNext();) {
                    TypeParameterTree tparam = it.next();
                    scan(tparam, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                accept(GT, GTGT, GTGTGT);
                space();
            }
            Tree retType = node.getReturnType();
            if (retType != null) {
                scan(retType, p);
                if (indent == old)
                    indent += continuationIndentSize;
                space();
            }
            if (!ERROR.contentEquals(node.getName()))
                accept(IDENTIFIER);
            if (indent == old)
                indent += continuationIndentSize;
            spaces(cs.spaceBeforeMethodDeclParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends VariableTree> params = node.getParameters();
            if (params != null && !params.isEmpty()) {
                spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0, true);
                wrapList(cs.wrapMethodParams(), cs.alignMultilineMethodParams(), params);
                spaces(cs.spaceWithinMethodDeclParens() ? 1 : 0);
            }
            accept(RPAREN);
            List<? extends ExpressionTree> threxs = node.getThrows();
            if (threxs != null && !threxs.isEmpty()) {
                wrapToken(cs.wrapThrowsKeyword(), 1, THROWS);
                spaces(1, true);
                wrapList(cs.wrapThrowsList(), cs.alignMultilineThrows(), threxs);
            }
            Tree init = node.getDefaultValue();
            if (init != null) {
                spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                accept(EQ);
                spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                scan(init, p);
            }
            indent = old;
            BlockTree body = node.getBody();
            if (body != null) {
                scan(body, p);
            } else {
                accept(SEMICOLON);
            }
            return true;
        }

        @Override
        public Boolean visitModifiers(ModifiersTree node, Void p) {
            boolean ret = true;
            afterAnnotation = false;
            Iterator<? extends AnnotationTree> annotations = node.getAnnotations().iterator();
            do {
                if (tokens.offset() >= endPos)
                    break;
                if (tokens.token().id() == AT) {
                    if (annotations.hasNext()) {
                        wrapTree(cs.wrapAnnotations(), 0, annotations.next());
                        afterAnnotation = true;
                    } else {
                        afterAnnotation = false;
                    }
                    ret = false;
                } else {
                    afterAnnotation = false;
                    ret = true;
                    col += tokens.token().length();
                }
            } while (tokens.offset() < endPos && tokens.moveNext());
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            return ret;
        }

        @Override
        public Boolean visitAnnotation(AnnotationTree node, Void p) {
            accept(AT);
            scan(node.getAnnotationType(), p);
            List<? extends ExpressionTree> args = node.getArguments();
            spaces(cs.spaceBeforeAnnotationParen() ? 1 : 0);
            accept(LPAREN);
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinAnnotationParens() ? 1 : 0);
                for (Iterator<? extends ExpressionTree> it = args.iterator(); it.hasNext();) {
                    ExpressionTree arg = it.next();
                    scan(arg, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                spaces(cs.spaceWithinAnnotationParens() ? 1 : 0);
            }
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterTree node, Void p) {
            if (!ERROR.contentEquals(node.getName()))
                accept(IDENTIFIER);
            List<? extends Tree> bounds = node.getBounds();
            if (bounds != null && !bounds.isEmpty()) {
                space();
                accept(EXTENDS);
                space();
                for (Iterator<? extends Tree> it = bounds.iterator(); it.hasNext();) {
                    Tree bound = it.next();
                    scan(bound, p);
                    if (it.hasNext()) {
                        space();
                        accept(AMP);
                        space();
                    }
                }
            }
            return true;
        }

        @Override
        public Boolean visitParameterizedType(ParameterizedTypeTree node, Void p) {
            scan(node.getType(), p);
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                accept(LT);
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                accept(GT, GTGT, GTGTGT);
            }
            return true;
        }

        @Override
        public Boolean visitWildcard(WildcardTree node, Void p) {
            accept(QUESTION);
            Tree bound = node.getBound();
            if (bound != null) {
                space();
                accept(EXTENDS, SUPER);
                space();
                scan(bound, p);
            }
            return true;
        }

        @Override
        public Boolean visitBlock(BlockTree node, Void p) {
            if (node.isStatic())
                accept(STATIC);
            CodeStyle.BracePlacement bracePlacement;
            boolean spaceBeforeLeftBrace = false;
            switch (getCurrentPath().getParentPath().getLeaf().getKind()) {
                case CLASS:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeStaticInitLeftBrace();
                    break;
                case METHOD:
                    bracePlacement = cs.getMethodDeclBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeMethodDeclLeftBrace();
                    break;
                case TRY:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((TryTree)getCurrentPath().getParentPath().getLeaf()).getBlock() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeTryLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeFinallyLeftBrace();
                    break;
                case CATCH:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeCatchLeftBrace();
                    break;
                case WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeWhileLeftBrace();
                    break;
                case FOR_LOOP:
                case ENHANCED_FOR_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeForLeftBrace();
                    break;
                case DO_WHILE_LOOP:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeDoLeftBrace();
                    break;
                case IF:
                    bracePlacement = cs.getOtherBracePlacement();
                    if (((IfTree)getCurrentPath().getParentPath().getLeaf()).getThenStatement() == node)
                        spaceBeforeLeftBrace = cs.spaceBeforeIfLeftBrace();
                    else
                        spaceBeforeLeftBrace = cs.spaceBeforeElseLeftBrace();
                    break;
                case SYNCHRONIZED:
                    bracePlacement = cs.getOtherBracePlacement();
                    spaceBeforeLeftBrace = cs.spaceBeforeSynchronizedLeftBrace();
                    break;
                default:
                    bracePlacement = cs.getOtherBracePlacement();
                    break;
            }
            int old = indent;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    if (node instanceof FakeBlock) {
                        diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), "{")); //NOI18N
                    } else {
                        accept(LBRACE);
                    }
                    indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    if (node instanceof FakeBlock) {
                        indent += indentSize;
                        String s = "{" + getNewlines(1) + getIndent();
                        diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), s)); //NOI18N
                    } else {
                        accept(LBRACE);
                        indent += indentSize;
                    }
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        indent = old + indentSize;
                        String s = "{" + getNewlines(1) + getIndent();
                        diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), s)); //NOI18N
                    } else {
                        accept(LBRACE);
                        indent = old + indentSize;
                    }
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    if (node instanceof FakeBlock) {
                        String s = "{" + getNewlines(1) + getIndent();
                        diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), s)); //NOI18N
                    } else {
                        accept(LBRACE);
                    }
                    break;
            }
            for (StatementTree stat  : node.getStatements()) {
                if (!info.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), stat))) {
                    if (stat.getKind() == Tree.Kind.LABELED_STATEMENT && cs.absoluteLabelIndent()) {
                        int o = indent;
                        indent = 0;
                        blankLines();
                        indent = o;
                    }
                    blankLines();
                    scan(stat, p);
                }
            }
            indent = halfIndent;
            if (node instanceof FakeBlock) {
                String s = getNewlines(1) + getIndent() + "}"; //NOI18N
                col = indent + 1;
                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), s));
            } else {
                blankLines();
                Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
                if (diff != null && diff.end == tokens.offset()) {
                    if (diff.text != null) {
                        int idx = diff.text.lastIndexOf('\n'); //NOI18N
                        if (idx < 0)
                            diff.text = getIndent();
                        else
                            diff.text = diff.text.substring(0, idx + 1) + getIndent();

                    }
                    String spaces = diff.text != null ? diff.text : getIndent();
                    if (spaces.equals(info.getText().substring(diff.start, diff.end)))
                        diffs.removeFirst();
                }
                accept(RBRACE);
            }
            indent = old;
            return true;
        }       

        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, Void p) {
            scan(node.getExpression(), p);
            accept(DOT);
            accept(IDENTIFIER, STAR, THIS, SUPER, CLASS);
            return true;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            ExpressionTree ms = node.getMethodSelect();
            if (ms.getKind() == Tree.Kind.MEMBER_SELECT) {
                ExpressionTree exp = ((MemberSelectTree)ms).getExpression();
                scan(exp, p);
                accept(DOT);
                List<? extends Tree> targs = node.getTypeArguments();
                if (targs != null && !targs.isEmpty()) {
                    accept(LT);
                    for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                        Tree targ = it.next();
                        scan(targ, p);
                        if (it.hasNext()) {
                            spaces(cs.spaceBeforeComma() ? 1 : 0);
                            accept(COMMA);
                            spaces(cs.spaceAfterComma() ? 1 : 0);
                        }
                    }
                    accept(GT, GTGT, GTGTGT);
                }
                if(exp.getKind() == Tree.Kind.METHOD_INVOCATION)
                    wrapToken(cs.wrapChainedMethodCalls(), 0, IDENTIFIER);
                else
                    accept(IDENTIFIER);
            } else {
                scan(node.getMethodSelect(), p);
            }
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends ExpressionTree> args = node.getArguments();
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true);
                wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), args);
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);            
            }
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Void p) {
            ExpressionTree encl = node.getEnclosingExpression();
            if (encl != null) {
                scan(encl, p);
                accept(DOT);
            }
            accept(NEW);
            space();
            List<? extends Tree> targs = node.getTypeArguments();
            if (targs != null && !targs.isEmpty()) {
                accept(LT);
                for (Iterator<? extends Tree> it = targs.iterator(); it.hasNext();) {
                    Tree targ = it.next();
                    scan(targ, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                accept(GT, GTGT, GTGTGT);
            }
            scan(node.getIdentifier(), p);
            spaces(cs.spaceBeforeMethodCallParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends ExpressionTree> args = node.getArguments();
            if (args != null && !args.isEmpty()) {
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0, true); 
                wrapList(cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs(), args);
                spaces(cs.spaceWithinMethodCallParens() ? 1 : 0);            
            }
            accept(RPAREN);
            ClassTree body = node.getClassBody();
            if (body != null)
                scan(body, p);
            return true;
        }

        @Override
        public Boolean visitAssert(AssertTree node, Void p) {
            accept(ASSERT);
            int old = indent;
            indent += continuationIndentSize;
            space();
            scan(node.getCondition(), p);
            ExpressionTree detail = node.getDetail();
            if (detail != null) {
                spaces(cs.spaceBeforeColon() ? 1 : 0);
                accept(COLON);
                wrapTree(cs.wrapAssert(), cs.spaceAfterColon() ? 1 : 0, detail);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            accept(RETURN);
            int old = indent;
            indent += continuationIndentSize;
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                space();
                scan(exp, p);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            accept(THROW);
            int old = indent;
            indent += continuationIndentSize;
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                space();
                scan(exp, p);
            }
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitTry(TryTree node, Void p) {
            accept(TRY);
            scan(node.getBlock(), p);
            for (CatchTree catchTree : node.getCatches()) {
                if (cs.placeCatchOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeCatch() ? 1 : 0);
                scan(catchTree, p);
            }
            BlockTree finallyBlockTree = node.getFinallyBlock();
            if (finallyBlockTree != null) {
                if (cs.placeFinallyOnNewLine())
                    newline();
                else
                    spaces(cs.spaceBeforeFinally() ? 1 : 0);
                accept(FINALLY);
                scan(finallyBlockTree, p);
            }
            return true;
        }

        @Override
        public Boolean visitCatch(CatchTree node, Void p) {
            accept(CATCH);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeCatchParen() ? 1 : 0);
            accept(LPAREN);
            spaces(cs.spaceWithinCatchParens() ? 1 : 0);
            scan(node.getParameter(), p);
            spaces(cs.spaceWithinCatchParens() ? 1 : 0);
            accept(RPAREN);
            indent = old;
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            accept(IF);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeIfParen() ? 1 : 0);
            scan(node.getCondition(), p);
            indent = old;
            boolean prevblock = wrapStatement(cs.wrapIfStatement(), cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace() ? 1 : 0, node.getThenStatement());
            StatementTree elseStat = node.getElseStatement();
            if (elseStat != null) {
                if (cs.placeElseOnNewLine() || !prevblock) {
                    newline();
                } else {
                    spaces(cs.spaceBeforeElse() ? 1 : 0);
                }
                accept(ELSE);
                if (elseStat.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
                    space();
                    scan(elseStat, p);
                } else {
                    wrapStatement(cs.wrapIfStatement(), cs.redundantIfBraces(), cs.spaceBeforeElseLeftBrace() ? 1 : 0, elseStat);
                }
                indent = old;
            }
            return true;
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            accept(DO);
            int old = indent;
            boolean prevblock = wrapStatement(cs.wrapDoWhileStatement(), cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace() ? 1 : 0, node.getStatement());
            if (cs.placeWhileOnNewLine() || !prevblock) {
                newline();
            } else {
                spaces(cs.spaceBeforeWhile() ? 1 : 0);
            }
            accept(WHILE);
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
            scan(node.getCondition(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
            accept(WHILE);
            int old = indent;
            indent += continuationIndentSize;            
            spaces(cs.spaceBeforeWhileParen() ? 1 : 0);
            scan(node.getCondition(), p);
            indent = old;
            wrapStatement(cs.wrapWhileStatement(), cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, Void p) {
            accept(FOR);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeForParen() ? 1 : 0);
            accept(LPAREN);
            List<? extends StatementTree> inits = node.getInitializer();
            if (inits != null && !inits.isEmpty()) {
                spaces(cs.spaceWithinForParens() ? 1 : 0);
                for (Iterator<? extends StatementTree> it = inits.iterator(); it.hasNext();) {
                    StatementTree init = it.next();
                    scan(init, p);
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                spaces(cs.spaceBeforeSemi() ? 1 : 0);
            }
            accept(SEMICOLON);
            ExpressionTree cond = node.getCondition();
            if (cond != null) {
                wrapTree(cs.wrapFor(), cs.spaceAfterSemi() ? 1 : 0, cond);
                spaces(cs.spaceBeforeSemi() ? 1 : 0);
            }
            accept(SEMICOLON);
            List<? extends ExpressionStatementTree> updates = node.getUpdate();
            if (updates != null && !updates.isEmpty()) {
                boolean first = true;
                for (Iterator<? extends ExpressionStatementTree> it = updates.iterator(); it.hasNext();) {
                    ExpressionStatementTree update = it.next();
                    if (first) {
                        wrapTree(cs.wrapFor(), cs.spaceAfterSemi() ? 1 : 0, update);
                    } else {
                        scan(update, p);
                    }
                    first = false;
                    if (it.hasNext()) {
                        spaces(cs.spaceBeforeComma() ? 1 : 0);
                        accept(COMMA);
                        spaces(cs.spaceAfterComma() ? 1 : 0);
                    }
                }
                spaces(cs.spaceWithinForParens() ? 1 : 0);
            }
            accept(RPAREN);
            indent = old;
            wrapStatement(cs.wrapForStatement(), cs.redundantForBraces(), cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;            
        }

        @Override
        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            accept(FOR);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeForParen() ? 1 : 0);
            accept(LPAREN);
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            scan(node.getVariable(), p);
            spaces(cs.spaceBeforeColon() ? 1 : 0);
            accept(COLON);
            spaces(cs.spaceAfterColon() ? 1 : 0);
            scan(node.getExpression(), p);
            spaces(cs.spaceWithinForParens() ? 1 : 0);
            accept(RPAREN);
            indent = old;
            wrapStatement(cs.wrapForStatement(), cs.redundantForBraces(), cs.spaceBeforeForLeftBrace() ? 1 : 0, node.getStatement());
            return true;
        }

        @Override
        public Boolean visitSynchronized(SynchronizedTree node, Void p) {
            accept(SYNCHRONIZED);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeSynchronizedParen() ? 1 : 0);
            scan(node.getExpression(), p);
            indent = old;
            scan(node.getBlock(), p);
            return true;
        }

        @Override
        public Boolean visitSwitch(SwitchTree node, Void p) {
            accept(SWITCH);
            int old = indent;
            indent += continuationIndentSize;
            spaces(cs.spaceBeforeSwitchParen() ? 1 : 0);
            scan(node.getExpression(), p);
            CodeStyle.BracePlacement bracePlacement = cs.getOtherBracePlacement();
            boolean spaceBeforeLeftBrace = cs.spaceBeforeSwitchLeftBrace();
            boolean indentCases = cs.indentCasesFromSwitch();
            indent = old;
            int halfIndent = indent;
            switch(bracePlacement) {
                case SAME_LINE:
                    spaces(spaceBeforeLeftBrace ? 1 : 0);
                    accept(LBRACE);
                    if (indentCases)
                        indent += indentSize;
                    break;
                case NEW_LINE:
                    newline();
                    accept(LBRACE);
                    if (indentCases)
                        indent += indentSize;
                    break;
                case NEW_LINE_HALF_INDENTED:
                    indent += (indentSize >> 1);
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (indentCases)
                        indent = old + indentSize;
                    else
                        indent = old;
                    break;
                case NEW_LINE_INDENTED:
                    indent += indentSize;
                    halfIndent = indent;
                    newline();
                    accept(LBRACE);
                    if (!indentCases)
                        indent = old;
                    break;
            }
            for (CaseTree caseTree : node.getCases()) {
                blankLines();
                scan(caseTree, p);
            }
            blankLines();
            indent = halfIndent;
            Diff diff = diffs.isEmpty() ? null : diffs.getFirst();
            if (diff != null && diff.end == tokens.offset()) {
                if (diff.text != null) {
                    int idx = diff.text.lastIndexOf('\n'); //NOI18N
                    if (idx < 0)
                        diff.text = getIndent();
                    else
                        diff.text = diff.text.substring(0, idx + 1) + getIndent();
                    
                }
                String spaces = diff.text != null ? diff.text : getIndent();
                if (spaces.equals(info.getText().substring(diff.start, diff.end)))
                    diffs.removeFirst();
            }
            accept(RBRACE);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitCase(CaseTree node, Void p) {
            ExpressionTree exp = node.getExpression();
            if (exp != null) {
                accept(CASE);
                space();
                scan(exp, p);
            } else {
                accept(DEFAULT);
            }
            accept(COLON);
            int old = indent;
            indent += indentSize;
            for (Iterator<? extends StatementTree> it = node.getStatements().iterator(); it.hasNext();) {
                blankLines();
                scan(it.next(), p);
            }
            indent = old;
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            accept(BREAK);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            accept(CONTINUE);
            Name label = node.getLabel();
            if (label != null) {
                space();
                accept(IDENTIFIER);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Void p) {
            boolean b = scan(node.getVariable(), p);
            if (b || getCurrentPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION) {
                spaces(cs.spaceAroundAssignOps() ? 1 : 0);
                accept(EQ);
                wrapTree(cs.wrapAssignOps(), cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
            } else {
                scan(node.getExpression(), p);
            }
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            scan(node.getVariable(), p);
            spaces(cs.spaceAroundAssignOps() ? 1 : 0);
            if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                tokens.moveNext();
            }
            wrapTree(cs.wrapAssignOps(), cs.spaceAroundAssignOps() ? 1 : 0, node.getExpression());
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitPrimitiveType(PrimitiveTypeTree node, Void p) {
            switch (node.getPrimitiveTypeKind()) {
            case BOOLEAN:
                accept(BOOLEAN);
                break;
            case BYTE:
                accept(BYTE);
                break;
            case CHAR:
                accept(CHAR);
                break;
            case DOUBLE:
                accept(DOUBLE);
                break;
            case FLOAT:
                accept(FLOAT);
                break;
            case INT: 
                accept(INT);
                break;
            case LONG:
                accept(LONG);
                break;
            case SHORT:
                accept(SHORT);
                break;
            case VOID:
                accept(VOID);
                break;
            }
            return true;
        }

        @Override
        public Boolean visitArrayType(ArrayTypeTree node, Void p) {
            boolean ret = scan(node.getType(), p);
            int index = tokens.index();
            int col = this.col;
            JavaTokenId id = accept(LBRACKET, ELLIPSIS, IDENTIFIER);
            if (id == ELLIPSIS)
                return ret;
            if (id != IDENTIFIER) {
                accept(RBRACKET);
                return ret;
            }
            rollback(index, col);
            space();
            accept(IDENTIFIER);
            accept(LBRACKET);
            accept(RBRACKET);
            return false;
        }

        @Override
        public Boolean visitArrayAccess(ArrayAccessTree node, Void p) {
            scan(node.getExpression(), p);
            accept(LBRACKET);
            scan(node.getIndex(), p);
            accept(RBRACKET);
            return true;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Void p) {
            Tree type = node.getType();
            List<? extends ExpressionTree> inits = node.getInitializers();
            if (type != null) {
                accept(NEW);
                space();
                int n = inits != null ? 1 : 0;
                while (type.getKind() == Tree.Kind.ARRAY_TYPE) {
                    n++;
                    type = ((ArrayTypeTree)type).getType();
                }
                scan(type, p);
                for (ExpressionTree dim : node.getDimensions()) {
                    accept(LBRACKET);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    scan(dim, p);
                    spaces(cs.spaceWithinArrayInitBrackets() ? 1 : 0);
                    accept(RBRACKET);
                }
                while(--n >= 0) {
                    accept(LBRACKET);
                    accept(RBRACKET);
                }
            }
            if (inits != null && !inits.isEmpty()) {
                spaces(cs.spaceBeforeArrayInitLeftBrace() ? 1 : 0);
                accept(LBRACE);
                spaces(cs.spaceWithinBraces() ? 1 : 0, true);
                wrapList(cs.wrapArrayInit(), cs.alignMultilineArrayInit(), inits);
                spaces(cs.spaceWithinBraces() ? 1 : 0);
                accept(RBRACE);
            }
            return true;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, Void p) {
            accept(IDENTIFIER, THIS, SUPER);
            return true;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Void p) {
            if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                tokens.moveNext();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                scan(node.getExpression(), p);
            } else {
                scan(node.getExpression(), p);
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                tokens.moveNext();
                spaces(cs.spaceAroundUnaryOps() ? 1 : 0);
            }
            return true;
        }

        @Override
        public Boolean visitBinary(BinaryTree node, Void p) {
            scan(node.getLeftOperand(), p);
            spaces(cs.spaceAroundBinaryOps() ? 1 : 0);
            if (OPERATOR.equals(tokens.token().id().primaryCategory())) {
                col += tokens.token().length();
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                tokens.moveNext();
            }
            wrapTree(cs.wrapBinaryOps(), cs.spaceAroundBinaryOps() ? 1 : 0, node.getRightOperand());
            return true;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            scan(node.getCondition(), p);
            wrapToken(cs.wrapTernaryOps(), cs.spaceAroundTernaryOps() ? 1 : 0, QUESTION);
            spaces(cs.spaceAroundTernaryOps() ? 1 : 0);
            scan(node.getTrueExpression(), p);
            wrapToken(cs.wrapTernaryOps(), cs.spaceAroundTernaryOps() ? 1 : 0, COLON);
            spaces(cs.spaceAroundTernaryOps() ? 1 : 0);
            scan(node.getFalseExpression(), p);
            return true;
        }

        @Override
        public Boolean visitEmptyStatement(EmptyStatementTree node, Void p) {
            accept(SEMICOLON);
            return true;
        }

        @Override
        public Boolean visitExpressionStatement(ExpressionStatementTree node, Void p) {
            int old = indent;
            indent += continuationIndentSize;
            scan(node.getExpression(), p);
            accept(SEMICOLON);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitInstanceOf(InstanceOfTree node, Void p) {
            scan(node.getExpression(), p);
            space();
            accept(INSTANCEOF);
            space();
            scan(node.getType(), p);
            return true;
        }

        @Override
        public Boolean visitLabeledStatement(LabeledStatementTree node, Void p) {
            if (!ERROR.contentEquals(node.getLabel()))
                accept(IDENTIFIER);            
            accept(COLON);
            int old = indent;
            indent += cs.getLabelIndent();
            int cnt = indent - col;
            if (cnt < 0)
                newline();
            else
                spaces(cnt);
            scan(node.getStatement(), p);
            indent = old;
            return true;
        }

        @Override
        public Boolean visitTypeCast(TypeCastTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens = cs.spaceWithinTypeCastParens();
            spaces(spaceWithinParens ? 1 : 0);
            scan(node.getType(), p);
            spaces(spaceWithinParens ? 1 : 0);
            accept(RPAREN);
            spaces(cs.spaceAfterTypeCast() ? 1 : 0);
            scan(node.getExpression(), p);
            return true;
        }

        @Override
        public Boolean visitParenthesized(ParenthesizedTree node, Void p) {
            accept(LPAREN);
            boolean spaceWithinParens;
            switch(getCurrentPath().getParentPath().getLeaf().getKind()) {
                case IF:
                    spaceWithinParens = cs.spaceWithinIfParens();
                    break;
                case FOR_LOOP:
                    spaceWithinParens = cs.spaceWithinForParens();
                    break;
                case DO_WHILE_LOOP:
                case WHILE_LOOP:
                    spaceWithinParens = cs.spaceWithinWhileParens();
                    break;
                case SWITCH:
                    spaceWithinParens = cs.spaceWithinSwitchParens();
                    break;
                case SYNCHRONIZED:
                    spaceWithinParens = cs.spaceWithinSynchronizedParens();
                    break;
                default:
                    spaceWithinParens = cs.spaceWithinParens();
            }
            spaces(spaceWithinParens ? 1 : 0);
            scan(node.getExpression(), p);
            spaces(spaceWithinParens ? 1 : 0);
            accept(RPAREN);
            return true;
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            return true;
        }

        @Override
        public Boolean visitErroneous(ErroneousTree node, Void p) {
            for (Tree tree : node.getErrorTrees()) {
                int pos = (int)info.getTrees().getSourcePositions().getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                do {
                    col += tokens.token().length();
                } while (tokens.moveNext() && tokens.offset() < endPos);
                lastBlankLines = -1;
                lastBlankLinesTokenIndex = -1;
                scan(tree, p);
            }
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            return true;
        }

        @Override
        public Boolean visitOther(Tree node, Void p) {
            do {
                col += tokens.token().length();
            } while (tokens.moveNext() && tokens.offset() < endPos);
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            return true;
        }

        private JavaTokenId accept(JavaTokenId first, JavaTokenId... rest) {
            lastBlankLines = -1;
            lastBlankLinesTokenIndex = -1;
            EnumSet<JavaTokenId> tokenIds = EnumSet.of(first, rest);
            Token<JavaTokenId> lastWSToken = null;
            int after = 0;
            do {
                if (tokens.offset() >= endPos)
                    return null;
                JavaTokenId id = tokens.token().id();
                if (tokenIds.contains(id)) {
                    String spaces = after == 1 //after line comment
                            ? getIndent()
                            : after == 2 //after javadoc comment
                            ? getNewlines(1) + getIndent()
                            : null;
                    if (lastWSToken != null) {
                        if (spaces == null || !spaces.contentEquals(lastWSToken.text()))
                            diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                    } else {
                        if (spaces != null && spaces.length() > 0)
                            diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                    }
                    if (after > 0)
                        col = indent;
                    col += tokens.token().length();
                    return tokens.moveNext() ? id : null;
                }
                switch(id) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        after = 0;
                        break;
                    default:
                        return null;
                }
            } while(tokens.moveNext());
            return null;
        }

        private void space() {
            spaces(1);
        }

        private void spaces(int count) {
            spaces(count, false);
        }
        
        private void spaces(int count, boolean preserveNewline) {
            Token<JavaTokenId> lastWSToken = null;
            int after = 0;
            do {
                if (tokens.offset() >= endPos)
                    return;
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastWSToken = tokens.token();
                        break;
                    case LINE_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (preserveNewline) {
                                String text = lastWSToken.text().toString();
                                int idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0)
                                    spaces = getNewlines(1) + getIndent();
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        col = 0;
                        after = 1; //line comment
                        break;
                    case JAVADOC_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (preserveNewline) {
                                String text = lastWSToken.text().toString();
                                int idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        String tokenText = tokens.token().text().toString();
                        int idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        after = 2; //javadoc comment
                        break;
                    case BLOCK_COMMENT:
                        if (lastWSToken != null) {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : SPACE;
                            if (preserveNewline) {
                                String text = lastWSToken.text().toString();
                                idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                            lastWSToken = null;
                            if (after > 0)
                                col = indent;
                            else
                                col++;
                        } else {
                            String spaces = after == 1 //after line comment
                                    ? getIndent()
                                    : after == 2 //after javadoc comment
                                    ? getNewlines(1) + getIndent()
                                    : null;
                            if (spaces != null && spaces.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                            if (after > 0)
                                col = indent;
                        }
                        tokenText = tokens.token().text().toString();
                        idx = tokenText.lastIndexOf('\n'); //NOI18N
                        if (idx >= 0)
                            tokenText = tokenText.substring(idx + 1);
                        col += getCol(tokenText);
                        after = 0;
                        break;
                    default:
                        String spaces = after == 1 //after line comment
                                ? getIndent()
                                : after == 2 //after javadoc comment
                                ? getNewlines(1) + getIndent()
                                : getSpaces(count);
                        if (lastWSToken != null) {
                            if (preserveNewline) {
                                String text = lastWSToken.text().toString();
                                idx = text.lastIndexOf('\n'); //NOI18N
                                if (idx >= 0) {
                                    spaces = getNewlines(1) + getIndent();
                                    after = 3;
                                }
                            }
                            if (!spaces.contentEquals(lastWSToken.text()))
                                diffs.addFirst(new Diff(tokens.offset() - lastWSToken.length(), tokens.offset(), spaces));
                        } else if (spaces.length() > 0) {
                            diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), spaces));
                        }
                        if (after > 0)
                            col = indent;
                        else
                            col += count;
                        return;
                }
            } while(tokens.moveNext());
        }

        private void newline() {
            blankLines(0);
        }

        private void blankLines() {
            blankLines(ANY_COUNT);
        }

        private void blankLines(int count) {
            if (count >= 0) {
                if (lastBlankLinesTokenIndex < 0) {
                    lastBlankLines = count;
                    lastBlankLinesTokenIndex = tokens.index();
                } else if (lastBlankLines < count) {
                    lastBlankLines = count;
                    rollback(lastBlankLinesTokenIndex, indent);
                } else {
                    return;
                }
            } else {
                if (lastBlankLinesTokenIndex < 0) {
                    lastBlankLinesTokenIndex = tokens.index();
                } else {
                    return;
                }
            }        
            Token<JavaTokenId> lastToken = null;
            boolean afterBlockComment = false;
            do {
                if (tokens.offset() >= endPos)
                    return;
                switch(tokens.token().id()) {
                    case WHITESPACE:
                        lastToken = tokens.token();
                        break;
                    case BLOCK_COMMENT:
                        if (count >= 0 && tokens.index() > 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx > 0) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text.substring(lastIdx)))
                                    diffs.addFirst(new Diff(offset + lastIdx, tokens.offset(), indent));
                            }
                            lastToken = null;
                        }
                        afterBlockComment = true;
                        break;
                    case JAVADOC_COMMENT:
                        if (count >= 0 && tokens.index() > 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx > 0) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text.substring(lastIdx)))
                                    diffs.addFirst(new Diff(offset + lastIdx, tokens.offset(), indent));
                            }
                            lastToken = null;
                        }
                        afterBlockComment = false;
                        break;
                    case LINE_COMMENT:
                        if (lastToken != null) {
                            if (count >= 0 && tokens.index() > 1)
                                count++;
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx > 0) {
                                String indent = getIndent();
                                if (!indent.contentEquals(text.substring(lastIdx)))
                                    diffs.addFirst(new Diff(offset + lastIdx, tokens.offset(), indent));
                            }
                            lastToken = null;
                        }
                        if (count != 0)
                            count--;
                        afterBlockComment = false;
                        break;
                    default:
                        if (count >= 0 && tokens.index() > 1)
                            count++;
                        if (lastToken != null) {
                            int offset = tokens.offset() - lastToken.length();
                            String text = lastToken.text().toString();
                            int idx = 0;
                            int lastIdx = 0;
                            while(count != 0 && (idx = text.indexOf('\n', lastIdx)) >= 0) { //NOI18N
                                if (idx > lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx, null));
                                lastIdx = idx + 1;
                                count--;
                            }
                            if ((idx = text.lastIndexOf('\n')) >= 0) { //NOI18N
                                afterBlockComment = false;
                                if (idx >= lastIdx)
                                    diffs.addFirst(new Diff(offset + lastIdx, offset + idx + 1, null));
                                lastIdx = idx + 1;
                            }
                            if (lastIdx == 0 && count < 0) {
                                count = count == ANY_COUNT ? 1 : 0;
                            }
                            String indent = afterBlockComment ? SPACE : getNewlines(count) + getIndent();
                            if (!indent.contentEquals(text.substring(lastIdx)))
                                diffs.addFirst(new Diff(offset + lastIdx, tokens.offset(), indent));
                        } else {
                            if (lastBlankLines < 0 && count == ANY_COUNT)
                                count = lastBlankLines = 1;
                            String text = getNewlines(count) + getIndent();
                            if (text.length() > 0)
                                diffs.addFirst(new Diff(tokens.offset(), tokens.offset(), text));
                        }
                        col = indent;
                        return;
                }
            } while(tokens.moveNext());
        }

        private void rollback(int index, int col) {
            tokens.moveIndex(index);
            tokens.moveNext();
            Diff d;
            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= tokens.offset())
                diffs.removeFirst();
            this.col = col;
        }

        private int wrapToken(CodeStyle.WrapStyle wrapStyle, int spacesCnt, JavaTokenId first, JavaTokenId... rest) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    newline();
                    ret = col;
                    accept(first, rest);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    spaces(spacesCnt, true);
                    ret = col;
                    accept(first, rest);
                    if (this.col > rightMargin) {
                        rollback(index, c);
                        newline();
                        ret = col;
                        accept(first, rest);
                    }
                    break;
                case WRAP_NEVER:
                    spaces(spacesCnt, true);
                    ret = col;
                    accept(first, rest);
                    break;
            }
            return ret;
        }

        private int wrapTree(CodeStyle.WrapStyle wrapStyle, int spacesCnt, Tree tree) {
            int ret = -1;
            switch (wrapStyle) {
                case WRAP_ALWAYS:
                    newline();
                    ret = col;
                    scan(tree, null);
                    break;
                case WRAP_IF_LONG:
                    int index = tokens.index();
                    int c = col;
                    spaces(spacesCnt, true);
                    ret = col;
                    wrapDepth++;
                    scan(tree, null);
                    wrapDepth--;
                    if (col > rightMargin && (wrapDepth == 0 || c <= rightMargin)) {
                        rollback(index, c);
                        newline();
                        ret = col;
                        scan(tree, null);
                    }
                    break;
                case WRAP_NEVER:
                    spaces(spacesCnt, true);
                    ret = col;
                    scan(tree, null);
                    break;
            }
            return ret;
        }

        private boolean wrapStatement(CodeStyle.WrapStyle wrapStyle, CodeStyle.BracesGenerationStyle bracesGenerationStyle, int spacesCnt, StatementTree tree) {
            if (tree.getKind() == Tree.Kind.BLOCK) {
                if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.ELIMINATE) {
                    Iterator<? extends StatementTree> stats = ((BlockTree)tree).getStatements().iterator();
                    if (stats.hasNext()) {
                        StatementTree stat = stats.next();
                        if (!stats.hasNext() && stat.getKind() != Tree.Kind.VARIABLE) {
                            int start = tokens.offset();
                            accept(LBRACE);
                            Diff d;
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start)
                                diffs.removeFirst();
                            diffs.addFirst(new Diff(start, tokens.offset(), null));
                            int old = indent;
                            indent += indentSize;
                            wrapTree(wrapStyle, spacesCnt, stat);
                            indent = old;
                            start = tokens.offset();
                            accept(RBRACE);
                            while (!diffs.isEmpty() && (d = diffs.getFirst()) != null && d.getStartOffset() >= start)
                                diffs.removeFirst();
                            diffs.addFirst(new Diff(start, tokens.offset(), null));
                            return false;
                        }
                    }
                }
                scan(tree, null);
                return true;
            }
            if (bracesGenerationStyle == CodeStyle.BracesGenerationStyle.GENERATE) {
                scan(new FakeBlock(tree), null);
                return true;
            }
            int old = indent;
            indent += indentSize;
            int ret = wrapTree(wrapStyle, spacesCnt, tree);
            indent = old;
            return false;
        }

        private void wrapList(CodeStyle.WrapStyle wrapStyle, boolean align, List<? extends Tree> trees) {
            boolean first = true;
            int old = indent;
            for (Iterator<? extends Tree> it = trees.iterator(); it.hasNext();) {
                Tree impl = it.next();
                if (first) {
                    if (align)
                        indent = col;
                    scan(impl, null);
                } else {
                    wrapTree(wrapStyle, cs.spaceAfterComma() ? 1 : 0, impl);
                }
                first = false;
                if (it.hasNext()) {
                    spaces(cs.spaceBeforeComma() ? 1 : 0);
                    accept(COMMA);
                }
            }
            indent = old;
        }

        private String getSpaces(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return SPACE;
            StringBuilder sb = new StringBuilder(); 
            while (count-- > 0)
                sb.append(' '); //NOI18N
            return sb.toString();
        }

        private String getNewlines(int count) {
            if (count <= 0)
                return EMPTY;
            if (count == 1)
                return NEWLINE;
            StringBuilder sb = new StringBuilder(); 
            while (count-- > 0)
                sb.append('\n'); //NOI18N
            return sb.toString();
        }

        private String getIndent() {
            StringBuilder sb = new StringBuilder(); 
            int col = 0;
            if (!expandTabToSpaces) {
                while (((col + tabSize) &~ (tabSize - 1)) <= indent) {
                    sb.append('\t'); //NOI18N
                    col = col + tabSize & ~(tabSize - 1);
                }
            }
            while (col < indent) {
                sb.append(SPACE); //NOI18N
                col++;
            }
            return sb.toString();
        }

        private int getIndentLevel(TreePath path) {
            if (path.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT)
                return 0;
            TokenSequence<JavaTokenId> sourceTS = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            if (sourceTS == null)
                return -1;
            int indent = 0;
            SourcePositions sp = info.getTrees().getSourcePositions();
            Tree lastTree = null;
            while (path != null) {
                int offset = (int)sp.getStartPosition(path.getCompilationUnit(), path.getLeaf());
                if (offset < 0)
                    return -1;
                sourceTS.move(offset);
                if (sourceTS.movePrevious()) {
                    Token<JavaTokenId> token = sourceTS.token();
                    if (token.id() == WHITESPACE) {
                        String text = token.text().toString();
                        int idx = text.lastIndexOf('\n');
                        if (idx >= 0) {
                            text = text.substring(idx + 1);
                            indent = getCol(text);
                            break;
                        } else if (sourceTS.movePrevious()) {
                            if (sourceTS.token().id() == LINE_COMMENT) {
                                indent = getCol(text);
                                break;
                            }                        
                        }
                    }
                }
                lastTree = path.getLeaf();
                path = path.getParentPath();
            }
            if (lastTree != null && path != null) {
                switch (path.getLeaf().getKind()) {
                case CLASS:
                    for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                case BLOCK:
                    for (Tree tree : ((BlockTree)path.getLeaf()).getStatements()) {
                        if (tree == lastTree) {
                            indent += tabSize;
                            break;
                        }
                    }
                    break;
                }
            }
            return indent;
        }

        private int getCol(String text) {
            int col = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\t') {
                    col = col+tabSize & ~(tabSize-1);
                } else {
                    col++;
                }
            }
            return col;
        }

        private boolean isEnumerator(VariableTree tree) {
            return (((JCModifiers)tree.getModifiers()).flags & Flags.ENUM) != 0;
        }
        
        private static class FakeBlock extends JCBlock {
            
            private StatementTree stat;
            
            private FakeBlock(StatementTree stat) {
                super(0L, com.sun.tools.javac.util.List.of((JCStatement)stat));
                this.stat = stat;
            }
        }
    }

    private static class Diff {
        private int start;
        private int end;
        private String text;

        private Diff(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public int getStartOffset() {
            return start;
        }
        
        public int getEndOffset() {
            return end;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">:" + text; //NOI18N
        }
    }
}

