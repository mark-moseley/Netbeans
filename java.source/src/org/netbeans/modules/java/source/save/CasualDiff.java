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
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.UndoListService;
import org.netbeans.api.java.source.transform.UndoList;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.query.CommentHandler;
import org.netbeans.api.java.source.query.CommentSet;
import org.netbeans.modules.java.source.engine.ASTModel;
import org.netbeans.api.java.source.query.Query;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.engine.SourceRewriter;
import org.netbeans.modules.java.source.engine.StringSourceRewriter;
import org.netbeans.modules.java.source.engine.JavaFormatOptions;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.ListMatcher;
import org.netbeans.modules.java.source.save.TreeDiff.LineInsertionType;
import static org.netbeans.modules.java.source.save.ListMatcher.*;
import static com.sun.tools.javac.code.Flags.*;
import static org.netbeans.modules.java.source.save.TreeDiff.*;

public class CasualDiff {
    protected ListBuffer<Diff> diffs;
    protected CommentHandler comments;
    protected ASTModel model;
    protected UndoList undo;
    protected JCTree oldParent;
    protected JCTree newParent;
    protected JCCompilationUnit oldTopLevel;
    
    private WorkingCopy workingCopy;
    private TokenSequence<JavaTokenId> tokenSequence;
    private SourceRewriter output;
    private String origText;
    private VeryPretty printer;
    private int pointer;
    private Context context;

    private Map<Integer, String> diffInfo = new HashMap<Integer, String>();
    
    /** provided for test only */
    public CasualDiff() {
    }

    protected CasualDiff(Context context, WorkingCopy workingCopy) {
        diffs = new ListBuffer<Diff>();
        comments = CommentHandlerService.instance(context);
        model = ASTService.instance(context);
        undo = UndoListService.instance(context);
        this.workingCopy = workingCopy;
        this.tokenSequence = workingCopy.getTokenHierarchy().tokenSequence();
        this.output = new StringSourceRewriter();
        this.origText = workingCopy.getText();
        this.context = context;
        printer = new VeryPretty(context, JavaFormatOptions.getDefault());
    }
    
    public com.sun.tools.javac.util.List<Diff> getDiffs() {
        return diffs.toList();
    }
    
