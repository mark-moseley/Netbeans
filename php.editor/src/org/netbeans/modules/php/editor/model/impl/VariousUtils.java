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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class VariousUtils {

    public static final String CONSTRUCTOR_TYPE_PREFIX = "constuct:";
    public static final String FUNCTION_TYPE_PREFIX = "fn:";
    public static final String METHOD_TYPE_PREFIX = "mtd:";
    public static final String STATIC_METHOD_TYPE_PREFIX = "static.mtd:";
    public static final String FIELD_TYPE_PREFIX = "fld:";
    public static final String STATIC_FIELD__TYPE_PREFIX = "static.fld:";
    public static final String VAR_TYPE_PREFIX = "var:";

    static String extractTypeFroVariableBase(VariableBase varBase, Map<String, VariableNameImpl> allAssignments) {
        Stack<VariableBase> stack = new Stack<VariableBase>();
        String typeName = null;
        createVariableBaseChain(varBase, stack);
        while (!stack.isEmpty() && stack.peek() != null) {
            varBase = stack.pop();
            String tmpType = extractVariableTypeFromVariableBase(varBase, allAssignments);
            if (tmpType == null) {
                typeName = tmpType;
                break;
            }
            if (typeName == null) {
                typeName = tmpType;
            } else {
                typeName += tmpType;
            }
        }
        return typeName; //extractVariableTypeFromVariableBase(varBase);
    }

    private VariousUtils() {
    }

    public static String getReturnTypeFromPHPDoc(Program root, FunctionDeclaration functionDeclaration) {
        return getTypeFromPHPDoc(root, functionDeclaration, PHPDocTag.Type.RETURN);
    }

    public static String getFieldTypeFromPHPDoc(Program root, SingleFieldDeclaration field) {
        return getTypeFromPHPDoc(root, field, PHPDocTag.Type.VAR);
    }

    public static String getTypeFromPHPDoc(Program root, ASTNode node, PHPDocTag.Type tagType) {
        Comment comment = Utils.getCommentForNode(root, node);

        if (comment instanceof PHPDocBlock) {
            PHPDocBlock phpDoc = (PHPDocBlock) comment;

            for (PHPDocTag tag : phpDoc.getTags()) {
                if (tag.getKind() == tagType) {
                    String parts[] = tag.getValue().split("\\s+", 2); //NOI18N

                    if (parts.length > 0) {
                        String type = parts[0].split("\\;", 2)[0];
                        return type;
                    }

                    break;
                }
            }
        }
        return null;
    }


    @CheckForNull
    static String extractVariableTypeFromAssignment(Assignment assignment, Map<String, VariableNameImpl> allAssignments) {
        Expression expression = assignment.getRightHandSide();
        if (expression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment) expression, allAssignments);
        } else if (expression instanceof Reference) {
            Reference ref = (Reference) expression;
            expression = ref.getExpression();
        }
        if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
            Expression className = classInstanceCreation.getClassName().getName();

            if (className instanceof Identifier) {
                Identifier identifier = (Identifier) className;
                return identifier.getName();
            }
        } else if (expression instanceof ArrayCreation) {
            return "array"; //NOI18N
        } else if (expression instanceof VariableBase) {
            return extractTypeFroVariableBase((VariableBase) expression, allAssignments);//extractVariableTypeFromVariableBase(varBase);
        }

        return null;
    }

    //TODO: needs to be improved to properly return more types
    public static List<? extends TypeScope> getType(ModelScope topScope, VariableScope varScope, String semiTypeName, int offset, boolean justDispatcher) throws IllegalStateException {
        Stack<String> stack = new Stack<String>();
        TypeScope type = null;
        if (semiTypeName != null && semiTypeName.contains("@")) {
            String operation = null;
            String[] fragments = semiTypeName.split("[@:]");
            int len = (justDispatcher) ? fragments.length - 1 : fragments.length;
            for (int i = 0; i < len; i++) {
                String frag = fragments[i];
                if (frag.trim().length() == 0) {
                    continue;
                }
                if (VariousUtils.METHOD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;
                        stack.push(frag);
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        String clsName = stack.isEmpty() ? null : stack.pop();
                        if (clsName == null) {
                            semiTypeName = null;
                            break;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachedModelSupport.getClasses(clsName,topScope));
                        if (cls == null) {
                            semiTypeName = null;
                            break;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachedModelSupport.getMethods(cls, frag, topScope, PHPIndex.ANY_ATTR));
                        if (meth == null) {
                            semiTypeName = null;
                            break;
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        FunctionScope fnc = ModelUtils.getFirst(CachedModelSupport.getFunctions(frag, topScope));
                        if (fnc == null) {
                            semiTypeName = null;
                            break;
                        }
                        type = ModelUtils.getFirst(fnc.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName == null) {
                            semiTypeName = null;
                            break;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachedModelSupport.getClasses(clsName, topScope));
                        if (cls == null) {
                            semiTypeName = null;
                            break;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachedModelSupport.getMethods(cls, frgs[1],topScope, PHPIndex.ANY_ATTR));
                                //ModelUtils.getFirst(cls.getMethods(frgs[1], PhpModifiers.STATIC));
                        if (meth == null) {
                            semiTypeName = null;
                            break;
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        type = null;
                        if (varScope instanceof MethodScope) {//NOI18N
                            MethodScope mScope = (MethodScope) varScope;
                            if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                type = (ClassScope) mScope.getInScope();
                            } 
                            if (type != null) {
                                stack.push(type.getName());
                                operation = null;
                            }
                        }
                        if (type == null) {
                            List<? extends VariableName> variables = varScope.getVariables(frag);
                            if (!variables.isEmpty()) {
                                VariableName varName = ModelUtils.getFirst(variables);
                                type = varName != null ? ModelUtils.getFirst(varName.getTypes(offset)) : null;
                                if (type != null) {
                                    stack.push(type.getName());
                                } else {
                                    semiTypeName = null;
                                    break;
                                }
                                operation = null;
                            }
                        }
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        //TODO: not implemented yet
                        return Collections.<TypeScope>emptyList();
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
            if (stack.size() == 1) {
                semiTypeName = stack.pop();
            }
        //throw new UnsupportedOperationException("Not supported yet.");
        }
        if (semiTypeName != null) {
            if (type != null && semiTypeName.equals(type.getName())) {
                return Collections.<TypeScope>singletonList(type);
            } else {
                type = ModelUtils.getFirst(CachedModelSupport.getTypes(semiTypeName, topScope));
                if (type != null) {
                    return Collections.<TypeScope>singletonList(type);
                }
                //assert false;
            }
        }
        return Collections.<TypeScope>emptyList();
    }

    public static Stack<? extends ModelElement> getElemenst(ModelScope topScope, VariableScope varScope, String semiTypeName, int offset) throws IllegalStateException {
        Stack<ModelElement> emptyStack = new Stack<ModelElement>();
        Stack<ModelElement> retval = new Stack<ModelElement>();
        Stack<String> stack = new Stack<String>();
        TypeScope type = null;
        if (semiTypeName != null && semiTypeName.contains("@")) {
            String operation = null;
            String[] fragments = semiTypeName.split("[@:]");
            int len = fragments.length;
            for (int i = 0; i < len; i++) {
                String frag = fragments[i];
                if (frag.trim().length() == 0) {
                    continue;
                }
                if (VariousUtils.METHOD_TYPE_PREFIX.startsWith(frag)) {
                    operation = VariousUtils.METHOD_TYPE_PREFIX;
                } else if (VariousUtils.FUNCTION_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FUNCTION_TYPE_PREFIX;
                } else if (VariousUtils.STATIC_METHOD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.STATIC_METHOD_TYPE_PREFIX;
                } else if (VariousUtils.VAR_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.VAR_TYPE_PREFIX;
                } else if (VariousUtils.FIELD_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.FIELD_TYPE_PREFIX;
                } else if (VariousUtils.CONSTRUCTOR_TYPE_PREFIX.startsWith(frag)) {
                    assert operation == null;
                    operation = VariousUtils.CONSTRUCTOR_TYPE_PREFIX;
                } else {
                    if (operation == null) {
                        assert i == 0;
                        stack.push(frag);
                    } else if (operation.startsWith(VariousUtils.METHOD_TYPE_PREFIX)) {
                        String clsName = stack.isEmpty() ? null : stack.pop();
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachedModelSupport.getClasses(clsName,topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachedModelSupport.getMethods(cls, frag, topScope, PHPIndex.ANY_ATTR));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.FUNCTION_TYPE_PREFIX)) {
                        FunctionScope fnc = ModelUtils.getFirst(CachedModelSupport.getFunctions(frag, topScope));
                        if (fnc == null) {
                            semiTypeName = null;
                            break;
                        } else {
                            retval.push(fnc);
                        }
                        type = ModelUtils.getFirst(fnc.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.CONSTRUCTOR_TYPE_PREFIX)) {
                        ClassScope cls = ModelUtils.getFirst(CachedModelSupport.getClasses(frag, topScope));
                        if (cls == null) {
                            semiTypeName = null;
                            break;
                        } else {
                            MethodScope meth = ModelUtils.getFirst(CachedModelSupport.getMethods(cls, "__construct",topScope, PHPIndex.ANY_ATTR));//NOI18N
                            if (meth != null) {
                                retval.push(meth);
                            } else {
                                return emptyStack;
                            }
                        }
                        stack.push(cls.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.STATIC_METHOD_TYPE_PREFIX)) {
                        String[] frgs = frag.split("\\.");
                        assert frgs.length == 2;
                        String clsName = frgs[0];
                        if (clsName == null) {
                            return emptyStack;
                        }
                        ClassScope cls = ModelUtils.getFirst(CachedModelSupport.getClasses(clsName, topScope));
                        if (cls == null) {
                            return emptyStack;
                        }
                        MethodScope meth = ModelUtils.getFirst(CachedModelSupport.getMethods(cls, frgs[1],topScope, PHPIndex.ANY_ATTR));
                                //ModelUtils.getFirst(cls.getMethods(frgs[1], PhpModifiers.STATIC));
                        if (meth == null) {
                            return emptyStack;
                        } else {
                            retval.push(meth);
                        }
                        type = ModelUtils.getFirst(meth.getReturnTypes());
                        if (type == null) {
                            semiTypeName = null;
                            break;
                        }
                        stack.push(type.getName());
                        operation = null;
                    } else if (operation.startsWith(VariousUtils.VAR_TYPE_PREFIX)) {
                        type = null;
                        if (varScope instanceof MethodScope) {//NOI18N
                            MethodScope mScope = (MethodScope) varScope;
                            if ((frag.equals("this") || frag.equals("$this"))) {//NOI18N
                                type = (ClassScope) mScope.getInScope();
                            }
                            if (type != null) {
                                stack.push(type.getName());
                                operation = null;
                            }
                        }
                        if (type == null) {
                            List<? extends VariableName> variables = varScope.getVariables(frag);
                            if (!variables.isEmpty()) {
                                VariableName varName = ModelUtils.getFirst(variables);
                                type = varName != null ? ModelUtils.getFirst(varName.getTypes(offset)) : null;
                                if (varName != null) {
                                    retval.push(varName);
                                }
                                if (type != null) {
                                    stack.push(type.getName());
                                } else {
                                    semiTypeName = null;
                                    break;
                                }
                                operation = null;
                            }
                        }
                    } else if (operation.startsWith(VariousUtils.FIELD_TYPE_PREFIX)) {
                        //TODO: not implemented yet
                        return emptyStack;
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                }
            }
            if (stack.size() == 1) {
                semiTypeName = stack.pop();
            }
        //throw new UnsupportedOperationException("Not supported yet.");
        }
        return retval;
    }

    private static void createVariableBaseChain(VariableBase node, Stack<VariableBase> stack) {
        stack.push(node);
        if (node instanceof MethodInvocation) {
            createVariableBaseChain(((MethodInvocation) node).getDispatcher(), stack);
        } else if (node instanceof FieldAccess) {
            createVariableBaseChain(((FieldAccess) node).getDispatcher(), stack);
        }
    }

    private static String extractVariableTypeFromVariableBase(VariableBase varBase, Map<String, VariableNameImpl> allAssignments) {
        if (varBase instanceof Variable) {
            String varName = CodeUtils.extractVariableName((Variable) varBase);
            VariableNameImpl nameImpl = allAssignments.get(varName);
            if (nameImpl != null) {
                VarAssignmentImpl last = ModelUtils.getLast(nameImpl.getAssignments());
                if (last != null) {
                    String semiTypeName = last.typeNameFromUnion();
                    if (semiTypeName != null) {
                        return semiTypeName;
                    }
                }
            }
            return "@" + VAR_TYPE_PREFIX + varName;
        } else if (varBase instanceof FunctionInvocation) {
            FunctionInvocation functionInvocation = (FunctionInvocation) varBase;
            String fname = CodeUtils.extractFunctionName(functionInvocation);
            return "@" + FUNCTION_TYPE_PREFIX + fname;
        } else if (varBase instanceof StaticMethodInvocation) {
            StaticMethodInvocation staticMethodInvocation = (StaticMethodInvocation) varBase;
            String className = staticMethodInvocation.getClassName().getName();
            String methodName = CodeUtils.extractFunctionName(staticMethodInvocation.getMethod());

            if (className != null && methodName != null) {
                return "@" + STATIC_METHOD_TYPE_PREFIX + className + '.' + methodName;
            }
        } else if (varBase instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation) varBase;
            String methodName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
            if (methodName != null) {
                return "@" + METHOD_TYPE_PREFIX + methodName;
            }
        } else if (varBase instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) varBase;
            String filedName = CodeUtils.extractVariableName(fieldAccess.getField());
            if (filedName != null) {
                return "@" + FIELD_TYPE_PREFIX + filedName;
            }
        }

        return null;
    }
    public static String resolveFileName(Include include) {
        Expression e = include.getExpression();

        if (e instanceof ParenthesisExpression) {
            e = ((ParenthesisExpression) e).getExpression();
        }

        if (e instanceof Scalar) {
            Scalar s = (Scalar) e;

            if (Type.STRING == s.getScalarType()) {
                String fileName = s.getStringValue();
                fileName = fileName.length() >= 2 ? fileName.substring(1, fileName.length() - 1) : fileName;//TODO: not nice
                return fileName;
            }
        }

        return null;
    }

    public static FileObject resolveInclude(FileObject currentFile, Include include) {
        String fileName = resolveFileName(include);
        return (fileName != null) ? resolveRelativeFile(currentFile, fileName) : null;
    }

    private static FileObject resolveRelativeFile(FileObject currentFile, String name) {
        while (true) {
            FileObject result;

//            if (psp != null) {
                result = PhpSourcePath.resolveFile(currentFile.getParent(), name);
//            } else {
//                result = info.getFileObject().getParent().getFileObject(name);
//            }

            if (result != null) {
                return result;
            }

            //try to strip a directory from the "name":
            int slash = name.indexOf('/');

            if (slash != (-1)) {
                name = name.substring(slash + 1);
            } else {
                return null;
            }
        }
    }


}
