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
package org.netbeans.modules.refactoring.php.findusages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Dispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.refactoring.php.findusages.AttributedNodes.AttributedElement.Kind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda, Radek Matous
 */
public class AttributedNodes extends DefaultVisitor {

    public DefinitionScope global;
    private Stack<DefinitionScope> scopes = new Stack<DefinitionScope>();
    private Map<ASTNode, AttributedElement> node2Element = new HashMap<ASTNode, AttributedElement>();
    private int offset;
    private CompilationInfo info;
    private Stack<ASTNode> nodes = new Stack<ASTNode>();

    public AttributedNodes(CompilationInfo info) {
        this(info, -1);
    }

    public AttributedNodes(CompilationInfo info, int o) {
        this.offset = o;
        this.info = info;
        scopes.push(global = new DefinitionScope());
    }

    public Map<ASTNode, AttributedElement> findDirectSubclasses(AttributedElement el) {
        //TODO: copy/paste maxi mess
        Map<AttributedElement, List<ASTNode>> forDuplCheck = new HashMap<AttributedElement, List<ASTNode>>();
        Map<ASTNode, AttributedElement> results = new HashMap<ASTNode, AttributedElement>();
        assert el != null;
        for (Entry<ASTNode, AttributedElement> entry : node2Element.entrySet()) {
            AttributedElement value = entry.getValue();
            if (value != null) {
                if (!(entry.getKey() instanceof ClassDeclaration)) {
                    continue;
                }
                if (!(value instanceof ClassElement)) {
                    continue;
                } else {
                    ClassElement superClass = ((ClassElement) value).getSuperClass();
                    if (superClass == null || !superClass.equals(el)) {
                        continue;
                    }
                }
                //cp/paste
                boolean overlap = false;
                ASTNode node = entry.getKey();
                List<ASTNode> ls = forDuplCheck.get(value);
                if (ls == null) {
                    ls = new ArrayList<ASTNode>();
                }
                OffsetRange newOne = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                for (Iterator<ASTNode> it = ls.iterator(); it.hasNext();) {
                    ASTNode aSTNode = it.next();
                    OffsetRange oldOne = new OffsetRange(aSTNode.getStartOffset(), aSTNode.getEndOffset());
                    if (newOne.overlaps(oldOne) || oldOne.overlaps(newOne) || newOne.containsInclusive(oldOne.getStart()) || oldOne.containsInclusive(newOne.getStart())) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    ls.add(node);
                    forDuplCheck.put(value, ls);
                    results.put(node, value);
                }
            }
        }

        return results;
    }

