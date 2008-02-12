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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
final class MagicSurroundWithTryCatchFix implements Fix {
    
    private JavaSource js;
    private List<TypeMirrorHandle> thandles;
    private int offset;
    private ElementHandle<ExecutableElement> method;
    List<String> fqns;
    
    public MagicSurroundWithTryCatchFix(JavaSource js, List<TypeMirrorHandle> thandles, int offset, ElementHandle<ExecutableElement> method, List<String> fqns) {
        this.js = js;
        this.thandles = thandles;
        this.offset = offset;
        this.method = method;
        this.fqns = fqns;
    }
    
    public String getText() {
        return NbBundle.getMessage(MagicSurroundWithTryCatchFix.class, "LBL_SurroundBlockWithTryCatch");
    }
    
    private static final String[] STREAM_ALIKE_CLASSES = new String[] {
        "java.io.InputStream",
        "java.io.OutputStream",
        "java.io.Reader",
        "java.io.Writer",
    };
    
    private boolean isStreamAlike(CompilationInfo info, TypeMirror type) {
        for (String fqn : STREAM_ALIKE_CLASSES) {
            Element inputStream = info.getElements().getTypeElement(fqn);
            
            if (info.getTypes().isAssignable(type, inputStream.asType()))
                return true;
        }
        
        return false;
    }

