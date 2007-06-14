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
package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import static com.sun.source.tree.Tree.*;
import com.sun.source.tree.VariableTree;

import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.*;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;

import java.io.*;
import java.util.EnumSet;
import java.util.LinkedList;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.query.Query;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.netbeans.modules.java.source.save.PositionEstimator.Direction;


/** Prints out a tree as an indented Java source program.
 */
public final class VeryPretty extends JCTree.Visitor {
    
    private static final char[] hex = "0123456789ABCDEF".toCharArray();
    private static final String REPLACEMENT = "%[a-z]*%";    

    private final CodeStyle cs;
    private final CharBuffer out;

    private final Name.Table names;
    private final CommentHandler commentHandler;
    private final Symtab symbols;
    private final Types types;
    private final TreeInfo treeinfo;
    private final WidthEstimator widthEstimator;
    private final DanglingElseChecker danglingElseChecker;
    
    public Name enclClassName; // the enclosing class name.
    private int prec; // visitor argument: the current precedence level.
    private LinkedList<Comment> pendingComments = null;
    private int lastReadCommentIdx = -1;
    private JCCompilationUnit origUnit;
    private CompilationInfo cInfo;
    
    private int fromOffset = -1;
    private int toOffset = -1;
    private boolean containsError = false;
    
    public VeryPretty(CompilationInfo cInfo) {
        this(cInfo, CodeStyle.getDefault(null));
    }

    public VeryPretty(CompilationInfo cInfo, CodeStyle cs) {
        this(JavaSourceAccessor.INSTANCE.getJavacTask(cInfo).getContext(), cs);
        this.cInfo = cInfo;
        this.origUnit = (JCCompilationUnit) cInfo.getCompilationUnit();
    }
    
    public VeryPretty(Context context) {
        this(context, CodeStyle.getDefault(null));
    }
    
    public VeryPretty(Context context, CodeStyle cs) {
	names = Name.Table.instance(context);
	enclClassName = names.empty;
        commentHandler = CommentHandlerService.instance(context);
	symbols = Symtab.instance(context);
        types = Types.instance(context);
	treeinfo = TreeInfo.instance(context);
	widthEstimator = new WidthEstimator(context);
        danglingElseChecker = new DanglingElseChecker();
        prec = TreeInfo.notExpression;
        this.cs = cs;
        out = new CharBuffer(cs.getRightMargin());
    }

    public String toString() {
	return out.toString();
    }
    
    public void toLeftMargin() {
	out.toLeftMargin();
    }
    
    public void reset(int margin) {
	out.setLength(0);
	out.leftMargin = margin;
    }

    /** Increase left margin by indentation width.
     */
    public int indent() {
	int old = out.leftMargin;
	out.leftMargin = old + cs.getIndentSize();
	return old;
    }
    
    public void undent(int old) {
	out.leftMargin = old;
    }

    public void newline() {
	out.nlTerm();
    }

    public void blankline() {
        out.blanklines(1);
    }

    public int setPrec(int prec) {
        int old = this.prec;
        this.prec = prec;
        return old;
    }
    
    public final void print(String s) {
	if (s == null)
	    return;
        out.append(s);
    }

    public final void print(Name n) {
	out.appendUtf8(n.table.names, n.index, n.len);
    }
    
    public void print(JCTree t) {
        blankLines(t, true);
        toLeftMargin();
	printPrecedingComments(t);
	t.accept(this);
        printTrailingComments(t);
        blankLines(t, false);
    }
    
    public String reformat(JCTree t, int fromOffset, int toOffset, int indent) {
        reset(indent);
        this.fromOffset = fromOffset;
        this.toOffset = toOffset;
        print(t);
        return containsError ? null : out.toString();
    }

    /** Print a package declaration.
     */
    public void printPackage(JCExpression pid) {
        if (pid != null) {
            blankLines(cs.getBlankLinesBeforePackage());
            print("package ");
            printExpr(pid);
            print(';');
            blankLines(cs.getBlankLinesAfterPackage());
        }
    }    

    public String getMethodHeader(MethodTree t, String s) {
        JCMethodDecl tree = (JCMethodDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if (tree.name == names.init) {
            print(enclClassName);
            s = replace(s, UiUtils.PrintPart.NAME);
        } else {
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
                s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            }
            print(tree.restype, tree.sym != null && tree.sym.type!=null ? tree.sym.type.getReturnType() : null);
            s = replace(s, UiUtils.PrintPart.TYPE);
            out.clear();
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
        }
        print('(');
        wrapTrees(tree.params, WrapStyle.WRAP_NEVER, out.col);
        print(')');
        s = replace(s, UiUtils.PrintPart.PARAMETERS);
        if (tree.thrown.nonEmpty()) {
            print(" throws ");
            wrapTrees(tree.thrown, WrapStyle.WRAP_NEVER, out.col);
            s = replace(s, UiUtils.PrintPart.THROWS);
        }
        return s.replaceAll(REPLACEMENT,"");
    }
    
