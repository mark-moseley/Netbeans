/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public final class ModelVisitor extends DefaultTreePathVisitor {

    private final FileScope fileScope;
    private Map<VariableContainerImpl, Map<String, VariableNameImpl>> vars;
    private Map<String, List<PhpDocTypeTagInfo>> varTypeComments;
    //private Map<String, String> var2TypeName;
    private OccurenceBuilder occurencesBuilder;
    private CodeMarkerBuilder markerBuilder;
    private ModelBuilder modelBuilder;
    private ParserResult info;

    public ModelVisitor(ParserResult info) {
        this(info, -1);        
    }

    public ModelVisitor(ParserResult info, int offset) {
        this.fileScope = new FileScope(info);
        varTypeComments = new HashMap<String, List<PhpDocTypeTagInfo>>();
        //var2TypeName = new HashMap<String, String>();
        occurencesBuilder = new OccurenceBuilder(offset);
        markerBuilder = new CodeMarkerBuilder(offset);
        this.modelBuilder = new ModelBuilder(this.fileScope, occurencesBuilder.getOffset());
        this.info = info;
    }

    public ParserResult getCompilationInfo() {
        return this.info;
    }

    @Override
    public void scan(ASTNode node) {
        super.scan(node);
    }

    @Override
    public void visit(ReturnStatement node) {
        markerBuilder.prepare(node, modelBuilder.getCurrentScope());
        super.visit(node);
    }

    @Override
    public void visit(Program program) {
        modelBuilder.setProgram(program);
        fileScope.setBlockRange(program);
        this.vars = new HashMap<VariableContainerImpl, Map<String, VariableNameImpl>>();
        try {
            prepareVarComments(program);
            super.visit(program);
            handleVarComments();
        } finally {
            program = null;
            vars = null;
            buildOccurences();
            buildCodeMarks();
        }
    }

    @Override
    public void visit(Include node) {
        modelBuilder.build(node, occurencesBuilder);
        super.visit(node);
    }

    @Override
    public void visit(ClassDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        checkComments(node);
        try {
            super.visit(node);
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        try {
            super.visit(node);
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(MethodDeclaration node) {
        modelBuilder.build(node, occurencesBuilder);
        markerBuilder.prepare(node, modelBuilder.getCurrentScope());
        checkComments(node);

        try {
            //super.visit(node);
            scan(node.getFunction().getFormalParameters());
            scan(node.getFunction().getBody());
        } finally {
            modelBuilder.reset();
        }
    }

    @Override
    public void visit(FieldsDeclaration node) {        
        modelBuilder.build(node, occurencesBuilder);
        checkComments(node);
        /*ScopeImpl scope = modelBuilder.getCurrentScope();
        assert scope != null && scope instanceof ClassScopeImpl;
        ClassScopeImpl classScope = (ClassScopeImpl) scope;
        List<? extends FieldElementImpl> createElements = classScope.createElements(modelBuilder.getProgram(), node);
         */
        super.visit(node);
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        scan(node.ctorParams());
    }

    @Override
    public void visit(InstanceOfExpression node) {
        occurencesBuilder.prepare(node.getClassName(), modelBuilder.getCurrentScope());
        String clsName = CodeUtils.extractClassName(node.getClassName());
        if (clsName != null) {
            Expression expression = node.getExpression();
            if (expression instanceof Variable) {
                Variable var = (Variable) expression;
                ScopeImpl currentScope = modelBuilder.getCurrentScope();
                VariableNameImpl varN = findVariable(currentScope, var);
                if (varN != null) {
                    varN.addElement(new VarAssignmentImpl(varN, currentScope,
                            getBlockRange(currentScope), ASTNodeInfo.create(var).getRange(),clsName));
                }
            }

        }
        super.visit(node);
    }

    @Override
    public void visit(MethodInvocation node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        scan(node.getDispatcher());
        scan(node.getMethod().getParameters());
    }

    @Override
    public void visit(Scalar scalar) {
        String stringValue = scalar.getStringValue();
        if (scalar.getScalarType() == Type.STRING && !NavUtils.isQuoted(stringValue)) {
            //if (!stringValue.equals("false") && !stringValue.equals("true")) {//NOI18N
            occurencesBuilder.prepare(Kind.CONSTANT, scalar, fileScope);
        //}
        }
        super.visit(scalar);
    }

    @Override
    public void visit(StaticMethodInvocation node) {
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        //scan(node.getClassName());
        scan(node.getMethod().getParameters());

    }

    @Override
    public void visit(ClassName node) {
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
    }

    @Override
    public void visit(StaticConstantAccess node) {
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        occurencesBuilder.prepare(Kind.IFACE, node.getClassName(), scope);
    }

    @Override
    public void visit(ClassConstantDeclaration node) {
        //ScopeImpl scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        //TODO: constants can be also in ifaces
        assert scope != null && scope instanceof TypeScopeImpl;
        List<? extends ClassConstantDeclarationInfo> constantDeclarationInfos = ClassConstantDeclarationInfo.create(node);
        TypeScopeImpl typeScope = (TypeScopeImpl) scope;
        //InterfaceScopeImpl interfaceScopeImpl = (InterfaceScopeImpl) scope;

        for (ClassConstantDeclarationInfo nodeInfo : constantDeclarationInfos) {
            ClassConstantElement element = typeScope.createElement(nodeInfo);
            occurencesBuilder.prepare(nodeInfo, element);
        }
        super.visit(node);
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        //super.visit(node);
    }

    @Override
    public void visit(ReflectionVariable node) {
        Expression name = node.getName();
        while (name instanceof ReflectionVariable) {
            ReflectionVariable refName = (ReflectionVariable) name;
            name = refName.getName();
        }
        if (name instanceof Variable) {
            scan(name);
        }
    /*if (name instanceof Variable) {
    scan(name);
    } else if (name instanceof FieldAccess) {

    }*/
    //super.visit(node);
    }

    @Override
    public void visit(Variable node) {
        String varName = CodeUtils.extractVariableName(node);
        if (varName == null) {
            return;
        }
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);

        if (scope instanceof VariableContainerImpl) {
            VariableContainerImpl varContainer = (VariableContainerImpl) scope;
            Map<String, VariableNameImpl> map = vars.get(varContainer);
            if (map == null) {
                map = new HashMap<String, VariableNameImpl>();
                vars.put(varContainer, map);
            }
            String name = VariableNameImpl.toName(node);
            VariableName original = map.get(name);
            if (original == null) {
                VariableNameImpl varInstance = varContainer.createElement(modelBuilder.getProgram(), node);
                map.put(name, varInstance);
            }
        } else {
            assert scope instanceof ClassScopeImpl : scope;
        }
        super.visit(node);
    }

    @Override
    public void visit(GlobalStatement node) {
        super.visit(node);
        List<Variable> variables = node.getVariables();
        for (Variable var : variables) {
            String varName = CodeUtils.extractVariableName(var);
            //TODO: ugly
            if (varName == null) {
                continue;
            }
            ScopeImpl scope = modelBuilder.getCurrentScope();
            if (scope instanceof VariableContainerImpl) {
                VariableContainerImpl vc = (VariableContainerImpl) scope;
                List<? extends VariableNameImpl> variablesImpl = vc.getVariablesImpl(varName);
                VariableNameImpl varElem = ModelUtils.getFirst(variablesImpl);
                if (varElem != null) {
                    varElem.setGloballyVisible(true);
                } else {
                    vc = (VariableContainerImpl) modelBuilder.getFileScope();
                    variablesImpl = vc.getVariablesImpl(varName);
                    varElem = ModelUtils.getFirst(variablesImpl);
                    if (varElem != null) {
                        varElem.setGloballyVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void visit(FieldAccess node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        //super.visit(node);
        Variable field = node.getField();
        if (field instanceof ArrayAccess) {
            ArrayAccess access = (ArrayAccess) field;
            scan(access.getIndex());
            VariableBase name = access.getName();
            while (name instanceof ArrayAccess) {
                ArrayAccess access1 = (ArrayAccess) name;
                scan(access1.getIndex());
                name = access1.getName();
            }
        }
        scan(node.getDispatcher());
    }

    @Override
    public void visit(FunctionName node) {
        //intentionally ommited - if deleted, golden tests will fail and will show the reason 
        //super.visit(node);
    }

    @Override
    public void visit(Assignment node) {
        //ScopeImpl scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        final VariableBase leftHandSide = node.getLeftHandSide();
        Expression rightHandSide = node.getRightHandSide();
        super.scan(leftHandSide);
        if (leftHandSide instanceof Variable) {
            VariableNameImpl varN = findVariable(scope, leftHandSide);
            //TODO: global variables or vars from other files
            //assert varN != null : CodeUtils.extractVariableName((Variable)leftHandSide);
            if (varN != null) {
                Map<String, VariableNameImpl> allAssignments = vars.get(scope);
                Variable var = ((Variable) leftHandSide);
                varN.createElement(scope, getBlockRange(scope), new OffsetRange(var.getStartOffset(), var.getEndOffset()), node, allAssignments);
                occurencesBuilder.prepare((Variable) leftHandSide, scope);
            }

        }

        super.scan(rightHandSide);
    }

    @Override
    public void visit(FormalParameter node) {
        Expression parameterName = node.getParameterName();
        Identifier parameterType = node.getParameterType();
        String typeName = parameterType != null ? parameterType.getName() : null;
        //FunctionScopeImpl scope = (FunctionScopeImpl) currentScope.peek();
        FunctionScopeImpl scope = (FunctionScopeImpl) modelBuilder.getCurrentScope();
        while(parameterName instanceof Reference) {
            Reference ref = (Reference)parameterName;
            Expression expression = ref.getExpression();
            if (expression instanceof Variable || expression instanceof Reference) {
                parameterName = expression;
            }
        }
        if (typeName != null && parameterName instanceof Variable) {
            VariableNameImpl varNameImpl = scope.createElement(modelBuilder.getProgram(), (Variable) parameterName);
            varNameImpl.addElement(new VarAssignmentImpl(varNameImpl, scope, scope.getBlockRange(),
                    new OffsetRange(parameterType.getStartOffset(), parameterType.getEndOffset()), typeName));
        }
        if (parameterName instanceof Variable) {
            occurencesBuilder.prepare(Kind.CLASS, parameterType, scope);
            occurencesBuilder.prepare(Kind.IFACE, parameterType, scope);
            occurencesBuilder.prepare((Variable) parameterName, scope);
        }
        super.visit(node);
    }

    @Override
    public void visit(CatchClause node) {
        Identifier className = node.getClassName();
        Variable variable = node.getVariable();
        //ScopeImpl scopeImpl = currentScope.peek();
        ScopeImpl scopeImpl = modelBuilder.getCurrentScope();
        VariableContainerImpl varContainer = (VariableContainerImpl) scopeImpl;
        if (varContainer instanceof VariableContainerImpl) {
            VariableContainerImpl ps = (VariableContainerImpl) varContainer;
            Map<String, VariableNameImpl> map = vars.get(ps);
            if (map == null) {
                map = new HashMap<String, VariableNameImpl>();
                vars.put(ps, map);
            }
            VariableNameImpl varNameImpl = varContainer.createElement(modelBuilder.getProgram(), variable);
            String name = varNameImpl.getName();
            varNameImpl.addElement(new VarAssignmentImpl(varNameImpl, scopeImpl,new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                    VariableNameImpl.toOffsetRange(variable), className.getName()));
            VariableName original = map.get(name);
            if (original == null) {
                map.put(name, varNameImpl);
            }
        }

        occurencesBuilder.prepare(Kind.CLASS, className, scopeImpl);
        occurencesBuilder.prepare(variable, scopeImpl);


        scan(node.getBody());
    }

    @Override
    public void visit(FunctionDeclaration node) {
        //ScopeImpl scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        assert scope != null && ((scope instanceof FunctionScopeImpl) ||
                (scope instanceof MethodScopeImpl) || (scope instanceof FileScope));
        if (scope instanceof FileScope) {
            FileScope ps = (FileScope) scope;
            FunctionScopeImpl fncScope = ps.createElement(modelBuilder.getProgram(), node);
            //currentScope.push(scope = fncScope);
            modelBuilder.setCurrentScope(scope = fncScope);
            occurencesBuilder.prepare(node, fncScope);
            markerBuilder.prepare(node, modelBuilder.getCurrentScope());
            checkComments(node);
        } else if (!(scope instanceof ModelScope)) {
            ScopeImpl tmpScope = scope;
            while (!(tmpScope instanceof ModelScope)) {
                tmpScope = tmpScope.getInScope();
            }
            if (tmpScope instanceof FileScope) {
                FileScope ps = (FileScope) tmpScope;
                FunctionScopeImpl fncScope = ps.createElement(modelBuilder.getProgram(), node);
                //currentScope.push(scope = fncScope);
                modelBuilder.setCurrentScope(scope = fncScope);
                occurencesBuilder.prepare(node, fncScope);
                markerBuilder.prepare(node, modelBuilder.getCurrentScope());
                checkComments(node);
            }
        }
        scope.setBlockRange(node.getBody());
        scan(node.getFormalParameters());
        scan(node.getBody());
        modelBuilder.reset();
    }

    @Override
    public void visit(FunctionInvocation node) {
        //ScopeImpl scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        ASTNodeInfo<FunctionInvocation> nodeInfo = ASTNodeInfo.create(node);
        String name = nodeInfo.getName();
        if ("define".equals(name) && node.getParameters().size() == 2) {//NOI18N
            Expression d = node.getParameters().get(0);
            if (d instanceof Scalar && ((Scalar) d).getScalarType() == Type.STRING) {
                Scalar scalar = (Scalar) d;
                String value = scalar.getStringValue();
                if (NavUtils.isQuoted(value)) {
                    ASTNodeInfo<Scalar> scalarInfo = ASTNodeInfo.create(Kind.CONSTANT, scalar);
                    ConstantElementImpl constantImpl = fileScope.createElement(scalarInfo);
                    occurencesBuilder.prepare(scalarInfo, constantImpl);
                }
            }
        } else if ("constant".equals(name) && node.getParameters().size() == 1) {
            Expression d = node.getParameters().get(0);
            if (d instanceof Scalar) {
                Scalar scalar = (Scalar) d;
                if (scalar.getScalarType() == Type.STRING && NavUtils.isQuoted(scalar.getStringValue())) {
                    occurencesBuilder.prepare(Kind.CONSTANT, scalar, fileScope);
                }

            }

        }

        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        //ScopeImpl scope = currentScope.peek();
        ScopeImpl scope = modelBuilder.getCurrentScope();
        occurencesBuilder.prepare(node, scope);
        occurencesBuilder.prepare(Kind.CLASS, node.getClassName(), scope);
        Variable field = node.getField();
        if (field instanceof ArrayAccess) {
            ArrayAccess access = (ArrayAccess) field;
            scan(access.getIndex());
            VariableBase name = access.getName();
            while (name instanceof ArrayAccess) {
                ArrayAccess access1 = (ArrayAccess) name;
                scan(access1.getIndex());
                name = access1.getName();
            }
        }

    //super.visit(node);
    }

    @Override
    public void visit(PHPDocTypeTag node) {
        occurencesBuilder.prepare(node, modelBuilder.getCurrentScope());
        super.visit(node);
    }
    @Override
    public void visit(PHPDocVarTypeTag node) {
        ScopeImpl currentScope = modelBuilder.getCurrentScope();
        List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(node, currentScope);
        for (PhpDocTypeTagInfo phpDocTypeTagInfo : tagInfos) {
            if (phpDocTypeTagInfo.getKind().equals(Kind.FIELD)) {
                new FieldElementImpl(currentScope, phpDocTypeTagInfo.getTypeName(), phpDocTypeTagInfo);
            }
        }
        occurencesBuilder.prepare(node, currentScope);
        super.visit(node);
    }

    public ModelScope getModelScope() {
        return fileScope;
    }

    @CheckForNull
    public CodeMarker getCodeMarker(int offset) {
        return findStrictCodeMarker((FileScope) getModelScope(), offset, null);
    }

    private void checkComments(ASTNode node) {
        Comment comment = node instanceof Comment ? (Comment)node :
            Utils.getCommentForNode(modelBuilder.getProgram(), node);
        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;
            for (PHPDocTag tag : phpDoc.getTags()) {
                scan(tag);
            }
        } else if (comment instanceof PHPVarComment) {
            PHPDocVarTypeTag typeTag = ((PHPVarComment) comment).getVariable();
            List<? extends PhpDocTypeTagInfo> tagInfos = PhpDocTypeTagInfo.create(typeTag, fileScope);
            for (PhpDocTypeTagInfo tagInfo : tagInfos) {
                if (tagInfo.getKind().equals(ASTNodeInfo.Kind.VARIABLE)) {
                    String name = tagInfo.getName();
                    List<PhpDocTypeTagInfo> infos = varTypeComments.get(name);
                    if (infos == null) {
                        infos = new ArrayList<PhpDocTypeTagInfo>();
                        varTypeComments.put(name, infos);
                    }
                    infos.add(tagInfo);
                }
            }            
        }
    }

    @CheckForNull
    private ASTNode findConditionalStatement(List<ASTNode> path) {
        for (ASTNode aSTNode : path) {
            if (aSTNode instanceof IfStatement) {
                return aSTNode;
            } else if (aSTNode instanceof WhileStatement) {
                return aSTNode;
            } else if (aSTNode instanceof DoStatement) {
                return aSTNode;
            } else if (aSTNode instanceof ForEachStatement) {
                return aSTNode;
            } else if (aSTNode instanceof ForStatement) {
                return aSTNode;
            } else if (aSTNode instanceof CatchClause) {
                return aSTNode;
            } else if (aSTNode instanceof SwitchStatement) {
                return aSTNode;
            }
        }
        return null;
    }

    private CodeMarker findStrictCodeMarker(FileScope scope, int offset, CodeMarker atOffset) {
        buildCodeMarks();
        List<? extends CodeMarker> markers = scope.getMarkers();
        for (CodeMarker codeMarker : markers) {
            assert codeMarker != null;
            if (codeMarker.getOffsetRange().containsInclusive(offset)) {
                atOffset = codeMarker;
            }
        }
        return atOffset;
    }

    @CheckForNull
    public Occurence getOccurence(int offset) {
        return findStrictOccurence((FileScope) getModelScope(), offset, null);
    }

    public VariableScope getNearestVariableScope(int offset) {
        return findNearestVarScope((FileScope) getModelScope(), offset, null);
    }

    public VariableScope getVariableScope(int offset) {
        VariableScope retval = null;
        List<ModelElement> elements = new ArrayList<ModelElement>();
        elements.add(getModelScope());
        elements.addAll(getModelScope().getElements());
        for (ModelElement modelElement : elements) {
            if (modelElement instanceof VariableScope) {
                VariableScope varScope = (VariableScope) modelElement;
                if (varScope.getBlockRange().containsInclusive(offset)) {
                    if (retval == null ||
                            retval.getBlockRange().overlaps(varScope.getBlockRange())) {
                        retval = varScope;
                    }
                }
            } else if (modelElement instanceof ClassScope) {
                ClassScope clsScope = (ClassScope) modelElement;
                List<? extends MethodScope> allMethods = clsScope.getAllMethods();
                for (MethodScope methodScope : allMethods) {
                    if (methodScope.getBlockRange().containsInclusive(offset)) {
                        if (retval == null ||
                                retval.getBlockRange().overlaps(methodScope.getBlockRange())) {
                            retval = methodScope;
                        }
                    }
                }
            }
        }
        return retval;
    }

    static List<Occurence> getAllOccurences(ModelScope modelScope, Occurence occurence) {
        ModelElementImpl declaration = (ModelElementImpl) occurence.getDeclaration();
        if (declaration instanceof MethodScope) {
            MethodScope methodScope = (MethodScope) declaration;
            if (methodScope.isConstructor()) {
                declaration = (ModelElementImpl) methodScope.getInScope();
            }
        }
        if (declaration instanceof VarAssignmentImpl) {
            VarAssignmentImpl impl = (VarAssignmentImpl) declaration;
            declaration = impl.getVar();
        }
        FileScope fileScope = (FileScope) modelScope;
        return fileScope.getAllOccurences(declaration);
    }

    public static IndexScope getIndexScope(ParserResult info) {
        return new IndexScopeImpl(info);
    }

    public static IndexScope getIndexScope(PHPIndex idx) {
        return new IndexScopeImpl(idx);
    }

    private void buildCodeMarks() {
        if (markerBuilder != null) {
            markerBuilder.build(fileScope);
            markerBuilder = null;
        }
    }

    private void buildOccurences() {
        if (occurencesBuilder != null) {
            occurencesBuilder.build(fileScope);
            occurencesBuilder = null;
            fileScope.getCachedModelSupport().clearCaches();
        }
    }

    private Occurence findStrictOccurence(FileScope scope, int offset,
            Occurence atOffset) {
        buildOccurences();
        //FileObject fileObject = scope.getFileObject();
        List<Occurence> occurences = scope.getOccurences();
        for (Occurence occ : occurences) {
            assert occ != null;
            if (occ.getOccurenceRange().containsInclusive(offset)) {
                atOffset = occ;
            }
        }
        return atOffset;
    }

    private VariableScope findNearestVarScope(Scope scope, int offset, VariableScope atOffset) {
        buildOccurences();
        List<? extends ModelElementImpl> elements = ((ScopeImpl)scope).getElements();
        for (ModelElementImpl varScope : elements) {
            if (varScope instanceof ClassScope) {
                atOffset = findNearestVarScope((ClassScope)varScope, offset, atOffset);
            }
            if (varScope instanceof VariableScope) {
                if (varScope.getNameRange().getStart() <= offset) {
                    if (atOffset == null || atOffset.getOffset() < varScope.getOffset()) {
                        if (varScope instanceof VariableScope) {
                            FileObject fileObject = varScope.getFileObject();
                            if (fileObject == scope.getFileObject()) {
                                atOffset = (VariableScope) varScope;
                            }
                        }
                    }
                }
            }

        }
        if (atOffset == null) {
            while (scope != null && !(scope instanceof VariableScope)) {
                scope = scope.getInScope();
            }
            atOffset = (VariableScope) scope;
        }
        return atOffset;
    }

    private VariableNameImpl findVariable(ScopeImpl scope, final VariableBase leftHandSide) {
        Map<String, VariableNameImpl> varnames = vars.get(scope);
        VariableNameImpl varN = null;
        while (scope != null && varnames != null) {
            varN = varnames.get(VariableNameImpl.toName((Variable) leftHandSide));
            if (varN != null) {
                break;
            }
            scope = scope.getInScope();
            varnames = vars.get(scope);
        }
        return varN;
    }

    private OffsetRange getBlockRange(Scope currentScope) {
        ASTNode conditionalNode = findConditionalStatement(getPath());
        OffsetRange scopeRange = (conditionalNode != null) ? new OffsetRange(conditionalNode.getStartOffset(), conditionalNode.getEndOffset()) : currentScope.getBlockRange();
        return scopeRange;
    }

    private void handleVarComments() {
        Set<String> varCommentNames = varTypeComments.keySet();
        for (String name : varCommentNames) {
            List<PhpDocTypeTagInfo> varComments = varTypeComments.get(name); //varComments.size() varTypeComments.size()
            if (varComments != null) {
                for (PhpDocTypeTagInfo phpDocTypeTagInfo : varComments) {
                    VariableScope varScope = getVariableScope(phpDocTypeTagInfo.getRange().getStart());
                    VariableNameImpl varInstance = (VariableNameImpl) ModelUtils.getFirst(varScope.getVariables(name));
                    if (varInstance == null) {
                        if (varScope instanceof ScopeImpl) {
                            ScopeImpl scp = (ScopeImpl) varScope;
                            varInstance = new VariableNameImpl(scp, name, scp.getFile(), phpDocTypeTagInfo.getRange(), scp instanceof FileScope);
                            scp.addElement(varInstance);
                        }
                    }
                    if (varInstance != null) {
                        VarAssignmentImpl vAssignment = new VarAssignmentImpl(varInstance, (ScopeImpl) varScope, getBlockRange(varScope), phpDocTypeTagInfo.getRange(), phpDocTypeTagInfo.getTypeName());
                        varInstance.addElement(vAssignment);
                    }
                    //scan(phpDocTypeTagInfo.getTypeTag());
                    occurencesBuilder.prepare(phpDocTypeTagInfo.getTypeTag(), varScope);

                }
            }
        }
    }

    private void prepareVarComments(Program program) {
        List<Comment> comments = program.getComments();
        for (Comment comment : comments) {
            Comment.Type type = comment.getCommentType();
            if (type.equals(Comment.Type.TYPE_VARTYPE)) {
                checkComments(comment);
            }
        }
    }

    /*private String getVarTypeName(String name, ScopeImpl scopeImpl) {
        String typeName = var2TypeName.get(name);
        if (typeName == null) {
            PhpDocTypeTagInfo typeTag = var2DefaultType.get(name);
            if (typeTag != null) {
                OffsetRange scopeRange = scopeImpl.getBlockRange();
                if (scopeRange != null && scopeRange.overlaps(typeTag.getRange())) {
                    typeName = typeTag.getTypeName();
                    var2TypeName.put(name, typeName);
                    scan(typeTag.getTypeTag());
                } 
            }
        }
        return typeName;
    }*/
}