    public ChangeInfo implement() throws IOException {
        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                TreePath currentPath = wc.getTreeUtilities().pathFor(offset + 1);

                //find statement:
                while (currentPath != null && !UncaughtException.STATEMENT_KINDS.contains(currentPath.getLeaf().getKind()))
                    currentPath = currentPath.getParentPath();

                //TODO: test for final??
                TreePath statement = currentPath;
                boolean streamAlike = false;

                if (statement.getLeaf().getKind() == Kind.VARIABLE) {
                    //special case variable declarations which intializers create streams or readers/writers:
                    Element curType = wc.getTrees().getElement(statement);

                    streamAlike = isStreamAlike(wc, curType.asType());
                }

                //find try block containing this statement, if exists:
                TreePath catchTree = currentPath;

                while (catchTree != null
                        && catchTree.getLeaf().getKind() != Kind.TRY
                        && catchTree.getLeaf().getKind() != Kind.CLASS
                        && catchTree.getLeaf().getKind() != Kind.CATCH)
                    catchTree = catchTree.getParentPath();

                boolean createNewTryCatch = false;

                if (catchTree.getLeaf().getKind() == Kind.TRY) {
                    TryTree tt = (TryTree) catchTree.getLeaf();
                    //#104085: if the statement to be wrapped is inside a finally block of the try-catch,
                    //do not attempt to extend existing catches...:
                    for (Tree t : currentPath) {
                        if (tt.getFinallyBlock() == t) {
                            createNewTryCatch = true;
                            break;
                        }
                    }

                    if (!createNewTryCatch) {
                    //only add catches for uncatched exceptions:
                    new TransformerImpl(wc, thandles, streamAlike, statement).scan(catchTree, null);
                    }
                } else {
                    createNewTryCatch = true;
                }

                if (createNewTryCatch) {
                    //find block containing this statement, if exists:
                    TreePath blockTree = currentPath;

                    while (blockTree != null
                            && blockTree.getLeaf().getKind() != Kind.BLOCK)
                        blockTree = blockTree.getParentPath();

                    new TransformerImpl(wc, thandles, streamAlike, statement).scan(blockTree, null);
                }
            }
        }).commit();
        
        return null;
    }
    
    static boolean DISABLE_JAVA_UTIL_LOGGER = false;
    
    private final class TransformerImpl extends TreePathScanner<Void, Void> {
        
        private WorkingCopy info;
        private List<TypeMirrorHandle> thandles;
        private boolean streamAlike;
        private TreePath statement;
        private TreeMaker make;
        
        public TransformerImpl(WorkingCopy info, List<TypeMirrorHandle> thandles, boolean streamAlike, TreePath statement) {
            this.info = info;
            this.thandles = thandles;
            this.streamAlike = streamAlike;
            this.statement = statement;
            this.make = info.getTreeMaker();
        }
        
        public @Override Void visitTry(TryTree tt, Void p) {
            List<CatchTree> catches = createCatches(info, make, thandles, statement);
            
            catches.addAll(tt.getCatches());
            
            if (!streamAlike) {
                info.rewrite(tt, make.Try(tt.getBlock(), catches, tt.getFinallyBlock()));
            } else {
                VariableTree originalDeclaration = (VariableTree) statement.getLeaf();
                VariableTree declaration = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), originalDeclaration.getName(), originalDeclaration.getType(), make.Literal(null));
                StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(originalDeclaration.getName()), originalDeclaration.getInitializer()));
                List<StatementTree> finallyStatements = new ArrayList<StatementTree>(tt.getFinallyBlock() != null ? tt.getFinallyBlock().getStatements() : Collections.<StatementTree>emptyList());
                
                finallyStatements.add(createFinallyCloseBlockStatement(originalDeclaration));
                
                BlockTree finallyTree = make.Block(finallyStatements, false);
                
                info.rewrite(originalDeclaration, assignment);
                
                TryTree nueTry = make.Try(tt.getBlock(), catches, finallyTree);
                
                TreePath currentBlockCandidate = statement;
                
                while (currentBlockCandidate.getLeaf() != tt) {
                    currentBlockCandidate = currentBlockCandidate.getParentPath();
                }
                
                if (currentBlockCandidate.getLeaf().getKind() == Kind.BLOCK) {
                    BlockTree originalTree = (BlockTree) currentBlockCandidate.getLeaf();
                    List<StatementTree> statements = new ArrayList<StatementTree>(originalTree.getStatements());
                    int index = statements.indexOf(tt);
                    
                    statements.remove(index);
                    statements.add(index, nueTry);
                    statements.add(index, declaration);
                    info.rewrite(originalTree, make.Block(statements, false));
                } else {
                    BlockTree nueBlock = make.Block(Arrays.asList(declaration, nueTry), false);
                    
                    info.rewrite(tt, nueBlock);
                }
            }
            
            return null;
        }
        
        private StatementTree createFinallyCloseBlockStatement(VariableTree origDeclaration) {
            Trees trees = info.getTrees();
            TypeMirror tm = trees.getTypeMirror(statement);
            ElementUtilities elUtils = info.getElementUtilities();
            Iterable iterable = elUtils.getMembers(tm, new ElementAcceptor() {
                public boolean accept(Element e, TypeMirror type) {
                    return e.getKind() == ElementKind.METHOD && "close".equals(e.getSimpleName().toString()); // NOI18N
                }
            });
            boolean throwsIO = false;
            for (Iterator iter = iterable.iterator(); iter.hasNext(); ) {
                ExecutableElement elem = (ExecutableElement) iter.next();
                if (!elem.getParameters().isEmpty()) {
                    continue;
                } else {
                     for (TypeMirror typeMirror : elem.getThrownTypes()) {
                         if ("java.io.IOException".equals(typeMirror.toString())) { // NOI18N
                             throwsIO = true;
                             break;
                         }
                     }
                }
            }
            
            CharSequence name = origDeclaration.getName();
            StatementTree close = make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(name), "close"), Collections.<ExpressionTree>emptyList()));
            StatementTree result = close;
            if (throwsIO) {
                result = make.Try(make.Block(Collections.singletonList(close), false), Collections.singletonList(createCatch(info, make, statement, inferName(info, statement), info.getElements().getTypeElement("java.io.IOException").asType())), null);
            }
            
            return result;
        }
        
        private BlockTree createBlock(StatementTree... trees) {
            List<StatementTree> statements = new LinkedList<StatementTree>();
            
            for (StatementTree t : trees) {
                if (t != null) {
                    statements.add(t);
                }
            }
            
            return make.Block(statements, false);
        }
        
        public @Override Void visitBlock(BlockTree bt, Void p) {
            List<CatchTree> catches = createCatches(info, make, thandles, statement);
            
            //#89379: if inside a constructor, do not wrap the "super"/"this" call:
            //please note that the "super" or "this" call is supposed to be always
            //in the constructor body
            BlockTree toUse = bt;
            StatementTree toKeep = null;
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            
            if (parent.getKind() == Kind.METHOD && bt.getStatements().size() > 0) {
                MethodTree mt = (MethodTree) parent;
                
                if (mt.getReturnType() == null) {
                    toKeep = bt.getStatements().get(0);
                    toUse = make.Block(bt.getStatements().subList(1, bt.getStatements().size()), false);
                }
            }
            
            if (!streamAlike) {
                info.rewrite(bt, createBlock(toKeep, make.Try(toUse, catches, null)));
            } else {
                VariableTree originalDeclaration = (VariableTree) statement.getLeaf();
                VariableTree declaration = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), originalDeclaration.getName(), originalDeclaration.getType(), make.Identifier("null"));
                StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(originalDeclaration.getName()), originalDeclaration.getInitializer()));
                BlockTree finallyTree = make.Block(Collections.singletonList(createFinallyCloseBlockStatement(originalDeclaration)), false);
                
                info.rewrite(originalDeclaration, assignment);
                info.rewrite(bt, createBlock(toKeep, declaration, make.Try(toUse, catches, finallyTree)));
            }
            
            return null;
        }
        
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MagicSurroundWithTryCatchFix other = (MagicSurroundWithTryCatchFix) obj;
        if (this.js != other.js && (this.js == null || !this.js.equals(other.js))) {
            return false;
        }
        if (!this.fqns.equals(other.fqns)) {
            return false;
        }
        if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.js != null ? this.js.hashCode() : 0);
        hash = 23 * hash + (this.method != null ? this.method.hashCode() : 0);
        return hash;
    }

    
    private static StatementTree createExceptionsStatement(CompilationInfo info, TreeMaker make, String name) {
        if (!ErrorFixesFakeHint.isUseExceptions()) {
            return null;
        }
        
        TypeElement exceptions = info.getElements().getTypeElement("org.openide.util.Exceptions");

        if (exceptions == null) {
            return null;
        }

        return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.QualIdent(exceptions), "printStackTrace"), Arrays.asList(make.Identifier(name))));
    }

    private static StatementTree createLogStatement(CompilationInfo info, TreeMaker make, TreePath statement, String name) {
        if (!ErrorFixesFakeHint.isUseLogger()) {
            return null;
        }

        if (!GeneratorUtils.supportsOverride(info.getFileObject())) {
            return null;
        }

        TypeElement logger = info.getElements().getTypeElement("java.util.logging.Logger");
        TypeElement level = info.getElements().getTypeElement("java.util.logging.Level");

        if (logger == null || level == null) {
            return null;
        }
        // find the containing top level class
        ClassTree containingTopLevel = null;
        for (Tree t : statement) {
            if (Kind.CLASS == t.getKind()) {
                containingTopLevel = (ClassTree) t;
            }
        }
        // take it easy and make it as an identfier or literal
        ExpressionTree arg = containingTopLevel != null ? make.Identifier(containingTopLevel.getSimpleName() + ".class.getName()") : make.Literal("global"); // global should never happen

        // check that there isn't any Logger class imported
        boolean useFQN = false;
        for (ImportTree dovoz : info.getCompilationUnit().getImports()) {
            MemberSelectTree id = (MemberSelectTree) dovoz.getQualifiedIdentifier();
            if ("Logger".equals(id.getIdentifier()) && !"java.util.logging.Logger".equals(id.toString())) {
                useFQN = true;
            }
        }
        // finally, make the invocation
        ExpressionTree etExpression = make.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                make.MemberSelect(
                useFQN ? make.Identifier(logger.toString()) : make.QualIdent(logger),
                "getLogger"),
                Collections.<ExpressionTree>singletonList(arg));
        ExpressionTree levelExpression = make.MemberSelect(make.QualIdent(level), "SEVERE");

        return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(etExpression, "log"), Arrays.asList(levelExpression, make.Literal(null), make.Identifier(name))));
    }
        
    private static StatementTree createPrintStackTraceStatement(CompilationInfo info, TreeMaker make, String name) {
        return make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(name), "printStackTrace"), Collections.<ExpressionTree>emptyList()));
    }

    private static CatchTree createCatch(CompilationInfo info, TreeMaker make, TreePath statement, String name, TypeMirror type) {
        StatementTree logStatement = createExceptionsStatement(info, make, name);

        if (logStatement == null) {
            logStatement = createLogStatement(info, make, statement, name);
        }
        
        if (logStatement == null) {
            logStatement = createPrintStackTraceStatement(info, make, name);
        }

        return make.Catch(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(type), null), make.Block(Collections.singletonList(logStatement), false));
    }

    static List<CatchTree> createCatches(CompilationInfo info, TreeMaker make, List<TypeMirrorHandle> thandles, TreePath currentPath) {
        String name = inferName(info, currentPath);
        List<CatchTree> catches = new ArrayList<CatchTree>();

        for (TypeMirrorHandle th : thandles) {
            catches.add(createCatch(info, make, currentPath, name, th.resolve(info)));
        }

        return catches;
    }

    private static String inferName(CompilationInfo info, TreePath currentPath) {
        Scope s = info.getTrees().getScope(currentPath);
        Set<String> existingVariables = new HashSet<String>();

        for (Element e : info.getElementUtilities().getLocalVars(s, new ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {
                return e != null && (e.getKind() == ElementKind.PARAMETER || e.getKind() == ElementKind.LOCAL_VARIABLE || e.getKind() == ElementKind.EXCEPTION_PARAMETER);
            }
            })) {
            existingVariables.add(e.getSimpleName().toString());
        }

        int index = 0;

        while (true) {
            String proposal = "ex" + (index == 0 ? "" : ("" + index));

            if (!existingVariables.contains(proposal)) {
                return proposal;
            }

            index++;
        }
    }
    
}
