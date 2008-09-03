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

package org.netbeans.modules.php.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
public final class VarTypeResolver {
    private final String varName;
    private final List<ASTNode> pathUnderCaret;
    private final ASTNode blockOfCaret;
    private int anchor;
    private PHPIndex index;
    private CompilationInfo info;
    private PHPParseResult result;
    private VarTypeResolver(final PHPCompletionItem.CompletionRequest request,
            final String varName) {
        this(varName,request.anchor,request.index, request.info, request.result);
    }

    private VarTypeResolver(final String varName,int anchor,PHPIndex index,CompilationInfo info,PHPParseResult result) {
        this.result = result;
        this.anchor = anchor;
        this.index = index;
        this.info = info;
        this.varName = varName;
        pathUnderCaret = NavUtils.underCaret(info, anchor);
        blockOfCaret = findNearestBlock(pathUnderCaret);
    }

    private VarTypeResolver(final CompilationInfo info, final int offset, final String varName)  {
        this(varName,offset,PHPIndex.get(info.getIndex(PHPLanguage.PHP_MIME_TYPE)), info,
                (PHPParseResult)info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, offset));
    }
    public static VarTypeResolver getInstance(final PHPCompletionItem.CompletionRequest request,
            final String varName)  {

        return new VarTypeResolver(request, varName);
    }

    public static VarTypeResolver getInstance(final CompilationInfo info, final int offset, final String varName)  {
        return new VarTypeResolver(info, offset, varName);
    }

    public String resolveType() {
        final Map<String, Union2<Variable, String>> assignments = new HashMap<String, Union2<Variable, String>>();
        final List<ASTNode> path = new LinkedList<ASTNode>();
        new DefaultVisitor() {
            @Override
            public void scan(ASTNode node) {
                path.add(node);
                super.scan(node);
                path.remove(node);
            }

            @Override
            public void visit(ClassDeclaration node) {
                assignments.put("$this", Union2.<Variable, String>createSecond(CodeUtils.extractClassName(node)));//NOI18N
                assignments.put("self", Union2.<Variable, String>createSecond(CodeUtils.extractClassName(node)));//NOI18N
                assignments.put("parent", Union2.<Variable, String>createSecond(CodeUtils.extractSuperClassName(node)));//NOI18N                
                super.visit(node);
            }


            @Override
            public void visit(FunctionDeclaration node) {
                int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    if (isValidBlock(path)) {
                        List<FormalParameter> formalParameters = node.getFormalParameters();
                        for (FormalParameter param : formalParameters) {
                            Identifier parameterType = param.getParameterType();
                            if (parameterType == null) continue;
                            String typeName = parameterType.getName();
                            String paramName = null;
                            Expression parameterName = param.getParameterName();
                            if (parameterName instanceof Variable) {
                                paramName = CodeUtils.extractVariableName((Variable) parameterName);
                            }
                            if (paramName != null && typeName != null && typeName.length() > 0) {
                                assignments.put(paramName, Union2.<Variable, String>createSecond(typeName));
                            }
                        }
                    }
                }

                super.visit(node);
            }


            public void visit(Assignment node) {
                int offset = anchor;
                if ((offset != (-1) && offset >= node.getStartOffset())) {
                    VariableBase leftHandSide = node.getLeftHandSide();
                    Expression rightHandSide = node.getRightHandSide();
                    if (leftHandSide instanceof Variable) {
                        String leftVarName = CodeUtils.extractVariableName((Variable) leftHandSide);
                        if (isValidBlock(path)) {
                            if (rightHandSide instanceof Variable) {
                                String rightVarName = CodeUtils.extractVariableName((Variable) rightHandSide);
                                Union2<Variable, String> rAssignment = assignments.get(rightVarName);
                                if (rAssignment != null) {
                                    assignments.put(leftVarName, rAssignment);
                                } else {
                                    assignments.put(leftVarName, Union2.<Variable, String>createFirst((Variable) rightHandSide));
                                }
                            } else if (rightHandSide instanceof ClassInstanceCreation) {
                                assignments.put(leftVarName, Union2.<Variable, String>createSecond(CodeUtils.extractClassName((ClassInstanceCreation) rightHandSide)));
                            } else {
                                String typeName = null;
                                if (rightHandSide instanceof VariableBase) {
                                    Stack<VariableBase> stack = new Stack<VariableBase>();
                                    createVariableBaseChain((VariableBase) rightHandSide, stack);
                                    while (!stack.isEmpty() && stack.peek() != null) {
                                        VariableBase varBase = stack.pop();
                                        if (typeName == null) {
                                            if (varBase instanceof FunctionInvocation) {
                                                typeName = getReturnType((FunctionInvocation) varBase, result,index);
                                            } else if (varBase instanceof Variable) {
                                                typeName = findPrecedingType((Variable) varBase, assignments);
                                            } else if (varBase instanceof StaticFieldAccess) {
                                                typeName = getReturnType((StaticFieldAccess)varBase, result,index);
                                            } else if (varBase instanceof StaticMethodInvocation) {
                                                typeName = getReturnType((StaticMethodInvocation)varBase, result,index);
                                            }
                                            if (typeName == null) {
                                                break;
                                            }
                                        } else {
                                            if (varBase instanceof MethodInvocation) {
                                                typeName = getReturnType(typeName, (MethodInvocation) varBase, result,index);
                                            } else {
                                                typeName = null;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (typeName == null) {
                                    assignments.put(leftVarName, null);
                                } else {
                                    assignments.put(leftVarName, Union2.<Variable, String>createSecond(typeName));
                                }
                            }
                        } else {
                            assignments.put(leftVarName, null);
                        }
                    }
                    super.visit(node);
                }
            }
        }.scan(Utils.getRoot(info));
        return findPrecedingType(varName, assignments);
    }
    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation)node).getDispatcher(), stack);
        }
    }
    private static String getReturnType(FunctionInvocation node,PHPParseResult result,PHPIndex index) {
        Collection<IndexedFunction> functions = index.getFunctions(result, CodeUtils.extractFunctionName(node), NameKind.EXACT_NAME);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticMethodInvocation node,PHPParseResult result,PHPIndex index) {
        StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) node;
        String clsName = staticMethodInvocation.getClassName().getName();
        FunctionInvocation method = staticMethodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(result, clsName, fncName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String getReturnType(StaticFieldAccess node,PHPParseResult result,PHPIndex index) {
        StaticFieldAccess staticFieldAccess = (StaticFieldAccess) node;
        String clsName = staticFieldAccess.getClassName().getName();
        Variable var = staticFieldAccess.getField();
        String varName = CodeUtils.extractVariableName(var);
        varName = (varName.startsWith("$")) //NOI18N
                ? varName.substring(1) : varName;

        Collection<IndexedConstant> constants =
                index.getAllProperties(result, clsName, varName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);

        if (!constants.isEmpty()) {
            IndexedConstant con = constants.iterator().next();
            return con.getTypeName();
        }
        return null;
    }
    private static String getReturnType(String className, VariableBase v,PHPParseResult result, PHPIndex index) {
        return null;
    }

    private static String getReturnType(String className, MethodInvocation methodInvocation,PHPParseResult result,PHPIndex index) {
        FunctionInvocation method = methodInvocation.getMethod();
        String fncName = CodeUtils.extractFunctionName(method);
        Collection<IndexedFunction> functions =
                index.getAllMethods(result, className, fncName, NameKind.EXACT_NAME, PHPIndex.ANY_ATTR);
        if (!functions.isEmpty()) {
            IndexedFunction fnc = functions.iterator().next();
            return fnc.getReturnType();
        }
        return null;
    }
    private static String findPrecedingType(Variable node, final Map<String, Union2<Variable, String>> assignments) {
        String varName = CodeUtils.extractVariableName(node);
        return findPrecedingType(varName, assignments);
    }

    private static String findPrecedingType(String varName, final Map<String, Union2<Variable, String>> assignments) {
        String retval = null;
        Union2<Variable, String> rAssignment = assignments.get(varName);
        if (rAssignment != null && rAssignment.hasSecond()) {
            retval = rAssignment.second();
        }
        return retval;
    }

    private ASTNode findNearestBlock(final List<ASTNode> path) {
        ASTNode retval = null;
        int size = path.size();
        for (int i = size - 1; i >= 0; i--) {
            ASTNode node = path.get(i);
            if (node instanceof Program || node instanceof Block || node instanceof FunctionDeclaration) {
                retval = node;
                break;
            }
        }
        return retval;
    }

    private boolean isValidBlock(final List<ASTNode> path) {
        ASTNode nearestBlock = findNearestBlock(path);
        if (nearestBlock == null) {
            return false;
        } else if (blockOfCaret == nearestBlock) {
            return true;
        }
        int size = pathUnderCaret.size();
        for (int i = size - 1; i >= 0; i--) {
            ASTNode node = pathUnderCaret.get(i);
            if (node instanceof FunctionDeclaration) {
                if (nearestBlock instanceof FunctionDeclaration) {
                    String fncName = CodeUtils.extractFunctionName((FunctionDeclaration) node);
                    String fncName2 = CodeUtils.extractFunctionName((FunctionDeclaration) nearestBlock);
                    return (fncName.equals(fncName2));
                } else {
                    return false;
                }
            }
            if (node == nearestBlock) {
                return true;
            }
        }
        return false;

    }


}