    public Map<ASTNode, AttributedElement> findUsages(AttributedElement el) {
        //TODO: maxi mess - deserves to be be polished
        Map<AttributedElement, List<ASTNode>> forDuplCheck = new HashMap<AttributedElement, List<ASTNode>>();
        Map<ASTNode, AttributedElement> results = new HashMap<ASTNode, AttributedElement>();
        assert el != null;
        for (Entry<ASTNode, AttributedElement> entry : node2Element.entrySet()) {
            AttributedElement value = entry.getValue();
            if (value != null && (el.getName().equals(value.getName()) && el.getKind().equals(value.getKind()))) {
                boolean same = true;
                List<Union2<ASTNode, IndexedElement>> writes = value.getWrites();
                List<Union2<ASTNode, IndexedElement>> writes2 = el.getWrites();
                Map<FileObject, Integer> idxs = new HashMap<FileObject, Integer>();
                if (writes2.size() != 0 && writes.size() != 0) {
                    for (Union2<ASTNode, IndexedElement> union : writes2) {
                        if (union.hasSecond()) {
                            IndexedElement second = union.second();
                            idxs.put(second.getFileObject(), second.getOffset());
                        }
                    }
                    if (idxs.size() > 0) {
                        same = false;
                    }
                    for (Union2<ASTNode, IndexedElement> union : writes) {
                        if (union.hasSecond()) {
                            IndexedElement second = union.second();
                            Integer offset = idxs.get(second.getFileObject());
                            if (offset != null) {
                                if (offset.equals(second.getOffset())) {
                                    same = true;
                                    break;
                                }
                            }
                        } else {
                            if (el.getKind().equals(Kind.VARIABLE)) {
                                Types elTypes = el.getTypes();
                                Types valueTypes = value.getTypes();
                                int s = Math.min(elTypes.size(), valueTypes.size());
                                for (int i = 0; i < s; i++) {
                                    AttributedType elT = elTypes.getType(i);
                                    AttributedType vT = valueTypes.getType(i);
                                    boolean elIsFuncType = (elT != null) ? (elT instanceof FunctionType) : false;
                                    boolean vIsFuncType = (vT != null) ? (vT instanceof FunctionType) : false;
                                    if (elIsFuncType != vIsFuncType) {
                                        same = false;
                                    } else {
                                        if (elIsFuncType) {
                                            FunctionType elFT =(FunctionType) elT;
                                            FunctionType vFT =(FunctionType) vT;
                                            if (!elFT.getElement().getName().equals(vFT.getElement().getName())) {                                                
                                                same = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                boolean elIsClassMember = el instanceof ClassMemberElement;
                boolean valIsClassMember = value instanceof ClassMemberElement;                
                if (elIsClassMember != valIsClassMember) {
                    same = false;
                }
                if (same /*&& !s.contains(value)*/) {
                    boolean overlap = false;
                    ASTNode node = entry.getKey();
                    List<ASTNode> ls = forDuplCheck.get(value);
                    if (ls == null) {
                        ls = new ArrayList<ASTNode>();
                    }
                    OffsetRange newOne = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                    for (Iterator<ASTNode> it = ls.iterator(); it.hasNext();) {
                        ASTNode aSTNode = it.next();
                        OffsetRange oldOne = new OffsetRange(aSTNode.getStartOffset(), aSTNode.getEndOffset());
                        if (newOne.overlaps(oldOne) || oldOne.overlaps(newOne) || newOne.containsInclusive(oldOne.getStart()) || oldOne.containsInclusive(newOne.getStart())) {
                            overlap = true;
                            break;
                        }
                    }
                    if (!overlap) {
                        ls.add(node);
                        forDuplCheck.put(value, ls);
                        results.put(node, value);
                    }
                }


            }
        }
        return results;
    }

    @Override
    public void scan(ASTNode node) {
        if (node == null) {
            return;
        }

        if ((offset != (-1) && offset <= node.getStartOffset())) {
            throw new Stop();
        }

        nodes.push(node);

        super.scan(node);

        nodes.pop();

        if ((offset != (-1) && offset <= node.getEndOffset())) {
            throw new Stop();
        }
    }

    @Override
    public void visit(Program program) {
        //functions defined on top-level of the current file are visible before declared:
        performEnterPass(global, program.getStatements());
        enterInclude(getInfo().getFileObject());
        super.visit(program);
    }

    @Override
    public void visit(Assignment node) {
        final VariableBase vb = node.getLeftHandSide();

        if (vb instanceof Variable) {
            AttributedType at = null;
            Expression rightSideExpression = node.getRightHandSide();

            if (rightSideExpression instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
                Expression className = classInstanceCreation.getClassName().getName();

                if (className instanceof Identifier) {
                    Identifier identifier = (Identifier) className;
                    ClassElement ce = (ClassElement) lookup(identifier.getName(), Kind.CLASS);

                    if (ce != null) {
                        at = new ClassType(ce);
                    }
                }
            } else if (rightSideExpression instanceof FieldAccess) {
                FieldAccess access = (FieldAccess) rightSideExpression;
                Variable field = access.getField();
                node2Element.put(vb, scopes.peek().enterWrite(extractVariableName(field), Kind.VARIABLE, access, at));
            }

            String name = extractVariableName((Variable) vb);

            if (name != null) {
                node2Element.put(vb, scopes.peek().enterWrite(name, Kind.VARIABLE, vb, at));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        String name = node.getFunctionName().getName();
        FunctionElement fc = (FunctionElement) global.enterWrite(name, Kind.FUNC, node);

        DefinitionScope top = scopes.peek();

        if (!node2Element.containsKey(node)) {
            assert !top.classScope;
            node2Element.put(node, fc);
        }

        scopes.push(fc.enclosedElements);

        if (top.classScope) {
            assert top.thisVar != null;
            scopes.peek().enter(top.thisVar.name, top.thisVar.getKind(), top.thisVar);
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(FormalParameter node) {
        if (node.getParameterName() instanceof Variable) {
            String name = extractVariableName((Variable) node.getParameterName());

            if (name != null) {
                scopes.peek().enterWrite(name, Kind.VARIABLE, node);
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(Variable node) {
        if (!node2Element.containsKey(node)) {
            String name = extractVariableName(node);

            if (name != null) {
                node2Element.put(node, lookup(name, Kind.VARIABLE));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionInvocation node) {
        Expression exp = node.getFunctionName().getName();
        String name = null;

        if (exp instanceof Identifier) {
            name = ((Identifier) exp).getName();
        }

        if (exp instanceof Variable) {
            //XXX:
            Expression n = ((Variable) exp).getName();

            if (n instanceof Identifier) {
                name = ((Identifier) n).getName();
            }
        }

        if (name != null) {
            AttributedElement thisEl = null;
            ASTNode n = nodes.pop();
            ASTNode parent = nodes.peek();

            nodes.push(n);

            if (parent instanceof MethodInvocation) {
                ClassElement ce = resolveTypeSimple((Dispatch) parent);

                if (ce != null) {
                    thisEl = ce.lookup(name, Kind.FUNC);
                }
            } else {
                if (parent instanceof StaticMethodInvocation) {
                    StaticMethodInvocation smi = (StaticMethodInvocation) parent;
                    Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS, smi.getClassName().getName());
                    if (!nn.isEmpty()) {
                        boolean par = false;
                        if ("parent".equals(smi.getClassName().getName())) {
                            par = true;
                        }
                        String contextClassName = (par) ? getContextSuperClassName() : getContextClassName();
                        for (AttributedElement ell : nn) {
                            ClassElement ce = (ClassElement) ell;
                            if (ce != null) {
                                if (contextClassName == null || contextClassName.equals(ce.getName())) {
                                    thisEl = ce.lookup(name, Kind.FUNC);
                                    node2Element.put(smi.getClassName(), ce);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    thisEl = lookup(name, Kind.FUNC);
                }
            }

            node2Element.put(node, thisEl);

            if ("define".equals(name) && node.getParameters().size() == 2) {
                Expression d = node.getParameters().get(0);

                if (d instanceof Scalar && ((Scalar) d).getScalarType() == Type.STRING) {
                    String value = ((Scalar) d).getStringValue();

                    if (RefactoringUtils.isQuoted(value)) {
                        node2Element.put(d, global.enterWrite(RefactoringUtils.dequote(value), Kind.CONST, d));
                    }
                }
            }
        }

        if (node2Element.containsKey(node)) {
            //super.visit(node);
            scan(node.getParameters());
        } else {
            super.visit(node);
        }
    }

    @Override
    public void visit(ClassDeclaration node) {
        String name = node.getName().getName();
        ClassElement ce = (ClassElement) global.enterWrite(name, Kind.CLASS, node);

        node2Element.put(node, ce);

        if (node.getSuperClass() != null) {
            ce.superClass = (ClassElement) lookup(node.getSuperClass().getName(), Kind.CLASS);
        }

        scopes.push(ce.enclosedElements);

        if (node.getBody() != null) {
            performEnterPass(ce.enclosedElements, node.getBody().getStatements());
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        Expression name = node.getClassName().getName();

        if (name instanceof Identifier) {
            node2Element.put(node, lookup(((Identifier) name).getName(), Kind.CLASS));
        }

        super.visit(node);
    }

    @Override
    public void visit(Include node) {
        FileObject toInclude = RefactoringUtils.resolveInclude(getInfo(), node);

        if (toInclude != null) {
            enterInclude(toInclude);
        }

        super.visit(node);
    }

    @Override
    public void visit(GlobalStatement node) {
        for (Variable v : node.getVariables()) {
            String name = extractVariableName(v);

            if (name != null) {
                enterGlobalVariable(name);
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(Scalar scalar) {
        if (scalar.getScalarType() == Type.STRING && !RefactoringUtils.isQuoted(scalar.getStringValue())) {
            AttributedElement def = global.lookup(scalar.getStringValue(), Kind.CONST);

            node2Element.put(scalar, def);
        }

        super.visit(scalar);
    }

    @Override
    public void visit(FieldAccess node) {
        scan(node.getDispatcher());

        ClassElement ce = resolveTypeSimple(node);
        String name = extractVariableName(node.getField());

        if (ce != null && name != null) {
            AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
            node2Element.put(node, thisEl);
            node2Element.put(node.getField(), thisEl);
        }

        scan(node.getField());
    }

    @Override
    public void visit(StaticConstantAccess node) {
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,
                node.getClassName().getName());//NOI18N

        if (!nn.isEmpty()) {
            ClassElement ce = (ClassElement) nn.iterator().next();
            if (ce != null) {
                String name = node.getConstant().getName();
                AttributedElement thisEl = ce.lookup(name, Kind.CONST);
                node2Element.put(node.getClassName(), ce);
                node2Element.put(node, thisEl);
                node2Element.put(node.getConstant(), thisEl);
            }

        }
        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,
                node.getClassName().getName());
        if (!nn.isEmpty()) {
            boolean parent = false;
            if ("parent".equals(node.getClassName().getName())) {
                parent = true;
            }
            String contextClassName = (parent) ? getContextSuperClassName() : getContextClassName();
            for (AttributedElement ell : nn) {
                ClassElement ce = (ClassElement) ell;
                if (ce != null) {
                    String name = extractVariableName(node.getField());
                    AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
                    if (thisEl != null) {
                        if (contextClassName == null || contextClassName.equals(ce.getName())) {
                            node2Element.put(node.getClassName(), ce);
                            node2Element.put(node, thisEl);
                            node2Element.put(node.getField(), thisEl);
                            break;
                        }
                    //break;
                    }
                }
            }
        }
        super.visit(node);
    }

    private AttributedElement enterGlobalVariable(String name) {
        AttributedElement g = global.lookup(name, Kind.VARIABLE);

        if (g == null) {
            //XXX: untested:
            g = global.enterWrite(name, Kind.VARIABLE, (ASTNode) null);
        }

        scopes.peek().enter(name, Kind.VARIABLE, g);

        return g;
    }

    @Override
    public void visit(ArrayAccess node) {
        if (node.getName() instanceof Variable && node.getIndex() instanceof Scalar) {
            String variableName = extractVariableName((Variable) node.getName());

            if ("GLOBALS".equals(variableName)) {
                Scalar v = (Scalar) node.getIndex();

                if (v.getScalarType() == Type.STRING) {
                    String value = v.getStringValue();
                    if (RefactoringUtils.isQuoted(value)) {
                        node2Element.put(node, enterGlobalVariable(RefactoringUtils.dequote(value)));
                    }
                }
            }
        }

        super.visit(node);
    }

    private String getContextClassName() {
        String contextClassName = null;
        Enumeration<DefinitionScope> elements = scopes.elements();
        while (elements.hasMoreElements()) {
            DefinitionScope nextElement = elements.nextElement();
            if (nextElement.enclosingClass != null) {
                contextClassName = nextElement.enclosingClass.getName();
            }
        }
        return contextClassName;
    }

    private String getContextSuperClassName() {
        String contextClassName = null;
        Enumeration<DefinitionScope> elements = scopes.elements();
        while (elements.hasMoreElements()) {
            DefinitionScope nextElement = elements.nextElement();
            if (nextElement.enclosingClass != null && nextElement.enclosingClass.superClass != null) {
                contextClassName = nextElement.enclosingClass.superClass.getName();
            }
        }
        return contextClassName;
    }

    private CompilationInfo getInfo() {
        return info;
    }

    private AttributedElement lookup(String name, Kind k) {
        DefinitionScope ds = scopes.peek();
        AttributedElement e;

        switch (k) {
            case FUNC:
            case CLASS:
                e = global.lookup(name, k);
                break;
            default:
                e = ds.lookup(name, k);
                break;
        }

        if (e != null) {
            return e;
        }

        switch (k) {
            case FUNC:
            case CLASS:
                return global.enterWrite(name, k, (ASTNode) null);
            default:
                return ds.enterWrite(name, k, (ASTNode) null);
        }
    }

    public Collection<AttributedElement> getGlobalElements(Kind k) {
        return global.getElements(k);
    }

    public Collection<AttributedElement> getNamedGlobalElements(Kind k, String... filterNames) {
        Map<String, AttributedElement> name2El = global.name2Writes.get(k);

        Collection<AttributedElement> elements = getGlobalElements(k);
        List<AttributedElement> retval = new ArrayList<AttributedElement>();
        for (String fName : filterNames) {
            if (Kind.CLASS.equals(k) && fName.equals("self")) {//NOI18N

                retval.addAll(name2El.values());
            } else if (Kind.CLASS.equals(k) && fName.equals("parent")) {//NOI18N

                Collection<AttributedElement> values = name2El.values();
                for (AttributedElement ael : values) {
                    if (ael instanceof ClassElement) {
                        ClassElement ce = (ClassElement) ael;
                        ClassElement superClass = ce.getSuperClass();
                        if (superClass != null) {
                            retval.add(superClass);
                        }
                    }
                }
            } else {
                AttributedElement el = name2El.get(fName);
                if (el != null) {
                    retval.add(el);
                }
            }
        }
        return retval;
    }

    public AttributedElement getElement(ASTNode n) {
        return node2Element.get(n);
    }
    private Collection<IndexedElement> name2ElementCache;

    public void enterInclude(FileObject file) {
        if (file == null) {
            return;
        }

        if (name2ElementCache == null) {
            Index i = getInfo().getIndex(PhpSourcePath.MIME_TYPE);
            PHPIndex index = PHPIndex.get(i);
            name2ElementCache = new LinkedList<IndexedElement>();
            name2ElementCache.addAll(index.getFunctions(null, "", NameKind.PREFIX));
            name2ElementCache.addAll(index.getConstants(null, "", NameKind.PREFIX));
            name2ElementCache.addAll(index.getClasses(null, "", NameKind.PREFIX));
        }

        Set<FileObject> files = new HashSet<FileObject>();

        files.add(file);

        Index i = getInfo().getIndex(PhpSourcePath.MIME_TYPE);
        PHPIndex index = PHPIndex.get(i);

        try {
            for (String s : index.getAllIncludes(file.getURL().getPath())) {//XXX: getPath?

                files.add(FileUtil.toFileObject(FileUtil.normalizeFile(new File(s))));//TODO: normalization will slow down things - try to do better

            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        files.remove(null);

        for (IndexedElement f : name2ElementCache) {
            if (files.contains(f.getFileObject())) {
                Kind k = null;

                if (f instanceof IndexedFunction) {
                    k = Kind.FUNC;
                }

                if (f instanceof IndexedConstant) {
                    k = Kind.CONST;
                }

                if (f instanceof IndexedClass) {
                    ClassElement ce = (ClassElement) global.enterWrite(f.getName(), Kind.CLASS, f);

                    if (!ce.isInitialized()) {
                        //HACK: should create correct hierarchy, not use All* methods:
                        for (IndexedFunction m : index.getAllMethods(null, f.getName(), "", NameKind.PREFIX, PHPIndex.ANY_ATTR)) {
                            ce.enclosedElements.enterWrite(m.getName(), Kind.FUNC, m);
                        }
                        for (IndexedConstant m : index.getAllProperties(null, f.getName(), "", NameKind.PREFIX, PHPIndex.ANY_ATTR)) {
                            String name = m.getName();
                            name = (name.startsWith("$")) ? name.substring(1) : name;
                            ce.enclosedElements.enterWrite(name, Kind.VARIABLE, m);
                        }
                        for (IndexedConstant m : index.getClassConstants(null, f.getName(), "", NameKind.PREFIX)) {
                            String name = m.getName();
                            name = (name.startsWith("$")) ? name.substring(1) : name;
                            ce.enclosedElements.enterWrite(name, Kind.CONST, m);
                        }


                        ce.initialized();
                    }
                }

                if (k != null) {
                    global.enterWrite(f.getName(), k, f);
                }
            }
        }
    }

    private void performEnterPass(DefinitionScope scope, Collection<? extends ASTNode> nodes) {
        for (ASTNode n : nodes) {
            if (n instanceof MethodDeclaration) {
                FunctionDeclaration nn = ((MethodDeclaration) n).getFunction();
                String name = nn.getFunctionName().getName();
                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
                node2Element.put(nn, scope.enterWrite(name, Kind.FUNC, n));
                continue;
            }

            if (n instanceof FunctionDeclaration) {
                String name = ((FunctionDeclaration) n).getFunctionName().getName();

                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
            }

            if (n instanceof FieldsDeclaration) {
                for (SingleFieldDeclaration f : ((FieldsDeclaration) n).getFields()) {
                    String name = extractVariableName(f.getName());

                    if (name != null) {
                        node2Element.put(n, scope.enterWrite(name, Kind.VARIABLE, n));
                    }
                }
            }

            if (n instanceof ClassDeclaration) {
                ClassDeclaration node = (ClassDeclaration) n;
                String name = node.getName().getName();
                ClassElement ce = (ClassElement) global.enterWrite(name, Kind.CLASS, node);
                node2Element.put(node, ce);
                if (node.getSuperClass() != null) {
                    ce.superClass = (ClassElement) lookup(node.getSuperClass().getName(), Kind.CLASS);
                    node2Element.put(node.getSuperClass(), ce.superClass);
                }
                if (node.getBody() != null) {
                    performEnterPass(ce.enclosedElements, node.getBody().getStatements());
                }
            }

            if (n instanceof ClassConstantDeclaration) {
                List<Identifier> constNames = ((ClassConstantDeclaration) n).getNames();
                for (Identifier id : constNames) {
                    node2Element.put(n, scope.enterWrite(id.getName(), Kind.CONST, n));
                }
            }

        }
    }
    private static Map<CompilationInfo, AttributedNodes> info2Attr = new WeakHashMap<CompilationInfo, AttributedNodes>();

    public static AttributedNodes semiAttribute(CompilationInfo info) {
        AttributedNodes a = info2Attr.get(info);

        if (a == null) {
            long startTime = System.currentTimeMillis();

            a = new AttributedNodes(info);

            a.scan(RefactoringUtils.getRoot(info));

            a.info = null;

            info2Attr.put(info, a);

            long endTime = System.currentTimeMillis();

            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global instance", new Object[]{info.getFileObject(), a});
            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global time", new Object[]{info.getFileObject(), (endTime - startTime)});
        }

        return a;
    }

    public static AttributedNodes semiAttribute(CompilationInfo info, int stopOffset) {
        AttributedNodes a = new AttributedNodes(info, stopOffset);

        try {
            a.scan(RefactoringUtils.getRoot(info));
        } catch (Stop s) {
        }

        return a;
    }

    private static String name(ASTNode n) {
        if (n instanceof Identifier) {
            return ((Identifier) n).getName();
        }

        return null;
    }

    public static String extractVariableName(Variable var) {
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            return id.getName();
        }

        return null;
    }

    private ClassElement resolveTypeSimple(Dispatch node) {
        ClassElement ce = null;
        AttributedElement el = node2Element.get(node.getDispatcher());

        if (el != null) {
            AttributedType type = el.writesTypes.get(el.getWrites().size() - 1);

            if (type instanceof ClassType) {
                ce = ((ClassType) type).getElement();
            }
        }

        return ce;
    }

    public Collection<AttributedElement> getFunctions() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getFunctions();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getConstants() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getConstants();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getGlobalVariables() {
        Collection<AttributedElement> retval = null;
        if (global != null) {
            retval = global.getVariables();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<ClassElement> getClasses() {
        Collection<ClassElement> retval = null;
        if (global != null) {
            retval = global.getClasses();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public boolean hasGlobalVisibility(AttributedElement elem) {
        if (elem.isClassMember()) {
            ClassMemberElement cme = (ClassMemberElement) elem;
            boolean isGlobal = (cme.getModifier() == -1 || !cme.isPrivate()) && hasGlobalVisibility(cme.getClassElement());
            return isGlobal;
        }
        return (global != null) ? global.getElements(elem.getKind()).contains(elem) : false;
    }

    public static class AttributedElement {

        private List<Union2<ASTNode, IndexedElement>> writes; //aka declarations

        private List<AttributedType> writesTypes;
        private String name;
        private Kind k;

        public AttributedElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            this(n, name, k, null);
        }

        public AttributedElement(Union2<ASTNode, IndexedElement> n, String name, Kind k, AttributedType type) {
            this.writes = new LinkedList<Union2<ASTNode, IndexedElement>>();
            this.writesTypes = new LinkedList<AttributedType>();
            this.writes.add(n);
            this.writesTypes.add(type);
            this.name = name;
            this.k = k;
        }

        public boolean isClassMember() {
            return false;
        }

        public List<Union2<ASTNode, IndexedElement>> getWrites() {
            return writes;
        }

        public Kind getKind() {
            return k;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AttributedElement)) {
                return false;
            }
            AttributedElement element = (AttributedElement) obj;
            return this.name.equals(element.name) && this.k.equals(element.k);
        }

        void addWrite(Union2<ASTNode, IndexedElement> node, AttributedType type) {
            writes.add(node);
            writesTypes.add(type);
        }

        Types getTypes() {
            return new Types(this);
        }

        public enum Kind {

            VARIABLE, FUNC, CLASS, CONST;
        }
    }

    private static class Types {

        private AttributedElement el;

        Types(AttributedElement el) {
            this.el = el;
        }

        int size() {
            return el.writesTypes.size();
        }

        AttributedType getType(int idx) {
            return el.writesTypes.get(idx);
        }
    }

    public static class ClassMemberElement extends AttributedElement {

        private ClassElement classElement;
        int modifier = -1;

        public ClassMemberElement(Union2<ASTNode, IndexedElement> n, ClassElement classElement, String name, Kind k) {
            super(n, name, k);
            this.classElement = classElement;
            setModifiers(n, name);
            assert classElement != null;
        }

        public String getClassName() {
            return getClassElement().getName();
        }

        //see BodyDeclaration.Modifier
        public int getModifier() {
            return modifier;
        }

        public boolean isPublic() {
            return BodyDeclaration.Modifier.isPublic(getModifier());
        }

        public boolean isPrivate() {
            return BodyDeclaration.Modifier.isPrivate(getModifier());
        }

        public boolean isProtected() {
            return BodyDeclaration.Modifier.isProtected(getModifier());
        }

        public boolean isStatic() {
            return BodyDeclaration.Modifier.isStatic(getModifier());
        }

        public ClassElement getClassElement() {
            return classElement;
        }

        @Override
        public boolean isClassMember() {
            return true;
        }

        public ClassMemberKind getClassMemberKind() {
            ClassMemberKind retval = null;
            switch (getKind()) {
                case CONST:
                    retval = ClassMemberKind.CONST;
                    break;
                case FUNC:
                    retval = ClassMemberKind.METHOD;
                    break;
                case VARIABLE:
                    retval = ClassMemberKind.FIELD;
                    break;
                default:
                    assert false;

            }
            assert retval != null;
            return retval;
        }

        private void setModifiers(Union2<ASTNode, IndexedElement> n, String name) {
            if (n.hasFirst()) {
                ASTNode node = n.first();
                if (node instanceof BodyDeclaration) {
                    modifier = ((BodyDeclaration) node).getModifier();
                } else if (name.equals("this")) {
                    //NOI18N
                    assert false;
                } else if (node instanceof ClassConstantDeclaration) {
                    modifier |= BodyDeclaration.Modifier.PUBLIC;
                } else {
                    assert false : name;
                }
            } else if (n.hasSecond()) {
                IndexedElement index = n.second();
                if (index != null) {
                    Set<Modifier> modifiers = index.getModifiers();
                    for (Modifier mod : modifiers) {
                        switch (mod) {
                            case PRIVATE:
                                modifier |= BodyDeclaration.Modifier.PRIVATE;
                                break;
                            case PROTECTED:
                                modifier |= BodyDeclaration.Modifier.PROTECTED;
                                break;
                            case PUBLIC:
                                modifier |= BodyDeclaration.Modifier.PUBLIC;
                                break;
                            case STATIC:
                                modifier |= BodyDeclaration.Modifier.STATIC;
                                break;
                        }
                    }
                }
            }
        }

        public enum ClassMemberKind {

            FIELD, METHOD, CONST;
        }
    }

    public static class ClassElement extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private ClassElement superClass;
        private boolean initialized;

        public ClassElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            AttributedElement el = enclosedElements.lookup(name, k);

            if (el != null) {
                return el;
            }

            if (superClass != null) {
                return superClass.lookup(name, k);
            }

            return null;
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<AttributedElement>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getMethods() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getFields() {
            Collection<AttributedElement> elems = getElements(Kind.VARIABLE);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (AttributedElement elm : elems) {
                if (!elm.getName().equals("this")) {
                    retval.add(elm);
                }
            }
            return retval;
        }

        public ClassElement getSuperClass() {
            return superClass;
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));

            if (superClass != null) {
                superClass.getElements0(elements, k);
            }
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }
    }

    public static class FunctionElement extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private boolean initialized;

        public FunctionElement(Union2<ASTNode, IndexedElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            return enclosedElements.lookup(name, k);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<AttributedElement>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<AttributedElement>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }
    }

    public static class DefinitionScope {

        private final Map<Kind, Map<String, AttributedElement>> name2Writes = new HashMap<Kind, Map<String, AttributedElement>>();
//        private final Map<AttributedElement, ASTNode> reads = new HashMap<AttributedElement, ASTNode>();
        private boolean classScope;
        private boolean functionScope;
        private AttributedElement thisVar;
        private ClassElement enclosingClass;
        private FunctionElement enclosingFunction;

        public DefinitionScope() {
        }

        public DefinitionScope(ClassElement enclosingClass) {
            this.enclosingClass = enclosingClass;
            this.classScope = enclosingClass != null;


            if (classScope) {
                thisVar = enterWrite("this", Kind.VARIABLE, (ASTNode) null, new ClassType(enclosingClass));
            }
        }

        public DefinitionScope(FunctionElement enclosingFunction) {
            this.enclosingFunction = enclosingFunction;
            this.functionScope = enclosingFunction != null;
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node) {
            return enterWrite(name, k, node, null);
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node, AttributedType type) {
            return enterWrite(name, k, Union2.<ASTNode, IndexedElement>createFirst(node), type);
        }

        public AttributedElement enterWrite(String name, Kind k, IndexedElement el) {
            return enterWrite(name, k, Union2.<ASTNode, IndexedElement>createSecond(el), null);
        }

        private AttributedElement enterWrite(String name, Kind k, Union2<ASTNode, IndexedElement> node, AttributedType type) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);

            if (name2El == null) {
                name2Writes.put(k, name2El = new HashMap<String, AttributedElement>());
            }

            AttributedElement el = name2El.get(name);

            if (el == null) {
                if (k == Kind.CLASS) {
                    el = new ClassElement(node, name, k);
                } else {
                    if (classScope && !Arrays.asList(new String[]{"this"}).contains(name)) {
                        switch (k) {
                            case CONST:
                            case FUNC:
                            case VARIABLE:
                                el = new ClassMemberElement(node, enclosingClass, name, k);
                                break;
                            default:
                                assert false;
                        }
                    } else {
                        if (k == Kind.FUNC) {
                            el = new FunctionElement(node, name, k);
                        } else if (k == Kind.VARIABLE) {
                            if (type == null && functionScope && enclosingFunction != null) {
                                type = new FunctionType(enclosingFunction);
                            }
                            el = new AttributedElement(node, name, k, type);
                        } else {
                            el = new AttributedElement(node, name, k, type);
                        }
                    }
                }

                name2El.put(name, el);
            } else {
                el.addWrite(node, type);
            }

            return el;
        }

        public AttributedElement enter(String name, Kind k, AttributedElement el) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);

            if (name2El == null) {
                name2Writes.put(k, name2El = new HashMap<String, AttributedElement>());
            }

            name2El.put(name, el);

            return el;
        }

        public AttributedElement lookup(String name, Kind k) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);

            if (name2El == null) {
                return null;
            }

            return name2El.get(name);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El != null) {
                return Collections.unmodifiableCollection(name2El.values());
            }
            return Collections.emptyList();
        }

        public Collection<AttributedElement> getFunctions() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private Collection<AttributedElement> getConstants() {
            return getElements(Kind.CONST);
        }

        public Collection<ClassElement> getClasses() {
            Collection<ClassElement> retval = new LinkedHashSet<ClassElement>();
            Collection<AttributedElement> elements = getElements(Kind.CLASS);
            for (AttributedElement el : elements) {
                assert el instanceof ClassElement;
                retval.add((ClassElement) el);
            }
            return retval;
        }
    }

    private static final class Stop extends Error {
    }

    public static abstract class AttributedType {

        public abstract String getTypeName();
    }

    public static class ClassType extends AttributedType {

        private ClassElement element;

        public ClassType(ClassElement element) {
            this.element = element;
        }

        public ClassElement getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }

    public static class FunctionType extends AttributedType {

        private FunctionElement element;

        public FunctionType(FunctionElement element) {
            this.element = element;
        }

        public FunctionElement getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }
}
