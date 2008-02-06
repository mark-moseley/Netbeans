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

package org.netbeans.api.java.source;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.save.PositionEstimator;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;

/**
 * Replaces identifiers representing all used types with the new ones - imports
 * for them will be solved throughout new commit phase.
 * 
 * This is provided because of refactoring, which wants to take tree from
 * one compilation unit and add it to another one and wants to have all
 * types resolved.
 *
 * @author Pavel Flaska
 */
class TranslateIdentifier implements TreeVisitor<Tree, Void> {
    
    private final WorkingCopy copy;
    private final TreeMaker make;
    private final CompilationUnitTree unit;
    private final boolean copyComments;
    private final boolean resolveImports;
    private final TokenSequence<JavaTokenId> seq;
    private final CommentHandlerService commentService;
    private int tokenIndexAlreadyAdded = -1;
    
    
    public TranslateIdentifier(final WorkingCopy copy, 
            final boolean copyComments, 
            final boolean resolveImports,
            final TokenSequence<JavaTokenId> seq) 
    {
        this.copy = copy;
        this.make = copy.getTreeMaker();
        this.unit = copy.getCompilationUnit();
        this.seq = seq;
        this.copyComments = copyComments;
        this.resolveImports = resolveImports;
        this.commentService = CommentHandlerService.instance(copy.impl.getJavacTask().getContext());
    }

    public Tree visitAnnotation(AnnotationTree node, Void p) {
        Tree annotationType = translate(node.getAnnotationType());
        List<? extends ExpressionTree> arguments = translate(node.getArguments());
        
        if (annotationType != node.getAnnotationType() ||
            arguments != node.getArguments()) 
        {
            node = make.Annotation(annotationType, arguments);
        }
        return node;
    }

    public Tree visitMethodInvocation(MethodInvocationTree node, Void p) {
        List<? extends ExpressionTree> arguments = translate(node.getArguments());
        ExpressionTree methodSelect = (ExpressionTree) translate(node.getMethodSelect());
        List<? extends Tree> typeArguments = translate(node.getTypeArguments());
        
        if (arguments != node.getArguments() ||
            methodSelect != node.getMethodSelect() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.MethodInvocation((List<? extends ExpressionTree>) typeArguments, methodSelect, arguments);
        }
        return node;
    }

    public Tree visitAssert(AssertTree node, Void p) {
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        ExpressionTree detail = (ExpressionTree) translate(node.getDetail());
        
        if (condition != node.getCondition() ||
            detail != node.getDetail())
        {
            node = make.Assert(condition, detail);
        }
        return node;
    }

