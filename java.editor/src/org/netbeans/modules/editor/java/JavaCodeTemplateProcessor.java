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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateProcessor implements CodeTemplateProcessor {
    
    public static final String INSTANCE_OF = "instanceof"; //NOI18N
    public static final String ARRAY = "array"; //NOI18N
    public static final String ITERABLE = "iterable"; //NOI18N
    public static final String TYPE = "type"; //NOI18N
    public static final String ITERABLE_ELEMENT_TYPE = "iterableElementType"; //NOI18N
    public static final String LEFT_SIDE_TYPE = "leftSideType"; //NOI18N
    public static final String RIGHT_SIDE_TYPE = "rightSideType"; //NOI18N
    public static final String CAST = "cast"; //NOI18N
    public static final String NEW_VAR_NAME = "newVarName"; //NOI18N
    public static final String NAMED = "named"; //NOI18N
    public static final String UNCAUGHT_EXCEPTION_TYPE = "uncaughtExceptionType"; //NOI18N

    private static final String FALSE = "false"; //NOI18N
    private static final String NULL = "null"; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N
    
    private CodeTemplateInsertRequest request;

    private CompilationInfo cInfo = null;
    private Future<Void> initTask = null;
    private TreePath treePath = null;
    private Scope scope = null;
    private TypeElement enclClass = null;
    private Iterable<? extends Element> locals = null;
    private Map<CodeTemplateParameter, String> param2hints = new HashMap<CodeTemplateParameter, String>();
    private Map<CodeTemplateParameter, TypeMirror> param2types = new HashMap<CodeTemplateParameter, TypeMirror>();
    private ErrChecker errChecker = new ErrChecker();
    
    private JavaCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
    }
    
    public synchronized void updateDefaultValues() {
        boolean cont = true;
        while (cont) {
            cont = false;
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                String value = getProposedValue(param); 
                if (value != null && !value.equals(param.getValue())) {
                    param.setValue(value);
                    cont = true;
                }
            }
        }
        updateImports();
    }
    
    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        if (typingChange) {
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                if (!param.isUserModified()) {
                    String value = getProposedValue(param);
                    if (value != null && !value.equals(param.getValue()))
                        param.setValue(value);
                } else {
                    param2types.remove(param);
                }
            }
            updateImports();                    
        }
    }
    
    public void release() {
    }
    
    private void updateImports() {
        if (!param2types.isEmpty()) {
            AutoImport imp = AutoImport.get(cInfo);
            for (Map.Entry<CodeTemplateParameter, TypeMirror> entry : param2types.entrySet()) {
                CodeTemplateParameter param = entry.getKey();
                TypeMirror tm = param2types.get(param);
                TreePath tp = cInfo.getTreeUtilities().pathFor(request.getInsertTextOffset() + param.getInsertTextOffset());
                CharSequence typeName = imp.resolveImport(tp, tm);
                if (CAST.equals(param2hints.get(param))) {
                    param.setValue("(" + typeName + ")"); //NOI18N
                } else if (INSTANCE_OF.equals(param2hints.get(param))) {
                    String value = param.getValue().substring(param.getValue().lastIndexOf('.') + 1); //NOI18N
                    param.setValue(typeName + "." + value); //NOI18N
                } else {
                    param.setValue(typeName.toString());
                }
            }
        }
    }
    
    private String getProposedValue(CodeTemplateParameter param) {
        param2hints.remove(param);
        param2types.remove(param);
        String name = null;
        for (Object e : param.getHints().entrySet()) {
            Map.Entry entry = (Map.Entry)e;
            if (INSTANCE_OF.equals(entry.getKey())) {
                VariableElement ve = instanceOf((String)entry.getValue(), name);
                if (ve != null) {
                    param2hints.put(param, INSTANCE_OF);
                    return ve.getSimpleName().toString();
                } else if (name == null) {
                    ve = staticInstanceOf((String)entry.getValue(), name);
                    if (ve != null) {
                        param2hints.put(param, INSTANCE_OF);
                        TypeMirror tm = ve.getEnclosingElement().asType();
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return Utilities.getTypeName(tm, true) + "." + ve.getSimpleName();
                    } else {
                        return valueOf((String)entry.getValue());
                    }                    
                }
            } else if (ARRAY.equals(entry.getKey())) {
                VariableElement ve = array();
                if (ve != null) {
                    param2hints.put(param, ARRAY);
                    return ve.getSimpleName().toString();
                }
            } else if (ITERABLE.equals(entry.getKey())) {
                VariableElement ve = iterable();
                if (ve != null) {
                    param2hints.put(param, ITERABLE);
                    return ve.getSimpleName().toString();
                }
            } else if (TYPE.equals(entry.getKey())) {
                TypeMirror tm = type((String)entry.getValue());
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable)tm).getUpperBound();
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, TYPE);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return value;
                    }
                }
            } else if (ITERABLE_ELEMENT_TYPE.equals(entry.getKey())) {
                TypeMirror tm = iterableElementType(param.getInsertTextOffset() + 1);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable)tm).getUpperBound();
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, ITERABLE_ELEMENT_TYPE);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return value;
                    }
                }
            } else if (LEFT_SIDE_TYPE.equals(entry.getKey())) {
                TypeMirror tm = assignmentSideType(param.getInsertTextOffset() + 1, true);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable)tm).getUpperBound();
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, LEFT_SIDE_TYPE);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return value;
                    }
                }
            } else if (RIGHT_SIDE_TYPE.equals(entry.getKey())) {
                TypeMirror tm = assignmentSideType(param.getInsertTextOffset() + 1, false);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable)tm).getUpperBound();
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, RIGHT_SIDE_TYPE);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return value;
                    }
                }
            } else if (CAST.equals(entry.getKey())) {
                TypeMirror tm = cast(param.getInsertTextOffset() + 1);
                if (tm == null) {
                    param2hints.put(param, CAST);
                    param2types.remove(param);
                    return ""; //NOI18N
                } else if (tm.getKind() != TypeKind.ERROR) {
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, CAST);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm); //NOI18N
                        return "(" + value + ")"; //NOI18N
                    }
                }
            } else if (NEW_VAR_NAME.equals(entry.getKey())) {
                param2hints.put(param, NEW_VAR_NAME);
                return newVarName(param.getInsertTextOffset() + 1);
            } else if (NAMED.equals(entry.getKey())) {
                name = param.getName();
            } else if (UNCAUGHT_EXCEPTION_TYPE.equals(entry.getKey())) {
                TypeMirror tm = uncaughtExceptionType(param.getInsertTextOffset() + 1);
                if (tm != null && tm.getKind() != TypeKind.ERROR) {
                    if (tm.getKind() == TypeKind.TYPEVAR)
                        tm = ((TypeVariable)tm).getUpperBound();
                    String value = Utilities.getTypeName(tm, true).toString();
                    if (value != null) {
                        param2hints.put(param, UNCAUGHT_EXCEPTION_TYPE);
                        if (containsDeclaredType(tm))
                            param2types.put(param, tm);
                        return value;
                    }
                }
            }
        }
        return null;
    }
    
    private VariableElement instanceOf(String typeName, String name) {
        try {
            if (initParsing()) {
                TypeMirror type = cInfo.getTreeUtilities().parseType(typeName, enclClass);
                VariableElement closest = null;
                int distance = Integer.MAX_VALUE;
                if (type != null) {
                    Types types = cInfo.getTypes();
                    for (Element e : locals) {
                        if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName()) && types.isAssignable(e.asType(), type)) {
                            if (name == null)
                                return (VariableElement)e;
                            int d = ElementHeaders.getDistance(e.getSimpleName().toString(), name);
                            if (types.isSameType(e.asType(), type))
                                d -= 1000;
                            if (d < distance) {
                                distance = d;
                                closest = (VariableElement)e;
                            }
                        }
                    }
                }
                return closest;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private VariableElement staticInstanceOf(String typeName, String name) {
        try {
            if (initParsing()) {
                final TreeUtilities tu = cInfo.getTreeUtilities();
                TypeMirror type = tu.parseType(typeName, enclClass);
                VariableElement closest = null;
                int distance = Integer.MAX_VALUE;
                if (type != null) {
                    final Types types = cInfo.getTypes();
                    if (type.getKind() == TypeKind.DECLARED) {
                        final DeclaredType dType = (DeclaredType)type;
                        final TypeElement element = (TypeElement)dType.asElement();
                        final boolean isStatic = element.getKind().isClass() || element.getKind().isInterface();
                        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                            public boolean accept(Element e, TypeMirror t) {
                                return e.getKind().isField() && !ERROR.contentEquals(e.getSimpleName()) &&
                                        (!isStatic || e.getModifiers().contains(Modifier.STATIC)) &&
                                        tu.isAccessible(scope, e, t) &&
                                        (e.getKind().isField() && types.isAssignable(((VariableElement)e).asType(), dType) || e.getKind() == ElementKind.METHOD && types.isAssignable(((ExecutableElement)e).getReturnType(), dType));
                            }
                        };
                        for (Element ee : cInfo.getElementUtilities().getMembers(dType, acceptor)) {
                            if (name == null)
                                return (VariableElement)ee;
                            int d = ElementHeaders.getDistance(ee.getSimpleName().toString(), name);
                            if (ee.getKind().isField() && types.isSameType(((VariableElement)ee).asType(), dType) || ee.getKind() == ElementKind.METHOD && types.isSameType(((ExecutableElement)ee).getReturnType(), dType))
                                d -= 1000;
                            if (d < distance) {
                                distance = d;
                                closest = (VariableElement)ee;
                            }
                        }
                    }
                }
                return closest;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private String valueOf(String typeName) {
        try {
            if (initParsing()) {
                TypeMirror type = cInfo.getTreeUtilities().parseType(typeName, enclClass);
                if (type != null) {
                    if (type.getKind() == TypeKind.DECLARED)
                        return NULL;
                    else if (type.getKind() == TypeKind.BOOLEAN)
                        return FALSE;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private VariableElement array() {
        if (initParsing()) {
            for (Element e : locals) {
                if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName()) && e.asType().getKind() == TypeKind.ARRAY)
                    return (VariableElement)e;
            }
        }
        return null;
    }

    private VariableElement iterable() {
        if (initParsing()) {
            TypeElement iterableTE = cInfo.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
            if (iterableTE != null) {
                TypeMirror iterableType = cInfo.getTypes().getDeclaredType(iterableTE);
                for (Element e : locals) {
                    if (e instanceof VariableElement && !ERROR.contentEquals(e.getSimpleName()) && (e.asType().getKind() == TypeKind.ARRAY || cInfo.getTypes().isAssignable(e.asType(), iterableType)))
                        return (VariableElement)e;
                }
            }
        }
        return null;
    }

    private TypeMirror type(String typeName) {
        return initParsing() ? cInfo.getTreeUtilities().parseType(typeName, enclClass) : null;
    }
    
    private TypeMirror iterableElementType(int caretOffset) {
        try {
            if (initParsing()) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement(request.getInsertText(), sourcePositions);
                if (errChecker.containsErrors(stmt))
                    return null;
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset, sourcePositions[0]);
                TreePath loop = Utilities.getPathElementOfKind(Tree.Kind.ENHANCED_FOR_LOOP, path);
                if (loop != null) {
                    tu.attributeTree(stmt, scope);
                    TypeMirror type = cInfo.getTrees().getTypeMirror(new TreePath(loop, ((EnhancedForLoopTree)loop.getLeaf()).getExpression()));
                    switch (type.getKind()) {
                        case ARRAY:
                            type = ((ArrayType)type).getComponentType();
                            return type;
                        case DECLARED:
                            Iterator<? extends TypeMirror> types = ((DeclaredType)type).getTypeArguments().iterator();
                            if (types.hasNext())
                                return types.next();
                            return cInfo.getElements().getTypeElement("java.lang.Object").asType(); //NOI18N
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private TypeMirror assignmentSideType(int caretOffset, boolean left) {
        try {
            if (initParsing()) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement(request.getInsertText(), sourcePositions);
                if (errChecker.containsErrors(stmt))
                    return null;
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset, sourcePositions[0]);
                TreePath tree = Utilities.getPathElementOfKind(EnumSet.of(Tree.Kind.ASSIGNMENT, Tree.Kind.VARIABLE), path);
                if (tree == null)
                    return null;
                tu.attributeTree(stmt, scope);
                if (tree.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree as = (AssignmentTree)tree.getLeaf();
                    TreePath type = new TreePath(tree, left ? as.getVariable() : as.getExpression());
                    return cInfo.getTrees().getTypeMirror(type);
                }
                VariableTree vd = (VariableTree)tree.getLeaf();
                TreePath type = new TreePath(tree, left ? vd.getType() : vd.getInitializer());
                return cInfo.getTrees().getTypeMirror(type);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private TypeMirror cast(int caretOffset) {
        try {
            if (initParsing()) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement(request.getInsertText(), sourcePositions);
                if (errChecker.containsErrors(stmt))
                    return null;
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset, sourcePositions[0]);
                TreePath tree = Utilities.getPathElementOfKind(EnumSet.of(Tree.Kind.ASSIGNMENT, Tree.Kind.VARIABLE), path);
                if (tree == null)
                    return null;
                tu.attributeTree(stmt, scope);
                if (tree.getLeaf().getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree as = (AssignmentTree)tree.getLeaf();
                    TypeMirror left = cInfo.getTrees().getTypeMirror(new TreePath(tree, as.getVariable()));
                    TreePath exp = new TreePath(tree, as.getExpression());
                    if (exp.getLeaf() instanceof TypeCastTree)
                        exp = new TreePath(exp, ((TypeCastTree)exp.getLeaf()).getExpression());
                    TypeMirror right = cInfo.getTrees().getTypeMirror(exp);
                    if (right == null || left == null)
                        return null;
                    if (cInfo.getTypes().isAssignable(right, left))
                        return null;
                    return left;
                }
                VariableTree vd = (VariableTree)tree.getLeaf();
                TypeMirror left = cInfo.getTrees().getTypeMirror(new TreePath(tree, vd.getType()));
                TreePath exp = new TreePath(tree, vd.getInitializer());
                if (exp.getLeaf() instanceof TypeCastTree)
                    exp = new TreePath(exp, ((TypeCastTree)exp.getLeaf()).getExpression());
                TypeMirror right = cInfo.getTrees().getTypeMirror(exp);
                if (right == null)
                    return null;
                if (left == null)
                    return null;
                if (right.getKind() != TypeKind.ERROR && cInfo.getTypes().isAssignable(right, left))
                    return null;
                return left;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private String newVarName(int caretOffset) {
        try {
            if (initParsing()) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement(request.getInsertText(), sourcePositions);
                if (errChecker.containsErrors(stmt))
                    return null;
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset, sourcePositions[0]);
                TreePath decl = Utilities.getPathElementOfKind(Tree.Kind.VARIABLE, path);
                if (decl != null) {
                    Scope s = tu.attributeTreeTo(stmt, scope, decl.getLeaf());
                    TypeMirror type = cInfo.getTrees().getTypeMirror(decl);
                    boolean isConst = ((VariableTree)decl.getLeaf()).getModifiers().getFlags().containsAll(EnumSet.of(Modifier.FINAL, Modifier.STATIC));
                    final Name varName = ((VariableTree)decl.getLeaf()).getName();
                    ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                        public boolean accept(Element e, TypeMirror t) {
                            switch(e.getKind()) {
                                case EXCEPTION_PARAMETER:
                                case LOCAL_VARIABLE:
                                case PARAMETER:
                                    return varName != e.getSimpleName();
                                default:
                                    return false;
                            }
                        }
                    };
                    Iterator<String> names = Utilities.varNamesSuggestions(type, null, cInfo.getTypes(), cInfo.getElements(), cInfo.getElementUtilities().getLocalVars(s, acceptor), isConst).iterator();
                    if (names.hasNext())
                        return names.next();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private TypeMirror uncaughtExceptionType(int caretOffset) {
        try {
            if (initParsing()) {
                SourcePositions[] sourcePositions = new SourcePositions[1];
                TreeUtilities tu = cInfo.getTreeUtilities();
                StatementTree stmt = tu.parseStatement(request.getInsertText(), sourcePositions);                
                if (errChecker.containsErrors(stmt))
                    return null;
                TreePath path = tu.pathFor(new TreePath(treePath, stmt), caretOffset, sourcePositions[0]);
                path = Utilities.getPathElementOfKind(Tree.Kind.TRY, path);
                if (path != null && ((TryTree)path.getLeaf()).getBlock() != null) {
                    tu.attributeTree(stmt, scope);
                    Iterator<? extends TypeMirror> excs = tu.getUncaughtExceptions(new TreePath(path, ((TryTree)path.getLeaf()).getBlock())).iterator();
                    if (excs.hasNext())
                        return excs.next();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private boolean containsDeclaredType(TypeMirror type) {
        switch(type.getKind()) {
        case ARRAY:
            return containsDeclaredType(((ArrayType)type).getComponentType());
        case DECLARED:
            return true;
        default:
            return false;
        }
    }
    
    private synchronized boolean initParsing() {
        if (cInfo == null && initTask == null) {
            JTextComponent c = request.getComponent();
            final int caretOffset = c.getCaret().getDot();
            JavaSource js = JavaSource.forDocument(c.getDocument());
            if (js != null) {
                try {
                    initTask = js.runWhenScanFinished(new Task<CompilationController>() {

                        public void run(final CompilationController controller) throws IOException {
                            controller.toPhase(JavaSource.Phase.RESOLVED);
                            synchronized(JavaCodeTemplateProcessor.this) {
                                cInfo = controller;
                            }
                            final TreeUtilities tu = cInfo.getTreeUtilities();
                            treePath = tu.pathFor(caretOffset);
                            scope = tu.scopeFor(caretOffset);
                            enclClass = scope.getEnclosingClass();
                            final boolean isStatic = enclClass != null ? tu.isStaticContext(scope) : false;
                            if (enclClass == null) {
                                CompilationUnitTree cut = treePath.getCompilationUnit();
                                Iterator<? extends Tree> it = cut.getTypeDecls().iterator();
                                if (it.hasNext())
                                    enclClass = (TypeElement)cInfo.getTrees().getElement(TreePath.getPath(cut, it.next()));
                            }
                            final Trees trees = controller.getTrees();
                            final SourcePositions sp = trees.getSourcePositions();
                            final Collection<? extends Element> illegalForwardRefs = Utilities.getForwardReferences(treePath, caretOffset, sp, trees);;
                            final ExecutableElement method = scope.getEnclosingMethod();
                            ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
                                public boolean accept(Element e, TypeMirror t) {
                                    switch (e.getKind()) {
                                    case LOCAL_VARIABLE:
                                        if (isStatic && e.getSimpleName().contentEquals("this") || e.getSimpleName().contentEquals("super")) //NOI18N
                                            return false;
                                    case EXCEPTION_PARAMETER:
                                    case PARAMETER:
                                        return (method == e.getEnclosingElement() || e.getModifiers().contains(Modifier.FINAL)) &&
                                                !illegalForwardRefs.contains(e);
                                    case FIELD:
                                        if (illegalForwardRefs.contains(e))
                                            return false;
                                    default:
                                        return (!isStatic || e.getModifiers().contains(Modifier.STATIC)) && tu.isAccessible(scope, e, t);
                                    }
                                }
                            };
                            locals = cInfo.getElementUtilities().getLocalMembersAndVars(scope, acceptor);
                        }
                    },false);
                    if (!initTask.isDone())
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaCodeTemplateFilter.class, "JCT-scanning-in-progress")); //NOI18N
                } catch(IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        return cInfo != null;
    }

    public static final class Factory implements CodeTemplateProcessorFactory {
        
        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new JavaCodeTemplateProcessor(request); 
        }        
    }
    
    
    private static class ErrChecker extends TreeScanner<Void, Void> {
        private boolean containsErrors;
        
        public boolean containsErrors(Tree tree) {
            containsErrors = false;
            scan(tree, null);
            return containsErrors;
        }
        
        public Void visitErroneous(ErroneousTree node, Void p) {
            containsErrors = true;
            return null;
        }
        
        public Void scan(Tree node, Void p) {
            if (containsErrors)
                return null;
            return super.scan(node, p);
        }
    }
}