    public static com.sun.tools.javac.util.List<Diff> diff(Context context,
            WorkingCopy copy,
            JCTree oldTree,
            JCTree newTree) 
    {
        CasualDiff td = new CasualDiff(context, copy);
        try {
            td.diffTree(oldTree, newTree);
            String resultSrc = td.output.toString();
            td.makeListMatch(td.workingCopy.getText(), resultSrc);
            JavaSourceAccessor.INSTANCE.getCommandEnvironment(td.workingCopy).setResult(td.diffInfo, "user-info");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return td.getDiffs();
    }
    
    private void append(Diff diff) {
        // check if diff already found -- true for variables that share 
        // fields, such as the mods for "public int foo, bar;"
        for (Diff d : diffs)
            if (d.equals(diff))
                return;
        diffs.append(diff);
    }
    
    // todo (#pf): Is this really needed? -- Seems it duplicates the work
    // in SourcePositions, but in different way. Uses map of endPositions.
    // Look into the implementation and try to use SourcePositions!
    private int endPos(JCTree t) {
        return model.getEndPos(t, oldTopLevel);
    }
    
    private int endPos(com.sun.tools.javac.util.List<? extends JCTree> trees) {
        int result = -1;
	if (trees.nonEmpty()) {
	    result = endPos(trees.head);
	    for (com.sun.tools.javac.util.List <? extends JCTree> l = trees.tail; l.nonEmpty(); l = l.tail) {
		result = endPos(l.head);
	    }
	}
        return result;
    }

    protected void diffTopLevel(JCCompilationUnit oldT, JCCompilationUnit newT) {
        oldTopLevel = oldT;
        diffList(oldT.packageAnnotations, newT.packageAnnotations, LineInsertionType.NONE, 0);
        int posHint;
        // no package declaration available (default package)
        if (oldT.pid == null) {
            if (oldT.getImports().head != null) {
                posHint = oldT.getImports().head.getStartPosition();
            } else if (oldT.getTypeDecls().head != null) {
                posHint = oldT.getTypeDecls().head.getStartPosition();
            } else {
                // there is neither package declaration nor
                // import nor class definition. Who wrote 
                // such a file? Make package declaration
                // at the beginning of file.
                posHint = 1;
            }
        } else {
            // replacing old declaration.
            posHint = endPos(oldT.pid);
        }
        try {
            diffPackageStatement(oldT, newT);
            if (oldT.getImports().isEmpty()) {
                // imports are not available, compute position from package and
                // type decl position.
                // XXX: empty lines!!! (if there is not empty line between
                // package and type decl. Also, when package and imports are
                // unavailable
                if (oldT.pid != null) {
                    // hint pos is at the end of package statement before
                    // its semicolon. Move it after the semicolon.
                    // XXX todo (#pf): ensure there are enough empty lines.
                    posHint = TokenUtilities.moveFwdToToken(tokenSequence, posHint, JavaTokenId.SEMICOLON);
                    posHint += JavaTokenId.SEMICOLON.fixedText().length();
                }
            } else {
                posHint = oldT.getImports().head.pos;
            }
            printer.reset(0);
            int[] pos = diffList(oldT.getImports(), newT.getImports(), posHint, EstimatorFactory.imports(), Measure.DEFAULT, printer);
            if (pointer < pos[0])
                output.writeTo(origText.substring(pointer, pos[0]));
            if (pos[1] > pointer)
                pointer = pos[1];
            if (pos[0] == -1 && pos[1] == -1) {
                // they match
                output.writeTo(origText.substring(pointer, pointer = posHint));
            } else {
                output.writeTo(printer.toString());
            }
            if (oldT.getTypeDecls().nonEmpty()) {
                posHint = getOldPos(oldT.getTypeDecls().head);
            } else {
                // todo (#pf): this has to be fixed, missing code here.
            }
            printer.reset(0);
            pos = diffList(oldT.getTypeDecls(), newT.getTypeDecls(), posHint, EstimatorFactory.toplevel(), Measure.DEFAULT, printer);
            if (pointer < pos[0])
                output.writeTo(origText.substring(pointer, pos[0]));
            if (pos[1] > pointer) {
                pointer = pos[1];
            }
            output.writeTo(printer.toString());
            output.writeTo(origText.substring(pointer));
        } catch (Exception e) {
            // report an exception and do not any change in source to prevent deletion of code!
            Logger.getLogger("global").log(Level.SEVERE, "Error during generating!", e);
            this.output = new StringSourceRewriter();
            try {
                this.output.writeTo(origText);
            } catch (BadLocationException ex) {
            } catch (IOException ex) {
            }
        }
    }
    
    private static enum ChangeKind {
        INSERT,
        DELETE,
        MODIFY,
        NOCHANGE;
    }
    
    private ChangeKind getChangeKind(Tree oldT, Tree newT) {
        if (oldT == newT) {
            return ChangeKind.NOCHANGE;
        }
        if (oldT != null && newT != null) {
            return ChangeKind.MODIFY;
        }
        if (oldT != null) {
            return ChangeKind.DELETE;
        } else {
            return ChangeKind.INSERT;
        }
    }
    
    private void diffPackageStatement(JCCompilationUnit oldT, JCCompilationUnit newT) throws IOException, BadLocationException {
        ChangeKind change = getChangeKind(oldT.pid, newT.pid);
        printer.reset(0);
        switch (change) {
            // packages are the same or not available, i.e. both are null
            case NOCHANGE:
                break;
                
            // package statement is new, print the keyword and semicolon
            case INSERT:
                printer.print("package ");
                printer.print(newT.pid);
                printer.print(";");
                printer.newline();
                output.writeTo(printer.toString());
                break;
                
            // package statement was deleted.    
            case DELETE:
                TokenUtilities.movePrevious(tokenSequence, oldT.pid.getStartPosition());
                output.writeTo(origText.substring(pointer, tokenSequence.offset()));
                TokenUtilities.moveNext(tokenSequence, endPos(oldT.pid));
                pointer = tokenSequence.offset() + 1;
                break;
    
            // package statement was modified.
            case MODIFY:
                output.writeTo(origText.substring(pointer, getOldPos(oldT.pid)));
                pointer = endPos(oldT.pid);
                printer.print(newT.pid);
                output.writeTo(printer.toString());
                break;
        }
    }
    
    protected void diffImport(JCImport oldT, JCImport newT) {
        if (TreeInfo.fullName(oldT.qualid) != TreeInfo.fullName(newT.qualid))
            append(Diff.modify(oldT, getOldPos(oldT), newT));  // includes possible staticImport change
        else if (oldT.staticImport != newT.staticImport)
            append(Diff.flags(oldT.pos, endPos(oldT), 
                              oldT.staticImport ? Flags.STATIC : 0L,
                              newT.staticImport ? Flags.STATIC : 0L));
    }

    // need by visitMethodDef - in case of renaming class, we do not know
    // this name in constructor matcher - save it in diffClassDef() and use
    // in diffMethodDef(). Probably better to write method with additional
    // parameter which delegates to original one visitMethodDef().
    private Name origClassName = null;
    
    protected int diffClassDef(JCClassDecl oldT, JCClassDecl newT, int[] bounds) {
        int localPointer =  bounds[0];
        int insertHint = localPointer;
        JCTree opar = oldParent;
        oldParent = oldT;
        JCTree npar = newParent;
        newParent = newT;
        // skip the section when printing anonymous class
        if (anonClass == false) {
        tokenSequence.move(oldT.pos);
        tokenSequence.moveNext();
        insertHint = TokenUtilities.moveNext(tokenSequence, tokenSequence.offset());
        localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(localPointer, insertHint));
            printer.print(newT.name);
            diffInfo.put(insertHint, "Change class name");
            localPointer = insertHint += oldT.name.length();
            origClassName = oldT.name;
        } else {
            insertHint += oldT.name.length();
        }
        localPointer = diffParameterList(oldT.typarams, newT.typarams, localPointer);
        if (oldT.typarams.nonEmpty()) {
            // if type parameters exists, compute correct end of type parameters.
            // ! specifies the offset for insertHint var.
            // public class Yerba<E, M>! { ...
            insertHint = endPos(oldT.typarams.last());
            TokenUtilities.moveFwdToToken(tokenSequence, insertHint, JavaTokenId.GT);
            insertHint = tokenSequence.offset() + JavaTokenId.GT.fixedText().length();
        }
        switch (getChangeKind(oldT.extending, newT.extending)) {
            case NOCHANGE:
                insertHint = oldT.extending != null ? endPos(oldT.extending) : insertHint;
                printer.print(origText.substring(localPointer, localPointer = insertHint));
                break;
            case MODIFY:
                printer.print(origText.substring(localPointer, getOldPos(oldT.extending)));
                printer.print(newT.extending);
                localPointer = endPos(oldT.extending);
                break;
                
            case INSERT:
                printer.print(origText.substring(localPointer, insertHint));
                printer.print(" extends ");
                printer.print(newT.extending);
                localPointer = insertHint;
                break;
            case DELETE:
                printer.print(origText.substring(localPointer, insertHint));
                localPointer = endPos(oldT.extending);
                break;
        }
        // TODO (#pf): there is some space for optimization. If the new list
        // is also empty, we can skip this computation.
        if (oldT.implementing.isEmpty()) {
            // if there is not any implementing part, we need to adjust position
            // from different place. Look at the examples in all if branches.
            // | represents current adjustment and ! where we want to point to
            if (oldT.extending != null)
                // public class Yerba<E>| extends Object! { ...
                insertHint = endPos(oldT.extending);
            else {
                // currently no need to adjust anything here:
                // public class Yerba<E>|! { ...
            }
        } else {
            // we already have any implements, adjust position to first
            // public class Yerba<E>| implements !Mate { ...
            // Note: in case of all implements classes are removed,
            // diffing mechanism will solve the implements keyword.
            insertHint = oldT.implementing.iterator().next().getStartPosition();
        }
        long flags = oldT.sym != null ? oldT.sym.flags() : oldT.mods.flags;
        PositionEstimator estimator = (flags & INTERFACE) == 0 ? EstimatorFactory.implementz() : EstimatorFactory.extendz();
        if (!newT.implementing.isEmpty())
            printer.print(origText.substring(localPointer, insertHint));
        localPointer = diffList2(oldT.implementing, newT.implementing, insertHint, estimator);
        insertHint = endPos(oldT) - 1;

        if (oldT.defs.isEmpty()) {
            // if there is nothing in class declaration, use position
            // before the closing curly.
            // TODO (#pf): optimize new lines, this will look ugly. --
            // do before the last new line character before the closing curly.
            insertHint = endPos(oldT) - 1;
        } else {
            // XXX hack: be careful, syntetic constructor in head has the
            // same position as class declaration too - go to the next feature.
            // do not be upset to me for the next line, I will replace it
            // with some better and final solution soon, hopefully :-).
            JCTree t = oldT.defs.head.pos == oldT.pos ? oldT.defs.tail.head : oldT.defs.head;
            if (t != null) insertHint = t.getStartPosition();
        }
        } else {
            insertHint = TokenUtilities.moveFwdToToken(tokenSequence, getOldPos(oldT), JavaTokenId.LBRACE);
            tokenSequence.moveNext();
            insertHint = tokenSequence.offset();
        }
        int old = printer.indent();
        VeryPretty mujPrinter = new VeryPretty(context, JavaFormatOptions.getDefault());
        mujPrinter.reset(old);
        mujPrinter.indent();
        mujPrinter.enclClassName = newT.getSimpleName();
        int[] pos = diffList(filterHidden(oldT.defs), filterHidden(newT.defs), insertHint, EstimatorFactory.members(), Measure.DEFAULT, mujPrinter);
        if (localPointer < pos[0])
            printer.print(origText.substring(localPointer, pos[0]));
        printer.print(mujPrinter.toString());
        if (pos[1] != -1)
            printer.print(origText.substring(pos[1], bounds[1]));
        else
            printer.print(origText.substring(localPointer, bounds[1]));
        //pointer = bounds[1];
        oldParent = opar;
        newParent = npar;
        // the reference is no longer needed.
        origClassName = null;
        printer.undent(old);
        return bounds[1];
    }

    protected int diffMethodDef(JCMethodDecl oldT, JCMethodDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.mods != newT.mods) {
            if (newT.mods.toString().length() > 0) {
                localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
            } else {
                int oldPos = getOldPos(oldT.mods);
                printer.print(origText.substring(localPointer, oldPos));
                localPointer = getOldPos(oldT.restype);
            }
        }
        diffTree(oldT.restype, newT.restype);
        int posHint;
        if (oldT.typarams.isEmpty()) {
            posHint = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();
        } else {
            posHint = oldT.typarams.iterator().next().getStartPosition();
        }
        if (!oldT.sym.isConstructor() || origClassName != null) {
            if (nameChanged(oldT.name, newT.name)) {
                printer.print(origText.substring(localPointer, oldT.pos));
                // use orig class name in case of constructor
                if (oldT.sym.isConstructor() && (origClassName != null)) {
                    printer.print(newT.name);
                    localPointer = oldT.pos + origClassName.length();
                }
                else {
                    printer.print(newT.name);
                    localPointer = oldT.pos + oldT.name.length();
                }
            } else {
                printer.print(origText.substring(localPointer, localPointer = (oldT.pos + oldT.name.length())));
            }
        }
        diffParameterList(oldT.typarams, newT.typarams, posHint, ListType.TYPE_PARAMETER);
        if (oldT.params.isEmpty()) {
            // compute the position. Find the parameters closing ')', its
            // start position is important for us. This is used when 
            // there was not any parameter in original tree.
            int startOffset = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();
            
            tokenSequence.move(startOffset);
            TokenUtilities.moveFwdToToken(tokenSequence, startOffset, JavaTokenId.RPAREN);
            posHint = tokenSequence.offset();
        } else {
            // take the position of the first old parameter
            posHint = oldT.params.iterator().next().getStartPosition();
        }
        diffParameterList(oldT.params, newT.params, posHint, ListType.PARAMETER);
        // temporary
        tokenSequence.moveNext();
        posHint = tokenSequence.offset();
        if (localPointer < posHint)
            printer.print(origText.substring(localPointer, localPointer = posHint));
        // if abstract, hint is before ending semi-colon, otherwise before method body
        if (oldT.thrown.isEmpty()) {
            posHint = (oldT.body == null ? endPos(oldT) : oldT.body.pos) - 1;
            // now check, that there is a whitespace. It is not mandatory, we
            // have to ensure. If whitespace is not present, take body beginning
            tokenSequence.move(posHint);
            if (tokenSequence.token().id() != JavaTokenId.WHITESPACE) {
                ++posHint;
            }
        } else {
            posHint = oldT.thrown.iterator().next().getStartPosition();
        }
        //if (!newT.thrown.isEmpty())
            printer.print(origText.substring(localPointer, localPointer = posHint));
        localPointer = diffList2(oldT.thrown, newT.thrown, posHint, EstimatorFactory.throwz());
        posHint = endPos(oldT) - 1;
        localPointer = diffTree(oldT.body, newT.body, localPointer);
        diffTree(oldT.defaultValue, newT.defaultValue);
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected int diffVarDef(JCVariableDecl oldT, JCVariableDecl newT, int[] bounds) {
       int localPointer = bounds[0];
        if (oldT.mods != newT.mods) {
            if (newT.mods.toString().length() > 0) {
                localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
            } else {
                int oldPos = getOldPos(oldT.mods);
                printer.print(origText.substring(localPointer, oldPos));
                localPointer = oldT.vartype.pos;
            }
        }
        localPointer = diffTree(oldT.vartype, newT.vartype, new int[] { localPointer, getOldPos(oldT.vartype) });
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(localPointer, oldT.pos));
            printer.print(newT.name);
            localPointer = oldT.pos + oldT.name.length();
        }
        if (newT.init != null && oldT.init != null) {
            printer.print(origText.substring(localPointer, localPointer = getOldPos(oldT.init)));
            localPointer = diffTree(oldT.init, newT.init, new int[] { localPointer, endPos(oldT.init) });
        } else {
            diffTreeToken("=", endPos(oldT.init), oldT.init, newT.init, "");
        }
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected int diffBlock(JCBlock oldT, JCBlock newT, int lastPrinted) {
        int localPointer = lastPrinted;
        if (oldT.flags != newT.flags)
            append(Diff.flags(oldT.pos, endPos(oldT), oldT.flags, newT.flags));
        VeryPretty bodyPrinter = new VeryPretty(context, JavaFormatOptions.getDefault());
        int oldIndent = printer.indent();
        bodyPrinter.reset(oldIndent);
        bodyPrinter.indent();
        if (oldT.stats.head != null && oldT.stats.head.pos == oldT.pos) {
            // syntetic super() found, skip it
            oldT.stats = oldT.stats.tail;
            newT.stats = newT.stats.tail;
        }
        int[] pos = diffList(oldT.stats, newT.stats, oldT.pos + 1, EstimatorFactory.members(), Measure.DEFAULT, bodyPrinter); // hint after open brace
        if (localPointer < pos[0]) {
            printer.print(origText.substring(localPointer, pos[0]));
        }
        localPointer = pos[1];
        printer.print(bodyPrinter.toString());
        if (localPointer < endPos(oldT)) {
            printer.print(origText.substring(localPointer, localPointer = endPos(oldT)));
        }
        printer.undent(oldIndent);
        return localPointer;
    }

    protected int diffDoLoop(JCDoWhileLoop oldT, JCDoWhileLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0], printer);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffWhileLoop(JCWhileLoop oldT, JCWhileLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        // condition
        int[] condPos = getBounds(oldT.cond);
        copyTo(localPointer, condPos[0], printer);
        localPointer = diffTree(oldT.cond, newT.cond, condPos);
        // body
        int[] bodyPos = getBounds(oldT.body);
        copyTo(localPointer, bodyPos[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyPos);
        
        copyTo(localPointer, bounds[1], printer);
        return bounds[1];
    }

    protected int diffForLoop(JCForLoop oldT, JCForLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        int initListHint = oldT.cond != null ? oldT.cond.pos - 1 : Query.NOPOS;
        int stepListHint = oldT.cond != null ? endPos(oldT.cond) + 1 : Query.NOPOS;
        printer.print(origText.substring(bounds[0], getOldPos(oldT.init.head)));
        localPointer = diffList(oldT.init, newT.init, LineInsertionType.NONE, initListHint);
        printer.print(origText.substring(localPointer, getOldPos(oldT.cond)));
        localPointer = diffTree(oldT.cond, newT.cond, new int[] { getOldPos(oldT.cond), endPos(oldT.cond) });
        printer.print(origText.substring(localPointer, getOldPos(oldT.step.head)));
        localPointer = diffList(oldT.step, newT.step, LineInsertionType.NONE, stepListHint);
        printer.print(origText.substring(localPointer, getOldPos(oldT.body)));
        localPointer = diffTree(oldT.body, newT.body, new int[] { getOldPos(oldT.body), endPos(oldT.body) });
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }
    
    protected int diffForeachLoop(JCEnhancedForLoop oldT, JCEnhancedForLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        // variable
        int[] varBounds = getBounds(oldT.var);
        copyTo(localPointer, varBounds[0], printer);
        localPointer = diffTree(oldT.var, newT.var, varBounds);
        // expression
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0], printer);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        // body
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected void diffLabelled(JCLabeledStatement oldT, JCLabeledStatement newT) {
        if (nameChanged(oldT.label, newT.label))
            append(Diff.name(oldT.pos, oldT.label, newT.label));
        diffTree(oldT.body, newT.body);
    }

    protected void diffSwitch(JCSwitch oldT, JCSwitch newT) {
        diffTree(oldT.selector, newT.selector);
        int castListHint = oldT.cases.size() > 0 ? oldT.cases.head.pos : Query.NOPOS;
        diffList(oldT.cases, newT.cases, LineInsertionType.BEFORE, castListHint);
    }

    protected void diffCase(JCCase oldT, JCCase newT) {
        diffTree(oldT.pat, newT.pat);
        diffList(oldT.stats, newT.stats, LineInsertionType.BEFORE, endPos(oldT) + 1); // after colon
    }

    protected int diffSynchronized(JCSynchronized oldT, JCSynchronized newT, int[] bounds) {
        int localPointer = bounds[0];
        // lock
        int[] lockBounds = getBounds(oldT.lock);
        copyTo(localPointer, lockBounds[0], printer);
        localPointer = diffTree(oldT.lock, newT.lock, lockBounds);
        // body
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffTry(JCTry oldT, JCTry newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] bodyPos = getBounds(oldT.body);
        copyTo(localPointer, bodyPos[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyPos);
        int pos = oldT.catchers.head != null ? getOldPos(oldT.catchers.head) : oldT.body.endpos + 1;
        copyTo(localPointer, pos, printer);
        localPointer = diffList(oldT.catchers, newT.catchers, LineInsertionType.BEFORE, pos);
        if (oldT.finalizer != null) {
            int[] finalBounds = getBounds(oldT.finalizer);
            copyTo(localPointer, finalBounds[0], printer);
            localPointer = diffTree(oldT.finalizer, newT.finalizer, finalBounds);
        }
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffCatch(JCCatch oldT, JCCatch newT, int[] bounds) {
        int localPointer = bounds[0];
        // param
        int[] paramBounds = getBounds(oldT.param);
        copyTo(localPointer, paramBounds[0], printer);
        localPointer = diffTree(oldT.param, newT.param, paramBounds);
        // body
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0], printer);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffConditional(JCConditional oldT, JCConditional newT, int[] bounds) {
        int localPointer = bounds[0];
        // cond
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0], printer);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        // true
        int[] trueBounds = getBounds(oldT.truepart);
        copyTo(localPointer, trueBounds[0], printer);
        localPointer = diffTree(oldT.truepart, newT.truepart, trueBounds);
        // false
        int[] falseBounds = getBounds(oldT.falsepart);
        copyTo(localPointer, falseBounds[0], printer);
        localPointer = diffTree(oldT.falsepart, newT.falsepart, falseBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffIf(JCIf oldT, JCIf newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.elsepart == null && newT.elsepart != null ||
            oldT.elsepart != null && newT.elsepart == null) {
            // mark the whole if statement to be reformatted, which Commit will refine.
            append(Diff.modify(oldT, getOldPos(oldT), newT));
        } else {
            int[] condPos = new int[] { getOldPos(oldT.cond), endPos(oldT.cond) };
            printer.print(origText.substring(localPointer, condPos[0]));
            localPointer = diffTree(oldT.cond, newT.cond, condPos);
            int[] part = new int[] { getOldPos(oldT.thenpart), endPos(oldT.thenpart) };
            printer.print(origText.substring(localPointer, part[0]));
            localPointer = diffTree(oldT.thenpart, newT.thenpart, part);
            if (oldT.elsepart != null) {
                part = new int[] { getOldPos(oldT.elsepart), endPos(oldT.elsepart) };
                printer.print(origText.substring(localPointer, part[0]));
                localPointer = diffTree(oldT.elsepart, newT.elsepart, part);
            }
        }
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected int diffExec(JCExpressionStatement oldT, JCExpressionStatement newT, int[] bounds) {
        int localPointer = getOldPos(oldT);
        printer.print(origText.substring(bounds[0], localPointer));
        localPointer = diffTree(oldT.expr, newT.expr, bounds);
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected void diffBreak(JCBreak oldT, JCBreak newT) {
        if (nameChanged(oldT.label, newT.label))
            append(Diff.name(oldT.pos, oldT.label, newT.label));
        diffTree(oldT.target, newT.target);
    }

    protected void diffContinue(JCContinue oldT, JCContinue newT) {
        if (nameChanged(oldT.label, newT.label))
            append(Diff.name(oldT.pos, oldT.label, newT.label));
        diffTree(oldT.target, newT.target);
    }

    protected int diffReturn(JCReturn oldT, JCReturn newT, int[] bounds) {
        int[] exprBounds = new int[] { getOldPos(oldT), endPos(oldT) };
        copyTo(bounds[0], exprBounds[0], printer);
        int localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffThrow(JCThrow oldT, JCThrow newT, int[] bounds) {
        int localPointer = bounds[0];
        // expr
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0], printer);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffAssert(JCAssert oldT, JCAssert newT, int[] bounds) {
        int localPointer = bounds[0];
        // cond
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0], printer);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        // detail
        int[] detailBounds = getBounds(oldT.detail);
        copyTo(localPointer, detailBounds[0], printer);
        localPointer = diffTree(oldT.detail, newT.detail, detailBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffApply(JCMethodInvocation oldT, JCMethodInvocation newT, int[] bounds) {
        int localPointer = bounds[0];
        diffParameterList(oldT.typeargs, newT.typeargs, localPointer);
        localPointer = diffTree(oldT.meth, newT.meth, new int[] { getOldPos(oldT.meth), endPos(oldT.meth) });
        localPointer = diffParameterList(oldT.args, newT.args, localPointer);
        return localPointer;
    }

    boolean anonClass = false;
    
    protected int diffNewClass(JCNewClass oldT, JCNewClass newT, int[] bounds) {
        int localPointer = bounds[0];
        diffTree(oldT.encl, newT.encl);
        diffParameterList(oldT.typeargs, newT.typeargs, localPointer);
        localPointer = diffTree(oldT.clazz, newT.clazz, localPointer);
        localPointer = diffParameterList(oldT.args, newT.args, localPointer);
        // let diffClassDef() method notified that anonymous class is printed.
        if (oldT.def != null) {
            printer.print(origText.substring(localPointer, getOldPos(oldT.def)));
            anonClass = true; 
            localPointer = diffTree(oldT.def, newT.def, new int[] { getOldPos(oldT.def), endPos(oldT.def)});
            anonClass = false;
        }
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected void diffNewArray(JCNewArray oldT, JCNewArray newT) {
        diffTree(oldT.elemtype, newT.elemtype);
        diffParameterList(oldT.dims, newT.dims, -1);
        diffList(oldT.elems, newT.elems, LineInsertionType.NONE, Query.NOPOS);
    }

    protected int diffParens(JCParens oldT, JCParens newT, int[] bounds) {
        int localPointer = bounds[0];
        printer.print(origText.substring(localPointer, getOldPos(oldT.expr)));
        localPointer = diffTree(oldT.expr, newT.expr, new int[] { getOldPos(oldT.expr), endPos(oldT.expr) });
        return localPointer;
    }

    protected int diffAssign(JCAssign oldT, JCAssign newT, int[] bounds) {
        int localPointer = bounds[0];
        // lhs
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0], printer);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        // rhs
        int[] rhsBounds = getBounds(oldT.rhs);
        copyTo(localPointer, rhsBounds[0], printer);
        localPointer = diffTree(oldT.rhs, newT.rhs, rhsBounds);
        
        copyTo(localPointer, bounds[1], printer);
        return bounds[1];
    }

    protected int diffAssignop(JCAssignOp oldT, JCAssignOp newT, int[] bounds) {
        int localPointer = bounds[0];
        // lhs
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0], printer);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        // rhs
        int[] rhsBounds = getBounds(oldT.rhs);
        copyTo(localPointer, rhsBounds[0], printer);
        localPointer = diffTree(oldT.rhs, newT.rhs, rhsBounds);
        
        // todo (#pf): bugy behaviour, should be rewrriten. Perhaps nobody uses
        // this right now.
        if (oldT.tag != newT.tag)
            append(Diff.name(oldT.pos, operatorName(oldT.tag), operatorName(newT.tag)));
        
        copyTo(localPointer, bounds[1], printer);
        return bounds[1];
    }

    protected int diffUnary(JCUnary oldT, JCUnary newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] argBounds = new int[] { getOldPos(oldT.arg), endPos(oldT.arg) };
        printer.print(origText.substring(localPointer, argBounds[0]));
        localPointer = diffTree(oldT.arg, newT.arg, argBounds);
        if (oldT.tag != newT.tag)
            append(Diff.name(oldT.pos, operatorName(oldT.tag), operatorName(newT.tag)));
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }

    protected int diffBinary(JCBinary oldT, JCBinary newT, int[] bounds) {
        int localPointer = bounds[0];
        localPointer = diffTree(oldT.lhs, newT.lhs, new int[] { localPointer, endPos(oldT.lhs) });
        int rhs = getOldPos(oldT.rhs);
        if (localPointer < rhs) {
            printer.print(origText.substring(localPointer, rhs));
        }
        localPointer = diffTree(oldT.rhs, newT.rhs, new int[] { rhs, endPos(oldT.rhs) });
        if (oldT.tag != newT.tag)
            append(Diff.name(oldT.pos, operatorName(oldT.tag), operatorName(newT.tag)));
        printer.print(origText.substring(localPointer, bounds[1]));
        return bounds[1];
    }
    
    private String operatorName(int tag) {
        // dummy instance, just to access a public method which should be static
        return new Pretty(null, false).operatorName(tag); 
    }

    protected int diffTypeCast(JCTypeCast oldT, JCTypeCast newT, int[] bounds) {
        int localPointer = bounds[0];
        // indexed
        int[] clazzBounds = getBounds(oldT.clazz);
        copyTo(localPointer, clazzBounds[0], printer);
        localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        // expression
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0], printer);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected void diffTypeTest(JCInstanceOf oldT, JCInstanceOf newT) {
        diffTree(oldT.expr, newT.expr);
        diffTree(oldT.clazz, newT.clazz);
    }

    protected int diffIndexed(JCArrayAccess oldT, JCArrayAccess newT, int[] bounds) {
        int localPointer = bounds[0];
        // indexed
        int[] indexedBounds = getBounds(oldT.indexed);
        copyTo(localPointer, indexedBounds[0], printer);
        localPointer = diffTree(oldT.indexed, newT.indexed, indexedBounds);
        // index
        int[] indexBounds = getBounds(oldT.index);
        copyTo(localPointer, indexBounds[0], printer);
        localPointer = diffTree(oldT.index, newT.index, indexBounds);
        copyTo(localPointer, bounds[1], printer);
        
        return bounds[1];
    }

    protected int diffSelect(JCFieldAccess oldT, JCFieldAccess newT, int[] bounds) {
        int localPointer = bounds[0];
        printer.print(origText.substring(localPointer, getOldPos(oldT.selected)));
        localPointer = diffTree(oldT.selected, newT.selected, new int[] { getOldPos(oldT.selected), endPos(oldT.selected) });
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(localPointer, endPos(oldT.selected)));
            printer.print(".");
            printer.print(newT.name);
            localPointer = endPos(oldT.selected) + 1 +oldT.name.length();
        }
        return localPointer;
    }

    protected int diffIdent(JCIdent oldT, JCIdent newT, int lastPrinted) {
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(lastPrinted, getOldPos(oldT)));
            printer.print(newT.name);
            return endPos(oldT);
        }
        return lastPrinted;
    }

    protected void diffLiteral(JCLiteral oldT, JCLiteral newT) {
        if (oldT.typetag != newT.typetag || !oldT.value.equals(newT.value))
            append(Diff.modify(oldT, getOldPos(oldT), newT));
    }

    protected void diffTypeIdent(JCPrimitiveTypeTree oldT, JCPrimitiveTypeTree newT) {
        if (oldT.typetag != newT.typetag)
            append(Diff.modify(oldT, getOldPos(oldT), newT));
    }

    protected void diffTypeArray(JCArrayTypeTree oldT, JCArrayTypeTree newT) {
        diffTree(oldT.elemtype, newT.elemtype);
    }

    protected void diffTypeApply(JCTypeApply oldT, JCTypeApply newT) {
        diffTree(oldT.clazz, newT.clazz);
        diffParameterList(oldT.arguments, newT.arguments, -1);
    }

    protected void diffTypeParameter(JCTypeParameter oldT, JCTypeParameter newT) {
        if (nameChanged(oldT.name, newT.name))
            append(Diff.name(oldT.pos, oldT.name, newT.name));
        diffParameterList(oldT.bounds, newT.bounds, -1);
    }
    
    protected void diffWildcard(JCWildcard oldT, JCWildcard newT) {
        if (oldT.kind != newT.kind)
            append(Diff.name(oldT.pos, oldT.kind.toString(), newT.kind.toString()));
        diffTree(oldT.inner, newT.inner);
    }
    
    protected void diffTypeBoundKind(TypeBoundKind oldT, TypeBoundKind newT) {
        if (oldT.kind != newT.kind)
            append(Diff.name(oldT.pos, oldT.kind.toString(), newT.kind.toString()));
    }
    
    protected void diffAnnotation(JCAnnotation oldT, JCAnnotation newT) {
        diffTree(oldT.annotationType, newT.annotationType);
        diffParameterList(oldT.args, newT.args, -1);
    }
    
    protected int diffModifiers(JCModifiers oldT, JCModifiers newT, JCTree parent, int lastPrinted) {
        if (oldT == newT) {
            // modifiers wasn't changed, return the position lastPrinted.
            return lastPrinted;
        }
        int oldPos = oldT.pos != Position.NOPOS ? getOldPos(oldT) : getOldPos(parent);
        printer.print(origText.substring(lastPrinted, lastPrinted = oldPos));
        int result = endPos(oldT.annotations);
        if (listsMatch(oldT.annotations, newT.annotations)) {
            if (result > 0) {
                printer.print(origText.substring(lastPrinted, lastPrinted = result));
            }
        } else {
            printer.printAnnotations(newT.annotations);
            if (result > 0) {
                printer.print(origText.substring(lastPrinted, lastPrinted = result));
            }
        }
        if (oldT.flags != newT.flags) {
            int endPos = endPos(oldT);
            if (endPos > 0) {
                printer.print(newT.toString().trim());
                lastPrinted = endPos;
            } else {
                printer.print(newT.toString());
            }
        }
        printer.print(origText.substring(lastPrinted, endPos(oldT)));
        return endPos(oldT);
    }
    
    protected void diffLetExpr(LetExpr oldT, LetExpr newT) {
        diffList(oldT.defs, newT.defs, LineInsertionType.NONE, Query.NOPOS);
        diffTree(oldT.expr, newT.expr);
    }
    
    protected void diffErroneous(JCErroneous oldT, JCErroneous newT) {
        diffList(oldT.errs, newT.errs, LineInsertionType.BEFORE, Query.NOPOS);
    }

    protected boolean listContains(List<? extends JCTree> list, JCTree tree) {
        for (JCTree t : list)
            if (treesMatch(t, tree))
                return true;
        return false;
    }

    protected boolean treesMatch(JCTree t1, JCTree t2) {
        return treesMatch(t1, t2, true);
    }
    
    public boolean treesMatch(JCTree t1, JCTree t2, boolean deepMatch) {
        if (t1 == t2)
            return true;
        if (t1 == null || t2 == null)
            return false;
        if (t1.tag != t2.tag)
            return false;
        if (!deepMatch)
            return true;
        
        // don't use visitor, since we want fast-fail behavior
        switch (t1.tag) {
          case JCTree.TOPLEVEL:
              return ((JCCompilationUnit)t1).sourcefile.equals(((JCCompilationUnit)t2).sourcefile);
          case JCTree.IMPORT:
              return matchImport((JCImport)t1, (JCImport)t2);
          case JCTree.CLASSDEF:
              return ((JCClassDecl)t1).sym == ((JCClassDecl)t2).sym;
          case JCTree.METHODDEF:
              return ((JCMethodDecl)t1).sym == ((JCMethodDecl)t2).sym;
          case JCTree.VARDEF:
              return ((JCVariableDecl)t1).sym == ((JCVariableDecl)t2).sym;
          case JCTree.SKIP:
              return true;
          case JCTree.BLOCK:
              return matchBlock((JCBlock)t1, (JCBlock)t2);
          case JCTree.DOLOOP:
              return matchDoLoop((JCDoWhileLoop)t1, (JCDoWhileLoop)t2);
          case JCTree.WHILELOOP:
              return matchWhileLoop((JCWhileLoop)t1, (JCWhileLoop)t2);
          case JCTree.FORLOOP:
              return matchForLoop((JCForLoop)t1, (JCForLoop)t2);
          case JCTree.FOREACHLOOP:
              return matchForeachLoop((JCEnhancedForLoop)t1, (JCEnhancedForLoop)t2);
          case JCTree.LABELLED:
              return matchLabelled((JCLabeledStatement)t1, (JCLabeledStatement)t2);
          case JCTree.SWITCH:
              return matchSwitch((JCSwitch)t1, (JCSwitch)t2);
          case JCTree.CASE:
              return matchCase((JCCase)t1, (JCCase)t2);
          case JCTree.SYNCHRONIZED:
              return matchSynchronized((JCSynchronized)t1, (JCSynchronized)t2);
          case JCTree.TRY:
              return matchTry((JCTry)t1, (JCTry)t2);
          case JCTree.CATCH:
              return matchCatch((JCCatch)t1, (JCCatch)t2);
          case JCTree.CONDEXPR:
              return matchConditional((JCConditional)t1, (JCConditional)t2);
          case JCTree.IF:
              return matchIf((JCIf)t1, (JCIf)t2);
          case JCTree.EXEC:
              return treesMatch(((JCExpressionStatement)t1).expr, ((JCExpressionStatement)t2).expr);
          case JCTree.BREAK:
              return matchBreak((JCBreak)t1, (JCBreak)t2);
          case JCTree.CONTINUE:
              return matchContinue((JCContinue)t1, (JCContinue)t2);
          case JCTree.RETURN:
              return treesMatch(((JCReturn)t1).expr, ((JCReturn)t2).expr);
          case JCTree.THROW:
              return treesMatch(((JCThrow)t1).expr, ((JCThrow)t2).expr);
          case JCTree.ASSERT:
              return matchAssert((JCAssert)t1, (JCAssert)t2);
          case JCTree.APPLY:
              return matchApply((JCMethodInvocation)t1, (JCMethodInvocation)t2);
          case JCTree.NEWCLASS:
              return matchNewClass((JCNewClass)t1, (JCNewClass)t2);
          case JCTree.NEWARRAY:
              return matchNewArray((JCNewArray)t1, (JCNewArray)t2);
          case JCTree.PARENS:
              return treesMatch(((JCParens)t1).expr, ((JCParens)t2).expr);
          case JCTree.ASSIGN:
              return matchAssign((JCAssign)t1, (JCAssign)t2);
          case JCTree.TYPECAST:
              return matchTypeCast((JCTypeCast)t1, (JCTypeCast)t2);
          case JCTree.TYPETEST:
              return matchTypeTest((JCInstanceOf)t1, (JCInstanceOf)t2);
          case JCTree.INDEXED:
              return matchIndexed((JCArrayAccess)t1, (JCArrayAccess)t2);
          case JCTree.SELECT:
              return ((JCFieldAccess)t1).sym == ((JCFieldAccess)t2).sym;
          case JCTree.IDENT:
              return ((JCIdent)t1).sym == ((JCIdent)t2).sym;
          case JCTree.LITERAL:
              return matchLiteral((JCLiteral)t1, (JCLiteral)t2);
          case JCTree.TYPEIDENT:
              return ((JCPrimitiveTypeTree)t1).typetag == ((JCPrimitiveTypeTree)t2).typetag;
          case JCTree.TYPEARRAY:
              return treesMatch(((JCArrayTypeTree)t1).elemtype, ((JCArrayTypeTree)t2).elemtype);
          case JCTree.TYPEAPPLY:
              return matchTypeApply((JCTypeApply)t1, (JCTypeApply)t2);
          case JCTree.TYPEPARAMETER:
              return matchTypeParameter((JCTypeParameter)t1, (JCTypeParameter)t2);
          case JCTree.WILDCARD:
              return matchWildcard((JCWildcard)t1, (JCWildcard)t2);
          case JCTree.TYPEBOUNDKIND:
              return ((TypeBoundKind)t1).kind == ((TypeBoundKind)t2).kind;
          case JCTree.ANNOTATION:
              return matchAnnotation((JCAnnotation)t1, (JCAnnotation)t2);
          case JCTree.LETEXPR:
              return matchLetExpr((LetExpr)t1, (LetExpr)t2);
          case JCTree.POS:
          case JCTree.NEG:
          case JCTree.NOT:
          case JCTree.COMPL:
          case JCTree.PREINC:
          case JCTree.PREDEC:
          case JCTree.POSTINC:
          case JCTree.POSTDEC:
          case JCTree.NULLCHK:
              return matchUnary((JCUnary)t1, (JCUnary)t2);
          case JCTree.OR:
          case JCTree.AND:
          case JCTree.BITOR:
          case JCTree.BITXOR:
          case JCTree.BITAND:
          case JCTree.EQ:
          case JCTree.NE:
          case JCTree.LT:
          case JCTree.GT:
          case JCTree.LE:
          case JCTree.GE:
          case JCTree.SL:
          case JCTree.SR:
          case JCTree.USR:
          case JCTree.PLUS:
          case JCTree.MINUS:
          case JCTree.MUL:
          case JCTree.DIV:
          case JCTree.MOD:
              return matchBinary((JCBinary)t1, (JCBinary)t2);
          case JCTree.BITOR_ASG:
          case JCTree.BITXOR_ASG:
          case JCTree.BITAND_ASG:
          case JCTree.SL_ASG:
          case JCTree.SR_ASG:
          case JCTree.USR_ASG:
          case JCTree.PLUS_ASG:
          case JCTree.MINUS_ASG:
          case JCTree.MUL_ASG:
          case JCTree.DIV_ASG:
          case JCTree.MOD_ASG:
              return matchAssignop((JCAssignOp)t1, (JCAssignOp)t2);
          default:
              String msg = ((com.sun.source.tree.Tree)t1).getKind().toString() +
                      " " + t1.getClass().getName();
              throw new AssertionError(msg);
        }
    }

    protected boolean nameChanged(Name oldName, Name newName) {
        if (oldName == newName)
            return false;
        byte[] arr1 = oldName.toUtf();
        byte[] arr2 = newName.toUtf();
        int len = arr1.length;
        if (len != arr2.length)
            return true;
        for (int i = 0; i < len; i++)
            if (arr1[i] != arr2[i])
                return true;
        return false;
    }

    /**
     * Diff an unordered list, which may contain insertions and deletions.
     * REMOVE IT WHEN CORRECT LIST MATCHING WILL BE IMPLEMENTED
     */
    protected int diffList(List<? extends JCTree> oldList, 
                            List<? extends JCTree> newList, 
                            LineInsertionType newLine, int insertHint) {
        int lastPrinted = insertHint;
        if (oldList == newList)
            return insertHint;
        assert oldList != null && newList != null;
        int lastOldPos = insertHint;
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        Iterator<? extends JCTree> newIter = newList.iterator();
        JCTree oldT = safeNext(oldIter);
        JCTree newT = safeNext(newIter);
        while (oldT != null && newT != null) {
            if (oldTopLevel != null) {
                int endPos = model.getEndPos(oldT, oldTopLevel);

                if (endPos != Position.NOPOS)
                    lastOldPos = endPos;
            }
            if (treesMatch(oldT, newT, false)) {
                lastPrinted  = diffTree(oldT, newT, new int[] { getOldPos(oldT), endPos(oldT) });
                oldT = safeNext(oldIter);
                newT = safeNext(newIter);
            }
            else if (!listContains(newList, oldT) && !listContains(oldList, newT)) {
                append(Diff.modify(oldT, getOldPos(oldT), newT));
                oldT = safeNext(oldIter);
                newT = safeNext(newIter);
            }
            else if (!listContains(newList, oldT)) {
                if (!isHidden(oldT, oldParent))
                    append(Diff.delete(oldT, getOldPos(oldT)));
                oldT = safeNext(oldIter);
            }
            else {
                if (!isHidden(newT, newParent))
                    append(Diff.insert(newT, getOldPos(oldT), newLine, null));
                newT = safeNext(newIter);
            }
        }
        while (oldT != null) {
            if (!isHidden(oldT, oldParent))
                append(Diff.delete(oldT, getOldPos(oldT)));
            if (oldTopLevel != null)
                lastOldPos = model.getEndPos(oldT, oldTopLevel);
            oldT = safeNext(oldIter);
        }
        while (newT != null) {
            if (!isHidden(newT, newParent))
                append(Diff.insert(newT, lastOldPos, newLine, null));
            newT = safeNext(newIter);
        }
        return lastPrinted;
    }
    
    private JCTree safeNext(Iterator<? extends JCTree> iter) {
        return iter.hasNext() ? iter.next() : null;
    }

    // XXX: this method should be removed later when all call will be
    // refactored and will use new list matching
    protected int diffParameterList(List<? extends JCTree> oldList, List<? extends JCTree> newList, int localPointer) {
        if (oldList == newList)
            return localPointer;
        assert oldList != null && newList != null;
        int lastOldPos = Query.NOPOS;
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        Iterator<? extends JCTree> newIter = newList.iterator();
        while (oldIter.hasNext() && newIter.hasNext()) {
            JCTree oldT = oldIter.next();
            printer.print(origText.substring(localPointer, localPointer = getOldPos(oldT)));
            localPointer = diffTree(oldT, newIter.next(), new int[] { localPointer, endPos(oldT) });
            if (oldTopLevel != null)
                lastOldPos = model.getEndPos(oldT, oldTopLevel);
        }
        while (oldIter.hasNext()) {
            JCTree oldT = oldIter.next();
            append(Diff.delete(oldT, getOldPos(oldT)));
        }
        while (newIter.hasNext()) {
            append(Diff.insert(newIter.next(), lastOldPos, LineInsertionType.BEFORE));
        }
        return localPointer;
    }
    
    /**
     * Diff a ordered list for differences.
     */
    protected int diffList2(
        List<? extends JCTree> oldList, List<? extends JCTree> newList,
        int initialPos, PositionEstimator estimator) 
    {
        if (oldList == newList)
            return initialPos;
        assert oldList != null && newList != null;
        int lastOldPos = initialPos;
        
        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(
                (List<JCTree>) oldList, 
                (List<JCTree>) newList
        );
        if (!matcher.match()) {
            return initialPos;
        }
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        ResultItem<JCTree>[] result = matcher.getTransformedResult();
        Separator s = matcher.separatorInstance();
        s.compute();
        estimator.initialize(oldList, workingCopy);
        int[][] matrix = estimator.getMatrix();
        int testPos = initialPos;
        int i = 0;
        for (int j = 0; j < result.length; j++) {
            JCTree oldT;
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    if (tokenSequence.moveIndex(matrix[i][4])) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    oldT = oldIter.next(); ++i;
                    //append(Diff.modify("", lastOldPos, oldT, item.element, "", ListType.PARAMETER));
                    printer.print(origText.substring(lastOldPos, lastOldPos = endPos(oldT)));
                    printer.print(item.element);
                    //lastOldPos = endPos(oldT);
                    break;
                }
                case INSERT: {
                    String prec = s.head(j) ? estimator.head() : s.prev(j) ? estimator.sep() : null;
                    String tail = s.next(j) ? estimator.sep() : null;
                    if (estimator.getIndentString() != null && !estimator.getIndentString().equals(" ")) {
                        prec += estimator.getIndentString();
                    }
                    printer.print(origText.substring(lastOldPos, testPos));
                    printer.print(prec);
                    printer.print(item.element);
                    printer.print(tail);
                    //append(Diff.insert(testPos, prec, item.element, tail, LineInsertionType.NONE));
                    break;
                }
                case DELETE: {
                    // compute offsets for removal (tree bounds are not enough
                    // in this case, we have to remove also tokens around like
                    // preceding keyword, i.e. throws, implements etc. and also
                    // separators like commas.
                    
                    // this is a hack: be careful when removing the first:
                    // throws Exception,IOException... do not remove the space
                    // after throws keyword, there is not space after comma!
                    int delta = 0;
                    if (i == 0 && matrix[i+1][2] != -1 && matrix[i+1][2] == matrix[i+1][3]) {
                        ++delta;
                    }
                    int startOffset = toOff(s.head(j) || s.prev(j) ? matrix[i][1] : matrix[i][2+delta]);
                    int endOffset = toOff(s.tail(j) || s.next(j) ? matrix[i+1][2] : matrix[i][4]);
                    assert startOffset != -1 && endOffset != -1 : "Invalid offset!";
                    //printer.print(origText.substring(lastOldPos, startOffset));
                    //append(Diff.delete(startOffset, endOffset));
                    if (tokenSequence.moveIndex(matrix[i][4])) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    if (i == 0 && !newList.isEmpty()) {
                        lastOldPos = endOffset;
                    } else {
                        lastOldPos = endPos(item.element);
                    }
                    oldT = oldIter.next(); ++i;
                    break;
                }
                case NOCHANGE: {
                    if (tokenSequence.moveIndex(matrix[i][4])) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    oldT = oldIter.next(); ++i;
                    printer.print(origText.substring(lastOldPos, lastOldPos = endPos(oldT)));
                    break;
                }
            }
        }
        return lastOldPos;
    }

    private int toOff(int tokenIndex) {
        if (tokenIndex == -1) {
            return -1;
        }
        tokenSequence.moveIndex(tokenIndex);
        return tokenSequence.offset();
    }
    
    /**
     * Diff a ordered list for differences.
     */
    protected void diffParameterList(List<? extends JCTree> oldList, 
                                     List<? extends JCTree> newList,
                                     int where, ListType type) 
    {
        if (oldList == newList)
            return;
        assert oldList != null && newList != null;
        int lastOldPos = where;
        
        ListMatcher<JCExpression> matcher = ListMatcher.<JCExpression>instance(
                (List<JCExpression>) oldList, 
                (List<JCExpression>) newList
        );
        if (!matcher.match()) {
            return;
        }
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        ResultItem<JCExpression>[] result = matcher.getTransformedResult();
        Separator s = matcher.separatorInstance();
        s.compute();
        for (int j = 0; j < result.length; j++) {
            JCTree oldT;
            ResultItem<JCExpression> item = result[j];
            switch (item.operation) {
                case MODIFY:
                    oldT = oldIter.next();
                    append(Diff.modify("", lastOldPos, oldT, item.element, "", ListType.PARAMETER));
                    lastOldPos = endPos(oldT);
                    break;
                case INSERT: {
                    String prec = s.head(j) ? type.head() : s.prev(j) ? type.sep() : null;
                    String tail = s.tail(j) ? type.tail() : s.next(j) ? type.sep() : null;
                    // XXX: todo (#pf): strange, should be solved better. Used for
                    // move position after last available separator. In case there
                    // is no available separator, use lastOldPos immediately for
                    // diff.
                    //if (type.sep().equals(tail)) {
                    // is there any next element in old list and we do not
                    // insert the first element.
                    int pos = lastOldPos;
                    if (oldIter.hasNext() && lastOldPos != where) {
                        pos = TokenUtilities.moveFwdToToken(tokenSequence, lastOldPos, type.sep);
                        if (pos > 0) {
                            pos += type.sep.fixedText().length();
                        }
                    }
                    append(Diff.insert(prec, item.element, pos, tail, ListType.PARAMETER));
                    break;
                }
                case DELETE: {
                    // XXX: todo (#pf): use tokens around. Should be refactored to JavaTokenId and
                    // some formatting policy
                    String prec = s.head(j) ? type.headToken() : s.prev(j) ? type.sepToken() : null;
                    String tail = s.tail(j) ? type.tailToken() : s.next(j) ? type.sepToken() : null;
                    append(Diff.delete(prec, item.element, lastOldPos, tail));
                    oldT = oldIter.next();
                    lastOldPos = endPos(item.element);
                    break;
                }
                case NOCHANGE:
                    oldT = oldIter.next();
                    lastOldPos = endPos(oldT);
                    break;
            }
        }
    }
    
    /**
     * Used for diffing lists which does not contain any separator.
     * (Currently for imports and members diffing.)
     */
    protected int[] diffList(
            List<? extends JCTree> oldList, 
            List<? extends JCTree> newList,
            int initialPos, 
            PositionEstimator estimator,
            Measure measure, 
            VeryPretty printer)
    {
        int[] ret = new int[] { -1, -1 };
        if (oldList == newList) {
            return ret;
        }
        assert oldList != null && newList != null;
        
        ListMatcher<JCTree> matcher = ListMatcher.instance(
                oldList, 
                newList,
                measure
        );
        if (!matcher.match()) {
            return ret;
        }
        JCTree lastdel = null; // last deleted element
        ResultItem<JCTree>[] result = matcher.getResult();
        int posHint = initialPos;
        estimator.initialize(oldList, workingCopy);
        int i = 0;
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    assert true : "Modify is no longer operated!";
                    break;
                }
                case INSERT: {
                    int pos = estimator.getInsertPos(i);
                    // estimator couldn't compute the position - probably
                    // first element is inserted to the collection
                    String head = "", tail = "";
                    if (pos < 0 && oldList.isEmpty() && i == 0) {
                        pos = initialPos;
                        StringBuilder aHead = new StringBuilder(), aTail = new StringBuilder();
                        pos = estimator.prepare(initialPos, aHead, aTail);
                        if (j+1 == result.length) {
                            tail = aTail.toString();
                        }
                        head = aHead.toString();
                        posHint = pos;
                        if (ret[0] < 0) ret[0] = posHint;
                        if (ret[1] < 0) ret[1] = posHint;
                    } else {
                        if (ret[0] < 0) ret[0] = posHint;
                        if (ret[1] < 0) ret[1] = posHint;
                    }
                    int oldPos = item.element.getKind() != Kind.VARIABLE ? getOldPos(item.element) : item.element.pos;
                    boolean found = false;
                    if (oldPos > 0) {
                        for (JCTree oldT : oldList) {
                            int oldNodePos = oldT.getKind() != Kind.VARIABLE ? getOldPos(oldT) : oldT.pos;
                            if (oldPos == oldNodePos) {
                                found = true;
                                VeryPretty oldPrinter = this.printer;
                                int old = oldPrinter.indent();
                                this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                                this.printer.reset(old);
                                int index = oldList.indexOf(oldT);
                                int[] poss = estimator.getPositions(index);
                                diffTree(oldT, item.element, poss);
                                printer.print(this.printer.toString());
//                                if (pointer < poss[1])
//                                    printer.print(origText.substring(pointer, pointer = poss[1]));
                                this.printer = oldPrinter;
                                this.printer.undent(old);
                                break;
                            }
                        }
                    }
                    if (!found) {
                        if (lastdel != null && treesMatch(item.element, lastdel, false)) {
                            VeryPretty oldPrinter = this.printer;
                            int old = oldPrinter.indent();
                            this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                            this.printer.reset(old);
                            int index = oldList.indexOf(lastdel);
                            int[] poss = estimator.getPositions(index);
                            diffTree(lastdel, item.element, poss);
                            printer.print(this.printer.toString());
    //                                if (pointer < poss[1])
    //                                    printer.print(origText.substring(pointer, pointer = poss[1]));
                            this.printer = oldPrinter;
                            this.printer.undent(old);
                            break;
                        }
                        if (j == 0 && !oldList.isEmpty()) {
                            posHint = estimator.getPositions(0)[0];
                        }
                        printer.print(head);
                        int old = printer.indent();
                        VeryPretty inPrint = new VeryPretty(context, JavaFormatOptions.getDefault());
                        inPrint.reset(old);
                        inPrint.enclClassName = printer.enclClassName;
//                        inPrint.indent();
                        inPrint.print(item.element);
                        inPrint.newline();
                        printer.print(inPrint.toString());
                        printer.undent(old);
                        //printer.print(tail);
//                        if (j+1 != result.length) {
//                            printer.toColExactly(currentIndentLevel);
//                        }
                    }
                    break;
                }
                case DELETE: {
                    int[] pos = estimator.getPositions(i);
                    lastdel = oldList.get(i);
                    if (ret[0] < 0) {
                        ret[0] = pos[0];
                    }
                     ++i;
                    ret[1] = pos[1];
                    posHint = pos[1];
                    break;
                }
                case NOCHANGE: {
                    int[] pos = estimator.getPositions(i);
                    if (ret[0] < 0) {
                        ret[0] = pos[0];
                    }
                    printer.print(origText.substring(pos[0], pos[1]));
                    ret[1] = pos[1];
                    ++i;
                    break;
                }
            }
        }
        if (!oldList.isEmpty()) {
            Iterator<? extends JCTree> it = oldList.iterator();
            for (i = 0; it.hasNext(); i++, it.next()) ;
            int[] pos = estimator.getPositions(i);
            ret[1] = pos[1];
        }
        return ret;
    }
    
    private List filterHidden(List<JCTree> list) {
        List<JCTree> result = new ArrayList<JCTree>(); // todo (#pf): capacity?
        for (JCTree tree : list) {
            if (Kind.METHOD == tree.getKind()) {
                CharSequence name = ((MethodTree) tree).getName();
                if ("<init>".contentEquals(name) && tree.pos == oldParent.pos)
                    continue;
            }
            result.add(tree);
        }
        return result;
    }
    
    private boolean isHidden(JCTree t, JCTree parent) {
        if (parent == null)
            return false;
        //TODO: the test was originaly: t.pos == parent.pos, which caused problems when adding
        //member into class without non-syntetic constructors. See ConstructorTest.
        if (t.pos == Query.NOPOS)
            return true;
        return model.isSynthetic(t);
    }
    
    protected void diffPrecedingComments(JCTree oldT, JCTree newT) {
        CommentSet cs = comments.getComments(newT);
        if (!cs.hasChanges())
            return;
        List<Comment> oldComments = comments.getComments(oldT).getPrecedingComments();
        List<Comment> newComments = cs.getPrecedingComments();
        diffCommentLists(oldT, newT, oldComments, newComments, false);
    }

    protected void diffTrailingComments(JCTree oldT, JCTree newT) {
        CommentSet cs = comments.getComments(newT);
        if (!cs.hasChanges())
            return;
        List<Comment> oldComments = comments.getComments(oldT).getTrailingComments();
        List<Comment> newComments = cs.getTrailingComments();
        diffCommentLists(oldT, newT, oldComments, newComments, true);
    }
    
    private void diffCommentLists(JCTree oldT, JCTree newT, List<Comment>oldList, 
                                  List<Comment>newList, boolean trailing) {
        int lastPos = getOldPos(oldT);
        Iterator<Comment> oldIter = oldList.iterator();
        Iterator<Comment> newIter = newList.iterator();
        Comment oldC = safeNext(oldIter);
        Comment newC = safeNext(newIter);
        while (oldC != null && newC != null) {
            lastPos = oldC.pos();
            if (commentsMatch(oldC, newC)) {
                oldC = safeNext(oldIter);
                newC = safeNext(newIter);
            }
            else if (!listContains(newList, oldC)) {
                if  (!listContains(oldList, newC)) {
                    append(Diff.modify(oldT, newT, oldC, newC));
                    oldC = safeNext(oldIter);
                    newC = safeNext(newIter);
                } else {
                    append(Diff.delete(oldT, newT, oldC));
                    oldC = safeNext(oldIter);
                }
            }
            else {
                append(Diff.insert(lastPos, LineInsertionType.BEFORE, oldT, newT, newC, trailing));
                newC = safeNext(newIter);
            }
        }
        while (oldC != null) {
            append(Diff.delete(oldT, newT, oldC));
            oldC = safeNext(oldIter);
        }
        while (newC != null) {
            append(Diff.insert(lastPos, LineInsertionType.BEFORE, oldT, newT, newC, trailing));
            lastPos += newC.endPos() - newC.pos();
            newC = safeNext(oldIter);
        }
    }
    
    private Comment safeNext(Iterator<Comment> iter) {
        return iter.hasNext() ? iter.next() : null;
    }
    
    private boolean commentsMatch(Comment oldC, Comment newC) {
        if (oldC == null && newC == null)
            return true;
        if (oldC == null || newC == null)
            return false;
        return oldC.equals(newC);
    }
    
    private boolean listContains(List<Comment>list, Comment comment) {
        for (Comment c : list)
            if (c.equals(comment))
                return true;
        return false;
    }
    
    // from TreesService
    private static JCTree leftMostTree(JCTree tree) {
        switch (tree.tag) {
            case(JCTree.APPLY):
                return leftMostTree(((JCMethodInvocation)tree).meth);
            case(JCTree.ASSIGN):
                return leftMostTree(((JCAssign)tree).lhs);
            case(JCTree.BITOR_ASG): case(JCTree.BITXOR_ASG): case(JCTree.BITAND_ASG):
            case(JCTree.SL_ASG): case(JCTree.SR_ASG): case(JCTree.USR_ASG):
            case(JCTree.PLUS_ASG): case(JCTree.MINUS_ASG): case(JCTree.MUL_ASG):
            case(JCTree.DIV_ASG): case(JCTree.MOD_ASG):
                return leftMostTree(((JCAssignOp)tree).lhs);
            case(JCTree.OR): case(JCTree.AND): case(JCTree.BITOR):
            case(JCTree.BITXOR): case(JCTree.BITAND): case(JCTree.EQ):
            case(JCTree.NE): case(JCTree.LT): case(JCTree.GT):
            case(JCTree.LE): case(JCTree.GE): case(JCTree.SL):
            case(JCTree.SR): case(JCTree.USR): case(JCTree.PLUS):
            case(JCTree.MINUS): case(JCTree.MUL): case(JCTree.DIV):
            case(JCTree.MOD):
                return leftMostTree(((JCBinary)tree).lhs);
            case(JCTree.CLASSDEF): {
                JCClassDecl node = (JCClassDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                break;
            }
            case(JCTree.CONDEXPR):
                return leftMostTree(((JCConditional)tree).cond);
            case(JCTree.EXEC):
                return leftMostTree(((JCExpressionStatement)tree).expr);
            case(JCTree.INDEXED):
                return leftMostTree(((JCArrayAccess)tree).indexed);
            case(JCTree.METHODDEF): {
                JCMethodDecl node = (JCMethodDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                if (node.restype != null) // true for constructors
                    return leftMostTree(node.restype);
                return node;
            }
            case(JCTree.SELECT):
                return leftMostTree(((JCFieldAccess)tree).selected);
            case(JCTree.TYPEAPPLY):
                return leftMostTree(((JCTypeApply)tree).clazz);
            case(JCTree.TYPEARRAY):
                return leftMostTree(((JCArrayTypeTree)tree).elemtype);
            case(JCTree.TYPETEST):
                return leftMostTree(((JCInstanceOf)tree).expr);
            case(JCTree.POSTINC):
            case(JCTree.POSTDEC):
                return leftMostTree(((JCUnary)tree).arg);
            case(JCTree.VARDEF): {
                JCVariableDecl node = (JCVariableDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                return leftMostTree(node.vartype);
            }
            case(JCTree.TOPLEVEL): {
                JCCompilationUnit node = (JCCompilationUnit)tree;
                assert node.defs.size() > 0;
                return node.pid != null ? node.pid : node.defs.head;
            }
        }
        return tree;
    }
    
    private int getOldPos(JCTree oldT) {
        return getOldPos(oldT, model, undo);
    }
    
    static int getOldPos(JCTree oldT, ASTModel model, UndoList undo) {
        int oldPos = model.getStartPos(oldT);
        if (oldPos == Query.NOPOS) {
            // see if original tree is available for position
            JCTree t = (JCTree)undo.getOld(leftMostTree(oldT));
            if (t != null && t != oldT)
                // recurse in case there are multiple changes to this tree
                oldPos = getOldPos(t, model, undo);
        }
        if (oldPos == Query.NOPOS)
            oldPos = oldT.pos == Query.NOPOS ? leftMostTree(oldT).pos : oldT.pos;
        return oldPos;
    }
    
    /**
     * Create differences between trees. Old tree has to exist, i.e.
     * <code>oldT != null</code>. There is a one exception - when both
     * <code>oldT</code> and <code>newT</code> are null, then method
     * just returns.
     * 
     * @param  oldT  original tree in source code
     * @param  newT  tree to repace the original tree
     */
    protected int diffTree(JCTree oldT, JCTree newT, int[] elementBounds) {
        if (oldT == null && newT != null)
            throw new IllegalArgumentException("Null is not allowed in parameters.");
 
        if (oldT == newT)
            return elementBounds[0];
        diffPrecedingComments(oldT, newT);
        int retVal = -1;
        int oldPos = getOldPos(oldT);

        // todo (#pf): use this just for the non-rewritten places.
        if ((oldT.tag != JCTree.TOPLEVEL && 
             oldT.tag != JCTree.METHODDEF && 
             oldT.tag != JCTree.CLASSDEF && 
             oldT.tag != JCTree.VARDEF &&
             oldT.tag != JCTree.IDENT &&
             oldT.tag != JCTree.EXEC &&
             oldT.tag != JCTree.NEWCLASS &&
             oldT.getKind() != Kind.MEMBER_SELECT) &&
            (oldT.tag != newT.tag || newT.pos == Query.NOPOS || oldT.type != newT.type)) {
            append(Diff.modify(oldT, oldPos, newT));
            return oldPos;
        }

        switch (oldT.tag) {
          case JCTree.TOPLEVEL:
              diffTopLevel((JCCompilationUnit)oldT, (JCCompilationUnit)newT);
              break;
          case JCTree.IMPORT:
              diffImport((JCImport)oldT, (JCImport)newT);
              break;
          case JCTree.CLASSDEF:
              retVal = diffClassDef((JCClassDecl)oldT, (JCClassDecl)newT, elementBounds);
              break;
          case JCTree.METHODDEF:
              retVal = diffMethodDef((JCMethodDecl)oldT, (JCMethodDecl)newT, elementBounds);
              break;
          case JCTree.VARDEF:
              return diffVarDef((JCVariableDecl)oldT, (JCVariableDecl)newT, elementBounds);
          case JCTree.SKIP:
              break;
          case JCTree.BLOCK:
              retVal = diffBlock((JCBlock)oldT, (JCBlock)newT, elementBounds[0]);
              break;
          case JCTree.DOLOOP:
              retVal = diffDoLoop((JCDoWhileLoop)oldT, (JCDoWhileLoop)newT, elementBounds);
              break;
          case JCTree.WHILELOOP:
              retVal = diffWhileLoop((JCWhileLoop)oldT, (JCWhileLoop)newT, elementBounds);
              break;
          case JCTree.FORLOOP:
              retVal = diffForLoop((JCForLoop)oldT, (JCForLoop)newT, elementBounds);
              break;
          case JCTree.FOREACHLOOP:
              retVal = diffForeachLoop((JCEnhancedForLoop)oldT, (JCEnhancedForLoop)newT, elementBounds);
              break;
          case JCTree.LABELLED:
              diffLabelled((JCLabeledStatement)oldT, (JCLabeledStatement)newT);
              break;
          case JCTree.SWITCH:
              diffSwitch((JCSwitch)oldT, (JCSwitch)newT);
              break;
          case JCTree.CASE:
              diffCase((JCCase)oldT, (JCCase)newT);
              break;
          case JCTree.SYNCHRONIZED:
              retVal = diffSynchronized((JCSynchronized)oldT, (JCSynchronized)newT, elementBounds);
              break;
          case JCTree.TRY:
              retVal = diffTry((JCTry)oldT, (JCTry)newT, elementBounds);
              break;
          case JCTree.CATCH:
              retVal = diffCatch((JCCatch)oldT, (JCCatch)newT, elementBounds);
              break;
          case JCTree.CONDEXPR:
              retVal = diffConditional((JCConditional)oldT, (JCConditional)newT, elementBounds);
              break;
          case JCTree.IF:
              retVal = diffIf((JCIf)oldT, (JCIf)newT, elementBounds);
              break;
          case JCTree.EXEC:
              retVal = diffExec((JCExpressionStatement)oldT, (JCExpressionStatement)newT, elementBounds);
              break;
          case JCTree.BREAK:
              diffBreak((JCBreak)oldT, (JCBreak)newT);
              break;
          case JCTree.CONTINUE:
              diffContinue((JCContinue)oldT, (JCContinue)newT);
              break;
          case JCTree.RETURN:
              retVal = diffReturn((JCReturn)oldT, (JCReturn)newT, elementBounds);
              break;
          case JCTree.THROW:
              retVal = diffThrow((JCThrow)oldT, (JCThrow)newT,elementBounds);
              break;
          case JCTree.ASSERT:
              retVal = diffAssert((JCAssert)oldT, (JCAssert)newT, elementBounds);
              break;
          case JCTree.APPLY:
              retVal = diffApply((JCMethodInvocation)oldT, (JCMethodInvocation)newT, elementBounds);
              break;
          case JCTree.NEWCLASS:
              retVal = diffNewClass((JCNewClass)oldT, (JCNewClass)newT, elementBounds);
              break;
          case JCTree.NEWARRAY:
              diffNewArray((JCNewArray)oldT, (JCNewArray)newT);
              break;
          case JCTree.PARENS:
              retVal = diffParens((JCParens)oldT, (JCParens)newT, elementBounds);
              break;
          case JCTree.ASSIGN:
              retVal = diffAssign((JCAssign)oldT, (JCAssign)newT, elementBounds);
              break;
          case JCTree.TYPECAST:
              retVal = diffTypeCast((JCTypeCast)oldT, (JCTypeCast)newT, elementBounds);
              break;
          case JCTree.TYPETEST:
              diffTypeTest((JCInstanceOf)oldT, (JCInstanceOf)newT);
              break;
          case JCTree.INDEXED:
              retVal = diffIndexed((JCArrayAccess)oldT, (JCArrayAccess)newT, elementBounds);
              break;
          case JCTree.SELECT:
              retVal = diffSelect((JCFieldAccess)oldT, (JCFieldAccess)newT, elementBounds);
              break;
          case JCTree.IDENT:
              retVal = diffIdent((JCIdent)oldT, (JCIdent)newT, elementBounds[0]);
              break;
          case JCTree.LITERAL:
              diffLiteral((JCLiteral)oldT, (JCLiteral)newT);
              break;
          case JCTree.TYPEIDENT:
              diffTypeIdent((JCPrimitiveTypeTree)oldT, (JCPrimitiveTypeTree)newT);
              break;
          case JCTree.TYPEARRAY:
              diffTypeArray((JCArrayTypeTree)oldT, (JCArrayTypeTree)newT);
              break;
          case JCTree.TYPEAPPLY:
              diffTypeApply((JCTypeApply)oldT, (JCTypeApply)newT);
              break;
          case JCTree.TYPEPARAMETER:
              diffTypeParameter((JCTypeParameter)oldT, (JCTypeParameter)newT);
              break;
          case JCTree.WILDCARD:
              diffWildcard((JCWildcard)oldT, (JCWildcard)newT);
              break;
          case JCTree.TYPEBOUNDKIND:
              diffTypeBoundKind((TypeBoundKind)oldT, (TypeBoundKind)newT);
              break;
          case JCTree.ANNOTATION:
              diffAnnotation((JCAnnotation)oldT, (JCAnnotation)newT);
              break;
          case JCTree.LETEXPR:
              diffLetExpr((LetExpr)oldT, (LetExpr)newT);
              break;
          case JCTree.POS:
          case JCTree.NEG:
          case JCTree.NOT:
          case JCTree.COMPL:
          case JCTree.PREINC:
          case JCTree.PREDEC:
          case JCTree.POSTINC:
          case JCTree.POSTDEC:
          case JCTree.NULLCHK:
              retVal = diffUnary((JCUnary)oldT, (JCUnary)newT, elementBounds);
              break;
          case JCTree.OR:
          case JCTree.AND:
          case JCTree.BITOR:
          case JCTree.BITXOR:
          case JCTree.BITAND:
          case JCTree.EQ:
          case JCTree.NE:
          case JCTree.LT:
          case JCTree.GT:
          case JCTree.LE:
          case JCTree.GE:
          case JCTree.SL:
          case JCTree.SR:
          case JCTree.USR:
          case JCTree.PLUS:
          case JCTree.MINUS:
          case JCTree.MUL:
          case JCTree.DIV:
          case JCTree.MOD:
              retVal = diffBinary((JCBinary)oldT, (JCBinary)newT, elementBounds);
              break;
          case JCTree.BITOR_ASG:
          case JCTree.BITXOR_ASG:
          case JCTree.BITAND_ASG:
          case JCTree.SL_ASG:
          case JCTree.SR_ASG:
          case JCTree.USR_ASG:
          case JCTree.PLUS_ASG:
          case JCTree.MINUS_ASG:
          case JCTree.MUL_ASG:
          case JCTree.DIV_ASG:
          case JCTree.MOD_ASG:
              retVal = diffAssignop((JCAssignOp)oldT, (JCAssignOp)newT, elementBounds);
              break;
          case JCTree.ERRONEOUS:
              diffErroneous((JCErroneous)oldT, (JCErroneous)newT);
              break;
          default:
              String msg = "Diff not implemented: " +
                  ((com.sun.source.tree.Tree)oldT).getKind().toString() +
                  " " + oldT.getClass().getName();
              throw new AssertionError(msg);
        }
        diffTrailingComments(oldT, newT);
        return retVal;
    }

    // #todo (#pf): this method should be removed!
    private void diffTree(JCTree oldT, JCTree newT) {
        diffTree(oldT, newT, new int[] { -1, -1 });
    }
    
    private int diffTree(JCTree oldT, JCTree newT, int lastPrinted) {
        return diffTree(oldT, newT, new int[] { lastPrinted, -1 });
    }
    
    protected boolean listsMatch(List<? extends JCTree> oldList, List<? extends JCTree> newList) {
        if (oldList == newList)
            return true;
        int n = oldList.size();
        if (newList.size() != n)
            return false;
        for (int i = 0; i < n; i++)
            if (!treesMatch(oldList.get(i), newList.get(i)))
                return false;
        return true;
    }

    private boolean matchImport(JCImport t1, JCImport t2) {
        return t1.staticImport == t2.staticImport && treesMatch(t1.qualid, t2.qualid);
    }

    private boolean matchBlock(JCBlock t1, JCBlock t2) {
        return t1.flags == t2.flags && listsMatch(t1.stats, t2.stats);
    }

    private boolean matchDoLoop(JCDoWhileLoop t1, JCDoWhileLoop t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.body, t2.body);
    }

    private boolean matchWhileLoop(JCWhileLoop t1, JCWhileLoop t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.body, t2.body);
    }

    private boolean matchForLoop(JCForLoop t1, JCForLoop t2) {
        return listsMatch(t1.init, t2.init) && treesMatch(t1.cond, t2.cond) && 
               listsMatch(t1.step, t2.step) && treesMatch(t1.body, t2.body);
    }
    
    private boolean matchForeachLoop(JCEnhancedForLoop t1, JCEnhancedForLoop t2) {
        return treesMatch(t1.var, t2.var) && treesMatch(t1.expr, t2.expr) &&
               treesMatch(t1.body, t2.body);
    }

    private boolean matchLabelled(JCLabeledStatement t1, JCLabeledStatement t2) {
        return t1.label == t2.label && treesMatch(t1.body, t2.body);
    }

    private boolean matchSwitch(JCSwitch t1, JCSwitch t2) {
        return treesMatch(t1.selector, t2.selector) && listsMatch(t1.cases, t2.cases);
    }

    private boolean matchCase(JCCase t1, JCCase t2) {
        return treesMatch(t1.pat, t2.pat) && listsMatch(t1.stats, t2.stats);
    }

    private boolean matchSynchronized(JCSynchronized t1, JCSynchronized t2) {
        return treesMatch(t1.lock, t2.lock) && treesMatch(t1.body, t2.body);
    }

    private boolean matchTry(JCTry t1, JCTry t2) {
        return treesMatch(t1.finalizer, t2.finalizer) && 
                listsMatch(t1.catchers, t2.catchers) && 
                treesMatch(t1.body, t2.body);
    }

    private boolean matchCatch(JCCatch t1, JCCatch t2) {
        return treesMatch(t1.param, t2.param) && treesMatch(t1.body, t2.body);
    }
    
    private boolean matchConditional(JCConditional t1, JCConditional t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.truepart, t2.truepart) &&
               treesMatch(t1.falsepart, t2.falsepart);
    }
    
    private boolean matchIf(JCIf t1, JCIf t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.thenpart, t2.thenpart) &&
               treesMatch(t1.elsepart, t2.elsepart);
    }

    private boolean matchBreak(JCBreak t1, JCBreak t2) {
        return t1.label == t2.label && treesMatch(t1.target, t2.target);
    }

    private boolean matchContinue(JCContinue t1, JCContinue t2) {
        return t1.label == t2.label && treesMatch(t1.target, t2.target);
    }

    private boolean matchAssert(JCAssert t1, JCAssert t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.detail, t2.detail);
    }
    
    private boolean matchApply(JCMethodInvocation t1, JCMethodInvocation t2) {
        return t1.varargsElement == t2.varargsElement && 
               listsMatch(t1.typeargs, t2.typeargs) &&
               treesMatch(t1.meth, t2.meth) &&
               listsMatch(t1.args, t2.args);
    }
    
    private boolean matchNewClass(JCNewClass t1, JCNewClass t2) {
        return t1.constructor == t2.constructor && 
               listsMatch(t1.typeargs, t2.typeargs) &&
               listsMatch(t1.args, t2.args) &&
               t1.varargsElement == t2.varargsElement;
    }
    
    private boolean matchNewArray(JCNewArray t1, JCNewArray t2) {
        return treesMatch(t1.elemtype, t2.elemtype) &&
               listsMatch(t1.dims, t2.dims) && listsMatch(t1.elems, t2.elems);
    }

    private boolean matchAssign(JCAssign t1, JCAssign t2) {
        return treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }

    private boolean matchAssignop(JCAssignOp t1, JCAssignOp t2) {
        return t1.operator == t2.operator &&
               treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }

    private boolean matchUnary(JCUnary t1, JCUnary t2) {
        return t1.operator == t2.operator && treesMatch(t1.arg, t2.arg);
    }

    private boolean matchBinary(JCBinary t1, JCBinary t2) {
        return t1.operator == t2.operator &&
               treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }
    
    private boolean matchTypeCast(JCTypeCast t1, JCTypeCast t2) {
        return treesMatch(t1.clazz, t2.clazz) && treesMatch(t1.expr, t2.expr);
    }
    
    private boolean matchTypeTest(JCInstanceOf t1, JCInstanceOf t2) {
        return treesMatch(t1.clazz, t2.clazz) && treesMatch(t1.expr, t2.expr);
    }
    
    private boolean matchIndexed(JCArrayAccess t1, JCArrayAccess t2) {
        return treesMatch(t1.indexed, t2.indexed) && treesMatch(t1.index, t2.index);
    }

    private boolean matchLiteral(JCLiteral t1, JCLiteral t2) {
        return t1.typetag == t2.typetag && t1.value == t2.value;
    }

    private boolean matchTypeApply(JCTypeApply t1, JCTypeApply t2) {
        return treesMatch(t1.clazz, t2.clazz) && 
               listsMatch(t1.arguments, t2.arguments);
    }
    
    private boolean matchTypeParameter(JCTypeParameter t1, JCTypeParameter t2) {
        return t1.name == t2.name && listsMatch(t1.bounds, t2.bounds);
    }
    
    private boolean matchWildcard(JCWildcard t1, JCWildcard t2) {
        return t1.kind == t2.kind && treesMatch(t1.inner, t2.inner);
    }

    private boolean matchAnnotation(JCAnnotation t1, JCAnnotation t2) {
        return treesMatch(t1.annotationType, t2.annotationType) &&
               listsMatch(t1.args, t2.args);
    }
    
    private boolean matchModifiers(JCModifiers t1, JCModifiers t2) {
        return t1.flags == t2.flags && listsMatch(t1.annotations, t2.annotations);
    }

    private boolean matchLetExpr(LetExpr t1, LetExpr t2) {
        return listsMatch(t1.defs, t2.defs) && treesMatch(t1.expr, t2.expr);
    }

    private void diffTreeToken(String preceding, int pos, JCTree t1, JCTree t2, String tail) {
        if (t1 == t2) {
            return;
        } else {
            append(Diff.modify(preceding, pos, t1, t2, tail, null));
        }
    }
    
    private int[] getBounds(JCTree tree) {
        return new int[] { getOldPos(tree), endPos(tree) };
    }

    private void copyTo(int from, int to, VeryPretty printer) {
        printer.print(origText.substring(from, to));
    }
    
    private static class Line {
        Line(String data, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
        }
        
        @Override
        public String toString() {
            return data.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Line) {
                return data.equals(((Line) o).data);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return data.hashCode();
        }
        
        String data;
        int end;
        int start;
    }
    
    private List<Line> getLines(String text) {
        char[] chars = text.toCharArray();
        List<Line> list = new ArrayList<Line>();
        int pointer = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                list.add(new Line(new String(chars, pointer, i-pointer+1), pointer, i+1));
                pointer = i+1;
            }
        }
        if (pointer < chars.length) {
            list.add(new Line(new String(chars, pointer, chars.length-pointer), pointer, chars.length));
        }
        return list;
    }
    
    public List<Diff> makeListMatch(String text1, String text2) {
        List<Line> list1 = getLines(text1);
        List<Line> list2 = getLines(text2);
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List diffs = new ComputeDiff(lines1, lines2).diff();
        for (Object o : diffs) {
            Difference diff     = (Difference)o; // generify
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            String from = toString(delStart, delEnd);
            String to = toString(addStart, addEnd);
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            if (logDiffs) {
                System.out.println(from + type + to);
                if (delEnd != Difference.NONE) {
                    printLines(delStart, delEnd, "<", lines1);
                    if (addEnd != Difference.NONE) {
                        System.out.println("---");
                    }
                }
                if (addEnd != Difference.NONE) {
                    printLines(addStart, addEnd, ">", lines2);
                }
            }
            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end,
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
            
            // deletion
            else if (type == 'd') {
                append(Diff.delete(lines1[delStart].start, lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }
                String match1 = builder.toString();
                builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                String match2 = builder.toString();
                makeTokenListMatch(match1, match2, lines1[delStart].start);
                
//                append(Diff.delete(lines1[delStart].start, lines1[delEnd].end));
//                StringBuilder builder = new StringBuilder();
//                for (int i = addStart; i <= addEnd; i++) {
//                    builder.append(lines2[i].data);
//                }
//                append(Diff.insert(delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end,
//                        builder.toString(), null, "", LineInsertionType.NONE));
            }
                    
        }
        return null;
    }
    
    /**
     * Temporary logging variable - just for debugging reason during development.
     */
    private static boolean logDiffs = false;
    
    public List<Diff> makeTokenListMatch(String text1, String text2, int currentPos) {
        if (logDiffs) System.out.println("----- token match for change -");
        TokenSequence<JavaTokenId> seq1 = TokenHierarchy.create(text1, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        TokenSequence<JavaTokenId> seq2 = TokenHierarchy.create(text2, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        List<Line> list1 = new ArrayList<Line>();
        List<Line> list2 = new ArrayList<Line>();
        while (seq1.moveNext()) {
            String data = seq1.token().text().toString();
            list1.add(new Line(data, seq1.offset(), seq1.offset() + data.length()));
        }
        while (seq2.moveNext()) {
            String data = seq2.token().text().toString();
            list2.add(new Line(data, seq2.offset(), seq2.offset() + data.length()));
        }
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List diffs = new ComputeDiff(lines1, lines2).diff();
        for (Object o : diffs) {
            Difference diff     = (Difference)o; // generify
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            String from = toString(delStart, delEnd);
            String to = toString(addStart, addEnd);
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            if (logDiffs) {
                System.out.println(from + type + to);
                if (delEnd != Difference.NONE) {
                    printTokens(delStart, delEnd, "<", lines1);
                    if (addEnd != Difference.NONE) {
                        System.out.println("---");
                    }
                }
                if (addEnd != Difference.NONE) {
                    printTokens(addStart, addEnd, ">", lines2);
                }
            }
            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
            
            // deletion
            else if (type == 'd') {
                append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                /*for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }*/
                append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
                //builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
                    
        }
        if (logDiffs) System.out.println("----- end token match -");
        return null;
    }
    
    protected String toString(int start, int end) {
        // adjusted, because file lines are one-indexed, not zero.
        
        StringBuffer buf = new StringBuffer();
        
        // match the line numbering from diff(1):
        buf.append(end == Difference.NONE ? start : (1 + start));
        
        if (end != Difference.NONE && start != end) {
            buf.append(",").append(1 + end);
        }
        return buf.toString();
    }
    
    private void printLines(int start, int end, String ind, Line[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            System.out.print(ind + " " + lines[lnum]);
        }
    }
    
    private void printTokens(int start, int end, String ind, Line[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            System.out.println(ind + " '" + lines[lnum] + "'");
        }
    }
    
}