    public String getClassHeader(ClassTree t, String s) {
        JCClassDecl tree = (JCClassDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        long flags = tree.sym != null ? tree.sym.flags() : tree.mods.flags;
        if ((flags & ENUM) != 0)
            printFlags(flags & ~(INTERFACE | STATIC | FINAL));
        else
            printFlags(flags & ~(INTERFACE | ABSTRACT));
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if ((flags & INTERFACE) != 0) {
            print("interface ");
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.implementing.nonEmpty()) {
                print(" extends ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
        } else {
            if ((flags & ENUM) != 0)
                print("enum ");
            else {
                if ((flags & ABSTRACT) != 0)
                    print("abstract ");
                print("class ");
            }
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.extending != null) {
                print(" extends ");
                print(tree.extending, tree.sym != null
                        ? types.supertype(tree.sym.type) : null);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
            if (tree.implementing.nonEmpty()) {
                print(" implements ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, UiUtils.PrintPart.IMPLEMENTS);
            }
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getVariableHeader(VariableTree t, String s) {
        JCVariableDecl tree = (JCVariableDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
	printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
        Type type = tree.type != null ? tree.type : tree.vartype.type;
	print(tree.vartype, type);
        s = replace(s, UiUtils.PrintPart.TYPE);
	needSpace();
	print(tree.name);
        s = replace(s, UiUtils.PrintPart.NAME);
        return s.replaceAll(REPLACEMENT,"");
    }
    
    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    public void visitTopLevel(JCCompilationUnit tree) {
        printPrecedingComments(tree);
        printPackage(tree.pid);
        boolean hasImports = false;
        List<JCTree> l = tree.defs;
        while (l.nonEmpty() && l.head.tag == JCTree.IMPORT){
            if (!hasImports) {
                blankLines(cs.getBlankLinesBeforeImports());
                hasImports = true;
            }
            printStat(l.head);
            newline();
            l = l.tail;
        }
        if (hasImports)
            blankLines(cs.getBlankLinesAfterImports());
	while (l.nonEmpty()) {
            printStat(l.head);
            newline();
            l = l.tail;
	}
    }

    public void visitImport(JCImport tree) {
        print("import ");
        if (tree.staticImport)
            print("static ");
        print(fullName(tree.qualid));
        print(';');
    }

    public void visitClassDef(JCClassDecl tree) {
	Name enclClassNamePrev = enclClassName;
	enclClassName = tree.name;
	toLeftMargin();
        printAnnotations(tree.mods.annotations);
	long flags = tree.sym != null ? tree.sym.flags() : tree.mods.flags;
	if ((flags & ENUM) != 0)
	    printFlags(flags & ~(INTERFACE | STATIC | FINAL));
	else
	    printFlags(flags & ~(INTERFACE | ABSTRACT));
	if ((flags & INTERFACE) != 0 || (flags & ANNOTATION) != 0) {
            if ((flags & ANNOTATION) != 0) print('@');
	    print("interface ");
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.implementing.nonEmpty()) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	} else {
	    if ((flags & ENUM) != 0)
		print("enum ");
	    else {
		if ((flags & ABSTRACT) != 0)
		    print("abstract ");
		print("class ");
	    }
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.extending != null) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		print(tree.extending, tree.sym != null 
                      ? types.supertype(tree.sym.type) : null);
	    }
	    if (tree.implementing.nonEmpty()) {
                wrap("implements ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	}
	int old = cs.indentTopLevelClassMembers() ? indent() : out.leftMargin;
	int bcol = old;
        switch(cs.getClassDeclBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (cs.getIndentSize() >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeClassDeclLeftBrace())
            needSpace();
	print('{');
        boolean emptyClass = true;
        for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
            if (!isSynthetic(l.head)) {
                emptyClass = false;
                break;
            }
        }
	if (!emptyClass) {
	    blankLines(cs.getBlankLinesAfterClassHeader());
            if ((tree.mods.flags & ENUM) != 0) {
                boolean first = true;
                boolean hasNonEnumerator = false;
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (isEnumerator(l.head)) {
                        if (first) {
                            toColExactly(out.leftMargin);
                            first = false;
                        } else {
                            print(cs.spaceBeforeComma() ? " ," : ",");
                            switch(cs.wrapEnumConstants()) {
                            case WRAP_IF_LONG:
                                int rm = cs.getRightMargin();
                                if (widthEstimator.estimateWidth(l.head, rm - out.col) + out.col + 1 <= rm) {
                                    if (cs.spaceAfterComma())
                                        print(' ');
                                    break;
                                }
                            case WRAP_ALWAYS:
                                toColExactly(out.leftMargin);
                                break;
                            case WRAP_NEVER:
                                if (cs.spaceAfterComma())
                                    print(' ');
                                break;
                            }
                        }
                        printStat(l.head);
                    } else if (!isSynthetic(l.head))
                        hasNonEnumerator = true;
                }
                if (hasNonEnumerator) {
                    print(";");
                    newline();
                }
            }
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                JCTree t = l.head;
                if (!isEnumerator(t)) {
                    if (isSynthetic(t))
                        continue;
                    toColExactly(out.leftMargin);
                    printStat(t);
                    newline();
                }
            }
        } else {
            printEmptyBlockComments(tree);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
	enclClassName = enclClassNamePrev;
    }
    
    public void visitMethodDef(JCMethodDecl tree) {
	if ((tree.mods.flags & Flags.SYNTHETIC)==0 && 
		tree.name != names.init || 
		enclClassName != null) {
	    Name enclClassNamePrev = enclClassName;
	    enclClassName = null;
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags);
            if (tree.name == names.init) {
                print(enclClassNamePrev);
            } else {
                if (tree.typarams != null) {
                    printTypeParameters(tree.typarams);
                    needSpace();
                }
                print(tree.restype, tree.sym != null && tree.sym.type!=null ? tree.sym.type.getReturnType() : null);
                needSpace();
                print(tree.name);
            }
            print(cs.spaceBeforeMethodDeclParen() ? " (" : "(");
            if (cs.spaceWithinMethodDeclParens())
                print(' ');
            wrapTrees(tree.params, cs.wrapMethodParams(), cs.alignMultilineMethodParams()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
            if (cs.spaceWithinMethodDeclParens())
                needSpace();
            print(')');
            if (tree.thrown.nonEmpty()) {
                wrap("throws ", cs.wrapThrowsKeyword());
                wrapTrees(tree.thrown, cs.wrapThrowsList(), cs.alignMultilineThrows()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
            }
            if (tree.body != null) {
                boolean constructor = (tree.name == names.init);
                List<JCStatement> stats = tree.body.stats;
                JCTree head = stats.head;
                if(head instanceof JCExpressionStatement) head = ((JCExpressionStatement)head).expr;
                if(constructor && head instanceof JCMethodInvocation) {
                    JCMethodInvocation ap = (JCMethodInvocation) head;
                    if(ap.args.isEmpty() && ap.meth instanceof JCIdent) {
                        JCIdent id = (JCIdent) ap.meth;
                        Name n = id.sym==null ? id.name : id.sym.name;
                        if(n == names.init) {
                        /* We have an invocation of the null constructor
                           at the beginning of a constructor: eliminate it */
                            stats = stats.tail;
                        }
                    }
                }
                printBlock(tree.body, stats, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace());
            } else {
                print(';');
            }
            enclClassName = enclClassNamePrev;
	}
    }

    public void visitVarDef(JCVariableDecl tree) {
 	if ((tree.mods.flags & ENUM) != 0)
	    print(tree.name);
	else {
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags);
            Type type = tree.type != null ? tree.type : tree.vartype.type;
            if ((tree.mods.flags & VARARGS) != 0) {
                // Variable arity method. Expecting  ArrayType, print ... instead of [].
                // todo  (#pf): should we check the array type to prevent CCE?
                printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
                print("...");
            } else {
                print(tree.vartype, type);
            }
            needSpace();
            print(tree.name);
            if (tree.init != null) {
                if (cs.spaceAroundAssignOps())
                    print(' ');
                print('=');
                int rm = cs.getRightMargin();
                switch(cs.wrapAssignOps()) {
                case WRAP_IF_LONG:
                    if (widthEstimator.estimateWidth(tree.init, rm - out.col) + out.col <= cs.getRightMargin()) {
                        if(cs.spaceAroundAssignOps())
                            print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                    break;
                case WRAP_NEVER:
                    if(cs.spaceAroundAssignOps())
                        print(' ');
                    break;
                }
                printNoParenExpr(tree.init);
            }
            if (prec == treeinfo.notExpression)
                print(';');
	}
    }

    public void visitSkip(JCSkip tree) {
	print(';');
    }

    public void visitBlock(JCBlock tree) {
	printFlags(tree.flags, false);
	printBlock(tree, tree.stats, cs.getOtherBracePlacement(), (tree.flags & Flags.STATIC) != 0 ? cs.spaceBeforeStaticInitLeftBrace() : false);
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
	print("do");
        if (cs.spaceBeforeDoLeftBrace())
            print(' ');
	printIndentedStat(tree.body, cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace(), cs.wrapDoWhileStatement());
        boolean prevblock = tree.body.getKind() == Tree.Kind.BLOCK || cs.redundantDoWhileBraces() == BracesGenerationStyle.GENERATE;
        if (cs.placeWhileOnNewLine() || !prevblock) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeWhile()) {
	    needSpace();
        }
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens()? " );" : ");");
    }

    public void visitWhileLoop(JCWhileLoop tree) {
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace(), cs.wrapWhileStatement());
    }

    public void visitForLoop(JCForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        int col = out.col;
	if (tree.init.nonEmpty()) {
	    if (tree.init.head.tag == JCTree.VARDEF) {
		printNoParenExpr(tree.init.head);
		for (List<? extends JCTree> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
		    JCVariableDecl vdef = (JCVariableDecl) l.head;
		    print(", " + vdef.name + " = ");
		    printNoParenExpr(vdef.init);
		}
	    } else {
		printExprs(tree.init);
	    }
	}
        String sep = cs.spaceBeforeSemi() ? " ;" : ";";
	print(sep);
        if (tree.cond != null) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.cond, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
	    printNoParenExpr(tree.cond);
        }
	print(sep);
        if (tree.step.nonEmpty()) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.step, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
            printExprs(tree.step);
        }
	print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }

    public void visitLabelled(JCLabeledStatement tree) {
        toColExactly(cs.absoluteLabelIndent() ? 0 : out.leftMargin);
	print(tree.label);
	print(':');
        int old = out.leftMargin;
        out.leftMargin += cs.getLabelIndent();
        toColExactly(out.leftMargin);
	printStat(tree.body);
        undent(old);
    }

    public void visitSwitch(JCSwitch tree) {
	print("switch");
        print(cs.spaceBeforeSwitchParen() ? " (" : "(");
        if (cs.spaceWithinSwitchParens())
            print(' ');
	printNoParenExpr(tree.selector);
        print(cs.spaceWithinSwitchParens() ? " )" : ")");
        int bcol = out.leftMargin;
        switch(cs.getOtherBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(bcol);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (cs.getIndentSize() >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol += cs.getIndentSize();
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeSwitchLeftBrace())
            needSpace();
	print('{');
        if (tree.cases.nonEmpty()) {
            newline();
            printStats(tree.cases);
            toColExactly(bcol);
        }
	print('}');
    }

    public void visitCase(JCCase tree) {
        int old = cs.indentCasesFromSwitch() ? indent() : out.leftMargin; 
        toLeftMargin();
	if (tree.pat == null) {
	    print("default");
	} else {
	    print("case ");
	    printNoParenExpr(tree.pat);
	}
	print(':');
	newline();
	indent();
	printStats(tree.stats);
	undent(old);
    }

    public void visitSynchronized(JCSynchronized tree) {
	print("synchronized");
        print(cs.spaceBeforeSynchronizedParen() ? " (" : "(");
        if (cs.spaceWithinSynchronizedParens())
            print(' ');
	printNoParenExpr(tree.lock);
	print(cs.spaceWithinSynchronizedParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeSynchronizedLeftBrace());
    }

    public void visitTry(JCTry tree) {
	print("try");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeTryLeftBrace());
	for (List < JCCatch > l = tree.catchers; l.nonEmpty(); l = l.tail)
	    printStat(l.head);
	if (tree.finalizer != null) {
            if (cs.placeFinallyOnNewLine()) {
                newline();
                toLeftMargin();
            } else if (cs.spaceBeforeFinally()) {
                needSpace();
            }
	    print("finally");
	    printBlock(tree.finalizer, cs.getOtherBracePlacement(), cs.spaceBeforeFinallyLeftBrace());
	}
    }

    public void visitCatch(JCCatch tree) {
        if (cs.placeCatchOnNewLine()) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeCatch()) {
            needSpace();
        }
	print("catch");
        print(cs.spaceBeforeCatchParen() ? " (" : "(");
        if (cs.spaceWithinCatchParens())
            print(' ');
	printNoParenExpr(tree.param);
	print(cs.spaceWithinCatchParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
    }

    public void visitConditional(JCConditional tree) {
        printExpr(tree.cond, treeinfo.condPrec - 1);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.truepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? "? " : "?"); 
        printExpr(tree.truepart, treeinfo.condPrec);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.falsepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? ": " : ":"); 
        printExpr(tree.falsepart, treeinfo.condPrec);
    }

    public void visitIf(JCIf tree) {
	print("if");
        print(cs.spaceBeforeIfParen() ? " (" : "(");
        if (cs.spaceWithinIfParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinIfParens() ? " )" : ")");
        boolean prevblock = tree.thenpart.getKind() == Tree.Kind.BLOCK || cs.redundantIfBraces() == BracesGenerationStyle.GENERATE;
	if (tree.elsepart != null && danglingElseChecker.hasDanglingElse(tree.thenpart)) {
	    printBlock(tree.thenpart, cs.getOtherBracePlacement(), cs.spaceBeforeIfLeftBrace());
	    prevblock = true;
	} else
	    printIndentedStat(tree.thenpart, cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace(), cs.wrapIfStatement());
	if (tree.elsepart != null) {
	    if (cs.placeElseOnNewLine() || !prevblock) {
                newline();
                toLeftMargin();
            } else if (cs.spaceBeforeElse()) {
		needSpace();
            }
	    print("else");
	    if (tree.elsepart.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
		needSpace();
		printStat(tree.elsepart);
	    } else
		printIndentedStat(tree.elsepart, cs.redundantIfBraces(), cs.spaceBeforeElseLeftBrace(), cs.wrapIfStatement());
	}
    }

    public void visitExec(JCExpressionStatement tree) {
	printNoParenExpr(tree.expr);
	if (prec == treeinfo.notExpression)
	    print(';');
    }

    public void visitBreak(JCBreak tree) {
	print("break");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    public void visitContinue(JCContinue tree) {
	print("continue");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    public void visitReturn(JCReturn tree) {
	print("return");
	if (tree.expr != null) {
	    needSpace();
	    printNoParenExpr(tree.expr);
	}
	print(';');
    }

    public void visitThrow(JCThrow tree) {
	print("throw ");
	printNoParenExpr(tree.expr);
	print(';');
    }

    public void visitAssert(JCAssert tree) {
	print("assert ");
	printExpr(tree.cond);
	if (tree.detail != null) {
            print(cs.spaceBeforeColon() ? " :" : ":");
            switch(cs.wrapAssert()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.detail, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterColon())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterColon())
                    print(' ');
                break;
            }
	    printExpr(tree.detail);
	}
	print(';');
    }
    
    public void visitApply(JCMethodInvocation tree) {
        int prevPrec = this.prec;
        this.prec = treeinfo.postfixPrec;
        if (tree.meth.tag == JCTree.SELECT) {
            JCFieldAccess left = (JCFieldAccess)tree.meth;
            printExpr(left.selected);
            print('.');
            if (left.selected.tag == JCTree.APPLY) {
                switch(cs.wrapChainedMethodCalls()) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    int estWidth = left.name.length();
                    if (tree.typeargs.nonEmpty())
                        estWidth += widthEstimator.estimateWidth(tree.typeargs, rm - out.col - estWidth) + 2;
                    estWidth += widthEstimator.estimateWidth(tree.args, rm - out.col - estWidth) + 2;
                    if (estWidth + out.col <= rm)
                        break;
                case WRAP_ALWAYS:
                    toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                    break;
                }
            }
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            print(left.name);
        } else {
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            printExpr(tree.meth);
        }
        this.prec = prevPrec;
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() ? " )" : ")");
    }

    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    printExpr(tree.encl);
	    print('.');
	}
	print("new ");
        if (tree.typeargs.nonEmpty()) {
            print("<");
            printExprs(tree.typeargs);
            print(">");
        }
	if (tree.encl == null)
	    print(tree.clazz, tree.clazz.type);
	else if (tree.clazz.type != null)
	    print(tree.clazz.type.tsym.name);
	else
	    print(tree.clazz);
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() ? " )" : ")");
	if (tree.def != null) {
	    Name enclClassNamePrev = enclClassName;
	    enclClassName = null;
	    printBlock(null, ((JCClassDecl) tree.def).defs, cs.getOtherBracePlacement(), cs.spaceBeforeClassDeclLeftBrace());
	    enclClassName = enclClassNamePrev;
	}
    }

    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    print("new ");
	    int n = tree.elems != null ? 1 : 0;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.tag == JCTree.TYPEARRAY) {
		n++;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    printExpr(elemtype);
	    for (List<? extends JCTree> l = tree.dims; l.nonEmpty(); l = l.tail) {
		print(cs.spaceWithinArrayInitBrackets() ? "[ " : "[");
		printNoParenExpr(l.head);
		print(cs.spaceWithinArrayInitBrackets() ? " ]" : "]");
		n--;
	    }
	    while(--n >= 0) 
                print(cs.spaceWithinArrayInitBrackets() ? "[ ]" : "[]");
	}
	if (tree.elems != null) {
            if (cs.spaceBeforeArrayInitLeftBrace())
                needSpace();
	    print('{');
            if (cs.spaceWithinBraces())
                print(' ');
	    wrapTrees(tree.elems, cs.wrapArrayInit(), cs.alignMultilineArrayInit()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    print(cs.spaceWithinBraces() ? " }" : "}");
	}
    }

    public void visitParens(JCParens tree) {
	print('(');
        if (cs.spaceWithinParens())
            print(' ');
	printExpr(tree.expr);
	print(cs.spaceWithinParens() ? " )" : ")");
    }

    public void visitAssign(JCAssign tree) {
        int col = out.col;
	printExpr(tree.lhs, treeinfo.assignPrec + 1);
	if (cs.spaceAroundAssignOps())
            print(' ');
	print('=');
	int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundAssignOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundAssignOps())
                print(' ');
            break;
        }
	printExpr(tree.rhs, treeinfo.assignPrec);
    }

    public void visitAssignop(JCAssignOp tree) {
        int col = out.col;
	printExpr(tree.lhs, treeinfo.assignopPrec + 1);
	if (cs.spaceAroundAssignOps())
            print(' ');
	print(treeinfo.operatorName(tree.tag - JCTree.ASGOffset));
        print('=');
	int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundAssignOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundAssignOps())
                print(' ');
            break;
        }
	printExpr(tree.rhs, treeinfo.assignopPrec);
    }

    public void visitUnary(JCUnary tree) {
	int ownprec = treeinfo.opPrec(tree.tag);
	Name opname = treeinfo.operatorName(tree.tag);
	if (tree.tag <= JCTree.PREDEC) {
            if (cs.spaceAroundUnaryOps()) {
                needSpace();
                print(opname);
                print(' ');
            } else {
                print(opname);
            }
	    printExpr(tree.arg, ownprec);
	} else {
	    printExpr(tree.arg, ownprec);
            if (cs.spaceAroundUnaryOps()) {
                print(' ');
                print(opname);
                print(' ');
            } else {
                print(opname);
            }
	}
    }

    public void visitBinary(JCBinary tree) {
	int ownprec = treeinfo.opPrec(tree.tag);
	Name opname = treeinfo.operatorName(tree.tag);
        int col = out.col;
	printExpr(tree.lhs, ownprec);
	if(cs.spaceAroundBinaryOps())
            print(' ');
	print(opname);
	int rm = cs.getRightMargin();
        switch(cs.wrapBinaryOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundBinaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(cs.alignMultilineBinaryOp() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundBinaryOps())
                print(' ');
            break;
        }
	printExpr(tree.rhs, ownprec + 1);
    }

    public void visitTypeCast(JCTypeCast tree) {
	print(cs.spaceWithinTypeCastParens() ? "( " : "(");
	print(tree.clazz, tree.clazz.type);
	print(cs.spaceWithinTypeCastParens() ? " )" : ")");
        if (cs.spaceAfterTypeCast())
            needSpace();
        if (origUnit != null && TreePath.getPath(origUnit, tree.expr) != null) {
            int a = TreeInfo.getStartPos(tree.expr);
            int b = TreeInfo.getEndPos(tree.expr, origUnit.endPositions);
            print(cInfo.getText().substring(a, b));
            return;
        }
	printExpr(tree.expr, treeinfo.prefixPrec);
    }

    public void visitTypeTest(JCInstanceOf tree) {
	printExpr(tree.expr, treeinfo.ordPrec);
	print(" instanceof ");
	print(tree.clazz, tree.clazz.type);
    }

    public void visitIndexed(JCArrayAccess tree) {
	printExpr(tree.indexed, treeinfo.postfixPrec);
	print('[');
	printExpr(tree.index);
	print(']');
    }

    public void visitSelect(JCFieldAccess tree) {
	if ((fromOffset < 0 || toOffset < 0) && tree.sym instanceof Symbol.ClassSymbol) {
	    print(null, tree.type);
	} else {
	    printExpr(tree.selected, treeinfo.postfixPrec);
	    print('.');
	    print(tree.sym==null ? tree.name : tree.sym.name);
	}
    }

    public void visitIdent(JCIdent tree) {
	if ((fromOffset < 0 || toOffset < 0) && tree.sym instanceof Symbol.ClassSymbol)
	    print(null, tree.type);
	else {
	    Name n = tree.sym==null ? tree.name : tree.sym.name;
	    if(n==names.init) print(tree.name);
	    else print(n);
	}
    }

    public void visitLiteral(JCLiteral tree) {
	switch (tree.typetag) {
	  case INT:
	    print(tree.value.toString());
	    break;
	  case LONG:
	    print(tree.value.toString() + "L");
	    break;
	  case FLOAT:
	    print(tree.value.toString() + "F");
	    break;
	  case DOUBLE:
	    print(tree.value.toString());
	    break;
	  case CHAR:
	    print("\'" +
		  Convert.quote(
		  String.valueOf((char) ((Number) tree.value).intValue())) +
		  "\'");
	    break;
	   case CLASS:
	    print("\"" + Convert.quote((String) tree.value) + "\"");
	    break;
          case BOOLEAN:
            print(tree.getValue().toString());
            break;
          case BOT:
            print("null");
            break;
	  default:
	    print(tree.value.toString());
	}
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
	print(symbols.typeOfTag[tree.typetag].tsym.name);
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
	printExpr(tree.elemtype);
	print("[]");
    }

    public void visitTypeApply(JCTypeApply tree) {
	printExpr(tree.clazz);
	print('<');
	printExprs(tree.arguments);
	print('>');
    }

    public void visitTypeParameter(JCTypeParameter tree) {
	print(tree.name);
	if (tree.bounds.nonEmpty()) {
	    print(" extends ");
	    printExprs(tree.bounds, " & ");
	}
    }
    
    public void visitWildcard(JCWildcard tree) {
	print("" + tree.kind);
	if (tree.kind != BoundKind.UNBOUND)
	    printExpr(tree.inner);
    }
    
    public void visitModifiers(JCModifiers tree) {
	printAnnotations(tree.annotations);
	printFlags(tree.flags);
    }
    
    public void visitAnnotation(JCAnnotation tree) {
	print("@");
	printExpr(tree.annotationType);
        if (tree.args.nonEmpty()) {
            print(cs.spaceBeforeAnnotationParen() ? " (" : "(");
            if (cs.spaceWithinAnnotationParens())
                print(' ');
            printExprs(tree.args);
            print(cs.spaceWithinAnnotationParens() ? " )" : ")");
        }
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        printExpr(tree.getVariable());
        String sep = cs.spaceBeforeColon() ? " :" : ":";
        print(cs.spaceAfterColon() ? sep + " " : sep);
        printExpr(tree.getExpression());
        print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.getStatement(), cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }
    
    public void visitLetExpr(LetExpr tree) {
	print("(let " + tree.defs + " in " + tree.expr + ")");
    }

    public void visitErroneous(JCErroneous tree) {
	print("(ERROR)");
        containsError = true;
    }

    public void visitTree(JCTree tree) {
	print("(UNKNOWN: " + tree + ")");
	newline();
    }

    /**************************************************************************
     * Private implementation
     *************************************************************************/

    private void print(char c) {
	out.append(c);
    }

    private void needSpace() {
	out.needSpace();
    }

    private void blankLines(int n) {
        out.blanklines(n);
    }
    
    private void blankLines(JCTree tree, boolean before) {
        int n = 0;
        switch (tree.getKind()) {
            case CLASS:
                n = before ? cs.getBlankLinesBeforeClass() : cs.getBlankLinesAfterClass();
        	if (((JCClassDecl) tree).defs.nonEmpty() && !before) n = 0;
                else out.blanklines(n);
                return;
            case METHOD: // do not handle for sythetic things
        	if ((((JCMethodDecl) tree).mods.flags & Flags.SYNTHETIC) == 0 && 
                    ((JCMethodDecl) tree).name != names.init || 
                    enclClassName != null)
                {
                    n = before ? cs.getBlankLinesBeforeMethods() : cs.getBlankLinesAfterMethods();
                    out.blanklines(n);
        	    toLeftMargin();
                }
                return;
            case VARIABLE: // just for the fields
                if (enclClassName != null && enclClassName != names.empty && (((JCVariableDecl) tree).mods.flags & ENUM) == 0) {
                    n = before ? cs.getBlankLinesBeforeFields() : cs.getBlankLinesAfterFields();
                    out.blanklines(n);
                    if (before) toLeftMargin();
                }
                return;
        }
    }
    
    private void toColExactly(int n) {
	if (n < out.col) newline();
	out.toCol(n);
    }

    private void printQualified(Symbol t) {
	if (t.owner != null && t.owner.name.len > 0
		&& !(t.type instanceof Type.TypeVar)
		&& !(t.owner instanceof MethodSymbol)) {
	    if (t.owner instanceof Symbol.PackageSymbol)
		printAllQualified(t.owner);
	    else
		printQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }

    private void printAllQualified(Symbol t) {
	if (t.owner != null && t.owner.name.len > 0) {
	    printAllQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }
    
    private void print(JCTree t, Type ty) {
	if (ty == null || ty == Type.noType || (fromOffset >= 0 && toOffset >= 0)) {
	    print(t);
	} else {
	    int arrCnt = 0;
	    while (ty instanceof Type.ArrayType) {
		ty = ((Type.ArrayType) ty).elemtype;
		arrCnt++;
	    }
	    printQualified(ty.tsym);
	    if (ty instanceof Type.ClassType) {
		List < Type > typarams = ((Type.ClassType) ty).typarams_field;
		if (typarams != null && typarams.nonEmpty()) {
                    print('<');
		    for (; typarams.nonEmpty(); typarams = typarams.tail) {
			print(null, typarams.head);
                        if (typarams.tail.nonEmpty()) {
                            if (cs.spaceBeforeComma())
                                print(' ');
                            print(cs.spaceAfterComma() ? ", " : ",");
                        }
		    }
		    print('>');
		}
	    }
	    while (--arrCnt >= 0)
		print("[]");
	}
    }

    private void printAnnotations(List<JCAnnotation> annotations) {
        while (annotations.nonEmpty()) {
	    printNoParenExpr(annotations.head);
            if (annotations.tail != null && annotations.tail.nonEmpty()) {
                switch(cs.wrapAnnotations()) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    if (widthEstimator.estimateWidth(annotations.tail.head, rm - out.col) + out.col + 1 <= rm) {
                        print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    toColExactly(out.leftMargin);
                    break;
                case WRAP_NEVER:
                    print(' ');
                    break;
                }
            } else { 
                toColExactly(out.leftMargin);
            }
            annotations = annotations.tail;
        }
    }

    public void printFlags(long flags) {
        printFlags(flags, true);
    }
    
    public void printFlags(long flags, boolean addSpace) {
	print(treeinfo.flagNames(flags));
        if ((flags & StandardFlags) != 0) {
            if (cs.placeNewLineAfterModifiers())
                toColExactly(out.leftMargin);
            else if (addSpace)
	        needSpace();
        }
    }

    private void printExpr(JCTree tree) {
	printExpr(tree, treeinfo.noPrec);
    }

    private void printNoParenExpr(JCTree tree) {
	while (tree instanceof JCParens)
	    tree = ((JCParens) tree).expr;
	printExpr(tree, treeinfo.noPrec);
    }

    private void printExpr(JCTree tree, int prec) {
	if (tree == null) {
	    print("/*missing*/");
	} else {
	    int prevPrec = this.prec;
	    this.prec = prec;
            tree.accept(this);
	    this.prec = prevPrec;
	}
    }

    private <T extends JCTree >void printExprs(List < T > trees) {
        String sep = cs.spaceBeforeComma() ? " ," : ",";
	printExprs(trees, cs.spaceAfterComma() ? sep + " " : sep);
    }

    private <T extends JCTree >void printExprs(List < T > trees, String sep) {
	if (trees.nonEmpty()) {
	    printNoParenExpr(trees.head);
	    for (List < T > l = trees.tail; l.nonEmpty(); l = l.tail) {
		print(sep);
		printNoParenExpr(l.head);
	    }
	}
    }
    
    static final EnumSet<JavaTokenId> nonRelevant = EnumSet.of(
            JavaTokenId.LINE_COMMENT,
            JavaTokenId.BLOCK_COMMENT,
            JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.WHITESPACE
        );
    
    private void printStat(JCTree tree) {
	if(tree==null) print(';');
	else {
            blankLines(tree, true);
	    printPrecedingComments(tree);
            printExpr(tree, treeinfo.notExpression);
	    int tag = tree.tag;
	    if(JCTree.APPLY<=tag && tag<=JCTree.MOD_ASG) print(';');
            printTrailingComments(tree);
            blankLines(tree, false);
	}
    }

    private void printIndentedStat(JCTree tree, BracesGenerationStyle redundantBraces, boolean spaceBeforeLeftBrace, WrapStyle wrapStat) {
        if (fromOffset >= 0 && toOffset >= 0 && (TreeInfo.getStartPos(tree) < fromOffset || TreeInfo.getEndPos(tree, origUnit.endPositions) > toOffset))
            redundantBraces = BracesGenerationStyle.LEAVE_ALONE;
	switch(redundantBraces) {
        case GENERATE:
            printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
            return;
        case ELIMINATE:
	    while(tree instanceof JCBlock) {
		List<JCStatement> t = ((JCBlock) tree).stats;
		if(t.isEmpty() || t.tail.nonEmpty()) break;
		if (t.head instanceof JCVariableDecl)
		    // bogus code has a variable declaration -- leave alone.
		    break;
		printPrecedingComments(tree);
		tree = t.head;
	    }
        case LEAVE_ALONE:
            if (tree instanceof JCBlock) {
                printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
                return;
            }
            int old = indent();
            switch(wrapStat) {
            case WRAP_NEVER:
                if (spaceBeforeLeftBrace)
                    needSpace();
                printStat(tree);
                undent(old);
                return;
            case WRAP_IF_LONG:
                int oldhm = out.harden();
                int oldc = out.col;
                int oldu = out.used;
                int oldm = out.leftMargin;
                try {
                    if (spaceBeforeLeftBrace)
                        needSpace();
                    printStat(tree);
                    undent(old);
                    out.restore(oldhm);
                    return;
                } catch(Throwable t) {
                    out.restore(oldhm);
                    out.col = oldc;
                    out.used = oldu;
                    out.leftMargin = oldm;
                }
            case WRAP_ALWAYS:
                if (out.col > 0)
                    newline();
                toLeftMargin();
                printStat(tree);
                undent(old);
            }
	}
    }

    private <T extends JCTree >void printStats(List < T > trees) {
	for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
	    T t = l.head;
	    if (isSynthetic(t))
		continue;
	    toColExactly(out.leftMargin);
	    printStat(t);
	}
    }
    
    private void printBlock(JCTree t, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace) {
        JCTree block;
	List<? extends JCTree> stats;
	if (t instanceof JCBlock) {
            block = t;
	    stats = ((JCBlock) t).stats;
        } else {
            block = null;
	    stats = List.of(t);
        }
	printBlock(block, stats, bracePlacement, spaceBeforeLeftBrace);
    }

    private void printBlock(JCTree tree, List<? extends JCTree> stats, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace) {
	int old = indent();
	int bcol = old;
        switch(bracePlacement) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (cs.getIndentSize() >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (spaceBeforeLeftBrace)
            needSpace();
	print('{');
	if (stats.nonEmpty()) {
	    newline();
	    printStats(stats);
        } else {
            printEmptyBlockComments(tree);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
    }

    private void printTypeParameters(List < JCTypeParameter > trees) {
	if (trees.nonEmpty()) {
	    print('<');
	    printExprs(trees);
	    print('>');
	}
    }

    private void printTypeArguments(List<? extends JCExpression> typeargs) {
        if (typeargs.nonEmpty()) {
            print('<');
            printExprs(typeargs);
            print('>');
        }
    }

    private void printPrecedingComments(JCTree tree) {
        CommentSet commentSet = commentHandler.getComments(tree);
        if (commentSet.getPrecedingComments().isEmpty() && cInfo != null) {
            int startPos = TreeInfo.getStartPos(tree);
            int endPos = TreeInfo.getEndPos(tree, origUnit.endPositions);
            if (startPos >= 0 && endPos >= 0) {
                if (pendingComments != null) {
                    commentSet.addPrecedingComments(pendingComments);
                    pendingComments = null;
                }
                TokenSequence<JavaTokenId> tokens = cInfo.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                tokens.move(startPos);
                PositionEstimator.moveToSrcRelevant(tokens, Direction.BACKWARD);
                int indent = Query.NOPOS;
                while (tokens.moveNext() && PositionEstimator.nonRelevant.contains(tokens.token().id())) {
                    if (tokens.index() > lastReadCommentIdx) {
                        switch (tokens.token().id()) {
                            case LINE_COMMENT:
                                commentSet.addPrecedingComment(Comment.create(Style.LINE, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = 0;
                                break;
                            case BLOCK_COMMENT:
                                commentSet.addPrecedingComment(Comment.create(Style.BLOCK, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case JAVADOC_COMMENT:
                                commentSet.addPrecedingComment(Comment.create(Style.JAVADOC, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case WHITESPACE:
                                String tokenText = tokens.token().toString();
                                commentSet.addPrecedingComment(Comment.create(Style.WHITESPACE, Query.NOPOS, Query.NOPOS, Query.NOPOS, tokenText));
                                int newLinePos = tokenText.lastIndexOf('\n');
                                if (newLinePos < 0) {
                                    if (indent >= 0)
                                        indent += tokenText.length();
                                } else {
                                    indent = tokenText.length() - newLinePos - 1;
                                }
                                break;
                        }
                        lastReadCommentIdx = tokens.index();
                    }
                }
            }
        }
        if (!commentSet.hasComments())
            return;
        for (Comment c : commentSet.getPrecedingComments())
            printComment(c, true);
    }

    private void printTrailingComments(JCTree tree) {
        CommentSet commentSet = commentHandler.getComments(tree);
        if (commentSet.getTrailingComments().isEmpty() && cInfo != null) {
            int pos = TreeInfo.getEndPos(tree, origUnit.endPositions);
            if (pos >= 0) {
                if (pendingComments == null)
                    pendingComments = new LinkedList<Comment>();
                TokenSequence<JavaTokenId> tokens = cInfo.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                tokens.move(pos);
                boolean afterNewline = false;
                int indent = Query.NOPOS;
                outer:
                while (tokens.moveNext() && (PositionEstimator.nonRelevant.contains(tokens.token().id()) || PositionEstimator.isSeparator(tokens.token().id()))) {
                    if (tokens.index() > lastReadCommentIdx) {
                        switch (tokens.token().id()) {
                            case LINE_COMMENT:
                                pendingComments.add(Comment.create(Style.LINE, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                if (!afterNewline) {
                                    commentSet.addTrailingComments(pendingComments);
                                    pendingComments = new LinkedList<Comment>();
                                    afterNewline = true;
                                }
                                indent = 0;
                                break;
                            case BLOCK_COMMENT:
                                pendingComments.add(Comment.create(Style.BLOCK, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case JAVADOC_COMMENT:
                                pendingComments.add(Comment.create(Style.JAVADOC, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case WHITESPACE:
                                String tokenText = tokens.token().toString();
                                pendingComments.add(Comment.create(Style.WHITESPACE, Query.NOPOS, Query.NOPOS, Query.NOPOS, tokenText));
                                int newLinePos = tokenText.lastIndexOf('\n');
                                if (!afterNewline && newLinePos >= 0) {
                                    commentSet.addTrailingComments(pendingComments);
                                    pendingComments = new LinkedList<Comment>();
                                    afterNewline = true;
                                }
                                if (newLinePos < 0) {
                                    if (indent >= 0)
                                        indent += tokenText.length();
                                } else {
                                    indent = tokenText.length() - newLinePos - 1;
                                }
                                break;
                            case RBRACE:
                            case SEMICOLON:
                                commentSet.addTrailingComments(pendingComments);
                                pendingComments = null;
                                indent = Query.NOPOS;
                                break outer;
                        }
                        lastReadCommentIdx = tokens.index();
                    }
                }
            }
        }
        if (!commentSet.hasComments())
            return;
        for (Comment c : commentSet.getTrailingComments())
            printComment(c, false);
    }

    private void printEmptyBlockComments(JCTree tree) {
        LinkedList<Comment> comments = new LinkedList<Comment>();
        if (cInfo != null) {
            int pos = TreeInfo.getEndPos(tree, origUnit.endPositions) - 1;
            if (pos >= 0) {
                TokenSequence<JavaTokenId> tokens = cInfo.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                tokens.move(pos);
                PositionEstimator.moveToSrcRelevant(tokens, Direction.BACKWARD);
                int indent = Query.NOPOS;
                while (tokens.moveNext() && PositionEstimator.nonRelevant.contains(tokens.token().id())) {
                    if (tokens.index() > lastReadCommentIdx) {
                        switch (tokens.token().id()) {
                            case LINE_COMMENT:
                                comments.add(Comment.create(Style.LINE, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = 0;
                                break;
                            case BLOCK_COMMENT:
                                comments.add(Comment.create(Style.BLOCK, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case JAVADOC_COMMENT:
                                comments.add(Comment.create(Style.JAVADOC, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
                                indent = Query.NOPOS;
                                break;
                            case WHITESPACE:
                                String tokenText = tokens.token().toString();
                                comments.add(Comment.create(Style.WHITESPACE, Query.NOPOS, Query.NOPOS, Query.NOPOS, tokenText));
                                int newLinePos = tokenText.lastIndexOf('\n');
                                if (newLinePos < 0) {
                                    if (indent >= 0)
                                        indent += tokenText.length();
                                } else {
                                    indent = tokenText.length() - newLinePos - 1;
                                }
                                break;
                        }
                        lastReadCommentIdx = tokens.index();
                    }
                }
            }
        }
        for (Comment c : comments)
            printComment(c, false);
    }

    private void printComment(Comment comment, boolean preceding) {
        if (Comment.Style.WHITESPACE == comment.style()) {
            char[] data = comment.getText().toCharArray();
            int n = -1;
            for (int i = 0; i < data.length; i++) {
                if (data[i] == '\n') {
                    n++;
                }
            }
            if (n > 0) {
                if (out.lastBlankLines > 0 && out.lastBlankLines < n)
                    n = out.lastBlankLines;
                blankLines(n);
                toLeftMargin();
            }
            return;
        }
	String body = comment.getText();
        boolean rawBody = body.charAt(0) != '/';
        LinkedList<CommentLine> lines = new LinkedList<CommentLine>();
	int stpos = -1;
	int limit = body.length();
	for (int i = 0; i<limit; i++) {
	    char c = body.charAt(i);
            if (c == '\n') {
                lines.add(new CommentLine(stpos, stpos < 0 ? 0 : i - stpos, body));
                stpos = -1;
            } else if (c > ' ' && stpos < 0) {
                stpos = i;
            }
        }
        if (stpos >= 0 && stpos < limit)
	    lines.add(new CommentLine(stpos, limit - stpos, body));
        if (comment.indent() == 0) {
            newline();
        } else if (comment.indent() > 0 && !preceding) {
            newline();
            toLeftMargin();
        } else {
            needSpace();
        }
        if (rawBody) {
            switch(comment.style()) {
            case LINE:
                print("// ");
                break;
            case BLOCK:
                print("/* ");
                break;
            case JAVADOC:
                print("/**");
                newline();
                toLeftMargin();
                print(" * ");
            }
        }
        lines.removeFirst().print(out.col);
        while (!lines.isEmpty()) {
            newline();
            toLeftMargin();
            CommentLine line = lines.removeFirst();
            if (rawBody)
                print(" * ");
            else if (line.body.charAt(line.startPos) == '*')
                print(' ');
            line.print(out.col);
        }
        if (rawBody) {
            switch(comment.style()) {
            case BLOCK:
                print(" */");
                break;
            case JAVADOC:
                newline();
                toLeftMargin();
                print(" */");
                break;
            }
        }
        if (comment.indent() > 0) {
            newline();
            toLeftMargin();
        } else if (comment.style() == Comment.Style.LINE) {
            newline();
        } else {
           needSpace();
        }
    }

    private void wrap(String s, WrapStyle wrapStyle) {
        switch(wrapStyle) {
        case WRAP_IF_LONG:
            if (s.length() + out.col + 1 <= cs.getRightMargin()) {
                print(' ');
                break;
            }
        case WRAP_ALWAYS:
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            print(' ');
            break;
        }
        print(s);
    }

    private <T extends JCTree> void wrapTrees(List<T> trees, WrapStyle wrapStyle, int wrapIndent) {
        boolean first = true;
        for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
            if (!first) {
                print(cs.spaceBeforeComma() ? " ," : ",");
                switch(wrapStyle) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    if (widthEstimator.estimateWidth(l.head, rm - out.col) + out.col + 1 <= rm) {
                        if (cs.spaceAfterComma())
                            print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    toColExactly(wrapIndent);
                    break;
                case WRAP_NEVER:
                    if (cs.spaceAfterComma())
                        print(' ');
                    break;
                }
            }
            printNoParenExpr(l.head);
            first = false;
        }
    }

    private Name fullName(JCTree tree) {
	switch (tree.tag) {
	case JCTree.IDENT:
	    return ((JCIdent) tree).name;
	case JCTree.SELECT:
            JCFieldAccess sel = (JCFieldAccess)tree;
	    Name sname = fullName(sel.selected);
	    return sname != null && sname.len > 0 ? sname.append('.', sel.name) : sel.name;
	default:
	    return null;
	}
    }

    // consider usage of TreeUtilities.isSynthethic() - currently tree utilities
    // is not available in printing class and method is insufficient for our
    // needs.
    private boolean isSynthetic(JCTree tree) {
        // filter syntetic constructors
        if (Kind.METHOD == tree.getKind() && (((JCMethodDecl) tree).mods.flags & Flags.GENERATEDCONSTR) != 0)
            return true;
        // todo (#pf): original method - useless IMO, left here till all 
        // issues with synthetic things will not be finished.
	return false;
    }
    
    /** Is the given tree an enumerator definition? */
    private boolean isEnumerator(JCTree tree) {
        return tree.tag == JCTree.VARDEF && (((JCVariableDecl) tree).mods.flags & ENUM) != 0;
    }

    private String replace(String a,String b) {
        a = a.replace(b, out.toString());
        out.clear();
        return a;
    }

    private class CommentLine {
	private int startPos;
	private int length;
        private String body;
	CommentLine(int sp, int l, String b) {
	    if((length = l)==0) {
		startPos = 0;
	    } else {
		startPos = sp;
            }
            body = b;
	}
	public void print(int col) {
	    if(length>0) {
		int limit = startPos+length;
		for(int i = startPos; i<limit; i++)
		    out.append(body.charAt(i));
	    }
	}
    }
}