    public Tree visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translate(node.getVariable());
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.Assignment(variable, expression);
        }
        return node;
    }

    public Tree visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        ExpressionTree variable = (ExpressionTree) translate(node.getVariable());
        
        if (expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.CompoundAssignment(node.getKind(), variable, expression);
        }
        return node;
    }

    public Tree visitBinary(BinaryTree node, Void p) {
        ExpressionTree leftOperand = (ExpressionTree) translate(node.getLeftOperand());
        ExpressionTree rightOperand = (ExpressionTree) translate(node.getRightOperand());
        
        if (leftOperand != node.getLeftOperand() ||
            rightOperand != node.getRightOperand())
        {
            node = make.Binary(node.getKind(), leftOperand, rightOperand);
        }   
        return node;
    }

    public Tree visitBlock(BlockTree node, Void p) {
        List<? extends StatementTree> statements = translate(node.getStatements());
        
        if (statements != node.getStatements()) {
            node = make.Block(statements, node.isStatic());
        }
        return node;
    }

    public Tree visitBreak(BreakTree node, Void p) {
        return node;
    }

    public Tree visitCase(CaseTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        List<? extends StatementTree> statements = translate(node.getStatements());
        
        if (expression != node.getExpression() ||
            statements != node.getStatements())
        {
            node = make.Case(expression, statements);
        }
        return node;
    }

    public Tree visitCatch(CatchTree node, Void p) {
        BlockTree block = (BlockTree) translate(node.getBlock());
        VariableTree parameter = (VariableTree) translate(node.getParameter());
        
        if (block != node.getBlock() ||
            parameter != node.getParameter()) 
        {
            node = make.Catch(parameter, block);
        }
        return node;
    }

    public Tree visitClass(ClassTree node, Void p) {
        Tree extendsClause = translate(node.getExtendsClause());
        List<? extends Tree> implementsClause = translate(node.getImplementsClause());
        List<? extends Tree> members = translate(node.getMembers());
        ModifiersTree modifiers = (ModifiersTree) translate(node.getModifiers());
        List<? extends TypeParameterTree> typeParameters = translate(node.getTypeParameters());
        
        if (extendsClause != node.getExtendsClause() ||
            implementsClause != node.getImplementsClause() ||
            members != node.getMembers() ||
            modifiers != node.getModifiers() ||
            typeParameters != node.getTypeParameters())
        {
            node = make.Class(modifiers, node.getSimpleName(), typeParameters, extendsClause, implementsClause, members);
        }
        return node;
    }

    public Tree visitConditionalExpression(ConditionalExpressionTree node, Void p) {
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        ExpressionTree falseExpression = (ExpressionTree) translate(node.getFalseExpression());
        ExpressionTree trueExpression = (ExpressionTree) translate(node.getTrueExpression());
        
        if (condition != node.getCondition() ||
            falseExpression != node.getFalseExpression() ||
            trueExpression != node.getTrueExpression())
        {
            node = make.ConditionalExpression(condition, trueExpression, falseExpression);
        }
        return node;
    }

    public Tree visitContinue(ContinueTree node, Void p) {
        return node;
    }

    public Tree visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translate(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.DoWhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitErroneous(ErroneousTree node, Void p) {
        List<? extends Tree> errorTrees = translate(node.getErrorTrees());
        
        if (errorTrees != node.getErrorTrees()) {
            node = make.Erroneous(errorTrees);
        }
        return node;
    }

    public Tree visitExpressionStatement(ExpressionStatementTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (expression != node.getExpression()) {
            node = make.ExpressionStatement(expression);
        }
        return node;
    }

    public Tree visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translate(node.getStatement());
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        VariableTree variable = (VariableTree) translate(node.getVariable());
        
        if (statement != node.getStatement() ||
            expression != node.getExpression() ||
            variable != node.getVariable()) 
        {
            node = make.EnhancedForLoop(variable, expression, statement);
        }
        return node;
    }

    public Tree visitForLoop(ForLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translate(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        List<? extends StatementTree> initializer = translate(node.getInitializer());
        List<? extends ExpressionStatementTree> update = translate(node.getUpdate());
        
        if (statement != node.getStatement() ||
            condition != node.getCondition() ||
            initializer != node.getInitializer() ||
            update != node.getUpdate()) 
        {
            node = make.ForLoop(initializer, condition, update, statement);
        }
        return node;
    }

    public Tree visitIdentifier(IdentifierTree node, Void p) {
        if (!resolveImports) return node;
        
        TreePath path = copy.getTrees().getPath(unit, node);
        Element element;
        if (path == null) {
            element = ((JCIdent) node).sym;
        } else {
            element = copy.getTrees().getElement(path);
        }
        if (element != null) {
            // solve the imports only when declared type!!!
            if (element != null && (element.getKind().isClass() || element.getKind().isInterface())) {
                return make.QualIdent(element);
            }
        }
        return node;
    }
    
    public Tree visitIf(IfTree node, Void p) {
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        StatementTree elseStatement = (StatementTree) translate(node.getElseStatement());
        StatementTree thenStatement = (StatementTree) translate(node.getThenStatement());
        
        if (condition != node.getCondition() ||
            elseStatement != node.getElseStatement() ||
            thenStatement != node.getThenStatement())
        {
            node = make.If(condition, thenStatement, elseStatement);
        }
        return node;
    }

    public Tree visitImport(ImportTree node, Void p) {
        return node;
    }

    public Tree visitArrayAccess(ArrayAccessTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        ExpressionTree index = (ExpressionTree) translate(node.getIndex());
        
        if (expression != node.getExpression() ||
            index != node.getIndex())
        {
            node = make.ArrayAccess(expression, index);
        }
        return node;
    }

    public Tree visitLabeledStatement(LabeledStatementTree node, Void p) {
        StatementTree statement = (StatementTree) translate(node.getStatement());
        
        if (statement != node.getStatement()) {
            node = make.LabeledStatement(node.getLabel(), statement);
        }
        return node;
    }

    public Tree visitLiteral(LiteralTree node, Void p) {
        return node;
    }

    public Tree visitMethod(MethodTree node, Void p) {
        BlockTree body = (BlockTree) translate(node.getBody());
        Tree defaultValue = translate(node.getDefaultValue());
        List<? extends VariableTree> parameters = translate(node.getParameters());
        ModifiersTree modifiers = (ModifiersTree) translate(node.getModifiers());
        Tree returnType = translate(node.getReturnType());
        List<? extends ExpressionTree> aThrows = translate(node.getThrows());
        List<? extends TypeParameterTree> typeParameters = translate(node.getTypeParameters());
        
        if (body != node.getBody() ||
            defaultValue != node.getDefaultValue() ||
            parameters != node.getParameters() ||
            modifiers != node.getModifiers() ||
            returnType != node.getReturnType() ||
            aThrows != node.getThrows() ||
            typeParameters != node.getTypeParameters()) 
        {
            node = make.Method(modifiers,
                    node.getName(),
                    returnType,
                    typeParameters,
                    parameters,
                    aThrows,
                    body,
                    (ExpressionTree) defaultValue
            );
        }
        return node;
    }

    public Tree visitModifiers(ModifiersTree node, Void p) {
        List<? extends AnnotationTree> annotations = translate(node.getAnnotations());
        
        if (annotations != node.getAnnotations()) {
            node = make.Modifiers(node.getFlags(), annotations);
        }
        return node;
    }

    public Tree visitNewArray(NewArrayTree node, Void p) {
        List<? extends ExpressionTree> initializers = translate(node.getInitializers());
        List<? extends ExpressionTree> dimensions = translate(node.getDimensions());
        Tree type = translate(node.getType());
        
        if (initializers != node.getInitializers() ||
            dimensions != node.getDimensions() ||
            type != node.getType()) 
        {
            node = make.NewArray(type, dimensions, initializers);
        }
        return node;
    }

    public Tree visitNewClass(NewClassTree node, Void p) {
        List<? extends ExpressionTree> arguments = translate(node.getArguments());
        ClassTree classBody = (ClassTree) translate(node.getClassBody());
        ExpressionTree enclosingExpression = (ExpressionTree) translate(node.getEnclosingExpression());
        ExpressionTree identifier = (ExpressionTree) translate(node.getIdentifier());
        List<? extends Tree> typeArguments = translate(node.getTypeArguments());
        
        if (arguments != node.getArguments() ||
            classBody != node.getClassBody() ||
            enclosingExpression != node.getEnclosingExpression() ||
            identifier != node.getIdentifier() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.NewClass(enclosingExpression, (List<? extends ExpressionTree>) typeArguments, identifier, arguments, classBody);
        }
        return node;
    }

    public Tree visitParenthesized(ParenthesizedTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (expression != node.getExpression()) {
            node = make.Parenthesized(expression);
        }
        return node;
    }

    public Tree visitReturn(ReturnTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (expression != node.getExpression()) {
            node = make.Return(expression);
        }
        return node;
    }

    public Tree visitMemberSelect(MemberSelectTree node, Void p) {
        TypeElement e = copy.getElements().getTypeElement(node.toString());
        if (e != null) {
            return copy.getTreeMaker().QualIdent(e);
        } else {
            return node;
        }
        
//        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
//        
//        if (expression != node.getExpression()) {
//            node = make.MemberSelect(expression, node.getIdentifier());
//        }
//        return node;
    }

    public Tree visitEmptyStatement(EmptyStatementTree node, Void p) {
        return node;
    }

    public Tree visitSwitch(SwitchTree node, Void p) {
        List<? extends CaseTree> cases = translate(node.getCases());
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (cases != node.getCases() ||
            expression != node.getExpression()) 
        {
            node = make.Switch(expression, cases);
        }
        return node;
    }

    public Tree visitSynchronized(SynchronizedTree node, Void p) {
        BlockTree block = (BlockTree) translate(node.getBlock());
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (block != node.getBlock() ||
            expression != node.getExpression())
        {
            node = make.Synchronized(expression, block);
        }
        return node;
    }

    public Tree visitThrow(ThrowTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (expression != node.getExpression()) {
            node = make.Throw(expression);
        }
        return node;
    }

    public Tree visitCompilationUnit(CompilationUnitTree node, Void p) {
        List<? extends Tree> typeDecls = translate(node.getTypeDecls());
        
        if (typeDecls != node.getTypeDecls()) {
            node = make.CompilationUnit(
                    node.getPackageName(),
                    node.getImports(),
                    typeDecls,
                    node.getSourceFile()
            );                   
        }
        return node;
    }

    public Tree visitTry(TryTree node, Void p) {
        BlockTree block = (BlockTree) translate(node.getBlock());
        List<? extends CatchTree> catches = translate(node.getCatches());
        BlockTree finallyBlock = (BlockTree) translate(node.getFinallyBlock());
        
        if (block != node.getBlock() ||
            catches != node.getCatches() ||
            finallyBlock != node.getFinallyBlock())
        {
            node = make.Try(block, catches, finallyBlock);
        }
        return node;
    }

    public Tree visitParameterizedType(ParameterizedTypeTree node, Void p) {
        Tree type = translate(node.getType());
        List<? extends Tree> typeArguments = translate(node.getTypeArguments());
        
        if (type != node.getType() ||
            typeArguments != node.getTypeArguments())
        {
            node = make.ParameterizedType(type, typeArguments);
        }
        return node;
    }

    public Tree visitArrayType(ArrayTypeTree node, Void p) {
        Tree type = translate(node.getType());
        
        if (type != node.getType()) {
            node = make.ArrayType(type);
        }
        return node;
    }

    public Tree visitTypeCast(TypeCastTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        Tree type = translate(node.getType());
        
        if (expression != node.getExpression() ||
            type != node.getType()) 
        {
            node = make.TypeCast(type, expression);
        }
        return node;
    }

    public Tree visitPrimitiveType(PrimitiveTypeTree node, Void p) {
        return node;
    }

    public Tree visitTypeParameter(TypeParameterTree node, Void p) {
        List<? extends Tree> bounds = translate(node.getBounds());
        
        if (bounds != node.getBounds()) {
            node = make.TypeParameter(node.getName(), (List<? extends ExpressionTree>) bounds);
        }
        return node;
    }

    public Tree visitInstanceOf(InstanceOfTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        Tree type = translate(node.getType());
        
        if (expression != node.getExpression() ||
            type != node.getType())
        {
            node = make.InstanceOf(expression, type);
        }
        return node;
    }

    public Tree visitUnary(UnaryTree node, Void p) {
        ExpressionTree expression = (ExpressionTree) translate(node.getExpression());
        
        if (expression != node.getExpression()) {
            node = make.Unary(node.getKind(), expression);
        }
        return node;
    }

    public Tree visitVariable(VariableTree node, Void p) {
        ModifiersTree modifiers = (ModifiersTree) translate(node.getModifiers());
        Tree type = translate(node.getType());
        ExpressionTree initializer = (ExpressionTree) translate(node.getInitializer());

        if (modifiers != node.getModifiers() || type != node.getType() || initializer != node.getInitializer()) {
            node = make.Variable(modifiers, node.getName(), type, initializer);
        }
        return node;
    }

    public Tree visitWhileLoop(WhileLoopTree node, Void p) {
        StatementTree statement = (StatementTree) translate(node.getStatement());
        ExpressionTree condition = (ExpressionTree) translate(node.getCondition());
        
        if (condition != node.getCondition() || statement != node.getStatement()) {
            node = make.WhileLoop(condition, statement);
        }
        return node;
    }

    public Tree visitWildcard(WildcardTree node, Void p) {
        Tree tree = translate(node.getBound());
        
	if (tree != node.getBound()) {
	    node = make.Wildcard(node.getKind(), tree);
        }
        return node;
    }

    public Tree visitOther(Tree node, Void p) {
        return node;
    }

    ////////////////////////////////////////////////////////////////////////////
    public Tree translate(Tree tree) {
        if (tree == null) {
            return null;
        } else {
            if (copyComments) {
                mapComments(tree);
            }
            return tree.accept(this, null);
        }
    }

    public <T extends Tree> List<T> translate(List<T> trees) {
        if (trees == null || trees.isEmpty()) {
            return trees;
        }
        List<T> newTrees = new ArrayList<T>();
        boolean changed = false;
        for (T t : trees) {
            T newT = (T) translate(t);
            if (newT != t) {
                changed = true;
            }
            if (newT != null) {
                newTrees.add(newT);
            }
        }
        return changed ? newTrees : trees;
    }

    private void mapComments(Tree tree) {
        if (((JCTree) tree).pos <= 0) {
            return;
        }
        SourcePositions pos = copy.getTrees().getSourcePositions();
        seq.move((int) pos.getStartPosition(null, tree));
        PositionEstimator.moveToSrcRelevant(seq, Direction.BACKWARD);
        int indent = NOPOS;
        Token<JavaTokenId> token;
        boolean b = false;
        while (seq.moveNext() && nonRelevant.contains((token = seq.token()).id())) {
            if (seq.index() <= tokenIndexAlreadyAdded) {
                return;
            } else {
                if (!b) {
                    tokenIndexAlreadyAdded = seq.index();
                    b = true;
                }
            }
            switch (token.id()) {
                case LINE_COMMENT:
                    commentService.addComment(tree, Comment.create(Style.LINE, NOPOS, NOPOS, indent, token.toString()));
                    indent = 0;
                    break;
                case BLOCK_COMMENT:
                    commentService.addComment(tree, Comment.create(Style.BLOCK, NOPOS, NOPOS, indent, token.toString()));
                    indent = NOPOS;
                    break;
                case JAVADOC_COMMENT:
                    commentService.addComment(tree, Comment.create(Style.JAVADOC, NOPOS, NOPOS, indent, token.toString()));
                    indent = NOPOS;
                    break;
                case WHITESPACE:
                    String tokenText = token.toString();
                    commentService.addComment(tree, Comment.create(Style.WHITESPACE, NOPOS, NOPOS, NOPOS, tokenText));
                    int newLinePos = tokenText.lastIndexOf('\n');
                    if (newLinePos < 0) {
                        if (indent >= 0)
                            indent += tokenText.length();
                    } else {
                        indent = tokenText.length() - newLinePos - 1;
                    }
                    break;
            }
        }
    }
}